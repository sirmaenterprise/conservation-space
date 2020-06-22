package com.sirma.itt.seip.rule;

import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RulesConfigurations;
import com.sirma.itt.emf.rule.invoker.RuleInvoker;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * The Class RuleObserverTest.
 *
 * @author Hristo Lungov
 */
@Test
public class RuleObserverTest extends EmfTest {

	@Mock
	private RuleInvoker ruleObserverHelper;

	@Mock
	private LockService lockService;

	@Mock
	private RulesConfigurations rulesConfigurations;

	@Spy
	ConfigurationPropertyMock<Boolean> ruleConfig = new ConfigurationPropertyMock<>();

	@InjectMocks
	private RuleObserver ruleObserver;

	@Spy
	TransactionSupport transactionSupport = new TransactionSupportFake();

	@Spy
	private EmfInstance documentInstance = new EmfInstance();

	/**
	 * Sets the up.
	 */
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		createTypeConverter();
	}

	/**
	 * Sets the up instance.
	 */
	private void setUpInstance() {
		Mockito.when(documentInstance.getProperties()).thenReturn(new HashMap<String, Serializable>());
		documentInstance.setId("docId");
		setReferenceField(documentInstance);
		LockInfo lockInfo = Mockito.mock(LockInfo.class);
		when(lockInfo.isLocked()).thenReturn(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
		when(lockService.lockStatus(documentInstance.toReference())).thenReturn(lockInfo);
	}

	/**
	 * Document change event test.
	 */
	public void documentChangeEventTest() {
		when(rulesConfigurations.getIsRulesActivate()).thenReturn(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
		setUpInstance();
		InstancePersistedEvent<EmfInstance> documentChangeEvent = new InstancePersistedEvent<>(documentInstance, documentInstance, "");
		ruleObserver.onInstancePersistedEvent(documentChangeEvent);
		Mockito.verify(ruleObserverHelper, Mockito.times(0)).invokeRules(documentInstance, documentInstance, "");
		ruleObserver.onInstancePersistedEvent(documentChangeEvent);
		Mockito.verify(ruleObserverHelper, Mockito.times(0)).invokeRules(documentInstance, documentInstance, "");
		ruleObserver.onInstancePersistedEvent(documentChangeEvent);
		Mockito.verify(ruleObserverHelper, Mockito.times(0)).invokeRules(documentInstance, documentInstance, "");
		ruleObserver.onInstancePersistedEvent(documentChangeEvent);
		Mockito.verify(ruleObserverHelper, Mockito.times(0)).invokeRules(documentInstance, documentInstance, "");

		ruleObserver.onInstancePersistedEvent(documentChangeEvent);
		Mockito.verify(ruleObserverHelper).invokeRules(documentInstance, documentInstance, "");
	}
}
