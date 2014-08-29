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
		Intent intent = new Intent(this, TestResultsListActivity.class);
		startActivity(intent);
	}
	
	public void studentsClick(View v) {
		Intent intent = new Intent(this, StudentsListActivity.class);
		startActivity(intent);
	}
}
