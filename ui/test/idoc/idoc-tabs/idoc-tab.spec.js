import {IdocTab} from 'idoc/idoc-tabs/idoc-tab';
import {MODE_EDIT} from 'idoc/idoc-constants';

describe('IdocTab', () => {
  it('should set active tab id', () => {
    let idocTab = new IdocTab();
    idocTab.config = {
      activeTabId: 'tab_1'
    };
    idocTab.tab = {id: 'tab_2'};
    idocTab.setActiveTab();
    expect(idocTab.config.activeTabId).to.equal('tab_2');
  });

  it('should not update active tab id when the same tab is set to active', () => {
    let idocTab = new IdocTab();
    idocTab.config = {
      activeTabId: 'tab_1'
    };
    idocTab.tab = {id: 'tab_1'};
    idocTab.setActiveTab();
    expect(idocTab.config.activeTabId).to.equal('tab_1');
  });

  it('should return correct value when isActiveTab() is called', () => {
    let idocTab = new IdocTab();
    idocTab.config = {
      activeTabId: 'test_id'
    };
    idocTab.tab = {id: 'test_id'};
    expect(idocTab.isActiveTab()).to.be.true;
    idocTab.tab = {id: 'inactive_id'};
    expect(idocTab.isActiveTab()).to.be.false;
  });

  it('should return true when editing title is enabled (tab is active and not locked)', () => {
    let idocTab = new IdocTab();
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

    let promiseAdapterSpy = {
      resolve: sinon.spy()
    };

    let idocTab = new IdocTab(promiseAdapterSpy);
    idocTab.tab = {id: 'tab_1', title: 'new_tab', showNavigation: true, showComments: false, locked: true};
    idocTab.createTabActions();
    expect(idocTab.tabActions).to.have.property('wrapperClass', 'idoc-tabs-menu');
    expect(idocTab.tabActions).to.have.property('buttonAsTrigger', false);
    expect(idocTab.tabActions.loadItems).is.a('function');

    idocTab.tabActions.loadItems();
    expect(promiseAdapterSpy.resolve.callCount).to.equals(1);
    expect(promiseAdapterSpy.resolve.getCall(0).args[0]).to.eql([{"id":"configureIdocTabs"}]);

    promiseAdapterSpy.resolve.reset();
    idocTab.tab = {id: 'tab_1', title: 'new_tab', showNavigation: true, showComments: false, locked: false};
    idocTab.createTabActions();
    idocTab.tabActions.loadItems();
    expect(promiseAdapterSpy.resolve.callCount).to.equals(1);
    expect(promiseAdapterSpy.resolve.getCall(0).args[0]).to.eql([{id: 'deleteIdocTabs'}, {"id":"configureIdocTabs"}]);
  });
});
