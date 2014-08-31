package pl.adamp.testchecker.client.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.test.entities.Answer;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class GradesListAdapter extends BaseAdapter {
	public static final int NEW_EMPTY_ROW = Integer.MAX_VALUE - 1;
	
	private Context context;
	private SparseArray<String> grading;
	private DataSourceChangedListener changedListener;
	
	public void setOnDataSourceChangedListener(DataSourceChangedListener changedListener) {
		this.changedListener = changedListener;
	}
	

	public GradesListAdapter(Context context, SparseArray<String> gradingTable) {
		this.context = context;
		this.grading = gradingTable;
	}

	@Override
	public int getCount() {
		return grading.size();
	}

	@Override
	public Object getItem(int position) {
		return grading.valueAt(position);
	}

	@Override
	public long getItemId(int position) {
		return grading.keyAt(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int percent = grading.keyAt(position);
		String grade = grading.valueAt(position);

		ViewHolder vh;
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.grade_item, null);
			vh = new ViewHolder();
			final View v = convertView;
			
			vh.editText_grade = (EditText) convertView.findViewById(R.id.grade);
			vh.editText_grade.setOnClickListener(focusOnClick);
			vh.editText_grade.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View arg0, boolean gotFocus) {
					if (!gotFocus) return;
					final ViewHolder vh = (ViewHolder) v.getTag(R.id.viewholder);
					
					DialogHelper.showDialog(context, R.string.grade, R.string.grade, new OnAcceptListener() {
						@Override
						public void onAccept(String input) {
							vh.editText_grade.setText(input.toString());
							GradesListAdapter.this.notifyDataSetChanged();
							if (changedListener != null) {
								changedListener.dataSourceChanged();
							}
						}
					}, true, vh.editText_grade.getText().toString());
				}
				
			});
			vh.editText_grade.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					int percent = (int) v.getTag(R.id.id);
					grading.put(percent, s.toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}					
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
			});
			
			vh.editText_grade_limit = (EditText) convertView.findViewById(R.id.grade_limit);
			vh.editText_grade_limit.setOnClickListener(focusOnClick);
			vh.editText_grade_limit.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View view, boolean gotFocus) {
					if (!gotFocus) return;
					final int oldPercent = (int) v.getTag(R.id.id);
					int value = oldPercent;
					if (value == NEW_EMPTY_ROW) {
						int lastButOneKey = grading.size() - 2; // przedostatni najwiêkszy id (ostatni to NEW_EMPTY_ROW)
						if (lastButOneKey >= 0 && lastButOneKey < grading.size()) {
							value = grading.keyAt(lastButOneKey);
						} else value = 0;
					}
					DialogHelper.showNumberChooser(context, R.string.grade_min_percent,
						new OnAcceptListener() {
							@Override
							public void onAccept(String input) {
								try {
									String oldValue = grading.get(oldPercent);
									grading.remove(oldPercent);
									grading.put(Integer.parseInt(input), oldValue);
									if (oldPercent == NEW_EMPTY_ROW) {
										grading.put(NEW_EMPTY_ROW, ""); // przywrócenie pustego wiersza
									}
									GradesListAdapter.this.notifyDataSetInvalidated();
									if (changedListener != null) {
										changedListener.dataSourceChanged();
									}
								}
								catch (NumberFormatException ex) {}
							}
						}, value, 0, 100);
					}
				});
						
			convertView.setTag(R.id.viewholder, vh);
		} else {
			vh = (ViewHolder) convertView.getTag(R.id.viewholder);
		}
		convertView.setTag(R.id.id, percent);

		vh.editText_grade.setText(grade + "");
		vh.editText_grade_limit.setText(percent == NEW_EMPTY_ROW ? "" : percent + "");
		return convertView;
	}
	
	/**
	 * Zamienia klikniêcie na zdarzenie focus
	 */
	private static OnClickListener focusOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			v.getOnFocusChangeListener().onFocusChange(v, true);
		}
	};
	
	private static class ViewHolder {
		public EditText editText_grade;
		public EditText editText_grade_limit;
	}
	
	public interface DataSourceChangedListener {
		void dataSourceChanged();
	}
}