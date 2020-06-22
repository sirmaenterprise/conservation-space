import {EmptyCell} from 'form-builder/empty-cell/empty-cell';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test-utils';
import {mockFormWrapper} from 'test/form-builder/form-wrapper-mock';

describe('Empty Cell', () => {

  let fakeElement = {
    addClass: () => {},
    removeClass: () => {}
  };

  it('should initialize empty cell and set its visibility depending on its rendered status', () => {
    let stub = sinon.spy(fakeElement,'removeClass');

    EmptyCell.prototype.formWrapper =  mockFormWrapper()
      .setFieldsMap({
        field1: {
          rendered: true
        }
      })
      .get();

    EmptyCell.prototype.identifier = 'field1';

    let emptyCell = new EmptyCell(fakeElement);
    emptyCell.ngOnInit();
    expect(stub.calledOnce).to.be.true;
    expect(stub.calledWith('hidden')).to.be.true;
  });

  describe('#ngAfterViewInit', () => {
    it('should not emit event by default to formWrapper', () => {
      let emptyCell = new EmptyCell(fakeElement);
      emptyCell.formEventEmitter = stub(EventEmitter);
      emptyCell.ngAfterViewInit();
      expect(emptyCell.formEventEmitter.publish.calledOnce).to.be.true;
    });
  });
});