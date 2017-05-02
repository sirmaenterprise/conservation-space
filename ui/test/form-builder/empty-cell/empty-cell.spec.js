import {EmptyCell} from 'form-builder/empty-cell/empty-cell';

describe('Empty Cell', ()=> {
  let fakeElement = {
    addClass: ()=> {},
    removeClass: ()=> {}
  };
  it('should initialize empty cell and set its visibility depending on its rendered status', ()=> {
    let stub = sinon.spy(fakeElement,'removeClass');
    EmptyCell.prototype.fieldViewModel = {rendered: true};
    let emptyCell = new EmptyCell(fakeElement);
    emptyCell.ngOnInit();
    expect(stub.calledOnce).to.be.true;
    expect(stub.calledWith('hidden')).to.be.true;
  });

});