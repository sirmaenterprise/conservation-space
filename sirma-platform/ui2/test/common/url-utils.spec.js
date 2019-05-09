import {UrlUtils} from 'common/url-utils';

describe('UrlUtils', () => {

  describe('getUrlFragment', () => {
    it('should extract the url fragment if any', () => {
      const URL = 'http://localhost/#/idoc/emf:123456?param1=value1#tab1';
      expect(UrlUtils.getUrlFragment(URL)).to.equal('tab1');
      const encodedURL = 'http://localhost/#/idoc/emf:123456?param1=value1%23tab1';
      expect(UrlUtils.getUrlFragment(encodedURL)).to.equal('tab1');
    });

    it('should return empty string if no url fragment is found', () => {
      const URL = 'http://localhost/#/idoc/emf:123456?param1=value1';
      expect(UrlUtils.getUrlFragment(URL)).to.equal('');
    });
  });

  describe('getParameter()', () => {
    it('should properly extracts request parameter from url', () => {
      const URL = 'http://localhost/#/?param1=value1&param2=value_2';
      var result = UrlUtils.getParameter(URL, 'param1');
      expect(result).to.equal('value1')
    });

    it('should properly returns null on missing parameter', () => {
      const URL = 'http://localhost/#/?param1=value1&param2=value_2';
      var result = UrlUtils.getParameter(URL, 'param3');
      expect(result).to.be.null;
    });

    it('should properly returns parameter from url if there is url segment after it', () => {
      const URL = 'http://localhost/#/idoc/emf:123456?param1=value1%23tabid1';
      var result = UrlUtils.getParameter(URL, 'param1');
      expect(result).to.equal('value1');
    });

    it('should return first parameter if more than one with the same name exist', () => {
      expect(UrlUtils.getParameter('http://localhost/#/idoc/emf:123456?param1=value1&param1=value2', 'param1')).to.equal('value1');
    });
  });

  describe('buildIdocUrl()', () => {
    it('should build url using provided instance id without additional parameters', () => {
      expect(UrlUtils.buildIdocUrl('emf:123456')).to.equal('/#/idoc/emf:123456');
    });

    it('should build url using provided instance id and additional request parameters', () => {
      expect(UrlUtils.buildIdocUrl('emf:123456', null, {
        'mode': 'preview',
        'user': 'John'
      })).to.equal('/#/idoc/emf:123456?mode=preview&user=John');
    });

    it('should build url for concrete idoc tab and additional request parameters', () => {
      expect(UrlUtils.buildIdocUrl('emf:123456', 'tab1', {
        'mode': 'preview',
        'user': 'John'
      })).to.equal('/#/idoc/emf:123456?mode=preview&user=John#tab1');
    });
  });

  describe('getParamSeparator', () => {
    it('should return proper separator according to the last symbol of the provided url string', () => {
      expect(UrlUtils.getParamSeparator('/remote/api/export/test')).to.equal('?');
      expect(UrlUtils.getParamSeparator('/remote/api/export/test?param1=1')).to.equal('&');
      expect(UrlUtils.getParamSeparator('/remote/api/export/test?param1=1&param2=2')).to.equal('&');
    });
  });

  it('getParameterArray() should return an array with parameter values if multiple URL parameters with the same name exist', () => {
    expect(UrlUtils.getParameterArray('/#/emf:123456?tab=1&anotherParam=randomValue&tab=2&tab=test', 'tab')).to.eql(['1', '2', 'test']);
    expect(UrlUtils.getParameterArray('/#/emf:123456?tabParam=1&anotherParam=randomValue', 'tab')).to.be.undefined;
  });

  describe('getIdFromUrl()', () => {
    it('should return null if no URL is provided', () => {
      expect(UrlUtils.getIdFromUrl(null)).to.be.null;
      expect(UrlUtils.getIdFromUrl('')).to.be.null;
    });

    it('should extract instance identifier from url', () => {
      expect(UrlUtils.getIdFromUrl('http://localhost/#/idoc/emf:123456')).to.equal('emf:123456');
      expect(UrlUtils.getIdFromUrl('http://localhost/#/emf:123456/param1=value1&param2=value_2')).to.equal('emf:123456');
      expect(UrlUtils.getIdFromUrl('http://localhost/#/emf:123456-1abc?param1=value1&param2=value_2')).to.equal('emf:123456-1abc');
      expect(UrlUtils.getIdFromUrl('http://localhost/#/emf:a233456-1abc')).to.equal('emf:a233456-1abc');
      expect(UrlUtils.getIdFromUrl('http://localhost/#/emf:a2b-c1233456/param1=value1&param2=value_2')).to.equal('emf:a2b-c1233456');
      expect(UrlUtils.getIdFromUrl('http://localhost/#/idoc/param1=value1&param2=value_2')).to.be.null;
      expect(UrlUtils.getIdFromUrl('http://localhost/#/idoc/emf:admin-default.tenant')).to.equal('emf:admin-default.tenant');
      expect(UrlUtils.getIdFromUrl('http://localhost/#/idoc/emf:GROUP_ALL_USERS')).to.equal('emf:GROUP_ALL_USERS');
    });

    it('should extract the instance id if there is host:port in the URL', ()=> {
      expect(UrlUtils.getIdFromUrl('https://localhost:443#/idoc/emf:admin')).to.equal('emf:admin');
    });

    it('should extract the instance id if there is tab id in the URL', () => {
      expect(UrlUtils.getIdFromUrl('https://localhost:443/?debugger#/idoc/emf:admin#123-tabid-456')).to.equal('emf:admin');
    });
  });

  describe('removeQueryParam()', () => {
    it('should remove query param from given single param', () => {
      let window = mockWindow('http://localhost/#/idoc/emf:instanceId?from=test');

      UrlUtils.removeQueryParam(window, 'from');

      expect(window.history.replaceState.calledWith({}, '', 'http://localhost/#/idoc/emf:instanceId')).to.be.true;
    });

    it('should remove last query param and keep other params', () => {
      let window = mockWindow('http://localhost/#/idoc/emf:instanceId?tab=true&from=test');

      UrlUtils.removeQueryParam(window, 'from');

      expect(window.history.replaceState.calledWith({}, '', 'http://localhost/#/idoc/emf:instanceId?tab=true')).to.be.true;
    });

    it('should remove in between query param and keep other params', () => {
      let window = mockWindow('http://localhost/#/idoc/emf:instanceId?tab=true&from=test&state=active');

      UrlUtils.removeQueryParam(window, 'from');

      expect(window.history.replaceState.calledWith({}, '', 'http://localhost/#/idoc/emf:instanceId?tab=true&state=active')).to.be.true;
    });

    it('should do nothing if query param is missing', () => {
      let window = mockWindow('http://localhost/#/idoc/emf:instanceId?tab=true');

      UrlUtils.removeQueryParam(window, 'from');

      expect(window.history.replaceState.called).to.be.false;
    });
  });

  function mockWindow(url) {
    return {
      location: {
        href: url
      },
      history: {
        replaceState: sinon.spy()
      }
    };
  }
});