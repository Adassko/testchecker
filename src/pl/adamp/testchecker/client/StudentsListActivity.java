package pl.adamp.testchecker.client;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.StudentsListAdapter;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import pl.adamp.testchecker.test.entities.Student;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class StudentsListActivity extends Activity {
	private StudentsListAdapter listAdapter;
	private DataManager dataManager;
	private List<Student> students;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_students_list);
		dataManager = new DataManager(this);

		students = new ArrayList<Student>();
		listAdapter = new StudentsListAdapter(this, students);
		ListView students_list = (ListView) findViewById(R.id.listView_students_list);
		students_list.setAdapter(listAdapter);
		students_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				Student student = listAdapter.getItem(position);
				Intent intent = new Intent(getApplicationContext(), StudentEditActivity.class);
				intent.putExtra(StudentEditActivity.STUDENT_ID, student.getId());
				startActivity(intent);
				listAdapter.notifyDataSetChanged();			
			}
		});
		
		Button new_student = (Button) findViewById(R.id.button_new_student);
		new_student.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DialogHelper.showDialog(StudentsListActivity.this, R.string.student_id, R.string.msg_new_student_id, new OnAcceptListener() {
					@Override
					public void onAccept(String input) {
						try {
							long id = Long.parseLong(input);
							Student student = dataManager.getStudent(id);
							if (student == null) {
								student = new Student(id, "");
								student = dataManager.saveStudent(student);
								if (student == null) {
									return;
								}
								listAdapter.add(student);
							}
							Intent intent = new Intent(getApplicationContext(), StudentEditActivity.class);
							intent.putExtra(StudentEditActivity.STUDENT_ID, student.getId());
							startActivity(intent);
						}
						catch (NumberFormatException ex) {}
					}
				}, true, "", InputType.TYPE_CLASS_NUMBER);
			}
		});
		
		registerForContextMenu(students_list);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (listAdapter != null) {
			students.clear();
			for (Student student : dataManager.getStudents()) {
				students.add(student);
			}
			listAdapter.notifyDataSetInvalidated();
		}
	}
}
