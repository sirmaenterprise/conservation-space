package com.sirma.itt.seip.definition.compile;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Base definition management service implementation that provides default type definitions. The implementation does not
 * provide {@link DefinitionManagementService#getEnabledEmfContainers()}
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionManagementServiceImpl implements DefinitionManagementService {

	@Inject
	@ExtensionPoint(DefinitionManagementServiceExtension.TARGET_NAME)
	private Iterable<DefinitionManagementServiceExtension> extensions;

	private Map<Class<?>, Set<DefinitionManagementServiceExtension>> extensionMapping;

	@Inject
	private Instance<AdaptersConfiguration> adaptersConfig;

	/**
	 * Initializes the extension mapping.
	 */
	@PostConstruct
	public void initialize() {
		extensionMapping = CollectionUtils.createLinkedHashMap(50);
		for (DefinitionManagementServiceExtension extension : extensions) {
			for (Class target : extension.getSupportedObjects()) {
				CollectionUtils.addValueToSetMap(extensionMapping, target, extension);
			}
		}
	}

	@Override
	public Set<String> getEnabledEmfContainers() {
		if (!adaptersConfig.isUnsatisfied()) {
			return Collections.singleton(adaptersConfig.get().getDmsContainerId().get());
		}
		return Collections.emptySet();
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (definitionClass == null) {
			return Collections.emptyList();
		}
		Set<DefinitionManagementServiceExtension> set = extensionMapping.get(definitionClass);
		if (CollectionUtils.isEmpty(set)) {
			return Collections.emptyList();
		}

		if (set.size() == 1) {
			List<FileDescriptor> definitions;
			definitions = set.iterator().next().getDefinitions(definitionClass);
			return definitions == null ? Collections.emptyList() : definitions;
		}
		List<FileDescriptor> result = new LinkedList<>();
		for (DefinitionManagementServiceExtension extension : set) {
			List<FileDescriptor> definitions = extension.getDefinitions(definitionClass);
			if (definitions != null) {
				result.addAll(definitions);
			}
		}
		return Collections.unmodifiableList(result);
	}

}
