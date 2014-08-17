package pl.adamp.testchecker.test.entities;

import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.test.TestRow;
import android.util.Pair;
import android.util.SparseArray;

public class TestSheet implements Serializable {
	private static final long serialVersionUID = -2301572555540307633L;

	private List<Question> questions;
	private List<Metadata> metadata;
	private List<Metadata.Row> metadataRows;
	private int questionsCount = 0;
	private int metadataRowsCount = 0;
	private final int variant;	
	private final int id;
	private String name;

	/**
	 * Zwraca kopiê listy pytañ bezpieczn¹ do iterowania w osobnym w¹tku
	 * @return Lista pytañ
	 */
	public List<Question> getQuestions() {
		synchronized (questions) {
			List<Question> result = new ArrayList<Question>();
			for (Question question : questions) {
				result.add(question);
			}
			return result;
		}
	}
	
	/**
	 * Zwraca liczbê pytañ
	 * @return Liczba pytañ
	 */
	public int getQuestionsCount() {
		return questionsCount;
	}
	
	public List<Metadata> getMetadata() {
		synchronized (metadata) {
			List<Metadata> result = new ArrayList<Metadata>();
			for (Metadata metadatum : metadata) {
				result.add(metadatum);
			}
			return result;
		}
	}

	public TestSheet(String name, int id, int variant) {
		this.metadataRows = new ArrayList<Metadata.Row>(10);
		this.metadata = new ArrayList<Metadata>(1);
		this.questions = new ArrayList<Question>(20);
		this.id = id;
		this.variant = variant;
		this.name = name;
	}
	
	/**
	 * Funkcja ustawia nazwê testu
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Funkcja zwraca nazwê testu
	 * @return Nazwa testu
	 */
	public String getName() {
		return this.name;
	}

	public Question addQuestion(String question) {
		return addQuestion(new Question(question));
	}
	
	public Question addQuestion(Question question) {
		synchronized (questions) {
			questions.add(question);
			questionsCount = questions.size();
			return question;
		}
	}
	
	public void addMetadata(Metadata metadata) {
		synchronized (metadataRows) {
			this.metadata.add(metadata);
			metadataRows.addAll(metadata.getTestRows());
			metadataRowsCount = metadataRows.size();
		}
	}

	public int getId() {
		return this.id;
	}
	
	public int getVariant() {
		return this.variant;
	}

	public int getTestRowsCount() {
		return metadataRowsCount + questionsCount;
	}

	public TestRow getTestRow(int id) {
		if (id >= questionsCount) {
			id -= questionsCount;
			synchronized (metadataRows) {
				return metadataRows.get(id);
			}
		} else {
			synchronized (questions) {
				return questions.get(id);
			}
		}
	}
	
	public int getAnswersCountForRow(int row) {
		TestRow testRow = getTestRow(row);
		if (testRow != null)
			return testRow.getAnswersCount();
		return 0;
	}

	public int getReservedSpaceSizeForRow(int row) {
		TestRow testRow = getTestRow(row);
		if (testRow != null)
			return testRow.getReservedSpaceSize();
		return 0;
	}

	public static TestSheet getSampleInstance() {
		TestSheet test = new TestSheet("Sample test", 1, 1);
		Question q = test.addQuestion("Pytanie pierwsze");
		q.addAnswer("Odp 1.1").setCorrect(true);
		q.addAnswer("Odp 1.2");
		q.addAnswer("Odp 1.3");
		q.addAnswer("Odp 1.4");

		q = test.addQuestion("Pytanie drugie");
		q.addAnswer("Odp 2.1");
		q.addAnswer("Odp 2.2").setCorrect(true);
		q.addAnswer("Odp 2.3");
		q.addAnswer("Odp 2.4");

		q = test.addQuestion("Pytanie trzecie");
		q.addAnswer("Odp 3.1");
		q.addAnswer("Odp 3.2");
		q.addAnswer("Odp 3.3");
		q.addAnswer("Odp 3.4").setCorrect(true);

		for (int i = 4; i <= 10; i ++) {
			q = test.addQuestion("Pytanie " + i);
			q.addAnswer(i + ".1").setCorrect(true);
			q.addAnswer(i + ".2");
			q.addAnswer(i + ".3");
			q.addAnswer(i + ".4");
		}
		
		test.addMetadata(new Metadata(Metadata.Type.StudentId, 4));
		
		return test;
	}
}