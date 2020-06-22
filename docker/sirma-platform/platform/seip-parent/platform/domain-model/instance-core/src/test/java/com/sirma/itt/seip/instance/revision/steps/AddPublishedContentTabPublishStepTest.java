package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;

/**
 * Rest for {@link AddPublishedContentTabPublishStep}
 *
 * @author BBonev
 */
public class AddPublishedContentTabPublishStepTest {
	@InjectMocks
	private AddPublishedContentTabPublishStep step;
	@Mock
	private LabelProvider labelProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(labelProvider.getLabel(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void shouldInsertPreviewContentTab() throws Exception {
		PublishContext context = new PublishContext(mock(PublishInstanceRequest.class), new EmfInstance());
		Idoc idoc = Idoc.parse(getClass().getResourceAsStream("/publish-idoc.html"));
		context.setView(idoc);
		long tabs = idoc.getSections().stream().count();
		assertEquals(4, tabs);

		step.execute(context);

		Idoc copy = Idoc.parse(idoc.asHtml());

		assertEquals(3, copy.getSections().stream().count());
		SectionNode node = copy.getSections().getSectionByIndex(0);
		assertEquals(AddPublishedContentTabPublishStep.DEFAULT_TAB_TITLE, node.getTitle());
	}
}
