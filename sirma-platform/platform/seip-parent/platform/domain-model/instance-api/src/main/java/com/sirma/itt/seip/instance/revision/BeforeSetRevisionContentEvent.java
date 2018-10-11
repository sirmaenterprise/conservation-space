package com.sirma.itt.seip.instance.revision;

import java.io.File;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired before setting the content of a revision.
 *
 * @author yasko
 */
public class BeforeSetRevisionContentEvent extends AbstractInstanceEvent<Instance> {

	private Instance revision;
	private File content;

	/**
	 * Contructor.
	 *
	 * @param instance
	 *            Original instances.
	 * @param revision
	 *            The created revision of instance.
	 * @param content
	 *            The content of the the newly created revision.
	 */
	public BeforeSetRevisionContentEvent(Instance instance, Instance revision, File content) {
		super(instance);
		this.revision = revision;
		this.content = content;
	}

	/**
	 * @return the revision
	 */
	public Instance getRevision() {
		return revision;
	}

	/**
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(Instance revision) {
		this.revision = revision;
	}

	/**
	 * @return the content
	 */
	public File getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(File content) {
		this.content = content;
	}

}
