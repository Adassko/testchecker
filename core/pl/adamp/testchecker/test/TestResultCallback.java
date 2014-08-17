package pl.adamp.testchecker.test;

import pl.adamp.testchecker.test.entities.AnswerSheet;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.TestSheet;

public interface TestResultCallback {
	void foundPossibleAnswer(QuestionAnswers answer);
	void foundArea(TestArea area);
	AnswerSheet getAnswerSheet();	
	TestSheet getTestSheet();
}
