import {PickerService} from 'services/picker/picker-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

/**
 * Default picker service stub.
 */
export function stubPickerService(selectedItems = []) {
  var pickerService = stub(PickerService);
  pickerService.configureAndOpen = sinon.spy(() => {
    return PromiseStub.resolve(selectedItems);
  });
  pickerService.open = sinon.spy(() => {
    return PromiseStub.resolve(selectedItems);
  });
  return pickerService;
}