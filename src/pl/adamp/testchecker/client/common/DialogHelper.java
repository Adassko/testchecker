package pl.adamp.testchecker.client.common;

import pl.adamp.testchecker.client.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class DialogHelper {

	public static void showDialog(Context context, int titleResId, int messageResId, OnAcceptListener accept, boolean withTextInput) {
		final EditText input;
		final OnAcceptListener acceptListener = accept;
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
			.setTitle(titleResId).setMessage(messageResId);
		if (withTextInput) {
			input = new EditText(context);
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

	public interface OnAcceptListener {
		void onAccept(String input);
	}
}
