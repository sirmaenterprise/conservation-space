import {ConfigureIdocTabs} from 'idoc/idoc-tabs/configure-idoc-tabs';

describe('Tests for ConfigureIdocTabs component', () => {
  it('Should have a successful execution', () => {
      let context = {
        id: 'test',
        mode: 'edit',
        activeTabId: 'test_id',
        tabsCounter: 2,
        tabs: [
          {
            id: 'test_id',
            title: 'test_id',
            showNavigation: true,
            showComments: false,
            revision: 'exportable'
          }, {
            id: 2,
            title: 'new_tab',
            showNavigation: true,
            showComments: false,
            revision: 'cloneable'
          }]
      };
      let service = {
        create: () => {
        }
      };

      let configureIdocTabs = new ConfigureIdocTabs(service);
      let spy = sinon.spy(service, 'create');
      configureIdocTabs.execute({}, context);
      expect(spy.calledOnce).to.be.true;
      expect(spy.calledWith({id: 'test_id', title: 'test_id', showNavigation: true, showComments: false}));
    }
  );

  it('should create tab when addTab() is called', () => {
    let context = {
      mode: 'edit',
      activeTabId: 'test_id',
      tabsCounter: 2,
      tabs: []
    };
    let configureIdocTabs = new ConfigureIdocTabs({});
    configureIdocTabs.initContext(context);
    let newTab = {
      tabTitle: 'new_tab',
      showNavigation: true,
      showComments: false,
      revision: 'cloneable',
      locked: true
    };
    configureIdocTabs.addTab(newTab);
    let expectedObject = {
      mode: 'edit',
      activeTabId: configureIdocTabs.context.activeTabId,
      tabsCounter: 3,
      tabs: [{
        id: configureIdocTabs.context.activeTabId,
        title: 'new_tab',
        showNavigation: true,
        showComments: false,
        revision: 'cloneable',
        locked: true
      }]
    };

    expect(configureIdocTabs.context).to.deep.equal(expectedObject);
  });

  it('should set tab counter to 5', () => {
    let context = {
      tabsCounter: 5
    };
    let configureIdocTabs = new ConfigureIdocTabs({});
    configureIdocTabs.initContext(context);
    expect(configureIdocTabs.context.tabsCounter).to.equal(5);
  });

  it('should update tab sections and title when updateTab is called', () => {
    let tab = {id: 1, title: 'new_tab', showNavigation: true, showComments: false, revision: 'exportable'};
    let newValue = {tabTitle: 'changed_title', showNavigation: false, showComments: true, revision: 'exportable'};
    let context = {
      tabs: [tab]
    };
    let configureIdocTabs = new ConfigureIdocTabs({});
    configureIdocTabs.initContext(context);
    configureIdocTabs.updateTab(tab, newValue);

    let expectedObject = {
      tabs: [{
        id: 1,
        title: 'changed_title',
        showNavigation: false,
        showComments: true,
        revision: 'exportable',
        locked: undefined
      }]
    };

    expect(configureIdocTabs.context).to.deep.equal(expectedObject);
  });

  it('should set default revision property if missing', () => {
    let configureIdocTabs = new ConfigureIdocTabs({});
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
    expect(configureIdocTabs.getPropertiesConfig(tab)).to.deep.equal(expectedObject);
  });

  it('should set popup config properties', () => {
    let configureIdocTabs = new ConfigureIdocTabs({});
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
    expect(configureIdocTabs.getPropertiesConfig(tab)).to.deep.equal(expectedObject);

    configureIdocTabs = new ConfigureIdocTabs({});
    configureIdocTabs.initContext({tabsCounter: 3});
    expectedObject = {
      config: {
        tabTitle: 'Tab3',
        showComments: true,
        showNavigation: true,
        revision: 'exportable',
        locked: false
      }
    };
    expect(configureIdocTabs.getPropertiesConfig({})).to.deep.equal(expectedObject);
  });
});
