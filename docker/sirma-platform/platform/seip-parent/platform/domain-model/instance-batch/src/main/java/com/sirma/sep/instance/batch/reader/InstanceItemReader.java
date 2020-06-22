package com.sirma.sep.instance.batch.reader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;

/**
 * Generic {@link javax.batch.api.chunk.ItemReader} that produces loaded instances with their properties.<br>
 * The chunk size of the reader should be passed in the batch configuration xml.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
@Named
public class InstanceItemReader extends BaseItemReader<Instance> {
	@Inject
	private InstanceService instanceService;

	@Override
	protected void loadBatchData(Collection<String> instanceIds, BiConsumer<String, Instance> onLoadedItem) {
		List<Serializable> ids = new ArrayList<>(instanceIds.size());
		ids.addAll(instanceIds);
		instanceService.loadByDbId(ids).forEach(instance -> onLoadedItem.accept(instance.getId().toString(), instance));
	}
}
