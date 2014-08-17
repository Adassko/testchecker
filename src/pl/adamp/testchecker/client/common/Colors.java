package pl.adamp.testchecker.client.common;

import pl.adamp.testchecker.client.R;
import android.content.res.Resources;
import android.view.View;

public class Colors {
	private static Resources res = null;

	public static int maskColor;
	public static int resultColor;
	public static int resultPointColor;
	public static int viewfinder_laser;
	public static int viewfinder_laser_end;
	public static int answersheet_background;
	public static int answersheet_correct;
	public static int answersheet_incorrect;
	public static int answersheet_text;
	
	private Colors() { }
	
	public synchronized static void init(View view) {
		if (res == null) {
			res = view.getResources(); 
			maskColor = res.getColor(R.color.viewfinder_mask);
			resultColor = res.getColor(R.color.result_view);
			resultPointColor = res.getColor(R.color.possible_result_points);
			viewfinder_laser = res.getColor(R.color.viewfinder_laser);
			viewfinder_laser_end = res.getColor(R.color.viewfinder_laser_end);
			answersheet_background = res.getColor(R.color.answersheet_background);
			answersheet_correct = res.getColor(R.color.answersheet_correct);
			answersheet_incorrect = res.getColor(R.color.answersheet_incorrect);
			answersheet_text = res.getColor(R.color.answersheet_text);
		}
	}
}
