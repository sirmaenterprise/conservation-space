import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {ContextualObjectsFactory, CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {AdvancedSearchCriteriaOperators} from './advanced-search-criteria-operators';
import {MULTIPLE_SELECTION, NO_SELECTION} from 'search/search-selection-modes';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {DialogService} from 'components/dialog/dialog-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import 'components/select/object/object-select';

import template from './advanced-search-relation-criteria.html!text';
import './advanced-search-relation-criteria.css!css';

const EMPTY_MODE = AdvancedSearchCriteriaOperators.EMPTY.id;

/**
 * Component for choosing relation values in the advanced search criteria form.
 *
 * The component supports two types of relation selection:
 *  1) manual - provides functionality for selecting specific objects through the object picker
 *  2) automatic - or choosing dynamic sub queries entered in the object picker
 *
 * @author nvelkov
 */
@Component({
  selector: 'seip-advanced-search-relation-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria',
    'context': 'context',
    'property': 'property'
  }
})
@View({
  template: template
})
@Inject(NgScope, DialogService, InstanceRestService, PromiseAdapter, PickerService, ContextualObjectsFactory)
export class AdvancedSearchRelationCriteria extends Configurable {

  constructor($scope, dialogService, instanceRestService, promiseAdapter, pickerService, contextualObjectsFactory) {
    super({});
    this.$scope = $scope;
    this.dialogService = dialogService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
    this.pickerService = pickerService;
    this.contextualObjectsFactory = contextualObjectsFactory;
  }

  ngOnInit() {
    this.createSelectConfig();
    this.registerOperatorWatcher();
  }

  createSelectConfig() {
    this.selectConfig = {
      predefinedItems: [this.contextualObjectsFactory.getAnyObject()],
      types: this.property.range
    };

    if (this.context) {
      this.selectConfig.predefinedItems.push(this.contextualObjectsFactory.getCurrentObject());
    }
  }

  /**
   * Watches the criteria operator and invokes the model resetting to make sure the correct model is assigned
   * for the new operator.
   */
  registerOperatorWatcher() {
    this.$scope.$watch(()=> {
      return this.criteria.operator;
    }, () => {
      this.resetModel();
    });
  }

  openPicker() {
    this.loadSelectedItems().then((promises) => {
      var items = promises.map((promise) => promise.data);
      this.pickerService.configureAndOpen(this.createPickerConfig(items), this.context, this.createDialogConfig());
    });
  }

  /**
   * If the selection mode has changed the model value needs to be reset if different.
   */
  resetModel() {
    var currentMode = this.getSelectionMode();
    if (!this.criteria.value) {
      this.addDefaultModel(currentMode);
    }
    if (!this.oldSelectionMode) {
      this.oldSelectionMode = currentMode;
    } else if (this.oldSelectionMode !== currentMode) {
      this.addDefaultModel(currentMode);
      this.oldSelectionMode = currentMode;
    }
  }

  /**
   * Assigns default model value depending on the current selection mode.
   */
  addDefaultModel(currentSelectionMode) {
    if (currentSelectionMode === MULTIPLE_SELECTION) {
      this.criteria.value = [];
    } else {
      this.criteria.value = {};
    }
  }

  createPickerConfig(items) {
    var pickerConfig = {
      extensions: {}
    };

    var searchTree = this.isManualSelection() ? {} : this.criteria.value;
    var currentLevel = this.config.level || 1;
    var selectionMode = this.getSelectionMode();

    this.searchConfig = {
      level: ++currentLevel,
      criteria: searchTree,
      results: {
        config: {
          selection: selectionMode,
          selectedItems: items
        }
      },
      predefinedTypes: this.property.range,
      useRootContext: this.config.useRootContext
    };

    if (selectionMode === NO_SELECTION) {
      this.searchConfig.searchMode = searchTree.searchMode;
      pickerConfig.inclusions = [SEARCH_EXTENSION];
    } else if (this.previousManualSelection) {
      this.searchConfig.criteria = this.previousManualSelection.criteria;
      this.searchConfig.searchMode = this.previousManualSelection.searchMode;
    }

    pickerConfig.extensions[SEARCH_EXTENSION] = this.searchConfig;
    return pickerConfig;
  }

  loadSelectedItems() {
    if (!this.criteria || !this.criteria.value || !this.isManualSelection()) {
      return this.promiseAdapter.resolve([]);
    }

    var promises = [];
    this.criteria.value.forEach((id) => {
      // Avoid any contextual items that do not represent specific instance
      if (id !== ANY_OBJECT && id !== CURRENT_OBJECT) {
        promises.push(this.instanceRestService.load(id));
      }
    });

    return this.promiseAdapter.all(promises);
  }

  getSelectionMode() {
    if (this.isManualSelection()) {
      return MULTIPLE_SELECTION;
    } else if (this.criteria.operator === EMPTY_MODE) {
      return EMPTY_MODE;
    }
    return NO_SELECTION;
  }

  createDialogConfig() {
    return {
      buttons: [{
        // specify explicit label
        label: 'dialog.button.save',
      }],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        if (buttonId === DialogService.OK) {
          this.onOkButtonClicked();
        }

        dialogConfig.dismiss();
      }
    };
  }

  onOkButtonClicked() {
    var criteriaTree = this.searchConfig.searchMediator.queryBuilder.tree;
    var searchMode = this.searchConfig.searchMediator.searchMode;

    if (!this.isManualSelection()) {
      this.criteria.value = criteriaTree;
      // Gotta save the search mode in the root criteria so it could be restored later
      this.criteria.value.searchMode = searchMode;
      return;
    }

    var items = this.searchConfig.results.config.selectedItems;
    if (items) {
      // Select2 likes to assign nulls if there is no selection...
      this.criteria.value = this.criteria.value || [];
      // If the value contains some contextual items, they should not be replaced
      var selectedIds = items.filter(item => this.criteria.value.indexOf(item.id) < 0).map(item => item.id);
      this.criteria.value.push(...selectedIds);
    }

    // Temporarily storing the criteria from the object picker so it could be restored if the picker is opened again
    this.previousManualSelection = {
      criteria: criteriaTree,
      searchMode: searchMode
    };
  }

  isManualSelection() {
    var op = this.criteria.operator;
    return AdvancedSearchCriteriaOperators.SET_TO.id === op || AdvancedSearchCriteriaOperators.NOT_SET_TO.id === op;
  }
}