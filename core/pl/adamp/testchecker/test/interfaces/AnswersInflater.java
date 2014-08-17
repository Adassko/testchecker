package pl.adamp.testchecker.test.interfaces;

import java.util.List;

import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Question;

public interface AnswersInflater {
	List<Answer> getAnswers(Question question);
}