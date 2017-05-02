package com.sirma.itt.seip.domain.search.tree;

import java.util.List;

/**
 * Represent a search criteria e.g title eq "How to Search".
 * 
 * @author yasko
 */
public class Rule extends SearchTreeNode {

	private String field;
	// TODO: if we know all types this could also be an enum
	private String type;
	private String operation;
	private List<String> values;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.RULE;
	}

}
