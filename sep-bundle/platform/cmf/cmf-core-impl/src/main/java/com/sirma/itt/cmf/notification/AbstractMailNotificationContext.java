package com.sirma.itt.cmf.notification;

import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.emf.mail.notification.MailNotificationContext;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * The AbstractMailNotificationContext is base implementation that provides the helper service as
 * backend.
 */
public abstract class AbstractMailNotificationContext implements MailNotificationContext {

	/** The helper service. */
	protected MailNotificationHelperService helperService;

	/**
	 * Instantiates a new abstract mail notification context.
	 *
	 * @param helperService the helper service
	 */
	public AbstractMailNotificationContext(MailNotificationHelperService helperService) {
		this.helperService = helperService;
	}

	@Override
	public Resource getSendFrom() {
		return null;
	}
}
