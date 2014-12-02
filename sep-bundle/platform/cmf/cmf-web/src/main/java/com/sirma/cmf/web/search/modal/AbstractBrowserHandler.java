package com.sirma.cmf.web.search.modal;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * The Interface EntityBrowserHandler.
 * 
 * @param <E>
 *            Target element type
 * @param <T>
 *            Selected generic type
 * @param <A>
 *            the generic type
 */
public abstract class AbstractBrowserHandler<E extends Entity, T extends Entity, A extends Entity>
		extends EntityAction {

	/**
	 * Initialize entity browser.
	 * 
	 * @param a
	 *            the a
	 */
	public abstract void initialize(A a);

	/**
	 * When iterated result list, this method is invoked to check if current iteration element is
	 * same as selected in order to apply some css class for styling.
	 * 
	 * @param e
	 *            the e
	 * @return true, if is current
	 */
	public abstract boolean isCurrent(E e);

	/**
	 * Toggle selection target.
	 * 
	 * @param e
	 *            selected target
	 */
	public abstract void toggleTarget(E e);

	/**
	 * Accept selected target and perform operation.
	 * 
	 * @return Navigation string.
	 */
	public abstract String acceptSelectedTarget();

	/**
	 * Cancel selection and clear selected fields.
	 * 
	 * @return navigation string
	 */
	public abstract String cancelSelection();

	/**
	 * After search is invoked by the entity browser api after searching in order to allow results
	 * list to be modified.
	 * 
	 * @param <S>
	 *            search arguments type
	 * @param searchData
	 *            the search data
	 */
	public abstract <S extends SearchArguments<Instance>> void afterSearch(S searchData);

}
