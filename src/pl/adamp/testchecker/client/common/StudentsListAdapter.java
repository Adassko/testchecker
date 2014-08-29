package pl.adamp.testchecker.client.common;

import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.test.entities.Student;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StudentsListAdapter extends ArrayAdapter<Student> {
	public StudentsListAdapter(Context context, List<Student> answers) {
		super(context, R.layout.students_item, answers);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = getContext();
		
		final Student student = getItem(position);    
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.students_item, parent, false);
		}
		
		TextView student_id = (TextView) convertView.findViewById(R.id.textView_student_id);
		student_id.setText(student.getId() + "");
		
		TextView student_name = (TextView) convertView.findViewById(R.id.textView_student_name);
		student_name.setText(student.getFullName());
		
		return convertView;
	}
}
