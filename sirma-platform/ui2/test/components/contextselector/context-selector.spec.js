import {stub} from 'test/test-utils';
import {ContextSelector} from 'components/contextselector/context-selector';
import {PickerService} from 'services/picker/picker-service';
import {HeadersService} from 'instance-header/headers-service';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {TranslateService} from 'services/i18n/translate-service';
import {EventEmitter} from 'common/event-emitter';
import {PromiseStub} from 'promise-stub';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';

describe('ContextSelector', () => {

  const ERROR_MESSAGE_NO_PERMISSION_LABEL_KEY = 'error.permission';
  const ERROR_MESSAGE_NO_PERMISSION_LABEL = 'You do not have permission';
  const ERROR_MESSAGE_IN_CONTEXT_ONLY_LABEL_KEY = 'error.in_context_only';
  const ERROR_MESSAGE_IN_CONTEXT_ONLY_LABEL = 'You can create object in context only';
  const EMF_ID = 'emf:parent_id';
  const MESSAGE_NO_CONTEXT = 'No context';
  const IDOC_HEADER = 'idoc breadcrumb header';

  let contextSelector;
  let pickerService;
  let translateService;
  let headersService;
  let idocContextFactory;

  beforeEach(() => {
    pickerService = stub(PickerService);
    translateService = createTranslateServiceStub();
    headersService = createHeadersServiceStub();
    idocContextFactory = stub(IdocContextFactory);

    contextSelector = new ContextSelector(pickerService, translateService, headersService, idocContextFactory);
  });

  describe('selectContext', () => {
    it('should clear context when loading of context failed', () => {
      contextSelector.config.parentId = EMF_ID;
      pickerService.configureAndOpen.returns(PromiseStub.resolve([]));
      let loadContextStub = sinon.stub(contextSelector, 'loadContext').returns(PromiseStub.reject());
      let clearContextAndNotifyStub = sinon.stub(contextSelector, 'clearContextAndNotify');

      contextSelector.selectContext();

      expect(clearContextAndNotifyStub.calledOnce).to.be.true;

      loadContextStub.restore();
      clearContextAndNotifyStub.restore();
    });

    it('should load context when context changed', () => {
      let testData = [
        {
          newInd: [],
          isContextChangeEventPublished: false
        }, {
          newInd: [{id: EMF_ID}],
          isContextChangeEventPublished: true
        }, {
          oldId: [{id: EMF_ID}],
          isContextChangeEventPublished: true
        }, {
          oldId: EMF_ID,
          newInd: [{id: EMF_ID}],
          isContextChangeEventPublished: false
        }];
      testData.forEach((data) => {
        contextSelector.config.parentId = data.oldId;
        pickerService.configureAndOpen.returns(PromiseStub.resolve(data.newInd));
        let loadContextStub = sinon.stub(contextSelector, 'loadContext').returns(PromiseStub.resolve());
        let publishContextChangedEventStub = sinon.stub(contextSelector, 'publishContextChangedEvent');

        contextSelector.selectContext();

        expect(publishContextChangedEventStub.calledOnce).to.be.equal(data.isContextChangeEventPublished);
        loadContextStub.restore();
        publishContextChangedEventStub.restore();
      });
    });
  });

  it('should load context', () => {
    contextSelector.loadContext(EMF_ID);

    expect(contextSelector.config.parentId).to.equal(EMF_ID);
    expect(contextSelector.header).to.equal(IDOC_HEADER);
  });

  it('should clear context', () => {
    contextSelector.config.parentId = EMF_ID;
    contextSelector.header = IDOC_HEADER;

    contextSelector.clearContext();

    expect(contextSelector.config.parentId === null).to.be.true;
    expect(contextSelector.header).to.equal(MESSAGE_NO_CONTEXT);
    expect(pickerService.clearSelectedItems.calledOnce).to.be.true;
  });

  it('should unregister all subscribers and remove element on destroy', () => {
    let eventEmitter = stub(EventEmitter);

    let addErrorMessageHandler = createHandlerSpy();
    let clearErrorMessageHandler = createHandlerSpy();
    let removeErrorMessageHandler = createHandlerSpy();
    let clearContextHandler = createHandlerSpy();

    eventEmitter.subscribe.onCall(0).returns(addErrorMessageHandler);
    eventEmitter.subscribe.onCall(1).returns(clearErrorMessageHandler);
    eventEmitter.subscribe.onCall(2).returns(removeErrorMessageHandler);
    eventEmitter.subscribe.onCall(3).returns(clearContextHandler);
    contextSelector.config = {eventEmitter};

    contextSelector.ngOnInit();
    contextSelector.ngOnDestroy();

    expect(addErrorMessageHandler.unsubscribe.calledOnce).to.be.true;
    expect(clearErrorMessageHandler.unsubscribe.calledOnce).to.be.true;
    expect(removeErrorMessageHandler.unsubscribe.calledOnce).to.be.true;
    expect(clearContextHandler.unsubscribe.calledOnce).to.be.true;
  });

  it('should configure picker with the parent as selection', () => {
    contextSelector.config.parentId = EMF_ID;
    contextSelector.ngOnInit();
    expect(contextSelector.pickerService.setSelectedItems.calledWith(contextSelector.pickerConfig, [{id: EMF_ID}])).to.be.true;
  });

  function createHandlerSpy() {
    let Handler = sinon.spy();
    Handler.unsubscribe = sinon.spy();
    return Handler;
  }

  function createTranslateServiceStub() {
    let service = stub(TranslateService);
    service.translateInstant.withArgs(ERROR_MESSAGE_NO_PERMISSION_LABEL_KEY).returns(ERROR_MESSAGE_NO_PERMISSION_LABEL);
    service.translateInstant.withArgs(ERROR_MESSAGE_IN_CONTEXT_ONLY_LABEL_KEY).returns(ERROR_MESSAGE_IN_CONTEXT_ONLY_LABEL);
    service.translateInstant.withArgs(ContextSelector.NO_CONTEXT).returns(MESSAGE_NO_CONTEXT);
    return service;
  }

  function createHeadersServiceStub() {
    let service = stub(HeadersService);
    service.loadHeaders.withArgs([EMF_ID], HEADER_BREADCRUMB).returns(PromiseStub.resolve({[EMF_ID]: {[HEADER_BREADCRUMB]: IDOC_HEADER}}));
    return service;
  }

});
