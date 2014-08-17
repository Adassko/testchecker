package pl.adamp.testchecker.test;

import pl.adamp.testchecker.test.entities.AnswerSheet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

public class TestDecoderResult extends Result {
	private AnswerSheet answers;

	public TestDecoderResult(AnswerSheet answers) {
		super("Test completed", new byte[0], new ResultPoint[0], BarcodeFormat.TEST);
		this.answers = answers;
	}
	
	public AnswerSheet getAnswers() {
		return answers;
	}
}
