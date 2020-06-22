package com.sirma.itt.seip.instance.headers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.ConfigurationChangeListener;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;

/**
 * Test for {@link DefinitionHeaderChangeListener}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
public class DefinitionHeaderChangeListenerTest {

	private static final String BREAD_CRUMB = "bread_crumb";
	private static final String ALT_TITLE = "alt_title_label";
	private static final String DEFINITION_ONE_ID = "definition1";
	private static final String DEFINITION_TWO_ID = "definition2";
	private static final String DEFINITION_THREE_ID = "definition3";
	
	@InjectMocks
	private DefinitionHeaderChangeListener headerChangeListener;

	@Mock
	private InstanceHeaderService instanceHeaderService;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private LabelService labelService;
	@Mock
	private SystemConfiguration systemConfiguration;
	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		LabelDefinition breadCrumbLabel = mock(LabelDefinition.class);
		when(breadCrumbLabel.getLabels()).thenReturn(Collections.singletonMap(Locale.ENGLISH.getLanguage(), BREAD_CRUMB));
		when(labelService.getLabel(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(breadCrumbLabel);

		String altTitleId = "altTitleLabelId";
		LabelDefinition altTitleLabel = mock(LabelDefinition.class);
		when(altTitleLabel.getLabels()).thenReturn(Collections.singletonMap(Locale.ENGLISH.getLanguage(), ALT_TITLE));
		when(labelService.getLabel(altTitleId)).thenReturn(altTitleLabel);

		// Definition without field altTitle.
		DefinitionModel definitionModel1 = mock(DefinitionModel.class);
		PropertyDefinition property1 = mock(PropertyDefinition.class);
		when(property1.getLabelId()).thenReturn(DefaultProperties.HEADER_BREADCRUMB);
		when(definitionModel1.getField(DefaultProperties.HEADER_LABEL)).thenReturn(Optional.empty());
		when(definitionModel1.getField(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(Optional.of(property1));

		when(definitionModel1.getIdentifier()).thenReturn(DEFINITION_ONE_ID);

		// Definition contains field altTitle which contains labelIc.
		DefinitionModel definitionModel2 = mock(DefinitionModel.class);
		PropertyDefinition property2 = mock(PropertyDefinition.class);
		when(property2.getLabelId()).thenReturn(altTitleId);
		when(definitionModel2.getField(DefaultProperties.HEADER_LABEL)).thenReturn(Optional.of(property2));

		when(definitionModel2.getIdentifier()).thenReturn(DEFINITION_TWO_ID);

		// Definition contains field altTitle but without labelId.
		DefinitionModel definitionModel3 = mock(DefinitionModel.class);
		PropertyDefinition altTitle = mock(PropertyDefinition.class);
		when(altTitle.getLabelId()).thenReturn("");
		PropertyDefinition breadCrumb = mock(PropertyDefinition.class);
		when(breadCrumb.getLabelId()).thenReturn(DefaultProperties.HEADER_BREADCRUMB);
		when(definitionModel3.getField(DefaultProperties.HEADER_LABEL)).thenReturn(Optional.of(altTitle));
		when(definitionModel3.getField(DefaultProperties.HEADER_BREADCRUMB)).thenReturn(Optional.of(breadCrumb));
		when(definitionModel3.getIdentifier()).thenReturn(DEFINITION_THREE_ID);

		when(definitionService.getAllDefinitions()).then(a -> Stream.of(definitionModel1, definitionModel2, definitionModel3));

		when(systemConfiguration.getSystemLanguage()).thenReturn(Locale.ENGLISH.getLanguage());
	}

	@Test
	public void onDefinitionsUpdated_shouldRegisterBreadcrumbHeaders() throws Exception {
		headerChangeListener.onDefinitionsUpdated(null);

		// Definition one has no altTitle field so bread_crumb have to be used
		verify(instanceHeaderService, times(1)).trackHeader(DEFINITION_ONE_ID, BREAD_CRUMB);
		// Definition two has altTitle with labelId so altTitle have to be used
		verify(instanceHeaderService, times(1)).trackHeader(DEFINITION_TWO_ID, ALT_TITLE);
		// Definition three has altTitle without labelId so bread_crumb have to be used
		verify(instanceHeaderService, times(1)).trackHeader(DEFINITION_THREE_ID, BREAD_CRUMB);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onInit_shouldTriggerDefinitionCheckOnLanguageChange() {
		ConfigurationProperty<String> property = mock(ConfigurationProperty.class);
		doAnswer(a -> {
			a.getArgumentAt(0, ConfigurationChangeListener.class).onConfigurationChange(null);
			return null;
		}).when(property).addConfigurationChangeListener(any());

		when(systemConfiguration.getSystemLanguageConfiguration()).thenReturn(property);

		headerChangeListener.initialize();

		// Definition one has no altTitle field so bread_crumb have to be used
		verify(instanceHeaderService, times(1)).trackHeader(DEFINITION_ONE_ID, BREAD_CRUMB);
		// Definition two has altTitle with labelId so altTitle have to be used
		verify(instanceHeaderService, times(1)).trackHeader(DEFINITION_TWO_ID, ALT_TITLE);
		// Definition three has altTitle without labelId so bread_crumb have to be used
		verify(instanceHeaderService, times(1)).trackHeader(DEFINITION_THREE_ID, BREAD_CRUMB);
	}
}
