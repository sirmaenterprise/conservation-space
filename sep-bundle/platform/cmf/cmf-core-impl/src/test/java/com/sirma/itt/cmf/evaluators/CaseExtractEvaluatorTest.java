package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.evaluation.BaseEvaluatorTest;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.evaluation.ExpressionsManager;

/**
 * The Class CaseExtractEvaluatorTest.
 * 
 * @author BBonev
 */
@Test
public class CaseExtractEvaluatorTest extends BaseEvaluatorTest {

	/**
	 * Test case extract.
	 */
	public void testCaseExtract() {
		ExpressionsManager expressionsManager = createManager();

		CaseInstance caseInstance = new CaseInstance();

		caseInstance.setProperties(new HashMap<String, Serializable>());
		caseInstance.getProperties().put("sourceField", "expectedSourceValue");

		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.setIdentifier("targetField");
		definition.setValue("${extract(sourceField)}");
		DataType type = new DataType();
		type.setJavaClass(String.class);
		type.setJavaClassName(String.class.getName());
		type.setName(DataTypeDefinition.TEXT);
		definition.setDataType(type);

		ExpressionContext context = expressionsManager.createDefaultContext(caseInstance,
				definition, null);

		Serializable serializable = expressionsManager.evaluate(definition, context);
		Assert.assertEquals("expectedSourceValue", serializable);
	}

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		// add case evaluator
		CaseExtractEvaluator evaluator = new CaseExtractEvaluator();
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}
}
