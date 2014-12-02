package com.sirma.itt.emf.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Base class for implementing nodes for working with instances in scripts. Each node instance
 * represents a single instance. The class could be extended for provide custom methods for the
 * concrete instance implementations.<br>
 * <b>NOTE:</b> All classes that extend the current should not define any long running scope. If the
 * classes is just injected in other CDI bean it will be uninitialized until the
 * {@link #setTarget(Instance)} method is called with a valid instance.
 * 
 * @author BBonev
 */
public class ScriptNode {

	/** The target. */
	protected Instance target;

	/** The instance service. */
	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

	/** The link service. */
	@Inject
	protected LinkService linkService;

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	/** The state service. */
	@Inject
	protected StateService stateService;

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
		return save("EDIT_DETAILS");
	}

	/**
	 * Saves the target instance with the provided operation.
	 * 
	 * @param operation
	 *            the operation
	 * @return the current script node
	 */
	public ScriptNode save(String operation) {
		if (getTarget() != null) {
			instanceService.save(getTarget(), new Operation(operation));
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
	 * Gets the linked objects where the current instance is the begging and the relation type is
	 * the provided link id. If the link id is <code>null</code> all links will be returned.
	 * 
	 * @param linkId
	 *            the link id to filter
	 * @return the linked objects.
	 */
	public ScriptNode[] getLinks(String linkId) {
		if (target == null) {
			return new ScriptNode[0];
		}
		List<ScriptNode> result = getLinksInternal(linkId);
		return toArray(result);
	}

	/**
	 * Gets the links from the current instance with the given link type.
	 * 
	 * @param linkId
	 *            the link id
	 * @return the links internal
	 */
	protected List<ScriptNode> getLinksInternal(String linkId) {
		List<LinkReference> linkReferences = null;
		if (StringUtils.isNullOrEmpty(linkId)) {
			linkReferences = linkService.getLinks(target.toReference());
		} else {
			linkReferences = linkService.getLinks(target.toReference(), linkId);
		}
		List<LinkInstance> linkInstances = linkService.convertToLinkInstance(linkReferences, true);
		List<ScriptNode> result = new ArrayList<ScriptNode>(linkInstances.size());
		for (LinkInstance linkInstance : linkInstances) {
			ScriptNode scriptNode = typeConverter.convert(ScriptNode.class, linkInstance.getTo());
			result.add(scriptNode);
		}
		return result;
	}

	/**
	 * Gets the all linked objects from all links where the current objects is at the end of the
	 * links.
	 * 
	 * @return the all linked objects
	 */
	public ScriptNode[] getAllLinksTo() {
		return getLinksTo(null);
	}

	/**
	 * Gets the linked objects from all links where the current object is at the end of the relation
	 * and the link id is with the provided name.
	 * 
	 * @param linkId
	 *            the link id
	 * @return the linked objects
	 */
	public ScriptNode[] getLinksTo(String linkId) {
		if (target == null) {
			return new ScriptNode[0];
		}
		List<LinkReference> linkReferences = null;
		if (StringUtils.isNullOrEmpty(linkId)) {
			linkReferences = linkService.getLinksTo(target.toReference());
		} else {
			linkReferences = linkService.getLinksTo(target.toReference(), linkId);
		}
		List<LinkInstance> linkInstances = linkService.convertToLinkInstance(linkReferences, true);
		List<ScriptNode> result = new ArrayList<ScriptNode>(linkInstances.size());
		for (LinkInstance linkInstance : linkInstances) {
			ScriptNode scriptNode = typeConverter.convert(ScriptNode.class, linkInstance.getFrom());
			result.add(scriptNode);
		}
		return toArray(result);
	}

	/**
	 * Change state of the instance using the transition identified by the specified operation.
	 * 
	 * @param operation
	 *            the operation to execute
	 * @return the script node
	 */
	public ScriptNode changeState(String operation) {
		if (StringUtils.isNullOrEmpty(operation) || (target == null)) {
			return this;
		}
		stateService.changeState(target, new Operation(operation));
		return this;
	}

	/**
	 * Gets the parent node if any or <code>null</code>.
	 * 
	 * @return the parent
	 */
	public ScriptNode getParent() {
		if (target instanceof OwnedModel) {
			return typeConverter.convert(ScriptNode.class,
					((OwnedModel) target).getOwningInstance());
		}
		return null;
	}

	/**
	 * Checks if the current instance name is matching the provided string value.
	 * 
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public boolean is(String value) {
		if ((target == null) || StringUtils.isNullOrEmpty(value)) {
			return false;
		}
		return target.getClass().getName().toLowerCase().contains(value);
	}

	/**
	 * Gets the properties of the current instance.
	 * 
	 * @return the properties
	 */
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
	public Instance getTarget() {
		return target;
	}

	/**
	 * Setter method for target.
	 * 
	 * @param target
	 *            the target to set
	 * @return the script node
	 */
	public ScriptNode setTarget(Instance target) {
		if (target == null) {
			throw new EmfRuntimeException("Cannot set null instance in a ScriptNode");
		}
		this.target = target;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ScriptNode)) {
			return false;
		}
		ScriptNode other = (ScriptNode) obj;
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * To array.
	 * 
	 * @param result
	 *            the result
	 * @return the script node[]
	 */
	protected ScriptNode[] toArray(Collection<ScriptNode> result) {
		return result.toArray(new ScriptNode[result.size()]);
	}
}
