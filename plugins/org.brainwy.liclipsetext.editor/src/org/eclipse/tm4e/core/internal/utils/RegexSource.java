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
package org.eclipse.tm4e.core.internal.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.tm4e.core.internal.oniguruma.IOnigCaptureIndex;

public class RegexSource {

	private static final Pattern CAPTURING_REGEX_SOURCE = Pattern
			.compile("\\$(\\d+)|\\$\\{(\\d+):\\/(downcase|upcase)}");

	public static boolean hasCaptures(String regexSource) {
		if (regexSource == null) {
			return false;
		}
		return CAPTURING_REGEX_SOURCE.matcher(regexSource).find();
	}

	public static String replaceCaptures(String regexSource, String captureSource, IOnigCaptureIndex[] captureIndices) {
		Matcher m = CAPTURING_REGEX_SOURCE.matcher(regexSource);
		StringBuffer result = new StringBuffer();
		while (m.find()) {
			String match = m.group();
			String replacement = getReplacement(match, captureSource, captureIndices);
			m.appendReplacement(result, replacement);
		}
		m.appendTail(result);
		return result.toString();
	}

	private static String getReplacement(String match, String captureSource, IOnigCaptureIndex[] captureIndices) {
		int index = -1;
		String command = null;
		int doublePointIndex = match.indexOf(":");
		if (doublePointIndex != -1) {
			index = Integer.parseInt(match.substring(2, doublePointIndex));
			command = match.substring(doublePointIndex + 2, match.length() - 1);
		} else {
			index = Integer.parseInt(match.substring(1, match.length()));
		}
		IOnigCaptureIndex capture = captureIndices.length > index ? captureIndices[index] : null;
		if (capture != null) {
			String result = captureSource.substring(capture.getStart(), capture.getEnd());
			// Remove leading dots that would make the selector invalid
			while (result.length() > 0 && result.charAt(0) == '.') {
				result = result.substring(1);
			}
			if ("downcase".equals(command)) {
				return result.toLowerCase();
			} else if ("upcase".equals(command)) {
				return result.toUpperCase();
			} else {
				return result;
			}
		} else {
			return match;
		}
	}
}
