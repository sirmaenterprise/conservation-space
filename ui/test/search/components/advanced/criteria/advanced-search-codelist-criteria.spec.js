import {AdvancedSearchCodelistCriteria} from 'search/components/advanced/criteria/advanced-search-codelist-criteria';

import {AdvancedSearchMocks} from 'test/search/components/advanced/advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';

describe('AdvancedSearchCodelistCriteria', () => {

  var criteria;
  var advancedSearchCodelistCriteria;
  beforeEach(() => {
    // Fix scope mixing issue in karma
    AdvancedSearchCodelistCriteria.prototype.config = undefined;

    criteria = AdvancedSearchMocks.getCriteria().rules[0];
    AdvancedSearchCodelistCriteria.prototype.criteria = criteria;
    AdvancedSearchCodelistCriteria.prototype.property = {
      codeLists: [1]
    };

    advancedSearchCodelistCriteria = new AdvancedSearchCodelistCriteria(mock$scope(), mockCodelistRestService(), PromiseAdapterMock.mockAdapter());
  });

  afterEach(() => {
    // Fix scope mixing issue in karma
    AdvancedSearchCodelistCriteria.prototype.criteria = undefined;
    AdvancedSearchCodelistCriteria.prototype.config = undefined;
  });

  it('should construct the component to be enabled by default', () => {
    expect(advancedSearchCodelistCriteria.config.disabled).to.be.false;
  });

  it('should fetch the code list values and sort', (done) => {
    advancedSearchCodelistCriteria.property.codeLists = [1];
    advancedSearchCodelistCriteria.loadCodeValues().then((codeValues) => {
      var expected = [{
        id: 'approved',
        text: 'Approved'
      }, {
        id: 'deleted',
        text: 'Deleted'
      }];
      expect(codeValues).to.deep.equal(expected);
      expect(advancedSearchCodelistCriteria.codelistRestService.getCodelist.called).to.be.true;
      var expectedArguments = {codelistNumber: 1};
      expect(advancedSearchCodelistCriteria.codelistRestService.getCodelist.getCall(0).args[0]).to.deep.equal(expectedArguments);
      done();
    }).catch(done);
  });

  it('should resolve empty array if there are no code values', (done) => {
    advancedSearchCodelistCriteria.property.codeLists = [1];
    advancedSearchCodelistCriteria.codelistRestService.getCodelist = () => {
      return Promise.resolve([]);
    };
    advancedSearchCodelistCriteria.loadCodeValues().then((codeValues) => {
      expect(codeValues).to.deep.equal([]);
      done();
    }).catch(done);
  });

  it('should merge code list values from multiple code lists', (done) => {
    // Switching 2 to be the first so it will load its code values first
    advancedSearchCodelistCriteria.property.codeLists = [2, 1];
    advancedSearchCodelistCriteria.loadCodeValues().then((codeValues) => {
      var expected = [{
        id: 'approved',
        text: 'Approved'
      }, {
        id: 'deleted',
        text: 'Removed'
      }];
      expect(codeValues).to.deep.equal(expected);
      done();
    }).catch(done);
  });

  it('should construct a select configuration', () => {
    advancedSearchCodelistCriteria.loadCodeValues = sinon.spy(()=> {
      return PromiseStub.resolve([]);
    });
    advancedSearchCodelistCriteria.createSelectConfig();
    expect(advancedSearchCodelistCriteria.selectConfig).to.exist;
    expect(advancedSearchCodelistCriteria.selectConfig.disabled).to.be.false;
    expect(advancedSearchCodelistCriteria.selectConfig.data).to.exist;
    expect(advancedSearchCodelistCriteria.selectConfig.selectOnClose).to.be.true;
  });

  it('should construct the select configuration with the code list values', () => {
    advancedSearchCodelistCriteria.loadCodeValues = sinon.spy(() => {
      return PromiseStub.resolve(getTestCodeValues());
    });
    advancedSearchCodelistCriteria.createSelectConfig();
    expect(advancedSearchCodelistCriteria.loadCodeValues.called).to.be.true;
    expect(advancedSearchCodelistCriteria.selectConfig.data).to.deep.equal(getTestCodeValues());
  });

  it('should register a watcher for the disabled property', () => {
    advancedSearchCodelistCriteria.$scope.$watch = sinon.spy();
    advancedSearchCodelistCriteria.registerDisabledWatcher();
    expect(advancedSearchCodelistCriteria.$scope.$watch.called).to.be.true;
  });

  it('should update the select enabled state when the configuration is changed', () => {
    advancedSearchCodelistCriteria.config.disabled = true;
    advancedSearchCodelistCriteria.selectConfig = {};
    advancedSearchCodelistCriteria.$scope.$digest();
    expect(advancedSearchCodelistCriteria.selectConfig.disabled).to.be.true;
  });
});

function mockCodelistRestService() {
  return {
    getCodelist: sinon.spy((opts) => {
      var data;
      if (opts.codelistNumber === 1) {
        data = getTestCodeValues();
      } else {
        data = [{
          value: 'deleted',
          label: 'Removed'
        }];
      }
      return {data: data};
    })
  };
}

function getTestCodeValues() {
  return [{
    value: 'deleted',
    label: 'Deleted'
  }, {
    value: 'approved',
    label: 'Approved'
  }];
}