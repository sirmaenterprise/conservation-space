import {Component, View} from 'app/app';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelValidationReport} from 'administration/model-management/model/validation/model-validation-report';
import {ModelListView} from 'administration/model-management/components/list/model-list-view';

import './model-list-validation-view.css!css';
import template from './model-list-validation-view.html!text';

/**
 * Component meant to display a list of models, which have some kind of validation log. It extends the generic component
 * {@link ModelListView} and offers the same functionality for selection types. The properties must be
 * extended with <code>report</code> property, which is an instance of {@link ModelValidationReport}. New
 * optional config is introduced: 'skipValidModels'. If it is set to true, only invalid models will be displayed.
 *
 * @author Radoslav Dimitrov
 */
@Component({
  selector: 'model-list-validation-view',
  properties: {
    'config': 'config',
    'report': 'report',
    'models': 'models',
    'onAction': 'on-action'
  }
})
@View({
  template
})
export class ModelListValidationView extends ModelListView {

  constructor() {
    super();
    this.filterModels();
  }

  filterModels() {
    if (this.config.skipValidModels) {
      let oldList = this.models;
      this.models = new ModelList();
      oldList.getModels()
        .filter(model => this.getValidationReport().hasErrors(model.getId()))
        .forEach(model => this.models.insert(model));
    }
  }

  getValidationReport() {
    return this.report;
  }

  selectAll() {
    this.models.getModels().forEach(this.selectItem.bind(this));
  }

  selectItem(item) {
    if (!this.hasCriticalErrors(item)) {
      this.config.selected.insert(item);
      this.triggerListAction();
    }
  }

  getErrors(item) {
    return this.getValidationReport().getErrorsForModel(item.getId());
  }

  hasCriticalErrors(model) {
    return this.getValidationReport().hasCriticalErrors(model.getId());
  }

  isErrorMessage(message) {
    return ModelValidationReport.isErrorMessage(message);
  }

  isWarningMessage(message) {
    return ModelValidationReport.isWarningMessage(message);
  }
}