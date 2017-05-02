import {IdentityRestService} from 'services/identity/identity-service';

var expect = chai.expect;

describe('Tests for the Identity REST service client', function() {
	it('IdentityRestService.login() should call RestClient with url containing the username and password', function() {
		var restClient = {};
		var mockGet = sinon.spy();
		restClient.get = mockGet;
		
		var identityService = new IdentityRestService(restClient);
		identityService.login('admin','123456');
		
		expect(mockGet.calledOnce);
		
		var mockCallArgs = mockGet.getCall(0).args[0];
		expect(mockCallArgs).to.contain('admin');
		expect(mockCallArgs).to.contain('123456');
		
	});
});