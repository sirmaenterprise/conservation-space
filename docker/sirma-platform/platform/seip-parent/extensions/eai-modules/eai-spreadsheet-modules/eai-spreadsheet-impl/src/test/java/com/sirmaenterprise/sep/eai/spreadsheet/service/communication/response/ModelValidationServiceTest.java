package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.CONTENT_SOURCE;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.IMPORT_STATUS;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.PRIMARY_CONTENT_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.createNewEntry;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockEntityType;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockInstance;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockPropertyDefinition;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockPropertyType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.service.IntegrationOperations;

/**
 * Tests for {@link ModelValidationService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelValidationServiceTest {

	private static final String EXISTING_IN_CONTEXT_VALUE_BOTH = "BOTH";
	private static final String EXISTING_IN_CONTEXT_VALUE_WITHOUT_CONTEXT = "WITHOUT_CONTEXT";
	private static final String EXISTING_IN_CONTEXT_VALUE_IN_CONTEXT = "IN_CONTEXT";

	@Mock
	private InstanceValidationService validationService;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private TemplateService templateService;
	@InjectMocks
	private ModelValidationService modelValidationService;
	@Mock
	private ModelConfiguration modelConfiguration;
	@Mock
	private ClassInstance typeClass;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private InstanceContentService instanceContentService;

	private static final String DEF_ID = "type";
	private EntityType entityType;

	@Before
	public void setUp() {
		entityType = mockEntityType(modelConfiguration, DEF_ID, typeClass,
									mockPropertyType(modelConfiguration, DEF_ID, "dcterms:", UNIQUE_IDENTIFIER, null,
													 true),
									mockPropertyType(modelConfiguration, DEF_ID, "emf:", TITLE, null, true),
									mockPropertyType(modelConfiguration, DEF_ID, "emf:", TYPE, 1, true),
									mockPropertyType(modelConfiguration, DEF_ID, "emf:", "size", null, true),
									mockPropertyType(modelConfiguration, DEF_ID, "emf:", "fileName", null, true),
									mockPropertyType(modelConfiguration, DEF_ID, "emf:", "references", null, false));

		when(typeConverter.convert(any(Class.class), any(String.class))).thenAnswer(
				invocation -> invocation.getArgumentAt(1, Object.class));
		when(typeConverter.convert(eq(ShortUri.class), any(String.class))).then(
				invocation -> new ShortUri(invocation.getArgumentAt(1, String.class)));
		when(labelProvider.getValue(Matchers.anyString())).thenReturn("errorMessage");

	}

	@Test
	public void should_HaveError_When_PropertyDefinitionExistInContextIsWithoutContextAndHaveContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_WITHOUT_CONTEXT).setContext().build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertFalse(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_NotHaveError_When_PropertyDefinitionExistInContextIsInContextAndHaveContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_IN_CONTEXT).setContext().build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertTrue(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_NotHaveError_When_PropertyDefinitionExistInContextIsBothAndHaveContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_BOTH).setContext().build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertTrue(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_NotHaveError_When_PropertyDefinitionExistInContextIsNotSetAndHaveContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().setContext().build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertTrue(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_HaveError_When_PropertyDefinitionExistInContextIsInContextContextAndHaveNotContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_IN_CONTEXT).build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();
		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertFalse(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_HaveError_When_PropertyDefinitionExistInContextIsInContextAndHaveNotContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_IN_CONTEXT).build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertFalse(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_NotHaveError_When_PropertyDefinitionExistInContextIsWithoutContextAndHaveNotContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_WITHOUT_CONTEXT).build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertTrue(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_NotHaveError_When_PropertyDefinitionExistInContextIsBothAndHaveNotContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().addExistingInContext(
				EXISTING_IN_CONTEXT_VALUE_BOTH).build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertTrue(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void should_NotHaveError_When_PropertyDefinitionExistInContextIsNotSetAndHaveNotContext() {
		IntegrationData integrationData = new IntegrationDataBuilder().build();
		ErrorBuilderProvider errorBuilderProvider = new ErrorBuilderProvider();

		modelValidationService.validateExistingInContext(integrationData, errorBuilderProvider);

		Assert.assertTrue(errorBuilderProvider.build().isEmpty());
	}

	@Test
	public void testValidatePermissionsValid() throws Exception {
		when(semanticDefinitionService.getClassInstance(eq("emf:type"))).thenReturn(typeClass);
		when(instanceAccessEvaluator.canWrite(any())).thenReturn(Boolean.TRUE);
		modelValidationService.validateCreatablePermissions(entityType);
	}

	@Test(expected = EAIReportableException.class)
	public void testValidatePermissionsNoWrite() throws Exception {
		when(semanticDefinitionService.getClassInstance(eq("emf:type"))).thenReturn(typeClass);
		when(instanceAccessEvaluator.canWrite(any())).thenReturn(Boolean.FALSE);
		modelValidationService.validateCreatablePermissions(entityType);
	}

	@Test(expected = EAIReportableException.class)
	public void testValidateMissingClass() throws Exception {
		when(semanticDefinitionService.getClassInstance(eq("emf:type"))).thenReturn(null);
		modelValidationService.validateCreatablePermissions(entityType);
	}

	@Test
	public void testValidatePropertyModelValid() throws Exception {
		when(semanticDefinitionService.getClassInstance(eq("emf:type"))).thenReturn(typeClass);
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", true, true));
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("", errorBuilder.toString());
	}

	@Test
	public void testValidatePropertyModelInvalidModel() throws Exception {
		when(semanticDefinitionService.getClassInstance(eq("emf:type"))).thenReturn(typeClass);
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", true, true));
		data.getSource().getProperties().put(IMPORT_STATUS, "status is system property");
		data.getSource().getProperties().put("emf:unknown", "val");
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("Property [emf:unknown] is not valid for Title_type", errorBuilder.toString());
		// now with two invalid properties
		data.getSource().getProperties().put("emf:unknown2", "val");
		errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("Properties [emf:unknown, emf:unknown2] are not valid for Title_type", errorBuilder.toString());
	}

	@Test
	public void testValidatePropertyModelCreatableUploadbleScenarios() throws Exception {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(instanceContentService.getContent("emf:id", null)).thenReturn(contentInfo);
		when(contentInfo.getMimeType()).thenReturn("image/jpeg");

		Instance mockInstance = mockInstance(DEF_ID, "emf:id", true, true);
		IntegrationData data = createData(mockInstance);
		data.getSource().put(CONTENT_SOURCE, "filename");
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("There is no content file associated with the uploadable object.", errorBuilder.toString());
		// there is content id as well
		data.getSource().put(PRIMARY_CONTENT_ID, "emf:id");
		errorBuilder = new ErrorBuilderProvider();
		Template template = mock(Template.class);
		when(template.getCorrespondingInstance()).thenReturn("emf:template");
		when(templateService.getTemplate(any(TemplateSearchCriteria.class))).thenReturn(template);
		modelValidationService.validatePropertyModel(data, errorBuilder);
		verify(mockInstance).add(eq(LinkConstants.HAS_TEMPLATE), eq("emf:template"));
		assertEquals("", errorBuilder.toString());
		// remove system properties like primary content id and mimetype
		data.getSource().getProperties().remove(PRIMARY_CONTENT_ID);
		data.getSource().getProperties().remove("emf:" + MIMETYPE);
		errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("", errorBuilder.toString());
		// there is no source and content id
		data.getSource().getProperties().remove(CONTENT_SOURCE);
		errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("", errorBuilder.toString());
	}

	@Test
	public void testValidatePropertyModelCreatableScenarios() throws Exception {
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", true, false));
		data.getSource().put(PRIMARY_CONTENT_ID, "emf:id");
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("There is content file associated with a creatable object.", errorBuilder.toString());
		// no id
		data.getSource().getProperties().remove(PRIMARY_CONTENT_ID);
		errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("", errorBuilder.toString());
	}

	@Test
	public void testValidatePropertyModelUploadableScenarios() throws Exception {
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", false, true));

		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(instanceContentService.getContent("emf:id", null)).thenReturn(contentInfo);
		when(contentInfo.getMimeType()).thenReturn("image/jpeg");
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("There is no content file associated with the uploadable object.", errorBuilder.toString());
		// with id
		data.getSource().getProperties().put(PRIMARY_CONTENT_ID, "emf:id");
		errorBuilder = new ErrorBuilderProvider();
		when(templateService.getTemplate(any(TemplateSearchCriteria.class))).thenReturn(null);
		modelValidationService.validatePropertyModel(data, errorBuilder);
		assertEquals("", errorBuilder.toString());
	}

	private IntegrationData createData(Instance mockInstance) {
		return new IntegrationData(mockInstance, null, createNewEntry("1"), new SpreadsheetEntryId("0", "1"),
								   IntegrationOperations.CREATE_OP.getOperation(), modelConfiguration);
	}

	@Test
	public void testValidateInstanceModelValid() throws Exception {
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", true, true));
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		when(validationService.validate(any())).thenReturn(new InstanceValidationResult(Collections.emptyList()));
		modelValidationService.validateInstanceModel(data, errorBuilder);
	}

	@Test
	public void testValidateInstanceModelInvalid() throws Exception {
		testDefaultErrors(PropertyValidationErrorTypes.INVALID_BOOLEAN, "boolean", "Invalid boolean!");
		testDefaultErrors(PropertyValidationErrorTypes.INVALID_DATE, "date", "Invalid date!");
		testDefaultErrors(PropertyValidationErrorTypes.INVALID_TEXT_FORMAT, "an..100", "Invalid test!");
		testDefaultErrors(PropertyValidationErrorTypes.INVALID_NUMBER, "n..10", "Invalid number!");
		testDefaultErrors(PropertyValidationErrorTypes.INVALID_URI, "uri", "Invalid uri!");
		testDefaultErrors(PropertyValidationErrorTypes.INVALID_CODELIST, "codelist", "Invalid codelist!");

	}

	@Test
	public void testValidateInstanceModelInvalidMandatory() throws Exception {
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", true, true));
		List<PropertyValidationError> errors = new LinkedList<>();
		PropertyDefinition property = mockPropertyDefinition("emf:title", "an..100", null);
		errors.add(newError(PropertyValidationErrorTypes.MISSING_MANDATORY_PROPERTY,
							"Property is missing but it is collected as list.", property));
		testValidation(data, errors, "Missing mandatory property [title(emf:title)].");
		PropertyDefinition property2 = mockPropertyDefinition("emf:title2", "an..100", null);
		errors.add(newError(PropertyValidationErrorTypes.MISSING_MANDATORY_PROPERTY,
							"Property is missing but it is collected as list.", property2));
		testValidation(data, errors, "Missing mandatory properties [title(emf:title),title2(emf:title2)].");
	}

	private void testDefaultErrors(String error, String type, String bundleMsg) {
		IntegrationData data = createData(mockInstance(DEF_ID, "emf:id", true, true));
		List<PropertyValidationError> errors = new LinkedList<>();
		PropertyDefinition property = mockPropertyDefinition("emf:title", type, null);
		errors.add(newError(error, bundleMsg, property));
		testValidation(data, errors, bundleMsg);
		// with default msg
		errors.clear();
		errors.add(newError(error, PropertyValidationError.DEFAULT_MESSAGE, property));
		testValidation(data, errors, error + " for title(emf:title)");
	}

	private void testValidation(IntegrationData data, List<PropertyValidationError> errors, String expected) {
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		when(validationService.validate(any())).thenReturn(new InstanceValidationResult(errors));
		modelValidationService.validateInstanceModel(data, errorBuilder);
		assertEquals(expected, errorBuilder.toString());
	}

	private static PropertyValidationError newError(final String type, final String msg,
			final PropertyDefinition propertyDefinition) {
		return new PropertyValidationError() {

			@Override
			public String getValidationType() {
				return type;
			}

			@Override
			public PropertyDefinition getFieldName() {
				return propertyDefinition;
			}

			@Override
			public String getMessage() {
				return msg;
			}
		};
	}

	private class IntegrationDataBuilder {
		private Instance instance = new ObjectInstance();
		private DefinitionModel instanceDefinitionModel = Mockito.mock(DefinitionModel.class);
		private boolean withContext = false;
		private String existingInContext;

		private IntegrationDataBuilder addExistingInContext(String existingInContext) {
			this.existingInContext = existingInContext;
			return this;
		}

		private IntegrationData build() {
			IntegrationData integrationData = Mockito.mock(IntegrationData.class);
			Mockito.when(integrationData.getIntegrated()).thenReturn(instance);
			Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinitionModel);

			Mockito.when(validationService.canExistInContext(instanceDefinitionModel)).thenReturn(canExistInContext());
			Mockito.when(validationService.canExistWithoutContext(instanceDefinitionModel))
					.thenReturn(canExistWithoutContext());
			if (withContext) {
				Mockito.when(integrationData.getContext()).thenReturn(Mockito.mock(Instance.class));
			}

			return integrationData;
		}

		private boolean canExistInContext() {
			return StringUtils.isBlank(existingInContext) || EXISTING_IN_CONTEXT_VALUE_BOTH.equals(
					existingInContext) || EXISTING_IN_CONTEXT_VALUE_IN_CONTEXT.equals(
					existingInContext);
		}

		private boolean canExistWithoutContext() {
			return StringUtils.isBlank(existingInContext) || EXISTING_IN_CONTEXT_VALUE_BOTH.equals(
					existingInContext) || EXISTING_IN_CONTEXT_VALUE_WITHOUT_CONTEXT.equals(
					existingInContext);
		}

		public IntegrationDataBuilder setContext() {
			withContext = true;
			return this;
		}
	}

}
