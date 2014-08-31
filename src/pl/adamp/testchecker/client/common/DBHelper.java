package pl.adamp.testchecker.client.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import pl.adamp.testchecker.client.R;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.content.Context;

final class DBHelper {
	private static final String TAG = DBHelper.class.getSimpleName();
	private Context context;
	private String lastError = null;

	public DBHelper(Context context) {
		this.context = context;
	}
	
	public String getLastError() {
		return lastError;
	}
	
	public void select(String sql, String[] args, RowReader reader) {
		SQLiteOpenHelper helper = new OpenHelper(context);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = helper.getReadableDatabase();
			cursor = db.rawQuery(sql, args);
			Reader rdr = new Reader(cursor);
			while (cursor.moveToNext()) {
				try {
					reader.readRow(rdr);
				}
				catch (Exception e) {
					Log.d(TAG, e.toString());
					break;
				}
			}
		}
		catch (SQLiteException ex) {
			lastError = ex.getMessage();
			Log.d(TAG, ex.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}
	
	/**
	 * @return Id nowego wiersza lub -1 w przypadku b��du
	 */
	public long insert(String table, ContentValues values) {
		SQLiteOpenHelper helper = new OpenHelper(context);
		SQLiteDatabase db = null;
		try {
			db = helper.getWritableDatabase();
			return db.insert(table, null, values);
		}
		catch (SQLiteException ex) {
			lastError = ex.getMessage();
			Log.d(TAG, ex.toString());
			return -1;
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
	
	/**
	 * Usuwa wiersze z tabeli
	 * @return Liczba usuni�tych wierszy. Liczba wierszy nie jest zwr�cona je�eli nie podano warunku
	 */
	public int delete(String table, String where, String[] whereArgs) {
		SQLiteOpenHelper helper = new OpenHelper(context);
		SQLiteDatabase db = null;
		try {
			db = helper.getWritableDatabase();
			return db.delete(table, where, whereArgs);
		}
		catch (SQLiteException ex) {
			lastError = ex.getMessage();
			Log.d(TAG, ex.toString());	
			return 0;
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
	
	/**
	 * @return Liczba zmienionych wierszy
	 */
	public int update(String table, ContentValues values, String where, String[] whereArgs) {
		SQLiteOpenHelper helper = new OpenHelper(context);
		SQLiteDatabase db = null;
		try {
			db = helper.getWritableDatabase();
			return db.update(table, values, where, whereArgs);
		}
		catch (SQLiteException ex) {
			lastError = ex.getMessage();
			Log.d(TAG, ex.toString());
			return 0;
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void select(String[] columns, String from, String where, String[] whereArgs, String orderBy, RowReader reader) {
		SQLiteOpenHelper helper = new OpenHelper(context);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = helper.getReadableDatabase();
			cursor = db.query(from, columns, where, whereArgs, null, null, orderBy);
			Reader rdr = new Reader(cursor);
			while (cursor.moveToNext()) {
				try {
					reader.readRow(rdr);
				}
				catch (Exception ex) {
					lastError = ex.getMessage();
					Log.d(TAG, ex.toString());
					break;
				}
			}
		}
		catch (SQLiteException ex) {
			lastError = ex.getMessage();
			Log.d(TAG, ex.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}
	
	public void exportDb(String fileName) throws IOException {
		final String inFileName = context.getDatabasePath(OpenHelper.DB_NAME).getPath();
	    File dbFile = new File(inFileName);
	    FileInputStream fis = new FileInputStream(dbFile);
	    OutputStream output = new FileOutputStream(fileName);

	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = fis.read(buffer))>0){
	        output.write(buffer, 0, length);
	    }

	    output.flush();
	    output.close();
	    fis.close();
	}
	
	public void importDb(String fileName) throws IOException {
		final String outFileName = context.getDatabasePath(OpenHelper.DB_NAME).getPath();
	    File dbFile = new File(outFileName);
	    FileInputStream fis = new FileInputStream(fileName);
	    OutputStream output = new FileOutputStream(dbFile);

	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = fis.read(buffer))>0){
	        output.write(buffer, 0, length);
	    }

	    output.flush();
	    output.close();
	    fis.close();
	}

	public interface RowReader {
		void readRow(Reader r);
	}
	
	public class Reader {
		private Cursor c;
		
		public Reader(Cursor c) {
			this.c = c;
		}
		
		public String getString(String columnName) {
			return c.getString(c.getColumnIndex(columnName));			
		}
		
		public String getString(String columnName, String def) {
			int colId = c.getColumnIndex(columnName);
			return c.isNull(colId) ? def :
				c.getString(colId);
		}
		
		public int getInt(String columnName) {
			return c.getInt(c.getColumnIndex(columnName));
		}
		
		public int getInt(String columnName, int def) {
			int colId = c.getColumnIndex(columnName);
			return c.isNull(colId) ? def :
				c.getInt(colId);
		}
		
		public boolean getBool(String columnName) {
			return c.getInt(c.getColumnIndex(columnName)) != 0;
		}
		
		public boolean getBool(String columnName, boolean def) {
			int colId = c.getColumnIndex(columnName);
			return c.isNull(colId) ? def :
				c.getInt(colId) != 0;
		}
		
		public float getFloat(String columnName) {
			return c.getFloat(c.getColumnIndex(columnName));
		}
		
		public float getFloat(String columnName, float def) {
			int colId = c.getColumnIndex(columnName);
			return c.isNull(colId) ? def :
				c.getFloat(colId);
		}
		
		public boolean isNull(String columnName) {
			return c.isNull(c.getColumnIndex(columnName));
		}
	}
	
	public class OpenHelper extends SQLiteOpenHelper {
		private static final int DB_VERSION = 7;
		public static final String DB_NAME = "testchecker.db";
		private static final boolean ENABLE_FOREIGN_KEYS = true;
		
		private final Context context;

		OpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.context = context;
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			String schema = context.getResources().getString(R.string.db_schema);
			for (String sql : schema.split(";")) {
				sqLiteDatabase.execSQL(sql);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
			if (oldVersion < 7) {
				sqLiteDatabase.execSQL("DROP TABLE Students;");
				sqLiteDatabase.execSQL("	CREATE TABLE Students (    Id INTEGER PRIMARY KEY, Name TEXT, Description TEXT, "+
					    "Email TEXT,	    Telephone TEXT);");
			}
		}
		
		@Override
		public void onConfigure(SQLiteDatabase db) {
			if (ENABLE_FOREIGN_KEYS)
				db.execSQL("PRAGMA foreign_keys = ON;");
		}
	}
}
