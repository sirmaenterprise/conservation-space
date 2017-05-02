import {TabsConfig} from 'idoc/idoc-tabs/idoc-tabs-config';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('TabsConfig', () => {
  it('should set active tab and fire event for that', () => {
    let tabsConfig = new TabsConfig(IdocMocks.mockEventBus());
    let publishSpy = sinon.spy(tabsConfig.eventbus, 'publish');
    tabsConfig.tabs = [{
      id: 'tab1'
    }, {
      id: 'tab2'
    }];

    tabsConfig.activeTabId = 'tab2';

    expect(tabsConfig.activeTab.id).to.equal('tab2');
    // expect IdocTabOpenedEvent to be fired with the active tab as data
    expect(publishSpy.getCall(0).args[0].getData()).to.equal(tabsConfig.tabs[1]);
  });

  it('should set active tab\'s shouldRender to true', () => {
    let tabsConfig = new TabsConfig(IdocMocks.mockEventBus());
    tabsConfig.tabs = [{
      id: 'tab1'
    }, {
      id: 'tab2'
    }];

    tabsConfig.activeTabId = 'tab2';

    expect(tabsConfig.activeTab.id).to.equal('tab2');
    expect(tabsConfig.activeTab.shouldRender).to.be.true;
  });

  it('getActiveTab() should return active tab', () => {
    let tabsConfig = new TabsConfig(IdocMocks.mockEventBus());
    tabsConfig.tabs = [{
      id: 'tab1'
    }, {
      id: 'tab2'
    }];

    tabsConfig.activeTabId = 'tab2';
    expect(tabsConfig.getActiveTab()).to.eql(tabsConfig.tabs[1]);
  });
});
