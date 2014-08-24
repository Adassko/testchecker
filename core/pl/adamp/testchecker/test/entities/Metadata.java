package pl.adamp.testchecker.test.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.adamp.testchecker.test.TestRow;

public class Metadata implements Serializable {
	private static final long serialVersionUID = 6388829776331714385L;
	
	//private String name;
	private int length;
	private final List<Metadata.Row> testRows;
	//private final Integer[] values;
	private final Type type;
	
	public Metadata(Type type, int length) {
		this.type = type;
		this.length = length;
		
		//values = new Integer[length];
		
		testRows = new ArrayList<Metadata.Row>(length);
		for (int i = 0; i < length; i ++) {
			Row row = new Row(this, i);
			row.setAnswersCount(type.getAnswersCount());
			testRows.add(row);
		}
	}
	
	public int getLength() {
		return length;
	}
	
	public Type getType() {
		return type;
	}
	
	public List<Metadata.Row> getTestRows() {
		return testRows;
	}
	
	/*private void setValue(int position, Integer value) {
		if (position >= 0 || position < length)
			values[position] = value;
	}*/
	
	/*public String getValue() {
		StringBuilder builder = new StringBuilder();
		for (Integer x : values) {
			if (x == null)
				builder.append(' ');
			else
				builder.append((int)x);
		}
		return builder.toString();
	}*/
	
	@Override
	public String toString() {
		return this.type.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return this.length * prime + this.type.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Metadata == false)
			return false;
		Metadata other = (Metadata)o;
		return this.type == other.type && this.length == other.length;
	}
	
	public class Row extends TestRow {
		private static final long serialVersionUID = 4657313035853346008L;
		
		private final int position;
		private final Metadata metadata;
		
		/*public void setValue(Integer value) {
			metadata.setValue(position, value);
		}*/
		
		public int getPosition() {
			return this.position;
		}
		
		public Metadata getMetadata() { 
			return this.metadata;
		}
		
		public Row(Metadata metadata, int position) {
			this.position = position;
			this.metadata = metadata;
		}
	}
	
	public enum Type {
		StudentId("StudentId", 10);

		private String name;
		private int answersCount;
		
		public String getName() {
			return this.name;
		}
		
		public int getAnswersCount() {
			return this.answersCount;
		}
		
		private Type(String name, int answersCount) {
			this.name = name;
			this.answersCount = answersCount;
		}
	}
}
