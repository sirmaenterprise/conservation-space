import {MailboxInfoService} from 'services/rest/mailbox-info-service';
import {HEADER_V2_JSON} from 'services/rest-client';

describe('MailboxInfoService', function () {
  let restClient = {basePath: 'basepath'};
  let mailboxInfoService = new MailboxInfoService(restClient);
  let accountName = 'testMail@domain.com';

  describe('getUnreadMessagesCount', () => {
    it('should call correct rest service with correct data', () => {
      restClient.get = sinon.spy();
      mailboxInfoService.getUnreadMessagesCount(accountName, {skipInterceptor: true});
      expect(restClient.get.calledOnce);
      expect(restClient.get.getCall(0).args[0]).to.equal('/mailbox/' + accountName + '/unread');

      let headers = restClient.get.getCall(0).args[1].headers;
      expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
      expect(restClient.get.getCall(0).args[1].skipInterceptor).to.be.true;
    });
  });
});