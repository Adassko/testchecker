package pl.adamp.testchecker.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;












import pl.adamp.testchecker.test.entities.AnswerSheet;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.TestSheet;
import android.graphics.Color;
import android.util.Log;




import android.util.Pair;
import android.util.SparseArray;




//import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PointsAverager;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.RotatedBitMatrix;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.oned.OneDReader;

public class TestReader implements Reader {
	private static final int MAX_AVG_VARIANCE = 42;
	private final String TAG = TestReader.class.getName();
	
	static final int[][] CODE_PATTERNS = {
	      {1, 2, 1, 2, 2, 2, 2}, // 0
	      {1, 2, 2, 2, 1, 2, 2},
	      {1, 2, 2, 2, 2, 2, 1},
	      {1, 1, 2, 1, 2, 2, 3},
	      {1, 1, 2, 1, 3, 2, 2},
	      {1, 1, 3, 1, 2, 2, 2}, // 5
	      {1, 1, 2, 2, 2, 1, 3},
	      {1, 1, 2, 2, 3, 1, 2},
	      {1, 1, 3, 2, 2, 1, 2},
	      {1, 2, 2, 1, 2, 1, 3},
	      {1, 2, 2, 1, 3, 1, 2}, // 10
	      {1, 2, 3, 1, 2, 1, 2},
	      {1, 1, 1, 2, 2, 3, 2},
	      {1, 1, 2, 2, 1, 3, 2},
	      {1, 1, 2, 2, 2, 3, 1},
	      {1, 1, 1, 3, 2, 2, 2}, // 15
	      {1, 1, 2, 3, 1, 2, 2},
	      {1, 1, 2, 3, 2, 2, 1},
	      {1, 2, 2, 3, 2, 1, 1},
	      {1, 2, 2, 1, 1, 3, 2},
	      {1, 2, 2, 1, 2, 3, 1}, // 20
	      {1, 2, 1, 3, 2, 1, 2},
	      {1, 2, 2, 3, 1, 1, 2},
	      {1, 3, 1, 2, 1, 3, 1},
	      {1, 3, 1, 1, 2, 2, 2},
	      {1, 3, 2, 1, 1, 2, 2}, // 25
	      {1, 3, 2, 1, 2, 2, 1},
	      {1, 3, 1, 2, 2, 1, 2},
	      {1, 3, 2, 2, 1, 1, 2},
	      {1, 3, 2, 2, 2, 1, 1},
	      {1, 2, 1, 2, 1, 2, 3}, // 30
	      {1, 2, 1, 2, 3, 2, 1},
	      {1, 2, 3, 2, 1, 2, 1},
	      {1, 1, 1, 1, 3, 2, 3},
	      {1, 1, 3, 1, 1, 2, 3},
	      {1, 1, 3, 1, 3, 2, 1}, // 35
	      {1, 1, 1, 2, 3, 1, 3},
	      {1, 1, 3, 2, 1, 1, 3},
	      {1, 1, 3, 2, 3, 1, 1},
	      {1, 2, 1, 1, 3, 1, 3},
	      {1, 2, 3, 1, 1, 1, 3}, // 40
	      {1, 2, 3, 1, 3, 1, 1},
	      {1, 1, 1, 2, 1, 3, 3},
	      {1, 1, 1, 2, 3, 3, 1},
	      {1, 1, 3, 2, 1, 3, 1},
	      {1, 1, 1, 3, 1, 2, 3}, // 45
	      {1, 1, 1, 3, 3, 2, 1},
	      {1, 1, 3, 3, 1, 2, 1},
	      {1, 3, 1, 3, 1, 2, 1},
	      {1, 2, 1, 1, 3, 3, 1},
	      {1, 2, 3, 1, 1, 3, 1}, // 50
	      {1, 2, 1, 3, 1, 1, 3},
	      {1, 2, 1, 3, 3, 1, 1},
	      {1, 2, 1, 3, 1, 3, 1},
	      {1, 3, 1, 1, 1, 2, 3},
	      {1, 3, 1, 1, 3, 2, 1}, // 55
	      {1, 3, 3, 1, 1, 2, 1},
	      {1, 3, 1, 2, 1, 1, 3},
	      {1, 3, 1, 2, 3, 1, 1},
	      {1, 3, 3, 2, 1, 1, 1}
	};
	
	public static int[] getUpperCodePattern(int code) {
		return CODE_PATTERNS[code % MAX_TEST_ROWS];
	}
	
	public static int[] getLowerCodePattern(int code) {
		return CODE_PATTERNS[code % MAX_TEST_ROWS];
	}
	
	public static final int MAX_TEST_ROWS = CODE_PATTERNS.length;
	
	private static final int CONFIDENCE_THRESHOLD = 3;

	private HashMap<QuestionAnswers, Integer> possibleResults;
	
	private ResultPointCallback resultPointCallback = null;
	private TestResultCallback testResultCallback = null;
	
	private TestSheet currentTestSheet;
	private AnswerSheet answerSheet;
	
	public TestReader() {
		possibleResults = new HashMap<QuestionAnswers, Integer>(200);
	}
	
	@Override
	public Result decode(BinaryBitmap image) throws NotFoundException,
			ChecksumException, FormatException {
		return decode(image, null);
	}

	private Marker findPatternOnColumn(BitArray column, int x, int[][] patterns, boolean reverse, int limit) throws NotFoundException {
		int height = column.getSize();
		boolean silenceZoneClear;

		int patternSize = patterns[0].length;
		int[] counters = new int[patternSize];
		int position = 0, patternStart, firstUnset, maxLineHeight, minLineHeight;

		do {
			// znajdŸ pojedyncz¹ kreskê poprzedzon¹ czyst¹ stref¹ 5x
			do {
				patternStart = column.getNextSet(position);
				int silenceZoneLength = patternStart - position; 
				firstUnset = column.getNextUnset(patternStart);
				position = firstUnset;
				
				int singleLineHeight = firstUnset - patternStart; // wysokoœæ pojedynczej kreski
				counters[0] = singleLineHeight;
				maxLineHeight = 5 * singleLineHeight;
				minLineHeight = singleLineHeight / 2;
				silenceZoneClear = silenceZoneLength >= Math.min(patternStart, maxLineHeight);
			}
			while (!silenceZoneClear && position < limit);
			
			// jeœli dotarliœmy do koñca i nadal nie znaleŸliœmy niczego
			if (!silenceZoneClear) break;
			
			int i;
			boolean isWhite = true;
	
			for (i = 1; i < patternSize && position < limit; i ++) {
				int y = isWhite ? column.getNextSet(position) : column.getNextUnset(position);
				isWhite ^= true;
				
				counters[i] = y - position;
				position = y;
				
				if (counters[i] > maxLineHeight || counters[i] < minLineHeight) {
					// za d³uga lub za krótka linia/przerwa -> szukaj kolejnego kodu
					break;
				}
			}
			if (i < patternSize) {
				// cofnij siê do pierwszego pustego miejsca w b³êdnie rozpoznawanym kodzie
				position = firstUnset;
				continue;
			}
			
			// sprawdzenie czy istnieje "czysta strefa" z przodu kodu
			silenceZoneClear = position < limit && column.isRange(position, Math.min(limit, position + maxLineHeight), false);
		}
		while (!silenceZoneClear && position < limit);
		
		// position >= height -> nie znaleŸliœmy niczego
		if (!silenceZoneClear) throw NotFoundException.getNotFoundInstance();

		int bestMatch = -1;
		int bestVariance = MAX_AVG_VARIANCE;
		for (int code = 0; code < patterns.length; code ++) {
			int variance = OneDReader.patternMatchVariance(counters, patterns[code], 179);
			if (variance < bestVariance) {
				bestVariance = variance;
				bestMatch = code;
			}
		}
		if (bestMatch == -1)
        	throw NotFoundException.getNotFoundInstance();
		
		return new Marker(new ResultPoint(x, reverse ? height - position : position), bestMatch);
	}
	
	private class Marker {
		int code;
		ResultPoint position;
		
		public int getCode() {
			return code;
		}

		public ResultPoint getPosition() {
			return position;
		}

		public Marker(ResultPoint position, int code) {
			this.position = position;
			this.code = code;
		}
	}

	List<Marker> rejectOddResults(List<Marker> markers) {
		int initSize = markers.size();
		if (initSize == 0) return markers;

		int size = markers.size();
		
		for (int i = size - 1; i >= 0; i--) {
			Marker current = markers.get(i);
			Marker previous = i > 0 ? markers.get(i - 1) : current;
			Marker next = i < size - 1 ? markers.get(i + 1) : current;

			if (Math.abs(current.getCode() - previous.getCode()) > 1
				&& Math.abs(current.getCode() - next.getCode()) > 1) {
				markers.remove(i);
				size--;
				continue;
			}
		}
		return markers;
	}
	
	List<Marker> averageMarkersPositions(List<Marker> markers) {
		List<Marker> result = new ArrayList<Marker>();
		
		int lastCode = -1;
		PointsAverager averager = new PointsAverager();
		
		for (int i = markers.size() - 1; i >= 0; i --) {
			Marker current = markers.get(i);
			if (current.getCode() != lastCode) {
				if (lastCode != -1) {
					result.add(new Marker(averager.getAveragePoint(), lastCode));
					averager.clear();
				}
				lastCode = current.getCode();
			}
			
			averager.addPoint(current.getPosition());
		}
		
		if (lastCode != -1) {
			result.add(new Marker(averager.getAveragePoint(), lastCode));
		}

		return result;
	}
	
	List<Marker> neatList(List<Marker> markers) {
		return averageMarkersPositions(rejectOddResults(markers));
	}
	
	List<TestRegion> detectTestRegions(BitMatrix matrix) {
		List<Marker> upperMarkers = new ArrayList<Marker>(),
				lowerMarkers = new ArrayList<Marker>();

		int width = matrix.getWidth(), height = matrix.getHeight();
		BitArray column = null;
		for (int x = 0; x < width; x += 2) {
			int limit = height;
			column = matrix.getColumn(x, column, false);
			try {
				Marker marker = findPatternOnColumn(column, x, CODE_PATTERNS, false, height);
				upperMarkers.add(marker);
				limit = height - (int)marker.getPosition().getY();
			}
			catch (NotFoundException ex) { }
			column.reverse();
			try {
				Marker marker = findPatternOnColumn(column, x, /*LOWER_PATTERNS*/ CODE_PATTERNS, true, limit);
				lowerMarkers.add(marker);
			}
			catch (NotFoundException ex) { }
		}
		
		return matchMarkers(neatList(upperMarkers), neatList(lowerMarkers));
	}
	
	List<TestRegion> matchMarkers(List<Marker> upperMarkers, List<Marker> lowerMarkers) {
		List<TestRegion> result = new ArrayList<TestRegion>();
		List<Float> angles = new ArrayList<Float>();
		
		int lowerPos = 0;
		int lowerSize = lowerMarkers.size();
		
		for (int i = 0, upperSize = upperMarkers.size(); i < upperSize && lowerPos < lowerSize; i++) {
			Marker upper = upperMarkers.get(i);
			
			for (int j = lowerPos; j < lowerSize; j++) {
				Marker lower = lowerMarkers.get(j);
				if (lower.getPosition().getY() < upper.getPosition().getY() + 10) {
					continue;
				}
				
				if (upper.getCode() % /*LOWER_PATTERNS.length*/ MAX_TEST_ROWS == lower.getCode()
						&& upper.getCode() < currentTestSheet.getTestRowsCount()) {
					TestRegion region = new TestRegion(upper.getCode(), upper.getPosition(), lower.getPosition());
					
					float angle = region.getSlopeAngle();
					//Log.d(TAG, "" + angle);
					if (Math.abs(Math.PI / 2 - angle) < Math.PI / 4) { // odchylenie od pionu < 45 stopni
						result.add(region);
						angles.add(angle);
						lowerPos = j;
					}
				}
			}
		}
		
		// odrzuæ odpowiedzi o k¹cie nachylenia odstaj¹ce od mediany o ponad 5 stopni
		if (angles.size() > 1) {
			Collections.sort(angles);
			float angleMedian = MathUtils.getMedian(angles);
			for (int i = result.size() - 1; i >= 0; i --) {
				TestRegion region = result.get(i);
				
				if (Math.abs(region.getSlopeAngle() - angleMedian) > Math.PI / 36) // odchylenie od mediany > 5 stopni
					result.remove(i);
			}
		}
		return result;
	}
		
	private boolean isTicked(BitMatrix matrix, ResultPoint center, float range) {
		int cX = (int)center.getX();
		int cY = (int)center.getY();
		int r = (int)range;
		int minusR = Math.max(0, cY - r);
		int plusR = Math.min(matrix.getHeight() - 1, cY + r);
		
		int checked = 0;
/*		for (int x = cX - r; x < cX + r; x ++) {
			if (matrix.get(x, cY)) {
				checked ++;
				if (checked > 4) return true;
			}
		}*/
		
		// TODO: zaimplementowaæ algorytm Bresenhama
		int threshold = Math.max(3, Math.round((plusR - minusR - 1) * 0.2f));
		for (int y = minusR; y < plusR; y ++) {
			if (y != cY && matrix.get(cX, y)) {
				checked ++;
				if (checked > threshold) return true;
			}
		}
		return false;
	}
	
	private void detectGivenAnswers(BitMatrix matrix, List<TestRegion> testRegions) {
		int margin = 10;
		int boxHeight = 27;
		int boxAreaHeight = boxHeight + 2 * 5; // 5px margins
		float halfBoxAreaHeight = boxAreaHeight / 2;
		float halfBoxHeight = boxHeight / 2;
		
		int testSheetRowCount = currentTestSheet.getTestRowsCount();
		Log.d(TAG, "TestSheetRowCount: " + testSheetRowCount);
		
		for (TestRegion region : testRegions) {
			int rowId = region.getRegionCode();
			if (rowId >= testSheetRowCount) {
				Log.d(TAG, "Za duzy kod: " + rowId);
				continue;
			}
			
			TestRow testRow = currentTestSheet.getTestRow(rowId);
			
			QuestionAnswers result = new QuestionAnswers(rowId, testRow);
			
			int answersCount = testRow.getAnswersCount();
			int totalHeight = testRow.getReservedSpaceSize() * boxAreaHeight + margin * 2;
			
			for (int i = 0; i < answersCount; i++) {
				float boxY = margin + i * boxAreaHeight + halfBoxAreaHeight;
				float dpp = ResultPoint.distance(region.getUpperPoint(), region.getLowerPoint()) / totalHeight;
				ResultPoint center = ResultPoint.lerp(region.getUpperPoint(), region.getLowerPoint(), boxY / totalHeight);
				
				if (isTicked(matrix, center, halfBoxHeight * dpp)) {
					result.addMarkedAnswer(i);
					center.setColor(Color.BLACK);
					resultPointCallback.foundPossibleResultPoint(matrix.translatePoint(center));
				} else {
					center.setColor(Color.WHITE);
					resultPointCallback.foundPossibleResultPoint(matrix.translatePoint(center));
				}
			}
			
			foundPossibleAnswer(result);
		}
	}
	
	private void foundPossibleAnswer(QuestionAnswers answer) {
		int questionNo = answer.getTestRowId();
		if (questionNo >= MAX_TEST_ROWS) return;

		testResultCallback.foundPossibleAnswer(answer);
		Integer reliability = possibleResults.get(answer);
		if (reliability == null) reliability = 0;
		
		possibleResults.put(answer, ++reliability);

		if (reliability > 2) {
				QuestionAnswers accepted = answerSheet.getAcceptedAnswer(questionNo);
				
				if (accepted != null) {
					if (accepted.isMarkedByUser()) // nie podmieniaj odpowiedzi zaznaczonych rêcznie
						return;
					
					Integer acceptedReliability = possibleResults.get(accepted);
					if (acceptedReliability != null && acceptedReliability > reliability)
						return;
				}
				
				if (reliability > CONFIDENCE_THRESHOLD)
					answer.trust();
				
				answerSheet.addAcceptedAnswer(answer);
		}
	}
	
	@Override
	public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
			throws NotFoundException, ChecksumException, FormatException {
		
		testResultCallback = (TestResultCallback) hints.get(DecodeHintType.NEED_TEST_RESULT_CALLBACK);
		resultPointCallback = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
		
		TestSheet testSheet = testResultCallback.getTestSheet();
		AnswerSheet newAnswerSheet = testResultCallback.getAnswerSheet();
		
		if (testSheet != currentTestSheet || answerSheet != newAnswerSheet) {
			restartState();
			currentTestSheet = testSheet;
			answerSheet = newAnswerSheet;
		}
		
		if (currentTestSheet == null || answerSheet == null) {
			throw NotFoundException.getNotFoundInstance();
		}

		BitMatrix matrix = image.getBlackMatrix();
		
		List<TestRegion> testRegions = detectTestRegions(matrix);
		
		detectGivenAnswers(matrix, testRegions);
		
		boolean allAnswersTrustworthy = true;

		int questionsCount = testSheet.getQuestionsCount();
		for (int i = 0; i < questionsCount; i ++) {
			QuestionAnswers accepted = answerSheet.getAcceptedAnswer(i);
			if (accepted == null || accepted.isTrustworthy() == false) {
				allAnswersTrustworthy = false;
				break;
			}
		}
		
		if (allAnswersTrustworthy) {
			TestDecoderResult result = new TestDecoderResult(testResultCallback.getAnswerSheet());
			restartState();
			return result;
		}

		throw NotFoundException.getNotFoundInstance();
	}

	@Override
	public void reset() {
		// do nothing (gets called every frame)
	}
	
	private void restartState() {
		possibleResults.clear();
	}
}
