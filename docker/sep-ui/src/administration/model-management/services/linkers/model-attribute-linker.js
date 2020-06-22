import {Inject, Injectable} from 'app/app';
import {ModelValuesLinker} from './model-values-linker';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';

const CREATE_DEFAULT_VALUE_PURPOSE = ModelAttributeMetaData.DEFAULT_VALUE_PURPOSE.CREATE;
const MISSING_DEFAULT_VALUE_PURPOSE = ModelAttributeMetaData.DEFAULT_VALUE_PURPOSE.MISSING;

/**
 * Service which builds and links attributes to a given model. Model is required to support attributes
 * in order to be properly linked. Created attributes linked to the model are of  type {@link ModelAttribute}
 * or any of the types which might extend off of it.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelValuesLinker)
export class ModelAttributeLinker {

  constructor(modelValuesLinker) {
    this.modelValuesLinker = modelValuesLinker;
  }

  /**
   * Links a collection of attributes provided by a restful response to the given model. The model can be
   * of any type, as long as it supports attributes. The Provided meta data is additionally linked with
   * each model attribute it belongs too.
   *
   * @param model - any model which is supporting attributes
   * @param attributes - list of attributes data provided by a service
   * @param meta - model meta data to be linked with the attributes
   */
  linkAttributes(model, attributes, meta) {
    // constructs & attaches set of attributes based on meta data and specific purpose
    this.constructAttributes(model, attributes, meta, MISSING_DEFAULT_VALUE_PURPOSE);
  }

  /**
   * Creates and attaches to a given model a collection of attributes based on the provided meta model.
   * Attributes to be created are strictly defined by the meta model provided to this method as a parameter.
   * Created attributes are then sored based on the order provided by the meta model.
   *
   * @param model - the model for which to create the collection of attributes
   * @param attributes - optional array of external attributes data to create
   * @param meta - meta model based on which to create the attributes
   */
  createAttributes(model, attributes, meta) {
    // constructs & attaches set of attributes based on meta data and specific purpose
    this.constructAttributes(model, attributes, meta, CREATE_DEFAULT_VALUE_PURPOSE);
  }

  constructAttributes(model, attributes, meta, purpose) {
    // core attributes based on data
    attributes.forEach(attribute => {
      let metaModel = meta.getModel(attribute.name || attribute.id);
      this.constructAttribute(attribute, metaModel, model, ModelAttribute.SOURCE.MODEL_DATA);
    });

    // add missing attributes from meta data
    meta.getModels().forEach(metaModel => {
      let attribute = this.extractAttributeData(metaModel, purpose);
      this.constructAttribute(attribute, metaModel, model, ModelAttribute.SOURCE.META_DATA);
    });

    // re-order attributes properly
    this.sortModelAttributes(model);
  }

  constructAttribute(data, meta, model, source) {
    // skip existing attributes or attributes which are not visible
    if (!!model.getAttribute(meta.getId()) || !this.isVisible(meta)) {
      return null;
    }

    // create attribute model based on the provided data;
    let attrModel = this.constructAttributeModel(data);
    attrModel.setMetaData(meta).setParent(model).setSource(source);
    this.resolveAttributeRestrictions(attrModel);

    // insert created attribute
    model.addAttribute(attrModel);
    return attrModel;
  }

  sortModelAttributes(model) {
    // Sort by meta model's order
    model.getAttributes().sort((attr1, attr2) => {
      // collect meta data from attributes
      let attr1Meta = attr1.getMetaData();
      let attr2Meta = attr2.getMetaData();

      // collect order from the meta data
      let attr1Order = attr1Meta ? attr1Meta.getOrder() : 0;
      let attr2Order = attr2Meta ? attr2Meta.getOrder() : 0;

      // execute comparison of orders
      return attr1Order - attr2Order;
    });
  }

  extractAttributeData(metaModel, purpose) {
    return {
      name: metaModel.getId(),
      type: metaModel.getType(),
      value: metaModel.getDefaultValue(purpose)
    };
  }

  constructAttributeModel(data) {
    let attribute = this.resolveAttributeModel(data);
    let value = this.resolveAttributeValue(data);
    this.insertValues(attribute, value);
    return attribute;
  }

  resolveAttributeModel(data) {
    let attribute = ModelAttributeTypes.isMultiValued(data.type) ? new ModelMultiAttribute(data.name) : new ModelSingleAttribute(data.name);
    attribute.setType(data.type);
    return attribute;
  }

  resolveAttributeValue(data) {
    // try to use provided value
    if (data.value !== undefined) {
      return data.value;
    }
    // use default fallback value when none is provided
    return ModelAttributeTypes.getDefaultValue(data.type);
  }

  resolveAttributeRestrictions(attribute) {
    attribute.getRestrictions().copyFrom(attribute.getMetaData().getValidationModel().getRestrictions());
  }

  insertValues(attribute, values) {
    this.modelValuesLinker.insertValues(attribute, values);
  }

  isVisible(metaData) {
    return metaData.getValidationModel().getRestrictions().isVisible();
  }
}