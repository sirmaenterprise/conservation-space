import {ClassIconsUpload, WRONG_FILE_TYPE, MAX_SIZE_EXCEEDED} from 'administration/class-icons-upload/class-icons-uploader';

describe('ClassIconsUpload ', () => {
  let classIconsUpload = {};

  let translateService = {
    translateInstantWithInterpolation: sinon.spy()
  };

  let timeout = (callback)=> {
    callback();
  };

  let element = {
    find: ()=> {

    }
  };

  beforeEach(() => {
    classIconsUpload = new ClassIconsUpload(element, translateService, timeout);
    classIconsUpload.config = {
      maxFileSize: 500
    };
  });

  it('should remove selected icon', ()=> {
    classIconsUpload.checkMandatoryIcons = sinon.spy();
    classIconsUpload.icons = [{file: 'file'}];
    classIconsUpload.removeIcon(0);
    expect(classIconsUpload.icons[0].file).to.be.undefined;
    expect(classIconsUpload.checkMandatoryIcons.called).to.be.true;
  });

  it('should open the input', ()=> {
    var inputs = [{click: sinon.spy()}];
    var stub = sinon.stub(classIconsUpload.element, 'find');
    stub.withArgs('.icon-upload-field').returns(inputs);
    classIconsUpload.openInput(0);
    expect(inputs[0].click.called).to.be.true;
  });

  it('should show error when non image is selected', ()=> {
    var event = {
      target: {
        files: [{type: 'text/html'}]
      }
    };
    var indexStub = {
      index: ()=> {
        return 0;
      }
    };
    classIconsUpload.removeIcon = sinon.spy();
    var stub = sinon.stub($.prototype, 'index').returns(0);

    classIconsUpload.handleChangeIconEvent(event);

    expect(classIconsUpload.translateService.translateInstantWithInterpolation.called).to.be.true;
    expect(classIconsUpload.translateService.translateInstantWithInterpolation.args[0][0]).to.equal(WRONG_FILE_TYPE);
    stub.restore();
    classIconsUpload.translateService.translateInstantWithInterpolation.reset();
  });

  it('should show error when file with size bigger than the allowed is selected', ()=> {
    var event = {
      target: {
        files: [{size: 1000}]
      }
    };
    var indexStub = {
      index: ()=> {
        return 0;
      }
    };
    classIconsUpload.removeIcon = sinon.spy();
    var stub = sinon.stub($.prototype, 'index').returns(0);

    classIconsUpload.handleChangeIconEvent(event);

    expect(classIconsUpload.translateService.translateInstantWithInterpolation.called).to.be.true;
    expect(classIconsUpload.translateService.translateInstantWithInterpolation.args[0][0]).to.equal(MAX_SIZE_EXCEEDED);
    stub.restore();
    classIconsUpload.translateService.translateInstantWithInterpolation.reset();
  });

  it('should handle the conversion of the image to base64 format', ()=> {
    classIconsUpload.lastSelectedIndex = 0;
    classIconsUpload.icons = [{}];
    var event = {
      target: {
        result: 'icon'
      }
    };
    classIconsUpload.checkMandatoryIcons = sinon.spy();
    classIconsUpload.handleFileConversion(event);
    expect(classIconsUpload.icons[0].file).to.not.be.undefined;
    expect(classIconsUpload.checkMandatoryIcons.called).to.be.true;
  });

});