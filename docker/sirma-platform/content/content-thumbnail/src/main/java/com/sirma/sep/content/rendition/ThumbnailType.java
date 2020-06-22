package com.sirma.sep.content.rendition;

/**
 * Defines the supported thumbnail types. Each thumbnail type has a priority attribute that determines the importance of
 * the thumbnail. When loading thumbnails, if an instance has more then one thumbnail, the one with higher priority will
 * be returned.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/12/2018
 */
public enum ThumbnailType {

	/**
	 * Legacy value
	 */
	DEFAULT(0),
	/**
	 * Specifies that the thumbnail of an instance is the instance itself. Mainly used to load and identify the
	 * thumbnail created from the content of the same instance that has this type. This thumbnail is updated when the
	 * content of the instance changes.
	 */
	SELF(1),
	/**
	 * Specifies that the thimbnail of the instance is related to other instance and it's not owned by the current
	 * instance. The thumbnail will be updated indirectly when the self thumbnail of the related instance is updated.
	 */
	ASSIGNED(2);

	private final short priority;

	ThumbnailType(int priority) {
		this.priority = (short) priority;
	}

	/**
	 * Returns the priority of the a thumbnail type.
	 *
	 * @return the priority of the type.
	 */
	public short getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
