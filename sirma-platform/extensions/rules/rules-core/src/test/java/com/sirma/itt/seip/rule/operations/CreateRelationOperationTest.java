package com.sirma.itt.seip.rule.operations;

import static com.sirma.itt.seip.rule.operations.CreateRelationOperation.PROPERTIES;
import static com.sirma.itt.seip.rule.operations.CreateRelationOperation.RELATION_ID;
import static com.sirma.itt.seip.rule.operations.CreateRelationOperation.REVERSE_RELATION_ID;
import static com.sirma.itt.seip.rule.operations.CreateRelationOperation.SIMPLE_ONLY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.rule.BaseRuleTest;
import com.sirma.itt.seip.rule.operations.CreateRelationOperation;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * The Class CreateRelationOperationTest.
 *
 * @author hlungov
 */
@Test
public class CreateRelationOperationTest extends BaseRuleTest {

	@Mock
	private LinkService linkService;

	@Mock
	private EventService eventService;

	@InjectMocks
	private CreateRelationOperation createRelationOperation;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		LinkConstants.init(mock(SecurityContextManager.class), ContextualMap.create());

		setUpLinkService();
	}

	/**
	 * Sets the up link service.
	 */
	private void setUpLinkService() {
		Mockito
				.when(linkService.link(documentInstance, objectInstance, RELATION_ID, REVERSE_RELATION_ID,
						LinkConstants.getDefaultSystemProperties()))
					.thenReturn(Pair.NULL_PAIR)
					.thenReturn(new Pair<Serializable, Serializable>("test", "test"));
	}

	/**
	 * Configure test.
	 */
	public void configureTest() {
		createRelationOperation.configure(configuration);
		Mockito.verify(configuration, times(0)).getIfSameType(RuleOperation.EVENT_ID, String.class);
		configuration.put(RELATION_ID, RELATION_ID);
		createRelationOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(RuleOperation.EVENT_ID, String.class);
		configuration.put(REVERSE_RELATION_ID, REVERSE_RELATION_ID);
		createRelationOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(RuleOperation.EVENT_ID, String.class);
		Mockito.verify(configuration, times(0)).getIfSameType(PROPERTIES, String.class);
		Map<String, Serializable> properties = new HashMap<>();
		configuration.put(PROPERTIES, properties);
		createRelationOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(PROPERTIES, Map.class);
		configuration.put(PROPERTIES, LinkConstants.getDefaultSystemProperties());
		createRelationOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(PROPERTIES, Map.class);
		Mockito.verify(configuration, times(0)).getIfSameType(SIMPLE_ONLY, Boolean.class);
		configuration.put(SIMPLE_ONLY, Boolean.FALSE);
		createRelationOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(SIMPLE_ONLY, Boolean.class, Boolean.FALSE);
	}

	/**
	 * Checks if is applicable test_no relation ids.
	 */
	public void isApplicableTest_noRelationIds() {
		// isDisabled = false;
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		buildRelationConfiguration.remove(RELATION_ID);
		buildRelationConfiguration.remove(REVERSE_RELATION_ID);
		createRelationOperation.configure(buildRelationConfiguration);
		Assert.assertEquals(createRelationOperation.isApplicable(buildRelationConfiguration), false);
	}

	/**
	 * Checks if is applicable test_no instance.
	 */
	public void isApplicableTest_noInstance() {
		// isDisabled = false;
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		createRelationOperation.configure(buildRelationConfiguration);
		Assert.assertEquals(createRelationOperation.isApplicable(buildRelationConfiguration), false);
	}

	/**
	 * Checks if is applicable test.
	 */
	public void isApplicableTest() {
		// isDisabled = false;
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		createRelationOperation.configure(buildRelationConfiguration);
		Assert.assertEquals(
				createRelationOperation.isApplicable(buildRuleContext(documentInstance, previousVerDocInstance, null)),
				true);
	}

	/**
	 * Execute test none.
	 */
	public void executeTestNone() {
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		createRelationOperation.configure(buildRelationConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		buildRuleContext.putAll(buildRelationConfiguration);
		// null matchedInstance
		createRelationOperation.execute(buildRuleContext, null, null);
		Mockito.verify(linkService, times(0)).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString(), anyString());
	}

	/**
	 * Execute test link without event.
	 */
	public void executeTestLinkWithoutEvent() {
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		createRelationOperation.configure(buildRelationConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		buildRuleContext.putAll(buildRelationConfiguration);
		createRelationOperation.execute(buildRuleContext, objectInstance, null);
		Mockito.verify(linkService).link(documentInstance, objectInstance, RELATION_ID, REVERSE_RELATION_ID,
				LinkConstants.getDefaultSystemProperties());
		Mockito.verify(linkService, times(0)).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString(), anyString());
		Mockito.verify(eventService, times(0)).fire(any(EmfEvent.class));
	}

	/**
	 * Execute test link with event.
	 */
	public void executeTestLinkWithEvent() {
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		buildRelationConfiguration.put(RuleOperation.EVENT_ID, "test");
		buildRuleContext.putAll(buildRelationConfiguration);
		createRelationOperation.configure(buildRelationConfiguration);
		createRelationOperation.execute(buildRuleContext, objectInstance, null);
		Mockito.verify(linkService, atLeastOnce()).link(documentInstance, objectInstance, RELATION_ID,
				REVERSE_RELATION_ID, LinkConstants.getDefaultSystemProperties());
		Mockito.verify(linkService, times(0)).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString(), anyString());
		Mockito.verify(eventService).fire(any(EmfEvent.class));
	}

	/**
	 * Execute test simple link with event.
	 */
	public void executeTestSimpleLinkWithEvent() {
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		buildRelationConfiguration.put(SIMPLE_ONLY, Boolean.TRUE);
		buildRelationConfiguration.put(RuleOperation.EVENT_ID, "test");
		createRelationOperation.configure(buildRelationConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		buildRuleContext.putAll(buildRelationConfiguration);
		createRelationOperation.execute(buildRuleContext, objectInstance, null);
		Mockito.verify(linkService, times(0)).link(documentInstance, objectInstance, RELATION_ID, REVERSE_RELATION_ID,
				LinkConstants.getDefaultSystemProperties());
		Mockito.verify(linkService, atLeastOnce()).linkSimple(docReference, objReference, RELATION_ID,
				REVERSE_RELATION_ID);
		Mockito.verify(eventService).fire(any(EmfEvent.class));
	}

	/**
	 * Execute test simple link without event.
	 */
	public void executeTestSimpleLinkWithoutEvent() {
		Context<String, Object> buildRelationConfiguration = buildDefaultRelationConfiguration();
		buildRelationConfiguration.put(SIMPLE_ONLY, Boolean.TRUE);
		createRelationOperation.configure(buildRelationConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		buildRuleContext.putAll(buildRelationConfiguration);
		createRelationOperation.execute(buildRuleContext, objectInstance, null);
		Mockito.verify(linkService, times(0)).link(documentInstance, objectInstance, RELATION_ID, REVERSE_RELATION_ID,
				LinkConstants.getDefaultSystemProperties());
		Mockito.verify(linkService, atLeastOnce()).linkSimple(docReference, objReference, RELATION_ID,
				REVERSE_RELATION_ID);
		Mockito.verify(eventService, times(0)).fire(any(EmfEvent.class));

	}

	/**
	 * Builds the relation configuration.
	 *
	 * @return the context
	 */
	private Context<String, Object> buildDefaultRelationConfiguration() {
		configuration.clear();
		configuration.put(RELATION_ID, RELATION_ID);
		configuration.put(REVERSE_RELATION_ID, REVERSE_RELATION_ID);
		configuration.put(PROPERTIES, LinkConstants.getDefaultSystemProperties());
		configuration.put(SIMPLE_ONLY, Boolean.FALSE);
		return configuration;

	}

}
