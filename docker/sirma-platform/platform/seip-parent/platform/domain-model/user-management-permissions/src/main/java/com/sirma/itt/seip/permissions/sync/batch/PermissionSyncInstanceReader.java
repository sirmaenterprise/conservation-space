package com.sirma.itt.seip.permissions.sync.batch;

import java.util.Collection;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.sep.instance.batch.reader.BaseItemReader;

/**
 * Chunk batch job reader that provides instances for permission synchronizations.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
@Named
public class PermissionSyncInstanceReader extends BaseItemReader<InstanceReference> {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	protected void loadBatchData(Collection<String> instanceIds, BiConsumer<String, InstanceReference> consumer) {
		instanceTypeResolver.resolveReferences(instanceIds).forEach(ref -> consumer.accept(ref.getId(), ref));
	}
}
