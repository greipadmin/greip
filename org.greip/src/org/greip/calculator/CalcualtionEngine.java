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

import org.eclipse.swt.SWT;
import org.greip.common.Util;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

class CalcualtionEngine {

	private static final String IGNORE = String.valueOf((char) 0);

	static final char NEGATE = '\u02D7';
	static final char SIGN = '\u00B1';
	static final char DIVIDE = '\u00F7';
	static final char MULTIPLY = '\u00D7';

	static final char MS = '\u0001';
	static final char MR = '\u0002';
	static final char MC = '\u0003';
	static final char M_PLUS = '\u0004';
	static final char M_MINUS = '\u0005';

	static final char SIN = '\uFFFF';
	static final char COS = '\uFFFE';
	static final char TAN = '\uFFFD';
	static final char SINH = '\uFFFC';
	static final char COSH = '\uFFFB';
	static final char TANH = '\uFFFA';

	static final char SQRT = '\uFFEF';
	static final char CBRT = '\uFFEE';
	static final char POW = '\uFFED';

	static final char LN = '\uFFCF';
	static final char LOG = '\uFFCE';
	static final char EXP = '\uFFCD';

	static final char PI = '\u03C0';
	static final char E = '\u2107';

	private static final String OPERATORS = "+-%/*^";

	private static final Function FSIN = new Func("sin", args -> Math.sin(Math.toRadians(args[0])));
	private static final Function FCOS = new Func("cos", args -> Math.cos(Math.toRadians(args[0])));
	private static final Function FTAN = new Func("tan", args -> Math.tan(Math.toRadians(args[0])));
	private static final Function FSINH = new Func("sinh", args -> Math.sinh(args[0]));
	private static final Function FCOSH = new Func("cosh", args -> Math.cosh(args[0]));
	private static final Function FTANH = new Func("tanh", args -> Math.tanh(args[0]));
	private static final Function FCBRT = new Func("cbrt", args -> Math.cbrt(args[0]));
	private static final Function FSQRT = new Func("sqrt", args -> Math.sqrt(args[0]));
	private static final Function FPOW = new Func("pow", args -> Math.pow(args[0], 2d));
	private static final Function FLN = new Func("ln", args -> Math.log(args[0]));
	private static final Function FLOG = new Func("log", args -> Math.log10(args[0]));
	private static final Function FEXP = new Func("exp", args -> Math.exp(args[0]));

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

	@FunctionalInterface
	interface Intrinsic {
		double calculate(double... values);
	}

	static class Func extends Function {

		private final Intrinsic intrinsic;

		Func(final String name, final Intrinsic function) {
			super(name);
			this.intrinsic = function;
		}

		@Override
		public double apply(final double... values) {
			return intrinsic.calculate(values);
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

	private void calculate() throws OverflowException {

		final BigDecimal value = calculateFormula();

		final int integerDigits = String.valueOf(value.longValue()).length();
		final int maxIntegerDigits = format.getMaximumIntegerDigits();

		if (integerDigits > maxIntegerDigits) {
			throw new OverflowException();
		}

		result = format.format(value).replace('-', NEGATE);
	}

	private BigDecimal calculateFormula() {

		final String expression = formula.toString().replace(NEGATE, '-').replace(decimalSeparator, '.');

		int openParenthesis = 0;
		int index = 0;

		while (openParenthesis < parentheses) {
			if (expression.charAt(index) == '(') {
				openParenthesis++;
			}
			index++;
		}

		final ExpressionBuilder eb = new ExpressionBuilder(expression.substring(index));
		eb.functions(FSIN, FCOS, FTAN, FSINH, FCOSH, FTANH, FSQRT, FCBRT, FPOW);

		return new BigDecimal(eb.build().evaluate());
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
			e.printStackTrace();
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
			case '^':
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

			case SIN:
				executeFunction(FSIN);
				break;
			case COS:
				executeFunction(FCOS);
				break;
			case TAN:
				executeFunction(FTAN);
				break;

			case SINH:
				executeFunction(FSINH);
				break;
			case COSH:
				executeFunction(FCOSH);
				break;
			case TANH:
				executeFunction(FTANH);
				break;

			case SQRT:
				executeFunction(FSQRT);
				break;
			case CBRT:
				executeFunction(FCBRT);
				break;

			case POW:
				executeFunction(FPOW);
				break;

			case LN:
				executeFunction(FLN);
				break;
			case LOG:
				executeFunction(FLOG);
				break;
			case EXP:
				executeFunction(FEXP);
				break;

			case PI:
				applyConstant(Math.PI);
				break;

			case E:
				applyConstant(Math.E);
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
				if (IGNORE.equals(lastOperation)) {
					lastOperation = null;
					result = String.valueOf(command);
					formula.setLength(0);
				} else if (!lastCharIs(')')) {
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

		isNumberEntered = Character.isDigit(command) || Util.in(command, SIGN, PI, E);
	}

	private void applyConstant(final double constant) {
		if (!lastCharIsOperator() || lastCharIs(')')) {
			formula.setLength(0);
		}
		result = getDecimalFormat().format(constant);
	}

	private void executeFunction(final Function function) throws ParseException {
		if (isNumberEntered || formula.length() == 0) {
			formula.append(function.getName()).append('(');
			formula.append(getCurrentValueAsString());
			formula.append(')');
			result = getDecimalFormat().format(function.apply(getCurrentValue().doubleValue()));
//		} else {
//			formula.insert(0, function.getName() + '(');
//			formula.append(')');
//			result = getDecimalFormat().format(function.apply(getCurrentValue().doubleValue()));
		}
		lastOperation = IGNORE;
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
		return (OPERATORS + SIN + COS + TAN + SINH + COSH + TANH + "0123456789=CE()" + SIGN + SWT.BS).indexOf(normalizeCommand(command)) != -1
				|| command == decimalSeparator;
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
