import {InputFilter} from 'components/filter/input-filter';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {KEY_ENTER} from 'common/keys';

describe('Input Filter component', () => {

  it('should create two-way binding if ngModel is present', () => {
    let scope = mock$scope();
    scope.$watch = sinon.spy();

    let inputFilter = new InputFilter(scope, mockElement());
    expect(inputFilter.$scope.$watch.calledOnce).to.be.true;
  });

  it('should not create two-way binding if ngModel is missing', () => {
    let scope = mock$scope();
    scope.$watch = sinon.spy();

    let element = mockElement();
    element.controller = () => {};

    let inputFilter = new InputFilter(scope, element);
    expect(inputFilter.$element.input.calledOnce).to.be.false;
    expect(inputFilter.$scope.$watch.calledOnce).to.be.false;
  });

  it('should set new value on model change', () => {
    let inputFilter = new InputFilter(mock$scope(), mockElement('oldValue'));

    inputFilter.ngModel.$viewValue = 'newValue';
    inputFilter.$scope.$digest();
    expect(inputFilter.$element.val.getCall(1).args[0]).to.equal('newValue');
    expect(inputFilter.$element.trigger.getCall(0).args[0]).to.equal('input');
  });

  it('should not set new value on model change if it is the same', () => {
    let inputFilter = new InputFilter(mock$scope(), mockElement('newValue'));

    inputFilter.ngModel.$viewValue = 'newValue';
    inputFilter.$scope.$digest();
    expect(inputFilter.$element.val.calledOnce).to.be.true;
  });

  it('should not set new value on model change if it is undefined', () => {
    let inputFilter = new InputFilter(mock$scope(), mockElement());

    inputFilter.ngModel.$viewValue = undefined;
    inputFilter.$scope.$digest();
    expect(inputFilter.$element.val.calledOnce).to.be.false;
  });

  it('should not trigger onKeyPressed if the key pressed is not the specified one', () => {
    let event = {keyCode: KEY_ENTER % 13};
    let inputFilter = new InputFilter(mock$scope(), mockElement());
    inputFilter.config.filterKey = KEY_ENTER;
    inputFilter.onKeyPressed = sinon.spy();

    inputFilter.filterOnKey(event);
    expect(inputFilter.onKeyPressed.calledOnce).to.be.false;
  });

  it('should trigger onKeyPressed if the key pressed is the specified one', () => {
    let event = {keyCode: KEY_ENTER};
    let inputFilter = new InputFilter(mock$scope(), mockElement());
    inputFilter.onKeyPressed = sinon.spy();
    inputFilter.config.filterKey = KEY_ENTER;

    inputFilter.filterOnKey(event);
    expect(inputFilter.onKeyPressed.calledOnce).to.be.true;
  });
});

function mockElement(value) {
  return {
    find: () => {
      return {
        on: sinon.spy(),
        input: sinon.spy(),
        trigger: sinon.spy(),
        val: sinon.spy(() => {
          return value;
        })
      };
    },
    controller: () => {
      return {};
    }
  };
}
