import {Inject, Injectable} from 'app/app';
import {ModelAttributeLinker} from './model-attribute-linker';
import {ModelDescriptionLinker} from './model-description-linker';
import {ModelRegion} from 'administration/model-management/model/model-region';

/**
 * Service which builds and links regions to a given model. Model is required to support regions
 * in order to be properly linked. Created regions linked to the model are of type {@link ModelRegion}
 * or any of the types which might extend off of it.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelAttributeLinker, ModelDescriptionLinker)
export class ModelRegionLinker {

  constructor(modelAttributeLinker, modelDescriptionLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  /**
   * Links a collection of regions provided by a restful response to the given model. The model can be
   * of any type, as long as it supports regions. The Provided meta data is additionally linked with
   * each model regions it belongs too.
   *
   * @param model - any model which is supporting regions
   * @param regions - list of regions provided by a restful service
   * @param meta - map of model meta data to be linked with the regions based on an identifier
   */
  linkRegions(model, regions, meta) {
    regions.forEach(region => {
      let regionModel = this.constructRegionModel(region);
      this.linkAttributes(regionModel, region.attributes, meta);
      this.linkDescriptions(regionModel, region.labels);

      model.addRegion(regionModel);
      regionModel.setParent(model);
    });
  }

  linkAttributes(model, attributes, meta) {
    this.modelAttributeLinker.linkAttributes(model, attributes, meta);
  }

  linkDescriptions(model, labels) {
    this.modelDescriptionLinker.insertDescriptions(model, labels);
  }

  constructRegionModel(data) {
    return new ModelRegion(data.id);
  }
}