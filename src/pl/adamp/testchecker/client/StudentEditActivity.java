package pl.adamp.testchecker.client;

import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.Student;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class StudentEditActivity extends Activity {
	public static final String STUDENT_ID = "student_id";
	
	private Student student;
	private DataManager dataManager;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.student_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.button_results) {
			Intent intent = new Intent(this, TestResultsListActivity.class);
			intent.putExtra(TestResultsListActivity.TEST_RESULT_STUDENT_ID, student.getId());
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_edit);
		
		dataManager = new DataManager(this);
		
		Intent intent = getIntent();
		long studentId = intent.getLongExtra(STUDENT_ID, -1);
		
		if (studentId >= 0) {
			student = dataManager.getStudent(studentId);
		}
		
		if (student == null) {
			finish();
		}
		
		EditText description = (EditText) findViewById(R.id.editText_description);
		description.setText(student.getDescription());
		description.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable value) {
				student.setDescription(value.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		
		EditText email = (EditText) findViewById(R.id.editText_email);
		email.setText(student.getEmail());
		email.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable value) {
				student.setEmail(value.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		
		EditText phone = (EditText) findViewById(R.id.editText_phone);
		phone.setText(student.getPhone());
		phone.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable value) {
				student.setPhone(value.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		
		EditText name = (EditText) findViewById(R.id.editText_fullname);
		name.setText(student.getFullName());
		name.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable value) {
				student.setFullName(value.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		
		TextView id = (TextView) findViewById(R.id.student_id);
		id.setText(student.getId() + "");
		
		Button save = (Button) findViewById(R.id.button_save);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (dataManager.saveStudent(student) != null) {
					finish();
				} else {
					Toast.makeText(StudentEditActivity.this, R.string.msg_cannot_save_student, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
