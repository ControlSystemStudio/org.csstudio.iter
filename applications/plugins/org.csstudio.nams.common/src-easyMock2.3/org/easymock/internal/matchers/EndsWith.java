/*
 * Copyright (c) 2001-2007 OFFIS, Tammo Freese.
 * This program is made available under the terms of the MIT License.
 */
package org.easymock.internal.matchers;

import org.easymock.IArgumentMatcher;

public class EndsWith implements IArgumentMatcher {

	private final String suffix;

	public EndsWith(final String suffix) {
		this.suffix = suffix;
	}

	public void appendTo(final StringBuffer buffer) {
		buffer.append("endsWith(\"" + this.suffix + "\")");
	}

	public boolean matches(final Object actual) {
		return (actual instanceof String)
				&& ((String) actual).endsWith(this.suffix);
	}
}