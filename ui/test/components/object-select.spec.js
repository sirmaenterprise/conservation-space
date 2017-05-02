import {ObjectSelect} from 'components/select/object/object-select';
import {SelectMocks} from './select-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {SearchServiceMock} from 'test/services/rest/search-service-mock';

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
        return Promise.resolve();
      })
    };

    objectSelect = new ObjectSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), searchService, instanceService);
  });

  afterEach(() => {
    // Fixing a problem where the scopes are mixed between different tests.
    ObjectSelect.prototype.config = undefined;
    ObjectSelect.prototype.actualConfig = undefined;
  });

  it('should not convert data without values', ()=> {
    var response = {
      data: {}
    };
    expect(objectSelect.config.dataConverter(response)).to.deep.equal([]);
  });

  it('should correctly convert data', ()=> {
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

  it('should construct search tree when fetching data', ()=> {
    var params = {
      data: {
        q: 'query'
      }
    };
    objectSelect.config.dataLoader(params);

    expect(searchService.search.getCall(0).args[0]).to.exist;
    expect(searchService.search.getCall(0).args[0].query).to.exist;
    expect(searchService.search.getCall(0).args[0].query.tree.rules[0].value).to.equal('query');
  });

  it('should construct search tree when fetching data with missing params', ()=> {
    objectSelect.config.dataLoader();
    expect(searchService.search.getCall(0).args[0].query.tree.rules[0].value).to.equal('')
  });

  it('should construct search tree when fetching data with missing data', ()=> {
    var params = {};
    objectSelect.config.dataLoader(params);
    expect(searchService.search.getCall(0).args[0].query.tree.rules[0].value).to.equal('')
  });

  it('should construct search tree when fetching data with missing query', ()=> {
    var params = {
      data: {}
    };
    objectSelect.config.dataLoader(params);
    expect(searchService.search.getCall(0).args[0].query.tree.rules[0].value).to.equal('')
  });

  it('should provide properties set when searching', () => {
    objectSelect.config.dataLoader();
    var searchArguments = searchService.search.getCall(0).args[0].arguments;
    expect(searchArguments).to.exist;
    expect(searchArguments.properties).to.deep.equal(['id', 'title']);
  });

  it('should append predefined items to results', (done) => {
    searchService.search = () => {
      return {
        promise: new Promise((resolve) => {
          resolve({
            data: {}
          });
        })
      }
    };
    objectSelect.config.predefinedItems = [getResultItem('1', 'One')];

    objectSelect.config.dataLoader().then((response)=> {
      expect(response.data.values).to.deep.equal([getResultItem('1', 'One')]);
      done();
    }).catch(done);
  });

  it('should append predefined items at top', (done) => {
    searchService.search = () => {
      return {
        promise: new Promise((resolve) => {
          resolve({
            data: {
              values: [getResultItem('2', 'Two')]
            }
          });
        })
      }
    };
    objectSelect.config.predefinedItems = [getResultItem('1', 'One')];

    var expected = [getResultItem('1', 'One'), getResultItem('2', 'Two')];
    objectSelect.config.dataLoader().then((response)=> {
      expect(response.data.values).to.deep.equal(expected);
      done();
    }).catch(done);
  });

  it('should correctly resolve objects in mapper', (done) => {
    instanceService.load = () => {
      return new Promise((resolve) => {
        resolve({
          data: getResultItem('2', 'Two')
        });
      });
    };
    objectSelect.config.predefinedItems = [getResultItem('1', 'One')];

    var expected = [{id: '1', text: 'One'}, {id: '2', text: 'Two'}];

    objectSelect.config.mapper(['1', '2']).then((result) => {
      expect(result).to.deep.equal(expected);
      done();
    }).catch(done);
  });

  it('should correctly handle instances with title object in the select mapper', (done) => {
    instanceService.load = () => {
      return new Promise((resolve) => {
        resolve({
          data: getResultItem('1', {text: 'One'})
        });
      });
    };

    objectSelect.config.predefinedItems = [getResultItem('2', {text: 'Two'})];

    var expected = [{id: '2', text: 'Two'}, {id: '1', text: 'One'}];
    objectSelect.config.mapper(['1', '2']).then((result) => {
      expect(result).to.deep.equal(expected);
      done();
    }).catch(done);
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
