package pl.adamp.testchecker.test;

public class Answer {
	String text;
	boolean isCorrect;
	
	public String getText() {
		return text;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public Answer(String text, boolean isCorrect) {
		this.text = text;
		this.isCorrect = isCorrect;
	}
}
