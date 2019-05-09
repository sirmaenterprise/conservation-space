import {ModelRestoreInheritedAttributeActionFactory} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action-factory';
import {ModelRestoreInheritedAttributeAction} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action';

import {ModelField} from 'administration/model-management/model/model-field';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

describe('ModelRestoreInheritedAttributeActionFactory', () => {

  let modelRestoreInheritedAttributeActionFactory;

  beforeEach(() => {
    modelRestoreInheritedAttributeActionFactory = new ModelRestoreInheritedAttributeActionFactory();
  });

  it('should properly create restore attribute action', () => {
    let toRestore = new ModelSingleAttribute();
    let expected = new ModelRestoreInheritedAttributeAction().setAttributesToRestore([toRestore]);
    expect(modelRestoreInheritedAttributeActionFactory.create(toRestore)).to.deep.eq(expected);
  });

  it('should properly evaluate restore attribute action', () => {
    let model = createTargetModel();
    let toRestore = model.getAttribute(ModelAttribute.TYPE_ATTRIBUTE);
    let action = new ModelRestoreInheritedAttributeAction()
      .setModel(model).setAttributesToRestore([toRestore]);

    modelRestoreInheritedAttributeActionFactory.evaluate(action);
    let result = action.getAttributesToRestore().map(a => a.getId());
    expect(result).to.deep.eq(['codeList', 'typeOption', 'value', 'type']);
  });

  function createTargetModel() {
    let model = new ModelField();
    model.addAttribute(new ModelSingleAttribute(ModelAttribute.TYPE_ATTRIBUTE).setParent(model));
    model.addAttribute(new ModelSingleAttribute(ModelAttribute.VALUE_ATTRIBUTE).setParent(model));
    model.addAttribute(new ModelSingleAttribute(ModelAttribute.CODELIST_ATTRIBUTE).setParent(model));
    model.addAttribute(new ModelSingleAttribute(ModelAttribute.TYPE_OPTION_ATTRIBUTE).setParent(model));
    return model;
  }
});
