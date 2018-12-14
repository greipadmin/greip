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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.BinaryOperator;

import org.eclipse.swt.SWT;
import org.greip.common.Util;

class Formula {

	static final char NEGATE = '\u02D7';
	static final char SIGN = '\u00B1';
	static final char DIVIDE = '\u00F7';
	static final char MULTIPLY = '\u00D7';

	static final char MS = '\u0001';
	static final char MR = '\u0002';
	static final char MC = '\u0003';
	static final char M_PLUS = '\u0004';
	static final char M_MINUS = '\u0005';

	private final Map<String, BinaryOperator<BigDecimal>> operations = new HashMap<>();
	private final String operators = "+-%" + DIVIDE + MULTIPLY;
	private final StringBuilder formula = new StringBuilder();
	private BigDecimal memory;

	private String result = "";
	private boolean calculated = true;
	private DecimalFormat format;
	private char decimalSeparator;
	private char lastOperator;
	private DecimalFormat defaultFormat;

	public Formula() {
		operations.put("+", BigDecimal::add);
		operations.put("-", BigDecimal::subtract);
		operations.put("%", (v1, v2) -> v1.multiply(v2).divide(new BigDecimal(100), 20, BigDecimal.ROUND_HALF_EVEN));
		operations.put(String.valueOf(DIVIDE), (v1, v2) -> v1.divide(v2, 20, BigDecimal.ROUND_HALF_EVEN));
		operations.put(String.valueOf(MULTIPLY), BigDecimal::multiply);

		setDecimalFormat(new DecimalFormat("#0.##########"));
		init(BigDecimal.ZERO);
	}

	public DecimalFormat getDefaultDecimalFormat() {
		return defaultFormat;
	}

	public String format() {
		final StringTokenizer tk = new FormulaTokenizer(formula, operators);
		final StringBuilder out = new StringBuilder();

		while (tk.hasMoreTokens()) {
			final String token = tk.nextToken();

			if (token.indexOf(decimalSeparator) == 0) {
				out.append('0');
			}

			out.append(token);

			if (tk.hasMoreTokens()) {
				out.append(' ');
			}
		}

		return out.toString();
	}

	private void calculate() throws ParseException, OverflowException {
		final StringTokenizer tk = new FormulaTokenizer(formula, operators);

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

		final int integerDigits = String.valueOf(value.longValue()).length();
		final int maxIntegerDigits = format.getMaximumIntegerDigits();

		if (integerDigits > maxIntegerDigits) {
			throw new OverflowException();
		}

		result = format.format(value);
	}

	private BigDecimal toBigDecimal(final String token) throws ParseException {
		return BigDecimal.valueOf(getDefaultDecimalFormat().parse(token.replace(NEGATE, '-')).doubleValue());
	}

	public String processAction(final String actions) throws ParseException, OverflowException {
		for (final char c : actions.toCharArray()) {
			processAction(c);
		}
		return result;
	}

	public String processAction(final char action) throws ParseException, OverflowException {
		try {
			return process(String.valueOf(action).replace('/', DIVIDE).replace('*', MULTIPLY).charAt(0));

		} catch (final ParseException | OverflowException e) {
			throw e;

		} catch (final Exception e) {
			formula.setLength(0);
			throw new OverflowException();
		}
	}

	private String process(final char action) throws ParseException, OverflowException {

		if (isMemoryAction(action)) {
			return processMemoryAction(action);
		}

		if (!isLegalAction(action)) {
			throw new IllegalArgumentException("unknown action " + action);
		}

		if (action == decimalSeparator) {
			if (calculated) {
				result = String.valueOf(decimalSeparator);
				if (!lastCharIsOperator()) {
					formula.setLength(0);
				}
			} else if (result.indexOf(decimalSeparator) == -1) {
				result += action;
			}
			calculated = false;
			return result;
		}

		switch (action) {
			case SWT.CR:
				if (!calculated) {
					formula.append(result);
					calculate();
				}
				break;

			case '=':
				if (!calculated) {
					formula.append(result);
					calculate();
					formula.setLength(0);
				} else if (lastOperator != 0) {
					if (!lastCharIsOperator()) {
						if (formula.length() == 0) {
							formula.append(getCurrentValueAsString());
						}
						formula.append(lastOperator);
					}
					formula.append(getCurrentValueAsString());
					calculate();
				}
				break;

			case SWT.BS:
				if (calculated || result.isEmpty()) {
					if (formula.length() > 0) {
						removeLastFormulaToken();
						calculated = false;
					}
				} else if (!calculated) {
					if (!result.isEmpty()) {
						result = result.substring(0, result.length() - 1);
					}
					if (formula.length() == 0 && result.isEmpty()) {
						calculate();
					}
				}
				break;

			case SIGN:
				if (!calculated && !result.isEmpty()) {
					if (result.indexOf(NEGATE) == 0) {
						result = result.substring(1);
					} else {
						result = NEGATE + result;
					}
				}
				break;

			case '+':
			case '-':
			case '%':
			case MULTIPLY:
			case DIVIDE:
				if (!calculated) {
					formula.append(getCurrentValueAsString());
				}
				if (formula.length() == 0 && calculated) {
					formula.append(getCurrentValueAsString());
				} else {
					if (lastCharIsOperator() && formula.length() > 0) {
						formula.deleteCharAt(formula.length() - 1);
					} else {
						calculate();
					}
				}
				formula.append(action);
				lastOperator = action;
				break;

			case 'c':
			case 'C':
				formula.setLength(0);
				calculate();
				break;

			case 'e':
			case 'E':
				result = "";
				if (formula.length() == 0) {
					calculate();
				}
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

	private String getCurrentValueAsString() throws ParseException {
		return getDefaultDecimalFormat().format(getCurrentValue()).replace('-', NEGATE);
	}

	private String processMemoryAction(final char action) throws ParseException {
		switch (action) {
			case MC:
				memory = null;
				break;
			case MR:
				if (memory != null) {
					result = getDefaultDecimalFormat().format(memory);
					calculated = false;
				}
				break;
			case MS:
				memory = getCurrentValue();
				break;
			case M_PLUS:
				memory = Util.nvl(memory, BigDecimal.ZERO).add(getCurrentValue());
				break;
			case M_MINUS:
				memory = Util.nvl(memory, BigDecimal.ZERO).subtract(getCurrentValue());
				break;
			default:
		}

		return result;
	}

	private BigDecimal getCurrentValue() throws ParseException {
		if (!calculated) return toBigDecimal(result);
		return BigDecimal.valueOf(getDecimalFormat().parse(result).doubleValue());
	}

	private void removeLastFormulaToken() {
		final String[] tokens = formula.toString().split(toRegex(operators));

		result = tokens[tokens.length - 1];
		formula.setLength(formula.lastIndexOf(result));
	}

	private static String toRegex(final String operators) {
		final StringBuilder regex = new StringBuilder("[");

		for (int i = 0; i < operators.length(); i++) {
			regex.append('\\').append(operators.charAt(i));
		}

		return regex.append(']').toString();
	}

	private boolean lastCharIsOperator() {
		final int length = formula.length();

		if (length > 0) {
			final char c = formula.charAt(length - 1);
			return (c < '0' || c > '9') && c != ',' && c != '=';
		}

		return false;
	}

	public boolean isLegalAction(final char action) {
		return ("+-/*%0123456789=cCeE" + SIGN + MULTIPLY + DIVIDE + SWT.CR + SWT.BS).indexOf(action) != -1 || action == decimalSeparator;
	}

	private static boolean isMemoryAction(final char action) {
		return action > '\u0000' && action < '\u0006';
	}

	public BigDecimal getMemory() {
		return memory;
	}

	public void setDecimalFormat(final DecimalFormat format) {
		BigDecimal value = BigDecimal.ZERO;

		try {
			if (this.format != null) {
				value = getCurrentValue();
			}
		} catch (final ParseException e) {
			// ignore
		}

		this.format = (DecimalFormat) format.clone();
		this.format.setParseBigDecimal(true);
		this.decimalSeparator = this.format.getDecimalFormatSymbols().getDecimalSeparator();

		defaultFormat = (DecimalFormat) this.format.clone();
		defaultFormat.setNegativePrefix("-");
		defaultFormat.setNegativeSuffix("");
		defaultFormat.setPositivePrefix("");
		defaultFormat.setPositiveSuffix("");
		defaultFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
		defaultFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
		defaultFormat.setMinimumFractionDigits(0);
		defaultFormat.setGroupingUsed(false);

		result = this.format.format(value.doubleValue());
	}

	public DecimalFormat getDecimalFormat() {
		return format;
	}

	public void init(final BigDecimal initialValue) {
		final BigDecimal value = Util.nvl(initialValue, BigDecimal.ZERO);
		result = format.format(value);
	}
}
