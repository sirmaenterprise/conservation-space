package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.ServiceChainProvider;

/**
 * Chain provider extension point for {@link LinkService}.
 *
 * @author BBonev
 */
@Documentation("Chain provider extension point for {@link LinkService}.")
public interface LinkServiceChainProvider extends ServiceChainProvider<LinkService> {
	String TARGET_NAME = "linkServiceChainProvider";
}
