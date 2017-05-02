
package com.sirma.itt.seip.instance.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Unit tests for OutjectPropertiesScriptProvider.
 *
 * @author A. Kunchev
 */
public class OutjectPropertiesScriptProviderTest {

	private static final String TEST_JSON = "{\"outject\":[\"title\", \"description\"], \"outjectNotEmpty\":[\"content\", \"createdBy\", \"compact_header\", \"name\"] }";

	@InjectMocks
	private OutjectPropertiesScriptProvider provider = new OutjectPropertiesScriptProvider();

	@Mock
	private InstanceService instanceService;

	@Mock
	private InstanceContextInitializer instanceContextInitializer;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// --------------------------------- getBindings ---------------------------------------

	@Test
	public void getBindings_withOurjectEntry_containsOutjectKeyAndValue() {
		assertTrue(provider.getBindings().containsKey("outject"));
		assertTrue(provider.getBindings().get("outject") != null);
	}

	// -------------------------------- getScripts -----------------------------------------

	@Test
	public void getScripts_oneScript_sizeOne() {
		assertEquals(1, provider.getScripts().size());
	}

	// -------------------------------- outjectProperties ----------------------------------

	@Test
	public void outjectProperties_nullSource_false() {
		assertFalse(provider.outjectProperties(null, new ScriptNode(), "{}"));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_nullPropertyObject_false() {
		assertFalse(provider.outjectProperties(new ScriptNode(), new ScriptNode(), null));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_emptyPropertyObject_false() {
		assertFalse(provider.outjectProperties(new ScriptNode(), new ScriptNode(), ""));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_noCurrentInstnace_false() {
		assertFalse(provider.outjectProperties(new ScriptNode(), prepareParentScriptNode(), TEST_JSON));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_noParentInstnace_false() {
		assertFalse(provider.outjectProperties(prepareCurrentScriptNode(), new ScriptNode(), TEST_JSON));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_true() {
		ScriptNode parent = prepareParentScriptNode();
		ScriptNode current = prepareCurrentScriptNode();

		assertTrue(provider.outjectProperties(current, parent, TEST_JSON));

		verify(instanceService).save(any(Instance.class), any(Operation.class));

		Instance parentInstance = parent.getTarget();
		assertEquals("", parentInstance.getString(DefaultProperties.DESCRIPTION));
		assertEquals("parentContent", parentInstance.getString(DefaultProperties.CONTENT));
		assertEquals("currentTitle", parentInstance.getString(DefaultProperties.TITLE));
		assertEquals("header", parentInstance.getString(DefaultProperties.HEADER_COMPACT));
		assertEquals("currentName", parentInstance.getString(DefaultProperties.NAME));
		assertNull(parentInstance.getString(DefaultProperties.CREATED_BY));

	}

	// -------------------------------- common methods --------------------------------------

	private static ScriptNode prepareCurrentScriptNode() {
		ScriptNode current = new ScriptNode();
		current.setTarget(prepareInstance("currentTitle", "", "", null, "currentName"));
		return current;
	}

	private static ScriptNode prepareParentScriptNode() {
		ScriptNode parent = new ScriptNode();
		parent.setTarget(prepareInstance("parentTitle", "parentDescription", "parentContent", "header", "parentName"));
		return parent;
	}

	private static Instance prepareInstance(String title, String description, String content, String header,
			String name) {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.TITLE, title);
		instance.add(DefaultProperties.DESCRIPTION, description);
		instance.add(DefaultProperties.CONTENT, content);
		instance.add(DefaultProperties.HEADER_COMPACT, header);
		instance.add(DefaultProperties.NAME, name);
		return instance;
	}

}
