package com.sirma.itt.emf.audit.solr.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test the Recent activities sentence generator.
 *
 * @author nvelkov
 */
public class RecentActivitiesSentenceGeneratorTest {

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private InstanceService objectService;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private AuditConfiguration auditConfiguration;

	@Mock
	private CodelistService codeListService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private UserPreferences userPreferences;

	@InjectMocks
	private RecentActivitiesSentenceGenerator generator = new RecentActivitiesSentenceGeneratorImpl();

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		mockLabelProvider();
		mockHeadersService();
		mockObjectService();
		mockDefinitionService();
		mockCodelistService();
		mockUserPreferences();
		mockAuditConfiguration("recent.activities.");
	}

	/**
	 * Test the sentence generation with correct data.
	 */
	@Test
	public void testSentenceGeneration() {
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setUserId("userId");
		activity.setAddedTargetProperties(new HashSet<>(Arrays.asList("addedId", "secondAddedId")));
		activity.setRemovedTargetProperties(new HashSet<>(Arrays.asList("removedId")));
		activity.setInstanceId("instanceId");
		activity.setTimestamp(new Date());

		List<RecentActivity> sentences = generator.generateSentences(Arrays.asList(activity));
		Assert.assertEquals(sentences.size(), 1);
		Assert.assertEquals(sentences.get(0).getSentence(),
				"<generatedHeader> added <generatedHeader>, <generatedHeader> (added <generatedHeader>, <generatedHeader>) and removed <generatedHeader> as primary image to <generatedHeader>");
		Assert.assertEquals(sentences.get(0).getTimestamp(), activity.getTimestamp());

	}

	/**
	 * Test the sentence generation with missing data.
	 */
	@Test
	public void testSentenceGenerationEmptyData() {
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setUserId("userId");
		activity.setInstanceId("instanceId");
		activity.setTimestamp(new Date());

		List<RecentActivity> sentences = generator.generateSentences(Arrays.asList(activity));
		Assert.assertEquals(sentences.size(), 1);
		Assert.assertEquals(sentences.get(0).getSentence(),
				"<generatedHeader> added  (added ) and removed  as primary image to <generatedHeader>");
		Assert.assertEquals(sentences.get(0).getTimestamp(), activity.getTimestamp());

	}
	/**
	 * Test the state label retrieval.
	 */
	@Test
	public void testStateRetrieval(){
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setState("state");
		activity.setUserId("userId");
		activity.setAddedTargetProperties(new HashSet<>(Arrays.asList("addedId", "secondAddedId")));
		activity.setRemovedTargetProperties(new HashSet<>(Arrays.asList("removedId")));
		activity.setInstanceId("instanceId");
		activity.setTimestamp(new Date());

		Mockito.when(labelProvider.getLabel(Matchers.anyString()))
		.thenReturn("Changed state to {state}.");
		List<RecentActivity> sentences = generator.generateSentences(Arrays.asList(activity));
		Assert.assertEquals(sentences.size(), 1);
		Assert.assertEquals("Changed state to label.", sentences.get(0).getSentence());
	}
	/**
	 * Test the state label retrieval with an action with no state.
	 */
	@Test
	public void testStateRetrievalMissingState(){
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setUserId("userId");
		activity.setAddedTargetProperties(new HashSet<>(Arrays.asList("addedId", "secondAddedId")));
		activity.setRemovedTargetProperties(new HashSet<>(Arrays.asList("removedId")));
		activity.setInstanceId("instanceId");
		activity.setTimestamp(new Date());

		Mockito.when(labelProvider.getLabel(Matchers.anyString()))
		.thenReturn("Changed state to {state}.");
		List<RecentActivity> sentences = generator.generateSentences(Arrays.asList(activity));
		Assert.assertEquals(sentences.size(), 1);
		Assert.assertEquals("Changed state to (Unknown state).", sentences.get(0).getSentence());

	}
	/**
	 * Test the sentence generation with a missing label.
	 */
	@Test
	public void testSentenceGenerationMissingLabel() {
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setUserId("userId");
		activity.setAddedTargetProperties(new HashSet<>(Arrays.asList("addedId", "secondAddedId")));
		activity.setRemovedTargetProperties(new HashSet<>(Arrays.asList("removedId")));
		activity.setInstanceId("instanceId");
		activity.setTimestamp(new Date());

		Mockito.when(labelProvider.getLabel(Matchers.anyString())).then(i -> i.getArgumentAt(0, String.class));
		Mockito.when(labelProvider.getValue(Matchers.anyString()))
				.thenReturn("Performed action on {object} with added {addedSubjects} and removed {removedSubjects}.");

		List<RecentActivity> sentences = generator.generateSentences(Arrays.asList(activity));
		Assert.assertEquals(sentences.size(), 1);
		Assert.assertEquals(sentences.get(0).getSentence(),
				"Performed action on <generatedHeader> with added <generatedHeader>, <generatedHeader> and removed <generatedHeader>.");
		Assert.assertEquals(sentences.get(0).getTimestamp(), activity.getTimestamp());

	}

	/**
	 * Test the sentence generation with a missing configuration.
	 */
	@Test
	public void testSentenceGenerationMissingConfiguration() {
		StoredAuditActivity activity = new StoredAuditActivity();
		activity.setAction("action");
		activity.setUserId("userId");
		activity.setAddedTargetProperties(new HashSet<>(Arrays.asList("addedId", "secondAddedId")));
		activity.setRemovedTargetProperties(new HashSet<>(Arrays.asList("removedId")));
		activity.setInstanceId("instanceId");
		activity.setTimestamp(new Date());

		Mockito.when(labelProvider.getValue(Matchers.anyString()))
				.thenReturn("Performed action on {object} with added {addedSubjects} and removed {removedSubjects}.");
		mockAuditConfiguration(null);

		List<RecentActivity> sentences = generator.generateSentences(Arrays.asList(activity));
		Assert.assertEquals(sentences.size(), 1);
		Assert.assertEquals(sentences.get(0).getSentence(),
				"Performed action on <generatedHeader> with added <generatedHeader>, <generatedHeader> and removed <generatedHeader>.");
		Assert.assertEquals(sentences.get(0).getTimestamp(), activity.getTimestamp());

	}

	public void mockLabelProvider() {
		Mockito.when(labelProvider.getLabel(Matchers.anyString())).thenReturn(
				"{User} added {subject} (added {addedSubjects}) and removed {removedSubjects} as primary image to {object}");
	}

	@SuppressWarnings("unchecked")
	private void mockHeadersService() {
		Mockito.doAnswer(invocation ->
			{
				Collection<Instance> instances = (Collection<Instance>) invocation.getArguments()[0];
				for (Instance instance : instances) {
					instance.add(DefaultProperties.HEADER_BREADCRUMB, "<generatedHeader>");
				}
				return null;
			}).when(instanceLoadDecorator).decorateResult(Matchers.anyCollection());
	}

	@SuppressWarnings("unchecked")
	private void mockObjectService() {
		Instance addedInstance = new EmfInstance();
		Instance secondAddedInstance = new EmfInstance();
		Instance removedInstance = new EmfInstance();
		Instance primaryInstance = new EmfInstance();
		Instance userInstance = new EmfInstance();

		addedInstance.setId("addedId");
		addedInstance.setIdentifier("definitionid");
		secondAddedInstance.setId("secondAddedId");
		secondAddedInstance.setIdentifier("definitionid");
		removedInstance.setId("removedId");
		removedInstance.setIdentifier("definitionid");
		primaryInstance.setId("instanceId");
		primaryInstance.setIdentifier("definitionid");
		userInstance.setId("userId");
		userInstance.setIdentifier("definitionid");
		Mockito.when(objectService.loadByDbId(Matchers.anyList())).thenReturn(
				Arrays.asList(addedInstance, secondAddedInstance, removedInstance, primaryInstance, userInstance));
	}

	private void mockAuditConfiguration(String prefix) {
		ConfigurationProperty<String> property = new ConfigurationPropertyMock<>(prefix);
		Mockito.when(auditConfiguration.getRecentActivitiesLabelPrefix()).thenReturn(property);
	}

	private void mockDefinitionService() {
		PropertyDefinitionMock property = new PropertyDefinitionMock();
		property.setCodelist(1);
		DefinitionModel definition = Mockito.mock(DefinitionModel.class);
		Mockito.when(definition.getField(Matchers.anyString())).thenReturn(Optional.of(property));
		Mockito.when(definitionService.find(Matchers.anyString())).thenReturn(definition);
	}

	private void mockCodelistService() {
		CodeValue value = new CodeValue();
		value.add("en", "label");
		Mockito.when(codeListService.getCodeValue(Matchers.anyInt(), Matchers.anyString())).thenReturn(value);
	}

	private void mockUserPreferences() {
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
	}
}
