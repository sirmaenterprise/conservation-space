package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.instance.model.ScheduleSynchronizationInstance;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Represents an actual instance of a Case.
 * 
 * @author BBonev
 */
public class CaseInstance extends EmfInstance implements BidirectionalMapping, InstanceContext,
		ScheduleSynchronizationInstance {

	private static final long serialVersionUID = 4117697610123839101L;

	private List<SectionInstance> sections = new LinkedList<>();

	private Role calculatedUserRole;

	/**
	 * Getter method for sections.
	 * 
	 * @return the sections
	 */
	public List<SectionInstance> getSections() {
		return sections;
	}

	/**
	 * Setter method for sections.
	 * 
	 * @param sections
	 *            the sections to set
	 */
	public void setSections(List<SectionInstance> sections) {
		this.sections = sections;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CaseInstance [id=");
		builder.append(getId());
		builder.append(", caseDefinitionId=");
		builder.append(getIdentifier());
		builder.append(", revision=");
		builder.append(getRevision());
		builder.append(", dmsId=");
		builder.append(getDmsId());
		builder.append(", contentManagementId=");
		builder.append(getContentManagementId());
		builder.append(",\nproperties=");
		builder.append(getProperties());
		builder.append(",\nsections=");
		builder.append(sections);
		builder.append("\n]");
		return builder.toString();
	}

	@Override
	public void initBidirection() {
		for (SectionInstance instance : sections) {
			instance.setOwningInstance(this);
			instance.setOwningReference(this.toReference());
			instance.initBidirection();
		}
	}

	/**
	 * Sets the calculated user role.
	 * 
	 * @param calculatedUserRole
	 *            the new calculated user role
	 */
	public void setCalculatedUserRole(Role calculatedUserRole) {
		this.calculatedUserRole = calculatedUserRole;
	}

	/**
	 * Gets the calculated user role.
	 * 
	 * @return the calculated user role
	 */
	public Role getCalculatedUserRole() {
		return calculatedUserRole;
	}

	@Override
	public boolean hasChildren() {
		return !getSections().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		Serializable serializable = getProperties().get(name);
		if (serializable instanceof Node) {
			return (Node) serializable;
		}
		return PathHelper.find(getSections(), name);
	}

}
