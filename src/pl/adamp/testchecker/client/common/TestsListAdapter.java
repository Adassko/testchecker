package pl.adamp.testchecker.client.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.test.entities.TestDefinition;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TestsListAdapter extends BaseAdapter {	
	private static DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);

	private Context _context;
	private List<TestDefinition> tests;

	public TestsListAdapter(Context context, List<TestDefinition> tests) {
		this._context = context;
		this.tests = tests;
	}

	@Override
	public int getCount() {
		return tests.size();
	}

	@Override
	public Object getItem(int position) {
		return tests.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tests.get(position).getId();
	}

	private static void setTextFieldValue(View parent, int resId, String text) {
		((TextView)parent.findViewById(resId)).setText(text);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TestDefinition test = tests.get(position);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.test_item, null);
		}

		setTextFieldValue(convertView, R.id.testName, test.getName());
		setTextFieldValue(convertView, R.id.testCreateDate, dateFormat.format(test.getModifyDate()));

		return convertView;
	}
}