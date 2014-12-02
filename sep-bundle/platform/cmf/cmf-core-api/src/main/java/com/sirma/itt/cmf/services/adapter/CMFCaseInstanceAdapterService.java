package com.sirma.itt.cmf.services.adapter;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;

/**
 * The Interface CMFCaseInstanceAdapterService is the base adapter service for.
 * {@link com.sirma.itt.cmf.services.CaseService}
 */
public interface CMFCaseInstanceAdapterService extends CMFAdapterService {

	/**
	 * Creates the case instance.
	 *
	 * @param caseInstance
	 *            the case instance
	 * @return the string
	 * @throws DMSException
	 *             on dms error
	 */
	public String createCaseInstance(CaseInstance caseInstance) throws DMSException;

	/**
	 * Deletes the case instance.
	 *
	 * @param caseInstance
	 *            the case instance to delete
	 * @return the dms id on success, null on error
	 * @throws DMSException
	 *             on dms error
	 */
	public String deleteCaseInstance(CaseInstance caseInstance) throws DMSException;

	/**
	 * Deletes the case instance.
	 *
	 * @param caseInstance
	 *            the case instance to delete
	 * @param force
	 *            whether to really delete the case from dms
	 * @return the dms id on success, null on error
	 * @throws DMSException
	 *             on dms error
	 */
	public String deleteCaseInstance(CaseInstance caseInstance, boolean force) throws DMSException;

	/**
	 * Closes the case instance.
	 *
	 * @param caseInstance
	 *            the case instance to close
	 * @return the dms id on success
	 * @throws DMSException
	 *             on dms error
	 */
	public String closeCaseInstance(CaseInstance caseInstance) throws DMSException;

	/**
	 * Updates the case instance.
	 *
	 * @param caseInstance
	 *            the case instance to update
	 * @return the dms id on success, null on error
	 * @throws DMSException
	 *             on dms error
	 */
	public String updateCaseInstance(CaseInstance caseInstance) throws DMSException;


}
