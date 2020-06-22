package com.sirma.sep;

import static com.sirma.itt.seip.definition.ValidationMessageUtils.error;
import static com.sirma.itt.seip.definition.validator.DataTypeFieldValidator.DataTypeFieldMessageBuilder.OBJECT_PROPERTY_AS_DATA;
import static com.sirma.itt.seip.definition.validator.DataTypeFieldValidator.DataTypeFieldMessageBuilder.UNREGISTERED_PROPERTY_URI;
import static com.sirma.itt.seip.definition.validator.DuplicateUriValidator.DuplicateUriMessageBuilder.DUPLICATED_URI;
import static com.sirma.itt.seip.definition.validator.FieldValueValidator.FieldValueMessageBuilder.FIELD_WITH_INVALID_BOOLEAN;
import static com.sirma.itt.seip.definition.validator.FieldValueValidator.FieldValueMessageBuilder.FIELD_WITH_INVALID_JSON;
import static com.sirma.itt.seip.definition.validator.FilterDefinitionsValidator.FilterValidatorMessageBuilder.FIELD_WITH_NON_SUPPORTED_FILTERING;
import static com.sirma.itt.seip.definition.validator.RangeFieldValidator.RangeValidatorMessageBuilder.MISSING_RELATION;
import static com.sirma.itt.seip.definition.validator.RangeFieldValidator.RangeValidatorMessageBuilder.RELATION_MISMATCH;
import static com.sirma.itt.seip.definition.validator.RangeFieldValidator.RangeValidatorMessageBuilder.UNREGISTERED_SEMANTIC_RANGE;
import static com.sirma.itt.seip.definition.validator.RangeFieldValidator.RangeValidatorMessageBuilder.UNREGISTERED_URI;
import static com.sirma.itt.seip.definition.validator.RangeFieldValidator.RangeValidatorMessageBuilder.USING_DATA_PROPERTY_AS_OBJECT;
import static com.sirma.itt.seip.definition.validator.TransitionValidator.TransitionValidatorMessageBuilder.DUPLICATE_TRANSITION_FIELD;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static com.sirma.sep.definition.DefinitionImportMessageBuilder.DUPLICATED_DEFINITIONS;
import static com.sirma.sep.definition.DefinitionImportMessageBuilder.DUPLICATED_FIELDS;
import static com.sirma.sep.definition.DefinitionImportMessageBuilder.DUPLICATED_LABELS;
import static com.sirma.sep.definition.DefinitionImportMessageBuilder.HIERARCHY_CYCLE;
import static com.sirma.sep.definition.DefinitionImportMessageBuilder.MISSING_PARENT;
import static com.sirma.sep.definition.DefinitionImportMessageBuilder.XML_PARSING_FAILURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.google.common.jimfs.Jimfs;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.cache.InMemoryCacheProvider;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.compile.GenericDefinitionCompilerCallback;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.filter.FilterServiceImpl;
import com.sirma.itt.seip.definition.jaxb.ComplexFieldDefinition;
import com.sirma.itt.seip.definition.jaxb.ComplexFieldsDefinition;
import com.sirma.itt.seip.definition.jaxb.ConditionDefinition;
import com.sirma.itt.seip.definition.jaxb.Configuration;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.jaxb.FilterDefinition;
import com.sirma.itt.seip.definition.jaxb.FilterDefinitions;
import com.sirma.itt.seip.definition.jaxb.Label;
import com.sirma.itt.seip.definition.jaxb.LabelValue;
import com.sirma.itt.seip.definition.jaxb.Labels;
import com.sirma.itt.seip.definition.jaxb.ObjectType;
import com.sirma.itt.seip.definition.jaxb.RegionDefinition;
import com.sirma.itt.seip.definition.jaxb.RegionsDefinition;
import com.sirma.itt.seip.definition.jaxb.TransitionDefinition;
import com.sirma.itt.seip.definition.jaxb.TransitionsDefinition;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelServiceImpl;
import com.sirma.itt.seip.definition.model.FilterDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.definition.validator.DataTypeFieldValidator;
import com.sirma.itt.seip.definition.validator.DefinitionValidator;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.filter.Filter;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.fakes.TempFileProviderFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.definition.DefinitionImportServiceImpl;
import com.sirma.sep.definition.DefinitionInfo;
import com.sirma.sep.definition.db.DefinitionContent;
import com.sirma.sep.definition.db.DefinitionEntry;
import com.sirma.sep.xml.JAXBHelper;

@RunWith(CdiRunner.class)
@AdditionalClasses({ DefinitionsDozerProvider.class, GenericDefinitionCompilerCallback.class,
	FilterServiceImpl.class, LabelServiceImpl.class })
@AdditionalPackages(DefinitionValidator.class)
@AdditionalClasspaths({ ObjectMapper.class, Extension.class, EventService.class })
public class DefinitionImportTest {

	@Test
	public void should_AllowChangingDefinitionIdentifier() {
		// Given stored definition and new definition with different identifier but same file name
		withExistingDefinition("someDefinitionId").inFile("test.xml");
		withNewDefinition("test.xml", "<definition id=\"newDefinitionId\" type=\"case\" />");

		// When I validate the definitions
		expectNoValidationErrors();

		// And I import the definitions
		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		// I expect the saved definition to be updated and the old removed
		assertEquals(1, savedDefinitions.size());

		GenericDefinition definition = savedDefinitions.get(0);
		assertEquals("newDefinitionId", definition.getIdentifier());
		verify(dbDao).delete(DefinitionContent.class, "someDefinitionId");
	}

	@Test
	public void should_ImportDefinitionHierarchy() {
		// Given I have a definition hierarchy
		withNewDefinitionFromFile("base_document.xml", "base_document.xml");
		withNewDefinitionFromFile("documents/simpleDocument.xml", "simple_document.xml");

		registerDataTypeProperty("dcterms:label", mock(PropertyInstance.class));
		registerDataTypeProperty("dcterms:description", mock(PropertyInstance.class));

		// When I validate the definitions
		expectNoValidationErrors();

		// And I import the definitions
		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		// I expect the following two definitions to be saved
		assertEquals(2, savedDefinitions.size());

		GenericDefinition childDefinition = savedDefinitions.get(0);
		assertEquals("baseDocument", childDefinition.getParentDefinitionId());
		assertEquals("simpleDocument", childDefinition.getIdentifier());
		assertFalse(childDefinition.isAbstract());
		assertEquals("Initial revision should be 1", Long.valueOf(1), childDefinition.getRevision());

		List<PropertyDefinition> fields = childDefinition.getFields();
		assertEquals(4, fields.size());

		assertEquals("label", fields.get(0).getName());
		assertEquals("type", fields.get(1).getName());
		assertEquals("title", fields.get(2).getName());
		assertEquals("description", fields.get(3).getName());

		GenericDefinition parentDefinition = savedDefinitions.get(1);
		assertNull(parentDefinition.getParentDefinitionId());
		assertEquals("baseDocument", parentDefinition.getIdentifier());
		assertTrue(parentDefinition.isAbstract());
		assertEquals(Long.valueOf(1), parentDefinition.getRevision());

		fields = parentDefinition.getFields();
		assertEquals(3, fields.size());

		assertEquals("type", fields.get(0).getName());
		assertEquals("title", fields.get(1).getName());
		assertEquals("description", fields.get(2).getName());

		// verify contents of the new definitions are saved
		assertEquals(2, savedContentEntries.size());

		DefinitionContent firstContent = savedContentEntries.get(0);

		assertEquals("simpleDocument", firstContent.getId());
		assertEquals("simpleDocument.xml", firstContent.getFileName());
		assertEquals(getFileContent("simple_document.xml"), firstContent.getContent());

		DefinitionContent secondContent = savedContentEntries.get(1);

		assertEquals("baseDocument", secondContent.getId());
		assertEquals("base_document.xml", secondContent.getFileName());
		assertEquals(getFileContent("base_document.xml"), secondContent.getContent());
	}

	@Test
	public void should_UpdateExistingDefinitionIncreasingItsRevisionNumber() {
		GenericDefinitionImpl existingDefinition = new GenericDefinitionImpl();
		existingDefinition.setIdentifier("def1");

		withExistingSavedDefinition(existingDefinition);

		withDefinition("def1").havingLabel("d1.label", "a1").inFile("def1.xml");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();
		assertEquals(1, savedDefinitions.size());

		GenericDefinition savedDefinition = savedDefinitions.get(0);
		assertEquals("The revision should be increased", Long.valueOf(2), savedDefinition.getRevision());
	}

	@Test
	public void should_NotSaveExistingDefinitionsThatAreNotReImported() {
		withExistingDefinition("def").inFile("def.xml");

		withExistingDefinition("defA")
			.havingParent("def")
			.inFile("defA.xml");

		withExistingDefinition("defAA")
			.havingParent("defA")
			.inFile("defAA.xml");

		withExistingDefinition("defAAA")
			.havingParent("defAA")
			.inFile("defAAA.xml");

		withExistingDefinition("defB").inFile("defB.xml");

		withExistingDefinition("defC").inFile("defC.xml");

		withDefinition("defAA").inFile("defAA.xml");
		withDefinition("defC").inFile("defC.xml");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		assertEquals(3, savedDefinitions.size());

		assertEquals("defC", savedDefinitions.get(0).getIdentifier());
		assertEquals("defAAA", savedDefinitions.get(1).getIdentifier());
		assertEquals("defAA", savedDefinitions.get(2).getIdentifier());
	}

	@Test
	public void should_SupportInheritanceFieldDataFromTheSameFieldInParentDefinition() {
		withDefinition("def1")
			.havingField("f1", "an..10", "readonly", "http://test", "label")
				.withCondition("mandatory", "[f1] IN ('TEST')")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingParent("def1")
			.havingField("f1", "an..20", null, null, "label")
			.inFile("def2.xml");

		registerDataTypeProperty("http://test", mock(PropertyInstance.class));

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		GenericDefinitionImpl def1 = savedDefinitions.get(1);
		assertEquals("def1", def1.getIdentifier());

		PropertyDefinition def1Field = def1.getFields().get(0);
		assertEquals("f1", def1Field.getIdentifier());
		assertEquals("an..10", def1Field.getType());
		assertEquals(DisplayType.READ_ONLY, def1Field.getDisplayType());
		assertEquals("http://test", def1Field.getUri());

		Condition def1Condition = def1Field.getConditions().get(0);
		assertEquals("mandatory", def1Condition.getIdentifier());
		assertEquals("[f1] IN ('TEST')", def1Condition.getExpression());

		GenericDefinitionImpl def2 = savedDefinitions.get(0);
		assertEquals("def2", def2.getIdentifier());

		PropertyDefinition def2Field = def2.getFields().get(0);
		assertEquals("f1", def2Field.getIdentifier());
		assertEquals("an..20", def2Field.getType());
		assertEquals(DisplayType.READ_ONLY, def2Field.getDisplayType());
		assertEquals("http://test", def2Field.getUri());

		Condition def2Condition = def2Field.getConditions().get(0);
		assertEquals("mandatory", def2Condition.getIdentifier());
		assertEquals("[f1] IN ('TEST')", def2Condition.getExpression());
	}

	@Test
	public void shouldSupportInheritanceOfConfigurationFields() {
		final String CONFIG_NAME = "creatable";

		withDefinition("def1")
			.havingConfigurationField(CONFIG_NAME, "boolean", "system", null, null, "true", "label")
			.havingConfigurationField("parent_config", "boolean", "system", null, null, "true", "label")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingConfigurationField(CONFIG_NAME, null, null, null, null, "false", "label")
			.havingConfigurationField("test_config", "an..20", "editable", null, null, "test", "label")
			.havingParent("def1")
			.inFile("def2.xml");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		GenericDefinitionImpl def1 = savedDefinitions.get(1);

		assertEquals(2, def1.getConfigurations().size());
		assertTrue(def1.getConfiguration(CONFIG_NAME).isPresent());
		PropertyDefinition config1 = def1.getConfiguration(CONFIG_NAME).get();
		verifyField(config1, CONFIG_NAME, "boolean", DisplayType.SYSTEM, null, "true");

		GenericDefinitionImpl def2 = savedDefinitions.get(0);

		assertEquals(3, def2.getConfigurations().size());
		PropertyDefinition config2 = def2.getConfiguration(CONFIG_NAME).orElse(null);
		assertNotNull(config2);
		verifyField(config2, CONFIG_NAME, "boolean", DisplayType.SYSTEM, null, "false");

		// also test if the functionality handles properties that don't exist in the parent
		PropertyDefinition testConfig = def2.getConfiguration("test_config").orElse(null);
		assertNotNull(testConfig);
		verifyField(testConfig, "test_config", "an..20", DisplayType.EDITABLE, null, "test");

		// test if non-predefined configurations also get inherited
		PropertyDefinition parentConfig = def2.getConfiguration("parent_config").orElse(null);
		assertNotNull(parentConfig);
		verifyField(parentConfig, "parent_config", "boolean", DisplayType.SYSTEM, null, "true");
	}

	@Test
	public void should_SupportMovingAFieldFromOneRegionInTheParentToAnotherInTheChild() {
		withDefinition("def1")
			.havingRegion("r1", "editable")
				.havingField("f1", "an..20", "editable", null, "label")
				.havingField("f2", "an..20", "editable", null, "label")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingParent("def1")
			.havingRegion("r2", "editable")
				.havingField("f1", "an..10", "readonly", "emf:uri1", "label")
			.inFile("def2.xml");

		registerDataTypeProperty("emf:uri1", mock(PropertyInstance.class));

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		assertEquals(2, savedDefinitions.size());

		GenericDefinitionImpl def2 = savedDefinitions.get(0);

		// expect f1 field to be removed from r1 and retained only in r2
		com.sirma.itt.seip.definition.RegionDefinition r1 = def2.getRegions().get(1);
		assertEquals("r1", r1.getIdentifier());

		assertEquals(1, r1.getFields().size());
		assertEquals("f2", r1.getFields().get(0).getIdentifier());

		com.sirma.itt.seip.definition.RegionDefinition r2 = def2.getRegions().get(0);
		assertEquals("r2", r2.getIdentifier());

		assertEquals(1, r2.getFields().size());

		PropertyDefinition f1 = r2.getFields().get(0);

		verifyField(f1, "f1", "an..10", DisplayType.READ_ONLY, "emf:uri1");
	}

	@Test
	public void should_ProperlyMoveFieldsInAndOutOfRegions() {

		registerDataTypeProperty("emf:field1", mock(PropertyInstance.class));
		registerDataTypeProperty("emf:field2", mock(PropertyInstance.class));
		registerDataTypeProperty("emf:field3", mock(PropertyInstance.class));
		registerDataTypeProperty("emf:field4", mock(PropertyInstance.class));


		// 4 fields outside regions
		withDefinition("def1")
				.havingField("field1", "an..10", "editable", "emf:field1", "Field 1")
				.havingField("field2", "an..10", "editable", "emf:field2", "Field 2")
				.havingField("field3", "an..10", "editable", "emf:field3", "Field 3")
				.havingField("field4", "an..10", "editable", "emf:field4", "Field 4")
				.inFile("def1.xml");

		// move 3 fields in 2 regions
		withDefinition("def2")
				.havingParent("def1")
				.havingRegion("r1", "editable")
					.havingField("field2", "an..10", "editable", "emf:field2", "Field 2")
					.havingField("field3", "an..10", "editable", "emf:field3", "Field 3")
				.havingRegion("r2", "editable")
					.havingField("field4", "an..10", "editable", "emf:field4", "Field 4")
				.inFile("def2.xml");

		withDefinition("def3")
				.havingParent("def2")
				// Move field 2 outside region
				.havingField("field2", "an..10", "editable", "emf:field2", "Field 2")
				.havingRegion("r1", "editable")
					// Move field 1 from def1 in region
					.havingField("field1", "an..10", "editable", "emf:field1", "Field 1")
				.havingRegion("r2", "editable")
					// Move field 3 from r1 to r2
					.havingField("field3", "an..10", "editable", "emf:field3", "Field 3")
				.inFile("def3.xml");

		expectNoValidationErrors();
		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();
		assertEquals(3, savedDefinitions.size());

		Map<String, GenericDefinitionImpl> definitionMap = savedDefinitions.stream()
				.collect(CollectionUtils.toIdentityMap(GenericDefinition::getIdentifier));

		// Definition 1
		GenericDefinitionImpl def1 = definitionMap.get("def1");
		expectFields(def1.getFields(), "field1", "field2", "field3", "field4");
		assertTrue(def1.getRegions().isEmpty());

		// Definition 2
		GenericDefinitionImpl def2 = definitionMap.get("def2");
		expectFields(def2.getFields(), "field1");

		List<com.sirma.itt.seip.definition.RegionDefinition> def2Regions = def2.getRegions();
		assertEquals(2, def2Regions.size());

		assertEquals("r1", def2Regions.get(0).getIdentifier());
		expectFields(def2Regions.get(0).getFields(),  "field2", "field3");

		assertEquals("r2", def2Regions.get(1).getIdentifier());
		expectFields(def2Regions.get(1).getFields(),  "field4");

		// Definition 3
		GenericDefinitionImpl def3 = definitionMap.get("def3");
		expectFields(def3.getFields(), "field2");

		List<com.sirma.itt.seip.definition.RegionDefinition> def3Regions = def3.getRegions();
		assertEquals(2, def3Regions.size());

		assertEquals("r1", def3Regions.get(0).getIdentifier());
		expectFields(def3Regions.get(0).getFields(),  "field1");

		assertEquals("r2", def3Regions.get(1).getIdentifier());
		expectFields(def3Regions.get(1).getFields(),  "field3", "field4");
	}

	private void expectFields(List<PropertyDefinition> actual, String ...expected) {
		Set<String> actualIds = actual.stream().map(PropertyDefinition::getName).collect(Collectors.toSet());
		Set<String> expectedIds = new HashSet<>(Arrays.asList(expected));
		assertEquals(expectedIds.size(), actualIds.size());
		assertEquals(expectedIds, actualIds);
	}

	@Test
	public void should_SupportCompleteOverridingOfParentFieldInTheChildDefinition() {
		withDefinition("def1")
			.havingField("f1", "an..10", "readonly", "http://test", "label")
				.withCondition("mandatory", "[f1] IN ('TEST')")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingParent("def1")
			.havingField("f1", "an..20", null, null, true, "label")
			.inFile("def2.xml");

		registerDataTypeProperty("http://test", mock(PropertyInstance.class));

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		GenericDefinitionImpl def1 = savedDefinitions.get(1);
		assertEquals("def1", def1.getIdentifier());

		PropertyDefinition def1Field = def1.getFields().get(0);
		assertEquals("f1", def1Field.getIdentifier());
		assertEquals("an..10", def1Field.getType());
		assertEquals(DisplayType.READ_ONLY, def1Field.getDisplayType());
		assertEquals("http://test", def1Field.getUri());

		assertFalse(def1Field.getConditions().isEmpty());

		GenericDefinitionImpl def2 = savedDefinitions.get(0);
		assertEquals("def2", def2.getIdentifier());

		PropertyDefinition def2Field = def2.getFields().get(0);
		assertEquals("f1", def2Field.getIdentifier());
		assertEquals("an..20", def2Field.getType());
		assertEquals(DisplayType.HIDDEN, def2Field.getDisplayType());
		assertEquals("FORBIDDEN", def2Field.getUri());

		assertTrue(def2Field.getConditions().isEmpty());
	}

	@Test
	public void should_RemoveFieldsMarkedForSkippingFromChildDefinition() {
		withDefinition("def1")
			.havingField("f1", "an..10", "readonly", null, "label")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingParent("def1")
			.havingField("f1", "an..10", "delete", null, "label")
			.inFile("def2.xml");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		GenericDefinitionImpl childDefinition = savedDefinitions.get(0);
		assertEquals("def2", childDefinition.getIdentifier());

		assertTrue(childDefinition.getFields().isEmpty());
	}

	@Test
	public void should_RemoveDisabledRegions() {
		withDefinition("def1")
			.havingRegion("r1", "editable")
			.havingField("f1", "an..10", "editable", null, "label")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingParent("def1")
			.havingRegion("r1", "system")
			.inFile("def2.xml");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		GenericDefinitionImpl def1 = savedDefinitions.get(1);
		assertEquals("def1", def1.getIdentifier());
		assertEquals(1, def1.getRegions().size());

		GenericDefinitionImpl def2 = savedDefinitions.get(0);
		assertEquals("def2", def2.getIdentifier());
		assertTrue(def2.getRegions().isEmpty());
	}

	@Test
	public void should_RemoveDisabledTransitions() {
		withDefinition("def1")
			.havingTransition("t1", null)
			.inFile("def1.xml");

		withDefinition("def2")
			.havingTransition("t1", "system")
			.inFile("def2.xml");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> importDefinitions = importDefinitions();

		GenericDefinitionImpl def1 = importDefinitions.get(1);
		assertEquals("def1", def1.getIdentifier());
		assertEquals(1, def1.getTransitions().size());

		GenericDefinitionImpl def2 = importDefinitions.get(0);
		assertEquals("def2", def2.getIdentifier());
		assertTrue(def2.getTransitions().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_NotAllowImportingBeforeFirstCallingValidationPhase() {
		withDefinition("def1").havingLabel("test.label", "a1").inFile("def1.xml");

		importDefinitions();
	}

	@Test
	public void should_ImportLabels() {
		withDefinition("def1").havingLabel("d1.label", "a1").inFile("def1.xml");
		withDefinition("def2").havingLabel("d2.label", "v").inFile("def2.xml");

		expectNoValidationErrors();

		importDefinitions();

		expectLabelsToBeSaved("d1.label", "d2.label");
	}

	@Test
	public void should_NotAcceptDuplicateLabels() {
		withDefinition("def1").havingLabel("test.label", "a1").inFile("def1.xml");
		withDefinition("def2").havingLabel("test.label", "v").inFile("def2.xml");

		expectValidationMessages(error("def1", DUPLICATED_LABELS, "def1", "<label>", "test.label", "[def1, def2]"));
	}

	@Test
	public void should_ImportFilters() {
		when(hashCalculator.computeHash(any())).then(invocation -> invocation.getArguments()[0].hashCode());

		withDefinition("def1").havingFilter("filter1", "f1")
							  .havingFilter("filter11", "f11")
							  .inFile("def1.xml");
		withDefinition("def2").havingFilter("filter2", "f2").inFile("def2.xml");

		withExistingFilter("filter1", "INCLUDE", "f1");

		withExistingFilter("filter2", "INCLUDE", "A0", "A2");

		expectNoValidationErrors();

		importDefinitions();

		expectFiltersToBeSaved("filter1", "filter11", "filter2");
	}

	@Test
	public void should_NotAcceptDuplicateFilters() {
		withDefinition("def1").havingFilter("test_filter", "a1").inFile("def1.xml");
		withDefinition("def2").havingFilter("test_filter", "v").inFile("def2.xml");

		expectValidationMessages(error("def1", DUPLICATED_LABELS, "def1", "<filter>", "test_filter", "[def1, def2]"));
	}

	@Test
	public void should_NotAcceptMultipleDefinitionsWithSameId() {
		withDefinition("def1").inFile("def1.xml");
		withDefinition("def1").inFile("def2.xml");

		expectValidationMessages(error("def1", DUPLICATED_DEFINITIONS, "def1"));
	}

	@Test
	public void should_NotAcceptDefinitionsWithMissingParent() {
		withDefinition("def1").havingParent("def2").inFile("def1.xml");

		expectValidationMessages(error("def1", MISSING_PARENT, "def1", "def2"));
	}

	@Test
	public void should_NotAcceptDefinitionsHierarchyCycles() {
		withDefinition("d1").havingParent("d3").inFile("def1.xml");
		withDefinition("d2").havingParent("d1").inFile("def2.xml");
		withDefinition("d3").havingParent("d2").inFile("def3.xml");

		expectValidationMessages(
				error("d1", HIERARCHY_CYCLE, "d1", "[d1, d3, d2, d1]"),
				error("d2", HIERARCHY_CYCLE, "d2", "[d2, d1, d3, d2]"),
				error("d3", HIERARCHY_CYCLE, "d3", "[d3, d2, d1, d3]"));
	}

	@Test
	public void should_NotAllowTwoFieldsWithSameUri() {
		withDefinition("def1")
			.havingField("f1", "an..10", "editable", "emf:testUri", "label")
			.havingField("f2", "an..10", "editable", "emf:testUri", "label")
			.inFile("def1.xml");

		registerDataTypeProperty("emf:testUri", mock(PropertyInstance.class));

		expectValidationMessages(error("def1", DUPLICATED_URI, "def1", "emf:testUri", "[f1, f2]"));
	}

	/**
	 * Tests that during the formal validation (validation performed before compilation) the definitions with errors
	 * are skipped and only those who don't have errors are processed further.
	 */
	@Test
	public void should_ReportErrorsOfTwoIndependentDefinitions_WhenTheFirstHasFormalAndTheSecondHasLogicalErrors() {
		withDefinition("def1")
			.havingField("f1", "an..10", "editable", "emf:testUri", "label")
			.havingField("f2", "an..10", "editable", "emf:testUri", "label")
			.inFile("def1.xml");

		withDefinition("def2")
				.havingParent("non_existing_parent")
				.inFile("def2.xml");

		withNewDefinition("def3.xml", "non_xml");
		registerDataTypeProperty("emf:testUri", mock(PropertyInstance.class));

		expectValidationMessages(
				error(null, XML_PARSING_FAILURE, "def3.xml", "[On line 1, column 1 : Content is not allowed in prolog.]"),
				error("def1", DUPLICATED_URI, "def1", "emf:testUri", "[f1, f2]"),
				error("def2", MISSING_PARENT, "def2", "non_existing_parent"));
	}

	/**
	 * If there is an ancestor with errors from formal validation then the whole hierarchy
	 * should be skipped from logical validation.
	 */
	@Test
	public void should_HandleCases_WhereThereIsDeepHierarchyWhichRootContainsErrors() {
		withDefinition("a3")
			.havingParent("a2")
			.inFile("a3.xml");

		withDefinition("a1")
			.inFile("a1.xml");

		withDefinition("a2")
			.havingParent("a1")
			.inFile("a2.xml");

		withDefinition("a1")
			.inFile("a11.xml");

		withDefinition("def1")
			.havingField("f1", "an..10", "editable", "emf:testUri", "label")
			.havingField("f2", "an..10", "editable", "emf:testUri", "label")
			.inFile("def1.xml");

		registerDataTypeProperty("emf:testUri", mock(PropertyInstance.class));

		expectValidationMessages(
				error("a1", DUPLICATED_DEFINITIONS, "a1"),
				error("a3", MISSING_PARENT, "a3", "a2"),
				error("a2", MISSING_PARENT, "a2", "a1"),
				error("def1", DUPLICATED_URI, "def1", "emf:testUri", "[f1, f2]"));
	}

	@Test
	public void should_NotAllowFieldsWithSameId() {
		withDefinition("d1")
				.havingRegion("info", "editable")
					.havingField("field1", "an..100", "readonly", null, "Field 1")
				.havingRegion("info2", "editable")
					.havingField("field1", "an..100", "readonly", null, "Field 1")
				.inFile("def1.xml");

		withDefinition("d2")
				.havingField("label", "an..100", "readonly", null, "label")
				.havingRegion("info", "editable")
					.havingField("label", "an..100", "readonly", null, "label")
				.inFile("def2.xml");

		expectValidationMessages(
				error("d1", DUPLICATED_FIELDS, "d1", "field1"),
				error("d2", DUPLICATED_FIELDS, "d2", "label"));
	}

	@Test
	public void should_NotProcessNonXmlFiles() {
		withDefinition("def1").inFile("def1.xml");
		withDefinition("def1").inFile("def2.txt");

		expectNoValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		assertEquals(1, savedDefinitions.size());
	}

	@Test
	public void should_NotAcceptDefinitionWithDuplicateName() {
		withNewDefinition("def1.xml", "test");
		withNewDefinition("test/def1.xml", "test");

		expectValidationMessages(error(null, "Duplicate file names found: def1.xml, test/def1.xml in the provided definitions"));
	}

	@Test
	public void should_NotAcceptDefinitionsWithXSDValidationErrors() {
		withNewDefinitionFromFile("def1.xml", "invalid_definition.xml");

		expectValidationMessages(
				error(null, XML_PARSING_FAILURE, "def1.xml",
						"[On line 2, column 17 : cvc-elt.1: Cannot find the declaration of element 'notADefinition'.]"));
	}

	@Test
	public void should_NotAllowImportDefinition_When_ThereAreInvalidValuesOfUries() {
		withNewDefinitionFromFile("def1.xml", "definition_invalid_uri_validation.xml");

		List<String> expectedErrors = new LinkedList<>();
		expectedErrors.add("On line 8, column 107 : cvc-pattern-valid: Value 'fieldWithoutPrefix' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		expectedErrors.add("On line 8, column 107 : cvc-attribute.3: The value 'fieldWithoutPrefix' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		expectedErrors.add("On line 9, column 112 : cvc-pattern-valid: Value ':fieldWithEmptyPrefix' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		expectedErrors.add("On line 9, column 112 : cvc-attribute.3: The value ':fieldWithEmptyPrefix' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		expectedErrors.add("On line 10, column 101 : cvc-pattern-valid: Value 'prefixOnly:' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		expectedErrors.add("On line 10, column 101 : cvc-attribute.3: The value 'prefixOnly:' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		expectedErrors.add("On line 12, column 107 : cvc-pattern-valid: Value 'emf:fieldWithSpace1 ' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		expectedErrors.add("On line 12, column 107 : cvc-attribute.3: The value 'emf:fieldWithSpace1 ' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		expectedErrors.add("On line 13, column 107 : cvc-pattern-valid: Value 'emf: fieldWithSpace2' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		expectedErrors.add("On line 13, column 107 : cvc-attribute.3: The value 'emf: fieldWithSpace2' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		expectedErrors.add("On line 14, column 104 : cvc-pattern-valid: Value ' :fieldWithSpace3' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		expectedErrors.add("On line 14, column 104 : cvc-attribute.3: The value ' :fieldWithSpace3' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");

		expectValidationMessages(
				error(null, XML_PARSING_FAILURE, "def1.xml", "[" + String.join(", ", expectedErrors) + "]")
		);
	}

	@Test
	public void should_NotAcceptDefinitions_HavingFields_WithMalformedBooleanValue() {
		withNewDefinitionFromFile("def1.xml", "definition_malformed_boolean.xml");

		registerDataTypeProperty("dcterms:label", mock(PropertyInstance.class));

		List<ValidationMessage> errorMessages = new LinkedList<>();
		// configurations
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "malformedBooleanField", "test"));
		// error messages for fields
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "correctBooleanFieldTRUE", "TRUE"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "correctBooleanFieldTrue", "True"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "correctBooleanFieldFALSE", "FALSE"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "correctBooleanFieldFalse", "False"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "malformedBooleanField", "leliya Gica"));
		// error messages for regions
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "regionMalformedBooleanFieldTRUE", "TRUE"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "regionMalformedBooleanFieldTrue", "True"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "regionMalformedBooleanFieldFALSE", "FALSE"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "regionMalformedBooleanFieldFalse", "False"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "regionMalformedBooleanField", "leliya Gica"));
		// error messages for transitions
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "transitionCorrectBooleanFieldTRUE", "TRUE"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "transitionCorrectBooleanFieldTrue", "True"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "transitionCorrectBooleanFieldFALSE", "FALSE"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "transitionCorrectBooleanFieldFalse", "False"));
		errorMessages.add(error("malformedBooleanDef", FIELD_WITH_INVALID_BOOLEAN, "malformedBooleanDef", "transitionMalformedBooleanField", "leliya Gica"));

		expectValidationMessages(errorMessages);
	}

	@Test
	public void should_NotAcceptDefinition_HavingFieldsWithSameNameInTransition() {
		// GIVEN:
		// Definition "duplicateFieldNameInTransition" has a transition which have two fields with same name.

		// WHEN
		// Definition is imported.
		withNewDefinitionFromFile("def1.xml", "definition_duplicated_field_name_in_transition.xml");

		// THEN:
		// error message have to be generated.
		expectValidationMessages(
				error("duplicateFieldNameInTransition", DUPLICATE_TRANSITION_FIELD, "duplicateFieldNameInTransition", "addWatchers",
						"sendMail"));
	}

	@Test
	public void should_NotAcceptDefinition_HavingFields_WithMismatchedRangeBetweenDefinitionAndSemantic() {
		// GIVEN:
		// We have definition with id "rangeValidationDef" and fields:
		// A field "fieldWithoutRange", this field has not registered range.
		// A field "fieldWithNotRegisteredUri", this field has range but its uri is not registered.

		// A field "fieldWithEmptyRange", this field has empty range and its uri is registered.
		PropertyInstance modifiedByRelation = new PropertyInstance();
		modifiedByRelation.setId("emf:modifiedBy");
		registerObjectProperty("emf:modifiedBy", modifiedByRelation);

		// A field "fieldWithRange", this field has not empty range, but range is in mismatch with semantic range.
		PropertyInstance createdByRelation = new PropertyInstance();
		createdByRelation.setId("emf:createdBy");
		createdByRelation.setRangeClass(AGENT_SHORT_URI);
		registerObjectProperty("emf:createdBy", createdByRelation);

		// A field "fieldWithNonRegisteredSemanticRange", this field has range and semantic has not range.
		PropertyInstance nonRegisteredSemanticRange = new PropertyInstance();
		nonRegisteredSemanticRange.setId("emf:nonRegisteredSemanticRange");
		nonRegisteredSemanticRange.setRangeClass("emf:MissingSemanticRange");
		registerObjectProperty("emf:nonRegisteredSemanticRange", nonRegisteredSemanticRange);

		// Semantic class "emf:Vendor".
		ClassInstance vendor = new ClassInstance();
		vendor.setId(VENDOR_FULL_URI);

		// Semantic class "emf:Customer".
		ClassInstance customer = new ClassInstance();
		vendor.setId(CUSTOMER_FULL_URI);

		// Semantic class "ptop:Agent", this class has two sub classes: "emf:Vendor" and "emf:Customer".
		ClassInstance agent = new ClassInstance();
		agent.setId(AGENT_FULL_URI);
		Map<String, ClassInstance> agentSubclasses = new HashMap<>(2);
		agentSubclasses.put(VENDOR_FULL_URI, vendor);
		agentSubclasses.put(CUSTOMER_FULL_URI, customer);
		agent.setSubClasses(agentSubclasses);

		// Semantic class "ptop:Agent" is registered as object property.
		when(semanticDefinitionService.getClassInstance(AGENT_SHORT_URI)).thenReturn(agent);
		// Semantic class "emf:DataProperty" is registered as data property.
		registerDataTypeProperty("emf:DataProperty", mock(PropertyInstance.class));

		// When:
		// Definition is imported.
		withNewDefinitionFromFile("def1.xml", "definition_range_validation.xml");

		// Then:
		// We expect:
		expectValidationMessages(
				// A error that "emf:notRegisteredUri" is not found.
				error("rangeValidationDef", MISSING_RELATION, "rangeValidationDef", "fieldWithNotRegisteredUri", "emf:notRegisteredUri"),
				// A error that "" is not sub class of range specified in semantic.
				error("rangeValidationDef", RELATION_MISMATCH, "rangeValidationDef", "fieldWithRange", "emf:Document", AGENT_FULL_URI),
				// A error "emf:createdBy"  is not registered in semantic.
				error("rangeValidationDef", UNREGISTERED_URI, "rangeValidationDef", "fieldWithRange", "emf:createdBy", "emf:NotExistingUri"),
				// A error that specified range "emf:MissingSemanticRange" is not found in semantic.
				error("rangeValidationDef", UNREGISTERED_SEMANTIC_RANGE, "rangeValidationDef", "fieldWithNonRegisteredSemanticRange",
						"emf:nonRegisteredSemanticRange", "emf:MissingSemanticRange"),
				// A error that object property "emf:DataProperty" is used as object property.
				error("rangeValidationDef", USING_DATA_PROPERTY_AS_OBJECT, "rangeValidationDef", "author", "emf:DataProperty")
		);
	}

	@Test
	public void should_NotAcceptDefinitions_HavingDataTypeField_WithWrongUri() {
		// GIVEN:
		// We have definition with id "dataTypeValidationDef" and fields:
		// A field "fieldWithNotRegisteredUri", it is with uri not registered into semantic.
		// A field "fieldWithObjectPropertyUri", it is with uri related to object property.
		registerObjectProperty("emf:objectProperty", mock(PropertyInstance.class));
		// A field "fieldWithDataTypePropertyUri", it is with uri related to data type property.
		registerDataTypeProperty("emf:dataTypeProperty", mock(PropertyInstance.class));

		// When:
		// Definition is imported.
		withNewDefinitionFromFile("def1.xml", "definition_data_type_field_validation.xml");

		// Then:
		// We expect:
		expectValidationMessages(
				error("dataTypeValidationDef", OBJECT_PROPERTY_AS_DATA, "dataTypeValidationDef", "fieldWithObjectPropertyUri",
						"emf:objectProperty"),
				error("dataTypeValidationDef", UNREGISTERED_PROPERTY_URI, "dataTypeValidationDef", "fieldWithNotRegisteredUri",
						"emf:notRegisteredUri")
		);
	}

	@Test
	public void should_NotAcceptDefinitions_HavingFields_WithMalformedJSONValue() {
		withNewDefinitionFromFile("def1.xml", "definition_malformed_json.xml");

		registerDataTypeProperty("dcterms:label", mock(PropertyInstance.class));
		registerDataTypeProperty("emf:test", mock(PropertyInstance.class));

		expectValidationMessages(
				error("malformedJsonDef", FIELD_WITH_INVALID_JSON, "malformedJsonDef", "malformedJsonField",
						"Invalid token=EOF at (line no=1, column no=60, offset=59). Expected tokens are: [COMMA]"),
				error("malformedJsonDef", FIELD_WITH_INVALID_JSON, "malformedJsonDef", "type",
						"Unexpected char 111 at (line no=1, column no=4, offset=3), expecting 'a'"),
				error("malformedJsonDef", FIELD_WITH_INVALID_JSON, "malformedJsonDef", "misspelledBooleanField",
						"Unexpected char 32 at (line no=1, column no=20, offset=19), expecting 'e'"),
				error("malformedJsonDef", FIELD_WITH_INVALID_JSON, "malformedJsonDef", "attachToConfig",
						"Invalid token=EOF at (line no=3, column no=129, offset=173). Expected tokens are: [COMMA]"),
				error("malformedJsonDef", FIELD_WITH_INVALID_JSON, "malformedJsonDef", "testOne",
						"Invalid token=STRING at (line no=3, column no=18, offset=61). Expected tokens are: [COMMA]"));
	}

	@Test
	public void should_NotAcceptDefinition_HavingFiltersOnFieldWithUriEmfType() {
		withNewDefinitionFromFile("def1.xml", "definition_filter_validation.xml");

		expectValidationMessages(
				error("filterDefinitionsDefinition", FIELD_WITH_NON_SUPPORTED_FILTERING, "filterDefinitionsDefinition", "type",
						"emf:type"),
				error("filterDefinitionsDefinition", FIELD_WITH_NON_SUPPORTED_FILTERING, "filterDefinitionsDefinition", "status",
						"emf:status"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowErrorOnMissingImportDirectory() {
		definitionImportService.validate(rootDirectory.resolve("/__not_exists__"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_ThrowErrorOnEmptyImportDirectory() {
		definitionImportService.validate(rootDirectory);
	}

	@Test(expected = NullPointerException.class)
	public void should_ThrowErrorWhenNoDirectoryIsProvided() {
		definitionImportService.validate(null);
	}

	@Test
	public void should_ProvideInformationAboutImportedDefinitions() {
		List<Object> rows = new ArrayList<>();

		Date now = new Date();
		rows.add(new Object[] {"def1", "def1.xml", "admin", now, (short) 1 });

		when(dbDao.fetchWithNamed(eq(DefinitionEntry.QUERY_FETCH_IMPORTED_DEFINITIONS_KEY), eq(Collections.emptyList())))
			.thenReturn(rows);

		List<DefinitionInfo> importedDefinitions = definitionImportService.getImportedDefinitions();

		assertEquals(1, importedDefinitions.size());

		DefinitionInfo definitionInfo = importedDefinitions.get(0);

		assertEquals("def1", definitionInfo.getId());
		assertEquals("def1.xml", definitionInfo.getFileName());
		assertTrue(definitionInfo.isAbstract());
		assertEquals(now, definitionInfo.getModifiedOn());
		assertEquals("admin", definitionInfo.getModifiedBy());
	}

	@Test
	public void should_Export_All_Definitions() throws IOException {
		withExistingDefinitionFromFile("def1.xml", "<definition>Content of def1</definition>");
		withExistingDefinitionFromFile("def2.xml", "<definition>Content of def2</definition>");
		withExistingDefinitionFromFile("def3.xml", "<definition>sadadsadasdasdasd</definition>");

		List<File> exported = definitionImportService.exportAllDefinitions();
		verifyDefinitionsExported(exported);
	}

	@Test
	public void should_Export_Requested_Definitions() throws IOException {
		withExistingDefinitionFromFile("def1.xml", "<definition>Content of def1</definition>");
		withExistingDefinitionFromFile("def2.xml", "<definition>Content of def2</definition>");
		withExistingDefinitionFromFile("def3.xml", "<definition>sadadsadasdasdasd</definition>");

		List<File> exported = definitionImportService.exportDefinitions(Arrays.asList("def1", "def2", "def3"));

		verifyIdsRequested("def1", "def2", "def3");
		verifyDefinitionsExported(exported);
	}

	@Inject
	private DefinitionImportServiceImpl definitionImportService;

	@Produces
	@Mock
	private DbDao dbDao;

	// not used but required by GenericDefinitionCompilerCallback
	@Produces
	@Mock
	private DefinitionManagementService definitionManagementService;

	@Produces
	@Mock
	private DefinitionService definitionService;

	@Produces
	@Mock
	private MutableDefinitionService mutableDefinitionService;

	// real implementation should be used when HashCalculator is moved to commons module
	@Produces
	@Mock
	private HashCalculator hashCalculator;

	private TemporaryFolder tempFolder = new TemporaryFolder();
	{
		try {
			tempFolder.create();
		} catch (IOException e) {
			throw new EmfApplicationException("Failed to creatae temp folder", e);
		}
	}

	@Produces
	private TempFileProvider tempFileProvider = new TempFileProviderFake(tempFolder.getRoot());

	@Produces
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Produces
	private EntityLookupCacheContext cacheContext = new EntityLookupCacheContextFake(new InMemoryCacheProvider());

	@Produces
	private TaskExecutor taskExecutor = new TaskExecutorFake();

	@Inject
	private TypeConverter typeConverter;

	@Produces
	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Before
	public void init() {
		FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
		rootDirectory = fs.getPath("/");

		when(definitionService.getDataTypeDefinition(anyString())).thenReturn(mock(DataTypeDefinition.class));

		when(dbDao.saveOrUpdate(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

		when(mutableDefinitionService.savePropertyIfChanged(any(), any())).thenAnswer(
				invocation -> invocation.getArguments()[0]);

		when(dbDao.fetchWithNamed(eq(DefinitionContent.FETCH_CONTENT_OF_ALL_DEFINITIONS_KEY), anyList())).thenReturn(
				existingDefinitionFiles);

		when(dbDao.fetchWithNamed(eq(DefinitionContent.FETCH_CONTENT_BY_DEFINITION_IDS_KEY), anyList())).thenReturn(
				existingDefinitionFiles);

		when(dbDao.fetchWithNamed(eq(FilterDefinitionImpl.QUERY_FILTERS_BY_ID_KEY), anyList())).thenReturn(
				existingFilters);

		when(dbDao.find(eq(DefinitionContent.class), anyString())).then(a -> existingDefinitionFiles.stream()
				.filter(storedContent -> nullSafeEquals(storedContent.getId(), a.getArgumentAt(1, String.class)))
				.findFirst()
				.orElse(null));

		typeConverter.addConverter(String.class, Uri.class, (shortUri) -> {
			if (namespaces.containsKey(shortUri)) {
				return namespaces.get(shortUri);
			}
			throw new IllegalStateException();
		});
	}

	private static String VENDOR_FULL_URI = "htt:/ittruse.ittbg.com/ontology/enterpriseManagementFramework#Vendor";
	private static String VENDOR_SHORT_URI = "emf:Vendor";
	private static String DOCUMENT_FULL_URI = "htt:/ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document";
	private static String DOCUMENT_SHORT_URI = "emf:Document";
	private static String CUSTOMER_FULL_URI = "htt:/ittruse.ittbg.com/ontology/enterpriseManagementFramework#Customer";
	private static String CUSTOMER_SHORT_URI = "emf:Customer";
	private static String AGENT_FULL_URI = "http://www.ontotext.com/proton/protontop#Agent";
	private static String AGENT_SHORT_URI = "ptop:Agent";
	private static String ENTITY_FULL_URI = "http://www.ontotext.com/proton/protontop#Entity";
	private static String ENTITY_SHORT_URI = "ptop:Entity";

	private static final Pattern DEFINITION_ID_SELECTOR = Pattern.compile("<definition\\s.*?id=\"(\\w+)\"\\s?.*?>");

	private static Map<String, Uri> namespaces = new HashMap<>();

	static {
		namespaces.put(VENDOR_SHORT_URI, new ShortUri(VENDOR_FULL_URI));
		namespaces.put(DOCUMENT_SHORT_URI, new ShortUri(DOCUMENT_FULL_URI));
		namespaces.put(CUSTOMER_SHORT_URI, new ShortUri(CUSTOMER_FULL_URI));
		namespaces.put(AGENT_SHORT_URI, new ShortUri(AGENT_FULL_URI));
		namespaces.put(ENTITY_SHORT_URI, new ShortUri(ENTITY_FULL_URI));
	}

	@After
	public void after() {
		tempFolder.delete();
	}

	private boolean allDefinitionsLoadedEventFired = false;

	void captureDefinitionsLoadedEvent(@Observes DefinitionsChangedEvent event) {
		allDefinitionsLoadedEventFired = true;
	}

	private void expectLabelsToBeSaved(String... expectedLabels) {
		assertEquals(expectedLabels.length, savedLabels.size());

		Set<String> savedLabelIds = savedLabels.stream()
											 .map(LabelDefinition::getIdentifier)
											 .collect(Collectors.toSet());
		for (String expectedLabel : expectedLabels) {
			assertTrue("Expected label '" + expectedLabel + "' to be saved",
					savedLabelIds.contains(expectedLabel));
		}

		for (LabelDefinition label : savedLabels) {
			assertNotNull(label.getDefinedIn());
			assertFalse("Should have defined in value", label.getDefinedIn().isEmpty());
		}
	}

	private void expectFiltersToBeSaved(String... expectedFilters) {
		assertEquals(expectedFilters.length, savedFilters.size());

		Set<String> savedFilterIds = savedFilters.stream()
											   .map(Filter::getIdentifier)
											   .collect(Collectors.toSet());

		for (String expectedLabel : expectedFilters) {
			assertTrue("Expected filter '" + expectedLabel + "' to be saved",
					savedFilterIds.contains(expectedLabel));
		}

		for (Filter filter : savedFilters) {
			assertNotNull(filter.getDefinedIn());
			assertFalse("Should have defined in value", filter.getDefinedIn().isEmpty());
		}
	}

	private void expectValidationMessages(ValidationMessage... expectedMessages) {
		expectValidationMessages(Arrays.asList(expectedMessages));
	}

	private void expectValidationMessages(List<ValidationMessage> expectedMessages) {
		ValidationReport validationReport = definitionImportService.validate(rootDirectory).getValidationReport();
		if (expectedMessages.size() != validationReport.getValidationMessages().size()) {
			fail();
		}
		assertTrue(validationReport.getValidationMessages().containsAll(expectedMessages));
	}

	private void expectNoValidationErrors() {
		expectValidationMessages();
	}

	private List<DefinitionContent> savedContentEntries = new ArrayList<>();

	private List<Filter> savedFilters = new ArrayList<>();

	private List<LabelDefinition> savedLabels = new ArrayList<>();

	private List<GenericDefinitionImpl> importDefinitions() {
		definitionImportService.importDefinitions(rootDirectory);

		ArgumentCaptor<Entity<?>> dbDaoCaptor = ArgumentCaptor.forClass(Entity.class);
		// the invocation count is irrelevant in this case. The captured value is asserted
		verify(dbDao, atMost(1000)).saveOrUpdate(dbDaoCaptor.capture());

		dbDaoCaptor.getAllValues().forEach(entity -> {
			if (entity instanceof DefinitionContent) {
				savedContentEntries.add((DefinitionContent) entity);
			} else if (entity instanceof Filter) {
				savedFilters.add((Filter) entity);
			} else if (entity instanceof LabelDefinition) {
				savedLabels.add((LabelDefinition) entity);
			} else {
				throw new IllegalStateException("Uknown type is persisted " + entity.getClass());
			}
		});

		ArgumentCaptor<GenericDefinitionImpl> captor = ArgumentCaptor.forClass(GenericDefinitionImpl.class);

		verify(mutableDefinitionService, atLeastOnce()).saveDefinition(captor.capture());

		assertTrue("AllDefinitionsLoaded event should be fired", allDefinitionsLoadedEventFired);

		return captor.getAllValues();
	}

	private void withExistingSavedDefinition(GenericDefinition definition) {
		if (definition.getRevision() == null) {
			definition.setRevision(1L);
		}

		when(definitionService.find(eq(definition.getIdentifier()))).thenReturn(definition);
	}

	private Path rootDirectory;

	private void withNewDefinition(String definitionPath, String content) {
		Path filePath = rootDirectory.resolve(definitionPath);
		try {
			Files.createDirectories(filePath.getParent());

			Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<DefinitionContent> existingDefinitionFiles = new ArrayList<>();

	private void withExistingDefinitionFromFile(String definitionPath, String content) {
		DefinitionContent contentEntity = new DefinitionContent();
		contentEntity.setFileName(definitionPath);
		contentEntity.setContent(content);

		Matcher matcher = DEFINITION_ID_SELECTOR.matcher(content);
		if (matcher.find()) {
			contentEntity.setId(matcher.group(1));
		}

		existingDefinitionFiles.add(contentEntity);
	}

	private DefinitionBuilder withExistingDefinition(String id) {
		return new DefinitionBuilder(id, this::withExistingDefinitionFromFile);
	}

	private void withNewDefinitionFromFile(String definitionPath, String sourceFile) {
		String content = getFileContent(sourceFile);
		withNewDefinition(definitionPath, content);
	}

	private String getFileContent(String file) {
		try {
			return IOUtils.toString(this.getClass().getResourceAsStream(file));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private DefinitionBuilder withDefinition(String id) {
		return new DefinitionBuilder(id, this::withNewDefinition);
	}

	private static void verifyField(PropertyDefinition field, String id, String type, DisplayType displayType, String uri) {
		assertEquals(id, field.getIdentifier());
		assertEquals(type, field.getType());
		assertEquals(displayType, field.getDisplayType());

		assertEquals(uri, field.getUri());
	}

	private static void verifyField(PropertyDefinition field, String id, String type, DisplayType displayType, String uri, String defaultValue) {
		verifyField(field, id, type, displayType, uri);

		assertEquals(defaultValue, field.getDefaultValue());
	}

	private void verifyIdsRequested(String... ids) {
		ArgumentCaptor<List<Pair<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
		verify(dbDao).fetchWithNamed(eq(DefinitionContent.FETCH_CONTENT_BY_DEFINITION_IDS_KEY), captor.capture());

		assertEquals(Arrays.asList(ids), captor.getValue().get(0).getSecond());
	}

	private void verifyDefinitionsExported(List<File> exportedActual) throws IOException {
		assertEquals("The number of exported definition files is not as expected", existingDefinitionFiles.size(),
				exportedActual.size());

		for (int i = 0; i < exportedActual.size(); i++) {
			String fileContentActual = FileUtils.readFileToString(exportedActual.get(i));
			String fileNameActual = exportedActual.get(i).getName();

			assertEquals("Content of exported file is not as expected", existingDefinitionFiles.get(i).getContent(),
					fileContentActual);
			assertEquals("File name of exported file is not as expected", existingDefinitionFiles.get(i).getFileName(),
					fileNameActual);
		}
	}

	private class DefinitionBuilder {
		private Definition definition = new Definition();
		private BiConsumer<String, String> storeFunction;
		private RegionDefinition currentRegion;
		private ComplexFieldDefinition currentField;

		public DefinitionBuilder(String id, BiConsumer<String, String> storeFunction) {
			this.storeFunction = storeFunction;
			definition.setId(id);
			definition.setType(ObjectType.OBJECT);
		}

		public DefinitionBuilder havingParent(String parentId) {
			definition.setParentId(parentId);
			return this;
		}

		public DefinitionBuilder havingTransition(String id, String displayType) {
			TransitionDefinition transition = new TransitionDefinition();
			transition.setId(id);
			transition.setDisplayType(displayType);

			if (definition.getTransitions() == null) {
				definition.setTransitions(new TransitionsDefinition());
			}

			definition.getTransitions().getTransition().add(transition);
			return this;
		}

		public DefinitionBuilder havingRegion(String id, String displayType) {
			currentRegion = new RegionDefinition();
			currentRegion.setId(id);
			currentRegion.setDisplayType(displayType);

			if (definition.getRegions() == null) {
				definition.setRegions(new RegionsDefinition());
			}

			definition.getRegions().getRegion().add(currentRegion);

			// reset state
			currentField = null;

			return this;
		}

		public DefinitionBuilder havingField(String name, String type, String displayType, String uri, String label) {
			return havingField(name, type, displayType, uri, null, label);
		}

		public DefinitionBuilder havingField(String name, String type, String displayType, String uri, Boolean override, String label) {
			ComplexFieldDefinition field = constructField(name, type, displayType, uri, override, null, label);

			if (currentRegion != null) {
				if (currentRegion.getFields() == null) {
					currentRegion.setFields(new ComplexFieldsDefinition());
				}

				currentRegion.getFields().getField().add(field);
			} else {
				if (definition.getFields() == null) {
					definition.setFields(new ComplexFieldsDefinition());
				}

				definition.getFields().getField().add(field);
			}

			currentField = field;

			return this;
		}

		public DefinitionBuilder havingConfigurationField(String name, String type, String displatType, String uri, Boolean override, String value, String label) {
			ComplexFieldDefinition field = constructField(name, type, displatType, uri, override, value, label);

			if (definition.getConfiguration() == null) {
				definition.setConfiguration(new Configuration());
			}

			if (definition.getConfiguration().getFields() == null) {
				definition.getConfiguration().setFields(new ComplexFieldsDefinition());
			}

			definition.getConfiguration().getFields().getField().add(field);

			return this;
		}

		private ComplexFieldDefinition constructField(String name, String type, String displatType, String uri,
				Boolean override, String defaultValue, String label) {
			ComplexFieldDefinition field = new ComplexFieldDefinition();
			field.setName(name);
			field.setType(type);
			field.setDisplayType(displatType);
			field.setUri(uri);
			field.setOverride(override);
			field.setValue(defaultValue);
			field.setLabel(label);
			return field;
		}

		public DefinitionBuilder withCondition(String id, String value) {
			ConditionDefinition condition = new ConditionDefinition();
			condition.setId(id);
			condition.setValue(value);

			if (currentField != null) {
				currentField.getCondition().add(condition);
			}

			return this;
		}

		public DefinitionBuilder havingLabel(String id, String value) {
			if (definition.getLabels() == null) {
				definition.setLabels(new Labels());
			}

			Label label = new Label();
			label.setId(id);

			LabelValue labelValue = new LabelValue();
			labelValue.setLang("en");
			labelValue.setValue(value);

			label.getValue().add(labelValue);

			definition.getLabels().getLabel().add(label);

			return this;
		}

		public DefinitionBuilder havingFilter(String id, String value) {
			if (definition.getFilterDefinitions() == null) {
				definition.setFilterDefinitions(new FilterDefinitions());
			}

			FilterDefinition filter = new FilterDefinition();
			filter.setId(id);
			filter.setValue(value);

			definition.getFilterDefinitions().getFilter().add(filter);

			return this;
		}

		public void inFile(String definitionPath) {
			String content = JAXBHelper.toXml(definition);

			storeFunction.accept(definitionPath, content);
		}
	}

	private List<FilterDefinitionImpl> existingFilters = new ArrayList<>();

	private void withExistingFilter(String id, String mode, String... values) {
		FilterDefinitionImpl filter = new FilterDefinitionImpl();
		filter.setIdentifier(id);
		filter.setMode(mode);
		filter.setFilterValues(new HashSet<>(Arrays.asList(values)));

		existingFilters.add(filter);
	}

	private void registerDataTypeProperty(String uri, PropertyInstance propertyInstance) {
		when(semanticDefinitionService.getProperty(uri)).thenReturn(propertyInstance);
	}

	private void registerObjectProperty(String uri, PropertyInstance propertyInstance) {
		when(semanticDefinitionService.getRelation(uri)).thenReturn(propertyInstance);
	}
}
