package pl.adamp.testchecker.test;

import java.io.Serializable;

public abstract class TestRow implements Serializable {
	private static final long serialVersionUID = 6293540298446037124L;

	private int answersCount = 0;
	protected int reservedSpaceSize = 0;
	
	public void setAnswersCount(int count) {
		answersCount = count;
		if (reservedSpaceSize < count)
			setReservedSpaceSize(count);
	}
	
	public int getAnswersCount() {
		return answersCount;
	}
	
	public int getReservedSpaceSize() {
		return Math.max(reservedSpaceSize, getAnswersCount());
	}
	
	public void setReservedSpaceSize(int size) {
		this.reservedSpaceSize = size;
	}
}