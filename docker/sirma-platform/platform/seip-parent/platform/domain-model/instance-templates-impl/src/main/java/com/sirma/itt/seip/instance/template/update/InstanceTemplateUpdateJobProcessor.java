package com.sirma.itt.seip.instance.template.update;

import javax.annotation.PostConstruct;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.sep.instance.batch.BatchProperties;

/**
 * Executes the merge algorithm and deterines whether an instance has to be udpdated of not.
 *
 * @author Adrian Mitev
 */
@Named
public class InstanceTemplateUpdateJobProcessor implements ItemProcessor {

	@Inject
	private JobContext jobContext;

	@Inject
	private BatchProperties batchProperties;

	@Inject
	private InstanceTemplateUpdater instanceTemplateUpdater;

	private String templateInstanceId;

	/**
	 * Initializes the processor.
	 */
	@PostConstruct
	public void init() {
		templateInstanceId = batchProperties.getJobProperty(jobContext.getExecutionId(),
				InstanceTemplateUpdateJobProperties.TEMPLATE_INSTANCE_ID);
	}

	@Override
	public Object processItem(Object item) throws Exception {
		String instanceId = (String) item;
		return instanceTemplateUpdater.updateItem(instanceId, templateInstanceId);
	}
}
