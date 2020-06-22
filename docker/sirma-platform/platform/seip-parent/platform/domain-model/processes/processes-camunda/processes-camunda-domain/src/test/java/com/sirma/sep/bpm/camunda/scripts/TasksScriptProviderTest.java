package com.sirma.sep.bpm.camunda.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Tests for {@link TasksScriptProvider}.
 *
 * @author A. Kunchev
 */
public class TasksScriptProviderTest {

	private TasksScriptProvider provider;

	@Before
	public void setup() {
		provider = new TasksScriptProvider();
	}

	@Test
	public void getBindings() {
		assertTrue(provider.getBindings().containsKey("tasks"));
	}

	@Test
	public void getScripts() {
		assertEquals(Collections.emptyList(), provider.getScripts());
	}

	@Test
	public void getTaskAssignees_nullInstance() {
		String[] assignees = provider.getTaskAssignees(null, new String[] { "property" });
		assertEquals(0, assignees.length);
	}

	@Test
	public void getTaskAssignees_noProperties() {
		String[] assignees = provider.getTaskAssignees(null, new String[] {});
		assertEquals(0, assignees.length);
	}

	@Test
	public void getTaskAssignees_oneStringProperty() {
		ScriptInstance instance = new ScriptNode();
		instance.setTarget(new EmfInstance());
		instance.add("assignee", "someone");
		String[] assignees = provider.getTaskAssignees(instance, new String[] { "assignee" });
		assertEquals(1, assignees.length);
		assertEquals("someone", assignees[0]);
	}

	@Test
	public void getTaskAssignees_twoStringProperties_sameValue() {
		ScriptInstance instance = new ScriptNode();
		instance.setTarget(new EmfInstance());
		instance.add("assignee", "someone");
		instance.add("assignees", "someone");
		String[] assignees = provider.getTaskAssignees(instance, new String[] { "assignee", "assignees" });
		assertEquals(1, assignees.length);
		assertEquals("someone", assignees[0]);
	}

	@Test
	public void getTaskAssignees_collectionProperty_sameValue() {
		ScriptInstance instance = new ScriptNode();
		instance.setTarget(new EmfInstance());
		instance.add("assignee", (Serializable) Arrays.asList("someone", "someone else"));
		String[] assignees = provider.getTaskAssignees(instance, new String[] { "assignee" });
		assertEquals(2, assignees.length);
		assertEquals("someone", assignees[1]);
		assertEquals("someone else", assignees[0]);
	}

	@Test
	public void getTaskAssignees_collectionAndStringProperty_sameValue() {
		ScriptInstance instance = new ScriptNode();
		instance.setTarget(new EmfInstance());
		instance.add("assignees", (Serializable) Arrays.asList("someone", "someone else"));
		instance.add("assignee", "someone else");
		String[] assignees = provider.getTaskAssignees(instance, new String[] { "assignee", "assignees" });
		assertEquals(2, assignees.length);
		assertEquals("someone", assignees[1]);
		assertEquals("someone else", assignees[0]);
	}

}
