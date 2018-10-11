package com.sirma.itt.seip.instance.headers.batch;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.headers.InstanceHeaderService;

/**
 * Batch {@link ItemProcessor} that generates the instance header based on the provided instance data.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
@Named
public class InstanceHeaderItemProcessor implements ItemProcessor {

	@Inject
	private InstanceHeaderService instanceHeaderService;

	@Override
	public Object processItem(Object item) throws Exception {
		Instance instance = (Instance) item;
		return instanceHeaderService.evaluateHeader(instance)
				.map(header -> new GeneratedHeaderData(instance, header))
				.orElse(null);
	}
}
