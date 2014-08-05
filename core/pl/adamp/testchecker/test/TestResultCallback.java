package pl.adamp.testchecker.test;

public interface TestResultCallback {
	void foundPossibleAnswer(TestResult answer);
	void foundAnswer(TestResult answer);
}
