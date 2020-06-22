import {UserAvatar} from 'user/avatar/user-avatar';
import {PromiseStub} from 'test/promise-stub';

class UserAvatarStub extends UserAvatar {
  constructor() {
    super($('<div></div>'),
      {
        getToken: () => PromiseStub.resolve('test-token')
      }
    );
  }

  ngOnInit() {
    this.user = {
      id: 123
    };
    super.ngOnInit();
  }
}

describe('UserAvatar', function () {

  it('should set 32 as default avatar size', function () {
    expect(new UserAvatarStub().size).to.eq(32);
  });

  describe('avatarUrl', function () {

    it('should construct user avatar url', function () {
      let userAvatar = new UserAvatarStub();
      userAvatar.ngOnInit();
      expect(userAvatar.avatarUrl).to.equal('/remote/api/thumbnails/123?jwt=test-token');
    });
  });
});
