package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.collections.SealedMap;

/**
 * The EmfGroup is holder for authority groups
 *
 * @author BBonev
 */
@SuppressWarnings("squid:S2160")
public class EmfGroup extends EmfResource implements Group, Sealable, GenericProxy<Group> {

	private static final long serialVersionUID = 6256379555654336317L;
	private transient boolean seal;

	/**
	 * Instantiates a new emf group.
	 */
	public EmfGroup() {
		this(null,null);
	}

	/**
	 * Instantiates a new emf group.
	 *
	 * @param groupId
	 *            the groupid
	 * @param displayName
	 *            the display name
	 */
	public EmfGroup(String groupId, String displayName) {
		setName(groupId);
		setDisplayName(displayName);
		setType(ResourceType.GROUP);
	}

	@Override
	public EmfGroup createCopy() {
		EmfGroup emfGroup = new EmfGroup(getName(), getDisplayName());
		emfGroup.setId(getId());
		emfGroup.setIdentifier(getIdentifier());
		emfGroup.setSource(getSource());
		emfGroup.addAllProperties(getProperties());
		return emfGroup;
	}

	@Override
	public void setId(Serializable id) {
		if (isSealed()) {
			return;
		}
		super.setId(id);
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		if (isSealed()) {
			return;
		}
		super.setProperties(properties);
	}

	@Override
	public void setType(ResourceType type) {
		if (isSealed()) {
			return;
		}
		super.setType(type);
	}

	@Override
	public void setName(String name) {
		if (isSealed()) {
			return;
		}
		super.setName(name);
		add(ResourceProperties.GROUP_ID, name);
	}

	@Override
	public String getDisplayName() {
		if (displayName == null) {
			return EmfResourcesUtil.getGroupDisplayName(getProperties());
		}
		return displayName;
	}

	@Override
	public boolean isSealed() {
		return seal;
	}

	@Override
	public void seal() {
		if (isSealed()) {
			return;
		}
		setProperties(new SealedMap<>(getProperties()));
		seal = true;
	}

	@Override
	public ResourceType getType() {
		ResourceType resourceType = super.getType();
		if (resourceType == null) {
			return ResourceType.GROUP;
		}
		return resourceType;
	}

	@Override
	public Group getTarget() {
		return null;
	}

	@Override
	public void setTarget(Group target) {
		// nothing to set for now
	}

	@Override
	public Group cloneProxy() {
		return createCopy();
	}

	@Override
	public String toString() {
		return new StringBuilder(512)
				.append("EmfGroup [id=")
					.append(getId())
					.append(", name=")
					.append(getName())
					.append(", displayName=")
					.append(getDisplayName())
					.append(", properties=")
					.append(getProperties())
					.append("]")
					.toString();
	}

}
