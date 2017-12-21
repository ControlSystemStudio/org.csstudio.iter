/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.languages.tmbundle.parsing;

class ElseNode extends NodeWithContents {

    public ElseNode(String snippet, int begin, int end) {
        super(snippet, begin, end);
    }

    @Override
    public void applyReplace(ReplaceContext ctx) throws Exception {
        for (Node c : children) {
            c.applyReplace(ctx);
        }
    }
}