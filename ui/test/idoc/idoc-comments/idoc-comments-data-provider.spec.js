import {IdocCommentsDataProvider} from 'idoc/idoc-comments/idoc-comments-data-provider';

describe('Idoc comments data provider test', function () {
  it('should test the loading of all the comments', function () {
    let commentsRestService = {loadComments: sinon.spy()};
    let dataProvider = new IdocCommentsDataProvider(commentsRestService);

    dataProvider.loadComments('instanceId', 'tabId');
    expect(commentsRestService.loadComments.getCall(0).args[1]).to.equal('tabId');
    expect(commentsRestService.loadComments.getCall(0).args[0]).to.equal('instanceId');
  });
});