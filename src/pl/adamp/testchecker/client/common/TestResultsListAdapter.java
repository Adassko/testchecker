package pl.adamp.testchecker.client.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.client.test.TestResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TestResultsListAdapter extends ArrayAdapter<TestResult> {
	private static DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
	
	public TestResultsListAdapter(Context context, List<TestResult> answers) {
		super(context, R.layout.test_results_item, answers);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = getContext();
		
		final TestResult testResult = getItem(position);    
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.test_results_item, parent, false);
		}
		
		TextView grade = (TextView) convertView.findViewById(R.id.textView_grade);
		grade.setText(testResult.getGrade());
		
		TextView testName = (TextView) convertView.findViewById(R.id.testName);
		testName.setText(testResult.getTestName() + "(" + testResult.getVariant() + ")");
		
		TextView studentId = (TextView) convertView.findViewById(R.id.textView_student_id);
		studentId.setText(testResult.getStudentId() + "");
		
		TextView resultDate = (TextView) convertView.findViewById(R.id.testResultDate);
		resultDate.setText(dateFormat.format(testResult.getDate()));		
		
		
		return convertView;
	}
}
