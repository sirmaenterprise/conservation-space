package com.sirma.itt.cmf.services;

import java.util.List;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.domain.VerificationMessage;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Service for creating and managing case instances.
 * 
 * @author BBonev
 */
public interface CaseService extends InstanceService<CaseInstance, CaseDefinition> {

	/**
	 * Verify the given case instance against specific case definition.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @param caseDefinition
	 *            the case definition
	 * @return the list of found errors or empty list
	 */
	List<VerificationMessage> verifyCaseInstance(CaseInstance caseInstance,
			CaseDefinition caseDefinition);

	/**
	 * Loads all case instances identified by their DMS IDs. The method first checks in the cache
	 * for each case if not found fetch it from DB and update cache. This method is optimal for
	 * loading many cases that are not in the cache.
	 * 
	 * @param dmsIds
	 *            is the ID to look for
	 * @param loadAllProperties
	 *            the load all properties for the found instances. If <code>false</code> then only
	 *            case properties will be loaded and not for the documents
	 * @return the found and populated instance or empty list if nothing is found
	 */
	List<CaseInstance> batchLoadCaseInstance(List<String> dmsIds, boolean loadAllProperties);

	/**
	 * Close case instance.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @param operation
	 *            the close operation
	 */
	void closeCaseInstance(CaseInstance caseInstance, Operation operation);

	/**
	 * Delete case instance. The deleted case instance will not longer appear in searches and cannot
	 * be seen. In general CMF module will not delete the case instance from his database if not
	 * configured. The caller have the option to actually delete the case instance from the DMS
	 * subsystem.
	 * 
	 * @param caseInstance
	 *            the case instance to delete
	 * @param operation
	 *            the detele operation
	 * @param permanent
	 *            if <code>true</code> the case instance will be physically deleted from DMS sub
	 *            system, if <code>false</code> will only be marked as deleted.
	 */
	@Override
	void delete(CaseInstance caseInstance, Operation operation, boolean permanent);

	/**
	 * Gets the primary case for document.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the primary case for document
	 */
	CaseInstance getPrimaryCaseForDocument(String dmsId);

	/**
	 * <b>NOTE:</b> Do not call this method to attach a document to a case but only for saving
	 * direct changes to the case. If need to attach document to a case use the construct
	 * 
	 * <pre>
	 * <code>
	 * 	&#64;Inject
	 * 	&#64;Proxy
	 * 	private InstanceService<Instance, DefinitionModel> instanceService;
	 * 	...
	 * 
	 * 	// this will save the document and update it's content to DMS
	 * 	instanceService.save(documentInstance, operation);
	 * 	// this will attach it to the section
	 * 	instanceService.attach(documentInstance.getOwningInstance(), operation,
	 * 			documentInstance);
	 * 
	 * 	RuntimeConfiguration.setConfiguration(
	 * 			RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
	 * 
	 * 	CaseInstance caseInstance = InstanceUtil
	 * 			.getParent(CaseInstance.class, documentInstance);
	 * 	try {
	 * 		// update the case without saving children to mark it as modified
	 * 		instanceService.save(caseInstance, operation);
	 * 	} finally {
	 * 		RuntimeConfiguration
	 * 				.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
	 * 	}</code>
	 * </pre>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	CaseInstance save(CaseInstance instance, Operation operation);
}
