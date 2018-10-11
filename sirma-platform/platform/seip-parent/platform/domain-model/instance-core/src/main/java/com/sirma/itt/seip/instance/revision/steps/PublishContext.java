package com.sirma.itt.seip.instance.revision.steps;

import java.util.Objects;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.sep.content.idoc.Idoc;

/**
 * Represents publish context that include the publish request object and the currently published instance
 *
 * @author BBonev
 */
public class PublishContext {

	private final PublishInstanceRequest request;
	private final Instance revision;

	private Idoc view;

	/**
	 * Instantiate new context based on the publish request
	 *
	 * @param request
	 *            the publish request
	 * @param revision
	 *            the published revision
	 */
	public PublishContext(PublishInstanceRequest request, Instance revision) {
		this.request = Objects.requireNonNull(request, "Cannot accept null publish request");
		this.revision = Objects.requireNonNull(revision, "Cannot accept null revision");
	}

	/**
	 * @return the view
	 */
	public Idoc getView() {
		return view;
	}

	/**
	 * @param view
	 *            the view to set
	 * @return the current instance to allow method chaining
	 */
	public PublishContext setView(Idoc view) {
		this.view = view;
		return this;
	}

	/**
	 * @return the request
	 */
	public PublishInstanceRequest getRequest() {
		return request;
	}

	/**
	 * @return the revision
	 */
	public Instance getRevision() {
		return revision;
	}

}
