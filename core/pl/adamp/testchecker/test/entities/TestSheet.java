package pl.adamp.testchecker.test.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.test.TestRow;

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
	private int totalPoints = 0;

	/**
	 * Zwraca kopi� listy pyta� bezpieczn� do iterowania w osobnym w�tku
	 * @return Lista pyta�
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
	 * Zwraca liczb� pyta�
	 * @return Liczba pyta�
	 */
	public int getQuestionsCount() {
		return questionsCount;
	}
	
	/**
	 * @return Ilo�� mo�liwych do uzyskania punkt�w
	 */
	public int getTotalPoints() {
		return totalPoints;
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
	 * Funkcja ustawia nazw� testu
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Funkcja zwraca nazw� testu
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
			totalPoints += question.getValue();
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
	
	/**
	 * Zwraca kod danego wiersza lub -1 gdy go nie znaleziono
	 */
	public int getTestRowCode(TestRow row) {
		synchronized (questions) {
			int index = questions.indexOf(row);
			if (index >= 0) return index;
		}
		synchronized (metadataRows) {
			int index = metadataRows.indexOf(row);
			if (index >= 0) return index + questionsCount;
		}
		return -1;
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
		
		test.addMetadata(new Metadata(Metadata.Type.StudentId, 5));
		
		for (TestRow row : test.questions) {
			row.setReservedSpaceSize(4);
		}
		for (TestRow row : test.metadataRows) {
			row.setReservedSpaceSize(4);
		}
		
		return test;
	}
}