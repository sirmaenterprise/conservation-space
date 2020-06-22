import {BrowseLibrariesAction} from 'layout/top-header/main-menu/quick-access/browse-libraries-action'

describe('Browse Libraries Action', function () {
  it('Should execute correcly', ()=> {
    let router = {
      navigate: sinon.spy()
    };
    let browseAction = new BrowseLibrariesAction(router);

    browseAction.execute({});
    expect(router.navigate.callCount).to.equal(1);
  })
});