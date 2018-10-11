import {IdocMocks} from '../idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {PublishAction} from 'idoc/actions/publish-action';
import {PromiseStub} from 'test/promise-stub';

describe('Publish idoc action', () => {
  const IDOC_ID = 'emf:123456';
  const PUBLISH = 'publish'
  var currentObject;
  var notificationService;
  beforeEach(() => {
    notificationService = {
      success: () => {
      }
    };
    sinon.stub(IdocContext.prototype, 'getCurrentObject', () => {
      return new Promise((resolve) => {
        resolve(currentObject);
      });
    });
  });
  afterEach(() => {
    IdocContext.prototype.getCurrentObject.restore();
  });

  it('should publish instance when call', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = currentObject;

    let translateService = IdocMocks.mockTranslateService();

    let actionsService = {
      publish: () => {
        return PromiseStub.resolve({
          data: {
            headers: {
              compact_header: 'header'
            }
          }
        })
      }
    };
    let context = {
      currentObject: {
        getId: () => 'emf:123456',
        getContextPathIds: () => [],
        getModels: () => '',
        getChangeset: () => ''
      },
      idocActionsController: {
        loadActions: sinon.spy()
      }
    };

    let spyPublishAction = sinon.spy(actionsService, PUBLISH);
    let handler = new PublishAction(actionsService, {}, notificationService, translateService);
    handler.execute({
        operation: PUBLISH,
        action: PUBLISH
      }, context
    );

    let payload = {
      operation: PUBLISH,
      userOperation: PUBLISH,
      contextPath: [],
      targetInstance: {
        definitionId: undefined,
        properties: ''
      }
    };

    expect(spyPublishAction.callCount).to.equal(1);
    expect(spyPublishAction.getCall(0).args).to.eql(['emf:123456', payload]);
    expect(context.idocActionsController.loadActions.called).to.be.true;
  });
});