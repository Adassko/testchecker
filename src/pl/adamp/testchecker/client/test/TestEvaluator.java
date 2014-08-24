package pl.adamp.testchecker.client.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.util.Log;
import pl.adamp.testchecker.test.TestRow;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.AnswerSheet;
import pl.adamp.testchecker.test.entities.Metadata;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.TestSheet;
import pl.adamp.testchecker.test.entities.Metadata.Type;

public class TestEvaluator {
	public static TestResult getTestResults(TestSheet test, AnswerSheet answers) {
		TestResult result = new TestResult();
		
		Map<Metadata.Type, Long> data = new HashMap<Metadata.Type, Long>(); 

		List<QuestionAnswers> acceptedAnswers = answers.getAcceptedAnswers();
		for (QuestionAnswers answer : acceptedAnswers) {
			TestRow row = answer.getTestRow();
			
			if (row instanceof Question) {
				Question question = (Question)row;
				
				if (isAnswerCorrect(question, answer)) {
					result.increaseCorrect(question.getValue());
				} else {
					result.increaseIncorrect();
				}
				result.increaseTotalPoints(question.getValue());
			}
			else if (row instanceof Metadata.Row) {
				if (answer.isExactlyOneAnswerMarked()) {
					Metadata.Row metadataRow = (Metadata.Row)row;
					Metadata.Type type = metadataRow.getMetadata().getType();
					
					Long value = data.get(type);
					if (value == null) value = 0l;
					
					value = setDigitAtDecimalPosition(value, metadataRow.getPosition(), answer.getFirstMarkedAnswerId());
					
					data.put(type, value);
				}
			}
		}
		
		Long studentId = data.get(Metadata.Type.StudentId);
		if (studentId != null) {
			result.setStudentId(studentId);
		}
		
		Log.d("TestEvaluator", result.getPoints() + " / " + result.getTotalPoints());
		return result;
	}
	
	/**
	 * Podmienia cyfrê w liczbie na podanej pozycji w systemie dziesiêtnym
	 * @param number Liczba wejœciowa
	 * @param position Pozycja na której dokonaæ zmiany
	 * @param digit Cyfra któr¹ ustawiæ na danej pozycji
	 * @return Zmieniona liczba
	 */
	private static long setDigitAtDecimalPosition(long number, int position, int digit) {
		int pow = 1;
		long add = 0;
		for (int i = 0; i <= position; i ++) {
			if (i == position) {
				add = pow * digit + number % pow;
			}
			pow *= 10;
		}
		return number - number % pow + add;
	}
	
	public static boolean isAnswerCorrect(QuestionAnswers answer) {
		TestRow question = answer.getTestRow();
		
		if (question instanceof Question == false)
			return true;
		return isAnswerCorrect((Question)question, answer);
	}
	
	public static boolean isAnswerCorrect(Question question, QuestionAnswers answer) {
		List<Answer> answers = question.getAnswers();
		int answersCount = answers.size();
		int markedCorrectly = 0;
		int markedIncorrectly = 0;
		int notMarked = 0;
		for (int i = 0; i < answersCount; i ++) {
			boolean marked = answer.isMarkedAnswer(i);
			boolean correct = answers.get(i).isCorrect();
			
			if (marked) {
				if (correct)
					markedCorrectly ++;
				else
					markedIncorrectly ++;
			} else
				if (correct)
					notMarked ++;
		}
		
		return markedCorrectly > 0 && markedIncorrectly == 0;			
	}
}
