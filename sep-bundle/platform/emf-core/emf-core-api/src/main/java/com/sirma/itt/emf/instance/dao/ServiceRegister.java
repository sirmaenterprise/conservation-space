package com.sirma.itt.emf.instance.dao;

import java.util.Set;

import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * Service that provides a common access to services and call backs by instance or definition. The
 * register provides an extension {@link InstanceToServiceRegisterExtension} that can be used add
 * additional support.
 * 
 * @author BBonev
 */
public interface ServiceRegister {

	/**
	 * Gets the instance service that handles the given object or <code>null</code>.
	 * 
	 * @param <I>
	 *            the instance type
	 * @param <D>
	 *            the definition type
	 * @param object
	 *            the object
	 * @return the instance service
	 */
	<I extends Instance, D extends DefinitionModel> InstanceService<I, D> getInstanceService(
			Object object);

	/**
	 * Gets the instance DAO that handles the given object or <code>null</code>.
	 * 
	 * @param <I>
	 *            the instance type
	 * @param object
	 *            the object
	 * @return the instance dao
	 */
	<I extends Instance> InstanceDao<I> getInstanceDao(Object object);

	/**
	 * Gets the property model callback used to persist properties for the given object or
	 * <code>null</code>.
	 * 
	 * @param <P>
	 *            the model type
	 * @param object
	 *            the object
	 * @return the model callback
	 */
	<P extends PropertyModel> PropertyModelCallback<P> getModelCallback(Object object);

	/**
	 * Gets the definition accessor used to access the definitions represented by the given object
	 * or <code>null</code>
	 * <p>
	 * <b>NOTE:</b> The method should return <code>null</code> if the instance does not support
	 * definition.
	 * 
	 * @param object
	 *            the object
	 * @return the definition accessor
	 */
	DefinitionAccessor getDefinitionAccessor(Object object);

	/**
	 * Gets the definition compiler callback that loads the definitions of the given object or
	 * <code>null</code>
	 * <p>
	 * <b>NOTE:</b> The method should return <code>null</code> if the instance does not support
	 * definition.
	 * 
	 * @param <T>
	 *            the definition type
	 * @param object
	 *            the object
	 * @return the compiler callback
	 */
	<T extends TopLevelDefinition> DefinitionCompilerCallback<T> getCompilerCallback(Object object);

	/**
	 * Gets the event provider for the given {@link Instance} object or {@link Instance} class.
	 * 
	 * @param <I>
	 *            the concrete instance type
	 * @param object
	 *            the object
	 * @return the event provider if any or <code>null</code> if not supported
	 */
	<I extends Instance> InstanceEventProvider<I> getEventProvider(Object object);

	/**
	 * Gets the list of supported objects.
	 * 
	 * @return the supported objects
	 */
	Set<Class<?>> getSupportedObjects();

	/**
	 * Gets the list of the valid registered extension.
	 * 
	 * @return the extension
	 */
	Set<InstanceToServiceRegisterExtension> getExtensions();
}
