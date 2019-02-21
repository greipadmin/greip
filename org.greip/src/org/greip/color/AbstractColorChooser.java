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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Util;

public abstract class AbstractColorChooser extends Composite {

	private final ColorResolution colorResolution;
	private RGB newRGB;
	private RGB rgb;

	private Composite previewPanel;
	private final boolean showInfo;
	private ColorInfo colorInfo;

	protected AbstractColorChooser(final Composite parent, final ColorResolution colorResolution, final boolean showInfo,
			final boolean showHistory) {
		super(parent, SWT.NO_FOCUS);

		this.colorResolution = colorResolution;
		this.showInfo = showInfo;
		this.newRGB = new RGB(0, 0, 0);

		setLayout(GridLayoutFactory.fillDefaults().numColumns(3).spacing(10, 0).create());
		setBackgroundMode(SWT.INHERIT_FORCE);

		addListener(SWT.Selection, e -> ColorHistoryList.INSTANCE.add(getRGB()));
		addListener(SWT.Traverse, e -> Util.when(e.detail == SWT.TRAVERSE_RETURN, () -> ColorHistoryList.INSTANCE.add(getRGB())));

		if (showHistory) createHistoryPanel();

		final int hSpan = 3 - (showHistory ? 1 : 0);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).span(hSpan, 1).applyTo(createColorChooserPanel());

		if (showInfo) {
			createColorInfoPanel();
			createPreviewPanel();
		}
	}

	private void createColorInfoPanel() {
		colorInfo = new ColorInfo(this);
		colorInfo.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).span(2, 1).indent(0, 8).grab(true, true).applyTo(colorInfo);
		Util.applyDerivedFont(colorInfo, -2, SWT.NONE);
	}

	protected abstract Composite createColorChooserPanel();

	public Point getMargins() {
		return new Point(10, 10);
	}

	private void createPreviewPanel() {
		previewPanel = new Composite(this, SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED);
		previewPanel.setLayoutData(GridDataFactory.fillDefaults().hint(35, 8).indent(0, 10).align(SWT.RIGHT, SWT.FILL).create());

		previewPanel.addListener(SWT.Paint, e -> {
			final Point size = getPreviewSize();
			final Color oldColor = new Color(e.display, rgb);
			final Color newColor = new Color(e.display, getRGB());

			e.gc.setAntialias(SWT.ON);
			e.gc.setBackground(oldColor);
			e.gc.fillRoundRectangle(0, 0, size.x, size.y - 1, 5, 5);
			e.gc.setBackground(newColor);
			e.gc.setClipping(0, 0, size.x / 2, size.y);
			e.gc.fillRoundRectangle(0, 0, size.x, size.y - 1, 5, 5);
			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
			e.gc.setClipping((Rectangle) null);
			e.gc.drawRoundRectangle(0, 0, size.x - 1, size.y - 1, 5, 5);

			oldColor.dispose();
			newColor.dispose();
		});

		previewPanel.addListener(SWT.MouseDown, e -> {
			final Point size = getPreviewSize();
			Util.when(e.x > size.x / 2, () -> setRGB(rgb));
		});

		previewPanel.addListener(SWT.MouseDoubleClick, e -> {
			final Point size = getPreviewSize();
			Util.when(e.x < size.x / 2, () -> notifyListeners(SWT.Selection, new Event()));
		});
	}

	private Point getPreviewSize() {
		final Point size = previewPanel.getSize();
		return new Point(size.x - 3, size.y);
	}

	private void createHistoryPanel() {
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(new ColorHistory(this));
	}

	protected final ColorResolution getColorResolution() {
		return colorResolution;
	}

	protected final void setNewRGB(final RGB rgb) {
		newRGB = rgb;

		if (showInfo) {
			colorInfo.setRGB(rgb);
			previewPanel.redraw();
			layout(true, true);
		}
	}

	/**
	 * Set the initial color. When the exact color is not available in the color
	 * chooser, the nearest color is selected.
	 *
	 * @param rgb
	 *        The colors RGB value.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the RGB value is null</li>
	 *            </ul>
	 */
	public void setRGB(final RGB rgb) {
		if (rgb == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.rgb = rgb;
		setNewRGB(rgb);
		redraw();
	}

	/**
	 * Gets the current selected color.
	 *
	 * @return The colors RGB value.
	 */
	public final RGB getRGB() {
		return newRGB;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * a color is selected by the user, by sending it one of the messages defined
	 * in the <code>SelectionListener</code> interface.
	 *
	 * @param listener
	 *        the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		addListener(SWT.Selection, new TypedListener(listener));
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when a color is selected by the user.
	 *
	 * @param listener
	 *        the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Selection, listener);
	}
}
