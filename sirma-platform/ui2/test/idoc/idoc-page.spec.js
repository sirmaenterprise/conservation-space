import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {BeforeIdocContentModelUpdateEvent} from 'idoc/events/before-idoc-content-model-update-event';
import {IdocContentModelUpdateEvent} from 'idoc/events/idoc-content-model-update-event';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {PromiseStub} from 'test/promise-stub';
import {SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import base64 from 'common/lib/base64';
import {IdocPageTestHelper} from './idoc-page-test-helper';
import {stub} from 'test/test-utils';

const IDOC_ID = 'emf:123456';

describe('IdocPage', () => {

  it('setViewMode() should update correct letiables', () => {
    let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
    let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = instanceObject;

    idocPage.setViewMode('test');

    expect(idocPage.context).to.have.property('mode', 'test');
    expect(idocPage.stateParamsAdapter.getStateParam('mode')).to.equal('test');
  });

  it('should subscribe for IdocReadyEvent and InstanceRefreshEvent', () => {
    let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
    let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = instanceObject;

    expect(idocPage.eventbus.subscribe.getCall(0).args[0]).to.equal(InstanceRefreshEvent);
    expect(idocPage.eventbus.subscribe.getCall(1).args[0]).to.equal(IdocReadyEvent);
  });

  describe('on scope destroy', () => {
    it('should unlock instance', () => {
      let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      sinon.stub(instanceObject, 'isPersisted').returns(true);
      sinon.stub(instanceObject, 'isVersion').returns(false);

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.isEditMode.returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit', idocContext);
      sinon.stub(idocPage, 'loadSystemTabs').returns(PromiseStub.resolve(instanceObject));
      idocPage.events = [];

      idocPage.loadData();
      idocPage.ngOnDestroy();

      expect(idocPage.actionsService.unlock.called).to.be.true;
    });

    it('should call destroy of inserted widgets registry', () => {
      let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      sinon.stub(instanceObject, 'isPersisted').returns(true);
      sinon.stub(instanceObject, 'isVersion').returns(false);

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.isEditMode.returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit', idocContext);
      sinon.stub(idocPage, 'loadSystemTabs').returns(PromiseStub.resolve(instanceObject));
      idocPage.events = [];

      idocPage.loadData();
      idocPage.ngOnDestroy();

      expect(idocPage.dynamicElementsRegistry.destroy.calledOnce).to.be.true;
    });
  });

  it('trimTrailingEmptyParagraphs should trim one empty paragraph at the end of content and all layout columns', () => {
    let idocPage = IdocPageTestHelper.instantiateIdocPage();

    let content = $('<section><div class="layoutmanager"><div class="layout-row"><div class="layout-column-editable"><p><br></p></div><div class="layout-column-editable"><p>&nbsp;</p></div></div></div><p><br></p> Free text content <p><br></p>  <p><br></p></section>');
    idocPage.trimTrailingEmptyParagraphs(content);

    expect(content.html()).to.equals('<div class="layoutmanager"><div class="layout-row"><div class="layout-column-editable"></div><div class="layout-column-editable"></div></div></div><p><br></p> Free text content <p><br></p>  ');
  });

  it('isWidgetVersioned should return true if widget does not support versioning', () => {
    let idocPage = IdocPageTestHelper.instantiateIdocPage();

    expect(idocPage.isWidgetVersioned($('<div class="widget object-data-widget"></div>'))).to.be.true;
    expect(idocPage.isWidgetVersioned($('<div class="widget aggregated-table"></div>'))).to.be.false;
  });

  describe('toggleHeader', () => {
    it('should toggle the state of the header when the header is visible', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.isHeaderVisible = true;

      idocPage.toggleHeader();

      expect(idocPage.isHeaderVisible).to.be.false;
    });

    it('should toggle the state of the header when the header is not visible', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.isHeaderVisible = false;

      idocPage.toggleHeader();

      expect(idocPage.isHeaderVisible).to.be.true;
    });
  });

  describe('redirects', () => {
    it('should redirect to error page when wrong URL is entered', () => {
      let instanceObject = new InstanceObject(IDOC_ID, null, IdocPageTestHelper.generateIntialContent());

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.isEditMode.returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit', idocContext);

      idocPage.loadCurrentObject();

      expect(idocPage.router.navigate.getCall(0).args[0]).to.equal('error');
    });

    it('should redirect to user dashboard when in preview mode with no id provided', () => {
      let instanceObject = new InstanceObject(null, null, IdocPageTestHelper.generateIntialContent());

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.isEditMode.returns(false);

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'preview', idocContext);

      idocPage.loadCurrentObject();

      expect(idocPage.router.navigate.getCall(0).args[0]).to.equal('userDashboard');
    });
  });

  describe('hasEditPermission', () => {
    it('should redirect to preview mode if edit not allowed', () => {
      let instanceObject = new InstanceObject('emf:id', IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      sinon.stub(instanceObject, 'isPersisted').returns(true);

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.isEditMode.returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IdocPageTestHelper.NO_EDIT_ALLOWED_IDOC_ID, 'preview', idocContext);
      idocPage.currentObject = instanceObject;

      idocPage.hasEditPermission();

      expect(idocPage.context.setMode.called).to.be.true;
      expect(idocPage.router.navigate.called).to.be.true;
      expect(idocPage.router.navigate.getCall(0).args[0]).to.equal('idoc');
    });

    it('should not redirect to preview mode if edit mode and idoc is not persisted', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      sinon.stub(instanceObject, 'isPersisted').returns(false);

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.isEditMode.returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IdocPageTestHelper.NO_EDIT_ALLOWED_IDOC_ID, 'preview', idocContext);
      idocPage.currentObject = instanceObject;

      idocPage.hasEditPermission();

      expect(idocPage.context.setMode.called).to.be.false;
      expect(idocPage.router.navigate.called).to.be.false;
    });
  });

  describe('getIdocContent', () => {
    it('should generate proper content string', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit', idocContext);

      expect(idocPage.getIdocContent()).to.equal('<div data-tabs-counter="3"><section data-id="id_0" data-title="Title 0" data-default="true" data-show-navigation="true" data-show-comments="true" data-revision="exportable" data-locked="false" data-user-defined="false">Content 0</section>' +
        '<section data-id="id_1" data-title="Title 1" data-default="false" data-show-navigation="true" data-show-comments="false" data-revision="exportable" data-locked="false" data-user-defined="false">Content 1</section>' +
        '<section data-id="id_2" data-title="Title 2" data-default="false" data-show-navigation="false" data-show-comments="false" data-revision="exportable" data-locked="true" data-user-defined="true">Content 2</section>' +
        '<section data-id="id_3" data-title="Title 3" data-default="false" data-show-navigation="false" data-show-comments="true" data-revision="cloneable" data-locked="false" data-user-defined="true">Content 3</section></div>');
    });

    it('should publish events for Idoc content model update', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit', idocContext);

      idocPage.getIdocContent();

      expect(idocPage.eventbus.publish.args[0][0] instanceof BeforeIdocContentModelUpdateEvent).to.be.true;
      expect(idocPage.eventbus.publish.args[1][0] instanceof IdocContentModelUpdateEvent).to.be.true;
      expect(idocPage.eventbus.publish.args[2][0] instanceof AfterIdocContentModelUpdateEvent).to.be.true;
      expect(idocPage.eventbus.publish.callCount).to.equal(3);
    });
  });

  describe('refresh', () => {
    it('should not notify if action definition absent', () => {
      let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      let idocContext = stub(IdocContext);
      idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));
      idocContext.reloadObjectDetails.returns(PromiseStub.resolve([]));

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit', idocContext);
      idocPage.currentObject = instanceObject;

      let instance = new InstanceObject('instance-id', IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      idocPage.refresh(undefined, instance);
      expect(idocPage.notificationService.success.calledOnce).to.be.false;

      idocPage.refresh({label: 'success'}, instance);
      expect(idocPage.notificationService.success.calledOnce).to.be.true;
    });
  });

  describe('updateContentForVersion', () => {
    it('should replace automatic selection searches with manually selected objects', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoiYXV0b21hdGljYWxseSIsImNyaXRlcmlhIjp7ImlkIjoidGVzdENyaXRlcmlhSWQifX0="></div></section></div>';
      let queriesMap = {
        testCriteriaId: ['emf:123456-v1', 'emf-999888-v3']
      };
      let updatedContent = idocPage.updateContentForVersion(content, queriesMap, {});
      let widgetConfig = decodeWidgetConfig(updatedContent);

      expect(widgetConfig.selectObjectMode).to.equal(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.eql(queriesMap.testCriteriaId);
    });

    it('should replace automatic selection searches with manually selected object for widgets with single selection', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoiYXV0b21hdGljYWxseSIsImNyaXRlcmlhIjp7ImlkIjoidGVzdENyaXRlcmlhSWQifSwic2VsZWN0aW9uIjoic2luZ2xlIn0="></div></section></div>';
      let queriesMap = {
        testCriteriaId: ['emf-999888-v3']
      };
      let updatedContent = idocPage.updateContentForVersion(content, queriesMap, {});
      let widgetConfig = decodeWidgetConfig(updatedContent);

      expect(widgetConfig.selectObjectMode).to.equal(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.be.undefined;
      expect(widgetConfig.selectedObject).to.equals('emf-999888-v3');
    });

    it('should replace manually selected URIs with the correct versioned URIs', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoibWFudWFsbHkiLCJzZWxlY3RlZE9iamVjdHMiOlsiZW1mOjEyMzQ1NiIsImVtZjo5OTk4ODgiXX0="></div></section></div>';
      let urisMap = {
        'emf:123456': 'emf:123456-v1.2',
        'emf:999888': 'emf:999888-v1.7'
      };
      let updatedContent = idocPage.updateContentForVersion(content, {}, urisMap);
      let widgetConfig = decodeWidgetConfig(updatedContent);

      expect(widgetConfig.selectObjectMode).to.equal(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.eql(['emf:123456-v1.2', 'emf:999888-v1.7']);
    });

    it('should replace manually selected URI with the correct versioned URI in case of single select (ODW)', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      let content = '<div><section><div class="widget" config="eyJzZWxlY3RPYmplY3RNb2RlIjoibWFudWFsbHkiLCJzZWxlY3RlZE9iamVjdCI6ImVtZjoxMjM0NTYifQ=="></div></section></div>';
      let urisMap = {
        'emf:123456': 'emf:123456-v1.2',
        'emf:999888': 'emf:999888-v1.7'
      };
      let updatedContent = idocPage.updateContentForVersion(content, {}, urisMap);
      let widgetConfig = decodeWidgetConfig(updatedContent);

      expect(widgetConfig.selectObjectMode).to.equal(SELECT_OBJECT_MANUALLY);
      expect(widgetConfig.selectedObjects).to.be.undefined;
      expect(widgetConfig.selectedObject).to.equal('emf:123456-v1.2');
    });

    function decodeWidgetConfig(updatedContent) {
      let widgetConfigEncoded = $($(updatedContent).find('.widget')[0]).attr('config');
      return JSON.parse(base64.decode(widgetConfigEncoded));
    }
  });

  describe('initViewFromTemplate', () => {
    let idocPage;
    beforeEach(() => {
      idocPage = IdocPageTestHelper.instantiateIdocPage();
      let models = {
        validationModel: {
          'emf:hasTemplate': {
            value: undefined
          }
        }
      };

      idocPage.currentObject = new InstanceObject('some-id', models);
    });

    it('initViewFromTemplate should set proper templateInstanceId', () => {
      let event = createEvent('1', 'templateId', 'T1');
      idocPage.initViewFromTemplate(event);

      expect(idocPage.currentObject.getModels().validationModel['emf:hasTemplate'].value).to.eql({
        results: ['templateId'],
        add: ['templateId'],
        remove: [],
        total: 1,
        headers: {}
      });
    });

    it('initViewFromTemplate should set Blank template id when Blank template is selected', () => {
      let eventBlank = createEvent('99', undefined, 'blank');
      idocPage.initViewFromTemplate(eventBlank);

      expect(idocPage.currentObject.getModels().validationModel['emf:hasTemplate'].value).to.eql({
        results: ['99'],
        add: ['99'],
        remove: [],
        total: 1,
        headers: {}
      });
    });

    function createEvent(id, templateInstanceId, content) {
      return {
        template: {
          id, templateInstanceId, content
        }
      };
    }
  });

});
