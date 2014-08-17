package com.google.zxing;

public class PointsAverager {
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