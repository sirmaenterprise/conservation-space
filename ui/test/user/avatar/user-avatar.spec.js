import {UserAvatar} from 'user/avatar/user-avatar';

class UserAvatarStub extends UserAvatar {
  constructor() {
    super({
      getToken: () => 'test-token',
    });
  }
}

describe('UserAvatar', function() {

  it('should set 32 as default avatar size', function() {
    expect(new UserAvatarStub().size).to.eq(32);
  });

  describe('avatarUrl', function() {

    it('should construct user avatar url', function() {
      UserAvatarStub.prototype.user = {id: 123};
      expect(new UserAvatarStub().avatarUrl).to.eq('/remote/api/thumbnails/123?jwt=test-token');
    });
  });
});