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

import java.util.Collection;

/**
 * TextMate grammar API.
 * 
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/main.ts
 *
 */
public interface IGrammar {

	/**
	 * Returns the name of the grammar.
	 * 
	 * @return the name of the grammar.
	 */
	String getName();

	/**
	 * Returns the scope name of the grammar.
	 * 
	 * @return the scope name of the grammar.
	 */
	String getScopeName();

	/**
	 * Returns the supported file types and null otherwise.
	 * 
	 * @return the supported file types and null otherwise.
	 */
	Collection<String> getFileTypes();

	/**
	 * Tokenize `lineText`.
	 * 
	 * @param lineText
	 *            the line text to tokenize.
	 * @return the result of the tokenization.
	 */
	ITokenizeLineResult tokenizeLine(String lineText);

	/**
	 * Tokenize `lineText` using previous line state `prevState`.
	 * 
	 * @param lineText
	 *            the line text to tokenize.
	 * @param prevState
	 *            previous line state.
	 * @return the result of the tokenization.
	 */
	ITokenizeLineResult tokenizeLine(String lineText, StackElement prevState);

	/**
	 * Tokenize `lineText` using previous line state `prevState`.
	 * The result contains the tokens in binary format, resolved with the following information:
	 *  - language
	 *  - token type (regex, string, comment, other)
	 *  - font style
	 *  - foreground color
	 *  - background color
	 * e.g. for getting the languageId: `(metadata & MetadataConsts.LANGUAGEID_MASK) >>> MetadataConsts.LANGUAGEID_OFFSET`
	 */
	ITokenizeLineResult2 tokenizeLine2(String lineText);
	
	/**
	 * Tokenize `lineText` using previous line state `prevState`.
	 * The result contains the tokens in binary format, resolved with the following information:
	 *  - language
	 *  - token type (regex, string, comment, other)
	 *  - font style
	 *  - foreground color
	 *  - background color
	 * e.g. for getting the languageId: `(metadata & MetadataConsts.LANGUAGEID_MASK) >>> MetadataConsts.LANGUAGEID_OFFSET`
	 */
	ITokenizeLineResult2 tokenizeLine2(String lineText, StackElement prevState);
	
}
