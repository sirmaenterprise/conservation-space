import {ActionExecutor} from 'services/actions/action-executor';
import {ActionHandler} from 'services/actions/action-handler';
import {PromiseStub} from 'test/promise-stub'

describe('ActionExecutor', () => {

  var eventbus;
  var promiseAdapter;
  var translateService = {
    translateInstantWithInterpolation: (key, params) => {
      if (key === 'action.confirm.operation') {
        key = 'Executing operation [{{operationName}}]?';
      }
      return key.replace('{{operationName}}', params.operationName);
    }
  };

  beforeEach(() => {
    eventbus = {
      publish: sinon.spy()
    };
    promiseAdapter = {
      resolve: () => {
        return PromiseStub.resolve();
      }
    };
  });

  describe('execute()', () => {
    it('should invoke the handler immediately if is a class of type ActionHandler', () => {
      var handler = new TestActionHandler();
      var dialogService = {};
      var pluginsService = {};
      var stubInvokeHandler = sinon.stub(ActionExecutor, 'invokeHandler');
      var actionExecutor = new ActionExecutor(dialogService, pluginsService, translateService, eventbus, promiseAdapter);
      actionExecutor.execute(handler, {});
      expect(stubInvokeHandler.calledOnce).to.be.true;
      expect(eventbus.publish.calledOnce).to.be.true;
      stubInvokeHandler.restore();
    });

    it('should invoke confirmation handler if action has confirmation message', () => {
      var dialogService = {};
      var pluginsService = {
        loadPluginModule: () => {
          return PromiseStub.resolve(new TestActionHandler());
        }
      };
      var stubConfirm = sinon.stub(ActionExecutor.prototype, 'confirm');
      var stubGetActionPlugins = sinon.stub(ActionExecutor.prototype, 'getActionPlugins', () => {
        return actionPluginDefinitions;
      });
      var actionExecutor = new ActionExecutor(dialogService, pluginsService, translateService);
      actionExecutor.execute({
        confirmationMessage: 'confirm'
      }, {});
      expect(stubConfirm.calledOnce).to.be.true;
      stubConfirm.restore();
      stubGetActionPlugins.restore();
    });

    it('should invoke confirmation handler if action has confirm=true property', () => {
      var dialogService = {};
      var pluginsService = {
        loadPluginModule: () => {
          return PromiseStub.resolve(new TestActionHandler());
        }
      };
      var stubConfirm = sinon.stub(ActionExecutor.prototype, 'confirm');
      var stubGetActionPlugins = sinon.stub(ActionExecutor.prototype, 'getActionPlugins', () => {
        return actionPluginDefinitions;
      });
      var actionExecutor = new ActionExecutor(dialogService, pluginsService, translateService);
      actionExecutor.execute({
        confirm: true
      }, {});
      expect(stubConfirm.calledOnce).to.be.true;
      stubConfirm.restore();
      stubGetActionPlugins.restore();
    });

    it('should load action handlers and invoke the handler', () => {
      var dialogService = {};
      var pluginsService = {
        loadPluginModule: () => {
          return PromiseStub.resolve(new TestActionHandler());
        }
      };
      var stubInvokeHandler = sinon.stub(ActionExecutor, 'invokeHandler');
      var stubConfirm = sinon.stub(ActionExecutor.prototype, 'confirm');
      var stubGetActionPlugins = sinon.stub(ActionExecutor.prototype, 'getActionPlugins', () => {
        return actionPluginDefinitions;
      });
      var actionExecutor = new ActionExecutor(dialogService, pluginsService, translateService, eventbus, promiseAdapter);
      actionExecutor.execute({}, {});
      expect(stubInvokeHandler.calledOnce).to.be.true;
      expect(eventbus.publish.calledOnce).to.be.true;
      stubConfirm.restore();
      stubGetActionPlugins.restore();
      stubInvokeHandler.restore();
    });
  });

  describe('invokeHandler', function() {
    it('should call handlers execute method', function () {
      var actionHandler = {
        execute: function (actionDefintion, context) {
        }
      };
      var spyExecuteMethod = sinon.spy(actionHandler, 'execute');
      ActionExecutor.invokeHandler(actionHandler, {}, {});
      expect(spyExecuteMethod.callCount).to.equal(1);
    });

    it('should pass correct arguments to executor', function() {
      var actionHandler = {
        execute: function(actionDefintion, context) {}
      };
      var spyExecuteMethod = sinon.spy(actionHandler, 'execute');
      var actionDefinition = {
        'id': 'create'
      };
      var context = {
        'id': '123'
      };
      ActionExecutor.invokeHandler(actionHandler, actionDefinition, context);
      var args = spyExecuteMethod.getCall(0).args;
      expect(args[0]).to.deep.equal({
        'id': 'create'
      });
      expect(args[1]).to.deep.equal({
        'id': '123'
      });
    });
  });

  describe('confirm', function() {

    it('should call dialog service with proper arguments where the confirmation message is default one', function() {
      var spyConfirmation = sinon.spy();
      var dialogServiceMock = {
        confirmation: spyConfirmation
      };
      var pluginsService = {
        getPluginDefinitions: function() {}
      };
      var actionExecutor = new ActionExecutor(dialogServiceMock, pluginsService, translateService);
      var actionHandler = {};
      var actionDefinition = {
        name: 'create'
      };
      var context = {};
      actionExecutor.confirm(actionHandler, actionDefinition, context);
      var args = spyConfirmation.getCall(0).args;
      expect(args[1]).to.equal(null);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(ActionExecutor.CONFIRM);
      expect(args[2].buttons[1].id).to.equal(ActionExecutor.CANCEL);
      expect(args[2].onButtonClick !== undefined).to.be.true;
    });

    it('should call dialog service with proper arguments where the confirmation message is provided', function() {
      var spyConfirmation = sinon.spy();
      var dialogServiceMock = {
        confirmation: spyConfirmation
      };
      var pluginsService = {
        getPluginDefinitions: function() {}
      };
      var actionExecutor = new ActionExecutor(dialogServiceMock, pluginsService, translateService);
      var actionHandler = {};
      var actionDefinition = {
        name: 'create',
        label: 'Create project',
        confirmationMessage: 'Test confirmation [{{operationName}}]?'
      };
      var context = {};
      actionExecutor.confirm(actionHandler, actionDefinition, context);
      var args = spyConfirmation.getCall(0).args;
      expect(args[0]).to.equal('Test confirmation [Create project]?');
    });
  });

  describe('getConfirmationMessage', () => {
    it('should use confirmation message provided with the action definition', () => {
      var actionDefinition = {
        name: 'delete',
        label: 'Delete document',
        confirmationMessage: 'Executing operation [{{operationName}}]?'
      };
      let message = ActionExecutor.getConfirmationMessage(actionDefinition, translateService);
      expect(message).to.equal('Executing operation [Delete document]?');
    });

    it('should use a default confirmation message if there is no confirmationMessage in action definition', () => {
      var actionDefinition = {
        name: 'delete',
        label: 'Delete document'
      };
      let message = ActionExecutor.getConfirmationMessage(actionDefinition, translateService);
      expect(message).to.equal('Executing operation [Delete document]?');
    });
  });

  describe('getActionPlugins()', () => {
    it('should return already loaded plugins for extension point', () => {
      var dialogService = {};
      var pluginsService = {};
      var actionExecutor = new ActionExecutor(dialogService, pluginsService, translateService);
      actionExecutor.actionPlugins['extension.point'] = actionPluginDefinitions;
      var actionPlugins = actionExecutor.getActionPlugins('extension.point');
      // Then return cached definitions
      expect(actionPlugins).to.eql(actionPluginDefinitions);
    });

    it('should load plugins for extension point cache them and return the result', () => {
      var dialogService = {};
      var pluginsService = {
        getPluginDefinitions: (extensionPoint, name) => {
          return actionPluginDefinitions;
        }
      };
      var actionExecutor = new ActionExecutor(dialogService, pluginsService, translateService);
      var actionPlugins = actionExecutor.getActionPlugins('extension.point');
      // Then cache loaded definitions
      expect(actionExecutor.actionPlugins['extension.point']).to.eql(actionPluginDefinitions);
      // Then return loaded definitions
      expect(actionPlugins).to.eql(actionPluginDefinitions);
    });

    describe('confirmHandler()', () => {
      it('should invoke action handler if OK/Confirm button is clicked and dismiss dialog', () => {
        var dialogConfig = {
          dismiss: sinon.spy()
        };
        var actionHandler = { execute: 1 };
        var actionDefinition = { name: 'action1' };
        var context = { prop: 1 };
        var stubInvokeHandler = sinon.stub(ActionExecutor, 'invokeHandler');

        var executor = new ActionExecutor(null, null, null, eventbus, promiseAdapter);
        executor.confirmHandler(ActionExecutor.CONFIRM, dialogConfig, actionHandler, actionDefinition, context);

        expect(stubInvokeHandler.calledOnce).to.be.true;
        expect(stubInvokeHandler.getCall(0).args).to.eql([{ execute: 1 }, { name: 'action1' }, { prop: 1 }]);
        expect(dialogConfig.dismiss.calledOnce).to.be.true;
        stubInvokeHandler.restore();
      });

      it('should not invoke action handler if OK/Confirm button is not pressed', () => {
        var dialogConfig = {
          dismiss: sinon.spy()
        };
        var actionHandler = { execute: 1 };
        var actionDefinition = { name: 'action1' };
        var context = { prop: 1 };
        var stubInvokeHandler = sinon.stub(ActionExecutor, 'invokeHandler');

        var executor = new ActionExecutor(null, null, null, eventbus, promiseAdapter);
        executor.confirmHandler('Cancel', dialogConfig, actionHandler, actionDefinition, context);

        expect(stubInvokeHandler.callCount).to.equal(0);
        expect(dialogConfig.dismiss.calledOnce).to.be.true;
        stubInvokeHandler.restore();
      });
    });
  });

  let actionPluginDefinitions = {
    'action1': {
      'name': 'action1'
    },
    'action2': {
      'name': 'action2'
    }
  };

});


class TestActionHandler extends ActionHandler {
  execute() {
  }
}