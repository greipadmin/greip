package org.greip.samples.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.greip.color.ColorButton;
import org.greip.samples.AbstractSample;

/**
 * This example demonstrates the use of {@link ColorButton} with a default color
 * chooser factory and a classical {@link SelectionListener}.
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