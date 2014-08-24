package pl.adamp.testchecker.client.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import pl.adamp.testchecker.test.TestReader;
import pl.adamp.testchecker.test.TestRow;
import pl.adamp.testchecker.test.entities.Answer;
import pl.adamp.testchecker.test.entities.Metadata;
import pl.adamp.testchecker.test.entities.Question;
import pl.adamp.testchecker.test.entities.TestSheet;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.LinearLayout;

public class TestPrinter {
	private TestSheet test;
	private Canvas canvas;
	
	public TestPrinter(TestSheet test) {
		this.test = test;
	}
	
	private static int measureSize(int[] pattern, int singleLineSize) {
		int size = 0;
		for (int i = 0; i < pattern.length; i ++)
			size += pattern[i] * singleLineSize;
		return size;
	}
	
	private static String upperCaseLetter(int index) {
		return (char)(65 + index % 26) + "";
	}
	
	private static String lowerCaseLetter(int index) {
		return (char)(97 + index % 26) + "";
	}
	
	public Bitmap drawToBitmap(PaperSize paper, int dpi) {
		UnitConverter from = new UnitConverter(dpi);
		
		int px = from.mm(paper.width);
		int py = from.mm(paper.height);
		Bitmap bitmap = Bitmap.createBitmap(px, py, Config.RGB_565);
		bitmap.setDensity(dpi);
		
		int pageMargin = from.mm(8);
		int maxX = px - pageMargin;
		int maxY = py - pageMargin;
		
		canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);

		Paint filled = new Paint(Paint.ANTI_ALIAS_FLAG);
		filled.setColor(Color.BLACK);
		filled.setStyle(Style.FILL_AND_STROKE);
		TextPaint tp = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		Paint unfilled = new Paint(Paint.ANTI_ALIAS_FLAG);
		unfilled.setColor(Color.BLACK);
		unfilled.setStyle(Style.STROKE);
		
		int boxSize = from.mm(4);
		int halfBoxSize = boxSize / 2;
		int singleLine = from.mm(0.3);
		int boxMargin = singleLine * 5; 
		int padding = from.mm(2);

		List<Question> questions = test.getQuestions();
		
		// TODO: USUN¥Æ !!!
		questions.addAll(new ArrayList<Question>(questions));
		
		
		int left, top;

		// kod kreskowy testu
		Bitmap barcode = drawBarcode("t" + test.getId() + "/" + test.getVariant(), from.cm(4), from.mm(7));
		left = px - from.mm(5) - barcode.getWidth();
		top = from.mm(5);
		barcode.setDensity(dpi);
		canvas.drawBitmap(barcode, left, top, filled);
		top += barcode.getHeight();
		tp.setTextSize(from.mm(2));
		tp.setTextAlign(Align.CENTER);
		new WrappedText("test " + test.getId() + " wariant " + test.getVariant(), tp, barcode.getWidth())
			.drawOn(canvas, left + barcode.getWidth() / 2, top);

		// tytu³ testu
		left = pageMargin;
		top = pageMargin;
		tp.setTextSize(from.mm(6));
		tp.setTextAlign(Align.LEFT);
		top += new WrappedText(test.getName(), tp, maxX - pageMargin - barcode.getWidth())
			.drawOn(canvas, left, top)
			.getHeight();
	    
		// linia oddzielaj¹ca tytu³
	    canvas.drawRect(new Rect(pageMargin, top, maxX, top + from.mm(1)), filled);
	    top += padding;

	    int maxTop = top;
	    
	    tp.setTextSize(from.mm(2.5));
	    tp.setTextAlign(Align.CENTER);

	    int initialLeft = left;
	    int initialTop = top;
	    left = maxX - boxSize;
	    
	    // dane typu identyfikator studenta
	    for (Metadata metadata : test.getMetadata()) {
	    	List<Metadata.Row> testRows = metadata.getTestRows();
	    	for (int q = testRows.size() - 1; q >= 0; q --) {
	    		TestRow testRow = testRows.get(q);
	    		int code = test.getTestRowCode(testRow);
	    		
				// górny kod paskowy pytania
				int bottom = drawTopPattern(code, left + singleLine * 2, top, boxSize - singleLine * 4, singleLine, filled);
				top = bottom + boxMargin;
				
				// literki przy odpowiedziach
				if (q == 0) {
					drawSymbols('0', '9', left - boxSize, top, boxSize, boxMargin, testRow.getReservedSpaceSize(), tp);
				}

				// kratki odpowiedzi
				bottom = drawTickBox(testRow, left, top, boxSize, boxMargin, unfilled);
				top = bottom;
				
				// dolny kod paskowy pytania
				bottom = drawBottomPattern(code, left + singleLine * 2, top, boxSize - singleLine * 4, singleLine, filled);
				top = bottom;
				
				maxTop = Math.max(maxTop, top);
				left -= boxSize + padding;
				top = initialTop;
	    	}
	    }
	    int metaDataLeft = left;
	    int metaDataBottom = maxTop;
	    
	    left = initialLeft + boxSize;	    
	    maxTop = top;
	    
	    boolean drawLetters = true;
	    // karta odpowiedzi
	    for (int q = 0; q < questions.size(); q ++) {
	    	Question question = questions.get(q);
			int code = test.getTestRowCode(question);

			// numer pytania (u góry po œrodku)
			canvas.drawText(Integer.toString(q + 1), left + halfBoxSize, top + boxSize - 5 * singleLine, tp);
			top += boxSize;
			
			// górny kod paskowy pytania
			int bottom = drawTopPattern(code, left + singleLine * 2, top, boxSize - singleLine * 4, singleLine, filled);
			top = bottom + boxMargin;

			// literki przy odpowiedziach
			if (drawLetters) {
				drawLetters = false;
				drawSymbols('A', 'Z', initialLeft, top, boxSize, boxMargin, question.getReservedSpaceSize(), tp);
			}
			
			// kratki odpowiedzi
			bottom = drawTickBox(question, left, top, boxSize, boxMargin, unfilled);
			top = bottom;

			
			// dolny kod paskowy pytania
			bottom = drawBottomPattern(code, left + singleLine * 2, top, boxSize - singleLine * 4, singleLine, filled);
			top = bottom;
			
			maxTop = Math.max(maxTop, top);
			left += boxSize + padding;
			if (left > metaDataLeft - boxSize + padding) { // zawijanie pytañ
				initialTop = maxTop + padding;
				left = initialLeft + boxSize;
				drawLetters = true;
			}
			top = initialTop;
		}
		
		top = maxTop + padding * 3; // przesuñ siê pionowo o maksymaln¹ wielkoœæ pytania
		left = initialLeft;
		
		TextPaint questionPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		questionPaint.setFakeBoldText(true);
		questionPaint.setTextSize(from.mm(3));

	    tp.setTextAlign(Align.LEFT);
		initialTop = top;
		
		int columns = 3;
		for (int i = 0; i < questions.size(); i ++) {
			Question question = questions.get(i);
			List<WrappedText> layouts = new ArrayList<WrappedText>();
			
			int ident = padding; // wciêcie przy odpowiedziach

			String text = String.format("%d. %s (%d %s)", i + 1, question.getQuestion(), question.getValue(), "pkt.");
			int maxWidth = (px - 2 * pageMargin - (columns - 1) * padding) / columns;
			
			layouts.add(new WrappedText(text, questionPaint, maxWidth, 0, singleLine * 5));
			
			int ai = 0;
			for (Answer answer : question.getAnswers()) {
				String answerText = String.format("%s) %s", lowerCaseLetter(ai), answer.getText());
				layouts.add(new WrappedText(answerText, tp, maxWidth, ident, 0));
				ai ++;
		    }
			
			if (totalHeight(layouts) + top > maxY) {
				left += maxWidth + padding;
				if (left + maxWidth > metaDataLeft) {
					top = metaDataBottom + padding;
				} else {
					top = initialTop;
				}
			}
			
			for (WrappedText wt : layouts) {
				top += wt.drawOn(canvas, left, top).getHeight();
			}
			
			top += padding * 2;
		}		
		
		return bitmap;
	}
	
	private int drawTopPattern(int code, int left, int top, int width, int lineHeight, Paint paint) {
		int[] pattern = TestReader.getUpperCodePattern(code);
	    int maxTopPatternSize = 15 * lineHeight;
		top += maxTopPatternSize - measureSize(pattern, lineHeight); // wyrównaj wysokoœæ wszystkich kodów
		boolean black = true;
		for(int j = 0; j < pattern.length; j ++) {
			int lineSize = lineHeight * pattern[j];
			if (black) {
				canvas.drawRect(new Rect(left, top, left + width, top + lineSize), paint);
			}
			top += lineSize;
			black ^= true;
		}
		return top;
	}
	
	private int drawBottomPattern(int code, int left, int top, int width, int lineHeight, Paint paint) {
		int[] pattern = TestReader.getLowerCodePattern(code);
		boolean black = true;
		for(int j = 0; j < pattern.length; j ++) {
			int lineSize = lineHeight * pattern[pattern.length - 1 - j]; // kod odwrócony
			if (black) {
				canvas.drawRect(new Rect(left, top, left + width, top + lineSize), paint);
			}
			top += lineSize;
			black ^= true;
		}
		return top;
	}
	
	private int drawTickBox(TestRow row, int left, int top, int boxSize, int boxMargin, Paint paint) {
		for(int i = 0; i < row.getReservedSpaceSize(); i ++) {
			if (i < row.getAnswersCount()) {
				canvas.drawRect(new Rect(left, top, left + boxSize, top + boxSize), paint);
			}
			top += boxSize + boxMargin;
		}
		return top;
	}

	/**
	 * Rysowanie liter/cyfr na karcie odpowiedzi
	 */
	private void drawSymbols(char start, char end, int left, int top, int boxSize, int boxMargin, int letters, TextPaint paint) {
		int count = end - start + 1;
		float textAscent = paint.ascent(); // "góra" tekstu wzglêdem baseline (wartoœæ ujemna)
		for (int i = 0; i < letters; i ++) {
			String txt = "" + (char)(start + i % count);
			canvas.drawText(txt, left + boxSize / 2, top + (boxSize - textAscent) / 2, paint);
			top += boxSize + boxMargin;
		}

	}
	
	private static int totalHeight(List<WrappedText> layouts) {
		int result = 0;
		for (WrappedText wt : layouts) {
			result += wt.getHeight();
		}
		return result;
	}
	
	private static class WrappedText {
		private StaticLayout sl;
		private int height;
		private int leftMargin;
		
		public WrappedText(String text, TextPaint paint, int maxWidth) {
			this(text, paint, maxWidth, 0, 0);
		}

		public WrappedText(String text, TextPaint paint, int maxWidth, int leftMargin, int bottomMargin) {
			this.sl = new StaticLayout(text, paint, maxWidth - leftMargin, Alignment.ALIGN_NORMAL, 1, 0, false);
			this.height = sl.getHeight() + bottomMargin;
			this.leftMargin = leftMargin;
		}
		
		public int getHeight() {
			return this.height;
		}
		
		public WrappedText drawOn(Canvas canvas, int left, int top) {
			canvas.save();
			canvas.translate(left + leftMargin, top);
			sl.draw(canvas);
			canvas.restore();
			return this;
		}
	}

	private static Bitmap drawBarcode(String contentsToEncode, int width, int height) {
		Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix result;
		try {
			result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.CODE_128, width, height, hints);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
		}
		catch (WriterException e) {
			return null;
		}
		width = result.getWidth();
		height = result.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? 0xff000000 : 0xffffffff;
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
}

	private static class UnitConverter {
		private int dpi;
		
		public UnitConverter(int dpi) {
			this.dpi = dpi;
		}
		
		public int mm(double milimeters) {
			return (int)Math.round(milimeters * this.dpi * 0.03937008);
		}
		
		public int cm(double centimeters) {
			return mm(centimeters * 10);
		}
	}
	
	public enum PaperSize {
		A4(210, 297);
		
		private int width;
		private int height;

		private PaperSize(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
}
