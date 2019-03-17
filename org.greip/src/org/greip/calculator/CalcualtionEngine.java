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
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.swt.SWT;
import org.greip.common.Util;

class CalcualtionEngine {

	static final char NEGATE = '\u02D7';
	static final char SIGN = '\u00B1';
	static final char DIVIDE = '\u00F7';
	static final char MULTIPLY = '\u00D7';

	static final char MS = '\u0001';
	static final char MR = '\u0002';
	static final char MC = '\u0003';
	static final char M_PLUS = '\u0004';
	static final char M_MINUS = '\u0005';

	private static final String OPERATORS = "+-%/*";

	static class CalculationResult {

		private final CharSequence formula;
		private final CharSequence result;

		private CalculationResult(final CharSequence formula, final CharSequence result) {
			this.formula = formula;
			this.result = result;
		}

		public String getFormula() {
			return formula.toString().replace('/', DIVIDE).replace('*', MULTIPLY);
		}

		public String getResult() {
			return result.toString();
		}
	}

	private final StringBuilder formula = new StringBuilder();
	private BigDecimal memory;

	private String result;
	private DecimalFormat format;
	private char decimalSeparator;
	private String lastOperation;
	private DecimalFormat defaultFormat;
	private int parentheses;
	private boolean isNumberEntered;

	public CalcualtionEngine() {
		setDecimalFormat(new DecimalFormat("#0.##########"));
		clearResult();
	}

	public DecimalFormat getDefaultDecimalFormat() {
		return defaultFormat;
	}

	private String getFormula() {
		return formula.toString().replaceAll("([" + Pattern.quote(OPERATORS) + "])", " $1 ").trim();
	}

	private void calculate() throws OverflowException, ScriptException {

		final BigDecimal value = calculateFormula();

		final int integerDigits = String.valueOf(value.longValue()).length();
		final int maxIntegerDigits = format.getMaximumIntegerDigits();

		if (integerDigits > maxIntegerDigits) {
			throw new OverflowException();
		}

		result = format.format(value).replace('-', NEGATE);
	}

	private BigDecimal calculateFormula() throws ScriptException {

		final String expression = formula.toString().replace(NEGATE, '-').replace(decimalSeparator, '.');

		int openParenthesis = 0;
		int index = 0;

		while (openParenthesis < parentheses) {
			if (expression.charAt(index) == '(') {
				openParenthesis++;
			}
			index++;
		}

		final ScriptEngineManager mgr = new ScriptEngineManager();
		final ScriptEngine engine = mgr.getEngineByName("JavaScript");

		return new BigDecimal(engine.eval(expression.substring(index)).toString());
	}

	private BigDecimal toBigDecimal(final String token) throws ParseException {
		return BigDecimal.valueOf(getDefaultDecimalFormat().parse(token.replace(NEGATE, '-')).doubleValue());
	}

	public void process(final char... commands) throws CalculationException {

		try {
			for (final char command : commands) {
				final char cmd = normalizeCommand(command);

				if (isMemoryCommand(cmd)) {
					processMemoryCommand(cmd);
				} else {
					processCommand(cmd);
				}
			}

		} catch (final Exception e) {
			resetTo(BigDecimal.ZERO);
			throw e instanceof CalculationException ? (CalculationException) e : new CalculationException(e);
		}
	}

	private static char normalizeCommand(final char command) {
		final String translations = "cCeE" + SWT.CR + "=" + MULTIPLY + "*" + DIVIDE + "/";
		final int index = translations.indexOf(command);
		return index == -1 || index % 2 == 1 ? command : translations.charAt(index + 1);
	}

	public CalculationResult getCalculationResult() {
		return new CalculationResult(getFormula(), result);
	}

	private void processCommand(final char command) throws Exception {

		if (!isLegalCommand(command)) {
			throw new IllegalArgumentException("unknown command " + command);
		}

		if (command == decimalSeparator) {
			if (!lastCharIs(')')) {
				if (!isNumberEntered) {
					result = "0" + String.valueOf(decimalSeparator);
					if (!lastCharIsOperator()) {
						formula.setLength(0);
					}
				} else if (result.indexOf(decimalSeparator) == -1) {
					result += command;
				}
				isNumberEntered = true;
			}
			return;
		}

		if (command == SWT.BS) {
			if (result.length() > 1) {
				result = result.substring(0, result.length() - 1);
				isNumberEntered = true;
			} else {
				result = "0";
				isNumberEntered = false;
			}
			return;
		}

		switch (command) {
			case '=':
				if (lastOperation != null) {
					if (!lastCharIs(')')) {
						if (isNumberEntered || lastCharIsOperator()) {
							formula.append(getCurrentValueAsString());
							lastOperation += getCurrentValueAsString();
						} else if (lastOperation.length() > 1) {
							formula.append(getCurrentValueAsString());
							formula.append(lastOperation);
						}
					}
					if (formula.length() > 0) {
						closeAllParentheses();
						calculate();
						formula.setLength(0);
					}
				}
				break;

			case SIGN:
				if (getCurrentValue().compareTo(BigDecimal.ZERO) != 0) {
					if (result.indexOf(NEGATE) == 0) {
						result = result.substring(1);
					} else {
						result = NEGATE + result;
					}
				}
				break;

			case '%':
				if (isNumberEntered && lastCharIsOperator() && !lastCharIs('(')) {
					final char operator = formula.charAt(formula.length() - 1);
					formula.setLength(formula.length() - 1);
					final double value = calculateFormula().doubleValue();
					final double factor = 100 / getCurrentValue().doubleValue();

					result = getDecimalFormat().format(value / factor);

					formula.append(operator);
					formula.append(result);
				}
				break;

			case '+':
			case '-':
			case '*':
			case '/':
				if (isNumberEntered || formula.length() == 0) {
					formula.append(getCurrentValueAsString());
					calculate();
					formula.append(command);
				} else if (!lastCharIs('(')) {
					if (!lastCharIs(')')) formula.setLength(formula.length() - 1);
					formula.append(command);
				}
				lastOperation = String.valueOf(command);
				break;

			case 'C':
				resetTo(BigDecimal.ZERO);
				break;

			case 'E':
				clearResult();
				break;

			case '(':
				if (isNumberEntered || formula.toString().matches(".*[0-9\\)]$")) return;
				formula.append(command);
				clearResult();
				parentheses++;
				break;

			case ')':
				if (parentheses > 0 && getCurrentValue().compareTo(BigDecimal.ZERO) != 0) {
					if (!isNumberEntered) {
						final char operator = formula.charAt(formula.length() - 1);
						formula.setLength(formula.length() - 1);
						result = getDecimalFormat().format(calculateFormula());

						formula.append(operator);
						formula.append(result);
					} else {
						formula.append(getCurrentValueAsString());
					}
					formula.append(')');
					parentheses--;
					calculate();
				}
				break;

			default:
				if (!lastCharIs(')')) {
					if (!isNumberEntered) {
						result = "";
						if (!lastCharIsOperator()) {
							formula.setLength(0);
						}
					}
					result += command;
					result = result.replaceFirst("^0(\\d)", "$1");
				}
		}

		if (command != '=' && lastOperation != null && lastOperation.length() > 1) {
			lastOperation = null;
		}

		isNumberEntered = command == SIGN || Character.isDigit(command);
	}

	private void closeAllParentheses() {
		while (parentheses > 0 && formula.charAt(formula.length() - 1) == '(') {
			formula.deleteCharAt(formula.length() - 1);
			parentheses--;
		}
		while (parentheses > 0) {
			formula.append(')');
			parentheses--;
		}
	}

	private String getCurrentValueAsString() throws ParseException {
		return getDecimalFormat().format(getCurrentValue()).replace('-', NEGATE);
	}

	private void processMemoryCommand(final char command) throws ParseException {
		final BigDecimal memoryValue = Util.nvl(memory, BigDecimal.ZERO);

		switch (command) {
			case MC:
				memory = null;
				break;
			case MR:
				result = getDecimalFormat().format(memoryValue);
				break;
			case MS:
				memory = getCurrentValue();
				break;
			case M_PLUS:
				memory = memoryValue.add(getCurrentValue());
				break;
			case M_MINUS:
				memory = memoryValue.subtract(getCurrentValue());
				break;
			default:
				throw new IllegalStateException("Unknown memory command.");
		}
	}

	private BigDecimal getCurrentValue() throws ParseException {
		if (isNumberEntered) return toBigDecimal(result);
		return BigDecimal.valueOf(getDecimalFormat().parse(result).doubleValue());
	}

	private boolean lastCharIsOperator() {
		final int length = formula.length();

		if (length > 0) {
			final char c = formula.charAt(length - 1);
			return (c < '0' || c > '9') && c != ',' && c != '=';
		}

		return false;
	}

	private boolean lastCharIs(final char ch) {
		final int length = formula.length();

		if (length > 0) {
			final char c = formula.charAt(length - 1);
			return c == ch;
		}

		return false;
	}

	public boolean isLegalCommand(final char command) {
		return (OPERATORS + "0123456789=CE()" + SIGN + SWT.BS).indexOf(normalizeCommand(command)) != -1 || command == decimalSeparator;
	}

	private static boolean isMemoryCommand(final char command) {
		return command > '\u0000' && command < '\u0006';
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

	private void clearResult() {
		result = format.format(BigDecimal.ZERO);
	}

	public void resetTo(final BigDecimal value) {
		formula.setLength(0);
		isNumberEntered = false;
		lastOperation = null;
		parentheses = 0;
		result = format.format(Util.nvl(value, BigDecimal.ZERO));
	}

	public BigDecimal compute() throws CalculationException {
		try {
			if (lastOperation != null && lastOperation.length() == 1) {
				formula.append(getCurrentValueAsString());
				closeAllParentheses();
				calculate();
			}
			return getCurrentValue();

		} catch (final Exception e) {
			throw e instanceof CalculationException ? (CalculationException) e : new CalculationException(e);

		} finally {
			resetTo(BigDecimal.ZERO);
		}
	}
}
