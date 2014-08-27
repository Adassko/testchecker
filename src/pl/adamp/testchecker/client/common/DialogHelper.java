package pl.adamp.testchecker.client.common;

import java.lang.reflect.Method;

import pl.adamp.testchecker.client.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;

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
		final int originalMin = min;
		if (min < 0) { // numberPicker nie pozwala na wybór ujemnych wartoœci - obejœcie
			value -= min;
			max = max - min;
			min = 0;

			input.setFormatter(new Formatter() {
				@Override
				public String format(int index) {
					return Integer.toString(index + originalMin);
				}
			});
		}
		input.setMinValue(min);
		input.setMaxValue(max);
		input.setValue(value);
		input.setWrapSelectorWheel(false);

		if (originalMin < 0) { // hack na bug w androidzie - wartoœci siê nie wyœwietlaj¹ poprawnie do pierwszego dotkniêcia
			try {
				Method method = input.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
				method.setAccessible(true);
				method.invoke(input, true);
			} catch (Exception e) { }
		}
		builder.setView(input);
		
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				acceptListener.onAccept(Integer.toString(input.getValue() + originalMin));
			}
		}).setNegativeButton(R.string.button_cancel, null).show();
	}

	public interface OnAcceptListener {
		void onAccept(String input);
	}
}
