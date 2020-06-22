package com.sirma.itt.seip.instance.revision.steps;

import java.util.Iterator;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.SectionNode.PublishMode;

/**
 * Remove tabs from Idoc that should not be included in the revision
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 108)
public class RemoveNonPublishedTabs implements PublishStep {

	@Override
	public void execute(PublishContext publishContext) {
		for (Iterator<SectionNode> it = publishContext.getView().getSections().iterator(); it.hasNext();) {
			SectionNode section = it.next();
			if (section.getPublishMode() == PublishMode.SKIP) {
				it.remove();
			}
		}
	}

	@Override
	public String getName() {
		return Steps.REMOVE_NON_PUBLISHED_TABS.getName();
	}

}
