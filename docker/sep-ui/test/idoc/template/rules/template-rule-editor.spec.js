import {TemplateRuleEditor} from 'idoc/template/rules/template-rule-editor';
import {DefinitionService} from 'services/rest/definition-service';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {ModelUtils} from 'models/model-utils';
import {Logger} from 'services/logging/logger';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('TemplateRuleEditor', function () {

  var templateRuleEditor;
  var definitionService;
  var logger;

  beforeEach(function () {
    definitionService = stub(DefinitionService);
    logger = stub(Logger);

    templateRuleEditor = new TemplateRuleEditor(definitionService, null, null, logger);

    templateRuleEditor.config = {
      template: {
        forObjectType: 'type1'
      }
    };

    definitionService.getDefinitions.withArgs('type1').returns(PromiseStub.resolve(buildModel('type1')));
  });

  it('should list only boolean and single-value codelist fields that are EDITABLE and mandatory', function () {
    templateRuleEditor.ngOnInit();

    var models = templateRuleEditor.models;

    expect(models.viewModel.fields.length).to.equal(2, 'The non-matching fields, and regions with non-matching fields should be removed');

    expect(models.viewModel.fields[0].identifier).to.equal('departmentRegion');
    expect(models.viewModel.fields[1].identifier).to.equal('primary');

    var departmentRegionField = models.viewModel.fields[0];
    expect(departmentRegionField.fields.length).to.equal(2);
    expect(departmentRegionField.fields[0].identifier).to.equal('department');
    expect(departmentRegionField.fields[1].identifier).to.equal('subdepartment');

    var validationModelEntries = Object.keys(models.validationModel);
    expect(validationModelEntries).to.eql(['department', 'subdepartment', 'primary']);
  });

  it('should turn single-valued codelist fields to multivalue', function () {
    templateRuleEditor.ngOnInit();

    var models = templateRuleEditor.models;

    var departmentRegionField = models.viewModel.fields[0];

    expect(departmentRegionField.fields[1].identifier).to.equal('subdepartment');
    expect(departmentRegionField.fields[1].multivalue).to.be.true;
  });

  it('should remove all validators expect the regex validator', function () {
    templateRuleEditor.ngOnInit();

    var models = templateRuleEditor.models;

    var departmentRegionField = models.viewModel.fields[0];

    var department = departmentRegionField.fields[0];
    var subdepartment = departmentRegionField.fields[1];

    expect(department.isMandatory).to.be.false;
    expect(subdepartment.isMandatory).to.be.false;

    expect(department.validators.length).to.equal(1);
    expect(department.validators[0].id).to.equal('regex');
  });

  it('should inform that the form is available when there is at least one eligible field to display', function () {
    let viewModel = new ViewModelBuilder()
      .addField('primary', 'EDITABLE', 'boolean', 'Primary', true)
      .getModel();

    var validationModel = {
      'primary': {}
    };

    var model = {
      data: {
        'type1': {
          validationModel: validationModel,
          viewModel: viewModel
        }
      }
    };

    definitionService.getDefinitions.withArgs('type1').returns(PromiseStub.resolve(model));

    templateRuleEditor.ngOnInit();

    expect(templateRuleEditor.config.formAvailable).to.be.true;
  });

  describe('should calculate rule', function () {

    it('containing boolean field', function () {
      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      instanceModel['primary'].value = true;

      templateRuleEditor.buildRuleFromModel(instanceModel);

      expect(templateRuleEditor.config.template.rules).to.equal('primary == true');
    });

    it('containing codelist field with single value', function () {
      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      instanceModel['department'].value = 'DEV';

      templateRuleEditor.buildRuleFromModel(instanceModel);

      expect(templateRuleEditor.config.template.rules).to.equal('department == "DEV"');
    });

    it('containing codelist field with single value inside an array', function () {
      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      instanceModel['department'].value = ['DEV'];

      templateRuleEditor.buildRuleFromModel(instanceModel);

      expect(templateRuleEditor.config.template.rules).to.equal('department == "DEV"');
    });

    it('containing codelist field with multiple values', function () {
      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      instanceModel['department'].value = ['DEV', 'QA', 'BA'];

      templateRuleEditor.buildRuleFromModel(instanceModel);

      expect(templateRuleEditor.config.template.rules).to.equal('(department == "DEV" || department == "QA" || department == "BA")');
    });

    it('containing codelist field that has no value', function () {
      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      instanceModel['department'].value = [];

      templateRuleEditor.buildRuleFromModel(instanceModel);

      expect(templateRuleEditor.config.template.rules).to.equal('');
    });

    it('containing multiple fields', function () {
      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      instanceModel['primary'].value = true;
      instanceModel['department'].value = 'DEV';

      templateRuleEditor.buildRuleFromModel(instanceModel);

      expect(templateRuleEditor.config.template.rules).to.equal('department == "DEV" && primary == true');
    });
  });

  describe('should populate model values from rule', function () {

    it('when the rule contains a codelist field that is single-valued', function () {
      templateRuleEditor.config.template.rules = 'subdepartment == "UI"';

      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      expect(instanceModel['subdepartment'].value).to.eql(['UI']);
    });

    it('when the rule contains a codelist field that is multi-valued', function () {
      templateRuleEditor.config.template.rules = '(department == "DEV" || department == "QA" || department == "BA")';

      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      expect(instanceModel['department'].value).to.eql(['DEV', 'QA', 'BA']);
    });

    it('when the rule contains a codelist field that is multi-valued and only single value is provided', function () {
      templateRuleEditor.config.template.rules = 'department == "DEV"';

      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      expect(instanceModel['department'].value).to.eql(['DEV'], 'The value should be stored in an array');
    });

    it('when the rule contains a codelist field that is boolean', function () {
      templateRuleEditor.config.template.rules = 'primary == true';

      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      expect(instanceModel['primary'].value).to.eql(true);
    });

    it('when the rule contains multiple clauses', function () {
      templateRuleEditor.config.template.rules = 'primary == true && (department == "DEV" || department == "QA")';

      templateRuleEditor.ngOnInit();

      var instanceModel = templateRuleEditor.formModels.validationModel;

      expect(instanceModel['primary'].value).to.eql(true);
      expect(instanceModel['department'].value).to.eql(['DEV', 'QA']);
    });

  });

  it('should yell warning when populating model from existing rule but a rule property is not in the model anymore', function () {
    templateRuleEditor.config.template.rules = 'test == true';

    templateRuleEditor.ngOnInit();

    expect(logger.warn.calledOnce).to.be.true;
  });

  it('should unsubscribe from model changes when destroyed', function () {
    templateRuleEditor.config.template.rules = 'primary == true && (department == "DEV" || department == "QA")';

    templateRuleEditor.ngOnInit();

    expect(templateRuleEditor.subscriptions.length).to.equal(3);

    templateRuleEditor.subscriptions.forEach(function (subscription) {
      sinon.spy(subscription, 'unsubscribe');
    });

    templateRuleEditor.ngOnDestroy();

    templateRuleEditor.subscriptions.forEach(function (subscription) {
      expect(subscription.unsubscribe.calledOnce).to.be.true;
    });
  });

  /**
   * Structure:
   *
   * title - text, editable, mandatory
   * departmentRegion
   *    department - codelist multivalue, mandatory
   *    subdepartment - codelist single-value, mandatory
   * otherInfoRegion
   *     group - codelist, single-value, not mandatory
   *     subgroup - codelist, multivalue-value, mandatory
   * active - boolean, hidden
   * primary - boolean, editable, mandatory
   */
  function buildModel(type) {
    let viewModel = new ViewModelBuilder()
      .addField('title', 'EDITABLE', 'text', 'field 1', true)
      .addRegion('departmentRegion', 'Department region', 'EDITABLE', false)
      .addField('department', 'EDITABLE', 'text', 'field 1', true, true, [{
        id: 'mandatory'
      }, {
        id: 'regex'
      }, {
        id: 'calculate'
      }], null, 10, false)
      .addField('subdepartment', 'EDITABLE', 'text', 'field 1', true, true, [], null, 11, false)
      .endRegion()
      .addRegion('otherInfoRegion', 'Other info region', 'EDITABLE', false)
      .addField('group', 'EDITABLE', 'text', 'Group', false, true, [], null, 12, false)
      .addField('subgroup', 'EDITABLE', 'text', 'Subgroup', true, true, [], null, 12, true)
      .endRegion()
      .addField('active', 'HIDDEN', 'boolean', 'Active', true)
      .addField('primary', 'EDITABLE', 'boolean', 'Primary', true)
      .getModel();

    //id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist, multivalue
    var validationModel = {};

    ModelUtils.walkModelTree(viewModel.fields, validationModel, (viewModelField, validatoionModelField) => {
      // no validation fields are kept for regions
      if (!ModelUtils.isRegion(viewModelField)) {
        validationModel[viewModelField.identifier] = {};
      }
    });

    var model = {
      validationModel: validationModel,
      viewModel: viewModel
    };

    return {
      data: {
        [type]: model
      }
    };
  }

});