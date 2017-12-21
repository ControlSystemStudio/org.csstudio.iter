/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.views.languages;

import org.brainwy.liclipsetext.editor.common.partitioning.reader.SubPartitionCodeReader.TypedPart;
import org.brainwy.liclipsetext.shared_core.structure.DataAndImageTreeNode;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class LiClipseLanguagesViewOutlineLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        if (element instanceof DataAndImageTreeNode) {
            DataAndImageTreeNode dataAndImageTreeNode = (DataAndImageTreeNode) element;
            return dataAndImageTreeNode.image;
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof DataAndImageTreeNode) {
            DataAndImageTreeNode dataAndImageTreeNode = (DataAndImageTreeNode) element;
            element = dataAndImageTreeNode.data;
            if (element instanceof TypedPart) {

            }
        }
        if (element == null) {
            element = "null";
        }
        return element.toString();
    }

}