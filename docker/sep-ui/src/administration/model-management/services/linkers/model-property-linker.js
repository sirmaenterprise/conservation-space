import {Injectable, Inject} from 'app/app';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';

const URI = ModelAttributeTypes.SINGLE_VALUE.MODEL_URI_TYPE;

/**
 * Service which builds and links properties to a given model. Model is required to support properties
 * in order to be properly linked. Created properties linked to the model are of type {@link ModelProperty}
 * or any of the types which might extend off of it.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelAttributeLinker, ModelDescriptionLinker)
export class ModelPropertyLinker {

  constructor(modelAttributeLinker, modelDescriptionLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  /**
   * Links the specified model to a semantic property. The provided map of properties is
   * used to determine which property links to which model.
   *
   * The model is required to have an attribute of type URI which is used to determine
   * the proper property identifier and the actual property the model is linked to.
   *
   * When a model has no labels associated with it but has a property associated with it
   * the labels from the property would be directly deep copied to the model.
   *
   * When no property and no labels are associated with the provided model a default label
   * based on the model identifier is assigned.
   *
   * @param model - the model for which to link a property
   * @param properties - map of model properties, mapped by the property's semantic URI.
   */
  linkPropertiesToFieldModel(model, properties) {
    let uri = model.getAttributeByType(URI);
    // attach the semantic property if it is present based on uri attribute
    model.setProperty(uri ? properties.getModel(uri.getValue().value) : null);
  }

  /**
   * Links semantic properties that belong to a given semantic class model. Such properties
   * are those which fall in the same domain as the provided model class.
   *
   * Properties are linked based on a simple rule which is that all properties which have a
   * parent matching the given model class are considered to be part of the domain this class
   * represents and thus are linked and attached to that model class. Further processing might
   * be introduced in the future to deduce and infer more complex relationships between class
   * and property models.
   *
   * @param model - the model for which to link the properties
   * @param properties - {@link ModelList} of semantic properties
   * @returns - the given semantic model
   */
  linkPropertiesToClassModel(model, properties) {
    // filter all properties which are located in the domain of the current class model
    let owned = properties.getModels().filter(property => property.getParent() === model);
    owned.forEach(property => model.addProperty(property));
    return model;
  }

  /**
   * Creates a model property from a provided optional meta model which contains the meta
   * data information needed to construct a basic model property. The property is created and
   * is not assigned to any owner or parent. Both input parameters are optional depending
   * on the caller's requirements for the constructed model property.
   *
   * @param id - the id of the property to be created, can be left empty or null
   * @param meta - the meta data for the property, can be left empty or null
   * @returns {@link ModelProperty} - created instance of model property
   */
  createModelProperty(id, meta) {
    let propertyModel = this.constructPropertyModel({id});
    this.createAttributes(propertyModel, meta);
    this.linkDescriptions(propertyModel, {});
    return propertyModel;
  }

  createAttributes(model, meta) {
    this.modelAttributeLinker.createAttributes(model, [], meta);
  }

  linkAttributes(model, attributes, meta) {
    this.modelAttributeLinker.linkAttributes(model, attributes, meta);
  }

  linkDescriptions(model, labels) {
    this.modelDescriptionLinker.insertDescriptions(model, labels);
  }

  constructPropertyModel(data) {
    return new ModelProperty(data.id);
  }
}
