/**
 *
 */
package com.sirma.itt.seip.resources.favorites;

import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Contains logic related to the favourites functionality. There are method for adding given instance to and removing it
 * form user favourites. The user can be passed or extracted. There also is a method, that updates the instance headers
 * favourite icon, when the some page is loaded.
 *
 * @author A. Kunchev
 */
public interface FavouritesService {

	/**
	 * Adds instance to the current logged user favourites. This is done by creating the relation between the user and
	 * the instance that is passed. The link that is created is simple. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#HAS_FAVOURITE} and it is created from the user to the
	 * instance. This method extracts the user, if you want to pass it instead, use the overloaded method.
	 * <p>
	 * This method fires FavouriteAddedEvent, when the relation was created successfully.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be marked as a favourite for the user
	 * @return true if the link is created successfully, false otherwise
	 */
	boolean add(InstanceReference instanceReference);

	/**
	 * Adds instance to the passed user favourites. This is done by creating the relation between the user and the
	 * instance that is passed. The link that is created is simple. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#HAS_FAVOURITE} and it is created from the user to the
	 * instance. This method accepts instance reference, that represents the user for whom the the instance is added as
	 * favourite.
	 * <p>
	 * This method fires FavouriteAddedEvent, when the relation was created successfully.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be marked as a favourite for the user
	 * @param userInstanceRef
	 *            the user instance reference
	 * @return true if the link is created successfully, false otherwise
	 */
	boolean add(InstanceReference instanceReference, InstanceReference userInstanceRef);

	/**
	 * Removes instance from the current logged user favourites. This is done by removing the simple link, that is
	 * created, when the instance is added to the user favourites. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#HAS_FAVOURITE}. This method extracts current logged
	 * user, if you want to pass it instead, use the overloaded method.
	 * <p>
	 * This method fires FavouriteDeletedEvent.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be removed from the user favourite
	 */
	void remove(InstanceReference instanceReference);

	/**
	 * Removes instance from favourites for passed user. This is done by removing the simple link, that is created, when
	 * the instance is added to the user favourites. The id of the relation is
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#HAS_FAVOURITE}. This method accepts instance reference,
	 * that represents the user for whom the the instance is removed from favourite.
	 * <p>
	 * This method fires FavouriteDeletedEvent.
	 *
	 * @param instanceReference
	 *            the reference to the instance that will be removed from the user favourite
	 * @param userInstanceRef
	 *            the user instance reference
	 */
	void remove(InstanceReference instanceReference, InstanceReference userInstanceRef);

	/**
	 * Get all favourites instances for the currently logged user. Extract all instances that have relation of type
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#HAS_FAVOURITE} to the current user. The user is extracted from the
	 * session.
	 *
	 * @return collection of favourites instances for the currently logged user
	 */
	Collection<InstanceReference> getAllForCurrentUser();

	/**
	 * Get all favourites instances for the given user. Extract all instances that have relation of type
	 * {@link com.sirma.itt.seip.instance.relation.LinkConstants#HAS_FAVOURITE} to the user.
	 *
	 * @param userInstanceRef
	 *            the user instance reference
	 * @return collection of favourites instances for the given user
	 */
	Collection<InstanceReference> getAllForUser(InstanceReference userInstanceRef);

	/**
	 * Updates favourite icon in the headers for single instance. First extract all favourite instances for the current
	 * logged user, then checks, if the passed instance is contained by the extracted favourite list for the user. If it
	 * is, then the passed instance is updated and returned, if not it is just returned.
	 * 
	 * @param <I>
	 *            instance types
	 * @param instance
	 *            the instance, which will be updated
	 * @return updated instance or the same instance, if it is not favourite for the current user
	 */
	<I extends Instance> I updateFavoriteStateForInstance(I instance);

	/**
	 * Updates the favourite icon in headers of the passed instances. First extract all favourite instances for the
	 * current logged user, then filters the passed instances. If they are in the used favourites, then their headers
	 * favourite icons are updated. This is done by replacing CSS class in the span element, which represents the icon.
	 * The headers are extracted from the instance properties and after the update, they are set back.
	 * 
	 * @param <I>
	 *            instance types
	 * @param instances
	 *            the collection of instances that will be processed
	 */
	<I extends Instance> void updateFavouriteStateForInstances(Collection<I> instances);

	/**
	 * Updates the favourite icon in headers of the passed instances. First extract all favourite instances for the
	 * passed user, then filters the passed instances. If they are in the used favourites, then their headers favourite
	 * icons are updated. This is done by replacing CSS class in the span element, which represents the icon. The
	 * headers are extracted from the instance properties and after the update, they are set back.
	 *
	 * @param <I>
	 *            instance types
	 * @param instances
	 *            the collection of instances that will be processed
	 * @param userInstanceRef
	 *            the user instance reference
	 */
	<I extends Instance> void updateFavouriteStateForInstances(Collection<I> instances,
			InstanceReference userInstanceRef);

}
