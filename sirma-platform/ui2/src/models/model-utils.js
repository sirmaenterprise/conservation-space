import {INSTANCE_HEADERS} from 'instance-header/header-constants';
import {InstanceModelProperty} from 'models/instance-model';
import _ from 'lodash';

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
  PICKER: false,
  CONCEPT_PICKER: false,
  DEFAULT_VALUE_PATTERN: true
};

export class ModelUtils {

  static createViewModel() {
    return {
      'fields': []
    };
  }

  static addField(viewModel, field) {
    viewModel.fields.push(field);
  }

  static createField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist, isDataProperty = true) {
    let model = {
      identifier: id,
      previewEmpty: true,
      disabled: false,
      displayType,
      tooltip: 'tooltip',
      validators: [...validators],
      dataType,
      label,
      isMandatory: mandatory,
      maxLength: 40,
      rendered,
      isDataProperty
    };
    if (codelist) {
      model.codelist = codelist;
    }
    if (control) {
      model.control = {
        'identifier': control
      };
    }
    return model;
  }

  static createInstanceModel() {
    return {};
  }

  static addProperty(instanceModel, id, property) {
    instanceModel[id] = property;
  }

  static getEmptyObjectPropertyValue() {
    return {
      results: [],
      total: 0,
      add: [],
      remove: [],
      headers: {}
    };
  }

  /**
   * Add missing value attributes.
   *
   * @param propertyModel The object property instance model.
   */
  static normalizeObjectPropertyValue(propertyModel) {
    if (!propertyModel.value) {
      propertyModel.value = ModelUtils.getEmptyObjectPropertyValue();
    }
    if (!propertyModel.defaultValue) {
      propertyModel.defaultValue = ModelUtils.getEmptyObjectPropertyValue();
    }
    if (!propertyModel.value.results) {
      propertyModel.value.results = [];
    }
    if (!propertyModel.value.add) {
      propertyModel.value.add = [];
    }
    if (!propertyModel.value.remove) {
      propertyModel.value.remove = [];
    }
    if (propertyModel.value.total === undefined) {
      propertyModel.value.total = 0;
    }
    if (!propertyModel.value.headers) {
      propertyModel.value.headers = {};
    }
  }

  /**
   * Adds a new object property in the instance model. The property will be added if it doesn't exist.
   * Only instance model is updated. If a propertyValue is provided, then it is populated in the field's model.
   *
   * @param instanceModel The instance model.
   * @param id The id of the object property to be created in the model.
   * @param propertyValue The initial value to be added in the property's value. It can be an array containing more
   * than one new value or a single id as a string.
   */
  static addObjectProperty(instanceModel, id, propertyValue) {
    if (!instanceModel[id]) {

      let value = ModelUtils.getEmptyObjectPropertyValue();
      instanceModel[id] = new InstanceModelProperty(value);

      if (propertyValue) {
        ModelUtils.updateObjectProperty(instanceModel, id, propertyValue);
      }
    }
  }

  static filterDuplicatedArrayValues(targetArray, toFilter) {
    let notExistentValues = [];
    toFilter.forEach((item) => {
      if (targetArray.indexOf(item) === -1) {
        notExistentValues.push(item);
      }
    });
    return notExistentValues;
  }

  /**
   * Adds a new value in the object property with given id in the instance model.
   *
   * @param instanceModel The object instance model.
   * @param id The object property field id in the model. The property value will be added to that field in the model.
   * @param propertyValue The initial value to be added in the property's value. It can be an array containing more
   * than one new value or a single id as a string.
   */
  static updateObjectProperty(instanceModel, id, propertyValue) {
    if (!instanceModel[id]) {
      return;
    }
    // If the property is present in model but there has undefined value, then one is created before the new object is
    // to be added.
    this.normalizeObjectPropertyValue(instanceModel[id]);

    const value = (propertyValue instanceof Array) ? propertyValue : [propertyValue];

    let newValues = ModelUtils.filterDuplicatedArrayValues(instanceModel[id].value.results, value);
    // Mutate the results once to prevent multiple invocations of the propertyChanged observer.
    instanceModel[id].value.results.push(...newValues);

    // New values go in the add array too. Both add and remove arrays form the property changeset.
    instanceModel[id].value.add.push(...ModelUtils.filterDuplicatedArrayValues(instanceModel[id].value.add, value));

    instanceModel[id].value.total += newValues.length;
  }

  /**
   * Updates an object property value with the provided selection. The selection contains the ids of all related objects
   * that should be present in the value. The changes-set is updated as well but only if the actual value has been
   * changed which is proved by comparing the new value to field's default value if any.
   *
   * @param instanceModelProperty The instance model.
   * @param isSingleValue If the property is single or multi-value type.
   * @param selection An array with instance ids which should be populated in the property value.
   */
  static updateObjectPropertyValue(instanceModelProperty, isSingleValue, selection = []) {
    ModelUtils.normalizeObjectPropertyValue(instanceModelProperty);

    instanceModelProperty.value.total = selection.length;
    let propertyValue = instanceModelProperty.value;

    if (isSingleValue) {

      let isSelectionChanged = selection.length > 0 && selection[0] !== propertyValue.results[0];
      if (isSelectionChanged) {
        propertyValue.add.splice(0, 1, ...selection);

        // if default value is present -> assume its the one to be removed,
        // else -> assume there has not been a value and just populate the add array.
        // !even if the value has been switched multiple times before instance save.
        if (instanceModelProperty.defaultValue && instanceModelProperty.defaultValue.results[0] !== propertyValue.remove[0]) {
          propertyValue.remove.splice(0, 1, ...instanceModelProperty.defaultValue.results);
        }

        propertyValue.results.splice(0, 1, ...selection);

        // If initial value is selected, then the changeset should be empty.
        // In create/upload dialog there is no defaultValue as the default values are assigned idoc context when a new
        // InstanceObject is created.
        if (instanceModelProperty.defaultValue && propertyValue.results[0] === instanceModelProperty.defaultValue.results[0]) {
          propertyValue.add.length = 0;
          propertyValue.remove.length = 0;
        }
      } else if(propertyValue.results.length > 0 && selection.length == 0){
        // check if old value is not empty this -> assume we have to remove it.
        propertyValue.remove.splice(0, 1, propertyValue.results[0]);
        propertyValue.results.splice(0, 1);
        propertyValue.add.length = 0;
      }
    } else {

      // Should update add and remove arrays with proper data.
      let added = _.difference(selection, propertyValue.results);
      added = _.union(propertyValue.add, added);

      let removed = _.difference(propertyValue.results, selection);
      removed = _.union(propertyValue.remove, removed);
      // Remove objects that were added again through the picker
      removed = _.without(removed, ...selection);
      propertyValue.remove.length = 0;
      propertyValue.remove.push(...removed);

      // If any previously added value is now removed, then remove it from the change set too.
      added = _.difference(added, removed);
      propertyValue.add.length = 0;
      propertyValue.add.push(...added);

      propertyValue.results.length = 0;
      propertyValue.results.push(...selection);

      let isDefaultValueChanged = !instanceModelProperty.defaultValue
        || !instanceModelProperty.defaultValue.results
        || !instanceModelProperty.defaultValue.results.length
        || !_.eq(instanceModelProperty.defaultValue.results, propertyValue.results);

      if (!isDefaultValueChanged) {
        propertyValue.remove.length = 0;
        propertyValue.add.length = 0;
      }

    }
  }

  /**
   * Set or update header in given object property model. An object property can be represented with one of the header
   * types in different widgets. This means that its different headers could be loaded and stored in the headers map and
   * used when needed. This method allows updating an existing header object with a new attribute or creating of new
   * headers object. The headers object is in format:
   * headers: { instanceId: { id: instanceId, compact_header: 'header', default_header: 'header', ... }, ... }
   *
   * @param model Can be InstanceModel or InstanceModelProperty
   * @param propertyName Optional property name which is used to extract the InstanceModelProperty from the model only
   *  if the model parameter is InstanceModel. Otherwise InstanceModelProperty is used directly.
   * @param instanceId The instance id for which should be added a header entry
   * @param header The header string/html
   * @param headerType The header type as defined in HeaderConstants.
   */
  static setObjectPropertyHeader(model, propertyName, instanceId, header, headerType) {
    let instancePropertyModel = model;

    if (!(model instanceof InstanceModelProperty)) {
      instancePropertyModel = model[propertyName];
    }

    if (!instancePropertyModel.value) {
      return;
    }

    let instancePropertyValue = instancePropertyModel.value;

    if (!instancePropertyValue.headers) {
      instancePropertyValue.headers = {};
    }

    ModelUtils.updateObjectPropertyHeaders(instancePropertyValue, instanceId, headerType, header);
  }

  static updateObjectPropertyHeaders(instancePropertyValue, instanceId, headerType, header) {
    if (!instancePropertyValue.headers[instanceId]) {
      instancePropertyValue.headers[instanceId] = {
        id: instanceId,
        [headerType]: header
      };
    } else {
      instancePropertyValue.headers[instanceId][headerType] = header;
    }
  }

  /**
   * This method is suitable for getting value when the instance has been wrapped in an InstanceObject where the model
   * is also wrapped in InstanceModel and the property is InstanceModelProperty. Loaded instance has object properties
   * with model like this { references: { results: [ids], total: number, limit: number }. When model is wrapped, the
   * value becomes { references: { value: { results: [ids], total: number, limit: number } }.
   * @param instanceModel
   * @return {*}
   */
  static getObjectPropertyValue(instanceModel) {
    if(instanceModel) {
      if(instanceModel.value) {
        return instanceModel.value.results;
      }
      return instanceModel.results;
    }
  }

  /**
   * There are situations where the instance property type can't be resolved due to the lack of a view model where the
   * actual type is returned. So the object property type is resolved by checking of the model format.
   * The two type of checks are needed because if raw data model is passed there will be no value attribute, but if an
   * InstanceModelProperty is passed there will be a value attribute in which the actual property value will be found.
   * InstanceModelProperty is created for every instance property when the instance model has been wrapped in an InstanceObject.
   * @param instanceModel The instance property model
   * @return {*}
   */
  static isObjectProperty(instanceModel) {
    return !!((instanceModel.value && instanceModel.value.results) || instanceModel.results !== undefined);
  }

  static createProperty(value, valid) {
    return {
      messages: [],
      value,
      valid: valid !== undefined ? valid : true
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
      identifier,
      displayType: 'EDITABLE',
      previewEmpty: true,
      control: {
        identifier: 'EMPTY_CELL'
      }
    };
  }

  static buildPreviewTextField(identifier) {
    return {
      identifier,
      displayType: 'READ_ONLY',
      previewEmpty: true,
      dataType: 'text',
      label: '',
      maxLength: 40,
      isMandatory: false,
      rendered: true,
      validators: []
    };
  }

  /**
   * Recursively traverses the viewModel tree and removes fields that don't match the filterFunction.
   * The fields are removed from view and validation models.
   * The regions are cleared only if there isn't any field that matches the filter function.
   *
   * @param viewModelFields fields that has to be visited
   * @param validationModel validation model
   * @param filterFunction function that acceptds a view model field and validation model fiend and should return true
   * if the particular field should be kept. If returns false for a particular fields, it will be removed.
   */
  static filterFields(viewModelFields, validationModel, filterFunction) {
    // iterate backwards, because the array gets mutated during traversing
    _.forEachRight(viewModelFields, (field, index) => {
      var shouldBeDeleted;
      if (ModelUtils.isRegion(field)) {
        ModelUtils.filterFields(field.fields, validationModel, filterFunction);

        shouldBeDeleted = field.fields.length === 0;
      } else {
        shouldBeDeleted = !filterFunction(field, validationModel[field.identifier]);
      }

      if (shouldBeDeleted) {
        viewModelFields.splice(index, 1);
        delete validationModel[field.identifier];
      }
    });
  }

  /**
   * Recursively traverses the viewModel tree and calls a visitor function for each node.
   * Note: for regions visitor function gets called after the region fields have been traversed
   *
   * @param viewModelFields fields that has to be visited
   * @param validationModel validation model
   * @param visitorFunction function that accepts a view model field and validation model
   */
  static walkModelTree(viewModelFields, validationModel, visitorFunction) {
    viewModelFields.forEach(field => {
      if (ModelUtils.isRegion(field)) {
        ModelUtils.walkModelTree(field.fields, validationModel, visitorFunction);
      }

      visitorFunction(field, validationModel[field.identifier]);
    });
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
    if (propertyModel.controlId && !SUPPORTED_CONTROLS[propertyModel.controlId.toUpperCase()]) {
      controlType = propertyModel.controlId;
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

  static isDatetime(viewModel) {
    return ModelUtils.defineControlType(viewModel) === CONTROL_TYPE.DATETIME;
  }

  static isText(viewModel) {
    return ModelUtils.defineControlType(viewModel) === CONTROL_TYPE.TEXT;
  }

  static isCheckbox(viewModel) {
    return ModelUtils.defineControlType(viewModel) === CONTROL_TYPE.CHECKBOX;
  }

  static isRadiobuttonGroup(viewModel) {
    return ModelUtils.defineControlType(viewModel) === CONTROL_TYPE.RADIO_BUTTON_GROUP;
  }

  static isPicker(viewModel) {
    return ModelUtils.defineControlType(viewModel) === CONTROL_TYPE.PICKER;
  }

  static isRichtext(viewModel) {
    return ModelUtils.defineControlType(viewModel) === CONTROL_TYPE.RICHTEXT;
  }

  static stripHTML(html) {
    return html ? html.replace(/(<([^>]+)>)/ig, '').replace(/&nbsp;/g, ' ') : '';
  }

  static getTextFieldType(currentModelItem, textareaMinCharsLength) {
    let controlType = CONTROL_TYPE.TEXT;
    if (currentModelItem.codelist) {
      controlType = CONTROL_TYPE.CODELIST;
    } else if (currentModelItem.maxLength > textareaMinCharsLength) {
      controlType = CONTROL_TYPE.TEXTAREA;
    }
    return controlType;
  }

  static getControl(control = [], controlId) {
    return _.find(control, (element) => {
      return element.identifier.toUpperCase() === controlId.toUpperCase();
    });
  }
}

export const CONTROL_TYPE = {
  INSTANCE_HEADER: 'INSTANCE_HEADER',
  RADIO_BUTTON_GROUP: 'RADIO_BUTTON_GROUP',
  PICKER: 'PICKER',
  CONCEPT_PICKER: 'CONCEPT_PICKER',
  CODELIST_LIST: 'codelist-list',
  REGION: 'region',
  DATETIME: 'datetime',
  CHECKBOX: 'checkbox',
  TEXT: 'text',
  PASSWORD: 'password',
  CODELIST: 'codelist',
  TEXTAREA: 'textarea',
  OBJECT_TYPE_SELECT: 'OBJECT_TYPE_SELECT',
  RICHTEXT: 'RICHTEXT'
};

export const NUMERIC_TYPES = {
  'int': 'int',
  'long': 'long',
  'float': 'float',
  'double': 'double'
};

ModelUtils.REPRESENTABLE_AS_TEXT = {
  'text': true,
  'int': true,
  'long': true,
  'float': true,
  'double': true
};