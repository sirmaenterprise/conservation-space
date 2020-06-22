import {ObjectSelect, OBJECT_SELECT_PROPERTIES} from 'components/select/object/object-select';

import {SelectMocks} from './select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {SearchServiceMock} from 'test/services/rest/search-service-mock';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';

import {ORDER_DESC, ORDER_RELEVANCE} from 'search/order-constants';
import {EMF_MODIFIED_ON} from 'instance/instance-properties';
import {CRITERIA_FTS_RULE_FIELD} from 'search/utils/search-criteria-utils';

describe('ObjectSelect', () => {

  var objectSelect;
  var searchService;
  var instanceService;

  beforeEach(() => {
    // Fixing a problem where the scopes are mixed between different tests.
    ObjectSelect.prototype.config = undefined;
    ObjectSelect.prototype.actualConfig = undefined;

    searchService = new SearchServiceMock();
    sinon.spy(searchService, 'search');
    instanceService = {
      load: sinon.spy(() => {
        return PromiseStub.resolve();
      })
    };

    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    objectSelect = new ObjectSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), searchService, instanceService, promiseAdapter);
  });

  afterEach(() => {
    // Fixing a problem where the scopes are mixed between different tests.
    ObjectSelect.prototype.config = undefined;
    ObjectSelect.prototype.actualConfig = undefined;
  });

  it('should set correct search mediator arguments', () => {
    expect(objectSelect.mediator.arguments.orderDirection).to.equal(ORDER_DESC);
    expect(objectSelect.mediator.arguments.properties).to.equal(OBJECT_SELECT_PROPERTIES);
  });

  it('should not convert data without values', () => {
    var response = {
      data: {}
    };
    expect(objectSelect.config.dataConverter(response)).to.deep.equal([]);
  });

  it('should correctly convert data', () => {
    var response = {
      data: {
        values: [getResultItem('1', 'One'), getResultItem('2', 'Two'), getResultItem('3', {text: 'Three'})]
      }
    };
    var expected = [{
      id: '1', text: 'One'
    }, {
      id: '2', text: 'Two'
    }, {
      id: '3', text: 'Three'
    }];
    expect(objectSelect.config.dataConverter(response)).to.deep.equal(expected);
  });

  it('should construct search tree & mediator arguments when fetching data', () => {
    var params = {
      data: {
        q: 'query'
      }
    };
    objectSelect.config.dataLoader(params);

    expect(searchService.search.getCall(0).args[0]).to.exist;
    expect(searchService.search.getCall(0).args[0].query).to.exist;
    expect(searchService.search.getCall(0).args[0].query.tree.rules[0].value).to.equal('query');
    expect(searchService.search.getCall(0).args[0].query.tree.rules[0].field).to.equal(CRITERIA_FTS_RULE_FIELD);
    expect(objectSelect.mediator.arguments.orderBy).to.eq(ORDER_RELEVANCE);
  });

  it('should not construct search tree & mediator arguments when fetching data with missing params', () => {
    objectSelect.config.dataLoader();
    expect(objectSelect.mediator.arguments.orderBy).to.eq(EMF_MODIFIED_ON);
    expect(searchService.search.getCall(0).args[0].query.tree.rules.length).to.equal(0);
  });

  it('should not construct search tree & mediator arguments when fetching data with missing data', () => {
    var params = {};
    objectSelect.config.dataLoader(params);
    expect(objectSelect.mediator.arguments.orderBy).to.eq(EMF_MODIFIED_ON);
    expect(searchService.search.getCall(0).args[0].query.tree.rules.length).to.equal(0);
  });

  it('should not construct search tree & mediator arguments when fetching data with missing query', () => {
    var params = {
      data: {}
    };
    objectSelect.config.dataLoader(params);
    expect(objectSelect.mediator.arguments.orderBy).to.eq(EMF_MODIFIED_ON);
    expect(searchService.search.getCall(0).args[0].query.tree.rules.length).to.equal(0);
  });

  it('should abort last search before making a new one', () => {
    var params = {
      data: {}
    };
    objectSelect.mediator.abortLastSearch = sinon.spy();

    objectSelect.config.dataLoader(params);
    expect(searchService.search.calledOnce).to.be.true;
    expect(searchService.search.getCall(0).args[0]).to.eq.true;
    expect(objectSelect.mediator.abortLastSearch.calledOnce).to.be.true;
  });

  it('should provide properties set when searching', () => {
    objectSelect.config.dataLoader();
    var searchArguments = searchService.search.getCall(0).args[0].arguments;
    expect(searchArguments).to.exist;
    expect(searchArguments.properties).to.deep.equal(['id', 'title']);
  });

  it('should append predefined items to results', () => {
    searchService.search = () => {
      return {
        promise: PromiseStub.resolve({
          data: {}
        })
      };
    };
    objectSelect.config.predefinedItems = [getResultItem('1', 'One')];

    objectSelect.config.dataLoader().then((response) => {
      expect(response.data.values).to.deep.equal([getResultItem('1', 'One')]);
    });
  });

  it('should append predefined items at top', () => {
    searchService.search = () => {
      return {
        promise: PromiseStub.resolve({
          data: {
            values: [getResultItem('2', 'Two')]
          }
        })
      };
    };
    objectSelect.config.predefinedItems = [getResultItem('1', 'One')];

    var expected = [getResultItem('1', 'One'), getResultItem('2', 'Two')];
    objectSelect.config.dataLoader().then((response) => {
      expect(response.data.values).to.deep.equal(expected);
    });
  });

  it('should correctly resolve objects in mapper', () => {
    instanceService.load = () => {
      return PromiseStub.resolve({
        data: getResultItem('2', 'Two')
      });
    };
    objectSelect.config.predefinedItems = [getResultItem('1', 'One')];

    var expected = [{id: '1', text: 'One'}, {id: '2', text: 'Two'}];

    objectSelect.config.mapper(['1', '2']).then((result) => {
      expect(result).to.deep.equal(expected);
    });
  });

  it('should correctly handle instances with title object in the select mapper', () => {
    instanceService.load = () => {
      return PromiseStub.resolve({
        data: getResultItem('1', {text: 'One'})
      });
    };

    objectSelect.config.predefinedItems = [getResultItem('2', {text: 'Two'})];

    var expected = [{id: '2', text: 'Two'}, {id: '1', text: 'One'}];
    objectSelect.config.mapper(['1', '2']).then((result) => {
      expect(result).to.deep.equal(expected);
    });
  });

  it('should append rule for available objects when constructing search tree', () => {
    objectSelect.config.availableObjects = ['emf:123456', 'emf:999888'];
    let searchTree = objectSelect.getSearchTree('test');
    expect(searchTree.rules).to.have.length(2);

    expect(searchTree.rules[0].value).to.equals('test');
    expect(searchTree.rules[0].field).to.equals(CRITERIA_FTS_RULE_FIELD);

    expect(searchTree.rules[1].field).to.equals('instanceId');
    expect(searchTree.rules[1].value).to.eql(['emf:123456', 'emf:999888']);
  });

  it('should append rule for types when constructing search tree', () => {
    objectSelect.config.types = ['emf:User', 'emf:Group'];
    let searchTree = objectSelect.getSearchTree('test');
    expect(searchTree.rules).to.have.length(2);

    expect(searchTree.rules[0].value).to.equals('test');
    expect(searchTree.rules[0].field).to.equals(CRITERIA_FTS_RULE_FIELD);

    expect(searchTree.rules[1].field).to.equals('types');
    expect(searchTree.rules[1].value).to.eql(['emf:User', 'emf:Group']);
  });
});

function getResultItem(id, title) {
  return {
    id: id,
    properties: {
      title: title
    }
  };
}
