package com.sirma.itt.emf.audit.observer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.emf.semantic.persistence.AddRelationEvent;
import com.sirma.itt.emf.semantic.persistence.LocalStatement;
import com.sirma.itt.emf.semantic.persistence.RemoveRelationEvent;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test the relation observer.
 *
 * @author nvelkov
 */
public class AuditRelationObserverTest {
	private static final String EXPECTED_USER = "user";
	private static final String EXPECTED_USERID = "userId";

	@Mock
	private AuditDao auditDao;

	@Mock
	private AuditObserverHelper helper;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private AuditRelationObserver observer;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		EmfUser user = new EmfUser(EXPECTED_USER);
		user.setId(EXPECTED_USERID);
		Mockito.when(helper.getCurrentUser()).thenReturn(user);
	}

	/**
	 * Test the add relation event observer.
	 */
	@Test
	public void testAddRelation() {
		mockSemanticDefinitionService(true);

		ObjectPropertyAddEvent event = new AddRelationEvent(mockStatement(), IRI -> IRI.stringValue());

		ArgumentCaptor<AuditActivity> activityCaptor = ArgumentCaptor.forClass(AuditActivity.class);

		observer.onRelationAdd(event);
		Mockito.verify(auditDao).publish(activityCaptor.capture());
		Assert.assertEquals(activityCaptor.getValue().getTargetProperties(), "targetId");
		Assert.assertEquals(activityCaptor.getValue().getObjectSystemID(), "sourceId");
		Assert.assertEquals(activityCaptor.getValue().getRelationId(), "pred");
		Assert.assertEquals(activityCaptor.getValue().getRelationStatus(), "add");
		Assert.assertEquals(activityCaptor.getValue().getUserName(), EXPECTED_USER);
		Assert.assertEquals(activityCaptor.getValue().getUserId(), EXPECTED_USERID);
	}

	/**
	 * Test the remove relation event observer.
	 */
	@Test
	public void testRemoveRelation() {
		mockSemanticDefinitionService(true);

		ObjectPropertyRemoveEvent event = new RemoveRelationEvent(mockStatement(), IRI -> IRI.stringValue());

		ArgumentCaptor<AuditActivity> activityCaptor = ArgumentCaptor.forClass(AuditActivity.class);

		observer.onRelationRemove(event);
		Mockito.verify(auditDao).publish(activityCaptor.capture());
		Assert.assertEquals(activityCaptor.getValue().getTargetProperties(), "targetId");
		Assert.assertEquals(activityCaptor.getValue().getObjectSystemID(), "sourceId");
		Assert.assertEquals(activityCaptor.getValue().getRelationId(), "pred");
		Assert.assertEquals(activityCaptor.getValue().getRelationStatus(), "remove");
	}

	/**
	 * Test the add relation event observer when the relation isn't auditable.
	 */
	@Test
	public void testAddRelationNotAuditable() {
		mockSemanticDefinitionService(false);
		ObjectPropertyRemoveEvent event = new RemoveRelationEvent(mockStatement(), IRI -> IRI.stringValue());
		observer.onRelationRemove(event);
		Mockito.verify(auditDao, Mockito.never()).publish(Matchers.any(AuditActivity.class));
	}

	private static LocalStatement mockStatement() {
		Statement statement = Mockito.mock(Statement.class);
		Value targetId = getURI("targetId");
		Mockito.when(statement.getObject()).thenReturn(targetId);

		IRI sourceId = getURI("sourceId");
		Mockito.when(statement.getSubject()).thenReturn(sourceId);

		IRI predicate = getURI("pred");
		Mockito.when(statement.getPredicate()).thenReturn(predicate);

		return LocalStatement.toAdd(statement);
	}

	private static IRI getURI(String value) {
		IRI rdfValue = Mockito.mock(IRI.class);
		Mockito.when(rdfValue.stringValue()).thenReturn(value);
		return rdfValue;
	}

	private void mockSemanticDefinitionService(boolean auditable) {
		PropertyInstance instance = new PropertyInstance();
		if (auditable) {
			instance.add("auditEvent", true);
		}
		Mockito.when(semanticDefinitionService.getRelation(Matchers.anyString())).thenReturn(instance);
	}
}
