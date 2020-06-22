package com.sirma.itt.seip.annotations.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.vocabulary.RDF;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * Represents a single annotation or reply to an annotation.
 *
 * @author BBonev
 */
public class Annotation extends EmfInstance {

	private static final String EMF_CREATED_BY = EMF.PREFIX + ":" + EMF.CREATED_BY.getLocalName();
	private static final String EMF_MODIFIED_BY = EMF.PREFIX + ":" + EMF.MODIFIED_BY.getLocalName();
	private static final String EMF_MENTIONED_USERS = EMF.PREFIX + ":" + EMF.MENTIONED_USERS.getLocalName();
	private static final String OA_MOTIVATED_BY = OA.PREFIX + ":" + OA.MOTIVATED_BY.getLocalName();
	private static final String OA_HAS_TARGET = OA.PREFIX + ":" + OA.HAS_TARGET.getLocalName();
	private static final String OA_HAS_BODY = OA.PREFIX + ":" + OA.HAS_BODY.getLocalName();
	private static final String EMF_CONTENT = EMF.PREFIX + ":" + EMF.CONTENT.getLocalName();
	private static final String RDF_TYPE = RDF.PREFIX + ":" + RDF.TYPE.getLocalName();
	private static final String STATUS = EMF.PREFIX + ":" + EMF.STATUS.getLocalName();

	/**
	 * Set of property names that should not be returned by the response because they are already in it.
	 */
	private static final Set<String> SPECIAL_PROPERTIES = new HashSet<>(
			Arrays.asList(EMF_CONTENT, OA_HAS_BODY, OA_MOTIVATED_BY, OA.HAS_BODY.toString(),
					OA.MOTIVATED_BY.toString(), EMF.CONTENT.toString(), RDF_TYPE, RDF.TYPE.toString(),
					OA.HAS_TARGET.toString(), OA_HAS_TARGET));

	private static final long serialVersionUID = -9211980287475339605L;

	private Collection<Annotation> replies = new LinkedList<>();
	private Annotation topic;

	/**
	 * Adds the reply.
	 *
	 * @param annotation
	 *            the annotation
	 */
	public void addReply(Annotation annotation) {
		replies.add(annotation);
	}

	/**
	 * Gets the replies.
	 *
	 * @return the replies
	 */
	public Collection<Annotation> getReplies() {
		return replies;
	}

	/**
	 * Gets the motivation that triggered this annotation.
	 *
	 * @return the motivation
	 */
	public Serializable getMotivation() {
		return get(OA_MOTIVATED_BY, () -> get(OA.MOTIVATED_BY.toString()));
	}

	/**
	 * The identifier of the object where this annotation is placed (image or instance id).
	 *
	 * @return the identifier
	 */
	public Serializable getTargetId() {
		return get(OA_HAS_TARGET, () -> get(OA.HAS_TARGET.toString()));
	}

	/**
	 * The annotation comment.
	 *
	 * @return the comment
	 */
	public Serializable getComment() {
		return get(OA_HAS_BODY, () -> get(OA.HAS_BODY.toString()));
	}

	/**
	 * Sets the content that represents the current annotation instance.
	 *
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		add(EMF_CONTENT, content);
	}

	/**
	 * Gets the content that represents the current annotation instance.
	 *
	 * @return the content or <code>null</code> if not available.
	 */
	public String getContent() {
		return getString(EMF_CONTENT);
	}

	/**
	 * Annotation topic setter.
	 *
	 * @param annotation
	 *            annotation
	 */
	public void setTopic(Annotation annotation) {
		this.topic = annotation;
	}

	/**
	 * Annotation topic getter.
	 *
	 * @return topic
	 */
	public Annotation getTopic() {
		return topic;
	}

	/**
	 * Action setter.
	 *
	 * @param actions
	 *            to set
	 */
	public void setActions(Set<Action> actionsSet) {
		add(AnnotationProperties.ACTIONS_LABEL, (Serializable) actionsSet);
	}

	/**
	 * Gets the response properties. These are properties that are not included in the json contained in the
	 * {@link #getContent()} and should be added before building the end result.
	 *
	 * @return the response properties
	 */
	public Map<String, Serializable> getResponseProperties() {
		Map<String, Serializable> result = new HashMap<>(getOrCreateProperties());
		result.keySet().removeAll(SPECIAL_PROPERTIES);
		return result;
	}

	/**
	 * Gets the annotation creator.
	 *
	 * @return the created by
	 */
	public Serializable getCreatedBy() {
		return get(EMF_CREATED_BY, () -> get(EMF.CREATED_BY.toString()));
	}

	/**
	 * Gets the last user that modified the annotation.
	 *
	 * @return the modified by
	 */
	public Serializable getModifiedBy() {
		return get(EMF_MODIFIED_BY, () -> get(EMF.MODIFIED_BY.toString()));
	}

	/**
	 * Represents the current annotation and it's replies as a stream. If the annotation does not have any replies the
	 * stream will contain only one element the current instance.
	 *
	 * @return the stream representing the current instance and all of it's replies
	 */
	public Stream<Annotation> stream() {
		if (replies.isEmpty()) {
			return Stream.of(this);
		}
		return Stream.concat(Stream.of(this), replies.stream());
	}

	/**
	 * Checks if this annotation is new. New annotation is one that does not have an id, yet.
	 *
	 * @return true, if it's new annotation or reply
	 */
	public boolean isNew() {
		return getId() == null;
	}

	/**
	 * Checks the motivation of this annotation it it's for editing
	 *
	 * @return true, if is for edit.
	 */
	public boolean isForEdit() {
		Serializable motivativation = this.get(OA_MOTIVATED_BY, () -> this.get(OA.MOTIVATED_BY.toString()));
		if (motivativation instanceof Collection<?>) {
			return ((Collection<?>) motivativation).contains(OA.EDITING);
		}
		return OA.EDITING.equals(motivativation);
	}

	/**
	 * Checks annotation if it's called an action
	 *
	 * @return true, if it's called an action
	 */
	public boolean isHasAction() {
		return isPropertyPresent(AnnotationProperties.ACTION.getLocalName())
				|| isPropertyPresent(AnnotationProperties.ACTION.toString());
	}

	/**
	 * Checks if this annotation is a reply of another annotation.
	 *
	 * @return true, if is a reply
	 */
	public boolean isReply() {
		return isPropertyPresent(AnnotationProperties.REPLY_PROPERTY);
	}


	/**
	 * Current annotation status getter.
	 *
	 * @return current status
	 */
	public String getCurrentStatus() {
		return isNew() ? AnnotationProperties.INIT_STATUS
				: getAsString(STATUS, () -> getAsString(EMF.STATUS.toString()));
	}

	/**
	 * Transition getter.
	 *
	 * @return transition
	 */
	public String getTransition() {
		return isNew() ? AnnotationProperties.CREATE_ACTION
				: this.getAsString(AnnotationProperties.ACTION.getLocalName(),
						() -> getAsString(AnnotationProperties.ACTION.toString()));
	}

	/**
	 * Gets any known property values that represent users in the annotation
	 *
	 * @return the users
	 */
	public Stream<Serializable> getUsers() {
		return Arrays.asList(getCreatedBy(), getModifiedBy()).stream();
	}

	/**
	 * Replace the user values in this annotation object using the values provided by the given resolver
	 *
	 * @param resourceProvider
	 *            the resource provider
	 */
	public void expandUsers(Function<Serializable, Instance> resourceProvider) {
		Serializable modifiedBy = getModifiedBy();
		if (modifiedBy != null && !(modifiedBy instanceof Resource)) {
			Instance resource = resourceProvider.apply(modifiedBy);
			if (addIfNotNull(EMF_MODIFIED_BY, resource)) {
				// clear the other value if not defined in the simple key
				remove(EMF.MODIFIED_BY.toString());
			}
		}
		Serializable createdBy = getCreatedBy();
		if (createdBy != null && !(createdBy instanceof Resource)) {
			Instance resource = resourceProvider.apply(createdBy);
			if (addIfNotNull(EMF_CREATED_BY, resource)) {
				// clear the other value if not defined in the simple key
				remove(EMF.CREATED_BY.toString());
			}
		}
	}

	/**
	 * Mentioned users getter.
	 *
	 * @return mentioned users
	 */
	public Collection<Serializable> getMentionedUsers() {
		return getAsCollection(EMF_MENTIONED_USERS,
				() -> getAsCollection(EMF.MENTIONED_USERS.toString(), HashSet<Serializable>::new));
	}

	/**
	 * Checks if this annotation has mentioned users.
	 *
	 * @return true, if there are mentioned users
	 */
	public boolean isSomeoneMentioned() {
		return !CollectionUtils.isEmpty(getMentionedUsers());
	}

	/**
	 * Comments on getter.
	 *
	 * @return comments on property
	 */
	public Serializable getCommentsOn() {
		return get(EMF.COMMENTS_ON.getLocalName(), () -> get(EMF.COMMENTS_ON.toString()));
	}

	@Override
	public String toString() {
		return new StringBuilder(1024)
				.append("Annotation [id=")
				.append(getId())
				.append(", properties=")
				.append(getProperties())
				.append("]")
				.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (replies == null ? 0 : replies.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Annotation)) {
			return false;
		}
		Annotation other = (Annotation) obj;
		return nullSafeEquals(replies, other.replies);
	}

}
