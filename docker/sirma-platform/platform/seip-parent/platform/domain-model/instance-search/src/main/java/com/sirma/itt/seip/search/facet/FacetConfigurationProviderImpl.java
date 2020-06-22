package com.sirma.itt.seip.search.facet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;

/**
 * Provides {@link FacetConfiguration} objects from parsing any compiled {@link GenericDefinition} of type
 * {@link ObjectType.FACET_CONFIG}.
 *
 * @author nvelkov
 * @author Mihail Radkov
 */
@ApplicationScoped
public class FacetConfigurationProviderImpl implements FacetConfigurationProvider {

	/**
	 * If some of the facet definitions is configured with this, it will be skipped from parsing.
	 */
	private static final Collection<DisplayType> HIDDEN_TYPES = new HashSet<>(
			Arrays.asList(DisplayType.HIDDEN, DisplayType.SYSTEM));

	// ObjectType.FACET_CONFIG.name() - this should be the value of the enum
	static final String FACET_CONFIG = "FACET_CONFIG";

	private static final String CONFIGURATION_FIELD_ID = "configuration";
	private static final String SORT_CONTROL_ID = "sort";
	private static final String ORDER_CONTROL_ID = "order";
	private static final String DEFAULT_CONTROL_ID = "default";
	private static final String PAGE_SIZE_CONTROL_ID = "pageSize";
	private static final String STATE_CONTROL_ID = "state";

	@Inject
	private DefinitionService definitionService;

	@Inject
	private ContextualMap<String, FacetConfiguration> facetConfigurations;

	@Override
	public FacetConfiguration getFacetConfigField(String facetName) {
		return get(facetName);
	}

	/**
	 * Observes the event fired after definition loading to trigger property reloading. This method is invoked
	 * <b>ONLY</b> if the service has been initialized to avoid double reloading.
	 *
	 * @param event
	 *            - the reload event
	 */
	public void onDefinitionReload(@Observes(notifyObserver = Reception.IF_EXISTS) DefinitionsChangedEvent event) {
		facetConfigurations.reset();
	}

	/**
	 * Reloads the configurations once the bean has been constructed.
	 */
	@PostConstruct
	public void init() {
		facetConfigurations.initializeWith(this::reloadConfigurations);
	}

	/**
	 * Reloads all facet configurations and repopulates the facet config map.
	 */
	@SuppressWarnings("squid:UnusedPrivateMethod")
	private Map<String, FacetConfiguration> reloadConfigurations() {
		return getFacetConfigDefinitions()
				.map(FacetConfigurationProviderImpl::buildFacetConfiguration)
					.filter(Objects::nonNull)
					.collect(CollectionUtils.toIdentityMap(FacetConfiguration::getName));
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private static FacetConfiguration buildFacetConfiguration(GenericDefinition facetConfigDefinition) {
		return facetConfigDefinition
				.getFields()
					.stream()
					.filter(property -> CONFIGURATION_FIELD_ID.equalsIgnoreCase(property.getName()))
					.filter(property -> !HIDDEN_TYPES.contains(property.getDisplayType()))
					.map(FacetConfigurationProviderImpl::toFacetConfiguration)
					.findFirst()
					.orElseGet(() -> null);
	}

	private static FacetConfiguration toFacetConfiguration(PropertyDefinition property) { // NOSONAR
		FacetConfiguration facetConfiguration = new FacetConfiguration();
		facetConfiguration.setName(PathHelper.getRootPath(property));
		facetConfiguration.setOrder(property.getOrder());
		facetConfiguration.setLabel(property.getLabel());
		ControlDefinition control = property.getControlDefinition();
		if (control != null) {
			ControlParam sortParam = (ControlParam) control.getChild(SORT_CONTROL_ID);
			ControlParam orderParam = (ControlParam) control.getChild(ORDER_CONTROL_ID);
			ControlParam defaultParam = (ControlParam) control.getChild(DEFAULT_CONTROL_ID);
			ControlParam pageSizeParam = (ControlParam) control.getChild(PAGE_SIZE_CONTROL_ID);
			ControlParam stateParam = (ControlParam) control.getChild(STATE_CONTROL_ID);
			if (pageSizeParam != null && pageSizeParam.getValue() != null) {
				facetConfiguration.setPageSize(Integer.parseInt(pageSizeParam.getValue()));
			}
			if (sortParam != null && orderParam != null) {
				facetConfiguration.setSort(sortParam.getValue());
				facetConfiguration.setSortOrder(orderParam.getValue());
				if (defaultParam != null) {
					facetConfiguration.setDefault(Boolean.parseBoolean(defaultParam.getValue()));
				}
			}
			if (stateParam != null) {
				facetConfiguration.setState(stateParam.getValue());
			}
		}
		return facetConfiguration;
	}

	/**
	 * Load all generic definition and filter the ones that are facetConfigs, then return the one that isn't abstract or
	 * if there is more than one, return null.
	 *
	 * @return the non-abstract generic definition
	 */
	private Stream<GenericDefinition> getFacetConfigDefinitions() {
		List<GenericDefinition> genericDefinitions = definitionService.getAllDefinitions(GenericDefinition.class);

		return genericDefinitions.stream().filter(d -> FACET_CONFIG.equalsIgnoreCase(d.getType()));
	}

	/**
	 * Get the {@link FacetConfiguration} from the facet config map while using a {@link ReentrantReadWriteLock}.
	 *
	 * @param the
	 *            key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
	 */
	private FacetConfiguration get(String key) {
		return facetConfigurations.get(key);
	}

	@Override
	public void reset() {
		facetConfigurations.reset();
	}
}
