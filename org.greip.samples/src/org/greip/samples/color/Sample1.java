package org.greip.samples.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.greip.color.ColorButton;
import org.greip.color.ColorChooserHSB;
import org.greip.color.ColorChooserRGB;
import org.greip.color.ColorPicker;
import org.greip.color.ColorResolution;
import org.greip.color.ColorWheelChooser;
import org.greip.samples.AbstractSample;

/**
 * This example demonstrates the use of {@link ColorButton} with a color chooser
 * factory and a classical {@link SelectionListener}.
 *
 * The available factories are {@link ColorWheelChooser.Factory},
 * {@link ColorChooserRGB.Factory}, {@link ColorChooserHSB.Factory} and
 * {@link ColorPicker.Factory}.
 */
public class Sample1 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample1 sample = new Sample1();
		sample.show("Greip ColorButton Sample");
	}

	@Override
	protected void layout() {
		final ColorButton colorButton = new ColorButton(shell, SWT.NONE);
		colorButton.setText("Click me!");

		// definition of the color chooser to be used
		colorButton.setColorChooserFactory(new ColorChooserHSB.Factory(ColorResolution.Medium, true, true));

		colorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// initialize with current background rgb
				colorButton.setRGB(shell.getBackground().getRGB());

				if (colorButton.chooseRGB()) {
					// set new color to shell background
					final Color color = new Color(display, colorButton.getRGB());
					shell.setBackground(color);
					color.dispose();
				}
			}
		});
	}
}