import {ModelUtils} from 'models/model-utils';
import {InstanceModel, InstanceModelProperty} from 'models/instance-model';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';

describe('ModelUtils', () => {

  describe('createViewModel', () => {
    it('should return viewModel skeleton', () => {
      expect(ModelUtils.createViewModel()).to.eql({
        fields: []
      });
    });
  });

  describe('createField', () => {
    it('should build a field view model', () => {
      let field = ModelUtils.createField('field1', 'EDITABLE', 'text', 'field 1', true, true, [], null, 1, false);
      expect(field).to.eql({
        identifier: 'field1',
        previewEmpty: true,
        disabled: false,
        displayType: 'EDITABLE',
        tooltip: 'tooltip',
        validators: [],
        dataType: 'text',
        label: 'field 1',
        isMandatory: true,
        maxLength: 40,
        rendered: true,
        codelist: 1,
        isDataProperty: false
      });
    });
  });

  describe('addField', () => {
    it('should add field in provided model', () => {
      let model = ModelUtils.createViewModel();
      ModelUtils.addField(model, ModelUtils.createField('field1', 'EDITABLE', 'text', 'field 1', true, true, [], null, 1, true));
      expect(model).to.eql({
        fields: [
          {
            identifier: 'field1',
            previewEmpty: true,
            disabled: false,
            displayType: 'EDITABLE',
            tooltip: 'tooltip',
            validators: [],
            dataType: 'text',
            label: 'field 1',
            isMandatory: true,
            maxLength: 40,
            rendered: true,
            codelist: 1,
            isDataProperty: true
          }
        ]
      });
    });
  });

  describe('createProperty', () => {
    it('should create instance model property', () => {
      expect(ModelUtils.createProperty('value1', true)).to.eql({
        'messages': [],
        'value': 'value1',
        'valid': true
      });
    });
  });

  describe('addProperty', () => {
    it('should add property in provided model', () => {
      let model = ModelUtils.createInstanceModel();
      ModelUtils.addProperty(model, 'field1', ModelUtils.createProperty('value1', false));
      expect(model).to.eql({
        'field1': {
          'messages': [],
          'value': 'value1',
          'valid': false
        }
      });
    });
  });

  describe('object property utils', () => {

    describe('isObjectProperty', () => {
      it('should return true if the property model has value.results or results attributes', () => {
        let model = { value: { results: ['1'] }};
        expect(ModelUtils.isObjectProperty(model)).to.be.true;
        model = { results: ['1'] };
        expect(ModelUtils.isObjectProperty(model)).to.be.true;
      });

      it('should return false if property model doesn`t have value.results or result attribute', () => {
        let model = { value: 'text' };
        expect(ModelUtils.isObjectProperty(model)).to.be.false;
      });
    });

    describe('getEmptyObjectPropertyValue', () => {
      it('should return a default empty object property value', () => {
        expect(ModelUtils.getEmptyObjectPropertyValue()).to.eql({
          results: [],
          add: [],
          remove: [],
          total: 0,
          headers: {}
        });
      });
    });

    describe('getObjectPropertyValue', () => {
      it('should return object property value', () => {
        let model = { results: ['1'] };
        expect(ModelUtils.getObjectPropertyValue(model)).to.eql(['1']);
        model = { value: { results: ['2'] } };
        expect(ModelUtils.getObjectPropertyValue(model)).to.eql(['2']);
      });
    });

    describe('updateObjectProperty', () => {
      it('should not touch instance model if there is no such property in it', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          }
        });
        let propertyName = 'dependsOn';
        let propertyValue = ['emf:222'];

        ModelUtils.updateObjectProperty(instanceModel, propertyName, propertyValue);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          }
        });
      });

      it('should assign a new value to existing property which has undefined value', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          },
          dependsOn: {}
        });
        let propertyName = 'dependsOn';
        let propertyValue = ['emf:222'];

        ModelUtils.updateObjectProperty(instanceModel, propertyName, propertyValue);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          },
          dependsOn: {
            value: {
              results: ['emf:222'],
              add: ['emf:222'],
              remove: [],
              total: 1,
              headers: {}
            },
            defaultValue: {
              results: [],
              total: 0,
              add: [],
              remove: [],
              headers: {}
            }
          }
        });
      });

      it('should update an existing property value', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          },
          dependsOn: {
            value: {
              results: ['emf:222'],
              total: 1
            }
          }
        });
        let propertyName = 'dependsOn';
        let propertyValue = ['emf:333'];

        ModelUtils.updateObjectProperty(instanceModel, propertyName, propertyValue);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          },
          dependsOn: {
            value: {
              results: ['emf:222', 'emf:333'],
              add: ['emf:333'],
              remove: [],
              total: 2,
              headers: {}
            },
            defaultValue: {
              results: [],
              total: 0,
              add: [],
              remove: [],
              headers: {}
            }
          }
        });
      });

      it('should not duplicate values', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111', 'emf:222', 'emf:333'],
              add: ['emf:222'],
              total: 3
            }
          }
        });
        let propertyName = 'references';
        let propertyValue = ['emf:222', 'emf:444'];

        ModelUtils.updateObjectProperty(instanceModel, propertyName, propertyValue);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111', 'emf:222', 'emf:333', 'emf:444'],
              add: ['emf:222', 'emf:444'],
              remove: [],
              total: 4,
              headers: {}
            },
            defaultValue: {
              results: [],
              total: 0,
              add: [],
              remove: [],
              headers: {}
            }
          }
        });
      });

      it('should add multiple values at once', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          },
          dependsOn: {
            value: {
              results: ['emf:222'],
              total: 1
            }
          }
        });
        let propertyName = 'dependsOn';
        let propertyValue = ['emf:333', 'emf:444'];

        ModelUtils.updateObjectProperty(instanceModel, propertyName, propertyValue);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          },
          dependsOn: {
            value: {
              results: ['emf:222', 'emf:333', 'emf:444'],
              add: ['emf:333', 'emf:444'],
              remove: [],
              total: 3,
              headers: {}
            },
            defaultValue: {
              results: [],
              total: 0,
              add: [],
              remove: [],
              headers: {}
            }
          }
        });
      });
    });

    describe('setObjectPropertyHeader', () => {
      it('should set an object property header in its value', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111'],
              total: 1
            }
          }
        });

        ModelUtils.setObjectPropertyHeader(instanceModel, 'references', 'emf:111', 'Compact header', HEADER_COMPACT);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111'],
              total: 1,
              headers: {
                'emf:111': {
                  id: 'emf:111',
                  compact_header: 'Compact header'
                }
              }
            }
          }
        });

        // Should work the same way if only property is passed
        let instanceModelProperty = new InstanceModelProperty({
          value: {
            results: ['emf:111'],
            total: 1
          }
        });

        ModelUtils.setObjectPropertyHeader(instanceModelProperty, null, 'emf:111', 'Compact header', HEADER_COMPACT);

        expect(instanceModelProperty.serialize()).to.eql({
          value: {
            results: ['emf:111'],
            total: 1,
            headers: {
              'emf:111': {
                id: 'emf:111',
                compact_header: 'Compact header'
              }
            }
          }
        });
      });

      it('should update an existing object property header if it exists', () => {
        let instanceModel = new InstanceModel({
          references: {
            value: {
              results: ['emf:111'],
              total: 1,
              headers: {
                'emf:111': {
                  id: 'emf:111',
                  default_header: 'Default header'
                }
              }
            }
          }
        });

        ModelUtils.setObjectPropertyHeader(instanceModel, 'references', 'emf:111', 'Compact header', HEADER_COMPACT);

        expect(instanceModel.serialize()).to.eql({
          references: {
            value: {
              results: ['emf:111'],
              total: 1,
              headers: {
                'emf:111': {
                  id: 'emf:111',
                  default_header: 'Default header',
                  compact_header: 'Compact header'
                }
              }
            }
          }
        });
      });
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

  describe('buildPreviewTextField', () => {
    it('should create a model for text field rendered in preview mode', () => {
      expect(ModelUtils.buildPreviewTextField('title')).to.eql({
        identifier: 'title',
        displayType: 'READ_ONLY',
        previewEmpty: true,
        dataType: 'text',
        label: '',
        maxLength: 40,
        isMandatory: false,
        rendered: true,
        validators: []
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
      });
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

    it('should return type=text when called with field model that has dataType=ANY and is object property ', () => {
      let testField = ViewModelBuilder.createField('testField', 'EDITABLE', 'any', 'test field', false, false, []);
      testField.isDataProperty = false;
      let type = ModelUtils.defineControlType(testField);
      expect(type).to.equal('text');
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

    it('should not return predefined control type when the control is DEFAULT_VALUE_PATTERN', () => {
      let fieldWithControl = ViewModelBuilder.createField('generated', 'EDITABLE', 'text', 'Generated', true, true, [], DEFAULT_VALUE_PATTERN);
      expect(ModelUtils.defineControlType(fieldWithControl)).to.equal('text');
    });

    it('should return codelist type', () => {
      let fieldWithControl = ViewModelBuilder.createField('status', 'EDITABLE', 'ANY', 'Generated', true, true, [], null, 210);
      expect(ModelUtils.defineControlType(fieldWithControl)).to.equal('codelist');
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
    let textareaMinCharsLength = 60;
    it('should return type=textarea if current model item has property maxLength greater than configured minimum', function () {
      let type = ModelUtils.getTextFieldType({
        'maxLength': 61
      }, textareaMinCharsLength);
      expect(type).to.equal('textarea');
    });

    it('should return type=text if current model item has property maxLength equal to the configured minimum', function () {
      let type = ModelUtils.getTextFieldType({
        'maxLength': 60
      }, textareaMinCharsLength);
      expect(type).to.equal('text');
    });

    it('should return type=text if current model item has dataType=text and maxLength less than the configured minimum', function () {
      let type = ModelUtils.getTextFieldType({
        'maxLength': 59
      }, textareaMinCharsLength);
      expect(type).to.equal('text');
    });
  });

  it('should filter recursively fields from the model based on a filter function', function () {
    let viewModel = new ViewModelBuilder()
      .addField('1', 'EDITABLE', 'text', 'field 1', false, false, [])
      .addRegion('2', 'Input fields', 'EDITABLE', false, false)
      .addField('2-1', 'EDITABLE', 'text', 'field 2-1', false, false, [])
      .addField('2-2', 'SYSTEM', 'text', 'field 2-2', false, false, [])
      .endRegion()
      .addRegion('3', 'System fields', 'EDITABLE', false, false)
      .addField('3-1', 'SYSTEM', 'text', 'field 3-1', false, false, [])
      .addField('3-2', 'SYSTEM', 'text', 'field 3-2', false, false, [])
      .endRegion()
      .addField('4', 'SYSTEM', 'text', 'field 4', false, false, [])
      .getModel();

    // assumption: regions are not stored in the validation model
    var validatomModel = {
      '1': {},
      '2': {},
      '2-1': {},
      '2-2': {},
      '3-1': {},
      '3-2': {},
      '3': {},
      '4': {}
    };


    ModelUtils.filterFields(viewModel.fields, validatomModel, (viewModelField) => {
      return viewModelField.displayType === 'EDITABLE';
    });

    var validationModelKeys = Object.keys(validatomModel);

    expect(validationModelKeys).to.eql(['1', '2', '2-1']);
  });

  it('should traverse hierarchical view model', function () {
    let viewModel = new ViewModelBuilder()
      .addField('1', 'EDITABLE', 'text', 'field 1', false, false, [])
      .addRegion('2', 'Input fields', 'EDITABLE', false, false)
      .addField('2-1', 'EDITABLE', 'text', 'field 2-1', false, false, [])
      .addField('2-2', 'SYSTEM', 'text', 'field 2-2', false, false, [])
      .endRegion()
      .addRegion('3', 'System fields', 'EDITABLE', false, false)
      .addField('3-1', 'SYSTEM', 'text', 'field 3-1', false, false, [])
      .addField('3-2', 'SYSTEM', 'text', 'field 3-2', false, false, [])
      .endRegion()
      .addField('4', 'SYSTEM', 'text', 'field 4', false, false, [])
      .getModel();

    // assumption: regions are not stored in the validation model
    var validatomModel = {
      '1': {},
      '2': {},
      '2-1': {},
      '2-2': {},
      '3-1': {},
      '3-2': {},
      '3': {},
      '4': {}
    };

    var visited = {};

    ModelUtils.walkModelTree(viewModel.fields, validatomModel, (viewModelField) => {
      visited[viewModelField.identifier] = '';
    });

    var validationModelKeys = Object.keys(validatomModel);

    var visitedKeys = Object.keys(visited);

    expect(validationModelKeys).to.eql(visitedKeys);
  });

  describe('updateObjectPropertyValue', () => {
    it('should remove property of single value ', () => {

      let propertyValue = createObjectPropertyValue(['emf:id']);
      let instanceModelProperty = new InstanceModelProperty({value: propertyValue});

      ModelUtils.updateObjectPropertyValue(instanceModelProperty, true);
      expect(propertyValue.results.length === 0).to.be.true;
      expect(propertyValue.remove).to.deep.equal(['emf:id']);
      expect(propertyValue.add.length === 0).to.be.true;
    });
  });

  function createObjectPropertyValue(results = []) {
    let propertyValue = ModelUtils.getEmptyObjectPropertyValue();
    propertyValue.total = results.length;
    propertyValue.results = results;
    return propertyValue;
  }
});