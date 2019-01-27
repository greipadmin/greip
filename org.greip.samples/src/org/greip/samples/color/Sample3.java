package org.greip.samples.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.greip.color.ColorButton;
import org.greip.color.ColorPicker;
import org.greip.samples.AbstractSample;

/**
 * The example shows the use of an {@link ColorButton} in conjunction with a
 * {@link ColorPicker.Factory} and a list of predefined colors.
 *
 * @author Thomas Lorbeer
 */
public class Sample3 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample3 sample = new Sample3();
		sample.show("Greip ColorPicker Sample");
	}

	@Override
	protected void layout() {
		// define the color list to be used
		final RGB[] rgbs = new RGB[] { display.getSystemColor(SWT.COLOR_WHITE).getRGB(), display.getSystemColor(SWT.COLOR_YELLOW).getRGB(),
				display.getSystemColor(SWT.COLOR_GREEN).getRGB(), display.getSystemColor(SWT.COLOR_RED).getRGB(),
				display.getSystemColor(SWT.COLOR_CYAN).getRGB(), display.getSystemColor(SWT.COLOR_MAGENTA).getRGB(),
				display.getSystemColor(SWT.COLOR_BLUE).getRGB(), display.getSystemColor(SWT.COLOR_GRAY).getRGB(),
				display.getSystemColor(SWT.COLOR_BLACK).getRGB() };

		final ColorButton colorButton = new ColorButton(shell, SWT.NONE);
		colorButton.setText("Background");
		colorButton.setColorChooserFactory(new ColorPicker.Factory(rgbs));

		colorButton.addListener(SWT.Selection, e -> {
			// initialize with current background rgb
			colorButton.setRGB(shell.getBackground().getRGB());

			// pick new color from predefined color list
			if (colorButton.chooseRGB()) {
				// set new color to shell background
				final Color color = new Color(display, colorButton.getRGB());
				shell.setBackground(color);
				color.dispose();
			}
		});
	}
}