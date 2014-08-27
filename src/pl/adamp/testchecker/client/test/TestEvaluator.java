package pl.adamp.testchecker.client.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.util.Log;
import android.util.SparseArray;
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
		result.setTestId(test.getId());
		result.setVariant(test.getVariant());
		
		Map<Metadata.Type, Long> data = new HashMap<Metadata.Type, Long>(); 

		List<QuestionAnswers> acceptedAnswers = answers.getAcceptedAnswers();
		for (QuestionAnswers answer : acceptedAnswers) {
			TestRow row = answer.getTestRow();
			
			if (row instanceof Question) {
				Question question = (Question)row;

				int points = getAnswerPoints(question, answer);
				answer.setPoints(points);
				if (points > 0) {
					result.increaseCorrect(points);
				} else {
					result.increaseIncorrect();
				}
			}
			else if (row instanceof Metadata.Row) {
				if (answer.isExactlyOneAnswerMarked()) {
					Metadata.Row metadataRow = (Metadata.Row)row;
					Metadata metadata = metadataRow.getMetadata();
					Metadata.Type type = metadata.getType();
					
					Long value = data.get(type);
					if (value == null) value = 0l;
					
					value = setDigitAtDecimalPosition(value, metadata.getLength() - 1 - metadataRow.getPosition(),
							answer.getFirstMarkedAnswerId());
					
					data.put(type, value);
				}
			}
		}
		
		Long studentId = data.get(Metadata.Type.StudentId);
		if (studentId != null) {
			result.setStudentId(studentId);
		}
		
		result.setMaxPoints(test.getTotalPoints());
		result.setQuestionsCount(test.getQuestionsCount());
		
		Log.d("TestEvaluator", result.getPoints() + " / " + result.getMaxPoints());
		return result;
	}
	
	public static TestResult getTestResults(TestSheet test, AnswerSheet answer, SparseArray<String> gradingTable) {
		TestResult result = getTestResults(test, answer);
		int percent = 100;
		if (result.getMaxPoints() > 0) {
			percent = result.getTotalPoints() * 100 / result.getMaxPoints();
		}
		result.setGrade(gradeForPercent(percent, gradingTable));
		return result;
	}
	
	public static String gradeForPercent(int percent, SparseArray<String> gradingTable) {
		int size = gradingTable.size();
		String grade = "";
		for (int i = 0; i < size; i ++) {
			if (i > 0 && gradingTable.keyAt(i) > percent)
				break;
			grade = gradingTable.valueAt(i);
		}
		return grade;
	}
	
	/**
	 * Pobiera z udzielonych odpowiedzi identyfikator studenta
	 */
	public static long getStudentId(AnswerSheet answers) {
		List<QuestionAnswers> acceptedAnswers = answers.getAcceptedAnswers();
		long studentId = 0;
		
		for (QuestionAnswers answer : acceptedAnswers) {
			TestRow row = answer.getTestRow();
			
			if (row instanceof Metadata.Row) {
				Metadata.Row metadataRow = (Metadata.Row)row;
				Metadata metadata = metadataRow.getMetadata();
				Metadata.Type type = metadata.getType();
				if (type != Type.StudentId) continue;
				
				if (answer.isExactlyOneAnswerMarked()) {
					studentId = setDigitAtDecimalPosition(studentId,
							metadata.getLength() - 1 - metadataRow.getPosition(), answer.getFirstMarkedAnswerId());
				}
			}
		}
		
		return studentId;
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
		return getAnswerPoints((Question)question, answer) > 0;
	}
	
	public static int getAnswerPoints(Question question, QuestionAnswers answer) {
		List<Answer> answers = question.getAnswers();
		int value = question.getValue();
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
		
		if (markedCorrectly == 0) return 0;
		return Math.round(Math.max(0, markedCorrectly - markedIncorrectly / 2f) * value
				/ (markedCorrectly + markedIncorrectly + notMarked));
	}
}
