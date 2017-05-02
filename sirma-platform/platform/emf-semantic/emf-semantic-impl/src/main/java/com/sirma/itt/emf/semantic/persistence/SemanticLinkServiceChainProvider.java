package com.sirma.itt.emf.semantic.persistence;

import javax.inject.Inject;

import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.relation.LinkServiceChainProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Chain provider for semantic link service implementation.
 *
 * @author BBonev
 */
@Extension(target = LinkServiceChainProvider.TARGET_NAME, order = 20)
public class SemanticLinkServiceChainProvider implements LinkServiceChainProvider {

	@Inject
	@SemanticDb
	private LinkService linkService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkService provide() {
		return linkService;
	}
}
