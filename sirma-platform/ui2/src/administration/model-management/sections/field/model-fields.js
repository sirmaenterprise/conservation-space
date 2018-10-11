import {View, Component} from 'app/app';
import {Configurable} from 'components/configurable';
import {ValidationService} from 'form-builder/validation/validation-service';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelAttributeTypes} from 'administration/model-management/model/model-attribute';
import _ from 'lodash';

import 'administration/model-management/components/model-field-view';
import 'administration/model-management/components/model-region-view';

import './model-fields.css!css';
import template from './model-fields.html!text';

const DISPLAY_TYPE_HIDDEN = ValidationService.DISPLAY_TYPE_HIDDEN;
const DISPLAY_TYPE_SYSTEM = ValidationService.DISPLAY_TYPE_SYSTEM;
const DISPLAY_TYPE = ModelAttributeTypes.SINGLE_VALUE.MODEL_DISPLAY_TYPE;

/**
 * A component in charge of displaying the field structure of a given model.
 * The provided model is supplied through a component property. It should
 * be of type {@link ModelDefinition}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-fields',
  properties: {
    'model': 'model',
    'emitter': 'emitter'
  }
})
@View({
  template
})
export class ModelFields extends Configurable {

  constructor() {
    super({
      filterTerm: '',
      showSystem: false,
      showHidden: false,
      showInherited: true
    });
  }

  ngOnInit() {
    this.initialize(this.model);
    this.subscribeToModelChanged();
  }

  initialize(model) {
    this.model = model;
    if (this.isModelDefinition()) {
      this.filterRules = _.clone(this.config);
      this.triggerFilter();
    }
  }

  subscribeToModelChanged() {
    this.emitter && this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (model) => this.initialize(model));
  }

  filter(models, callback) {
    return models.map(item => {
      if (this.isModelField(item)) {
        // execute the field callback
        callback(item);
      } else {
        let fields = item.getFields();
        // get all visible fields from the region
        let visible = this.filter(fields, callback);
        // hide parent region if all fields are hidden
        item.getView().setVisible(visible.length >= 1);
      }
      return item;
    }).filter(item => item.getView().isVisible());
  }

  triggerFilter() {
    this.showSection = this.filter(this.model.getModels(), field => {
        // compute the result based on the filter conditions
        let matching = this.isFieldNameMatchingKeyword(field);
        let system = this.isFieldSystem(field) ? this.filterRules.showSystem : true;
        let hidden = this.isFieldHidden(field) ? this.filterRules.showHidden : true;
        let inherited = this.isFieldInherited(field) ? this.filterRules.showInherited : true;

        // resolve field view properties based on the filter conditions
        field.getView().setShowParent(this.isFieldInherited(field))
          .setVisible(matching && inherited && system && hidden);
      }).length >= 1;
  }

  isFieldInherited(field) {
    return field.getParent() != this.model;
  }

  isFieldSystem(field) {
    return this.getDisplayType(field) === DISPLAY_TYPE_SYSTEM;
  }

  isFieldHidden(field) {
    return this.getDisplayType(field) === DISPLAY_TYPE_HIDDEN;
  }

  isFieldNameMatchingKeyword(field) {
    let term = this.filterRules.filterTerm;
    let name = field.getDescription().getValue().toUpperCase();
    return (!term || !term.length) ? true : name.indexOf(term.toUpperCase()) >= 0;
  }

  isModelField(model) {
    return model instanceof ModelField;
  }

  isModelDefinition() {
    return this.model && this.model instanceof ModelDefinition;
  }

  getDisplayType(field) {
    return this.getAttributeValue(field, DISPLAY_TYPE);
  }

  getOrder(field) {
    return this.getAttributeValue(field, 'order');
  }

  getAttributeValue(model, type) {
    let attribute = model.getAttribute(type);
    return attribute && attribute.getValue().getValue();
  }
}