/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.tile;

import org.eclipse.swt.custom.StyleRange;

public final class LinkRange extends StyleRange {
	private final String id;

	public LinkRange(final String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}