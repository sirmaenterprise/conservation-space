import {View, Component} from 'app/app';
import {Configurable} from 'components/configurable';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import './model-list-view.css!css';
import template from './model-list-view.html!text';

export const RADIOBTN = 'radio';
export const CHECKBOX = 'checkbox';

/**
 * Generic component meant to display a list of models. The list should be of type {@link ModelList}, furthermore
 * this component supports two different selection types - multi and single selection implemented as a checkbox
 * and radio button selections. To enable this behaviour a proper configuration should be passed to this component
 * which can also contain the pre-selected items also of type {@link ModelList}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-list-view',
  properties: {
    'config': 'config',
    'models': 'models',
    'onAction': 'on-action'
  }
})
@View({
  template
})
export class ModelListView extends Configurable {

  constructor() {
    super({
      singleSelection: true,
      selectableItems: false
    });
  }

  ngOnInit() {
    // make sure that a proper selection list is prepared
    if (this.config.selectableItems && !this.config.selected) {
      this.config.selected = new ModelList();
    }
  }

  toggleItem(item) {
    // when configured as single select
    if (this.config.singleSelection) {
      this.deselectAll();
    }

    // toggle the item presence
    if (!this.isSelected(item)) {
      this.selectItem(item);
    } else {
      this.deselectItem(item);
    }
  }

  selectAll() {
    this.config.selected.copyFrom(this.models);
    this.triggerListAction();
  }

  deselectAll() {
    this.config.selected.clear();
    this.triggerListAction();
  }

  selectItem(item) {
    this.config.selected.insert(item);
    this.triggerListAction();
  }

  deselectItem(item) {
    this.config.selected.remove(item.getId());
    this.triggerListAction();
  }

  getModelName(model) {
    return ModelManagementUtility.getModelName(model);
  }

  getModelIdentifier(model) {
    return ModelManagementUtility.getModelIdentifier(model);
  }

  getSelectionControlType() {
    return this.config.singleSelection ? 'radio' : 'checkbox';
  }

  isSelected(item) {
    let selected = this.config.selected;
    return selected.hasModel(item.getId());
  }

  isSelectDeselectEnabled() {
    return !this.config.singleSelection && this.config.selectableItems;
  }

  hasModelsToDisplay() {
    return !!this.models && this.models.getModels().length > 0;
  }

  triggerListAction() {
    this.onAction && this.onAction();
  }
}