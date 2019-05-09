import {IdocPageTestHelper} from './idoc-page-test-helper';
import {PromiseStub} from 'test/promise-stub';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {stub} from 'test/test-utils';

const IDOC_ID = 'emf:123456';

describe('Idoc render tabs', () => {

  it('loadData should load system tabs if current object is persisted', () => {
    let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
    sinon.stub(instanceObject, 'isPersisted').returns(true);
    sinon.stub(instanceObject, 'isVersion').returns(false);

    let idocContext = stub(IdocContext);
    idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));

    let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, null, idocContext);
    sinon.stub(idocPage, 'loadSystemTabs').returns(PromiseStub.resolve(instanceObject));

    idocPage.loadData();

    expect(idocPage.loadSystemTabs.callCount).to.equal(1);
  });

  describe('appendContent', () => {
    it('should generate correct tabs array and set active tab correctly', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.currentObject = instanceObject;
      idocPage.tabsConfig.activeTabId = undefined;

      let content = '<div>' +
        '<section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="true" data-show-navigation="true" data-show-comments="false">Second content</section>' +
        '</div>';
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
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.currentObject = instanceObject;

      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="false" data-show-navigation="true" data-show-comments="false">Second content</section></div>';
      idocPage.appendContent(content);

      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_0');
    });

    it('should add and render all tabs if mode is print and there are no explicitly requested tabs', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'print');
      idocPage.currentObject = instanceObject;

      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section>' +
        '<section data-id="id_1" data-title="Second title" data-default="false" data-show-navigation="true" data-show-comments="false">Second content</section></div>';
      idocPage.appendContent(content);

      expect(idocPage.tabsConfig.tabs.length).to.equals(2);
      expect(idocPage.tabsConfig.tabs[0].shouldRender).to.be.true;
      expect(idocPage.tabsConfig.tabs[1].shouldRender).to.be.true;
    });

    it('should add and render only requested tabs if mode is print', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'print');
      idocPage.currentObject = instanceObject;
      idocPage.requestedTabs = ['id_0', 'id_2'];
      sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');

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

    it('should trim content empty paragraphs if mode is print', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'print');
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
      idocPage.currentObject = instanceObject;

      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);

      expect(trimTrailingEmptyParagraphsSpy.callCount).to.equals(1);
    });

    it('should not trim content empty paragraphs if mode is not print', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit');
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');
      idocPage.currentObject = instanceObject;

      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);

      expect(trimTrailingEmptyParagraphsSpy.callCount).to.equals(0);
    });

    it('should disable save button while appending content', () => {
      let instanceObject = new InstanceObject(null, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

      let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID, 'edit');
      idocPage.currentObject = instanceObject;
      idocPage.disableSaveButton = sinon.spy();
      idocPage.idocIsReady = true;

      let content = '<div><section data-id="id_0" data-title="First title" data-default="false" data-show-navigation="true" data-show-comments="true">First content</section></div>';
      idocPage.appendContent(content);

      expect(idocPage.disableSaveButton.calledTwice).to.be.true;
      expect(idocPage.disableSaveButton.getCall(0).args[0]).to.be.true;
      expect(idocPage.disableSaveButton.getCall(1).args[0]).to.be.false;
    });
  });

  describe('setDefaultActiveTab', () => {
    it('should set active tab to first tab if currently active tab does not exist', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.tabsConfig.activeTabId = 'id_1';
      idocPage.setDefaultActiveTab(false);

      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_0');
    });

    it('should does not set active tab to first tab if currently active tab exists', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.tabsConfig.activeTabId = 'id_1';

      idocPage.setDefaultActiveTab(true);

      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_1');
    });

    it('should not set active tab to system tab if not especially requested', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.tabsConfig.activeTabId = 'permission-tab';

      idocPage.setDefaultActiveTab(false);

      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'id_0');
    });

    it('should set active tab to system tab if especially requested', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.tabsConfig.activeTabId = 'permission-tab';

      idocPage.setDefaultActiveTab(true);

      expect(idocPage.tabsConfig).to.have.property('activeTabId', 'permission-tab');
    });
  });

  describe('setTabsCounter()', () => {
    it('should obtain the DOM attribute value ', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      let templateDom = {
        data: () => {
          return 2;
        }
      };
      idocPage.setTabsCounter(templateDom);

      expect(idocPage.tabsConfig.tabsCounter).to.equal(2);
    });

    it('should set the sections length as value if no DOM value is present ', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      let templateDom = {
        data: () => {
          return undefined;
        }
      };
      // two sections
      let sections = [{}, {}];
      idocPage.setTabsCounter(templateDom, sections);

      // the counter value shows the next number
      expect(idocPage.tabsConfig.tabsCounter).to.equal(3);
    });
  });

  describe('showTab', () => {
    it('should return true if mode is not print and the tab is active', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage('emf:1234', 'edit');
      idocPage.tabsConfig.activeTabId = 'tab1';

      expect(idocPage.showTab({id: 'tab1'})).to.be.true;
    });

    it('should return false if mode is print', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage('emf:1234', 'print');
      idocPage.tabsConfig.activeTabId = 'tab1';

      expect(idocPage.showTab({id: 'tab1'})).to.be.false;
    });

    it('should return false if not print mode and the tab is not active', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage('emf:1234', 'edit');
      idocPage.tabsConfig.activeTabId = 'tab1';

      expect(idocPage.showTab({id: 'tab2'})).to.be.false;
    });
  });

  describe('buildTabModel', () => {
    it('should trim empty paragraphs in print mode', () => {
      let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      sinon.stub(instanceObject, 'isVersion').returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage('emf:1234', 'print');
      idocPage.currentObject = instanceObject;
      let trimTrailingEmptyParagraphsSpy = sinon.spy(idocPage, 'trimTrailingEmptyParagraphs');

      idocPage.buildTabModel('<section></section>');

      expect(trimTrailingEmptyParagraphsSpy.callCount).to.equals(1);
    });

    it('should set showComments to false if current object is version', () => {
      let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());
      sinon.stub(instanceObject, 'isVersion').returns(true);

      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.currentObject = instanceObject;

      let tabModel = idocPage.buildTabModel('<section data-show-comments="true"></section>');

      expect(tabModel.showComments).to.be.false;
    });
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
      idocPage = IdocPageTestHelper.instantiateIdocPage();
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
        id: 'mailbox-tab',
        name: 'system.tab.mailbox',
        component: 'seip-mailbox',
        module: 'idoc/system-tabs/mailbox/mailbox',
        filter: () => {
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
      result['mailbox-tab'].filter = () => {
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

});