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
package org.eclipse.tm4e.core.internal.grammar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.grammar.GrammarHelper;
import org.eclipse.tm4e.core.grammar.Injection;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.internal.matcher.IMatchInjectionsResult;
import org.eclipse.tm4e.core.internal.matcher.IMatchResult;
import org.eclipse.tm4e.core.internal.oniguruma.IOnigCaptureIndex;
import org.eclipse.tm4e.core.internal.oniguruma.IOnigNextMatchResult;
import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.eclipse.tm4e.core.internal.rule.BeginEndRule;
import org.eclipse.tm4e.core.internal.rule.BeginWhileRule;
import org.eclipse.tm4e.core.internal.rule.CaptureRule;
import org.eclipse.tm4e.core.internal.rule.ICompiledRule;
import org.eclipse.tm4e.core.internal.rule.MatchRule;
import org.eclipse.tm4e.core.internal.rule.Rule;
import org.eclipse.tm4e.core.logger.ILogger;

class LineTokenizer {

	class WhileStack {

		public final StackElement stack;
		public final BeginWhileRule rule;

		public WhileStack(StackElement stack, BeginWhileRule rule) {
			this.stack = stack;
			this.rule = rule;
		}
	}

	class WhileCheckResult {

		public final StackElement stack;
		public final int linePos;
		public final int anchorPosition;
		public final boolean isFirstLine;

		public WhileCheckResult(StackElement stack, int linePos, int anchorPosition, boolean isFirstLine) {
			this.stack = stack;
			this.linePos = linePos;
			this.anchorPosition = anchorPosition;
			this.isFirstLine = isFirstLine;
		}
	}

	private final Grammar grammar;
	private final OnigString lineText;
	private boolean isFirstLine;
	private int linePos;
	private StackElement stack;
	private final LineTokens lineTokens;
	private int anchorPosition = -1;
	private boolean STOP;
	private final int lineLength;

	public LineTokenizer(Grammar grammar, OnigString lineText, boolean isFirstLine, int linePos, StackElement stack,
			LineTokens lineTokens) {
		this.grammar = grammar;
		this.lineText = lineText;
		this.lineLength = lineText.utf8_length();
		this.isFirstLine = isFirstLine;
		this.linePos = linePos;
		this.stack = stack;
		this.lineTokens = lineTokens;
	}

	public StackElement scan() {
		STOP = false;

		WhileCheckResult whileCheckResult = _checkWhileConditions(grammar, lineText, isFirstLine, linePos, stack,
				lineTokens);
		stack = whileCheckResult.stack;
		linePos = whileCheckResult.linePos;
		isFirstLine = whileCheckResult.isFirstLine;
		anchorPosition = whileCheckResult.anchorPosition;

		while (!STOP) {
			scanNext(); // potentially modifies linePos && anchorPosition
		}

		return stack;
	}

	private void scanNext() {
		ILogger logger = lineTokens.getLogger();
		if (logger.isEnabled()) {
			logger.log("");
			logger.log("@@scanNext: |" + lineText.getString().replaceAll("\n", "\\n").substring(linePos) + '|');
		}

		IMatchResult r = matchRuleOrInjections(grammar, lineText, isFirstLine, linePos, stack, anchorPosition);

		if (r == null) {
			if (logger.isEnabled()) {
				logger.log(" no more matches.");
			}
			// No match
			lineTokens.produce(stack, lineLength);
			STOP = true;
			return;
		}

		IOnigCaptureIndex[] captureIndices = r.getCaptureIndices();
		int matchedRuleId = r.getMatchedRuleId();

		boolean hasAdvanced = (captureIndices != null && captureIndices.length > 0)
				? (captureIndices[0].getEnd() > linePos) : false;

		if (matchedRuleId == -1) {
			// We matched the `end` for this rule => pop it
			BeginEndRule poppedRule = (BeginEndRule) stack.getRule(grammar);

			/*if (logger.isEnabled()) {
				logger.log("  popping " + poppedRule.debugName + " - " + poppedRule.debugEndRegExp);
			}*/

			lineTokens.produce(stack, captureIndices[0].getStart());
			stack = stack.setContentNameScopesList(stack.nameScopesList);
			handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, poppedRule.endCaptures, captureIndices);
			lineTokens.produce(stack, captureIndices[0].getEnd());

			// pop
			StackElement popped = stack;
			stack = stack.pop();

			if (!hasAdvanced && popped.getEnterPos() == linePos) {
				// Grammar pushed & popped a rule without advancing
				if (logger.isEnabled()) {
					logger.log(
							"[1] - Grammar is in an endless loop - Grammar pushed & popped a rule without advancing");
				}
				// See https://github.com/Microsoft/vscode-textmate/issues/12
				// Let's assume this was a mistake by the grammar author and the
				// intent was to continue in this state
				stack = popped;
				
				lineTokens.produce(stack, lineLength);
				STOP = true;
				return;
			}
		} else if (captureIndices != null && captureIndices.length > 0) {
			// We matched a rule!
			Rule _rule = grammar.getRule(matchedRuleId);

			lineTokens.produce(stack, captureIndices[0].getStart());

			StackElement beforePush = stack;
			// push it on the stack rule
			String scopeName = _rule.getName(lineText.getString(), captureIndices);
			ScopeListElement nameScopesList = stack.contentNameScopesList.push(grammar, scopeName);
			stack = stack.push(matchedRuleId, linePos, null, nameScopesList, nameScopesList);

			if (_rule instanceof BeginEndRule) {
				BeginEndRule pushedRule = (BeginEndRule) _rule;
				
				// if (IN_DEBUG_MODE) {
				// console.log(' pushing ' + pushedRule.debugName + ' - ' +
				// pushedRule.debugBeginRegExp);
				// }

				handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, pushedRule.beginCaptures,
						captureIndices);
				lineTokens.produce(stack, captureIndices[0].getEnd());
				anchorPosition = captureIndices[0].getEnd();
				
				String contentName = pushedRule.getContentName(lineText.getString(), captureIndices);
				ScopeListElement contentNameScopesList = nameScopesList.push(grammar, contentName);
				stack = stack.setContentNameScopesList(contentNameScopesList);

				if (pushedRule.endHasBackReferences) {
					stack = stack.setEndRule(pushedRule.getEndWithResolvedBackReferences(lineText.getString(), captureIndices));
				}

				if (!hasAdvanced && beforePush.hasSameRuleAs(stack)) {
					// Grammar pushed the same rule without advancing
					if (logger.isEnabled()) {
						logger.log(
								"[2] - Grammar is in an endless loop - Grammar pushed the same rule without advancing");
					}
					stack = stack.pop();
					lineTokens.produce(stack, lineLength);
					STOP = true;
					return;
				}
			} else if (_rule instanceof BeginWhileRule) {
				BeginWhileRule pushedRule = (BeginWhileRule) _rule;
				// if (IN_DEBUG_MODE) {
				// console.log(' pushing ' + pushedRule.debugName);
				// }

				handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, pushedRule.beginCaptures,
						captureIndices);
				lineTokens.produce(stack, captureIndices[0].getEnd());
				anchorPosition = captureIndices[0].getEnd();

				String contentName = pushedRule.getContentName(lineText.getString(), captureIndices);
				ScopeListElement contentNameScopesList = nameScopesList.push(grammar, contentName);
				stack = stack.setContentNameScopesList(contentNameScopesList);

				if (pushedRule.whileHasBackReferences) {
					stack = stack.setEndRule(pushedRule.getWhileWithResolvedBackReferences(lineText.getString(), captureIndices));
				}

				if (!hasAdvanced && beforePush.hasSameRuleAs(stack)) {
					// Grammar pushed the same rule without advancing
					if (logger.isEnabled()) {
						logger.log(
								"[3] - Grammar is in an endless loop - Grammar pushed the same rule without advancing");
					}
					stack = stack.pop();
					lineTokens.produce(stack, lineLength);
					STOP = true;
					return;
				}
			} else {
				MatchRule matchingRule = (MatchRule) _rule;
				// if (IN_DEBUG_MODE) {
				// console.log(' matched ' + matchingRule.debugName + ' - ' +
				// matchingRule.debugMatchRegExp);
				// }

				handleCaptures(grammar, lineText, isFirstLine, stack, lineTokens, matchingRule.captures,
						captureIndices);
				lineTokens.produce(stack, captureIndices[0].getEnd());

				// pop rule immediately since it is a MatchRule
				stack = stack.pop();

				if (!hasAdvanced) {
					// Grammar is not advancing, nor is it pushing/popping
					if (logger.isEnabled()) {
						logger.log(
								"[4] - Grammar is in an endless loop - Grammar is not advancing, nor is it pushing/popping");
					}
					stack = stack.safePop();
					lineTokens.produce(stack, lineLength);
					STOP = true;
					return;
				}
			}
		}

		if (captureIndices[0].getEnd() > linePos) {
			// Advance stream
			linePos = captureIndices[0].getEnd();
			isFirstLine = false;
		}
	}

	private IMatchResult matchRule(Grammar grammar, OnigString lineText, boolean isFirstLine, final int linePos,
			StackElement stack, int anchorPosition) {
		Rule rule = stack.getRule(grammar);
		final ICompiledRule ruleScanner = rule.compile(grammar, stack.endRule, isFirstLine,
				linePos == anchorPosition);
		final IOnigNextMatchResult r = ruleScanner.scanner._findNextMatchSync(lineText, linePos);

		if (r != null) {
			return new IMatchResult() {

				@Override
				public int getMatchedRuleId() {
					return ruleScanner.rules[r.getIndex()];
				}

				@Override
				public IOnigCaptureIndex[] getCaptureIndices() {
					return r.getCaptureIndices();
				}
			};
		}
		return null;
	}

	private IMatchResult matchRuleOrInjections(Grammar grammar, OnigString lineText, boolean isFirstLine,
			final int linePos, StackElement stack, int anchorPosition) {
		// Look for normal grammar rule
		IMatchResult matchResult = matchRule(grammar, lineText, isFirstLine, linePos, stack, anchorPosition);

		// Look for injected rules
		List<Injection> injections = grammar.getInjections(stack);
		if (injections.size() == 0) {
			// No injections whatsoever => early return
			return matchResult;
		}

		IMatchInjectionsResult injectionResult = matchInjections(injections, grammar, lineText, isFirstLine, linePos,
				stack, anchorPosition);
		if (injectionResult == null) {
			// No injections matched => early return
			return matchResult;
		}

		if (matchResult == null) {
			// Only injections matched => early return
			return injectionResult;
		}

		// Decide if `matchResult` or `injectionResult` should win
		int matchResultScore = matchResult.getCaptureIndices()[0].getStart();
		int injectionResultScore = injectionResult.getCaptureIndices()[0].getStart();

		if (injectionResultScore < matchResultScore
				|| (injectionResult.isPriorityMatch() && injectionResultScore == matchResultScore)) {
			// injection won!
			return injectionResult;
		}

		return matchResult;
	}

	private IMatchInjectionsResult matchInjections(List<Injection> injections, Grammar grammar, OnigString lineText,
			boolean isFirstLine, int linePos, StackElement stack, int anchorPosition) {
		// The lower the better
		int bestMatchRating = Integer.MAX_VALUE;
		IOnigCaptureIndex[] bestMatchCaptureIndices = null;
		int bestMatchRuleId = -1;
		boolean bestMatchResultPriority = false;

		for (Injection injection : injections) {
			ICompiledRule ruleScanner = grammar.getRule(injection.ruleId).compile(grammar, null, isFirstLine,
					linePos == anchorPosition);
			IOnigNextMatchResult matchResult = ruleScanner.scanner._findNextMatchSync(lineText, linePos);

			if (matchResult == null) {
				continue;
			}

			int matchRating = matchResult.getCaptureIndices()[0].getStart();

			if (matchRating > bestMatchRating) {
				continue;
			} else if (matchRating == bestMatchRating && (!injection.priorityMatch || bestMatchResultPriority)) {
				continue;
			}

			bestMatchRating = matchRating;
			bestMatchCaptureIndices = matchResult.getCaptureIndices();
			bestMatchRuleId = ruleScanner.rules[matchResult.getIndex()];
			bestMatchResultPriority = injection.priorityMatch;

			if (bestMatchRating == linePos && bestMatchResultPriority) {
				// No more need to look at the rest of the injections
				break;
			}
		}

		if (bestMatchCaptureIndices != null) {
			final int matchedRuleId = bestMatchRuleId;
			final IOnigCaptureIndex[] matchCaptureIndices = bestMatchCaptureIndices;
			final boolean matchResultPriority = bestMatchResultPriority;
			return new IMatchInjectionsResult() {

				@Override
				public int getMatchedRuleId() {
					return matchedRuleId;
				}

				@Override
				public IOnigCaptureIndex[] getCaptureIndices() {
					return matchCaptureIndices;
				}

				@Override
				public boolean isPriorityMatch() {
					return matchResultPriority;
				}
			};
		}

		return null;
	}

	private void handleCaptures(Grammar grammar, OnigString lineText, boolean isFirstLine, StackElement stack,
			LineTokens lineTokens, List<CaptureRule> captures, IOnigCaptureIndex[] captureIndices) {
		if (captures.size() == 0) {
			return;
		}

		int len = Math.min(captures.size(), captureIndices.length);
		List<LocalStackElement> localStack = new ArrayList<LocalStackElement>();
		int maxEnd = captureIndices[0].getEnd();
		IOnigCaptureIndex captureIndex;

		for (int i = 0; i < len; i++) {
			CaptureRule captureRule = captures.get(i);
			if (captureRule == null) {
				// Not interested
				continue;
			}

			captureIndex = captureIndices[i];

			if (captureIndex.getLength() == 0) {
				// Nothing really captured
				continue;
			}

			if (captureIndex.getStart() > maxEnd) {
				// Capture going beyond consumed string
				break;
			}

			// pop captures while needed
			while (localStack.size() > 0
					&& localStack.get(localStack.size() - 1).getEndPos() <= captureIndex.getStart()) {
				// pop!
				lineTokens.produceFromScopes(localStack.get(localStack.size() - 1).getScopes(), localStack.get(localStack.size() - 1).getEndPos());
				localStack.remove(localStack.size() - 1);
			}

			if (localStack.size() > 0) {
				lineTokens.produceFromScopes(localStack.get(localStack.size() - 1).getScopes(), captureIndex.getStart());
			} else {
				lineTokens.produce(stack, captureIndex.getStart());
			}

			if (captureRule.retokenizeCapturedWithRuleId != null) {
				// the capture requires additional matching
				String scopeName = captureRule.getName(lineText.getString(), captureIndices);
				ScopeListElement nameScopesList = stack.contentNameScopesList.push(grammar, scopeName);
				String contentName = captureRule.getContentName(lineText.getString(), captureIndices);
				ScopeListElement contentNameScopesList = nameScopesList.push(grammar, contentName);
				
				// the capture requires additional matching
				StackElement stackClone = stack.push(captureRule.retokenizeCapturedWithRuleId, captureIndex.getStart(), null, nameScopesList, contentNameScopesList);
				_tokenizeString(grammar,
						GrammarHelper.createOnigString(lineText.getString().substring(0, captureIndex.getEnd())),
						(isFirstLine && captureIndex.getStart() == 0), captureIndex.getStart(), stackClone, lineTokens);
				continue;
			}

			// push
			String captureRuleScopeName = captureRule.getName(lineText.getString(), captureIndices);
			if (captureRuleScopeName != null) {
				// push
				ScopeListElement base = localStack.size() > 0 ? localStack.get(localStack.size() - 1).getScopes() : stack.contentNameScopesList;
				ScopeListElement captureRuleScopesList = base.push(grammar, captureRuleScopeName);
				localStack.add(new LocalStackElement(captureRuleScopesList, captureIndex.getEnd()));
			}
		}

		while (localStack.size() > 0) {
			// pop!
			lineTokens.produceFromScopes(localStack.get(localStack.size() - 1).getScopes(), localStack.get(localStack.size() - 1).getEndPos());
			localStack.remove(localStack.size() - 1);
		}
	}

	/**
	 * Walk the stack from bottom to top, and check each while condition in this
	 * order. If any fails, cut off the entire stack above the failed while
	 * condition. While conditions may also advance the linePosition.
	 */
	private WhileCheckResult _checkWhileConditions(Grammar grammar, OnigString lineText, boolean isFirstLine,
			int linePos, StackElement stack, LineTokens lineTokens) {
		int anchorPosition = -1;
		List<WhileStack> whileRules = new ArrayList<>();
		for (StackElement node = stack; node != null; node = node.pop()) {
			Rule nodeRule = node.getRule(grammar);
			if (nodeRule instanceof BeginWhileRule) {
				whileRules.add(new WhileStack(node, (BeginWhileRule) nodeRule));
			}
		}
		for (int i = whileRules.size() - 1; i >= 0; i--) {
			WhileStack whileRule = whileRules.get(i);
			ICompiledRule ruleScanner = whileRule.rule.compileWhile(grammar, whileRule.stack.endRule, isFirstLine,
					anchorPosition == linePos);
			IOnigNextMatchResult r = ruleScanner.scanner._findNextMatchSync(lineText, linePos);
			// if (IN_DEBUG_MODE) {
			// console.log(' scanning for while rule');
			// console.log(debugCompiledRuleToString(ruleScanner));
			// }

			if (r != null) {
				Integer matchedRuleId = ruleScanner.rules[r.getIndex()];
				if (matchedRuleId != -2) {
					// we shouldn't end up here
					stack = whileRule.stack.pop();
					break;
				}
				if (r.getCaptureIndices() != null && r.getCaptureIndices().length > 0) {
					lineTokens.produce(whileRule.stack, r.getCaptureIndices()[0].getStart());
					handleCaptures(grammar, lineText, isFirstLine, whileRule.stack, lineTokens,
							whileRule.rule.whileCaptures, r.getCaptureIndices());
					lineTokens.produce(whileRule.stack, r.getCaptureIndices()[0].getEnd());
					anchorPosition = r.getCaptureIndices()[0].getEnd();
					if (r.getCaptureIndices()[0].getEnd() > linePos) {
						linePos = r.getCaptureIndices()[0].getEnd();
						isFirstLine = false;
					}
				}
			} else {
				stack = whileRule.stack.pop();
				break;
			}
		}

		return new WhileCheckResult(stack, linePos, anchorPosition, isFirstLine);
	}

	public static StackElement _tokenizeString(Grammar grammar, OnigString lineText, boolean isFirstLine, int linePos,
			StackElement stack, LineTokens lineTokens) {
		return new LineTokenizer(grammar, lineText, isFirstLine, linePos, stack, lineTokens).scan();
	}
}
