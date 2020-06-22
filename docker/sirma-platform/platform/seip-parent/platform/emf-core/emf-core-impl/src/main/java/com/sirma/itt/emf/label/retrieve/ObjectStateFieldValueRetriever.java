package com.sirma.itt.emf.label.retrieve;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Retrieves State labels based on the type of the object and the state.
 *
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 1)
public class ObjectStateFieldValueRetriever extends CodeListFieldValueRetriever {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECT_STATE);
	}

	/** The services extension points. */
	@Inject
	@ExtensionPoint(StateServiceExtension.TARGET_NAME)
	private Plugins<StateServiceExtension> services;

	@Inject
	private StateService stateService;

	@Inject
	private ContextualSet<Integer> stateCodeLists;

	@Inject
	private InstanceTypes instanceTypes;

	/**
	 * Inits the state codelists.
	 */
	@PostConstruct
	public void init() {
		stateCodeLists.initializeWith(this::getAllPrimaryStateCodelists);
	}

	/**
	 * Retrieves the state label based on the state and the object type. The object type must be a full semantic uri.
	 *
	 * @param value
	 *            the value
	 * @param additionalParameters
	 *            additional parameters: {@link FieldValueRetrieverParameters#OBJECTTYPE} - object type (full URI)
	 *            needed to obtain the state. <b>Required</b>
	 * @return the label
	 */
	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null && additionalParameters != null
				&& additionalParameters.get(FieldValueRetrieverParameters.OBJECTTYPE) != null) {
			String typeName = additionalParameters.getFirst(FieldValueRetrieverParameters.OBJECTTYPE);
			Optional<InstanceType> type = instanceTypes.from(typeName);
			if (!type.isPresent()) {
				LOGGER.warn("Could not find datatype: {}", typeName);
				return null;
			}
			int codelist = stateService.getPrimaryStateCodelist(type.get().getCategory());
			if (codelist != 0) {
				List<String> codeListIds = new ArrayList<>(1);
				codeListIds.add(String.valueOf(codelist));
				additionalParameters.getRequest().put(FieldValueRetrieverParameters.CODE_LIST_ID, codeListIds);
				return super.getLabel(value, additionalParameters);
			}
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		SearchRequest localRequest = additionalParameters;
		if (localRequest == null) {
			localRequest = new SearchRequest(CollectionUtils.createHashMap(stateCodeLists.size()));
		}
		for (Integer codeListId : stateCodeLists) {
			localRequest.add(FieldValueRetrieverParameters.CODE_LIST_ID, String.valueOf(codeListId));
		}
		return super.getValues(filter, localRequest, offset, limit);
	}

	/**
	 * Gets the all primary state codelists.
	 *
	 * @return the all primary state codelists
	 */
	@SuppressWarnings("squid:UnusedPrivateMethod")
	private Set<Integer> getAllPrimaryStateCodelists() {
		return services.stream().map(StateServiceExtension::getPrimaryStateCodelist).collect(Collectors.toSet());
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
