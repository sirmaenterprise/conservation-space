package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.ArrayList;
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
import com.sirma.itt.emf.plugin.Chaining;
import com.sirma.itt.emf.security.Secure;

/**
 * Proxy between the two implementations of the {@link LinkService}. If semantic implementation is
 * present then it will be used otherwise the relational implementation will be used.<br>
 * REVIEW: we could implement something with the plugin API and not to be limited with only 2
 * realizations, but for now this will work fine.
 *
 * @author BBonev
 */
@Stateless
@Chaining
public class ChainingLinkService implements LinkService {

	/** The relational link service. */
	@Inject
	@RelationalDb
	private LinkService relationalLinkService;

	/** The semantic link service. */
	@Inject
	@SemanticDb
	private javax.enterprise.inject.Instance<LinkService> semanticLinkService;

	/** The link services. */
	private List<LinkService> linkServices;

	/**
	 * Initialize the active link service.
	 */
	@PostConstruct
	public void initialize() {
		linkServices = new ArrayList<>(2);
		linkServices.add(relationalLinkService);
		// if semantic implementation is present will use it
		// REVIEW: we could add an option to for preferred implementation
		if (!semanticLinkService.isUnsatisfied()) {
			linkServices.add(semanticLinkService.get());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		Pair<Serializable, Serializable> pair = null;
		for (LinkService linkService : linkServices) {
			pair = linkService.link(from, to, mainLinkId, reverseLinkId, properties);
		}
		if (pair == null) {
			return Pair.nullPair();
		}
		return pair;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties) {
		Pair<Serializable, Serializable> pair = null;
		for (LinkService linkService : linkServices) {
			pair = linkService.link(from, to, mainLinkId, reverseLinkId, properties);
		}
		if (pair == null) {
			return Pair.nullPair();
		}
		return pair;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Secure
	public boolean associate(Instance from, Instance to, String assocName) {
		return linkServices.get(0).associate(from, to, assocName);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Secure
	public boolean reassociate(Instance from, Instance to, Instance oldParent, String assocName) {
		return linkServices.get(0).reassociate(from, to, oldParent, assocName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		return linkServices.get(0).getLinks(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return linkServices.get(0).getLinks(from, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return linkServices.get(0).getLinksTo(to);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return linkServices.get(0).getLinksTo(to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		return linkServices.get(0).getLinksTo(to, linkIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance) {
		for (LinkService linkService : linkServices) {
			linkService.removeLinksFor(instance);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		for (LinkService linkService : linkServices) {
			linkService.removeLinksFor(instance, linkIds);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to) {
		for (LinkService linkService : linkServices) {
			linkService.unlink(from, to);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId,
			String reverseLinkid) {
		for (LinkService linkService : linkServices) {
			linkService.unlink(from, to, linkId, reverseLinkid);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeLink(LinkInstance instance) {
		for (LinkService linkService : linkServices) {
			linkService.removeLink(instance);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		for (LinkService linkService : linkServices) {
			linkService.updateLinkProperties(id, properties);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LinkInstance convertToLinkInstance(LinkReference source) {
		return linkServices.get(0).convertToLinkInstance(source);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source,
			boolean ignoreMissing) {
		return linkServices.get(0).convertToLinkInstance(source, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLink(LinkReference instance) {
		for (LinkService linkService : linkServices) {
			linkService.removeLink(instance);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLinkById(Serializable linkDbId) {
		for (LinkService service : linkServices) {
			service.removeLinkById(linkDbId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		for (LinkService service : linkServices) {
			service.linkSimple(from, to, linkId);
		}
		return true;
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		for (LinkService service : linkServices) {
			service.linkSimple(from, to, linkId, reverseId);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getSimpleLinks(from, linkId);
		}
		return linkServices.get(0).getSimpleLinks(from, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId,
			Class<? extends Instance> toFilter) {
		return linkServices.get(0).getLinks(from, linkId, toFilter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.linkSimple(from, tos, linkId);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, linkId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, to, linkId);
		}
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId,
			String reverseId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, to, linkId, reverseId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		return linkServices.get(0).isLinked(from, to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkServices.get(linkServices.size() - 1).isLinkedSimple(from, to, linkId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkInstance getLinkInstance(Serializable id) {
		for (LinkService service : linkServices) {
			LinkInstance instance = service.getLinkInstance(id);
			if (instance != null) {
				return instance;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkReference getLinkReference(Serializable id) {
		for (LinkService service : linkServices) {
			LinkReference reference = service.getLinkReference(id);
			if (reference != null) {
				return reference;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		return linkServices.get(0).getLinks(from);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getSimpleLinksTo(to, linkId);
		}
		return linkServices.get(0).getSimpleLinksTo(to, linkId);
	}

	@Override
	public boolean dissociate(Instance parent, Instance child, String assocName) {
		return linkServices.get(0).dissociate(parent, child, assocName);
	}

	@Override
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).searchLinks(arguments);
		}
		return linkServices.get(0).searchLinks(arguments);
	}
}
