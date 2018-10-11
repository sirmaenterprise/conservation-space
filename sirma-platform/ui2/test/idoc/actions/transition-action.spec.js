import {TransitionAction} from 'idoc/actions/transition-action';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {InstanceRestService} from 'services/rest/instance-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {ActionsService} from 'services/rest/actions-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {stub} from 'test/test-utils';

describe('TransitionAction', () => {
  let payload;
  let handler;
  let context;
  let eventbus;
  let models;
  let id = 'emf:123456';
  beforeEach(() => {
    let actionsService = stub(ActionsService);
    actionsService.executeTransition.returns(PromiseStub.resolve());

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
      return payload;
    };

    models = {
      definitionId: 'ET220001'
    };
    context = {
      currentObject: {
        models,
        contextPath: ['emf:999999'],
        getId: () => {
          return id;
        },
        isPersisted: () => {
          return true;
        },
        getModels: () => {
          return models;
        },
        getChangeset: () => {
          return {
            property1: 123
          };
        }
      },
      idocContext: {
        mergeObjectsModels: sinon.spy()
      },
      idocActionsController: {
        loadActions: sinon.spy()
      }
    };

    let validationService = stub(ValidationService);
    validationService.init.returns(PromiseStub.resolve());
    validationService.validate.returns(true);

    let saveDialogService = stub(SaveDialogService);
    saveDialogService.openDialog.returns(PromiseStub.reject());

    let instanceRestService = stub(InstanceRestService);
    instanceRestService.loadInstanceObject.returns(PromiseStub.resolve(new InstanceObject('emf:123456', {
      definitionId: 'ET220001',
      validationModel: {'field1': {}},
      viewModel: {
        fields: []
      },
      headers: ['header']
    })));

    eventbus = stub(Eventbus);

    handler = new TransitionAction(actionsService, validationService, saveDialogService, instanceRestService, eventbus, {}, PromiseStub);
  });

  describe('Execute()', () => {
    it('should restore the model if the transition is canceled', () => {
      let actionDefinition = {
        action: 'approve'
      };
      handler.validationService.validate.returns(false);
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
      expect(handler.saveDialogService.openDialog.called).to.be.true;
      expect(handler.actionsService.executeTransition.called).to.be.false;
    });
  });

  describe('executeTransition()', () => {
    it('should invoke service to execute the transition with proper arguments and without idoc context', () => {
      context.idocContext = null;
      handler.executeTransition(context, {}, models);
      expect(handler.actionsService.executeTransition.callCount).to.equal(1);
      expect(handler.actionsService.executeTransition.getCall(0).args).to.eql(['emf:123456', payload]);
    });

    it('should invoke service to execute the transition with proper arguments', () => {
      handler.executeTransition(context, {}, models);
      expect(handler.actionsService.executeTransition.callCount).to.equal(1);
      expect(handler.actionsService.executeTransition.getCall(0).args).to.eql(['emf:123456', payload]);
    });

    it('should publish InstanceRefreshEvent if action is properly executed', () => {
      handler.executeTransition(context, {}, models);
      expect(handler.eventbus.publish.calledOnce).to.be.true;
    });

    it('should invoke permissions check for edit button', () => {
      handler.executeTransition(context, {}, models);
      expect(context.idocActionsController.loadActions.called).to.be.true;
    });

  });

  describe('getObjectModel()', () => {
    it('should pass the services with proper arguments for model loading', () => {
      let actionDefinition = {
        action: 'approve'
      };
      handler.getObjectModel(context, actionDefinition);
      expect(handler.instanceRestService.loadInstanceObject.calledOnce).to.be.true;
      expect(handler.instanceRestService.loadInstanceObject.getCall(0).args).to.eql(['emf:123456', 'approve']);
    });

    it('should return array with single model as returned by the instanceService', () => {
      let actionDefinition = {
        action: 'approve'
      };
      handler.validationService.validate.returns(false);

      let expectedInstanceObject = new InstanceObject(id, {
        definitionId: 'ET220001',
        validationModel: {'field1': {}},
        viewModel: {
          fields: []
        },
        headers: ['header']
      });

      handler.getObjectModel(context, actionDefinition).then((models) => {
        expect(JSON.stringify(models)).to.deep.equal(JSON.stringify({
          'emf:123456': expectedInstanceObject
        }));
      });
    });
  });
});