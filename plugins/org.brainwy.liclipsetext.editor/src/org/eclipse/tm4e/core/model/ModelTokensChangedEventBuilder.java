/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.model;

import java.util.ArrayList;
import java.util.List;

class ModelTokensChangedEventBuilder {

	private final ITMModel model;
	private final List<Range> ranges;

	public ModelTokensChangedEventBuilder(ITMModel model) {
		this.model = model;
		this.ranges = new ArrayList<>();
	}

	public void registerChangedTokens(int lineNumber) {
		int rangesLength = ranges.size();
		Range previousRange = rangesLength > 0 ? ranges.get(rangesLength - 1) : null;

		if (previousRange != null && previousRange.toLineNumber == lineNumber - 1) {
			// extend previous range
			previousRange.toLineNumber++;
		} else {
			// insert new range
			ranges.add(new Range(lineNumber, lineNumber));
		}
	}

	public ModelTokensChangedEvent build() {
		if (this.ranges.size() == 0) {
			return null;
		}
		return new ModelTokensChangedEvent(ranges, model);
	}
}
