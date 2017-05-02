import {PickerService} from 'services/picker/picker-service';
import {SEARCH_EXTENSION, BASKET_EXTENSION, RECENT_EXTENSION, CREATE_EXTENSION} from 'services/picker/picker-service';
import {PICKER_EXTENSION_POINT, PICKER_DEFAULT_HEADER, PICKER_DEFAULT_HELP_TARGET} from 'services/picker/picker-service';
import {SearchCriteriaUtils} from "search/utils/search-criteria-utils";
import {NO_SELECTION, SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';

import {PromiseStub} from 'test/promise-stub';

describe('PickerService', () => {

  var pickerService;
  beforeEach(() => {
    var extensionsDialogService = {
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
    pickerService = new PickerService(extensionsDialogService);
  });

  describe('open()', () => {
    var pickerConfig = {};
    var context = {};
    var dialogConfig = {};

    it('should open the picker by delegating to the extensions dialog service', () => {
      pickerService.open(pickerConfig, context, dialogConfig);
      var openDialogSpy = pickerService.extensionsDialogService.openDialog;
      expect(openDialogSpy.calledOnce).to.be.true;
      expect(openDialogSpy.getCall(0).args[0]).to.equal(pickerConfig);
      expect(openDialogSpy.getCall(0).args[1]).to.equal(context);
      expect(openDialogSpy.getCall(0).args[2]).to.equal(dialogConfig);
    });

    it('should return a promise with the selected items when resolved', () => {
      pickerService.open(pickerConfig, context, dialogConfig).then((selectedItems) => {
        expect(selectedItems).to.exist;
        expect(selectedItems).to.deep.equal([{id: 'emf:123456'}]);
      });
    });
  });

  describe('configureAndOpen()', () => {
    it('should open by delegating to open()', () => {
      var pickerConfig = {};
      pickerService.configureAndOpen(pickerConfig).then((selectedItems) => {
        var openDialogSpy = pickerService.extensionsDialogService.openDialog;
        expect(openDialogSpy.calledOnce).to.be.true;
        expect(openDialogSpy.getCall(0).args[0]).to.equal(pickerConfig);
        expect(selectedItems).to.exist;
        expect(selectedItems).to.deep.equal([{id: 'emf:123456'}]);
      });
    });

    it('should assign default extensions configuration', () => {
      var pickerConfig = {};
      pickerService.configureAndOpen(pickerConfig);

      expect(pickerConfig.extensionPoint).to.equal(PICKER_EXTENSION_POINT);
      expect(pickerConfig.helpTarget).to.equal(PICKER_DEFAULT_HELP_TARGET);
      expect(pickerConfig.header).to.equal(PICKER_DEFAULT_HEADER);

      expect(pickerConfig.extensions).to.exist;
      expect(pickerConfig.panelCls).to.exist;
      expect(pickerConfig.tabs).to.exist;
      expect(pickerConfig.extensions[SEARCH_EXTENSION]).to.exist;
    });

    it('should not override extensions configuration with default one', () => {
      var pickerConfig = {
        helpTarget: 'some-help',
        header: 'some.header',
        extensions: {}
      };
      pickerService.configureAndOpen(pickerConfig);

      expect(pickerConfig.helpTarget).to.not.equal(PICKER_DEFAULT_HELP_TARGET);
      expect(pickerConfig.header).to.not.equal(PICKER_DEFAULT_HEADER);
    });
  });

  describe('assignDefaultSearchConfigurations()', () => {
    it('should assign default configuration for the search extension point', () => {
      var pickerConfig = {};
      pickerService.configureAndOpen(pickerConfig);

      var searchExtensionConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchExtensionConfig.criteriaType).to.equal(SearchCriteriaUtils.MIXED_MODE);
      expect(searchExtensionConfig.criteria).to.deep.equal({});
      expect(searchExtensionConfig.useRootContext).to.be.true;
      expect(searchExtensionConfig.triggerSearch).to.be.true;
      expect(searchExtensionConfig.results).to.exists;
      expect(searchExtensionConfig.results.config).to.exists;
      expect(searchExtensionConfig.results.config.selection).to.equal(SINGLE_SELECTION);
      expect(searchExtensionConfig.results.config.selectedItems).to.deep.equal([]);
      expect(searchExtensionConfig.results.config.exclusions).to.deep.equal([]);
      expect(searchExtensionConfig.results.config.selectionHandler).to.exist;
    });

    it('should not override externally provided search configurations', () => {
      var pickerConfig = {
        extensions: {}
      };
      pickerConfig.extensions[SEARCH_EXTENSION] = {
        triggerSearch: false,
        results: {
          config: {
            selection: MULTIPLE_SELECTION,
            selectedItems: [{id: 'emf:123456'}]
          }
        }
      };
      pickerService.configureAndOpen(pickerConfig);

      var searchExtensionConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchExtensionConfig.triggerSearch).to.be.false;
      expect(searchExtensionConfig.results.config.selection).to.equal(MULTIPLE_SELECTION);
      expect(searchExtensionConfig.results.config.selectedItems).to.deep.equal([{id: 'emf:123456'}]);
    });
  });

  describe('assignDefaultSelectionHandler()', () => {
    it('should assign function to delegate to handleSelection()', () => {
      var pickerConfig = {};
      pickerService.handleSelection = sinon.spy();
      pickerService.assignDefaultConfigurations(pickerConfig);
      var searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      var handler = searchConfig.results.config.selectionHandler;
      var selectedItems = searchConfig.results.config.selectedItems;

      expect(pickerService.handleSelection.called).to.be.false;
      handler({id: '1'});
      expect(pickerService.handleSelection.calledOnce).to.be.true;
      expect(pickerService.handleSelection.getCall(0).args[0]).to.equal(selectedItems);
      expect(pickerService.handleSelection.getCall(0).args[1]).to.be.true;
      expect(pickerService.handleSelection.getCall(0).args[2]).to.deep.equal({id: '1'});
    });
  });

  describe('assignDefaultBasketConfigurations()', () => {
    it('should assign configurations for the instance list component', () => {
      var pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[BASKET_EXTENSION]).to.exist;

      var searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      var basketConfig = pickerConfig.extensions[BASKET_EXTENSION];
      expect(basketConfig.selectableItems).to.be.true;
      expect(basketConfig.singleSelection).to.be.true;
      expect(basketConfig.selectableItems).to.be.true;
      // Should keep same references
      expect(basketConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
      expect(basketConfig.selectionHandler).to.equal(searchConfig.results.config.selectionHandler);
    });

    it('should create additional tab configuration for the basket extension tab', () => {
      var pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);

      expect(pickerConfig.tabs).to.exist;
      expect(pickerConfig.tabs[BASKET_EXTENSION]).to.exist;

      var postfix = pickerConfig.tabs[BASKET_EXTENSION].postfix;
      expect(postfix).to.exist;
      expect(postfix()).equal('<span class="badge">0</span>');

      pickerService.setSelectedItems(pickerConfig, ['9']);
      expect(postfix()).equal('<span class="badge">1</span>');
    });

    it('should favor the search configurations instead of the default', () => {
      var pickerConfig = {extensions: {}};
      pickerConfig.extensions[SEARCH_EXTENSION] = {results: {config: {selection: NO_SELECTION}}};
      pickerService.assignDefaultConfigurations(pickerConfig);

      var basketConfig = pickerConfig.extensions[BASKET_EXTENSION];
      expect(basketConfig.selectableItems).to.be.false;
    });
  });

  describe('assignDefaultRecentObjectsConfigurations()', () => {
    it('should assign configurations for the instance list component', () => {
      var pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[RECENT_EXTENSION]).to.exist;

      var searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      var recentConfig = pickerConfig.extensions[RECENT_EXTENSION];
      expect(recentConfig.selectableItems).to.be.true;
      expect(recentConfig.singleSelection).to.be.true;
      expect(recentConfig.selectableItems).to.be.true;
      // Should keep same references
      expect(recentConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
      expect(recentConfig.selectionHandler).to.equal(searchConfig.results.config.selectionHandler);
      expect(recentConfig.exclusions).to.equal(searchConfig.results.config.exclusions);
    });
  });

  describe('assignDefaultCreateConfigurations()', () => {
    it('should assign configurations for the create component', () => {
      var pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[CREATE_EXTENSION]).to.exist;

      var searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      var recentConfig = pickerConfig.extensions[CREATE_EXTENSION];

      // Should keep same references
      expect(recentConfig.useRootContext).to.equal(searchConfig.useRootContext);
      expect(recentConfig.predefinedTypes).to.equal(searchConfig.predefinedTypes);
      expect(recentConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
      expect(recentConfig.selectionHandler).to.equal(searchConfig.results.config.selectionHandler);
    });
  });

  describe('handleSelection(selectedItems, instance)', () => {
    it('should properly handle single selection', () => {
      var selectedItems = [];

      pickerService.handleSelection(selectedItems, true, {id: '1'});
      expect(selectedItems.length).to.equal(1);
      expect(selectedItems[0].id).to.equal('1');

      pickerService.handleSelection(selectedItems, true, {id: '2'});
      expect(selectedItems.length).to.equal(1);
      expect(selectedItems[0].id).to.equal('2');
    });

    it('should properly deselect multiple items', () => {
      var selectedItems = [{id: '1'}, {id: '2'}, {id: '3'}];

      pickerService.handleSelection(selectedItems, false, {id: '2'});
      expect(selectedItems.length).to.equal(2);

      pickerService.handleSelection(selectedItems, false, {id: '1'});
      expect(selectedItems.length).to.equal(1);

      pickerService.handleSelection(selectedItems, false, {id: '3'});
      expect(selectedItems.length).to.equal(0);
    });

    it('should properly select multiple items', () => {
      var selectedItems = [];

      pickerService.handleSelection(selectedItems, false, {id: '2'});
      pickerService.handleSelection(selectedItems, false, {id: '3'});

      expect(selectedItems.length).to.equal(2);
      expect(selectedItems[0].id).to.equal('2');
      expect(selectedItems[1].id).to.equal('3');
    });
  });

  describe('clearSelectedItems(pickerConfig)', () => {
    it('should clear the selected items in the provided picker configuration', () => {
      var selectedItems = ['1', '2'];
      var pickerConfig = {
        extensions: {}
      };
      pickerConfig.extensions[SEARCH_EXTENSION] = {results: {config: {selectedItems}}};
      pickerService.assignDefaultConfigurations(pickerConfig);
      pickerService.clearSelectedItems(pickerConfig);
      // Should keep the reference
      expect(selectedItems.length).to.equal(0);
    });
  });

  describe('getSelectedItems()', () => {
    it('should retrieve the selected items array from the provided picker configuration', () => {
      var selectedItems = ['1', '2'];
      var pickerConfig = {
        extensions: {}
      };
      pickerConfig.extensions[SEARCH_EXTENSION] = {results: {config: {selectedItems}}};
      pickerService.assignDefaultConfigurations(pickerConfig);
      // Should keep the reference
      expect(pickerService.getSelectedItems(pickerConfig)).to.equal(selectedItems);
      expect(pickerService.getSelectedItems(pickerConfig.extensions)).to.equal(selectedItems);
    });
  });

  describe('setSelectedItems()', () => {
    it('should replace the selected items in the picker configuration with the provided selection', () => {
      var selectedItems = ['1', '2'];
      var pickerConfig = {
        extensions: {}
      };
      pickerConfig.extensions[SEARCH_EXTENSION] = {results: {config: {selectedItems}}};
      pickerService.assignDefaultConfigurations(pickerConfig);
      pickerService.setSelectedItems(pickerConfig, ['3', '4']);
      expect(selectedItems.length).to.equal(2);
      expect(selectedItems).to.deep.equal(['3', '4']);
    });
  });

});