import {ContextSelector} from 'components/contextselector/context-selector';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {PromiseStub} from 'test/promise-stub';

const ENTITY_ID = 'currentObjectId';
const ENTITY_DEFINITION_ID = 'definitionId';
const ENTITY_HEADER = 'Instance breadcrumb header';

describe('ContextSelector', ()=> {

  var pickerService;
  var contextSelector;
  beforeEach(() => {
    pickerService = new PickerService(mockExtensionDialogService());
    ContextSelector.prototype.config = getContextSelectorConfig();
    contextSelector = new ContextSelector(pickerService, mockTranslateService(), mockInstanceRestService(), mockContextFactoryService());
  });

  it('should assign instance parent as a default selection', () => {
    expect(contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems).to.deep.equal([{id: ENTITY_ID}]);
  });

  it('should load default context when open', () => {
    expect(contextSelector.breadcrumbHeader).to.equal(ENTITY_HEADER);
    expect(contextSelector.config.parentId).to.equal(ENTITY_ID);
  });

  it('should call onContextSelected when selecting a new context', () => {
    contextSelector.config.onContextSelected = sinon.spy();
    contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems = [{id: 'aa873a4d-ccb2-4878-8a68-6be03deb2e7d'}];
    contextSelector.selectContext();
    expect(contextSelector.config.onContextSelected.calledOnce).to.be.true;
  });

  it('should call onContextSelected when clearing the context', () => {
    contextSelector.config.onContextSelected = sinon.spy();
    contextSelector.clearContext();
    expect(contextSelector.config.onContextSelected.calledOnce).to.be.true;
  });

  it('should clear context if parent is null or undefined', () => {
    contextSelector.config.parentId = 'emf:123';
    contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems = ['emf:123'];
    contextSelector.getContext(null);
    expect(contextSelector.breadcrumbHeader).to.equal(ContextSelector.NO_CONTEXT);
    expect(contextSelector.config.parentId).to.equal(null);
    expect(contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems).to.deep.equal([]);
  });

  it('should clear context if parent instance is deleted', () => {
    var pickerService = new PickerService(mockExtensionDialogService());
    let instanceRestService = {
      load: () => {
        return PromiseStub.reject();
      }
    };
    contextSelector = new ContextSelector(pickerService, mockTranslateService(), instanceRestService);
    expect(contextSelector.breadcrumbHeader).to.equal(ContextSelector.NO_CONTEXT);
    expect(contextSelector.config.parentId).to.equal(null);
  });

  it('should clear selected context', () => {
    contextSelector.clearContext();
    expect(contextSelector.config.parentId).to.equal(null);
    expect(contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems).to.deep.equal([]);
    expect(contextSelector.breadcrumbHeader).to.equal(ContextSelector.NO_CONTEXT);
  });

  it('should change selected context', () => {
    // The pickerConfig should be updated by the dialog but we use mocks here so it cannot be updated
    contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems = [{id: 'aa873a4d-ccb2-4878-8a68-6be03deb2e7d'}];
    contextSelector.selectContext();
    expect(contextSelector.breadcrumbHeader).to.equal(ENTITY_HEADER);
    expect(contextSelector.config.parentId).to.equal('aa873a4d-ccb2-4878-8a68-6be03deb2e7d');
  });

  it('should configure a help target & header', () => {
    expect(contextSelector.pickerConfig.header).to.exist;
    expect(contextSelector.pickerConfig.helpTarget).to.exist;
  });

  it('should configure the picker to trigger a search', () => {
    expect(contextSelector.pickerConfig.extensions[SEARCH_EXTENSION].triggerSearch).to.be.true;
  });
});

function mockTranslateService() {
  return {
    translateInstant: () => {
      return ContextSelector.NO_CONTEXT;
    }
  }
}

function mockExtensionDialogService() {
  let pickerPromiseResult = {};
  pickerPromiseResult[SEARCH_EXTENSION] = {
    results: {
      config: {
        selectedItems: [{'id': 'aa873a4d-ccb2-4878-8a68-6be03deb2e7d', 'default_header': 'Object #0'}]
      }
    }
  };
  return {
    openDialog: () => {
      return PromiseStub.resolve(pickerPromiseResult);
    }
  }
}

function mockInstanceRestService() {
  return {
    load: () => {
      return PromiseStub.resolve({
        data: {
          definitionId: ENTITY_DEFINITION_ID,
          headers: {breadcrumb_header: ENTITY_HEADER}
        }
      });
    }
  };
}

function mockContextFactoryService() {
  return {
    getCurrentContext: () => {
      return undefined;
    }
  };
}

function getContextSelectorConfig() {
  return {
    parentId: ENTITY_ID
  };
}
