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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;

public class Tokenizer implements ITokenizationSupport {

	private final IGrammar grammar;
	private final DecodeMap decodeMap;

	public Tokenizer(IGrammar grammar) {
		this.grammar = grammar;
		this.decodeMap = new DecodeMap();
	}

	@Override
	public TMState getInitialState() {
		return new TMState(null, null);
	}

	@Override
	public LineTokens tokenize(String line, TMState state) {
		return tokenize(line, state, null, null);
	}

	@Override
	public LineTokens tokenize(String line, TMState state, Integer offsetDelta, Integer stopAtOffset) {
		if (offsetDelta == null) {
			offsetDelta = 0;
		}
		// Do not attempt to tokenize if a line has over 20k
		// or if the rule stack contains more than 100 rules (indicator of
		// broken grammar that forgets to pop rules)
		// if (line.length >= 20000 || depth(state.ruleStack) > 100) {
		// return new RawLineTokens(
		// [new Token(offsetDelta, '')],
		// [new ModeTransition(offsetDelta, this._modeId)],
		// offsetDelta,
		// state
		// );
		// }
		TMState freshState = state.clone();
		ITokenizeLineResult textMateResult = grammar.tokenizeLine(line, freshState.getRuleStack());
		freshState.setRuleStack(textMateResult.getRuleStack());

		// Create the result early and fill in the tokens later
		List<TMToken> tokens = new ArrayList<>();
		String lastTokenType = null;
		for (int tokenIndex = 0, len = textMateResult.getTokens().length; tokenIndex < len; tokenIndex++) {
			IToken token = textMateResult.getTokens()[tokenIndex];
			int tokenStartIndex = token.getStartIndex();
			String tokenType = decodeTextMateToken(this.decodeMap, token.getScopes().toArray(new String[0]));

			// do not push a new token if the type is exactly the same (also
			// helps with ligatures)
			if (!tokenType.equals(lastTokenType)) {
				tokens.add(new TMToken(tokenStartIndex + offsetDelta, tokenType));
				lastTokenType = tokenType;
			}
		}
		return new LineTokens(tokens, offsetDelta + line.length(), freshState);

	}

	private String decodeTextMateToken(DecodeMap decodeMap, String[] scopes) {
		String[] prevTokenScopes = decodeMap.prevToken.scopes;
		int prevTokenScopesLength = prevTokenScopes.length;
		Map<Integer, Map<Integer, Boolean>> prevTokenScopeTokensMaps = decodeMap.prevToken.scopeTokensMaps;

		Map<Integer, Map<Integer, Boolean>> scopeTokensMaps = new LinkedHashMap<>();
		Map<Integer, Boolean> prevScopeTokensMaps = new LinkedHashMap<>();
		boolean sameAsPrev = true;
		for (int level = 1/* deliberately skip scope 0 */; level < scopes.length; level++) {
			String scope = scopes[level];

			if (sameAsPrev) {
				if (level < prevTokenScopesLength && prevTokenScopes[level].equals(scope)) {
					prevScopeTokensMaps = prevTokenScopeTokensMaps.get(level);
					scopeTokensMaps.put(level, prevScopeTokensMaps);
					continue;
				}
				sameAsPrev = false;
			}

			int[] tokens = decodeMap.getTokenIds(scope);
			prevScopeTokensMaps = new LinkedHashMap<>(prevScopeTokensMaps);
			for (int i = 0; i < tokens.length; i++) {
				prevScopeTokensMaps.put(tokens[i], true);
			}
			scopeTokensMaps.put(level, prevScopeTokensMaps);
		}

		decodeMap.prevToken = new TMTokenDecodeData(scopes, scopeTokensMaps);
		return decodeMap.getToken(prevScopeTokensMaps);
	}
}
