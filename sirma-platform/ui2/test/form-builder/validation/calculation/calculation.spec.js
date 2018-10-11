import {Calculation} from 'form-builder/validation/calculation/calculation';
import {InstanceModel} from 'models/instance-model';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';
import {Eventbus} from 'services/eventbus/eventbus';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {HeadersService} from 'instance-header/headers-service';
import {Logger} from 'services/logging/logger';

describe('Calculation validator', function () {

  let expressionTemplateService = {};
  expressionTemplateService.evaluateValues = sinon.spy(() => {
    return PromiseStub.resolve({
      data: {data: [{id: 'emf:123456', properties: [{propertyName: 'emf:creator.name', propertyValue: 'FirstName'}]}]}
    });
  });

  let configuration = {
    get: () => {
      return sinon.spy();
    }
  };
  let momentAdapter = {
    format: () => {
      return '09.07.2017';
    }
  };
  let labelRestService = {
    getDefinitionLabels: () => {
      return sinon.spy();
    }
  };

  let promiseAdapter = {
    resolve: (data) => PromiseStub.resolve(data),
    all: () => {
      return PromiseStub.resolve([
        {data: {data: [{id: 'emf:123456', properties: [{propertyName: 'emf:references.type', propertyValue: '001'}]}]}},
        {data: {expressionLabel: '#Expression#', sequenceLabel: '#Sequence#'}}
      ]);
    },
    promise: () => {
      return PromiseStub.resolve({
        data: {data: [{id: 'emf:123456', properties: [{propertyName: 'emf:creator.name', propertyValue: 'FirstName'}]}]}
      });
    }
  };

  let headersServiceStub = stub(HeadersService);
  let loggerStub = stub(Logger);

  let validator = new Calculation(expressionTemplateService, configuration, momentAdapter, promiseAdapter, labelRestService, mockTimeout(), stubEventbus(), headersServiceStub, loggerStub);

  it('should not suggest and change template if instance is persisted', function () {
    let validationModel =  new InstanceModel({identifier: {value: 'emf:123456'}, generatedField: {value: 'Document with description: Saved instance'}, description: {value: 'test description'}});
    let validatorDef = {'rules': [{'context': {'bindings': ['dcterms:description'], functions: []}}]};
    validator.validate('generatedField', validatorDef, validationModel, {});
    expect(validationModel['generatedField'].value).to.equal('Document with description: Saved instance');
  });

  it('should not suggest and change template if field value is edited by user', function () {
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: 'Document with description: Edited by user'}});
    let flatModel = {generatedField: { isDataProperty: true, editedByUser: true}};
    let validatorDef = {'rules': [{'context': {'bindings': ['dcterms:description'], functions: []}}]};
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with description: Edited by user');
  });

  it('should not suggest and change template if model is partially loaded',()=>{
    let validationModel = new InstanceModel({field: {value: 'Document with description: Edited by user'}});
    let flatModel = {field:{isDataProperty: true, editedByUser: false}};
    let validatorDef = {'rules':[{'context':{'bindings':['dcterms:description'],functions:[]}}]};
    validator.validate('field',validatorDef,validationModel,flatModel);
    expect(validationModel['field'].value).to.equal('Document with description: Edited by user');
  });

  it('should not suggest and change template if field is bind to itself', function () {
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: 'Document with description: Linked to itself'}, description: {value: 'test description'}});
    let flatModel = {generatedField: {isDataProperty: true, uri: 'emf:generatedField'}};
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:generatedField'], functions: []}}]};
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with description: Linked to itself');
  });

  it('should replace function values in template with user friendly labels', function () {
    let template = 'Template with functions unique number: {${sequenceName}|sequenceLabel} expression: {${expression}|expressionLabel}';
    let controlParams = {expressionlabel: '${expression}', sequencelabel: '${sequenceName}'};
    let functions =  ['expressionLabel', 'sequenceLabel'];
    let labels = {expressionLabel: '#Expression#', sequenceLabel: '#Sequence#'};
    template = validator.replaceFunctionsInTemplate(template, functions, controlParams, labels);
    expect(template).to.equal('Template with functions unique number: #Sequence# expression: #Expression#');
  });

  it('should not change template if there\'s not function placeholders', function () {
    let template = 'Template without functions';
    let controlParams = {expressionlabel: '${expression}', sequencelabel: '${sequenceName}'};
    let labels = {expressionLabel: '#Expression#', sequenceLabel: '#Sequence#'};
    template = validator.replaceFunctionsInTemplate(template, undefined, controlParams, labels);
    expect(template).to.equal('Template without functions');
  });

  it('should fill template bindings with values from server', function () {
    let template = 'Document created by $[emf:creator.name]';
    let data = [{id: 'emf:123456', properties: [{propertyName: 'emf:creator.name', propertyValue: 'Stella'}]}];
    template = validator.applyServerValues(data, template);
    expect(template).to.equal('Document created by Stella');
  });

  it('should fill template bindings with empty string if server return null', function () {
    let template = 'Document created by $[emf:creator.name]';
    let data = [{id: 'emf:123456', properties: [{propertyName: 'emf:creator.name', propertyValue: null}]}];
    template = validator.applyServerValues(data, template);
    expect(template).to.equal('Document created by ');
  });

  it('should fill template bindings with correct string if server return false', function () {
    let template = 'Document created by $[emf:creator.selected]';
    let data = [{id: 'emf:123456', properties: [{propertyName: 'emf:creator.selected', propertyValue: false}]}];
    template = validator.applyServerValues(data, template);
    expect(template).to.equal('Document created by false');
  });

  it('should find placeholder in template and replace it with field value', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['dcterms:description'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, description: {value: 'test description'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document with description: $[dcterms:description]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      description: {isDataProperty: true, identifier: 'description', uri: 'dcterms:description'}
    };
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with description: test description');
  });

  it('should find placeholder in template and replace all occurance with field value', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['dcterms:description'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, description: {value: 'test description'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document with description: $[dcterms:description] and description: $[dcterms:description]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      description: {isDataProperty: true, identifier: 'description', uri: 'dcterms:description'}
    };
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with description: test description and description: test description');
  });

  it('should find placeholder in template and replace it with field value label', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:type'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, type: {value: '001', valueLabel: 'type 1'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document of type: $[emf:type]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      type: {isDataProperty: true, identifier: 'type', uri: 'emf:type'}
    };
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document of type: type 1');
  });

  it('should find placeholder in template and replace it with formatted value if field is of type date', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:planedStartDate'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, planedStartDate: {value: '2017-07-09T19:20:30.45+01:00'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document with start date: $[emf:planedStartDate]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      planedStartDate: {isDataProperty: true, identifier: 'planedStartDate', uri: 'emf:planedStartDate', dataType: 'date'}};
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with start date: 09.07.2017');
  });

  it('should find placeholder in template and replace it with formatted value if field is of type datetime', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:planedStartDate'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, planedStartDate: {value: '2017-07-09T19:20:30.45+01:00'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document with start date: $[emf:planedStartDate]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      planedStartDate: {isDataProperty: true, identifier: 'planedStartDate', uri: 'emf:planedStartDate', dataType: 'datetime'}};
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with start date: 09.07.2017');
  });

  it('should find placeholder in template and replace it with formatted value if field is of type checkbox', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:flag'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, flag: {value: 'true'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Boolean value: $[emf:flag]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      flag: {isDataProperty: true, identifier: 'flag', uri: 'emf:flag', dataType: 'boolean'}
    };
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Boolean value: true');
  });

  it('should find placeholder in template and replace it with label if field is of type radio button', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:radio'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, radioGroup: {value: 'col1'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document with radio grp: $[emf:radio]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      radioGroup: {isDataProperty: true, identifier: 'radioGroup', uri: 'emf:radio', controlId: 'RADIO_BUTTON_GROUP', control: [{identifier: 'RADIO_BUTTON_GROUP', controlFields: [{identifier: 'col1', label: 'radio 1'}]}]}};
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document with radio grp: radio 1');
  });

  it('should find placeholder in template and replace it with breadcrumb header if field is object property', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:createdBy'], functions: []}}]};
    let validationModel =  new InstanceModel({
      identifier: {value: 'NO_ID'},
      generatedField: {value: ''},
      createdBy: {
        value: {
          'total': 5,
          'offset': 0,
          'limit': 5,
          'results': [
            '1'
          ],
          headers: {
            '1': '<span>Admin admin</span>'
          }
        }
      }
    });
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document created by: $[emf:createdBy]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      createdBy: {isDataProperty: true, identifier: 'createdBy', uri: 'emf:createdBy', controlId: 'PICKER', control: [{identifier: 'PICKER'}]}};
    let formControl = {};
    validator.eventsMap.clear();
    validator.headersService.loadHeaders.returns(PromiseStub.resolve({ 'createdBy': { id: 'createdBy', breadcrumb_header: '<span>Admin admin</span>'}}));

    validator.validate('generatedField', validatorDef, validationModel, flatModel, formControl);

    expect(validationModel['generatedField'].value).to.equal('Document created by: Admin admin');
  });

  it('should not change template if there\'s no match in model', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:title'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, type: {value: 'test title'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document of type: $[emf:type]'}, identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text'},
      title: {isDataProperty: true, identifier: 'title', uri: 'emf:type'}};
    validator.eventsMap.clear();
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('Document of type: $[emf:type]');
  });

  it('should build correct payload including object properties id', function () {
    let unresolvedBindings = ['emf:owner.firstName', 'emf:owner.LastName', 'emf:createdBy.email'];
    let flatModel = {
      owner: {isDataProperty: false, identifier: 'owner', uri: 'emf:owner'},
      creator: {isDataProperty: false, identifier: 'createdBy', uri: 'emf:createdBy'},
      generatedField: {isDataProperty: true, identifier: 'generatedField', uri: 'emf:generatedField'}};
    let validationModel =  new InstanceModel({owner: {value: {results: ['emf:123456']}}, createdBy: {value: {results: ['emf:123456']}}});
    let payload = validator.buildPayload(unresolvedBindings, flatModel, validationModel, 'generatedField');
    expect(payload).to.eql([
      {id: 'emf:123456', target: 'emf:owner.firstName', source: 'generatedField'},
      {id: 'emf:123456', target: 'emf:owner.LastName', source: 'generatedField'},
      {id: 'emf:123456', target: 'emf:createdBy.email', source: 'generatedField'}
    ]);
  });

  it('should build empty payload if property is not set', function () {
    let unresolvedBindings = ['emf:owner.name', 'emf:createdBy.email'];
    let flatModel = {
      owner: {isDataProperty: false, identifier: 'owner', uri: 'emf:owner'},
      creator: {isDataProperty: false, identifier: 'createdBy', uri: 'emf:createdBy'}};
    let validationModel =  new InstanceModel({owner: {value: []}, createdBy: {value: []}});
    let payload = validator.buildPayload(unresolvedBindings, flatModel, validationModel);
    expect(payload).to.eql([]);
  });

  it('should build empty payload if property is set with more then one value', function () {
    let unresolvedBindings = ['emf:dependsOn.description'];
    let flatModel = {dependsOn: {isDataProperty: false, identifier: 'dependsOn', uri: 'emf:dependsOn'}};
    let validationModel = new InstanceModel({dependsOn: {value: [{id: 'instance-id-1'}, {id: 'instance-id-2'}]}});
    let payload = validator.buildPayload(unresolvedBindings, flatModel, validationModel);
    expect(payload).to.eql([]);
  });

  it('should restore original functions placeholders before save', function () {
    let template = 'Template with unique number: #Sequence# and expression: #Expression#';
    let controlParams = {expressionlabel: '${get([title])}', sequencelabel: '${seq({+eaiSequence})}'};
    validator.functionsLabels = new Map();
    validator.functionsLabels.set('generatedField', {expressionlabel: '#Sequence#', sequencelabel: '#Expression#'});
    template = validator.restoreOriginalFunctionsPlaceholders(template, controlParams, 'generatedField');
    expect(template).to.equal('Template with unique number: ${get([title])} and expression: ${seq({+eaiSequence})}');
  });

  it('should not make any changes in template if labels for given field are not provided', function () {
    let template = 'Template with unique number: #Sequence# and expression: #Expression#';
    let controlParams = {expressionlabel: '${get([title])}', sequencelabel: '${seq({+eaiSequence})}'};
    validator.functionsLabels = new Map();
    template = validator.restoreOriginalFunctionsPlaceholders(template, controlParams, 'generatedField');
    expect(template).to.equal('Template with unique number: #Sequence# and expression: #Expression#');
  });

  it('should not subscribe for BeforeIdocSaveEvent if field control is different than text', function () {
    validator.eventsMap = new Map();
    let flatModel = {generatedField: {isDataProperty: true, control: [{identifier: DEFAULT_VALUE_PATTERN}], dataType: 'codelist', editedByUser: false}};
    validator.subscribeBeforeIdocSaveEvent('generatedField', flatModel);
    expect(validator.eventbus.subscribe.calledOnce).to.be.false;
  });

  it('should subscribe for BeforeIdocSaveEvent', function () {
    validator.eventsMap = new Map();
    let flatModel = {generatedField: {isDataProperty: true, control: [{identifier: DEFAULT_VALUE_PATTERN}], dataType: 'text', editedByUser: false}};
    validator.subscribeBeforeIdocSaveEvent('generatedField', flatModel);
    expect(validator.eventbus.subscribe.getCall(0).args[0]).to.eq(BeforeIdocSaveEvent);
  });

  it('should restore original functions placeholders when BeforeIdocSaveEvent is fired', () => {
    let validator = new Calculation(expressionTemplateService, configuration, momentAdapter, promiseAdapter, labelRestService, mockTimeout(), new Eventbus());
    let data = {properties: {generatedField: 'Template with unique number: #Sequence# and expression: #Expression#'}};
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{identifier: DEFAULT_VALUE_PATTERN, controlParams: {expressionlabel: '${get([title])}', sequencelabel: '${seq({+eaiSequence})}'}}], dataType: 'text', editedByUser: false}
    };
    validator.functionsLabels = new Map();
    validator.functionsLabels.set('generatedField', {expressionlabel: '#Sequence#', sequencelabel: '#Expression#'});
    validator.subscribeBeforeIdocSaveEvent('generatedField', flatModel, 'generatedField');

    validator.eventbus.publish(new BeforeIdocSaveEvent(data));
    expect(flatModel['generatedField'].editedByUser).to.be.false;
    expect(data.properties.generatedField).to.equal('Template with unique number: ${get([title])} and expression: ${seq({+eaiSequence})}');
  });

  it('should not try to restore original functions placeholders if no needed data is send', () => {
    let validator = new Calculation(expressionTemplateService, configuration, momentAdapter, promiseAdapter, labelRestService, mockTimeout(), new Eventbus());
    let data = {properties: {}};
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{identifier: DEFAULT_VALUE_PATTERN, controlParams: {expressionlabel: '${get([title])}', sequencelabel: '${seq({+eaiSequence})}'}}],  dataType: 'text', editedByUser: false}
    };
    validator.subscribeBeforeIdocSaveEvent('generatedField', flatModel);
    validator.eventbus.publish(new BeforeIdocSaveEvent(data));
    expect(data.properties.generatedField).to.equal(undefined);
  });

  it('should set code value if the field is of type codelist ', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:type'], functions: []}}]};
    let validationModel =  new InstanceModel({
      identifier: {
        value: 'NO_ID'
      },
      generatedField: {
        value: ''
      },
      type: {
        value: '001',
        valueLabel: 'type 1'
      }
    });
    let flatModel = {
      generatedField: {
        isDataProperty: true,
        control: [
          {
            controlParams: {
              template: 'Document of type: $[emf:type]'
            },
            identifier: DEFAULT_VALUE_PATTERN
          }
        ]
      },
      type: {
        identifier: 'type',
        uri: 'emf:type',
        isDataProperty: true
      }
    };
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('001');
  });

  it('should set code value returned from server if the field is of type codelist ', function () {
    let validatorDef = {'rules': [{'context': {'bindings': ['emf:type'], functions: []}}]};
    let validationModel =  new InstanceModel({identifier: {value: 'NO_ID'}, generatedField: {value: ''}, type: { valueLabel: 'type 1'}});
    let flatModel = {
      generatedField: {isDataProperty: true, control: [{controlParams: {template: 'Document of type: $[emf:type]'}, identifier: DEFAULT_VALUE_PATTERN}], isDataProperty: true},
      type: {isDataProperty: true, identifier: 'type', uri: 'emf:type'}
    };
    validator.validate('generatedField', validatorDef, validationModel, flatModel);
    expect(validationModel['generatedField'].value).to.equal('001');
  });

  it('should clear events map and functions labels map if definitionId is changed ', function () {
    validator.eventsMap = new Map();
    validator.functionsLabels = new Map();
    let eventbus = stubEventbus();
    validator.eventsMap.set('generatedField', eventbus);
    validator.functionsLabels.set('test', 'test label');
    validator.definitionId = 'emf:123';
    validator.clearEventsMap('emf:456');
    expect(eventbus.unsubscribe.calledOnce).to.be.true;
    expect(validator.eventsMap.size).to.equal(0);
    expect(validator.functionsLabels.size).to.equal(0);
  });

  describe('parseObjectsBindingResponseToObjectValue', () => {
    it('should parse non empty response', () => {
      let idOne = 'emf:0001';
      let headerOne = 'header 1';
      let idTwo = 'emf:0002';
      let headerTwo = 'header 2';

      let objectsBindingResponse = JSON.stringify([{id: idOne, headers:{compact_header: headerOne}}, {id: idTwo, headers:{compact_header: headerTwo}}]);

      let result = validator.parseObjectsBindingResponseToObjectValue(objectsBindingResponse);
      expect(result).to.eql({
        results: [idOne, idTwo],
        add: [idOne, idTwo],
        remove: [],
        total: 2,
        headers: {
          [idOne]: { id: idOne, compact_header: headerOne },
          [idTwo]: { id: idTwo, compact_header: headerTwo }
        }
      });
    });

    it('should return empty object property value when response is empty', () => {
      let result = validator.parseObjectsBindingResponseToObjectValue(null);
      expect(result).to.eql({
        results: [],
        total: 0,
        add: [],
        remove: [],
        headers: {}
      });
    });
  });

  it('hasValue', () => {
    let testData = [
      { model: null, expectedOutcome: false },
      { model: undefined, expectedOutcome: false },
      { model: 'string', expectedOutcome: true },
      { model: [], expectedOutcome: true }, // ?
      { model: ['1'], expectedOutcome: true },
      { model: '', expectedOutcome: false },
      { model: { results: []}, expectedOutcome: false},
      { model: { results: ['1']}, expectedOutcome: true}
    ];
    testData.forEach((set) => {
      let result = Calculation.hasValue(set.model);
      expect(result, `Should return ${set.expectedOutcome} with model "${set.model}"`).to.equal(set.expectedOutcome);
    });
  });

  function mockTimeout() {
    return sinon.spy((func) => {
      func();
    });
  }

  function stubEventbus() {
    return stub(Eventbus);
  }

});