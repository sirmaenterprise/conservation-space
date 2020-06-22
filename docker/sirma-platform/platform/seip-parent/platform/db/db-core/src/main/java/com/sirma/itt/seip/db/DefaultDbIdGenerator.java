package com.sirma.itt.seip.db;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Default {@link DbIdGenerator} implementation that uses UUID to generate the database ids.
 *
 * @author BBonev
 */
public class DefaultDbIdGenerator implements DbIdGenerator {

	private static final char ID_PART_SEPARATOR = '-';
	private static final char PREFIX_SEPARATOR = ':';
	/** The Default namespace prefix */
	protected static final String PREFIX = "emf" + PREFIX_SEPARATOR;

	@Override
	public Serializable generateId() {
		return addPrefixTo(getRandomId());
	}

	/**
	 * Gets the random id.
	 *
	 * @return the random id
	 */
	private static String getRandomId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public Serializable getValidId(Serializable id) {
		if (id == null) {
			return generateId();
		}
		String s = id.toString();
		if (s.isEmpty()) {
			return generateId();
		}
		if (s.indexOf(PREFIX_SEPARATOR) > 0) {
			return s;
		}
		return addPrefixTo(s);
	}

	/**
	 * Adds the prefix to the argument
	 *
	 * @param id
	 *            the id
	 * @return the id with the proper prefix
	 */
	protected String addPrefixTo(String id) {
		String prefix = getPrefix();
		return new StringBuilder(id.length() + prefix.length()).append(prefix).append(id).toString();
	}

	/**
	 * Gets the prefix that is applicable for the current user or return the default one.
	 *
	 * @return the prefix including the prefix separator to be used in the new identifier.
	 */
	protected String getPrefix() {
		return PREFIX;
	}

	@Override
	public Serializable generateIdForType(String type) {
		if (StringUtils.isBlank(type)) {
			return generateId();
		}
		String randomId = getRandomId();
		StringBuilder builder = new StringBuilder(type.length() + 1 + randomId.length())
				.append(type)
					.append(ID_PART_SEPARATOR)
					.append(randomId);
		return addPrefixTo(builder.toString());
	}

	@Override
	public Serializable generateRevisionId(Serializable src, String revision) {
		if (src == null || StringUtils.isBlank(revision)) {
			return generateId();
		}
		Serializable validId = getValidId(src);
		return new StringBuilder(validId.toString().length() + 2 + revision.length())
				.append(validId)
					.append(ID_PART_SEPARATOR)
					.append('r')
					.append(revision)
					.toString();
	}

}
