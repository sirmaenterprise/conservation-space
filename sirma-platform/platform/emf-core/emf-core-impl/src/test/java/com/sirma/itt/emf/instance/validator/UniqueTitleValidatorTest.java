package com.sirma.itt.emf.instance.validator;

import java.io.Serializable;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.vocabulary.DCTERMS;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.instance.UniquePropertiesService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test the unique title validator.
 *
 * @author nvelkov
 */
public class UniqueTitleValidatorTest {

	@Mock
	private UniquePropertiesService uniquePropertiesService;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private UniqueTitleValidator uniqueTitleValidator;

	@BeforeClass
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the unique title validator with a tag instance.
	 */
	@Test
	public void validationSuccesfull() {
		mockUniquePropertiesService(false);
		ValidationContext context = new ValidationContext(createInstance("test", "test",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag", "user"), Operation.NO_OPERATION);
		uniqueTitleValidator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 0);
	}

	/**
	 * Test the unique title validator with an instance that is not a tag. The validation should be succesfull.
	 */
	@Test
	public void validationSuccesfullNotATag() {
		ValidationContext context = new ValidationContext(createInstance("test", "test", "somethingelse", "user"),
				Operation.NO_OPERATION);
		uniqueTitleValidator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 0);
	}

	/**
	 * Test the unique title validator with a saved search instance. The validation should be succesfull.
	 */
	@Test
	public void validationSuccesfullSavedSearch() {
		mockUniquePropertiesService(false);
		ValidationContext context = new ValidationContext(
				createInstance("test", "test",
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedSearch", "user"),
				Operation.NO_OPERATION);
		uniqueTitleValidator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 0);
	}

	/**
	 * Test the unique title validator when there is a saved search with the same title.
	 */
	@Test
	public void validationFailedObjectAlreadyExists() {
		mockUniquePropertiesService(true);
		mockLabelProvider(LabelProvider.NO_LABEL);
		ValidationContext context = new ValidationContext(
				createInstance("test", "test",
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedSearch", "user"),
				Operation.NO_OPERATION);
		uniqueTitleValidator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 1);
	}

	/**
	 * Test that the correct properties are passed to the unique properties service to ensure that the title, creator
	 * and semantic type are set.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testCorrectProperties() {
		ValidationContext context = new ValidationContext(
				createInstance("test", "test",
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedSearch", "user"),
				Operation.NO_OPERATION);
		uniqueTitleValidator.validate(context);

		ArgumentCaptor<Map<String, Serializable>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(uniquePropertiesService).objectExists(propertiesCaptor.capture(), Matchers.anyString());
		mockLabelProvider(LabelProvider.NO_LABEL);

		String title = DCTERMS.PREFIX + ":" + DCTERMS.TITLE.getLocalName();
		String createdBy = EMF.PREFIX + ":" + EMF.CREATED_BY.getLocalName();
		Assert.assertEquals(propertiesCaptor.getValue().get(title), "^\\Qtest\\E$");
		Assert.assertEquals(propertiesCaptor.getValue().get(DefaultProperties.SEMANTIC_TYPE),
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedSearch");
		Assert.assertEquals(propertiesCaptor.getValue().get(createdBy), "user");
	}

	/**
	 * Creates a mock {@link Instance}.
	 *
	 * @param title
	 *            the mock's title
	 * @param uri
	 *            the mock's uri
	 * @return the mock instance
	 */
	private static Instance createInstance(String title, String uri, String semanticType, String createdBy) {
		Instance instance = new EmfInstance();
		instance.add(DefaultProperties.TITLE, title);
		instance.add(DefaultProperties.SEMANTIC_TYPE, semanticType);
		instance.add(DefaultProperties.CREATED_BY, createdBy);
		instance.setId(uri);
		return instance;
	}

	private void mockUniquePropertiesService(boolean objectExists) {
		Mockito.when(uniquePropertiesService.objectExists(Matchers.anyMap(), Matchers.anyString())).thenReturn(
				objectExists);
	}

	/**
	 * Mocks {@link LabelProvider#getValue(String)} to return the given label for any string.
	 *
	 * @param label
	 *            - the given label
	 */
	private void mockLabelProvider(String label) {
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn(label);
	}
}
