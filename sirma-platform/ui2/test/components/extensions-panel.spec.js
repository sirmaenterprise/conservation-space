import {ExtensionsPanel} from 'components/extensions-panel/extensions-panel';
import _ from 'lodash';

describe('ExtensionsPanel', () => {

  var extensionsPanel;
  var $element = mockElement();
  var $compile = mockCompile();
  var pluginsRegistry;

  beforeEach(() => {
    pluginsRegistry = mockPluginsRegistry();
    ExtensionsPanel.prototype.config = {
      extensionPoint: 'picker'
    };
    extensionsPanel = new ExtensionsPanel(mockScope(), $element, $compile, pluginsRegistry);
  });

  it('should get extensions from PluginsService', () => {
    expect(pluginsRegistry.loadComponentModules.calledOnce).to.be.true;
    expect(pluginsRegistry.loadComponentModules.getCall(0).args).to.deep.equal(['picker', 'name']);
  });

  it('should filter out extensions if configured', () => {
    var extensions = mockExtension();
    extensionsPanel.filterExcludedExtensions(extensions, ['my-extension']);
    expect(extensions).to.deep.equal({
      'my-other-extension': {}
    });
  });

  it('should not filter out extensions if not configured', () => {
    var extensions = mockExtension();
    var extensionsCopy = _.cloneDeep(extensions);
    extensionsPanel.filterExcludedExtensions(extensions, []);
    expect(extensions).to.deep.equal(extensionsCopy);
  });

  it('should leave only included extensions if configured', () => {
    var extensions = mockExtension();
    extensionsPanel.filterIncludedExtensions(extensions, ['my-extension']);
    expect(extensions).to.deep.equal({
      'my-extension': {
        name: 'my-extension'
      }
    });
  });

  it('should not filter extensions if inclusions is not configured', () => {
    var extensions = mockExtension();
    var extensionsCopy = _.cloneDeep(extensions);
    extensionsPanel.filterIncludedExtensions(extensions, []);
    expect(extensions).to.deep.equal(extensionsCopy);
  });

  it('should not compile any extensions on initialization', () => {
    expect(extensionsPanel.compiledExtensions).to.deep.equal([]);
  });

  it('should assign default extensions-config on initialization', () => {
    expect(extensionsPanel.config.extensions).to.deep.equal({});
  });

  it('should create tab configuration based on given extensions', () => {
    extensionsPanel.config.defaultTab = 'my-extension2';
    extensionsPanel.config.sortComparator = 'sort-callback';

    var extensions = {
      'my-extension': {
        name: 'my-extension',
        label: 'label.my-extension'
      },
      'my-extension2': {
        name: 'my-extension2',
        label: 'label.my-extension2'
      }
    };
    var expectedTabsConfig = {
      activeTab: 'my-extension2',
      sortComparator: 'sort-callback',
      classes: 'nav-stacked nav-left',
      tabs: [{
        id: 'my-extension',
        label: 'label.my-extension',
        classes: 'my-extension'
      }, {
        id: 'my-extension2',
        label: 'label.my-extension2',
        classes: 'my-extension2'
      }]
    };
    expect(extensionsPanel.getExtensionTabsConfig(extensions)).to.deep.equal(expectedTabsConfig);
  });

  it('should assign additional tab configurations if present', () => {
    var postFixMessage = 'a postfix function for my extension\'s tab';
    extensionsPanel.config.tabs = {
      'my-extension': {
        postfix: () => {
          return postFixMessage;
        }
      }
    };
    var extensions = mockExtension();
    var tabsConfig = extensionsPanel.getExtensionTabsConfig(extensions);
    expect(tabsConfig.tabs[0].postfix).to.exist;
    expect(tabsConfig.tabs[0].postfix()).to.equal(postFixMessage);
  });

  it('should not compile missing extensions', () => {
    extensionsPanel.extensions = {};
    extensionsPanel.compileExtension('my-extension');
    expect($element.find.callCount).to.equal(0);
    expect($compile.callCount).to.equal(0);
  });

  it('should compile extensions', () => {
    extensionsPanel.extensions = mockExtension();
    extensionsPanel.extensions['my-extension'].component = 'my-component';
    extensionsPanel.compiledExtensions = [];
    extensionsPanel.compileExtension('my-extension');

    var expectedTag = `<my-component config="extensionsPanel.config.extensions['my-extension']" context="extensionsPanel.context"></my-component>`;
    expect($compile.callCount).to.equal(1);
    expect($compile.getCall(0).args[0]).to.equal(expectedTag);
  });

  it('should append compiled extension to the DOM', () => {
    var appendSpy = sinon.spy();
    extensionsPanel.$element.find = () => {
      return {
        append: appendSpy
      }
    };

    extensionsPanel.extensions = mockExtension();
    extensionsPanel.compiledExtensions = [];
    extensionsPanel.compileExtension('my-extension');

    expect(appendSpy.callCount).to.equal(1);
    expect(appendSpy.getCall(0).args[0]).to.equal('compiled-extension');
  });

  it('should populate compiled extension', () => {
    extensionsPanel.extensions = mockExtension();
    extensionsPanel.compiledExtensions = [];
    extensionsPanel.compileExtension('my-extension');

    expect(extensionsPanel.compiledExtensions).to.deep.equal(['my-extension']);
  });

  it('should assign default extension configuration', () => {
    extensionsPanel.extensions = mockExtension();
    extensionsPanel.config.extensions = {};
    extensionsPanel.compiledExtensions = [];
    extensionsPanel.compileExtension('my-extension');

    var expected = {'my-extension': {}};
    expect(extensionsPanel.config.extensions).to.deep.equal(expected);
  });

  it('should compile visible extensions', () => {
    extensionsPanel.compileExtension = sinon.spy();
    extensionsPanel.compiledExtensions = [];
    extensionsPanel.tabsConfig = getTabsConfig();
    extensionsPanel.showExtension('my-extension');
    expect(extensionsPanel.compileExtension.callCount).to.equal(1);
    expect(extensionsPanel.compileExtension.getCall(0).args[0]).to.equal('my-extension');
  });

  it('should not compile already compiled extensions', () => {
    extensionsPanel.compileExtension = sinon.spy();
    extensionsPanel.compiledExtensions = ['my-extension'];
    extensionsPanel.tabsConfig = getTabsConfig();
    extensionsPanel.showExtension('my-extension');
    expect(extensionsPanel.compileExtension.callCount).to.equal(0);
  });

  it('should not compile non visible extensions', () => {
    extensionsPanel.compileExtension = sinon.spy();
    extensionsPanel.compiledExtensions = [];
    extensionsPanel.tabsConfig = getTabsConfig();
    extensionsPanel.showExtension('other-extension');
    expect(extensionsPanel.compileExtension.callCount).to.equal(0);
  });

  it('should show the currently visible extension', () => {
    extensionsPanel.tabsConfig = getTabsConfig();
    extensionsPanel.compiledExtensions = ['my-extension'];
    expect(extensionsPanel.showExtension('my-extension')).to.be.true;
  });

  it('should hide all extensions which are not the current one', () => {
    extensionsPanel.tabsConfig = getTabsConfig();
    expect(extensionsPanel.showExtension('your-extension')).to.be.false;
  });

  it('should show navigation if there are more than one extension', () => {
    extensionsPanel.extensions = mockExtension();
    expect(extensionsPanel.showNavigation()).to.be.true;
  });

  it('should hide navigation if there is just one extension', () => {
    extensionsPanel.extensions = {
      'my-extension': {
        name: 'my-extension'
      }
    };
    expect(extensionsPanel.showNavigation()).to.be.false;
  });

  it('should notify for changing the extension tabs', () => {
    extensionsPanel.onTabChanged = sinon.spy();
    let newTab = {};
    let oldTab = {};
    let tabConfig = {};
    extensionsPanel.handleOnTabChanged(newTab, oldTab, tabConfig);
    expect(extensionsPanel.onTabChanged.calledOnce).to.be.true;
    expect(extensionsPanel.onTabChanged.getCall(0).args[0].newTab).to.equal(newTab);
    expect(extensionsPanel.onTabChanged.getCall(0).args[0].oldTab).to.equal(oldTab);
    expect(extensionsPanel.onTabChanged.getCall(0).args[0].config).to.equal(tabConfig);
  });

});

function getTabsConfig() {
  return {
    activeTab: 'my-extension'
  };
}

function mockScope() {
  return {};
}

function mockElement() {
  var element = {
    append: sinon.spy()
  };
  return {
    find: sinon.spy(() => {
      return {
        append: () => {
        }
      };
    })
  };
}

function mockCompile() {
  var compile = () => {
    var returnFunc = () => {
      return ['compiled-extension']
    };
    return sinon.spy(returnFunc);
  };
  return sinon.spy(compile);
}

function mockPluginsRegistry() {
  return {
    loadComponentModules: sinon.spy(() => {
      return Promise.resolve(mockExtension());
    })
  };
}

function mockExtension() {
  return {
    'my-extension': {
      name: 'my-extension'
    },
    'my-other-extension': {}
  };
}