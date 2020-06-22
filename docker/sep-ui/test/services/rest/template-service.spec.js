import {TemplateService} from 'services/rest/template-service';
import {PromiseStub} from 'test/promise-stub';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {stub} from 'test/test-utils';

describe('TemplateService', () => {
  var templateService;
  var restClient;

  beforeEach(() => {
    restClient = stub(RestClient);
    restClient.get.returns(PromiseStub.resolve({data: []}));
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
        title: templateData.title,
        primary: templateData.primary,
        purpose: templateData.purpose,
        sourceInstance: templateData.sourceInstance
      });
      expect(postSpy.getCall(0).args[2].headers).to.deep.eq(expectedHeaders);
    });
  });

  describe('loadTemplates', () => {

    it('should handle string as argument', () => {
      templateService.loadTemplates('defid');

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.eq('/templates?group-id=defid');
    });

    it('should handle array as argument', () => {
      templateService.loadTemplates(['defid1', 'defid2']);

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.eq('/templates?group-id=defid1&group-id=defid2');
    });

    it('should call service with appropriate headers', (done) => {
      var expectedHeaders = {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON,
      };

      templateService.loadTemplates().then(() => {
        expect(restClient.get.calledOnce).to.be.true;
        expect(restClient.get.getCall(0).args[0]).to.eq('/templates');
        expect(restClient.get.getCall(0).args[1].headers).to.deep.eq(expectedHeaders);
        done();
      }).catch(done);
    });
  });

  it('should POST endpoint to load templates providing group id, purpose and filter map ', function () {
    const INSTANCE_ID = 'testId';
    const PURPOSE = 'creatable';
    const FILTER_CRITERIA = {
      'active': true
    };

    templateService.loadTemplates(INSTANCE_ID, PURPOSE, FILTER_CRITERIA);

    expect(restClient.post.calleOnce);

    expect(restClient.post.getCall(0).args[0]).to.equal('/templates/search');
    expect(restClient.post.getCall(0).args[1]).to.eql({
      group: INSTANCE_ID,
      purpose: PURPOSE,
      filter: FILTER_CRITERIA
    });
  });

  describe('loadContent', () => {

    it('should call the rest client for all identifiers', (done) => {
      templateService.loadContent('1').then(() => {
        expect(restClient.get.calledOnce).to.be.true;
        expect(restClient.get.getCall(0).args[0]).to.eq('/templates/1/content');
        expect(restClient.get.getCall(0).args[1].headers).to.deep.eq({'Accept': 'text/html'});
        done();
      }).catch(done);
    });
  });

  it('should call the rest client to set a template as primary', function () {
    const INSTANCE_ID = 'testId';

    templateService.setTemplateAsPrimary(INSTANCE_ID);

    expect(restClient.post.calleOnce);

    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/${INSTANCE_ID}/actions/set-template-as-primary`);
  });

  it('should call the rest client to edit template rules', function () {
    const INSTANCE_ID = 'testId';
    const RULES = 'primary == true';

    templateService.editTemplateRules(INSTANCE_ID, RULES);

    expect(restClient.post.calleOnce);

    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/${INSTANCE_ID}/actions/edit-template-rule`);
    expect(restClient.post.getCall(0).args[1]).to.eql({
      rule: RULES
    });
  });

  it('should call the rest client with null value if no rules are provided', function () {
    const INSTANCE_ID = 'testId';

    templateService.editTemplateRules(INSTANCE_ID, '');

    expect(restClient.post.calleOnce);

    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/${INSTANCE_ID}/actions/edit-template-rule`);
    expect(restClient.post.getCall(0).args[1]).to.eql({
      rule: null
    });
  });

  it('should call the rest client to deactivate a template', function () {
    const INSTANCE_ID = 'testId';

    templateService.deactivateTemplate(INSTANCE_ID);

    expect(restClient.post.calleOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/${INSTANCE_ID}/actions/deactivate-template`);
  });

  it('should call the rest client to update existing objects with template', function () {
    const INSTANCE_ID = 'testId';

    templateService.updateInstanceTemplate(INSTANCE_ID);

    expect(restClient.post.calleOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/actions/update-instance-template`);
    expect(restClient.post.getCall(0).args[1]).to.eql({
      templateInstance: INSTANCE_ID
    });
  });

  it('should call the rest client to update instance with latest template version', function () {
    const INSTANCE_ID = 'testId';

    templateService.updateSingleInstanceTemplate(INSTANCE_ID);

    expect(restClient.post.calleOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/actions/update-to-latest-template`);
    expect(restClient.post.getCall(0).args[1]).to.eql({
      instance: INSTANCE_ID
    });
  });

  it('should call the rest client to get latest template version used by instance', function () {
    const INSTANCE_ID = 'testId';

    templateService.getActualTemplateVersion(INSTANCE_ID);

    expect(restClient.post.calleOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal(`/instances/template-version`);
    expect(restClient.post.getCall(0).args[1]).to.eql({
      instance: INSTANCE_ID
    });
  });
});