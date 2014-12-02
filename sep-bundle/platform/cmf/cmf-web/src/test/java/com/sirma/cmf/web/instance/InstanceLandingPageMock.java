package com.sirma.cmf.web.instance;

import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.instance.landingpage.InstanceLandingPage;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * InstanceLandingPageMock.
 * 
 * @author svelikov
 */
public class InstanceLandingPageMock extends InstanceLandingPage<CaseInstance, DefinitionModel> {

	@Override
	protected Class<DefinitionModel> getInstanceDefinitionClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CaseInstance getNewInstance(DefinitionModel selectedDefinition, Instance context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<CaseInstance> getInstanceClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected InstanceReference getParentReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String saveInstance(CaseInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String cancelEditInstance(CaseInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onExistingInstanceInitPage(CaseInstance instance) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onNewInstanceInitPage(CaseInstance instance) {
		// TODO Auto-generated method stub

	}

	@Override
	protected FormViewMode getFormViewModeExternal(CaseInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNavigationString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getDefinitionFilterType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected InstanceService<CaseInstance, DefinitionModel> getInstanceService() {
		// TODO Auto-generated method stub
		return null;
	}

}
