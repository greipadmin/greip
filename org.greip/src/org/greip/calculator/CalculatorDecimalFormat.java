package org.greip.calculator;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class CalculatorDecimalFormat extends DecimalFormat {

	private static final long serialVersionUID = -3376729682573336894L;

	private int maxLength;

	public CalculatorDecimalFormat() {
		setGroupingUsed(true);
		setMaxLength(6);
	}

	public void setMaxLength(final int maxLength) {
		if (maxLength < 6) {
			throw new IllegalArgumentException("The maximum length must be greater than 6.");
		}
		this.maxLength = maxLength;

		setMaximumIntegerDigits(maxLength);
		setMaximumFractionDigits(maxLength);
	}

	@Override
	public StringBuffer format(final long number, final StringBuffer result, final FieldPosition fieldPosition) {
		return super.format(Double.valueOf(number), result, fieldPosition);
	}

	@Override
	public StringBuffer format(final double number, final StringBuffer result, final FieldPosition fieldPosition) {

		final Entry<DecimalFormat, String> result1 = formatSimple(number);
		final Entry<DecimalFormat, String> result2 = formatWithExponent(number);

		if (result1 != null) {
			final Double diff1 = getDifference(number, result1);
			final Double diff2 = getDifference(number, result2);

			return new StringBuffer(diff1.compareTo(diff2) <= 0 ? result1.getValue() : result2.getValue());
		}

		return new StringBuffer(result2.getValue());
	}

	private Double getDifference(final double number, final Entry<DecimalFormat, String> entry) {

		try {
			final double value = entry.getKey().parse(entry.getValue()).doubleValue();
			return Math.abs(number - value);
		} catch (final ParseException e) {
			throw new IllegalStateException(e);
		}
	}

	private Entry<DecimalFormat, String> formatWithExponent(final double number) {
		String pattern = createPatternWithExponent();

		do {
			final DecimalFormat df = new DecimalFormat(pattern);
			final String res = df.format(number);
			if (res.length() <= maxLength) {
				return new SimpleEntry<>(df, res.replace(",E", "E"));
			}
			pattern = pattern.replaceFirst("#E", "E");
		} while (true);
	}

	private Entry<DecimalFormat, String> formatSimple(final double number) {
		final DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(getMaximumFractionDigits());
		df.setMaximumIntegerDigits(Integer.MAX_VALUE);
		df.setGroupingUsed(isGroupingUsed());

		while (df.getMaximumFractionDigits() > 0) {
			df.setMaximumFractionDigits(df.getMaximumFractionDigits() - 1);
			final String res = df.format(number);
			if (res.length() <= maxLength) {
				return new SimpleEntry<>(df, res);
			}
		}

		return null;
	}

	private String createPatternWithExponent() {
		final StringBuilder sb = new StringBuilder("0.");

		for (int i = 0; i < maxLength - 3; i++) {
			sb.append('#');
		}

		return sb.append("E0").toString();
	}
}
