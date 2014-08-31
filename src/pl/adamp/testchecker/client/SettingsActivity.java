package pl.adamp.testchecker.client;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.adamp.testchecker.client.common.DataManager;
import pl.adamp.testchecker.client.common.DialogHelper;
import pl.adamp.testchecker.client.common.GradesListAdapter;
import pl.adamp.testchecker.client.common.TestsListAdapter;
import pl.adamp.testchecker.client.common.DialogHelper.OnAcceptListener;
import pl.adamp.testchecker.client.common.GradesListAdapter.DataSourceChangedListener;
import pl.adamp.testchecker.client.test.TestDefinition;
import pl.adamp.testchecker.test.entities.Answer;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.os.Build;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
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
		private ListView listView_grades;
		private SparseArray<String> gradingTable;
		private DataManager dataManager;
		private GradesListAdapter gradesAdapter;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_grading,
					container, false);
			
			dataManager = new DataManager(getActivity());
			gradingTable = dataManager.getGradingTable();
			gradingTable.put(GradesListAdapter.NEW_EMPTY_ROW, "");

			gradesAdapter = new GradesListAdapter(getActivity(), gradingTable);
			gradesAdapter.setOnDataSourceChangedListener(new DataSourceChangedListener() {
				@Override
				public void dataSourceChanged() {
					dataManager.saveGradingTable(gradingTable);
				}
			});
			listView_grades = (ListView) rootView.findViewById(R.id.listView_grades);
			listView_grades.setAdapter(gradesAdapter);
			listView_grades.setLongClickable(true);

			registerForContextMenu(listView_grades);
			
			Button export_database = (Button) rootView.findViewById(R.id.button_export_database);
			export_database.setOnClickListener(onExportDatabaseClick);
			Button restore_database = (Button) rootView.findViewById(R.id.button_restore);
			restore_database.setOnClickListener(onRestoreDatabaseClick);
			
			return rootView;
		}
		
		private boolean export(boolean auto) {
			File path = new File(Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS), "TestCheckerBackup");
			path.mkdirs();
			File file = new File(path, new SimpleDateFormat("yyyy-MM-dd kk-mm-ss", Locale.US).format(new Date()) + (auto ? "(auto)" : "") + ".db");
		    
			if (dataManager.export_db(file.getAbsolutePath())) {
				if (!auto)
					Toast.makeText(getActivity(),
							String.format(Locale.US, getString(R.string.export_succeed, file.getAbsolutePath())),
							Toast.LENGTH_LONG).show();
				return true;
			} else {
				if (!auto)
					Toast.makeText(getActivity(), R.string.export_failed, Toast.LENGTH_LONG).show();
				return false;
			}
		}
		
		private OnClickListener onExportDatabaseClick = new OnClickListener() {
			public void onClick(View arg0) {
				export(false);
			}
		};
		
		private OnClickListener onRestoreDatabaseClick = new OnClickListener() {
			public void onClick(View arg0) {
				final File path = new File(Environment.getExternalStoragePublicDirectory(
			            Environment.DIRECTORY_DOWNLOADS), "TestCheckerBackup");
				path.mkdirs();
				List<String> files = new ArrayList<String>();
				for(File file : path.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase(Locale.US).endsWith(".db");
					}
				})) {
					files.add(file.getName());
				}

				ListView listview = new ListView(getActivity());
				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.single_item, R.id.single_item, files);
				listview.setAdapter(adapter);
				
				final AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setView(listview)
				.setNegativeButton(R.string.button_cancel, null)
				.create();

				listview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
						dialog.dismiss();
						String fileName = (String) adapter.getItem(position);
						File sourceFile = new File(path, fileName);
						if (dataManager.import_db(sourceFile.getAbsolutePath())) {
							Toast.makeText(getActivity(), R.string.import_succeed, Toast.LENGTH_LONG).show();
							Intent intent = getActivity().getIntent();
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						} else {
							Toast.makeText(getActivity(), R.string.import_failed, Toast.LENGTH_LONG).show();
						}
					}
				});
				dialog.show();
			}
		};
		
		@Override
		public void onDestroyView() {
			unregisterForContextMenu(listView_grades);
			super.onDestroyView();
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			if (v.getId() == R.id.listView_grades) {
			    menu.setHeaderTitle(R.string.grade);
			    getActivity().getMenuInflater().inflate(R.menu.grades, menu);
			    return;
			}

			super.onCreateContextMenu(menu, v, menuInfo);
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			if (item.getItemId() == R.id.menu_remove) {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				
				gradingTable.removeAt(info.position);
				dataManager.saveGradingTable(gradingTable);
				gradesAdapter.notifyDataSetInvalidated();
			}
			return super.onContextItemSelected(item);
		}
	}

}
