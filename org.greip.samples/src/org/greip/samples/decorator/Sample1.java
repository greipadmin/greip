package org.greip.samples.decorator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.greip.decorator.ImageDecorator;
import org.greip.samples.AbstractSample;

/**
 * This example shows the display of an animated GIF using the
 * {@link ImageDecorator}.
 *
 * @author Thomas Lorbeer
 */
public class Sample1 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample1 sample = new Sample1();
		sample.show("Greip animated GIF Sample");
	}

	@Override
	protected void layout() {
		// load the image
		final InputStream stream = getClass().getResourceAsStream("/images/005.gif");

		// Creation of the composite on which the decorator should be displayed.
		// It is important to specify SWT.DOUBLE_BUFFERED, otherwise the display
		// flickers.
		final Composite composite = new Composite(shell, SWT.DOUBLE_BUFFERED);
		composite.setLayoutData(new GridData(250, 250));

		// creates the decorator, sets the image and scale it
		final ImageDecorator decorator = new ImageDecorator(composite);
		decorator.loadImage(stream);
		decorator.scaleTo(new Point(SWT.DEFAULT, 250));

		// adds a listener to paint the decorator
		composite.addListener(SWT.Paint, e -> decorator.doPaint(e.gc, 0, 0));
	}
}
