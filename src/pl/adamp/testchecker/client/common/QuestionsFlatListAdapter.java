package pl.adamp.testchecker.client.common;

import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.test.entities.Question;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class QuestionsFlatListAdapter extends BaseAdapter {	
	private Context context;
	private List<Question> questions;

	public QuestionsFlatListAdapter(Context context, List<Question> questions) {
		this.context = context;
		this.questions = questions;
	}

	@Override
	public int getCount() {
		return questions.size();
	}

	@Override
	public Object getItem(int position) {
		return questions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return questions.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Question question = questions.get(position);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.single_item, null);
		}

		TextView text = (TextView)convertView.findViewById(R.id.single_item);
		text.setText(question.getQuestion());
		convertView.setTag(R.id.item, question);
		return convertView;
	}
}