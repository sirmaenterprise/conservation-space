package com.sirma.itt.seip.eai.service;

import java.util.Collection;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.internal.DataIntegrationRequest;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The {@link IntegrateObjectsServiceAdapter} is responsible to process the {@link IntegrateExternalObjectsService}
 * subsequence request and to process them as required by a single integrated system. <br>
 * {@link #getName()} should match the system id that this adapter is responsible for
 */
public interface IntegrateObjectsServiceAdapter extends Plugin, Named {
	/** Plugin name. */
	String PLUGIN_ID = "IntegrateObjectsServiceAdapter";

	/**
	 * Resolve instance by its external id. Element is not stored or linked - no further resolving is taken
	 *
	 * @param resolvable
	 *            the resolvable instance to resolve
	 * @param persist
	 *            whether to persist the instance after resolve. Also this would link the instance to all instances that
	 *            already exist in the system and that are part of the persisted instance relation model.
	 * @return the instance that is resolved - possibly null
	 * @throws EAIException
	 *             on any error of the invoked service with detailed description
	 */
	<T extends ExternalInstanceIdentifier> Instance resolveInstance(T resolvable, boolean persist) throws EAIException;

	/**
	 * Extract some {@link ExternalInstanceIdentifier} if the adapter is applicable for that instance.
	 * 
	 * @param <T>
	 *            class the is known from the corresponding services for the system
	 * @param source
	 *            to test if the adapter is applicable for and to use as source data for the external id
	 * @return the generated id or null if the adapter is not applicable
	 * @throws EAIException
	 *             on any error during extract with detailed description
	 */
	<T extends ExternalInstanceIdentifier> T extractExternalInstanceIdentifier(Instance source) throws EAIException;

	/**
	 * Import instances represented as a list of externalIds. All instances are persisted and optionally (linkInstances)
	 * are linked. If linked instance is missing it is optionally resolved.
	 *
	 * @param externalIds
	 *            the external ids
	 * @param linkInstances
	 *            whether to create links and link the imported instances
	 * @param resolveLinks
	 *            whether to resolve link referred as {@link ResolvableInstance} in the intermediate model
	 * @return the collection of imported instances
	 * @throws EAIException
	 *             on any error of the invoked service with detailed description
	 */
	<T extends ExternalInstanceIdentifier> Collection<Instance> importInstances(Collection<T> externalIds,
			boolean linkInstances, boolean resolveLinks) throws EAIException;

	/**
	 * By default has the same behavior as {@link #importInstances(Collection, boolean, boolean)}.
	 * 
	 * @param <T>
	 *            is the import ids class, which is known from the corresponding adapter for that system
	 * @param request
	 *            is wrapper of all needed input parameters
	 * @return the collection of imported instances
	 * @throws EAIException
	 *             on any error of the invoked service with detailed description
	 */
	@SuppressWarnings("unchecked")
	default <T extends ExternalInstanceIdentifier, R> Collection<R> importInstances(DataIntegrationRequest<T> request)
			throws EAIException {
		return (Collection<R>) importInstances(request.getRequestData(), request.isLinkInstances(),
				request.isResolveLinks());
	}

	/**
	 * Checks if resolve is supported on the provided instance
	 *
	 * @param resolvable
	 *            the resolvable instance to check
	 * @return true, if is supported
	 */
	boolean isResolveSupported(ResolvableInstance resolvable);

}
