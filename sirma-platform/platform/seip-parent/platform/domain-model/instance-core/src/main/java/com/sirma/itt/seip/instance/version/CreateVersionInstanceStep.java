package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATED_ON;

import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles creation of version instance. The primary job of this step is to create the new instance for the version so
 * that we could change it safely. This includes changes over properties and other things. This way we avoid to do so
 * over the target instance, because it causes problems, because it is used as result form the whole save/version
 * process.
 *
 * @author A. Kunchev
 */
@Extension(target = VersionStep.TARGET_NAME, enabled = true, order = 13)
public class CreateVersionInstanceStep implements VersionStep {

	@Inject
	private TypeConverter typeConverter;

	@Override
	public String getName() {
		return "createVersionInstance";
	}

	@Override
	public void execute(VersionContext context) {
		Instance instance = context.getTargetInstance();
		instance.add(VERSION_CREATED_ON, context.getCreationDate());
		ArchivedInstance version = typeConverter.convert(ArchivedInstance.class, instance);
		context.setVersionInstance(version);
	}
}