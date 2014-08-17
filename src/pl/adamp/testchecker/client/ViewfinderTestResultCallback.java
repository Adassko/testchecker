/*
d * Copyright (C) 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.adamp.testchecker.client;

import pl.adamp.testchecker.test.TestArea;
import pl.adamp.testchecker.test.TestResultCallback;
import pl.adamp.testchecker.test.entities.AnswerSheet;
import pl.adamp.testchecker.test.entities.QuestionAnswers;
import pl.adamp.testchecker.test.entities.TestSheet;

final class ViewfinderTestResultCallback implements TestResultCallback {

	private final ViewfinderView viewfinderView;
	private final CaptureActivity activity;

	ViewfinderTestResultCallback(ViewfinderView viewfinderView, CaptureActivity activity) {
		this.viewfinderView = viewfinderView;
		this.activity = activity;
	}

	@Override
	public void foundPossibleAnswer(QuestionAnswers answer) {
		viewfinderView.addPossibleAnswer(answer);
	}

	@Override
	public TestSheet getTestSheet() {
		return activity.getCurrentTestSheet();
	}

	@Override
	public AnswerSheet getAnswerSheet() {
		return viewfinderView.getAnswerSheet();
	}

	@Override
	public void foundArea(TestArea area) {
		viewfinderView.addArea(area);	
	}
}
