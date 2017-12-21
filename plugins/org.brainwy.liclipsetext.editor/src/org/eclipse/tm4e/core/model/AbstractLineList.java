/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract class for Model lines used by the TextMate model. Implementation
 * class must :
 * 
 * <ul>
 * <li>synchronizes lines with the lines of the editor content when it changed.</li>
 * <li>call {@link AbstractLineList#invalidateLine(int)} with the first changed line.</li>
 * </ul>
 *
 */
public abstract class AbstractLineList implements IModelLines {

	private final List<ModelLine> list = Collections.synchronizedList(new ArrayList<>());

	private TMModel model;

	public AbstractLineList() {
	}

	void setModel(TMModel model) {
		this.model = model;
	}

	@Override
	public void addLine(int line) {
		try {
			this.list.add(line, new ModelLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeLine(int line) {
		this.list.remove(line);
	}

	@Override
	public void updateLine(int line) {
		try {
			// this.list.get(line).text = this.lineToTextResolver.apply(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ModelLine get(int index) {
		return this.list.get(index);
	}

	@Override
	public int getSize() {
		return this.list.size();
	}

	@Override
	public void forEach(Consumer<ModelLine> consumer) {
		this.list.forEach(consumer);
	}

	protected void invalidateLine(int lineIndex) {
		if (model != null) {
			model.invalidateLine(lineIndex);
		}
	}
}