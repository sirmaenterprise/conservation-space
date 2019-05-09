import {ModelRestoreInheritedAttributeActionProcessor} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action-processor';
import {ModelRestoreInheritedAttributeAction} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

import {stub} from 'test/test-utils';

describe('ModelRestoreInheritedAttributeActionProcessor', () => {

  let modelRestoreInheritedAttributeActionFactory;

  let modelChangeSetBuilderStub;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelRestoreInheritedAttributeActionFactory = new ModelRestoreInheritedAttributeActionProcessor(modelChangeSetBuilderStub);
  });

  it('should restore the provided attributes as inherited for a given model', () => {
    let parentModel = new ModelField('field').setParent(new ModelDefinition());
    let parentAttribute = new ModelSingleAttribute('1').setParent(parentModel);
    parentModel.addAttribute(parentAttribute);

    let concreteModel = new ModelField('field').setParent(new ModelDefinition());
    let concreteAttribute = new ModelSingleAttribute('1').setParent(concreteModel);
    concreteModel.addAttribute(concreteAttribute);

    concreteModel.setReference(parentModel);
    modelRestoreInheritedAttributeActionFactory.execute(new ModelRestoreInheritedAttributeAction().
      setContext(concreteModel.getParent()).setModel(concreteModel).setAttributesToRestore([concreteAttribute]));

    expect(concreteModel.getAttributes()).to.deep.eq([parentAttribute]);
    expect(concreteModel.getParent().getField('field')).to.eq(parentModel);
  });

  it('should revert the provided attributes as non inherited for a given model', () => {
    let parentModel = new ModelField('field').setParent(new ModelDefinition());
    let parentAttribute = new ModelSingleAttribute('1').setParent(parentModel);
    parentModel.addAttribute(parentAttribute);

    let concreteModel = new ModelField('field').setParent(new ModelDefinition());
    let concreteAttribute = new ModelSingleAttribute('1').setParent(concreteModel);
    concreteModel.addAttribute(parentAttribute);

    concreteModel.setReference(parentModel);
    modelRestoreInheritedAttributeActionFactory.restore(new ModelRestoreInheritedAttributeAction().
      setContext(concreteModel.getParent()).setModel(concreteModel).setAttributesToRestore([concreteAttribute]));

    expect(concreteModel.getAttributes()).to.deep.eq([concreteAttribute]);
    expect(concreteModel.getParent().getField('field')).to.eq(concreteModel);
  });

  it('should be able to build change set for the given action', () => {
    let models = [new ModelSingleAttribute(), new ModelSingleAttribute()];
    modelRestoreInheritedAttributeActionFactory.changeset(new ModelRestoreInheritedAttributeAction().setAttributesToRestore(models));

    expect(modelChangeSetBuilderStub.buildChangeSets.calledOnce);
    expect(modelChangeSetBuilderStub.buildChangeSets.calledWith(models)).to.be.true;
  });
});