import {Inject, Injectable} from 'app/app';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';

import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';

@Injectable()
@Inject(ModelAttributeLinker, ModelDescriptionLinker)
export class ModelActionGroupLinker {

  constructor(modelAttributeLinker, modelDescriptionLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  linkActionGroups(model, modelActionGroups, meta) {
    let actionGroups = modelActionGroups || [];
    actionGroups.forEach((actionGroup) => {
      let actionGroupModel = this.constructActionGroupModel(actionGroup);
      this.linkAttributes(actionGroupModel, actionGroup.attributes, meta);
      this.modelDescriptionLinker.insertDescriptions(actionGroupModel, actionGroup.labels);

      actionGroupModel.setParent(model);
      model.addActionGroup(actionGroupModel);
    });
  }

  linkAttributes(model, attributes, meta) {
    this.modelAttributeLinker.linkAttributes(model, attributes, meta);
  }

  constructActionGroupModel(actionGroup) {
    let actionGroupModel = new ModelActionGroup(actionGroup.id);
    actionGroupModel.setInherited(false);
    return actionGroupModel;
  }
}