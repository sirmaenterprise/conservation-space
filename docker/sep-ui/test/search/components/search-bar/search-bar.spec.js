import {SearchBar, INSTANCE_SELECTOR_PROPERTIES} from 'search/components/search-bar/search-bar';
import {SEARCH_EXTENSION} from 'services/picker/picker-service';
import {TranslateService} from 'services/i18n/translate-service';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {KEY_ENTER} from 'common/keys';

import {stub} from 'test/test-utils';
import {stubPickerService} from 'test/services/picker/picker-service.stub';
import {stubContextualFactory} from 'test/services/context/contextual-objects-factory.stub';

describe('SearchBar', () => {

  let searchBar;
  beforeEach(() => {
    searchBar = new SearchBar(mockScope(), {}, mockDocument(), stubPickerService(), stubTranslateService(), stubContextualFactory());
  });

  it('should hold correct default configuration', () => {
    expect(searchBar.config).to.deep.equal({
      disabled: false,
      multiple: false,
      predefinedTypes: [],
      enableCurrentObject: true,
      defaultToCurrentObject: false
    })
  });

  describe('ngOnInit', () => {
    it('should construct a configuration for the object type select menu', () => {
      searchBar.ngOnInit();
      expect(searchBar.typesConfig.multiple).to.be.false;
      expect(searchBar.typesConfig.defaultValue).to.equal(ANY_OBJECT);
      expect(searchBar.typesConfig.predefinedData.length).to.equal(1);
      expect(searchBar.typesConfig.predefinedData[0].id).to.equal(ANY_OBJECT);
    });

    it('should provide any predefined types to the object type select menu as class filters', () => {
      let predefinedTypes = ['emf:123'];
      searchBar.config.predefinedTypes = predefinedTypes;
      searchBar.ngOnInit();
      expect(searchBar.typesConfig.classFilter).to.deep.equal(predefinedTypes);
    });

    it('should construct a configuration for a multi select menu if configured as multiple', () => {
      searchBar.config.multiple = true;
      searchBar.ngOnInit();
      expect(searchBar.typesConfig.multiple).to.be.true;
    });

    it('should use the provided object type as default value for the type select', () => {
      searchBar.objectType = 'emf:Document';
      searchBar.ngOnInit();
      expect(searchBar.typesConfig.defaultValue).to.equal('emf:Document');

      searchBar.config.multiple = true;
      searchBar.ngOnInit();
      expect(searchBar.typesConfig.defaultValue).to.equal('emf:Document');
    });

    it('should create a configuration object for the instance header component', () => {
      searchBar.ngOnInit();
      expect(searchBar.headerConfig).to.exist;
      expect(searchBar.headerConfig.preventLinkRedirect).to.be.true;
    });

    it('should not show the options on initialization by default', () => {
      searchBar.ngOnInit();
      expect(searchBar.showOptions).to.be.false;
    });

    it('should not select current object by default', () => {
      searchBar.selectedContext = 'context';
      searchBar.ngOnInit();
      expect(searchBar.selectedContext).to.equal('context');
    });

    it('should not default to current object if not enabled', () => {
      searchBar.selectedContext = 'context';
      searchBar.config.enableCurrentObject = false;
      searchBar.config.defaultToCurrentObject = true;
      searchBar.ngOnInit();
      expect(searchBar.selectedContext).to.equal('context');
    });

    it('should not default to current object if enabled but context is already present', () => {
      searchBar.selectedContext = 'context';
      searchBar.config.enableCurrentObject = true;
      searchBar.config.defaultToCurrentObject = true;
      searchBar.ngOnInit();
      expect(searchBar.selectedContext).to.equal('context');
    });

    it('should default to current object if enabled', () => {
      searchBar.config.enableCurrentObject = true;
      searchBar.config.defaultToCurrentObject = true;
      searchBar.ngOnInit();
      expect(searchBar.selectedContext.id).to.equal(CURRENT_OBJECT);
    });
  });

  describe('search', () => {
    it('should invoke the components event with the selected criteria', () => {
      searchBar.objectType = 'emf:Type';
      searchBar.freeText = 'foo bar';
      searchBar.selectedContext = {id: 'emf:123'};
      searchBar.onSearch = sinon.spy();

      searchBar.search();
      expect(searchBar.onSearch.calledOnce).to.be.true;

      var payload = searchBar.onSearch.getCall(0).args[0];
      expect(payload).to.deep.equal({
        params: {
          objectType: 'emf:Type',
          freeText: 'foo bar',
          context: 'emf:123'
        }
      });
    });

    it('should acknowledge undefined context', () => {
      searchBar.onSearch = sinon.spy();
      searchBar.search();
      var payload = searchBar.onSearch.getCall(0).args[0];
      expect(payload.context).to.not.exist;
    });
  });

  describe('onKeyPressed', () => {
    it('should trigger a search if the pressed key is Enter', () => {
      searchBar.onSearch = sinon.spy();
      searchBar.onKeyPressed({keyCode: KEY_ENTER});
      expect(searchBar.onSearch.calledOnce).to.be.true;
    });
  });

  describe('selectContext', () => {
    it('should open the object picker with proper configuration', () => {
      expect(searchBar.pickerService.configureAndOpen.called).to.be.false;
      searchBar.selectContext();
      expect(searchBar.pickerService.configureAndOpen.calledOnce).to.be.true;

      var pickerConfig = searchBar.pickerService.configureAndOpen.getCall(0).args[0];
      expect(pickerConfig).to.exist;

      var searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchConfig.triggerSearch).to.be.true;
      expect(searchConfig.results.config).to.exist;
      expect(searchConfig.results.config.selection).to.equal(SINGLE_SELECTION);
      expect(searchConfig.results.config.selectedItems).to.deep.equal([]);
      expect(searchConfig.arguments.properties).to.deep.equal(INSTANCE_SELECTOR_PROPERTIES);
    });

    it('should use the current context as selected item in the picker', () => {
      var context = {id: 'emf:123'};
      searchBar.selectedContext = context;
      searchBar.selectContext();
      var pickerConfig = searchBar.pickerService.configureAndOpen.getCall(0).args[0];
      var searchConfig = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchConfig.results.config.selectedItems).to.deep.equal([context]);
    });

    it('should use the selection as context', () => {
      var selectedItems = [{id: 'emf:123'}];
      searchBar.pickerService = stubPickerService([{id: 'emf:123'}]);
      searchBar.selectContext();
      expect(searchBar.selectedContext).to.exist;
      expect(searchBar.selectedContext).to.deep.equal(selectedItems[0]);
    });

    it('should notify for clearing the context', () => {
      searchBar.onContextChange = sinon.spy();
      var selectedItems = [{id: 'emf:123'}];
      searchBar.pickerService = stubPickerService([{id: 'emf:123'}]);
      searchBar.selectContext();
      expect(searchBar.onContextChange.calledOnce).to.be.true;
      expect(searchBar.onContextChange.getCall(0).args[0].context).to.deep.equal(selectedItems[0]);
    });

    it('should hide the context menu', () => {
      searchBar.showContextMenu = true;
      searchBar.selectContext();
      expect(searchBar.showContextMenu).to.be.false;
    });
  });

  describe('onContextMenuSelection()', () => {
    it('should set the provided instance as selected context & notify', () => {
      searchBar.onContextChange = sinon.spy();
      let instance = {id: 'emf:123'};

      searchBar.onContextMenuSelection(instance);
      expect(searchBar.selectedContext).to.deep.equal(instance);
      expect(searchBar.onContextChange.calledOnce).to.be.true;
      expect(searchBar.onContextChange.getCall(0).args[0].context).to.deep.equal(instance);
    });

    it('should hide the context menu', () => {
      searchBar.showContextMenu = true;
      searchBar.onContextMenuSelection();
      expect(searchBar.showContextMenu).to.be.false;
    });
  });

  describe('clearContext', () => {
    it('should clear any selected context', () => {
      searchBar.selectedContext = {id: 'emf:123'};
      searchBar.clearContext();
      expect(searchBar.selectedContext).to.not.exist;
    });

    it('should notify for clearing the context', () => {
      searchBar.onContextChange = sinon.spy();
      searchBar.clearContext();
      expect(searchBar.onContextChange.calledOnce).to.be.true;
      expect(searchBar.onContextChange.getCall(0).args[0].context).to.be.undefined;
    });
  });

  describe('toggleOptions()', () => {
    it('should lazily render the options', () => {
      expect(searchBar.renderOptions).to.be.undefined;
      searchBar.toggleOptions();
      expect(searchBar.renderOptions).to.be.true;
    });

    it('should toggle the visibility of the search options', () => {
      searchBar.toggleOptions();
      expect(searchBar.showOptions).to.be.true;
      searchBar.toggleOptions();
      expect(searchBar.showOptions).to.be.false;
    });

    it('should hide the context menu', () => {
      searchBar.showContextMenu = true;
      searchBar.toggleOptions();
      expect(searchBar.showContextMenu).to.be.false;
    });
  });

  describe('toggleContextMenu()', () => {
    it('should lazily render the context menu', () => {
      expect(searchBar.renderContextMenu).to.be.undefined;
      searchBar.toggleContextMenu();
      expect(searchBar.renderContextMenu).to.be.true;
    });

    it('should toggle the visibility of the context menu', () => {
      searchBar.toggleContextMenu();
      expect(searchBar.showContextMenu).to.be.true;
      searchBar.toggleContextMenu();
      expect(searchBar.showContextMenu).to.be.false;
    });

    it('should hide the context menu', () => {
      searchBar.showOptions = true;
      searchBar.toggleContextMenu();
      expect(searchBar.showOptions).to.be.false;
    });
  });

  describe('registerOptionsClickHandler()', () => {
    let optionsButton;
    let optionsWrapper;
    let contextMenuButton;

    beforeEach(() => {
      let findStub = sinon.stub();
      searchBar.$element.find = findStub;

      optionsButton = stubButton();
      findStub.onFirstCall().returns([optionsButton]);

      optionsWrapper = {
        contains: sinon.spy(() => false)
      };
      findStub.onSecondCall().returns([optionsWrapper]);

      contextMenuButton = stubButton();
      findStub.onThirdCall().returns([contextMenuButton]);

      searchBar.ngAfterViewInit();
    });

    function stubButton() {
      return {
        isSameNode: sinon.spy(() => false),
        contains: sinon.spy(() => false)
      };
    }

    it('should register a click handler on the document', () => {
      expect(searchBar.$document.on.calledOnce).to.be.true;
      expect(searchBar.$document.on.calledWith('click', searchBar.documentClickHandler)).to.be.true;
    });

    it('should hide the search bar options if clicked outside of the component', () => {
      searchBar.showOptions = true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showOptions).to.be.false;
      expect(searchBar.$scope.$digest.calledOnce).to.be.true;
    });

    it('should not hide the search bar options if clicked within the component', () => {
      optionsWrapper.contains = () => true;
      searchBar.showOptions = true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showOptions).to.be.true;
    });

    it('should not hide the search bar options if clicked upon the options toggle button', () => {
      optionsButton.contains = () => true;
      searchBar.showOptions = true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showOptions).to.be.true;

      optionsButton.contains = () => false;
      optionsButton.isSameNode = () => true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showOptions).to.be.true;
    });

    it('should not check event target if the options component is already hidden', () => {
      searchBar.showOptions = false;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showOptions).to.be.false;
      expect(optionsButton.contains.called).to.be.false;
      expect(optionsButton.isSameNode.called).to.be.false;
      expect(optionsWrapper.contains.called).to.be.false;
    });

    it('should hide the context menu if clicked outside of the component', () => {
      searchBar.showContextMenu = true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showContextMenu).to.be.false;
      expect(searchBar.$scope.$digest.calledOnce).to.be.true;
    });

    it('should not hide the context menu if clicked on its toggle button', () => {
      contextMenuButton.contains = () => true;
      searchBar.showContextMenu = true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showContextMenu).to.be.true;

      contextMenuButton.contains = () => false;
      contextMenuButton.isSameNode = () => true;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showContextMenu).to.be.true;
    });

    it('should not check event targets if the context menu is already hidden', () => {
      searchBar.showContextMenu = false;
      searchBar.documentClickHandler({target: {}});
      expect(searchBar.showContextMenu).to.be.false;
      expect(contextMenuButton.contains.called).to.be.false;
      expect(contextMenuButton.isSameNode.called).to.be.false;
    });
  });

  describe('onSearchSelected(savedSearch)', () => {
    it('should hide the options and invoke onSavedSearchSelected component event with the given saved search instance', () => {
      let savedSearch = {id: 'emf:123'};
      searchBar.showOptions = true;
      searchBar.onSavedSearchSelected = sinon.spy();

      searchBar.onSearchSelected(savedSearch);
      expect(searchBar.showOptions).to.be.false;
      expect(searchBar.onSavedSearchSelected.calledOnce).to.be.true;
      expect(searchBar.onSavedSearchSelected.getCall(0).args[0]).to.deep.equal({savedSearch});
    });
  });

  describe('Current Object', () => {
    it('should return proper object containing current object', () => {
      searchBar.selectCurrentObject();
      expect(searchBar.selectedContext.id).to.equal(CURRENT_OBJECT);
    });

    it('should properly resolve shouldAssignCurrentObject() when another selected context is present', () => {
      searchBar.enableCurrentObject = true;
      searchBar.defaultToCurrentObject = true;
      searchBar.selectedContext = {
        id: 'Some context'
      };
      expect(searchBar.shouldAssignCurrentObject()).to.be.false;
    });

    it('should properly resolve isCurrentObjectSelected() when current object is selected', () => {
      searchBar.selectedContext = {
        id: CURRENT_OBJECT
      };
      expect(searchBar.isCurrentContextSelected()).to.be.true;
    });

    it('should properly resolve isCurrentObjectSelected() when current object is not selected', () => {
      searchBar.selectedContext = {
        id: 'Not The Current Object'
      };
      expect(searchBar.isCurrentContextSelected()).to.be.false;
    });

    it('should properly resolve isArbitraryContextSelected() when arbitrary object is selected', () => {
      searchBar.selectedContext = {
        id: 'emf:123-456-789'
      };
      expect(searchBar.isArbitraryContextSelected()).to.be.true;
    });

    it('should properly resolve isArbitraryContextSelected() when current object is selected', () => {
      searchBar.selectedContext = {
        id: CURRENT_OBJECT
      };
      expect(searchBar.isArbitraryContextSelected()).to.be.false;
    });

    it('should notify for selecting current object as context', () => {
      searchBar.onContextChange = sinon.spy();
      searchBar.selectCurrentObject();
      expect(searchBar.onContextChange.calledOnce).to.be.true;
      expect(searchBar.onContextChange.getCall(0).args[0].context.id).to.deep.equal(CURRENT_OBJECT);
    });
  });

  describe('onModeSelected(mode)', () => {
    it('should hide the options and invoke onSearchModeSelected component event with the given mode', () => {
      let mode = 'advanced';
      searchBar.showOptions = true;
      searchBar.onSearchModeSelected = sinon.spy();

      searchBar.onModeSelected(mode);
      expect(searchBar.showOptions).to.be.false;
      expect(searchBar.onSearchModeSelected.calledOnce).to.be.true;
      expect(searchBar.onSearchModeSelected.getCall(0).args[0]).to.deep.equal({mode});
    });
  });

  describe('ngOnDestroy()', () => {
    it('should deregister the document click handler for the search options', () => {
      searchBar.documentClickHandler = () => {
      };
      searchBar.ngOnDestroy();
      expect(searchBar.$document.off.calledOnce).to.be.true;
      expect(searchBar.$document.off.calledWith('click', searchBar.documentClickHandler)).to.be.true;
    });
  });

  function mockScope() {
    return {
      $digest: sinon.spy()
    };
  }

  function mockDocument() {
    return {
      on: sinon.spy(),
      off: sinon.spy()
    };
  }

  function stubTranslateService() {
    return stub(TranslateService);
  }

});
