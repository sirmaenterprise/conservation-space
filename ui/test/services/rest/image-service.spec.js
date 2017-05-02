import {ImageService} from 'services/rest/image-service';

describe('Tests for image service rest client', function () {
  let restClient = {basePath: 'basepath'};
  let imageService = new ImageService(restClient);

  it('Test if image service perform create request with proper arguments ', () => {
    restClient.post = sinon.spy();
    let data = {
      imageWidgetId: 'widgetId',
      selectedImageIds: []
    };
    imageService.createManifest(data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/image/manifest/create');
    expect(restClient.post.getCall(0).args[1].imageWidgetId).to.equal('widgetId');
    let headers = restClient.post.getCall(0).args[2].headers;
    expect(headers[Object.keys(headers)[0]]).to.equal('application/json');
  });

  it('Test if image service return proper get path ', () => {
    let manifestId = 'manifestId';
    restClient.get = sinon.spy();
    imageService.getManifest(manifestId);
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/image/manifest/manifestId');
  });

  it('Test if image service perform update request with proper arguments ', () => {
    restClient.post = sinon.spy();
    let data = {
      manifestId: 'manifestId',
      imageWidgetId: 'widgetId',
      selectedImageIds: []
    };
    imageService.updateManifest(data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/image/manifest/update');
    expect(restClient.post.getCall(0).args[1].manifestId).to.equal('manifestId');
    let headers = restClient.post.getCall(0).args[2].headers;
    expect(headers[Object.keys(headers)[0]]).to.equal('application/json');
  });

});
