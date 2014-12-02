package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.Secure;

/**
 * Proxy between the two implementations of the {@link LinkService}. If semantic implementation is
 * present then it will be used otherwise the relational implementation will be used.
 * <p>
 * <b>NOTE:</b>The transaction management should stay with required transaction policy for all
 * method otherwise the semantic implementation will crash for the non transactional methods.
 * <p>
 * REVIEW: we could implement something with the plugin API and not to be limited with only 2
 * realizations, but for now this will work fine.
 *
 * @author BBonev
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class LinkServiceProxy implements LinkService {

	/** The relational link service. */
	@Inject
	@RelationalDb
	private LinkService relationalLinkService;

	/** The semantic link service. */
	@Inject
	@SemanticDb
	private javax.enterprise.inject.Instance<LinkService> semanticLinkService;

	/** The link service. */
	private LinkService linkService;

	/**
	 * Initialize the active link service.
	 */
	@PostConstruct
	public void initialize() {
		// if semantic implementation is present will use it
		// REVIEW: we could add an option to for preferred implementation
		if (semanticLinkService.isUnsatisfied()) {
			linkService = relationalLinkService;
		} else {
			linkService = semanticLinkService.get();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		return linkService.link(from, to, mainLinkId, reverseLinkId, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties) {
		return linkService.link(from, to, mainLinkId, reverseLinkId, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Secure
	public boolean associate(Instance from, Instance to, String assocName) {
		return linkService.associate(from, to, assocName);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Secure
	public boolean reassociate(Instance from, Instance to, Instance oldParent, String assocName) {
		return linkService.reassociate(from, to, oldParent, assocName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		return linkService.getLinks(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return linkService.getLinks(from, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return linkService.getLinksTo(to);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return linkService.getLinksTo(to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		return linkService.getLinksTo(to, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance) {
		return linkService.removeLinksFor(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		return linkService.removeLinksFor(instance, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to) {
		return linkService.unlink(from, to);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId,
			String reverseLinkid) {
		return linkService.unlink(from, to, linkId, reverseLinkid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeLink(LinkInstance instance) {
		linkService.removeLink(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		return linkService.updateLinkProperties(id, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkInstance convertToLinkInstance(LinkReference source) {
		return linkService.convertToLinkInstance(source);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source,
			boolean ignoreMissing) {
		return linkService.convertToLinkInstance(source, ignoreMissing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeLink(LinkReference instance) {
		linkService.removeLink(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeLinkById(Serializable linkDbId) {
		linkService.removeLinkById(linkDbId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkService.linkSimple(from, to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		return linkService.linkSimple(from, tos, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		return linkService.getSimpleLinks(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void unlinkSimple(InstanceReference from, String linkId) {
		linkService.unlinkSimple(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		linkService.unlinkSimple(from, to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId,
			Class<? extends Instance> toFilter) {
		return linkService.getLinks(from, linkId, toFilter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		return linkService.isLinked(from, to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkService.isLinkedSimple(from, to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkInstance getLinkInstance(Serializable id) {
		return linkService.getLinkInstance(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkReference getLinkReference(Serializable id) {
		return linkService.getLinkReference(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		return linkService.getLinks(from);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		return linkService.getSimpleLinksTo(to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean dissociate(Instance parent, Instance child, String assocName) {
		return linkService.dissociate(parent, child, assocName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		return linkService.searchLinks(arguments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		return linkService.linkSimple(from, to, linkId, reverseId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		linkService.unlinkSimple(from, to, linkId, reverseId);
	}
}
