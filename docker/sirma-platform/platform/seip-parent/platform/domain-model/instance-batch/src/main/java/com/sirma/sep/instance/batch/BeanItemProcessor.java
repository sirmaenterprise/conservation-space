package com.sirma.sep.instance.batch;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.util.CDI;

/**
 * Generic {@link ItemProcessor} that acts as a proxy for other {@code ItemProcessor} passed as batch job parameter
 * under the name {@value #PROCESSOR_NAME}. If no such parameters is passed then instance creation will fail.<br>
 * For convenience the method {@link BatchRequestBuilder#customJob(String, String, String, Collection)} should
 * be used.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@Named
public class BeanItemProcessor implements ItemProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String PROCESSOR_NAME = "itemProcessor";

	@Inject
	private JobContext context;
	@Inject
	private BatchProperties batchProperties;

	private ItemProcessor itemProcessor;

	@PostConstruct
	void init() {
		String beanName = batchProperties.getJobProperty(context.getExecutionId(), PROCESSOR_NAME);
		if (StringUtils.isBlank(beanName)) {
			throw new IllegalArgumentException("'" + PROCESSOR_NAME + "' property not configured!");
		}
		itemProcessor = CDI.instantiateBean(beanName, ItemProcessor.class, CDI.getCachedBeanManager());
		if (itemProcessor == null) {
			throw new IllegalArgumentException("Bean with name '" + beanName + "' not found!");
		}
		LOGGER.debug("Instantiated batch item processor: {}", beanName);
	}

	@Override
	public Object processItem(Object item) throws Exception {
		return itemProcessor.processItem(item);
	}
}
