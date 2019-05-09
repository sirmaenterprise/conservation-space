/**
 *
 */
package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.DEFAULT_HEADERS;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Instance decorator that builds instance headers from a summary field.
 *
 * @author BBonev
 */
// Should be executed last but not to be last last
@Extension(target = InstanceLoadDecorator.INSTANCE_DECORATOR, order = InstanceLoadDecorator.MAX_ORDER - 1000)
@Extension(target = InstanceLoadDecorator.VERSION_INSTANCE_DECORATOR, order = InstanceLoadDecorator.MAX_ORDER - 1000)
public class HeadersLoadDecorator implements InstanceLoadDecorator {

	private static final String[] ALL_HEADERS = CollectionUtils.toArray(DEFAULT_HEADERS, String.class);

	@Inject
	private HeadersService headersService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		// when loading a single instance eval all headers including tooltip
		headersService.generateInstanceHeaders(instance, ALL_HEADERS);
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		// here tooltip header is not loaded because when batch loading that header is not shown
		collection.forEach(instance -> headersService.generateInstanceHeaders(instance));
	}

	@Override
	public boolean allowParallelProcessing() {
		return false;
	}
}