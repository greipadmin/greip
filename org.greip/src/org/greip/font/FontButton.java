/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.font;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.greip.color.IColorChooserFactory;

/**
 * Instances of this class allow the user to select a font from all available
 * fonts in the system.
 *
 * @author Thomas Lorbeer
 */
public class FontButton extends Button {

	private static class FontButtonListener implements Listener {

		private final Supplier<FontData> fontData;
		private final Supplier<RGB> fontColor;
		private final BiConsumer<FontData, RGB> fontConsumer;
		private IColorChooserFactory colorChooserFactory;

		public FontButtonListener(final BiConsumer<FontData, RGB> fontConsumer, final Supplier<FontData> fontData,
				final Supplier<RGB> fontColor) {
			this.fontConsumer = fontConsumer;
			this.fontData = fontData;
			this.fontColor = fontColor;
		}

		public void setColorChooserFactory(final IColorChooserFactory colorChooserFactory) {
			this.colorChooserFactory = colorChooserFactory;
		}

		@Override
		public void handleEvent(final Event e) {
			final FontChooserPopup popup = new FontChooserPopup((Control) e.widget, colorChooserFactory);

			popup.setFontData(fontData.get());
			popup.setFontColor(fontColor.get());
			popup.open();

			if (popup.getFontData() == null) {
				e.type = SWT.None;
			} else {
				fontConsumer.accept(popup.getFontData(), popup.getFontColor());
			}
		}
	}

	private FontData fontData;
	private RGB fontColor;
	private final FontButtonListener fontChooser;

	/**
	 * Constructs a new instance of this class.
	 *
	 * @param parent
	 *        A composite which will be the parent of the new instance.
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
	public FontButton(final Composite parent, final int style) {
		super(parent, style | SWT.PUSH);

		fontChooser = new FontButtonListener((fd, rgb) -> {
			setFontData(fd);
			setFontColor(rgb);
		}, this::getFontData, this::getFontColor);

		addListener(SWT.Selection, fontChooser);
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	/**
	 * Returns the selected font data object or <code>null</code> if no font
	 * selected.
	 *
	 * @return the font data of the selected font
	 */
	public FontData getFontData() {
		return fontData;
	}

	/**
	 * Sets the initial font data. If no font data is set the system font is the
	 * default.
	 *
	 * @param fontData
	 *        the initial font data
	 */
	public void setFontData(final FontData fontData) {
		this.fontData = fontData;
	}

	/**
	 * Gets the selected font color or <code>null</code> if no font is selected
	 * or no color chooser factory is set.
	 *
	 * @return the new font color
	 *
	 * @see #setColorChooserFactory(IColorChooserFactory)
	 */
	public RGB getFontColor() {
		return fontColor;
	}

	/**
	 * Sets the initial font color. The font color is only used when a color
	 * chooser factory is defined.
	 *
	 * @param fontColor
	 *        the font color
	 *
	 * @see #setColorChooserFactory(IColorChooserFactory)
	 */
	public void setFontColor(final RGB fontColor) {
		this.fontColor = fontColor;
	}

	/**
	 * Sets the factory for creating the color chooser.
	 *
	 * @param factory
	 *        The factory instance or <code>null</code> if color selection not
	 *        needed.
	 *
	 * @see IColorChooserFactory
	 */
	public void setColorChooserFactory(final IColorChooserFactory factory) {
		fontChooser.setColorChooserFactory(factory);
	}
}