package com.sirmaenterprise.sep.annotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.annotations.model.Annotation;

/**
 * Wraps a list of annotations and a mapping between target instance IDs and their compact headers to be parsed by the
 * UI.
 *
 * @author Vilizar Tsonev
 */
public class DiscussionsResponse {

	/** Immutable empty discussion response */
	public static final DiscussionsResponse EMPTY_RESPONSE = new DiscussionsResponse()
			.setAnnotations(Collections.emptyList())
				.setTargetInstanceHeaders(Collections.emptyMap());

	/**
	 * The list of all found annotations.
	 */
	private List<Annotation> annotations;

	/**
	 * A mapping between the IDs of all target instances and their compact headers that will be used in the UI.
	 */
	private Map<String, String> targetInstanceHeaders;

	/**
	 * Getter method for annotations.
	 *
	 * @return the annotations
	 */
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	/**
	 * Setter method for annotations.
	 *
	 * @param annotations
	 *            the annotations to set
	 * @return the same discussions response instance
	 */
	public DiscussionsResponse setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
		return this;
	}

	/**
	 * Getter method for targetInstanceHeaders.
	 *
	 * @return the targetInstanceHeaders
	 */
	public Map<String, String> getTargetInstanceHeaders() {
		return targetInstanceHeaders;
	}

	/**
	 * Setter method for targetInstanceHeaders.
	 *
	 * @param targetInstanceHeaders
	 *            the targetInstanceHeaders to set
	 * @return the same discussions response instance
	 */
	public DiscussionsResponse setTargetInstanceHeaders(Map<String, String> targetInstanceHeaders) {
		this.targetInstanceHeaders = targetInstanceHeaders;
		return this;
	}
}
