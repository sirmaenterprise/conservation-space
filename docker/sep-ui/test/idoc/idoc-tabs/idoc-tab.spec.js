import {IdocTab} from 'idoc/idoc-tabs/idoc-tab';
import {MODE_EDIT} from 'idoc/idoc-constants';
import {stub} from 'test/test-utils';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseStub} from 'test/promise-stub';

describe('IdocTab', () => {

  let idocTab;
  before(() => {
    let promiseAdapterStub = stub(PromiseAdapter);
    let pluginsServiceStub = stub(PluginsService);
    pluginsServiceStub.loadComponentModules.returns(PromiseStub.resolve({
    }));
    idocTab = new IdocTab(promiseAdapterStub, pluginsServiceStub);
  });

  it('should set active tab id', () => {
    idocTab.config = {
      activeTabId: 'tab_1'
    };
    idocTab.tab = {id: 'tab_2'};
    idocTab.setActiveTab();
    expect(idocTab.config.activeTabId).to.equal('tab_2');
  });

  it('should not update active tab id when the same tab is set to active', () => {
    idocTab.config = {
      activeTabId: 'tab_1'
    };
    idocTab.tab = {id: 'tab_1'};
    idocTab.setActiveTab();
    expect(idocTab.config.activeTabId).to.equal('tab_1');
  });

  it('should return correct value when isActiveTab() is called', () => {
    idocTab.config = {
      activeTabId: 'test_id'
    };
    idocTab.tab = {id: 'test_id'};
    expect(idocTab.isActiveTab()).to.be.true;
    idocTab.tab = {id: 'inactive_id'};
    expect(idocTab.isActiveTab()).to.be.false;
  });

  it('should return true when editing title is enabled (tab is active and not locked)', () => {
    idocTab.config = {
      activeTabId: 'tab_1'
    };
    idocTab.mode = MODE_EDIT;

    idocTab.tab = {
      id: 'tab_1',
      locked: false
    };
    idocTab.enableEdit();
    expect(idocTab.editable).to.be.true;

    idocTab.tab = {
      id: 'tab_2',
      locked: false
    };
    idocTab.enableEdit();

    idocTab.tab = {
      id: 'tab_1',
      locked: true
    };
    idocTab.enableEdit();
    expect(idocTab.editable).to.be.false;
  });

  it('should build tab actions configuration for dropdown menu for given class', () => {
    PluginRegistry.add('idoc-tabs-menu-items', {
      id: 'deleteIdocTabs'
    });

    PluginRegistry.add('idoc-tabs-menu-items', {
      id: 'configureIdocTabs'
    });

    idocTab.promiseAdapter = {
      resolve: sinon.spy()
    };

    idocTab.tab = {id: 'tab_1', title: 'new_tab', showNavigation: true, showComments: false, locked: true};
    idocTab.createTabActions();
    expect(idocTab.tabActions).to.have.property('wrapperClass', 'idoc-tabs-menu');
    expect(idocTab.tabActions).to.have.property('buttonAsTrigger', false);
    expect(idocTab.tabActions.loadItems).is.a('function');

    idocTab.tabActions.loadItems();
    expect(idocTab.promiseAdapter.resolve.callCount).to.equals(1);
    expect(idocTab.promiseAdapter.resolve.getCall(0).args[0]).to.eql([{"id": "configureIdocTabs"}]);

    idocTab.promiseAdapter.resolve.reset();
    idocTab.tab = {id: 'tab_1', title: 'new_tab', showNavigation: true, showComments: false, locked: false};
    idocTab.createTabActions();
    idocTab.tabActions.loadItems();
    expect(idocTab.promiseAdapter.resolve.callCount).to.equals(1);
    expect(idocTab.promiseAdapter.resolve.getCall(0).args[0]).to.eql([{id: 'deleteIdocTabs'}, {"id": "configureIdocTabs"}]);
  });
});
