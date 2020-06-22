/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;

/**
 * Mock object for {@link ControlParam}
 *
 * @author BBonev
 */
public class ControlParamMock implements ControlParam {

	protected String identifier;
	protected String name;
	protected String value;
	protected String type;
	private ControlDefinition control;

	@Override
	public PathElement getParentElement() {
		return getControlDefinition();
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ControlDefinition getControlDefinition() {
		return control;
	}

	/**
	 * Sets the control definition.
	 *
	 * @param controlDefinition
	 *            the new control definition
	 */
	public void setControlDefinition(ControlDefinition controlDefinition) {
		control = controlDefinition;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

}
