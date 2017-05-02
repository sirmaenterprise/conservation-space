import {CloneAction} from 'idoc/actions/clone-action';
import {InstanceObject} from 'idoc/idoc-context';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ModelsService} from 'services/rest/models-service';

import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('CloneAction', () => {

  var cloneAction;
  beforeEach(() => {
    var emptyInstance = getInstanceData();
    var instanceRestService = mockInstanceRestService(emptyInstance, emptyInstance, emptyInstance);
    var eventbus = mockEventbus();
    var namespaceService = mockNamespaceService();
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    cloneAction = new CloneAction({}, instanceRestService, eventbus, namespaceService, promiseAdapter);
  });

  describe('execute()', () => {

    var actionContext;
    beforeEach(() => {
      actionContext = {
        currentObject: new InstanceObject('emf:789', {})
      };

      var originalInstance = getInstanceData('emf:123', 'document-definition', 'emf:Document', 'emf:parent');
      var toClone = getInstanceData('emf:123', 'document-definition', 'emf:Document');

      cloneAction.instanceRestService = mockInstanceRestService(originalInstance, toClone, getInstanceData());
      cloneAction.createPanelService.openCreateInstanceDialog = sinon.spy();
    });

    it('should fetch original & cloned instance properties', () => {
      cloneAction.execute(cloneAction, actionContext);

      expect(cloneAction.instanceRestService.load.calledOnce).to.be.true;
      expect(cloneAction.instanceRestService.load.getCall(0).args[0]).to.equal('emf:789');
      expect(cloneAction.instanceRestService.cloneProperties.calledOnce).to.be.true;
      expect(cloneAction.instanceRestService.cloneProperties.getCall(0).args[0]).to.equal('emf:789');
    });

    it('should construct correct configuration object for instance create dialog', () => {
      cloneAction.execute(cloneAction, actionContext);

      var openSpy = cloneAction.createPanelService.openCreateInstanceDialog;
      expect(openSpy.calledOnce).to.be.true;

      var expectedInstanceData = getInstanceData('emf:123', 'document-definition', 'emf:Document');
      var config = openSpy.getCall(0).args[0];
      expect(config.parentId).to.equal('emf:parent');
      expect(config.instanceData).to.deep.equal(expectedInstanceData);
      expect(config.instanceType).to.equal('emf:Document#full');
      expect(config.instanceSubType).to.equal('document-definition');
      expect(config.controls).to.exist;
      expect(config.forceCreate).to.be.true;
      expect(config.controls).to.deep.eq({showCreateMore: false});
      expect(config.forceCreate).to.be.true;
      expect(config.purpose).to.deep.equal([ModelsService.PURPOSE_CREATE, ModelsService.PURPOSE_UPLOAD]);
      expect(config.header).to.exist;
      expect(config.helpTarget).to.exist;
      expect(config.exclusions).to.contains('file-upload-panel');
      expect(config.createButtonLabel).to.exist;
    });

    it('should not set parent if there is not one', () => {
      var originalInstance = getInstanceData('emf:123', 'document-definition', 'emf:Document');
      var toClone = getInstanceData('emf:123', 'document-definition', 'emf:Document');
      cloneAction.instanceRestService = mockInstanceRestService(originalInstance, toClone, getInstanceData());

      cloneAction.execute(cloneAction, actionContext);

      var openSpy = cloneAction.createPanelService.openCreateInstanceDialog;
      var config = openSpy.getCall(0).args[0];
      expect(config.parentId).to.not.exist;
    });

    it('should configure onCreate() function', () => {
      cloneAction.createButtonHandler = sinon.spy();
      cloneAction.execute(cloneAction, actionContext);

      var openSpy = cloneAction.createPanelService.openCreateInstanceDialog;
      var config = openSpy.getCall(0).args[0];
      expect(config.onCreate).to.exist;

      config.onCreate();
      expect(cloneAction.createButtonHandler.calledOnce).to.be.true;
    });
  });

  describe('createButtonHandler()', () => {
    var clonedInstance;
    var models;
    beforeEach(() => {
      models = {
        instanceId: '???',
        definitionId: 'document-definition',
        parentId: 'emf:123'
      };
      clonedInstance = getInstanceData('emf:456', 'document-definition', 'emf:Document');
      cloneAction.instanceRestService = mockInstanceRestService(getInstanceData(), getInstanceData(), clonedInstance);
    });

    it('should provide correct data to the clone service', () => {
      cloneAction.createButtonHandler('emf:123', models);

      var cloneSpy = cloneAction.instanceRestService.cloneInstance;
      expect(cloneSpy.calledOnce).to.be.true;
      expect(cloneSpy.getCall(0).args[0]).to.equal('emf:123');

      var data = cloneSpy.getCall(0).args[1];
      expect(data.definitionId).to.equal('document-definition');
      expect(data.parentId).to.equal('emf:123');
      expect(data.properties).to.exist;
    });

    it('should throw an event that an instance is created/cloned', () => {
      cloneAction.createButtonHandler('emf:123', models);

      var publishSpy = cloneAction.eventbus.publish;
      expect(publishSpy.calledOnce).to.be.true;

      var publishPayload = publishSpy.getCall(0).args[0];
      expect(publishPayload instanceof InstanceCreatedEvent).to.be.true;
      expect(publishPayload.getData()[0].currentObject).to.exist;
      expect(publishPayload.getData()[0].currentObject).to.deep.equal(clonedInstance);
    });

    it('should resolve with the cloned instance', () => {
      cloneAction.createButtonHandler('emf:123', models).then((response) => {
        expect(response).to.deep.equal(clonedInstance);
      });
    });
  });

  function getInstanceData(id, definitionId, rdfType, parentId) {
    var data = {
      id: id,
      definitionId: definitionId,
      properties: {
        'rdf:type': [{id: rdfType}]
      }
    };
    if (parentId) {
      data.properties['hasParent'] = [{id: parentId}];
    }
    return data;
  }

  /**
   * Mocks the instance rest service with predefined instance data.
   * @param originalInstance - the original instance that will be loaded before opening the dialog.
   * @param properties - the cloned properties from the original instance
   * @param clonedInstance - the cloned instance after executing the action
   */
  function mockInstanceRestService(originalInstance, properties, clonedInstance) {
    return {
      load: sinon.spy(() => {
        return PromiseStub.resolve({data: originalInstance || {}});
      }),
      cloneProperties: sinon.spy(() => {
        return PromiseStub.resolve({data: properties || {}});
      }),
      cloneInstance: sinon.spy(() => {
        return PromiseStub.resolve({data: clonedInstance || {}});
      })
    };
  }

  function mockEventbus() {
    return {
      publish: sinon.spy()
    };
  }

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
  }
});