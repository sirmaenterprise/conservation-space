import {LabelRestService} from 'services/rest/label-service';

describe.skip('LabelRestService', function() {

	var restClient = {};
	restClient.get = sinon.spy();
	var labelRestService = new LabelRestService(restClient);

	it('should perform get request with proper arguments ', function() {
		labelRestService.getLabels('en');
		expect(restClient.get.calledOnce);
		expect(restClient.get.getCall(0).args[0]).to.equal('/label?lang=en');
	});
});

describe('LabelRestService', function () {
  let restClient = {basePath: 'basepath'};
  let labelRestService = new LabelRestService(restClient);

  it('should properly remove empty labels & prepare provided languages', function() {
    let languages = {
      en: {
        'label1': 'label1',
        'label2': ''
      },
      bg: {
        'label3': 'label3',
        'label4': ''
      }
    };

    labelRestService.clearEmptyTranslationLabels(languages.en);
    expect(languages.en.label1).to.exist;
    expect(languages.en.label2).to.not.exist;

    labelRestService.clearEmptyTranslationLabels(languages.bg);
    expect(languages.bg.label3).to.exist;
    expect(languages.bg.label4).to.not.exist;
  });

  it('should perform request for multiple labels with proper arguments ', () => {
    restClient.post = sinon.spy();
    let labels = ['label1', 'label2'];

    labelRestService.getDefinitionLabels(labels);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/label/multi');
    expect(restClient.post.getCall(0).args[1]).to.eql(labels);
  });
});
