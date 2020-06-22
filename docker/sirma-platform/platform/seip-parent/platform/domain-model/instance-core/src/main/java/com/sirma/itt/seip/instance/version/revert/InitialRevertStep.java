package com.sirma.itt.seip.instance.version.revert;

import java.io.Serializable;
import java.util.Collections;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles preparing of data and {@link RevertContext} populating with the data needed to continue the revert process.
 * This step is responsible for loading the required instances, which includes the version with data should be used in
 * the process and the current instance, which data will be replaced. If any of this instances fails to load
 * {@link InstanceNotFoundException} will be thrown and the revert process will be stopped.<br>
 * For the result instance for the context is used clone of the version instance so that we do not modify directly the
 * version instance as we may need its data for something else at some point.
 *
 * @author A. Kunchev
 */
@Extension(target = RevertStep.EXTENSION_NAME, enabled = true, order = 10)
public class InitialRevertStep implements RevertStep {

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private InstanceService instanceService;

	@Override
	public String getName() {
		return "initRevert";
	}

	@Override
	public void invoke(RevertContext context) {
		// loads version or throws InstanceNotFoundException, if there is no version with such id
		Instance version = instanceVersionService.loadVersion(context.getVersionId());

		Serializable currentInstanceId = context.getCurrentInstanceId();
		Instance current = instanceTypeResolver
				.resolveReference(currentInstanceId)
					.map(InstanceReference::toInstance)
					.orElseThrow(() -> new InstanceNotFoundException(currentInstanceId));

		Instance resultInstance = instanceService.deepClone(version, context.getOperation());
		resultInstance.removeProperties(VersionProperties.get(Collections.singleton(VersionProperties.DEFINITION_ID)));
		context.setRevertResultInstance(resultInstance).setCurrentInstance(current);
	}
}