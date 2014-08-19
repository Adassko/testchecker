package pl.adamp.testchecker.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.QuestionsListAdapter;
import pl.adamp.testchecker.client.common.TestsListAdapter;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import pl.adamp.testchecker.test.entities.TestDefinition;

public class CreateTestActivity extends Activity {
	protected TestDefinition choosenTest;
	private CreateTestFragment currentFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_createtest);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			setFragment(new TestChooserFragment());
		}
	}
		
	protected void chooseTest(TestDefinition test) {
		choosenTest = test;
		setFragment(new QuestionsChooserFragment());
	}
	
	protected TestDefinition getChoosenTest() {
		return choosenTest;
	}
	
	private void setFragment(CreateTestFragment newFragment) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		if (currentFragment != null)
			ft.addToBackStack(currentFragment.getName());
		ft.replace(R.id.container, newFragment).commit();
		currentFragment = newFragment;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_test, menu);
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


	public static class TestChooserFragment extends CreateTestFragment {
		ListView testsListView;
		TestsListAdapter listAdapter;
		List<TestDefinition> tests;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setName("Wybór testu");
			
			View rootView = inflater.inflate(R.layout.fragment_choose_test,
					container, false);
			
			testsListView = (ListView) rootView.findViewById(R.id.listView_testList);
			testsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
			tests = getDataManager().getTests();

			listAdapter = new TestsListAdapter(rootView.getContext(), tests);
			testsListView.setAdapter(listAdapter);
			
			testsListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					getTestActivity().chooseTest(tests.get(position));
				}				
			});

			Button newTestButton = (Button)rootView.findViewById(R.id.button_new_test);
			newTestButton.setOnClickListener(newTestClick);
			
			return rootView;
		}
		
		private OnClickListener newTestClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input = new EditText(getActivity());
				new AlertDialog.Builder(getActivity())
			    .setTitle("New test")
			    .setMessage("New test name")
			    .setView(input)
			    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            String value = input.getText().toString();
			            TestDefinition test = new TestDefinition(value);
			            
			            test = getDataManager().saveTest(test);
			            if (test != null) {
			            	getTestActivity().chooseTest(test);
			            } else {
			            	Toast.makeText(getTestActivity(), "Error while creating test", Toast.LENGTH_LONG).show();
			            }
			        }
			    }).setNegativeButton(R.string.button_cancel, null).show();
			}
		}; 
	}

	public static class QuestionsChooserFragment extends CreateTestFragment {
		public static final int QUESTION_EDIT_RESULT = 1;
		
		QuestionsListAdapter listAdapter;
		ExpandableListView expListView;
		List<QuestionCategory> categories;
		Set<Question> selectedQuestions;
		TestDefinition choosenTest;

		@Override
		public void onDestroyView() {
			unregisterForContextMenu(expListView);
			super.onDestroyView();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setName("Wybór pytañ (" + getTestActivity().getChoosenTest().getName() + ")");
			
			View rootView = inflater.inflate(R.layout.fragment_create_test,
					container, false);

			expListView = (ExpandableListView) rootView.findViewById(R.id.questionsList);
			expListView.setEmptyView(inflater.inflate(R.layout.empty, null));
			choosenTest = getTestActivity().getChoosenTest();

			categories = getDataManager().getQuestionCategories();
			selectedQuestions = new HashSet<Question>(getDataManager().getQuestions(choosenTest));

			listAdapter = new QuestionsListAdapter(rootView.getContext(), categories);
			expListView.setAdapter(listAdapter);
			listAdapter.setSelectedCallback(new QuestionsListAdapter.SelectedCallback() {
				public boolean isSelected(Question question) {
					return selectedQuestions.contains(question);
				}
			});
			if (categories.size() == 1) {
				expListView.expandGroup(0);
			}

			registerForContextMenu(expListView);

			expListView.setOnChildClickListener(new OnChildClickListener() {
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					Object o = v.getTag(R.id.item);
					if (o instanceof Question) {
						Question question = (Question)o;
						
						if (selectedQuestions.contains(question)) {
							if (getDataManager().unassignTestQuestion(question, choosenTest)) {
								selectedQuestions.remove(question);
								v.setBackgroundColor(Color.TRANSPARENT);
							}
						} else {
							if (getDataManager().assignQuestionToTest(question, choosenTest)) {
								selectedQuestions.add(question);
								v.setBackgroundColor(getActivity().getResources().getColor(R.color.question_selected));
							}
						}
						return true;
					}
					return false;
				}
			});
			
			
			expListView.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> adapter, View v,
						int position, long id) {
					Object o = v.getTag(R.id.item);
					if (o instanceof QuestionCategory) {
						final QuestionCategory category = (QuestionCategory)o;
					
						if (category == QuestionCategory.DefaultCategory)
							return true; // nie pokazuj menu kontekstowego
					}
					return false;
				}
			});
			
			((Button)rootView.findViewById(R.id.button_new_question)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), QuestionEditActivity.class);
					startActivityForResult(intent, QUESTION_EDIT_RESULT);
				}
			});
			
			return rootView;
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.questions, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			if (v.getId() == R.id.questionsList) {
			    MenuInflater inflater = new MenuInflater(getActivity());
				Object o = getMenuItemContext(menuInfo);

				if (o instanceof Question) {
					Question question = (Question)o;
				    menu.setHeaderTitle(question.getQuestion());
				    inflater.inflate(R.menu.questions, menu);
				    return;
				}
				
				if (o instanceof QuestionCategory) {
					QuestionCategory category = (QuestionCategory)o;
					menu.setHeaderTitle(category.getName());
					inflater.inflate(R.menu.question_categories, menu);
					
					boolean hasChildren = category.getQuestions().size() > 0;
				    menu.findItem(R.id.menu_questioncategory_remove).setEnabled(!hasChildren);

					return;
				}
			}
			super.onCreateContextMenu(menu, v, menuInfo);
		}
		
		private Object getMenuItemContext(ContextMenuInfo info) {
			if (info instanceof ExpandableListView.ExpandableListContextMenuInfo) {
				View view = ((ExpandableListView.ExpandableListContextMenuInfo)info).targetView;
				return view.getTag(R.id.item);
			}
			return null;
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			if (requestCode == QUESTION_EDIT_RESULT && resultCode == RESULT_OK) {
				int questionId = data.getIntExtra(QuestionEditActivity.QUESTION_ID, -1);
				if (questionId >= 0) {
					categories = getDataManager().getQuestionCategories();
		            listAdapter.notifyDataSetInvalidated();
		            
		            Question q = getDataManager().getQuestion(questionId);
		            if (selectedQuestions.contains(q)) {
		            	selectedQuestions.add(q);
			            getDataManager().assignQuestionToTest(q, choosenTest);
		            }
				}				
			}
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			Object itemContext = getMenuItemContext(item.getMenuInfo());
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
			final QuestionCategory category;
			final Question question;
			
			switch (item.getItemId()) {
				case R.id.menu_questions_edit:
					question = (Question)itemContext;
					Intent intent = new Intent(getActivity(), QuestionEditActivity.class);
					intent.putExtra(QuestionEditActivity.QUESTION_ID, question.getId());
					
					startActivityForResult(intent, QUESTION_EDIT_RESULT);
					return true;
					
				case R.id.menu_questions_remove:
					question = (Question)itemContext;
					builder.setMessage(R.string.msg_sure);
					builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i2) {
							if (!getDataManager().deleteQuestion(question)) {
								Toast.makeText(getActivity(), "Nie uda³o siê usun¹æ pytania\nPrawdopodobnie jest u¿yte w teœcie", Toast.LENGTH_LONG).show();
							} else {
								selectedQuestions.remove(question);
								for (QuestionCategory category : categories)
									category.invalidateQuestions();
					            listAdapter.notifyDataSetInvalidated();
							}
						}
					});
					builder.setNegativeButton(R.string.button_cancel, null);
					builder.show();
					return true;
					
				case R.id.menu_questioncategory_editname:
					category = (QuestionCategory)itemContext;
					final EditText input = new EditText(getActivity());
				    builder.setTitle("Update category name")
				    .setMessage("New category name")
				    .setView(input)
				    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            String value = input.getText().toString();
				            category.setName(value);
				            getDataManager().saveQuestionCategory(category);
				            listAdapter.notifyDataSetChanged();
				        }
				    }).setNegativeButton(R.string.button_cancel, null).show();
					return true;
					
				case R.id.menu_questioncategory_remove:
					category = (QuestionCategory)itemContext;
					builder.setMessage(R.string.msg_sure);
					builder.setCancelable(true);
					builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i2) {
							if (!getDataManager().deleteQuestionCategory(category)) {
								Toast.makeText(getActivity(), "Nie uda³o siê usun¹æ kategorii\nPrawdopodobnie ma przypisane pytania", Toast.LENGTH_LONG).show();
							} else {
								categories.remove(category);
					            listAdapter.notifyDataSetChanged();
							}
						}
					});
					builder.setNegativeButton(R.string.button_cancel, null);
					builder.show();
					return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
	
	@SuppressLint("ValidFragment")
	private static class CreateTestFragment extends Fragment {
		private String name;
		private DataManager dataManager = null;
		
		protected CreateTestActivity getTestActivity() {
			return (CreateTestActivity)this.getActivity();
		}
		
		protected DataManager getDataManager() {
			if (dataManager == null)
				dataManager = new DataManager(this.getActivity());
			return dataManager;
		}

		public String getName() {
			return name;
		}
		
		protected void setName(String name) {
			this.name = name;
			getTestActivity().setTitle(this.name);
		}
		
		protected void setName(int resId) {
			setName(this.getActivity().getString(resId));
		}
	}
}
