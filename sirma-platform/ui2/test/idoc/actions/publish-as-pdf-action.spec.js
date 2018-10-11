import {IdocMocks} from '../idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {PublishAsPdfAction} from 'idoc/actions/publish-as-pdf-action';
import {PromiseStub} from 'test/promise-stub';

describe('Publish as pdf action', () => {
  const IDOC_ID = 'emf:123456';
  const PUBLISH = 'publishAsPdf'
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

  it('should publish as pdf when call', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = currentObject;

    let translateService = IdocMocks.mockTranslateService();

    let actionsService = {
      publishAsPdf: () => {
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
      }
    }

    let spyPublishAsPdfAction = sinon.spy(actionsService, PUBLISH);
    let handler = new PublishAsPdfAction(actionsService, {}, notificationService, translateService);
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

    expect(spyPublishAsPdfAction.callCount).to.equal(1);
    expect(spyPublishAsPdfAction.getCall(0).args).to.eql(['emf:123456', payload]);
  });
});