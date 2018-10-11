import _ from 'lodash';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {ModelUtils, NUMERIC_TYPES} from 'models/model-utils';
import {RelatedObject} from 'models/related-object';
import * as angularAdapter from 'adapters/angular/angular-adapter';

// used as dummy widget id to ensure that current object would never be deregistered
export const CURRENT_OBJECT_TEMP_ID = 'currentObjectTempId';

export class InstanceObject {
  constructor(id, models, content, partiallyLoaded) {
    this.id = id;
    this.models = models;
    this.content = content;
    this.partiallyLoaded = partiallyLoaded;
    this.shouldReload = false;
    if (models) {
      models.id = id;
      this.headers = models.headers || {};
      this.instanceType = models.instanceType;
      if (models.validationModel && !(models.validationModel instanceof InstanceModel)) {
        // TODO: Why and is it needed? push headers in view and validation models
        Object.keys(this.headers).forEach((header) => {
          this.models.validationModel[header] = {
            value: this.headers[header],
            defaultValue: this.headers[header]
          };
          this.models.viewModel.fields.push({
            'identifier': header,
            'dataType': 'text',
            'displayType': 'SYSTEM',
            'isDataProperty': true,
            'controlId': 'INSTANCE_HEADER',
            'control': [{
              'identifier': 'INSTANCE_HEADER'
            }]
          });
        });
        this.models.validationModel = new InstanceModel(models.validationModel);
      }
      if (models.viewModel && !(models.viewModel instanceof DefinitionModel)) {
        this.models.viewModel = new DefinitionModel(this.models.viewModel);
      }
    }
  }

  /**
   * Creates a deep clone of this object. The result is plain javascript object and not an InstanceObject type.
   * @returns {*}
   */
  clone() {
    return _.cloneDeep(this);
  }

  getModels() {
    return this.models;
  }

  setModels(newModels) {
    this.models = newModels;
    return this;
  }

  isUser() {
    return this.getInstanceType() === 'user';
  }

  getContent() {
    return this.content;
  }

  setContent(newContent) {
    this.content = newContent;
    return this;
  }

  getId() {
    return this.id;
  }

  setId(newId) {
    this.id = newId;
    return this;
  }

  getHeader(type) {
    return this.headers && this.headers[type || HEADER_COMPACT];
  }

  getHeaders() {
    return this.headers;
  }

  setHeaders(headers) {
    this.headers = headers;
  }

  getInstanceType() {
    return this.instanceType;
  }

  setInstanceType(instanceType) {
    this.instanceType = instanceType;
  }

  setThumbnail(thumbnail) {
    this.thumbnail = thumbnail;
  }

  getThumbnail() {
    return this.thumbnail;
  }

  setWriteAllowed(writeAllowed) {
    this.writeAllowed = writeAllowed;
  }

  getWriteAllowed() {
    return this.writeAllowed;
  }

  isDirty() {
    return this.dirtyFlag || this.isChanged();
  }

  setDirty(dirty) {
    this.dirtyFlag = dirty;
  }

  isPersisted() {
    return !!this.getId() && this.getId() !== CURRENT_OBJECT_TEMP_ID;
  }

  isLocked() {
    return this.models.validationModel.lockedBy && this.models.validationModel.lockedBy.value !== undefined;
  }

  getShouldReload() {
    return this.shouldReload;
  }

  setShouldReload(reload) {
    this.shouldReload = reload;
  }

  getPartiallyLoaded() {
    return this.partiallyLoaded;
  }

  setPartiallyLoaded(partiallyLoaded) {
    this.partiallyLoaded = partiallyLoaded;
  }

  setContextPath(contextPath) {
    this.contextPath = contextPath;
  }

  getContextPath() {
    return this.contextPath;
  }

  getContextPathIds() {
    let path = this.getContextPath();
    if (path && path.length) {
      return this.getContextPath().map((item) => {
        return item.id;
      });
    }
    return [];
  }

  /**
   * Returns the semantic class of the object assuming that the last class in the semantic
   * hierarchy is the one that is needed.
   * @returns The semantic class string or undefined if semanticHierarchy is missing or empty.
   */
  getSemanticClass() {
    let semanticClass;
    let hierarchy = this.getPropertyValue('semanticHierarchy');
    if (hierarchy && hierarchy.length) {
      semanticClass = hierarchy[hierarchy.length - 1];
    }
    return semanticClass;
  }

  getPropertyValue(propertyName) {
    let validationModel = _.get(this, 'models.validationModel');
    if (validationModel && validationModel[propertyName]) {
      let value = validationModel[propertyName].value;
      // If its object property the value.results might be empty and only value.total to be set. This is how model is
      // loaded. By default instance service only returns total for persisted instances. For new ones results are
      // present in the model.
      let viewModel = this.getViewModelFieldById(propertyName);
      if (InstanceObject.isObjectProperty(viewModel)) {
        let normalizedValue = value || {};
        return new RelatedObject(normalizedValue);
      }
      return value;
    }
  }

  getPropertyValueByUri(propertyUri) {
    let viewModelField = this.getViewModelFieldByUri(propertyUri);
    let propertyName = viewModelField.identifier;
    return this.getPropertyValue(propertyName);
  }

  getViewModelFieldById(propertyName) {
    let fields = _.get(this, 'models.viewModel').fields;
    let result;
    ModelUtils.walkModelTree(fields, {}, (viewModelField) => {
      if (propertyName.indexOf(viewModelField.identifier) !== -1) {
        result = viewModelField;
      }
    });
    return result;
  }

  getViewModelFieldByUri(propertyUri) {
    let fields = _.get(this, 'models.viewModel').fields;
    let result;
    ModelUtils.walkModelTree(fields, {}, (viewModelField) => {
      if (propertyUri.indexOf(viewModelField.uri) !== -1) {
        result = viewModelField;
      }
    });
    return result;
  }

  /**
   * Sets the provided map of properties into the validation model of the instance. The properties are converted before
   * being stored in the validation model.
   *
   * This operation will be considered as a change set in the model!
   * If a property does not exist in the validation model it will not be set!
   *
   * @param properties - map of instance properties
   */
  setPropertiesValue(properties) {
    if (this.models.validationModel && properties) {
      let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
      Object.keys(properties).forEach((key) => {
        if (this.models.validationModel[key]) {
          let newValue = properties[key];
          InstanceObject.setIncomingPropertyValue(flatViewModelMap.get(key), this.models.validationModel[key], newValue);
        }
      });
    }
  }

  // We need to know whether an persisted object is creatable or uploadable in order to dispalay the correct templates. Persisted
  // uploadable objects will always have set value in emf:contentId and creatable won't. At the moment we don't have better
  // mechanism to differentiate between uploadable and creatable persisted objects.
  getPurpose() {
    return this.getPropertyValue('emf:contentId') ? 'uploadable' : 'creatable';
  }

  /**
   * Generates properties change set based on the validation and view model for an instance.
   *
   * @param forDraft - if true changed values are returned as is without modifications
   * @param validationModel {@link InstanceModel} wrapped validationModel
   * @param viewModel {@link DefinitionModel} wrapped viewModel
   * @returns {{}} a map with property name and propery value for all changed properties
   */
  static getModelChangeSet(forDraft, validationModel, viewModel) {
    let changeSet = {};
    if (!validationModel) {
      return changeSet;
    }
    let flatViewModelMap = ModelUtils.flatViewModel(viewModel);
    Object.keys(validationModel.serialize()).forEach((propertyName) => {
      let propertyViewModel = flatViewModelMap.get(propertyName);
      let propertyValidationModel = validationModel[propertyName];
      // Changeset for draft should be unchanged so it can be restored as is
      let propertyValues;
      if (forDraft) {
        propertyValues = propertyValidationModel;
      } else {
        propertyValues = InstanceObject.convertPropertyValues(propertyViewModel, propertyValidationModel);
      }

      let areEqual = angularAdapter.equals(propertyValues.defaultValue, propertyValues.value);
      // When value is cleared by the user through the UI or by condition assume the value is changed if the defaultValue
      // is defined which means the field initially had some value. Otherwise if the field didn't have value initially
      // then the user has completed the value, but later removed it again, then as result the value should not be
      // considered as changed because it was initially not defined: value=null and defaultValue=null
      if ((!propertyValues.value && typeof propertyValues.value !== 'number') && propertyValues.defaultValue) {
        // for different type values return:
        // string: null
        // object (like user): null
        // boolean (checkboxes): can not be erased from the UI - always true|false
        // date|datetime: null - dates are sent as strings so if deleted from UI, return nulls
        // !!!array (multiselect fields) - probably null?
        changeSet[propertyName] = null;
      } else if (!areEqual && (propertyValues.value || typeof propertyValues.value === 'number')) {
        if (forDraft) {
          changeSet[propertyName] = InstanceObject.restoreRichtextValue(propertyViewModel, propertyValidationModel);
        } else {
          changeSet[propertyName] = propertyValues.value;
        }
      }
    });
    return changeSet;
  }

  static restoreRichtextValue(propertyViewModel, propertyValidationModel) {
    if (ModelUtils.isRichtext(propertyViewModel)) {
      return propertyValidationModel.richtextValue;
    }
    return propertyValidationModel.value;
  }

  /**
   * Generates properties change set based on the current instance object validation and view model for this instance.
   *
   * @param forDraft - if true changed values are returned as is without modifications
   * @returns {{}} a map with property name and propery value for all changed properties
   */
  getChangeset(forDraft) {
    return InstanceObject.getModelChangeSet(forDraft, this.models.validationModel, this.models.viewModel);
  }

  /**
   * Iterates the model and returns true the first time when a changed property is found
   * @returns {boolean}
   */
  isChanged() {
    if (!this.models.validationModel) {
      return false;
    }
    let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
    let index = _.findIndex(Object.keys(this.models.validationModel.serialize()), (propertyName) => {
      let propertyViewModel = flatViewModelMap.get(propertyName);
      let propertyValidationModel = this.models.validationModel[propertyName];
      let propertyValues = InstanceObject.convertPropertyValues(propertyViewModel, propertyValidationModel);

      return !angularAdapter.equals(propertyValues.defaultValue, propertyValues.value)
        && (!InstanceObject.isNil(propertyValues.defaultValue) || !InstanceObject.isNil(propertyValues.value));
    });
    return index !== -1;
  }

  /**
   * Checks if the given model has any mandatory fields.
   *
   * @return true if there is any mandatory field, false otherwise.
   */
  hasMandatory() {
    return this.checkMandatory(this.models.viewModel);
  }

  checkMandatory(model) {
    return !model.fields ? model.isMandatory : model.fields.some((field) => {
        return this.checkMandatory(field);
      });
  }

  static isNil(value) {
    return value === null || value === undefined;
  }

  /**
   * Converts property values to format used when saving the instance.
   *
   *InstanceObjects are object properties when the user works with them, but only URIs are send to the server
   *Numeric data is a string when the user works with then,but it needs to be parsed into a number to the server.
   *
   * @param propertyViewModel
   * @param propertyValidationModel
   * @returns {{defaultValue: *, value: *}}
   */
  static convertPropertyValues(propertyViewModel, propertyValidationModel) {
    let defaultValue = propertyValidationModel.defaultValue;
    let value = propertyValidationModel.value;

    if (InstanceObject.isObjectProperty(propertyViewModel)) {
      defaultValue = InstanceObject.formatObjectPropertyValue(defaultValue);
      value = InstanceObject.formatObjectPropertyValue(value);
    } else if (InstanceObject.isNumericProperty(propertyViewModel) && value) {
      defaultValue = parseFloat(defaultValue);
      value = parseFloat(value);
    } else if (ModelUtils.isRichtext(propertyViewModel)) {
      value = propertyValidationModel.richtextValue;
      defaultValue = propertyValidationModel.defaultRichTextValue;
    }

    return {
      defaultValue,
      value
    };
  }

  /**
   * Converts and sets property value into validation model.
   * @param propertyViewModel
   * @param propertyValidationModel
   * @param newValue
   */
  static setIncomingPropertyValue(propertyViewModel, propertyValidationModel, newValue) {
    if (InstanceObject.isCodelistProperty(propertyViewModel)) {
      if (newValue instanceof Array) {
        let value = [];
        let valueLabel = [];
        newValue.forEach((item) => {
          value.push(item.id);
          valueLabel.push(item.text);
        });
        propertyValidationModel.value = value;
        propertyValidationModel.valueLabel = valueLabel.join(', ');
      } else if (InstanceObject.isPropertyValueObject(newValue)) {
        propertyValidationModel.value = newValue.id;
        propertyValidationModel.valueLabel = newValue.text;
      } else {
        propertyValidationModel.value = newValue;
      }
    } else if (ModelUtils.isRichtext(propertyViewModel)) {
      propertyValidationModel.richtextValue = newValue;
      propertyValidationModel.value = ModelUtils.stripHTML(newValue);
    } else {
      propertyValidationModel.value = newValue;
    }
  }

  static isObjectProperty(viewModel) {
    return viewModel && !viewModel.isDataProperty;
  }

  static isCodelistProperty(viewModel) {
    return viewModel && !!viewModel.codelist;
  }

  static isNumericProperty(viewModel) {
    return viewModel && NUMERIC_TYPES[viewModel.dataType];
  }

  static isPropertyValueObject(value) {
    return !!(value && value.id);
  }

  /**
   * Converts object property value into proper format to be send to the server.
   * Relations are stored in the model in JSON format used for storing instances. When sending to the server only their
   * ids are sent.
   * @param rawValue is the value or default value
   * @returns {*}
   */
  static formatObjectPropertyValue(rawValue) {
    if (rawValue) {
      return {
        add: rawValue.add || [],
        remove: rawValue.remove || []
      };
    } else {
      // If default value is undefined but user opens the picker and does not select an object then defaultValue will be
      // undefined and value will be {} and property will be considered changed.
      return {
        add: [],
        remove: []
      };
    }
  }

  updateLocalModel(updatedModel) {
    let serializedModel = this.models.validationModel.serialize();
    Object.keys(serializedModel).filter((propertyName) => {
      return updatedModel.hasOwnProperty(propertyName);
    }).forEach((propertyName) => {
      let isUpdatedProperty = (updatedModel[propertyName].value !== this.models.validationModel[propertyName].value);

      if (isUpdatedProperty) {
        this.models.validationModel[propertyName].value = updatedModel[propertyName].value;
      }
      // Clone in case that value is not primitive
      this.models.validationModel[propertyName].defaultValue = _.cloneDeep(serializedModel[propertyName].value);
      let isFieldWithLabel = this.models.validationModel[propertyName].valueLabel || updatedModel[propertyName].valueLabel;
      if (isFieldWithLabel) {
        this.models.validationModel[propertyName].valueLabel = updatedModel[propertyName].valueLabel;
      }
    });
  }

  /**
   * This is usually used to merge models after idoc save action where some object properties might be changed or
   * populated on the serverside. The other case of using is to populate the model with values when an object or batch
   * is loaded because the instance with its properties are loaded in parallel with different request.
   *
   * @param properties Is a map with properties values mapped to their properties names.
   */
  mergePropertiesIntoModel(properties) {
    if (this.models.validationModel) {
      // for easy access because the view is hierarchical
      let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
      Object.keys(this.models.validationModel.serialize()).forEach((propertyName) => {

        if (properties && properties[propertyName] !== undefined) {
          let newValue = properties[propertyName];
          InstanceObject.setIncomingPropertyValue(flatViewModelMap.get(propertyName), this.models.validationModel[propertyName], newValue);
        }

        // The defaultValue must be equal to value initially because when an instance is going to be saved the changeset
        // is built by comparing the value with the default value as the values are bound to form fields and can be
        // modified or deleted by the user.
        this.models.validationModel[propertyName].defaultValue = _.cloneDeep(this.models.validationModel[propertyName].value);
        if (this.models.validationModel[propertyName].valueLabel) {
          this.models.validationModel[propertyName].defaultValueLabel = _.cloneDeep(this.models.validationModel[propertyName].valueLabel);
        }
        if (this.models.validationModel[propertyName].richtextValue) {
          this.models.validationModel[propertyName].defaultRichTextValue = this.models.validationModel[propertyName].richtextValue;
        }

      });

      if (properties) {
        Object.keys(properties).forEach((propertyName) => {
          if (!this.models.validationModel[propertyName]) {
            // Add dummy property into validation model for properties which does not exist in the model
            this.models.validationModel[propertyName] = {
              value: properties[propertyName],
              defaultValue: properties[propertyName]
            };
          }
        });
      }
    }
  }

  /**
   * Headers are available in instance but not in model anymore so their values have to be filled in validation model.
   */
  mergeHeadersIntoModel(headers) {
    if (headers) {
      Object.keys(headers).forEach((header) => {
        if (!this.models.validationModel[header]) {
          this.models.validationModel[header] = {};
        }
        this.models.validationModel[header].value = headers[header];
        this.models.validationModel[header].defaultValue = _.cloneDeep(this.models.validationModel[header].value);
      });
    }
  }

  /**
   * Used when there are changes in a separated/cloned model that should be merged in the original model. This usually
   * happens if there is a cloned version of the model for example when idoc is saved and there is invalid data in an
   * object which causes the object details to be rendered in a window. For that window a cloned model is used in order
   * to allow the cancel operation to not require any sort of data revert but just dismiss the cloned model.
   *
   * @param newModel
   */
  mergeModelIntoModel(newModel) {
    Object.keys(this.models.validationModel.serialize()).forEach((propertyName) => {
      if (newModel.hasOwnProperty(propertyName) && newModel[propertyName].value !== this.models.validationModel[propertyName].value) {
        this.models.validationModel[propertyName].value = newModel[propertyName].value;
        this.models.validationModel[propertyName].richtextValue = newModel[propertyName].richtextValue;
      }
    });
  }

  /**
   * Reverts any changes to the value and valueLabel(if the property has one) to their default values.
   * @param validationModel {@link InstanceModel} wrapped validationModel
   */
  static revertModelChanges(validationModel) {
    Object.keys(validationModel.serialize()).forEach((propertyName) => {
      InstanceObject.assignDefaultValue(validationModel[propertyName], 'value', 'defaultValue');

      if (validationModel[propertyName].richtextValue) {
        InstanceObject.assignDefaultValue(validationModel[propertyName], 'richtextValue', 'defaultRichTextValue');
      }
      if (validationModel[propertyName].defaultValueLabel) {
        InstanceObject.assignDefaultValue(validationModel[propertyName], 'valueLabel', 'defaultValueLabel');
      }
    });
  }

  revertChanges() {
    InstanceObject.revertModelChanges(this.models.validationModel);
  }

  static assignDefaultValue(propertyModel, valueKey, defaultValueKey) {
    let clonedDefaultValue = _.cloneDeep(propertyModel[defaultValueKey]);
    if (propertyModel[valueKey] instanceof Array) {
      propertyModel[valueKey].splice(0);
      if (clonedDefaultValue) {
        propertyModel[valueKey].push(...clonedDefaultValue);
      }
    } else {
      propertyModel[valueKey] = clonedDefaultValue;
    }
  }

  isVersion() {
    return !!this.getPropertyValue('isVersion');
  }
}