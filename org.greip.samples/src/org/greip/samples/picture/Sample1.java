package org.greip.samples.picture;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.FileDialog;
import org.greip.picture.Picture;
import org.greip.samples.AbstractSample;

/**
 * This example shows the use of Picture widget with a fix width scaled to 250
 * pixels.
 *
 * @author Thomas Lorbeer
 */
public class Sample1 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample1 sample = new Sample1();
		sample.show("Greip Picture Sample");
	}

	@Override
	protected void layout() {
		final FileDialog dialog = new FileDialog(shell);

		dialog.setFilterExtensions(new String[] { "*.bmp;*.gif;*.jpg;*.png" });
		final String filename = dialog.open();

		if (filename != null) {
			final Picture picture = new Picture(shell, SWT.BORDER);

			picture.loadImage(filename);
			picture.scaleTo(new Point(SWT.DEFAULT, 250));

		} else {
			shell.close();
		}
	}
}
