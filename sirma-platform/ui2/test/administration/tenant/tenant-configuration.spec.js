import {TenantConfiguration} from 'administration/tenant/tenant-configuration';
import {StatusCodes} from 'services/rest/status-codes';
import {DialogService} from 'components/dialog/dialog-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {KEY_ENTER} from 'common/keys';

import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('TenantConfiguration', () => {

  var tenantConfiguration;
  beforeEach(() => {
    var eventbus = mockEventbus();
    var configService = mockConfigService();
    var dialogService = mockDialogService();
    var notificationService = mockNotificationService();
    var translateService = mockTranslateService();
    tenantConfiguration = new TenantConfiguration(mock$scope(), eventbus, configService, dialogService, notificationService, translateService);
  });

  it('should correctly extract configuration value if there is rawValue', () => {
    var value = tenantConfiguration.getConfigurationValue({
      rawValue: '1',
      defaultValue: '2'
    });
    expect(value).to.equal(1);

    value = tenantConfiguration.getConfigurationValue({
      rawValue: 'true',
      defaultValue: 'false'
    });
    expect(value).to.be.true;

    value = tenantConfiguration.getConfigurationValue({
      rawValue: 'configuration_value'
    });
    expect(value).to.equal('configuration_value');
  });

  it('should correctly extract configuration value if there is defaultValue', () => {
    var value = tenantConfiguration.getConfigurationValue({
      rawValue: undefined,
      defaultValue: 'configuration_value'
    });
    expect(value).to.equal('configuration_value');
  });

  it('should correctly extract configuration value if there is value', () => {
    var value = tenantConfiguration.getConfigurationValue({
      value: 'configuration_value'
    });
    expect(value).to.equal('configuration_value');
  });

  it('should filter out any group configuration properties', () => {
    var transformed = tenantConfiguration.transformConfigurations([{
      key: 'group-config',
      dependsOn: []
    }, {
      key: 'property-config',
      value: 'property-value'
    }]);
    expect(transformed.length).to.equal(1);
    expect(transformed[0].key).to.equal('property-config');
  });

  it('should setup form base configuration options', () => {
    var wrapperConfig = {formViewMode:'EDIT'};
    wrapperConfig.collapsibleRegions = true;
    tenantConfiguration.setupFormConfig();
    expect(tenantConfiguration.wrapperConfig).to.deep.equal(wrapperConfig);
  });

  it('should setup form model configuration options', () => {
    var formConfig = {};
    formConfig.models = {};
    tenantConfiguration.setupFormConfig();
    expect(tenantConfiguration.formConfig).to.deep.equal(formConfig);
  });

  it('should extract base key from a configuration', () => {
    var sampleConfig = {key: 'sample.test.key'};
    var baseKey = tenantConfiguration.extractBaseKey(sampleConfig);
    expect(baseKey).to.deep.equal('sample');
  });

  it('should capitalize first letter of a string', () => {
    var sampleString = 'sample';
    var capitalized = tenantConfiguration.capitalizeFirstLetter(sampleString);
    expect(capitalized).to.deep.equal('Sample');
  });

  it('should extract correct constraints based on the fields java types', () => {
    var constraints = fieldConstraints();
    for (var constraint of constraints) {
      var resultConstraint = tenantConfiguration.extractConstraints(constraint);
      expect(resultConstraint.type).to.deep.equal(constraint.expectedType);
      expect(resultConstraint.regexp).to.deep.equal(constraint.expectedRegexp);
    }
  });

  it('should create a properly configured region', () => {
    tenantConfiguration.setupFormConfig();
    var region = tenantConfiguration.createRegion('region');
    expect(region).to.deep.equal({
      identifier: 'region',
      isMandatory: true,
      collapsed: true,
      label: 'Region',
      displayType: 'EDITABLE',
      fields: []
    });
  });

  it('should sort regions alphabetically based on their identifier', () => {
    var regionsArray = scrambledFields();
    tenantConfiguration.sortAlphabeticallyByIdentifier(regionsArray);
    expect(regionsArray).to.deep.equal(sortedFields());
  });

  it('should sort region fields alphabetically based on their identifier', () => {
    var regionMapping = [{
      identifier: 'sample.region',
      fields: scrambledFields()
    }];

    tenantConfiguration.sortRegionFieldsAlphabetically(regionMapping);
    expect(regionMapping).to.deep.equal([{
      identifier: 'sample.region',
      fields: sortedFields()
    }]);
  });

  it('should create a properly configured field', () => {
    var sampleField = sortedRawData()[0];
    var field = tenantConfiguration.createField(sampleField);
    expect(field).to.deep.equal(extractedModelFromData().viewModel.fields[0].fields[0]);
  });

  it('should create a properly configured validation model for a field', () => {
    var sampleField = rawConfigData()[0];
    var validation = tenantConfiguration.createValidation(sampleField);
    expect(validation).to.deep.equal(extractedModelFromData().validationModel[sampleField.key]);
  });

  it('should build the entire form model from raw data', () => {
    tenantConfiguration.setupFormConfig();
    let rawData = extractedModelFromData();
    tenantConfiguration.tenantConfig = rawConfigData();
    tenantConfiguration.setupViewAndModel();
    expect(tenantConfiguration.formConfig.models.viewModel.serialize()).to.deep.equal(rawData.viewModel);
    expect(tenantConfiguration.formConfig.models.validationModel.serialize()).to.deep.equal(rawData.validationModel);
  });

  it('should should build the entire form model using mocked service', () => {
    tenantConfiguration.loadTenantConfig();
    let expected = extractedModelFromData();
    expect(tenantConfiguration.formConfig.models.viewModel.serialize()).to.deep.equal(expected.viewModel);
    expect(tenantConfiguration.formConfig.models.validationModel.serialize()).to.deep.equal(expected.validationModel);
    expect(tenantConfiguration.formReady).to.deep.equal(true);
  });

  it('should have the form invalidated if incorrect or incomplete model is fetched', () => {
    tenantConfiguration.formConfig = {};
    expect(tenantConfiguration.isFormValid()).to.equal(false);
  });

  it('should reload the model if modified back to defaults', () => {
    tenantConfiguration.loadTenantConfig();
    let rawData = extractedModelFromData();
    var viewModel = tenantConfiguration.formConfig.models.validationModel;
    viewModel['file.sample.maxsize'].value = 'modified1';
    viewModel['string.sample.string'].value = 'modified2';
    tenantConfiguration.cancelConfigurationChanges();
    expect(tenantConfiguration.formConfig.models.viewModel.serialize()).to.deep.equal(rawData.viewModel);
    expect(tenantConfiguration.formConfig.models.validationModel.serialize()).to.deep.equal(rawData.validationModel);
  });

  it('should call resolve notification with success', () => {
    tenantConfiguration.displayResultNotification(StatusCodes.SUCCESS, 'success', 'error');
    expect(tenantConfiguration.notificationService.success.calledOnce).to.be.true;
  });

  it('should call resolve notification with error', () => {
    tenantConfiguration.displayResultNotification(StatusCodes.BAD_REQUEST, 'success', 'error');
    expect(tenantConfiguration.notificationService.error.calledOnce).to.be.true;
  });

  it('should show success notification when the modified configurations are updated', () => {
    var configs = [];
    tenantConfiguration.updateConfigurations(configs);
    expect(tenantConfiguration.notificationService.success.calledOnce).to.be.true;
  });

  it('should publish an event notifying that configurations are updated', () => {
    var configs = [];
    tenantConfiguration.updateConfigurations(configs);
    expect(tenantConfiguration.eventbus.publish.calledOnce).to.be.true;
  });

  it('should initialize a confirmation dialog and trigger it', () => {
    tenantConfiguration.loadTenantConfig();
    var viewModel = tenantConfiguration.formConfig.models.validationModel;
    viewModel['file.sample.maxsize'].value = 'modified1';
    viewModel['string.sample.string'].value = 'modified2';
    tenantConfiguration.saveModifiedConfigurations();
    expect(tenantConfiguration.dialogService.confirmation.calledOnce).to.be.true;
  });

  it('should not initialize a confirmation dialog and trigger it if there are no changed configurations', () => {
    tenantConfiguration.loadTenantConfig();
    tenantConfiguration.saveModifiedConfigurations();
    expect(tenantConfiguration.dialogService.confirmation.called).to.be.false;
  });

  it('should save configuration on confirmation', () => {
    var configForUpdating = [];
    tenantConfiguration.updateConfigurations = sinon.spy();
    tenantConfiguration.showSaveConfirmation(configForUpdating);

    var dialogConfig = tenantConfiguration.dialogService.confirmation.getCall(0).args[2];
    var buttonHandler = dialogConfig.onButtonClick;
    buttonHandler(DialogService.YES, undefined, {
      dismiss: () => {
      }
    });

    expect(tenantConfiguration.updateConfigurations.calledOnce).to.be.true;
  });

  it('should not save configuration on confirmation cancellation', () => {
    var configForUpdating = [];
    tenantConfiguration.updateConfigurations = sinon.spy();
    tenantConfiguration.showSaveConfirmation(configForUpdating);

    var dialogConfig = tenantConfiguration.dialogService.confirmation.getCall(0).args[2];
    var buttonHandler = dialogConfig.onButtonClick;
    buttonHandler(DialogService.CANCEL, undefined, {
      dismiss: () => {
      }
    });

    expect(tenantConfiguration.updateConfigurations.called).to.be.false;
  });

  it('should include the keys of the modified configurations in the confirmation', () => {
    var configForUpdating = [{
      key: 'config-key'
    }];
    tenantConfiguration.showSaveConfirmation(configForUpdating);
    var message = tenantConfiguration.dialogService.confirmation.getCall(0).args[0];
    expect(message).to.include('config-key');
  });

  it('should should extract the configurations that are modified in the form model', () => {
    tenantConfiguration.setupFormConfig();
    tenantConfiguration.tenantConfig = rawConfigData();
    tenantConfiguration.setupViewAndModel();

    var validationModel = tenantConfiguration.formConfig.models.validationModel;
    validationModel['boolean.sample.flag'].value = false;

    var configForUpdate = tenantConfiguration.extractConfigurationsForUpdate();
    expect(configForUpdate).to.deep.equal([{
      shared: true,
      system: false,
      rawValue: 'true',
      defaultValue: '',
      label: 'Test java boolean type value',
      value: true,
      key: 'boolean.sample.flag',
      javaType: 'java.lang.Boolean'
    }]);
  });

  it('should correctly update the configurations from the form model on saving', () => {
    tenantConfiguration.setupFormConfig();
    tenantConfiguration.tenantConfig = rawConfigData();
    tenantConfiguration.setupViewAndModel();

    var validationModel = tenantConfiguration.formConfig.models.validationModel;
    validationModel['boolean.sample.flag'].value = false;
    var configForUpdate = tenantConfiguration.extractConfigurationsForUpdate();

    expect(configForUpdate[0].value).to.be.true;
    tenantConfiguration.updateConfigurations(configForUpdate);
    expect(configForUpdate[0].value).to.be.false
  });

  it('should restore to all configurations when key word is empty', () => {
    var keyWord = '';
    tenantConfiguration.setupFormConfig();
    tenantConfiguration.tenantConfig = rawConfigData();
    tenantConfiguration.setupViewAndModel();
    tenantConfiguration.filterKeyWord = keyWord;
    tenantConfiguration.filterConfigurations();

    let regions = extractedModelFromData().viewModel.fields;
    regions.forEach((region) => {
      let fields = region.fields;
      fields.forEach((field) => {
        field.displayType = ValidationService.DISPLAY_TYPE_EDITABLE;
      });
      region.displayType = ValidationService.DISPLAY_TYPE_EDITABLE;
    });

    expect(tenantConfiguration.formConfig.models.viewModel.serialize().fields).to.deep.equal(regions);
  });

  it('should correctly filter configurations by key word', () => {
    var keyWord = 'size';
    tenantConfiguration.setupFormConfig();
    tenantConfiguration.tenantConfig = rawConfigData();
    tenantConfiguration.setupViewAndModel();
    tenantConfiguration.filterKeyWord = keyWord;
    tenantConfiguration.filterConfigurations();

    let regions = extractedModelFromData().viewModel.fields;
    regions.forEach((region) => {
      let fields = region.fields;
      fields.forEach((field) => {
        field.displayType = field.identifier.indexOf(keyWord) === -1 ? ValidationService.DISPLAY_TYPE_HIDDEN : ValidationService.DISPLAY_TYPE_EDITABLE;
      });
      region.displayType = region.identifier !== 'file' ? ValidationService.DISPLAY_TYPE_HIDDEN : ValidationService.DISPLAY_TYPE_EDITABLE;
    });

    expect(tenantConfiguration.formConfig.models.viewModel.serialize().fields).to.deep.equal(regions);
  });

  it('should clear the filter keyword field after cancel', () => {
    tenantConfiguration.loadTenantConfig();
    var viewModel = tenantConfiguration.formConfig.models.validationModel;
    viewModel['file.sample.maxsize'].value = 'modified1';
    viewModel['string.sample.string'].value = 'modified2';
    tenantConfiguration.cancelConfigurationChanges();
    tenantConfiguration.filterKeyWord = 'size';
    tenantConfiguration.filterConfigurations();
    tenantConfiguration.cancelConfigurationChanges();

    expect(tenantConfiguration.filterKeyWord).to.deep.equal('');
  });

  function mockEventbus() {
    return {
      publish: sinon.spy()
    };
  }

  function mockTranslateService() {
    return {
      translateInstant: () => {
        return 'translated';
      }
    };
  }

  function mockConfigService() {
    return {
      loadConfigurations: () => {
        return PromiseStub.resolve({
          data: rawConfigData(),
          status: 200
        });
      },
      updateConfigurations: () => {
        return PromiseStub.resolve({
          data: rawConfigData(),
          status: 200
        });
      },
      reloadConfigurations: () => {
        return PromiseStub.resolve({
          status: 200
        });
      }
    };
  }

  function mockNotificationService() {
    return {
      error: sinon.spy((ms) => {
        return PromiseStub.resolve(ms);
      }),
      success: sinon.spy((ms) => {
        return PromiseStub.resolve(ms);
      })
    };
  }

  function mockDialogService() {
    return {
      confirmation: sinon.spy((ms) => {
        return PromiseStub.resolve(ms);
      })
    };
  }

  function fieldConstraints() {
    return [
      {
        expectedType: 'text',
        expectedRegexp: '-?[0-9]+',
        javaType: 'java.lang.Integer'
      },
      {
        expectedType: 'text',
        expectedRegexp: '-?[0-9]+\.?[0-9]+',
        javaType: 'java.lang.Float'
      }, {
        expectedType: 'text',
        expectedRegexp: '.+',
        javaType: 'java.lang.String'
      }, {
        expectedType: 'boolean',
        expectedRegexp: 'true|false',
        javaType: 'java.lang.Boolean'
      }, {
        expectedType: 'date',
        expectedRegexp: '.+',
        javaType: 'java.lang.Date'
      }, {
        expectedType: 'datetime',
        expectedRegexp: '.+',
        javaType: 'java.lang.DateTime'
      }, {
        expectedType: 'text',
        expectedRegexp: '(https?|ftp|file|jdbc:[a-zA-Z0-9.]+)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]',
        javaType: 'java.lang.URI'
      }
    ];
  }

  function sortedRawData() {
    let sortedData = rawConfigData();
    sortedData.sort((lhs, rhs) => {
      if (lhs.key < rhs.key) return -1;
      if (lhs.key > rhs.key) return 1;
      return 0;
    });
    return sortedData;
  }

  function rawConfigData() {
    return [{
      shared: true,
      system: false,
      rawValue: '1048',
      defaultValue: '',
      label: 'Test java integer type value',
      value: 1048,
      key: 'file.sample.maxsize',
      javaType: 'java.lang.Integer'
    }, {
      shared: true,
      system: false,
      rawValue: 'test string',
      defaultValue: 'default value',
      label: 'Test java string type value',
      value: 'test string',
      key: 'string.sample.string',
      javaType: 'java.lang.String'
    }, {
      shared: true,
      system: false,
      rawValue: 'file.txt',
      defaultValue: 'test-default.txt',
      label: 'Test java file type value',
      value: 'file.txt',
      key: 'file.sample.filename',
      javaType: 'java.lang.File'
    }, {
      shared: true,
      system: false,
      rawValue: 'https://www.youtube.com',
      defaultValue: 'https://www.google.bg',
      label: 'Test java uri type value',
      value: 'https://www.youtube.com',
      key: 'uri.sample.website',
      javaType: 'java.lang.URI'
    }, {
      shared: true,
      system: false,
      rawValue: 'true',
      defaultValue: '',
      label: 'Test java boolean type value',
      value: true,
      key: 'boolean.sample.flag',
      javaType: 'java.lang.Boolean'
    }, {
      shared: true,
      system: false,
      rawValue: '123.456',
      defaultValue: '',
      label: 'Test java float type value',
      value: 123.456,
      key: 'float.sample.real',
      javaType: 'java.lang.Float'
    }];
  }

  function extractedModelFromData() {
    return {
      'viewModel': {
        'fields': [{
          'identifier': 'boolean',
          'isMandatory': true,
          'collapsed': true,
          'label': 'Boolean',
          'displayType': 'EDITABLE',
          'fields': [{
            'previewEmpty': true,
            'identifier': 'boolean.sample.flag',
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'Test java boolean type value',
            'dataType': 'boolean',
            'label': 'boolean.sample.flag',
            'isMandatory': true,
            'validators': [{
              'id': 'regex',
              'context': {'pattern': 'true|false'},
              'message': 'administration.tenant.message.format',
              'level': 'error'
            }, {'id': 'mandatory', 'message': 'validation.field.mandatory', 'level': 'error'}]
          }]
        }, {
          'identifier': 'file',
          'isMandatory': true,
          'collapsed': true,
          'label': 'File',
          'displayType': 'EDITABLE',
          'fields': [{
            'previewEmpty': true,
            'identifier': 'file.sample.filename',
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'Test java file type value',
            'dataType': 'text',
            'label': 'file.sample.filename',
            'isMandatory': true,
            'validators': [{
              'id': 'regex',
              'context': {'pattern': '.+'},
              'message': 'administration.tenant.message.format',
              'level': 'error'
            }, {'id': 'mandatory', 'message': 'validation.field.mandatory', 'level': 'error'}]
          }, {
            'previewEmpty': true,
            'identifier': 'file.sample.maxsize',
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'Test java integer type value',
            'dataType': 'text',
            'label': 'file.sample.maxsize',
            'isMandatory': true,
            'validators': [{
              'id': 'regex',
              'context': {'pattern': '-?[0-9]+'},
              'message': 'administration.tenant.message.format',
              'level': 'error'
            }, {'id': 'mandatory', 'message': 'validation.field.mandatory', 'level': 'error'}]
          }]
        }, {
          'identifier': 'float',
          'isMandatory': true,
          'collapsed': true,
          'label': 'Float',
          'displayType': 'EDITABLE',
          'fields': [{
            'previewEmpty': true,
            'identifier': 'float.sample.real',
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'Test java float type value',
            'dataType': 'text',
            'label': 'float.sample.real',
            'isMandatory': true,
            'validators': [{
              'id': 'regex',
              'context': {'pattern': '-?[0-9]+.?[0-9]+'},
              'message': 'administration.tenant.message.format',
              'level': 'error'
            }, {'id': 'mandatory', 'message': 'validation.field.mandatory', 'level': 'error'}]
          }]
        }, {
          'identifier': 'string',
          'isMandatory': true,
          'collapsed': true,
          'label': 'String',
          'displayType': 'EDITABLE',
          'fields': [{
            'previewEmpty': true,
            'identifier': 'string.sample.string',
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'Test java string type value',
            'dataType': 'text',
            'label': 'string.sample.string',
            'isMandatory': true,
            'validators': [{
              'id': 'regex',
              'context': {'pattern': '.+'},
              'message': 'administration.tenant.message.format',
              'level': 'error'
            }, {'id': 'mandatory', 'message': 'validation.field.mandatory', 'level': 'error'}]
          }]
        }, {
          'identifier': 'uri',
          'isMandatory': true,
          'collapsed': true,
          'label': 'Uri',
          'displayType': 'EDITABLE',
          'fields': [{
            'previewEmpty': true,
            'identifier': 'uri.sample.website',
            'disabled': false,
            'displayType': 'EDITABLE',
            'tooltip': 'Test java uri type value',
            'dataType': 'text',
            'label': 'uri.sample.website',
            'isMandatory': true,
            'validators': [{
              'id': 'regex',
              'context': {'pattern': '(https?|ftp|file|jdbc:[a-zA-Z0-9.]+)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]'},
              'message': 'administration.tenant.message.format',
              'level': 'error'
            }, {'id': 'mandatory', 'message': 'validation.field.mandatory', 'level': 'error'}]
          }]
        }]
      },
      'validationModel': {
        'file.sample.maxsize': {'defaultValue': 1048, 'value': 1048, 'messages': []},
        'string.sample.string': {'defaultValue': 'test string', 'value': 'test string', 'messages': []},
        'file.sample.filename': {'defaultValue': 'file.txt', 'value': 'file.txt', 'messages': []},
        'uri.sample.website': {
          'defaultValue': 'https://www.youtube.com',
          'value': 'https://www.youtube.com',
          'messages': []
        },
        'boolean.sample.flag': {'defaultValue': true, 'value': true, 'messages': []},
        'float.sample.real': {'defaultValue': 123.456, 'value': 123.456, 'messages': []}
      }
    }
  }

  function scrambledFields() {
    return [{identifier: 'document'}, {identifier: 'wood'}, {identifier: 'ui'}, {identifier: 'admin'}, {identifier: 'ui'}];
  }

  function sortedFields() {
    return [{identifier: 'admin'}, {identifier: 'document'}, {identifier: 'ui'}, {identifier: 'ui'}, {identifier: 'wood'}];
  }

});