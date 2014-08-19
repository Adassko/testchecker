package pl.adamp.testchecker.client.common;

import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class QuestionCategoriesListAdapter extends BaseAdapter {	
	private Context context;
	private List<QuestionCategory> categories;

	public QuestionCategoriesListAdapter(Context context, List<QuestionCategory> categories) {
		this.context = context;
		this.categories = categories;
	}

	@Override
	public int getCount() {
		return categories.size();
	}

	@Override
	public Object getItem(int position) {
		return categories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return categories.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		QuestionCategory category = categories.get(position);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.single_item, null);
		}

		TextView textView = (TextView)convertView.findViewById(R.id.single_item);

		String name = category.getName();
		if (category == QuestionCategory.DefaultCategory)
			name = convertView.getResources().getString(R.string.no_category);
		textView.setText(name);
		convertView.setTag(R.id.item, category);
		return convertView;
	}
}