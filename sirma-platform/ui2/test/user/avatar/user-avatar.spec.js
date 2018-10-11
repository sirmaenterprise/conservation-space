import {UserAvatar} from 'user/avatar/user-avatar';

class UserAvatarStub extends UserAvatar {
  constructor() {
    super($('<div></div>'),
      {
        getToken: () => 'test-token'
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
      expect(userAvatar.avatarUrl).to.eq('/remote/api/thumbnails/123?jwt=test-token');
    });
  });
});
