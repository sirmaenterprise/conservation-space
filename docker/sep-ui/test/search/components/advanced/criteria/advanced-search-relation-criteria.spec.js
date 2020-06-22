import {AdvancedSearchRelationCriteria} from 'search/components/advanced/criteria/advanced-search-relation-criteria';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {MULTIPLE_SELECTION, NO_SELECTION} from 'search/search-selection-modes';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';

import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {stubContextualFactory} from 'test/services/context/contextual-objects-factory.stub';

function mockPromiseAdapter(resolveSpy, allSpy) {
  return {
    resolve: resolveSpy,
    all: allSpy
  };
}

function mockInstanceRestService(loadSpy) {
  return {
    load: function (id) {
      loadSpy(id);
      return id;
    }
  };
}

function mockExtensionDialogService() {
  return {
    openDialog: sinon.spy(() => {
      var pickerResult = {};
      pickerResult[SEARCH_EXTENSION] = {
        results: {
          config: {
            selectedItems: [{id: 'emf:123456'}]
          }
        }
      };
      return PromiseStub.resolve(pickerResult);
    })
  };
}

function mockDialogService() {
  return {
    createButton: sinon.spy()
  };
}

function getComponentInstance() {
  var scope = mock$scope();
  var dialogService = mockDialogService();
  var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
  var extensionDialogService = mockExtensionDialogService();
  var pickerService = new PickerService(extensionDialogService);
  let criteria = new AdvancedSearchRelationCriteria(scope, dialogService, undefined, promiseAdapter, pickerService, stubContextualFactory());
  criteria.property = {
    range: ['User', 'Group']
  };
  criteria.ngOnInit();
  return criteria;
}

describe('AdvancedSearchRelationCriteria', () => {
  var criteria;

  describe('createSelectConfig()', function () {
    it('should always include any object', function () {
      criteria = getComponentInstance();
      expect(criteria.selectConfig.predefinedItems[0].id).to.equal(ANY_OBJECT);
      expect(criteria.selectConfig.types).to.deep.equal(['User', 'Group']);
    });

    it('should include current object if there is a context', function () {
      AdvancedSearchRelationCriteria.prototype.context = {};
      criteria = getComponentInstance();
      expect(criteria.selectConfig.predefinedItems[0].id).to.equal(ANY_OBJECT);
      expect(criteria.selectConfig.predefinedItems[1].id).to.equal(CURRENT_OBJECT);
      expect(criteria.selectConfig.types).to.deep.equal(['User', 'Group']);
    });
  });

  describe('createPickerConfig(items)', function () {
    var relationCriteria;
    beforeEach(() => {
      relationCriteria = getComponentInstance();
      relationCriteria.property = {
        range: ['Case', 'Project', 'Task']
      };
    });

    it('should set initial criteria if not manual selection', function () {
      relationCriteria.isManualSelection = () => false;
      relationCriteria.criteria = {value: 'test'};

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.extensions[SEARCH_EXTENSION].criteria).to.equal('test');
    });

    it('should set empty initial criteria if manual selection', function () {
      relationCriteria.isManualSelection = () => true;

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.extensions[SEARCH_EXTENSION].criteria).to.deep.equal({});
    });

    it('should include only the search extension if for automatic selection', () => {
      relationCriteria.isManualSelection = () => false;
      relationCriteria.criteria = {value: 'test'};

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.inclusions).to.deep.equal([SEARCH_EXTENSION]);
    });

    it('should increment level', function () {
      relationCriteria.isManualSelection = () => true;

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.extensions[SEARCH_EXTENSION].level).to.equal(2);
    });

    it('should set multiple selection mode if manual', function () {
      relationCriteria.isManualSelection = () => true;

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.extensions[SEARCH_EXTENSION].results.config.selection).to.equal(MULTIPLE_SELECTION);
    });

    it('should set no selection mode if not manual', function () {
      relationCriteria.criteria = {
        value: {}
      };
      relationCriteria.isManualSelection = () => false;

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.extensions[SEARCH_EXTENSION].results.config.selection).to.equal(NO_SELECTION);
    });

    it('should set search mode if not manual', function () {
      relationCriteria.criteria = {
        value: {
          searchMode: 'search-mode'
        }
      };
      relationCriteria.isManualSelection = () => false;

      var pickerConfig = relationCriteria.createPickerConfig();
      expect(pickerConfig.extensions[SEARCH_EXTENSION].searchMode).to.equal('search-mode');
    });

    it('should restore previous object picker criteria for manual selection', () => {
      relationCriteria.isManualSelection = () => true;
      relationCriteria.previousManualSelection = {
        criteria: {id: 'criteria-id'},
        searchMode: 'search-mode'
      };

      var config = relationCriteria.createPickerConfig();
      expect(config.extensions[SEARCH_EXTENSION].criteria).to.deep.equal({id: 'criteria-id'});
      expect(config.extensions[SEARCH_EXTENSION].searchMode).to.equal('search-mode');
    });

    it('should provide predefined types if present to search configuration', () => {
      relationCriteria.isManualSelection = () => true;

      var config = relationCriteria.createPickerConfig();
      expect(config.extensions[SEARCH_EXTENSION].predefinedTypes).to.deep.equal(['Case', 'Project', 'Task']);
    });

    it('should provide useRootContext property to search configuration', () => {
      relationCriteria.isManualSelection = () => true;
      relationCriteria.config.useRootContext = true;

      var config = relationCriteria.createPickerConfig();
      expect(config.extensions[SEARCH_EXTENSION].useRootContext).to.be.true;
    });
  });

  describe('loadSelectedItems()', function () {
    var resolveSpy;
    var loadSpy;
    var allSpy;
    var relationCriteria;

    beforeEach(() => {
      resolveSpy = sinon.spy();
      loadSpy = sinon.spy();
      allSpy = sinon.spy();
      var instanceRestMock = mockInstanceRestService(loadSpy);
      var promiseMock = mockPromiseAdapter(resolveSpy, allSpy);
      relationCriteria = new AdvancedSearchRelationCriteria(mock$scope(), null, instanceRestMock, promiseMock, null, stubContextualFactory());
    });

    it('should return empty array if criteria is falsy', function () {
      relationCriteria.loadSelectedItems();

      expect(resolveSpy.calledOnce).to.be.true;
      expect(resolveSpy.getCall(0).args[0]).to.deep.equal([]);
    });

    it('should return empty array if criteria value is falsy', function () {
      relationCriteria.loadSelectedItems();

      expect(resolveSpy.calledOnce).to.be.true;
      expect(resolveSpy.getCall(0).args[0]).to.deep.equal([]);
    });

    it('should return empty array if not manual selection', function () {
      relationCriteria.isManualSelection = () => false;

      relationCriteria.loadSelectedItems();

      expect(resolveSpy.calledOnce).to.be.true;
      expect(resolveSpy.getCall(0).args[0]).to.deep.equal([]);
    });

    it('should load instance for each selected id', function () {
      relationCriteria.isManualSelection = () => true;
      relationCriteria.criteria = {value: [1, 2]};

      relationCriteria.loadSelectedItems();

      expect(loadSpy.calledTwice).to.be.true;
      expect(loadSpy.getCall(0).args[0]).to.equal(1);
      expect(loadSpy.getCall(1).args[0]).to.equal(2);

      expect(allSpy.calledOnce).to.be.true;
      expect(allSpy.getCall(0).args[0]).to.deep.equal([1, 2]);
    });

    it('should skip contextual items or wildcards from loading instance data', () => {
      relationCriteria.isManualSelection = () => true;
      relationCriteria.criteria = {value: [ANY_OBJECT, CURRENT_OBJECT]};
      relationCriteria.loadSelectedItems();
      expect(loadSpy.called).to.be.false;
    });
  });

  describe('getSelectionMode()', function () {
    var relationCriteria;
    beforeEach(() => {
      relationCriteria = getComponentInstance();
      relationCriteria.criteria = {};
    });

    it('should return multiple selection if manual', function () {
      relationCriteria.isManualSelection = () => true;
      expect(relationCriteria.getSelectionMode()).to.equal(MULTIPLE_SELECTION);
    });

    it('should return no selection if not manual', function () {
      relationCriteria.isManualSelection = () => false;
      expect(relationCriteria.getSelectionMode()).to.equal(NO_SELECTION);
    });

    it('should return empty if the operator is "Is empty"', () => {
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EMPTY.id;
      expect(relationCriteria.getSelectionMode()).to.equal(AdvancedSearchCriteriaOperators.EMPTY.id);
    });
  });

  describe('onOkButtonClicked()', function () {
    var relationCriteria;
    beforeEach(() => {
      relationCriteria = getComponentInstance();
      relationCriteria.searchConfig = {
        searchMediator: {
          queryBuilder: {
            tree: {
              id: 'test criteria'
            }
          }
        }
      };
    });

    it('should set search tree as criteria value if not manual', function () {
      relationCriteria.criteria = {};
      relationCriteria.searchConfig.searchMediator.searchMode = 'search-mode';
      relationCriteria.isManualSelection = () => false;
      relationCriteria.onOkButtonClicked();

      expect(relationCriteria.criteria.value).to.deep.equal({
        id: 'test criteria',
        searchMode: 'search-mode'
      });
    });

    it('should set selected items as criteria value if manual', function () {
      relationCriteria.criteria = {
        value: []
      };
      relationCriteria.searchConfig.results = {
        config: {
          selectedItems: [{id: 1}]
        }
      };
      relationCriteria.isManualSelection = () => true;

      relationCriteria.onOkButtonClicked();
      expect(relationCriteria.criteria.value).to.deep.equal([1]);
    });

    it('should not set criteria value if selected items are falsy', function () {
      relationCriteria.criteria = {
        value: []
      };
      relationCriteria.isManualSelection = () => true;
      relationCriteria.searchConfig.results = {
        config: {
          selectedItems: null
        }
      };
      relationCriteria.onOkButtonClicked();

      expect(relationCriteria.criteria.value.length).to.equal(0);
    });

    it('should preserve any items in the value array', () => {
      relationCriteria.criteria = {
        value: [ANY_OBJECT, '1']
      };
      relationCriteria.isManualSelection = () => true;
      relationCriteria.searchConfig.results = {
        config: {
          selectedItems: [{id: '1'}, {id: '2'}]
        }
      };
      relationCriteria.onOkButtonClicked();
      expect(relationCriteria.criteria.value).to.deep.equal([ANY_OBJECT, '1', '2']);
    });

    it('should reinitialize the criteria value if null', () => {
      relationCriteria.criteria = {};
      relationCriteria.isManualSelection = () => true;
      relationCriteria.searchConfig.results = {
        config: {
          selectedItems: [{id: '1'}, {id: '2'}]
        }
      };
      relationCriteria.onOkButtonClicked();
      expect(relationCriteria.criteria.value).to.deep.equal(['1', '2']);
    });

    it('should preserve the picker criteria and search mode for manual selection', () => {
      relationCriteria.criteria = {};
      relationCriteria.isManualSelection = () => true;
      relationCriteria.searchConfig.searchMediator.searchMode = 'search-mode';
      relationCriteria.searchConfig.results = {
        config: {
          selectedItems: [{id: '1'}, {id: '2'}]
        }
      };
      relationCriteria.onOkButtonClicked();
      expect(relationCriteria.previousManualSelection).to.exist;
      expect(relationCriteria.previousManualSelection.searchMode).to.equal('search-mode');
      expect(relationCriteria.previousManualSelection.criteria).to.deep.equal({id: 'test criteria'});
    });
  });

  describe('isManualSelection()', function () {
    var relationCriteria;
    beforeEach(() => relationCriteria = getComponentInstance());

    it('should return true if operator is set_to or not_set_to', function () {
      relationCriteria.criteria = {operator: AdvancedSearchCriteriaOperators.SET_TO.id};
      expect(relationCriteria.isManualSelection()).to.be.true;

      relationCriteria.criteria = {operator: AdvancedSearchCriteriaOperators.NOT_SET_TO.id};
      expect(relationCriteria.isManualSelection()).to.be.true;
    });

    it('should return false if operator is set_to_query or not_set_to_query', function () {
      relationCriteria.criteria = {operator: AdvancedSearchCriteriaOperators.SET_TO_QUERY.id};
      expect(relationCriteria.isManualSelection()).to.be.false;

      relationCriteria.criteria = {operator: AdvancedSearchCriteriaOperators.NOT_SET_TO_QUERY.id};
      expect(relationCriteria.isManualSelection()).to.be.fasle;
    });
  });

  describe('registerOperatorWatcher()', () => {
    it('should register a watcher', () => {
      var scope = mock$scope();
      scope.$watch = sinon.spy();
      var relationCriteria = new AdvancedSearchRelationCriteria(scope, null, null, null, null, stubContextualFactory());
      relationCriteria.registerOperatorWatcher();
      expect(scope.$watch.calledOnce).to.be.true;
    });
  });

  describe('createDialogConfig()', () => {
    it('should create a default dialog configuration', () => {
      let relationCriteria = getComponentInstance();
      let config = relationCriteria.createDialogConfig();

      expect(config.onButtonClick).to.exist;
      expect(config.buttons).to.deep.equal([{
        label: 'dialog.button.save',
      }]);
    });
  });

  describe('resetModel()', () => {
    var relationCriteria;
    beforeEach(() => {
      relationCriteria = getComponentInstance();
      relationCriteria.criteria = {};
    });

    it('should initialize the default model value for manual selection if it is undefined', () => {
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO.id;
      relationCriteria.resetModel();
      expect(relationCriteria.criteria.value).to.deep.equal([]);
    });

    it('should initialize the default model value for automatic selection if it is undefined', () => {
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO_QUERY.id;
      relationCriteria.resetModel();
      expect(relationCriteria.criteria.value).to.deep.equal({});
    });

    it('should reset the model value if the new selection mode is for manual selection', () => {
      relationCriteria.criteria.value = {rules: []};
      relationCriteria.oldSelectionMode = NO_SELECTION;
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO.id;
      relationCriteria.resetModel();
      expect(relationCriteria.criteria.value).to.deep.equal([]);
    });

    it('should reset the model value if the new selection mode is for automatic selection', () => {
      relationCriteria.criteria.value = [{id: 'emf:123456'}];
      relationCriteria.oldSelectionMode = MULTIPLE_SELECTION;
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO_QUERY.id;
      relationCriteria.resetModel();
      expect(relationCriteria.criteria.value).to.deep.equal({});
    });

    it('should NOT reset the model value if the old selection mode is still undefined', () => {
      relationCriteria.criteria.value = [{id: 'emf:123456'}];
      relationCriteria.oldSelectionMode = undefined;
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO.id;
      relationCriteria.resetModel();
      expect(relationCriteria.criteria.value).to.deep.equal([{id: 'emf:123456'}]);
    });

    it('should NOT reset the model value if the new selection mode is the same as the old one', () => {
      relationCriteria.criteria.value = [{id: 'emf:123456'}];
      relationCriteria.oldSelectionMode = MULTIPLE_SELECTION;
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO.id;
      relationCriteria.resetModel();
      expect(relationCriteria.criteria.value).to.deep.equal([{id: 'emf:123456'}]);
    });

    it('should update the old selection mode if it is changed', () => {
      relationCriteria.oldSelectionMode = MULTIPLE_SELECTION;
      relationCriteria.criteria.operator = AdvancedSearchCriteriaOperators.SET_TO_QUERY.id;
      relationCriteria.resetModel();
      expect(relationCriteria.oldSelectionMode).to.equal(NO_SELECTION);
    });
  });

  describe('openPicker()', () => {
    var relationCriteria;
    var extensionsDialogService;
    beforeEach(() => {
      relationCriteria = getComponentInstance();
      relationCriteria.criteria = {};
      relationCriteria.isManualSelection = () => true;
      extensionsDialogService = relationCriteria.pickerService.extensionsDialogService;
    });

    it('should open an extension dialog for picker', () => {
      relationCriteria.openPicker();
      expect(extensionsDialogService.openDialog.calledOnce).to.be.true;

      var extensionDialogConfig = extensionsDialogService.openDialog.getCall(0).args[0];
      expect(extensionDialogConfig).to.exist;
      expect(extensionDialogConfig.extensionPoint).to.equal('picker');
    });

    it('should configure on button click handler for the picker', () => {
      relationCriteria.openPicker();
      var dialogConfig = extensionsDialogService.openDialog.getCall(0).args[2];
      expect(dialogConfig.onButtonClick).to.exist;
    });
  });
});