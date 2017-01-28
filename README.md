# greip
greip - additional SWT controls

## Color chooser example
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
