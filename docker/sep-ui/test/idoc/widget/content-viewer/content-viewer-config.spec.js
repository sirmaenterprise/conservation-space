import {ContentViewerConfig} from 'idoc/widget/content-viewer/content-viewer-config';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PromiseAdapterMock} from '../../../adapters/angular/promise-adapter-mock';
import {SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';

describe('ContentViewerConfig', () => {

  let widgetConfig;
  let objectSelectorHelper;

  beforeEach(() => {
    let instanceService = {
      loadBatch: () => {
        return Promise.resolve({data: []});
      }
    };
    objectSelectorHelper = new ObjectSelectorHelper(PromiseAdapterMock.mockAdapter(), undefined, instanceService);
    ContentViewerConfig.prototype.config = undefined;
    widgetConfig = new ContentViewerConfig(objectSelectorHelper, translateService());
  });

  it('should select object manually by default', () => {
    expect(widgetConfig.config.selectObjectMode).to.equal(SELECT_OBJECT_MANUALLY);
  });

  it('should trigger a search if there is existing criteria', () => {
    ContentViewerConfig.prototype.config = {
      criteria: {constructor: {'test': 123}}
    };
    widgetConfig = new ContentViewerConfig(objectSelectorHelper, translateService());
    expect(widgetConfig.objectSelectorConfig.triggerSearch).to.be.true;
    ContentViewerConfig.prototype.config = undefined;
  });

  it('should not trigger a search if there is no existing criteria', () => {
    ContentViewerConfig.prototype.config = {
      criteria: {}
    };
    widgetConfig = new ContentViewerConfig(objectSelectorHelper, translateService());
    expect(widgetConfig.objectSelectorConfig.triggerSearch).to.be.false;
  });

  it('should not trigger a search if there empty existing criteria', () => {
    expect(widgetConfig.objectSelectorConfig.triggerSearch).to.be.false;
  });

  it('should assign previously selected item if any', () => {
    ContentViewerConfig.prototype.config = {
      selectedObject: 'prev-selection'
    };
    ContentViewerConfig.prototype.context = {
      getSharedObjects: () => {
        return Promise.resolve([]);
      }
    };
    widgetConfig = new ContentViewerConfig(objectSelectorHelper, translateService());
    let expected = [{
      id: 'prev-selection', properties: {}, headers: {}
    }];
    expect(widgetConfig.objectSelectorConfig.selectedItems).to.deep.equal(expected);
    ContentViewerConfig.prototype.config = undefined;
  });

  it('should not assign previously selected item if none', () => {
    expect(widgetConfig.objectSelectorConfig.selectedItems).to.deep.equal([]);
  });

  it('should not assign selected object if no results', () => {
    widgetConfig.config.selectedObject = undefined;
    widgetConfig.setSelectedObject([]);
    expect(widgetConfig.config.selectedObject).to.not.exist;
  });

  it('should not assign selected object if more than one result', () => {
    widgetConfig.config.selectedObject = undefined;
    widgetConfig.setSelectedObject([{id: '1'}, {id: '2'}]);
    expect(widgetConfig.config.selectedObject).to.not.exist;
  });

  it('should assign selected object if just one result', () => {
    widgetConfig.config.selectedObject = [];
    widgetConfig.setSelectedObject([{id: '1'}]);
    expect(widgetConfig.config.selectedObject).to.equal('1');
  });

  it('should update object selection mode', () => {
    widgetConfig.config.selectObjectMode = '';
    let onSelectorChangedPayload = getOnSelectorChangedPayload('select-mode');
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.selectObjectMode).to.equal('select-mode');
  });

  it('should update search criteria', () => {
    widgetConfig.config.criteria = {};
    let criteria = {condition: 'OR', rules: []};
    let onSelectorChangedPayload = getOnSelectorChangedPayload('select-mode', criteria);
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.criteria).to.deep.equal(criteria);
  });

  it('should clone updated criteria', () => {
    widgetConfig.config.criteria = {};
    let criteria = {condition: 'OR', rules: []};
    let onSelectorChangedPayload = getOnSelectorChangedPayload('select-mode', criteria);
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.criteria).to.not.equal(criteria);
  });

  it('should remove previously selected item', () => {
    widgetConfig.config.selectedObject = 'ni';
    let onSelectorChangedPayload = getOnSelectorChangedPayload('select-mode');
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.selectedObject).to.not.exist;
  });

  it('should retrieve selected item when selecting manually', () => {
    widgetConfig.config.selectedObject = 'ni';
    let onSelectorChangedPayload = getOnSelectorChangedPayload(SELECT_OBJECT_MANUALLY, {}, [{id: 'icky'}]);
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.selectedObject).to.equal('icky');
  });

  it('should retrieve search item when selecting automatically', () => {
    widgetConfig.config.selectedObject = 'ni';
    let onSelectorChangedPayload = getOnSelectorChangedPayload(SELECT_OBJECT_AUTOMATICALLY, {}, [], [{id: 'Ptang'}]);
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.selectedObject).to.equal('Ptang');
  });

  it('should retrieve the search mode', () => {
    widgetConfig.config.searchMode = 'basic';
    let onSelectorChangedPayload = getOnSelectorChangedPayload('select-mode', {}, {}, {}, 'advanced');
    widgetConfig.onObjectSelectorChanged(onSelectorChangedPayload);
    expect(widgetConfig.config.searchMode).to.equal('advanced');
  });

});

function getOnSelectorChangedPayload(selectObjectMode, searchCriteria, selectedItems, searchResults, searchMode) {
  return {
    selectObjectMode,
    searchCriteria,
    selectedItems,
    searchResults,
    searchMode
  };
}

function translateService() {
  return {
    translateInstant: (key) => {
      return key;
    }
  };
}
