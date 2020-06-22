package com.sirma.itt.seip.domain.mock;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * Mock for {@link InstanceType}.
 *
 * @author smustafov
 */
public class TestType implements InstanceType {

	private Serializable id;
	private Set<InstanceType> subTypes = new HashSet<>();

	public void setSubTypes(Set<InstanceType> subTypes) {
		this.subTypes = subTypes;
	}

	public void setId(Serializable id) {
		this.id = id;
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public String getCategory() {
		return null;
	}

	@Override
	public Set<InstanceType> getSuperTypes() {
		return null;
	}

	@Override
	public Set<InstanceType> getSubTypes() {
		return subTypes;
	}

	@Override
	public boolean hasTrait(String trait) {
		return false;
	}

	@Override
	public String getProperty(String propertyName) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InstanceType)) {
			return false;
		}
		InstanceType other = (InstanceType) obj;
		return nullSafeEquals(getId(), other.getId());
	}

	@Override
	public int hashCode() {
		return 31 + id.hashCode();
	}

	@Override
	public boolean isVersionable() {
		return true;
	}

}
