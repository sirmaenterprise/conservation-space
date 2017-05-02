import {ObjectBrowserRestService} from 'services/rest/object-browser-service';

describe('ObjectBrowserService', function () {
  it('should adapt the parameters to satisfy the contract of the remote service', function () {

    const ID = 'current-node';
    const CURRENT_PATH = 'root-node/current-node';

    var restClientStub = {};
    restClientStub.get = sinon.stub();
    restClientStub.get.onCall(0).returns(Promise.resolve({}));

    var objectBrowserService = new ObjectBrowserRestService(restClientStub);

    var result = objectBrowserService.getChildNodes(CURRENT_PATH, {
      id: ID,
      selectable: true,
      clickableLinks: true,
      openInNewWindow: false
    });

    var callParams = restClientStub.get.getCall(0).args[1].params;
    expect(callParams.currId).to.equal(ID);
    expect(callParams.node).to.equal(CURRENT_PATH);
    expect(callParams.allowSelection).to.be.true;
    expect(callParams.clickableLinks).to.be.true;
    expect(callParams.clickOpenWindow).to.be.false;
  });
});
