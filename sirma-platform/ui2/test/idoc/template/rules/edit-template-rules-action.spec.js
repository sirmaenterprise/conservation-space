import {EditTemplateRuleAction} from 'idoc/template/rules/edit-template-rules-action';
import {TemplateRuleEditorService} from 'idoc/template/rules/template-rule-edit-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {Logger} from 'services/logging/logger';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {stub} from 'test/test-utils';

describe('EditTemplateRuleAction', () => {

  let action;
  let currentObject;
  let templateRuleEditorService;
  let instanceService;

  const TYPE = 'test_type';
  const RULE = 'primary == false';
  const PURPOSE = 'creatable';

  beforeEach(() => {
    templateRuleEditorService = stub(TemplateRuleEditorService);
    templateRuleEditorService.openRuleEditor.returns(PromiseStub.resolve());
    instanceService = stub(InstanceRestService);

    action = new EditTemplateRuleAction(stub(Logger), templateRuleEditorService, instanceService);
    action.refreshInstance = sinon.stub();

    let viewModel = new ViewModelBuilder()
      .addField('forObjectType', 'SYSTEM', 'text', undefined, false, false, [], undefined, undefined, false, undefined)
      .addField('templateRule', 'SYSTEM', 'text', undefined, false, false, [], undefined, undefined, false, undefined)
      .addField('templatePurpose', 'SYSTEM', 'text', undefined, false, false, [], undefined, undefined, false, undefined)
      .addField('isPrimaryTemplate', 'SYSTEM', 'text', undefined, false, false, [], undefined, undefined, false, undefined)
      .getModel();

    let validationModel = new ValidationModelBuilder()
      .addProperty('forObjectType', TYPE)
      .addProperty('templateRule', RULE)
      .addProperty('templatePurpose', PURPOSE)
      .addProperty('isPrimaryTemplate', true)
      .getModel();

    let models = {
      viewModel,
      validationModel
    };

    currentObject = new InstanceObject('testObject', models);
  });

  it('should call rest service with proper data, and reload the entity if opened in idoc', () => {
    action.execute({
      action: 'editTemplateRules'
    }, {
      currentObject,
      idocContext: {}
    }).then(() => {
      expect(templateRuleEditorService.openRuleEditor.getCall(0).args[0]).to.eql({
        instanceId: currentObject.getId(),
        forObjectType: TYPE,
        rules: RULE,
        purpose: PURPOSE,
        primary: true
      });

      expect(action.refreshInstance.called).to.be.true;
    });
  });

  it('should load the full object if its partially loaded', () => {
    const ID = 'testId';

    instanceService.loadInstanceObject.withArgs(ID).returns(PromiseStub.resolve(currentObject));

    let partiallyLoadedCurrentObject = new InstanceObject(ID);

    action.execute({
      action: 'editTemplateRules'
    }, {
      currentObject: partiallyLoadedCurrentObject,
      idocContext: {}
    }).then(() => {
      expect(templateRuleEditorService.openRuleEditor.getCall(0).args[0]).to.eql({
        instanceId: currentObject.getId(),
        forObjectType: TYPE,
        rules: RULE,
        purpose: PURPOSE,
        primary: true
      });

      expect(action.refreshInstance.called).to.be.true;
    });
  });

  it('should not reload the entity if not opened in idoc', () => {
    action.execute({
      action: 'editTemplateRules'
    }, {
      currentObject
    }).then(() => {
      expect(action.refreshInstance.called).to.be.false;
    });
  });

});