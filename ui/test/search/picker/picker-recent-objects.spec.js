import {PickerRecentObjects} from 'search/picker/picker-recent-objects';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('PickerRecentObjects', () => {

  var pickerRecentObjects;
  beforeEach(() => {
    PickerRecentObjects.prototype.config = undefined;
    pickerRecentObjects = getComponentInstance();
  });
  afterEach(() => {
    PickerRecentObjects.prototype.config = undefined;
  });

  function getComponentInstance(scope, namespaceService) {
    scope = scope || mock$scope();
    namespaceService = namespaceService || mockNamespaceService(false);
    return new PickerRecentObjects(scope, namespaceService);
  }

  it('should have default configuration & values for selection', () => {
    expect(pickerRecentObjects.config).to.exist;
    expect(pickerRecentObjects.config.selectableItems).to.be.true;
    expect(pickerRecentObjects.config.singleSelection).to.be.true;
    expect(pickerRecentObjects.config.emptyListMessage).to.exist;
  });

  it('should configure the selected items array', () => {
    var selectedItems = ['emf:123'];
    PickerRecentObjects.prototype.config = {
      selectedItems: selectedItems
    };
    pickerRecentObjects = getComponentInstance();
    // Should keep the reference!
    expect(pickerRecentObjects.selectedItems).to.equal(selectedItems);
  });

  it('should configure the internally used instance list component', () => {
    PickerRecentObjects.prototype.config = {
      singleSelection: false,
      emptyListMessage: 'message',
      selectionHandler: () => {
        return 'handler';
      }
    };
    pickerRecentObjects = getComponentInstance();

    expect(pickerRecentObjects.recentObjectsListConfig).to.exist;
    expect(pickerRecentObjects.recentObjectsListConfig.selectableItems).to.be.true;
    expect(pickerRecentObjects.recentObjectsListConfig.singleSelection).to.be.false;
    expect(pickerRecentObjects.recentObjectsListConfig.emptyListMessage).to.equal('message');

    expect(pickerRecentObjects.recentObjectsListConfig.selectionHandler).to.exist;
    expect(pickerRecentObjects.recentObjectsListConfig.selectionHandler()).to.equal('handler')
  });

  it('should register criteria watcher', () => {
    var scope = mock$scope();
    scope.$watch = sinon.spy();
    pickerRecentObjects = getComponentInstance(scope);
    expect(scope.$watch.calledOnce).to.be.true;
  });

  it('should not assign types filter by default', () => {
    expect(pickerRecentObjects.typesFilter).to.deep.equal([]);
  });

  it('should assign types filter if the criteria has a semantic object type', () => {
    var scope = mock$scope();
    pickerRecentObjects = getComponentInstance(scope, mockNamespaceService(true));
    pickerRecentObjects.config.criteria = SearchCriteriaUtils.getDefaultObjectTypeRule(['http://emf']);
    scope.$digest();
    expect(pickerRecentObjects.typesFilter).to.deep.equal(['http://emf']);
  });

  function mockNamespaceService(isUri) {
    return {
      isUri: () => {
        return isUri;
      }
    };
  }
});