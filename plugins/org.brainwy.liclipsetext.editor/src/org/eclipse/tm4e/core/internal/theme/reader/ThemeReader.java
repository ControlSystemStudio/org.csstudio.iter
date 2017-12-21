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
package org.eclipse.tm4e.core.internal.theme.reader;

import java.io.InputStream;

import org.eclipse.tm4e.core.internal.parser.json.JSONPListParser;
import org.eclipse.tm4e.core.internal.parser.xml.XMLPListParser;
import org.eclipse.tm4e.core.theme.IRawTheme;

/**
 * TextMate Theme reader utilities.
 *
 */
public class ThemeReader {

	public final static IThemeParser XML_PARSER = new IThemeParser() {

		private XMLPListParser<IRawTheme> parser = new XMLPListParser<IRawTheme>(true);

		@Override
		public IRawTheme parse(InputStream contents) throws Exception {
			return parser.parse(contents);
		}
	};

	public final static IThemeParser JSON_PARSER = new IThemeParser() {

		private JSONPListParser<IRawTheme> parser = new JSONPListParser<IRawTheme>(true);

		@Override
		public IRawTheme parse(InputStream contents) throws Exception {
			return parser.parse(contents);
		}
	};

	public static IRawTheme readThemeSync(String filePath, InputStream in) throws Exception {
		SyncThemeReader reader = new SyncThemeReader(in, getThemeParser(filePath));
		return reader.load();
	}

	private static IThemeParser getThemeParser(String filePath) {
		if (filePath.endsWith(".json")) {
			return JSON_PARSER;
		}
		return XML_PARSER;
	}
}
