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
package org.eclipse.tm4e.core.internal.rule;

public class IRegExpSourceAnchorCache {

	public String A0_G0;
	public String A0_G1;
	public String A1_G0;
	public String A1_G1;

	public IRegExpSourceAnchorCache(String A0_G0, String A0_G1, String A1_G0, String A1_G1) {
		this.A0_G0 = A0_G0;
		this.A0_G1 = A0_G1;
		this.A1_G0 = A1_G0;
		this.A1_G1 = A1_G1;
	}
}
