import {Component, View, Inject, NgScope, NgElement, NgDocument} from 'app/app';
import {Keys} from 'common/keys';
import {Configurable} from 'components/configurable';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {ContextualObjectsFactory, CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {TranslateService} from 'services/i18n/translate-service';

import 'components/select/object/object-type-select';
import 'search/components/search-bar/search-bar-options';
import 'instance-header/static-instance-header/static-instance-header';
import 'search/components/search-bar/search-context-menu';

import './search-bar.css!css';
import template from './search-bar.html!text';

export const INSTANCE_SELECTOR_PROPERTIES = ['id', HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB];

/**
 * Search component which wraps & configures nested components to perform a basic search:
 * 1) Object type select
 * 2) Free text search input
 * 3) Context selection
 *
 * When the component initiates search, it invokes the onSearch function provided as an event component property. It is mandatory.
 *
 * Selecting context is done by requesting an object picker through the PickerService or by selecting it from the context
 * menu drop down which is enabled by <code>config.enableCurrentObject</code>
 *
 * Because the component supports selecting search context, it also supports to be externally changed. To change it
 * simply update the reference on the <code>selectedContext</code> component property to point to another InstanceObject.
 *
 * Another context component property is the <code>idocContext</code which is provided to the PickerService when opened
 * to be able to perform contextual searches.
 *
 * To provide extended functionality, the search bar wraps & configures SearchBarOptions and renders it as a dropdown.
 *
 * Example configuration:
 * <code>
 * {
 *   disabled: false,
 *   multiple: true,
 *   predefinedTypes: [],
 *   enableCurrentObject: true,
 *   defaultToCurrentObject: false
 * }
 * </code>
 * 1) disabled - initial state of the search bar components, toggling it will update them
 * 2) multiple - used to configure the selection behaviour of the object type select, false means only single selection
 * 3) predefinedTypes - provisions the object type select with predefined object types, must be in full URI format
 * 4) enableCurrentObject - enabled the selection of CURRENT_OBJECT from the context menu
 * 5) defaultToCurrentObject - if enableCurrentObject is true and no initial context is provided, it will default
 *                             to CURRENT_OBJECT
 *
 * The component has model properties to be initially provisioned with concrete model for the nested components or any
 * wrapping component to be updated on internal change.
 * 1) objectType - if provided, it will set the value as types in the object type select
 * 2) freeText - if provided, it will set the value of the free text input
 * 3) selectedContext - if provided, it will set the currently chosen context. Note that this must be an instance object
 *                      with id and header properties.
 *
 * If a wrapping component must be notified for changes, this one will invoke the following component events:
 * 1) onFreeTextChange - fired on each model change upon freeText. Example payload <code>{freeText: 'abc'}</code>
 * 2) onContextChange - fired on each model change upon selectedContext. Example payload <code>{context: {...}}</code>
 * 3) onSearch - fired when the search icon is pressed or Enter is pressed in the free text input field
 * 4) onSavedSearchSelected - when a saved search is selected inside SearchBarOptions dropdown
 * 5) onSearchModeSelected - when a different search mode is selected inside SearchBarOptions dropdown
 *
 * Example use in a wrapping component:
 * <code>
 *  <search-bar ng-if="<expression>"
 *              ng-show="<expression>"
 *              config="::<wrapping-component>.searchBarConfig"
 *              object-type="<wrapping-component>.searchBarModel.objectType"
 *              free-text="<wrapping-component>.searchBarModel.freeText"
 *              selected-context="<wrapping-component>.searchBarModel.context"
 *              on-free-text-change="::<wrapping-component>.onFreeTextChange(freeText)"
 *              on-context-change="::<wrapping-component>.onContextChange(context)"
 *              on-search="::<wrapping-component>.onSearch(params)"
 *              on-saved-search-selected="::<wrapping-component>.loadSavedSearch(savedSearch)"
 *              on-search-mode-selected="::<wrapping-component>.changeMode(mode)"></search-bar>
 * </code>
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'search-bar',
  properties: {
    'config': 'config',
    'idocContext': 'idoc-context',
    'objectType': 'object-type',
    'freeText': 'free-text',
    'selectedContext': 'selected-context'
  },
  events: ['onFreeTextChange', 'onContextChange', 'onSearch', 'onSavedSearchSelected', 'onSearchModeSelected']
})
@View({
  template: template
})
@Inject(NgScope, NgElement, NgDocument, PickerService, TranslateService, ContextualObjectsFactory)
export class SearchBar extends Configurable {

  constructor($scope, $element, $document, pickerService, translateService, contextualObjectsFactory) {
    super({
      disabled: false,
      multiple: false,
      predefinedTypes: [],
      enableCurrentObject: true,
      defaultToCurrentObject: false
    });

    this.$scope = $scope;
    this.$element = $element;
    this.$document = $document;
    this.pickerService = pickerService;
    this.translateService = translateService;
    this.contextualObjectsFactory = contextualObjectsFactory;
  }

  ngOnInit() {
    this.showOptions = false;

    this.typesConfig = {
      multiple: this.config.multiple,
      defaultValue: this.objectType,
      width: 'auto',
      dropdownAutoWidth: true,
      isDisabled: () => this.config.disabled,
      classFilter: this.config.predefinedTypes
    };

    if (!this.config.multiple) {
      // All is available only for single selection menu
      this.typesConfig.predefinedData = [{
        id: ANY_OBJECT,
        text: this.translateService.translateInstant('search.bar.types.all')
      }];
      if (!this.objectType) {
        this.typesConfig.defaultValue = ANY_OBJECT;
      }
    }

    this.headerConfig = {
      preventLinkRedirect: true
    };

    if (this.shouldAssignCurrentObject()) {
      this.selectCurrentObject();
    }
  }

  ngAfterViewInit() {
    this.registerOptionsClickHandler();
  }

  onFTSChange() {
    this.onFreeTextChange({freeText: this.freeText});
  }

  onKeyPressed(event) {
    if (Keys.isEnter(event.keyCode)) {
      this.search();
    }
  }

  search() {
    this.onSearch({
      params: {
        objectType: this.objectType,
        freeText: this.freeText,
        context: this.selectedContext ? this.selectedContext.id : undefined
      }
    });
  }

  selectContext() {
    this.showContextMenu = false;
    this.pickerService.configureAndOpen(this.getPickerConfig(this.selectedContext), this.idocContext).then((selectedItems) => {
      this.selectedContext = selectedItems[0];
      this.notifyForContextChange(this.selectedContext);
    });
  }

  onContextMenuSelection(instance) {
    this.selectedContext = instance;
    this.showContextMenu = false;
    this.notifyForContextChange(instance);
  }

  selectCurrentObject() {
    this.selectedContext = this.contextualObjectsFactory.getCurrentObject();
    this.notifyForContextChange(this.selectedContext);
  }

  clearContext() {
    delete this.selectedContext;
    this.notifyForContextChange();
  }

  getPickerConfig(currentContext) {
    let pickerConfig = {
      extensions: {}
    };

    pickerConfig.extensions[SEARCH_EXTENSION] = {
      triggerSearch: true,
      useRootContext: true,
      results: {
        config: {
          selection: SINGLE_SELECTION,
          selectedItems: currentContext ? [currentContext] : []
        }
      },
      arguments: {
        properties: INSTANCE_SELECTOR_PROPERTIES
      }
    };

    return pickerConfig;
  }

  toggleOptions() {
    // Lazily initialize the options
    if (!this.renderOptions) {
      this.renderOptions = true;
    }
    this.showOptions = !this.showOptions;
    this.showContextMenu = false;
  }

  toggleContextMenu() {
    // Lazily initialize the context menu
    if (!this.renderContextMenu) {
      this.renderContextMenu = true;
    }
    this.showContextMenu = !this.showContextMenu;
    this.showOptions = false;
  }

  registerOptionsClickHandler() {
    let optionsButton = this.$element.find('.search-options-btn')[0];
    let optionsWrapper = this.$element.find('.search-options-wrapper')[0];
    let contextMenuButton = this.$element.find('.search-context-menu-btn')[0];

    this.documentClickHandler = (event) => {
      if (this.showOptions) {
        this.handleOptionsClick(optionsButton, optionsWrapper, event);
      }

      if (this.showContextMenu) {
        this.handleContextMenuClick(contextMenuButton, event);
      }
    };

    this.$document.on('click', this.documentClickHandler);
  }

  handleOptionsClick(optionsButton, optionsWrapper, event) {
    // Skip all clicks upon the button that opens it
    if (optionsButton.isSameNode(event.target) || optionsButton.contains(event.target)) {
      return;
    }
    // If the clicked element is not within the options component -> hide them
    if (!optionsWrapper.contains(event.target)) {
      this.showOptions = false;
      // We are outside of angular's scope here so the digest must be forced
      this.$scope.$digest();
    }
  }

  handleContextMenuClick(contextMenuButton, event) {
    // If the target is not the button that opened the context menu -> hide the menu
    if (!contextMenuButton.isSameNode(event.target) && !contextMenuButton.contains(event.target)) {
      this.showContextMenu = false;
      // We are outside of angular's scope here so the digest must be forced
      this.$scope.$digest();
    }
  }

  onSearchSelected(savedSearch) {
    this.showOptions = false;
    this.onSavedSearchSelected({savedSearch});
  }

  onModeSelected(mode) {
    this.showOptions = false;
    this.onSearchModeSelected({mode});
  }

  notifyForContextChange(context) {
    if (this.onContextChange) {
      this.onContextChange({context});
    }
  }

  ngOnDestroy() {
    this.$document.off('click', this.documentClickHandler);
  }

  isCurrentContextSelected() {
    return this.selectedContext && this.selectedContext.id === CURRENT_OBJECT;
  }

  isArbitraryContextSelected() {
    return this.selectedContext && this.selectedContext.id !== CURRENT_OBJECT;
  }

  shouldAssignCurrentObject() {
    // assign current object if set by default and is enabled and no context is actually selected
    return this.config.enableCurrentObject && this.config.defaultToCurrentObject && !this.selectedContext;
  }
}