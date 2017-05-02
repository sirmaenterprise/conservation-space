import {ModelUtils} from 'models/model-utils';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';

describe('ModelUtils', () => {

  describe('createViewModel', () => {
    it('should return viewModel scelaton', () => {
      expect(ModelUtils.createViewModel()).to.eql({
        fields: []
      });
    });
  });

  describe('createField', () => {
    it('should build a field view model', () => {
      let field = ModelUtils.createField('field1', 'EDITABLE', 'text', 'field 1', true, true, [], null, 1);
      expect(field).to.eql({
        'identifier': 'field1',
        'previewEmpty': true,
        'disabled': false,
        'displayType': 'EDITABLE',
        'tooltip': 'tooltip',
        'validators': [],
        'dataType': 'text',
        'label': 'field 1',
        'isMandatory': true,
        'maxLength': 40,
        'rendered': true,
        'codelist': 1
      })
    });
  });

  describe('addField', () => {
    it('should add field in provided model', () => {
      let model = ModelUtils.createViewModel();
      ModelUtils.addField(model, ModelUtils.createField('field1', 'EDITABLE', 'text', 'field 1', true, true, [], null, 1));
      expect(model).to.eql({
        fields: [
          {
            'identifier': 'field1',
            'previewEmpty': true,
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'tooltip',
            'validators': [],
            'dataType': 'text',
            'label': 'field 1',
            'isMandatory': true,
            'maxLength': 40,
            'rendered': true,
            'codelist': 1
          }
        ]
      });
    });
  });

  describe('createProperty', () => {
    it('should create instance model property', () => {
      expect(ModelUtils.createProperty('value1', true)).to.eql({
        'messages': {},
        'value': 'value1',
        'valid': true
      })
    });
  });

  describe('addProperty', () => {
    it('should add property in provided model', () => {
      let model = ModelUtils.createInstanceModel();
      ModelUtils.addProperty(model, 'field1', ModelUtils.createProperty('value1', false));
      expect(model).to.eql({
        'field1': {
          'messages': {},
          'value': 'value1',
          'valid': false
        }
      })
    });
  });

  describe('buildEmptyCell()', () => {
    it('should create field model for EMPTY_CELL control', () => {
      expect(ModelUtils.buildEmptyCell('123')).to.eql({
        identifier: '123',
        displayType: 'EDITABLE',
        previewEmpty: true,
        control: {
          identifier: 'EMPTY_CELL'
        }
      });
    });
  });

  describe('flatViewModel()', () => {
    it('should create a flatten form view model', () => {
      let model = new ViewModelBuilder()
        .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false)
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', false, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', false, false, [])
        .addField('field6', 'EDITABLE', 'text', 'field 6', false, false, [])
        .endRegion()
        .getModel();
      let flatModel = ModelUtils.flatViewModel(model);
      let expectedKeys = [
        'field1',
        'field2',
        'field3',
        'field4',
        'field5',
        'field6'
      ];
      expect(flatModel.size).to.equal(expectedKeys.length);
      expectedKeys.forEach((key) => {
        flatModel.has(key);
      })
    });
  });

  describe('defineControlType', () => {
    it('should return null for control type when called with missing field model', () => {
      let type = ModelUtils.defineControlType();
      expect(type === null).to.be.true;
    });

    it('should return null for control type when called with empty field model', () => {
      let type = ModelUtils.defineControlType({});
      expect(type === null).to.be.true;
    });

    it('should return type=region when called with field model that has fields property set', () => {
      let region = ViewModelBuilder.createRegion('region1', 'region 1', 'EDITABLE', false, false);
      expect(ModelUtils.defineControlType(region)).to.equal('region');
    });

    it('should return the predefined control type when called with field model that has control property set', () => {
      let fieldWithControl = ViewModelBuilder.createField('field1', 'EDITABLE', 'text', 'field 1', true, true, [], 'picklist');
      expect(ModelUtils.defineControlType(fieldWithControl)).to.equal('picklist');
    });

    it('should return type=text when called with field model that has dataType=text, int or long property set', () => {
      let textfield = ViewModelBuilder.createField('textfield', 'EDITABLE', 'text', 'field 1', true, true, []);
      Object.keys(ModelUtils.REPRESENTABLE_AS_TEXT).forEach((dataType) => {
        textfield.dataType = dataType;
        expect(ModelUtils.defineControlType(textfield)).to.equal('text');
      });
    });

    it('should return type=datetime when called with field model that has dataType=date property set', () => {
      let datetime = ViewModelBuilder.createField('createdOn', 'EDITABLE', 'date', 'field 1', true, true, []);
      expect(ModelUtils.defineControlType(datetime)).to.equal('datetime');
    });

    it('should return type=datetime when called with field model that has dataType=datetime property set', () => {
      let datetime = ViewModelBuilder.createField('createdOn', 'EDITABLE', 'datetime', 'field 1', true, true, []);
      expect(ModelUtils.defineControlType(datetime)).to.equal('datetime');
    });

    it('should return type=checkbox when called with field model that has dataType=boolean property set', () => {
      let bool = ViewModelBuilder.createField('isActive', 'EDITABLE', 'boolean', 'field 1', true, true, []);
      expect(ModelUtils.defineControlType(bool)).to.equal('checkbox');
    });

    it('should return type=null when called with field model that has dataType set with unknown by the system data type', () => {
      let undefinedField = ViewModelBuilder.createField('undefinedType', 'EDITABLE', 'undefinedtype', 'field 1', true, true, []);
      expect(ModelUtils.defineControlType(undefinedField) === null).to.be.true;
    });

    // Some filed control types are currently not implemented in UI2 but the fields with such controls must be rendered
    // because they might be mandatory and should be populated before the object to be saved. So, such controls are
    // checked and if found one, then the field is rendered in normal way.
    it('should not return predefined control type when the control type is supported', () => {
      let fieldWithControl = ViewModelBuilder.createField('department', 'EDITABLE', 'text', 'Department', true, true, [], 'RELATED_FIELDS');
      expect(ModelUtils.defineControlType(fieldWithControl)).to.equal('text');
    });

    it('should return INSTANCE_HEADER type if the field has identifier=compact_header', () => {
      let header = ViewModelBuilder.createField(HEADER_COMPACT, 'EDITABLE', 'text', 'header', true, true, []);
      expect(ModelUtils.defineControlType(header)).to.equal('INSTANCE_HEADER');
    });

    it('should return INSTANCE_HEADER type if the field has identifier=default_header', () => {
      let header = ViewModelBuilder.createField(HEADER_DEFAULT, 'EDITABLE', 'text', 'header', true, true, []);
      expect(ModelUtils.defineControlType(header)).to.equal('INSTANCE_HEADER');
    });

    it('should return INSTANCE_HEADER type if the field has identifier=breadcrumb_header', () => {
      let header = ViewModelBuilder.createField(HEADER_BREADCRUMB, 'EDITABLE', 'text', 'header', true, true, []);
      expect(ModelUtils.defineControlType(header)).to.equal('INSTANCE_HEADER');
    });

    it('should return type=password when called with field model that has dataType=password property set', () => {
      let password = ViewModelBuilder.createField('currentPassword', 'EDITABLE', 'password', 'Current password', true, true, []);
      expect(ModelUtils.defineControlType(password)).to.equal('password');
    });
  });

  describe('isRegion', () => {
    it(' should recognize the region type fields', () => {
      let region = ViewModelBuilder.createRegion('region1', 'region 1', 'EDITABLE', false, false);
      let isRegion = ModelUtils.isRegion(region);
      expect(isRegion).to.be.true;
    });

    it('should recognize if a field is not a region', () => {
      let field = ViewModelBuilder.createField('field1', 'EDITABLE', 'text', 'field 1', true, true, []);
      let isRegion = ModelUtils.isRegion(field);
      expect(isRegion).to.be.false;
    });
  });

  describe('getTextFieldType', () => {
    let textareaMinCharsLength = 50;
    it('should return type=codelist if current model item has codelist property', function () {
      let type = ModelUtils.getTextFieldType({
        'codelist': 100
      }, textareaMinCharsLength);
      expect(type).to.equal('codelist');
    });

    it('should return type=textarea if current model item has property maxLength>50', function () {
      let type = ModelUtils.getTextFieldType({
        'maxLength': 60
      }, textareaMinCharsLength);
      expect(type).to.equal('textarea');
    });

    it('should return type=textarea if current model item has property maxLength=50', function () {
      let type = ModelUtils.getTextFieldType({
        'maxLength': 50
      }, textareaMinCharsLength);
      expect(type).to.equal('textarea');
    });

    it('should return type=text if current model item has dataType=text and maxLength<50 properties', function () {
      let type = ModelUtils.getTextFieldType({
        'maxLength': 49
      }, textareaMinCharsLength);
      expect(type).to.equal('text');
    });
  });
});