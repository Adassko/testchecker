package pl.adamp.testchecker.test;

import java.util.ArrayList;
import java.util.List;

public class Test {
	List<Question> questions;
	int variant;
	
	public int getVariant() {
		return variant;
	}

	public void setVariant(int variant) {
		this.variant = variant;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public Test() {
		this.setVariant(1);
		this.questions = new ArrayList<Question>(20);
	}
	
	public void AddQuestion(Question question) {
		this.questions.add(question);
	}
	
	public static Test getSampleInstance() {
		Test test = new Test();
		Question q = new Question("Pytanie pierwsze");
		q.AddAnswer("Odp 1", true);
		q.AddAnswer("Odp 2", false);
		q.AddAnswer("Odp 3", false);
		q.AddAnswer("Odp 4", false);
		test.AddQuestion(q);
		
		q = new Question("Pytanie drugie");
		q.AddAnswer("Odp 2.1", false);
		q.AddAnswer("Odp 2.2", true);
		q.AddAnswer("Odp 2.3", false);
		q.AddAnswer("Odp 2.4", false);
		test.AddQuestion(q);
		
		return test;
	}
}