/*
 *
 */
package com.sirma.itt.cmf.services.impl.dao;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;

/**
 * The Class SectionInstanceDao. {@link com.sirma.itt.emf.instance.dao.InstanceDao} implementation
 * for {@link SectionInstance}.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.SECTION)
public class SectionInstanceDao extends BaseSectionInstanceDao<SectionInstance, SectionDefinition> {

	@Override
	protected Class<SectionInstance> getInstanceClass() {
		return SectionInstance.class;
	}

	@Override
	protected Class<SectionDefinition> getDefinitionClass() {
		return SectionDefinition.class;
	}

}
