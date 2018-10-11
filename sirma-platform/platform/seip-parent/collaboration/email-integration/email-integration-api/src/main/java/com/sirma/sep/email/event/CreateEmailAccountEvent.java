package com.sirma.sep.email.event;

import java.util.Map;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired when email account should be created for persisted instance.
 *
 * @author S.Djulgerova
 */
@Documentation("Event fired when email account should be created for persisted instance.")
public class CreateEmailAccountEvent implements EmfEvent {

	private final String instanceId;
	private final String accountName;
	private final Map<String, String> attributes;

	/**
	 * Instantiates a new event.
	 *
	 * @param instanceId
	 *            the id of instance which needs email account
	 * @param accountName
	 *            the email account
	 * @param attributes
	 *            specific account creation attributes;
	 */
	public CreateEmailAccountEvent(String instanceId, String accountName, Map<String, String> attributes) {
		this.instanceId = instanceId;
		this.accountName = accountName;
		this.attributes = attributes;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getAccountName() {
		return accountName;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

}
