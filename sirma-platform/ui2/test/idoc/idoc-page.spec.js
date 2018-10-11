import {IdocMocks} from './idoc-mocks';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {BeforeIdocContentModelUpdateEvent} from 'idoc/events/before-idoc-content-model-update-event';
import {IdocContentModelUpdateEvent} from 'idoc/events/idoc-content-model-update-event';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {IdocPage} from 'idoc/idoc-page';
import {HELP_INSTANCE_TYPE} from 'services/help/help-service';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import base64 from 'common/lib/base64';
import {ModelingIdocContextBuilder} from 'idoc/template/modeling-idoc-context-builder';
import {stub} from 'test/test-utils';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import {SessionStorageService} from 'services/storage/session-storage-service';

describe('IdocPage', function () {
  const IDOC_ID = 'emf:123456';
  var currentObject;
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

  it('should redirect to preview mode if edit not allowed', () => {
    let idocPage = IdocMocks.instantiateIdocPage(IdocMocks.NO_EDIT_ALLOWED_IDOC_ID);

    idocPage.currentObject = new InstanceObject('emf:id', IdocMocks.generateModels(), IdocMocks.generateIntialContent());

    idocPage.context.isEditMode = function () {
      return true;
    };
    var spySetMode = sinon.spy(idocPage.context, 'setMode');
    var spyRouter = sinon.spy(idocPage.router, 'navigate');

    idocPage.hasEditPermission();
    expect(spySetMode.called).to.be.true;
    expect(spyRouter.called).to.be.true;
  });

  it('should not redirect to preview mode if edit mode and idoc is not persisted', () => {
    let idocPage = IdocMocks.instantiateIdocPage();

    idocPage.currentObject = new InstanceObject(undefined, IdocMocks.generateModels(), IdocMocks.generateIntialContent());

    idocPage.context.isEditMode = function () {
      return true;
    };

    var spySetMode = sinon.spy(idocPage.context, 'setMode');
    var spyRouter = sinon.spy(idocPage.router, 'navigate');

    idocPage.hasEditPermission();
    expect(spySetMode.called).to.be.false;
    expect(spyRouter.called).to.be.false;
  });

  it('should load draft and append content when mode is edit and idoc is persisted', (done) => {
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = {
      isPersisted: function () {
        return true;
      },
      isLocked: function () {
        return true;
      },
      getId(){
        return 'emf:123456';
      }
    };
    idocPage.context.isEditMode = function () {
      return true;
    };

    var stubLoadDraft = sinon.stub(idocPage.idocDraftService, 'loadDraft').returns(PromiseStub.resolve({loaded: true}));
    var spyAppendContent = sinon.spy(idocPage, 'appendContent');
    var spyStartDraftInterval = sinon.spy(idocPage, 'startDraftInterval');
    var spyPublish = sinon.spy(idocPage.eventbus, 'publish');

    idocPage.appendDraftContent({editAllowed: true}).then(() => {
      expect(spyAppendContent.called).to.be.true;
      expect(spyStartDraftInterval.called).to.be.true;
      expect(spyPublish.called).to.be.true;
      stubLoadDraft.restore();
      done();
    }).catch(done);
  });

  it('should load draft but not append content when result not loaded', (done) => {
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = {
      isPersisted: function () {
        return true;
      },
      isLocked: function () {
        return true;
      },
      getId(){
        return 'emf:123456';
      }
    };
    idocPage.context.isEditMode = function () {
      return true;
    };

    var stubLoadDraft = sinon.stub(idocPage.idocDraftService, 'loadDraft').returns(PromiseStub.resolve({loaded: false}));
    var spyAppendContent = sinon.spy(idocPage, 'appendContent');
    var spyStartDraftInterval = sinon.spy(idocPage, 'startDraftInterval');
    var spyPublish = sinon.spy(idocPage.eventbus, 'publish');

    idocPage.appendDraftContent({editAllowed: true}).then(() => {
      expect(spyAppendContent.called).to.be.false;
      expect(spyStartDraftInterval.called).to.be.true;
      expect(spyPublish.called).to.be.true;
      stubLoadDraft.restore();
      done();
    }).catch(done);
  });

  it('loadData should load system tabs if current object is persisted', () => {
    let idocPage = IdocMocks.instantiateIdocPage();
    let loadSystemTabsSpy = sinon.spy(idocPage, 'loadSystemTabs');
    idocPage.loadCurrentObject = () => {
      return PromiseStub.resolve({
        isPersisted: () => true,
        isVersion: () => false
      });
    };
    idocPage.loadData();
    expect(loadSystemTabsSpy.callCount).to.equals(1);
  });

  describe('loadSystemTabs', () => {
    let result = {
      'permissions-tab': {
        'id': 'permissions-tab',
        'name': 'system.tab.permissions',
        'component': 'seip-permissions',
        'module': 'idoc/system-tabs/permissions/permissions'
      },
      'versions-tab': {
        'id': 'versions-tab',
        'name': 'system.tab.versions',
        'component': 'seip-versions',
        'module': 'idoc/system-tabs/versions/versions'
      }
    };
    let idocPage;
    beforeEach(() => {
      idocPage = IdocMocks.instantiateIdocPage();
      idocPage.pluginsService.loadComponentModules = () => {
        return PromiseStub.resolve(result);
      };
    });

    it('should populate systemTabs array', () => {
      idocPage.loadSystemTabs();
      expect(idocPage.systemTabs).to.have.length(2);
      expect(idocPage.systemTabs[0]).to.have.property('id', 'permissions-tab');
      expect(idocPage.systemTabs[0]).to.have.property('system', true);
      expect(idocPage.systemTabs[1]).to.have.property('id', 'versions-tab');
      expect(idocPage.systemTabs[1]).to.have.property('system', true);
    });

    it('should load not filtered system tab', () => {
      result['mailbox-tab'] = {
        'id': 'mailbox-tab',
        'name': 'system.tab.mailbox',
        'component': 'seip-mailbox',
        'module': 'idoc/system-tabs/mailbox/mailbox',
        'filter': (context) => {
          return true;
        }
      };

      idocPage.loadSystemTabs();
      expect(idocPage.systemTabs).to.have.length(3);
      expect(idocPage.systemTabs[0]).to.have.property('id', 'permissions-tab');
      expect(idocPage.systemTabs[0]).to.have.property('system', true);
      expect(idocPage.systemTabs[1]).to.have.property('id', 'versions-tab');
      expect(idocPage.systemTabs[1]).to.have.property('system', true);
      expect(idocPage.systemTabs[2]).to.have.property('id', 'mailbox-tab');
      expect(idocPage.systemTabs[2]).to.have.property('system', true);
    });

    it('should not load filtered system tab', () => {
      result['mailbox-tab'].filter = (context) => {
        return false;
      };
      idocPage.loadSystemTabs();
      expect(idocPage.systemTabs).to.have.length(2);
      expect(idocPage.systemTabs[0]).to.have.property('id', 'permissions-tab');
      expect(idocPage.systemTabs[0]).to.have.property('system', true);
      expect(idocPage.systemTabs[1]).to.have.property('id', 'versions-tab');
      expect(idocPage.systemTabs[1]).to.have.property('system', true);
    });
  });

  it('should generates proper string as result when getIdocContent() is called', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    expect(idocPage.getIdocContent()).to.equal('<div data-tabs-counter="3"><section data-id="id_0" data-title="Title 0" data-default="true" data-show-navigation="true" data-show-comments="true" data-revision="exportable" data-locked="false" data-user-defined="false">Content 0</section>' +
      '<section data-id="id_1" data-title="Title 1" data-default="false" data-show-navigation="true" data-show-comments="false" data-revision="exportable" data-locked="false" data-user-defined="false">Content 1</section>' +
      '<section data-id="id_2" data-title="Title 2" data-default="false" data-show-navigation="false" data-show-comments="false" data-revision="exportable" data-locked="true" data-user-defined="true">Content 2</section>' +
      '<section data-id="id_3" data-title="Title 3" data-default="false" data-show-navigation="false" data-show-comments="true" data-revision="cloneable" data-locked="false" data-user-defined="true">Content 3</section></div>');
  });

  it('getIdocContent() should publish events for Idoc content model update', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.eventbus = {
      publish: sinon.spy()
    };
    idocPage.getIdocContent();

    expect(idocPage.eventbus.publish.args[0][0] instanceof BeforeIdocContentModelUpdateEvent).to.be.true;
    expect(idocPage.eventbus.publish.args[1][0] instanceof IdocContentModelUpdateEvent).to.be.true;
    expect(idocPage.eventbus.publish.args[2][0] instanceof AfterIdocContentModelUpdateEvent).to.be.true;
    expect(idocPage.eventbus.publish.callCount).to.equal(3);
  });

  describe('appendContent()', () => {
    it('should generate correct tabs array and set active tab correctly', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage();
      idocPage.currentObject = currentObject;
      let content = '<div>' +
        '<section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="true" data-show-navigation="true" data-show-comments="false">Second content</section>' +
        '</div>';
      idocPage.tabsConfig.activeTabId = undefined;
      idocPage.appendContent(content);
      expect(idocPage.tabsConfig.tabs[0]).to.have.property('title', 'First title');
      expect(idocPage.tabsConfig.tabs[0]).to.have.property('showNavigation', true);
      expect(idocPage.tabsConfig.tabs[0]).to.have.property('showComments', true);
      expect(idocPage.tabsConfig.tabs[1]).to.have.property('title', 'Second title');
      expect(idocPage.tabsConfig.tabs[1]).to.have.property('showNavigation', true);
      expect(idocPage.tabsConfig.tabs[1]).to.have.property('showComments', false);
      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_1');
      expect(idocPage.dynamicElementsRegistry.reload.calledOnce).to.be.true;
    });

    it('should set active tab to first tab if there is no default tab', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage();
      idocPage.currentObject = currentObject;
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="false" data-show-navigation="true" data-show-comments="false">Second content</section></div>';
      idocPage.appendContent(content);
      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_0');
    });

    it('should trim content empty paragraphs if mode is print', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID, 'print');
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
      idocPage.currentObject = currentObject;
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);
      expect(trimTrailingEmptyParagraphsSpy.callCount).to.equals(1);
    });

    it('should add and render all tabs if mode is print and there are no explicitly requested tabs', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID, 'print');
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
      idocPage.currentObject = currentObject;
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="false" data-show-navigation="true" data-show-comments="false">Second content</section></div>';
      idocPage.appendContent(content);
      expect(idocPage.tabsConfig.tabs.length).to.equals(2);
      expect(idocPage.tabsConfig.tabs[0].shouldRender).to.be.true;
      expect(idocPage.tabsConfig.tabs[1].shouldRender).to.be.true;
    });

    it('should add and render only requested tabs if mode is print', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID, 'print');
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
      idocPage.currentObject = currentObject;
      idocPage.requestedTabs = ['id_0', 'id_2'];
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="false" data-show-navigation="true" data-show-comments="false">Second content</section>' +
        '<section data-id="id_2" data-title="Third title" data-default="false" data-show-navigation="true" data-show-comments="false">Third content</section></div>';
      idocPage.appendContent(content);
      expect(idocPage.tabsConfig.tabs.length).to.equals(2);
      expect(idocPage.tabsConfig.tabs[0].id).to.equals('id_0');
      expect(idocPage.tabsConfig.tabs[0].shouldRender).to.be.true;
      expect(idocPage.tabsConfig.tabs[1].id).to.equals('id_2');
      expect(idocPage.tabsConfig.tabs[1].shouldRender).to.be.true;
    });

    it('should not trim content empty paragraphs if mode is not print', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID, 'edit');
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
      idocPage.currentObject = currentObject;
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);
      expect(trimTrailingEmptyParagraphsSpy.callCount).to.equals(0);
    });

    it('should disable save button while appending content if idoc is initialized', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID, 'edit');
      idocPage.currentObject = currentObject;
      idocPage.disableSaveButton = sinon.spy();
      idocPage.idocIsReady = true;
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);

      expect(idocPage.disableSaveButton.calledTwice).to.be.true;
      expect(idocPage.disableSaveButton.getCall(0).args[0]).to.be.true;
      expect(idocPage.disableSaveButton.getCall(1).args[0]).to.be.false;
    });

    it('shlouldn`t disable save button while appending content if idoc isn`t initialized', () => {
      currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID, 'edit');
      idocPage.currentObject = currentObject;
      idocPage.disableSaveButton = sinon.spy();
      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);
      expect(idocPage.disableSaveButton.called).to.be.false;
    });
  });

  it('setDefaultActiveTab() should set active tab to first tab if currently active tab does not exist', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.tabsConfig.activeTabId = 'id_1';
    idocPage.setDefaultActiveTab(false);
    expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_0');
  });

  it('setDefaultActiveTab() should does not set active tab to first tab if currently active tab exists', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.tabsConfig.activeTabId = 'id_1';
    idocPage.setDefaultActiveTab(true);
    expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_1');
  });

  it('setDefaultActiveTab() should not set active tab to system tab if not especially requested', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.tabsConfig.activeTabId = 'permission-tab';
    idocPage.setDefaultActiveTab(false);
    expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_0');
  });

  it('setDefaultActiveTab() should set active tab to system tab if especially requested', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.tabsConfig.activeTabId = 'permission-tab';
    idocPage.setDefaultActiveTab(true);
    expect(idocPage.tabsConfig).to.have.property('activeTabId', 'permission-tab');
  });

  it('setViewMode() should update correct variables', () => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage(IDOC_ID);
    idocPage.setViewMode('test');
    expect(idocPage.context).to.have.property('mode', 'test');
    expect(idocPage.stateParamsAdapter.getStateParam('mode')).to.equal('test');
  });

  it('toggleHeader() should toggle the state of the header when the header is visible', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.isHeaderVisible = true;
    idocPage.toggleHeader();
    expect(idocPage.isHeaderVisible).to.be.false;
  });

  it('toggleHeader() should toggle the state of the header when the header is not visible', () => {
    currentObject = new InstanceObject(null, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.isHeaderVisible = false;
    idocPage.toggleHeader();
    expect(idocPage.isHeaderVisible).to.be.true;
  });

  it('should redirect to error page when wrong URL is entered', () => {
    currentObject = new InstanceObject(null, null, IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateWrongIdocPage();
    let appendContentStub = sinon.stub(idocPage, 'appendContent');
    idocPage.loadCurrentObject().then(() => {
      expect(idocPage.router.navigate.getCall(0).args[0]).to.equal('error');
      appendContentStub.reset();
    });
  });

  it('should redirect to user dashboard when in preview mode with no id provided', (done) => {
    currentObject = new InstanceObject(null, null, IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateWrongIdocPage();
    idocPage.context.isEditMode = function () {
      return false;
    };
    idocPage.context.getCurrentObject = () => PromiseStub.resolve({
      getModels: function () {
        return undefined;
      }
    });

    idocPage.promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();

    idocPage.loadCurrentObject().then(() => {
    }, () => {
      expect(idocPage.router.navigate.getCall(0).args[0]).to.equal('userDashboard');
      done();
    });
  });

  it('should subscribe for IdocReadyEvent and InstanceRefreshEvent', () => {
    let eventbus = new Eventbus();
    let spySubscribe = sinon.spy(eventbus, 'subscribe');
    new IdocPage(undefined, IdocMocks.mockScope(), eventbus, IdocMocks.mockStateParamsAdapter(),
      IdocMocks.mockLocationAdapter(), IdocMocks.mockRouter(), {}, IdocMocks.mockTimeout(), IdocMocks.mockIdocContextFactory(),
      {}, IdocMocks.mockTranslateService(), {}, IdocMocks.mockActionsService(), IdocMocks.mockInterval(), IdocMocks.mockIdocDraftService(), PromiseAdapterMock.mockAdapter(),
      IdocMocks.mockConfiguration(), IdocMocks.mockPluginsService(), {}, {}, {}, stub(ModelingIdocContextBuilder), {}, stub(SessionStorageService));

    expect(spySubscribe.getCall(0).args[0]).to.equal(InstanceRefreshEvent);
    expect(spySubscribe.getCall(1).args[0]).to.equal(IdocReadyEvent);
  });

  it('should unlock instance on scope destroy', (done) => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'edit');
    idocPage.currentObject = currentObject;
    idocPage.events = [];
    var spyUnlock = sinon.spy(idocPage.actionsService, 'unlock');
    idocPage.loadCurrentObject = () => {
      return PromiseStub.resolve({
        isPersisted: () => true,
        isVersion: () => false
      });
    };
    idocPage.loadData().then(() => {
      idocPage.dynamicElementsRegistry = {
        destroy: sinon.spy()
      };
      idocPage.ngOnDestroy();
      expect(spyUnlock.called).to.be.true;
      done();
    }).catch(done);
  });

  it('should call destroy of inserted widgets registry on scope destroy', (done) => {
    currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'edit');
    idocPage.currentObject = currentObject;
    idocPage.events = [];
    var spyUnlock = sinon.spy(idocPage.actionsService, 'unlock');
    idocPage.loadData().then(() => {
      idocPage.dynamicElementsRegistry = {
        destroy: sinon.spy()
      };
      idocPage.ngOnDestroy();
      expect(idocPage.dynamicElementsRegistry.destroy.calledOnce).to.be.true;
      done();
    }).catch(done);
  });

  describe('setTabsCounter()', () => {
    it('should obtain the DOM attribute value ', () => {
      var idocPage = IdocMocks.instantiateIdocPage();
      var templateDom = {
        data: () => {
          return 2;
        }
      };
      idocPage.setTabsCounter(templateDom);
      expect(idocPage.tabsConfig.tabsCounter).to.equal(2);
    });

    it('should set the sections length as value if no DOM value is present ', () => {
      var idocPage = IdocMocks.instantiateIdocPage();
      var templateDom = {
        data: () => {
          return undefined;
        }
      };
      // two sections
      var sections = [{}, {}];
      idocPage.setTabsCounter(templateDom, sections);
      // the counter value shows the next number
      expect(idocPage.tabsConfig.tabsCounter).to.equal(3);
    });
  });

  describe('start stop draft interval', () => {
    it('startDraftInterval should start an interval if mode is edit', () => {
      currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'edit');
      let intervalSpy = sinon.spy(idocPage, '$interval');
      let saveDraftSpy = sinon.spy(idocPage.idocDraftService, 'saveDraft');
      idocPage.startDraftInterval();
      expect(intervalSpy.callCount).to.equal(1);
    });

    it('stopDraftInterval should cancel draft interval if such exists', () => {
      currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'edit');
      idocPage.$interval.cancel = sinon.spy();
      idocPage.draftInterval = idocPage.$interval;
      idocPage.stopDraftInterval();
      expect(idocPage.$interval.cancel.callCount).to.equal(1);
    });
  });

  describe('showTab', () => {
    it('should return true if mode is not print and tab is active tab', () => {
      let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'edit');
      idocPage.tabsConfig.activeTabId = 'tab1';
      expect(idocPage.showTab({id: 'tab1'})).to.be.true;
    });

    it('should return false if mode is print', () => {
      let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'print');
      idocPage.tabsConfig.activeTabId = 'tab1';
      expect(idocPage.showTab({id: 'tab1'})).to.be.false;
    });

    it('should return false if not print mode and tab is not active tab', () => {
      let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'edit');
      idocPage.tabsConfig.activeTabId = 'tab1';
      expect(idocPage.showTab({id: 'tab2'})).to.be.false;
    });
  });

  describe('refresh', () => {
    let instance;
    let idocPage;
    beforeEach(() => {
      instance = new InstanceObject("instance-id", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      idocPage = IdocMocks.instantiateIdocPage();
      idocPage.currentObject = new InstanceObject("current-object", IdocMocks.generateModels(), IdocMocks.generateIntialContent());
      idocPage.notificationService = {success: sinon.spy()};
    });

    it('should not notify if action definition absent', () => {
      idocPage.refresh(undefined, instance);
      expect(idocPage.notificationService.success.calledOnce).to.be.false;
    });

    it('should notify if action definition is present', () => {
      idocPage.refresh({label: "success"}, instance);
      expect(idocPage.notificationService.success.calledOnce).to.be.true;
    });
  });

  describe('configureContextualHelp(currentObject, helpService)', () => {
    it('should configure the contextual help if the instance is not a help one', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      idocPage.helpTarget = undefined;
      var helpServiceMock = {
        getHelpInstanceId: () => {
          return 'instance-id';
        }
      };
      var models = {
        definitionId: 'image',
        instanceType: 'image-type'
      };
      var currentObject = new InstanceObject('some-id', models);
      idocPage.configureContextualHelp(currentObject, helpServiceMock);

      expect(idocPage.helpTarget).to.equal('object.image');
    });

    it('should not configure the contextual help if the instance is a help one', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      idocPage.helpTarget = undefined;

      var models = {
        definitionId: 'help',
        instanceType: HELP_INSTANCE_TYPE
      };
      var currentObject = new InstanceObject('some-id', models);
      idocPage.configureContextualHelp(currentObject);

      expect(idocPage.helpTarget).to.not.exist;
    });

    it('should not configure the contextual help if there is no instance for the target', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      idocPage.helpTarget = undefined;
      var helpServiceMock = {
        getHelpInstanceId: () => {
          return undefined;
        }
      };
      var models = {
        definitionId: 'image',
        instanceType: 'image-type'
      };
      var currentObject = new InstanceObject('some-id', models);
      idocPage.configureContextualHelp(currentObject, helpServiceMock);

      expect(idocPage.helpTarget).to.not.exist;
    });
  });

  it('trimTrailingEmptyParagraphs should trim one empty paragraph at the end of content and all layout columns', () => {
    let idocPage = IdocMocks.instantiateIdocPage();
    let content = $('<section><div class="layoutmanager"><div class="layout-row"><div class="layout-column-editable"><p><br></p></div><div class="layout-column-editable"><p>&nbsp;</p></div></div></div><p><br></p> Free text content <p><br></p>  <p><br></p></section>');
    idocPage.trimTrailingEmptyParagraphs(content);
    expect(content.html()).to.equals('<div class="layoutmanager"><div class="layout-row"><div class="layout-column-editable"></div><div class="layout-column-editable"></div></div></div><p><br></p> Free text content <p><br></p>  ');
  });

  it('buildTabModel should trim empty paragraphs in print mode', () => {
    let idocPage = IdocMocks.instantiateIdocPage('emf:1234', 'print');
    idocPage.currentObject = {
      isVersion: () => true
    };
    let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
    idocPage.buildTabModel('<section></section>');
    expect(trimTrailingEmptyParagraphsSpy.callCount).to.equals(1);
  });

  it('buildTabModel should set showComments to false if current object is version', () => {
    let idocPage = IdocMocks.instantiateIdocPage();
    idocPage.currentObject = {
      isVersion: () => true
    };
    let tabModel = idocPage.buildTabModel('<section data-show-comments="true"></section>');
    expect(tabModel.showComments).to.be.false;
  });

  describe('updateContentForVersion', () => {
    it('should replace automatically searches with manually selected objects', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      /* Widget config object:
       {
       selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
       criteria: {
       id: 'testCriteriaId'
       }
       }
       */
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoiYXV0b21hdGljYWxseSIsImNyaXRlcmlhIjp7ImlkIjoidGVzdENyaXRlcmlhSWQifX0="></div></section></div>';
      let queriesMap = {
        testCriteriaId: ['emf:123456-v1', 'emf-999888-v3']
      };
      let updatedContent = idocPage.updateContentForVersion(content, queriesMap, {});
      let widgetConfigEncoded = $($(updatedContent).find('.widget')[0]).attr('config');
      let widgetConfig = JSON.parse(base64.decode(widgetConfigEncoded));
      expect(widgetConfig.selectObjectMode).to.equals(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.eql(queriesMap.testCriteriaId);
    });

    it('should replace automatically searches with manually selected object for widgets with single selection', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      /* Widget config object:
       {
       selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
       criteria: {
       id: 'testCriteriaId'
       },
       selection: SINGLE_SELECTION
       }
       */
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoiYXV0b21hdGljYWxseSIsImNyaXRlcmlhIjp7ImlkIjoidGVzdENyaXRlcmlhSWQifSwic2VsZWN0aW9uIjoic2luZ2xlIn0="></div></section></div>';
      let queriesMap = {
        testCriteriaId: ['emf-999888-v3']
      };
      let updatedContent = idocPage.updateContentForVersion(content, queriesMap, {});
      let widgetConfigEncoded = $($(updatedContent).find('.widget')[0]).attr('config');
      let widgetConfig = JSON.parse(base64.decode(widgetConfigEncoded));
      expect(widgetConfig.selectObjectMode).to.equals(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.be.undefined;
      expect(widgetConfig.selectedObject).to.equals('emf-999888-v3');
    });

    it('should replace manually selected URIs with the correct versioned URIs', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      /* Widget config object:
       {
       selectObjectMode: SELECT_OBJECT_MANUALLY,
       selectedObjects: ['emf:123456', 'emf:999888']
       }
       */
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoibWFudWFsbHkiLCJzZWxlY3RlZE9iamVjdHMiOlsiZW1mOjEyMzQ1NiIsImVtZjo5OTk4ODgiXX0="></div></section></div>';
      let urisMap = {
        'emf:123456': 'emf:123456-v1.2',
        'emf:999888': 'emf:999888-v1.7'
      };
      let updatedContent = idocPage.updateContentForVersion(content, {}, urisMap);
      let widgetConfigEncoded = $($(updatedContent).find('.widget')[0]).attr('config');
      let widgetConfig = JSON.parse(base64.decode(widgetConfigEncoded));
      expect(widgetConfig.selectObjectMode).to.equals(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.eql(['emf:123456-v1.2', 'emf:999888-v1.7']);
    });

    it('should replace manually selected URI with the correct versioned URI in case of single select (ODW)', () => {
      let idocPage = IdocMocks.instantiateIdocPage();
      /* Widget config object:
       {
       selectObjectMode: SELECT_OBJECT_MANUALLY,
       selectedObject: 'emf:123456'
       }
       */
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoibWFudWFsbHkiLCJzZWxlY3RlZE9iamVjdCI6ImVtZjoxMjM0NTYifQ=="></div></section></div>';
      let urisMap = {
        'emf:123456': 'emf:123456-v1.2',
        'emf:999888': 'emf:999888-v1.7'
      };
      let updatedContent = idocPage.updateContentForVersion(content, {}, urisMap);
      let widgetConfigEncoded = $($(updatedContent).find('.widget')[0]).attr('config');
      let widgetConfig = JSON.parse(base64.decode(widgetConfigEncoded));
      expect(widgetConfig.selectObjectMode).to.equals(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.be.undefined;
      expect(widgetConfig.selectedObject).to.equals('emf:123456-v1.2');
    });
  });

  it('isWidgetVersioned should return true if widget does not support versioning', () => {
    let idocPage = IdocMocks.instantiateIdocPage();
    expect(idocPage.isWidgetVersioned($('<div class="widget object-data-widget"></div>'))).to.be.true;
    expect(idocPage.isWidgetVersioned($('<div class="widget aggregated-table"></div>'))).to.be.false;
  });

  describe('initViewFromTemplate', () => {
    let idocPage;
    beforeEach(() => {
      idocPage = IdocMocks.instantiateIdocPage();
      var models = {
        validationModel: {
          'emf:hasTemplate': {
            value: undefined
          }
        }
      };

      var currentObject = new InstanceObject('some-id', models);
      idocPage.currentObject = currentObject;
    });

    it('initViewFromTemplate should set proper templateInstanceId', () => {
      let event = {
        template: {
          id: '1',
          templateInstanceId: 'templateId',
          content: 'T1'
        }
      };

      idocPage.initViewFromTemplate(event);

      expect(idocPage.currentObject.getModels().validationModel['emf:hasTemplate'].value).to.deep.equal({
        results: ['templateId'],
        add: ['templateId'],
        remove: [],
        total: 1,
        headers: {}
      });
    });

    it('initViewFromTemplate should set Blank template id when Blank template is selected', () => {
      let eventBlank = {
        template: {
          id: '99',
          templateInstanceId: undefined,
          content: 'blank'
        }
      };

      idocPage.initViewFromTemplate(eventBlank);

      expect(idocPage.currentObject.getModels().validationModel['emf:hasTemplate'].value).to.deep.equal({
        results: ['99'],
        add: ['99'],
        remove: [],
        total: 1,
        headers: {}
      });
    });
  });

});
