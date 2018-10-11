/**
 *
 */
package com.sirma.sep.content.event;

import java.io.Serializable;

import com.sirma.itt.seip.security.util.SecureEvent;
import com.sirma.itt.seip.security.util.SecureExecutor;
import com.sirma.sep.content.Content;

/**
 * Base event for content change notifications. Provides access to modified content and it's type/purpose. The event
 * implements {@link SecureEvent} so it's possible to implement asynchronous observers.
 *
 * @author BBonev
 */
public abstract class ContentEvent implements SecureEvent {

	/** The secure executor for asynchronous observers. */
	private SecureExecutor secureExecutor;

	/** The modified content. */
	private final Content content;

	/** The content owner. */
	private final Serializable owner;

	/**
	 * Instantiates a new content event.
	 *
	 * @param owner
	 *            The content owner
	 * @param modifiedContent
	 *            the modified content
	 */
	public ContentEvent(Serializable owner, Content modifiedContent) {
		this.owner = owner;
		content = modifiedContent;
	}

	/**
	 * Gets the secure executor.
	 *
	 * @return the secure executor
	 */
	@Override
	public SecureExecutor getSecureExecutor() {
		return secureExecutor;
	}

	/**
	 * Sets the secure executor.
	 *
	 * @param executor
	 *            the new secure executor
	 */
	@Override
	public void setSecureExecutor(SecureExecutor executor) {
		secureExecutor = executor;
	}

	/**
	 * Gets the new/modified content.
	 *
	 * @return the content
	 */
	public Content getContent() {
		return content;
	}

	/**
	 * Gets the content owner.
	 *
	 * @return the owner
	 */
	public Serializable getOwner() {
		return owner;
	}

}
