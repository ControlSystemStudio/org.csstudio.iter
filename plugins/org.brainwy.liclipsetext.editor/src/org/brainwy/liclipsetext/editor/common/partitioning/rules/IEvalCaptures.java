/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning.rules;

import org.brainwy.liclipsetext.editor.rules.IRuleWithSubRules;
import org.eclipse.jface.text.rules.IToken;

public interface IEvalCaptures {

    boolean isSimple();

    IToken getToken();

    IRuleWithSubRules getRuleWithSubRules();

}
