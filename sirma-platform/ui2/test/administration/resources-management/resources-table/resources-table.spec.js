import {ResourcesTable} from 'administration/resources-management/resources-table/resources-table';
import {DialogService} from 'components/dialog/dialog-service';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {ConfigurationRestService} from 'services/rest/configurations-service';
import {Configuration} from 'common/application-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('ResourcesTable', () => {

  let resourcesTable;
  let dialogService;
  let propertiesSelectorHelper;
  let configurationService;
  let configuration;
  let eventbus;
  let $scope;

  beforeEach(() => {
    dialogService = stub(DialogService);
    propertiesSelectorHelper = stubPropertiesSelectorHelper();
    configurationService = stub(ConfigurationRestService);
    configuration = stubConfiguration();
    eventbus = stub(Eventbus);
    $scope = mock$scope();

    resourcesTable = new ResourcesTable(dialogService, propertiesSelectorHelper, configurationService, configuration, eventbus, $scope);
    resourcesTable.resources = [];
    resourcesTable.config = {
      propertiesConfigKey: Configuration.USER_MANAGEMENT_USER_PROPERTIES,
      models: {
        viewModel: {
          fields: [{ identifier: 'title' }]
        }
      }
    };
  });

  describe('ngOnInit', () => {
    it('should load and set configured selected properties', () => {
      resourcesTable.ngOnInit();
      let expected = {
        'firstName': {'name': 'firstName'},
        'lastName': {'name': 'lastName'},
        'email': {'name': 'email'}
      };
      expect(resourcesTable.selectedProperties).to.deep.equal(expected);
    });

    it('should subscribe to InstanceRefreshEvent', () => {
      resourcesTable.config = {
        actionExecutedCallback: () => {},
        propertiesConfigKey: Configuration.USER_MANAGEMENT_USER_PROPERTIES,
        models: {
          viewModel: {
            fields: []
          }
        }
      };
      resourcesTable.eventbus.subscribe.returns({ subscription: 'someevent' });
      resourcesTable.ngOnInit();
      expect(resourcesTable.eventbus.subscribe.called).to.be.true;
      expect(resourcesTable.instanceRefreshEventSubscription).to.eql({ subscription: 'someevent' });
    });

    it('should create a flat view model', () => {
      resourcesTable.ngOnInit();
      // The flatViewModel is returned as a Map object
      expect(resourcesTable.flatViewModel.has('title')).to.be.true;
    });

    it('should create a propertiesSelectorConfig', () => {
      resourcesTable.ngOnInit();
      let expected = {
        'config': {
          'definitions': {
            'userDefinition':[]
          },
          'selectedProperties':{
            'undefined': {
              'firstName': {'name': 'firstName'},
              'lastName': {'name': 'lastName'},
              'email': {'name': 'email'}
            }
          }
        }
      };
      expect(resourcesTable.propertiesSelectorConfig).to.eql(expected);
    });
  });

  describe('initializeSubscriptions', () => {
    it('should do nothing when there is no callback in config', () => {
      resourcesTable.initializeSubscriptions();
      expect(eventbus.subscribe.called).to.be.false;
    });

    it('should subscribe to event when there is a callback in config', () => {
      resourcesTable.config.actionExecutedCallback = () => {
      };

      resourcesTable.initializeSubscriptions();
      expect(eventbus.subscribe.calledOnce).to.be.true;
    });
  });

  describe('initializeSelectedProperties', () => {
    it('should initialize selected properties', () => {
      resourcesTable.initializeSelectedProperties(['firstName', 'lastName', 'email']);
      let expected = {
        'firstName': {'name': 'firstName'},
        'lastName': {'name': 'lastName'},
        'email': {'name': 'email'}
      };
      expect(resourcesTable.selectedProperties).to.deep.equal(expected);
    });
  });

  describe('initializeViewModel', () => {
    it('should initialize flat view model from passed models', () => {
      resourcesTable.initializeViewModel();
      expect(resourcesTable.flatViewModel).to.exist;
    });
  });

  describe('updateConfiguration', () => {
    it('should correctly construct payload for save', () => {
      resourcesTable.selectedProperties = {
        'userId': {'name': 'userId'},
        'email': {'name': 'email'},
        'isMemberOf': {'name': 'isMemberOf'}
      };
      let value = {
        columns: ['userId', 'email', 'isMemberOf']
      };
      let expected = [{
        key: Configuration.USER_MANAGEMENT_USER_PROPERTIES,
        value: JSON.stringify(value)
      }];

      resourcesTable.updateConfiguration();
      expect(configurationService.updateConfigurations.calledOnce).to.be.true;
      expect(configurationService.updateConfigurations.getCall(0).args[0]).to.deep.equal(expected);
    });
  });

  describe('createPropertiesSelectorConfig', () => {
    it('should create properties selector config with selected properties', () => {
      let userDefinitionId = 'userDefinition';
      resourcesTable.config.definitionId = userDefinitionId;
      resourcesTable.createPropertiesSelectorConfig();

      expect(resourcesTable.propertiesSelectorConfig).to.exist;
      expect(resourcesTable.propertiesSelectorConfig.config.definitions).to.exist;
      expect(resourcesTable.propertiesSelectorConfig.config.selectedProperties[userDefinitionId]).to
        .deep.equal(resourcesTable.selectedProperties);
    });
  });

  describe('createPropertiesDialogConfig', () => {
    it('should create properties dialog config', () => {
      resourcesTable.createPropertiesDialogConfig('userDefinition');

      expect(resourcesTable.propertiesDialogConfig).to.exist;
    });
  });

  describe('getPropertyType', () => {
    it('should determine correct property types', () => {
      let flatViewModel = new Map();
      flatViewModel.set('firstName', {
        identifier: 'firstName',
        dataType: 'text'
      });
      flatViewModel.set('language', {
        identifier: 'language',
        dataType: 'text',
        codelist: 13
      });
      flatViewModel.set('isMemberOf', {
        identifier: 'isMemberOf',
        dataType: 'any',
        controlId: 'picker'
      });
      resourcesTable.flatViewModel = flatViewModel;

      expect(resourcesTable.getPropertyType('language')).to.equal('codelist');
      expect(resourcesTable.getPropertyType('isMemberOf')).to.equal('picker');
      expect(resourcesTable.getPropertyType('firstName')).to.equal('text');
      expect(resourcesTable.getPropertyType('notExistent')).to.be.undefined;
    });
  });

  describe('getPropertyLabel', () => {
    it('should retrieve property label', () => {
      let flatViewModel = new Map();
      flatViewModel.set('firstName', {
        identifier: 'firstName',
        label: 'First Name'
      });
      flatViewModel.set('language', {
        identifier: 'language',
        label: 'Language'
      });
      resourcesTable.flatViewModel = flatViewModel;

      expect(resourcesTable.getPropertyLabel('language')).to.equal('Language');
      expect(resourcesTable.getPropertyLabel('firstName')).to.equal('First Name');
      expect(resourcesTable.getPropertyLabel('notExistent')).to.be.undefined;
    });
  });

  describe('ngOnDestroy', () => {
    it('should do nothing when there is no subscription', () => {
      resourcesTable.ngOnDestroy();
      expect(resourcesTable.instanceRefreshEventSubscription).to.not.exist;
    });

    it('should unsubscribe when there is a subscription', () => {
      let instanceRefreshEventSubscription = {
        unsubscribe: sinon.stub()
      };
      resourcesTable.instanceRefreshEventSubscription = instanceRefreshEventSubscription;

      resourcesTable.ngOnDestroy();
      expect(instanceRefreshEventSubscription.unsubscribe.calledOnce).to.be.true;
    });
  });

  function stubPropertiesSelectorHelper() {
    let helper = stub(PropertiesSelectorHelper);
    helper.extractDefinitionsByIdentifiers.returns(PromiseStub.resolve({
      'userDefinition': []
    }));
    return helper;
  }

  function stubConfiguration() {
    let configuration = stub(Configuration);
    configuration.getJson.returns({
      columns: ['firstName', 'lastName', 'email']
    });
    return configuration;
  }

});