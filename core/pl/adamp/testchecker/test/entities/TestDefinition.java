package pl.adamp.testchecker.test.entities;

import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import pl.adamp.testchecker.test.TestRow;
import pl.adamp.testchecker.test.entities.Metadata.Type;
import pl.adamp.testchecker.test.interfaces.QuestionsInflater;
import android.util.Pair;
import android.util.SparseArray;

public class TestDefinition implements Serializable {
	private static final long serialVersionUID = -2301572555540307633L;

	private List<Question> questions;
	private boolean shuffleQuestions;
	private boolean shuffleAnswers;
	private String name;
	private int studentIdLength;
	private QuestionsInflater questionInflater;
	private int id;
	private int questionsCount = -1;
	private Date modifyDate;

	public TestDefinition(String name) {
		this(-1, name);
	}
	
	public TestDefinition(int id, String name) {
		this.questions = new ArrayList<Question>(20);
		this.id = id;
		this.studentIdLength = 0;
		this.name = name;
		this.modifyDate = new Date();
	}
	
	/**
	 * Zwraca nazw� testu
	 * @return Nazwa testu
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Ustawia dat� modyfikacji testu
	 * @param date Data modyfikacji
	 */
	public void setModifyDate(Date date) {
		this.modifyDate = date;
	}
	
	/**
	 * Zwraca dat� modyfikacji testu
	 * @return Data modyfikacji
	 */
	public Date getModifyDate() {
		return this.modifyDate;
	}
	
	/**
	 * Ustawia nazw� testu
	 * @param name Nowa nazwa testu
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Pobiera list� pyta� w te�cie
	 * <p>
	 * Lista pyta� jest �adowana leniwie co oznacza �e pierwsze wywo�anie
	 * tej funkcji mo�e spowodowa� zapytanie do bazy danych
	 * @return Lista dost�pnych pyta�
	 */
	public List<Question> getQuestions() {
		if (questionInflater != null && questions.size() == 0) {
			questions = questionInflater.getQuestions(this);
		}
		
		return questions;
	}
	
	/**
	 * Funkcja zwraca ilo�� pyta� kt�re maj� by� w wygenerowanym te�cie
	 * (ilo�� ta mo�e by� mniejsza ni� dost�pna ilo�� pyta� w te�cie)
	 * @return Ilo�� odpowiedzi na gotowym arkuszu testowym 
	 */
	public int getQuestionsCount() {
		return this.questionsCount;
	}
	
	/**
	 * Funkcja ogranicza ilo�� pyta� na generowanym te�cie do wylosowania z puli dost�pnych pyta� 
	 * @param count Ilo�� pyta� na wygenerowanym te�cie
	 */
	public void setQuestionsCount(int count) {
		this.questionsCount = count;
	}
	
	/**
	 * Funkcja ustawia czy miesza� pytania podczas generowania wariantu testu
	 * @param shuffle True je�li pytania maj� by� pomieszane
	 */
	public void shuffleQuestions(boolean shuffle) {
		this.shuffleQuestions = shuffle;
	}
	
	/**
	 * Funkcja ustawia czy miesza� odpowiedzi w pytaniach podczas generowania wariantu testu
	 * @param shuffle True je�li odpowiedzi na te�cie maj� by� pomieszane
	 */
	public void shuffleAnswers(boolean shuffle) {
		this.shuffleAnswers = shuffle;
	}
	
	/**
	 * Funkcja zwraca aktualne ustawienie mieszania pyta�
	 * @return Aktualne ustawienie mieszania pyta�
	 */
	public boolean getShuffleQuestions() {
		return this.shuffleQuestions;
	}
	
	/**
	 * Funkcja zwraca aktualne ustawienie mieszania odpowiedzi
	 * @return Aktualne ustawienie mieszania odpowiedzi
	 */
	public boolean getShuffleAnswers() {
		return this.shuffleAnswers;
	}
	
	/**
	 * Funkcja generuje wariant testu zwracaj�c ustawion� liczb� pyta� i mieszaj�c je w zale�no�ci od ustawie�
	 * @param variant Numer wariantu testu do wygenerowania
	 * @return Arkusz testowy
	 * @see TestSheet
	 * @see #shuffleAnswers(boolean)
	 * @see #shuffleQuestions(boolean)
	 */
	public TestSheet getTestSheet(int variant) {
		TestSheet result = new TestSheet(this.name, this.id, variant);
		
		// generator liczb losowych z ziarnem zale�nym od identyfikatora i wariantu testu
		// standard gwarantuje identyczn� kolejno�� pyta� i odpowiedzi przy ka�dym "losowaniu" przy tym samym ziarnie
		final int prime = 31;
		Random randomGenerator = new Random(this.id * prime + variant);
		
		List<Question> questions = new ArrayList<Question>(this.getQuestions());
		
		if (shuffleQuestions)
			Collections.shuffle(questions, randomGenerator); // przetasuj pytania
		
		int count = this.questionsCount;
		int availableCount = questions.size();
		if (count < 0 || count > availableCount)
			count = availableCount; // je�li nie ustalono liczby pyta� w te�cie, pobierz wszystkie pytania
		
		int maxAnswersCount = 0;
		for (int i = 0; i < count; i ++) {
			Question question = new Question(questions.get(i)); // sklonuj pytanie przed modyfikacjami
			if (shuffleAnswers) {
				question = new Question(question);
				Collections.shuffle(question.getAnswers(), randomGenerator);
			}
			result.addQuestion(question);
			
			int answersCount = question.getAnswersCount();
			if (answersCount > maxAnswersCount)
				maxAnswersCount = answersCount;
		}
		
		for(Question q : result.getQuestions()) {
			q.setReservedSpaceSize(maxAnswersCount); // wyr�wnaj kratki odpowiedzi na arkuszu testowym
		}
		
		if (studentIdLength > 0)
			result.addMetadata(new Metadata(Type.StudentId, studentIdLength));
		
		return result;
	}
	
	/**
	 * Ustawia identyfikator testu
	 * @param id Nowy identyfikator testu
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Pobiera identyfikator testu
	 * @return Identyfikator testu
	 */
	public int getId() {
		return this.id;
	}
}