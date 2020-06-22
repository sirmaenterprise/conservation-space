package com.sirmaenterprise.sep.roles;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.permissions.model.RoleInstance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider for the security configurations
 *
 * @author BBonev
 */
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 100)
public class SecurityDefinitionManagementServiceExtension implements DefinitionManagementServiceExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@SuppressWarnings("rawtypes")
	private static final List<Class> SUPPORTED_OBJECTS = Arrays.asList(RoleInstance.class, RoleDefinition.class);

	@Inject
	private Instance<DefintionAdapterService> adapterService;

	@Override
	@SuppressWarnings("rawtypes")
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (adapterService.isUnsatisfied()) {
			LOGGER.info("Disabled security definition loading. No DMS integeration installed");
			return Collections.emptyList();
		}
		try {
			return adapterService.get().getDefinitions(definitionClass);
		} catch (Exception e) {
			LOGGER.error("Failed to recieve definition type : {} from DMS due to:", definitionClass, e);
		}
		return Collections.emptyList();
	}

}
