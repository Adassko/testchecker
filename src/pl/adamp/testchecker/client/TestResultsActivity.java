package pl.adamp.testchecker.client;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.QuestionAnswersListAdapter;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.common.QuestionAnswersListAdapter.OnAnswerChangedListener;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.client.test.TestEvaluator;
import pl.adamp.testchecker.client.test.TestResult;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.Student;
import pl.adamp.testchecker.test.entities.TestSheet;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class TestResultsActivity extends Activity {
	public static final String TEST_RESULT_ID = "result_id";
	private TestResult result;
	private List<QuestionAnswers> questionAnswers;
	private DataManager dataManager;
	private final Activity that = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_results);

		int testResultId = getIntent().getIntExtra(TEST_RESULT_ID, -1);
		dataManager = new DataManager(this);

		result = dataManager.getTestResult(testResultId);
		TestDefinition testDefinition = dataManager.getTest(result.getTestId());

		// lista pytañ
		if (testDefinition != null) {
			TestSheet testSheet = testDefinition.getTestSheet(result.getVariant());
			questionAnswers = dataManager.getQuestionAnswers(testSheet, result);
		} else {
			questionAnswers = new ArrayList<QuestionAnswers>();
		}

		QuestionAnswersListAdapter listAdapter = new QuestionAnswersListAdapter(this, questionAnswers);
		ListView listview_qa = (ListView) findViewById(R.id.listView_questionAnswers);
		listview_qa.setAdapter(listAdapter);
		listAdapter.setOnAnswerChangedListener(new OnAnswerChangedListener() {
			@Override
			public void onAnswerChanged(QuestionAnswers answers) {
				recalculatePoints(true);
			}
		});

		// id studenta
		EditText editText_student_id = (EditText) findViewById(R.id.editText_student_id);
		editText_student_id.setText(result.getStudentId() + "");
		editText_student_id.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				try {
					long id = Long.parseLong(editable.toString());
					result.setStudentId(id);
					loadStudentName();
				}
				catch (NumberFormatException e) {}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		
		final EditText addPoints = (EditText) findViewById(R.id.editText_additionalPoints);
		addPoints.setText(result.getAdditionalPoints() + "");
		addPoints.setOnClickListener(focusOnClick);
		addPoints.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == false) return;

				DialogHelper.showNumberChooser(that, R.string.additional_points, new OnAcceptListener() {
					@Override
					public void onAccept(String input) {
						result.setAdditionalPoints(Integer.parseInt(input));
						addPoints.setText(input);
						recalculatePoints(false);
					}
				}, result.getAdditionalPoints(), -100, 100);
			}
		});

		loadStudentName();
		recalculatePoints(false);
	}
	
	private void loadStudentName() {
		TextView student_name = (TextView) findViewById(R.id.textView_student_name);
		Student student = dataManager.getStudent(result.getStudentId());
		String name = "";
		if (student != null) {
			name = student.getFullName(); 
		}
		student_name.setText(name);			
	}
	
	private void recalculatePoints(boolean recalculateQuestions) {
		int points = result.getPoints();
		
		if (recalculateQuestions) {
			points = 0;
			for (QuestionAnswers answers : questionAnswers) {
				points += answers.getPoints();
			}
			result.setPoints(points);
		}
		TextView points_summary = (TextView) findViewById(R.id.textView_points_summary);
		int percent = result.getTotalPoints() * 100 / result.getMaxPoints();
		points_summary.setText(result.getTotalPoints() + "/" + result.getMaxPoints() + " (" + percent + "%)");
		String grade = TestEvaluator.gradeForPercent(percent, dataManager.getGradingTable());
		
		TextView textView_grade = (TextView) findViewById(R.id.textView_grade);
		textView_grade.setText(grade);
	}
	
	/**
	 * Zamienia klikniêcie na zdarzenie focus
	 */
	private static OnClickListener focusOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			v.getOnFocusChangeListener().onFocusChange(v, true);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.test_results, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_confirm) {
			dataManager.saveTestResult(result);
			for (QuestionAnswers answers : questionAnswers) {
				dataManager.saveQuestionAnswers(result, answers);
			}
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
