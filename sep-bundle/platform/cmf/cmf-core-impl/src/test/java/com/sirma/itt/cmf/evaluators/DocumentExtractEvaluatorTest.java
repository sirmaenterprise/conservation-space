package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.evaluation.BaseEvaluatorTest;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * The Class DocumentExtractEvaluatorTest.
 * 
 * @author BBonev
 */
@Test
public class DocumentExtractEvaluatorTest extends BaseEvaluatorTest {

	/**
	 * Test case extract.
	 */
	@Test
	public void testCaseExtract() {
		ExpressionsManager expressionsManager = createManager();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setProperties(new HashMap<String, Serializable>());
		SectionInstance section = new SectionInstance();
		section.setIdentifier("sectionId");
		DocumentInstance document = new DocumentInstance();
		document.setOwningInstance(section);
		section.setOwningInstance(caseInstance);
		caseInstance.getSections().add(section);
		section.getContent().add(document);

		document.setProperties(new HashMap<String, Serializable>());
		document.getProperties().put("selectorField", "selectorValue");
		document.getProperties().put("sourceField", "expectedSourceValue");
		document.getProperties().put(DefaultProperties.TYPE, "expectedTypeValue");
		// ensure the file has content - we does not check files without content
		document.getProperties().put(DocumentProperties.ATTACHMENT_LOCATION, "someDmsUri");

		section = new SectionInstance();
		section.setIdentifier("sectionId2");
		document = new DocumentInstance();
		document.setOwningInstance(section);
		section.setOwningInstance(caseInstance);
		caseInstance.getSections().add(section);
		section.getContent().add(document);

		document.setProperties(new HashMap<String, Serializable>());
		document.getProperties().put("selectorField", "selectorValue");
		document.getProperties().put("sourceField", "expectedSourceValue2");
		document.getProperties().put(DefaultProperties.TYPE, "expectedTypeValue");
		document.getProperties().put(DefaultProperties.CREATED_ON, "user");
		// ensure the file has content - we does not check files without content
		document.getProperties().put(DocumentProperties.ATTACHMENT_LOCATION, "someDmsUri");

		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.setIdentifier("targetField");
		DataType type = new DataType();
		type.setJavaClass(String.class);
		type.setJavaClassName(String.class.getName());
		type.setName(DataTypeDefinition.TEXT);
		definition.setDataType(type);

		// single section selection
		definition.setValue("${extract(sectionId.selectorField[selectorValue].sourceField)}");
		ExpressionContext context = expressionsManager.createDefaultContext(caseInstance,
				definition, null);

		Serializable serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue");

		// all sections selector
		definition.setValue("${extract(,.selectorField[selectorValue].sourceField)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue");

		definition.setValue("${extract(.selectorField[selectorValue].sourceField)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue");

		definition.setValue("${extract([expectedTypeValue].sourceField)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue");

		definition.setValue("${extract(,,,,[expectedTypeValue].sourceField)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue");

		// check if order is correct
		definition.setValue("${extract(sectionId2,sectionId[expectedTypeValue].sourceField)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue2");

		definition.setValue("${extract(sectionId,sectionId2[expectedTypeValue].sourceField)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "expectedSourceValue");

		// search for a field that is located in the second section but not in the first when both
		// documents match the condition
		definition.setValue("${extract(sectionId,sectionId2[expectedTypeValue].createdOn)}");
		context = expressionsManager.createDefaultContext(caseInstance, definition, null);

		serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals(serializable, "user");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		// add case evaluator
		DocumentExtractEvaluator evaluator = new DocumentExtractEvaluator();
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}
}
