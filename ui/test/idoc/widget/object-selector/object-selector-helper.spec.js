import {ObjectSelectorHelper, UNDEFINED_CRITERIA} from 'idoc/widget/object-selector/object-selector-helper';
import {InstanceObject} from 'idoc/idoc-context';
import {CURRENT_OBJECT_TEMP_ID} from 'idoc/idoc-context';
import {
  SELECT_OBJECT_CURRENT,
  SELECT_OBJECT_MANUALLY,
  SELECT_OBJECT_AUTOMATICALLY
} from 'idoc/widget/object-selector/object-selector';
import {PromiseAdapterMock} from '../../../adapters/angular/promise-adapter-mock';

describe('ObjectSelectorHelper', () => {

  var objectSelectorHelper;
  var searchService;
  var promiseAdapter;
  var instanceService;
  beforeEach(() => {
    searchService = {
      search: sinon.spy()
    };
    instanceService = {
      loadBatch: () => {
        return Promise.resolve({data: []});
      }
    };
    promiseAdapter = PromiseAdapterMock.mockAdapter();
    objectSelectorHelper = new ObjectSelectorHelper(promiseAdapter, searchService, instanceService);
  });

  describe('getSelectedObjects()', () => {
    it('should return the id of the current object in an array', (done) => {
      var context = mockContext('emf:123');
      var config = {
        selectObjectMode: SELECT_OBJECT_CURRENT
      };

      objectSelectorHelper.getSelectedObjects(config, context).then((selection) => {
        var expected = ['emf:123'];
        expect(selection.results).to.deep.equal(expected);
        expect(selection.total).to.equal(1);
        done();
      }).catch(done);
    });

    it('should return the temporary id of the current object if not persisted', (done) => {
      var context = mockContext(CURRENT_OBJECT_TEMP_ID);
      var config = {
        selectObjectMode: SELECT_OBJECT_CURRENT
      };

      objectSelectorHelper.getSelectedObjects(config, context).then((selection) => {
        var expected = [CURRENT_OBJECT_TEMP_ID];
        expect(selection.results).to.deep.equal(expected);
        done();
      }).catch(done);
    });

    it('should reject if configured when the current object is not persisted (temporary ID)', (done) => {
      var context = mockContext(CURRENT_OBJECT_TEMP_ID);
      var config = {
        selectObjectMode: SELECT_OBJECT_CURRENT
      };
      var selectorArguments = {
        ignoreNotPersisted: true
      };

      objectSelectorHelper.getSelectedObjects(config, context, undefined, selectorArguments).then(done).catch((error) => {
        expect(error).to.exist;
        done();
      });
    });

    it('should return the IDs of the manually selected objects in multiple selection', (done) => {
      var config = {
        selectObjectMode: SELECT_OBJECT_MANUALLY,
        selectedItems: ['firstId', 'secondId']
      };

      objectSelectorHelper.getSelectedObjects(config).then((selection) => {
        var expected = ['firstId', 'secondId'];
        expect(selection.results).to.deep.equal(expected);
        expect(selection.total).to.equal(2);
        done();
      }).catch(done);
    });

    it('should return the IDs of the manually selected objects in multiple selection (ver2)', (done) => {
      var config = {
        selectObjectMode: SELECT_OBJECT_MANUALLY,
        selectedObjects: ['firstId', 'secondId']
      };

      objectSelectorHelper.getSelectedObjects(config).then((selection) => {
        var expected = ['firstId', 'secondId'];
        expect(selection.results).to.deep.equal(expected);
        expect(selection.total).to.equal(2);
        done();
      }).catch(done);
    });

    it('should return the ID of the manually selected object in single selection mode', (done) => {
      var config = {
        selectObjectMode: SELECT_OBJECT_MANUALLY,
        selectedObject: 'emf:123'
      };

      objectSelectorHelper.getSelectedObjects(config).then((selection) => {
        var expected = ['emf:123'];
        expect(selection.results).to.deep.equal(expected);
        expect(selection.total).to.equal(1);
        done();
      }).catch(done);
    });

    it('should return the IDs of a performed search if the mode is automatically', (done) => {
      let config = {
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
        criteria: {rules: []}
      };
      searchService.search = sinon.spy(() => {
        return {
          promise: Promise.resolve(getSearchResponse())
        };
      });

      objectSelectorHelper.getSelectedObjects(config).then((selection) => {
        expect(searchService.search.called).to.be.true;
        expect(selection.results).to.deep.equal(['1']);
        expect(selection.total).to.equal(123);
        done();
      }).catch(done);
    });

    it('should return current object + search results when includeCurrent is true', (done) => {
      var config = {
        includeCurrent: true,
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      var context = mockContext('emf:123');

      var results = ['emf:456'];
      objectSelectorHelper.searchForObjects = () => Promise.resolve({total: 33, results});

      objectSelectorHelper.getSelectedObjects(config, context).then((selection) => {
        expect(selection.total).to.equal(33);
        expect(selection.results).to.deep.equal(['emf:123', 'emf:456']);
        done();
      }).catch(done);
    });

    it('should return the current object instead of rejecting when there are no search results', (done) => {
      var config = {
        includeCurrent: true,
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      var context = mockContext('emf:123');

      objectSelectorHelper.searchForObjects = () => Promise.reject();

      objectSelectorHelper.getSelectedObjects(config, context).then((selection) => {
        expect(selection.results).to.deep.equal(['emf:123']);
        done();
      }).catch(done);
    });

    it('should reject if there are no search results and current object is not needed', (done) => {
      var config = {
        includeCurrent: false,
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      var context = mockContext('emf:123');

      objectSelectorHelper.searchForObjects = () => Promise.reject('Error!');

      objectSelectorHelper.getSelectedObjects(config, context).then(done).catch((error) => {
        expect(error).to.equal('Error!');
        done();
      })
    });

    it('should fail for invalid select mode', (done) => {
      var results = [{id: 'search-result-id'}];
      objectSelectorHelper.searchForObjects = () => Promise.resolve({results});

      var config = {};

      objectSelectorHelper.getSelectedObjects(config, context).catch((error) => {
        expect(error).to.exist;
        done();
      });
    });
  });

  describe('getSelectedObject()', () => {
    it('should defer to getObjectSelection() and resolve selection', (done) => {
      var context = mockContext('emf:123');
      var config = {
        selectObjectMode: SELECT_OBJECT_CURRENT
      };

      var methodSpy = sinon.spy(objectSelectorHelper, 'getSelectedObject');

      objectSelectorHelper.getSelectedObject(config, context).then((selection) => {
        expect(methodSpy.calledOnce).to.be.true;
        expect(selection).to.deep.equal('emf:123');
        done();
      }).catch(done);
    });

    it('should reject if there are more than one objects', (done) => {
      var config = {
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };

      var results = ['emf:123', 'emf:456'];
      objectSelectorHelper.searchForObjects = () => Promise.resolve({total: 2, results});


      objectSelectorHelper.getSelectedObject(config).then(done).catch((error) => {
        expect(error).to.exist;
        done();
      });
    });

    it('should propagate any getObjectSelection() rejections', (done) => {
      // No select mode
      objectSelectorHelper.getSelectedObject({}).then(done).catch((error) => {
        expect(error).to.exist;
        done();
      });
    });
  });

  describe('searchForObjects()', () => {
    it('should resolve if there is exactly one result', (done) => {
      let criteria = {rules: []};
      searchService.search = sinon.spy(() => {
        return {
          promise: Promise.resolve(getSearchResponse())
        };
      });

      objectSelectorHelper.searchForObjects(criteria).then((response) => {
        expect(response.results[0]).to.equal('1');
        done();
      }).catch(done);
    });

    it('should reject if there are no results', (done) => {
      let criteria = {rules: []};
      searchService.search = sinon.spy(() => {
        return {
          promise: Promise.resolve({
            data: {values: []}
          })
        };
      });

      objectSelectorHelper.searchForObjects(criteria).then(done).catch((error) => {
        expect(error).to.exist;
        done();
      });
    });

    it('should use the provided arguments map', (done) => {
      let criteria = {rules: []};
      searchService.search = sinon.spy(() => {
        return {
          promise: Promise.resolve(getSearchResponse())
        };
      });
      var argsMap = {
        pageSize: 1000,
        properties: ['id', 'title']
      };
      objectSelectorHelper.searchForObjects(criteria, undefined, argsMap).then(() => {
        expect(searchService.search.getCall(0).args[0].arguments).to.deep.equal(argsMap);
        done();
      }).catch(done);
    });

    it('should set arguments map by default', (done) => {
      let criteria = {rules: []};
      searchService.search = sinon.spy(() => {
        return {
          promise: Promise.resolve(getSearchResponse())
        };
      });
      objectSelectorHelper.searchForObjects(criteria, undefined, undefined).then(() => {
        expect(searchService.search.getCall(0).args[0].arguments).to.deep.equal({
          properties: ['id']
        });
        done();
      }).catch(done);
    });

    it('should use the provided search mode', (done) => {
      let criteria = {rules: []};
      searchService.search = sinon.spy(() => {
        return {
          promise: Promise.resolve(getSearchResponse())
        };
      });
      objectSelectorHelper.searchForObjects(criteria, undefined, {}, 'search-mode').then(() => {
        expect(searchService.search.getCall(0).args[0].searchMode).to.equal('search-mode');
        done();
      }).catch(done);
    });

    it('should reject if the provided criteria is not yet defined', (done) => {
      let criteria = {};
      objectSelectorHelper.searchForObjects(criteria).then(done).catch((error) => {
        expect(error).to.exist;
        expect(error).to.eql({
          reason: UNDEFINED_CRITERIA
        });
        done();
      });
    });
  });

  describe('getSelectedItems()', () => {
    it('should update selectedItemsReference object synchronously', () => {
      let config = {
        selectedObjects: ['emf:123456', 'emf:999888']
      };
      let selectedItemsReference = [];
      objectSelectorHelper.getSelectedItems(config, selectedItemsReference);
      expect(selectedItemsReference).to.have.length(2);
      expect(selectedItemsReference[0]).to.have.property('id', 'emf:123456');
      expect(selectedItemsReference[1]).to.have.property('id', 'emf:999888');
    });

    it('should resolve selected items as whole instances', (done) => {
      let config = {
        selectedObjects: ['emf:123456', 'emf:999888', 'emf:missing']
      };
      let instances = [{
        id: 'emf:123456',
        instanceType: 'instance',
        headers: {
          default_header: '123456_default_header'
        },
        properties: {},
        thumbnailImage: 'thumbnail-data'
      }, {
        id: 'emf:999888',
        instanceType: 'emfinstance',
        headers: {
          default_header: '999888_default_header'
        },
        properties: {}
      }];
      instanceService.loadBatch = ()=> {
        return Promise.resolve({data: instances});
      };
      objectSelectorHelper.getSelectedItems(config).then((selectedItems) => {
        expect(selectedItems).to.deep.equal(instances);
        done();
      }).catch(done);
    });

    it('should work with single selection', () => {
      let config = {
        selectedObject: 'emf:123456'
      };
      let selectedItemsReference = [];
      objectSelectorHelper.getSelectedItems(config, selectedItemsReference);
      expect(selectedItemsReference).to.have.length(1);
      expect(selectedItemsReference[0]).to.have.property('id', 'emf:123456');
    });
  });

  describe('removeSelectedObjects()', () => {
    it('should remove selected object from config for single selection', () => {
      let config = {
        selectedObject: 'emf:123456'
      };
      objectSelectorHelper.removeSelectedObjects(config, ['emf:123456']);
      expect(config.selectedObject).to.be.undefined;
    });

    it('should remove selected objects from config for multiple selection', () => {
      let config = {
        selectedObjects: ['emf:123456', 'emf:999888', 'emf:111111']
      };
      objectSelectorHelper.removeSelectedObjects(config, ['emf:123456', 'emf:111111']);
      expect(config.selectedObjects).to.eql(['emf:999888']);
    });
  });
});

function getSearchResponse() {
  return {
    data: {
      resultSize: 123,
      values: [{
        id: '1'
      }]
    }
  };
}

function mockContext(id) {
  return {
    getCurrentObject: sinon.spy(() => {
      var object = new InstanceObject(id);
      return Promise.resolve(object);
    })
  };
}