import {BpmActionsToolbar} from 'idoc/toolbars/bpm-actions-toolbar/bpm-actions-toolbar';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import {StatusCodes} from 'services/rest/status-codes';
import {PromiseStub} from 'test/promise-stub';

describe('BpmActionsToolbar ', () => {

  let toolbar;
  let bpmService;
  let actionsService;
  let configService;
  let eventBus;
  let actionExecutor;
  let stubCollectImplementedHandlers;

  beforeEach(() => {
    ActionsHelper.prototype.getActionsLoaderConfig = () => {
      return {};
    };

    stubCollectImplementedHandlers = sinon.stub(ActionsHelper, 'collectImplementedHandlers', () => {
      return {
        'bpmTransitionAction': {}
      };
    });
    actionExecutor = mockActionExecutor();
    bpmService = mockBpmService();
    actionsService = mockActionService();
    configService = mockConfigService();
    eventBus = mockEventBus();
    BpmActionsToolbar.prototype.config = mockConfig();
    BpmActionsToolbar.prototype.isToolbarEnabled = () => {
      return true;
    };
    toolbar = new BpmActionsToolbar(bpmService, actionsService, configService, actionExecutor, eventBus);
  });

  afterEach(() => {
    stubCollectImplementedHandlers.restore();
  });

  it('executeAction with existing action', () => {
    toolbar.executeAction({
      serverOperation: 'bpmTransition',
      disabled: false
    });
    expect(actionExecutor.execute).to.be.calledOnce;
  });

  it('executeAction with disabled action', () => {
    toolbar.executeAction({
      serverOperation: 'bpmTransition',
      disabled: true
    });
    expect(actionExecutor.execute.called).to.be.false;
  });

  function mockEventBus() {
    return {
      subscribe: sinon.spy(() => {

      })
    };
  }

  function mockBpmService() {
    return {
      getInfo: (id) => {
        return PromiseStub.resolve({
          status: StatusCodes.SUCCESS,
          data: {
            active: true,
            process: {
              id: 'emf:id',
              headers: {
                compact_header: '<span>Compact header</span>'
              }
            }
          }
        });
      }
    };
  }

  function mockActionService() {
    return {
      getFlatActions: sinon.spy(() => {
        return PromiseStub.resolve({
          data: [{
            serverOperation: 'bpmTransition'
          }, {
            serverOperation: 'action'
          }]
        });
      })
    };
  }

  function mockActionExecutor() {
    return {
      execute: sinon.spy(() => {
        return PromiseStub.resolve({});
      })
    }
  }

  function mockConfig() {
    return {
      idocContext: {
        isPreviewMode: () => {
          return true;
        }
      },
      currentObject: {
        id: 'emf:id',
        getContextPathIds: () => {
          return {};
        }
      }
    };
  }

  function mockConfigService() {
    return {
      get: () => {
        return "";
      }
    };
  }
});