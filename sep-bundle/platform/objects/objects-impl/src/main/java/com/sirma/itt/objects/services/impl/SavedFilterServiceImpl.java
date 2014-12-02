package com.sirma.itt.objects.services.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.SavedFilter;
import com.sirma.itt.objects.services.SavedFilerService;

/**
 * Default {@link SavedFilerService} implementation.
 * 
 * @author BBonev
 */
@Stateless
public class SavedFilterServiceImpl extends
		BaseSemanticInstanceServiceImpl<SavedFilter, ObjectDefinition> implements SavedFilerService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SavedFilterServiceImpl.class);

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesObject.SAVED_FILTER)
	private InstanceDao<SavedFilter> instanceDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<ObjectDefinition> getInstanceDefinitionClass() {
		return ObjectDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected InstanceDao<SavedFilter> getInstanceDao() {
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

}
