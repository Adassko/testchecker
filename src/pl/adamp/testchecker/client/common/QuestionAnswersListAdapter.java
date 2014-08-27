package pl.adamp.testchecker.client.common;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.client.test.TestEvaluator;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuestionAnswersListAdapter extends ArrayAdapter<QuestionAnswers> {
	private LinearLayout.LayoutParams wrapContent;
	private OnAnswerChangedListener answerChangedListener;
	
	public QuestionAnswersListAdapter(Context context, List<QuestionAnswers> answers) {
		super(context, R.layout.item_question_answer, answers);
		wrapContent = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
	}
	
	public void setOnAnswerChangedListener(OnAnswerChangedListener answerChangedListener) {
		this.answerChangedListener = answerChangedListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Context context = getContext();
		
		final QuestionAnswers answers = getItem(position);    
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.item_question_answer, parent, false);
		}
		
		final Question question = (Question) answers.getTestRow();
		
		TextView textview_question = (TextView) convertView.findViewById(R.id.textview_question);
		textview_question.setText((position + 1) + ") " + question.getQuestion());
		
		final TextView textview_points = (TextView) convertView.findViewById(R.id.textview_points);
		int points = answers.getPoints();
		textview_points.setText(points + "/" + question.getValue());

		LinearLayout answers_layout = (LinearLayout) convertView.findViewById(R.id.answers);
		answers_layout.removeAllViews();
		
		final LinearLayout answers_detailed = (LinearLayout) convertView.findViewById(R.id.answers_detailed);
		answers_detailed.removeAllViews();
		answers_detailed.setVisibility(answers.isExpanded() ? View.VISIBLE : View.GONE);
		
		List<Answer> answers_list = question.getAnswers(); 
		for (int i = 0; i < answers_list.size(); i ++) {
			final int pos = i;
			Answer answer = answers_list.get(i);
			CheckBox checkbox = new CheckBox(context);
			checkbox.setChecked(answers.isMarkedAnswer(i));
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean checked) {
					if (checked) {
						answers.addMarkedAnswer(pos);
					} else {
						answers.removeMarkedAnswer(pos);
					}
					int points = TestEvaluator.getAnswerPoints(question, answers);
					answers.setPoints(points);
					textview_points.setText(points + "/" + question.getValue());
					if (answerChangedListener != null) {
						answerChangedListener.onAnswerChanged(answers);
					}
				}
			});
			answers_layout.addView(checkbox, wrapContent);
			
			TextView textview = new TextView(context);
			textview.setText(answer.getText());
			if (answer.isCorrect()) {
				textview.setTextColor(Color.GREEN);				
			}
			answers_detailed.addView(textview, wrapContent);
		}
		
		
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean expanded = !answers.isExpanded();
				answers.setExpanded(expanded);
				answers_detailed.setVisibility(expanded ? View.VISIBLE : View.GONE);
			}
		});
		
		return convertView;
	}
	
	public interface OnAnswerChangedListener {
		void onAnswerChanged(QuestionAnswers answers);
	}
}
