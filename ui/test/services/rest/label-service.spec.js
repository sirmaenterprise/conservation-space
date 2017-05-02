import {LabelRestService} from 'services/rest/label-service';

describe.skip('Tests for label service rest client', function() {

	var restClient = {};
	restClient.get = sinon.spy();
	var labelRestService = new LabelRestService(restClient);

	it('Test if label service perform get request with proper arguments ', function() {
		labelRestService.getLabels('en');
		expect(restClient.get.calledOnce);
		expect(restClient.get.getCall(0).args[0]).to.equal('/label?lang=en');
	});
});
