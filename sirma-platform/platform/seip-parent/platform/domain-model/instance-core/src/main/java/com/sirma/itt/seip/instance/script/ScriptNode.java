package com.sirma.itt.seip.instance.script;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.ASSIGNEES_NOTIFICATION_ENABLED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.EMAIL_DISTRIBUTION_LIST;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * * Base class for implementing nodes for working with instances in scripts. Each node instance represents a single
 * instance. The class could be extended for provide custom methods for the concrete instance implementations.<br>
 * <b>NOTE:</b> All classes that extend the current should not define any long running scope. If the classes is just
 * injected in other CDI bean it will be uninitialized until the {@link #setTarget(Instance)} method is called with a
 * valid instance.
 *
 * @author BBonev
 */
@InstanceType(type = ScriptInstance.SCRIPT_TYPE)
public class ScriptNode implements ScriptInstance {

	protected static final ScriptNode[] EMPTY_NODES = new ScriptNode[0];

	/** The target. */
	protected Instance target;

	/** The instance service. */
	@Inject
	protected InstanceService instanceService;

	/** The link service. */
	@Inject
	protected LinkService linkService;

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	/** The state service. */
	@Inject
	protected StateService stateService;

	@Inject
	protected TypeMappingProvider typeProvider;

	@Inject
	protected SecurityContext securityContext;

	@Inject
	protected EventService eventService;

	@Inject
	private LinkProviderService linkProviderService;

	@Inject
	private InstanceContextService contextService;

	/** The atomic integer. */
	private static AtomicInteger atomicInteger = new AtomicInteger(0);

	/** The index. */
	private int index = atomicInteger.incrementAndGet();

	/**
	 * Saves the target instance using the default operation for editing.
	 *
	 * @return the script node
	 */
	public ScriptNode save() {
		return save(ActionTypeConstants.EDIT_DETAILS);
	}

	/**
	 * Saves the target instance with the provided operation.
	 *
	 * @param operation
	 *            the operation
	 * @return the current script node
	 */
	public ScriptNode save(String operation) {
		return save(operation, null);
	}

	/**
	 * Saves the target instance with the provided operation.
	 *
	 * @param operation
	 *            the operation
	 * @param preferredState
	 *            the preferred state to be set when saving the instance.
	 * @return the current script node
	 */
	public ScriptNode save(String operation, String preferredState) {
		if (getTarget() != null) {
			instanceService.save(getTarget(), new Operation(operation, preferredState, null));
		}
		return this;
	}

	/**
	 * Gets the all linked objects where the current object is the begging of the link.
	 *
	 * @return the target objects of the links as {@link ScriptNode}s.
	 */
	public ScriptNode[] getAllLinks() {
		return getLinks(null);
	}

	/**
	 * Gets the linked objects where the current instance is the begging and the relation type is the provided link id.
	 * If the link id is <code>null</code> all links will be returned.
	 *
	 * @param linkId
	 *            the link id to filter
	 * @return the linked objects.
	 */
	public ScriptNode[] getLinks(String linkId) {
		if (target == null) {
			return EMPTY_NODES;
		}
		List<LinkReference> linkReferences = null;
		if (StringUtils.isBlank(linkId)) {
			linkReferences = linkService.getLinks(toReference());
		} else {
			linkReferences = linkService.getLinks(toReference(), linkId);
		}
		return convertLinksToNodes(linkReferences, LinkReference::getTo);
	}

	/**
	 * Gets the all linked objects from all links where the current objects is at the end of the links.
	 *
	 * @return the all linked objects
	 */
	public ScriptNode[] getAllLinksTo() {
		return getLinksTo(null);
	}

	/**
	 * Gets the linked objects from all links where the current object is at the end of the relation and the link id is
	 * with the provided name.
	 *
	 * @param linkId
	 *            the link id
	 * @return the linked objects
	 */
	public ScriptNode[] getLinksTo(String linkId) {
		if (target == null) {
			return EMPTY_NODES;
		}
		List<LinkReference> linkReferences = null;
		if (StringUtils.isBlank(linkId)) {
			linkReferences = linkService.getLinksTo(toReference());
		} else {
			linkReferences = linkService.getLinksTo(toReference(), linkId);
		}
		return convertLinksToNodes(linkReferences, LinkReference::getFrom);
	}

	private ScriptNode[] convertLinksToNodes(List<LinkReference> linkReferences,
			Function<LinkReference, InstanceReference> linkExctractor) {
		return linkReferences
				.stream()
					.map(linkExctractor)
					.map(InstanceReference::toInstance)
					.map(this::toScriptNode)
					.toArray(ScriptNode[]::new);
	}

	/**
	 * Links the current node to the given node using the given link type
	 *
	 * @param to
	 *            that target link node
	 * @param linkId
	 *            the link id
	 * @return true, if successful
	 * @see ScriptNode#linkToReference(InstanceReference, String, String)
	 */
	public boolean linkTo(ScriptNode to, String linkId) {
		return linkTo(to, linkId, null);
	}

	/**
	 * Links the current node to the given node using the given direct and inverse relations
	 *
	 * @param to
	 *            the target link instance
	 * @param linkId
	 *            the link id
	 * @param reverseLinkId
	 *            the reverse link id
	 * @return true, if successful
	 * @see ScriptNode#linkToReference(InstanceReference, String, String)
	 */
	public boolean linkTo(ScriptNode to, String linkId, String reverseLinkId) {
		if (to == null) {
			return false;
		}
		return linkToReference(to.getTarget().toReference(), linkId, reverseLinkId);
	}

	/**
	 * Links the current node to the given target represented by the given {@link InstanceReference} . A link will be
	 * created when at least one of the relation type arguments is passed.
	 *
	 * @param to
	 *            the link target
	 * @param linkId
	 *            the main link id
	 * @param reverseLinkId
	 *            the reverse link id
	 * @return <code>true</code>, if successful and <code>false</code> if failed to created link or there are missing
	 *         required parameters.
	 */
	public boolean linkToReference(InstanceReference to, String linkId, String reverseLinkId) {
		if (to == null || StringUtils.isBlank(linkId)) {
			return false;
		}
		getTarget().append(linkId, to.getId());
		return true;
	}

	/**
	 * Attaches the current node the given script node.
	 *
	 * @param destination
	 *            the attach destination
	 * @param operation
	 *            the operation for attachment
	 * @return <code>true</code>, if successful or <code>false</code> if destination is <code>null</code>.
	 */
	public boolean attachTo(ScriptNode destination, String operation) {
		if (destination == null) {
			return false;
		}

		instanceService.attach(destination.getTarget(), new Operation(operation), getTarget());
		return true;
	}

	/**
	 * Change state of the instance using the transition identified by the specified operation.
	 *
	 * @param operation
	 *            the operation to execute
	 * @return the script node
	 */
	public ScriptNode changeState(String operation) {
		if (StringUtils.isBlank(operation) || target == null) {
			return this;
		}
		stateService.changeState(target, new Operation(operation));
		return this;
	}

	/**
	 * Gets the state for the current node.
	 *
	 * @return the state
	 */
	public String getState() {
		if (target == null) {
			return null;
		}
		return stateService.getPrimaryState(target);
	}

	/**
	 * Publish the current node using the provided operation into the given target node
	 *
	 * @param operation
	 *            the operation
	 * @return the script node
	 */
	public ScriptNode publish(String operation) {
		return toScriptNode(instanceService.publish(getTarget(), new Operation(operation)));
	}

	/**
	 * Gets the parent node if any or <code>null</code>.
	 *
	 * @return the parent
	 */
	public ScriptNode getParent() {
		return getParentInternal(contextService.getContext(target).map(InstanceReference::toInstance).orElse(null));
	}

	/**
	 * Searches for parent of given type in the given instance hierarchy. If such parent exists it will be returned, or
	 * <code>null</code> otherwise.
	 *
	 * @param type
	 *            the parent type (e.g., project, case, document, workflow, workflowTask, task, etc.)
	 * @return the parent {@link ScriptNode} or <code>null</code>
	 */
	public ScriptNode getParent(String type) {
		return getParent();
	}

	private ScriptNode getParentInternal(Instance parent) {
		if (parent == null) {
			return null;
		}
		instanceService.refresh(parent);
		return toScriptNode(parent);
	}

	/**
	 * Checks if the current instance name is matching the provided string value.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public boolean is(String value) {
		if (target == null || target.type() == null || StringUtils.isBlank(value)) {
			return false;
		}
		return target.type().is(value) || target.type().getCategory().toLowerCase().contains(value);
	}

	/**
	 * Gets the properties of the current instance.
	 *
	 * @return the properties
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		if (target == null) {
			return null;
		}
		return target.getProperties();
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	@Override
	public Instance getTarget() {
		return target;
	}

	/**
	 * Refresh the target {@link Instance}
	 */
	public void refresh() {
		if (target != null) {
			instanceService.refresh(target);
		}
	}

	/**
	 * Builds a bookmarkable URL to the target instance.
	 *
	 * @return bookmarkable link to the target instance
	 */
	public String buildBookmarkableURL() {
		return linkProviderService.buildLink(target);
	}

	/**
	 * Gets target primary key.
	 *
	 * @return the id
	 */
	@Override
	public Serializable getId() {
		return getTarget().getId();
	}

	/**
	 * Setter method for target.
	 *
	 * @param target
	 *            the target to set
	 * @return the script node
	 */
	@Override
	public ScriptNode setTarget(Instance target) {
		if (target == null) {
			throw new EmfRuntimeException("Cannot set null instance in a ScriptNode");
		}
		this.target = target;
		return this;
	}

	/**
	 * Sets a text content to the current node.
	 *
	 * @param content
	 *            the new text content
	 * @return true, if operation completed successfully
	 */
	public boolean setTextContent(String content) {
		// nothing to do for generic nodes.
		return true;
	}

	/**
	 * Fires @AuditableEvent for the audit log.
	 *
	 * @param operationId
	 *            event operation id
	 * @return {@link ScriptNode}
	 */
	public ScriptNode fireAuditableEvent(String operationId) {
		eventService.fire(new AuditableEvent(target, operationId));
		return this;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (target == null ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ScriptNode)) {
			return false;
		}
		ScriptNode other = (ScriptNode) obj;
		return nullSafeEquals(target, other.target);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScriptNode [index=");
		builder.append(index);
		builder.append(", target=");
		builder.append(target);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Converts the given instance to script node.
	 *
	 * @param instance
	 *            the instance
	 * @return the script node
	 */
	protected ScriptNode toScriptNode(Instance instance) {
		return typeConverter.convert(ScriptNode.class, instance);
	}

	/**
	 * To array.
	 *
	 * @param result
	 *            the result
	 * @return the script node[]
	 */
	protected ScriptNode[] toArray(Collection<ScriptNode> result) {
		return result.toArray(new ScriptNode[result.size()]);
	}

	@Override
	public void setRevision(Long revision) {
		getTarget().setRevision(revision);
	}

	@Override
	public InstanceReference toReference() {
		return getTarget().toReference();
	}

	@Override
	public boolean isDeleted() {
		return getTarget().isDeleted();
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		getTarget().setProperties(properties);
	}

	@Override
	public Long getRevision() {
		return getTarget().getRevision();
	}

	@Override
	public PathElement getParentElement() {
		return getTarget().getParentElement();
	}

	@Override
	public String getPath() {
		return getTarget().getPath();
	}

	@Override
	public boolean hasChildren() {
		return getTarget().hasChildren();
	}

	@Override
	public Node getChild(String name) {
		return getTarget().getChild(name);
	}

	@Override
	public String getIdentifier() {
		return getTarget().getIdentifier();
	}

	@Override
	public void setIdentifier(String identifier) {
		getTarget().setIdentifier(identifier);
	}

	@Override
	public void setId(Serializable id) {
		getTarget().getId();
	}

	/**
	 * Checks if the current document is locked.
	 *
	 * @return true, if it's locked
	 */
	@Override
	public boolean isLocked() {
		return target != null && getTarget().isLocked();
	}

	/**
	 * Gets the locked by user.
	 *
	 * @return the locked by user
	 */
	@Override
	public String getLockedBy() {
		if (target == null) {
			return null;
		}
		return getTarget().getLockedBy();
	}

	/**
	 * Get the email distribution list of a workflow
	 *
	 * @return {@link Collection} containing all identifiers of users and groups added so far in the email distribution
	 *         list
	 */
	public String[] getEmailDistributionList() {
		return getArray(EMAIL_DISTRIBUTION_LIST);
	}

	/**
	 * Checks whether assignees notification is enabled.
	 *
	 * @return true, if assignees notification is enabled
	 */
	public boolean isAssigneesNotificationEnabled() {
		return target.getBoolean(ASSIGNEES_NOTIFICATION_ENABLED);
	}

	/**
	 * Gets multivalue property from the target instance and returns array of it values. If the passed property is null
	 * or it is not a {@link Collection} this method will return empty array. This method will return results, only if
	 * the collection values are strings.
	 *
	 * @param property
	 *            the property that should be retrieved from the instance
	 * @return array with the property values or empty array if the property is null or it is not a {@link Collection}
	 */
	public String[] getArray(String property) {
		Predicate<Serializable> isString = String.class::isInstance;
		return getAsCollection(property, ArrayList::new).stream().filter(isString).toArray(String[]::new);
	}

}
