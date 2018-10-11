import {ShareFolderService} from 'services/rest/share-folder-service';

describe('ShareFolderService', () => {
  let restService = {
    post: sinon.spy()
  };

  it('should call rest service with proper parameters', () => {
    let shareFolderService = new ShareFolderService(restService);
    let expectedMail = 'test-email@domain.com';
    shareFolderService.mountObjectShareFolder(expectedMail);
    let actualArgs = restService.post.args[0];
    expect(actualArgs[0]).to.equal(`/mailbox/${expectedMail}/mount`);
  });

});