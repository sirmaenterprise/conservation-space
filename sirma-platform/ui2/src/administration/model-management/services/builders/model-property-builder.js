import {Inject, Injectable} from 'app/app';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';

/**
 * Service which builds a map of semantic properties. Properties are of type {@link ModelProperty} and
 * are mapped in an object by their unique identifier - URI.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelAttributeLinker, ModelDescriptionLinker)
export class ModelPropertyBuilder {

  constructor(modelAttributeLinker, modelDescriptionLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  /**
   * Builds a map of model properties of type {@link ModelProperty} mapped by their URI
   *
   * @param properties - collection of properties provided by a restful service
   * @param meta - model property meta data to be mapped with the created properties.
   * @returns {uri: ModelProperty} - the mapped model properties in an object
   */
  buildProperties(properties, meta) {
    let result = new ModelList();

    properties.forEach(property => {
      // create the actual semantic property
      let model = new ModelProperty(property.id);
      this.linkAttributes(model, property.attributes, meta);
      this.linkDescriptions(model, property.labels);

      // set the semantic domain as a parent of the current property
      let domain = model.getAttribute(ModelAttribute.DOMAIN_ATTRIBUTE);
      domain && model.setParent(domain.getValue().getValue());

      // append to result
      result.insert(model);
    });
    return result;
  }

  linkAttributes(model, attributes, meta) {
    this.modelAttributeLinker.linkAttributes(model, attributes, meta);
  }

  linkDescriptions(model, labels) {
    this.modelDescriptionLinker.insertDescriptions(model, labels);
  }
}