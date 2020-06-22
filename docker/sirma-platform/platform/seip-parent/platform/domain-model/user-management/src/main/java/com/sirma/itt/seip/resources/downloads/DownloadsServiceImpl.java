package com.sirma.itt.seip.resources.downloads;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MARKED_FOR_DOWNLOAD;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.annotation.Chaining;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.db.RelationalDb;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Concrete implementation of the DownloadsService. Contains logic for adding and removing downloads for user. Also
 * contains methods for creating, removing and getting archive(zip file) from DMS.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class DownloadsServiceImpl implements DownloadsService {

	@Inject
	private EventService eventService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	@RelationalDb
	private LinkService relationalLinkService;

	@Inject
	private DownloadsAdapterService downloadsAdapterService;

	@Inject
	@Chaining
	private LinkService chainingLinkService;

	@Override
	public boolean add(InstanceReference instanceReference) {
		InstanceReference currentUser = getCurrentUser(securityContext);
		return add(instanceReference, currentUser);
	}

	@Override
	public boolean add(InstanceReference instanceReference, InstanceReference userInstanceReference) {
		if (instanceReference != null && userInstanceReference != null) {
			boolean relationCreated = chainingLinkService.linkSimple(userInstanceReference, instanceReference,
					LinkConstants.MARKED_FOR_DOWNLOAD);
			if (relationCreated) {
				eventService.fire(new AddedToDownloadsEvent(userInstanceReference, instanceReference));
			}
			return relationCreated;
		}
		return false;
	}

	@Override
	public void remove(InstanceReference instanceReference) {
		InstanceReference currentUser = getCurrentUser(securityContext);
		remove(instanceReference, currentUser);
	}

	@Override
	public void remove(InstanceReference instanceReference, InstanceReference userInstanceReference) {
		if (instanceReference != null && userInstanceReference != null) {
			chainingLinkService.unlinkSimple(userInstanceReference, instanceReference,
					LinkConstants.MARKED_FOR_DOWNLOAD);
			eventService.fire(new DeletedFromDownloadsEvent(userInstanceReference, instanceReference));
		}
	}

	@Override
	public boolean removeAll() {
		InstanceReference currentUser = getCurrentUser(securityContext);
		return removeAll(currentUser);
	}

	@Override
	public boolean removeAll(InstanceReference userInstanceReference) {
		if (userInstanceReference != null) {
			getAll(userInstanceReference).forEach(instanceReference -> chainingLinkService
					.unlinkSimple(userInstanceReference, instanceReference, LinkConstants.MARKED_FOR_DOWNLOAD));
			eventService.fire(new DeletedAllFromDownloadsEvent(userInstanceReference));
			return true;
		}
		return false;
	}

	@Override
	public Collection<InstanceReference> getAll() {
		InstanceReference currentUser = getCurrentUser(securityContext);
		return getAll(currentUser);
	}

	@Override
	public Collection<InstanceReference> getAll(InstanceReference userInstanceReference) {
		if (userInstanceReference == null) {
			return Collections.emptyList();
		}
		return new LinkIterable<>(
				relationalLinkService.getSimpleLinks(userInstanceReference, LinkConstants.MARKED_FOR_DOWNLOAD));
	}

	@Override
	public <I extends Instance> I updateDownloadStateForInstance(I instance) {
		InstanceReference userInstanceRef = getCurrentUser(securityContext);

		if (instance == null || userInstanceRef == null || !instance.isUploaded()) {
			return instance;
		}

		// extracting downloads instances for the user
		boolean isAnyMatch = relationalLinkService
				.getSimpleLinks(userInstanceRef, LinkConstants.MARKED_FOR_DOWNLOAD)
					.stream()
					.map(linkReference -> linkReference.getTo().getId())
					.anyMatch(downloadsInstancesId -> downloadsInstancesId.equals(instance.getId()));

		instance.add(MARKED_FOR_DOWNLOAD, Boolean.valueOf(isAnyMatch));

		return instance;
	}

	@Override
	public <I extends Instance> void updateDownloadStateForInstances(Collection<I> instances) {
		InstanceReference currentUser = getCurrentUser(securityContext);
		updateDownloadStateForInstances(instances, currentUser);
	}

	@Override
	public <I extends Instance> void updateDownloadStateForInstances(Collection<I> instances,
			InstanceReference userInstanceReference) {
		if (CollectionUtils.isEmpty(instances) || userInstanceReference == null) {
			return;
		}

		// mapping the instances with their ids
		Map<String, I> mappedInstances = instances.stream().filter(Instance::isUploaded).collect(
				Collectors.toMap(instance -> instance.getId().toString(), Function.identity(), (key1, key2) -> key1));

		if (mappedInstances.isEmpty()) {
			return;
		}

		// updates icons in instance headers for instances marked for download
		relationalLinkService
				.getSimpleLinks(userInstanceReference, LinkConstants.MARKED_FOR_DOWNLOAD)
					.stream()
					.map(linkReference -> mappedInstances.get(linkReference.getTo().getId()))
					.filter(Objects::nonNull)
					.forEach(instance -> instance.add(MARKED_FOR_DOWNLOAD, Boolean.TRUE));
	}

	@Override
	public String createArchive() {
		Collection<InstanceReference> instances = getAll();
		if (instances.isEmpty()) {
			return null;
		}

		Collection<Serializable> instanceIds = instances.stream().map(InstanceReference::getId).collect(
				Collectors.toList());

		return downloadsAdapterService.createArchive(instanceIds);
	}

	@Override
	public String getArchiveStatus(String archiveId) {
		if (StringUtils.isBlank(archiveId)) {
			return null;
		}

		return downloadsAdapterService.getArchiveStatus(archiveId);
	}

	@Override
	public String removeArchive(String archiveId) {
		if (StringUtils.isBlank(archiveId)) {
			return null;
		}

		return downloadsAdapterService.removeArchive(archiveId);
	}

	@Override
	public String getArchiveLink(String archiveId) {
		if (StringUtils.isBlank(archiveId)) {
			return null;
		}

		return downloadsAdapterService.getArchiveURL(archiveId);
	}

	/**
	 * Gets the current logged user, if there is one.
	 *
	 * @param securityContext
	 *            current security context instance
	 * @return the reference for current logged user or <b>null</b>, if there is no authenticated user
	 */
	private static InstanceReference getCurrentUser(SecurityContext securityContext) {
		com.sirma.itt.seip.security.User authenticated = securityContext.getAuthenticated();
		if (authenticated instanceof User) {
			User currentUser = (User) authenticated;
			return currentUser.toReference();
		}
		return null;
	}

}
