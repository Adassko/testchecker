package pl.adamp.testchecker.test;

import com.google.zxing.ResultPoint;

public class TestArea {
	
	private ResultPoint upperLeft;
	private ResultPoint upperRight;
	private ResultPoint bottomLeft;
	private ResultPoint bottomRight;
	
	public TestArea(TestRegion firstRegion, TestRegion lastRegion, int regionsCount) {
		float exvalue = 1 + 1 / (2f * (regionsCount - 1)); // ekstrapolacja obszaru na podstawie œrodków skrajnych obszarów
		upperLeft = ResultPoint.lerp(lastRegion.getUpperPoint(), firstRegion.getUpperPoint(), exvalue);
		upperRight = ResultPoint.lerp(firstRegion.getUpperPoint(), lastRegion.getUpperPoint(), exvalue);
		
		bottomLeft = ResultPoint.lerp(lastRegion.getLowerPoint(), firstRegion.getLowerPoint(), exvalue);
		bottomRight = ResultPoint.lerp(firstRegion.getLowerPoint(), lastRegion.getLowerPoint(), exvalue);
	}

	public ResultPoint getUpperLeft() {
		return upperLeft;
	}

	public ResultPoint getUpperRight() {
		return upperRight;
	}

	public ResultPoint getBottomLeft() {
		return bottomLeft;
	}

	public ResultPoint getBottomRight() {
		return bottomRight;
	}

}
