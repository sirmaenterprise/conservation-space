package com.sirma.itt.emf.revision.script;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.script.ScriptNode;

/**
 * Test for {@link RevisionsScriptProvider}.
 *
 * @author A. Kunchev
 */
public class RevisionsScriptProviderTest {

	@InjectMocks
	private RevisionsScriptProvider provider;

	@Mock
	private RevisionService revisionService;

	@Before
	public void setup() {
		provider = new RevisionsScriptProvider();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isRevision_nullNode_false() {
		assertFalse(provider.isRevision(null));
	}

	@Test
	public void isRevision_nullInstance_false() {
		assertFalse(provider.isRevision(new ScriptNode()));
	}

	@Test
	public void isRevision_notRevision_false() {
		ScriptNode node = new ScriptNode();
		Instance instance = new EmfInstance();
		node.setTarget(instance);
		when(revisionService.isRevision(instance)).thenReturn(false);
		assertFalse(provider.isRevision(node));
	}

	@Test
	public void isRevision_revision_true() {
		ScriptNode node = new ScriptNode();
		Instance instance = new EmfInstance();
		node.setTarget(instance);
		when(revisionService.isRevision(instance)).thenReturn(true);
		assertTrue(provider.isRevision(node));
	}

}
