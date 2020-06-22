package com.sirma.itt.emf.audit.observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.PropertyInstance;

/**
 * Test for {@link PropertyActionResolver}
 *
 * @author BBonev
 */
public class PropertyActionResolverTest {

	@InjectMocks
	private PropertyActionResolver actionResolver;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(semanticDefinitionService.getRelation("emf:references"))
				.then(a -> createProperty("+addReference|reference|-removeReference"));
		when(semanticDefinitionService.getRelation("emf:block")).then(a -> createProperty("block"));
		when(semanticDefinitionService.getRelation("emf:assign")).then(a -> createProperty(null));
	}

	@Test
	public void isAuditableProperty_onUnkownReference_ReturnFalse() throws Exception {
		assertFalse(actionResolver.isAuditableProperty("emf:someRelationId"));
	}

	@Test
	public void isAuditableProperty_onNotConfiguredAuditEvent_ReturnFalse() throws Exception {
		assertFalse(actionResolver.isAuditableProperty("emf:assign"));
	}

	@Test
	public void isAuditableProperty_onValidRefenrece_ReturnTrue() throws Exception {
		assertTrue(actionResolver.isAuditableProperty("emf:references"));
		assertTrue(actionResolver.isAuditableProperty("emf:block"));
	}

	@Test
	public void getAddActionForProperty_forCustomEventId_ReturnTheCustomAction() throws Exception {
		assertEquals("addReference", actionResolver.getAddActionForProperty("emf:references"));
	}

	@Test
	public void getAddActionForProperty_forNotDefinedEventId_ReturnChangeAction() throws Exception {
		assertEquals("block", actionResolver.getAddActionForProperty("emf:block"));
	}

	@Test
	public void getRemoveActionForProperty_forCustomEventId_ReturnTheCustomAction() throws Exception {
		assertEquals("removeReference", actionResolver.getRemoveActionForProperty("emf:references"));
	}

	@Test
	public void getRemoveActionForProperty_forNotDefinedEventId_ReturnChangeAction() throws Exception {
		assertEquals("block", actionResolver.getRemoveActionForProperty("emf:block"));
	}

	@Test
	public void getChangeActionForProperty_forCustomEventId_ReturnTheCustomAction() throws Exception {
		assertEquals("reference", actionResolver.getChangeActionForProperty("emf:references"));
		assertEquals("block", actionResolver.getChangeActionForProperty("emf:block"));
	}

	private static PropertyInstance createProperty(String auditEvent) {
		PropertyInstance propertyInstance = new PropertyInstance();
		propertyInstance.add("auditEvent", auditEvent);
		return propertyInstance;
	}
}
