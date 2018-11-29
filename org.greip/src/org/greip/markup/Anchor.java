/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.markup;

import org.eclipse.swt.custom.StyleRange;

public final class Anchor extends StyleRange {

	public final String href;

	public Anchor(final String href) {
		this.href = href;
	}
}