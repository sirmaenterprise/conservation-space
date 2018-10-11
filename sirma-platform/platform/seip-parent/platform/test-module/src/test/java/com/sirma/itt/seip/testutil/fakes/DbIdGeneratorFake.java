package com.sirma.itt.seip.testutil.fakes;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.db.DbIdGenerator;

/**
 * Dummy implementation of {@link DbIdGenerator} for tests
 *
 * @author BBonev
 */
public class DbIdGeneratorFake implements DbIdGenerator {

	private static final char ID_PART_SEPARATOR = '-';
	private static final char PREFIX_SEPARATOR = ':';
	protected static final String PREFIX = "emf" + PREFIX_SEPARATOR;

	@Override
	public Serializable generateId() {
		return addPrefixTo(getRandomId());
	}

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

	private static String addPrefixTo(String id) {
		return PREFIX + id;
	}

	@Override
	public Serializable generateIdForType(String type) {
		if (StringUtils.isBlank(type)) {
			return generateId();
		}
		return addPrefixTo(type + ID_PART_SEPARATOR + getRandomId());
	}

	@Override
	public Serializable generateRevisionId(Serializable src, String revision) {
		if (src == null || StringUtils.isBlank(revision)) {
			return generateId();
		}
		return getValidId(src).toString() + ID_PART_SEPARATOR + 'r' + revision;
	}


}
