package pl.adamp.testchecker.test.entities;

import pl.adamp.testchecker.test.TestRow;
import java.io.Serializable;

public class QuestionAnswers implements Serializable {
	private static final long serialVersionUID = 1873662026412815095L;

	private int possibleMistakes; // pole bitowe
	private int answers;
	private boolean markedByUser;
	private boolean trustworthy;
	private final boolean isMetadata;
	private final int testRowId;
	private final TestRow testRow;
	private boolean expanded;
	private int questionValue;
	private int points;

	/**
	 * @return Identyfikator wiersza na te�cie do kt�rego nale�y ta odpowied�
	 */
	public int getTestRowId() {
		return testRowId;
	}

	/**
	 * @return Pobiera powi�zany z odpowiedzi� wiersz odpowiedzi na te�cie
	 */
	public TestRow getTestRow() {
		return testRow;
	}
	
	/**
	 * @return True je�li odpowied� dotyczy metadanych (pytanie niepunktowane)
	 */
	public boolean isMetadata() {
		return isMetadata;
	}
	
	/**
	 * @return Lista zaznaczonych odpowiedzi jako maska bitowa (pierwsza odpowied� na najm�odszym bicie)
	 */
	public int getMarkedAnswers() {
		return answers;
	}
	
	/**
	 * Ustawia liste zaznaczonych odpowiedzi
	 * @param answers Odpowiedzi (jako maska bitowa - pierwsza odpowied� na najm�odszym bicie)
	 */
	public void setMarkedAnswers(int answers) {
		this.answers = answers;
	}
	
	/**
	 * @return Pozycja pierwszej zaznaczonej odpowiedzi
	 */
	public Integer getFirstMarkedAnswerId() {
		if (answers == 0)
			return null;
		return Integer.numberOfTrailingZeros(answers);
	}
	
	/**
	 * @return True je�li zaznaczono dok�adnie jedno pole odpowiedzi
	 */
	public boolean isExactlyOneAnswerMarked() {
		return Integer.bitCount(answers) == 1;
	}

	/**
	 * @return Lista potencjalnie b��dnie zaznaczonych odpowiedzi
	 */
	public int getPossibleMistakes() {
		return possibleMistakes;
	}

	/**
	 * Ustawia �e odpowied� zosta�a zaznaczona przez u�ytkownika i nie powinna by� zmieniona przez skaner
	 */
	public void setMarkedByUser(boolean value) {
		this.markedByUser = value;
	}

	/**
	 * @return True je�li odpowied� zosta�a zaznaczona przez u�ytkownika i nie powinna by� zmieniona przez skaner
	 */
	public boolean isMarkedByUser() {
		return this.markedByUser;
	}

	/**
	 * @return True je�li odpowied� jest godna zaufania (wielokrotnie potwierdzona przy skanowaniu)
	 */
	public boolean isTrustworthy() {
		return this.markedByUser || this.trustworthy;
	}

	/**
	 * Ustaje odpowied� za wiarygodn�
	 */
	public void trust() {
		trustworthy = true;
	}

	/**
	 * Oznacza konkretn� kratk� w pytaniu jako zaznaczon� 
	 * @param id Pozycja kratki w pytaniu
	 */
	public void addMarkedAnswer(int id) {
		answers |= 1 << id;
	}
	
	/**
	 * Usuwa zaznaczenie odpowiedzi na danej pozycji
	 * @param id Pozycja kratki w pytaniu
	 */
	public void removeMarkedAnswer(int id) {
		answers &= ~(1 << id);
	}

	/**
	 * @param id Pozycja odpowiedzi
	 * @return True je�li zaznaczono kratk� odpowiedzi w pytaniu na danej pozycji
	 */
	public boolean isMarkedAnswer(int id) {
		return ((answers >> id) & 1) == 1;
	}

	/**
	 * Oznacza kratk� odpowiedzi w pytaniu jako potencjalnie b��dnie zaznaczon� 
	 * @param id Pozycja kratki
	 */
	public void addPossibleMistake(int id) {
		answers |= 1 << id;
		possibleMistakes |= 1 << id;
	}

	/**
	 * Sprawdza czy zaznaczona na danej pozycji kratka zosta�a zaznaczona przez pomy�k� 
	 * @param id Pozycja kratki w pytaniu
	 * @return True je�li kratka zosta�a zaznaczona i oznaczona jako b��d
	 */
	public boolean isPossibleMistake(int id) {
		return ((possibleMistakes >> id) & 1) == 1;
	}

	public QuestionAnswers(int testRowId, TestRow testRow) {
		this.testRow = testRow;
		isMetadata = testRow instanceof Metadata.Row;
		this.testRowId = testRowId;
		this.markedByUser = false;
	}
	
	/**
	 * @return Czy lista powinna by� rozwini�ta (podgl�d wynik�w)
	 */
	public boolean isExpanded() {
		return expanded;
	}
	
	/**
	 * Rozwija list� odpowiedzi (w podgl�dzie wynik�w)
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	/**
	 * @param points Liczba punkt�w uzyskanych za t� odpowied�
	 */
	public void setPoints(int points) {
		this.points = points;
	}
	
	public void setQuestionValue(int points) {
		this.questionValue = points;
	}
	
	public int getQuestionValue() {
		return this.questionValue;
	}
	
	/**
	 * @return Lista punkt�w uzyskanych za t� odpowied�
	 */
	public int getPoints() {
		return this.points;
	}

	@Override
	public final boolean equals(Object other) {
		if (other instanceof QuestionAnswers) {
			QuestionAnswers otherResult = (QuestionAnswers)other;
			return testRowId == otherResult.testRowId && answers == otherResult.answers &&
					possibleMistakes == otherResult.possibleMistakes;
		}
		return false;
	}

	@Override
	public final int hashCode() {
		int hash = testRowId;
		hash = 31 * hash + answers;
		hash = 31 * hash + possibleMistakes;
		return hash;
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder("Q" + testRowId + "(");

		int tmp = this.answers;
		while (tmp > 0) {
			builder.append((tmp & 1) > 0 ? "X" : "_");
			tmp >>= 1;
		}
		builder.append(")");
		return builder.toString();
	}
}
