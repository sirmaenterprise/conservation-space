/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * @author BBonev
 *
 */
public class ControlDefintionMock implements ControlDefinition {

	private PathElement parent;
	private String identifier;
	private List<PropertyDefinition> fields = new LinkedList<>();
	private Long revision;
	private Integer hash;
	private List<ControlParam> uiParams = new LinkedList<>();
	private List<ControlParam> controlParams = new LinkedList<>();

	@Override
	public PathElement getParentElement() {
		return parent;
	}

	public void setParent(PathElement parent) {
		this.parent = parent;
	}

	@Override
	public String getPath() {
		return getIdentifier();
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
	public List<PropertyDefinition> getFields() {
		return fields;
	}

	/**
	 * Sets the fields.
	 *
	 * @param fields
	 *            the new fields
	 */
	public void setFields(List<PropertyDefinition> fields) {
		this.fields = fields;
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	@Override
	public JSONObject toJSONObject() {
		return null;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		//
	}

	@Override
	public List<ControlParam> getControlParams() {
		return controlParams;
	}

	/**
	 * Sets the control params.
	 *
	 * @param controlParams
	 *            the new control params
	 */
	public void setControlParams(List<ControlParam> controlParams) {
		this.controlParams = controlParams;
	}

	@Override
	public List<ControlParam> getUiParams() {
		return uiParams;
	}

}
