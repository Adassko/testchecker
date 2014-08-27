package pl.adamp.testchecker.client.test;

import java.util.Date;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;

public class TestResult implements Serializable {
	private static final long serialVersionUID = -7888862357092431943L;
	
	private int correct;
	private int incorrect;
	private int points;
	private int additionalPoints;
	private int maxPoints;
	private Map<String, String> metadata;
	private long studentId;
	private int questionsCount;
	private int id;
	private String grade;
	private Date date;
	private int testId;
	private int variant;
	
	public TestResult() {
		this(-1);
	}
	
	public TestResult(int id) {
		this.id = id;
		this.metadata = new HashMap<String, String>();
		this.date = new Date();
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public void setTestId(int testId) {
		this.testId = testId;
	}
	
	public int getTestId() {
		return this.testId;
	}
	
	public int getVariant() {
		return this.variant;
	}
	
	public void setVariant(int variant) {
		this.variant = variant;
	}
	
	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	public String getGrade() {
		return this.grade;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
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
	
	public void setQuestionsCount(int questionsCount) {
		this.questionsCount = questionsCount;
	}
	
	public int getQuestionsCount() {
		return this.questionsCount;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
	public void increasePoints(int points) {
		this.points += points;
	}
	
	public void setAdditionalPoints(int points) {
		this.additionalPoints = points;
	}
	
	public int getAdditionalPoints() {
		return this.additionalPoints;
	}

	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}
	
	public void increaseMaxPoints(int points) {
		this.maxPoints += points;
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
		return points + additionalPoints;
	}

	public int getMaxPoints() {
		return maxPoints;
	}
}
