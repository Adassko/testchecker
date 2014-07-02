package pl.adamp.testchecker.test;

import java.util.Map;

import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;

public class TestReader implements Reader {
	private final String TAG = TestReader.class.getName(); 
	
	@Override
	public Result decode(BinaryBitmap image) throws NotFoundException,
			ChecksumException, FormatException {
		return decode(image, null);
	}

	@Override
	public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
			throws NotFoundException, ChecksumException, FormatException {
		
		ResultPointCallback resultPointCallback = hints == null ? null :
	        (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);

		BitMatrix matrix = image.getBlackMatrix();
		
		int height = matrix.getHeight();
		
		int crosses = 0;
		int x = matrix.getWidth() / 2;
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
		
		Log.d(TAG, "Liczba linii: " + crosses);
		
		throw NotFoundException.getNotFoundInstance();
		//return null;
	}

	@Override
	public void reset() {
		// do nothing
	}

}
