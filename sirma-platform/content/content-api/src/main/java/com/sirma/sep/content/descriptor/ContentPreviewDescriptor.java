package com.sirma.sep.content.descriptor;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (mimetype == null ? 0 : mimetype.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ContentPreviewDescriptor)) {
			return false;
		}
		ContentPreviewDescriptor other = (ContentPreviewDescriptor) obj;
		return nullSafeEquals(mimetype, other.getMimetype());
	}

}
