package org.greip.samples.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.greip.color.ColorChooserHSB;
import org.greip.color.ColorChooserRGB;
import org.greip.color.ColorPicker;
import org.greip.color.ColorResolution;
import org.greip.color.ColorWheelChooser;
import org.greip.samples.AbstractSample;

/**
 * This example demonstrates the use of {@link ColorWheelChooser}. The use of
 * {@link ColorChooserRGB}, {@link ColorChooserHSB} and {@link ColorPicker} is
 * identical.
 *
 * @author Thomas Lorbeer
 */
public class Sample4 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample4 sample = new Sample4();
		sample.show("Greip Color Chooser Sample");
	}

	@Override
	protected void layout() {
		final ColorWheelChooser colorChooser = new ColorWheelChooser(shell, ColorResolution.Minimal, true, true);
		colorChooser.addListener(SWT.Selection, e -> {
			final MessageBox msgBox = new MessageBox(shell);

			msgBox.setMessage("Selected color is " + colorChooser.getRGB() + ".");
			msgBox.open();
		});
	}
}