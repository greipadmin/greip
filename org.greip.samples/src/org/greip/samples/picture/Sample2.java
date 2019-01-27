package org.greip.samples.picture;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.greip.picture.Picture;
import org.greip.samples.AbstractSample;

/**
 * The example displays an animated GIF and surrounds it with a border that has
 * rounded corners.
 *
 * @author Thomas Lorbeer
 */
public class Sample2 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample2 sample = new Sample2();
		sample.show("Greip Picture Sample");
	}

	@Override
	protected void layout() {
		final Picture picture = new Picture(shell, SWT.NONE);

		// load the image and scale it to 250 pixels height
		final InputStream stream = getClass().getResourceAsStream("/images/005.gif");
		picture.loadImage(stream);

		// configure the border
		picture.setBorderWidth(5);
		picture.setBorderColor(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		picture.setCornerRadius(16);
	}
}
