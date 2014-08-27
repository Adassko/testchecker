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
// test commit

package pl.adamp.testchecker.client;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.adamp.testchecker.client.ViewfinderView.ScanProgressListener;
import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.common.TestsListAdapter;
import pl.adamp.testchecker.client.result.ResultButtonListener;
import pl.adamp.testchecker.client.result.ResultHandler;
import pl.adamp.testchecker.client.result.ResultHandlerFactory;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.client.test.TestEvaluator;
import pl.adamp.testchecker.client.test.TestResult;
import pl.adamp.testchecker.test.TestDecoderResult;
import pl.adamp.testchecker.test.entities.AnswerSheet;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.TestSheet;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {

	private volatile TestSheet currentTestSheet = null;
	
	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;
	
	public static final String SCAN_TEST_ID = "test_id";
	public static final String SCAN_TEST_VARIANT = "variant";

	private static final Collection<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet
			.of(ResultMetadataType.ISSUE_NUMBER,
					ResultMetadataType.SUGGESTED_PRICE,
					ResultMetadataType.ERROR_CORRECTION_LEVEL,
					ResultMetadataType.POSSIBLE_COUNTRY);

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private View resultView;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private TestDefinition currentTestDefinition;
	private DataManager dataManager;
	private ProgressBar progressBar;

	TestSheet getCurrentTestSheet() {
		return currentTestSheet;
	}
	
	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		dataManager = new DataManager(this);

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		Intent intent = getIntent();
		int testId = intent.getIntExtra(SCAN_TEST_ID, -1);
		int variant = intent.getIntExtra(SCAN_TEST_VARIANT, -1);
		
		((TextView)findViewById(R.id.status_view))
		.setText(R.string.scan_test_barcode);
		
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
		progressBar.getProgressDrawable().setColorFilter(Color.GREEN, Mode.MULTIPLY);
		
		if (testId >= 0 && variant >= 1)
			chooseTest(testId, variant);
				
		currentTestSheet = null;
	}
	

	/**
	 * Zmienia test
	 * @return True jeœli test istnieje w bazie danych i jest ró¿ny od ju¿ za³adowanego
	 */
	private boolean chooseTest(int id, int variant) {
		if (currentTestDefinition == null || currentTestDefinition.getId() != id)
			currentTestDefinition = new DataManager(this).getTest(id);
		
		if (currentTestDefinition == null)
			return false;

		if (variant >= 0 && (currentTestSheet == null || currentTestSheet.getVariant() != variant)) {
			chooseTest(currentTestDefinition.getTestSheet(variant));
			
			return true;
		}
		return false;
	}
	
	private void chooseTest(TestSheet testSheet) {
		currentTestSheet = testSheet;		

		((TextView)findViewById(R.id.textView_test_name))
			.setText(currentTestSheet.getName());
	
		((TextView)findViewById(R.id.status_view))
			.setText(R.string.msg_default_status);
		
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setMax(testSheet.getQuestionsCount());
		progressBar.setProgress(0);
		
		viewfinderView.setTestInstance(testSheet);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		viewfinderView.setScanProgressListener(new ScanProgressListener() {
			@Override
			public void setProgress(int progress) {
				progressBar.setProgress(progress);
			}
			
			@Override
			public void setSecondaryProgress(int progress) {
				progressBar.setSecondaryProgress(progress);
			}
		});

		resultView = findViewById(R.id.result_view);
		statusView = (TextView) findViewById(R.id.status_view);

		handler = null;

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

		beepManager.updatePrefs();

		inactivityTimer.onResume();

		decodeFormats = new ArrayList<BarcodeFormat>();
		decodeFormats.add(BarcodeFormat.TEST);
		decodeFormats.add(BarcodeFormat.CODE_128);
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		if (toast != null)
			toast.cancel();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	long lastBackPress;
	@Override
	public void onBackPressed() {
		long currentTime = System.currentTimeMillis();
		if (lastBackPress > currentTime - 500) { // podwójne przyciœniêcie przycisku WSTECZ
			super.onBackPressed();
		} else {
			restartPreviewAfterDelay(0L);
			lastBackPress = currentTime;
			showToast(R.string.press_back_twice);
		}
	}
	
	private Toast toast;
	
	private void showToast(int textResId) {
		if (toast != null) {
			toast.cancel();
		}
		toast = Toast.makeText(this, textResId, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_FOCUS:
			cameraManager.autoFocus();
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraManager.setTorch(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.capture, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		final Activity that = this;
		switch (item.getItemId()) {
		case R.id.menu_choose_test:
			List<TestDefinition> tests = new DataManager(this).getTests();
			final ListView testsList = new ListView(this);
			testsList.setPadding(7, 7, 7, 7);
			
			final AlertDialog dialog = new AlertDialog.Builder(this)
				.setView(testsList)
				.setTitle(R.string.button_choose_test)
				.setNegativeButton(R.string.button_cancel, null)
				.create();

			final TestsListAdapter adapter = new TestsListAdapter(this, tests); 
			testsList.setAdapter(adapter);
			testsList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					final TestDefinition test = (TestDefinition)adapter.getItem(position);
					if (test == null) return;
					
					DialogHelper.showNumberChooser(that, R.string.test_variant, new OnAcceptListener() {
						@Override
						public void onAccept(String input) {
							chooseTest(test.getTestSheet(Integer.parseInt(input)));
							dialog.cancel();
						}
					}, 1, 1, 40);
				}
			});
			dialog.show();
			
			break;
		case R.id.menu_complete:
			AnswerSheet answerSheet = viewfinderView.getAnswerSheet();
			
			TestResult testResult = TestEvaluator.getTestResults(getCurrentTestSheet(), answerSheet);
			testResult = dataManager.saveTestResult(testResult);
			if (testResult != null) {
				for (QuestionAnswers answer : answerSheet.getAcceptedAnswers()) {
					dataManager.saveQuestionAnswers(testResult, answer);
				}
			
				startActivity(
						new Intent(getApplicationContext(), TestResultsActivity.class)
							.putExtra(TestResultsActivity.TEST_RESULT_ID, testResult.getId())
							);
			} else {
				Toast.makeText(this, R.string.msg_saveerror, Toast.LENGTH_LONG).show();
			}
			
			return true;			
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		
		Log.d(TAG, "Got result: " + rawResult.getClass().getName());
		
		if (rawResult instanceof TestDecoderResult) {
			TestDecoderResult result = (TestDecoderResult)rawResult;
			
			TestResult testResult = TestEvaluator.getTestResults(getCurrentTestSheet(), result.getAnswers());
			testResult = dataManager.saveTestResult(testResult);
			if (testResult != null) {
				for (QuestionAnswers answer : result.getAnswers().getAcceptedAnswers()) {
					dataManager.saveQuestionAnswers(testResult, answer);
				}
			
				Intent intent = new Intent(getApplicationContext(), TestResultsActivity.class);
				intent.putExtra(TestResultsActivity.TEST_RESULT_ID, testResult.getId());
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.msg_saveerror, Toast.LENGTH_LONG).show();
			}
			
			return;			
		}
		
		if (rawResult.getBarcodeFormat() == BarcodeFormat.CODE_128) {
			String text = rawResult.getText();
			Pattern pattern = Pattern.compile("^t(\\d+)/(\\d+)$");
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				try {
					int testId = Integer.parseInt(matcher.group(1));
					int testVariant = Integer.parseInt(matcher.group(2));
					if (testVariant < 1) return;

					if (chooseTest(testId, testVariant)) {
						Toast.makeText(this, currentTestSheet.getName() + " (" + getString(R.string.test_variant) + " " + testVariant + ")",
								Toast.LENGTH_LONG).show();
						viewfinderView.clearView();
					}
				}
				catch (NumberFormatException | NullPointerException e) {
					Log.d(TAG, e.toString());
				}
			}
			restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
			return;
		}
	}


	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(),
					scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*if (event.getAction() == MotionEvent.ACTION_UP) {
			currentTestSheet = TestSheet.getSampleInstance();
			viewfinderView.clearView();
		}*/

		return super.onTouchEvent(event);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		resultView.setVisibility(View.GONE);
		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		viewfinderView.clearView();
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
}
