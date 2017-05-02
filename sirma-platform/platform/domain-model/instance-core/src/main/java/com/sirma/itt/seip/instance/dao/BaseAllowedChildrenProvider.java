package com.sirma.itt.seip.instance.dao;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.definition.AllowedChildrenProvider;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.DictionaryService;
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

	/** The allowed children type provider. */
	protected TypeMappingProvider allowedChildrenTypeProvider;

	/** The dictionary service. */
	protected DictionaryService dictionaryService;

	/**
	 * Instantiates a new base allowed children provider.
	 *
	 * @param dictionaryService
	 *            the dictionary service
	 * @param allowedChildrenTypeProvider
	 *            the allowed children type provider
	 */
	public BaseAllowedChildrenProvider(DictionaryService dictionaryService,
			TypeMappingProvider allowedChildrenTypeProvider) {
		this.dictionaryService = dictionaryService;
		this.allowedChildrenTypeProvider = allowedChildrenTypeProvider;
	}

	/**
	 * Gets the definition.
	 *
	 * @param <T>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @return the definition
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DefinitionModel> T getDefinition(I instance) {
		return (T) dictionaryService.getInstanceDefinition(instance);
	}

	/**
	 * Gets the definition.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the definition
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DefinitionModel> Class<T> getDefinition(String type) {
		return (Class<T>) allowedChildrenTypeProvider.getDefinitionClass(type);
	}

	/**
	 * Calculate active.
	 *
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	@Override
	public boolean calculateActive(I instance, String type) {
		return false;
	}

	/**
	 * Gets the active.
	 *
	 * @param <A>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type
	 * @return the active
	 */
	@Override
	public <A extends Instance> List<A> getActive(I instance, String type) {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DefinitionModel> List<T> getAllDefinitions(I instance, String type) {
		Class<DefinitionModel> definition = getDefinition(type);
		return (List<T>) dictionaryService.getAllDefinitions(definition);
	}

}
