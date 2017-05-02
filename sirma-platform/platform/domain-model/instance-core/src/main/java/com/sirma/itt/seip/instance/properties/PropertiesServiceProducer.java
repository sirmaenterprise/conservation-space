package com.sirma.itt.seip.instance.properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.ArchivedDataAccess;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * Producer bean that creates different properties service instances based on the use of {@link ArchivedDataAccess}
 * qualifier.
 *
 * @author BBonev
 */
@ApplicationScoped
public class PropertiesServiceProducer {

	@Inject
	@InstanceType(type = "default")
	private PropertyModelCallback<PropertyModel> defaultInstanceCallback;
	@Inject
	private PropertiesDao propertiesDao;
	/*
	 * @Any annotation is used here to ignore callback qualifiers and inject all of them
	 */
	@Inject
	@Any
	private Instance<PropertyModelCallback<? extends PropertyModel>> callbacks;
	@Inject
	@InstanceType(type = "CommonInstance")
	private Instance<InstanceDao> instanceDao;
	@Inject
	private PropertiesStorageAccess defaultAccess;
	@Inject
	@ArchivedDataAccess
	private PropertiesStorageAccess acriveDataAccess;
	@Inject
	private DatabaseIdManager idManager;
	@Inject
	private TaskExecutor taskExecutor;
	@Inject
	private ServiceRegistry serviceRegistry;
	@Inject
	private ObjectMapper mapper;

	/**
	 * Produce default properties service.
	 *
	 * @return the properties service
	 */
	@Produces
	@Default
	@ApplicationScoped
	public PropertiesService produceDefaultPropertiesService() {
		return new PropertiesServiceImpl(defaultAccess, propertiesDao, defaultInstanceCallback, callbacks, instanceDao,
				idManager, taskExecutor, serviceRegistry, mapper);
	}

	/**
	 * Produce archived properties service.
	 *
	 * @return the archive properties service
	 */
	@Produces
	@ArchivedDataAccess
	@ApplicationScoped
	public PropertiesService produceArchivedPropertiesService() {
		return new PropertiesServiceImpl(acriveDataAccess, propertiesDao, defaultInstanceCallback, callbacks,
				instanceDao, idManager, taskExecutor, serviceRegistry, mapper);
	}

}
