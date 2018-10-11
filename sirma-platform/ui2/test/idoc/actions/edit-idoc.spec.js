import {IdocMocks} from '../idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {EditIdocAction} from 'idoc/actions/edit-idoc-action';
import {PromiseStub} from 'test/promise-stub';
import {AfterEditActionExecutedEvent} from 'idoc/actions/events/after-edit-action-executed-event';

describe('Edit idoc action', () => {
  const IDOC_ID = 'emf:123456';
  let currentObject;

  var eventbus = {
    publish: sinon.spy(),
    subscribe: sinon.spy()
  };

  beforeEach(() => {
    sinon.stub(IdocContext.prototype, 'getCurrentObject', () => {
      return new Promise((resolve) => {
        resolve(currentObject);
      });
    });
  });

  afterEach(() => {
    IdocContext.prototype.getCurrentObject.restore();
  });

  it('should update state params properly and set version mode property to MINOR', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.currentObject = currentObject;
    let router = {
      navigate: sinon.spy()
    };
    let instanceService = getInstanceServiceMock();
    let logger = () => {
    };
    let handler = new EditIdocAction(router, IdocMocks.mockStateParamsAdapter(IDOC_ID, 'edit'), instanceService, eventbus, logger, IdocMocks.mockActionsService(), PromiseStub, IdocMocks.mockIdocDraftService());
    var actionContext = {
      idocContext: {
        reloadObjectDetails: () => PromiseStub.resolve()
      },
      currentObject: currentObject,
      idocPageController: idocPage
    };
    handler.execute({}, actionContext);

    expect(idocPage.context).to.have.property('mode', 'edit');
    expect(handler.router.navigate.calledOnce);
    expect(handler.router.navigate.getCall(0).args[0]).to.equal('idoc');
    expect(handler.router.navigate.getCall(0).args[1]).to.have.property('mode', 'edit');
    expect(handler.router.navigate.getCall(0).args[2]).to.have.property('notify', false);

    expect(currentObject.getModels()['$versionMode$']).to.equal('MINOR');

    var event = eventbus.publish.args[0][0];
    expect(event instanceof AfterEditActionExecutedEvent).to.be.true;
    expect(event.getData()[0]).to.equal(actionContext);
  });

  it('afterInstanceRefreshHandler should load draft', () => {
    let idocDraftServiceMock = IdocMocks.mockIdocDraftService();
    let loadDraftSpy = sinon.spy(idocDraftServiceMock, 'loadDraft');
    var handler = new EditIdocAction(IdocMocks.mockRouter(), IdocMocks.mockStateParamsAdapter(IDOC_ID, 'edit'), getInstanceServiceMock(), IdocMocks.mockEventBus(), {}, IdocMocks.mockActionsService(), PromiseStub, idocDraftServiceMock);

    var context = {
      currentObject: new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent())
    };

    context.idocPageController = {
      setViewMode: sinon.stub(),
      appendContent: sinon.spy(),
      startDraftInterval: sinon.spy()
    };

    handler.afterInstanceRefreshHandler(context);
    expect(loadDraftSpy.calledOnce).to.be.true;
    expect(context.idocPageController.startDraftInterval.calledOnce).to.be.true;
    expect(context.idocPageController.appendContent.calledOnce).to.be.true;
    expect(context.idocPageController.appendContent.getCall(0).args[0]).to.equal('content');
  });

  it('afterInstanceRefreshHandler should load draft content if draft is loaded', () => {
    let idocDraftServiceMock = IdocMocks.mockIdocDraftService();
    idocDraftServiceMock.loadDraft = () => PromiseStub.resolve({loaded: true, content: 'draft content'});
    var handler = new EditIdocAction(IdocMocks.mockRouter(), IdocMocks.mockStateParamsAdapter(IDOC_ID, 'edit'), getInstanceServiceMock(),
      IdocMocks.mockEventBus(), {}, IdocMocks.mockActionsService(), PromiseStub, idocDraftServiceMock);

    var context={} ;
    context.idocPageController = {
      setViewMode: sinon.stub(),
      appendContent: sinon.spy(),
      startDraftInterval: sinon.spy()
    };
    context.currentObject = {
      setContent: sinon.spy(),
      getId: sinon.spy()
    };

    handler.afterInstanceRefreshHandler(context);
    expect(context.idocPageController.appendContent.calledOnce).to.be.true;
    expect(context.idocPageController.appendContent.getCall(0).args[0]).to.equal('draft content');
    expect(context.currentObject.setContent.calledOnce).to.be.true;
    expect(context.currentObject.setContent.getCall(0).args[0]).to.equal('draft content');
    expect(context.idocPageController.startDraftInterval.calledOnce).to.be.true;

  });

  function getInstanceServiceMock() {
    return {
      load: () => {
        return PromiseStub.resolve({
          data: {
            id: 'id'
          }
        });
      },
      loadView: () => {
        return PromiseStub.resolve({
          data: 'content'
        });
      }
    };
  }
});