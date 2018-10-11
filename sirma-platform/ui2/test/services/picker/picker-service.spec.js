import {
  PickerService,
  PICKER_DIALOG_CLASS,
  PICKER_PANEL_CLASS,
  SEARCH_EXTENSION,
  BASKET_EXTENSION,
  RECENT_EXTENSION,
  CREATE_EXTENSION,
  UPLOAD_EXTENSION,
  PICKER_EXTENSION_POINT,
  PICKER_DEFAULT_HEADER,
  PICKER_DEFAULT_HELP_TARGET
} from 'services/picker/picker-service';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {PickerRestrictionsService} from 'services/picker/picker-restrictions-service';
import {NO_SELECTION, SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('PickerService', () => {

  let pickerService;
  beforeEach(() => {
    let extensionDialogServiceStub = stub(ExtensionsDialogService);
    let pickerResult = {};
    pickerResult[SEARCH_EXTENSION] = {
      results: {
        config: {
          selectedItems: [{id: 'emf:123456'}]
        }
      }
    };
    extensionDialogServiceStub.openDialog.returns(PromiseStub.resolve(pickerResult));
    pickerService = new PickerService(extensionDialogServiceStub, stub(PickerRestrictionsService));
  });

  describe('open()', () => {
    let pickerConfig = {};
    let context = {};
    let dialogConfig = {};

    it('should open the picker by delegating to the extensions dialog service', () => {
      pickerService.open(pickerConfig, context, dialogConfig);
      let openDialogSpy = pickerService.extensionsDialogService.openDialog;
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
      let pickerConfig = {};
      pickerService.configureAndOpen(pickerConfig).then((selectedItems) => {
        let openDialogSpy = pickerService.extensionsDialogService.openDialog;
        expect(openDialogSpy.calledOnce).to.be.true;
        expect(openDialogSpy.getCall(0).args[0]).to.equal(pickerConfig);
        expect(selectedItems).to.exist;
        expect(selectedItems).to.deep.equal([{id: 'emf:123456'}]);
      });
    });

    it('should assign default extensions configuration', () => {
      let pickerConfig = {};
      pickerService.configureAndOpen(pickerConfig);

      expect(pickerConfig.extensionPoint).to.equal(PICKER_EXTENSION_POINT);
      expect(pickerConfig.helpTarget).to.equal(PICKER_DEFAULT_HELP_TARGET);
      expect(pickerConfig.header).to.equal(PICKER_DEFAULT_HEADER);

      expect(pickerConfig.extensions).to.exist;
      expect(pickerConfig.modalCls).to.equal(PICKER_DIALOG_CLASS);
      expect(pickerConfig.panelCls).to.equal(PICKER_PANEL_CLASS);
      expect(pickerConfig.tabs).to.exist;
      expect(pickerConfig.extensions[SEARCH_EXTENSION]).to.exist;
    });

    it('should not override extensions configuration with default one', () => {
      let pickerConfig = {
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
      let pickerConfig = {};
      pickerService.configureAndOpen(pickerConfig);

      let searchExtensionConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchExtensionConfig.criteria).to.deep.equal({});
      expect(searchExtensionConfig.useRootContext).to.be.true;
      expect(searchExtensionConfig.triggerSearch).to.be.true;
      expect(searchExtensionConfig.predefinedTypes).to.deep.equal([]);
      expect(searchExtensionConfig.results).to.exists;
      expect(searchExtensionConfig.results.config).to.exists;
      expect(searchExtensionConfig.results.config.linkRedirectDialog).to.be.true;
      expect(searchExtensionConfig.results.config.selection).to.equal(SINGLE_SELECTION);
      expect(searchExtensionConfig.results.config.selectedItems).to.deep.equal([]);
      expect(searchExtensionConfig.results.config.exclusions).to.deep.equal([]);
      expect(searchExtensionConfig.results.config.selectionHandler).to.exist;
    });

    it('should not override externally provided search configurations', () => {
      let pickerConfig = {
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

      let searchExtensionConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchExtensionConfig.triggerSearch).to.be.false;
      expect(searchExtensionConfig.results.config.selection).to.equal(MULTIPLE_SELECTION);
      expect(searchExtensionConfig.results.config.selectedItems).to.deep.equal([{id: 'emf:123456'}]);
    });

    it('should delegate to assign restrictions', () => {
      let restrictions = SearchCriteriaUtils.getDefaultRule();
      let pickerConfig = {
        extensions: {}
      };
      pickerConfig.extensions[SEARCH_EXTENSION] = {criteria: {}, restrictions};
      pickerService.configureAndOpen(pickerConfig);
      expect(pickerService.pickerRestrictionsService.assignRestrictionCriteria.calledWith({}, restrictions)).to.be.true;
    });
  });

  describe('assignDefaultSelectionHandler()', () => {
    it('should assign function to delegate to handleSelection()', () => {
      let pickerConfig = {};
      pickerService.handleSelection = sinon.spy();
      pickerService.assignDefaultConfigurations(pickerConfig);
      let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      let handler = searchConfig.results.config.selectionHandler;
      let selectedItems = searchConfig.results.config.selectedItems;

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
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[BASKET_EXTENSION]).to.exist;

      let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      let basketConfig = pickerConfig.extensions[BASKET_EXTENSION];
      expect(basketConfig.selectableItems).to.be.true;
      expect(basketConfig.singleSelection).to.be.true;
      expect(basketConfig.selectableItems).to.be.true;
      expect(basketConfig.linkRedirectDialog).to.be.true;
      // Should keep same references
      expect(basketConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
      expect(basketConfig.selectionHandler).to.equal(searchConfig.results.config.selectionHandler);
    });

    it('should create additional tab configuration for the basket extension tab', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);

      expect(pickerConfig.tabs).to.exist;
      expect(pickerConfig.tabs[BASKET_EXTENSION]).to.exist;

      let postfix = pickerConfig.tabs[BASKET_EXTENSION].postfix;
      expect(postfix).to.exist;
      expect(postfix()).equal('<span class="badge">0</span>');

      pickerService.setSelectedItems(pickerConfig, ['9']);
      expect(postfix()).equal('<span class="badge">1</span>');
    });

    it('should update basket configuration if tabs contains configurations of basket extension', () => {
      let customBasketLabel = 'custom-label';
      let pickerConfig = {
        tabs: []
      };

      pickerConfig.tabs[BASKET_EXTENSION] = {
        label: customBasketLabel
      };

      pickerService.assignDefaultConfigurations(pickerConfig);

      let basketConfiguration = pickerConfig.tabs[BASKET_EXTENSION];
      //verifying that passed configuration is not lost
      expect(basketConfiguration.label).to.equal(customBasketLabel);

      //verifying that configuration is updated with 'postfix'
      let postfix = basketConfiguration.postfix;
      expect(postfix).to.exist;
      expect(postfix()).equal('<span class="badge">0</span>');
    });

    it('should favor the search configurations instead of the default', () => {
      let pickerConfig = {extensions: {}};
      pickerConfig.extensions[SEARCH_EXTENSION] = {results: {config: {selection: NO_SELECTION}}};
      pickerService.assignDefaultConfigurations(pickerConfig);

      let basketConfig = pickerConfig.extensions[BASKET_EXTENSION];
      expect(basketConfig.selectableItems).to.be.false;
    });
  });

  describe('assignDefaultDialogConfigurations()', () => {
    it('should assign configurations for the dialog configuration', () => {
      let dialogConfig = {};
      pickerService.assignDefaultDialogConfigurations(dialogConfig);

      expect(dialogConfig).to.deep.eq({
        buttons: [{
          label: 'dialog.button.select',
        }, {
          label: 'dialog.button.cancel',
        }]
      });
    });
  });

  describe('assignDefaultRecentObjectsConfigurations()', () => {
    it('should assign configurations for the instance list component', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[RECENT_EXTENSION]).to.exist;

      let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      let recentConfig = pickerConfig.extensions[RECENT_EXTENSION];
      expect(recentConfig.selectableItems).to.be.true;
      expect(recentConfig.singleSelection).to.be.true;
      expect(recentConfig.linkRedirectDialog).to.be.true;

      // Should keep same references
      expect(recentConfig.predefinedTypes).to.equal(searchConfig.predefinedTypes);
      expect(recentConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
      expect(recentConfig.selectionHandler).to.equal(searchConfig.results.config.selectionHandler);
      expect(recentConfig.exclusions).to.equal(searchConfig.results.config.exclusions);
    });

    it('should assign configurations related to the restrictions filter', () => {
      let restrictions = {};
      let pickerConfig = {extensions: {}};
      pickerConfig.extensions[SEARCH_EXTENSION] = {restrictions};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[RECENT_EXTENSION]).to.exist;

      let recentConfig = pickerConfig.extensions[RECENT_EXTENSION];
      pickerService.pickerRestrictionsService.filterByRestrictions = sinon.spy();

      recentConfig.restrictionFilter([1, 2, 3]);
      expect(recentConfig.restrictionFilter).to.exist;
      expect(pickerService.pickerRestrictionsService.filterByRestrictions.calledOnce).to.be.true;
      expect(pickerService.pickerRestrictionsService.filterByRestrictions.calledWith([1, 2, 3], {})).to.be.true;
    });
  });

  describe('assignDefaultCreateConfigurations()', () => {
    it('should assign configurations for the create component', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[CREATE_EXTENSION]).to.exist;

      let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      let createConfig = pickerConfig.extensions[CREATE_EXTENSION];

      expect(createConfig.useContext).to.equal(searchConfig.useContext);
      expect(createConfig.predefinedTypes).to.equal(searchConfig.predefinedTypes);
      expect(createConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
    });

    it('should assign & call the default selection handler for create tab when no restrictions are present', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[CREATE_EXTENSION]).to.exist;

      let createConfig = pickerConfig.extensions[CREATE_EXTENSION];

      pickerService.handleSelection = sinon.spy();
      pickerService.pickerRestrictionsService.handleSelection = sinon.spy();

      createConfig.selectionHandler({id: 1});
      expect(pickerService.handleSelection.calledOnce).to.be.true;
      expect(pickerService.pickerRestrictionsService.handleSelection.calledOnce).to.be.false;
    });

    it('should assign & call the custom selection handler for create tab when restrictions are present', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[CREATE_EXTENSION]).to.exist;

      let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      let createConfig = pickerConfig.extensions[CREATE_EXTENSION];

      searchConfig.restrictions = {};
      pickerService.handleSelection = sinon.spy();
      pickerService.pickerRestrictionsService.handleSelection = sinon.spy();

      createConfig.selectionHandler({id: 1});
      expect(pickerService.handleSelection.calledOnce).to.be.false;
      expect(pickerService.pickerRestrictionsService.handleSelection.calledOnce).to.be.true;
      expect(pickerService.pickerRestrictionsService.handleSelection.calledWith({id: 1}, {},
             searchConfig.results.config.selectionHandler)).to.be.true;
    });
  });

  describe('assignDefaultUploadConfigurations()', () => {
    it('should assign configurations for the upload component', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerConfig.extensions[UPLOAD_EXTENSION]).to.exist;

      let searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      let recentConfig = pickerConfig.extensions[CREATE_EXTENSION];

      // Should keep same references
      expect(recentConfig.useContext).to.equal(searchConfig.useContext);
      expect(recentConfig.predefinedTypes).to.equal(searchConfig.predefinedTypes);
      expect(recentConfig.selectedItems).to.equal(searchConfig.results.config.selectedItems);
    });
  });

  describe('assignDefaultPickerDialogConfigurations()', () => {
    it('should assign default dialog warning message for picker when restrictions are present', () => {
      let restrictions = {};
      let pickerConfig = {extensions: {}};
      pickerConfig.extensions[SEARCH_EXTENSION] = {restrictions};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerService.pickerRestrictionsService.assignRestrictionMessage.calledWith(pickerConfig, restrictions)).to.be.true;
    });

    it('should assign default dialog warning message for picker when restrictions are not present', () => {
      let pickerConfig = {};
      pickerService.assignDefaultConfigurations(pickerConfig);
      expect(pickerService.pickerRestrictionsService.assignRestrictionMessage.calledOnce).to.be.false;
    });
  });

  describe('handleSelection(selectedItems, instance)', () => {
    it('should properly handle single selection', () => {
      let selectedItems = [];

      pickerService.handleSelection(selectedItems, true, {id: '1'});
      expect(selectedItems.length).to.equal(1);
      expect(selectedItems[0].id).to.equal('1');

      pickerService.handleSelection(selectedItems, true, {id: '2'});
      expect(selectedItems.length).to.equal(1);
      expect(selectedItems[0].id).to.equal('2');
    });

    it('should properly deselect multiple items', () => {
      let selectedItems = [{id: '1'}, {id: '2'}, {id: '3'}];

      pickerService.handleSelection(selectedItems, false, {id: '2'});
      expect(selectedItems.length).to.equal(2);

      pickerService.handleSelection(selectedItems, false, {id: '1'});
      expect(selectedItems.length).to.equal(1);

      pickerService.handleSelection(selectedItems, false, {id: '3'});
      expect(selectedItems.length).to.equal(0);
    });

    it('should properly select multiple items', () => {
      let selectedItems = [];

      pickerService.handleSelection(selectedItems, false, {id: '2'});
      pickerService.handleSelection(selectedItems, false, {id: '3'});

      expect(selectedItems.length).to.equal(2);
      expect(selectedItems[0].id).to.equal('2');
      expect(selectedItems[1].id).to.equal('3');
    });
  });

  describe('clearSelectedItems(pickerConfig)', () => {
    it('should clear the selected items in the provided picker configuration', () => {
      let selectedItems = ['1', '2'];
      let pickerConfig = {
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
      let selectedItems = ['1', '2'];
      let pickerConfig = {
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
      let selectedItems = ['1', '2'];
      let pickerConfig = {
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