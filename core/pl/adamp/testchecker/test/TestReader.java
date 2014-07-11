package pl.adamp.testchecker.test;

import java.util.Map;

import android.util.Log;

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
	private static final int INTEGER_MATH_SHIFT = 8;
	private final String TAG = TestReader.class.getName(); 
	
	private ResultPointCallback resultPointCallback = null;
	
	@Override
	public Result decode(BinaryBitmap image) throws NotFoundException,
			ChecksumException, FormatException {
		return decode(image, null);
	}

	private int[] findPatternOnColumn(BitArray column) throws NotFoundException {
		int height = column.getSize();

		int[] counters = new int[5];
		int position = column.getNextSet(0);
		int patternStart = position;
		int i = 0;
		boolean isWhite = false;

		for (i = 0; i < 5 && position < height; i ++) {
			int y = isWhite ? column.getNextSet(position) : column.getNextUnset(position);
			
			counters[i] = y - position;
			position = y;
			isWhite ^= true;
		}
		if (i < 3) throw NotFoundException.getNotFoundInstance();
		
		int variance = OneDReader.patternMatchVariance(counters, new int[] { 2,1,1,2,3 }, 179);
		boolean silenceZoneClear = column.isRange(Math.max(0, patternStart - (position - patternStart) / 2), patternStart, false); 
		if (variance >= 32 || !silenceZoneClear) {
        	throw NotFoundException.getNotFoundInstance();
        }
		Log.d(TAG, "Pattern found at " + position);
		
		return new int[] { patternStart, position };
	}
	
	private ResultPoint[] findTestArea(BitMatrix matrix) throws NotFoundException {
		int width = matrix.getWidth(),
			height = matrix.getHeight();
		ResultPoint upperLeft = null, upperRight = null,
			lowerLeft = null, lowerRight = null;
		int upperHelper = 0, lowerHelper = 0; // auxiliary variables for binary search
		
		int step = width >> 4; // 1/16 of width
		// find left border of test area
		BitArray column = null;
		for (int x = 0; x < width && (upperLeft == null || lowerLeft == null); x += step)
		{
			column = matrix.getColumn(x, column);
			if (upperLeft == null) {
				try {
					int[] upper = findPatternOnColumn(column);
					upperLeft = new ResultPoint(x, upper[1]);
				}
				catch (NotFoundException ex) {
					upperHelper = x;
				}
			}
			if (lowerLeft == null) {
				column.reverse(); // results are reversed too!
				try {
					int[] lower = findPatternOnColumn(column);
					lowerLeft = new ResultPoint(x, height - lower[1]);
				}
				catch (NotFoundException ex) {
					lowerHelper = x;
				}
			}
		}
		
		if (upperLeft == null || lowerLeft == null) {
			Log.d(TAG, (upperLeft == null ? "Upper left not found" : " ") + (lowerLeft == null ? "Lower left not found" : " "));
			throw NotFoundException.getNotFoundInstance();
		}
		Log.d(TAG, "Upper left: " + upperLeft.toString() + " / Lower left: " + lowerLeft.toString());

		// binary search for exact point of lines start
		while (upperLeft.getX() - upperHelper > 2) {
			int middle = (int)(upperLeft.getX() + upperHelper) >> 1;
			column = matrix.getColumn(middle, column);
			try {
				int[] upper = findPatternOnColumn(column);
				upperLeft = new ResultPoint(middle, upper[1]);
			}
			catch (NotFoundException ex) {
				upperHelper = middle;
			}
		}
		while (lowerLeft.getX() - lowerHelper > 2) {
			int middle = (int)(lowerLeft.getX() + lowerHelper) >> 1;
			column = matrix.getColumn(middle, column);
			try {
				int[] lower = findPatternOnColumn(column);
				lowerLeft = new ResultPoint(middle, height - lower[1]);
			}
			catch (NotFoundException ex) {
				lowerHelper = middle;
			}
		}
		
		Log.d(TAG, "Exact upper left: " + upperLeft.toString() + " / Lower left: " + lowerLeft.toString());

		
		
		// find right border of test area
		lowerHelper = upperHelper = width - 1;
		for (int x = width - 1; x > 0 && (upperRight == null || lowerRight == null); x -= step)
		{
			column = matrix.getColumn(x, column);
			if (upperRight == null) {
				try {
					int[] upper = findPatternOnColumn(column);
					upperRight = new ResultPoint(x, upper[1]);
				}
				catch (NotFoundException ex) {
					upperHelper = x;
				}
			}
			if (lowerRight == null) {
				column.reverse(); // results are reversed too!
				try {
					int[] lower = findPatternOnColumn(column);
					lowerRight = new ResultPoint(x, height - lower[1]);
				}
				catch (NotFoundException ex) {
					lowerHelper = x;
				}
			}
		}
		if (upperRight == null || lowerRight == null)
			throw NotFoundException.getNotFoundInstance();

		// binary search again
		while (upperHelper - upperRight.getX() > 2) {
			int middle = (int)(upperRight.getX() + upperHelper) >> 1;
			column = matrix.getColumn(middle, column);
			try {
				int[] upper = findPatternOnColumn(column);
				upperRight = new ResultPoint(middle, upper[1]);
			}
			catch (NotFoundException ex) {
				upperHelper = middle;
			}
		}
		while (lowerHelper - lowerRight.getX() > 2) {
			int middle = (int)(lowerRight.getX() + lowerHelper) >> 1;
			column = matrix.getColumn(middle, column);
			try {
				int[] lower = findPatternOnColumn(column);
				lowerRight = new ResultPoint(middle, height - lower[1]);
			}
			catch (NotFoundException ex) {
				lowerHelper = middle;
			}
		}
		
		return new ResultPoint[] { upperLeft, upperRight, lowerLeft, lowerRight };
	}

	//			resultPointCallback.foundPossibleResultPoint(new ResultPoint(x, result[0]));
	//			resultPointCallback.foundPossibleResultPoint(new ResultPoint(x, result[1]));

	@Override
	public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
			throws NotFoundException, ChecksumException, FormatException {
		
		resultPointCallback = hints == null ? null :
	        (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);

		BitMatrix matrix = image.getBlackMatrix();

		ResultPoint[] resultPoints = findTestArea(matrix);
		
		return new Result("", new byte[] { }, resultPoints, BarcodeFormat.TEST);

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
		// do nothing
	}
}
