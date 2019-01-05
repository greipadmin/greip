/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.greip.common.DropDownButton;
import org.greip.common.Util;

/**
 * The <code>ColorButton</code> is a button that displays a color and/or text
 * and allows the user to change the color selection.
 *
 * @see Button
 */
public class ColorButton extends DropDownButton {

	private RGB rgb;
	private IColorChooserFactory factory;
	private Consumer<RGB> consumer;

	/**
	 * Constructs a new instance of this class given its parent.
	 *
	 * @param parent
	 *        a composite control which will be the parent of the new instance
	 *        (cannot be null)
	 * @param style
	 *        The style bits of the button. You can use all styles decribed by
	 *        {@link Button} except SWT.PUSH, SWT.CHECK and SWT.RADIO.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the parent</li>
	 *            </ul>
	 */
	public ColorButton(final Composite parent, final int style) {
		super(parent, SWT.DROP_DOWN);

		addListener(SWT.Resize, e -> changeImage());
		addListener(SWT.Dispose, e -> disposeImage());

		addListener(SWT.Selection, e -> {
			if (factory != null && consumer != null) {
				Util.when(chooseRGB(), () -> consumer.accept(rgb));
				e.type = SWT.None;
			}
		});

		setRGB(new RGB(255, 255, 255));
	}

	/**
	 * Opens the color chooser and allows the user to select a color.
	 *
	 * @return <code>true</code> if a color was selected or <code>false</code>
	 *         otherwise.
	 */
	public boolean chooseRGB() {
		final ColorChooserPopup popup = new ColorChooserPopup(this);

		popup.createContent(Util.nvl(factory, new ColorWheelChooser.Factory(ColorResolution.Maximal, true, true)));
		popup.setRGB(rgb);

		return popup.open(() -> setRGB(popup.getRGB()));
	}

	/**
	 * Sets the RGB value for the color and update the control.
	 *
	 * @param rgb
	 *        RGB value
	 */
	public void setRGB(final RGB rgb) {
		this.rgb = rgb;
		changeImage();
	}

	/**
	 * Gets the selected RGB.
	 *
	 * @return RGB
	 */
	public RGB getRGB() {
		return rgb;
	}

	@Override
	public void setText(final String text) {
		setRedraw(false);
		super.setText(text);
		changeImage();
		setRedraw(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.greip.common.DropDownButton#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final Point size = super.computeSize(wHint, hHint, changed);
		return new Point(Math.max(30, size.x + 20), size.y);
	}

	private void changeImage() {
		final Point size = getSize();
		final int height = size.y - 16;
		final int width = getText().isEmpty() ? size.x - (isDropDownArrowVisible() ? 25 : 15) : height;

		disposeImage();
		setImage(height > 0 && width > 0 ? createImage(width, height) : null);
	}

	private Image createImage(final int width, final int height) {
		final PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
		final ImageData source = new ImageData(width, height, 24, palette);

		final Image image = new Image(getDisplay(), source);

		Util.withResource(new GC(image), gc -> {
			final Device device = gc.getDevice();

			if (rgb != null && isEnabled()) {
				Util.withResource(new Color(device, rgb), color -> {
					gc.setBackground(color);
					gc.fillRectangle(0, 0, width, height);
				});
			}

			gc.setForeground(device.getSystemColor(isEnabled() ? SWT.COLOR_DARK_GRAY : SWT.COLOR_GRAY));
			gc.drawRectangle(0, 0, width - 1, height - 1);
		});

		return image;
	}

	private void disposeImage() {
		Util.whenNotNull(getImage(), Image::dispose);
	}

	/**
	 * Sets the factory for creating the color chooser.
	 *
	 * @param factory
	 *        The factory instance.
	 *
	 * @see IColorChooserFactory
	 */
	public void setColorChooserFactory(final IColorChooserFactory factory) {
		this.factory = factory;
	}

	/**
	 * Sets the consumer for the selected color.
	 *
	 * @param consumer
	 *        The color consumer.
	 */
	public void setColorConsumer(final Consumer<RGB> consumer) {
		this.consumer = consumer;
	}
}
