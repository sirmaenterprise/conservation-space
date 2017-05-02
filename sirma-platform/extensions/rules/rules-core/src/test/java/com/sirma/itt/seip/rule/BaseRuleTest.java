package com.sirma.itt.seip.rule;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * The Class BaseRuleTest.
 *
 * @author hlungov
 */
public abstract class BaseRuleTest extends EmfTest {

	@Spy
	protected EmfInstance documentInstance;

	@Spy
	protected EmfInstance previousVerDocInstance;

	@Spy
	protected EmfInstance objectInstance;

	@Spy
	protected EmfInstance previousVerObjInstance;

	@Spy
	protected LinkSourceId docReference;

	@Spy
	protected LinkSourceId objReference;

	@Spy
	protected Context<String, Object> configuration = new Context<>();

	/**
	 * Sets the up.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		Mockito.doReturn(docReference).when(documentInstance).toReference();
		Mockito.doReturn(objReference).when(objectInstance).toReference();
		documentInstance.setProperties(new HashMap<String, Serializable>());
		previousVerDocInstance.setProperties(new HashMap<String, Serializable>());
		objectInstance.setProperties(new HashMap<String, Serializable>());
		previousVerObjInstance.setProperties(new HashMap<String, Serializable>());
	}

	/**
	 * Builds Rule context.
	 *
	 * @param currentInstance
	 *            the current instance
	 * @param oldVersionInstance
	 *            the old version instance
	 * @param operation
	 *            the operation
	 * @return the context
	 */
	protected Context<String, Object> buildRuleContext(Instance currentInstance, Instance oldVersionInstance,
			String operation) {
		Context<String, Object> context = new Context<>(10);
		context.put(RuleContext.PROCESSING_INSTANCE, currentInstance);
		context.put(RuleContext.PREVIOUS_VERSION, oldVersionInstance);
		context.put(RuleContext.OPERATION, operation);
		return context;
	}

}
