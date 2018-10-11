import {CloneAction} from 'idoc/actions/clone-action';
import {InstanceObject} from 'models/instance-object';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {ModelsService} from 'services/rest/models-service';
import {PromiseStub} from 'test/promise-stub';
import {Eventbus} from 'services/eventbus/eventbus';
import {NamespaceService} from 'services/rest/namespace-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {stub} from 'test/test-utils';

describe('CloneAction', () => {

  let cloneAction;
  let instanceRestService;
  let modelService;

  beforeEach(() => {
    let eventbus = stub(Eventbus);
    let emptyInstance = getInstanceData();

    instanceRestService = stub(InstanceRestService);
    instanceRestService.load.returns(PromiseStub.resolve({data: emptyInstance}));
    instanceRestService.cloneProperties.returns(PromiseStub.resolve({data: emptyInstance}));
    instanceRestService.cloneInstance.returns(PromiseStub.resolve({data: emptyInstance}));

    let namespaceService = stub(NamespaceService);
    namespaceService.toFullURI.returns(PromiseStub.resolve({
      data: {
        'emf:Document': 'emf:Document#full'
      }
    }));

    modelService = stub(ModelsService);
    modelService.getExistingInContextInfo.returns(PromiseStub.resolve());
    cloneAction = new CloneAction({}, instanceRestService, eventbus, namespaceService, PromiseStub, modelService);
  });

  describe('execute()', () => {

    let actionContext;

    beforeEach(() => {
      actionContext = {
        currentObject: new InstanceObject('emf:789', {})
      };

      let originalInstance = getInstanceData('emf:123', 'document-definition', 'emf:Document', 'emf:parent');
      let toClone = getInstanceData('emf:123', 'document-definition', 'emf:Document');

      instanceRestService.load.returns(PromiseStub.resolve({data: originalInstance}));
      instanceRestService.cloneProperties.returns(PromiseStub.resolve({data: toClone}));
      instanceRestService.cloneInstance.returns(PromiseStub.resolve({data: getInstanceData()}));

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

      let openSpy = cloneAction.createPanelService.openCreateInstanceDialog;
      expect(openSpy.calledOnce).to.be.true;

      let expectedInstanceData = getInstanceData('emf:123', 'document-definition', 'emf:Document');
      let config = openSpy.getCall(0).args[0];
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
      let originalInstance = getInstanceData('emf:123', 'document-definition', 'emf:Document');
      let toClone = getInstanceData('emf:123', 'document-definition', 'emf:Document');
      instanceRestService.load.returns(PromiseStub.resolve({data: originalInstance}));
      instanceRestService.cloneProperties.returns(PromiseStub.resolve({data: toClone}));
      instanceRestService.cloneInstance.returns(PromiseStub.resolve({data: getInstanceData()}));

      cloneAction.execute(cloneAction, actionContext);

      let openSpy = cloneAction.createPanelService.openCreateInstanceDialog;
      let config = openSpy.getCall(0).args[0];
      expect(config.parentId).to.not.exist;
    });

    it('should configure onCreate() function', () => {
      cloneAction.createButtonHandler = sinon.spy(() => {
        return PromiseStub.resolve();
      });

      cloneAction.execute(cloneAction, actionContext);

      let openSpy = cloneAction.createPanelService.openCreateInstanceDialog;
      let config = openSpy.getCall(0).args[0];
      expect(config.onCreate).to.exist;

      config.onCreate();
      expect(cloneAction.createButtonHandler.calledOnce).to.be.true;
    });
  });

  describe('createButtonHandler()', () => {
    let clonedInstance;
    let models;
    beforeEach(() => {
      models = {
        instanceId: '???',
        definitionId: 'document-definition',
        parentId: 'emf:123'
      };
      clonedInstance = getInstanceData('emf:456', 'document-definition', 'emf:Document');
      instanceRestService.load.returns(PromiseStub.resolve({data: getInstanceData()}));
      instanceRestService.cloneProperties.returns(PromiseStub.resolve({data: getInstanceData()}));
      instanceRestService.cloneInstance.returns(PromiseStub.resolve({data: clonedInstance}));
    });

    it('should provide correct data to the clone service', () => {
      cloneAction.createButtonHandler('emf:123', models);

      let cloneSpy = cloneAction.instanceRestService.cloneInstance;
      expect(cloneSpy.calledOnce).to.be.true;
      expect(cloneSpy.getCall(0).args[0]).to.equal('emf:123');

      let data = cloneSpy.getCall(0).args[1];
      expect(data.definitionId).to.equal('document-definition');
      expect(data.parentId).to.equal('emf:123');
      expect(data.properties).to.exist;
    });

    it('should fire BeforeIdocSaveEvent and InstanceCreatedEvent when instance is created/cloned', () => {
      cloneAction.createButtonHandler('emf:123', models);

      let publishSpy = cloneAction.eventbus.publish;
      expect(publishSpy.calledTwice).to.be.true;
      expect(publishSpy.getCall(0).args[0] instanceof BeforeIdocSaveEvent).to.be.true;

      let publishPayload = publishSpy.getCall(1).args[0];
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
    let instanceModel = {
      id,
      definitionId,
      properties: {
        'rdf:type': {results: [rdfType]}
      }
    };
    if (parentId) {
      instanceModel.properties['hasParent'] = {results: [parentId]};
    }

    return instanceModel;
  }

});