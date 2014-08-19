package pl.adamp.testchecker.test.entities;

import java.io.Serializable;

public class Answer implements Serializable, HasId {
	private static final long serialVersionUID = 2642447049129762178L;

	private String text;
	private boolean isCorrect;
	private int id;
	
	public String getText() {
		return text;
	}

	public boolean isCorrect() {
		return isCorrect;
	}
	
	public void setCorrect(boolean value) {
		this.isCorrect = value;
	}

	public Answer(String text) {
		this(-1, text, false);
	}
	
	public Answer(int id, String text, boolean isCorrect) {
		this.text = text;
		this.isCorrect = isCorrect;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return this.text.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return this.text.equals(other);
	}
	
	@Override
	public String toString() {
		return this.text + (this.isCorrect ? " (correct)" : "");
	}
}
