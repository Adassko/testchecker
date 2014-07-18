package pl.adamp.testchecker.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.zxing.BarcodeFormat;
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
import com.google.zxing.common.detector.MonochromeRectangleDetector;
import com.google.zxing.common.detector.WhiteRectangleDetector;
import com.google.zxing.oned.OneDReader;

public class TestReader implements Reader {
	private static final int MAX_AVG_VARIANCE = 42;
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
	
	private ResultPointCallback resultPointCallback = null;
	
	@Override
	public Result decode(BinaryBitmap image) throws NotFoundException,
			ChecksumException, FormatException {
		return decode(image, null);
	}

	private Marker findPatternOnColumn(BitMatrix matrix, int x, int[][] patterns, boolean reverse) throws NotFoundException {
		BitArray column = matrix.getColumn(x, null, reverse);
		int height = matrix.getHeight();
		boolean silenceZoneClear;

		int patternSize = patterns[0].length;
		int[] counters = new int[patternSize];
		int position = 0, patternStart, silenceZoneLength;

		do {
			// znajdü pojedynczπ kreskÍ poprzedzonπ czystπ strefπ 5x
			do {
				position = column.getNextSet(position);
				patternStart = position;
				position = column.getNextUnset(position);
				counters[0] = position - patternStart; // wysokosc pojedynczej kreski
				silenceZoneLength = counters[0] * 5;
				silenceZoneClear = column.isRange(Math.max(0, patternStart - silenceZoneLength), patternStart, false);
			}
			while (!silenceZoneClear && position < height);
			
			// jeúli dotarliúmy do koÒca i nadal nie znaleüliúmy niczego
			if (!silenceZoneClear) break;
			
			int i;
			boolean isWhite = true;
	
			for (i = 1; i < patternSize && position < height; i ++) {
				int y = isWhite ? column.getNextSet(position) : column.getNextUnset(position);
				isWhite ^= true;
				
				counters[i] = y - position;
				position = y;
				
				if (counters[i] > silenceZoneLength) break; // za d≥uga przerwa - szukaj kolejnego kodu
			}
			if (i < patternSize) continue;
			
			silenceZoneClear = column.isRange(position, Math.min(height, position + silenceZoneLength), false);
		}
		while (!silenceZoneClear && position < height);
		
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
		private int questionNumber;
		private ResultPoint upper;
		private ResultPoint lower;

		public int getQuestionNumber() {
			return questionNumber;
		}

		public ResultPoint getUpper() {
			return upper;
		}
		
		public ResultPoint getLower() {
			return lower;
		}

		public TestRegion(int questionNumber, ResultPoint upper, ResultPoint lower) {
			this.questionNumber = questionNumber;
			this.upper = upper;
			this.lower = lower;
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

	List<Marker> getLowerMarkers(BitMatrix matrix) {
		List<Marker> markers = new ArrayList<Marker>();
		
		int width = matrix.getWidth();
		for (int x = 0; x < width; x ++) {
			try {
				Marker marker = findPatternOnColumn(matrix, x, LOWER_PATTERNS, true);
				markers.add(marker);
			}
			catch (NotFoundException ex) { }
		}
		
		return clearList(markers);
	}
	
	float getDerivative(ResultPoint a, ResultPoint b) {
		float deltaX = b.getX() - a.getY();
		float deltaY = b.getY() - a.getY();
		return deltaY / deltaX;
	}
	
	List<Marker> rejectHighDeviation(List<Marker> markers) {
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
		Log.d(TAG, "Zostawiono " + markers.size() + " z " + initSize);
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
		List<Marker> result = markers;
		for (int i = 0; i < 2; i ++) {
			result = rejectHighDeviation(averageMarkersPositions(result));
		}
		return result;
	}
	
	List<Marker> getUpperMarkers(BitMatrix matrix) {
		List<Marker> markers = new ArrayList<Marker>();

		int width = matrix.getWidth();
		for (int x = 0; x < width; x ++) {
			try {
				Marker marker = findPatternOnColumn(matrix, x, CODE_PATTERNS, false);
				markers.add(marker);
			}
			catch (NotFoundException ex) { }
		}
		
		return clearList(markers);
	}

	List<TestRegion> matchMarkers(List<Marker> upperMarkers, List<Marker> lowerMarkers) {
		List<TestRegion> result = new ArrayList<TestRegion>();
		List<Float> ctgs = new ArrayList<Float>();
		
		int lowerPos = 0;
		int lowerSize = lowerMarkers.size();
		
		for (int i = 0, upperSize = upperMarkers.size(); i < upperSize && lowerPos < lowerSize; i++) {
			Marker upper = upperMarkers.get(i);
			
			for (int j = lowerPos; j < lowerSize; j++) {
				Marker lower = lowerMarkers.get(j);
				if (lower.getPosition().getY() < upper.getPosition().getY() + 10) {
					continue;
				}
				
				float ctg = (upper.getPosition().getX() - lower.getPosition().getX()) /
						(upper.getPosition().getY() - lower.getPosition().getY());
				if (Math.abs(ctg) > 0.5) {
					continue;
				}
				
				if (upper.getCode() % LOWER_PATTERNS.length == lower.getCode()) {
					result.add(new TestRegion(upper.getCode(), upper.getPosition(), lower.getPosition()));
					ctgs.add(ctg);
					lowerPos = j;
				}
			}
		}
		if (ctgs.size() > 1) {
			Collections.sort(ctgs);
			float ctgMedian = Math.abs(ctgs.get(ctgs.size() / 2));
			for (int i = result.size() - 1; i >= 0; i --) {
				TestRegion region = result.get(i);
				float ctg = (region.getUpper().getX() - region.getLower().getX()) /
						(region.getUpper().getY() - region.getLower().getY());
				if (Math.abs(ctg - ctgMedian) > 0.1)
					result.remove(i);
			}
		}
		return result;
	}
	
	ResultPoint lerp(ResultPoint a, ResultPoint b, float factor) {
		return new ResultPoint(a.getX() * (1 - factor) + b.getX() * factor,
				a.getY() * (1 - factor) + b.getY() * factor);
	}
	
	@Override
	public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
			throws NotFoundException, ChecksumException, FormatException {
		
		resultPointCallback = hints == null ? null :
	        (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);

		BitMatrix matrix = image.getBlackMatrix();
		
		List<TestRegion> testRegions = matchMarkers(getUpperMarkers(matrix), getLowerMarkers(matrix));
		/*for (TestRegion region : testRegions) {
			for (int i = 1; i < 5; i ++)
				resultPointCallback.foundPossibleResultPoint(lerp(region.getUpper(), region.getLower(), 0.2f * i));
			//Log.d(TAG, "Znaleziono pytanie: " + region.getQuestionNumber());
		}*/
		
		if (testRegions.size() > 2) {
			ResultPoint[] area = { testRegions.get(0).getUpper(), testRegions.get(testRegions.size() - 1).getUpper(),
					testRegions.get(testRegions.size() - 1).getLower(), testRegions.get(0).getLower() };
			resultPointCallback.foundArea(area);
		}
		Log.d(TAG, "Pytan: " + testRegions.size());
		
		
/*		while ((y = column.getNextSet(y)) < height) {
			findPatternOnColumn(column)
			resultPointCallback.foundPossibleResultPoint(new ResultPoint(0, y));
			y = column.getNextUnset(y);
		}*/
		//findAnswers(matrix);
		//ResultPoint[] resultPoints = findTestArea(matrix);
		
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
