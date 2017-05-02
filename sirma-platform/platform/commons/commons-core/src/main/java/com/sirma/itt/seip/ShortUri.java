package com.sirma.itt.seip;

import java.util.regex.Pattern;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Proxy class for the {@link Uri} that represents the short format of the URIs.
 *
 * @author BBonev
 */
public class ShortUri implements Uri {
	private static final long serialVersionUID = 379450026355340070L;
	private static final Pattern SPLIT = Pattern.compile(":");
	private final String full;
	private String[] parts;

	/**
	 * Instantiates a new instance with short uri.
	 *
	 * @param shortUri
	 *            the short uri
	 */
	public ShortUri(String shortUri) {
		if (shortUri == null) {
			throw new EmfRuntimeException("Cannot create proxy from null short uri!");
		}
		full = shortUri;
	}

	@Override
	public String getNamespace() {
		parse();
		return parts[0];
	}

	/**
	 * Parses the given short uri into parts
	 */
	private void parse() {
		if (parts == null) {
			parts = SPLIT.split(full, 2);
			if (parts.length < 2) {
				throw new EmfRuntimeException("Invalid uri: " + full);
			}
		}
	}

	@Override
	public String getLocalName() {
		parse();
		return parts[1];
	}

	@Override
	public String toString() {
		return full;
	}

}
