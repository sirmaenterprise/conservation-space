package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Converts the object definition id to object label.
 * 
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 3)
public class ObjectTypeFieldValueRetriever extends PairFieldValueRetriever {
	private static final String TITLE = "title";
	private static final String INSTANCE = "instance";
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<String>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECTTYPE);
	}
	@Inject
	private Instance<SemanticDefinitionService> semanticDefinitionService;

	@Override
	public String getLabel(String... value) {
		if (!ArrayUtils.isEmpty(value) && (value[0] != null)) {
			ClassInstance model = semanticDefinitionService.get().getClassInstance(value[0]);
			if (model != null) {
				return model.getLabel(getCurrentUserLanguage());
			}
			return null;
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		int localOffset = offset != null ? offset : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();
		List<ClassInstance> classes = semanticDefinitionService.get().getClasses();
		Set<ClassInstance> processed = CollectionUtils.createLinkedHashSet(classes.size());

		for (int i = 0; i < classes.size(); i++) {
			ClassInstance classInstance = classes.get(i);
			if (isSearchable(classInstance) && !processed.contains(classInstance)) {
				processed.add(classInstance);

				for (ClassInstance subInstance : classInstance.getSubClasses().values()) {
					if (isSearchable(subInstance)) {
						processed.add(subInstance);
					}
				}

				String text = (String) classInstance.getProperties().get(TITLE);
				String id = (String) classInstance.getProperties().get(INSTANCE);
				if (StringUtils.isNullOrEmpty(filter)
						|| text.toLowerCase().startsWith(filter.toLowerCase())) {
					validateAndAddPair(results, id, text, filter, localOffset, limit, total);
					total++;
				}
			}
		}
		return new RetrieveResponse(total, results);
	}

	/**
	 * Check if the instance is searcheable
	 * 
	 * @param classInstance
	 *            the class to check
	 * @return true if is searcheable
	 */
	private boolean isSearchable(ClassInstance classInstance) {
		return Boolean.TRUE == classInstance.getProperties().get("searchable");
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
