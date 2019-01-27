package org.greip.samples.separator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.greip.common.Greip;
import org.greip.samples.AbstractSample;
import org.greip.separator.LineStyle;
import org.greip.separator.Separator;

/**
 * This example shows the creation of a vertically oriented {@link Separator}
 * and demonstrates the detection of click events.
 *
 * @author Thomas Lorbeer
 */
public class Sample2 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample2 sample = new Sample2();
		sample.show("Greip Separator Sample");
	}

	@Override
	protected void layout() {
		final Separator separator = new Separator(shell, SWT.VERTICAL);

		separator.setText("Additional informations");
		separator.setLineStyle(LineStyle.ShadowIn);

		final InputStream stream = getClass().getResourceAsStream("/images/ico_views.png");
		separator.loadImage(stream);

		separator.addListener(SWT.Selection, e -> {
			final MessageBox msgBox = new MessageBox(shell);

			if (e.detail == Greip.IMAGE) {
				msgBox.setMessage("You have clicked the image.");
			} else {
				msgBox.setMessage("You have clicked outside the image.");
			}

			msgBox.open();
		});
	}
}
