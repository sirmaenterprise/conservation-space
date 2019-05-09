package com.sirma.itt.emf.semantic.label;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.definition.label.LabelResolver;
import com.sirma.itt.seip.domain.definition.label.LabelResolverProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Integration of semantic labels and tooltips in the {@link LabelProvider}. <br>
 * Provider for label resolvers that access the title and description of the semantic properties, relations and classes.
 * The implementation is based on the data returned from the {@link SemanticDefinitionService} and is stored in the
 * plugin instance and reset on {@link SemanticDefinitionsReloaded} event.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/10/2018
 */
@Singleton
@Extension(target = LabelResolverProvider.TARGET_NAME, order = 234)
public class SemanticPropertiesLabelResolverProvider implements LabelResolverProvider, MutationObservable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/*
	 * a cache that maps the known labels by language
	 */
	@Inject
	private ContextualMap<String, Map<String, String>> store;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private SecurityContext securityContext;

	private List<Executable> observers = new CopyOnWriteArrayList<>();

	@PostConstruct
	void init() {
		store.initializeWith(this::loadLabels);
	}

	private Map<String, Map<String, String>> loadLabels() {
		Map<String, Map<String, String>> langToValues = new HashMap<>();
		// object properties
		semanticDefinitionService.getRelations()
				.forEach(assignSemanticInstanceLabels(langToValues, DefaultProperties.TITLE, "definition"));
		// data and annotation properties
		semanticDefinitionService.getProperties()
				.forEach(assignSemanticInstanceLabels(langToValues, DefaultProperties.TITLE, "definition"));
		// all known classes
		semanticDefinitionService.getClasses()
				.forEach(assignSemanticInstanceLabels(langToValues, DefaultProperties.TITLE, "description"));
		return langToValues;
	}

	private Consumer<Instance> assignSemanticInstanceLabels(Map<String, Map<String, String>> langToValues,
			String titlePropertyName, String descriptionPropertyName) {
		return instance -> {
			String id = instance.getId().toString();

			Map<String, String> labels = instance.getAs(titlePropertyName, toMap(), Collections::emptyMap);
			labels.forEach(assignLabel(langToValues, LabelProvider.buildUriLabelId(id)));

			Map<String, String> descriptions = instance.getAs(descriptionPropertyName, toMap(), Collections::emptyMap);
			descriptions.forEach(assignLabel(langToValues, LabelProvider.buildUriTooltipId(id)));
		};
	}

	@SuppressWarnings("unchecked")
	private Function<Serializable, Map<String, String>> toMap() {
		return value -> {
			if (value instanceof Map) {
				return (Map<String, String>) value;
			} else if (value instanceof MultiLanguageValue) {
				Map<String, String> langToValue = new HashMap<>();
				((MultiLanguageValue) value).forEach(langToValue::put);
				return langToValue;
			} else if (value instanceof String) {
				return Collections.singletonMap("en", value.toString());
			}
			return Collections.emptyMap();
		};
	}

	private BiConsumer<String, String> assignLabel(Map<String, Map<String, String>> langToValues, String labelid) {
		return (language, value) ->
				langToValues.computeIfAbsent(language, l -> new HashMap<>()).put(labelid, value);
	}

	@Override
	public LabelResolver getLabelResolver(Locale locale) {
		if (securityContext.isSystemTenant()) {
			return null;
		}
		if (!store.containsKey(locale.getLanguage())) {
			return null;
		}
		return LabelResolver.wrap(store.get(locale.getLanguage()));
	}

	void onModelChange(@Observes SemanticDefinitionsReloaded event) {
		if (securityContext.isSystemTenant()) {
			return;
		}
		LOGGER.info("Reloading the semantic labels");
		// reload the store contents and notify the observers for the changes
		store.reset();
		notifyForChanges();
	}

	private void notifyForChanges() {
		observers.forEach(Executable::execute);
	}

	@Override
	public void addMutationObserver(Executable executable) {
		observers.add(executable);
	}
}
