
package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.db.RelationalDb;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkService;

/**
 * Default implementation for object instance support.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.OBJECT)
public class ObjectInstanceDao
		extends BaseSemanticInstanceDaoImpl<ObjectInstance, Serializable, String, GenericDefinition> {

	/** The relational link service. */
	@Inject
	@RelationalDb
	private LinkService relationalLinkService;

	@Override
	protected void populateInstanceForModel(Instance instance, GenericDefinition model) {
		TenantAware.setContainer(instance, securityContext.getCurrentTenantId());
		populateProperties(instance, model);
	}

	@Override
	protected Class<ObjectInstance> getInstanceClass() {
		return ObjectInstance.class;
	}

	@Override
	protected Class<ObjectInstance> getEntityClass() {
		return ObjectInstance.class;
	}
}
