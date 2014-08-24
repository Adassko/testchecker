package pl.adamp.testchecker.client.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.test.entities.Answer;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AnswersListAdapter extends BaseAdapter {	
	private Context context;
	private List<Answer> answers;

	public AnswersListAdapter(Context context, List<Answer> answers) {
		this.context = context;
		this.answers = answers;
	}

	@Override
	public int getCount() {
		return answers.size();
	}

	@Override
	public Object getItem(int position) {
		return answers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return answers.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Answer answer = answers.get(position);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.checkable_item, null);
		}

		CheckedTextView check = (CheckedTextView)convertView.findViewById(R.id.checkable_item);
		check.setText(answer.getText());
		convertView.setTag(R.id.item, answer);
		return convertView;
	}
}