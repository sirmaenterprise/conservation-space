package com.sirma.itt.seip.instance.template.update;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.instance.batch.BatchProperties;

/**
 * Performs the actual instance update by updating the view and the template version of the instances.
 *
 * @author Adrian Mitev
 */
@Named
public class InstanceTemplateUpdateJobWriter extends AbstractItemWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private JobContext jobContext;

	@Inject
	private BatchProperties batchProperties;

	@Inject
	private InstanceTemplateUpdater instanceTemplateUpdater;

	@Inject
	private TransactionSupport transactionSupport;

	private String templateVersion;

	/**
	 * Initializes the writer.
	 */
	@PostConstruct
	public void init() {
		templateVersion = batchProperties.getJobProperty(jobContext.getExecutionId(),
				InstanceTemplateUpdateJobProperties.TEMPLATE_VERSION);
	}

	@Override
	public void writeItems(List<Object> items) throws Exception {
		TimeTracker timeTracker = TimeTracker.createAndStart();

		items.stream()
			.map(InstanceTemplateUpdateItem.class::cast)
			.forEach(item -> {
				try {
					transactionSupport.invokeInNewTx(() -> instanceTemplateUpdater.saveItem(item, templateVersion,
							ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE));
				} catch (RuntimeException e) {
					LOGGER.error("Skipping saving of instance [" + "item.getInstanceId()" + "]", e);
				}
		});

		LOGGER.debug("Updated template for {} instances in {} ms", Integer.valueOf(items.size()),
				Long.valueOf(timeTracker.stop()));
	}
}
