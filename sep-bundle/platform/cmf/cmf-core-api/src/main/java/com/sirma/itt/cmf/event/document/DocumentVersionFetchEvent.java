package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that a old document version has been retrieved and is going to be opened.<br>
 * A separate event will be fired before document open.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that a old document version has been retrieved and is going to be opened.<br>A separate event will be fired before document open.")
public class DocumentVersionFetchEvent extends AbstractInstanceEvent<DocumentInstance> {

	/** The version. */
	private final String version;

	/**
	 * Instantiates a new document version fetch event.
	 * 
	 * @param instance
	 *            the instance
	 * @param version
	 *            the version that has been opened
	 */
	public DocumentVersionFetchEvent(DocumentInstance instance, String version) {
		super(instance);
		this.version = version;
	}

	/**
	 * Getter method for version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

}
