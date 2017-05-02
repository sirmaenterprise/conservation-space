import {CodelistRestService} from 'services/rest/codelist-service';

describe('Tests for codelist service rest client', function () {

  var restClient = {};
  restClient.get = sinon.spy();
  var codelistService = new CodelistRestService(restClient);

  it('Test codelist service get request with proper arguments ', function () {
    let opts = {
      'codelistNumber': 100,
      'filterBy': 'TST',
      'inclusive': false,
      'q': 'test query'
    };

    codelistService.getCodelist(opts);
    expect(restClient.get.calledOnce);
    expect(restClient.get.args[0][0]).to.equal('/codelist/100');
    expect(restClient.get.args[0][1]).to.eql({ params:{
      'customFilters[]': undefined,
      'filterBy': 'TST',
      'inclusive': false,
      'filterSource':undefined,
      'q': 'test query'}
    });
  });
});
