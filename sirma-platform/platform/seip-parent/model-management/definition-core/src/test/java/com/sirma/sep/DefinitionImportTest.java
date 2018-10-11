package com.sirma.sep;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.mockito.Mockito;

import com.google.common.jimfs.Jimfs;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.cache.InMemoryCacheProvider;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
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
	public void should_UpdateExistingDefinitionIncreasingItsRevisonNumber() {
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
	public void should_NotSaveExistingDefinitionsThatAreNotReimported() {
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
		PropertyDefinition config1 = def1.getConfiguration(CONFIG_NAME).get();
		verifyField(config1, CONFIG_NAME, "boolean", DisplayType.SYSTEM, null, "true");

		GenericDefinitionImpl def2 = savedDefinitions.get(0);

		assertEquals(3, def2.getConfigurations().size());
		PropertyDefinition config2 = def2.getConfiguration(CONFIG_NAME).get();
		verifyField(config2, CONFIG_NAME, "boolean", DisplayType.SYSTEM, null, "false");

		// also test if the functionality handles properties that don't exist in the parent
		PropertyDefinition testConfig = def2.getConfiguration("test_config").get();
		verifyField(testConfig, "test_config", "an..20", DisplayType.EDITABLE, null, "test");

		// test if non-predefined configurations also get inherited
		PropertyDefinition parentConfig = def2.getConfiguration("parent_config").get();
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
	public void should_SupportCompleteOverridingOfParentFieldInTheChildDefinition() {
		withDefinition("def1")
			.havingField("f1", "an..10", "readonly", "http://test", "label")
				.withCondition("mandatory", "[f1] IN ('TEST')")
			.inFile("def1.xml");

		withDefinition("def2")
			.havingParent("def1")
			.havingField("f1", "an..20", null, null, true, "label")
			.inFile("def2.xml");

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

		expectLabelsToBeSaved("d1.label","d2.label");
	}

	@Test
	public void should_NotAcceptDuplicateLabels() {
		withDefinition("def1").havingLabel("test.label", "a1").inFile("def1.xml");
		withDefinition("def2").havingLabel("test.label", "v").inFile("def2.xml");

		expectValidationErrors("Duplicate label");
	}

	@Test
	public void should_ImportFilters() {
		when(hashCalculator.computeHash(any())).then(invokation -> invokation.getArguments()[0].hashCode());

		withDefinition("def1").havingFilter("filter1", "f1")
							  .havingFilter("filter11", "f11")
							  .inFile("def1.xml");
		withDefinition("def2").havingFilter("filter2", "f2").inFile("def2.xml");

		withExistingFilter("filter1", "INCLUDE", "f1");

		withExistingFilter("filter2", "INCLUDE", "A0", "A2");

		expectNoValidationErrors();

		importDefinitions();

		expectFiltersToBeSaved("filter11", "filter2");
	}

	@Test
	public void should_NotAcceptDuplicateFilters() {
		withDefinition("def1").havingFilter("test_filter", "a1").inFile("def1.xml");
		withDefinition("def2").havingFilter("test_filter", "v").inFile("def2.xml");

		expectValidationErrors("Duplicate filter");
	}

	@Test
	public void should_NotAcceptMultipleDefinitionsWithSameId() {
		withDefinition("def1").inFile("def1.xml");
		withDefinition("def1").inFile("def2.xml");

		expectValidationErrors("Multiple definitions");
	}

	@Test
	public void should_NotAcceptDefinitionsWithMissingParent() {
		withDefinition("def1").havingParent("def2").inFile("def1.xml");

		expectValidationErrors("The parent definition 'def2' of definition 'def1'");
	}

	@Test
	public void should_NotAcceptDefinitionsHierarchyCycles() {
		withDefinition("d1").havingParent("d3").inFile("def1.xml");
		withDefinition("d2").havingParent("d1").inFile("def2.xml");
		withDefinition("d3").havingParent("d2").inFile("def3.xml");

		expectValidationErrors("Definition 'd1' contains hierarchy cycle",
				"Definition 'd2' contains hierarchy cycle",
				"Definition 'd3' contains hierarchy cycle");
	}

	@Test
	public void should_NotAllowTwoFieldsWithSameUri() {
		withDefinition("def1")
			.havingField("f1", "an..10", "editable", "emf:testUri", "label")
			.havingField("f2", "an..10", "editable", "emf:testUri", "label")
			.inFile("def1.xml");

		expectValidationErrors("(duplicate use of uri) : emf:testUri");
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

		expectValidationErrors("XML validation error", "The parent definition 'non_existing_parent'", "(duplicate use of uri) : emf:testUri");
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

		expectValidationErrors("Multiple definitions with id 'a1' are found",
							"The parent definition 'a2' of definition 'a3' is missing",
							"The parent definition 'a1' of definition 'a2' is missing",
							"(duplicate use of uri) : emf:testUri");
	}

	@Test
	public void should_NotAllowMultipleVisibleFieldsWithSameId() {
		withDefinition("d2")
			.havingField("label", "an..100", "readonly", null, "label")
			.havingRegion("info", "editable")
				.havingField("label", "an..100", "readonly", null, "label")
			.inFile("def2.xml");

		expectValidationErrors("Found duplicate VISIBLE field [label] from [d2]",
				"Found duplicate VISIBLE field [label] in [d2/info]");
	}

	@Test
	public void should_NotProcessNonXmlFiles() {
		withDefinition("def1").inFile("def1.xml");
		withDefinition("def1").inFile("def2.txt");

		expectValidationErrors();

		List<GenericDefinitionImpl> savedDefinitions = importDefinitions();

		assertEquals(1, savedDefinitions.size());
	}

	@Test
	public void should_NotAcceptDefinitionWithDuplicateName() {
		withNewDefinition("def1.xml", "test");
		withNewDefinition("test/def1.xml", "test");

		expectValidationErrors("Duplicate file names found");
	}

	@Test
	public void should_NotAcceptDefinitionsWithXSDValidation() {
		withNewDefinitionFromFile("def1.xml", "invalid_definition.xml");

		expectValidationErrors("Cannot find the declaration of element 'notADefinition'");
	}

	@Test
	public void should_NotAllowImportDefinition_When_ThereAreInvalidValuesOfUries() {
		withNewDefinitionFromFile("def1.xml", "definition_invalid_uri_validation.xml");
		List<String> errorMessages = new ArrayList<>();
		errorMessages.add("XML validation error in def1.xml: On line 8, column 107 : cvc-pattern-valid: Value 'fieldWithoutPrefix' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		errorMessages.add("XML validation error in def1.xml: On line 8, column 107 : cvc-attribute.3: The value 'fieldWithoutPrefix' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		errorMessages.add("XML validation error in def1.xml: On line 9, column 112 : cvc-pattern-valid: Value ':fieldWithEmptyPrefix' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		errorMessages.add("XML validation error in def1.xml: On line 9, column 112 : cvc-attribute.3: The value ':fieldWithEmptyPrefix' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		errorMessages.add("XML validation error in def1.xml: On line 10, column 101 : cvc-pattern-valid: Value 'prefixOnly:' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		errorMessages.add("XML validation error in def1.xml: On line 10, column 101 : cvc-attribute.3: The value 'prefixOnly:' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		errorMessages.add("XML validation error in def1.xml: On line 12, column 107 : cvc-pattern-valid: Value 'emf:fieldWithSpace1 ' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		errorMessages.add("XML validation error in def1.xml: On line 12, column 107 : cvc-attribute.3: The value 'emf:fieldWithSpace1 ' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		errorMessages.add("XML validation error in def1.xml: On line 13, column 107 : cvc-pattern-valid: Value 'emf: fieldWithSpace2' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		errorMessages.add("XML validation error in def1.xml: On line 13, column 107 : cvc-attribute.3: The value 'emf: fieldWithSpace2' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		errorMessages.add("XML validation error in def1.xml: On line 14, column 104 : cvc-pattern-valid: Value ' :fieldWithSpace3' is not facet-valid with respect to pattern '(\\S+:\\S+)|(FORBIDDEN)' for type '#AnonType_uricomplexFieldDefinition'.");
		errorMessages.add("XML validation error in def1.xml: On line 14, column 104 : cvc-attribute.3: The value ' :fieldWithSpace3' of attribute 'uri' on element 'field' is not valid with respect to its type, 'null'.");
		expectValidationErrors(errorMessages);
	}

	@Test
	public void should_NotAcceptDefinitions_HavingFields_WithMalformedBooleanValue() {
		withNewDefinitionFromFile("def1.xml", "definition_malformed_boolean.xml");

		List<String> errorMessages = new ArrayList<>();
		// configurations
		errorMessages.add("In configuration, Value: 'test' of field malformedBooleanField is invalid for type boolean");
		// error messages for fields
		errorMessages.add("Error found in definition 'malformedBooleanDef': In fields, Value: 'TRUE' of field correctBooleanFieldTRUE is invalid for type boolean ");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In fields, Value: 'True' of field correctBooleanFieldTrue is invalid for type boolean ");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In fields, Value: 'FALSE' of field correctBooleanFieldFALSE is invalid for type boolean ");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In fields, Value: 'False' of field correctBooleanFieldFalse is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In fields, Value: 'leliya Gica' of field malformedBooleanField is invalid for type boolean ");
		// error messages for regions
		errorMessages.add("Error found in definition 'malformedBooleanDef': In region 'systemData', Value: 'TRUE' of field regionMalformedBooleanFieldTRUE is invalid for type boolean ");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In region 'systemData', Value: 'True' of field regionMalformedBooleanFieldTrue is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In region 'systemData', Value: 'FALSE' of field regionMalformedBooleanFieldFALSE is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In region 'systemData', Value: 'False' of field regionMalformedBooleanFieldFalse is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In region 'systemData', Value: 'leliya Gica' of field regionMalformedBooleanField is invalid for type boolean");
		// error messages for transitions
		errorMessages.add("Error found in definition 'malformedBooleanDef': In transition 'contactPerson', Value: 'TRUE' of field transitionCorrectBooleanFieldTRUE is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In transition 'contactPerson', Value: 'True' of field transitionCorrectBooleanFieldTrue is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In transition 'contactPerson', Value: 'FALSE' of field transitionCorrectBooleanFieldFALSE is invalid for type boolean");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In transition 'contactPerson', Value: 'False' of field transitionCorrectBooleanFieldFalse is invalid for type boolean ");
		errorMessages.add("Error found in definition 'malformedBooleanDef': In transition 'contactPerson', Value: 'leliya Gica' of field transitionMalformedBooleanField is invalid for type boolean");

		expectValidationErrors(errorMessages);
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
		when(semanticDefinitionService.getRelation("emf:modifiedBy")).thenReturn(modifiedByRelation);


		// A field "fieldWithRange", this field has not empty range, but range is in mismatch with semantic range.
		PropertyInstance createdByRelation = new PropertyInstance();
		createdByRelation.setId("emf:createdBy");
		createdByRelation.setRangeClass(AGENT_SHORT_URI);
		when(semanticDefinitionService.getRelation("emf:createdBy")).thenReturn(createdByRelation);

		// A field "fieldWithNonRegisteredSemanticRange", this field has range and semantic has not range.
		PropertyInstance nonRegisteredSemanticRange = new PropertyInstance();
		nonRegisteredSemanticRange.setId("emf:nonRegisteredSemanticRange");
		nonRegisteredSemanticRange.setRangeClass("emf:MissingSemanticRange");
		when(semanticDefinitionService.getRelation("emf:nonRegisteredSemanticRange")).thenReturn(
				nonRegisteredSemanticRange);

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
		when(semanticDefinitionService.getProperty("emf:DataProperty")).thenReturn(Mockito.mock(PropertyInstance.class));

		// When:
		// Definition is imported.
		withNewDefinitionFromFile("def1.xml", "definition_range_validation.xml");

		// Then:
		// We expect:
		List<String> errorMessages = new ArrayList<>();
		// A error that "emf:notRegisteredUri" is not found.
		errorMessages.add("Error found in definition 'rangeValidationDef': Uri: emf:notRegisteredUri of property: fieldWithNotRegisteredUri is not found!");
		// A error that "" is not sub class of range specified in semantic.
		errorMessages.add("Error found in definition 'rangeValidationDef': There is incorrect property range emf:Document in property: fieldWithRange. It have to be: ptop:Agent or subclass of it!");
		// A error "emf:createdBy"  is not registered in semantic.
		errorMessages.add("Error found in definition 'rangeValidationDef': Uri: emf:createdBy of property: fieldWithRange is not registered!");
		// A error that specified range "emf:MissingSemanticRange" is not found in semantic.
		errorMessages.add("Error found in definition 'rangeValidationDef': Semantic range: emf:MissingSemanticRange of uri emf:nonRegisteredSemanticRange not found!");
		// A error that object property "emf:DataProperty" is used as object property.
		errorMessages.add("Uri emf:DataProperty of property author is defined as data property and can't be used as object property!");
		expectValidationErrors(errorMessages);
	}

	@Test
	public void should_NotAcceptDefinitions_HavingFields_WithMalformedJSONValue() {
		withNewDefinitionFromFile("def1.xml", "definition_malformed_json.xml");

		String errorMessage1 = "Error found in definition 'malformedJsonDef': In fields, in the JSON value of field 'malformedJsonField' : Invalid token=EOF at (line no=1, column no=60, offset=59). Expected tokens are: [COMMA]";
		String errorMessage2 = "Error found in definition 'malformedJsonDef': In region 'systemData', in the JSON value of field 'type' : Unexpected char 111 at (line no=1, column no=4, offset=3), expecting 'a'";
		String errorMessage3 = "Error found in definition 'malformedJsonDef': In transition 'contactPerson', in the JSON value of field 'misspelledBooleanField' : Unexpected char 32 at (line no=1, column no=20, offset=19), expecting 'e'";
		String errorMessage4 = "Error found in definition 'malformedJsonDef': In transition 'contactPerson', in the JSON value of field 'attachToConfig' : Invalid token=EOF at (line no=3, column no=129, offset=173). Expected tokens are: [COMMA]";
		String errorMessage5 = "Error found in definition 'malformedJsonDef': In transition 'testTransition', in the JSON value of field 'testOne' : Invalid token=STRING at (line no=3, column no=18, offset=61). Expected tokens are: [COMMA]";

		expectValidationErrors(errorMessage1, errorMessage2, errorMessage3, errorMessage4, errorMessage5);
	}

	@Test
	public void should_NotAcceptDefinition_HavingFiltersOnFieldWithUriEmfType() {
		withNewDefinitionFromFile("def1.xml", "definition_filter_validation.xml");

		List<String> errorMessages = new ArrayList<>();
		errorMessages.add("Field with property name: \"type\" and uri: \"emf:type\" can't be filtering!");
		errorMessages.add("Error found in definition 'filterDefinitionsDefinition': Field with property name: \"status\" and uri: \"emf:status\" can't be filtering!");

		expectValidationErrors(errorMessages);
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
		verifyDefintiionsExported(exported);
	}

	@Test
	public void should_Export_Requested_Definitions() throws IOException {
		withExistingDefinitionFromFile("def1.xml", "<definition>Content of def1</definition>");
		withExistingDefinitionFromFile("def2.xml", "<definition>Content of def2</definition>");
		withExistingDefinitionFromFile("def3.xml", "<definition>sadadsadasdasdasd</definition>");

		List<File> exported = definitionImportService.exportDefinitions(Arrays.asList("def1", "def2", "def3"));

		verifyIdsRequested("def1", "def2", "def3");
		verifyDefintiionsExported(exported);
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
	private TempFileProvider templFileProvider = new TempFileProviderFake(tempFolder.getRoot());

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

		when(dbDao.saveOrUpdate(any())).thenAnswer(invokation -> invokation.getArguments()[0]);

		when(mutableDefinitionService.savePropertyIfChanged(any(), any())).thenAnswer(
				invokation -> invokation.getArguments()[0]);

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
	}

	private void expectValidationErrors(String... expectedErrors) {
		List<String> errors = new ArrayList<>(definitionImportService.validate(rootDirectory));

		if (expectedErrors.length != errors.size()) {
			String message = "Expected " + expectedErrors.length + " messages but found " + errors.size() + "\n";
			message += errors.stream().collect(Collectors.joining("\n"));

			fail(message);
		}

		for (int i = 0; i < expectedErrors.length; i++) {
			assertTrue(errors.get(i).toLowerCase() + " should contain: " + expectedErrors[i].toLowerCase(),
					errors.get(i).toLowerCase().contains(expectedErrors[i].toLowerCase()));
		}
	}

	private void expectValidationErrors(List<String> expectedErrors) {
		expectValidationErrors(expectedErrors.stream().toArray(String[]::new));
	}

	private void expectNoValidationErrors() {
		expectValidationErrors();
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

	private void verifyDefintiionsExported(List<File> exportedActual) throws IOException {
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

		public DefinitionBuilder havingField(String name, String type, String displatType, String uri, String label) {
			return havingField(name, type, displatType, uri, null, label);
		}

		public DefinitionBuilder havingField(String name, String type, String displatType, String uri, Boolean override, String label) {
			ComplexFieldDefinition field = constructField(name, type, displatType, uri, override, null, label);

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

}
