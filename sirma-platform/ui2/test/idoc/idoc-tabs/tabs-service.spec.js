import {TabsService} from 'idoc/idoc-tabs/tabs-service';

describe('Tabs Service', () => {
  let tabsService;
  let dialogService = {
    create: sinon.spy()
  };
  beforeEach(() => {
    tabsService = new TabsService(dialogService);
  });

  it('should create tab when addTab() is called', () => {
    let context = {
      mode: 'edit',
      activeTabId: 'test_id',
      tabsCounter: 2,
      tabs: []
    };

    let newTab = {
      tabTitle: 'new_tab',
      showNavigation: true,
      showComments: false,
      revision: 'cloneable',
      locked: true
    };

    tabsService.addTab({}, newTab, context);

    let expectedObject = {
      mode: 'edit',
      activeTabId: context.activeTabId,
      tabsCounter: 3,
      tabs: [{
        id: context.activeTabId,
        title: 'new_tab',
        showNavigation: true,
        showComments: false,
        revision: 'cloneable',
        locked: true,
      }]
    };

    expect(context).to.deep.equal(expectedObject);
  });

  it('should create new tab using a predefined tab configuration', () => {
    let context = {
      mode: 'edit',
      activeTabId: 'test_id',
      tabsCounter: 2,
      tabs: []
    };

    let newTab = {
      tabTitle: 'new_tab',
      showNavigation: true,
      showComments: false,
      revision: 'cloneable',
      locked: true
    };

    tabsService.addTab({
      userDefined: true
    }, newTab, context);

    let expectedObject = {
      mode: 'edit',
      activeTabId: context.activeTabId,
      tabsCounter: 3,
      tabs: [{
        id: context.activeTabId,
        title: 'new_tab',
        showNavigation: true,
        showComments: false,
        revision: 'cloneable',
        locked: true,
        userDefined: true
      }]
    };

    expect(context).to.deep.equal(expectedObject);
  });

  it('should update tab sections and title when updateTab is called', () => {
    let tab = {id: 1, title: 'new_tab', showNavigation: true, showComments: false, revision: 'exportable'};
    let newValue = {tabTitle: 'changed_title', showNavigation: false, showComments: true, revision: 'exportable'};
    let context = {
      tabs: [tab]
    };

    tabsService.updateTab(tab, newValue);

    let expectedObject = {
        id: 1,
        title: 'changed_title',
        showNavigation: false,
        showComments: true,
        revision: 'exportable',
        locked: undefined
      };
    expect(tab).to.deep.equal(expectedObject);
  });

  it('should set default revision property if missing', () => {
    let tab = {id: 1, title: 'tab_title', showNavigation: true, showComments: false};

    let expectedObject = {
      config: {
        tabTitle: 'tab_title',
        showComments: false,
        showNavigation: true,
        revision: 'exportable',
        locked: undefined
      }
    };
    expect(tabsService.getPropertiesConfig(tab)).to.deep.equal(expectedObject);
  });

  it('should set popup config properties', () => {
    let tab = {id: 1, title: 'tab_title', showNavigation: true, showComments: false, revision: 'exportable'};

    let expectedObject = {
      config: {
        tabTitle: 'tab_title',
        showComments: false,
        showNavigation: true,
        revision: 'exportable',
        locked: undefined
      }
    };
    expect(tabsService.getPropertiesConfig(tab)).to.deep.equal(expectedObject);


    expectedObject = {
      config: {
        tabTitle: 'Tab3',
        showComments: true,
        showNavigation: true,
        revision: 'exportable',
        locked: false
      }
    };
    expect(tabsService.getPropertiesConfig({}, {tabsCounter: 3})).to.deep.equal(expectedObject);
  });
});
