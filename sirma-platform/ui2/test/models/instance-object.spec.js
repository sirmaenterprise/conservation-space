import {InstanceObject, CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';
import {RelatedObject} from 'models/related-object';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {ViewModelBuilder, ModelBuildUtil} from 'test/form-builder/view-model-builder';
import {InstanceModelProperty} from 'models/instance-model';

const IDOC_ID = 'emf:123456';
const DEFINITION_ID = 'OT210027';

describe('InstanceObject', () => {

  it('should return value of field with field uri \'emf:title\'', () => {

    let propertyValue = 'Title of instance object';
    let propertyName = 'title';
    let propertyUri = 'emf:title';

    let propertyModel = ModelBuildUtil.createField(propertyName, 'EDITABLE', 'text', 'Title', false, false, [], undefined, undefined, false, propertyUri);
    propertyModel.isDataProperty = true;
    let viewModel = new ViewModelBuilder().appendField(propertyModel).getModel();

    let validationModel = new ValidationModelBuilder()
      .addProperty(propertyName, propertyValue)
      .getModel();
    let instanceObject = new InstanceObject('emf:0001', {viewModel, validationModel}, null, null);

    expect(instanceObject.getPropertyValueByUri(propertyUri)).to.be.equal('Title of instance object');
  });

  describe('getContextPathIds()', () => {
    it('should convert context path to list of identifiers', () => {
      var models = generateModels(DEFINITION_ID);
      let instanceObject = new InstanceObject(IDOC_ID, models);
      instanceObject.setContextPath(models.path);
      let path = instanceObject.getContextPathIds();
      expect(path).to.eql(['emf:123456', 'emf:234567']);
    });
  });

  describe('setPropertiesValue()', () => {

    var instanceObject;
    beforeEach(() => {
      var validationModel = {};
      var viewModel = {
        fields: []
      };
      addFieldValidationModel(validationModel, 'title', 'title1', 'title1');
      addFieldValidationModel(validationModel, 'identifier', '', '123');
      addFieldValidationModel(validationModel, 'status', 'DRAFT', '');
      instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
    });

    it('should report being persisted if its id is not the surrogate temp id', () => {
      expect(instanceObject.isPersisted()).to.be.true;
    });

    it('should not report being persisted if its id is the surrogate temp id', () => {
      instanceObject.id = CURRENT_OBJECT_TEMP_ID;
      expect(instanceObject.isPersisted()).to.be.false;
    });

    it('should not report being persisted if have no id set', () => {
      instanceObject.id = null;
      expect(instanceObject.isPersisted()).to.be.false;
    });

    it('should not update the validation model if no properties are given', () => {
      instanceObject.setPropertiesValue();
      expect(Object.keys(instanceObject.models.validationModel.serialize()).length).to.equals(3);
      expect(instanceObject.models.validationModel['title'].value).to.equals('title1');
      expect(instanceObject.models.validationModel['identifier'].value).to.equals('');
      expect(instanceObject.models.validationModel['status'].value).to.equals('DRAFT');
    });

    it('should not update the validation model for properties that are missing in it', () => {
      instanceObject.setPropertiesValue({
        'type': 'CASE01'
      });
      expect(Object.keys(instanceObject.models.validationModel.serialize()).length).to.equals(3);
      expect(instanceObject.models.validationModel['type']).to.not.exist;
    });

    it('should update the validation model with the provided properties', () => {
      instanceObject.setPropertiesValue({
        'title': 'new-title',
        'identifier': '456'
      });
      expect(Object.keys(instanceObject.models.validationModel.serialize()).length).to.equals(3);
      expect(instanceObject.models.validationModel['title'].value).to.equals('new-title');
      expect(instanceObject.models.validationModel['identifier'].value).to.equals('456');
      expect(instanceObject.models.validationModel['status'].value).to.equals('DRAFT');
    });
  });

  describe('getChangeset', () => {
    it('should generate correct change set', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'title', 'title1', 'title1');
      addFieldValidationModel(validationModel, 'identifier', '', '123');
      addFieldValidationModel(validationModel, 'status', 'DRAFT', '');
      addFieldValidationModel(validationModel, 'createdOn', '12.12.2012', '12.12.2012');
      addFieldValidationModel(validationModel, 'emfObjectProperty', {
        results: ['emf:123456'], limit: 0, total: 1, offset: 0, add: ['emf:123456'], remove: []
      }, {
        results: [], limit: 0, total: 0, offset: 0, add: [], remove: []
      });
      addFieldValidationModel(validationModel, 'emfObjectPropertyUnchanged', {
        results: ['emf:999888', 'emf:123456'], limit: 0, total: 2, offset: 0, add: [], remove: []
      }, {
        results: ['emf:999888', 'emf:123456'], limit: 0, total: 2, offset: 0, add: [], remove: []
      });
      let viewModel = {
        fields: []
      };
      addFieldViewModel(viewModel, 'emfObjectProperty', 'PICKER', 'emf:objectProperty', false);
      addFieldViewModel(viewModel, 'emfObjectPropertyUnchanged', 'PICKER', 'emf:objectPropertyUnchanged', false);
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      instanceObject.models.validationModel.title.value = 'modified value';
      instanceObject.models.viewModel.identifier = {dataType: ''};
      let result = instanceObject.getChangeset();

      // old and new value differs
      expect(result).to.have.property('title', 'modified value');
      // value removed probably from user trough the UI
      expect(result).to.have.property('identifier', null);
      // has new value and no default
      expect(result).to.have.property('status', 'DRAFT');
      // not modified: should be missing from changeset
      expect(result).to.not.have.property('createdOn');
      expect(result).to.have.property('emfObjectProperty');
      expect(result['emfObjectProperty']).to.eql({add: ['emf:123456'], remove: []});
      expect(result).to.not.have.property('emfObjectPropertyUnchanged');
    });

    it('should properly convert numeric values', () => {
      let validationModel = {
        numericProperty: {defaultValue: '10', value: '00'}
      };
      let viewModel = {
        fields: [{
          dataType: 'double',
          displayType: 'EDITABLE',
          identifier: 'numericProperty',
          isDataProperty: true,
          isMandatory: false,
          label: 'numeric',
          uri: 'chd:numeric'
        }]
      };
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      let result = instanceObject.getChangeset();
      expect(result).to.have.property('numericProperty', 0);
    });


    it('should return unmodified values if forDraft is true', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'emfObjectProperty', [{id: 'emf:123456'}], []);
      let viewModel = {
        fields: []
      };
      addFieldViewModel(viewModel, 'emfObjectProperty', 'PICKER', 'emf:objectProperty', false);
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      let result = instanceObject.getChangeset(true);
      expect(result).to.have.property('emfObjectProperty');
      expect(result['emfObjectProperty']).to.eql([{id: 'emf:123456'}]);
    });
  });

  describe('isChanged', () => {
    it('should return true if there are any changes in the model', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'title', 'title1', 'title1');
      addFieldValidationModel(validationModel, 'identifier', '', '123');
      let viewModel = {
        fields: []
      };
      addFieldViewModel(viewModel, 'emf:objectProperty', 'PICKER', false);
      addFieldViewModel(viewModel, 'emf:objectPropertyUnchanged', 'PICKER', false);
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      expect(instanceObject.isChanged()).to.be.true;
    });

    it('should return false if value is null and default value is undefined', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'title', null, undefined);
      let viewModel = {
        fields: []
      };
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      expect(instanceObject.isChanged()).to.be.false;
    });
  });

  it('isNil should return true for null or undefined value', () => {
    expect(InstanceObject.isNil(null)).to.be.true;
    expect(InstanceObject.isNil(undefined)).to.be.true;
    expect(InstanceObject.isNil('')).to.be.false;
    expect(InstanceObject.isNil(false)).to.be.false;
    expect(InstanceObject.isNil(0)).to.be.false;
    expect(InstanceObject.isNil('test')).to.be.false;
    expect(InstanceObject.isNil([])).to.be.false;
  });

  describe('convertPropertyValues()', () => {
    let testData = {
      object: {
        viewModel: {
          isDataProperty: false,
          control: {identifier: 'PICKER'}
        },
        validationModel: {
          value: {
            results: ['emf:123456'],
            limit: 0,
            total: 0,
            offset: 0,
            add: ['emf:123456'],
            remove: []
          },
          defaultValue: {
            results: [],
            limit: 0,
            total: 0,
            offset: 0,
            add: [],
            remove: []
          }
        }
      },
      int: {
        viewModel: {dataType: 'int', isDataProperty: true},
        validationModel: {value: '1000', defaultValue: '1200'},
        expectedTypeOf: 'number',
        expectedValue: 1000
      },
      long: {
        viewModel: {dataType: 'long', isDataProperty: true},
        validationModel: {value: '1236547889656', defaultValue: '1236547889657'},
        expectedTypeOf: 'number',
        expectedValue: 1236547889656
      },
      double: {
        viewModel: {dataType: 'double', isDataProperty: true},
        validationModel: {value: '123.123', defaultValue: '987.876'},
        expectedTypeOf: 'number',
        expectedValue: 123.123
      },
      float: {
        viewModel: {dataType: 'float', isDataProperty: true},
        validationModel: {value: '123.123', defaultValue: '987.876'},
        expectedTypeOf: 'number',
        expectedValue: 123.123
      },
      // strings should be left as is
      string: {
        viewModel: {dataType: 'text', isDataProperty: true},
        validationModel: {value: 'test-string', defaultValue: 'test-string'},
        expectedTypeOf: 'string'
      },
      noValueInt: {
        viewModel: {datType: 'int', isDataProperty: true},
        validationModel: {value: undefined, defaultValue: undefined}
      },
      // leading zeroes should be trimmed
      leadingZeroInt: {
        viewModel: {dataType: 'int', isDataProperty: true},
        validationModel: {value: '0', defaultValue: '0'},
        expectedTypeOf: 'number',
        expectedValue: 7
      },
      // excess zeroes should be trimmed
      zeroInt: {
        viewModel: {dataType: 'int', isDataProperty: true},
        validationModel: {value: '0000', defaultValue: '00'},
        expectedTypeOf: 'number',
        expectedValue: 0
      },
      dataProperty: {
        viewModel: {isDataProperty: true},
        validationModel: {value: ['value1'], defaultValue: ['value1', 'value2']}
      }
    };

    it('should convert values for object property', () => {
      let convertedValues = InstanceObject.convertPropertyValues(testData.object.viewModel, testData.object.validationModel);
      expect(convertedValues.value).to.eql({add: ['emf:123456'], remove: []});
      expect(convertedValues.defaultValue).to.eql({add: [], remove: []});
    });

    it('should convert numeric type properties to number instead of string', () => {
      let testControls = ['int', 'long', 'double', 'float', 'string', 'leadingZeroInt', 'zeroInt'];
      let propertyConvertResults = {};
      testControls.forEach(control => propertyConvertResults[control] = InstanceObject.convertPropertyValues(testData[control].viewModel, testData[control].validationModel));
      testControls.forEach((control) => {
        let expectedType = testData[control].expectedTypeOf;
        let expectedResult = testData[control].expectedValue;
        expect((typeof propertyConvertResults[control].value === expectedType) && (typeof propertyConvertResults[control].defaultValue === expectedType), `${control} property value results`).to.be.true;
        if (testData[control].hasOwnProperty('expectedResult')) {
          expect(propertyConvertResults[control].value, `${control} expected result`).to.equal(expectedResult);
        }
      });
      // noValueInt property will be tested separately
      let noValueIntResult = InstanceObject.convertPropertyValues(testData.noValueInt.viewModel, testData.noValueInt.validationModel);
      expect(noValueIntResult.value, 'int property with no value should be undefined').to.be.undefined;
      expect(noValueIntResult.defaultValue, 'int property with no value should be undefined').to.be.undefined;
    });

    it('should return values as is for data property', () => {
      let convertedValues = InstanceObject.convertPropertyValues(testData.dataProperty.viewModel, testData.dataProperty.validationModel);
      expect(convertedValues.value).to.eql(['value1']);
      expect(convertedValues.defaultValue).to.eql(['value1', 'value2']);
    });
  });

  describe('setIncomingPropertyValue', () => {
    it('should convert properly codelist single field value and set it into validation model', () => {
      let viewModel = {
        codelist: 500
      };
      let validationModel = {};
      let newValue = {
        id: 'codeValue',
        text: 'codeLabel'
      };

      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.equals('codeValue');
      expect(validationModel.valueLabel).to.equals('codeLabel');
    });

    it('should convert properly codelist multi field value and set it into validation model', () => {
      let viewModel = {
        codelist: 500
      };
      let validationModel = {};
      let newValue = [{
        id: 'codeValue1',
        text: 'codeLabel1'
      }, {
        id: 'codeValue2',
        text: 'codeLabel2'
      }];

      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.eql(['codeValue1', 'codeValue2']);
      expect(validationModel.valueLabel).to.equals('codeLabel1, codeLabel2');
    });

    it('should set normal field value into validation model as is', () => {
      let viewModel = {};
      let validationModel = {};
      let newValue = 'simple value';

      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.equals('simple value');
      expect(validationModel.valueLabel).to.be.undefined;
    });

    it('should handle primitive property values for code list properties', () => {
      let viewModel = {
        codelist: 500
      };
      let validationModel = {};
      let newValue = 'simple value';
      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.equals('simple value');
      expect(validationModel.valueLabel).to.be.undefined;
    });
  });

  it('isObjectProperty should return correct result', () => {
    let fieldViewModelForObjectProperty = {
      identifier: 'objectProperty',
      isDataProperty: false
    };
    expect(InstanceObject.isObjectProperty(fieldViewModelForObjectProperty)).to.be.true;
    let fieldViewModelForDataProperty = {
      identifier: 'dataProperty',
      isDataProperty: true
    };
    expect(InstanceObject.isObjectProperty(fieldViewModelForDataProperty)).to.be.false;
  });

  it('isCodelistProperty shoud return correct result', () => {
    let fieldViewModelForCodeListProperty = {
      codelist: 500
    };
    expect(InstanceObject.isCodelistProperty(fieldViewModelForCodeListProperty)).to.be.true;
    expect(InstanceObject.isCodelistProperty({})).to.be.false;
  });

  it('formatObjectPropertyValue should properly convert relation raw values', () => {
    let objectPropertyRawValues = {
      results: [],
      limit: 0,
      total: 0,
      offset: 0,
      add: ['emf:123456', 'emf:999888'],
      remove: []
    };

    expect(InstanceObject.formatObjectPropertyValue(objectPropertyRawValues)).to.eql({
      add: ['emf:123456', 'emf:999888'],
      remove: []
    });
    expect(InstanceObject.formatObjectPropertyValue(undefined)).to.eql({add: [], remove: []});
  });

  it('mergePropertiesIntoModel() should properly update validation model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.mergePropertiesIntoModel({
      'field1': 'value to merge',
      'emfObjectProperty': ['emf:123456'],
      'testRichtextField': {richtextValue: '<span> sampleRichTextValue </span>'}
    });
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'value to merge');
    expect(instanceObject.models.validationModel.field1).to.have.property('defaultValue', 'value to merge');
    expect(instanceObject.models.validationModel.field1).to.have.property('valueLabel', 'label2');
    expect(instanceObject.models.validationModel.field1).to.have.property('defaultValueLabel', 'label2');

    expect(instanceObject.models.validationModel['emfObjectProperty'].value).to.eql(['emf:123456']);
    expect(instanceObject.models.validationModel['emfObjectProperty'].defaultValue).to.eql(['emf:123456']);

    expect(instanceObject.models.validationModel.testRichtextField.richtextValue).to.equal('<span> sampleRichTextValue </span>');
    expect(instanceObject.models.validationModel.testRichtextField.defaultRichTextValue).to.equal('<span> sampleRichTextValue </span>');
  });

  it('mergePropertiesIntoModel() should also add properties which does not exist in the model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.mergePropertiesIntoModel({'nonModelField': 'nonModelField value'});
    expect(instanceObject.models.validationModel.nonModelField).to.have.property('value', 'nonModelField value');
    expect(instanceObject.models.validationModel.nonModelField).to.have.property('defaultValue', 'nonModelField value');
  });

  it('mergeHeadersIntoModel() should add all available headers in the model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.models.validationModel['breadcrumb_header'] = {};
    instanceObject.models.validationModel['default_header'] = {};
    instanceObject.mergeHeadersIntoModel({
      'breadcrumb_header': 'breadcrumb_header_string',
      'default_header': 'default_header_string'
    });
    expect(instanceObject.models.validationModel['breadcrumb_header'].value).to.equal('breadcrumb_header_string');
    expect(instanceObject.models.validationModel['breadcrumb_header'].value).to.eql(instanceObject.models.validationModel['breadcrumb_header'].defaultValue);
    expect(instanceObject.models.validationModel['default_header'].value).to.equal('default_header_string');
    expect(instanceObject.models.validationModel['default_header'].value).to.eql(instanceObject.models.validationModel['default_header'].defaultValue);
  });

  describe('revertChanges', () => {
    it('should properly update validation model', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      instanceObject.models.validationModel.field1.value = 'modified value';
      expect(instanceObject.models.validationModel.field1).to.have.property('value', 'modified value');
      instanceObject.revertChanges();
      expect(instanceObject.models.validationModel.field1).to.have.property('value', 'value1');
      expect(instanceObject.models.validationModel.field1).to.have.property('valueLabel', 'label1');
    });

    it('should clone defaultValue instead of directly assigning it to value', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      instanceObject.models.validationModel['emfObjectProperty'].value = {results: ['emf:999888', 'emf:999777']};
      instanceObject.revertChanges();
      expect(instanceObject.models.validationModel['emfObjectProperty'].value.results).to.have.length(1);
      // value and default value should not reference to the same object
      expect(instanceObject.models.validationModel['emfObjectProperty'].value === instanceObject.models.validationModel['emfObjectProperty'].defaultValue).to.be.false;
      expect(instanceObject.models.validationModel['emfObjectProperty'].value.results[0]).to.equal('emf:123456');
    });

    it('should not add undefined value to property of type array', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      instanceObject.models.validationModel['fieldWithUndefinedDefaultValue'].value = [{id: 'emf:999888'}, {id: 'emf:999777'}];
      instanceObject.revertChanges();
      expect(instanceObject.models.validationModel['fieldWithUndefinedDefaultValue'].value).to.have.length(0);
    });
  });

  it('mergeModelIntoModel() should properly update validation model ', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    let updatedValidationModel = generateModels(DEFINITION_ID).validationModel;
    updatedValidationModel.field1.value = 'updated value 1';
    instanceObject.mergeModelIntoModel(updatedValidationModel);
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'updated value 1');
  });

  it('updateLocalModel() should update model values and default values', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    let updatedValidationModel = generateModels(DEFINITION_ID).validationModel;
    updatedValidationModel.field1.value = 'updated value 1';
    instanceObject.updateLocalModel(updatedValidationModel);
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'updated value 1');
    expect(instanceObject.models.validationModel.field1).to.have.property('defaultValue', 'updated value 1');
  });

  describe('getProperyValue()', () => {
    it('should return property value if such property exists and has a value', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      expect(instanceObject.getPropertyValue('title')).to.equals('Title');
      expect(instanceObject.getPropertyValue('non-existing-property')).to.be.undefined;
    });

    it('should return RelatedObject if the property is an object property', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      expect(instanceObject.getPropertyValue('emfObjectProperty') instanceof RelatedObject).to.be.true;
    });
  });

  describe('getSemanticClass', () => {
    it('should return the last element from the semanticHierarchy property from validation model', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));

      instanceObject.getModels().validationModel.semanticHierarchy = new InstanceModelProperty({
        value: [
          'http://www.ontotext.com/proton/protontop#Entity',
          'http://www.ontotext.com/proton/protontop#Happening',
          'http://www.ontotext.com/proton/protontop#Event',
          'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity',
          'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project'
        ],
        defaultValue: [
          'http://www.ontotext.com/proton/protontop#Entity',
          'http://www.ontotext.com/proton/protontop#Happening',
          'http://www.ontotext.com/proton/protontop#Event',
          'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity',
          'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project'
        ]
      });

      let semanticClass = instanceObject.getSemanticClass();

      expect(semanticClass).to.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project');
    });

    it('should return undefined if semanticHierarchy is empty or missing', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));

      let semanticClass = instanceObject.getSemanticClass();
      expect(semanticClass).to.be.undefined;

      instanceObject.getModels().validationModel.semanticHierarchy = new InstanceModelProperty({
        value: [],
        defaultValue: []
      });

      semanticClass = instanceObject.getSemanticClass();
      expect(semanticClass).to.be.undefined;
    });
  });

  it('isVersion should return true if property isVersion is true', () => {
    let propertyModel = ModelBuildUtil.createField('isVersion', 'EDITABLE', 'text', undefined, false, false, [], undefined, undefined, false, undefined);
    propertyModel.isDataProperty = true;
    let viewModel = new ViewModelBuilder().appendField(propertyModel).getModel();

    let validationModel = new ValidationModelBuilder()
      .addProperty('isVersion', true)
      .getModel();

    let models = {
      viewModel,
      validationModel
    };

    let instanceObject = new InstanceObject(IDOC_ID, models);
    expect(instanceObject.isVersion()).to.be.true;
  });

  describe('hasMandatory ', () => {

    it('no mandatory properties', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      expect(instanceObject.hasMandatory()).to.be.false;
    });

    it('with mandatory properties', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModelsForValidation(DEFINITION_ID));
      expect(instanceObject.hasMandatory()).to.be.true;
    });

    it('checkMandatory ', () => {
      let properties = {
        isMandatory: true
      };
      let instanceObject = new InstanceObject(IDOC_ID, generateModelsForValidation(DEFINITION_ID));
      expect(instanceObject.checkMandatory(properties)).to.be.true;
      let fancyProperties = {
        fields: [{
          isMandatory: false
        }, {
          isMandatory: true
        }]
      };
      expect(instanceObject.checkMandatory(fancyProperties)).to.be.true;
      let fancyPropertiesFalse = {
        fields: [{
          isMandatory: false
        }, {
          isMandatory: false
        }]
      };
      expect(instanceObject.checkMandatory(fancyPropertiesFalse)).to.be.false;
    });
  });
});

function addFieldValidationModel(model, name, value, defaultValue) {
  model[name] = {
    value,
    defaultValue
  };
}

function generateModels(id) {
  return {
    definitionId: id,
    parentId: 'parentId',
    returnUrl: 'returnUrl',
    viewModel: {
      fields: [{
        identifier: 'emfObjectProperty',
        isDataProperty: false,
        uri: 'emf:objectProperty',
        control: {
          identifier: 'PICKER'
        }
      }]
    },
    validationModel: {
      'field1': {
        defaultValue: 'value1',
        value: 'value1',
        defaultValueLabel: 'label1',
        valueLabel: 'label2'
      },
      'field2': {
        defaultValue: 'value2',
        value: 'value2'
      },
      'title': {
        defaultValue: 'title',
        value: 'Title'
      },
      'emfObjectProperty': {
        value: {results: ['emf:123456']},
        defaultValue: {results: ['emf:123456']}
      },
      'fieldWithUndefinedDefaultValue': {
        value: [{id: 'emf:123456'}],
        defaultValue: undefined
      },
      'testRichtextField': {
        value: 'sampleRichTextValue',
        richtextValue: '<span> sampleRichTextValue </span>'
      }
    },
    path: [
      {
        compactHeader: '\n<span class=\'truncate-element\'><a class=\'SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\' href=\'/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\' uid=\'10\'><b><span data-property=\'identifier\'>10<\/span> (<span data-property=\'type\'>Project for testing<\/span>) <span data-property=\'title\'>10<\/span> (<span data-property=\'status\'>Submitted<\/span>)\n<\/b><\/a><\/span><span class=\'header-icons\' data-instanceId=\'emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\'><span class=\'custom-icon dislikes favourites\' title=\'Add to favourites\'><\/span><\/span>',
        id: 'emf:123456',
        type: 'projectinstance'
      }, {
        compactHeader: '\n<span><span class=\'banner label label-warning\'><\/span><span class=\'truncate-element\'><a class=\'instance-link has-tooltip\' href=\'http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\' >\n(<span data-property=\'type\'>Common document<\/span>) <span data-property=\'title\'>Обикновен документ<\/span><span class=\'document-version version badge\'>1.7<\/span><\/a><\/span><span class=\'header-icons\' data-instanceId=\'emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\'><span class=\'custom-icon download downloads-list\' title=\'Add to downloads\'><\/span><span class=\'custom-icon dislikes favourites\' title=\'Add to favourites\'><\/span><\/span><\/span>',
        id: 'emf:234567',
        type: 'documentinstance'
      }
    ]
  };
}

function addFieldViewModel(model, identifier, controlIdentifier, uri, isDataProperty, dataType) {
  let field = {
    identifier,
    uri,
    isDataProperty,
    dataType
  };
  if (controlIdentifier) {
    field.control = {
      identifier: controlIdentifier
    };
  }
  model.fields.push(field);
}

function generateModelsForValidation(id) {
  return {
    definitionId: id,
    parentId: 'parentId',
    returnUrl: 'returnUrl',
    viewModel: {
      fields: [{
        identifier: 'emfObjectProperty',
        uri: 'emf:objectProperty',
        isMandatory: true,
        control: {
          identifier: 'PICKER'
        }
      }]
    },
    validationModel: {
      'field1': {
        defaultValue: 'value1',
        value: 'value1',
        defaultValueLabel: 'label1',
        valueLabel: 'label2'
      },
      'field2': {
        defaultValue: 'value2',
        value: 'value2'
      },
      'title': {
        defaultValue: 'title',
        value: 'Title'
      },
      'emfObjectProperty': {
        value: [{id: 'emf:123456'}],
        defaultValue: [{id: 'emf:123456'}]
      },
      'fieldWithUndefinedDefaultValue': {
        value: [{id: 'emf:123456'}],
        defaultValue: undefined
      }
    },
    path: [
      {
        compactHeader: '\n<span class=\'truncate-element\'><a class=\'SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\' href=\'/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\' uid=\'10\'><b><span data-property=\'identifier\'>10<\/span> (<span data-property=\'type\'>Project for testing<\/span>) <span data-property=\'title\'>10<\/span> (<span data-property=\'status\'>Submitted<\/span>)\n<\/b><\/a><\/span><span class=\'header-icons\' data-instanceId=\'emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\'><span class=\'custom-icon dislikes favourites\' title=\'Add to favourites\'><\/span><\/span>',
        id: 'emf:123456',
        type: 'projectinstance'
      }, {
        compactHeader: '\n<span><span class=\'banner label label-warning\'><\/span><span class=\'truncate-element\'><a class=\'instance-link has-tooltip\' href=\'http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\' >\n(<span data-property=\'type\'>Common document<\/span>) <span data-property=\'title\'>Обикновен документ<\/span><span class=\'document-version version badge\'>1.7<\/span><\/a><\/span><span class=\'header-icons\' data-instanceId=\'emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\'><span class=\'custom-icon download downloads-list\' title=\'Add to downloads\'><\/span><span class=\'custom-icon dislikes favourites\' title=\'Add to favourites\'><\/span><\/span><\/span>',
        id: 'emf:234567',
        type: 'documentinstance'
      }
    ]
  };
}