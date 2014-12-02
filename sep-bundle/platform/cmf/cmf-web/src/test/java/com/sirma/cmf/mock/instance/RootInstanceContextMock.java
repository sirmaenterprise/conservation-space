package com.sirma.cmf.mock.instance;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.RootInstanceContext;

/**
 * The Class ProjectInstanceMock.
 */
public class RootInstanceContextMock implements Instance, RootInstanceContext {

	@Override
	public Map<String, Serializable> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public Long getRevision() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathElement getParentElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node getChild(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIdentifier(String identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Serializable id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRevision(Long revision) {
		// TODO Auto-generated method stub

	}

	@Override
	public InstanceReference toReference() {
		// TODO Auto-generated method stub
		return null;
	}

}
