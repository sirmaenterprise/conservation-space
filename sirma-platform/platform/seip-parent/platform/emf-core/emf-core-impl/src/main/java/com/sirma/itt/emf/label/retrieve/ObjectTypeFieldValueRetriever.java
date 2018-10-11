package com.sirma.itt.emf.label.retrieve;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Converts the object definition id to object label.
 *
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 3)
public class ObjectTypeFieldValueRetriever extends PairFieldValueRetriever {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String TITLE = DefaultProperties.TITLE;
	private static final String INSTANCE = "instance";

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECT_TYPE);
	}

	@Inject
	private Instance<SemanticDefinitionService> semanticDefinitionService;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null) {
			ClassInstance model = semanticDefinitionService.get().getClassInstance(value);
			if (model != null) {
				return model.getLabel(getCurrentUserLanguage());
			}
			return value;
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		int localOffset = offset != null ? offset.intValue() : 0;
		long total = 0;

		if (semanticDefinitionService.isUnsatisfied()) {
			LOGGER.warn("SemanticDefinitionService is unsatisfied.");
			return new RetrieveResponse(total, CollectionUtils.<Pair<String, String>> emptyList());
		}

		List<ClassInstance> classes = semanticDefinitionService.get().getClasses();
		Set<ClassInstance> processed = CollectionUtils.createLinkedHashSet(classes.size());

		// TODO: SIZE!
		List<Pair<String, String>> results = new ArrayList<>();

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
				if (StringUtils.isBlank(filter) || text.toLowerCase().startsWith(filter.toLowerCase())) {

					validateAndAddPair(results, id, text, filter, localOffset, limit, total);
					total++;
				}
			}
		}
		return new RetrieveResponse(total, results);
	}

	/**
	 * Check if the instance is searchable
	 *
	 * @param classInstance
	 *            the class to check
	 * @return true if is searchable
	 */
	private static boolean isSearchable(ClassInstance classInstance) {
		return classInstance.type().isSearchable();
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
