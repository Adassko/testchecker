package pl.adamp.testchecker.client.test;

import java.util.HashMap;
import java.util.Map;

public class TestResult {
	private int correct;
	private int incorrect;
	private int points;
	private int totalPoints;
	private Map<String, String> metadata;
	private long studentId;
	
	public TestResult() {
		this.metadata = new HashMap<String, String>();
	}
	
	public void setMetadataValue(String metadataName, String value) {
		metadata.put(metadataName, value);
	}
	
	public void setStudentId(long id) {
		this.studentId = id;
	}
	
	public long getStudentId() {
		return this.studentId;
	}
	
	public void setCorrect(int correct) {
		this.correct = correct;
	}
	
	public void increaseCorrect(int questionValue) {
		this.correct += 1;
		this.points += questionValue;
	}

	public void setIncorrect(int incorrect) {
		this.incorrect = incorrect;
	}
	
	public void increaseIncorrect() {
		this.incorrect += 1;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
	public void increasePoints(int points) {
		this.points += points;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}
	
	public void increaseTotalPoints(int points) {
		this.totalPoints += points;
	}
	
	public int getCorrect() {
		return correct;
	}

	public int getIncorrect() {
		return incorrect;
	}

	public int getPoints() {
		return points;
	}

	public int getTotalPoints() {
		return totalPoints;
	}
}
