import {TemplateService, BLANK_INSTANCE_TEMPLATE_ID} from 'services/rest/template-service';
import {HEADER_V2_JSON} from 'services/rest-client';

describe('TemplateService', () => {
  var templateService;
  var getSpy = sinon.spy();
  var restClient = {
    get: function() {
      getSpy.apply(null, arguments);
      return Promise.resolve({ data: [] });
    }
  };

  beforeEach(() => {
    getSpy.reset();
    templateService = new TemplateService(restClient);
  });

  describe('create', () => {
    beforeEach(() => {
      restClient.post = sinon.spy();
    });

    it('should call service with appropriate headers', () => {
      var postSpy = restClient.post;
      var templateData = {
        forType: 'tag',
        title: 'Primary Tag template',
        primary: false,
        purpose: 'creatable',
        sourceInstance: 'instanceId'
      };

      var expectedHeaders = {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      };

      templateService.create(templateData);

      expect(postSpy.calledOnce).to.be.true;
      expect(postSpy.getCall(0).args[0]).to.eq('/templates');
      expect(postSpy.getCall(0).args[1]).to.deep.eq({
        forType: templateData.forType,
        properties: {
          title: templateData.title,
          primary: templateData.primary,
          purpose: templateData.purpose
        },
        sourceInstance: templateData.sourceInstance
      });
      expect(postSpy.getCall(0).args[2].headers).to.deep.eq(expectedHeaders);
    });
  });

  describe('loadTemplates', () => {

    it('should handle string as argument', () => {
      templateService.loadTemplates('defid');

      expect(getSpy.calledOnce).to.be.true;
      expect(getSpy.getCall(0).args[0]).to.eq('/templates?group-id=defid');
    });

    it('should handle array as argument', () => {
      templateService.loadTemplates(['defid1', 'defid2']);

      expect(getSpy.calledOnce).to.be.true;
      expect(getSpy.getCall(0).args[0]).to.eq('/templates?group-id=defid1&group-id=defid2');
    });

    it('should call service with appropriate headers', (done) => {
      var expectedHeaders = {'Accept': HEADER_V2_JSON};

      templateService.loadTemplates().then(() => {
        expect(getSpy.calledOnce).to.be.true;
        expect(getSpy.getCall(0).args[0]).to.eq('/templates');
        expect(getSpy.getCall(0).args[1].headers).to.deep.eq(expectedHeaders);
        done();
      }).catch(done);
    });
  });

  describe('loadContent', () => {

    it('should call the rest client for all identifiers', (done) => {
      templateService.loadContent('1').then(() => {
        expect(getSpy.calledOnce).to.be.true;
        expect(getSpy.getCall(0).args[0]).to.eq('/templates/1/content');
        expect(getSpy.getCall(0).args[1].headers).to.deep.eq({'Accept': 'text/html'});
        done();
      }).catch(done);
    });
  });
});