package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidgetBuilder;

/**
 * Test for {@link SaveIdocPublishStep}
 *
 * @author BBonev
 */
public class SaveIdocPublishStepTest {

	@Test
	public void shouldAddViewToRevision() throws Exception {
		PublishInstanceRequest request = new PublishInstanceRequest(new EmfInstance(), new Operation(), null, null);
		Instance revision = new EmfInstance();
		PublishContext context = new PublishContext(request, revision);

		ContentNodeFactory.getInstance().registerBuilder(new ImageWidgetBuilder());
		Idoc idoc = Idoc.parse(getClass().getResourceAsStream("/publish-idoc.html"));

		List<String> originalTabs = idoc.getSections().stream().map(SectionNode::getId).collect(Collectors.toList());
		context.setView(idoc);
		new SaveIdocPublishStep().execute(context);

		List<String> newTabs = idoc.getSections().stream().map(SectionNode::getId).collect(Collectors.toList());
		assertEquals(originalTabs.size(), newTabs.size());
		assertNotEquals(originalTabs, newTabs);
		assertNotNull(revision.getString(TEMP_CONTENT_VIEW));

	}
}
