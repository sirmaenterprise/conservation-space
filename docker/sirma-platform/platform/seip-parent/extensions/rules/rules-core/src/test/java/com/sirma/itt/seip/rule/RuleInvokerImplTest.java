package com.sirma.itt.seip.rule;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.invoker.RuleRunner;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * The Class RuleObserverHelperTest.
 *
 * @author Hristo Lungov
 */
public class RuleInvokerImplTest {

	@Spy
	private List<InstanceRule> rules = new ArrayList<>();

	@Mock
	RuleStore ruleStore;

	@Mock
	private InstanceRule instanceRule;

	@Spy
	Statistics statistics = new NoOpStatistics();

	@Mock
	private DefinitionService definitionService;

	@Mock
	private EventService eventService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	private RuleInvokerImpl ruleInvoker;

	@Mock
	RuleRunner ruleRunner;

	@Spy
	private EmfInstance documentInstance = new EmfInstance();

	@Spy
	private EmfInstance objectInstance = new EmfInstance();

	private RuleContext docContext;

	private RuleContext objectContext;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rules.add(instanceRule);

		documentInstance.setId("document");
		objectInstance.setId("object");

		docContext = RuleContext.create(documentInstance, documentInstance, ActionTypeConstants.EDIT_DETAILS);

		objectContext = RuleContext.create(objectInstance, objectInstance, ActionTypeConstants.EDIT_DETAILS);

		when(definitionService.getAllDefinitions(any(Class.class))).thenReturn(Collections.emptyList());

		when(ruleStore.findRules(anyString(), eq(docContext))).thenReturn(rules);
	}

	@Test
	public void invokeRulesTest() {
		ruleInvoker.invokeRules(documentInstance, documentInstance, ActionTypeConstants.EDIT_DETAILS);
		Mockito.verify(ruleRunner, atLeastOnce()).runRules(Arrays.asList(instanceRule), docContext);
		ruleInvoker.invokeRules(objectInstance, objectInstance, ActionTypeConstants.EDIT_DETAILS);
		Mockito.verify(ruleRunner, never()).runRules(Arrays.asList(instanceRule), objectContext);
	}
}
