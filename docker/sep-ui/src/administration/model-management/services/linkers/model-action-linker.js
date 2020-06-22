import {Inject, Injectable} from 'app/app';
import {ModelAction} from 'administration/model-management/model/model-action';

import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';

/**
 * Service which builds and links {@link ModelAction}s to a given {@link ModelDefinition} model.
 *
 * @author B.Tonchev
 */
@Injectable()
@Inject(ModelAttributeLinker, ModelDescriptionLinker)
export class ModelActionLinker {

  constructor(modelAttributeLinker, modelDescriptionLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  linkActions(model, modelActions, meta) {
    let actions = modelActions || [];
    actions.forEach(action => {
      let actionModel = ModelActionLinker.constructActionModel(action);
      this.modelAttributeLinker.linkAttributes(actionModel, action.attributes, meta);
      this.modelDescriptionLinker.insertDescriptions(actionModel, action.labels);
      actionModel.setParent(model);
      model.addAction(actionModel);
    });
  }

  static constructActionModel(action) {
    let actionModel = new ModelAction(action.id);
    actionModel.setInherited(false);
    return actionModel;
  }
}