package pl.adamp.testchecker.client.common;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.client.common.DBHelper.Reader;
import pl.adamp.testchecker.client.common.DBHelper.RowReader;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import pl.adamp.testchecker.test.interfaces.AnswersInflater;
import pl.adamp.testchecker.test.interfaces.QuestionsInflater;
import pl.adamp.testchecker.test.entities.TestDefinition;
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
	 * Zwraca listê pytañ wraz z odpowiedziami dla danego testu
	 * @param test Definicja testu dla którego ma byæ zwrócona lista dostêpnych pytañ
	 * @return Lista dostêpnych pytañ dla testu
	 */
	@Override
	public List<Question> getQuestions(TestDefinition test) {
		final int testId = test.getId();		
		final List<Question> result = new ArrayList<Question>();
		final SparseArray<Question> questionsById = new SparseArray<Question>();
		db.select(
				"SELECT q.Id, q.Text, tq.Value, a.Id AnswerId, a.Text AnswerText, a.IsCorrect"
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
	 * Zwraca instancjê pytania na podstawie identyfikatora
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
	
	/**
	 * Zwraca listê odpowiedzi dla pytania
	 * @param question Pytanie dla którego zwróciæ odpowiedzi
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
	 * Zwraca listê pytañ w danej kategorii
	 * @param category	Kategoria nadrzêdna
	 * @return Lista pytañ
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
	 * Funkcja dodaje lub aktualizuje pytanie
	 * @param question	Pytanie do zapisania
	 * @param questionCategory	Kategoria do której ma nale¿eæ pytanie
	 * @return Instancja pytania uzupe³niona o identyfikator lub NULL w przypadku b³êdu
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
	 * @param question Pytanie do usuniêcia
	 * @return True jeœli usuwanie siê powiod³o
	 */
	public boolean deleteQuestion(Question question) {
		return db.delete("Questions", "Id = ?", vals(question.getId())) == 1;
	}

	/**
	 * Funkcja dodaje lub aktualizuje kategoriê
	 * @param questionCategory Kategoria do zapisania
	 * @return Instancja kategorii uzupe³niona o identyfikator lub NULL w przypadku b³êdu
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
	 * Zwraca listê dostêpnych definicji testów
	 * @return Lista dostêpnych testów
	 */
	public List<TestDefinition> getTests() {
		final List<TestDefinition> result = new ArrayList<TestDefinition>();

		db.select(new String[] { "Id", "Name", "ModifyDate", "QuestionsCount", "ShuffleQuestions", "ShuffleAnswers" },
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
						result.add(test);
					}
				});
		
		return result;
	}
	
	/**
	 * Zapisuje definicje testu. Pytania nale¿y powi¹zaæ z testem osobno korzystaj¹c z {@link #assignQuestionToTest()}
	 * @param test Test do zapisu
	 * @return Instancja testu uzupe³niona o identyfikator lub NULL w przypadku b³êdu
	 */
	public TestDefinition saveTest(TestDefinition test) {
		ContentValues values = new ContentValues();
		values.put("Name", test.getName());
		values.put("QuestionsCount", test.getQuestionsCount());
		values.put("ShuffleQuestions", test.getShuffleQuestions() ? 1 : 0);
		values.put("ShuffleAnswers", test.getShuffleAnswers() ? 1 : 0);
		values.put("ModifyDate", System.currentTimeMillis() / 1000);
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
	 * @param question Test do usuniêcia
	 * @return True jeœli usuwanie siê powiod³o
	 */
	public boolean deleteTest(TestDefinition test) {
		return db.delete("Tests", "Id = ?", vals(test.getId())) == 1;
	}
	
	/**
	 * Przypisuje pytanie do testu z ustalon¹ wartoœci¹ (mo¿e byæ ró¿na od wartoœci domyœlnej pytania)
	 * <p>
	 * Aktualizuje wartoœæ pytania w przypadku gdy pytanie ju¿ by³o wczeœniej przypisane
	 * @param question Pytanie
	 * @param test Test do którego ma nale¿eæ
	 * @return True jeœli uda³o siê dopi¹æ pytanie do testu
	 */
	public boolean assignQuestionToTest(Question question, TestDefinition test) {
		ContentValues values = new ContentValues();
		values.put("Value", question.getValue());

		// próba zaktualizowania aktualnego rekordu
		boolean updated = db.update("TestQuestions", values, "TestId = ? AND QuestionId = ?", vals(test.getId(), question.getId())) == 1;
		
		if (updated)
			return true;
		
		// wstawienie nowego rekordu jeœli nie istnieje
		values.put("TestId", test.getId());
		values.put("QuestionId", question.getId());
		values.put("Ordinal", System.currentTimeMillis() / 1000);
		
		return db.insert("TestQuestions", values) != -1;
	}
	
	/**
	 * Odepnij pytanie od testu
	 * @param question Pytanie
	 * @param test Test od którego odpi¹æ pytanie
	 * @return True jeœli uda³o siê odpi¹æ pytanie od testu
	 */
	public boolean unassignTestQuestion(Question question, TestDefinition test) {
		return db.delete("TestQuestions", "TestId = ? AND QuestionId = ?", vals(test.getId(), question.getId())) == 1;
	}
	
	/**
	 * Funkcja zwraca z bazy danych listê dostêpnych kategorii
	 * @return Lista dostêpnych kategorii 
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
	 * Usuwa kategoriê pytañ
	 * @param category Kategoria do usuniêcia
	 * @return True jeœli usuwanie siê powiod³o
	 */
	public boolean deleteQuestionCategory(QuestionCategory category) {
		return db.delete("QuestionCategories", "Id = ?", vals(category.getId())) == 1;
	}
	
	/**
	 * Funkcja tworzy tablicê stringów konwertuj¹c argumenty funkcji 
	 * @param values Parametry
	 * @return Tablica przekonwertowanych parametrów
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
