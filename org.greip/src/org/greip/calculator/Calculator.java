/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.calculator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.greip.calculator.CalcualtionEngine.CalculationResult;
import org.greip.common.Util;
import org.greip.nls.Messages;

/**
 * Instances of this class represent a calculator. Typically a calculator is
 * used in combination with an text widget. In this case use
 * <code>Calculator</code> in combination with {@link CalculatorTextAdapter}.
 *
 * @see CalculatorTextAdapter
 *
 * @author Thomas Lorbeer
 */
public final class Calculator extends Composite {

	private enum KeyHandlers {
		CTRL_C(
			(e, c) -> e.stateMask == SWT.CTRL && e.keyCode == 'c',
			(e, c) -> {
				final Clipboard cb = new Clipboard(e.display);
				final TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { c.lblResult.getText() }, new Transfer[] { textTransfer });
			}),
		CTRL_MINUS(
			(e, c) -> e.keyCode == '-' && e.stateMask == SWT.CTRL,
			(e, c) -> c.processAction(CalcualtionEngine.SIGN)),
		CTRL_CR(
			(e, c) -> e.keyCode == SWT.CR && e.stateMask == SWT.CTRL,
			(e, c) -> {
				c.processAction(SWT.CR);
				c.propagateValue();
			}),
		DEFAULT_ACTION(
			(e, c) -> c.engine.isLegalCommand(e.character),
			(e, c) -> c.processAction(e.character));

		private final BiPredicate<Event, Calculator> predicate;
		private final BiConsumer<Event, Calculator> consumer;

		KeyHandlers(final BiPredicate<Event, Calculator> predicate, final BiConsumer<Event, Calculator> consumer) {
			this.predicate = predicate;
			this.consumer = consumer;
		}

		public static void execute(final Event e, final Calculator calculator) {
			for (final KeyHandlers handler : values()) {
				if (handler.predicate.test(e, calculator)) {
					handler.consumer.accept(e, calculator);
					break;
				}
			}
		}
	}

	private static final char SPACER = (char) -1;

	private Composite resultPanel;
	private Label lblResult;
	private Label lblFormula;
	private Text txtFocus;

	private final CalcualtionEngine engine = new CalcualtionEngine();
	private BigDecimal value = BigDecimal.ZERO;
	private Color resultBackground;
	private Color resultForeground;

	private Label lblMemory;

	/**
	 * Constructs a new instance of this class given its parent.
	 *
	 * @param parent
	 *        a composite control which will be the parent of the new instance
	 *        (cannot be null)
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the parent</li>
	 *            </ul>
	 */
	public Calculator(final Composite parent) {
		super(parent, SWT.NONE);

		setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).spacing(2, 2).numColumns(5).create());
		setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		setBackgroundMode(SWT.INHERIT_FORCE);
		addListener(SWT.Resize, e -> showFormula());

		createFocusControl();
		createResultPanel();
		createButtons();
	}

	private void createFocusControl() {
		txtFocus = new Text(this, SWT.NONE);
		txtFocus.setLayoutData(GridDataFactory.fillDefaults().exclude(true).create());
		txtFocus.addListener(SWT.KeyDown, e -> KeyHandlers.execute(e, this));
	}

	private void createResultPanel() {
		resultPanel = new Composite(this, SWT.BORDER);
		resultPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1));
		resultPanel.setLayout(GridLayoutFactory.swtDefaults().margins(3, 1).spacing(0, 0).numColumns(2).create());
		resultPanel.setBackground(getResultBackground());

		lblFormula = createInfoLabel(resultPanel, 2);
		Util.applyDerivedFont(lblFormula, -2, SWT.NONE);

		createMemoryIndicator();

		lblResult = createInfoLabel(resultPanel, 1);
		lblResult.setText("0"); //$NON-NLS-1$
		Util.applyDerivedFont(lblResult, 2, SWT.BOLD);
	}

	private void createMemoryIndicator() {
		lblMemory = new Label(resultPanel, SWT.RIGHT);
		lblMemory.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		lblMemory.setText("M");
		lblMemory.setVisible(false);
		Util.applyDerivedFont(lblMemory, -3, SWT.NONE);
	}

	private Label createInfoLabel(final Composite parent, final int hSpan) {
		final Label lbl = new Label(parent, SWT.RIGHT);

		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, hSpan, 1));
		lbl.setForeground(getResultForeground());

		return lbl;
	}

	private void createButtons() {
		createSpacer();

		createSmallButton("MC", CalcualtionEngine.MC, 0, SWT.COLOR_DARK_RED);
		createSmallButton("MR", CalcualtionEngine.MR, 0, SWT.COLOR_DARK_RED);
		createSmallButton("MS", CalcualtionEngine.MS, 0, SWT.COLOR_DARK_RED);
		createSmallButton("M+", CalcualtionEngine.M_PLUS, 3, SWT.COLOR_DARK_RED);
		createSmallButton("M-", CalcualtionEngine.M_MINUS, 0, SWT.COLOR_DARK_RED);

		createSmallButton("\u2190", SWT.BS, 0, SWT.COLOR_BLACK);
		createSmallButton("CE", 'E', 0, SWT.COLOR_BLACK);
		createSmallButton("C", 'C', 0, SWT.COLOR_BLACK);
		createSmallButton("(", '(', 3, SWT.COLOR_BLACK);
		createSmallButton(")", ')', 0, SWT.COLOR_BLACK);

		createSpacer();

		createButtonsFor('7', '8', '9', SPACER, CalcualtionEngine.DIVIDE, '%');
		createButtonsFor('4', '5', '6', SPACER, CalcualtionEngine.MULTIPLY, CalcualtionEngine.SIGN);
		createButtonsFor('1', '2', '3', SPACER, '-');
		createButton('=', 1, 2, 0);
		createButton('0', 2, 1, 0);
		createButtonsFor(',', SPACER, '+');
	}

	private void createSpacer() {
		final Label lbl = new Label(this, SWT.LEFT);
		lbl.setLayoutData(GridDataFactory.fillDefaults().span(5, 1).hint(3, 3).create());
	}

	private void createButtonsFor(final char... actions) {
		int indent = 0;

		for (final char action : actions) {
			if (action == SPACER) {
				indent = 3;
			} else {
				createButton(action, 1, 1, indent);
				indent = 0;
			}
		}
	}

	private void createButton(final char action, final int hSpan, final int vSpan, final int indent) {
		final Button btn = createButton(String.valueOf(action), action);
		btn.setLayoutData(
				GridDataFactory.fillDefaults().span(hSpan, vSpan).grab(true, true).minSize(30, SWT.DEFAULT).indent(indent, 0).create());
	}

	private Button createButton(final String text, final char action) {
		final Button btn = new Button(this, SWT.PUSH);

		btn.setText(text);
		btn.addListener(SWT.Selection, e -> processAction(action));
		btn.addListener(SWT.Traverse, e -> e.doit = e.detail != SWT.TRAVERSE_RETURN);

		return btn;
	}

	private Button createSmallButton(final String text, final char action, final int hIndent, final int color) {
		final Button btn = createButton("", action);

		btn.setForeground(getDisplay().getSystemColor(color));
		btn.setLayoutData(
				GridDataFactory.fillDefaults().grab(true, true).minSize(30, SWT.DEFAULT).hint(SWT.DEFAULT, 19).indent(hIndent, 0).create());

		Util.applyDerivedFont(btn, -3, SWT.NONE);

		btn.addListener(SWT.Paint, e -> {
			final Point size = btn.getSize();
			final Point textSize = e.gc.textExtent(text, SWT.TRANSPARENT);
			e.gc.drawText(text, (size.x - textSize.x) / 2, (size.y - textSize.y) / 2, true);
		});

		return btn;
	}

	private void processAction(final char action) {

		try {
			engine.process(action);
			final CalculationResult result = engine.getCalculationResult();

			lblResult.setText(result.getResult());
			lblResult.setForeground(getResultForeground());

		} catch (final OverflowException e) {
			lblResult.setText(Messages.Overflow);
			lblResult.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));

		} catch (final CalculationException e) {
			lblResult.setText(Messages.Error);
			lblResult.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		}

		showFormula();
		lblMemory.setVisible(engine.getMemory() != null);
		txtFocus.setFocus();
	}

	private void showFormula() {
		final String formula = engine.getCalculationResult().getFormula();

		Util.withResource(new GC(lblFormula), gc -> {
			final int width = lblFormula.getSize().x;
			lblFormula.setText(reverse(Util.shortenText(gc, reverse(formula), width, SWT.NONE)));
		});
	}

	private static String reverse(final String text) {
		return new StringBuilder(text).reverse().toString();
	}

	private void propagateValue() {
		value = calculateFormula();
		notifyListeners(SWT.Selection, new Event());
	}

	private BigDecimal calculateFormula() {
		return (BigDecimal) engine.getDecimalFormat().parse(lblResult.getText(), new ParsePosition(0));
	}

	/**
	 * Sets the initial value.
	 *
	 * @param value
	 *        The initial value.
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setValue(final BigDecimal value) {
		checkWidget();
		engine.resetTo(value);
	}

	/**
	 * Gets the current value.
	 *
	 * @return The current value.
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public BigDecimal getValue() {
		checkWidget();
		return value;
	}

	/**
	 * Sets the format for number formatting. The default format is
	 * "#0.##########".
	 *
	 * @param format
	 *        The new format.
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setDecimalFormat(final DecimalFormat format) {
		checkWidget();
		engine.setDecimalFormat(format);
		processAction('=');
	}

	/**
	 * Returns the results foreground color.
	 *
	 * @return the results foreground color
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public Color getResultForeground() {
		checkWidget();
		return Util.nvl(resultForeground, getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	}

	/**
	 * Sets the results foreground color to the color specified by the argument,
	 * or to <code>SWT.COLOR_INFO_FOREGROUND</code> if the argument is null.
	 *
	 * @param resultForeground
	 *        the new color (or null)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *            disposed</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setResultForeground(final Color resultForeground) {
		checkWidget();
		this.resultForeground = Util.checkResource(resultForeground, true);

		lblResult.setForeground(resultForeground);
		lblFormula.setForeground(resultForeground);
	}

	/**
	 * Returns the results background color.
	 *
	 * @return the results background color
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public Color getResultBackground() {
		checkWidget();
		return Util.nvl(resultBackground, getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	/**
	 * Sets the results area background color to the color specified by the
	 * argument, or to <code>SWT.COLOR_INFO_BACKGROUND</code> if the argument is
	 * null.
	 *
	 * @param resultBackground
	 *        the new color (or null)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *            disposed</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setResultBackground(final Color resultBackground) {
		checkWidget();
		this.resultBackground = Util.checkResource(resultBackground, true);

		lblResult.setBackground(resultBackground);
		lblFormula.setBackground(resultBackground);
		resultPanel.setBackground(resultBackground);
	}
}
