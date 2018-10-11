import {AdvancedSearchCodelistCriteria} from 'search/components/advanced/criteria/advanced-search-codelist-criteria';
import {CodelistRestService} from 'services/rest/codelist-service';

import {AdvancedSearchMocks} from 'test/search/components/advanced/advanced-search-mocks'
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('AdvancedSearchCodelistCriteria', () => {

  var criteria;
  var advancedSearchCodelistCriteria;
  beforeEach(() => {
    criteria = AdvancedSearchMocks.getCriteria().rules[0];

    advancedSearchCodelistCriteria = new AdvancedSearchCodelistCriteria(mockCodelistRestService(), PromiseAdapterMock.mockImmediateAdapter());
    advancedSearchCodelistCriteria.criteria = criteria;
    advancedSearchCodelistCriteria.property = {
      codeLists: [1]
    };
    advancedSearchCodelistCriteria.ngOnInit();
  });

  it('should construct the component to be enabled by default', () => {
    expect(advancedSearchCodelistCriteria.config.disabled).to.be.false;
  });

  it('should fetch the code list values and sort them alphabetically', () => {
    var fetchedCodeValues = null;
    advancedSearchCodelistCriteria.property.codeLists = [1];
    advancedSearchCodelistCriteria.loadCodeValues().then(codeValues => fetchedCodeValues = codeValues);

    var expected = [{
      id: 'approved',
      text: 'Approved'
    }, {
      id: 'deleted',
      text: 'Deleted'
    }];
    expect(fetchedCodeValues).to.deep.equal(expected);

    var expectedArguments = {codelistNumber: 1};
    expect(advancedSearchCodelistCriteria.codelistRestService.getCodelist.called).to.be.true;
    expect(advancedSearchCodelistCriteria.codelistRestService.getCodelist.getCall(0).args[0]).to.deep.equal(expectedArguments);
  });

  it('should resolve empty array if there are no code values', () => {
    var fetchedCodeValues = null;
    advancedSearchCodelistCriteria.codelistRestService.getCodelist = () => PromiseStub.resolve([]);
    advancedSearchCodelistCriteria.loadCodeValues().then(codeValues => fetchedCodeValues = codeValues);
    expect(fetchedCodeValues).to.deep.equal([]);
  });

  it('should merge code list values from multiple code lists', () => {
    var fetchedCodeValues = null;
    // Switching 2 to be the first so it will load its code values first
    advancedSearchCodelistCriteria.property.codeLists = [2, 1];
    advancedSearchCodelistCriteria.loadCodeValues().then(codeValues => fetchedCodeValues = codeValues);
    var expected = [{
      id: 'approved',
      text: 'Approved'
    }, {
      id: 'deleted',
      text: 'Removed'
    }];
    expect(fetchedCodeValues).to.deep.equal(expected);
  });

  it('should construct a select configuration', () => {
    advancedSearchCodelistCriteria.loadCodeValues = sinon.spy(()=> {
      return PromiseStub.resolve([]);
    });
    advancedSearchCodelistCriteria.ngOnInit();
    expect(advancedSearchCodelistCriteria.selectConfig).to.exist;
    expect(advancedSearchCodelistCriteria.selectConfig.data).to.exist;
    expect(advancedSearchCodelistCriteria.selectConfig.selectOnClose).to.be.true;
  });

  it('should construct the select configuration with the code list values', () => {
    let codeValues = getTestCodeValues();
    advancedSearchCodelistCriteria.loadCodeValues = sinon.spy(() => {
      return PromiseStub.resolve(codeValues);
    });
    advancedSearchCodelistCriteria.createSelectConfig();
    expect(advancedSearchCodelistCriteria.loadCodeValues.called).to.be.true;
    expect(advancedSearchCodelistCriteria.selectConfig.data).to.deep.equal(codeValues);
  });

  it('should construct the select with a function for determining the disabled state', () => {
    expect(advancedSearchCodelistCriteria.selectConfig.isDisabled()).to.be.false;
    advancedSearchCodelistCriteria.config.disabled = true;
    expect(advancedSearchCodelistCriteria.selectConfig.isDisabled()).to.be.true;
  });

  it('should return empty array', () => {
    var fetchedCodeValues = null;
    advancedSearchCodelistCriteria.property = undefined;
    advancedSearchCodelistCriteria.loadCodeValues().then(codeValues => fetchedCodeValues = codeValues);
    expect(fetchedCodeValues).to.deep.equal([]);
  });
});

function mockCodelistRestService() {
  var stubbedService = stub(CodelistRestService);
  stubbedService.getCodelist = sinon.spy((opts) => {
    var data;
    if (opts.codelistNumber === 1) {
      data = getTestCodeValues();
    } else {
      data = [{
        value: 'deleted',
        label: 'Removed'
      }];
    }
    return PromiseStub.resolve({data});
  });
  return stubbedService;
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