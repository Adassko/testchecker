package pl.adamp.testchecker.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.common.QuestionsFlatListAdapter;
import pl.adamp.testchecker.client.common.QuestionsListAdapter;
import pl.adamp.testchecker.client.common.TestsListAdapter;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.client.test.TestPrinter;
import pl.adamp.testchecker.client.test.TestPrinter.PaperSize;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.QuestionCategory;
import pl.adamp.testchecker.test.entities.TestSheet;

public class CreateTestActivity extends Activity {
	private static final String BUNDLE_TEST_ID = "bundle_test_id";
	
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
		} else {
			if (savedInstanceState.containsKey(BUNDLE_TEST_ID)) {
				int savedId = savedInstanceState.getInt(BUNDLE_TEST_ID);
				choosenTest = new DataManager(this).getTest(savedId);
			}
				setFragment((CreateTestFragment)getFragmentManager().findFragmentByTag("main"));
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (choosenTest != null)
			outState.putInt(BUNDLE_TEST_ID, choosenTest.getId());
		super.onSaveInstanceState(outState);
	}
		
	protected void goToPreview() {
		setFragment(new PreviewFragment());		
	}
	
	protected void goToQuestionsEdit() {
		setFragment(new QuestionsChooserFragment());
	}
	
	protected void chooseTest(TestDefinition test) {
		choosenTest = test;
		if (test.beenPrinted()) {
			goToPreview();
		} else {
			goToQuestionsEdit();
		}
	}
	
	protected TestDefinition getChoosenTest() {
		return choosenTest;
	}
	
	private void setFragment(CreateTestFragment newFragment) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		if (currentFragment != null)
			ft.addToBackStack(currentFragment.getName());
		ft.replace(R.id.container, newFragment, "main").commit();
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

		return super.onOptionsItemSelected(item);
	}


	public static class TestChooserFragment extends CreateTestFragment {
		ListView testsListView;
		TestsListAdapter listAdapter;
		List<TestDefinition> tests;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setName(R.string.button_choose_test);
			
			View rootView = inflater.inflate(R.layout.fragment_choose_test,
					container, false);
			
			testsListView = (ListView) rootView.findViewById(R.id.listView_testList);
			testsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
			tests = getDataManager().getTests();

			listAdapter = new TestsListAdapter(rootView.getContext(), tests);
			testsListView.setAdapter(listAdapter);
			
			registerForContextMenu(testsListView);
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
		
		@Override
		public void onDestroyView() {
			unregisterForContextMenu(getView().findViewById(R.id.listView_testList));
			super.onDestroyView();
		}
		
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			if (v.getId() == R.id.listView_testList) {
			    MenuInflater inflater = new MenuInflater(getActivity());
			    AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
			    Object o = listAdapter.getItem(mi.position);
			    if (o instanceof TestDefinition) {
			    	TestDefinition test = (TestDefinition)o;
			    	menu.setHeaderTitle(test.getName());
			    	inflater.inflate(R.menu.tests, menu);
			    	return;
			    }
			}
			super.onCreateContextMenu(menu, v, menuInfo);
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
		    AdapterContextMenuInfo mi = (AdapterContextMenuInfo) item.getMenuInfo();
			Object o = listAdapter.getItem(mi.position);
			final TestDefinition test;
			
			switch (item.getItemId()) {
			case R.id.menu_tests_remove:
				test = (TestDefinition)o;
				DialogHelper.showDialog(getActivity(), R.string.msg_sure, R.string.msg_remove_test, new OnAcceptListener() {
					@Override
					public void onAccept(String input) {
						final OnAcceptListener accept = new OnAcceptListener() {
							@Override
							public void onAccept(String input) {
								if (getDataManager().deleteTest(test)) {
									Toast.makeText(getActivity(), R.string.msg_test_removed, Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(getActivity(), R.string.msg_cannot_remove_test, Toast.LENGTH_SHORT).show();
								}
								tests.remove(test);
								listAdapter.notifyDataSetInvalidated();
							}
						};
						
						if (test.beenPrinted()) {
							DialogHelper.showDialog(getActivity(), R.string.msg_sure, R.string.msg_remove_printed_test, accept, false);
						} else {
							accept.onAccept(null);
						}
					}
				}, false);
				return true;
				
			case R.id.menu_tests_editname:
				test = (TestDefinition)o;
				DialogHelper.showDialog(getActivity(), R.string.menu_tests_editname, R.string.msg_new_test_name, new OnAcceptListener() {
					@Override
					public void onAccept(String input) {
						test.setName(input);
						getDataManager().saveTest(test);
						listAdapter.notifyDataSetChanged();
					}
				}, true, test.getName());
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		private OnClickListener newTestClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input = new EditText(getActivity());
				new AlertDialog.Builder(getActivity())
			    .setTitle(R.string.msg_new_test)
			    .setMessage(R.string.msg_new_test_name)
			    .setView(input)
			    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            String value = input.getText().toString();
			            TestDefinition test = new TestDefinition(value);
			            
			            test = getDataManager().saveTest(test);
			            if (test != null) {
			            	getTestActivity().chooseTest(test);
			            } else {
			            	Toast.makeText(getTestActivity(), R.string.msg_cannot_create_test, Toast.LENGTH_LONG).show();
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
		TextView textView_selectedCount;

		@Override
		public void onDestroyView() {
			unregisterForContextMenu(expListView);
			super.onDestroyView();
		}
		
		
		private void reloadCategories() {
			categories.clear();
			for (QuestionCategory category : getDataManager().getQuestionCategories()) {
				categories.add(category);
			}
			listAdapter.notifyDataSetInvalidated();

			if (categories.size() == 1) {
				expListView.expandGroup(0);
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setName("Wybór pytañ (" + getTestActivity().getChoosenTest().getName() + ")");
			
			View rootView = inflater.inflate(R.layout.fragment_create_test,
					container, false);

			textView_selectedCount = (TextView)rootView.findViewById(R.id.selectedCount);
			expListView = (ExpandableListView) rootView.findViewById(R.id.questionsList);
			expListView.setEmptyView(inflater.inflate(R.layout.empty, null));
			choosenTest = getTestActivity().getChoosenTest();

			selectedQuestions = new HashSet<Question>(getDataManager().getQuestions(choosenTest));
			textView_selectedCount.setText(selectedQuestions.size() + "");

			categories = new ArrayList<QuestionCategory>();
			listAdapter = new QuestionsListAdapter(getActivity(), categories);
			expListView.setAdapter(listAdapter);
			listAdapter.setSelectedCallback(new QuestionsListAdapter.SelectedCallback() {
				public boolean isSelected(Question question) {
					return selectedQuestions.contains(question);
				}
			});
			reloadCategories();

			registerForContextMenu(expListView);

			expListView.setOnChildClickListener(new OnChildClickListener() {
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					Object o = v.getTag(R.id.item);
					if (o instanceof Question) {
						Question question = (Question)o;
						selectQuestion(question, v, !selectedQuestions.contains(question));
						return true;
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
			
			rootView.findViewById(R.id.button_next).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getTestActivity().goToPreview();
				}
			});
			
			return rootView;
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			if (requestCode == QUESTION_EDIT_RESULT && resultCode == RESULT_OK) {
				int questionId = data.getIntExtra(QuestionEditActivity.QUESTION_ID, -1);
				if (questionId >= 0) {
					reloadCategories();
					
		            Question q = getDataManager().getQuestion(questionId);
		            selectQuestion(q, null, true);
				}				
			}
		}
		
		private void selectQuestion(Question question, View v, boolean setSelected) {
			if (setSelected) {
				if (getDataManager().assignQuestionToTest(question, choosenTest)) {
					selectedQuestions.add(question);
					if (v != null)
						v.setBackgroundColor(getActivity().getResources().getColor(R.color.question_selected));
				}
			} else {
				if (getDataManager().unassignTestQuestion(question, choosenTest)) {
					selectedQuestions.remove(question);
					if (v != null)
						v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
			textView_selectedCount.setText(selectedQuestions.size() + "");
		}

		private Object getMenuItemContext(ContextMenuInfo info) {
			if (info instanceof ExpandableListView.ExpandableListContextMenuInfo) {
				View v = ((ExpandableListView.ExpandableListContextMenuInfo)info).targetView;
				return v.getTag(R.id.item);
			}
			return null;
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
					
					boolean isEmpty = category.getQuestions().size() == 0;
					boolean isDefault = category.equals(QuestionCategory.DefaultCategory);
				    menu.findItem(R.id.menu_questioncategory_remove).setEnabled(isEmpty && !isDefault);
				    menu.findItem(R.id.menu_questioncategory_editname).setEnabled(!isDefault);

					return;
				}
			}
			super.onCreateContextMenu(menu, v, menuInfo);
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
					
				case R.id.menu_questioncategory_selectall:
					category = (QuestionCategory)itemContext;
					for (Question q : category.getQuestions()) {
						if (!selectedQuestions.contains(q))
							selectQuestion(q, null, true);
						listAdapter.notifyDataSetChanged();
					}
					return true;
					
				case R.id.menu_questioncategory_editname:
					category = (QuestionCategory)itemContext;
					final EditText input = new EditText(getActivity());
				    builder.setTitle(R.string.msg_update_category_name)
				    .setMessage(R.string.msg_new_category_name)
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

	public static class PreviewFragment extends CreateTestFragment {
		private TestDefinition test;
		private int generate_variants = 1;		
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.test_preview, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == R.id.button_scan) {
				DialogHelper.showNumberChooser(getActivity(), R.string.test_variant, new OnAcceptListener() {
					@Override
					public void onAccept(String input) {
						Intent intent = new Intent(getActivity(), CaptureActivity.class);
						intent.putExtra(CaptureActivity.SCAN_TEST_ID, test.getId());
						intent.putExtra(CaptureActivity.SCAN_TEST_VARIANT, Integer.parseInt(input));
						
						startActivity(intent);
					}
				}, 1, 1, 40);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		private void blockLayout(View view) {
			final View layoutBlocker = view.findViewById(R.id.layout_blocker); 
			layoutBlocker.setVisibility(View.VISIBLE);
			layoutBlocker.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					DialogHelper.showDialog(getActivity(), R.string.warning, R.string.msg_edit_not_safe, new OnAcceptListener() {
						public void onAccept(String input) {
							layoutBlocker.setOnClickListener(null);
							layoutBlocker.setVisibility(View.GONE);
							test.printed(false);
						}
					}, false);
				}
			});

		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			test = getTestActivity().getChoosenTest();
			setName(test.getName() + " - " + " generowanie testu");
			
			final View rootView = inflater.inflate(R.layout.fragment_test_preview,
					container, false);
			setHasOptionsMenu(true);

			final List<Question> questions = getDataManager().getQuestions(test);
			QuestionsFlatListAdapter questionsAdapter = new QuestionsFlatListAdapter(getActivity(), questions);
			ListView questionsList = (ListView)rootView.findViewById(R.id.questions_list_preview);
			questionsList.setAdapter(questionsAdapter);
			questionsList.setLongClickable(true);
			questionsList.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					getTestActivity().goToQuestionsEdit();
					return true;
				}
			});

			if (test.beenPrinted()) {
				blockLayout(rootView);
			}
			
			// liczba pytañ
			final EditText questionsCount = (EditText)rootView.findViewById(R.id.editText_questions_count);
			if (test.getQuestionsCount() < 2 || test.getQuestionsCount() > questions.size()) {
				test.setQuestionsCount(questions.size());
			}
			questionsCount.setText(test.getQuestionsCount() + "");
			questionsCount.setOnClickListener(focusOnClick);
			questionsCount.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == false) return;

					DialogHelper.showNumberChooser(getActivity(), R.string.questions_count, new OnAcceptListener() {
						@Override
						public void onAccept(String input) {
							test.setQuestionsCount(parseInt(input, 0));
							questionsCount.setText(input);
						}
					}, test.getQuestionsCount(), 2, questions.size());
				}
			});

			// d³ugoœæ identyfikatora
			final EditText idLength = (EditText)rootView.findViewById(R.id.editText_id_length);
			idLength.setText(test.getStudentIdLength() + "");
			idLength.setOnClickListener(focusOnClick);
			idLength.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == false) return;
					DialogHelper.showNumberChooser(getActivity(), R.string.id_length, new OnAcceptListener() {
						@Override
						public void onAccept(String input) {
							test.setStudentIdLength(parseInt(input, 0));
							idLength.setText(input);
						}
					}, test.getStudentIdLength(), 0, 11);
				}
			});
			
			// prze³¹cznik mieszania odpowiedzi
			Switch shuffleQuestions = (Switch)rootView.findViewById(R.id.switch_shuffle_questions);
			shuffleQuestions.setChecked(test.getShuffleQuestions());
			shuffleQuestions.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean value) {
					test.shuffleQuestions(value);
					getDataManager().saveTest(test);
				}
			});
			
			// prze³¹cznik mieszania pytañ
			Switch shuffleAnswers = (Switch)rootView.findViewById(R.id.switch_shuffleAnswers);
			shuffleAnswers.setChecked(test.getShuffleAnswers());
			shuffleAnswers.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean value) {
					test.shuffleAnswers(value);
					getDataManager().saveTest(test);
				}
			});
			
			// liczba wariacji
			final EditText variants = (EditText)rootView.findViewById(R.id.editText_variants);
			variants.setText("1");
			variants.setOnClickListener(focusOnClick);
			variants.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus == false) return;

					DialogHelper.showNumberChooser(getActivity(), R.string.variants, new OnAcceptListener() {
						@Override
						public void onAccept(String input) {
							generate_variants = parseInt(input, 1);
							variants.setText(input);
						}
					}, generate_variants, 1, 40);
				}
			});
			
			// przycisk generowania
			Button generate = (Button)rootView.findViewById(R.id.button_generate);
			generate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (test.getQuestionsCount() < 2) {
						Toast.makeText(getActivity(), "Test musi zawieraæ co najmniej dwa pytania", Toast.LENGTH_LONG).show();
						return;
					}
					test.printed(true);
					blockLayout(rootView);
					getDataManager().saveTest(test);
					ArrayList<Uri> uris = new ArrayList<Uri>();
					
					for (int i = 1; i <= generate_variants; i ++) {
						TestSheet sheet = test.getTestSheet(i);
						Bitmap bmp = new TestPrinter(getActivity(), sheet).drawToBitmap(PaperSize.A4, 300);
						uris.add(saveBitmap(bmp, sheet));
					}
					
					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
					shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
					shareIntent.setType("image/png");
					startActivity(Intent.createChooser(shareIntent, "Wyœlij"));
				}
			});
			
			return rootView;
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
		
		public static Uri saveBitmap(Bitmap bitmap, TestSheet test) {
			String simpleName = test.getName().replaceAll("[^a-zA-Z0-9 _-]+", "");
			
			File path = new File(Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS), "Tests/" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
			path.mkdirs();
			File file = new File(path, simpleName + " (" + test.getId() + "-" + test.getVariant() + ").png");
		    
			OutputStream stream = null;
			try {
				stream = new FileOutputStream(file);
				bitmap.compress(CompressFormat.PNG, 100, stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (stream != null)
					try {
						stream.close();
					} catch (Exception e) { }
			}
			return Uri.fromFile(file);
		}


		private int parseInt(String input, int def) {
			try {
				return Integer.parseInt(input);
			}
			catch (NumberFormatException nfe) {
				return def;			
			}	
		}
	}
	
	private static abstract class CreateTestFragment extends Fragment {
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
