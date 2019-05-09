import {View, Component, Inject, NgScope} from 'app/app';
import {ModelControlExtensionProviderService} from 'administration/model-management/components/field/control/model-control-extension-provider-service';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {CONTROL_TYPE} from 'models/model-utils';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import 'administration/model-management/components/field/control/model-field-control-view';
import 'administration/model-management/components/controls/model-controls';

import './model-field-controls.css!css';
import template from './model-field-controls.html!text';

/**
 * Component responsible for rendering of model controls bound to given model field.
 * Provided model is of type {@link ModelField}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-field-controls',
  properties: {
    'model': 'model',
    'emitter': 'emitter'
  },
  events: ['onAttributeChange', 'onModelControlCreate', 'onModelControlRemove']
})
@View({
  template
})
@Inject(NgScope, TranslateService, ModelControlExtensionProviderService)
export class ModelFieldControls {

  constructor($scope, translateService, modelControlExtensionProviderService) {
    this.$scope = $scope;
    this.translateService = translateService;
    this.modelControlExtensionProviderService = modelControlExtensionProviderService;
  }

  ngOnInit() {
    this.initModelWatcher();
    this.initTypeWatcher();
    this.loadExtensions();
  }

  initModelWatcher() {
    this.$scope.$watch(() => this.model, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.initView();
      }
    });
  }

  initTypeWatcher() {
    this.$scope.$watch(() => this.getTypeOptionValue(), (newValue, oldValue) => {
      if (newValue !== oldValue && this.model.isDirty()) {
        let controls = this.model.getControls().slice(0);
        controls.forEach(control => this.removeControl(control));
        this.initView();
      }
    });
  }

  loadExtensions() {
    this.modelControlExtensionProviderService.loadModelControlExtensions()
      .then(extensions => {
        this.extensions = extensions || {};
        this.initView();
      });
  }

  getLinks(links) {
    if (this.extensions) {
      let typeOption = this.getTypeOptionValue();
      let supportedControls = this.getSupportedControls(this.extensions, typeOption);
      let newLinks = this.getControlLinks(supportedControls, this.model.getControls());
      if (links && links.length !== newLinks.length) {
        this.links = newLinks;
      }
    }
    return this.links;
  }

  initView() {
    let typeOption = this.getTypeOptionValue();
    let supportedControls = this.getSupportedControls(this.extensions, typeOption);
    this.links = this.getControlLinks(supportedControls, this.model.getControls());
  }

  getSupportedControls(extensions, type) {
    let supported = [];
    Object.keys(extensions).forEach(key => {
      if (this.isControlSupported(extensions[key], type)) {
        supported.push(extensions[key]);
      }
    });
    return supported;
  }

  getControlLinks(extensions, controls) {
    let controlTypes = controls.map(control => control.getId());
    if (controlTypes.length === 1 && this.extensions[controlTypes[0]].immutable) {
      return [];
    }

    // only leave links for controls which are not present in the model - those which could be added
    return extensions.filter(extension => controlTypes.indexOf(extension.type) === -1)
      .map(extension => {
        return {
          title: this.translateService.translateInstant(extension.link),
          tooltip: this.translateService.translateInstant(extension.linkTooltip),
          type: extension.type
        };
      });
  }

  createControl(type) {
    this.onModelControlCreate({type});
  }

  canBeRemoved(control) {
    return !this.extensions[control.getId()].immutable && (this.model.getControls().length > 1 || !ModelManagementUtility.isInherited(control, this.model));
  }

  removeControl(control) {
    return this.onModelControlRemove && this.onModelControlRemove({control});
  }

  getTypeOptionValue() {
    let typeOption = this.model.getAttribute(ModelAttribute.TYPE_OPTION_ATTRIBUTE);
    return typeOption.getValue().getValue();
  }

  isControlSupported(control, type) {
    let hasSemanticProperties = this.model.getProperty();
    let hidePickerControl = !hasSemanticProperties && control.type !== CONTROL_TYPE.PICKER;
    let hideConceptPickerControl = hasSemanticProperties && control.type !== CONTROL_TYPE.CONCEPT_PICKER;
    return !control.immutable &&  control.supportedBy.indexOf(type) !== -1 && (hidePickerControl || hideConceptPickerControl);
  }

  onControlChanged(attribute) {
    return this.onAttributeChange({attribute});
  }
}