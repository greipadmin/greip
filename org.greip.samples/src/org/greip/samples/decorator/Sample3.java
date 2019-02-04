package org.greip.samples.decorator;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.greip.decorator.ShapeDecorator;
import org.greip.samples.AbstractSample;

/**
 * The example shows the use of an {@link ShapeDecorator} to display a formatted
 * date.
 *
 * @author Thomas Lorbeer
 */
public class Sample3 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample3 sample = new Sample3();
		sample.show("Greip ShapeDecorator Sample", true);
	}

	@Override
	protected void layout() {

		// Creation of the composite on which the decorator should be displayed.
		// It is important to specify SWT.DOUBLE_BUFFERED, otherwise the display
		// flickers.
		final Composite composite = new Composite(shell, SWT.DOUBLE_BUFFERED);
		composite.setLayoutData(new GridData(200, 200));
		composite.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));

		// create a large font to use at decorator
		final FontData fontData = shell.getFont().getFontData()[0];
		final Font font = new Font(display, fontData.getName(), 24, SWT.BOLD);
		shell.addListener(SWT.Dispose, e -> font.dispose());

		// create the decorator
		final ShapeDecorator<Date> decorator = new ShapeDecorator<>(composite);

		// set value and output format
		decorator.setValue(new Date());
		decorator.setFormat(new SimpleDateFormat("hh:mm:ss"));

		// configure the shape
		decorator.setShapeSize(140, 50);
		decorator.setCornerArc(8, 8);
		decorator.setLineWidth(1);

		// configure font and colors
		decorator.setFont(font);
		decorator.setValueColor(display.getSystemColor(SWT.COLOR_GREEN));
		decorator.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
		decorator.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));

		// adds a listener to paint the decorator centered
		composite.addListener(SWT.Paint, e -> {
			final Rectangle clientArea = composite.getClientArea();
			final Point size = decorator.getSize();

			decorator.doPaint(e.gc, (clientArea.width - size.x) / 2, (clientArea.height - size.y) / 2);
		});
	}
}
