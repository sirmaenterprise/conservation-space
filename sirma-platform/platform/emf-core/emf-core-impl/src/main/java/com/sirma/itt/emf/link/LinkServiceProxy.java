package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.relation.LinkServiceChainProvider;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Proxy between the two implementations of the {@link LinkService}. If semantic implementation is present then it will
 * be used otherwise the relational implementation will be used.
 * <p>
 *
 * @author BBonev
 */
@ApplicationScoped

public class LinkServiceProxy implements LinkService {

	/** Reverse iterator. The last implementation will be used for the services that is present */
	@Inject
	@ExtensionPoint(value = LinkServiceChainProvider.TARGET_NAME, reverseOrder = true, singleton = true)
	private Iterable<LinkServiceChainProvider> chain;

	/** The link service. */
	private LinkService linkService;

	/**
	 * Initialize the active link service.
	 */
	@PostConstruct
	public void initialize() {
		// if semantic implementation is present will use it
		linkService = chain.iterator().next().provide();
	}

	@Override
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {
		return linkService.link(from, to, mainLinkId, reverseLinkId, properties);
	}

	@Override
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		return linkService.link(from, to, mainLinkId, reverseLinkId, properties);
	}

	@Override
	public boolean associate(Instance from, Instance to, String assocName) {
		return linkService.associate(from, to, assocName);
	}

	@Override
	public boolean reassociate(Instance from, Instance to, Instance oldParent, String assocName) {
		return linkService.reassociate(from, to, oldParent, assocName);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		return linkService.getLinks(from, linkId);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds) {
		return linkService.getLinks(from, linkIds);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to) {
		return linkService.getLinksTo(to);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return linkService.getLinksTo(to, linkId);
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds) {
		return linkService.getLinksTo(to, linkIds);
	}

	@Override
	public boolean removeLinksFor(InstanceReference instance) {
		return linkService.removeLinksFor(instance);
	}

	@Override
	public boolean removeLinksFor(InstanceReference instance, Set<String> linkIds) {
		return linkService.removeLinksFor(instance, linkIds);
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to) {
		return linkService.unlink(from, to);
	}

	@Override
	public boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid) {
		return linkService.unlink(from, to, linkId, reverseLinkid);
	}

	@Override
	public void removeLink(LinkInstance instance) {
		linkService.removeLink(instance);
	}

	@Override
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		return linkService.updateLinkProperties(id, properties);
	}

	@Override
	public LinkInstance convertToLinkInstance(LinkReference source) {
		return linkService.convertToLinkInstance(source);
	}

	@Override
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source) {
		return linkService.convertToLinkInstance(source);
	}

	@Override
	public void removeLink(LinkReference instance) {
		linkService.removeLink(instance);
	}

	@Override
	public void removeLinkById(Serializable linkDbId) {
		linkService.removeLinkById(linkDbId);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkService.linkSimple(from, to, linkId);
	}

	@Override
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		return linkService.linkSimple(from, tos, linkId);
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, String linkId) {
		return linkService.getSimpleLinks(from, linkId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, String linkId) {
		linkService.unlinkSimple(from, linkId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId) {
		linkService.unlinkSimple(from, to, linkId);
	}

	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		return linkService.isLinked(from, to, linkId);
	}

	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		return linkService.isLinkedSimple(from, to, linkId);
	}

	@Override
	public LinkInstance getLinkInstance(Serializable id) {
		return linkService.getLinkInstance(id);
	}

	@Override
	public LinkReference getLinkReference(Serializable id) {
		return linkService.getLinkReference(id);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		return linkService.getLinks(from);
	}

	@Override
	public List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId) {
		return linkService.getSimpleLinksTo(to, linkId);
	}

	@Override
	public boolean dissociate(Instance parent, Instance child, String assocName) {
		return linkService.dissociate(parent, child, assocName);
	}

	@Override
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		return linkService.searchLinks(arguments);
	}

	@Override
	public boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		return linkService.linkSimple(from, to, linkId, reverseId);
	}

	@Override
	public void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId) {
		linkService.unlinkSimple(from, to, linkId, reverseId);
	}

	@Override
	public List<LinkReference> getSimpleLinks(InstanceReference from, Set<String> linkIds) {
		return linkService.getSimpleLinks(from, linkIds);
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance) {
		return linkService.getInstanceRelations(instance);
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance, Predicate<String> relationFilter) {
		return linkService.getInstanceRelations(instance, relationFilter);
	}

	@Override
	public Pair<List<LinkReference>, List<LinkReference>> getRelationsDiff(InstanceReference source,
			Map<String, ? extends Collection<InstanceReference>> currentRelations) {
		return linkService.getRelationsDiff(source, currentRelations);
	}

	@Override
	public void saveRelations(List<LinkReference> toAdd, List<LinkReference> toRemove,
			Consumer<LinkReference> onSuccessAdd, Consumer<LinkReference> onSuccessRemove) {
		linkService.saveRelations(toAdd, toRemove, onSuccessAdd, onSuccessRemove);
	}

}
