package com.sirma.itt.emf.definition.compile;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSTenantAdapterService;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Base definition management service implementation that provides default type definitions. The
 * implementation does not provide {@link DefinitionManagementService#getEnabledEmfContainers()}
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionManagementServiceImpl implements DefinitionManagementService {

	/** The extensions. */
	@Inject
	@ExtensionPoint(DefinitionManagementServiceExtension.TARGET_NAME)
	private Iterable<DefinitionManagementServiceExtension> extensions;

	/** The extension mapping. */
	private Map<Class<?>, Set<DefinitionManagementServiceExtension>> extensionMapping;

	@Inject
	private Instance<DMSTenantAdapterService> definitionService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefinitionManagementServiceImpl.class);

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		extensionMapping = CollectionUtils.createLinkedHashMap(50);
		for (DefinitionManagementServiceExtension extension : extensions) {
			for (Class<?> target : extension.getSupportedObjects()) {
				CollectionUtils.addValueToSetMap(extensionMapping, target, extension);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getEnabledEmfContainers() {
		if (!definitionService.isUnsatisfied()) {
			try {
				return definitionService.get().getEmfContainers();
			} catch (DMSException e) {
				LOGGER.warn("Failed to retrieve EMF containers from DMS", e);
			}
		}
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (definitionClass == null) {
			return Collections.emptyList();
		}
		Set<DefinitionManagementServiceExtension> set = extensionMapping.get(definitionClass);
		if ((set == null) || set.isEmpty()) {
			return Collections.emptyList();
		}

		if (set.size() == 1) {
			List<FileDescriptor> definitions = set.iterator().next().getDefinitions(definitionClass);
			return definitions == null ? Collections.EMPTY_LIST : definitions;
		}
		List<FileDescriptor> result = new LinkedList<FileDescriptor>();
		for (DefinitionManagementServiceExtension extension : set) {
			List<FileDescriptor> definitions = extension.getDefinitions(definitionClass);
			if (definitions != null) {
				result.addAll(definitions);
			}
		}
		return Collections.unmodifiableList(result);
	}

}
