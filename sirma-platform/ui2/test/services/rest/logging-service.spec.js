import {LoggingRestService} from 'services/rest/logging-service';

describe('Tests for logging rest client', function() {

	var restClient = {};
	restClient.post = sinon.spy();
	var loggingRestClient = new LoggingRestService(restClient);
	
	it('Test if logMessage(message) actually performs post request', function() {
		loggingRestClient.logMessage('some error');
		expect(restClient.post.callCount).to.equal(1);
	});
	
	it('Test if logMessage(message) passes proper arguments to the rest client', function() {
		loggingRestClient.logMessage('some error');
		
		expect(restClient.post.getCall(0).args[0]).to.equal('/logger');
		expect(restClient.post.getCall(0).args[1]).to.equal('some error');
	});
});
