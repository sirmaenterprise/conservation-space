import {CreateInstanceAction} from 'idoc/actions/create-instance-action';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';


describe('CreateInstanceAction', function () {

  var createPanelService;
  var windowAdapter;
  var createInstanceAction;
  var promiseAdapter;
  beforeEach(() => {
    createPanelService = {
      openCreateInstanceDialog: () => {
        return {};
      }
    };

    windowAdapter = {
      location: {
        href: 'href-returnUrl'
      }
    };
    var namespaceService = mockNamespaceService();
    var modelsService = mockModelsService();
    promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    createInstanceAction = new CreateInstanceAction(createPanelService, modelsService, namespaceService, windowAdapter, promiseAdapter);
  });

  describe('removeEmptyElements()', () => {
    it('should return array without undefined, null or empty elements.', () => {
      var result = createInstanceAction.removeEmptyElements([undefined, null, '  ', 'AD210001']);
      expect(result.length).to.equal(1);
      expect(result[0]).to.equal('AD210001');
    });
  });

  describe('initDialogOptionsFromActionConfig', () => {
    it('should contextSelectorDisabled be true scenario with undefined contextSelectorDisabled configuration', () => {
      let action = {
        configuration: {
          predefinedTypes: ['chd:Survey'],
          predefinedSubTypes: ['AD210001']
        }
      };
      let config = createInstanceAction.initDialogOptionsFromActionConfig(action, null, getContext(), null);
      expect(config.contextSelectorDisabled).to.be.true;
    });

    it('should contextSelectorDisabled be true scenario with true contextSelectorDisabled configuration', () => {
      let action = {
        configuration: {
          predefinedTypes: ['chd:Survey'],
          predefinedSubTypes: ['AD210001'],
          contextSelectorDisabled: true
        }
      };
      let config = createInstanceAction.initDialogOptionsFromActionConfig(action, null, getContext(), null);
      expect(config.contextSelectorDisabled).to.be.true;
    });

    it('should contextSelectorDisabled be false scenario with false contextSelectorDisabled configuration', () => {
      let action = {
        configuration: {
          predefinedTypes: ['chd:Survey'],
          predefinedSubTypes: ['AD210001'],
          contextSelectorDisabled: false
        }
      };
      let config = createInstanceAction.initDialogOptionsFromActionConfig(action, null, getContext(), null);
      expect(config.contextSelectorDisabled).to.be.true;
    });
  });

  describe('execute(action, context)', () => {
    it('should call open dialog with file upload panel exclusion', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: ['chd:Survey'],
          predefinedSubTypes: ['AD210001'],
          forceCreate: false,
          openInNewTab: false
        }
      };

      let expectedConfigurations = {
        parentId: 'curent-object-id',
        returnUrl: 'href-returnUrl',
        operation: 'create',
        predefinedTypes: ['chd:Survey'],
        predefinedSubTypes: ['AD210001'],
        forceCreate: false,
        openInNewTab: false,
        exclusions: ['file-upload-panel'],
        instanceType: 'chd:Survey#full',
        contextSelectorDisabled: true
      };

      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedConfigurations);
    });

    it('should call open dialog with file create panel exclusion', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: ['chd:SurveyUploadable'],
          predefinedSubTypes: ['AD210001'],
          forceCreate: false,
          openInNewTab: false
        }
      };

      let expectedConfigurations = {
        parentId: 'curent-object-id',
        returnUrl: 'href-returnUrl',
        operation: 'create',
        exclusions: ['instance-create-panel'],
        instanceType: 'chd:SurveyUploadable#full',
        predefinedTypes: ['chd:SurveyUploadable'],
        predefinedSubTypes: ['AD210001'],
        forceCreate: false,
        openInNewTab: false,
        contextSelectorDisabled: true
    };

      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedConfigurations);
    });

    it('should call open dialog with configuration for one predefinedTypes and two predefinedTypes', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: ['emf:Document'],
          predefinedSubTypes: ['AD210001', 'CH210001'],
          forceCreate: false,
          openInNewTab: false
        }
      };

      let expectedConfigurations = {
        parentId: 'curent-object-id',
        returnUrl: 'href-returnUrl',
        operation: 'create',
        exclusions: [],
        instanceType: 'emf:Document#full',
        predefinedTypes: ['emf:Document'],
        predefinedSubTypes: ['AD210001', 'CH210001'],
        forceCreate: false,
        openInNewTab: false,
        contextSelectorDisabled: true
      };

      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedConfigurations);
    });

    it('should call open dialog with configuration for one predefinedTypes', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: ['emf:Document'],
          predefinedSubTypes: ['AD210001'],
          forceCreate: false,
          openInNewTab: false
        }
      };

      let expectedConfigurations = {
        parentId: 'curent-object-id',
        returnUrl: 'href-returnUrl',
        operation: 'create',
        exclusions: [],
        instanceType: 'emf:Document#full',
        predefinedTypes: ['emf:Document'],
        contextSelectorDisabled: true,
        predefinedSubTypes: ['AD210001'],
        forceCreate: false,
        openInNewTab: false
      };

      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedConfigurations);
    });

    it('should call open dialog with configuration for two predefinedTypes', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: ['emf:Document', 'chd:Survey', null, undefined, '', '   '],
          predefinedSubTypes: ['AD210001']
        }
      };

      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;

      expect(actualConfiguration).to.deep.equal(getExpectedConfigurationWithManySpecifiedType());
    });

    it('should call open dialog for configuration without predefinedTypes (with not valid elements of predefinedTypes)', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: [null, '', '  ', undefined],
          predefinedSubTypes: ['AD210001']
        }
      };
      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(getExpectedConfigurationWithoutSpecifiedType());
    });

    it('should call open dialog with configuration without predefinedTypes (with null predefinedTypes)', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: null,
          predefinedSubTypes: ['AD210001']
        }
      };
      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(getExpectedConfigurationWithoutSpecifiedType());
    });

    it('should call open dialog with configuration without predefinedTypes (with undefined predefinedTypes)', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedSubTypes: ['AD210001']
        }
      };
      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(getExpectedConfigurationWithoutSpecifiedType());
    });

    it('should call open dialog with configuration without predefinedTypes (with empty predefinedTypes)', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: [],
          predefinedSubTypes: ['AD210001']
        }
      };
      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(getExpectedConfigurationWithoutSpecifiedType());
    });

    it('should call open dialog with configuration for one predefinedTypes and empty predefinedTypes(scenario when subtype is not child of predefinedType)', () => {
      var openDialog = sinon.spy(createInstanceAction.createPanelService, 'openCreateInstanceDialog');
      let action = {
        configuration: {
          predefinedTypes: ['emf:Task'],
          predefinedSubTypes: ['GEP11111'],
          forceCreate: false,
          openInNewTab: false
        }
      };

      let expectedConfigurations = {
        parentId: 'curent-object-id',
        returnUrl: 'href-returnUrl',
        operation: 'create',
        exclusions: [],
        instanceType: 'emf:Task#full',
        predefinedTypes: ['emf:Task'],
        predefinedSubTypes: [],
        contextSelectorDisabled: true,
        forceCreate: false,
        openInNewTab: false
      };

      createInstanceAction.execute(action, getContext());
      expect(openDialog.calledOnce).to.be.true;
      var actualConfiguration = openDialog.args[0][0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedConfigurations);
    });
  });
});

function getContext() {
  return {
    currentObject: {
      id: 'curent-object-id'
    }
  };
};

function mockNamespaceService() {
  return {
    toFullURI: sinon.spy((uris) => {
      var map = {};
      uris.forEach((uri) => {
        map[uri] = uri + '#full';
      });
      return PromiseStub.resolve({data: map});
    })
  };
};

function mockModelsService() {
  return {
    getClassInfo: sinon.spy((configPredefinedTypes) => {

      if (configPredefinedTypes[0] == 'emf:Document') {
        return PromiseStub.resolve(
          {
            data: {
              creatable: true,
              uploadable: true
            }
          });
      }

      if (configPredefinedTypes[0] == 'chd:Survey') {
        return PromiseStub.resolve(
          {
            data: {
              creatable: true,
              uploadable: false
            }
          });
      }

      if (configPredefinedTypes[0] == 'chd:SurveyUploadable') {
        return PromiseStub.resolve(
          {
            data: {
              creatable: false,
              uploadable: true
            }
          });
      }


      var responseData = {
        creatable: true,
        uploadable: true
      };
      return PromiseStub.resolve({data: responseData});
    }),
    getModels: sinon.spy((purpose, contextId, mimetype, fileExtension, classFilter, definitionFilter) => {
      if (classFilter.length === 1 && classFilter[0] === 'chd:Survey' && definitionFilter.length === 1 && definitionFilter[0] === 'AD210001') {
        return PromiseStub.resolve({models: ['a model returned']});
      }
      if (classFilter.length === 1 && classFilter[0] === 'chd:SurveyUploadable' && definitionFilter.length === 1 && definitionFilter[0] === 'AD210001') {
        return PromiseStub.resolve({models: ['a model returned']});
      }
      if (classFilter.length === 1 && classFilter[0] === 'emf:Document' && definitionFilter.length === 2 && definitionFilter[0] === 'AD210001' && definitionFilter[1] === 'CH210001') {
        return PromiseStub.resolve({models: ['a model returned']});
      }
      if (classFilter.length === 1 && classFilter[0] === 'emf:Document' && definitionFilter.length === 1 && definitionFilter[0] === 'AD210001') {
        return PromiseStub.resolve({models: ['a model returned']});
      }
      return PromiseStub.resolve({models: []});
    })
  };
};

function getExpectedConfigurationWithManySpecifiedType() {
  return {
    parentId: 'curent-object-id',
    returnUrl: 'href-returnUrl',
    operation: 'create',
    exclusions: undefined,
    instanceType: undefined,
    predefinedTypes: ['emf:Document', 'chd:Survey'],
    predefinedSubTypes: [],
    forceCreate: undefined,
    openInNewTab: undefined,
    contextSelectorDisabled: true
  };
};

function getExpectedConfigurationWithoutSpecifiedType() {
  return {
    parentId: 'curent-object-id',
    returnUrl: 'href-returnUrl',
    operation: 'create',
    exclusions: undefined,
    instanceType: undefined,
    predefinedTypes: [],
    forceCreate: undefined,
    openInNewTab: undefined,
    contextSelectorDisabled: true,
    predefinedSubTypes: []
  };
};
