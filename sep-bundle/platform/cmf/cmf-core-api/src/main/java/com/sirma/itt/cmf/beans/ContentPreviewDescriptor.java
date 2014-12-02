package com.sirma.itt.cmf.beans;

/**
 * Extended content descriptor that holds the result mimetype as well
 */
public class ContentPreviewDescriptor extends RemoteFileDescriptor {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8760728734851510662L;

	/** The mimetype. */
	private String mimetype;

	/**
	 * Instantiates a new content preview descriptor.
	 *
	 * @param uri
	 *            the uri
	 * @param mimetype
	 *            the mimetype
	 */
	public ContentPreviewDescriptor(String uri, String mimetype) {
		super(uri);
		this.mimetype = mimetype;
	}

	/**
	 * Gets the mimetype.
	 *
	 * @return the mimetype
	 */
	public String getMimetype() {
		return mimetype;
	}

}
