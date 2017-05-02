package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditBookmarkCommand;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic in {@link AuditBookmarkCommand} by creating a mock of {@link ExpressionsManager}.
 *
 * @author Mihail Radkov
 */
public class AuditBookmarkCommandTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest
	private AuditBookmarkCommand bookmarkCommand;

	/** Mocked manager used when creating URLs to EMF instances. */
	@Inject
	private ExpressionsManager expressionsManager;

	/**
	 * Tests the bookmark retrieving of the provided {@link Instance} in the {@link PropertiesChangeEvent}.
	 */
	@Test
	public void testBookmarkRetrieving() {
		AuditActivity activity = new AuditActivity();
		Instance instance = new EmfInstance();

		AuditablePayload payload = new AuditablePayload(instance, null, null, true);

		bookmarkCommand.execute(payload, activity);
		assertNull(activity.getObjectURL());

		instance.setId("test");
		bookmarkCommand.execute(payload, activity);
		assertEquals("bookmark", activity.getObjectURL());
	}

	/**
	 * Setups mocks of {@link ExpressionsManager#createDefaultContext(Instance, PropertyDefinition, Map)} and
	 * {@link ExpressionsManager#evaluateRule(String, Class, ExpressionContext, Serializable...)}. TODO: More javadoc.
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setupMocks() {
		EasyMock
				.expect(expressionsManager.createDefaultContext(EasyMock.anyObject(Instance.class),
						EasyMock.anyObject(PropertyDefinition.class), EasyMock.anyObject(Map.class)))
					.andAnswer(() -> null)
					.anyTimes();
		EasyMock
				.expect(expressionsManager.evaluateRule(EasyMock.anyString(),
						EasyMock.anyObject(String.class.getClass()), EasyMock.anyObject(ExpressionContext.class),
						(Serializable[]) EasyMock.anyObject(Serializable.class)))
					.andAnswer(() -> "bookmark")
					.anyTimes();
		EasyMock.replay(expressionsManager);
	}
}
