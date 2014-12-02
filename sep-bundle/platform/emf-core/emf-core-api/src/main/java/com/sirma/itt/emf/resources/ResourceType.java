package com.sirma.itt.emf.resources;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * The ResourceTypes currently supported.
 */
public enum ResourceType {
	/** The unknown. */
	UNKNOWN(null, 0),
	/** The user. */
	USER("user", 1),
	/** The group. */
	GROUP("group", 2),
	/** Groups and users. */
	ALL("all", 3);

	/** The name. */
	private String name;
	private int type;

	/**
	 * Instantiates a new resource type.
	 * 
	 * @param name
	 *            the name
	 * @param type
	 *            - internal type
	 */
	private ResourceType(String name, int type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the internal type.
	 * 
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Get by internal id the type
	 * 
	 * @param type
	 *            is the internal id
	 * @return the type or null if not found
	 */
	public static ResourceType getById(int type) {
		for (ResourceType resource : values()) {
			if (type == resource.getType()) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * Gets the by type.
	 * 
	 * @param value
	 *            the value
	 * @return the by type
	 */
	public static ResourceType getByType(String value) {
		if (StringUtils.isNullOrEmpty(value)) {
			return UNKNOWN;
		}
		ResourceType[] types = values();
		for (ResourceType type : types) {
			if (type.name().equalsIgnoreCase(value)) {
				return type;
			}
		}

		return null;
	}

}