package com.sirma.itt.seip.instance.revision;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Request object to call {@link RevisionService} to perform publish operation on instance. <br>
 * The request consist of:
 * <ul>
 * <li>required
 * <ul>
 * <li>instance to publish
 * <li>operation</li>
 * </ul>
 * <li>optional
 * <ul>
 * <li>relation id - If not present the current instance children will be considered
 * <li>instance types - If empty then all related instance that are from the same type as the current instance will be
 * selected for publish
 * </ul>
 * </ul>
 * The optional arguments are used to query related instances of the given types that should be published with the
 * current instance. The related instance should be linked with the provided relation and of the given type. <br>
 * The above defaults are valid only if one of the both optional values is non empty. If both are not specified no
 * related instance will be published
 *
 * @author BBonev
 */
public class PublishInstanceRequest {

	private final Instance instanceToPublish;
	private final Operation triggerOperation;
	private final Set<String> relatedInstanceTypes;
	private final Optional<String> relationType;
	private boolean asPdf;
	private String contentIdToPublish;

	/**
	 * Instantiates a new publish request.
	 *
	 * @param instanceToPublish
	 * 		the instance to publish, required argument
	 * @param triggerOperation
	 * 		the trigger operation, required argument
	 * @param relationType
	 * 		the relation type
	 * @param relatedInstanceTypes
	 * 		the related instance types
	 */
	public PublishInstanceRequest(Instance instanceToPublish, Operation triggerOperation, String relationType,
			Set<String> relatedInstanceTypes) {
		this.instanceToPublish = Objects.requireNonNull(instanceToPublish, "Instance is required argument");
		this.triggerOperation = Objects.requireNonNull(triggerOperation, "Operation is required argument");
		this.relationType = Optional.ofNullable(relationType);
		this.relatedInstanceTypes = getOrDefault(relatedInstanceTypes, Collections.<String>emptySet());
	}

	/**
	 * Construct new publish request by changing the publish instance and keeping all other configurations
	 *
	 * @param newInstanceToPublish
	 * 		the new instance to publish
	 * @return new publish request instance that have the given instance as {@link #getInstanceToPublish()}
	 */
	public PublishInstanceRequest copyForNewInstance(Instance newInstanceToPublish) {
		return new PublishInstanceRequest(newInstanceToPublish, getTriggerOperation(), getRelationType().orElse(null),
										  getRelatedInstanceTypes());
	}

	/**
	 * Check if the instance should be published as PDF.
	 *
	 * @return the asPdf
	 */
	public boolean isAsPdf() {
		return asPdf;
	}

	/**
	 * Mark the request for pdf publish
	 *
	 * @return the current instance to allow method chaining
	 */
	public PublishInstanceRequest asPdf() {
		asPdf = true;
		return this;
	}

	/**
	 * Set content Id to publish
	 *
	 * @param contentId
	 * 		the content Id witch will replace the published instance's content
	 * @return the current instance to allow method chaining
	 */
	public PublishInstanceRequest withContentIdToPublish(String contentId) {
		contentIdToPublish = contentId;
		return this;
	}

	/**
	 * @return non <code>null</code> instance
	 */
	public Instance getInstanceToPublish() {
		return instanceToPublish;
	}

	/**
	 * @return the triggerOperation
	 */
	public Operation getTriggerOperation() {
		return triggerOperation;
	}

	/**
	 * @return the relatedInstanceTypes
	 */
	public Set<String> getRelatedInstanceTypes() {
		return relatedInstanceTypes;
	}

	/**
	 * @return the relationType
	 */
	public Optional<String> getRelationType() {
		return relationType;
	}

	/**
	 * @return the contentIdToPublish
	 */
	public String getContentIdToPublish() {
		return contentIdToPublish;
	}

}
