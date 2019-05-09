import {View, Component} from 'app/app';
import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelSection} from 'administration/model-management/sections/model-section';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import 'administration/model-management/components/attributes/model-attribute-view';
import 'administration/model-management/components/controls/model-controls';
import 'administration/model-management/components/controls/save/model-save';
import 'administration/model-management/components/controls/cancel/model-cancel';

import './model-general.css!css';
import template from './model-general.html!text';

/**
 * A component in charge of displaying all general attributes for a model.
 * The provided model is supplied through a component property. It can either
 * be a {@link ModelClass} or {@link ModelDefinition}. Internally model is
 * resolved depending on it's type.
 *
 * When a {@link ModelDefinition} is provided all of it's general attributes
 * are displayed along side with the general attributes of the type this
 * definition represents.
 *
 * When a {@link ModelClass} is provided all of it's general attributes
 * are displayed.
 *
 * Besides the model property, this component accepts an event emitter
 * property of type {@link EventEmitter}. This emitter is used to communicate
 * changes or modifications in the model if such are needed. If the model
 * which is provided is not expected to be changing this property is not
 * required to be provided.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-general',
  properties: {
    'model': 'model',
    'emitter': 'emitter'
  },
  events: ['onModelSave', 'onSectionStateChange', 'onModelStateChange', 'onModelActionCreateRequest',
    'onModelActionExecuteRequest', 'onModelActionRevertRequest']
})
@View({
  template
})
export class ModelGeneral extends ModelSection {

  constructor() {
    super();
  }

  ngOnInit() {
    this.subscribeToModelChanged();
    this.initializeModels(this.model);
  }

  initializeModels(model) {
    this.model = model;
    this.class = this.getModelClass(model);
    this.definition = this.getModelDefinition(model);
    this.getSectionModels().forEach(m => this.notifyForModelStateCalculation(m, m));
    this.afterModelChange();
  }

  //@Override
  getSectionModels() {
    let models = [];
    this.class && models.push(this.class);
    this.definition && models.push(this.definition);
    return models;
  }

  //@Override
  isModelValid(model) {
    return ModelBase.areModelsValid(model.getOwnAttributes());
  }

  subscribeToModelChanged() {
    this.emitter && this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (model) => this.initializeModels(model));
  }

  getClassName() {
    return ModelManagementUtility.getModelName(this.class);
  }

  getClassIdentifier() {
    return ModelManagementUtility.getModelIdentifier(this.class);
  }

  getDefinitionName() {
    return ModelManagementUtility.getModelName(this.definition);
  }

  getDefinitionIdentifier() {
    return ModelManagementUtility.getModelIdentifier(this.definition);
  }

  getModelClass(model) {
    return ModelManagementUtility.isModelDefinition(model) ? model.getType() : model;
  }

  getModelDefinition(model) {
    return ModelManagementUtility.isModelDefinition(model) ? model : null;
  }
}
