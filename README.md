# greip
greip - additional SWT controls

## The widgets

Currently greip contains the following widgets:

* **ColorButton**, a button that represents a color and opens one of this color choosers:
 * **_ColorChooserHSB_**, selects a color by hue, brightness and saturation.
 * **_ColorChooserRGB_**, selects a color by red, green, blue.
 * **_ColorWheel_**, selects a color from a color wheel.
 * **_ColorPicker_**, selects a color from a predefined or user defined color list.
* **Calculator**, a nice calculator for text widgets or as whole widget.

You can also use all color choosers as a whole widget.

## Color button example
```java
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
```

# License
Eclipse Public License, Version 1.0 (EPL-1.0): https://www.eclipse.org/legal/epl-v10.html
      
