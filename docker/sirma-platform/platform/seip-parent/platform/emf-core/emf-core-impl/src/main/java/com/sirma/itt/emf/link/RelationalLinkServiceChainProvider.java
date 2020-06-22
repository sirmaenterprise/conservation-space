package com.sirma.itt.emf.link;

import javax.inject.Inject;

import com.sirma.itt.seip.db.RelationalDb;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.relation.LinkServiceChainProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Chain provider for relational link service implementation.
 *
 * @author BBonev
 */
@Extension(target = LinkServiceChainProvider.TARGET_NAME, order = 10)
public class RelationalLinkServiceChainProvider implements LinkServiceChainProvider {

	/** The link service. */
	@Inject
	@RelationalDb
	private LinkService linkService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkService provide() {
		return linkService;
	}

}
