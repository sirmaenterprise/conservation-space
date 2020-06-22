package com.sirma.sep.content.jms;

/**
 * Contains content destinations.
 * 
 * @author nvelkov
 */
public class ContentDestinations {

	/**
	 * A jms topic used to store all assigned and updated contents for further operations. Create a
	 * consumer for this topic instead of listening for the
	 * {@link com.sirma.sep.content.event.ContentAssignedEvent} and
	 * {@link com.sirma.sep.content.event.ContentUpdatedEvent} directly if you need any jms
	 * features. <b>Messages will reside in the topic for 2 weeks</b>
	 * <p>
	 * <b>*No actual content will be put in the topic, only the following content data:</b>
	 * </p>
	 * <ul>
	 * <li>instanceId</li>
	 * <li>contentId</li>
	 * <li>mimetype</li>
	 * <li>purpose</li>
	 * </ul>
	 */
	public static final String CONTENT_TOPIC = "java:/jms.topic.ContentTopic";

	/**
	 * A JMS queue used to delete files from the various content stores durably in transaction. The queue should be
	 * sufficient to perform the deletion even the originating tenant is deleted so that it could be used for post
	 * tenant deletion cleanup.
	 * <br>The content store that persists the deleted content should provide information needed for the content
	 * deletion even the tenant or configuration not to be present. On message arrival the store will be called with
	 * the send data to perform the actual deletion.
	 */
	public static final String DELETE_CONTENT_QUEUE = "java:/jms.queue.DeleteContent";

	/**
	 * A JMS queue used to move files from one content store to other durably in transaction.
	 * <br>The message body should be in JSON format and should contain the source content id ({@code contentId}) and
	 * the destination store name ({@code targetStore}).
	 */
	public static final String MOVE_CONTENT_QUEUE = "java:/jms.queue.MoveContent";

	private ContentDestinations() {
		// Disallow instantiation.
	}
}
