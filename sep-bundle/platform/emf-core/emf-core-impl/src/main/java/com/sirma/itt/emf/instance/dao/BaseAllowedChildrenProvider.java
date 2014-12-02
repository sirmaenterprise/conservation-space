package com.sirma.itt.emf.instance.dao;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Default implementation for the {@link AllowedChildrenProvider}.
 * 
 * @param <I>
 *            the base instance type
 * @author BBonev
 */
public class BaseAllowedChildrenProvider<I extends Instance> implements
		AllowedChildrenProvider<I> {

	/** The allowed children type provider. */
	protected AllowedChildrenTypeProvider allowedChildrenTypeProvider;

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
			AllowedChildrenTypeProvider allowedChildrenTypeProvider) {
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

}
