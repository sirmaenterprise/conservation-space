package com.sirma.itt.seip.instance.dao;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Extension that provides a means to accessing concrete server/dao/callback implementations that are specific on a
 * instance type.
 *
 * @author BBonev
 */
@Documentation("Extension that provides a means to accessing concrete server/dao/callback implementations that are specific on a instance type.")
public interface InstanceToServiceRegistryExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "instanceToServiceRegister";

	/**
	 * Gets the instance service that handles the instances for the extension.
	 *
	 * @return the instance service
	 */
	InstanceService getInstanceService();

	/**
	 * Gets the instance DAO that handles the instances/entities for the extension.
	 *
	 * @return the instance dao
	 */
	InstanceDao getInstanceDao();

	/**
	 * Gets the property model callback used to persist properties for the instance/entity for the extension.
	 *
	 * @param
	 * 			<P>
	 *            the model type
	 * @return the model callback
	 */
	<P extends PropertyModel> PropertyModelCallback<P> getModelCallback();

	/**
	 * Gets the event provider.
	 *
	 * @param <I>
	 *            the instance type
	 * @return the event provider
	 */
	<I extends Instance> InstanceEventProvider<I> getEventProvider();
}
