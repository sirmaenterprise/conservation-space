import {Tabs} from 'components/tabs/tabs';

describe('Tabs', () => {

  let tabs;
  beforeEach(() => {
    tabs = new Tabs();
    tabs.config = getTabsConfig();
    tabs.onTabChanged = sinon.spy();
  });

  it('should assign default active tab if not specified', () => {
    tabs.ngOnInit();
    expect(tabs.config.activeTab).to.equal('firstTab');
  });

  it('should not assign default active tab if it is specified', () => {
    tabs.config.activeTab = 'secondTab';
    tabs.ngOnInit();
    expect(tabs.config.activeTab).to.equal('secondTab');
  });

  it('should not assign default active tab if not tabs are provided', () => {
    tabs = new Tabs();
    tabs.ngOnInit();
    expect(tabs.config.activeTab).to.not.exist;
  });

  it('should change the active tab', () => {
    tabs.config.activeTab = 'firstTab';
    tabs.ngOnInit();
    tabs.switchTab(tabs.config.tabs[1]);
    expect(tabs.config.activeTab).to.equal('secondTab');
  });

  it('should tell if given tab is active', () => {
    tabs.config.activeTab = 'firstTab';
    tabs.ngOnInit();
    expect(tabs.isActive(tabs.config.tabs[0])).to.be.true;
  });

  it('should tell if given tab is not active', () => {
    tabs.config.activeTab = 'firstTab';
    tabs.ngOnInit();
    expect(tabs.isActive(tabs.config.tabs[1])).to.be.false;
  });

  it('should notify when a tab is changed', () => {
    tabs.ngOnInit();
    tabs.switchTab(tabs.config.tabs[1]);
    expect(tabs.onTabChanged.calledOnce).to.be.true;
    // New  + old + config
    expect(tabs.onTabChanged.getCall(0).args[0].newTab).to.equal(tabs.config.tabs[1]);
    expect(tabs.onTabChanged.getCall(0).args[0].oldTab).to.equal(tabs.config.tabs[0]);
    expect(tabs.onTabChanged.getCall(0).args[0].config).to.equal(tabs.config);
  });

  function getTabsConfig() {
    return {
      tabs: [{
        id: 'firstTab'
      }, {
        id: 'secondTab'
      }]
    };
  }
});