package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.sep.content.idoc.Idoc;

/**
 * Test for {@link RemoveNonPublishedTabs}
 *
 * @author BBonev
 */
public class RemoveNonPublishedTabsTest {

	@Test
	public void shouldRemoveTabsWithModeSkip() throws Exception {
		PublishContext context = new PublishContext(mock(PublishInstanceRequest.class), new EmfInstance());
		Idoc idoc = Idoc.parse(getClass().getResourceAsStream("/publish-idoc.html"));
		context.setView(idoc);
		assertEquals(4L, idoc.getSections().stream().count());

		new RemoveNonPublishedTabs().execute(context);

		assertEquals(3L, idoc.getSections().stream().count());
		Idoc copy = Idoc.parse(idoc.asHtml());
		assertEquals(3L, copy.getSections().stream().count());

	}
}
