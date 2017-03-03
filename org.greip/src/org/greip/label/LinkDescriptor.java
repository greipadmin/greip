/**
 * Copyright (c) 2017 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.label;

/**
 * A LinkDescriptor describes an link tag in pseudo html code used by
 * {@link FormattedText} class.
 *
 * @author Thomas Lorbeer
 */
public final class LinkDescriptor {
	private final String id;
	private String url;

	LinkDescriptor(final String id) {
		this.id = id;
	}

	/**
	 * Returns the defined link id.
	 *
	 * @return The id or null, when no id is set.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the defined url.
	 *
	 * @return The url or null, when no url is set.
	 */
	public String getUrl() {
		return url;
	}

	void setUrl(final String url) {
		this.url = url;
	}
}