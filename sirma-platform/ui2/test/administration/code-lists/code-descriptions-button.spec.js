import {CodeDescriptionsButton} from 'administration/code-lists/manage/code-descriptions-button';
import {CodeDescriptions} from 'administration/code-lists/manage/code-descriptions';
import {DialogService} from 'components/dialog/dialog-service';
import {PREVIEW, EDIT} from 'administration/code-lists/manage/code-manage-modes';
import {stub} from 'test/test-utils';

describe('CodeDescriptionsButton', () => {

  let descriptionsButton;
  beforeEach(() => {
    descriptionsButton = new CodeDescriptionsButton(stub(DialogService));
    descriptionsButton.mode = PREVIEW;
    descriptionsButton.onClose = sinon.spy();
    descriptionsButton.onChange = sinon.spy();
  });

  describe('openDescriptions()', () => {
    beforeEach(() => {
      descriptionsButton.code = {
        id: '1',
        descriptions: {
          'EN': {
            name: 'One'
          }
        },
        description: {name: 'One'}
      };
    });

    it('should open a dialog for listing the code\'s descriptions', () => {
      descriptionsButton.openDescriptions();
      expect(descriptionsButton.dialogService.create.calledOnce).to.be.true;
      expect(descriptionsButton.dialogService.create.calledWith(CodeDescriptions)).to.be.true;
    });

    it('should supply the dialog with proper component configurations', () => {
      descriptionsButton.openDescriptions();
      let componentConfig = descriptionsButton.dialogService.create.getCall(0).args[1];
      expect(componentConfig.descriptions).to.deep.equal(descriptionsButton.code.descriptions);
      expect(componentConfig.mode).to.equal(PREVIEW);
      expect(componentConfig.onChange).to.exist;
    });

    it('should provide a proper callback with component configurations', () => {
      descriptionsButton.openDescriptions();
      let componentConfig = descriptionsButton.dialogService.create.getCall(0).args[1];
      componentConfig.onChange();

      expect(descriptionsButton.onChange.calledOnce).to.be.true;
    });

    it('should supply the dialog with component configurations based on own configuration', () => {
      descriptionsButton.mode = EDIT;
      descriptionsButton.openDescriptions();
      let componentConfig = descriptionsButton.dialogService.create.getCall(0).args[1];
      expect(componentConfig.mode).to.equal(EDIT);
    });

    it('should supply the dialog with proper dialog configurations', () => {
      descriptionsButton.openDescriptions();
      let dialogConfig = descriptionsButton.dialogService.create.getCall(0).args[2];
      expect(dialogConfig.largeModal).to.be.true;
      expect(dialogConfig.header).to.equal('1 One');
      expect(dialogConfig.helpTarget).to.exist;

      // Should dismiss the dialog
      let dialogConfigSpy = {dismiss: sinon.spy()};
      dialogConfig.onButtonClick({}, {}, dialogConfigSpy);
      expect(dialogConfigSpy.dismiss.calledOnce).to.be.true;
    });

    it('should notify that the dialog is closed', () => {
      descriptionsButton.openDescriptions();
      let dialogConfig = descriptionsButton.dialogService.create.getCall(0).args[2];
      dialogConfig.onButtonClick({}, {}, {dismiss: sinon.spy()});
      expect(descriptionsButton.onClose.calledOnce).to.be.true;
    });
  });

});