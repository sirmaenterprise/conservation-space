package com.sirma.itt.emf.definition.compile;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.Mergeable.MergeableInstanceFactory;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Helper utility class for merging objects and list of objects.
 *
 * @author BBonev
 */
public class MergeHelper {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(MergeHelper.class);

	/** The Constant trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();

	/**
	 * Instantiates a new merge helper.
	 */
	private MergeHelper() {
		// utility class
	}

	/**
	 * If the current element is <code>null</code> returns the new value.
	 *
	 * @param <E>
	 *            the element type
	 * @param current
	 *            the current
	 * @param newValue
	 *            the new value
	 * @return the e
	 */
	public static <E> E replaceIfNull(E current, E newValue) {
		return current == null ? newValue : current;
	}

	/**
	 * If the current element is <code>null</code> returns the new value.
	 *
	 * @param current
	 *            the current
	 * @param newValue
	 *            the new value
	 * @return the e
	 */
	public static String replaceIfNull(String current, String newValue) {
		return StringUtils.isEmpty(current) ? newValue : current;
	}

	/**
	 * If the current value is set and the new value is also not
	 * <code>null</code> then the new value will be returned otherwise the
	 * current one.
	 *
	 * @param <E>
	 *            the element type
	 * @param current
	 *            the current
	 * @param newValue
	 *            the new value
	 * @return the e
	 */
	public static <E> E replaceIfNotNull(E current, E newValue) {
		return (current == null) || (newValue != null) ? newValue : current;
	}

	/**
	 * Merge lists. IF the element from source list does not exists into the destination then the
	 * factory is used to create new element and source is merged with that element. Then the
	 * created element is added to destination list. In other words, if destination list is empty
	 * then the returned list is a deep copy of the source list.
	 * <p>
	 * <b>NOTE 1:</b> elements of the lists must be searcheable in the list.
	 * <p>
	 * <b>NOTE 2:</b> the elements that exist in the destination list and are not contained into
	 * source list will NOT be modified. In order to modify them call again merge with themselves!
	 *
	 * @param <E>
	 *            the element type
	 * @param dest
	 *            the destination list
	 * @param src
	 *            the source list
	 * @param factory
	 *            the factory
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<Mergeable<E>> mergeLists(
			List<Mergeable<E>> dest, List<Mergeable<E>> src,
			MergeableInstanceFactory<Mergeable<E>> factory) {
		if (TRACE) {
			LOGGER.trace(">>>>BEGIN merging lists of type " + factory);
		}
		List<Mergeable<E>> localDest = dest;
		if (localDest == null) {
			localDest = new LinkedList<Mergeable<E>>();
		}
		if (src == null) {
			return localDest;
		}
		if (localDest.isEmpty()) {
			if (TRACE) {
				LOGGER.trace("Destination list is empty");
			}
			for (Mergeable<E> item : src) {
				if (TRACE) {
					LOGGER.trace("Creating new instance from " + item.getIdentifier());
				}
				Mergeable<Mergeable<E>> mergeable = factory.createInstance();
				localDest.add(mergeable.mergeFrom(item));
			}
		} else {
			for (Mergeable<E> item : src) {
				int indexOf = localDest.indexOf(item);
				if (indexOf != -1) {
					Mergeable<E> e = dest.get(indexOf);
					e.mergeFrom((E) item);
				} else {
					Mergeable<Mergeable<E>> mergeable = factory.createInstance();
					localDest.add(mergeable.mergeFrom(item));
				}
			}
		}
		if (TRACE) {
			LOGGER.trace("<<<<END merging lists of type " + factory);
		}
		return localDest;
	}

	/**
	 * Copy or merges two lists. If the destination list does not contains the source element then
	 * that element is added to destination list. If element exists then the two are merged. In
	 * other words if the destination list is empty the returned list will contain all elements of
	 * the source list without any modifications.
	 * <p>
	 * <b>NOTE 1:</b> elements of the lists must be searcheable in the list.
	 * <p>
	 * <b>NOTE 2:</b> the elements that exist in the destination list and are not contained into
	 * source list will NOT be modified. In order to modify them call again merge with themselves!
	 *
	 * @param <E>
	 *            the element type
	 * @param dest
	 *            the destination list
	 * @param src
	 *            the source list
	 * @return the list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E> List<Mergeable<E>> copyOrMergeLists(List<Mergeable<E>> dest,
			List<Mergeable<E>> src) {
		List<Mergeable<E>> localDest = dest;
		if (localDest == null) {
			localDest = new LinkedList<Mergeable<E>>();
		}
		if (src == null) {
			return localDest;
		}
		if (localDest.isEmpty()) {
			if (TRACE) {
				LOGGER.trace("Destination list is empty");
			}
			for (Mergeable<E> mergeable : src) {
				if (mergeable instanceof GenericProxy) {
					localDest.add((Mergeable<E>) ((GenericProxy) mergeable).cloneProxy());
				} else {
					localDest.add(mergeable);
				}
			}
		} else {
			for (Mergeable<E> item : src) {
				int indexOf = localDest.indexOf(item);
				if (indexOf != -1) {
					Mergeable<E> e = dest.get(indexOf);
					e.mergeFrom((E) item);
				} else {
					if (item instanceof GenericProxy) {
						localDest.add((Mergeable<E>) ((GenericProxy) item).cloneProxy());
					} else {
						localDest.add(item);
					}
				}
			}
		}
		return localDest;
	}

	/**
	 * Merge list elements. The method does not modify the list but only the
	 * current list elements. The updated elements are the intersection between
	 * the two lists that are contained into the destination list.
	 * <p>
	 * <b>NOTE 1:</b> elements of the lists must be searcheable in the list.
	 * <p>
	 * <b>NOTE 2:</b> the elements that exist in the destination list and are
	 * not contained into source list will NOT be modified. In order to modify
	 * them call again merge with themselves!
	 *
	 * @param <E>
	 *            the element type
	 * @param dest
	 *            the destination list
	 * @param src
	 *            the source list
	 * @return the list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E> List<Mergeable<E>> mergeLists(List<Mergeable<E>> dest, List<Mergeable<E>> src) {
		List<Mergeable<E>> localDest = dest;
		if (localDest == null) {
			localDest = new LinkedList<Mergeable<E>>();
		}
		if (src == null) {
			return localDest;
		}
		if (!localDest.isEmpty()) {
			for (Mergeable<E> item : src) {
				int indexOf = localDest.indexOf(item);
				if (indexOf != -1) {
					Mergeable<E> e;
					if (item instanceof GenericProxy) {
						e = dest.remove(indexOf);
						e = (Mergeable<E>) ((GenericProxy) e).clone();
						dest.add(e);
					} else {
						e = dest.get(indexOf);
					}
					e.mergeFrom((E) item);
				}
			}
		}
		return localDest;
	}

	/**
	 * Convert to mergable.
	 *
	 * @param <E>
	 *            the element type
	 * @param src
	 *            the src
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<Mergeable<E>> convertToMergable(List<?> src) {
		return (List<Mergeable<E>>) src;
	}

	/**
	 * Merge the given source template definition to the given target. The method merges the fields
	 * and the regions from the template to the given target.
	 *
	 * @param target
	 *            the target definition
	 * @param src
	 *            the source template definition
	 */
	@SuppressWarnings("unchecked")
	public static void mergeTemplate(RegionDefinitionModel target, RegionDefinitionModel src) {
		if (src != null) {
			// REVIEW: replace the factory with reflection util that create instances based on the
			// implementation class
			mergeTemplateInternal(target.getFields(), src.getFields(),
					EmfMergeableFactory.FIELD_DEFINITION, false);
			mergeTemplateInternal(target.getRegions(), src.getRegions(),
					EmfMergeableFactory.REGION_DEFINITION, false);
			if ((target instanceof Transitional) && (src instanceof Transitional)) {
				mergeLists(MergeHelper.convertToMergable(((Transitional) target).getTransitions()),
						MergeHelper.convertToMergable(((Transitional) src).getTransitions()),
						EmfMergeableFactory.TRANSITION_DEFINITION);
			}
			if ((target instanceof StateTransitionalModel)
					&& (src instanceof StateTransitionalModel)) {
				List<StateTransition> targetStateTransitions = ((StateTransitionalModel) target)
						.getStateTransitions();
				List<StateTransition> srcStateTransitions = ((StateTransitionalModel) src)
						.getStateTransitions();
				if (targetStateTransitions.isEmpty() && !srcStateTransitions.isEmpty()) {
					MergeHelper.mergeLists(MergeHelper.convertToMergable(targetStateTransitions),
							MergeHelper.convertToMergable(srcStateTransitions),
							EmfMergeableFactory.STATE_TRANSITION);
				}
			}

			if ((target instanceof AllowedChildrenModel) && (src instanceof AllowedChildrenModel)) {
				List<AllowedChildDefinition> targetAllowedChildren = ((AllowedChildrenModel) target)
						.getAllowedChildren();
				List<AllowedChildDefinition> srcAllowedChildren = ((AllowedChildrenModel) src)
						.getAllowedChildren();
				if (targetAllowedChildren.isEmpty() && !srcAllowedChildren.isEmpty()) {
					MergeHelper.mergeLists(MergeHelper.convertToMergable(targetAllowedChildren),
							MergeHelper.convertToMergable(srcAllowedChildren),
							EmfMergeableFactory.ALLOWED_CHILDREN);
				}
			}
		}
	}

	/**
	 * Merges the given lists using the given factory. The methods realizes an algorithm for merging
	 * properties/regions from a template class to target class.
	 * 
	 * @param <E>
	 *            the element type
	 * @param targetList
	 *            the target list
	 * @param sourceList
	 *            the source list
	 * @param factory
	 *            the factory to use for object creation
	 * @param clearIds
	 *            if <code>true</code> the entity ids will be cleared after the first merge from the
	 *            template
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <E> void mergeTemplateInternal(List<E> targetList, List<E> sourceList,
			MergeableInstanceFactory factory, boolean clearIds) {
		// copy objects from the template
		List<Mergeable<?>> tempList = new LinkedList<Mergeable<?>>();

		// copy all elements from the template definition to temporary list
		mergeLists(convertToMergable(tempList), convertToMergable(sourceList), factory);

		// clear ids if needed
		if (clearIds) {
			for (Mergeable<?> mergeable : tempList) {
				if (mergeable instanceof Entity) {
					((Entity) mergeable).setId(null);
				}
			}
		}

		// merge incoming elements on this level
		// first we copy/merge the elements from the parent overridden definition
		mergeLists(convertToMergable(targetList), convertToMergable(sourceList), factory);

		// merge the temporary properties from the document definition with
		// the incoming of this level and save them locally
		// if not done some of the fields like type my not be filled and
		// also the relations a not set correctly
		mergeLists(convertToMergable(tempList), convertToMergable(targetList), factory);

		// NOTE: If not called the changes from the template will have
		// higher priority which we does not want
		mergeLists(convertToMergable(targetList), convertToMergable(tempList), factory);
	}
}
