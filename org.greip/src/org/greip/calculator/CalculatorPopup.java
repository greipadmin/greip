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

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.greip.common.Popup;

class CalculatorPopup extends Popup {

	private final Calculator calculator;
	private BigDecimal value;

	public CalculatorPopup(final Control control) {
		super(control);

		setLayout(new FillLayout());

		calculator = new Calculator(this);
		calculator.addListener(SWT.Selection, e -> {
			value = calculator.getValue();
			close();
		});
	}

	public final Calculator getCalculator() {
		return calculator;
	}

	public final BigDecimal calculate() {
		open();
		return value;
	}
}
