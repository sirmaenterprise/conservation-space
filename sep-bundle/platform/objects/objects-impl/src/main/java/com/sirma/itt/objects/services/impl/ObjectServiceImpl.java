package com.sirma.itt.objects.services.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;
import com.sirma.itt.objects.services.ObjectService;

/**
 * Default {@link ObjectService} implementation.
 *
 * @author BBonev
 */
@Stateless
public class ObjectServiceImpl extends
		BaseSemanticInstanceServiceImpl<ObjectInstance, ObjectDefinition> implements ObjectService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectServiceImpl.class);
	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesObject.OBJECT)
	private InstanceDao<ObjectInstance> instanceDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean move(ObjectInstance objectInstance, Instance src, Instance dest) {
		if (objectInstance == null) {
			// can't move nothing
			return false;
		}
		if (src != null) {
			instanceService.detach(src, new Operation(ActionTypeConstants.MOVE_OTHER_CASE),
					objectInstance);
		}
		if (dest != null) {
			instanceService.attach(dest, new Operation(ObjectActionTypeConstants.ATTACH_OBJECT),
					objectInstance);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected InstanceDao<ObjectInstance> getInstanceDao() {
		return instanceDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected Logger getLogger() {
		return LOGGER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<ObjectDefinition> getInstanceDefinitionClass() {
		return ObjectDefinition.class;
	}

}
