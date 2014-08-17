package pl.adamp.testchecker.test.entities;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.test.interfaces.QuestionsInflater;

public class QuestionCategory {
	public static final QuestionCategory DefaultCategory = new NullObject();
	
	private int id;
	private String name;
	private List<Question> questions;
	private QuestionsInflater questionsInflater;
	private boolean questionsLoaded = false;
	
	public QuestionCategory(int id, String name) {
		this.id = id;
		this.name = name;
		this.questions = new ArrayList<Question>();
	}
	
	public List<Question> getQuestions() {
		if (!questionsLoaded && this.questionsInflater != null && this.questions.size() == 0) {
			this.questions = questionsInflater.getQuestions(this);
			this.questionsLoaded = true;
		}
		return this.questions;
	}
	
	public void invalidateQuestions() {
		this.questions.clear();
		this.questionsLoaded = false;
	}
	
	public void setQuestionsInflater(QuestionsInflater inflater) {
		this.questionsInflater = inflater;
	}
	
	public QuestionCategory(String name) {
		this(-1, name);
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof QuestionCategory) {
			QuestionCategory other = (QuestionCategory) obj;
			if (id != other.id)
				return false;
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
	
	private static class NullObject extends QuestionCategory
	{
		NullObject() {
			super(-1, "");
		}
	}
}