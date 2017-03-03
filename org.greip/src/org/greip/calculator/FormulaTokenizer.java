/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.calculator;

import java.util.StringTokenizer;

class FormulaTokenizer extends StringTokenizer {

	public FormulaTokenizer(final CharSequence formula, final String operators) {
		super(formula.toString(), operators, true);
	}
}
