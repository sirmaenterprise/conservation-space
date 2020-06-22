import {AddRelationAction} from 'idoc/actions/add-relation-action';
import {stub} from 'test/test-utils';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {MULTIPLE_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import {PickerService, SEARCH_EXTENSION, BASKET_EXTENSION} from 'services/picker/picker-service';
import {ActionsService} from 'services/rest/actions-service';
import {PromiseStub} from 'test/promise-stub';
import {NotificationService} from 'services/notification/notification-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {InstanceRestService} from 'services/rest/instance-service';
import {Configuration} from 'common/application-config';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

describe('AddRelationAction', () => {

  const CURRENT_OBJECT_ID = 'emf:0001';

  // constants for hasAttachment property of current object
  const HAS_ATTACHMENT_PROPERTY_NAME = 'hasAttachment';
  const HAS_ATTACHMENT_PROPERTY_VALUE = {results: ['1', '2', '3', '6']};
  const HAS_ATTACHMENT_PROPERTY_LABEL = 'Has attachment';
  const HAS_ATTACHMENT_PROPERTY_URI = 'emf:hasAttachment';

  // constants for hasWatchers property of current object
  const HAS_WATCHERS_PROPERTY_NAME = 'hasWatchers';
  const HAS_WATCHERS_PROPERTY_VALUE = {results: ['2', '6', '222']};
  const HAS_WATCHERS_PROPERTY_LABEL = 'Has watchers';
  const HAS_WATCHERS_PROPERTY_URI = 'emf:hasWatchers';

  // constants for someRelation property of current object
  const HAS_SOME_RELATION_PROPERTY_NAME = 'someRelation';
  const HAS_SOME_RELATION_PROPERTY_VALUE = {results: ['55', '2', '3', '6']};
  const HAS_SOME_RELATION_PROPERTY_LABEL = 'Some relation';
  const HAS_SOME_RELATION_PROPERTY_URI = 'emf:someRelation';

  let addRelationAction;
  let pickerService;
  let actionsService;
  let notificationService;
  let eventbus;
  let instanceObject;
  let instanceRestService;
  let configuration;

  beforeEach(() => {
    instanceObject = createInstanceObject();
    pickerService = stub(PickerService);
    actionsService = stub(ActionsService);
    notificationService = stub(NotificationService);
    eventbus = stub(Eventbus);
    instanceRestService = stub(InstanceRestService);
    instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({data: []}));
    configuration = stub(Configuration);
    configuration.get.returns(3);
    addRelationAction = new AddRelationAction(pickerService, actionsService, notificationService, eventbus, PromiseStub, instanceRestService, configuration);
  });

  describe('execute', () => {
    it('should call promise reject when new selected items are same as old selected items', (done) => {
      let action = {
        action: 'user-operation',
        configuration: {
          relation: [HAS_WATCHERS_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI]
        }
      };
      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      let context = {
        currentObject: instanceObject,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER,
        idocContext
      };
      instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({data: ['2', '6', '222']}));
      pickerService.configureAndOpen.returns(PromiseStub.resolve([createValue(2), createValue(6), createValue(222)]));

      addRelationAction.execute(action, context).catch(() => {
        done();
      });
    });

    it('should call update relation when there is a new relations to be removed', () => {
      executeActionAndVerifyUpdateRelationsIsCalled([createValue(2), createValue(6)]);
    });

    it('should call update relation when there is a new relations to be added', () => {
      executeActionAndVerifyUpdateRelationsIsCalled([createValue(2), createValue(6), createValue(222), createValue(99999999)]);
    });

    it('should call update relation when there are new relations to be added and removed', () => {
      executeActionAndVerifyUpdateRelationsIsCalled([createValue(2), createValue(6), createValue(99999999)]);
    });

    function executeActionAndVerifyUpdateRelationsIsCalled(newSelectedItems) {
      let action = {
        action: 'user-operation',
        configuration: {
          relation: [HAS_WATCHERS_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI]
        }
      };
      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      let context = {
        currentObject: instanceObject,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER,
        idocContext
      };
      actionsService.updateRelations.returns(PromiseStub.resolve());
      pickerService.configureAndOpen.returns(PromiseStub.resolve(newSelectedItems));

      addRelationAction.execute(action, context);
      expect(actionsService.updateRelations.calledOnce).to.be.true;
    }
  });

  describe('loadInstanceObjectProperties', () => {
    it('should load instanceObject if context placeholder is not idoc page', (done) => {
      let viewModel = new ViewModelBuilder()
        .addField('propertyName', 'EDITABLE', 'text', undefined, false, false, [], undefined, undefined, false, undefined)
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('propertyName', 'propertyValue')
        .getModel();

      let models = {
        viewModel,
        validationModel
      };

      instanceRestService.loadInstanceObject.returns(PromiseStub.resolve(new InstanceObject(CURRENT_OBJECT_ID, models)));

      let processedObject = createInstanceObject();
      let actionContext = {
        currentObject: processedObject
      };

      addRelationAction.loadInstanceObjectProperties(actionContext).then((loadedInstanceObject) => {
        expect(loadedInstanceObject.id).to.be.equal(CURRENT_OBJECT_ID);
        expect(models).to.be.equal(loadedInstanceObject.models);
        done();
      });
    });

    it('should not load instanceObject if context placeholder is idoc page', (done) => {
      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      let actionContext = {
        currentObject: instanceObject,
        placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER,
        idocContext
      };
      addRelationAction.loadInstanceObjectProperties(actionContext).then(() => {
        expect(actionContext.currentObject).to.be.equal(instanceObject);
        done();
      });

    });
  });

  describe('updateRelations', () => {
    it('should notification service to be called and event be published when response of action service is successful', () => {
      let header = 'header';
      actionsService.updateRelations.returns(PromiseStub.resolve({data: 'the response data'}));

      addRelationAction.updateRelations(null, header);
      expect(notificationService.success.calledOnce).to.be.true;
      expect(notificationService.success.args[0][0]).to.be.equal(header);
      expect(eventbus.publish.calledOnce).to.be.true;
      let expectedPublishData = {response: {data: 'the response data'}};
      expect(eventbus.publish.args[0][0].getData()[0]).to.deep.equal(expectedPublishData);

    });
  });

  it('should build picker configuration based on action configuration', () => {
    let configLabel = 'Header of action';
    let predefinedTypes = ['emf:User'];
    let restrictions = SearchCriteriaUtils.getDefaultRule();
    let action = {
      label: configLabel,
      configuration: {
        predefinedTypes,
        relation: [HAS_ATTACHMENT_PROPERTY_URI],
        restrictions
      }
    };

    let pickerConfiguration = addRelationAction.buildPickerConfig(action, instanceObject);

    expect(pickerConfiguration.header).to.be.equal(configLabel);
    let searchExtensionConfiguration = pickerConfiguration.extensions[SEARCH_EXTENSION];
    expect(searchExtensionConfiguration.predefinedTypes).to.be.equal(predefinedTypes);
    expect(searchExtensionConfiguration.results.config.selection).to.be.equal(SINGLE_SELECTION);
    expect(searchExtensionConfiguration.results.config.exclusions).to.deep.equal([CURRENT_OBJECT_ID]);
    expect(searchExtensionConfiguration.restrictions).to.deep.equal(restrictions);
  });

  describe('getSelectedItems', () => {

    it('should return intersection of values when there are no relations #1', (done) => {
      instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({data: ['1', '2', '3', '6']}));
      addRelationAction.getSelectedItems([HAS_ATTACHMENT_PROPERTY_URI], instanceObject).then((selectedItems) => {
        expect(selectedItems).to.deep.equal([
          {id: '1'},
          {id: '2'},
          {id: '3'},
          {id: '6'}
        ]);
        done();
      });
    });

    it('should return intersection of values when there are no relations #2', (done) => {
      instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({data: ['2', '3', '6']}));
      addRelationAction.getSelectedItems([HAS_ATTACHMENT_PROPERTY_URI, HAS_SOME_RELATION_PROPERTY_URI], instanceObject).then((selectedItems) => {
        expect(selectedItems).to.deep.equal([
          {id: '2'},
          {id: '3'},
          {id: '6'}
        ]);
        done();
      });
    });

    it('should return intersection of values when there are no relations #3', (done) => {
      instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({ data: ['2', '6'] }));
      addRelationAction.getSelectedItems([HAS_WATCHERS_PROPERTY_URI, HAS_ATTACHMENT_PROPERTY_URI, HAS_SOME_RELATION_PROPERTY_URI], instanceObject).then((selectedItems) => {
        expect(selectedItems).to.deep.equal([
          { id: '2'},
          { id: '6'}
        ]);
        done();
      });
    });

    it('should return empty array when there are no relations', (done) => {
      instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({ data: [] }));
      addRelationAction.getSelectedItems(undefined, instanceObject).then((selectedItems) => {
        expect(selectedItems.length === 0).to.be.true;
        done();
      });
    });

    it('should return empty array when there are no relations', (done) => {
      instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({ data: [] }));
      addRelationAction.getSelectedItems([], instanceObject).then((selectedItems) => {
        expect(selectedItems.length === 0).to.be.true;
        done();
      });
    });
  });

  it('should return initialized picker configuration', () => {
    let label = 'Label of header';
    let expectedConfig = {
      header: label,
      extensions: {},
      tabs: {}
    };

    let pickerConfig = AddRelationAction.initPickerConfig(label);

    expect(pickerConfig).to.deep.equal(expectedConfig);
  });

  describe('setupSearchExtension', () => {
    it('should set properly search configuration without predefined types', () => {
      let pickerConfig = {extensions: []};

      AddRelationAction.setupSearchExtension(pickerConfig, undefined, MULTIPLE_SELECTION, CURRENT_OBJECT_ID);
      let searchExtension = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchExtension.predefinedTypes).to.deep.equal([]);
      let resultConfig = searchExtension.results.config;
      expect(resultConfig.selection).to.be.equal(MULTIPLE_SELECTION);
      expect(resultConfig.exclusions).to.deep.equal([CURRENT_OBJECT_ID]);
      expect(resultConfig.selectedItems).to.deep.equal([]);
    });

    it('should set properly search configuration with predefined types', () => {
      let pickerConfig = {extensions: []};

      AddRelationAction.setupSearchExtension(pickerConfig, ['emf:User'], MULTIPLE_SELECTION, CURRENT_OBJECT_ID);
      let searchExtension = pickerConfig.extensions[SEARCH_EXTENSION];
      expect(searchExtension.predefinedTypes).to.deep.equal(['emf:User']);
      let resultConfig = searchExtension.results.config;
      expect(resultConfig.selection).to.be.equal(MULTIPLE_SELECTION);
      expect(resultConfig.exclusions).to.deep.equal([CURRENT_OBJECT_ID]);
      expect(resultConfig.selectedItems).to.deep.equal([]);
    });
  });

  describe('setupBasketExtension', () => {
    it('should add custom basket label to configuration when relations contains only one relation', () => {
      let pickerConfig = {tabs: []};

      AddRelationAction.setupBasketExtension(pickerConfig, [HAS_WATCHERS_PROPERTY_URI], instanceObject);

      let basketExtension = pickerConfig.tabs[BASKET_EXTENSION];
      expect(basketExtension.label).to.equals(HAS_WATCHERS_PROPERTY_LABEL);
    });

    it('should not modified configuration when relations are more than one', () => {
      let pickerConfig = {tabs: []};

      AddRelationAction.setupBasketExtension(pickerConfig, [HAS_ATTACHMENT_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI], null);

      expect(pickerConfig.tabs).to.deep.equals([]);
    });
  });

  describe('getSelectionType', () => {
    it('should return multiple selection when relations contains one relation and relation is for field defined as multi value', () => {
      let result = AddRelationAction.getSelectionType([HAS_WATCHERS_PROPERTY_URI], instanceObject, {selection: 'single'});

      expect(result).to.be.equal(MULTIPLE_SELECTION);
    });

    it('should return single selection when relations contains one relation and relation is for field defined as single value', () => {
      let result = AddRelationAction.getSelectionType([HAS_ATTACHMENT_PROPERTY_URI], instanceObject, {selection: 'multiple'});

      expect(result).to.be.equal(SINGLE_SELECTION);
    });

    it('should return multiple selection when action configuration is set single', () => {
      let result = AddRelationAction.getSelectionType([HAS_ATTACHMENT_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI], null, {selection: 'single'});

      expect(result).to.be.equal(SINGLE_SELECTION);
    });

    it('should return multiple selection when action configuration is set multiple', () => {
      let result = AddRelationAction.getSelectionType([HAS_ATTACHMENT_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI], null, {selection: 'multiple'});

      expect(result).to.be.equal(MULTIPLE_SELECTION);
    });

    it('should return multiple selection when relation are more than one and action configuration not define selection', () => {
      let result = AddRelationAction.getSelectionType([HAS_ATTACHMENT_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI], null, {});

      expect(result).to.be.equal(MULTIPLE_SELECTION);
    });
  });

  describe('buildRelationRequestData', () => {
    it('should build proper data request scenario with add and remove links', () => {
      let expectedResult = {
        id: CURRENT_OBJECT_ID,
        userOperation: 'Add relation',
        add: [{
          linkId: HAS_ATTACHMENT_PROPERTY_URI,
          ids: ['2']
        }, {
          linkId: HAS_WATCHERS_PROPERTY_URI,
          ids: ['2']
        }],
        remove: [{
          linkId: HAS_ATTACHMENT_PROPERTY_URI,
          ids: ['5']
        }, {
          linkId: HAS_WATCHERS_PROPERTY_URI,
          ids: ['5']
        }]
      };

      executeAndVerify([createValue(2)], [createValue(5)], expectedResult);
    });

    it('should build proper data request scenario with only links to be removed', () => {
      let expectedResult = {
        id: CURRENT_OBJECT_ID,
        userOperation: 'Add relation',
        add: [{
          linkId: HAS_ATTACHMENT_PROPERTY_URI,
          ids: []
        }, {
          linkId: HAS_WATCHERS_PROPERTY_URI,
          ids: []
        }],
        remove: [{
          linkId: HAS_ATTACHMENT_PROPERTY_URI,
          ids: ['5']
        }, {
          linkId: HAS_WATCHERS_PROPERTY_URI,
          ids: ['5']
        }]
      };

      executeAndVerify(undefined, [createValue(5)], expectedResult);
    });

    it('should build proper data request scenario with only links to be added', () => {
      let expectedResult = {
        id: CURRENT_OBJECT_ID,
        userOperation: 'Add relation',
        add: [{
          linkId: HAS_ATTACHMENT_PROPERTY_URI,
          ids: ['2']
        }, {
          linkId: HAS_WATCHERS_PROPERTY_URI,
          ids: ['2']
        }],
        remove: [{
          linkId: HAS_ATTACHMENT_PROPERTY_URI,
          ids: []
        }, {
          linkId: HAS_WATCHERS_PROPERTY_URI,
          ids: []
        }]
      };

      executeAndVerify([createValue(2)], undefined, expectedResult);
    });

    function createAction() {
      return {
        configuration: {relation: [HAS_ATTACHMENT_PROPERTY_URI, HAS_WATCHERS_PROPERTY_URI]},
        action: 'Add relation'
      };
    }

    function executeAndVerify(toBeAdded, toBeRemoved, expectedResult) {
      let result = AddRelationAction.buildRelationRequestData(CURRENT_OBJECT_ID, createAction(), toBeAdded, toBeRemoved);
      expect(result).to.deep.equal(expectedResult);
    }
  });


  it('should return array with links that have be added', () => {
    getToBeAddedData.forEach((data) => {
      let result = addRelationAction.getToBeAdded(data.oldSelectedItems, data.newSelectedItems);
      expect(result).to.deep.equal(data.expectedResult);
    });
  });

  let getToBeAddedData = [
    {
      oldSelectedItems: [],
      newSelectedItems: [],
      expectedResult: []
    },
    {
      oldSelectedItems: undefined,
      newSelectedItems: [createValue(2)],
      expectedResult: [createValue(2)]
    },
    {
      oldSelectedItems: [],
      newSelectedItems: [createValue(2), createValue(1), createValue(3)],
      expectedResult: [createValue(2), createValue(1), createValue(3)]
    },
    {
      oldSelectedItems: undefined,
      newSelectedItems: undefined,
      expectedResult: []
    },
    {
      oldSelectedItems: [createValue(2), createValue(1), createValue(3)],
      newSelectedItems: [createValue(5), createValue(2), createValue(6)],
      expectedResult: [createValue(5), createValue(6)]
    }
  ];

  it('should return array with links that have be removed', () => {
    getToBeRemovedTestData.forEach((data) => {
      let result = addRelationAction.getToBeRemoved(data.oldSelectedItems, data.newSelectedItems);
      expect(result).to.deep.equal(data.expectedResult);
    });
  });

  let getToBeRemovedTestData = [
    {
      oldSelectedItems: [],
      newSelectedItems: [],
      expectedResult: []
    },
    {
      oldSelectedItems: undefined,
      newSelectedItems: [],
      expectedResult: []
    },
    {
      oldSelectedItems: [],
      newSelectedItems: undefined,
      expectedResult: []
    },
    {
      oldSelectedItems: undefined,
      newSelectedItems: undefined,
      expectedResult: []
    },
    {
      oldSelectedItems: [createValue(2), createValue(1), createValue(3)],
      newSelectedItems: [createValue(5), createValue(2), createValue(6)],
      expectedResult: [createValue(1), createValue(3)]
    }
  ];

  it('should return arrays with value which source array not contains', () => {
    let arrayOne = [createValue(2), createValue(1), createValue(3)];
    let arrayTwo = [createValue(5), createValue(2), createValue(6)];
    let expectedResult = [createValue(5), createValue(6)];

    let result = addRelationAction.getMissingInSource(arrayOne, arrayTwo);

    expect(result).to.deep.equal(expectedResult);
  });

  it('should return intersection of two arrays', () => {
    getIntersectionTestData.forEach((data) => {
      let result = addRelationAction.getIntersection(data.firstArray, data.secondArray);
      expect(result).to.deep.equal(data.expectedResult);
    });
  });

  let getIntersectionTestData = [
    {
      firstArray: undefined,
      secondArray: [createValue(2)],
      expectedResult: []
    },
    {
      firstArray: [createValue(2)],
      secondArray: undefined,
      expectedResult: []
    },
    {
      firstArray: [createValue(2), createValue(1), createValue(3)],
      secondArray: [createValue(5), createValue(2), createValue(6)],
      expectedResult: [createValue(2)]
    }
  ];

  function createValue(id) {
    return {id: id + ''};
  }

  /**
   * Creates object of InstanceObject and fill it with tree properties:
   * 1. Property has attachment with:
   *  1.1 name 'hasAttachment'
   *  1.2 label 'Has attachment'
   *  1.3 uri 'emf:hasAttachment'
   *  1.4 value [{id:1}), {id:2}, {id:3}, {id:6}]
   *  1.5 multivalue false
   * 2. Property has watchers with:
   *  2.1 name 'hasWatchers'
   *  2.2 label 'Has watchers'
   *  2.3 uri 'emf:hasWatchers'
   *  2.4 value [{id:2}, {id:6}, {id:222}]
   *  2.5 multivalue true
   * 3. Property some relation with:
   *  3.1 name 'someRelation'
   *  3.2 label 'Some relation'
   *  3.3 uri 'emf:someRelation'
   *  3.4 value [{id:55}, {id:2}, {id:3}, {id:6}]
   *  3.5 multivalue true
   * @returns {InstanceObject}
   */
  function createInstanceObject() {
    let viewModel = new ViewModelBuilder()
      .addField(HAS_ATTACHMENT_PROPERTY_NAME, 'EDITABLE', 'text', HAS_ATTACHMENT_PROPERTY_LABEL, false, false, [], undefined, undefined, false, HAS_ATTACHMENT_PROPERTY_URI)
      .addField(HAS_WATCHERS_PROPERTY_NAME, 'EDITABLE', 'text', HAS_WATCHERS_PROPERTY_LABEL, false, false, [], undefined, undefined, true, HAS_WATCHERS_PROPERTY_URI)
      .addField(HAS_SOME_RELATION_PROPERTY_NAME, 'EDITABLE', 'text', HAS_SOME_RELATION_PROPERTY_LABEL, false, false, [], undefined, undefined, true, HAS_SOME_RELATION_PROPERTY_URI)
      .getModel();
    let validationModel = new ValidationModelBuilder()
      .addProperty(HAS_ATTACHMENT_PROPERTY_NAME, HAS_ATTACHMENT_PROPERTY_VALUE)
      .addProperty(HAS_WATCHERS_PROPERTY_NAME, HAS_WATCHERS_PROPERTY_VALUE)
      .addProperty(HAS_SOME_RELATION_PROPERTY_NAME, HAS_SOME_RELATION_PROPERTY_VALUE)
      .getModel();
    return new InstanceObject(CURRENT_OBJECT_ID, {viewModel, validationModel}, null, null);
  }
});