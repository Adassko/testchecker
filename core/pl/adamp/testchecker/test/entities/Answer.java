package pl.adamp.testchecker.test.entities;

import java.io.Serializable;

public class Answer implements Serializable, Listable {
	private static final long serialVersionUID = 2642447049129762178L;

	private String text;
	private boolean isCorrect;
	private int id;
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
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
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Answer == false)
			return false;
		Answer other = (Answer)o;
		return this.id == other.id;
	}
	
	@Override
	public String toString() {
		return this.text + (this.isCorrect ? " (correct)" : "");
	}
}
