package com.google.zxing.common;

import com.google.zxing.ResultPoint;

public class RotatedBitMatrix extends BitMatrix {
	private BitMatrix matrix;
	private int width;
	private int height;

	public RotatedBitMatrix(BitMatrix matrix) {
		super(1);
		this.matrix = matrix;
		this.width = matrix.getHeight();
		this.height = matrix.getWidth();
	}

	public ResultPoint translatePoint(ResultPoint point) {
		point = matrix.translatePoint(point);
		return new ResultPoint(height - 1 - point.getY(), point.getX(), point.getColor());
	}
	
	public boolean get(int x, int y) {
		return matrix.get(y, width - 1 - x);
	}

	public void set(int x, int y) {
		matrix.set(y, width - 1 - x);
	}

	public void flip(int x, int y) {
		matrix.flip(y, width - 1 - x);
	}

	public void clear() {
		matrix.clear();
	}

	public void setRegion(int left, int top, int width, int height) {
		matrix.setRegion(top, this.width - 1 - left, top + width, this.width - 1 - left - height);
	}

	public BitArray getRow(int y, BitArray row) {
		return matrix.getColumn(width - 1 - y, row, false);
	}

	public BitArray getColumn(int x, BitArray column, boolean reverse) {
		BitArray r = matrix.getRow(x, column);
		if (!reverse) r.reverse();
		return r;
	}

	public void setRow(int y, BitArray row) {
		int size = row.getSize();
		for(int i = 0; i < size; i ++) {
			set(height - 1 - y, i);
		}
	}

	public int[] getEnclosingRectangle() {
		throw new UnsupportedOperationException();
	}

	public int[] getTopLeftOnBit() {
		throw new UnsupportedOperationException();
	}

	public int[] getBottomRightOnBit() {
		throw new UnsupportedOperationException();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BitMatrix)) {
			return false;
		}
		BitMatrix other = (BitMatrix) o;
		if (width != other.width || height != other.height ||
				rowSize != other.rowSize || bits.length != other.bits.length) {
			return false;
		}
		for (int i = 0; i < bits.length; i++) {
			if (bits[i] != other.bits[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = width;
		hash = 31 * hash + width;
		hash = 31 * hash + height;
		hash = 31 * hash + matrix.rowSize;
		for (int bit : matrix.bits) {
			hash = 31 * hash + bit;
		}
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(height * (width + 1));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				result.append(get(x, y) ? "X " : "  ");
			}
			result.append('\n');
		}
		return result.toString();
	}

}
