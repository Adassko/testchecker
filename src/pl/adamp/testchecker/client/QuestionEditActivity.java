package pl.adamp.testchecker.client;

import java.util.List;

import pl.adamp.testchecker.client.common.AnswersListAdapter;
import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.QuestionCategoriesListAdapter;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.os.Build;

public class QuestionEditActivity extends Activity {
	public static final String QUESTION_ID = "question_id";
	
	private Question question = null;
	private List<Answer> answers;
	//dataManager.saveQuestion(question, )
	
	private Question getCurrentQuestion() {
		return question;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_edit);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		DataManager dataManager = new DataManager(this);
		
		Intent intent = getIntent();
		int questionId = intent.getIntExtra(QUESTION_ID, -1);
		
		if (questionId >= 0) {
			question = dataManager.getQuestion(questionId);
		}
		
		if (question == null) {
			question = new Question(this.getString(R.string.new_question));
		}
	}

	/*
	 * 			setResult(RESULT_CANCELED, new Intent());
			finish();
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private QuestionEditActivity activity;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_question_edit,
					container, false);
			activity = (QuestionEditActivity)getActivity();
			
			AnswersListAdapter listAdapter = new AnswersListAdapter(activity, activity.getCurrentQuestion().getAnswers());
			activity.getCurrentQuestion().getAnswers().add(new Answer("Odpowiedz 1"));
			activity.getCurrentQuestion().getAnswers().add(new Answer("Odpowiedz 2"));
			activity.getCurrentQuestion().getAnswers().add(new Answer("Odpowiedz 3"));
			final ListView listView = (ListView)rootView.findViewById(R.id.listView_answers);
			listView.setAdapter(listAdapter);
			listView.setEmptyView(inflater.inflate(R.layout.empty, null));
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v,
						int position, long id) {
					Answer answer = (Answer)v.getTag(R.id.item);
					answer.setCorrect(!answer.isCorrect());
					listView.setItemChecked(position, answer.isCorrect());
				}
			});
			
			Spinner spinner = (Spinner)rootView.findViewById(R.id.spinner_question_category);
			DataManager dataManager = new DataManager(activity);
			QuestionCategory category = dataManager.getQuestionCategory(activity.getCurrentQuestion());

			List<QuestionCategory> categories = dataManager.getQuestionCategories();
			int position = categories.indexOf(category);
			QuestionCategoriesListAdapter categoriesAdapter = new QuestionCategoriesListAdapter(activity, categories);
			spinner.setAdapter(categoriesAdapter);
			spinner.setSelection(position);

			return rootView;
		}
	}

}
