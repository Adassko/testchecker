package pl.adamp.testchecker.client;

import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	public void captureClick(View v) {
		Intent intent = new Intent(this, CaptureActivity.class);
		startActivity(intent);
	}
	
	public void createTest(View v) {
		Intent intent = new Intent(this, CreateTestActivity.class);
		startActivity(intent);
	}
	
	public void viewResults(View v) {
		DataManager dm = new DataManager(this);
		QuestionCategory cat = new QuestionCategory("Kategoria" + (int)(Math.random() * 1000 + 1000));
		cat = dm.saveQuestionCategory(cat);
		for (int i = 0; i < 2; i ++) {
			Question q = new Question("Pytanie " + (int)(Math.random() * 1000));
			dm.saveQuestion(q, cat);
		}
		Question q = new Question("Pytanie " + (int)(Math.random() * 1000));
		dm.saveQuestion(q, QuestionCategory.DefaultCategory);
		Toast.makeText(this, "Stworzono pare pytan", Toast.LENGTH_SHORT).show();
		//Intent intent = new Intent(this, TestResultsActivity.class);
		//startActivity(intent);
	}
}
