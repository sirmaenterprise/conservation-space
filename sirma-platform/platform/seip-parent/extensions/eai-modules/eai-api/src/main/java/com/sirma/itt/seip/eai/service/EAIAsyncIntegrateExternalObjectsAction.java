package com.sirma.itt.seip.eai.service;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;

/**
 * {@link EAIAsyncIntegrateExternalObjectsAction} uses a list of instances that are EAI prepared (all of them have the
 * correct set of metadata) and using the {@link IntegrateExternalObjectsService} import them.
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Named(EAIAsyncIntegrateExternalObjectsAction.NAME)
public class EAIAsyncIntegrateExternalObjectsAction extends SchedulerActionAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Action name. */
	public static final String NAME = "EAIAsyncIntegrateExternalObjectsAction";
	/** Collection of instances to import. */
	public static final String EAI_REQUEST_IDS = "requestedInstances";

	private static final List<Pair<String, Class<?>>> PARAM_VALIDATION = Collections
			.singletonList(new Pair<>(EAI_REQUEST_IDS, Collection.class));

	@Inject
	private IntegrateExternalObjectsService integrationService;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		Collection<InstanceReference> request = context.getIfSameType(EAI_REQUEST_IDS, Collection.class);
		LOGGER.debug("Executing import on {} ", request);
		Collection<Instance> importInstances = integrationService
				.importInstances(request.stream().map(InstanceReference::toInstance).collect(Collectors.toList()));
		LOGGER.debug("Import completed. Result: {} ", importInstances);
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return PARAM_VALIDATION;
	}

}
