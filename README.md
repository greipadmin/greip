# Greip
Main repository for the Greip Project - additional SWT widgets. The goal of this project is to propose new widgets for the SWT API.

## The widgets

Currently greip contains the following widgets:

* **ColorButton**, a button that represents a color and opens one of this color choosers:
 * **_ColorChooserHSB_**, selects a color by hue, brightness and saturation.
 * **_ColorChooserRGB_**, selects a color by red, green, blue.
 * **_ColorWheel_**, selects a color from a color wheel.
 * **_ColorPicker_**, selects a color from a predefined or user defined color list.
* **Calculator**, a nice calculator for text widgets or as whole widget.

You can also use all color choosers as a whole widget.

### Color button example
```java
	...
	final ColorButton colorButton = new ColorButton(shell);
	colorButton.setText("Click me!");

	// initialize with current background rgb
	colorButton.setRGB(shell.getBackground().getRGB());

	colorButton.addListener(SWT.Selection, e -> {
		final RGB rgb = colorButton.chooseRGB(p -> new ColorChooserHSB(p, ColorResolution.Medium, true));
		// check if new color selected
		if (rgb != null) {
			// set new color to shell background
			final Color color = new Color(display, rgb);
			shell.setBackground(color);
			color.dispose();
		}
	});
	...
```
### Calculator example
```java
package org.greip.demos;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.greip.calculator.CalculatorTextAdapter;

public class CalculatorPopupDemo {

	public static void main(final String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);

		shell.setText("Greip - Calculator Popup Demo");
		shell.setLayout(new GridLayout(2, false));

		new Label(shell, SWT.NONE).setText("Enter a number:");

		final Text txt = new Text(shell, SWT.RIGHT | SWT.BORDER);
		txt.setLayoutData(new GridData(100, SWT.DEFAULT));

		final Label lbl = new Label(shell, SWT.NONE);
		lbl.setText("Press Ctrl+Enter to open calculator popup.");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		final CalculatorTextAdapter adapter = new CalculatorTextAdapter(txt);

		// optional: define your own decimal format (default is
		// "#,##0.##########")
		final DecimalFormat format = new DecimalFormat("#,##0.00 €");
		format.setMaximumIntegerDigits(6);

		adapter.setDecimalFormat(format);

		// optional: press SHIFT+CR to open calculator (default is '=')
		adapter.openCalculatorWhen(event -> {
			return event.keyCode == SWT.CR && event.stateMask == SWT.SHIFT;
		});

		// optional: initialize calculator properties (default is use default
		// properties)
		adapter.setCalculatorConfigurer(calculator -> {
			calculator.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		});

		// optional: define your own result consumer (default is set formatted
		// value to text field)
		adapter.setResultConsumer(value -> {
			final char groupingSeparator = format.getDecimalFormatSymbols().getGroupingSeparator();

			txt.setText(format.format(value).replace(String.valueOf(groupingSeparator), ""));
			txt.setSelection(0, txt.getText().length() - 2);
		});

		// optional: define your own value initializer (default is text field
		// content)
		adapter.setValueInitializer(() -> BigDecimal.TEN);

		// initialize text widget with zero
		txt.setText(format.format(10));
		txt.setSelection(0, 5);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
```
# Eclipse update site
https://github.com/greipadmin/greip/raw/master/updatesite

# License
Eclipse Public License, Version 1.0 (EPL-1.0). See https://www.eclipse.org/legal/epl-v10.html.
