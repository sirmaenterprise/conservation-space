package com.sirma.itt.seip.resources.favorites;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HAS_FAVOURITE;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
 * Concrete implementation of the FavoritesService. Contains logic for adding and removing favourites for user.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class FavouritesServiceImpl implements FavouritesService {

	@Inject
	@Chaining
	private LinkService chainingLinkService;

	@Inject
	@RelationalDb
	private LinkService relationalLinkService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private EventService eventService;

	@Override
	public boolean add(InstanceReference instanceReference) {
		InstanceReference instanceRef = getCurrentUser(securityContext);
		return add(instanceReference, instanceRef);
	}

	@Override
	public boolean add(InstanceReference instanceReference, InstanceReference userInstanceRef) {
		if (instanceReference != null && userInstanceRef != null) {
			boolean relationCreated = chainingLinkService.linkSimple(userInstanceRef, instanceReference,
					LinkConstants.HAS_FAVOURITE);
			if (relationCreated) {
				eventService.fire(new FavouriteAddedEvent(userInstanceRef, instanceReference));
			}
			return relationCreated;
		}
		return false;
	}

	@Override
	public void remove(InstanceReference instanceReference) {
		InstanceReference userInstanceRef = getCurrentUser(securityContext);
		remove(instanceReference, userInstanceRef);
	}

	@Override
	public void remove(InstanceReference instanceReference, InstanceReference userInstanceRef) {
		if (instanceReference != null && userInstanceRef != null) {
			chainingLinkService.unlinkSimple(userInstanceRef, instanceReference, LinkConstants.HAS_FAVOURITE);
			eventService.fire(new FavouriteDeletedEvent(userInstanceRef, instanceReference));
		}
	}

	@Override
	public Collection<InstanceReference> getAllForCurrentUser() {
		InstanceReference userInstanceRef = getCurrentUser(securityContext);
		return getAllForUser(userInstanceRef);
	}

	@Override
	public Collection<InstanceReference> getAllForUser(InstanceReference userInstanceRef) {
		if (userInstanceRef == null) {
			return Collections.emptyList();
		}

		return new LinkIterable<>(relationalLinkService.getSimpleLinks(userInstanceRef, LinkConstants.HAS_FAVOURITE));
	}

	@Override
	public <I extends Instance> I updateFavoriteStateForInstance(I instance) {
		InstanceReference userInstanceRef = getCurrentUser(securityContext);

		if (instance == null || userInstanceRef == null) {
			return instance;
		}

		// extracting favourite instances for the user
		boolean isAnyMatch = relationalLinkService
				.getSimpleLinks(userInstanceRef, LinkConstants.HAS_FAVOURITE)
					.stream()
					.map(linkReference -> linkReference.getTo().getId())
					.anyMatch(favoriteInstancesId -> favoriteInstancesId.equals(instance.getId()));

		instance.add(HAS_FAVOURITE, Boolean.valueOf(isAnyMatch));

		return instance;
	}

	@Override
	public <I extends Instance> void updateFavouriteStateForInstances(Collection<I> instances) {
		InstanceReference userInstanceRef = getCurrentUser(securityContext);
		updateFavouriteStateForInstances(instances, userInstanceRef);
	}

	@Override
	public <I extends Instance> void updateFavouriteStateForInstances(Collection<I> instances,
			InstanceReference userInstanceRef) {
		if (CollectionUtils.isEmpty(instances) || userInstanceRef == null) {
			return;
		}

		// mapping the instances with their ids
		Map<String, I> mappedInstances = instances.stream().collect(
				Collectors.toMap(instance -> instance.getId().toString(), Function.identity(), (key1, key2) -> key1));

		if (mappedInstances.isEmpty()) {
			return;
		}

		// filters and updates favourites instances headers
		relationalLinkService
				.getSimpleLinks(userInstanceRef, LinkConstants.HAS_FAVOURITE)
					.stream()
					.map(linkReference -> mappedInstances.get(linkReference.getTo().getId()))
					.filter(Objects::nonNull)
					.forEach(instance -> instance.add(HAS_FAVOURITE, Boolean.TRUE));
	}

	/**
	 * Gets the current logged user, if there is one.
	 *
	 * @param securityContext
	 *            current security context instance
	 * @return the reference for current logged user or <b>null</b>, if there is no authenticated user
	 */
	private static InstanceReference getCurrentUser(SecurityContext securityContext) {
		com.sirma.itt.seip.security.User authenticatedUser = securityContext.getAuthenticated();
		if (authenticatedUser instanceof User) {
			User currentUser = (User) authenticatedUser;
			return currentUser.toReference();
		}
		return null;
	}

}
