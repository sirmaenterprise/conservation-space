package com.sirma.itt.emf.label.retrieve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.StateServiceExtension;

/**
 * Retrieves State labels based on the type of the object and the state.
 * 
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 1)
public class ObjectStateFieldValueRetriever extends PairFieldValueRetriever {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<String>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECTSTATE);
	}
	/** The services extension points. */
	@Inject
	@ExtensionPoint(StateServiceExtension.TARGET_NAME)
	private Iterable<StateServiceExtension<Instance>> services;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private StateService stateService;

	private static Set<Integer> stateCodeLists;

	/**
	 * Inits the state codelists.
	 */
	@PostConstruct
	private void init() {
		stateCodeLists = getAllPrimaryStateCodelists();
	}

	/**
	 * Retrieves the state label based on the state and the object type. The object type must be a
	 * full semantic uri.
	 * 
	 * @param value
	 *            the value
	 * @return the label
	 */
	@Override
	public String getLabel(String... value) {
		if (!ArrayUtils.isEmpty(value) && value[0] != null && value[1] != null) {
			DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(value[1]);
			Class clazz = definition.getJavaClass();
			int codelist;
			if (definition.getJavaClassName().equals(
					"com.sirma.itt.objects.domain.model.ObjectInstance")
					|| definition.getJavaClassName().equals(
							"com.sirma.itt.objects.domain.model.SavedFilter")) {
				// TODO: workaround to get ProjectInstance
				Class projectClass = dictionaryService.getDataTypeDefinition("projectinstance")
						.getJavaClass();
				codelist = stateService.getPrimaryStateCodelist(projectClass);
			} else {
				codelist = stateService.getPrimaryStateCodelist(clazz);
			}
			if (codelist != 0) {
				CodeValue codeValue = codelistService.getCodeValue(codelist, value[0]);
				if (codeValue != null) {
					return codeValue.getProperties().get(getCurrentUserLanguage()).toString();
				}
			}
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		offset = offset != null ? offset : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();
		Set<String> codeValueIds = new HashSet<>();

		for (Integer codeListId : stateCodeLists) {
			Map<String, CodeValue> codeValues = codelistService.getCodeValues(codeListId);
			if (codeValues != null) {
				for (CodeValue codeValue : codeValues.values()) {
					Map<String, Serializable> properties = codeValue.getProperties();

					if (properties != null) {
						Serializable label = properties.get(getCurrentUserLanguage());
						if (label != null) {
							String codeValueValue = label.toString();
							if (StringUtils.isNullOrEmpty(codeValueValue)) {
								codeValueValue = codeValue.getValue();
							}
							if (codeValueIds.add(codeValue.getValue())) {
								if (StringUtils.isNullOrEmpty(filter)
										|| codeValueValue.toLowerCase().startsWith(
												filter.toLowerCase())) {
									validateAndAddPair(results, codeValue.getValue(),
											codeValueValue, filter, offset, limit, total);
									total++;
								}

							}
						}
					}
				}
			}

		}
		return new RetrieveResponse(total, results);
	}

	/**
	 * Gets the all primary state codelists.
	 * 
	 * @return the all primary state codelists
	 */
	private Set<Integer> getAllPrimaryStateCodelists() {
		Set<Integer> primaryStates = new HashSet<>();
		for (StateServiceExtension<Instance> servic : services) {
			primaryStates.add(servic.getPrimaryStateCodelist());
		}
		return primaryStates;
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
