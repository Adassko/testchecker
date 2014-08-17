package pl.adamp.testchecker.test.interfaces;

import java.util.List;

import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import pl.adamp.testchecker.test.entities.TestDefinition;

public interface QuestionsInflater {
	List<Question> getQuestions(QuestionCategory category);
	List<Question> getQuestions(TestDefinition test);
}