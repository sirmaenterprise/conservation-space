package com.sirma.sep.instance.batch;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.batch.api.chunk.ItemWriter;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.util.CDI;

/**
 * Generic {@link ItemWriter} that acts as a proxy for other {@code ItemWriter} passed as batch job parameter
 * under the name {@value #WRITER_NAME}. If no such parameters is passed then instance creation will fail.<br>
 * For convenience the method {@link BatchRequestBuilder#customJob(String, String, String, Collection)} should
 * be used.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@Named
public class BeanItemWriter implements ItemWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String WRITER_NAME = "itemWriter";

	@Inject
	private JobContext context;
	@Inject
	private BatchProperties batchProperties;
	private ItemWriter itemWriter;

	@PostConstruct
	void init() {
		String beanName = batchProperties.getJobProperty(context.getExecutionId(), WRITER_NAME);
		if (StringUtils.isBlank(beanName)) {
			throw new IllegalArgumentException("'" + WRITER_NAME + "' property not configured!");
		}
		itemWriter = CDI.instantiateBean(beanName, ItemWriter.class, CDI.getCachedBeanManager());
		if (itemWriter == null) {
			throw new IllegalArgumentException("Bean with name '" + beanName + "' not found!");
		}
		LOGGER.debug("Instantiated batch item writer: {}", beanName);
	}

	@Override
	public void open(Serializable checkpoint) throws Exception {
		itemWriter.open(checkpoint);
	}

	@Override
	public void close() throws Exception {
		itemWriter.close();
	}

	@Override
	public void writeItems(List<Object> items) throws Exception {
		itemWriter.writeItems(items);
	}

	@Override
	public Serializable checkpointInfo() throws Exception {
		return itemWriter.checkpointInfo();
	}
}
