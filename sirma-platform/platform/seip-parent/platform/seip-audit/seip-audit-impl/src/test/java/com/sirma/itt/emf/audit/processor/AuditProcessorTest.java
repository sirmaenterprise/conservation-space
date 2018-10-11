package com.sirma.itt.emf.audit.processor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditActionIDCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectIDCommand;
import com.sirma.itt.emf.audit.db.AuditDao;
import com.sirma.itt.emf.audit.exception.MissingOperationIdException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.util.ReflectionUtils;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Test the logic in {@link AuditProcessor} when it is enabled.
 *
 * @author Mihail Radkov
 */
public class AuditProcessorTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@Inject
	private AuditDao auditDao;

	@ObjectUnderTest(id = "ap", implementation = AuditProcessorImpl.class)
	private AuditProcessor processor;

	/**
	 * Init test data.
	 */
	@Before
	public void before() {
		List<AuditCommand> commands = new ArrayList<>();
		commands.add(new AuditObjectIDCommand());
		commands.add(new AuditActionIDCommand());
		ReflectionUtils.setFieldValue(processor, "commands", commands);
	}

	@Test
	public void testUserAction() {
		Capture<AuditActivity> capturedActivity = capturePublishing();

		// Missing user
		processor.auditUserOperation(null, null, null);
		Assert.assertFalse(capturedActivity.hasCaptured());
		EmfUser user = new EmfUser("Alduin");
		user.setId("alduin");
		processor.auditUserOperation(user, "login", null);
		Assert.assertTrue(capturedActivity.hasCaptured());
		Assert.assertEquals("Alduin", capturedActivity.getValue().getUserName());
		Assert.assertEquals("alduin", capturedActivity.getValue().getUserId());
	}

	@Test(expected = MissingOperationIdException.class)
	public void testUserActionWithMissingId() {
		processor.auditUserOperation(new EmfUser("Alduin"), null, null);
	}

	/**
	 * Tests the processor behavior when the operation id is null.
	 */
	@Test(expected = MissingOperationIdException.class)
	public void testWithNullOperation() {
		processor.process(null, null, null);
	}

	/**
	 * Tests the processor behavior when the operation id is empty string.
	 */
	@Test(expected = MissingOperationIdException.class)
	public void testWithEmptyOperation() {
		processor.process(null, "", null);
	}

	private Capture<AuditActivity> capturePublishing() {
		Capture<AuditActivity> capturedActivity = new Capture<>();

		auditDao.publish(EasyMock.capture(capturedActivity));
		EasyMock.replay(auditDao);
		return capturedActivity;
	}
}
