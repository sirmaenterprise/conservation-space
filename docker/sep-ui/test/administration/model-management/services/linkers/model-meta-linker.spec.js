import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelMetaDataLinker} from 'administration/model-management/services/linkers/model-meta-linker';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {stub} from 'test/test-utils';

describe('ModelMetaDataLinker', () => {

  let model;
  let modelMetaLinker;
  let modelDescriptionLinkerStub;

  beforeEach(() => {
    model = new ModelAttributeMetaData('meta');

    modelDescriptionLinkerStub = stub(ModelDescriptionLinker);
    modelDescriptionLinkerStub.insertDescriptions.returns({});
    modelMetaLinker = new ModelMetaDataLinker(modelDescriptionLinkerStub);
  });

  it('should link the data provided by the response with the model meta data', () => {
    modelMetaLinker.linkMetaData(model, getMetaData());

    expect(model.getType()).to.eq('string');
    expect(model.getDefaultValue()).to.eq('value');
    expect(model.getValidationModel().getRestrictions().isMandatory()).to.be.true;
    expect(model.getValidationModel().getRestrictions().isUpdateable()).to.be.false;

    let validationRule = model.getValidationModel().getValidationRules().getRules()[0];
    expect(validationRule.getValues()).to.eql(['test']);
    expect(validationRule.getCondition()).to.equal('AND');
    expect(validationRule.getErrorLabel()).to.equal('administration.models.management.validation.field.can.not.be.mandatory');

    let validationRuleExpression = validationRule.getExpressions()[0];
    expect(validationRuleExpression.getField()).to.equal('displayType');
    expect(validationRuleExpression.getOperation()).to.equal('in');
    expect(validationRuleExpression.getValues()).to.eql(['READ_ONLY', 'HIDDEN', 'SYSTEM']);
  });

  it('should have empty options if meta data for them is not provided', () => {
    let metaData = getMetaData();
    metaData.options = [];
    modelMetaLinker.linkMetaData(model, metaData);

    expect(model.getOptions().length === 0).to.be.true;
  });

  it('should have empty options if meta data for them is not provided', () => {
    modelMetaLinker.linkMetaData(model, getMetaData());
    let expectedOptions = [
      {
        value: 'value one',
        label: 'label of option one'
      }, {
        value: 'value two',
        label: 'label of option two'
      }
    ];

    expect(model.getOptions()).to.deep.equal(expectedOptions);
  });

  it('should labels and descriptions provided by the response', () => {
    modelMetaLinker = new ModelMetaDataLinker(new ModelDescriptionLinker(stub(ModelManagementLanguageService)));
    modelMetaLinker.linkMetaData(model, getMetaData());

    expect(model.getDescriptionByLanguage('en').getValue()).to.equal('Label');
    expect(model.getDescriptionByLanguage('bg').getValue()).to.equal('Етикет');
    expect(model.getTooltipByLanguage('en').getValue()).to.equal('Tooltip value');
    expect(model.getTooltipByLanguage('bg').getValue()).to.equal('Подсказка');
  });

  function getMetaData() {
    return {
      type: 'string',
      defaultValue: 'value',
      validationModel: {
        mandatory: true,
        updateable: false,
        affected: ['mandatory'],
        rules: [
          {
            values: ['test'],
            condition: 'AND',
            expressions: [
              {
                field: 'displayType',
                operation: 'in',
                values: ['READ_ONLY', 'HIDDEN', 'SYSTEM']
              }
            ],
            errorLabel: 'administration.models.management.validation.field.can.not.be.mandatory'
          }
        ]
      },
      options:[
        {
          value: 'value one',
          label: 'label of option one'
        }, {
          value: 'value two',
          label: 'label of option two'
        }
      ],
      labels: {
        'en': 'Label',
        'bg': 'Етикет'
      },
      descriptions: {
        'en': 'Tooltip value',
        'bg': 'Подсказка'
      }
    };
  }
});