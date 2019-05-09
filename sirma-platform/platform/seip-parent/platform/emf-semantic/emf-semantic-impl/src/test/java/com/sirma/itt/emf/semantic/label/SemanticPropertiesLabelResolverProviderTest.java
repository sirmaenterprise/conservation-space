package com.sirma.itt.emf.semantic.label;

import static org.junit.Assert.*;
import static com.sirma.itt.seip.domain.definition.label.LabelProvider.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.definition.label.LabelResolver;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link SemanticPropertiesLabelResolverProvider}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/11/2018
 */
public class SemanticPropertiesLabelResolverProviderTest {

	@InjectMocks
	private SemanticPropertiesLabelResolverProvider labelResolverProvider;

	@Spy
	private ContextualMap<String, Map<String, String>> store = ContextualMap.create();

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private SecurityContext securityContext;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		// reset any cached values and initialize the instance
		store.clearContextValue();
		labelResolverProvider.init();

		setUpData();
	}

	private void setUpData() {
		when(semanticDefinitionService.getRelations()).thenReturn(Arrays.asList(
				createProperty("emf:createdOn",
						createMapValue(label("en", "createdOn - English Label"), label("de", "createdOn - German label")),
						createMultiLangValue(label("en", "createdOn - English description"), label("de", "createdOn - German description"))),
				createProperty("dcterms:title",
						createMapValue(label("en", "English title"), label("de", "German title")),
						createMapValue(label("en", "English title description"), label("de", "German title description")))
		));
		when(semanticDefinitionService.getProperties()).thenReturn(Arrays.asList(
				createProperty("emf:references",
						createMultiLangValue(label("en", "References - English"), label("de", "References - German label")),
						createMapValue(label("en", "References - English description"), label("de", "References - German description"))),
				createProperty("emf:createdBy",
						createMultiLangValue(label("en", "createdBy - English Label"), label("de", "createdBy - German label")),
						createMapValue(label("en", "createdBy - English description"), label("de", "createdBy - German description")))
		));
		when(semanticDefinitionService.getClasses()).thenReturn(Arrays.asList(
				createClass("emf:Case",
						createMapValue(label("en", "Case - English Label"), label("de", "Case - German label")),
						createMapValue(label("en", "Case - English description"), label("de", "Case - German description"))),
				createClass("emf:Document",
						createMapValue(label("en", "Document - English Label"), label("de", "Document - German label")),
						createMultiLangValue(label("en", "Document - English description"), label("de", "Document - German description"))),
				createClass("emf:Project", "English Label", 42)
		));
	}

	private PropertyInstance createProperty(String id, Serializable titles, Serializable descriptions) {
		PropertyInstance instance = new PropertyInstance();
		instance.setId(id);
		instance.addIfNotNull(DefaultProperties.TITLE, titles);
		instance.addIfNotNull("definition", descriptions);
		return instance;
	}

	private ClassInstance createClass(String id, Serializable titles, Serializable descriptions) {
		ClassInstance instance = new ClassInstance();
		instance.setId(id);
		instance.addIfNotNull(DefaultProperties.TITLE, titles);
		instance.addIfNotNull(DefaultProperties.DESCRIPTION, descriptions);
		return instance;
	}

	private Serializable createMapValue(Consumer<BiConsumer<String, String>>... consumers) {
		HashMap<String, String> map = new HashMap<>();
		for (Consumer<BiConsumer<String, String>> consumer : consumers) {
			consumer.accept(map::put);
		}
		return map;
	}

	private Serializable createMultiLangValue(Consumer<BiConsumer<String, String>>... consumers) {
		MultiLanguageValue multiLanguageValue = new MultiLanguageValue();
		for (Consumer<BiConsumer<String, String>> consumer : consumers) {
			consumer.accept(multiLanguageValue::addValue);
		}
		return multiLanguageValue;
	}

	private Consumer<BiConsumer<String, String>> label(String lang, String value) {
		return consumer -> consumer.accept(lang, value);
	}

	@Test
	public void getLabelResolver_shouldProvideLabelsFromMultiLanguageValue() throws Exception {
		verifyLabelsData(Locale.ENGLISH, "emf:references", "References - English", "References - English description");
		verifyLabelsData(Locale.GERMANY, "emf:Document", "Document - German label", "Document - German description");
	}

	@Test
	public void getLabelResolver_shouldProvideLabelsFromMapValue() throws Exception {
		verifyLabelsData(Locale.GERMAN, "emf:createdOn", "createdOn - German label", "createdOn - German description");
		verifyLabelsData(Locale.US, "emf:Document", "Document - English Label", "Document - English description");
	}

	@Test
	public void getLabelResolver_shouldProvideSingleValuesUnderEnglish() throws Exception {
		verifyLabelsData(Locale.ENGLISH, "emf:Project", "English Label", null);
		verifyLabelsData(Locale.GERMANY, "emf:Project", null, null);
	}

	@Test
	public void getLabelResolver_shouldIgnoreNonSupportedValues() throws Exception {
		// the description is defined as number
		verifyLabelsData(Locale.ENGLISH, "emf:Project", "English Label", null);
	}

	private void verifyLabelsData(Locale locale, String uri, String expectedLabel, String expectedDescription) {
		LabelResolver labelResolver = labelResolverProvider.getLabelResolver(locale);
		assertNotNull(locale.getDisplayName() + " resolver should be present", labelResolver);

		String propertyLabel = labelResolver.getLabel(buildUriLabelId(uri));
		assertEquals(expectedLabel, propertyLabel);

		String propertyTooltip = labelResolver.getLabel(buildUriTooltipId(uri));
		assertEquals(expectedDescription, propertyTooltip);
	}

	@Test
	public void onModelChange_shouldNotifyTheRegisteredObservers() throws Exception {
		AtomicBoolean check = new AtomicBoolean(false);
		labelResolverProvider.addMutationObserver(() -> check.set(true));
		// trigger data loading
		verifyLabelsData(Locale.ENGLISH, "emf:Project", "English Label", null);

		labelResolverProvider.onModelChange(new SemanticDefinitionsReloaded());

		assertTrue("Observer should have been called", check.get());

		// verify the data is loaded again after reset
		verifyLabelsData(Locale.ENGLISH, "emf:Project", "English Label", null);
	}

	@Test
	public void shouldDoNothingInSystemTenantContext() throws Exception {
		reset(securityContext);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);

		AtomicBoolean check = new AtomicBoolean(false);
		labelResolverProvider.addMutationObserver(() -> check.set(true));

		// try to load data
		LabelResolver labelResolver = labelResolverProvider.getLabelResolver(Locale.ENGLISH);
		assertNull(Locale.ENGLISH.getDisplayName() + " resolver should not be present", labelResolver);

		// try model update
		labelResolverProvider.onModelChange(new SemanticDefinitionsReloaded());

		assertFalse("Observer should not have been called", check.get());
	}
}
