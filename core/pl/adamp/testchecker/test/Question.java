package pl.adamp.testchecker.test;

import java.util.ArrayList;
import java.util.List;

import android.util.Pair;

public class Question {
	String question;
	List<Pair<String, Boolean>> answers;
	
	public Question(String question) {
		this.question = question;
		this.answers = new ArrayList<Pair<String, Boolean>>(4);
	}
	
	public void AddAnswer(String answer, boolean isCorrect) {
		answers.add(new Pair<String, Boolean>(answer, isCorrect));		
	}

	public String getQuestion() {
		return question;
	}

	public List<Pair<String, Boolean>> getAnswers() {
		return answers;
	}
}
