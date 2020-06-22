package com.sirma.itt.seip.eai.cs.service.communication.request;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.eai.cs.EAIServicesConstants;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.model.request.CSRetrieveRequest;
import com.sirma.itt.seip.eai.cs.model.request.CSSearchRequest;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.ResultPaging;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchFormCriterion;
import com.sirma.itt.seip.eai.model.request.ResultOrdering;
import com.sirma.itt.seip.eai.model.request.query.RawQuery;
import com.sirma.itt.seip.eai.model.request.query.RawQueryEntry;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationServiceAdapter;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProviderAdapter;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * {@link CSRequestProviderAdapter} is the base class to provide CS specific {@link ServiceRequest} with all the data
 * converted and prepared for {@link EAICommunicationServiceAdapter}.
 *
 * @author bbanchev
 */
public abstract class CSRequestProviderAdapter implements EAIRequestProviderAdapter {
	@Inject
	protected ModelService modelService;

	@SuppressWarnings("unchecked")
	@Override
	public <R extends ServiceRequest> R buildRequest(EAIServiceIdentifier service, Object sourceArgument)
			throws EAIException {
		if (BaseEAIServices.SEARCH.equals(service)) {
			return (R) buildSearchRequest(sourceArgument);
		} else if (BaseEAIServices.RETRIEVE.equals(service)) {
			return (R) buildRetrieveRequest(sourceArgument);
		}
		throw new EAIException("Not implemented service: " + service);
	}

	protected CSSearchRequest buildSearchRequest(Object sourceArgument) throws EAIException {
		if (!(sourceArgument instanceof SearchArguments)) {
			throw new EAIRuntimeException(
					"Search request should be constructed by a " + SearchArguments.class.getSimpleName()
							+ " argument with a " + Condition.class.getSimpleName() + " query! ");
		}
		@SuppressWarnings("unchecked")
		SearchArguments<? extends Instance> searchArguments = (SearchArguments<? extends Instance>) sourceArgument;
		CSSearchRequest search = initNewSearchRequest(searchArguments);
		search.setQuery(searchTreeToQuery(searchArguments));
		search.setIncludeReferences(getIncludeReferencesParameter(searchArguments.getArguments()));
		search.setIncludeThumbnails(getIncludeThumbnailsParameter(searchArguments.getArguments()));
		search.setInstantiateMissing(getInstantiateMissingParameter(searchArguments.getArguments()));
		return search;
	}

	protected <E extends Instance, S extends SearchArguments<E>> CSSearchRequest initNewSearchRequest(S arguments)
			throws EAIModelException {
		CSSearchRequest request = new CSSearchRequest();
		// set paging
		ResultPaging paging = new ResultPaging();
		paging.setLimit(arguments.getPageSize());
		paging.setSkip(arguments.getSkipCount());
		request.setPaging(paging);
		// set ordering
		request.setOrdering(arguments
				.getSorters()
					.stream()
					.map(e -> new ResultOrdering(e.isAscendingOrder(),
							getSingleConverterdValueCriteria(e.getSortField(), null).getFirst()))
					.collect(Collectors.toList()));

		return request;
	}

	@SuppressWarnings("unchecked")
	protected CSRetrieveRequest buildRetrieveRequest(Object sourceArgument) {
		if (!(sourceArgument instanceof Collection)) {
			throw new EAIRuntimeException("Retrieve request should be constructed by a Collection<"
					+ CSExternalInstanceId.class.getSimpleName() + "> of uid of objects in the external system!");
		}
		CSRetrieveRequest csImportRequest = new CSRetrieveRequest();
		csImportRequest.setExternalIds((Collection<CSExternalInstanceId>) sourceArgument);
		return csImportRequest;
	}

	/**
	 * Convert internal property to external property as {@link EntityPropertyMapping#AS_CRITERIA} or throws
	 * {@link EAIRuntimeException} on error
	 * 
	 * @param internalName
	 *            is the internal uri as returned by {@link EntityProperty#getUri()}
	 * @param value
	 *            is the value to convert. might be null
	 * @return the first mapping result or throws {@link EAIRuntimeException} on missing conversion or during failure
	 */
	protected Pair<String, Serializable> getSingleConverterdValueCriteria(String internalName, Serializable value) {
		EAIModelConverter modelConverter = modelService.provideModelConverter(getName());
		EntitySearchCriterion criteria = modelService
				.getSearchConfiguration(getName())
					.getCriterionByInternalName(internalName);
		if (!(criteria instanceof EntitySearchFormCriterion)
				|| ((EntitySearchFormCriterion) criteria).getMapping() == null) {
			throw new EAIRuntimeException("Missing search criteria mapping for " + internalName + "!");
		}
		try {
			List<Pair<String, Serializable>> externalProperties = modelConverter
					.convertSEIPtoExternalProperty(criteria.getPropertyId(), value, null);
			if (externalProperties.isEmpty()) {
				throw new EAIRuntimeException("Missing conversion mapping for " + internalName + "!");
			}
			// the key is the mapping in external system
			return new Pair<>(((EntitySearchFormCriterion) criteria).getMapping(),
					externalProperties.get(0).getSecond());
		} catch (EAIModelException e) {
			throw new EAIRuntimeException("Failed to convert criteria: " + internalName, e);
		}
	}

	protected <E extends Instance, S extends SearchArguments<E>> RawQuery searchTreeToQuery(S arguments)
			throws EAIModelException {

		RawQuery rawQuery = new RawQuery();
		processCondition(arguments.getCondition(), arguments.getContext(), rawQuery);

		return rawQuery;
	}

	private RawQuery processCondition(Condition searchTree, String systemId, RawQuery rawQuery)
			throws EAIModelException {
		List<SearchNode> rules = searchTree.getRules();
		if (rules == null || rules.isEmpty()) {
			return rawQuery;
		}
		Iterator<SearchNode> iterator = rules.iterator();
		while (iterator.hasNext()) {
			SearchNode searchTreeEntry = iterator.next();
			appendQueryEntry(searchTreeEntry, systemId, rawQuery);
		}
		return rawQuery;
	}

	@SuppressWarnings("unchecked")
	protected void appendQueryEntry(SearchNode sourceQueryTree, String systemId, RawQuery rawQuery)
			throws EAIModelException {
		if (sourceQueryTree instanceof Condition) {
			processCondition((Condition) sourceQueryTree, systemId, rawQuery);
		} else if (sourceQueryTree instanceof Rule) {
			Rule sourceData = (Rule) sourceQueryTree;
			if (EqualsHelper.nullSafeEquals("types", sourceData.getField())) {
				return;
			}
			RawQueryEntry readValue = new RawQueryEntry();
			Pair<String, Serializable> externalProperty = getSingleConverterdValueCriteria(sourceData.getField(),
					(Serializable) sourceData.getValues());
			readValue.setProperty(externalProperty.getFirst());
			readValue.setOperator(sourceData.getOperation());
			if (externalProperty.getSecond() instanceof Collection<?>) {
				readValue.setValues((Collection<Object>) externalProperty.getSecond());
			} else {
				readValue.setValues(Collections.singletonList(externalProperty.getSecond()));
			}
			rawQuery.addEntry(readValue);
		}
	}

	/**
	 * By default activate the include references flag - {@link EAIServicesConstants#SEARCH_INCLUDE_REFERENCES}
	 * 
	 * @param arguments
	 *            to search in for flag configuration
	 * @return true to activate the flag
	 */
	protected static boolean getIncludeReferencesParameter(Map<String, Serializable> arguments) {
		// by default true
		if (arguments == null || arguments.get(EAIServicesConstants.SEARCH_INCLUDE_REFERENCES) == null) {
			return true;
		}
		return Boolean.parseBoolean(String.valueOf(arguments.get(EAIServicesConstants.SEARCH_INCLUDE_REFERENCES)));
	}

	/**
	 * By default activate the include thumbnails flag - {@link EAIServicesConstants#SEARCH_INCLUDE_THUMBNAILS}
	 * 
	 * @param arguments
	 *            to search in for flag configuration
	 * @return true to activate the flag
	 */
	protected static boolean getIncludeThumbnailsParameter(Map<String, Serializable> arguments) {
		if (arguments == null || arguments.get(EAIServicesConstants.SEARCH_INCLUDE_THUMBNAILS) == null) {
			return false;
		}
		return Boolean.parseBoolean(String.valueOf(arguments.get(EAIServicesConstants.SEARCH_INCLUDE_THUMBNAILS)));
	}

	/**
	 * By default activate the instantiate missing flag -
	 * {@link EAIServicesConstants#SEARCH_INSTANTIATE_MISSING_INSTANCES}
	 * 
	 * @param arguments
	 *            to search in for flag configuration
	 * @return true to activate the flag
	 */
	protected static boolean getInstantiateMissingParameter(Map<String, Serializable> arguments) {
		if (arguments == null || arguments.get(EAIServicesConstants.SEARCH_INSTANTIATE_MISSING_INSTANCES) == null) {
			return false;
		}
		return Boolean
				.parseBoolean(String.valueOf(arguments.get(EAIServicesConstants.SEARCH_INSTANTIATE_MISSING_INSTANCES)));
	}

}