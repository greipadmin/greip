/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.calculator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.greip.common.Util;

public class CalculatorTextAdapter {

	private DecimalFormat format;
	private Predicate<Event> keyEventPredicate = e -> e.character == '=';
	private Consumer<Calculator> configurer;
	private DecimalFormat verifyFormat;
	private Consumer<BigDecimal> resultConsumer;
	private Supplier<BigDecimal> initialValueSupplier;

	public CalculatorTextAdapter(final Text txt) {

		setDecimalFormat(Formula.getDefaultDecimalFormat());

		setResultConsumer(value -> {
			txt.setText(format.format(value));
			txt.selectAll();
		});

		setValueInitializer(() -> toBigDecimal(txt.getText(), new ParsePosition(0)));

		txt.addListener(SWT.Verify, e -> {
			final String text = txt.getText();
			final String newText = text.substring(0, e.start) + e.text + text.substring(e.end);
			final DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();

			final ParsePosition pos = new ParsePosition(0);
			final BigDecimal value = toBigDecimal(newText, pos);

			if (e.character == dfs.getGroupingSeparator()) {
				e.doit = false;

			} else if (pos.getErrorIndex() != -1) {
				e.doit = false;

			} else {
				final String[] tokens = value.toString().split("\\.");
				final int maxFractionDigits = format.getMaximumFractionDigits();
				final int maxIntegerDigits = format.getMaximumIntegerDigits();

				if (maxIntegerDigits < tokens[0].length()) {
					e.doit = false;
				} else if (tokens.length > 1 && maxFractionDigits < tokens[1].length()) {
					e.doit = false;
				}
			}
		});

		txt.addListener(SWT.KeyDown, e -> {
			if (keyEventPredicate.test(e) && txt.isEnabled()) {
				final CalculatorPopup popup = new CalculatorPopup(txt);
				final Calculator calculator = popup.getCalculator();

				calculator.setDecimalFormat(format);
				calculator.setValue(initialValueSupplier.get());

				Util.whenNotNull(configurer, c -> c.accept(calculator));

				final BigDecimal value = popup.calculate();
				Util.whenNotNull(value, resultConsumer);

				e.doit = false;
			}
		});
	}

	private BigDecimal toBigDecimal(final String text, final ParsePosition pos) {
		return Util.nvl((BigDecimal) verifyFormat.parse(text, pos), BigDecimal.ZERO);
	}

	public void setDecimalFormat(final DecimalFormat format) {
		this.format = (DecimalFormat) format.clone();
		this.format.setParseBigDecimal(true);

		this.verifyFormat = (DecimalFormat) this.format.clone();
		this.verifyFormat.setMaximumFractionDigits(format.getMaximumFractionDigits() + 1);
		this.verifyFormat.setMaximumIntegerDigits(format.getMaximumIntegerDigits() + 1);
	}

	public void setValueInitializer(final Supplier<BigDecimal> initialValueSupplier) {
		this.initialValueSupplier = initialValueSupplier;
	}

	public void setResultConsumer(final Consumer<BigDecimal> resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void setCalculatorConfigurer(final Consumer<Calculator> configurer) {
		this.configurer = configurer;
	}

	public void openCalculatorWhen(final Predicate<Event> keyEventPredicate) {
		this.keyEventPredicate = keyEventPredicate;
	}
}
