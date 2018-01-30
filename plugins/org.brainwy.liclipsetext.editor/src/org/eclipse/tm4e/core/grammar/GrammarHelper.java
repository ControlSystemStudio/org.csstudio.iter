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
package org.eclipse.tm4e.core.grammar;

import java.util.Map;

import org.eclipse.tm4e.core.internal.grammar.Grammar;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.theme.IThemeProvider;

public class GrammarHelper {

	public static IGrammar createGrammar(IRawGrammar grammar, int initialLanguage,
			Map<String, Integer> embeddedLanguages, IGrammarRepository repository, IThemeProvider themeProvider) {
		return new Grammar(grammar, initialLanguage, embeddedLanguages, repository, themeProvider);
	}

	public static OnigString createOnigString(String str) {
		return new OnigString(str);
	}

}