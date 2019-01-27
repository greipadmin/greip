package org.greip.samples.font;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.greip.color.ColorResolution;
import org.greip.color.ColorWheelChooser;
import org.greip.font.FontButton;
import org.greip.samples.AbstractSample;

/**
 * The example demonstrates the use of a font consumer instead of an selection
 * listener at {@link FontButton}.
 *
 * @author Thomas Lorbeer
 */
public class Sample3 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample3 sample = new Sample3();
		sample.show("Greip FontButton Sample", true);
	}

	@Override
	protected void layout() {

		// create color and font registry
		final FontRegistry fontRegistry = new FontRegistry(display, true);
		final ColorRegistry colorRegistry = new ColorRegistry(display, true);

		// create the font button
		final FontButton button = new FontButton(shell, SWT.NONE);
		button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		// create a label to display the selected font
		final Label lblFontName = new Label(shell, SWT.CENTER);
		lblFontName.setLayoutData(new GridData(400, 150));
		lblFontName.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		// configure the font button
		button.setText("  Select Font...  ");
		button.setColorChooserFactory(new ColorWheelChooser.Factory(ColorResolution.Maximal, true, true));

		button.setFontConsumer((fd, rgb) -> {
			// apply the selected font
			fontRegistry.put(fd.toString(), new FontData[] { fd });
			lblFontName.setFont(fontRegistry.get(fd.toString()));
			lblFontName.setText(fd.getName());

			// apply the selected color
			colorRegistry.put(rgb.toString(), rgb);
			lblFontName.setForeground(colorRegistry.get(rgb.toString()));
		});
	}
}
