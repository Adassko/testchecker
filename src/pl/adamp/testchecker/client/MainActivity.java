package pl.adamp.testchecker.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	public void captureClick(View v) {
		start(CaptureActivity.class);
	}
	
	public void createTest(View v) {
		start(CreateTestActivity.class);
	}
	
	public void viewResults(View v) {
		start(TestResultsListActivity.class);
	}
	
	public void studentsClick(View v) {
		start(StudentsListActivity.class);
	}
	
	public void settingsClick(View v) {
		start(SettingsActivity.class);
	}
	
	private void start(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}
}
