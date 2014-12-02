package com.sirma.itt.pm.domain.model;

import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.instance.model.ScheduleSynchronizationInstance;

/**
 * Instance implementation that represents a project.
 *
 * @author BBonev
 */
public class ProjectInstance extends EmfInstance implements RootInstanceContext,
		ScheduleSynchronizationInstance {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2104997890627772655L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance getOwningInstance() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOwningInstance(Instance instance) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProjectInstance [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(getIdentifier());
		builder.append(", revision=");
		builder.append(getVersion());
		builder.append(", container=");
		builder.append(getContainer());
		builder.append(", dmsId=");
		builder.append(getDmsId());
		builder.append(", contentManagementId=");
		builder.append(getContentManagementId());
		builder.append(", version=");
		builder.append(getVersion());
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append("]");
		return builder.toString();
	}

}
