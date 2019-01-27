package org.greip.samples.separator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.greip.samples.AbstractSample;
import org.greip.separator.LineStyle;
import org.greip.separator.Separator;

/**
 * This example shows the use of Separator.
 *
 * @author Thomas Lorbeer
 */
public class Sample1 extends AbstractSample {

	public static void main(final String[] args) {
		final Sample1 sample = new Sample1();
		sample.show("Greip Separator Sample");
	}

	@Override
	protected void layout() {
		final Separator separator = new Separator(shell, SWT.HORIZONTAL);

		separator.setText("Additional informations");
		separator.setMarginHeight(5);
		separator.setMarginWidth(5);
		separator.setLineStyle(LineStyle.Dot);

		final InputStream stream = getClass().getResourceAsStream("/images/ico_info.png");
		separator.loadImage(stream);
	}
}
