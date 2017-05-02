import {Tabs} from 'components/tabs/tabs';

describe('Tabs', ()=> {

  var tabs;
  beforeEach(() => {
    tabs = new Tabs();
  });

  it('should assign default active tab if not specified', () => {
    Tabs.prototype.config = getTabsConfig();
    tabs = new Tabs();
    expect(tabs.config.activeTab).to.equal('firstTab');
    Tabs.prototype.config = undefined;
  });

  it('should not assign default active tab if it is specified', () => {
    var config = getTabsConfig();
    config.activeTab = 'secondTab';
    Tabs.prototype.config = config;

    tabs = new Tabs();
    expect(tabs.config.activeTab).to.equal('secondTab');
    Tabs.prototype.config = undefined;
  });

  it('should not assign default active tab if not tabs are provided', () => {
    tabs = new Tabs();
    expect(tabs.config.activeTab).to.not.exist;
  });

  it('should change the active tab', () => {
    tabs.config.activeTab = 'firstTab';
    tabs.switchTab({id: 'secondTab'});
    expect(tabs.config.activeTab).to.equal('secondTab');
  });

  it('should tell if given tab is active', () => {
    tabs.config.activeTab = 'firstTab';
    expect(tabs.isActive({id: 'firstTab'})).to.be.true;
  });

  it('should tell if given tab is not active', () => {
    tabs.config.activeTab = 'firstTab';
    expect(tabs.isActive({id: 'secondTab'})).to.be.false;
  });

  function getTabsConfig() {
    return {
      tabs: [{
        id: 'firstTab'
      }, {
        id: 'secondTab'
      }]
    }
  }
});