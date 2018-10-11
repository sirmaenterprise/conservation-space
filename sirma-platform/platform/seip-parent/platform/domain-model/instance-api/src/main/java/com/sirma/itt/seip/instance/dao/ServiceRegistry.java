package com.sirma.itt.seip.instance.dao;

import java.util.Set;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;

/**
 * Service that provides a common access to services and call backs by instance or definition. The register provides an
 * extension {@link InstanceToServiceRegistryExtension} that can be used add additional support.
 *
 * @author BBonev
 */
public interface ServiceRegistry {

	/**
	 * Gets the instance service that handles the given object or <code>null</code>.
	 *
	 * @param object
	 *            the object
	 * @return the instance service
	 */
	InstanceService getInstanceService(Object object);

	/**
	 * Gets the instance DAO that handles the given object or <code>null</code>.
	 *
	 * @param object
	 *            the object
	 * @return the instance dao
	 */
	InstanceDao getInstanceDao(Object object);

	/**
	 * Gets the property model callback used to persist properties for the given object or <code>null</code>.
	 *
	 * @param
	 * 			<P>
	 *            the model type
	 * @param object
	 *            the object
	 * @return the model callback
	 */
	<P extends PropertyModel> PropertyModelCallback<P> getModelCallback(Object object);

	/**
	 * Gets the event provider for the given {@link Instance} object or {@link Instance} class.
	 *
	 * @param <I>
	 *            the concrete instance type
	 * @param object
	 *            the object
	 * @return the event provider if any or NOOP provider if not supported, but never <code>null</code>
	 */
	<I extends Instance> InstanceEventProvider<I> getEventProvider(Object object);

	/**
	 * Gets the list of supported objects.
	 *
	 * @return the supported objects
	 */
	Set<Class> getSupportedObjects();

	/**
	 * Gets the list of the valid registered extension.
	 *
	 * @return the extension
	 */
	Set<InstanceToServiceRegistryExtension> getExtensions();
}
