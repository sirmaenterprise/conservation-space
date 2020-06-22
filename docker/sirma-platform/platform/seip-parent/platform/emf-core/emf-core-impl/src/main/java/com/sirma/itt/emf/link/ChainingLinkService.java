package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		for (LinkService linkService : linkServices) {
			linkService.removeLinksFor(instance, linkIds);
		}
		return true;
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid) {
		for (LinkService linkService : linkServices) {
			linkService.unlink(from, to, linkId, reverseLinkid);
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
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		for (LinkService service : linkServices) {
			service.linkSimple(from, to, linkId);
		}
		return true;
	}

	@Override
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
	public void unlinkSimple(InstanceReference from, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, linkId);
		}
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		for (LinkService linkService : linkServices) {
			linkService.unlinkSimple(from, to, linkId);
		}
	}

	@Override
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
	public List<LinkReference> getInstanceRelations(Instance instance, Predicate<String> relationFilter) {
		if (linkServices.size() > 1) {
			return linkServices.get(1).getInstanceRelations(instance, relationFilter);
		}
		return linkServices.get(0).getInstanceRelations(instance, relationFilter);
	}
}
