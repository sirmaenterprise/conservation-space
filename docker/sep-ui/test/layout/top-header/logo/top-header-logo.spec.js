import {TopHeaderLogo} from 'layout/top-header/logo/top-header-logo';

describe("Top Header Logo", ()=> {

  describe("Path to logo", ()=> {
    it("should point to images/logo.png", ()=> {
      expect(new TopHeaderLogo(stubConfiguration()).logo).to.equal("logo.png");
    });
  });

  describe("State test", ()=> {
    it('should be "userDashboard"', ()=> {
      expect(new TopHeaderLogo(stubConfiguration()).state).to.equal('userDashboard');
    })
  })
});

function stubConfiguration() {
  var stub = sinon.stub();
  stub.withArgs('application.logo.image.path').returns('logo.png');

  return {
    get: stub
  };
}