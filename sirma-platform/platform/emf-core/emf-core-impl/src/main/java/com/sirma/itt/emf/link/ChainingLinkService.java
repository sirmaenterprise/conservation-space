package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Chaining;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.relation.LinkServiceChainProvider;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Chaining proxy implementation for {@link LinkService}. The create and delete method calls all available
 * implementations of the service. Get methods fetch data only from the relational (first) service from the chain.
 *
 * @author BBonev
 */
@ApplicationScoped
@Chaining
public class ChainingLinkService implements LinkService {

	/** The relational link service. */
	@Inject
	@ExtensionPoint(value = LinkServiceChainProvider.TARGET_NAME, singleton = true)
	private Iterable<LinkServiceChainProvider> chain;

	private List<LinkService> linkServices;

	/**
	 * Initialize the active link service.
	 */
	@PostConstruct
	public void initialize() {
		linkServices = new ArrayList<>(5);
		for (LinkServiceChainProvider provider : chain) {
			linkServices.add(provider.provide());
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {
		Pair<Serializable, Serializable> pair = null;
		for (LinkService linkService : linkServices) {
			pair = linkService.link(from, to, mainLinkId, reverseLinkId, properties);
		}
		if (pair == null) {
			return Pair.nullPair();
		}
		return pair;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to, String mainLinkId,
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

	@Override
	public boolean associate(Instance from, Instance to, String assocName) {
		return linkServices.get(0).associate(from, to, assocName);
	}

	@Override
	public boolean reassociate(Instance from, Instance to, Instance oldParent, String assocName) {
		return linkServices.get(0).reassociate(from, to, oldParent, assocName);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		return linkServices.get(0).getLinks(from, linkId);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return linkServices.get(0).getLinks(from, linkIds);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return linkServices.get(0).getLinksTo(to);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return linkServices.get(0).getLinksTo(to, linkId);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		return linkServices.get(0).getLinksTo(to, linkIds);
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance) {
		for (LinkService linkService : linkServices) {
			linkService.removeLinksFor(instance);
		}
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		for (LinkService linkService : linkServices) {
			linkService.removeLinksFor(instance, linkIds);
		}
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to) {
		for (LinkService linkService : linkServices) {
			linkService.unlink(from, to);
		}
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid) {
		for (LinkService linkService : linkServices) {
			linkService.unlink(from, to, linkId, reverseLinkid);
		}
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void removeLink(LinkInstance instance) {
		for (LinkService linkService : linkServices) {
			linkService.removeLink(instance);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		for (LinkService linkService : linkServices) {
			linkService.updateLinkProperties(id, properties);
		}
		return true;
	}

	@Override
	public LinkInstance convertToLinkInstance(LinkReference source) {
		return linkServices.get(0).convertToLinkInstance(source);
	}

	@Override
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source) {
		return linkServices.get(0).convertToLinkInstance(source);
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void removeLink(LinkReference instance) {
		for (LinkService linkService : linkServices) {
			linkService.removeLink(instance);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void removeLinkById(Serializable linkDbId) {
		for (LinkService service : linkServices) {
			service.removeLinkById(linkDbId);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		for (LinkService service : linkServices) {
			service.linkSimple(from, to, linkId);
		}
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		for (LinkService service : linkServices) {
			service.linkSimple(from, to, linkId, reverseId);
		}
		return true;
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getSimpleLinks(from, linkId);
		}
		return linkServices.get(0).getSimpleLinks(from, linkId);
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.linkSimple(from, tos, linkId);
		}
		return true;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void unlinkSimple(InstanceReference from, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, linkId);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, to, linkId);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, to, linkId, reverseId);
		}
	}

	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		return linkServices.get(0).isLinked(from, to, linkId);
	}

	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkServices.get(linkServices.size() - 1).isLinkedSimple(from, to, linkId);
	}

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

	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		return linkServices.get(0).getLinks(from);
	}

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

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, Set<String> linkIds) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getSimpleLinks(from, linkIds);
		}
		return linkServices.get(0).getSimpleLinks(from, linkIds);
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getInstanceRelations(instance);
		}
		return linkServices.get(0).getInstanceRelations(instance);
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance, Predicate<String> relationFilter) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getInstanceRelations(instance, relationFilter);
		}
		return linkServices.get(0).getInstanceRelations(instance, relationFilter);
	}

	@Override
	public Pair<List<LinkReference>, List<LinkReference>> getRelationsDiff(InstanceReference source,
			Map<String, ? extends Collection<InstanceReference>> currentRelations) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getRelationsDiff(source, currentRelations);
		}
		return linkServices.get(0).getRelationsDiff(source, currentRelations);
	}

	@Override
	public void saveRelations(List<LinkReference> toAdd, List<LinkReference> toRemove,
			Consumer<LinkReference> onSuccessAdd, Consumer<LinkReference> onSuccessRemove) {
		for (LinkService linkService : linkServices) {
			linkService.saveRelations(toAdd, toRemove, onSuccessAdd, onSuccessRemove);
		}
	}


}
