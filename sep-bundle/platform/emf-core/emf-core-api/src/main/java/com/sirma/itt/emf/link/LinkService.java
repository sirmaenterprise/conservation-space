package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * Service class for managing links between instances. Each link has an identifier that is used to
 * fetch the destination objects. Between to objects can exist multiple links with different
 * identifiers and properties. Custom properties can be associated with each link instance. The link
 * can be bidirectional this means calling the method {@link #getLinkedObjects(Instance, String)}
 * with each of the ends of the link will return all objects from the other end that match the given
 * link id.
 *
 * @author BBonev
 */
public interface LinkService {

	/**
	 * Creates a relation between the given two instances with a direct type the given linkId. The
	 * created relation is of simple direct type and does not support extra properties or
	 * deactivation but direct removal only. The method creates a directional link between the given
	 * instance, the reverse link is created automatically by the database or does not exists.
	 * Calling this method is same as
	 * {@link #linkSimple(InstanceReference, InstanceReference, String, String)} with
	 * <code>null</code> last parameter.
	 * 
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return true, if successful
	 */
	boolean linkSimple(InstanceReference from, InstanceReference to, String linkId);

	/**
	 * Creates a relation between the given two instances with a direct type the given linkId. The
	 * created relation is of simple direct type and does not support extra properties or
	 * deactivation but direct removal only. The method creates directional links between the given
	 * instance depending on the link id arguments. If service will create the links if not null. If
	 * the reverse id is not present the call will be the same as calling
	 * {@link #linkSimple(InstanceReference, InstanceReference, String)}.
	 * 
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverseId
	 *            the reverse id
	 * @return true, if successful
	 */
	boolean linkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId);

	/**
	 * Links the given source and the list of instance with the given linkId.
	 *
	 * @param from
	 *            the from
	 * @param tos
	 *            the list of instance to link
	 * @param linkId
	 *            the link id
	 * @return true, if successful
	 */
	boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId);

	/**
	 * Gets the simple link references to the given instance reference.
	 * 
	 * @param to
	 *            the target reference
	 * @param linkId
	 *            the link id
	 * @return the simple link references
	 */
	List<LinkReference> getSimpleLinksTo(InstanceReference to, String linkId);

	/**
	 * Gets the simple link references.
	 *
	 * @param from
	 *            the from
	 * @param linkId
	 *            the link id
	 * @return the simple link references
	 */
	List<LinkReference> getSimpleLinks(InstanceReference from, String linkId);

	/**
	 * Unlink simple.
	 *
	 * @param from
	 *            the from
	 * @param linkId
	 *            the link id
	 */
	void unlinkSimple(InstanceReference from, String linkId);

	/**
	 * Unlink simple.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 */
	void unlinkSimple(InstanceReference from, InstanceReference to, String linkId);

	/**
	 * Unlink simple.
	 * 
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverseId
	 *            the reverse id
	 */
	void unlinkSimple(InstanceReference from, InstanceReference to, String linkId, String reverseId);

	/**
	 * Creates a bidirectional link between first argument to second argument with the specified
	 * main and reverse id and adds the given custom properties to the link.
	 * 
	 * @param from
	 *            the link from element
	 * @param to
	 *            the link to element
	 * @param mainLinkId
	 *            the link identifier for the direction from -> to
	 * @param reverseLinkId
	 *            the reverse link id for the direction to -> from. If the link ID is
	 *            <code>null</code> then the direction will be unidirectional.
	 * @param properties
	 *            the properties to add for the link, could be <code>null</code>.
	 * @return pair of generated ids for the created links. The first entry from the pair
	 *         corresponds to the first link from -> to, while the second element of the pair for
	 *         the relation to -> from.
	 */
	Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties);

	/**
	 * Creates a bidirectional link between first argument to second argument with the specified
	 * main and reverse id and adds the given custom properties to the link.
	 *
	 * @param from
	 *            the from instance reference
	 * @param to
	 *            the to instance reference
	 * @param mainLinkId
	 *            the link identifier for the direction from -> to
	 * @param reverseLinkId
	 *            the reverse link id for the direction to -> from. If the link ID is
	 *            <code>null</code> then the direction will be unidirectional.
	 * @param properties
	 *            the properties to add for the link
	 * @return pair of generated ids for the created links. The first entry from the pair
	 *         corresponds to the first link from -> to, while the second element of the pair for
	 *         the relation to -> from.
	 */
	Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties);

	/**
	 * Creates an association link of type parent between two instances in DMS. This is helpful
	 * method to link instances as context dependent of custom scope
	 * 
	 * @param from
	 *            the link from element
	 * @param to
	 *            the link to element
	 * @param assocName
	 *            is the custom association name. could be null
	 * @return true, if successful
	 */
	boolean associate(Instance from, Instance to, String assocName);

	/**
	 * Removes an association link of type parent between two instances in DMS. The method will
	 * remove all associations between the provided instances that match the given association name
	 * if present or all if not.
	 * 
	 * @param parent
	 *            the link from element
	 * @param child
	 *            the child element
	 * @param assocName
	 *            the association name to search for and remove if provided.
	 * @return true, if successful
	 */
	boolean dissociate(Instance parent, Instance child, String assocName);

	/**
	 * Creates an association link of type parent between two instances in DMS and removed an
	 * existing one. This is helpful method to link instances as context dependent of custom scope
	 * 
	 * @param from
	 *            the link from element
	 * @param to
	 *            the link to element
	 * @param oldFrom
	 *            is the old parent to remove association from
	 * @param assocName
	 *            is the custom association name. could be null
	 * @return true, if successful
	 */
	boolean reassociate(Instance from, Instance to, Instance oldFrom, String assocName);

	/**
	 * Gets the link information for link between the given source and link id.
	 *
	 * @param from
	 *            the link start element
	 * @param linkId
	 *            the link id
	 * @return the link instance
	 */
	List<LinkReference> getLinks(InstanceReference from, String linkId);

	/**
	 * Gets all links for the given source and set of ids.
	 *
	 * @param from
	 *            the link start element
	 * @param linkIds
	 *            the link ids
	 * @return the links
	 */
	List<LinkReference> getLinks(InstanceReference from, Set<String> linkIds);

	/**
	 * Gets all links for the given source element.
	 *
	 * @param from
	 *            the link start element
	 * @return the links
	 */
	List<LinkReference> getLinks(InstanceReference from);

	/**
	 * Gets all links that the given instance is the end of the link.
	 *
	 * @param to
	 *            the link end
	 * @return the links that have the given argument as end
	 */
	List<LinkReference> getLinksTo(InstanceReference to);

	/**
	 * Gets the link that the given instance is the end of the link and is from the specified type.
	 *
	 * @param to
	 *            the link end
	 * @param linkId
	 *            the link type to search for
	 * @return the links that have the given argument as end
	 */
	List<LinkReference> getLinksTo(InstanceReference to, String linkId);

	/**
	 * Gets the link instance by link instance unique DB id.
	 *
	 * @param id
	 *            the link instance DB id
	 * @return the link instance or <code>null</code> if not found
	 */
	LinkInstance getLinkInstance(Serializable id);

	/**
	 * Gets the link reference by link instance unique DB id.
	 *
	 * @param id
	 *            the link instance DB id
	 * @return the link reference or <code>null</code> if not found
	 */
	LinkReference getLinkReference(Serializable id);

	/**
	 * Gets the link references.
	 *
	 * @param from
	 *            the from
	 * @param linkId
	 *            the link id
	 * @param toFilter
	 *            the to filter
	 * @return the link references
	 */
	List<LinkReference> getLinks(InstanceReference from, String linkId,
			Class<? extends Instance> toFilter);

	/**
	 * Gets links that the given instance is the end of the link and is from the specified types.
	 *
	 * @param to
	 *            the link end
	 * @param linkIds
	 *            the link types to search for
	 * @return the links that have the given argument as end
	 */
	List<LinkReference> getLinksTo(InstanceReference to, Set<String> linkIds);

	/**
	 * Removes all links for the give instance. The method will remove the links where the given
	 * object is source or destination of the link.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if removed at least one link. Will return <code>false</code> if the object was
	 *         never part of a link
	 */
	boolean removeLinksFor(InstanceReference instance);

	/**
	 * Removes the links of particular type for the given instance. The method removes only one side
	 * of the links. If the links are bidirectional and they need to be removed, the method should
	 * be called with the reverse ids.
	 *
	 * @param instance
	 *            the instance to remove the link for
	 * @param linkIds
	 *            the link ids
	 * @return true, if successful
	 */
	boolean removeLinksFor(InstanceReference instance, Set<String> linkIds);

	/**
	 * Removes all links between the first and second instance.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @return true, if removed at least one link. Will return <code>false</code> if the objects
	 *         were never linked.
	 */
	boolean unlink(InstanceReference from, InstanceReference to);

	/**
	 * Removes a particular link between the given objects. Based on the reverse argument the method
	 * may also remove the reverse link if present. The method removes only non <code>null</code>
	 * link id arguments. If both arguments are <code>null</code> the method does nothing.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @param reverseLinkid
	 *            the reverse link Id to remove
	 * @return true, if removed at least one link. Will return <code>false</code> if the objects are
	 *         not linked with the given link Id.
	 */
	boolean unlink(InstanceReference from, InstanceReference to, String linkId, String reverseLinkid);

	/**
	 * Deletes the given link. Does not touch the reverse link if any.
	 *
	 * @param instance
	 *            the instance
	 */
	void removeLink(LinkInstance instance);

	/**
	 * Removes the link references by the given {@link LinkReference}.
	 *
	 * @param instance
	 *            the instance
	 */
	void removeLink(LinkReference instance);

	/**
	 * Removes the link by DB id.
	 *
	 * @param linkDbId
	 *            the link db id
	 */
	void removeLinkById(Serializable linkDbId);

	/**
	 * Update properties for a link that is represented by the given DB id.
	 *
	 * @param id
	 *            DB id of the link to update
	 * @param properties
	 *            the properties to update
	 * @return <code>true</code> if the operation was successful.
	 */
	boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties);

	/**
	 * Convert to link instance.
	 *
	 * @param source
	 *            the source
	 * @return the c
	 */
	LinkInstance convertToLinkInstance(LinkReference source);

	/**
	 * Convert to link instance.
	 *
	 * @param source
	 *            the source
	 * @param ignoreMissing
	 *            if to ignore the missing references
	 * @return the loaded instances
	 */
	List<LinkInstance> convertToLinkInstance(List<LinkReference> source, boolean ignoreMissing);

	/**
	 * Checks if a link exists between the given source and destination of the given link type
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return true, if is linked
	 */
	boolean isLinked(InstanceReference from, InstanceReference to, String linkId);

	/**
	 * Checks if a simple link exists between the given source and destination of the given link
	 * type
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkId
	 *            the link id
	 * @return true, if is linked simple
	 */
	boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId);

	/**
	 * Search for links using the given criteria. The implementation should provide result
	 * pagination as well as filtering.
	 * 
	 * @param arguments
	 *            the arguments
	 * @return the link search arguments
	 */
	LinkSearchArguments searchLinks(LinkSearchArguments arguments);
}
