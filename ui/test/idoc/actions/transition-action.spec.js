import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {TransitionAction} from 'idoc/actions/transition-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'idoc/idoc-context';
import {PromiseAdapterMock} from '../../adapters/angular/promise-adapter-mock';

describe('TransitionAction', () => {
  let spyExecuteTransition;
  let payload;
  let handler;
  let context;
  let eventbus;
  let models;
  let id = 'emf:123456';
  beforeEach(() => {
    let actionsService = {
      executeTransition: () => {
        return PromiseStub.resolve({})
      }
    };
    spyExecuteTransition = sinon.spy(actionsService, 'executeTransition');
    payload = {
      operation: 'transition',
      userOperation: 'approve',
      contextPath: ['emf:123456'],
      targetInstance: {
        definitionId: 'ET210001',
        properties: {}
      }
    };
    TransitionAction.prototype.buildActionPayload = () => {
      return payload
    };
    models = {
      definitionId: 'ET220001'
    };
    context = {
      currentObject: {
        models: models,
        contextPath: ['emf:999999'],
        getId: () => {
          return id;
        },
        isPersisted: () => {
          return true
        },
        getModels: () => {
          return models
        },
        getChangeset: () => {
          return {
            property1: 123
          }
        }
      },
      idocContext: {
        mergeObjectsModels: sinon.spy()
      },
      idocPageController: {
        checkPermissionsForEditAction: sinon.spy()
      }
    };
    let validationService = {
      validate: () => {
        return true;
      },
      init: ()=> {
        return PromiseStub.resolve();
      }
    };
    let saveDialogService = {
      openDialog: ()=> {
        return PromiseStub.reject();
      }
    };
    let instanceRestService = {
      load: (id) => {
        return PromiseStub.resolve({
          data: {
            id: id,
            properties: {},
            relations: {},
            headers: ['header']
          }
        });
      },
      loadModel: () => {
        return PromiseStub.resolve({
          data: {
            definitionId: 'ET220001',
            validationModel: {'field1': {}},
            viewModel: {
              fields: []
            },
            headers: ['header']
          }
        });
      }
    };
    eventbus = {
      publish: sinon.spy()
    };
    handler = new TransitionAction(actionsService, validationService, saveDialogService, instanceRestService, eventbus, {}, PromiseAdapterMock.mockAdapter());
  });

  describe('Execute()', ()=> {
    it('should restore the model if the transition is canceled', ()=> {
      let actionDefinition = {
        action: 'approve'
      };
      let dialogSpy = sinon.spy(handler.saveDialogService, 'openDialog');
      sinon.stub(handler.validationService, 'validate').returns(false);
      sinon.stub(handler, 'getObjectModel').returns(PromiseStub.resolve({
        'emf:123456': new InstanceObject(id, {
          id: 'emf:123456',
          definitionId: 'ET220001',
          validationModel: {'field1': {}},
          viewModel: {
            fields: []
          },
          headers: ['header']
        })
      }));
      handler.execute(actionDefinition, context);
      expect(dialogSpy.called).to.be.true;
      expect(spyExecuteTransition.called).to.be.false;
    });
  });

  describe('executeTransition()', () => {
    it('should invoke service to execute the transition with proper arguments and without idoc context', () => {
      context.idocContext = null;
      handler.executeTransition(context, {}, models);
      expect(spyExecuteTransition.callCount).to.equal(1);
      expect(spyExecuteTransition.getCall(0).args).to.eql(['emf:123456', payload])
    });

    it('should invoke service to execute the transition with proper arguments', () => {
      handler.executeTransition(context, {}, models);
      expect(spyExecuteTransition.callCount).to.equal(1);
      expect(spyExecuteTransition.getCall(0).args).to.eql(['emf:123456', payload])
    });

    it('should publish InstanceRefreshEvent if action is properly executed', () => {
      handler.executeTransition(context, {}, models);
      expect(handler.eventbus.publish.calledOnce).to.be.true;
    });

    it('should invoke permissions check for edit button', () => {
      handler.executeTransition(context, {}, models);
      expect(context.idocPageController.checkPermissionsForEditAction.called).to.be.true;
    });

  });

  describe('onFormValidated()', () => {
    it('should disable OK button by setting disabled=true if form has invalid data', () => {
      let okButton = {};
      let data = [{
        isValid: false
      }];
      handler.onFormValidated(okButton, data);
      expect(okButton.disabled).to.be.true;
    });

    it('should enable OK button by setting disabled=false if form is valid', () => {
      let okButton = {};
      let data = [{
        isValid: true
      }];
      handler.onFormValidated(okButton, data);
      expect(okButton.disabled).to.be.false;
    });
  });

  describe('getObjectModel()', () => {
    it('should pass the services with proper arguments for model loading', () => {
      let actionDefinition = {
        action: 'approve'
      };
      let spyLoadInstance = sinon.spy(handler.instanceRestService, 'load');
      let spyLoadModel = sinon.spy(handler.instanceRestService, 'loadModel');
      handler.getObjectModel(context, actionDefinition);
      expect(spyLoadModel.calledOnce).to.be.true;
      expect(spyLoadModel.getCall(0).args[0]).to.eql('emf:123456');
      expect(spyLoadInstance.calledOnce).to.be.true;
      expect(spyLoadInstance.getCall(0).args).to.eql(['emf:123456']);
    });

    it('should return array with single model as returned by the instanceService', (done) => {
      let actionDefinition = {
        action: 'approve'
      };
      sinon.stub(handler.validationService, 'validate', () => {
        return false;
      });

      let expectedInstanceObject = new InstanceObject(id, {
        definitionId: 'ET220001',
        validationModel: {'field1': {}},
        viewModel: {
          fields: []
        },
        headers: ['header']
      });
      expectedInstanceObject.renderMandatory = true;

      handler.getObjectModel(context, actionDefinition).then((models) => {
        expect(JSON.stringify(models)).to.deep.equal(JSON.stringify({
          'emf:123456': expectedInstanceObject
        }));
        done();
      }).catch(done);
    });
  });
});