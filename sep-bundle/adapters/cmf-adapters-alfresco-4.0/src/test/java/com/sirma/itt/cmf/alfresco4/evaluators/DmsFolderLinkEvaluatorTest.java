package com.sirma.itt.cmf.alfresco4.evaluators;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.evaluation.BaseEvaluatorTest;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.evaluation.ExpressionsManager;

/**
 * The Class DmsFolderLinkEvaluatorTest.
 * 
 * @author BBonev
 */
@Test
public class DmsFolderLinkEvaluatorTest extends BaseEvaluatorTest {

	/**
	 * Test eval.
	 */
	public void testEval() {
		SectionInstance sectionInstance = new SectionInstance();
		sectionInstance.setDmsId("workspace://SpacesStore/e8345a17-e453-4e8e-82b3-b2321326bdd3");
		sectionInstance.setProperties(new HashMap<String, Serializable>());
		sectionInstance.getProperties().put("siteName", "testSite");

		ExpressionsManager manager = createManager();

		ExpressionContext context = manager.createDefaultContext(sectionInstance, null, null);
		String rule = manager.evaluateRule("${dmsFolderLink(currentInstance)}", String.class,
				context, null);
		Assert.assertNotNull(rule);
		Assert.assertEquals(
				rule,
				"http://localhost:8080/share/page/site/testSite/folder-details?nodeRef=workspace://SpacesStore/e8345a17-e453-4e8e-82b3-b2321326bdd3");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		// add case evaluator
		DmsFolderLinkEvaluator evaluator = new DmsFolderLinkEvaluator();
		ReflectionUtils.setField(evaluator, "dmsHost", "localhost");
		ReflectionUtils.setField(evaluator, "dmsPort", 8080);
		ReflectionUtils.setField(evaluator, "dmsProtocol", "http");
		evaluator.init();
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}
}
