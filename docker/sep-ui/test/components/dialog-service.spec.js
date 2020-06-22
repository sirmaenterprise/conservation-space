import {DialogService} from 'components/dialog/dialog-service';

describe('DialogService', () => {
  describe('configureBackdrop()', () => {
    var spyBackdrop = sinon.spy(DialogService, 'configureBackdrop');
    beforeEach(() => {
      spyBackdrop.reset();
    });

    it('should set "static" as default if none provided', () => {

      let compiled = {};
      DialogService.configureBackdrop(compiled, {});
      expect(spyBackdrop.returned('static')).to.be.true;
    });

    it('should set modal backdrop config when provided', () => {
      var compiled = {};
      let dialogConfig = {
        modeless: false,
        backdrop: true
      };
      DialogService.configureBackdrop(compiled, dialogConfig);
      expect(spyBackdrop.returned('true'));
    });
  });

  it('should return the dialog header into the viewport when it is under the viewport', () => {
    let modalDialogElement = {
      offset: () => {},
      find: (selector) => {
        return {
          width: () => 200,
          height: () => 200
        };
      }
    };
    DialogService.getWindowHeight = () => 800;
    DialogService.getWindowWidth = () => 800;
    DialogService.getWindowPositionOnScreen = () => {
      return {top: 0, bottom: 800}
    };
    let offset = {
      top: 798, left: 60
    };
    sinon.spy(modalDialogElement, 'offset');
    DialogService.dragStop(modalDialogElement, offset);
    // first two are used to get the top and left args.
    expect(modalDialogElement.offset.getCall(0).args).to.eql([{top: 600, left: 60}]);
  });

  it('should return the dialog into the viewport when it is above the viewport', () => {
    let modalDialogElement = {
      offset: () => {},
      find: (selector) => {
        return {
          width: () => 200,
          height: () => 200
        };
      }
    };
    DialogService.getWindowHeight = () => 800;
    DialogService.getWindowWidth = () => 800;
    DialogService.getWindowPositionOnScreen = () => {
      return {top: 0, bottom: 800}
    };
    let offset = {
      top: -150, left: 60
    };
    sinon.spy(modalDialogElement, 'offset');
    DialogService.dragStop(modalDialogElement, offset);
    expect(modalDialogElement.offset.getCall(0).args).to.eql([{top: 0, left: 60}]);
  });

  it('should return dialog into the viewport when it is on the left of the viewport', () => {
    let modalDialogElement = {
      offset: () => {},
      find: (selector) => {
        return {
          width: () => 200,
          height: () => 200
        };
      }
    };
    DialogService.getWindowHeight = () => 800;
    DialogService.getWindowWidth = () => 800;
    DialogService.getWindowPositionOnScreen = () => {
      return {top: 0, bottom: 800}
    };
    let offset = {
      top: 150, left: 690
    };
    sinon.spy(modalDialogElement, 'offset');
    DialogService.dragStop(modalDialogElement, offset);
    expect(modalDialogElement.offset.getCall(0).args).to.eql([{top: 150, left: 600}]);
  });

  it('shoudl return dialog into the viewport when it is on the right of the viewport', () => {
    let modalDialogElement = {
      offset: () => {},
      find: (selector) => {
        return {
          width: () => 200,
          height: () => 300
        };
      }
    };
    DialogService.getWindowHeight = () => 800;
    DialogService.getWindowWidth = () => 800;
    DialogService.getWindowPositionOnScreen = () => {
      return {top: 0, bottom: 800}
    };
    let offset = {
      top: 150, left: -100
    };
    sinon.spy(modalDialogElement, 'offset');
    DialogService.dragStop(modalDialogElement, offset);
    expect(modalDialogElement.offset.getCall(0).args).to.eql([{top: 150, left: 0}]);
  });

  it('onClose should call configured onClose function if any', () => {
    let dialogConfig = {
      onClose: sinon.spy()
    };
    DialogService.onClose(dialogConfig);
    expect(dialogConfig.onClose.callCount).to.equals(1);
  });
});
