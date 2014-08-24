package pl.adamp.testchecker.client.common;

import pl.adamp.testchecker.client.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.NumberPicker;

public class DialogHelper {

	public static void showDialog(Context context, int titleResId, int messageResId, OnAcceptListener accept, boolean withTextInput, String value) {
		final EditText input;
		final OnAcceptListener acceptListener = accept;
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
			.setTitle(titleResId).setMessage(messageResId);
		if (withTextInput) {
			input = new EditText(context);
			if (value != null)
				input.setText(value);
			builder.setView(input);
		} else
			input = null;
		
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = null;
				if (input != null)
					value = input.getText().toString();
				acceptListener.onAccept(value);
			}
		}).setNegativeButton(R.string.button_cancel, null).show();
	}
	
	public static void showDialog(Context context, int titleResId, int messageResId, OnAcceptListener accept, boolean withTextInput) {
		 showDialog(context, titleResId, messageResId, accept, withTextInput, null);
	}
	
	public static void showNumberChooser(Context context, int messageResId, OnAcceptListener accept, int value, int min, int max) {
		final NumberPicker input;
		final OnAcceptListener acceptListener = accept;
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
			.setMessage(messageResId);
		input = new NumberPicker(context);
		input.setOrientation(NumberPicker.HORIZONTAL);
		input.setMinValue(min);
		input.setMaxValue(max);
		input.setValue(value);
		builder.setView(input);
		
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				acceptListener.onAccept(input.getValue() + "");
			}
		}).setNegativeButton(R.string.button_cancel, null).show();
	}

	public interface OnAcceptListener {
		void onAccept(String input);
	}
}
