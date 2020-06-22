package com.sirma.itt.seip.resources;

import org.apache.commons.lang3.StringUtils;

/**
 * The ResourceTypes currently supported.
 */
public enum ResourceType {
	/** The unknown. */
	UNKNOWN(null, 0), /** The user. */
	USER("user", 1), /** The group. */
	GROUP("group", 2), /** system. */
	SYSTEM("system", 3), /** Groups and users. */
	ALL("all", 7);
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
	 * Checks if is user or group.
	 *
	 * @param type
	 *            the type
	 * @return true, if is user or group
	 */
	public static boolean isUserOrGroup(ResourceType type) {
		return type == ResourceType.USER || type == GROUP;
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
		if (StringUtils.isBlank(value)) {
			return UNKNOWN;
		}
		ResourceType[] types = values();
		for (ResourceType type : types) {
			if (type.name().equalsIgnoreCase(value)) {
				return type;
			}
		}

		return UNKNOWN;
	}

}