package pl.adamp.testchecker.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;






import android.util.Log;


//import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.oned.OneDReader;

public class TestReader implements Reader {
	private static final int MAX_AVG_VARIANCE = 34;
	private final String TAG = TestReader.class.getName();
	
	static final int[][] LOWER_PATTERNS = {
		{1, 1, 2, 1, 2}, // 0
	    {1, 2, 2, 1, 2},
	    //{1, 2, 2, 2, 2},
	    {1, 2, 2, 2, 1},
	    {1, 2, 1, 1, 3},
	    {1, 2, 1, 2, 3}, // 5
	    {1, 1, 2, 2, 1},
	    {1, 2, 2, 1, 1},
	    {1, 1, 1, 3, 2}, 
	    {1, 3, 1, 1, 2}
	};
	
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
	
	public static final int MAX_ANSWERS_COUNT = CODE_PATTERNS.length;

	private HashMap<TestResult, Integer> possibleResults;
	private TestResult[] acceptedResults;
	
	private ResultPointCallback resultPointCallback = null;
	private TestResultCallback testResultCallback = null;
	
	public TestReader() {
		possibleResults = new HashMap<TestResult, Integer>(200);
		acceptedResults = new TestResult[MAX_ANSWERS_COUNT];
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
			// znajdü pojedynczπ kreskÍ poprzedzonπ czystπ strefπ 5x
			do {
				patternStart = column.getNextSet(position);
				int silenceZoneLength = patternStart - position; 
				firstUnset = column.getNextUnset(patternStart);
				position = firstUnset;
				
				int singleLineHeight = firstUnset - patternStart; // wysokoúÊ pojedynczej kreski
				counters[0] = singleLineHeight;
				maxLineHeight = 5 * singleLineHeight;
				minLineHeight = singleLineHeight / 2;
				silenceZoneClear = silenceZoneLength >= Math.min(patternStart, maxLineHeight);
			}
			while (!silenceZoneClear && position < limit);
			
			// jeúli dotarliúmy do koÒca i nadal nie znaleüliúmy niczego
			if (!silenceZoneClear) break;
			
			int i;
			boolean isWhite = true;
	
			for (i = 1; i < patternSize && position < limit; i ++) {
				int y = isWhite ? column.getNextSet(position) : column.getNextUnset(position);
				isWhite ^= true;
				
				counters[i] = y - position;
				position = y;
				
				if (counters[i] > maxLineHeight || counters[i] < minLineHeight) {
					// za d≥uga lub za krÛtka linia/przerwa -> szukaj kolejnego kodu
					break;
				}
			}
			if (i < patternSize) {
				// cofnij siÍ do pierwszego pustego miejsca w b≥Ídnie rozpoznawanym kodzie
				position = firstUnset;
				continue;
			}
			
			// sprawdzenie czy istnieje "czysta strefa" z przodu kodu
			silenceZoneClear = position < limit && column.isRange(position, Math.min(limit, position + maxLineHeight), false);
		}
		while (!silenceZoneClear && position < limit);
		
		// position >= height -> nie znaleüliúmy niczego
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

	private class TestRegion {
		private int regionCode;
		private ResultPoint upper;
		private ResultPoint lower;
		private float angle;

		public int getRegionCode() {
			return regionCode;
		}

		public ResultPoint getUpperPoint() {
			return upper;
		}
		
		public ResultPoint getLowerPoint() {
			return lower;
		}
		
		public float getSlopeAngle() {
			return angle;
		}

		public TestRegion(int regionCode, ResultPoint upperPoint, ResultPoint lowerPoint) {
			this.regionCode = regionCode;
			this.upper = upperPoint;
			this.lower = lowerPoint;
			this.angle = (float)ResultPoint.getAtan2(upperPoint, lowerPoint);
		}
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
	
	private class PointsAverager {
		float x = 0;
		float y = 0;
		int count = 0;
		
		public void addPoint(ResultPoint point) {
			x += point.getX();
			y += point.getY();
			count ++;
		}
		
		public void clear() {
			count = 0;
			x = 0;
			y = 0;
		}
		
		public ResultPoint getAveragePoint() {
			return new ResultPoint(x / count, y / count);
		}
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
	
	List<Marker> clearList(List<Marker> markers) {
		return averageMarkersPositions(rejectOddResults(markers));
	}
	
	List<TestRegion> detectTestAreas(BitMatrix matrix) {
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
				limit = (int)marker.getPosition().getY();
			}
			catch (NotFoundException ex) { }
			column.reverse();
			try {
				Marker marker = findPatternOnColumn(column, x, LOWER_PATTERNS, true, limit);
				lowerMarkers.add(marker);
			}
			catch (NotFoundException ex) { }
		}
		
		return matchMarkers(clearList(upperMarkers), clearList(lowerMarkers));
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
				
				if (upper.getCode() % LOWER_PATTERNS.length == lower.getCode()) {
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
		
		// odrzuÊ odpowiedzi o kπcie nachylenia odstajπce od mediany o ponad 5 stopni
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
		
		int checked = 0;
/*		for (int x = cX - r; x < cX + r; x ++) {
			if (matrix.get(x, cY)) {
				checked ++;
				if (checked > 4) return true;
			}
		}*/
		for (int y = cY - r; y < cY + r; y ++) {
			if (y != cY && matrix.get(cX, y)) {
				checked ++;
				if (checked > 3) return true;
			}
		}
		return false;
	}
	
	private void detectGivenAnswers(BitMatrix matrix, List<TestRegion> testRegions) {
		int answersCount = 4;
		int margin = 10;
		int boxHeight = 27;
		int boxAreaHeight = boxHeight + 2 * 5; // 5px margins
		float halfBoxAreaHeight = boxAreaHeight / 2;
		float halfBoxHeight = boxHeight / 2;
		
		for (TestRegion region : testRegions) {
			int totalHeight = answersCount * boxAreaHeight + margin * 2;
			
			TestResult result = new TestResult(region.getRegionCode() + 1);
			List<TestResultMarker> markers = new ArrayList<TestResultMarker>(2);
			
			for (int i = 0; i < answersCount; i++) {
				float boxY = margin + i * boxAreaHeight + halfBoxAreaHeight;
				float dpp = ResultPoint.distance(region.getUpperPoint(), region.getLowerPoint()) / totalHeight;
				ResultPoint center = ResultPoint.lerp(region.getUpperPoint(), region.getLowerPoint(), boxY / totalHeight);
				
				if (isTicked(matrix, center, halfBoxHeight * dpp)) {
					result.addMarkedAnswer(i);
					markers.add(new TestResultMarker(center.getX(), center.getY(), boxHeight * dpp, region.getSlopeAngle(), result));
					resultPointCallback.foundPossibleResultPoint(center);
				}
			}
			
			testResultCallback.foundPossibleAnswer(result);
			foundPossibleAnswer(result);
		}
	}
	
	private void foundPossibleAnswer(TestResult answer) {
		int questionNo = answer.getQuestionNo();
		if (questionNo >= MAX_ANSWERS_COUNT) return;

		Integer reliability = possibleResults.get(answer);
		if (reliability == null) reliability = 0;
		
		possibleResults.put(answer, ++reliability);

		if (reliability > 2) {
				TestResult accepted = acceptedResults[questionNo];
				
				if (accepted != null) {
					if (accepted.isMarkedByUser())
						return;
					
					Integer acceptedReliability = possibleResults.get(accepted);
					if (acceptedReliability != null && acceptedReliability > reliability)
						return;
				}
				
				acceptedResults[questionNo] = answer;
				
				testResultCallback.foundAnswer(answer);
		}
	}
	
	@Override
	public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
			throws NotFoundException, ChecksumException, FormatException {

		testResultCallback = hints == null ? null :
			(TestResultCallback) hints.get(DecodeHintType.NEED_TEST_RESULT_CALLBACK);
		resultPointCallback = hints == null ? null :
	        (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);

		BitMatrix matrix = image.getBlackMatrix();
		
		List<TestRegion> testRegions = detectTestAreas(matrix);
		
		detectGivenAnswers(matrix, testRegions);
		
		if (testRegions.size() > 2) {
			ResultPoint[] area = { testRegions.get(0).getUpperPoint(), testRegions.get(testRegions.size() - 1).getUpperPoint(),
					testRegions.get(testRegions.size() - 1).getLowerPoint(), testRegions.get(0).getLowerPoint() };
			resultPointCallback.foundArea(area);
		}
		
		
		//return new Result("", new byte[] { }, resultPoints, BarcodeFormat.TEST);
		throw NotFoundException.getNotFoundInstance();

		/*
		int height = matrix.getHeight();
		int halfWidth = matrix.getWidth() / 2;
		
		int crosses = 0;
		
		for (int x = halfWidth - 50; x < halfWidth + 50; x++)
		{
			//int x = matrix.getWidth() / 2;
			boolean state = matrix.get(x, 0);
			for (int i = 0; i < height; i ++)
			{
				boolean newState = matrix.get(x, i);
				if (newState != state)
				{
					state = newState;
					if (newState)
					{
						crosses ++;
						resultPointCallback.foundPossibleResultPoint(new ResultPoint(x, i));
					}
				}
			}
		}
		Log.d(TAG, "Liczba linii: " + crosses);*/
		
		
		//return null;
	}

	@Override
	public void reset() {
		//offset = 0;
		// do nothing
	}
}
