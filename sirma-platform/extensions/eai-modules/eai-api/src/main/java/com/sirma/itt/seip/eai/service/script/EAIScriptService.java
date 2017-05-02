package com.sirma.itt.seip.eai.service.script;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.eai.service.EAIAsyncIntegrateExternalObjectsAction;
import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Provider extension for external search.
 *
 * @author gshevkedov
 * @author bbanchev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 108)
public class EAIScriptService implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(EAIScriptService.class);

	@Inject
	private SearchService searchService;
	@Inject
	private IntegrateExternalObjectsService integrateExternalObjectService;
	@Inject
	private SchedulerService schedulerService;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("eaiScriptService", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Execute search with the provided arguments.
	 *
	 * @param searchArguments
	 *            the arguments to use
	 */
	public <E extends Instance> void search(SearchArguments<E> searchArguments) {
		searchService.search(Instance.class, searchArguments);
	}

	/**
	 * Import instances from external source using {@link IntegrateExternalObjectsService}
	 *
	 * @param items
	 *            the items to import
	 */
	public void importInstances(Collection<Instance> items) {
		integrateExternalObjectService.importInstances(items);
	}

	/**
	 * Execute search with the provided arguments.
	 *
	 * @param searchArguments
	 *            the arguments to use
	 */
	public void searchAndImport(SearchArguments<Instance> searchArguments) {
		List<List<InstanceReference>> importable = new LinkedList<>();

		int batchSize = searchArguments.getPageSize();
		// search to find the total and process first page
		searchAndAppendNextBatch(searchArguments, importable, 0);
		int totalItems = searchArguments.getTotalItems();
		// Console log for easy debug :)
		LOGGER.info("Instances to process: " + totalItems);
		// Check if there are instances (items) for update.
		if (totalItems > 0) {
			// Calculating batch count.
			int batches = totalItems / batchSize;
			batches += totalItems % batchSize > 0 ? 1 : 0;
			// Console log for easy debug.
			LOGGER.info("Update is split into: " + batches + " batches");
			// Iterating batches.
			for (int batchNumber = 1; batchNumber < batches; batchNumber++) {
				searchAndAppendNextBatch(searchArguments, importable, batchNumber);
			}
		}
		if (!importable.isEmpty()) {
			LOGGER.info("Processing {} with size {}", importable, importable.size());
			for (int i = 0; i < importable.size(); i++) {
				try {
					LOGGER.info("Scheduling batch {} with size {}", importable.get(i), importable.get(i).size());
					importAsynchInstances(importable.get(i), i);
				} catch (Exception e) {
					LOGGER.error("Skip batch {} due to error!", i, e);
				}
			}
		}
		LOGGER.info("Auto update has completed!");
	}

	private void searchAndAppendNextBatch(SearchArguments<Instance> searchArguments,
			List<List<InstanceReference>> importable, int batchNumber) {
		// Setting skip count - it used for skipping already processed items in previous pages.
		searchArguments.setSkipCount(batchNumber * searchArguments.getPageSize());

		// Triggering new search with additional search arguments - batchSize and skipCount
		try {
			search(searchArguments);
			importable.add(new ArrayList<>(searchArguments.getResult())
					.stream()
						.map(Instance::toReference)
						.collect(Collectors.toList()));
			// Console log for easy debug
			LOGGER.info("Added batch with index " + (batchNumber + 1) + "!");
		} catch (Exception e) {
			LOGGER.error("Skipping batch scheduling {} due to error!", batchNumber, e);
		}
	}

	private void importAsynchInstances(List<InstanceReference> result, Integer batchNumber) {
		if (result.isEmpty()) {
			LOGGER.warn("Skipping scheduled import of batch '{}' with empty instances {} ", batchNumber, result);
			return;
		}
		LOGGER.trace("Scheduling import of batch '{}' with instances {} ", batchNumber, result);
		// create the config
		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.TIMED)
					// it is better to retry single time currently since here it is not known at what interval is next
					// global schedule
					.setMaxRetryCount(1)
					// start each batch with offset of 30 sec
					.setScheduleTime(
							new Date(Calendar.getInstance().getTimeInMillis() + batchNumber.intValue() * 30000))
					.setPersistent(true)
					.setMaxActivePerGroup(EAIAsyncIntegrateExternalObjectsAction.NAME, 25)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setRemoveOnSuccess(true);
		configuration.setIdentifier("import_batch_" + batchNumber + "_" + UUID.randomUUID().toString());
		SchedulerContext context = new SchedulerContext();
		context.put(EAIAsyncIntegrateExternalObjectsAction.EAI_REQUEST_IDS, new ArrayList<>(result));
		schedulerService.schedule(EAIAsyncIntegrateExternalObjectsAction.NAME, configuration, context);
	}

}