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
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.BinaryOperator;

import org.eclipse.swt.SWT;
import org.greip.common.Util;

class Formula {

	private static final String NEGATE = "\u02D7";
	private static final Map<String, BinaryOperator<BigDecimal>> operations = new HashMap<>();

	{
		operations.put("+", BigDecimal::add);
		operations.put("-", BigDecimal::subtract);
		operations.put("/", (v1, v2) -> v1.divide(v2, 20, BigDecimal.ROUND_HALF_EVEN));
		operations.put("*", BigDecimal::multiply);
	}

	private final StringBuilder formula = new StringBuilder();

	public static DecimalFormat getDefaultDecimalFormat() {
		return new DecimalFormat("#0.##########");
	}

	private String result;
	private boolean calculated = true;
	private DecimalFormat format = getDefaultDecimalFormat();

	public Formula() {
		init(BigDecimal.ZERO);
	}

	public String format() {
		final StringTokenizer tk = new FormulaTokenizer(formula, operations.keySet());
		final StringBuilder out = new StringBuilder();

		while (tk.hasMoreTokens()) {
			final String token = tk.nextToken();

			if (token.startsWith(",")) {
				out.append('0');
			}

			out.append(token);

			if (tk.hasMoreTokens()) {
				out.append(' ');
			}
		}

		return out.toString();
	}

	private void calculate() throws ParseException {
		final StringTokenizer tk = new FormulaTokenizer(formula, operations.keySet());

		BigDecimal value = BigDecimal.valueOf(0);
		BinaryOperator<BigDecimal> operation = operations.get("+");
		calculated = true;

		while (tk.hasMoreTokens()) {
			final String token = tk.nextToken();

			if (operations.containsKey(token)) {
				operation = operations.get(token);
			} else {
				value = operation.apply(value, toBigDecimal(token));
			}
		}

		result = format.format(value);
	}

	private BigDecimal toBigDecimal(final String token) throws ParseException {
		return BigDecimal.valueOf(getDefaultDecimalFormat().parse(token.replace(NEGATE, "-")).doubleValue());
	}

	public String processAction(final char action) throws ParseException {

		if (!isLegalAction(action)) {
			throw new IllegalArgumentException("unknown action " + action);
		}

		switch (action) {
			case SWT.CR:
			case '=':
				if (!calculated) {
					formula.append(result);
				}
				calculate();
				break;

			case SWT.BS:
				if (!calculated && !result.isEmpty()) {
					result = result.substring(0, result.length() - 1);
				}
				break;

			case ',':
				if (calculated) {
					result = ",";
					if (!lastCharIsOperator()) {
						formula.setLength(0);
					}
				} else if (!result.contains(",")) {
					result += action;
				}
				calculated = false;
				break;

			case '�':
				if (!calculated && !result.isEmpty()) {
					if (result.startsWith(Formula.NEGATE)) {
						result = result.substring(1);
					} else {
						result = Formula.NEGATE + result;
					}
				}
				break;

			case '+':
			case '-':
			case '*':
			case '/':
				if (!calculated) {
					formula.append(result);
				}
				if (lastCharIsOperator() && formula.length() > 0) {
					formula.deleteCharAt(formula.length() - 1);
				} else {
					calculate();
				}
				formula.append(action);
				break;

			case 'c':
			case 'C':
				formula.setLength(0);
				calculate();
				break;

			default:
				if (calculated) {
					result = "";
					if (!lastCharIsOperator()) {
						formula.setLength(0);
					}
				}
				result += action;
				calculated = false;
		}

		return result;
	}

	private boolean lastCharIsOperator() {
		final int length = formula.length();

		if (length > 0) {
			final char c = formula.charAt(length - 1);
			return (c < '0' || c > '9') && c != ',';
		}

		return true;
	}

	public boolean isLegalAction(final char action) {
		return "+-/*0123456789,=cC�".indexOf(action) != -1 || action == SWT.CR || action == SWT.BS;
	}

	public void setDecimalFormat(final DecimalFormat format) {
		this.format = (DecimalFormat) format.clone();
		this.format.setParseBigDecimal(true);
	}

	public DecimalFormat getDecimalFormat() {
		return format;
	}

	public void init(final BigDecimal initialValue) {
		final BigDecimal value = Util.nvl(initialValue, BigDecimal.ZERO);

		result = format.format(value);
		formula.setLength(0);

		if (!value.equals(BigDecimal.ZERO)) {
			formula.append(getDefaultDecimalFormat().format(initialValue));
		}
	}
}
