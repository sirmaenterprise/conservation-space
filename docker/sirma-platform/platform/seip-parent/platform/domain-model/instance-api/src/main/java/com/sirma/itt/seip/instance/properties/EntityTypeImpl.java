package com.sirma.itt.seip.instance.properties;

/**
 * Default implementation for {@link EntityType}.
 *
 * @author BBonev
 */
public final class EntityTypeImpl implements EntityType {

	/** The id. */
	private final int id;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new entity type.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 */
	public EntityTypeImpl(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTypeId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

}
