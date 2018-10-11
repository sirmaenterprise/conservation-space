import {View, Component} from 'app/app';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import 'administration/model-management/components/model-attribute-view';

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
  }
})
@View({
  template
})
export class ModelGeneral {

  ngOnInit() {
    this.subscribeToModelChanged();
    this.initializeModels(this.model);
  }

  initializeModels(model) {
    this.model = model;
    this.class = this.getModelClass(model);
    this.definition = this.getModelDefinition(model);
  }

  subscribeToModelChanged() {
    this.emitter && this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (model) => this.initializeModels(model));
  }

  getClassName() {
    return this.getModelName(this.class);
  }

  getClassIdentifier() {
    return this.getModelIdentifier(this.class);
  }

  getDefinitionName() {
    return this.getModelName(this.definition);
  }

  getDefinitionIdentifier() {
    return this.getModelIdentifier(this.definition);
  }

  getModelName(model) {
    return this.getModelValue(model.getDescription());
  }

  getModelIdentifier(model) {
    return this.getModelValue(model.getId());
  }

  getModelValue(model) {
    // handle proper model value case or do a fallback
    return (model.getValue && model.getValue()) || model;
  }

  getModelClass(model) {
    return this.isModelDefinition(model) ? model.getType() : model;
  }

  getModelDefinition(model) {
    return this.isModelDefinition(model) ? model : null;
  }

  isModelDefinition(model) {
    return model instanceof ModelDefinition;
  }
}