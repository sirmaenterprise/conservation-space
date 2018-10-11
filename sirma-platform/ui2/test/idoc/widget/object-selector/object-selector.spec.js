import {
  ObjectSelector,
  SELECT_OBJECT_CURRENT,
  SELECT_OBJECT_MANUALLY,
  SELECT_OBJECT_AUTOMATICALLY
} from 'idoc/widget/object-selector/object-selector';
import {InstanceObject} from 'models/instance-object';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {SINGLE_SELECTION, NO_SELECTION} from 'search/search-selection-modes';
import {EVENT_SEARCH} from 'search/search-mediator';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {ORDER_ASC} from 'search/order-constants';

describe('ObjectSelector', () => {

  var objectSelector;
  beforeEach(()=> {
    objectSelector = getObjectSelectorInstance();
  });

  function getObjectSelectorInstance(scope) {
    scope = scope || mock$scope();
    var pickerService = new PickerService();
    let selector =  new ObjectSelector(scope, pickerService);
    selector.ngOnInit();
    return selector;
  }

  it('should construct different search configurations for manual and automatic search', () => {
    expect(objectSelector.pickerSearchConfig).to.exist;
    expect(objectSelector.searchConfig).to.exist;
  });

  it('should construct picker configuration for manual search', () => {
    expect(objectSelector.pickerConfig).to.exist;
    expect(objectSelector.pickerConfig.extensions).to.exist;
    expect(objectSelector.pickerConfig.extensions[SEARCH_EXTENSION]).to.equal(objectSelector.pickerSearchConfig);
  });

  it('should not use root context by default', () => {
    var config = objectSelector.pickerConfig.extensions[SEARCH_EXTENSION];
    expect(config.useRootContext).to.be.false;
  });

  it('should not include current by default', () => {
    ObjectSelector.prototype.config = {
      selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
    };
    objectSelector = getObjectSelectorInstance();
    expect(objectSelector.config.includeCurrent).to.be.false;
    expect(objectSelector.includeCurrent).to.be.false;
  });

  it('should include current if configured to do so', () => {
    ObjectSelector.prototype.config = {
      includeCurrent: true,
      selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
    };
    objectSelector = getObjectSelectorInstance();
    expect(objectSelector.includeCurrent).to.be.true;
    expect(objectSelector.config.includeCurrent).to.be.true;
  });

  it('should disallow any selection when selection mode is SELECT_OBJECT_AUTOMATICALLY', () => {
    expect(objectSelector.searchConfig.results.config.selection).to.equal(NO_SELECTION);
  });

  it('should assign SINGLE_SELECTION by default when selection mode is SELECT_OBJECT_MANUALLY', () => {
    expect(objectSelector.pickerSearchConfig.results.config.selection).to.equal(SINGLE_SELECTION);
  });

  it('should assign predefined types', () => {
    var predefinedTypes = ['emf:Document'];
    ObjectSelector.prototype.config = {
      predefinedTypes: predefinedTypes
    };
    objectSelector = getObjectSelectorInstance();
    expect(objectSelector.searchConfig.predefinedTypes).to.deep.equal(predefinedTypes);
    expect(objectSelector.pickerSearchConfig.predefinedTypes).to.deep.equal(predefinedTypes);
  });

  it('should construct search configurations with a copy of the search criteria', () => {
    expect(objectSelector.searchConfig.criteria).to.exist;
    expect(objectSelector.searchConfig.arguments).to.exist;
    expect(objectSelector.pickerSearchConfig.criteria).to.exist;
    expect(objectSelector.pickerSearchConfig.arguments).to.exist;

    expect(objectSelector.searchConfig.criteria).to.not.equal(objectSelector.config.criteria);

    expect(objectSelector.searchConfig.criteria).to.deep.equal(objectSelector.config.criteria);
    expect(objectSelector.pickerSearchConfig.criteria).to.deep.equal(objectSelector.config.criteria);
  });

  it('should call onSelectObjectModeChanged when created', () => {
    let onSelectObjectModeChangedStub = sinon.stub(ObjectSelector.prototype, 'onSelectObjectModeChanged');
    objectSelector = getObjectSelectorInstance();
    expect(onSelectObjectModeChangedStub).to.be.calledOnce;
    onSelectObjectModeChangedStub.restore();
  });

  it('should call onSelectObjectModeChanged and onObjectSelectorChanged when select object mode is changed', () => {
    let onSelectObjectModeChangedStub = sinon.stub(ObjectSelector.prototype, 'onSelectObjectModeChanged');
    let onObjectSelectorChangedStub = sinon.stub(ObjectSelector.prototype, 'onObjectSelectorChanged');
    let scope = mock$scope();
    objectSelector = getObjectSelectorInstance(scope);

    objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
    scope.$digest();

    expect(onSelectObjectModeChangedStub).to.be.calledTwice;
    expect(onObjectSelectorChangedStub).to.be.calledOnce;
    onSelectObjectModeChangedStub.restore();
    onObjectSelectorChangedStub.restore();
  });

  it('should call onObjectSelectorChanged when EVENT_SEARCH is fired', () => {
    let onObjectSelectorChangedStub = sinon.stub(ObjectSelector.prototype, 'onObjectSelectorChanged');
    objectSelector = getObjectSelectorInstance();
    objectSelector.searchConfig.callbacks[EVENT_SEARCH]({query: {tree: {}}, response: {config: {params: {orderBy: 'modifiedOn', orderDirection: 'desc'}}}});
    expect(onObjectSelectorChangedStub).to.be.calledOnce;
    onObjectSelectorChangedStub.restore();
  });

  it('should set the current configuration when EVENT_SEARCH is fired', () => {
    objectSelector.searchCriteria = undefined;
    objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
    let criteria = {condition: 'AND', rules: []};
    objectSelector.searchConfig.criteria = criteria;
    objectSelector.searchConfig.callbacks[EVENT_SEARCH]({query: {tree: criteria}, response: {config: {params: {orderBy: 'modifiedOn', orderDirection: 'desc'}}}});
    expect(objectSelector.searchCriteria).to.deep.equal(criteria);
  });

  it('should set the current search mode when EVENT_SEARCH is fired', () => {
    objectSelector.searchConfig.callbacks[EVENT_SEARCH]({query: {tree: {}}, response: {config: {params: {orderBy: 'modifiedOn', orderDirection: 'desc'}}},searchMode: 'advanced'});
    expect(objectSelector.config.searchMode).to.equal('advanced');
  });

  it('should set the current order info when EVENT_SEARCH is fired', () => {
    objectSelector.searchConfig.callbacks[EVENT_SEARCH]({query: {tree: {}}, response: {config: {params: {orderBy: 'modifiedOn', orderDirection: 'desc'}}}});
    expect(objectSelector.config.orderBy).to.equal('modifiedOn');
    expect(objectSelector.config.orderDirection).to.equal('desc');
  });

  it('should use default value for render criteria', () => {
    expect(objectSelector.searchConfig.renderCriteria).to.be.true;
  });

  it('should use config value for render criteria', () => {
    ObjectSelector.prototype.config = {renderCriteria : false};
    objectSelector = getObjectSelectorInstance();
    expect(objectSelector.searchConfig.renderCriteria).to.be.false;
  });

  describe('getCurrentConfiguration()', () => {
    it('should return the picker search configuration if the mode is set to SELECT_OBJECT_MANUALLY', () => {
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      let config = {'picker': 'config'};
      objectSelector.pickerSearchConfig = config;
      expect(objectSelector.getCurrentConfiguration()).to.deep.equal(config);
    });

    it('should return the search configuration if the mode is not set to SELECT_OBJECT_MANUALLY', () => {
      objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      let config = {'search': 'config'};
      objectSelector.searchConfig = config;
      expect(objectSelector.getCurrentConfiguration()).to.deep.equal(config);
    });
  });

  describe('onSelectObjectModeChanged', () => {
    beforeEach(() => {
      ObjectSelector.prototype.config = undefined;
      objectSelector = getObjectSelectorInstance();
      objectSelector.searchCriteria = {
        rules: ['rule1', 'rule2']
      };
      objectSelector.config.selectedItems = ['selectedItem1', 'selectedItem2'];
    });

    it('should clear criteria and selected items if select object mode is "current object"', () => {
      objectSelector.config.selectObjectMode = SELECT_OBJECT_CURRENT;
      objectSelector.onSelectObjectModeChanged();
      expect(objectSelector.searchCriteria).to.be.undefined;
      expect(objectSelector.config.selectedItems).to.be.empty;
    });

    it('should add watcher for search results selectedItems if select object mode is "manually"', () => {
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      objectSelector.onSelectObjectModeChanged();
      expect(objectSelector.manuallySelectSelectedItemsWatcher).to.not.be.undefined;
    });

    it('should not include current object if select object mode is "manually"', () => {
      objectSelector.config.includeCurrent = true;
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      objectSelector.onSelectObjectModeChanged();
      expect(objectSelector.config.includeCurrent).to.be.false;
    });

    it('should preserve include current between changes in the search modes', () => {
      objectSelector.includeCurrent = true;
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;

      objectSelector.onSelectObjectModeChanged();
      expect(objectSelector.config.includeCurrent).to.be.false;
      expect(objectSelector.includeCurrent).to.be.true;

      objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      objectSelector.onSelectObjectModeChanged();
      expect(objectSelector.config.includeCurrent).to.be.true;
      expect(objectSelector.includeCurrent).to.be.true;
    });

    it('should clear selected items if select object mode is "automatically"', () => {
      objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      objectSelector.onSelectObjectModeChanged();
      expect(objectSelector.searchCriteria).to.be.deep.equal(objectSelector.searchCriteria);
      expect(objectSelector.config.selectedItems).to.be.empty;
    });

    it('should set latest search criteria and search mode in object picker config when object mode is "manually"', () => {
      objectSelector.searchCriteria = {rules: [{id: '456'}]};
      objectSelector.config.searchMode = 'testSearchMode';

      objectSelector.pickerSearchConfig.criteria = {rules: [{id: '123'}]};
      objectSelector.pickerSearchConfig.searchMode = undefined;

      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      objectSelector.onSelectObjectModeChanged();

      expect(objectSelector.pickerSearchConfig.criteria).to.not.equal(objectSelector.searchCriteria);
      expect(objectSelector.pickerSearchConfig.criteria).to.deep.equal(objectSelector.searchCriteria);
      expect(objectSelector.pickerSearchConfig.searchMode).to.equal(objectSelector.config.searchMode);
    });

    it('should set latest search criteria and search mode in search component config when object mode is "automatically"', () => {
      objectSelector.searchCriteria = {rules: []};
      objectSelector.config.searchMode = 'testSearchMode';

      objectSelector.searchConfig.criteria = {};
      objectSelector.searchConfig.searchMode = undefined;

      objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      objectSelector.onSelectObjectModeChanged();

      expect(objectSelector.searchConfig.criteria).to.not.equal(objectSelector.searchCriteria);
      expect(objectSelector.searchConfig.criteria).to.deep.equal(objectSelector.searchCriteria);
      expect(objectSelector.searchConfig.searchMode).to.equal(objectSelector.config.searchMode);
    });

    it('should set latest picked objects when mode is "manually"', () => {
      var selected = ['selectedItem5','selectedItem6','selectedItem9'];
      objectSelector.pickerSearchConfig.results.config.selectedItems = selected;
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      objectSelector.$scope.$digest();

      expect(objectSelector.config.selectedItems).to.deep.equal(selected);
    });

    it('should set selectedItems empty if user deselects all selected objects when mode is "manually"', () => {
      objectSelector.pickerSearchConfig.results.config.selectedItems = [];
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      objectSelector.$scope.$digest();

      expect(objectSelector.config.selectedItems).to.be.empty;
    });
  });

  describe('onObjectSelectorChanged', () => {
    it('should call config onObjectSelectorChanged if defined', () => {
      objectSelector.config.onObjectSelectorChanged = sinon.stub();
      objectSelector.onObjectSelectorChanged();
      expect(objectSelector.config.onObjectSelectorChanged).to.be.calledOnce;
    });

    it('should provide data from the current search configuration', () => {
      var selectorChangedSpy = sinon.spy();
      var data = [{'my': 'data'}];
      objectSelector.pickerSearchConfig.results.data = data;
      objectSelector.config.onObjectSelectorChanged = selectorChangedSpy;
      objectSelector.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      objectSelector.config.searchMode = 'seach-mode';

      objectSelector.onObjectSelectorChanged();
      expect(selectorChangedSpy.calledOnce).to.be.true;
      expect(selectorChangedSpy.getCall(0).args[0].searchResults).to.deep.equal(data);
      expect(selectorChangedSpy.getCall(0).args[0].searchMode).to.deep.equal('seach-mode');
    });
  });

  it('initCurrentObject should set current object', (done) => {
    let models = {
      validationModel: {},
      viewModel: {
        fields: []
      }
    };
    let currentObject = new InstanceObject(null, models, {
      headers: {
        'default_header': 'default_header',
        'compact_header': 'compact_header',
        'breadcrumb_header': 'breadcrumb_header'
      }
    });
    objectSelector.context = {
      getCurrentObject: () => {
        return Promise.resolve(currentObject);
      }
    };
    expect(objectSelector.currentObject).to.be.undefined;
    objectSelector.initCurrentObject().then(() => {
      expect(objectSelector.currentObject).to.equal(currentObject);
      done();
    }).catch(done);
  });

  it('showOption should return boolean depending on excludeOptions configuration', () => {
    objectSelector.config.excludeOptions = ['current'];
    expect(objectSelector.showOption('current')).to.be.false;
    expect(objectSelector.showOption('manually')).to.be.true;
  });

  describe('isAutomatically()', function() {
    it('should return true if configured for automatic selection', function() {
      objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
      expect(objectSelector.isAutomatically()).to.be.true;
    });

    it('should return false if not configured for automatic selection', function() {
      objectSelector.config.selectObjectMode = 'test';
      expect(objectSelector.isAutomatically()).to.be.false;
    });
  });

  it('getSearchConfiguration should return search arguments', () => {
    objectSelector.config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
    let config = {
      orderBy: 'emf:type',
      orderDirection: ORDER_ASC,
      orderByCodelistNumbers: '7,210'
    };
    let searchConfiguration = objectSelector.getSearchConfiguration(config);
    expect(searchConfiguration.arguments).to.eql(config);
  });
});