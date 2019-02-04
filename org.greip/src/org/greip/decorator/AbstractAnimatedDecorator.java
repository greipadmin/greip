package org.greip.decorator;

import org.eclipse.swt.widgets.Control;

abstract class AbstractAnimatedDecorator extends AbstractDecorator {

	protected static class AnimationContext {

		protected static int DEFAULT_STEP_COUNT = 20;
		protected static int DEFAULT_DELAY = 10;

		private int step = -1;

		public int getDelay() {
			return DEFAULT_DELAY;
		}

		public int getStepCount() {
			return DEFAULT_STEP_COUNT;
		}

		public void init() {
			step = 0;
		}

		public int getStep() {
			return step == -1 ? getStepCount() : step;
		}

		private int next() {
			if (!isActive()) throw new IllegalStateException();
			return ++step;
		}

		public boolean isActive() {
			return getStep() < getStepCount();
		}
	}

	private boolean animationEnabled = true;

	protected AbstractAnimatedDecorator(final Control parent) {
		super(parent);
	}

	abstract protected AnimationContext getAnimationContext();

	protected void startAnimation() {
		final AnimationContext ctx = getAnimationContext();
		if (ctx != null && animationEnabled) ctx.init();
		redrawAsync();
	}

	private void redrawAsync() {
		final AnimationContext ctx = getAnimationContext();

		if (ctx != null && ctx.isActive()) {
			ctx.next();
			getDisplay().timerExec(ctx.getDelay(), this::redrawAsync);
		}

		redraw();
	}

	public void setAnimationEnabled(final boolean enabled) {
		this.animationEnabled = enabled;
	}

	public boolean isAnimationEnabled() {
		return animationEnabled;
	}
}
