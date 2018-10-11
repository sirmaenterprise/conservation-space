import {CodelistRestService, OVERWRITE_SERVICE_URL, UPDATE_SERVICE_URL} from 'services/rest/codelist-service';
import {RestClient} from 'services/rest-client';
import {PromiseStub} from 'test/promise-stub';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {stub} from 'test/test-utils';
import {Eventbus} from 'services/eventbus/eventbus';

describe('CodelistRestService', () => {

  let restClient;
  let codelistService;

  beforeEach(() => {
    restClient = stubRestClient();
    codelistService = new CodelistRestService(restClient, stub(Eventbus), new RequestsCacheService());
  });

  function stubRestClient() {
    let clientStub = stub(RestClient);
    clientStub.get.returns(PromiseStub.resolve());
    clientStub.getUrl.returnsArg(0);
    return clientStub;
  }

  it('should perform get request with proper arguments', () => {
    let opts = {
      'codelistNumber': 100,
      'filterBy': 'TST',
      'inclusive': false,
      'q': 'test query'
    };

    codelistService.getCodelist(opts);
    expect(restClient.get.calledOnce);
    expect(restClient.get.args[0][0]).to.equal('/codelist/100');
    expect(restClient.get.args[0][1]).to.eql({
      params: {
        'customFilters[]': undefined,
        'filterBy': 'TST',
        'inclusive': false,
        'filterSource': undefined,
        'q': 'test query'
      }
    });
  });

  it('should return from cache if such exist for requested codelist and there are no additional options', () => {
    let opts = {
      'codelistNumber': 210
    };

    let cachedCodelist = PromiseStub.resolve([
      {
        'ln': 'en',
        'codelist': 210,
        'label': 'Common document',
        'value': 'OT210027',
        'descriptions': {'bg': 'Обикновен документ', 'en': 'Common document'}
      }, {
        'ln': 'en',
        'codelist': 210,
        'label': 'Препоръки за внедряване',
        'value': 'CH210001',
        'descriptions': {'en': 'Препоръки за внедряване', 'extra1': 'DT217002', 'bg': 'Препоръки за внедряване'}
      }
    ]);

    codelistService.cache = {
      '210': cachedCodelist
    };
    expect(codelistService.getCodelist(opts)).to.equals(cachedCodelist);
  });


  it('should not return from cache if there are additional options', () => {
    let opts = {
      'codelistNumber': 210,
      'filterBy': 'TST'
    };

    let cachedCodelist = PromiseStub.resolve([
      {
        'ln': 'en',
        'codelist': 210,
        'label': 'Common document',
        'value': 'OT210027',
        'descriptions': {'bg': 'Обикновен документ', 'en': 'Common document'}
      }, {
        'ln': 'en',
        'codelist': 210,
        'label': 'Препоръки за внедряване',
        'value': 'CH210001',
        'descriptions': {'en': 'Препоръки за внедряване', 'extra1': 'DT217002', 'bg': 'Препоръки за внедряване'}
      }
    ]);

    codelistService.cache = {
      '210': cachedCodelist
    };
    expect(codelistService.getCodelist(opts)).to.not.equals(cachedCodelist);
  });

  it('should provide the service URL for code list upload', () => {
    expect(codelistService.getUpdateServiceUrl()).to.equal(UPDATE_SERVICE_URL);
  });

  it('should provide the service URL for code list overwrite', () => {
    expect(codelistService.getOverwriteServiceUrl()).to.equal(OVERWRITE_SERVICE_URL);
  });

  describe('getCodeLists()', () => {
    it('should perform a proper request with default parameters', () => {
      codelistService.getCodeLists();
      expect(restClient.get.calledWith('/codelists')).to.be.true;
    });
  });

  describe('saveCodeList(coedList)', () => {
    it('should perform proper request with the provided code list', () => {
      let codeList = {value: '13'};
      codelistService.saveCodeList(codeList);
      expect(restClient.post.calledWith('/codelists/13', codeList)).to.be.true;
    });
  });

});
