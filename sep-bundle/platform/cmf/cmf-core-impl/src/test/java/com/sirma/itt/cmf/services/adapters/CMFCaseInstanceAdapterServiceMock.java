package com.sirma.itt.cmf.services.adapters;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService;
import com.sirma.itt.emf.adapter.DMSException;

/**
 * The Class CMFCaseInstanceAdapterServiceMock.
 */
@ApplicationScoped
public class CMFCaseInstanceAdapterServiceMock implements CMFCaseInstanceAdapterService {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String createCaseInstance(CaseInstance caseInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		// some dms id
		String dmsId = UUID.randomUUID().toString();
		List<SectionInstance> sections = caseInstance.getSections();
		for (SectionInstance sectionInstance : sections) {
			sectionInstance.setDmsId(dmsId);
		}
		return dmsId;
	}

	@Override
	public String deleteCaseInstance(CaseInstance caseInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return caseInstance.getDmsId();
	}

	@Override
	public String deleteCaseInstance(CaseInstance caseInstance, boolean force) throws DMSException {
		return deleteCaseInstance(caseInstance);
	}

	@Override
	public String closeCaseInstance(CaseInstance caseInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return caseInstance.getDmsId();
	}

	@Override
	public String updateCaseInstance(CaseInstance caseInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return caseInstance.getDmsId();
	}

}
