package pl.adamp.testchecker.test;

public class TestResult {
	int questionNo;
	int possibleMistakes; // pole bitowe
	int answers;
	boolean markedByUser;
	
	public int getQuestionNo() {
		return questionNo;
	}

	public int getMarkedAnswers() {
		return answers;
	}
	
	public int getPossibleMistakes() {
		return possibleMistakes;
	}
	
	public void setMarkedByUser(boolean value) {
		this.markedByUser = value;
	}
	
	public boolean isMarkedByUser() {
		return this.markedByUser;
	}
	
	public void addMarkedAnswer(int id) {
		answers |= 1 << id;
	}
	
	public boolean isMarkedAnswer(int id) {
		return ((answers >> id) & 1) == 1;
	}
	
	public void addPossibleMistake(int id) {
		answers |= 1 << id;
		possibleMistakes |= 1 << id;
	}
	
	public boolean isPossibleMistake(int id) {
		return ((possibleMistakes >> id) & 1) == 1;
	}
	
	public TestResult(int questionNo) {
		this.questionNo = questionNo;
		this.markedByUser = false;
	}
	
	  @Override
	  public final boolean equals(Object other) {
	    if (other instanceof TestResult) {
	      TestResult otherResult = (TestResult)other;
	      return questionNo == otherResult.questionNo && answers == otherResult.answers &&
	    		  possibleMistakes == otherResult.possibleMistakes;
	    }
	    return false;
	  }

	  @Override
	  public final int hashCode() {
		  int hash = questionNo;
		  hash = 31 * hash + answers;
		  hash = 31 * hash + possibleMistakes;
		  return hash;
	  }
	  
	  @Override
	  public final String toString() {
		  StringBuilder builder = new StringBuilder("Q" + questionNo + "(");
		  
		  int tmp = this.answers;
		  while (tmp > 0) {
			  builder.append((tmp & 1) > 0 ? "X" : "_");
			  tmp >>= 1;
		  }
		  builder.append(")");
		  return builder.toString();
	  }
}
