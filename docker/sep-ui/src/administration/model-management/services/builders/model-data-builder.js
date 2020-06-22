import {Inject, Injectable} from 'app/app';
import {ModelProperty} from 'administration/model-management/model/model-property';

import {ModelDataLinker} from 'administration/model-management/services/linkers/model-data-linker';
import {ModelFieldLinker} from 'administration/model-management/services/linkers/model-field-linker';
import {ModelRegionLinker} from 'administration/model-management/services/linkers/model-region-linker';

import {ModelHeaderLinker} from 'administration/model-management/services/linkers/model-header-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';

import {ModelActionGroupLinker} from 'administration/model-management/services/linkers/model-action-group-linker';
import {ModelActionLinker} from 'administration/model-management/services/linkers/model-action-linker';
import {ModelActionExecutionLinker} from 'administration/model-management/services/linkers/model-action-execution-linker';

/**
 * Service which builds and links all models provided as arguments to its builder function. Regions, fields and attributes
 * are properly constructed and linked to their respective models as provided in the arguments.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelDataLinker, ModelFieldLinker, ModelRegionLinker, ModelAttributeLinker, ModelActionGroupLinker, ModelActionLinker, ModelActionExecutionLinker, ModelHeaderLinker)
export class ModelDataBuilder {

  constructor(modelDataLinker, modelFieldLinker, modelRegionLinker, modelAttributeLinker, modelActionGroupLinker, modelActionLinker, modelActionExecutionLinker, modelHeaderLinker) {
    this.modelDataLinker = modelDataLinker;
    this.modelFieldLinker = modelFieldLinker;
    this.modelRegionLinker = modelRegionLinker;
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelActionGroupLinker = modelActionGroupLinker;
    this.modelActionLinker = modelActionLinker;
    this.modelActionExecutionLinker = modelActionExecutionLinker;
    this.modelHeaderLinker = modelHeaderLinker;
  }

  /**
   * Builds all links and references between a given model and related data. Further
   * links or references might also be internally inferred by the inheritance model.
   *
   * @param model - model for which to build links
   * @param models - data source store of type {@link ModelStore}
   */
  buildModelLinks(model, models) {
    if (!model || !models) {
      return;
    }
    let properties = models.getModelsList(ModelProperty);
    this.modelDataLinker.linkInheritanceModel(model, properties);
  }

  /**
   * Builds all models provided with the arguments of this method. This method converts the provided
   * response and meta data to the proper model and API deriving from {@link ModelBase} and related
   * types.
   *
   * @param models - a special models store which is of type {@link ModelStore}
   * @param meta - set of semantic or definition meta data instance of {@link ModelsMetaData}
   * @param data - set of semantic or definition models and related data provided by a restful service
   */
  buildModels(models, meta, data) {
    // load all of the class models
    data.classes.forEach(clazz => {
      let model = models.getModel(clazz.id);
      if (!model.isLoaded()) {
        this.modelAttributeLinker.linkAttributes(model, clazz.attributes, meta.getSemantics());
        model.setLoaded(true);
      }
    });

    // load all the definition models
    data.definitions.forEach(def => {
      let model = models.getModel(def.id);
      if (!model.isLoaded()) {
        this.modelAttributeLinker.linkAttributes(model, def.attributes, meta.getDefinitions());
        this.modelRegionLinker.linkRegions(model, def.regions, meta.getRegions());
        this.modelFieldLinker.linkFields(model, def.fields, meta);
        this.modelActionGroupLinker.linkActionGroups(model, def.actionGroups, meta.getActionGroups());
        this.modelActionLinker.linkActions(model, def.actions, meta.getActions());
        this.modelActionExecutionLinker.linkActionExecutions(model, def.actions, meta.getActionExecutions());
        this.modelHeaderLinker.linkHeaders(model, def.headers, meta.getHeaders());
        model.setLoaded(true);
      }
    });
  }
}