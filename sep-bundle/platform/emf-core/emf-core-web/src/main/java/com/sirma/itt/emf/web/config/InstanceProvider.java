package com.sirma.itt.emf.web.config;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.converter.SerializableConverter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * InstanceProvider.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class InstanceProvider {

	/** The type converter. */
	@Inject
	@SerializableConverter
	protected TypeConverter typeConverter;

	/**
	 * Fetch instance.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            The instance type. This should be the simple class name to lower case!
	 * @return the instance
	 */
	public Instance fetchInstance(Serializable instanceId, String instanceType) {
		Instance instance;
		InstanceReference reference = typeConverter.convert(InstanceReference.class, instanceType);
		reference.setIdentifier(instanceId.toString());
		instance = reference.toInstance();
		return instance;
	}
}
