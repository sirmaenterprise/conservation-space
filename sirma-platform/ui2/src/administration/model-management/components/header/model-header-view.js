import {View, Component} from 'app/app';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelValue} from 'administration/model-management/model/model-value';

import 'administration/model-management/components/validation/model-validation-messages';
import 'components/collapsible/collapsible-panel';
import 'filters/to-trusted-html';

import 'components/sourcearea/sourcearea';

import './model-header-view.css!css';
import template from './model-header-view.html!text';

/**
 * Component responsible for visualizing single header in a panel with additional controls.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-header-view',
  transclude: true,
  properties: {
    'attribute': 'attribute',
    'definition': 'definition',
    'language': 'language',
    'emitter': 'emitter'
  },
  events: ['onHeaderChange']
})
@View({
  template
})
export class ModelHeaderView extends ModelGenericAttribute {

  constructor() {
    super();
    this.expanded = false;
  }

  ngOnInit() {
    this.createSourceAreaConfig();
  }

  //@Override
  onChange() {
    return this.onHeaderChange && this.onHeaderChange({attribute: this.attribute});
  }

  //@Override
  getValue() {
    return this.getCurrentHeaderValue().getValueEscaped();
  }

  //@Override
  setValue(value) {
    this.getCurrentHeaderValue().setValue(value);
  }

  getCurrentHeaderValue() {
    let value = this.attribute.getValueByLanguage(this.language);

    if (!value) {
      value = new ModelValue(this.language, '');
      this.attribute.addValue(value);
    }
    return value;
  }

  getCurrentHeaderValidation() {
    let value = this.getCurrentHeaderValue();
    return this.attribute.getValidationForValue(value);
  }

  getHeaderTypeLabel() {
    return this.getHeader().getHeaderTypeOption().label;
  }

  getInheritedParentLabel() {
    // The parent definition from which the header is inherited
    return this.getHeader().getParent().getDescription().getValue();
  }

  getHeader() {
    return this.attribute.getParent();
  }

  copyDefaultValue($event) {
    $event.stopPropagation();
    let currentValue = this.attribute.getValueByLanguage(this.language).getValue();
    let defaultValue = this.attribute.getValue().getValue();
    this.attribute.getValueByLanguage(this.language).setValue(defaultValue);
    this.onModelChange(currentValue);
  }

  toggleView() {
    this.expanded = !this.expanded;
  }

  isDefaultLanguageLoaded() {
    return this.language === this.attribute.getValue().getLanguage();
  }

  isInherited() {
    return ModelManagementUtility.isInherited(this.getHeader(), this.definition);
  }

  isCurrentHeaderInvalid() {
    return this.getCurrentHeaderValidation().isInvalid();
  }

  isCurrentHeaderDirty() {
    return this.getCurrentHeaderValue().isDirty();
  }

  createSourceAreaConfig() {
    this.sourceareaConfig = {
      mode: 'htmlmixed'
    };
  }
}