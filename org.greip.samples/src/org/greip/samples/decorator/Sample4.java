package org.greip.samples.decorator;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.greip.decorator.PercentageDecorator;
import org.greip.samples.AbstractSample;

/**
 * The example shows the use of a {@link PercentageDecorator}.
 *
 * @author Thomas Lorbeer
 */
public class Sample4 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample4 sample = new Sample4();
		sample.show("Greip PercentageDecorator Sample", true);
	}

	@Override
	protected void layout() {

		// Creation of the composite on which the decorator should be displayed.
		// It is important to specify SWT.DOUBLE_BUFFERED, otherwise the display
		// flickers.
		final Composite composite = new Composite(shell, SWT.DOUBLE_BUFFERED);
		composite.setLayoutData(new GridData(200, 200));
		composite.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));

		// creates the decorator and set value
		final PercentageDecorator decorator = new PercentageDecorator(composite);
		decorator.setMinValue(-200d);
		decorator.setMaxValue(200d);
		decorator.setValue(165d);
		decorator.setFormat(new DecimalFormat("#0.0"));

		// configure colors
		decorator.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
		decorator.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		decorator.setValueColor(display.getSystemColor(SWT.COLOR_WHITE));

		// add a listener to paint the decorator centered
		composite.addListener(SWT.Paint, e -> {
			final Rectangle clientArea = composite.getClientArea();
			final Point size = decorator.getSize();

			decorator.doPaint(e.gc, (clientArea.width - size.x) / 2, (clientArea.height - size.y) / 2);
		});
	}
}
