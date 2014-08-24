package pl.adamp.testchecker.client;

import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.client.common.AnswersListAdapter;
import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.common.QuestionCategoriesListAdapter;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class QuestionEditActivity extends Activity {
	public static final String QUESTION_ID = "question_id";
	
	private Question question = null;
	
	private Question getCurrentQuestion() {
		return question;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_edit);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		DataManager dataManager = new DataManager(this);
		
		Intent intent = getIntent();
		int questionId = intent.getIntExtra(QUESTION_ID, -1);
		
		if (questionId >= 0) {
			question = dataManager.getQuestion(questionId);
		}
		
		if (question == null) {
			question = new Question("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private QuestionEditActivity activity;
		private DataManager dataManager;
		private List<QuestionCategory> categories;
		private List<Answer> answers;
		private QuestionCategory category;
		private QuestionCategory specialCategory;
		private QuestionCategoriesListAdapter categoriesAdapter;
		private Spinner spinner_category;
		private AnswersListAdapter listAdapter;
		private ListView listView_answers;
		private View rootView;
		
		public PlaceholderFragment() {
		}
		
		@Override
		public void onDestroyView() {
			unregisterForContextMenu(listView_answers);
			super.onDestroyView();
		}
		
		private Object getMenuItemContext(ContextMenuInfo info) {
			if (info instanceof AdapterView.AdapterContextMenuInfo) {
				View v = ((AdapterView.AdapterContextMenuInfo)info).targetView;
				return v.getTag(R.id.item);
			}
			return null;
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			if (v.getId() == R.id.listView_answers) {
			    MenuInflater inflater = new MenuInflater(getActivity());
				Object o = getMenuItemContext(menuInfo);

				if (o instanceof Answer) {
					Answer answer = (Answer)o;
				    menu.setHeaderTitle(answer.getText());
				    inflater.inflate(R.menu.answers, menu);
				    return;
				}
			}
			super.onCreateContextMenu(menu, v, menuInfo);
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			Object itemContext = getMenuItemContext(item.getMenuInfo());
			final Answer answer;
			
			switch (item.getItemId()) {
				case R.id.menu_answer_edit:
					answer = (Answer)itemContext;
					DialogHelper.showDialog(activity, R.string.answer, R.string.answer_text, new OnAcceptListener() {
						public void onAccept(String input) {
				            answer.setText(input);
				            listAdapter.notifyDataSetChanged();
						}
					}, true);
					return true;
					
				case R.id.menu_answer_remove:
					answer = (Answer)itemContext;
					DialogHelper.showDialog(activity, R.string.remove_answer, R.string.msg_sure, new OnAcceptListener() {
						public void onAccept(String input) {
							answers.remove(answer);
							updateAnswers();
						}
					}, false);
					return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		private void updateAnswers() {
			listAdapter.notifyDataSetInvalidated();
			for(int i = 0; i < answers.size(); i ++)
				listView_answers.setItemChecked(i, answers.get(i).isCorrect());
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_question_edit,
					container, false);
			activity = (QuestionEditActivity)getActivity();
			dataManager = new DataManager(activity);
			specialCategory = new QuestionCategory(-2, activity.getString(R.string.new_category));
			
			answers = activity.getCurrentQuestion().getAnswers();
			listAdapter = new AnswersListAdapter(activity, answers);
			
			listView_answers = (ListView)rootView.findViewById(R.id.listView_answers);
			listView_answers.setAdapter(listAdapter);
			listView_answers.setEmptyView(inflater.inflate(R.layout.empty, null));
			registerForContextMenu(listView_answers);
			updateAnswers();
			
			Button newAnswer = (Button)rootView.findViewById(R.id.button_addanswer);
			newAnswer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					DialogHelper.showDialog(activity, R.string.answer, R.string.answer_text, new OnAcceptListener() {
						public void onAccept(String input) {
				        	Answer answer = new Answer(input);
				        	answers.add(answer);
				        	updateAnswers();
						}
					}, true);
				}				
			});
			listView_answers.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v,
						int position, long id) {
					Answer answer = (Answer)v.getTag(R.id.item);
					answer.setCorrect(!answer.isCorrect());
					listView_answers.setItemChecked(position, answer.isCorrect());
				}
			});
			
			spinner_category = (Spinner)rootView.findViewById(R.id.spinner_question_category);
			categories = new ArrayList<QuestionCategory>();
			categoriesAdapter = new QuestionCategoriesListAdapter(activity, categories);
			reloadCategories();
			spinner_category.setAdapter(categoriesAdapter);
			
			Button saveButton = (Button)rootView.findViewById(R.id.button_save);
			saveButton.setOnClickListener(new OnClickListener() {
				private boolean ignoreNoCorrectAnswer = false;
				@Override
				public void onClick(View v) {
					final View view = v;
					final OnClickListener that = this;
					Question question = activity.getCurrentQuestion();
					if (!ignoreNoCorrectAnswer) {
						boolean anyCorrect = false;
						for (Answer answer : question.getAnswers())
							if (answer.isCorrect()) {
								anyCorrect = true;
								break;
							}
						
						if (!anyCorrect) {
							DialogHelper.showDialog(activity, R.string.warning, R.string.msg_no_correct_answer,	new OnAcceptListener() {
								public void onAccept(String input) {
						        	ignoreNoCorrectAnswer = true;
						        	that.onClick(view);
								}
							}, false);
							return;
						}
					}
						
					question = dataManager.saveQuestion(question, category);
					if (question == null || dataManager.saveAnswers(question) == false) {
						Toast.makeText(activity, R.string.msg_saveerror, Toast.LENGTH_LONG).show();
						return;
					}
					Intent result = new Intent();
					result.putExtra(QUESTION_ID, question.getId());
					activity.setResult(RESULT_OK, result);
					activity.finish();
				}
			});
			
			Button cancelButton = (Button)rootView.findViewById(R.id.button_cancel);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.setResult(RESULT_CANCELED, new Intent());
					activity.finish();
				}
			});

			bindViewToEntity(rootView, activity.getCurrentQuestion());
			
			// zlikwidowanie focusa i schowanie klawiatury przy klikniêciu poza polami
			rootView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
				    InputMethodManager imm = (InputMethodManager) view.getContext()
				            .getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			});
			return rootView;
		}
		
		/**
		 * Prze³aduj kategorie (na pocz¹tku lub po utworzeniu nowej)
		 */
		private void reloadCategories() {
			categories.clear();
			categories.addAll(dataManager.getQuestionCategories());
			categories.add(specialCategory);
			categoriesAdapter.notifyDataSetChanged();
		}
		
		/**
		 * Zaznacz na liœcie wskazan¹ kategoriê
		 */
		private void selectCategory(QuestionCategory category) {
			int position = categories.indexOf(category);
			this.category = category;
			spinner_category.setSelection(position);
		}
		
		/**
		 * Jednostronne powi¹zanie widoku z encj¹ pytania. Zmiany na widoku spowoduj¹ zaktualizowanie stanu pytania
		 */
		private void bindViewToEntity(View v, Question entity) {
			final Question question = entity;
			
			selectCategory(dataManager.getQuestionCategory(activity.getCurrentQuestion()));
			spinner_category.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View v,
						int position, long id) {
					QuestionCategory selectedCategory = categories.get(position);
					if (selectedCategory == specialCategory) {
						// jeœli wybrano "Now¹ kategoriê" - zaznacz poprzednio zapamiêtan¹ kategoriê
						// spytaj o utworzenie nowej kategorii
						selectCategory(category); 
						
						DialogHelper.showDialog(activity, R.string.new_category, R.string.new_category_name, new OnAcceptListener() {
							public void onAccept(String input) {
					        	QuestionCategory newCategory = new QuestionCategory(input);
					            newCategory = dataManager.saveQuestionCategory(newCategory);
					            if (newCategory == null) {
					            	Toast.makeText(activity, R.string.msg_saveerror, Toast.LENGTH_LONG).show();
					            } else {					            
					            	reloadCategories();
					            	selectCategory(newCategory);
					            }
							};
						}, true);
					} else {
						category = selectedCategory;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					category = QuestionCategory.DefaultCategory;
				}
			});
			
			TextView textView_question = (TextView)v.findViewById(R.id.question);
			textView_question.setText(entity.getQuestion());
			textView_question.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable value) {
					question.setQuestion(value.toString());
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			});
			
			final TextView textView_questionvalue = (TextView)v.findViewById(R.id.textView_questionvalue);
			
			SeekBar seekBar_value = (SeekBar)v.findViewById(R.id.seekbar_question_value);
			seekBar_value.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekbar, int position,
						boolean fromUser) {
					if (position < 1) {
						seekbar.setProgress(1);
						return;
					}
					
					question.setValue(position);
					textView_questionvalue.setText(position + "");					
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) { }
				@Override
				public void onStopTrackingTouch(SeekBar arg0) { }
			});
			seekBar_value.setProgress(entity.getValue());

		}
	}

}
