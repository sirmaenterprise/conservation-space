package com.sirma.itt.seip.template;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Provides commonly used functionality for managing template instances.
 * 
 * @author Vilizar Tsonev
 */
@Singleton
public class TemplateInstanceHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DomainInstanceService domainInstanceService;

	/**
	 * Sets the provided template instance to be secondary (alternative).
	 * 
	 * @param templateToDemote
	 *            is the template instance to demote
	 * @param newPrimaryTemplateId
	 *            the ID of the new primary template instance
	 */
	public void demoteInstance(Instance templateToDemote, String newPrimaryTemplateId) {
		LOGGER.debug("Demoting template instance [{}] to be secondary (alternative). The new primary is [{}]",
				templateToDemote.getId(), newPrimaryTemplateId);
		templateToDemote.add(TemplateProperties.IS_PRIMARY_TEMPLATE, Boolean.FALSE);
		domainInstanceService.save(InstanceSaveContext.create(templateToDemote, Operation.NO_OPERATION));
	}

}
