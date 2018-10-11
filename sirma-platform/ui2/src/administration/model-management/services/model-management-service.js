import {Inject, Injectable} from 'app/app';
import {Configuration} from 'common/application-config';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelManagementRestService} from './model-management-rest-service';

import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelDescription, ModelValue} from 'administration/model-management/model/model-value';
import {
  ModelMultiAttribute,
  ModelSingleAttribute,
  ModelAttributeTypes
} from 'administration/model-management/model/model-attribute';
import {ModelClassHierarchy, ModelDefinitionHierarchy} from 'administration/model-management/model/model-hierarchy';

import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {ModelFieldMetaData} from 'administration/model-management/meta/model-field-meta';
import {ModelRegionMetaData} from 'administration/model-management/meta/model-region-meta';
import {ModelPropertyMetaData} from 'administration/model-management/meta/model-property-meta';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelValidation} from 'administration/model-management/validation/model-validation';

import _ from 'lodash';

const DEFAULT_LANGUAGE = 'EN';
const CLASS_ICON = 'fa fa-fw fa-home';
const DEFINITION_ICON = 'fa fa-fw fa-cog';

const URI = ModelAttributeTypes.SINGLE_VALUE.MODEL_URI_TYPE;
const LABEL = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;

/**
 * Administration service for managing models in the system.
 * Responsible for converting models into proper structure for the web application.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementRestService, PromiseAdapter, Configuration, TranslateService)
export class ModelManagementService {

  constructor(modelManagementRestService, promiseAdapter, configuration, translateService) {
    this.configuration = configuration;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.modelManagementRestService = modelManagementRestService;
  }

  getModel(id, provider) {
    let model = provider(id);

    if (!model) {
      return this.promiseAdapter.reject();
    } else if (!model.isLoaded()) {
      return this.promiseAdapter.all([this.getMetaData(), this.modelManagementRestService.getModelData(id)]).then(response => {
        let meta = response[0];
        let data = response[1];

        // load all of the class models
        data.classes.forEach(clazz => {
          let model = provider(clazz.id);
          if (!model.isLoaded()) {
            this.linkAttributes(model, clazz.attributes, meta.getSemantics());
            model.setLoaded(true);
          }
        });

        // load all the definition models
        data.definitions.forEach(def => {
          let model = provider(def.id);
          if (!model.isLoaded()) {
            this.linkAttributes(model, def.attributes, meta.getDefinitions());
            this.linkRegions(model, def.regions, meta.getRegions());
            this.linkFields(model, def.fields, meta.getFields());
            model.setLoaded(true);
          }
        });

        // signal that model data is loaded
        return model.setLoaded(true) && model;
      });
    } else {
      return this.promiseAdapter.resolve(model);
    }
  }

  getMetaData(forceReload = false) {
    if (!this.modelsMetaData || forceReload) {
      return this.modelManagementRestService.getModelsMetaData().then(meta => {
        this.modelsMetaData = new ModelsMetaData();
        meta.fields.forEach(field => this.modelsMetaData.addField(this.constructFieldData(field)));
        meta.regions.forEach(region => this.modelsMetaData.addRegion(this.constructRegionData(region)));
        meta.properties.forEach(prop => this.modelsMetaData.addProperty(this.constructPropertyData(prop)));
        meta.semantics.forEach(clazz => this.modelsMetaData.addSemantic(this.constructAttributeData(clazz)));
        meta.definitions.forEach(def => this.modelsMetaData.addDefinition(this.constructAttributeData(def)));
        this.modelsMetaData.seal();
        return this.modelsMetaData;
      });
    } else {
      return this.promiseAdapter.resolve(this.modelsMetaData);
    }
  }

  getHierarchy() {
    return this.modelManagementRestService.getModelsHierarchy().then(hierarchy => {
      let tree = this.constructClassHierarchy(hierarchy);
      let flat = this.constructFlatHierarchy(tree);
      this.linkHierarchy(flat);
      return {tree, flat};
    });
  }

  getProperties() {
    return this.promiseAdapter.all([this.getMetaData(), this.modelManagementRestService.getModelProperties()]).then(response => {
      let result = {};
      let properties = response[1];
      let meta = response[0].getProperties();

      properties.forEach(property => {
        let model = this.constructPropertyModel(property);
        this.linkAttributes(model, property.attributes, meta);
        result[model.getId()] = model;
      });
      return result;
    });
  }

  linkHierarchy(hierarchy) {
    Object.keys(hierarchy).forEach(key => {
      let model = hierarchy[key].getRoot();

      let parent = model.getParent && model.getParent();
      if (parent && !(parent instanceof ModelBase)) {
        // link current model with it's parent model
        model.setParent(hierarchy[parent].getRoot());
      }

      let type = model.getType && model.getType();
      if (type && !(type instanceof ModelBase)) {
        // link current model with it's model type
        model.setType(hierarchy[type].getRoot());
      }
    });
  }

  linkModel(model, properties) {
    // get current and the parent models in reverse order
    let models = [model, ...model.getParents()].reverse();

    models.forEach(model => {
      let parent = model.getParent();
      if (this.isModelDefinition(model)) {
        // when the model is the root directly link it's properties
        let models = parent ? parent.getModels() : model.getModels();
        models && this.linkInheritedModels(model, models, properties);

        // link inherited attributes from the parent model to the current model
        parent && this.linkInheritedAttributes(model, parent.getAttributes());

        let type = model.getType();
        // link labels with descriptions for the semantic type of the definition
        type && this.linkLabelAttribute(type, type.getAttributeByType(LABEL));
      }
      // link the label attribute to the actual model description of the model
      model && this.linkLabelAttribute(model, model.getAttributeByType(LABEL));
    });
    this.isModelDefinition(model) && this.linkInheritedModels(model, model.getModels(), properties);
  }

  linkInheritedModels(model, inherited, properties) {
    inherited.forEach(item => {
      // try to add current item as a field first to the model
      let inserted = this.addFieldIfNotPresentAndGet(model, item);
      let shouldLinkProperties = inserted && !inserted.getProperty();

      if (item && !inserted) {
        // inserted item was not a field try to insert as a region
        inserted = this.addRegionIfNotPresentAndGet(model, item);
        // for the region traverse it's fields and link them separately
        this.linkInheritedModels(inserted, item.getFields(), properties);
      }

      if (inserted !== item) {
        // current item is parent of the inserted append attributes
        this.linkInheritedAttributes(inserted, item.getAttributes());
      }

      // link property if inserted item is a field and not linked already
      shouldLinkProperties && this.linkProperties(inserted, properties);
      this.linkLabelAttribute(inserted, inserted.getAttributeByType(LABEL));
    });
  }

  linkInheritedAttributes(model, inherited) {
    inherited.forEach(attribute => this.addAttributeIfNotPresentAndGet(model, attribute));
  }

  linkRegions(model, regions, meta) {
    regions.forEach(region => {
      let regionModel = new ModelRegion(region.id);
      this.linkAttributes(regionModel, region.attributes, meta);

      model.addRegion(regionModel);
      regionModel.setParent(model);
    });
  }

  linkFields(model, fields, meta) {
    fields.forEach(field => {
      // Create model field and link related meta info
      let fieldModel = this.constructFieldModel(field);
      this.linkAttributes(fieldModel, field.attributes, meta);
      fieldModel.setParent(model);

      if (!field.regionId) {
        // add field as base field
        model.addField(fieldModel);
      } else {
        // add field to the given region fields
        let region = model.getRegion(field.regionId);
        region.addField(fieldModel);
      }
    });
  }

  linkProperties(model, properties) {
    let uri = model.getAttributeByType(URI);
    let label = model.getAttributeByType(LABEL);
    let hasName = !_.isEmpty(label.getValues());

    let property = uri ? properties[uri.getValue().value] : null;
    model.setProperty(property);

    if (property && !hasName) {
      let title = property.getAttributeByType(LABEL);
      // copy references to the label attr
      label.setValue(title.getValue());
      label.setValues(title.getValues());
      //TODO: decide what to do w empty label
    } else if (!property && !hasName) {
      // fallback label for description
      this.insertDescriptions(model, {});
      // copy references to the label attr
      label.setValue(model.getDescription());
      label.setValues(model.getDescriptions());
    }
  }

  linkAttributes(model, attributes, meta) {
    attributes.forEach(attribute => {
      // Create attribute and link related meta info
      let attrModel = this.constructAttributeModel(attribute);
      attrModel.setMetaData(meta[attribute.name || attribute.id]);

      // insert created attribute
      model.addAttribute(attrModel);
      attrModel.setParent(model);
    });
  }

  linkLabelAttribute(model, attribute) {
    //link attribute with the model data
    model.setDescription(attribute.getValue());
    model.setDescriptions(attribute.getValues());
  }

  constructClassHierarchy(classes, root) {
    let roots = [];
    classes && classes.forEach(clazz => {
      let process = false;
      let node = new ModelClassHierarchy();

      if (!root && this.isRoot(classes, clazz)) {
        process = true;
        roots.push(node);
      } else if (root && clazz.parentId === root.getRoot().getId()) {
        process = true;
        root.insertChild(node);
      }

      if (process) {
        let modelClass = new ModelClass(clazz.id, clazz.parentId);
        modelClass.setIcon(CLASS_ICON);

        node.setParent(root);
        node.setRoot(modelClass);
        node.insertChildren(this.constructDefinitionHierarchy(modelClass, clazz));

        this.insertDescriptions(modelClass, clazz.labels);
        this.constructClassHierarchy(classes, node);
      }
    });
    return roots;
  }

  constructDefinitionHierarchy(model, clazz, root) {
    let roots = [];
    clazz.subTypes && clazz.subTypes.forEach(type => {
      let process = false;
      let node = new ModelDefinitionHierarchy();

      if (!root && this.isRoot(clazz.subTypes, type)) {
        process = true;
        roots.push(node);
      } else if (root && type.parentId === root.getRoot().getId()) {
        process = true;
        root.insertChild(node);
      }

      if (process) {
        let modelDefinition = new ModelDefinition(type.id, type.parentId, type.abstract, model.getId());
        modelDefinition.setIcon(DEFINITION_ICON);

        node.setParent(root);
        node.setRoot(modelDefinition);

        this.insertDescriptions(modelDefinition, type.labels);
        this.constructDefinitionHierarchy(model, clazz, node);
      }
    });
    return roots;
  }

  constructFlatHierarchy(hierarchy, flat = {}) {
    if (Array.isArray(hierarchy)) {
      hierarchy.forEach(node => this.constructFlatHierarchy(node, flat));
    } else {
      flat[hierarchy.getRoot().getId()] = hierarchy;
      hierarchy.getChildren().forEach(child => this.constructFlatHierarchy(child, flat));
    }
    return flat;
  }

  constructPropertyModel(data) {
    return new ModelProperty(data.id);
  }

  constructFieldModel(data) {
    return new ModelField(data.id);
  }

  constructAttributeModel(data) {
    let attribute = this.isMultiValued(data.type) ? new ModelMultiAttribute(data.name) : new ModelSingleAttribute(data.name);
    attribute.setType(data.type).setValidation(new ModelValidation());
    this.insertValues(attribute, data.value);
    return attribute;
  }

  constructAttributeData(data) {
    let id = data.uri || data.id || data.name;
    let metaData = new ModelAttributeMetaData(id);
    return this.attachMetaData(metaData, data);
  }

  constructPropertyData(data) {
    let metaData = new ModelPropertyMetaData(data.uri);
    return this.attachMetaData(metaData, data);
  }

  constructFieldData(data) {
    let metaData = new ModelFieldMetaData(data.id);
    return this.attachMetaData(metaData, data);
  }

  constructRegionData(data) {
    let metaData = new ModelRegionMetaData(data.id);
    return this.attachMetaData(metaData, data);
  }

  attachMetaData(model, data) {
    model.setType(data.type).setDefaultValue(data.defaultValue);
    model.getValidationRules().setMandatory(data.validationModel.mandatory);
    this.insertDescriptions(model, data.labels);
    return model;
  }

  insertDescriptions(model, labels) {
    let languages = labels && Object.keys(labels);

    if (!labels || !languages.length) {
      model.addDescription(new ModelDescription(DEFAULT_LANGUAGE, model.getId()));
    } else {
      languages.forEach(key => model.addDescription(new ModelDescription(this.transformLanguage(key), labels[key])));
    }
    model.setDescription(this.getModelDescription(model));
  }

  insertValues(model, values) {
    let languages = model.getType && this.isMultiValued(model.getType()) && Object.keys(values);

    if (!languages || !languages.length) {
      model.setValue(new ModelValue().setValue(values));
    } else {
      languages.forEach(key => model.addValue(new ModelValue(this.transformLanguage(key), values[key])));
      model.setValue(this.getModelValue(model));
    }
  }

  addRegionIfNotPresentAndGet(model, region) {
    let isOldRegion = false;
    let parent = region.getParent();
    let currentRegion = this.getRegion(model, region);

    if (parent) {
      let parentRegion = this.getRegion(parent, region);
      isOldRegion = (currentRegion === parentRegion) && (currentRegion !== region);
    }

    if (!currentRegion || isOldRegion) {
      model.addRegion(region);
      currentRegion = model.getRegion(region.getId());
    }
    return currentRegion;
  }

  addFieldIfNotPresentAndGet(model, field) {
    let isOldField = false;
    let parent = field.getParent();
    let currentField = this.getField(model, field);

    if (parent) {
      let parentField = this.getField(parent, field);
      isOldField = (currentField === parentField) && (currentField !== field);
    }

    if (!currentField || isOldField) {
      model.addField(field);
      currentField = model.getField(field.getId());
    }
    return currentField;
  }

  addAttributeIfNotPresentAndGet(model, attribute) {
    let isEmptyValue = false;
    let isOldAttribute = false;

    let parent = attribute.getParent();
    let currentAttr = this.getAttribute(model, attribute);

    if (parent) {
      let parentAttr = this.getAttribute(parent, attribute);
      isOldAttribute = (currentAttr === parentAttr) && (currentAttr !== attribute);
    }

    if (currentAttr) {
      let actualValue = currentAttr.getValue().value;
      isEmptyValue = _.isUndefined(actualValue) || _.isNull(actualValue) ||
        ((_.isObject(actualValue) || _.isString(actualValue)) && _.isEmpty(actualValue));
    }

    if (!currentAttr || isEmptyValue || isOldAttribute) {
      model.addAttribute(attribute);
      currentAttr = model.getAttribute(attribute.getId());
    }
    return currentAttr;
  }

  getAttribute(model, attribute) {
    return model.getAttribute(attribute.getId());
  }

  getRegion(model, region) {
    return model.getRegion(region.getId());
  }

  getField(model, field) {
    model = this.getOwningModel(model);
    // look for the field on base level first
    let found = model.getField(field.getId());

    if (!found) {
      // traverse all regions to find field
      model.getModels().some(model => {
        if (model instanceof ModelRegion) {
          found = model.getField(field.getId());
          return !!found;
        }
      });
    }
    return found;
  }

  getOwningModel(model) {
    if (!model) {
      return;
    }

    if (!(model instanceof ModelClass || model instanceof ModelDefinition)) {
      return this.getOwningModel(model.getParent());
    }
    return model;
  }

  getModelValue(model) {
    return this.getModelPropertyByLanguage(model, 'getValueByLanguage');
  }

  getModelDescription(model) {
    return this.getModelPropertyByLanguage(model, 'getDescriptionByLanguage');
  }

  getModelPropertyByLanguage(model, property) {
    let userLanguage = this.transformLanguage(this.translateService.getCurrentLanguage());
    let systemLanguage = this.transformLanguage(this.configuration.get(Configuration.SYSTEM_LANGUAGE));
    return model[property](userLanguage) || model[property](systemLanguage) || model[property](DEFAULT_LANGUAGE);
  }

  transformLanguage(lang) {
    return lang && lang.toUpperCase();
  }

  isRoot(collection, element) {
    return !element.parentId || _.every(collection, item => element.parentId !== item.id);
  }

  isLabel(attribute) {
    return ModelAttributeTypes.isLabel(attribute.getType());
  }

  isMultiValued(type) {
    return ModelAttributeTypes.isMultiValued(type);
  }

  isModelDefinition(model) {
    return model instanceof ModelDefinition;
  }
}