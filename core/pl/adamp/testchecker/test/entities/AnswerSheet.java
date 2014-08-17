package pl.adamp.testchecker.test.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.adamp.testchecker.test.TestReader;

public class AnswerSheet {
	private final QuestionAnswers[] answers;
	private final int size;
	
	public AnswerSheet(int maxAnswers) {
		size = maxAnswers;
		answers = new QuestionAnswers[size];
	}
	
	public AnswerSheet() {
		 this(TestReader.MAX_TEST_ROWS);
	}
	
	public synchronized QuestionAnswers getAcceptedAnswer(int id) {
		return answers[id];
	}
	
	public synchronized void addAcceptedAnswer(QuestionAnswers answer) {
		answers[answer.getTestRowId()] = answer;
	}
	
	public synchronized List<QuestionAnswers> getAcceptedAnswers() {
		List<QuestionAnswers> result = new ArrayList<QuestionAnswers>();
		
		for (QuestionAnswers answer : answers) {
			if (answer != null)
				result.add(answer);			
		}		
		return result;
	}	
}