/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Concrete implementation of the InstanceLoadDecorator. The implementation updates the downloads status in the instance
 * headers of the each passed instance. This extension is collected and called in ChaningInstanceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceLoadDecorator.TARGET_NAME, enabled = false, order = 30)
public class DownloadsServiceInstanceLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private DownloadsService downloadsService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		downloadsService.updateDownloadStateForInstance(instance);
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		downloadsService.updateDownloadStateForInstances(collection);
	}
}
