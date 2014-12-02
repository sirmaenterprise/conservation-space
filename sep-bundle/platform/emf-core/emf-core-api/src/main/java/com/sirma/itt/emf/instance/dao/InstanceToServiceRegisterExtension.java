package com.sirma.itt.emf.instance.dao;

import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.SupportablePlugin;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension that provides a means to accessing concrete server/dao/callback implementations that
 * are specific on a instance type.
 * 
 * @author BBonev
 */
@Documentation("Extension that provides a means to accessing concrete server/dao/callback implementations that are specific on a instance type.")
public interface InstanceToServiceRegisterExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "instanceToServiceRegister";

	/**
	 * Gets the instance service that handles the instances for the extension.
	 * 
	 * @param <I>
	 *            the instance type
	 * @param <D>
	 *            the definition type
	 * @return the instance service
	 */
	<I extends Instance, D extends DefinitionModel> InstanceService<I, D> getInstanceService();

	/**
	 * Gets the instance DAO that handles the instances/entities for the extension.
	 * 
	 * @param <I>
	 *            the instance type
	 * @return the instance dao
	 */
	<I extends Instance> InstanceDao<I> getInstanceDao();

	/**
	 * Gets the property model callback used to persist properties for the instance/entity for the
	 * extension.
	 * 
	 * @param <P>
	 *            the model type
	 * @return the model callback
	 */
	<P extends PropertyModel> PropertyModelCallback<P> getModelCallback();

	/**
	 * Gets the definition accessor used to access the definitions represented by the instance for
	 * the extension.
	 * <p>
	 * <b>NOTE:</b> The method should return <code>null</code> if the instance does not support
	 * definition.
	 * 
	 * @return the definition accessor
	 */
	DefinitionAccessor getDefinitionAccessor();

	/**
	 * Gets the definition compiler callback that loads the definitions of the instance definitions
	 * or the extension.
	 * <p>
	 * <b>NOTE:</b> The method should return <code>null</code> if the instance does not support
	 * definition.
	 * 
	 * @param <T>
	 *            the definition type
	 * @return the compiler callback
	 */
	<T extends TopLevelDefinition> DefinitionCompilerCallback<T> getCompilerCallback();

	/**
	 * Gets the event provider.
	 * 
	 * @param <I>
	 *            the instance type
	 * @return the event provider
	 */
	<I extends Instance> InstanceEventProvider<I> getEventProvider();
}
