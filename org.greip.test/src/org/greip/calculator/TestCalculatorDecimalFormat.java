package org.greip.calculator;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCalculatorDecimalFormat {

	private final CalculatorDecimalFormat cdf = new CalculatorDecimalFormat();
	
	@Test
	public void testFormat_noGrouping() {
		cdf.setMaxLength(6);
		cdf.setGroupingUsed(false);
		
		assertEquals("123456", cdf.format(123456));
		assertEquals("1234,1", cdf.format(1234.1));
		assertEquals("12345", cdf.format(12345.1));
		assertEquals("123457", cdf.format(123456.7));
		assertEquals("1,23E6", cdf.format(1234567.8));
		assertEquals("1,23E9", cdf.format(1231231230d));
		assertEquals("1,2E10", cdf.format(12312312300d));
		assertEquals("-1E13", cdf.format(-12312312300000d));
	}

	@Test
	public void testFormat_greaterOrEqualOne() {
		cdf.setMaxLength(7);

		assertEquals("1", cdf.format(1));
		assertEquals("1,1", cdf.format(1.1));
		assertEquals("123.456", cdf.format(123456));
		assertEquals("1.234,1", cdf.format(1234.1));
		assertEquals("12.345", cdf.format(12345.1));
		assertEquals("0,12345", cdf.format(.12345));
		assertEquals("0,12346", cdf.format(.123456));
		assertEquals("123.457", cdf.format(123456.7));
		assertEquals("1,235E6", cdf.format(1234567.8));
		assertEquals("1,231E8", cdf.format(123123123d));
		assertEquals("1,23E10", cdf.format(12312312300d));
		assertEquals("-1,2E13", cdf.format(-12312312300000d));
	}

	@Test
	public void testFormat_betweenZeroAndOne() throws Exception {
		cdf.setMaxLength(6);

		assertEquals("0,1", cdf.format(.1));
		assertEquals("0,1234", cdf.format(.1234));
		assertEquals("0,1234", cdf.format(.12342));
		assertEquals("0,3457", cdf.format(.34567));
		assertEquals("0,0067", cdf.format(.0067));
		assertEquals("6,7E-4", cdf.format(.00067));
		assertEquals("7E-5", cdf.format(.00007));
		
		cdf.setMaxLength(7);
		assertEquals("-0,1", cdf.format(-.1));
		assertEquals("-0,1234", cdf.format(-.1234));
		assertEquals("-0,1234", cdf.format(-.12342));
		assertEquals("-0,3457", cdf.format(-.34567));
		assertEquals("-0,0067", cdf.format(-.0067));
		assertEquals("-6,7E-4", cdf.format(-.00067));
		assertEquals("-7E-5", cdf.format(-.00007));
	}

	@Test
	public void testParse_greaterOrEqualOne() throws Exception {
		assertEquals(Long.valueOf(1), cdf.parse("1"));
		assertEquals(Double.valueOf(1.1D), cdf.parse("1,1"));
		assertEquals(Long.valueOf(123456), cdf.parse("123.456"));
		assertEquals(Double.valueOf(1234.1D), cdf.parse("1.234,1"));
		assertEquals(Long.valueOf(12345), cdf.parse("12.345"));
		assertEquals(Double.valueOf(0.12345D), cdf.parse("0,12345"));
		assertEquals(Long.valueOf(1235000), cdf.parse("1,235E6"));
		assertEquals(Long.valueOf(-12000000000000L), cdf.parse("-1,2E13"));
	}

	@Test
	public void testParse_betweenZeroAndOne() throws Exception {
		assertEquals(Double.valueOf(0.1D), cdf.parse("0,1"));
		assertEquals(Double.valueOf(0.1234D), cdf.parse("0,1234"));
		assertEquals(Double.valueOf(0.0067D), cdf.parse("0,0067"));
		assertEquals(Double.valueOf(0.00067D), cdf.parse("6,7E-4"));
		assertEquals(Double.valueOf(0.00007D), cdf.parse("7E-5"));
		
		assertEquals(Double.valueOf(-0.1D), cdf.parse("-0,1"));
		assertEquals(Double.valueOf(-0.1234D), cdf.parse("-0,1234"));
		assertEquals(Double.valueOf(-0.00067D), cdf.parse("-6,7E-4"));
		assertEquals(Double.valueOf(-0.00007D), cdf.parse("-7E-5"));
	}
}
