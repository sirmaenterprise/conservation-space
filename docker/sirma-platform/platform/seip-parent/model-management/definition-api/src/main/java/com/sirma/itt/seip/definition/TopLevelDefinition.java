package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Defines common methods for top level definition model
 *
 * @author BBonev
 */
public interface TopLevelDefinition extends DefinitionModel, TenantAware, DmsAware {

	/**
	 * Gets the parent definition id.
	 *
	 * @return the parent definition id
	 */
	String getParentDefinitionId();

	/**
	 * Gets the dms id.
	 *
	 * @return the dms id
	 */
	@Override
	String getDmsId();

	/**
	 * Sets the dms id.
	 *
	 * @param dmsId
	 *            the new dms id
	 */
	@Override
	void setDmsId(String dmsId);

	/**
	 * Gets the container.
	 *
	 * @return the container
	 */
	@Override
	String getContainer();

	/**
	 * Sets the container.
	 *
	 * @param container
	 *            the new container
	 */
	@Override
	void setContainer(String container);

	/**
	 * Checks if the given definition is abstract.
	 *
	 * @return true, if is abstract
	 */
	boolean isAbstract();

	/**
	 * Gets the revision.
	 *
	 * @return the revision
	 */
	@Override
	Long getRevision();

	/**
	 * Sets the revision.
	 *
	 * @param revision
	 *            the new revision
	 */
	void setRevision(Long revision);

}
