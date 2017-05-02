import {DefinitionModel, DefinitionModelProperty} from 'models/definition-model';

describe('DefinitionModel', ()=> {
  it('should initialize proper definition  flat model with fed data', ()=> {
    let definitionModel = new DefinitionModel(testData);
    let flatData = flatten(testData);
    Object.keys(definitionModel.definitionTree).forEach((property)=> {
      if (definitionModel.definitionTree[property] instanceof Array) {
        let definitionRegion = definitionModel.definitionTree[property];
        let testDataRegion = testData[property];
        definitionRegion.forEach((definitionProperty, index)=> {
          expect(definitionProperty instanceof DefinitionModelProperty).to.be.true;
          expect(definitionProperty.serialize()).to.eql(testDataRegion[index]);
        });
      } else {
        expect(definitionModel.definitionTree[property].modelProperty).to.equal(testData[property]);
      }
    });
    definitionModel.flatDefinitions.forEach((element, index)=> {
      expect(element instanceof DefinitionModelProperty).to.be.true;
      expect(element.serialize()).to.eql(flatData[index])
    });
    expect(definitionModel.flatDefinitions.length).to.equal(10);
  });

  it('should serialize data properly', ()=> {
    let definitionModel = new DefinitionModel(testData);
    expect(definitionModel.serialize()).to.eql(testData);
  });

  it('should initialize getter and setters with fed single data property', ()=> {
    let definitionProperty = new DefinitionModelProperty(testData.label, 'label');
    expect(definitionProperty.label).to.equal('UI2 Document for testing');
  });

  it('should initialize properly properly with an object for data property', ()=> {
    let definitionProperty = new DefinitionModelProperty(testData.fields[0]);
    expect(definitionProperty.displayType).to.equal('EDITABLE');
    expect(definitionProperty.isMandatory).to.be.false;
    expect(definitionProperty.dataType).to.eql({name: 'checkbox'});
    expect(definitionProperty.label).to.equal('Checkbox');
    expect(definitionProperty.name).to.equal('checkbox');
    expect(definitionProperty.previewEmpty).to.be.true;
    expect(definitionProperty.selected).to.be.true;
    expect(definitionProperty.modelProperty).to.eql(testData.fields[0]);
  });

  it('should properly edit existing property and emit propertyChanged event', ()=> {
    let value = '';
    let definitionModel = new DefinitionModel(testData);
    definitionModel.definitionTree.label.subscribe('propertyChanged', (received)=> {
      expect(received.label).to.equal('testLabel');
    });
    definitionModel.definitionTree.label = 'testLabel';
    expect(definitionModel.definitionTree.label).to.equal('testLabel');
  });

  it('should emmit property changed event on single definitionProperty',()=> {
    let value = '';
    let definitionModel = new DefinitionModel(testData);
    definitionModel.identifier.subscribe('propertyChanged', (received)=> {
      expect(received.identifier).to.equal('test');
    });
    definitionModel.definitionTree.identifier.identifier = 'test';
    expect(definitionModel.definitionTree.identifier.identifier).to.equal('test');
  });

  it('should add new fields to model',()=> {
    let definitionModel = new DefinitionModel(testData);
    let newFields = [
      {
        identifier: 'TestField',
        defaultValue: 'TestField',
        displayType: 'EDITABLE',
        dataType: {name: 'text'},
        isMandatory: false,
        label: 'Test Field',
        name: 'TestField',
        previewEmpty: true,
        selected: true
      }];
    definitionModel.addFieldsToModel(newFields);
    expect(definitionModel.serialize().fields[6]).to.eql(newFields[0])
  });

  function flatten(data) {
    let result;
    Object.keys(data).forEach((field)=> {
      if (field === 'fields') {
        result = flattenField(data[field]);
      }
    });
    return result;
  }

  function flattenField(data) {
    let flatRegion = [];
    data.forEach((field)=> {
      if (field.fields) {
        flatRegion.push(...flattenField(field.fields));
        flatRegion.push(field)
      } else {
        flatRegion.push(field);
      }
    });
    return flatRegion;
  }

  var testData = {
    identifier: 'UI210001',
    label: 'UI2 Document for testing',
    fields: [
      {
        displayType: 'EDITABLE',
        isMandatory: false,
        dataType: {name: 'checkbox'},
        label: 'Checkbox',
        name: 'checkbox',
        previewEmpty: true,
        selected: true
      },
      {
        defaultValue: 'COL1',
        displayType: 'EDITABLE',
        dataType: {name: 'text'},
        isMandatory: false,
        label: 'Radiobutton Group',
        name: 'radiobuttonGroup',
        previewEmpty: true,
        selected: true
      },
      {
        conditions: [],
        displayType: 'EDITABLE',
        fields: [{
          codelist: 239,
          dataType: {},
          displayType: 'EDITABLE',
          isMandatory: false,
          label: 'Activity Type',
          name: 'activityType',
          previewEmpty: true,
          selected: true
        },
          {
            codelist: 237,
            dataType: {},
            displayType: 'EDITABLE',
            isMandatory: false,
            label: 'Examination purpose',
            name: 'examinationPurpose',
            previewEmpty: false,
            selected: true
          },
          {
            codelist: 240,
            dataType: {},
            displayType: 'EDITABLE',
            isMandatory: false,
            label: 'Treatment purpose',
            name: 'treatmentPurpose',
            previewEmpty: false,
            selected: true
          },
          {
            codelist: 504,
            dataType: {},
            defaultValue: 'ALL',
            displayType: 'EDITABLE',
            isMandatory: true,
            label: 'Functional',
            name: 'functional',
            previewEmpty: true,
            selected: true
          }],
        identifier: 'specificDetails',
        label: 'Specific Details'
      },
      {
        dataType: {},
        defaultValue: '${today}',
        displayType: 'EDITABLE',
        isMandatory: false,
        label: 'Planned due date',
        name: 'plannedStartDate',
        previewEmpty: true,
        selected: true,
      },
      {
        dataType: {},
        displayType: 'READ_ONLY',
        isMandatory: false,
        label: 'Locked by',
        name: 'lockedBy',
        controlDefinition: {
          controlParams: [{
            name: 'range',
            value: 'emf:User'
          }],
          fields: [{
            displayType: 'HIDDEN',
            isMandatory: false,
            label: 'Black',
            name: 'COL3',
            previewEmpty: true,
            fields: [
              {
                displayType: 'HIDDEN',
                isMandatory: false,
                label: 'Green',
                name: 'COL2',
                previewEmpty: true,
                fields: [
                {
                  displayType: 'HIDDEN',
                  isMandatory: false,
                  label: 'Green',
                  name: 'COL2',
                  previewEmpty: true
                }]
              },
              {
                displayType: 'HIDDEN',
                isMandatory: false,
                label: 'Yellow',
                name: 'COL1',
                previewEmpty: true
              }
            ]
          }, {
            displayType: 'HIDDEN',
            isMandatory: false,
            label: 'Green',
            name: 'COL2',
            previewEmpty: true
          }, {
            displayType: 'HIDDEN',
            isMandatory: false,
            label: 'Yellow',
            name: 'COL1',
            previewEmpty: true
          }],
          identifier: 'PICKER'
        }
      },
      {
        controlDefinition: {
          controlParams: [{
            name: 'range',
            value: 'emf:User'
          }],
          fields: {},
          identifier: 'PICKER'
        },
        dataType: {},
        displayType: 'READ_ONLY',
        isMandatory: false,
        label: 'Locked by',
        name: 'lockedBy',
        previewEmpty: false,
        selected: true
      }]
  };
});