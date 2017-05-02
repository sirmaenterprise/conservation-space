import {LibrariesService} from 'services/rest/libraries-service';
import {HEADER_V2_JSON} from 'services/rest-client';

describe('LibrariesService', () => {

  let restClient = {};
  let librariesService = new LibrariesService(restClient);
  restClient.get = sinon.spy();

  it('loadLibraries() should call the rest client with proper arguments', () => {
    librariesService.loadLibraries();
    expect(restClient.get.calledWithExactly('/libraries', {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });
});