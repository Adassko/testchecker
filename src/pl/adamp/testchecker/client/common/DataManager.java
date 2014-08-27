package pl.adamp.testchecker.client.common;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.adamp.testchecker.client.common.DBHelper.Reader;
import pl.adamp.testchecker.client.common.DBHelper.RowReader;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.client.test.TestResult;
import pl.adamp.testchecker.test.TestRow;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import pl.adamp.testchecker.test.entities.Student;
import pl.adamp.testchecker.test.entities.TestSheet;
import pl.adamp.testchecker.test.interfaces.AnswersInflater;
import pl.adamp.testchecker.test.interfaces.QuestionsInflater;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

public class DataManager implements QuestionsInflater, AnswersInflater {
	private static final String TAG = DataManager.class.getSimpleName();
	private DBHelper db;
	private DataManager instance;

	public DataManager(Context context) {
		db = new DBHelper(context);
		instance = this;
	}

	/**
	 * Zwraca list� pyta� wraz z odpowiedziami dla danego testu
	 * @param test Definicja testu dla kt�rego ma by� zwr�cona lista dost�pnych pyta�
	 * @return Lista dost�pnych pyta� dla testu
	 */
	@Override
	public List<Question> getQuestions(TestDefinition test) {
		final int testId = test.getId();		
		final List<Question> result = new ArrayList<Question>();
		final SparseArray<Question> questionsById = new SparseArray<Question>();
		db.select(
				"SELECT q.Id, q.QuestionCategoryId, q.Text, tq.Value, a.Id AnswerId, a.Text AnswerText, a.IsCorrect"
				+ "	FROM TestQuestions tq"
				+ "	JOIN Questions q ON q.Id = tq.QuestionId"
				+ "	LEFT JOIN Answers a ON a.QuestionId = q.Id"
				+ "	WHERE tq.TestId = ?"
				+ "	ORDER BY tq.Ordinal, q.Id, a.Id",
				vals(testId),
				new RowReader() {
					public void readRow(Reader r) {
						int questionId = r.getInt("Id");
						
						Question q = questionsById.get(questionId);
						if (q == null) {
							q = new Question(r.getInt("Id"), r.getString("Text"), r.getInt("Value"));
							questionsById.put(questionId, q);
							result.add(q);
						}
						
						if (!r.isNull("AnswerId")) {
							Answer a = new Answer(r.getInt("AnswerId"), r.getString("AnswerText"), r.getBool("IsCorrect"));
							q.addAnswer(a);
						}
					}
				});
		return result;
	}
	
	/**
	 * Zwraca instancj� pytania na podstawie identyfikatora
	 * @param questionId Identyfikator pytania
	 * @return Pytanie
	 */
	public Question getQuestion(int questionId) {
		final Question[] result = new Question[] { null };
		db.select(
			new String[] { "Id", "Text", "DefaultValue" }, // columns
			"Questions", // FROM
			"Id = ?", // WHERE
			vals(questionId), // WHERE args
			null, // ORDER BY
			new RowReader() {
				public void readRow(Reader r) {
					Question q = new Question(r.getInt("Id"), r.getString("Text"), r.getInt("DefaultValue"));
					q.setAnswersInflater(instance);
					result[0] = q;
				}
		});
		return result[0];
	}
	
	public SparseArray<String> getGradingTable() {
		SparseArray<String> result = new SparseArray<String>();
		
		result.put(0, "1");
		result.put(30, "2");
		result.put(50, "3");
		result.put(70, "4");
		result.put(90, "5");
		
		return result;
	}
	
	/**
	 * Zwraca list� odpowiedzi dla pytania
	 * @param question Pytanie dla kt�rego zwr�ci� odpowiedzi
	 * @return Lista odpowiedzi
	 */
	@Override
	public List<Answer> getAnswers(Question question) {
		final int questionId = question.getId();		
		final List<Answer> result = new ArrayList<Answer>();
		db.select(
				new String[] { "Id", "Text", "IsCorrect" }, // columns
				"Answers", // FROM
				"QuestionId = ?", // WHERE
				vals(questionId), // WHERE args
				"Id", // ORDER BY
				new RowReader() {
					public void readRow(Reader r) {
						Answer a = new Answer(r.getInt("Id"), r.getString("Text"), r.getBool("IsCorrect"));
						result.add(a);
					}
				});
		return result;
	}
	
	/**
	 * Funkcja zapisuje odpowiedzi do pytania
	 * @param question Pytanie w kt�rym dokonywano modyfikacji
	 * @return True je�li aktualizacja bazy si� powiod�a
	 */
	public boolean saveAnswers(Question question) {
		Set<Answer> oldAnswers = new HashSet<Answer>(getAnswers(question)); // pytania z bazy danych
		for (Answer answer : question.getAnswers()) {
			answer = saveAnswer(question, answer);
			if (answer == null)
				return false;
			oldAnswers.remove(answer);
		}

		// usu� pytania kt�re by�y w bazie danych ale nie by�y w li�cie
		for (Answer answer : oldAnswers) {
			if (deleteAnswer(answer) == false)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Funkcja dodaje lub aktualizuje odpowied�
	 * @param question Pytanie do kt�rego nale�y odpowied�
	 * @param answer Pytanie do zapisu
	 * @return Instancja odpowiedzi uzupe�niona o identyfikator lub NULL w przypadku b��du
	 */
	public Answer saveAnswer(Question question, Answer answer) {
		ContentValues values = new ContentValues();
		values.put("Text", answer.getText());
		values.put("IsCorrect", answer.isCorrect() ? 1 : 0);
		values.put("QuestionId", question.getId());
		int answerId = answer.getId();
		if (answerId >= 0) {
			int updatedRows = db.update("Answers", values, "Id = ?", vals(answerId));
			
			if (updatedRows == 1)
				return answer;
		} else {
			int id = (int)db.insert("Answers", values);
			
			if (id >= 0) {
				answer.setId(id);
				return answer;
			}
		}
		return null;
	}

	/**
	 * Zwraca list� pyta� w danej kategorii
	 * @param category	Kategoria nadrz�dna
	 * @return Lista pyta�
	 */
	@Override
	public List<Question> getQuestions(QuestionCategory category) {
		final int categoryId = category.getId();		
		final List<Question> result = new ArrayList<Question>();
		db.select(
				new String[] { "Id", "Text", "DefaultValue" }, // columns
				"Questions", // FROM
				"QuestionCategoryId " + (categoryId >= 0 ? "=" + categoryId : "IS NULL"), // WHERE
				null, // WHERE args
				"Text, Id", // ORDER BY
				new RowReader() {
					public void readRow(Reader r) {
						Question q = new Question(r.getInt("Id"), r.getString("Text"), r.getInt("DefaultValue"));
						q.setAnswersInflater(instance);
						result.add(q);
					}
				});
		return result;
	}
	
	/**
	 * Zwraca kategori� do kt�rej nale�y pytanie
	 * @param question Pytanie
	 * @return Kategoria do kt�rej nale�y pytanie
	 */
	public QuestionCategory getQuestionCategory(Question question) {
		final QuestionCategory[] result = new QuestionCategory[] { QuestionCategory.DefaultCategory };
		
		db.select(
				"SELECT c.Id, c.Name"
						+ "	FROM Questions q"
						+ "	JOIN QuestionCategories c ON c.Id = q.QuestionCategoryId"
						+ "	WHERE q.Id = ?",
				vals(question.getId()),
				new RowReader() {
					public void readRow(Reader r) {
						QuestionCategory x = new QuestionCategory(r.getInt("Id"), r.getString("Name"));
						x.setQuestionsInflater(instance);
						result[0] = x;
					}
				});
		QuestionCategory defaultCategory = QuestionCategory.DefaultCategory;
		defaultCategory.setQuestionsInflater(this);
		return result[0];
	}
	
	/**
	 * Funkcja dodaje lub aktualizuje pytanie.
	 * <p>
	 * Odpowiedzi do pytania nie s� zapisywane. Aby zapisa� odpowiedzi wywo�aj {@link #saveAnswers(Question)}
	 * @param question	Pytanie do zapisania
	 * @param questionCategory	Kategoria do kt�rej ma nale�e� pytanie
	 * @return Instancja pytania uzupe�niona o identyfikator lub NULL w przypadku b��du
	 * @see #saveAnswers(Question)
	 */
	public Question saveQuestion(Question question, QuestionCategory questionCategory) {
		ContentValues values = new ContentValues();
		values.put("Text", question.getQuestion());
		values.put("DefaultValue", question.getValue());
		int categoryId = questionCategory.getId();
		values.put("QuestionCategoryId", categoryId >= 0 ? categoryId : null);
		if (question.getId() >= 0) {
			int updatedRows = db.update("Questions", values, "Id = ?", vals(question.getId()));
			
			if (updatedRows == 1)
				return question;
		} else {
			int id = (int)db.insert("Questions", values);
			
			if (id >= 0) {
				question.setId(id);
				return question;
			}
		}
		return null;
	}
	
	/**
	 * Usuwa pytanie
	 * @param question Pytanie do usuni�cia
	 * @return True je�li usuwanie si� powiod�o
	 */
	public boolean deleteQuestion(Question question) {
		return db.delete("Questions", "Id = ?", vals(question.getId())) == 1;
	}

	/**
	 * Usuwa odpowied� z pytania
	 * @param answer Odpowied� do usuni�cia
	 * @return True je�li usuwanie si� powiod�o
	 */
	public boolean deleteAnswer(Answer answer) {
		return db.delete("Answers", "Id = ?", vals(answer.getId())) == 1;
	}

	/**
	 * Funkcja dodaje lub aktualizuje kategori�
	 * @param questionCategory Kategoria do zapisania
	 * @return Instancja kategorii uzupe�niona o identyfikator lub NULL w przypadku b��du
	 */
	public QuestionCategory saveQuestionCategory(QuestionCategory questionCategory) {
		ContentValues values = new ContentValues();
		values.put("Name", questionCategory.getName());
		if (questionCategory.getId() >= 0) {
			int updatedRows = db.update("QuestionCategories", values, "Id = ?", vals(questionCategory.getId()));
			
			if (updatedRows == 1)
				return questionCategory;
		} else {
			int id = (int)db.insert("QuestionCategories", values);

			questionCategory.setId(id);
			if (id >= 0) {
				return questionCategory;
			}
		}
		return null;
	}
	
	/**
	 * Zwraca list� dost�pnych definicji test�w
	 * @return Lista dost�pnych test�w
	 */
	public List<TestDefinition> getTests() {
		final List<TestDefinition> result = new ArrayList<TestDefinition>();

		db.select(new String[] { "Id", "Name", "ModifyDate", "QuestionsCount", "ShuffleQuestions", "ShuffleAnswers", "Printed", "StudentIdLength" },
				"Tests", // FROM
				null, null, // WHERE
				"ModifyDate DESC", // ORDER BY
				new RowReader() {
					public void readRow(Reader r) {
						TestDefinition test = new TestDefinition(r.getInt("Id"), r.getString("Name"));
						test.setModifyDate(new Date(1000L * r.getInt("ModifyDate")));
						test.setQuestionsCount(r.getInt("QuestionsCount"));
						test.shuffleQuestions(r.getBool("ShuffleQuestions"));
						test.shuffleAnswers(r.getBool("ShuffleAnswers"));
						test.printed(r.getBool("Printed"));
						test.setStudentIdLength(r.getInt("StudentIdLength"));
						test.setQuestionInflater(instance);
						result.add(test);
					}
				});
		
		return result;
	}
	
	/**
	 * Zwraca test o danym identyfikatorze (NULL w przypadku b��du)
	 * @param id Identyfikator testu
	 * @return Instancja testu
	 */
	public TestDefinition getTest(int id) {
		final TestDefinition[] result = new TestDefinition[] { null };

		db.select(new String[] { "Id", "Name", "ModifyDate", "QuestionsCount", "ShuffleQuestions", "ShuffleAnswers", "Printed", "StudentIdLength" },
				"Tests", // FROM
				"Id = ?", vals(id), // WHERE
				null, // ORDER BY
				new RowReader() {
					public void readRow(Reader r) {
						TestDefinition test = new TestDefinition(r.getInt("Id"), r.getString("Name"));
						test.setModifyDate(new Date(1000L * r.getInt("ModifyDate")));
						test.setQuestionsCount(r.getInt("QuestionsCount"));
						test.shuffleQuestions(r.getBool("ShuffleQuestions"));
						test.shuffleAnswers(r.getBool("ShuffleAnswers"));
						test.printed(r.getBool("Printed"));
						test.setStudentIdLength(r.getInt("StudentIdLength"));
						test.setQuestionInflater(instance);
						result[0] = test;
					}
				});
		
		return result[0];
	}
	
	/**
	 * Pobiera rezultat testu o danym ID
	 * @param id ID rezultatu testu
	 * @return Instancja rezultatu testu lub NULL w przypadku b��du
	 */
	public TestResult getTestResult(int id) {
		final TestResult[] result = new TestResult[] { null };

		db.select(new String[] { "Id", "TestId", "StudentId", "Date", "Points", "AdditionalPoints", "CorrectAnswers",
				"IncorrectAnswers", "Grade", "MaxPoints", "Variant" },
				"TestResults", // FROM
				"Id = ?", vals(id), // WHERE
				null, // ORDER BY,
				new RowReader() {
			public void readRow(Reader r) {
				TestResult testResult = new TestResult(r.getInt("Id"));
				
				testResult.setTestId(r.getInt("TestId"));
				testResult.setStudentId(r.getInt("StudentId"));
				testResult.setDate(new Date(1000L * r.getInt("Date")));
				testResult.setPoints(r.getInt("Points"));
				testResult.setAdditionalPoints(r.getInt("AdditionalPoints"));
				testResult.setCorrect(r.getInt("CorrectAnswers"));
				testResult.setIncorrect(r.getInt("IncorrectAnswers"));
				testResult.setGrade(r.getString("Grade"));
				testResult.setMaxPoints(r.getInt("MaxPoints"));
				testResult.setVariant(r.getInt("Variant"));
				
				result[0] = testResult;
			}
		});

		return result[0];
	}
	
	/**
	 * Zapisuje reszultat testu
	 * @return Instancja rezultatu uzupe�niona o ID, lub NULL w przypadku b��du
	 */
	public TestResult saveTestResult(TestResult result) {
		ContentValues values = new ContentValues();
		values.put("TestId", result.getTestId());
		values.put("Variant", result.getVariant());
		values.put("StudentId", result.getStudentId());
		values.put("Date", result.getDate().getTime() / 1000L);
		values.put("Points", result.getPoints());
		values.put("AdditionalPoints", result.getAdditionalPoints());
		values.put("CorrectAnswers", result.getCorrect());
		values.put("IncorrectAnswers", result.getIncorrect());
		values.put("Grade", result.getGrade());
		values.put("MaxPoints", result.getMaxPoints());
		
		if (result.getId() >= 0) {
			int updatedRows = db.update("TestResults", values, "Id = ?", vals(result.getId()));
			
			if (updatedRows == 1)
				return result;
		} else {
			int id = (int)db.insert("TestResults", values);
			
			result.setId(id);
			if (id >= 0) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Zapisuje zaznaczone odpowiedzi do pytania na konkretnym wyniku testu 
	 * @param testResult Wynik testu
	 * @param answers Zaznaczone odpowiedzi w pojedynczym pytaniu
	 * @return Oryginalna instancja parametru answers, lub NULL je�eli wyst�pi� b��d
	 */
	public QuestionAnswers saveQuestionAnswers(TestResult testResult, QuestionAnswers answers) {
		int resultId = testResult.getId();
		if (resultId < 0) {
			return null;
		}

		TestRow testRow = answers.getTestRow();
		if (testRow instanceof Question == false)
			return null;
		
		Question question = (Question) testRow;
		int questionId = question.getId(); // =/= answers.getTestRowId()
		
		ContentValues values = new ContentValues();
		values.put("Points", answers.getPoints());
		values.put("Answers", answers.getMarkedAnswers());
		
		int updatedRows = db.update("TestQuestionAnswers", values, "TestResultId = ? AND QuestionId = ?",
				vals(resultId, questionId));
		
		if (updatedRows == 0) {
			values.put("TestResultId", resultId);
			values.put("QuestionId", questionId);
			
			int id = (int)db.insert("TestQuestionAnswers", values);
			
			if (id >= 0)
				return answers;
		}
		return null;
	}
	
	/**
	 * Pobiera zapisane odpowiedzi do testu
	 * @param testSheet Arkusz testowy
	 * @param testResult Rezultat
	 * @return
	 */
	public List<QuestionAnswers> getQuestionAnswers(TestSheet testSheet, TestResult testResult) {
		final SparseArray<QuestionAnswers> map = new SparseArray<QuestionAnswers>();
		List<QuestionAnswers> result = new ArrayList<QuestionAnswers>();

		TestSheet test = testSheet;
		for (Question question : testSheet.getQuestions()) {
			QuestionAnswers qa = new QuestionAnswers(test.getTestRowCode(question), question);
			map.put(question.getId(), qa);
			result.add(qa);
		}
		
		db.select(new String[] { "TestResultId", "Answers", "Points", "QuestionId" },
				"TestQuestionAnswers", // FROM
				"TestResultId = ?", vals(testResult.getId()), // WHERE
				"QuestionId ASC", // ORDER BY
				new RowReader() {
					@Override
					public void readRow(Reader r) {
						int questionId = r.getInt("QuestionId");
						QuestionAnswers qa = map.get(questionId);
						if (qa != null) {
							qa.setPoints(r.getInt("Points"));
							qa.setMarkedAnswers(r.getInt("Answers"));
						}
					}
				}
		);
		
		return result;
	}
	
	/**
	 * Zapisuje definicje testu. Pytania nale�y powi�za� z testem osobno korzystaj�c z {@link #assignQuestionToTest()}
	 * @param test Test do zapisu
	 * @return Instancja testu uzupe�niona o identyfikator lub NULL w przypadku b��du
	 */
	public TestDefinition saveTest(TestDefinition test) {
		ContentValues values = new ContentValues();
		values.put("Name", test.getName());
		values.put("QuestionsCount", test.getQuestionsCount());
		values.put("ShuffleQuestions", test.getShuffleQuestions() ? 1 : 0);
		values.put("ShuffleAnswers", test.getShuffleAnswers() ? 1 : 0);
		values.put("ModifyDate", System.currentTimeMillis() / 1000);
		values.put("Printed", test.beenPrinted() ? 1 : 0);
		values.put("StudentIdLength", test.getStudentIdLength());
		if (test.getId() >= 0) {
			int updatedRows = db.update("Tests", values, "Id = ?", vals(test.getId()));
			
			if (updatedRows == 1)
				return test;
		} else {
			int id = (int)db.insert("Tests", values);

			test.setId(id);
			if (id >= 0) {
				return test;
			}
		}
		return null;
	}
	
	/**
	 * Usuwa test
	 * @param question Test do usuni�cia
	 * @return True je�li usuwanie si� powiod�o
	 */
	public boolean deleteTest(TestDefinition test) {
		return db.delete("Tests", "Id = ?", vals(test.getId())) == 1;
	}
	
	/**
	 * Przypisuje pytanie do testu z ustalon� warto�ci� (mo�e by� r�na od warto�ci domy�lnej pytania)
	 * <p>
	 * Aktualizuje warto�� pytania w przypadku gdy pytanie ju� by�o wcze�niej przypisane
	 * @param question Pytanie
	 * @param test Test do kt�rego ma nale�e�
	 * @return True je�li uda�o si� dopi�� pytanie do testu
	 */
	public boolean assignQuestionToTest(Question question, TestDefinition test) {
		ContentValues values = new ContentValues();
		values.put("Value", question.getValue());

		// pr�ba zaktualizowania aktualnego rekordu
		boolean updated = db.update("TestQuestions", values, "TestId = ? AND QuestionId = ?", vals(test.getId(), question.getId())) == 1;
		
		if (updated)
			return true;
		
		// wstawienie nowego rekordu je�li nie istnieje
		values.put("TestId", test.getId());
		values.put("QuestionId", question.getId());
		
		// kolejno�� pytania w danym te�cie, nie musi by� kolejna
		// dla uproszczenia u�yto aktualnego czasu dzi�ki czemu
		// zawsze p�niej wybrane pytanie b�dzie mia�o p�niejsz� pozycj�
		values.put("Ordinal", System.currentTimeMillis());
		
		return db.insert("TestQuestions", values) != -1;
	}
	
	/**
	 * Odepnij pytanie od testu
	 * @param question Pytanie
	 * @param test Test od kt�rego odpi�� pytanie
	 * @return True je�li uda�o si� odpi�� pytanie od testu
	 */
	public boolean unassignTestQuestion(Question question, TestDefinition test) {
		return db.delete("TestQuestions", "TestId = ? AND QuestionId = ?", vals(test.getId(), question.getId())) == 1;
	}
	
	public Student getStudent(long studentId) {
		if (studentId == 107926) {
			return new Student("Adam P�onka");
		}
		return null;
	}
	
	/**
	 * Funkcja zwraca z bazy danych list� dost�pnych kategorii
	 * @return Lista dost�pnych kategorii 
	 */
	public List<QuestionCategory> getQuestionCategories() {
		final List<QuestionCategory> result = new ArrayList<QuestionCategory>();
		
		db.select(
				new String[] { "Id", "Name" }, // columns
				"QuestionCategories", // FROM
				null, null, // WHERE
				"Id", // ORDER BY
				new RowReader() {
					public void readRow(Reader r) {
						QuestionCategory x = new QuestionCategory(r.getInt("Id"), r.getString("Name"));
						x.setQuestionsInflater(instance);
						result.add(x);
					}
				});
		QuestionCategory defaultCategory = QuestionCategory.DefaultCategory;
		defaultCategory.setQuestionsInflater(this);
		result.add(defaultCategory);
		return result;
	}
	
	/**
	 * Usuwa kategori� pyta�
	 * @param category Kategoria do usuni�cia
	 * @return True je�li usuwanie si� powiod�o
	 */
	public boolean deleteQuestionCategory(QuestionCategory category) {
		return db.delete("QuestionCategories", "Id = ?", vals(category.getId())) == 1;
	}
	
	/**
	 * Funkcja tworzy tablic� string�w konwertuj�c argumenty funkcji 
	 * @param values Parametry
	 * @return Tablica przekonwertowanych parametr�w
	 */
	private static String[] vals(Object... values) {
		String[] results = new String[values.length];
		int i = 0;
		for (Object o : values) {
			if (o instanceof Boolean)
				results[i] = o == Boolean.TRUE ? "1" : "0";
			else results[i] = o.toString();
			i ++;
		}
		return results;
	}
}
