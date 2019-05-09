package com.sirma.sep.content.batch;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Selector data object that can be used for searching in content entries. If content matches all of the given criteria
 * it will be picked for processing.<br>
 * Note that some of the selection options are exclusive with the other. For example instance id pattern should not be
 * used when set of preselected instance ids is passed. <br>
 * If non of the filter methods is used then the selector will match all content in the selected content repository.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
public class ContentInfoMatcher {
	private String instanceIdPattern;
	private Set<String> contentIds;
	private Set<String> instanceIds;
	private String purpose;
	private String storeName;

	public Set<String> getContentIds() {
		if (contentIds == null) {
			contentIds = new LinkedHashSet<>();
		}
		return contentIds;
	}

	/**
	 * Set of known content identifiers to load
	 *
	 * @param contents the content identifiers to load
	 * @return the current instance for method chaining
	 */
	public ContentInfoMatcher matchContents(Collection<String> contents) {
		getContentIds().addAll(contents);
		return this;
	}

	public Set<String> getInstanceIds() {
		if (instanceIds == null) {
			instanceIds = new LinkedHashSet<>();
		}
		return instanceIds;
	}

	/**
	 * Set of known instance identifiers to load. The loaded content will be filtered by other criteria as well.
	 *
	 * @param instances the owning instance identifiers.
	 * @return the current instance for method chaining
	 */
	public ContentInfoMatcher matchInstances(Collection<String> instances) {
		getInstanceIds().addAll(instances);
		return this;
	}

	public String getPurpose() {
		return purpose;
	}

	/**
	 * The desired content purpose. The value could be SQL pattern as well.
	 *
	 * @param purpose the needed content purpose
	 * @return the current instance for method chaining
	 */
	public ContentInfoMatcher setPurpose(String purpose) {
		this.purpose = purpose;
		return this;
	}

	public String getStoreName() {
		return storeName;
	}

	/**
	 * The desired content purpose. The value could be SQL pattern as well. The matched content will be only from the given store.
	 *
	 * @param storeName the needed content store identifier.
	 * @return the current instance for method chaining
	 */
	public ContentInfoMatcher setStoreName(String storeName) {
		this.storeName = storeName;
		return this;
	}

	public String getInstanceIdPattern() {
		return instanceIdPattern;
	}

	/**
	 * Set an instance id pattern to be used when selecting instance content. This could be used to select all content
	 * for an instance including it's version and revision contents. The passed value should be in SQL pattern format.
	 * This is not compatible with the method {@link #matchInstances(Collection)}.
	 *
	 * @param instanceIdPattern the instance pattern to apply.
	 * @return the current instance for method chaining
	 */
	public ContentInfoMatcher setInstanceIdPattern(String instanceIdPattern) {
		this.instanceIdPattern = instanceIdPattern;
		return this;
	}
}
