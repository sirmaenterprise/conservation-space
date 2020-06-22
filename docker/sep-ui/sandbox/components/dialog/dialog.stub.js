import {Component, View, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {DatetimePicker} from 'components/datetimepicker/datetimepicker';
import {ContextualHelp} from 'components/help/contextual-help';
import 'style/bootstrap.css!';
import dialogTemplateStub from 'dialog-template!text';

@Component({
  selector: 'seip-dialog-stub'
})
@View({
  template: dialogTemplateStub
})
@Inject(DialogService)
export class Dialog {
  constructor(dialogService) {
    this.dialogService = dialogService;
  }

  error() {
    this.dialogService.error('Error message');
  }

  confirm() {
    this.dialogService.confirmation('Confirmation message');
  }

  notify() {
    this.dialogService.notification('Notification message');
  }

  customComponentBySelector() {
    let dialogConfig = {
      header: 'Custom component dialog',
      showClose: true,
      buttons: [
        {id: 'OK', label: 'Confirm', cls: 'btn-primary'},
        {id: 'CANCEL', label: 'Cancel', dismiss: true}
      ],
      onButtonClick: (buttonId)=> {
        $('.info-span').text(buttonId);
        dialogConfig.dismiss();
      }
    };
    this.dialogService.create('seip-datetime-picker', {config: {hideDate: true}}, dialogConfig, {id: 'datetime-picker-1'});
  }

  customComponentByComponent() {
    let dialogConfig = {
      header: 'Custom component dialog',
      showClose: true,
      buttons: [
        {id: 'CLOSE', label: 'Close', cls: 'btn-primary', dismiss: true}
      ]
    };
    this.dialogService.create(DatetimePicker, {config: {hideTime: true}}, dialogConfig, {id: 'datetime-picker-2'});
  }

  noHeaderDialog() {
    let dialogConfig = {
      buttons: [
        {id: 'CLOSE', label: 'Close', cls: 'btn-primary', dismiss: true}
      ]
    };
    this.dialogService.create(DatetimePicker, {config: {hideTime: true}}, dialogConfig, {id: 'datetime-picker-2'});
  }

  openModelessDialog() {
    let dialogConfig = {
      header: 'Modeless dialog',
      showClose: true,
      modeless: true,
      buttons: [
        {id: 'CLOSE', label: 'Close', cls: 'btn-primary', dismiss: true}
      ]
    };
    this.dialogService.create('seip-datetime-picker', {config: {hideDate: true}}, dialogConfig);
  }

  openDialogWithHelp() {
    let dialogConfig = {
      header: 'Dialog with a contextual help',
      helpTarget: 'existing-target',
      showClose: true,
      buttons: [
        {id: 'CLOSE', label: 'Close', cls: 'btn-primary', dismiss: true}
      ]
    };
    this.dialogService.create('seip-datetime-picker', {config: {hideDate: true}}, dialogConfig);
  }

  openDialogWithWarning() {
    let dialogConfig = {
      header: 'Dialog with a warning',
      warningMessage: 'This is a warning message',
      warningPopover: {
        body: 'This is a popover body message',
        title: 'This is a popover title'
      },
      showClose: true,
      buttons: [
        {id: 'CLOSE', label: 'Close', cls: 'btn-primary', dismiss: true}
      ]
    };
    this.dialogService.create('seip-datetime-picker', {config: {hideDate: true}}, dialogConfig);
  }
}
