import {EmailAccountDelegationService} from 'services/rest/email-account-delegation-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {HEADER_V2_JSON} from 'services/rest-client';

describe('EmailAccountDelegationService', function () {
  let restClient = {basePath: 'basepath'};
  let translateServiceStub = stub(TranslateService);
  let emailDistributionService = new EmailAccountDelegationService(restClient, PromiseStub, translateServiceStub);
  let instanceId = 'emf:123456';
  let targetMail = 'testMail@domain.com';

  describe('delegateRights', () => {
    it('should call correct rest service with correct data', () => {
      restClient.patch = sinon.spy();
      emailDistributionService.delegateRights(targetMail, instanceId);
      expect(restClient.patch.calledOnce);
      expect(restClient.patch.getCall(0).args[0]).to.equal('/instances/' + instanceId + '/email-account-delegate-permission');
      let headers = restClient.patch.getCall(0).args[2].headers;
      let params = restClient.patch.getCall(0).args[2].params;
      expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
      expect(params.target).to.equal(targetMail);
    });

    it('should not call service if required arguments are not provided', () => {
      restClient.patch = sinon.spy();
      let data = [
        [],
        [targetMail, undefined],
        [undefined, instanceId],
        [undefined, undefined]
      ];
      data.forEach((args) => {
        emailDistributionService.delegateRights.apply(emailDistributionService, args);
        expect(restClient.patch.callCount).to.equal(0);
      });
    });
  });

  describe('removeRights', () => {
    it('should call correct rest service with correct data', () => {
      restClient.patch = sinon.spy();
      emailDistributionService.removeRights(targetMail, instanceId);
      expect(restClient.patch.calledOnce);
      expect(restClient.patch.getCall(0).args[0]).to.equal('/instances/' + instanceId + '/email-account-remove-permission');
      let headers = restClient.patch.getCall(0).args[2].headers;
      let params = restClient.patch.getCall(0).args[2].params;
      expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
      expect(params.target).to.equal(targetMail);
    });

    it('should not call service if required arguments are not provided', () => {
      restClient.patch = sinon.spy();
      let data = [
        [],
        [targetMail, undefined],
        [undefined, instanceId],
        [undefined, undefined]
      ];
      data.forEach((args) => {
        emailDistributionService.delegateRights.apply(emailDistributionService, args);
        expect(restClient.patch.callCount).to.equal(0);
      });
    });
  });

  describe('getEmailAccountAttributes', () => {
    it('should call correct rest service with correct data', () => {
      restClient.get = sinon.spy();
      emailDistributionService.getEmailAccountAttributes(targetMail);
      expect(restClient.get.calledOnce);
      expect(restClient.get.getCall(0).args[0]).to.equal('/instances/' + targetMail + '/email-account-attributes');
      let headers = restClient.get.getCall(0).args[2].headers;
      expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
    });
  });


});