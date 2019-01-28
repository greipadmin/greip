package org.greip.samples.calculator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.greip.calculator.Calculator;
import org.greip.calculator.CalculatorTextAdapter;
import org.greip.samples.AbstractSample;

/**
 * The example shows how to use the {@link Calculator} in conjunction with an
 * SWT Text widget. Press the equals sign to open the calculator and ctrl+enter
 * to close the calculator popup.
 *
 * @author Thomas Lorbeer
 */
public class Sample2 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample2 sample = new Sample2();
		sample.show("Greip Calculator Sample", true);
	}

	@Override
	protected void layout() {
		final Text txt = new Text(shell, SWT.RIGHT | SWT.BORDER);
		txt.setLayoutData(new GridData(100, SWT.DEFAULT));

		new CalculatorTextAdapter(txt);
		txt.setText("0");
		txt.selectAll();
	}
}
