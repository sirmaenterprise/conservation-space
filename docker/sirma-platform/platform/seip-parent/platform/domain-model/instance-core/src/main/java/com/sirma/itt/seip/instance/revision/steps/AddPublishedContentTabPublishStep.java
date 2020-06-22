package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Sections;
import com.sirma.sep.content.idoc.SectionNode.PublishMode;

/**
 * Publish step that removes the the marked as published tabs and replace them with tab that displays the published
 * content in pdf viewer
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 103)
public class AddPublishedContentTabPublishStep implements PublishStep {
	static final String DEFAULT_TAB_TITLE = "Published content";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String TEMPLATE_PATH = "published-content-tab-template.html";

	private static final String PUBLISH_TAB_TITLE_LABEL = "idoc.tabs.publishedTabTitle";

	@Inject
	private LabelProvider labelProvider;

	@Override
	public String getName() {
		return Steps.REPLACE_EXPORTED_TABS.getName();
	}

	@Override
	public void execute(PublishContext publishContext) {
		Sections sections = publishContext.getView().getSections();
		removePublishedTabs(sections);

		addContentTab(sections);
	}

	private void addContentTab(Sections sections) {
		try {
			Idoc template = Idoc.parse(getClass().getResourceAsStream(TEMPLATE_PATH));
			SectionNode tab = template.getSections()
					.stream()
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Invalid idoc template. No sections found"));
			tab.setTitle(getTabTitle());

			sections.addFirst(tab);
		} catch (IOException e) {
			LOGGER.warn("Could not load published tab template!", e);
		}
	}

	private String getTabTitle() {
		String label = labelProvider.getLabel(PUBLISH_TAB_TITLE_LABEL);
		if (nullSafeEquals(label, PUBLISH_TAB_TITLE_LABEL)) {
			label = DEFAULT_TAB_TITLE;
		}
		return label;
	}

	private static void removePublishedTabs(Sections sections) {
		Iterator<SectionNode> it = sections.iterator();
		while (it.hasNext()) {
			SectionNode node = it.next();
			if (node.getPublishMode() == PublishMode.EXPORT) {
				it.remove();
			}
		}
	}

}
