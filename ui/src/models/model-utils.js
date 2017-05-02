import {INSTANCE_HEADERS} from 'instance-header/header-constants';

export class ModelUtils {

  static createViewModel() {
    return {
      'fields': []
    };
  }

  static addField(viewModel, field) {
    viewModel.fields.push(field);
  }

  static createField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist) {
    let model = {
      'identifier': id,
      'previewEmpty': true,
      'disabled': false,
      'displayType': displayType,
      'tooltip': 'tooltip',
      'validators': [...validators],
      'dataType': dataType,
      'label': label,
      'isMandatory': mandatory,
      'maxLength': 40,
      'rendered': rendered
    };
    if (codelist) {
      model.codelist = codelist;
    }
    if (control) {
      model.control = {
        'identifier': control
      }
    }
    return model;
  }

  static createInstanceModel() {
    return {

    }
  }

  static addProperty(instanceModel, id, property) {
    instanceModel[id] = property;
  }

  static createProperty(value, valid) {
    return {
      'messages': {},
      'value': value,
      'valid': valid !== undefined ? valid : true
    };
  }

  static flatViewModel(viewModel, flatViewModelMap) {
    if (flatViewModelMap) {
      viewModel.fields.forEach((field) => {
        if (!!field.fields) {
          ModelUtils.flatViewModel(field, flatViewModelMap);
        } else {
          flatViewModelMap.set(field.identifier, field);
        }
      });
    } else {
      let flatModelMap = new Map();
      ModelUtils.flatViewModel(viewModel, flatModelMap);
      return flatModelMap;
    }
  }

  static buildEmptyCell(identifier) {
    return {
      identifier: identifier,
      displayType: 'EDITABLE',
      previewEmpty: true,
      control: {
        identifier: 'EMPTY_CELL'
      }
    };
  }

  /**
   * Identify the control type using the viewModel properties for every field.
   * @returns control type
   */
  static defineControlType(propertyModel, textareaMinCharsLength) {
    let controlType = null;
    if (!propertyModel) {
      return controlType;
    }
    let dataType = propertyModel.dataType || '';
    if (propertyModel.control && !SUPPORTED_CONTROLS[propertyModel.control.identifier]) {
      controlType = propertyModel.control.identifier;
    } else {
      if (INSTANCE_HEADERS[propertyModel.identifier]) {
        controlType = CONTROL_TYPE.INSTANCE_HEADER;
      } else if (ModelUtils.isRegion(propertyModel)) {
        controlType = CONTROL_TYPE.REGION;
      } else if (ModelUtils.REPRESENTABLE_AS_TEXT[dataType]) {
        controlType = ModelUtils.getTextFieldType(propertyModel, textareaMinCharsLength);
      } else if (dataType === 'date' || dataType === 'datetime') {
        controlType = CONTROL_TYPE.DATETIME;
      } else if (dataType === 'boolean') {
        controlType = CONTROL_TYPE.CHECKBOX;
      } else if (dataType === 'password') {
        controlType = CONTROL_TYPE.PASSWORD;
      }
    }
    return controlType;
  }

  static isRegion(viewModel) {
    return viewModel.fields !== undefined;
  }

  static getTextFieldType(currentModelItem, textareaMinCharsLength) {
    let controlType = CONTROL_TYPE.TEXT;
    if (currentModelItem.codelist) {
      controlType = CONTROL_TYPE.CODELIST;
    } else if (currentModelItem.maxLength >= textareaMinCharsLength) {
      controlType = CONTROL_TYPE.TEXTAREA;
    }
    return controlType;
  }
}

export const CONTROL_TYPE = {
  INSTANCE_HEADER: 'INSTANCE_HEADER',
  RADIO_BUTTON_GROUP: 'RADIO_BUTTON_GROUP',
  CODELIST_LIST: 'codelist-list',
  REGION: 'region',
  DATETIME: 'datetime',
  CHECKBOX: 'checkbox',
  TEXT: 'text',
  PASSWORD: 'password',
  CODELIST: 'codelist',
  TEXTAREA: 'textarea'
};

// Some fields have controls in the definitions.
// Here are listed the controls that can be rendered as normal fields.
const SUPPORTED_CONTROLS = {
  RELATED_FIELDS: true,
  CALCULATED_DATE: true,
  DATERANGE: true,
  BYTE_FORMAT: true,
  EMPTY_CELL: false,
  INSTANCE_HEADER: false,
  USER: false,
  PICKER: false
};

ModelUtils.REPRESENTABLE_AS_TEXT = {
  'text': true,
  'int': true,
  'long': true,
  'float': true,
  'double': true
};