import {View, Component, Inject} from 'app/app';
import {CodelistRestService} from 'services/rest/codelist-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import _ from 'lodash';

import 'components/datetimepicker/datetimepicker';
import 'components/select/select';
import 'components/hint/hint';

import './model-field-view.css!css';
import template from './model-field-view.html!text';

const TEXT_TYPE = 'text';
const DATETIME_TYPE = 'date';
const PICKER_TYPE = 'picker';
const SELECT_TYPE = 'select';
const BOOLEAN_TYPE = 'boolean';
const TYPE_ATTRIBUTE = 'type';

const VALUE_ATTRIBUTE = 'value';
const TOOLTIP_ATTRIBUTE = 'tooltip';
const CODELIST_ATTRIBUTE = 'codeList';
const DISPLAY_ATTRIBUTE = 'displayType';
const MANDATORY_ATTRIBUTE = 'mandatory';
const MULTIVALUE_ATTRIBUTE = 'multiValued';

const EDITABLE = ValidationService.DISPLAY_TYPE_EDITABLE;
const READONLY = ValidationService.DISPLAY_TYPE_READ_ONLY;

/**
 * A component in charge of displaying a single model field.
 * The provided model is supplied through a component property.
 * The proved region should be of type {@link ModelField}
 * or any types extending off of it.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-field-view',
  properties: {
    'model': 'model'
  }
})
@View({
  template
})
@Inject(CodelistRestService)
export class ModelFieldView {

  constructor(codelistRestService) {
    this.codelistRestService = codelistRestService;
  }

  ngOnInit() {
    this.initializeValue();
    this.initializeConverter();
    this.tooltip = this.getTooltip();
    this.mandatory = this.getMandatory();
  }

  initializeConverter() {
    this.typeConverter = {};
    this.typeConverter[TEXT_TYPE] = (type) => /^[A-Z]+..\d*,?\d+?$/.test(type);
    this.typeConverter[PICKER_TYPE] = (type) => type === 'ANY' || type === 'URI';
    this.typeConverter[DATETIME_TYPE] = (type) => type === 'DATE' || type === 'DATETIME';
  }

  initializeValue() {
    let code = this.getCodeList();

    // check if field is a code list
    if (code && this.code !== code) {
      this.code = code;
      // fetch the code list provided as attribute in the current field model
      this.codelistRestService.getCodelist({codelistNumber: this.code}).then(response => {
        if (!this.selectConfig) {
          this.selectConfig = {
            disabled: false,
            selectOnClose: false,
            reloadOnDataChange: true
          };
        }
        // configure select based on the additional field attributes
        this.selectConfig.multiple = this.getAttributeValue(MULTIVALUE_ATTRIBUTE);
        this.selectConfig.data = response.data.map(item => this.getSelectItem(item));

        if (this.isReadOnly()) {
          // resolve value from list of existing code values
          let value = this.getAttributeValue(VALUE_ATTRIBUTE);
          let found = _.find(this.selectConfig.data, item => item.id === value);
          this.value = found ? found.text : this.selectConfig.data.map(item => item.text).join(', ');
        }
      });
    }
  }

  getType() {
    // resolve type early
    if (this.isCodeList()) {
      return SELECT_TYPE;
    }
    // resolve type based on attribute type's value
    let type = this.getAttributeValue(TYPE_ATTRIBUTE);
    let converters = Object.keys(this.typeConverter);
    return _.find(converters, item => this.typeConverter[item](type.toUpperCase())) || type;
  }

  getTooltip() {
    return this.getAttributeValue(TOOLTIP_ATTRIBUTE);
  }

  getMandatory() {
    return this.getAttributeValue(MANDATORY_ATTRIBUTE);
  }

  getCodeList() {
    return this.getAttributeValue(CODELIST_ATTRIBUTE);
  }

  getAttributeValue(attribute) {
    let attr = this.model.getAttribute(attribute);
    return attr && attr.getValue().getValue();
  }

  getSelectItem(data) {
    return {id: data.value, text: data.label};
  }

  isFieldVisible() {
    return this.model.getView().isVisible();
  }

  isParentVisible() {
    return this.model.getView().showParent();
  }

  isReadOnly() {
    return !this.isEditable();
  }

  isEditable() {
    return this.getAttributeValue(DISPLAY_ATTRIBUTE) === EDITABLE;
  }

  isCodeList() {
    return !!this.getAttributeValue(CODELIST_ATTRIBUTE);
  }

}