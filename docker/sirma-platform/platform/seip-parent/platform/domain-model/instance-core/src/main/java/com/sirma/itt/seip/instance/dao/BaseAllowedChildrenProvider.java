package com.sirma.itt.seip.instance.dao;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sirma.itt.seip.definition.AllowedChildrenProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Default implementation for the {@link AllowedChildrenProvider}.
 *
 * @param <I>
 *            the base instance type
 * @author BBonev
 */
public class BaseAllowedChildrenProvider<I extends Instance> implements AllowedChildrenProvider<I> {

	protected TypeMappingProvider allowedChildrenTypeProvider;

	protected DefinitionService definitionService;

	/**
	 * Instantiates a new base allowed children provider.
	 *
	 * @param definitionService
	 *            the definition service
	 * @param allowedChildrenTypeProvider
	 *            the allowed children type provider
	 */
	public BaseAllowedChildrenProvider(DefinitionService definitionService,
			TypeMappingProvider allowedChildrenTypeProvider) {
		this.definitionService = definitionService;
		this.allowedChildrenTypeProvider = allowedChildrenTypeProvider;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends DefinitionModel> T getDefinition(I instance) {
		return (T) definitionService.getInstanceDefinition(instance);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends DefinitionModel> Class<T> getDefinition(String type) {
		return (Class<T>) allowedChildrenTypeProvider.getDefinitionClass(type);
	}

	@Override
	public boolean calculateActive(I instance, String type) {
		return false;
	}

	@Override
	public <A extends Instance> List<A> getActive(I instance, String type) {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DefinitionModel> List<T> getAllDefinitions(I instance, String type) {
		return (List<T>) definitionService.getAllDefinitions(type).collect(Collectors.toList());
	}
}
