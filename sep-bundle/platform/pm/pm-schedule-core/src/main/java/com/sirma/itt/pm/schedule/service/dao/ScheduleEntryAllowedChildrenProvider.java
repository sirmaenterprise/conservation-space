package com.sirma.itt.pm.schedule.service.dao;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Implementation of the {@link AllowedChildrenProvider} that is used for {@link ScheduleEntry} to
 * be able to calculate the allowed children for a specific parent. The instance is implemented to
 * be suited for
 * {@link com.sirma.itt.emf.instance.dao.AllowedChildrenHelper#getAllowedChildren(Instance, AllowedChildrenProvider, com.sirma.itt.emf.definition.DictionaryService)}
 * 
 * @author BBonev
 */
public class ScheduleEntryAllowedChildrenProvider extends BaseAllowedChildrenProvider<Instance>
		implements AllowedChildrenProvider<Instance> {

	/** The current children. */
	private Map<String, List<ScheduleEntry>> currentChildren = CollectionUtils.createHashMap(10);
	/** The type to class mapping. */
	private Map<String, Class<? extends DefinitionModel>> typeToClassMapping = CollectionUtils
			.createHashMap(10);

	/**
	 * Instantiates a new schedule entry allowed children provider.
	 * 
	 * @param children
	 *            the list of current children of the {@link ScheduleEntry} that is being
	 *            calculated.
	 * @param typeProvider
	 *            the type provider
	 * @param converter
	 *            a type converter instance
	 * @param dictionaryService
	 *            the dictionary service
	 */
	@SuppressWarnings("unchecked")
	public ScheduleEntryAllowedChildrenProvider(List<ScheduleEntry> children,
			AllowedChildrenTypeProvider typeProvider, TypeConverter converter,
			DictionaryService dictionaryService) {
		super(dictionaryService, typeProvider);

		for (ScheduleEntry entry : children) {
			InstanceReference reference = entry.getInstanceReference();
			// first we check if the given instance already has a type, if not we don't care
			if ((reference != null) && (reference.getReferenceType() != null)) {
				String name = reference.getReferenceType().getName();
				CollectionUtils.addValueToMap(currentChildren, name, entry);
				if (!typeToClassMapping.containsKey(name)) {
					typeToClassMapping.put(name, converter.convert(Class.class, reference));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean calculateActive(Instance instance, String type) {
		return currentChildren.containsKey(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <A extends Instance> List<A> getActive(Instance instance, String type) {
		List<ScheduleEntry> list = currentChildren.get(type);
		if (list == null) {
			return Collections.emptyList();
		}
		return (List<A>) list;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends DefinitionModel> T getDefinition(Instance instance) {
		if (instance instanceof ScheduleEntry) {
			ScheduleEntry entry = (ScheduleEntry) instance;
			// this is not a definition class but it will work anyway
			Class instanceClass = entry.getActualInstanceClass();
			return (T) dictionaryService.getDefinition(instanceClass, entry.getIdentifier());
		}
		return super.getDefinition(instance);
	}

}
