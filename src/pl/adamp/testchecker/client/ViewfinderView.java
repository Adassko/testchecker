/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.adamp.testchecker.client;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.client.common.Colors;
import pl.adamp.testchecker.client.test.TestEvaluator;
import pl.adamp.testchecker.client.test.TestResult;
import pl.adamp.testchecker.test.TestArea;
import pl.adamp.testchecker.test.TestReader;
import pl.adamp.testchecker.test.TestRow;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.AnswerSheet;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.TestSheet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
	private static final String TAG = ViewfinderView.class.getSimpleName();

	private static final long ANIMATION_DELAY = 30L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 100; // 20;
	private static final int POINT_SIZE = 6;

	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	private float scannerPosition = 0;
	private boolean scannerGoRight = true; 
	private List<ResultPoint> possibleResultPoints;
	private List<ResultPoint> lastPossibleResultPoints;
	private AnswerSheet answerSheet;
	private Paint areaPaint;
	private TestArea testArea;
	private int testAreaValidTime;
	private volatile int foundAnswersPerFrame = 0;
	private final Paint laserPaint;
	private ScanProgressListener progressListener;
	private TestSheet currentTestSheet;

	public interface ScanProgressListener {
		void setProgress(int progress);

		void setSecondaryProgress(int progress);
	}
	
	public void setScanProgressListener(ScanProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Colors.init(this);

		// Initialize these once for performance rather than calling them every time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = getResources();
		
		areaPaint = new Paint();
		areaPaint.setColor(Color.BLUE);
		areaPaint.setAlpha(60);
		areaPaint.setStyle(Style.FILL);
		
		LinearGradient gradient = new LinearGradient(0, 0, 25, 0, Colors.viewfinder_laser, Colors.viewfinder_laser_end,
				TileMode.REPEAT);
		laserPaint = new Paint();
		laserPaint.setDither(true);
		laserPaint.setShader(gradient);
		
		clearView();
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	long lastTimeFound = System.currentTimeMillis();
	long lastTime = System.currentTimeMillis();
	long testResultsValidity = 0;
	TestResult testResult;
	
	@Override
	public void onDraw(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		long timeDelta = currentTime - lastTime; // for animation purposes
		if (timeDelta > 200) timeDelta = 200;
		lastTime = currentTime;
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (cameraManager == null) {
			// designer time draw
			paint.setColor(Colors.maskColor);
			canvas.drawRect(0, 0, width, height * 0.2f, paint);
			canvas.drawRect(0, height * 0.2f, width * 0.2f, height * 0.8f, paint);
			canvas.drawRect(width * 0.8f, height * 0.2f, width, height * 0.8f, paint);
			canvas.drawRect(0, height * 0.8f, width, height, paint);
			return;
		}
		
		if (foundAnswersPerFrame >= 2) {
			lastTimeFound = currentTime;
		} else {
			if (lastTimeFound < currentTime - 2000) {
				cameraManager.autoFocus();
				lastTimeFound = currentTime - 1500;
			}
		}
		foundAnswersPerFrame = 0;
		Rect frame = cameraManager.getFramingRect();
		Rect previewFrame = cameraManager.getFramingRectInPreview();    
		if (frame == null || previewFrame == null) {
			return;
		}

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? Colors.resultColor : Colors.maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(CURRENT_POINT_OPACITY);
			canvas.drawBitmap(resultBitmap, null, frame, paint);
		} else {

			float scaleX = frame.width() / (float) previewFrame.width();
			float scaleY = frame.height() / (float) previewFrame.height();

			List<ResultPoint> currentPossible = possibleResultPoints;
			List<ResultPoint> currentLast = lastPossibleResultPoints;
			int frameLeft = frame.left;
			int frameTop = frame.top;

			if (testArea != null) {
				Path path = new Path();
				areaPaint.setAlpha(12 * testAreaValidTime);
				path.moveTo(testArea.getUpperLeft().getX() * scaleX + frameLeft, testArea.getUpperLeft().getY() * scaleY + frameTop);
				path.lineTo(testArea.getUpperRight().getX() * scaleX + frameLeft, testArea.getUpperRight().getY() * scaleY + frameTop);
				path.lineTo(testArea.getBottomRight().getX() * scaleX + frameLeft, testArea.getBottomRight().getY() * scaleY + frameTop);
				path.lineTo(testArea.getBottomLeft().getX() * scaleX + frameLeft, testArea.getBottomLeft().getY() * scaleY + frameTop);
				path.close();
				canvas.drawPath(path, areaPaint);
				if (--testAreaValidTime <= 0) testArea = null;
			}

			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new ArrayList<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(CURRENT_POINT_OPACITY);
				synchronized (currentPossible) {
					for (ResultPoint point : currentPossible) {
						int pointColor = point.getColor();
						paint.setColor(pointColor == 0 ? Colors.resultPointColor : pointColor);
						canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
								frameTop + (int) (point.getY() * scaleY),
								POINT_SIZE, paint);
					}
				}
			}
			
			if (currentLast != null) {
				paint.setAlpha(CURRENT_POINT_OPACITY / 2);
				synchronized (currentLast) {
					float radius = POINT_SIZE / 2.0f;
					for (ResultPoint point : currentLast) {
						int pointColor = point.getColor();
						paint.setColor(pointColor == 0 ? Colors.resultPointColor : pointColor);
						canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
								frameTop + (int) (point.getY() * scaleY),
								radius, paint);
					}
				}
			}
			
			// Draw a "laser scanner" line through the middle to show decoding is active
			double pxDelta = timeDelta * 0.4; // pixels per millisecond
			if (!scannerGoRight)
				pxDelta = -pxDelta;
			scannerPosition += pxDelta; 
			if (scannerPosition > frame.width()) {
				scannerPosition = frame.width() * 2 - scannerPosition;
				scannerGoRight = false;
			} else if (scannerPosition < 0) {
				scannerPosition = -scannerPosition;
				scannerGoRight = true;
			}

			canvas.drawRect(frame.left + scannerPosition - 12, frame.top + 1, frame.left + scannerPosition + 12, frame.bottom - 1, laserPaint);

			paint.setColor(Colors.answersheet_background);
			canvas.drawRect(5, height - 120, width - 5, height - 5, paint);
			paint.setTextAlign(Align.CENTER);
			
			paint.setTextSize(20);
			int trusted = 0, accepted = 0;
			TestResult a;
			for (QuestionAnswers result : answerSheet.getAcceptedAnswers()) {
				accepted ++;
				if (result.isMetadata()) continue;
				Question testRow = (Question)result.getTestRow();
				int rowId = result.getTestRowId();
				float x = rowId * 20 + 10;
				
				paint.setColor(Colors.answersheet_text);
				canvas.drawText((rowId + 1) + "", x + 7, height - 105, paint);

				if (result.isTrustworthy()) {
					paint.setAlpha(0x80);
					trusted ++;
				} else {
					paint.setAlpha(0x30);
				}
				List<Answer> answers = testRow.getAnswers();
				for (int i = 0; i < answers.size(); i ++) {
					boolean isMarked = result.isMarkedAnswer(i);
					paint.setStyle(isMarked
						? Paint.Style.FILL
						: Paint.Style.STROKE);
					paint.setColor(isMarked == answers.get(i).isCorrect()
							? Colors.answersheet_correct
							: Colors.answersheet_incorrect);
					float y = height - 95 + i * 20;
					canvas.drawRect(x, y, x + 15, y + 15, paint);
				}
			}
			if (progressListener != null) {
				progressListener.setProgress(trusted);
				progressListener.setSecondaryProgress(accepted);
			}

			long studentId = TestEvaluator.getStudentId(answerSheet);
			if (studentId != 0) {
				paint.setTextAlign(Align.RIGHT);
				paint.setTextSize(20);
				paint.setAlpha(0xb0);
				paint.setColor(Color.BLUE);
				canvas.drawText(studentId + "", frame.width(), frame.height() - 5, paint);
			}
			
			if (currentTestSheet != null) {
				testResultsValidity -= timeDelta;
				if (testResultsValidity <= 0) {
					testResult = TestEvaluator.getTestResults(currentTestSheet, answerSheet);
					testResultsValidity = 300;
				}
			}
			
			if (testResult != null) {
				float percent = testResult.getTotalPoints() / (float)testResult.getMaxPoints(); 
				paint.setTextAlign(Align.RIGHT);
				paint.setColor(Color.HSVToColor(new float[] { 120f * percent, 1, 1 }));
				paint.setTextSize(38 + 8 * percent);
				float textAscent = paint.ascent();
				canvas.drawText(Math.round(percent * 100) + "%" , frame.right, frame.top - textAscent, paint);
			}
			
			paint.setStyle(Paint.Style.FILL);
			
			// Request another update at the animation interval, but only repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY,
					frame.left - POINT_SIZE,
					frame.top - POINT_SIZE,
					frame.right + POINT_SIZE,
					frame.bottom + POINT_SIZE);
		}
	}
	
	public void setTestInstance(TestSheet test) {
		currentTestSheet = test;
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live scanning display.
	 *
	 * @param barcode An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleAnswer(QuestionAnswers answer) {
		foundAnswersPerFrame ++;
		if (foundAnswersPerFrame == 1 && !cameraManager.isFocused())
			cameraManager.stopFocus();
	}
	
	public void addArea(TestArea area) {
		testArea = area;
		testAreaValidTime = 6;
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}

	public void clearView() {
		possibleResultPoints = new ArrayList<ResultPoint>(5);
		answerSheet = new AnswerSheet();
		lastPossibleResultPoints = null;
		testArea = null;
	}
	

	public AnswerSheet getAnswerSheet() {
		return this.answerSheet;
	}
}
