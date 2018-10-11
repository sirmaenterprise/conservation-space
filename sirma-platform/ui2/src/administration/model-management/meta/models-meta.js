import {ModelMetaData} from 'administration/model-management/meta/model-meta';

/**
 * Provides a description of a meta data which is used as a reference point.
 * Meta data is not associated with any given model it currently supports a
 * collection of meta data for semantic and definition attributes. Models
 * Meta data also contains a collection of meta data for semantic properties
 *
 * @author Svetlozar Iliev
 */
export class ModelsMetaData {

  constructor() {
    this.fields = {};
    this.regions = {};
    this.semantics = {};
    this.properties = {};
    this.definitions = {};
  }

  addSemantic(semanticMetaData) {
    if (semanticMetaData instanceof ModelMetaData) {
      this.semantics[semanticMetaData.getId()] = semanticMetaData;
    }
    return this;
  }

  addDefinition(definitionMetaData) {
    if (definitionMetaData instanceof ModelMetaData) {
      this.definitions[definitionMetaData.getId()] = definitionMetaData;
    }
    return this;
  }

  addProperty(propertyMetaData) {
    if (propertyMetaData instanceof ModelMetaData) {
      this.properties[propertyMetaData.getId()] = propertyMetaData;
    }
    return this;
  }

  addField(fieldMetaData) {
    if (fieldMetaData instanceof ModelMetaData) {
      this.fields[fieldMetaData.getId()] = fieldMetaData;
    }
    return this;
  }

  addRegion(regionMetaData) {
    if (regionMetaData instanceof ModelMetaData) {
      this.regions[regionMetaData.getId()] = regionMetaData;
    }
    return this;
  }

  getSemantics() {
    return this.semantics;
  }

  getDefinitions() {
    return this.definitions;
  }

  getProperties() {
    return this.properties;
  }

  getFields() {
    return this.fields;
  }

  getRegions() {
    return this.regions;
  }

  seal() {
    this.sealMetaDataMap(this.fields);
    this.sealMetaDataMap(this.regions);
    this.sealMetaDataMap(this.semantics);
    this.sealMetaDataMap(this.properties);
    this.sealMetaDataMap(this.definitions);

    // Using Object.freeze() to avoid reassigning existing fields which Object.seal() won't prevent.
    Object.freeze(this);
  }

  sealMetaDataMap(map) {
    Object.keys(map).forEach(key => map[key].seal());
  }

}