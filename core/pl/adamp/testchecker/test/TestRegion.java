package pl.adamp.testchecker.test;

import com.google.zxing.ResultPoint;

public class TestRegion {
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