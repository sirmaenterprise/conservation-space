package com.sirma.itt.seip.instance.version.revert;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link RevertContext}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class RevertContextTest {

	@Test(expected = IllegalArgumentException.class)
	public void create_nullIdentifier() {
		RevertContext.create(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void create_emptyIdentifier() {
		RevertContext.create("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void create_notVersionIdentifier() {
		RevertContext.create("instance-id");
	}

	@Test
	public void getCurrentInstanceId_extractedCorrect() {
		RevertContext context = RevertContext.create("instance-id-v1.5");
		assertEquals("instance-id", context.getCurrentInstanceId());
	}

	@Test
	public void testAll_correctData() {
		Instance current = new EmfInstance();
		current.setId("instance-id");
		Instance result = new EmfInstance();
		result.setId("instance-id-v1.5");
		RevertContext revertContext = RevertContext
				.create("instance-id-v1.5")
					.setCurrentInstance(current)
					.setRevertResultInstance(result);

		assertEquals("instance-id-v1.5", revertContext.getVersionId());
		assertEquals("instance-id", revertContext.getCurrentInstanceId());
		assertEquals(current, revertContext.getCurrentInstance());
		assertEquals(result, revertContext.getRevertResultInstance());
		assertEquals("revertVersion", revertContext.getOperation().getOperation());
	}

}
