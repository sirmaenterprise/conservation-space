import {HelpRequestRestService} from 'services/rest/help-request-service';

describe('HelpRequestRestService', () => {

  describe('sendHelpRequest()', () => {
    it('should call the rest service with correct params', () => {
      let restClient = {};
      restClient.post = sinon.spy();
      restClient.post.reset();
      let helpRequestRestService = new HelpRequestRestService(restClient);
      let subject = "subject entered by user";
      let type = "type from codelist";
      let description = "mail content";
      let data = {
        subject: subject,
        type: type,
        description: description
      };

      helpRequestRestService.sendHelpRequest(data);

      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.args[0][0]).to.equal('/user/help/request');
      expect(restClient.post.args[0][1].params['subject']).to.equal(subject);
      expect(restClient.post.args[0][1].params['type']).to.equal(type);
      expect(restClient.post.args[0][1].params['description']).to.equal(description);
      expect(restClient.post.args[0][2].headers['Accept']).to.equal('application/vnd.seip.v2+json');
      expect(restClient.post.args[0][2].headers['Content-Type']).to.equal('application/vnd.seip.v2+json');
    });
  });
});