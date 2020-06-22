package com.sirma.sep.instance.batch;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.batch.api.chunk.ItemReader;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.util.CDI;

/**
 * Generic {@link ItemReader} that acts as a proxy for other {@code ItemReader} passed as batch job parameter
 * under the name {@value #READER_NAME}. If no such parameters is passed then instance creation will fail.<br>
 * For convenience the method {@link BatchRequestBuilder#customJob(String, String, String, Collection)} should
 * be used.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@Named
public class BeanItemReader implements ItemReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String READER_NAME = "itemReader";

	@Inject
	private JobContext context;
	@Inject
	private BatchProperties batchProperties;

	private ItemReader itemReader;

	@PostConstruct
	void init() {
		String beanName = batchProperties.getJobProperty(context.getExecutionId(), READER_NAME);
		if (StringUtils.isBlank(beanName)) {
			throw new IllegalArgumentException("'" + READER_NAME + "' property not configured!");
		}
		itemReader = CDI.instantiateBean(beanName, ItemReader.class, CDI.getCachedBeanManager());
		if (itemReader == null) {
			throw new IllegalArgumentException("Bean with name '" + beanName + "' not found!");
		}
		LOGGER.debug("Instantiated batch item reader: {}", beanName);
	}

	@Override
	public void open(Serializable checkpoint) throws Exception {
		itemReader.open(checkpoint);
	}

	@Override
	public void close() throws Exception {
		itemReader.close();
	}

	@Override
	public Object readItem() throws Exception {
		return itemReader.readItem();
	}

	@Override
	public Serializable checkpointInfo() throws Exception {
		return itemReader.checkpointInfo();
	}
}
