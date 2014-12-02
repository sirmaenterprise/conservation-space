package com.sirma.cmf.web.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.PrimaryStateType;

/**
 * Utility method for {@link Instance} accessible thru EL.
 * 
 * @author yasko
 */
@Named
@ApplicationScoped
public class InstanceHelper {

	@Inject
	private TypeConverter typeConverter;

	/**
	 * Check if an instance is in a deleted state.
	 * 
	 * @param instance
	 *            Instance to check.
	 * @return {@code true} if the instance is deleted, {@code false} otherwise.
	 */
	public boolean isDeleted(Instance instance) {
		return isDeletedInstance(instance);
	}

	/**
	 * Checks if is deleted.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @return true, if is deleted
	 */
	public boolean isDeleted(String instanceId, String instanceType) {
		Instance instance = fetchInstance(instanceId, instanceType);
		return isDeletedInstance(instance);
	}

	/**
	 * Checks if is deleted instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is deleted instance
	 */
	private boolean isDeletedInstance(Instance instance) {
		if ((instance != null) && (instance.getProperties() != null)) {
			return PrimaryStateType.DELETED.equals(instance.getProperties().get(
					DefaultProperties.STATUS));
		}
		return false;
	}

	/**
	 * Fetch instance.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @return the instance
	 */
	public Instance fetchInstance(String instanceId, String instanceType) {
		Instance instance = null;
		if (StringUtils.isNotNullOrEmpty(instanceId) && StringUtils.isNotNullOrEmpty(instanceType)) {
			InstanceReference reference = typeConverter.convert(InstanceReference.class,
					instanceType);
			reference.setIdentifier(instanceId);
			instance = typeConverter.convert(InitializedInstance.class, reference).getInstance();
		}
		return instance;
	}
}
