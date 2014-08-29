package pl.adamp.testchecker.client;

import java.util.List;

import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.TestResultsListAdapter;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.test.TestResult;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class TestResultsListActivity extends Activity {
	public static final String TEST_RESULT_TEST_ID = "test_id";
	public static final String TEST_RESULT_STUDENT_ID = "student_id";
	
	private TestResultsListAdapter listAdapter;
	private DataManager dataManager;
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_test_results_list);
		
		Intent intent = this.getIntent();
		int test_id = intent.getIntExtra(TEST_RESULT_TEST_ID, -1);
		long student_id = intent.getIntExtra(TEST_RESULT_STUDENT_ID, -1);
		
		dataManager = new DataManager(this);
		ListView listView = (ListView) findViewById(R.id.listView_testResults);
		List<TestResult> results = dataManager.getTestResults(student_id, test_id);
		listAdapter = new TestResultsListAdapter(this, results);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				TestResult result = listAdapter.getItem(position);
				startActivity(
					new Intent(getApplicationContext(), TestResultsActivity.class)
						.putExtra(TestResultsActivity.TEST_RESULT_ID, result.getId())
						);
			}
		});
		listView.setAdapter(listAdapter);
		
		registerForContextMenu(listView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.test_results_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listView_testResults) {
		    MenuInflater inflater = new MenuInflater(this);
		    AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
		    Object o = listAdapter.getItem(mi.position);
		    if (o instanceof TestResult) {
		    	TestResult result = (TestResult)o;
		    	menu.setHeaderTitle(result.getTestName());
		    	inflater.inflate(R.menu.test_results_item, menu);
		    	return;
		    }
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo mi = (AdapterContextMenuInfo) item.getMenuInfo();
		Object o = listAdapter.getItem(mi.position);
		final TestResult result;
		
		switch (item.getItemId()) {
		case R.id.menu_testresults_remove:
			result = (TestResult)o;
			DialogHelper.showDialog(this, R.string.msg_sure, R.string.msg_remove_test_result, new OnAcceptListener() {
				@Override
				public void onAccept(String input) {
					if (dataManager.deleteTestResult(result)) {
						Toast.makeText(activity, R.string.msg_test_result_removed, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(activity, R.string.msg_cannot_remove_test_result, Toast.LENGTH_SHORT).show();
					}
					listAdapter.remove(result);
				}
			}, false);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
