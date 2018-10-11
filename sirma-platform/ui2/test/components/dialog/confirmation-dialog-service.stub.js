import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

/**
 * Stubs the confirmation dialog service for reusing across tests..
 */
export function stubConfirmationDialogService(confirm = true) {
  let serviceStub = stub(ConfirmationDialogService);
  serviceStub.confirm.returns(PromiseStub.promise((resolve, reject) => {
    if (confirm) {
      resolve();
    } else {
      reject();
    }
  }));
  return serviceStub;
}