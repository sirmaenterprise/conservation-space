package com.sirma.itt.emf.forum;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Observer that handles instance deletions to remove all comments for an instance
 *
 * @author BBonev
 */
@ApplicationScoped
public class CommentObserverForInstanceDelete {

	/** The Constant DELETE. */
	private static final Operation DELETE = new Operation("delete");

	/** The comment service. */
	@Inject
	private javax.enterprise.inject.Instance<CommentService> commentService;

	/**
	 * On before instance deleted.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	public <I extends Instance> void onBeforeInstanceDeleted(
			@Observes BeforeInstanceDeleteEvent<I, ?> event) {
		if (commentService.isUnsatisfied()) {
			// nothing to do if not present
			return;
		}
		CommentService service = commentService.get();
		List<TopicInstance> topics = service.getTopics(event.getInstance().toReference(), null, -1,
				false, null, null);
		for (TopicInstance topicInstance : topics) {
			service.delete(topicInstance, DELETE, false);
		}
	}

}
