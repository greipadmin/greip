package org.greip.samples.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.greip.color.ColorButton;
import org.greip.color.ColorChooserHSB;
import org.greip.color.ColorResolution;
import org.greip.samples.AbstractSample;

/**
 * The example shows the use of an {@link ColorButton} using a color consumer
 * instead of a selection listener.
 */
public class Sample2 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample2 sample = new Sample2();
		sample.show("Greip ColorButton Sample");
	}

	@Override
	protected void layout() {
		final ColorButton colorButton = new ColorButton(shell, SWT.NONE);
		colorButton.setText("Click me!");

		// definition of the color chooser to be used
		colorButton.setColorChooserFactory(new ColorChooserHSB.Factory(ColorResolution.Medium, true, true));

		// initialize with current background rgb
		colorButton.setRGB(shell.getBackground().getRGB());

		colorButton.setColorConsumer(rgb -> {
			// set new color to shell background
			final Color color = new Color(display, colorButton.getRGB());
			shell.setBackground(color);
			color.dispose(); // don't forget, created colors must be disposed
		});
	}
}