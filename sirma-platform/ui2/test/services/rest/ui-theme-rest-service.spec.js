import {ThemeRestClient} from 'services/rest/ui-theme-rest-service';
import {RestClient} from 'services/rest-client'
import {stub} from 'test/test-utils';

describe('ThemeRestClient', () => {
  let themeService;

  beforeEach(() => {
    themeService = initThemeRestClient();
  });

  describe('#getThemeData', () => {
    it('should use proper service url', () => {
      themeService.getThemeData();
      expect(themeService.restClient.get.calledWith('/theme')).to.be.true;
    });

  });
});

function initThemeRestClient() {
  return new ThemeRestClient(stub(RestClient));
}