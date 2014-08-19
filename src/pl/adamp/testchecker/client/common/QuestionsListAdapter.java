package pl.adamp.testchecker.client.common;

import java.util.List;

import pl.adamp.testchecker.client.R;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
public class QuestionsListAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private List<QuestionCategory> categories;
    private SelectedCallback selectedCallback;
    private int color_selected;
 
    public QuestionsListAdapter(Context context, List<QuestionCategory> categories) {
        this._context = context;
        Resources resources = context.getResources();
        this.color_selected = resources.getColor(R.color.question_selected);
        this.categories = categories;
    }
    
    public void setSelectedCallback(SelectedCallback callback) {
    	this.selectedCallback = callback;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosition) {
    	return getQuestion(groupPosition, childPosition);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getQuestion(groupPosition, childPosition).getId();
    }
    
    private Question getQuestion(int groupPosition, int childPosition) {
    	return getCategory(groupPosition).getQuestions().get(childPosition);
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	QuestionCategory category = getCategory(groupPosition);
    	Question question = category.getQuestions().get(childPosition);
        final String childText = (String) question.getQuestion();
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.single_item, null);
        }
 
        boolean isSelected = false;
        if (selectedCallback != null) {
        	isSelected = selectedCallback.isSelected(question);
        }
        convertView.setBackgroundColor(isSelected ? color_selected : Color.TRANSPARENT);

        convertView.setTag(R.id.item, question);
        convertView.setTag(R.id.parent, category);
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.single_item);
 
        txtListChild.setText(childText);
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	return getCategory(groupPosition).getQuestions().size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
    	return getCategory(groupPosition);
    }
    
    private QuestionCategory getCategory(int position) {
    	return this.categories.get(position);
    }
 
    @Override
    public int getGroupCount() {
        return this.categories.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return getCategory(groupPosition).getId();
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	QuestionCategory category = getCategory(groupPosition);
        String headerTitle = (String) category.getName();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.questions_group, null);
        }
        if (category == QuestionCategory.DefaultCategory)
        	headerTitle = convertView.getResources().getString(R.string.no_category);
 
        convertView.setTag(R.id.parent, null);
        convertView.setTag(R.id.item, category);
        
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.questionsGroupHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return true;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
    public static interface SelectedCallback {
    	boolean isSelected(Question question); 
    }
}