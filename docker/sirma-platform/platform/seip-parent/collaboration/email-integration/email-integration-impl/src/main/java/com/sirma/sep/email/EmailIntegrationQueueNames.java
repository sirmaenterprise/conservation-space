package com.sirma.sep.email;

import com.sirmaenterprise.sep.jms.annotations.DestinationDef;

/**
 * Constants for queue names used in email integration module. Queue definitions are read from DestinationDef
 * annotations here and registered in standalone.xml
 * 
 * @author svelikov
 */
public class EmailIntegrationQueueNames {

	@DestinationDef("java:/jms.queue.CreateEmailAccountQueue")
	public static final String CREATE_EMAIL_ACCOUNT_QUEUE = "java:/jms.queue.CreateEmailAccountQueue";

	@DestinationDef("java:/jms.queue.UpdateEmailAccountQueue")
	public static final String UPDATE_EMAIL_ACCOUNT_QUEUE = "java:/jms.queue.UpdateEmailAccountQueue";

	@DestinationDef("java:/jms.queue.DeleteEmailAccountQueue")
	public static final String DELETE_EMAIL_ACCOUNT_QUEUE = "java:/jms.queue.DeleteEmailAccountQueue";

	@DestinationDef("java:/jms.queue.GenerateEmailAddressQueue")
	public static final String GENERATE_EMAIL_ADDRESS_QUEUE = "java:/jms.queue.GenerateEmailAddressQueue";

	@DestinationDef("java:/jms.queue.ActivateMailboxSupportableQueue")
	public static final String ACTIVATE_MAILBOX_SUPPORTABLE_QUEUE = "java:/jms.queue.ActivateMailboxSupportableQueue";

	private EmailIntegrationQueueNames() {
		// utility class
	}
}
