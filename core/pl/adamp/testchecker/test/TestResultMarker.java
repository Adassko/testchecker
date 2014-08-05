package pl.adamp.testchecker.test;

import com.google.zxing.Dimension;
import com.google.zxing.ResultPoint;

public class TestResultMarker {
	
	private float size;
	private float angle;
	private ResultPoint point;
	private ResultPoint topLeft;
	private TestResult parentResult;
	
	public TestResultMarker(float x, float y, float size, float angle, TestResult parentResult) {
		this.point = new ResultPoint(x, y);
		this.topLeft = this.point.add(-size / 2, -size / 2).rotate(point, -angle);
		this.size = size;
		this.angle = angle;
	}
	
	public float getSize() {
		return size;
	}
	
	public ResultPoint getPoint() {
		return point;
	}
	
	public TestResult getTestResult() {
		return parentResult;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public ResultPoint getTopLeft() {
		return topLeft;
	}
	
	public ResultPoint getTopRight() {
		return topLeft.rotate(point, Math.PI / 2);
	}
	
	public ResultPoint getBottomLeft() {
		return topLeft.rotate(point, -Math.PI / 2);
	}
	
	public ResultPoint getBottomRight() {
		return topLeft.rotate(point, Math.PI);
	}
}