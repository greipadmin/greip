package org.greip.samples.calculator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.greip.calculator.Calculator;
import org.greip.samples.AbstractSample;

/**
 * The example shows how to use the {@link Calculator} in a resizable window.
 *
 * @author Thomas Lorbeer
 */
public class Sample1 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample1 sample = new Sample1();
		sample.show("Greip Calculator Sample", false);
	}

	@Override
	protected void layout() {
		final Calculator calculator = new Calculator(shell);
		calculator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
}
