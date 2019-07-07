/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.calculator;

import static org.greip.calculator.CalcualtionEngine.*;
import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.greip.calculator.CalcualtionEngine.CalculationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestCalculationEngine {

	private String inputString;
	private String expectedFormulaText;
	private String expectedInputText;

	@Parameters(name = "\"{0}\" = \"{1}\" ({index})")
	public static Collection<Object[]> data() {
		// @formatter:off
		return Arrays.asList(new Object[][] {
			// empty formula
			{ "", "", "0" },

			// first action
			{ "2", "", "2" },
			{ "+", "0+", "0" },
			{ "02", "", "2" },

			// clear
			{ "3c", "", "0" },
			{ "3+2=c", "", "0" },

			// clear entry
			{ "3e", "", "0" },
			{ "23+2e", "23+", "0" },

			// backspace
			{ "34" + SWT.BS, "", "3" },
			{ "3" + SWT.BS, "", "0" },

			// calculate
			{ "=", "", "0" },
			{ "5=", "", "5" },
			{ "2+=", "", "4"},
			{ "2+3=", "", "5" },
			{ "1+(5*=", "", "26" },
			{ "2+3=*5", "5*", "5" },
			{ "(66*3)((=", "", "198" },
			{ "3+(1+2=", "", "6" },
			{ "1+(1+2)==", "", "4" },

			// repeat calculate
			{ "5==", "", "5" },
			{ "2+3==", "", "8" },
			{ "2+3===", "", "11" },
			{ "2+==", "", "6" },

			// last char is open parenthesis
			{ "1+(*", "1+(", "0" },

			// sign
			{ String.valueOf(SIGN), "", "0" },
			{ "0" + SIGN, "", "0" },
			{ "0" + SIGN + "7", "", "7" },
			{ "3" + SIGN, "", NEGATE + "3" },
			{ "3" + SIGN + "=", "", NEGATE + "3" },
			{ "3" + SIGN + "*2", NEGATE + "3*", "2" },
			{ "3" + SIGN + "*2=", "", NEGATE + "6" },
			{ "3+" + SIGN, "3+", NEGATE + "3" },
			{ "4" + SIGN + SIGN, "", "4" },
			{ "1+2=" + SIGN, "", NEGATE + "3" },
			{ "3" + SIGN + "==", "", NEGATE + "3" },

			// open parenthesis
			{ "(", "(", "0" },
		    { "((", "((", "0" },
			{ "2(", "", "2" },
			{ "2+(", "2+(", "0" },
			{ "2+(3+(", "2+(3+(", "0" },
			{ "2+(3(", "2+(", "3" },
			{ "2+*(", "2*(", "0" },
			{ "2+((", "2+((", "0" },
			{ "66*3(", "66*", "3" },
			{ "66*3((", "66*", "3" },
			{ "(66*3)(", "(66*3)", "198" },
			{ "(66*3)((", "(66*3)", "198" },
			{ "(66*3)((=", "", "198" },

			// close parenthesis
			{ "(6)", "(6)", "6" },
			{ "(6))", "(6)", "6" },
			{ "(6+3)", "(6+3)", "9" },
			{ "(6+3)2", "(6+3)", "9" },
			{ "(6+3+)", "(6+3+9)", "18" },
			{ "(6+3)*", "(6+3)*", "9" },
			{ "(6+3)*2", "(6+3)*", "2" },
			{ "(6+3)*2=", "", "18" },
			{ "66*3()", "66*", "3" },
			{ "66*()", "66*(", "0" },
			{ "66*(())", "66*((", "0" },

			// overwrite operator
			{ "6*+", "6+", "6" },

			// percentage calculation
			{ "%", "", "0" },
			{ "1,%", "", "1," },
			{ "1+%", "1+", "1" },
			{ "1+(%", "1+(", "0" },
			{ "(1+2)%", "(1+2)", "3" },
			{ "50-10%", "50-5", "5" },
			{ "50-3,25%", "50-1,625", "1,625" },
			{ "50-10%(", "50-5", "5" },
			{ "50-10%=", "", "45" },
			{ "50-(10%=", "", "40" },
			{ "800-3%", "800-24", "24" },

			// decimal separator
			{ ",", "", "0," },
			{ ",,", "", "0," },
			{ "(,", "(", "0," },
			{ "(1+2),", "(1+2)", "3" },
			{ "5,,", "", "5," },
			{ "5,1,", "", "5,1" },
			{ "5,3=", "", "5,3" },
			{ "3+,", "3+", "0," },
			{ "1+,3", "1+", "0,3" },
			{ "1+,3=", "", "1,3" },

			// Backspace
			{ "" + SWT.BS, "", "0" },
			{ "1" + SWT.BS, "", "0" },
			{ "1+2" + SWT.BS, "1+", "0" },
			{ "1+2" + SWT.BS + SWT.BS, "1+", "0" },
			{ "10+2=" + SWT.BS, "", "1" },
			{ "10+2=" + SWT.BS + SWT.BS, "", "0" },

			// Memory functions
			{ "" + M_PLUS, "", "0" },
			{ "" + M_MINUS, "", "0" },
			{ "" + MR, "", "0" },
			{ "2" + MR, "", "0" },
			{ "1+2" + MS, "1+", "2" },
			{ "1+2" + M_PLUS + "=", "", "3" },
			{ "1+2" + M_PLUS + "=" + MR, "", "2" },
			{ "1+2" + MS + "=" + M_PLUS + MR, "", "5" },
			{ "1+2" + M_PLUS + "=" + MS + MR, "", "3" },
			{ "1+2" + MS + "c1+3=" + M_MINUS + MR, "", "-2" },
			{ "2" + M_PLUS + "+34" + MR, "2+", "2" },
			{ "2" + M_PLUS + MC + MR, "", "0" },

			// functions
			{ "3" + SIN, "sin(3)", "0,052" },
			{ "3" + SIN+"=", "", "0,052" },
			{ "1+3" + SIN, "1+sin(3)", "0,052" },
			{ "1+3" + SIN + "=", "", "1,052" },
			{ "3" + SIN + "2", "", "2" },
			{ "3" + SIN + SINH, "sinh(sin(3))", "0,052" },
			{ "3+" + SIN, "3+sin(3)", "0,052" },
			{ "50+" + SIN + TAN, "tan(50+sin(50))", "0,013" },
			{ "50+" + SIN + TAN + "=", "", "1,225" },
			{ "2" + LOG + "+2" + LOG + "=", "", "0,602" },
			{ "" + PI + LOG + LN + LN, "log(ln(ln(3,143)))", null },
			{ "390" + SINH, "sinh(390)", "1,185" },

			// constants
			{ "3+" + PI, "3+", "3,142" },
			{ "3+" + PI + "=", "", "6,142" },
			{ "3+1=" + PI, "", "3,142" },
			{ "3" + LOG + PI, "", "3,142" }
		});
		// @formatter:on
	}

	public TestCalculationEngine(String inputString, String expectedFormulaText, String expectedInputText) {
		this.inputString = inputString;
		this.expectedFormulaText = expectedFormulaText.replace('*', CalcualtionEngine.MULTIPLY);
		this.expectedInputText = expectedInputText;
	}

	@Test
	public void test() throws Exception {
		final CalcualtionEngine engine = new CalcualtionEngine();

		try {
			engine.setDecimalFormat(new DecimalFormat("#,##0.###"));
			engine.process(inputString.toCharArray());
			final CalculationResult result = engine.getCalculationResult();

			assertEquals(expectedFormulaText, result.getFormula().replace(" ", ""));
			assertEquals(expectedInputText, result.getResult());

		} catch (OverflowException e) {
			if (expectedInputText != null) {
				fail("OverflowException expected.");
			}
		}
	}
}
