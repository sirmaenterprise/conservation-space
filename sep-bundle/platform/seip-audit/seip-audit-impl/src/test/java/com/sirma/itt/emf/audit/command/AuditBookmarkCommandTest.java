package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.instance.AuditBookmarkCommand;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic in {@link AuditBookmarkCommand} by creating a mock of {@link ExpressionsManager}.
 * 
 * @author Mihail Radkov
 */
public class AuditBookmarkCommandTest {

	// XXX: Is this test worth anything? Or it's just testing the mocks...

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest
	private AuditBookmarkCommand bookmarkCommand;

	/** Mocked manager used when creating URLs to EMF instances. */
	@Inject
	private ExpressionsManager expressionsManager;

	/**
	 * Tests {@link AuditBookmarkCommand#execute(com.sirma.itt.emf.event.EmfEvent, AuditActivity)}
	 * by passing <code>null</code> as parameters.
	 */
	@Test
	public void nullTest() {
		AuditActivity activity = new AuditActivity();
		bookmarkCommand.execute(null, null);

		bookmarkCommand.execute(null, activity);
		assertNull(activity.getObjectURL());
		
		PropertiesChangeEvent event = new PropertiesChangeEvent(null, null, null, null);
		bookmarkCommand.execute(event, null);

	}

	/**
	 * Tests the bookmark retrieving of the provided {@link Instance} in the
	 * {@link PropertiesChangeEvent}.
	 */
	@Test
	public void testBookmarkRetrieving() {
		AuditActivity activity = new AuditActivity();
		Instance instance = new CaseInstance();
		instance.setId("test");
		PropertiesChangeEvent event = new PropertiesChangeEvent(instance, null, null, null);

		bookmarkCommand.execute(event, activity);

		assertNotNull(activity.getObjectURL());
		assertEquals("bookmark", activity.getObjectURL());
	}

	/**
	 * Setups mocks of
	 * {@link ExpressionsManager#createDefaultContext(Instance, PropertyDefinition, Map)} and
	 * {@link ExpressionsManager#evaluateRule(String, Class, ExpressionContext, Serializable...)}.
	 * TODO: More javadoc.
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setupMocks() {
		EasyMock.expect(
				expressionsManager.createDefaultContext(EasyMock.anyObject(Instance.class),
						EasyMock.anyObject(PropertyDefinition.class), EasyMock.anyObject(Map.class)))
				.andAnswer(new IAnswer<ExpressionContext>() {
					@Override
					public ExpressionContext answer() throws Throwable {
						return null;
					}
				}).anyTimes();
		EasyMock.expect(
				expressionsManager.evaluateRule(EasyMock.anyString(),
						EasyMock.anyObject(String.class.getClass()),
						EasyMock.anyObject(ExpressionContext.class),
						EasyMock.anyObject(Serializable.class)))
				.andAnswer(new IAnswer<Serializable>() {

					@Override
					public Serializable answer() throws Throwable {
						return "bookmark";
					}
				}).anyTimes();
		EasyMock.replay(expressionsManager);
	}
}
