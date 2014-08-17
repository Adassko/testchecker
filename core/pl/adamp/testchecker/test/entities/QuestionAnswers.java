package pl.adamp.testchecker.test.entities;

import pl.adamp.testchecker.test.TestRow;

public class QuestionAnswers {
	private int possibleMistakes; // pole bitowe
	private int answers;
	private boolean markedByUser;
	private boolean trustworthy;
	private final boolean isMetadata;
	private final int testRowId;
	private final TestRow testRow;

	public int getTestRowId() {
		return testRowId;
	}

	public TestRow getTestRow() {
		return testRow;
	}
	
	public boolean isMetadata() {
		return isMetadata;
	}
	
	public int getMarkedAnswers() {
		return answers;
	}
	
	public Integer getFirstMarkedAnswerId() {
		if (answers == 0)
			return null;
		return Integer.numberOfTrailingZeros(answers);
	}
	
	public boolean isExactlyOneAnswerMarked() {
		return Integer.bitCount(answers) == 1;
	}

	public int getPossibleMistakes() {
		return possibleMistakes;
	}

	public void setMarkedByUser(boolean value) {
		this.markedByUser = value;
	}

	public boolean isMarkedByUser() {
		return this.markedByUser;
	}

	public boolean isTrustworthy() {
		return this.markedByUser || this.trustworthy;
	}

	public void trust() {
		trustworthy = true;
	}

	public void addMarkedAnswer(int id) {
		answers |= 1 << id;
	}

	public boolean isMarkedAnswer(int id) {
		return ((answers >> id) & 1) == 1;
	}

	public void addPossibleMistake(int id) {
		answers |= 1 << id;
		possibleMistakes |= 1 << id;
	}

	public boolean isPossibleMistake(int id) {
		return ((possibleMistakes >> id) & 1) == 1;
	}

	public QuestionAnswers(int testRowId, TestRow testRow) {
		this.testRow = testRow;
		isMetadata = testRow instanceof Metadata.Row;
		this.testRowId = testRowId;
		this.markedByUser = false;
	}

	@Override
	public final boolean equals(Object other) {
		if (other instanceof QuestionAnswers) {
			QuestionAnswers otherResult = (QuestionAnswers)other;
			return testRowId == otherResult.testRowId && answers == otherResult.answers &&
					possibleMistakes == otherResult.possibleMistakes;
		}
		return false;
	}

	@Override
	public final int hashCode() {
		int hash = testRowId;
		hash = 31 * hash + answers;
		hash = 31 * hash + possibleMistakes;
		return hash;
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder("Q" + testRowId + "(");

		int tmp = this.answers;
		while (tmp > 0) {
			builder.append((tmp & 1) > 0 ? "X" : "_");
			tmp >>= 1;
		}
		builder.append(")");
		return builder.toString();
	}
}
