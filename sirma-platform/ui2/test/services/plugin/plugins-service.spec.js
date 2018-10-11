import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('PluginsService', function () {

  var logger = {
    warn: function () {}
  };
  sinon.stub(logger, 'warn');
  var injector = {
    get: function (name) {
      return {
        'name': name
      }
    }
  };
  var service = new PluginsService(logger, injector, PromiseAdapterMock.mockAdapter());
  var stubExecuteImport = sinon.stub(service, 'executeImport');

  PluginRegistry.add('ep1', {
    'order': 10,
    'name': 'plugin1',
    'module': 'plugin1Module'
  });
  PluginRegistry.add('ep1', {
    'order': 20,
    'name': 'plugin2',
    'module': 'plugin2Module'
  });
  PluginRegistry.add('ep2', {
    'order': 10,
    'name': 'plugin1',
    'module': 'plugin1Module'
  });
  PluginRegistry.add('ep2', {
    'order': 20,
    'name': 'plugin2',
    'module': 'plugin2Module'
  });

  it('should throw error if a plugin does not declare a module name', function () {
    var pluginDefinitions = [{
      'name': 'plugin1'
    }];
    expect(function () {
      service.collectPluginDefinitions(pluginDefinitions);
    }).to.throw(Error);
  });

  it('should import modules', function (done) {
    Promise.resolve(service.importModules(['module1'])).then(function (promises) {
      expect(promises.length).to.equal(1);
      done();
    });
  });

  describe('loadPluginServiceModules()', function() {
    it('should load all services plugins registered for an extension point', (done) => {
      service.loadPluginServiceModules('ep1', 'name').then((loadedModules) => {
        expect(loadedModules).to.eql({
          plugin1: {
            name: 'Plugin1'
          },
          plugin2: {
            name: 'Plugin2'
          }
        });
        done();
      }).catch(done);
    });

    it('should load all modules and return dem ordered in an array', (done) => {
      service.loadPluginServiceModules('ep1', 'name', true).then((loadedModules) => {
        expect(loadedModules.length).to.equal(2);
        expect(loadedModules[0].name).to.equal('Plugin1');
        expect(loadedModules[1].name).to.equal('Plugin2');
        done();
      }).catch(done);
    });
  });

  describe('loadComponentModules', function() {
    it('should load 2 plugin modules and to return their definitions', function(done) {
      Promise.resolve(service.loadComponentModules('ep1', 'name')).then(function(loadedDefinitions) {
        expect(loadedDefinitions !== undefined).to.be.true;
        expect(Object.keys(loadedDefinitions).length).to.equal(2);
        expect(loadedDefinitions.plugin1).to.deep.equal({
          'order': 10,
          'name': 'plugin1',
          'module': 'plugin1Module',
          'priority': 0
        });
        expect(loadedDefinitions.plugin2).to.deep.equal({
          'order': 20,
          'name': 'plugin2',
          'module': 'plugin2Module',
          'priority': 0
        });
        done();
      });
    });

    it('should filter loaded modules by given function', function(done) {
      let filterFunc = (pluginDefinition) => {
        return pluginDefinition.name === 'plugin2';
      };
      Promise.resolve(service.loadComponentModules('ep1', 'name', filterFunc)).then(function(loadedDefinitions) {
        expect(loadedDefinitions).to.exist;
        expect(Object.keys(loadedDefinitions).length).to.equal(1);
        expect(loadedDefinitions.plugin2).to.deep.equal({
          'order': 20,
          'name': 'plugin2',
          'module': 'plugin2Module',
          'priority': 0
        });
        done();
      });
    });
  });

  describe('getPluginDefinitions', function() {
    it('should return registered plugins mapped by default key when called without mapBy argument', function () {
      var definitions = service.getPluginDefinitions('ep1');
      expect(definitions != undefined).to.be.true;
      expect(definitions.plugin1 !== undefined).to.be.true;
      expect(definitions.plugin2 !== undefined).to.be.true;
      expect(Object.keys(definitions).length).to.equal(2);
    });

    it('should return registered plugins mapped by provided key when called with mapBy argument', function () {
      var definitions = service.getPluginDefinitions('ep2', 'module');
      expect(definitions != undefined).to.be.true;
      expect(definitions.plugin1Module !== undefined).to.be.true;
      expect(definitions.plugin2Module !== undefined).to.be.true;
      expect(Object.keys(definitions).length).to.equal(2);
    });
  });

  describe('loadPluginModule', function() {
    it('should return already loaded plugin module', function (done) {
      let pluginDefinition = {
        'name': 'plugin1',
        'module': 'path/to/module/plugin1'
      };
      let loadedModules = {
        'plugin1': {
          'name': 'plugin1'
        }
      };
      Promise.resolve(service.loadPluginModule(pluginDefinition, loadedModules, 'name')).then(function (loadedModule) {
        expect(loadedModule !== undefined).to.be.true;
        expect(loadedModule).to.deep.equal({
          'name': 'plugin1'
        });
        done();
      })
    });

    it('should load and return a plugin module', function (done) {
      class Plugin1module {}
      let promise = Promise.resolve(Plugin1module);
      let module = 'path/to/module/plugin1';
      stubExecuteImport.withArgs(module).returns(promise);
      let pluginDefinition = {
        'id': 'plugin1module',
        'module': module
      };
      let loadedModules = {};
      Promise.resolve(service.loadPluginModule(pluginDefinition, loadedModules, 'id')).then(function (loadedModule) {
        expect(loadedModule !== undefined).to.be.true;
        expect(loadedModule).to.deep.equal({
          'name': 'Plugin1module'
        });
        done();
      });
    });

    it('should load and return a plugin module by the module path defined in its definition', function (done) {
      class Plugin1module {}
      let promise = Promise.resolve(Plugin1module);
      let module = 'path/to/module/plugin1module';
      stubExecuteImport.withArgs(module).returns(promise);
      let pluginDefinition = {
        'id': 'plugin1module'
      };
      let loadedModules = {};
      let registeredPluginDefintions = {
        plugin1module: {
          module: module
        }
      };
      Promise.resolve(service.loadPluginModule(pluginDefinition, loadedModules, 'id', registeredPluginDefintions)).then(function (loadedModule) {
        expect(loadedModule !== undefined).to.be.true;
        expect(loadedModule).to.deep.equal({
          'name': 'Plugin1module'
        });
        done();
      });
    });

    it('should load module defined with its id', (done) => {
      let pluginDefinition = {
        'id': 'plugin1',
        'module': 'path/to/module/plugin1'
      };
      let loadedModules = {
        'plugin1': {
          'name': 'plugin1'
        }
      };
      Promise.resolve(service.loadPluginModule(pluginDefinition, loadedModules, 'name')).then(function (loadedModule) {
        expect(loadedModule !== undefined).to.be.true;
        expect(loadedModule).to.deep.equal({
          'name': 'plugin1'
        });
        done();
      })
    });
  });

});