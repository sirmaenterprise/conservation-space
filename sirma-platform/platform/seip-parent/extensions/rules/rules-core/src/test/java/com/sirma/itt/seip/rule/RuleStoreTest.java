/**
 *
 */
package com.sirma.itt.seip.rule;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.DynamicInstanceRule;
import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * Test for {@link RuleStore}.
 *
 * @author BBonev
 */
public class RuleStoreTest extends EmfTest {

	@InjectMocks
	RuleStore store;
	@Spy
	private ContextualMap<String, List<InstanceRule>> defRulesCache = ContextualMap.create();
	@Spy
	private ContextualMap<String, InstanceRule> ruleMapping = ContextualMap.create();
	@Spy
	private ContextualReadWriteLock ruleAccessLock = ContextualReadWriteLock.create();
	@Mock
	private TypeMappingProvider typeProvider;
	@Mock
	private DynamicInstanceRule rule1;
	@Mock
	private InstanceRule rule2;
	@Spy
	private List<InstanceRule> rules = new ArrayList<>();
	@Mock
	private DefinitionService definitionService;
	@Mock
	private BeanManager beanManager;
	@Mock
	private TaskExecutor taskExecutor;
	@Spy
	private ConfigurationProperty<Set<String>> inactiveRules = new ConfigurationPropertyMock<>(new HashSet<>());

	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		rules.clear();
	}

	@Test
	public void test_initialize() {
		store.initialize();
	}

	@Test
	public void test_rulesFromPlugin() {
		when(rule1.getRuleInstanceName()).thenReturn("rule1");
		when(rule1.getSupportedOperations()).thenReturn(Arrays.asList("open"));

		when(rule2.getRuleInstanceName()).thenReturn("rule2");
		when(rule2.getSupportedOperations()).thenReturn(Arrays.asList("close"));

		rules.add(rule1);
		rules.add(rule2);

		store.initialize();

		Context<String, Object> context = new Context<>();

		when(rule1.isApplicable(context)).thenReturn(Boolean.TRUE);

		Collection<InstanceRule> findRules = store.findRules("open", context);

		assertNotNull(findRules);
		assertFalse(findRules.isEmpty());
		assertEquals(findRules.size(), 1);
		assertEquals(findRules.iterator().next(), rule1);

		when(rule2.isApplicable(context)).thenReturn(Boolean.TRUE);

		findRules = store.findRules("close", context);

		assertNotNull(findRules);
		assertFalse(findRules.isEmpty());
		assertEquals(findRules.size(), 1);
		assertEquals(findRules.iterator().next(), rule2);
	}

	@Test
	public void test_rulesFromDefinition() {
		DefinitionMock mock = new DefinitionMock();
		mock.setIdentifier("ruleId");
		PropertyDefinitionProxy prop = new PropertyDefinitionProxy();
		prop.setIdentifier("ruleConfig");
		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("entityRecongnition");
		prop.setControlDefinition(control);
		prop.setValue("{}");
		mock.getFields().add(prop);
		mock.setType(ObjectTypes.RULE);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("onOperations");
		prop.setValue("open,close");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("onDefinitions");
		prop.setValue("def1");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("instanceTypes");
		prop.setValue("document");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("asyncSupport");
		prop.setValue("true");
		mock.getFields().add(prop);

		Bean bean = Mockito.mock(Bean.class);
		Set<Bean<?>> beans = Collections.singleton(bean);
		when(beanManager.getBeans("entityRecongnition")).thenReturn(beans);
		when(beanManager.resolve(anySet())).thenReturn(bean);
		CreationalContext cc = Mockito.mock(CreationalContext.class);
		when(beanManager.createCreationalContext(bean)).thenReturn(cc);
		when(beanManager.getReference(bean, InstanceRule.class, cc)).thenReturn(rule1);

		when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(Arrays.asList(mock));

		Context<String, Object> context = new Context<>();
		when(rule1.isApplicable(context)).thenReturn(Boolean.TRUE);
		when(rule1.configure(any(Context.class))).thenReturn(Boolean.TRUE);
		when(rule1.getRuleInstanceName()).thenReturn("entityRecongnition");
		when(rule1.getSupportedOperations()).thenReturn(Arrays.asList("open", "close"));

		store.initialize();

		Collection<InstanceRule> findRules = store.findRules("open", context);

		assertNotNull(findRules);
		assertFalse(findRules.isEmpty());
		assertEquals(findRules.size(), 1);
		assertEquals(findRules.iterator().next().getRuleInstanceName(), rule1.getRuleInstanceName());
	}

	@Test
	public void test_findRuleById() {
		DefinitionMock mock = new DefinitionMock();
		mock.setIdentifier("ruleId");
		PropertyDefinitionProxy prop = new PropertyDefinitionProxy();
		prop.setIdentifier("ruleConfig");
		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("entityRecongnition");
		prop.setControlDefinition(control);
		prop.setValue("{}");
		mock.getFields().add(prop);
		mock.setType(ObjectTypes.RULE);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("onOperations");
		prop.setValue("open,close");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("onDefinitions");
		prop.setValue("def1");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("instanceTypes");
		prop.setValue("document");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("asyncSupport");
		prop.setValue("true");
		mock.getFields().add(prop);

		Bean bean = Mockito.mock(Bean.class);
		Set<Bean<?>> beans = Collections.singleton(bean);
		when(beanManager.getBeans("entityRecongnition")).thenReturn(beans);
		when(beanManager.resolve(anySet())).thenReturn(bean);
		CreationalContext cc = Mockito.mock(CreationalContext.class);
		when(beanManager.createCreationalContext(bean)).thenReturn(cc);
		when(beanManager.getReference(bean, InstanceRule.class, cc)).thenReturn(rule1);

		when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(Arrays.asList(mock));

		Context<String, Object> context = new Context<>();
		when(rule1.isApplicable(context)).thenReturn(Boolean.TRUE);
		when(rule1.configure(any(Context.class))).thenReturn(Boolean.TRUE);
		when(rule1.getRuleInstanceName()).thenReturn("entityRecongnition");
		when(rule1.getSupportedOperations()).thenReturn(Arrays.asList("open", "close"));

		store.initialize();

		InstanceRule rule = store.getRuleById("entityRecongnition");

		assertNotNull(rule);
		assertEquals(rule.getRuleInstanceName(), "entityRecongnition");
	}

	@Test
	public void test_activate_deactivate() {
		DefinitionMock mock = new DefinitionMock();
		mock.setIdentifier("ruleId");
		PropertyDefinitionProxy prop = new PropertyDefinitionProxy();
		prop.setIdentifier("ruleConfig");
		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("entityRecongnition");
		prop.setControlDefinition(control);
		prop.setValue("{}");
		mock.getFields().add(prop);
		mock.setType(ObjectTypes.RULE);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("onOperations");
		prop.setValue("open,close");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("onDefinitions");
		prop.setValue("def1");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("instanceTypes");
		prop.setValue("document");
		mock.getFields().add(prop);

		prop = new PropertyDefinitionProxy();
		prop.setIdentifier("asyncSupport");
		prop.setValue("true");
		mock.getFields().add(prop);

		Bean bean = Mockito.mock(Bean.class);
		Set<Bean<?>> beans = Collections.singleton(bean);
		when(beanManager.getBeans("entityRecongnition")).thenReturn(beans);
		when(beanManager.resolve(anySet())).thenReturn(bean);
		CreationalContext cc = Mockito.mock(CreationalContext.class);
		when(beanManager.createCreationalContext(bean)).thenReturn(cc);
		when(beanManager.getReference(bean, InstanceRule.class, cc)).thenReturn(rule1);

		when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(Arrays.asList(mock));

		Context<String, Object> context = new Context<>();
		when(rule1.isApplicable(context)).thenReturn(Boolean.TRUE);
		when(rule1.configure(any(Context.class))).thenReturn(Boolean.TRUE);
		when(rule1.getRuleInstanceName()).thenReturn("entityRecongnition");
		when(rule1.getSupportedOperations()).thenReturn(Arrays.asList("open", "close"));

		store.initialize();

		Collection<InstanceRule> findRules = store.findRules("open", context);

		assertNotNull(findRules);
		assertFalse(findRules.isEmpty());
		assertEquals(findRules.size(), 1);
		assertEquals(findRules.iterator().next().getRuleInstanceName(), rule1.getRuleInstanceName());

		List<String> rulesNames = store
				.listActiveRules()
					.map(InstanceRule::getRuleInstanceName)
					.collect(Collectors.toList());
		assertEquals(rulesNames, Arrays.asList("entityRecongnition"));

		rulesNames = store.listInactiveRules().map(InstanceRule::getRuleInstanceName).collect(Collectors.toList());
		assertTrue(rulesNames.isEmpty());

		store.deactivateRule("entityRecongnition");

		findRules = store.findRules("open", context);

		assertNotNull(findRules);
		assertTrue(findRules.isEmpty());

		rulesNames = store.listActiveRules().map(InstanceRule::getRuleInstanceName).collect(Collectors.toList());
		assertTrue(rulesNames.isEmpty());

		rulesNames = store.listInactiveRules().map(InstanceRule::getRuleInstanceName).collect(Collectors.toList());
		assertEquals(rulesNames, Arrays.asList("entityRecongnition"));

		store.activateRule("entityRecongnition");

		findRules = store.findRules("open", context);

		assertNotNull(findRules);
		assertFalse(findRules.isEmpty());
		assertEquals(findRules.size(), 1);
		assertEquals(findRules.iterator().next().getRuleInstanceName(), rule1.getRuleInstanceName());
	}
}
