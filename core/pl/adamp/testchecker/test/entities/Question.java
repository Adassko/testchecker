package pl.adamp.testchecker.test.entities;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.test.TestRow;
import pl.adamp.testchecker.test.interfaces.AnswersInflater;

public class Question extends TestRow {
	private static final long serialVersionUID = 1098464872533403480L;
	
	private String question;
	private List<Answer> answers;
	private int value;
	private int id;
	private AnswersInflater inflater;
	
	public Question(String question) {
		this(-1, question, 1);
	}
	
	public Question(int id, String question, int value) {
		this.id = id;
		this.question = question;
		this.answers = new ArrayList<Answer>(4);
		this.value = value;
	}
	
	public void setAnswersInflater(AnswersInflater inflater) {
		this.inflater = inflater;
	}
	
	/**
	 * Konstruktor kopiuj¹cy
	 * @param source Obiekt Ÿród³owy
	 */
	public Question(Question source) {
		this(source.id, source.question, source.value);
		answers.addAll(source.getAnswers());
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Answer addAnswer(Answer answer) {
		answers.add(answer);
		return answer;
	}
	
	public Answer addAnswer(String text) {
		return addAnswer(new Answer(text));
	}
	
	public Answer addAnswer(String text, boolean isCorrect) {
		return addAnswer(new Answer(-1, text, isCorrect));
	}
	
	public boolean removeAnswer(Answer answer) {
		return answers.remove(answer);
	}

	public String getQuestion() {
		return question;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public List<Answer> getAnswers() {
		if (inflater != null && answers.size() == 0) {
			answers = inflater.getAnswers(this);
		}
		return answers;
	}
	
	@Override
	public int getAnswersCount() {
		return answers.size();
	}
	
	@Override
	public String toString() {
		return this.question;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Question == false)
			return false;
		Question other = (Question)o;
		
		return this.id == other.id;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
}