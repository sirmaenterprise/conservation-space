import _ from 'lodash';
import {Select} from 'components/select/select';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

import {SelectMocks} from './select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('Select component', () => {

  it('should correctly update listeners on $element', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());

    let listeners = {
      'event_1': () => {
        return 'listener 1';
      },
      'event_2': () => {
        return 'listener 2';
      },
      'event_3': 'Wrong listener. Must be a function.'
    };
    select.appendListeners(listeners);

    expect(select.$element.listeners).to.have.property('event_1');
    assert.isFunction(select.$element.listeners['event_1']);
    expect(select.$element.listeners['event_1']()).to.equal('listener 1');

    expect(select.$element.listeners).to.have.property('event_2');
    assert.isFunction(select.$element.listeners['event_2']);
    expect(select.$element.listeners['event_2']()).to.equal('listener 2');

    expect(select.$element.listeners).to.not.have.property('event_3');
  });

  it('should correctly compare strings', () => {
    expect(Select.compareValues('Same string', 'Same string')).to.be.true;
    expect(Select.compareValues('String 1', 'String 2')).to.be.false;
    expect(Select.compareValues('Same string', ['Same string'])).to.be.false;
  });

  it('should correctly compare arrays', () => {
    expect(Select.compareValues(['Same string'], ['Same string'])).to.be.true;
    expect(Select.compareValues(['String 1'], ['String 2'])).to.be.false;
    expect(Select.compareValues(['Same string', 'Another string'], ['Same string'])).to.be.false;
  });

  it('should correctly compare undefined', () => {
    expect(Select.compareValues(undefined, 'Same string')).to.be.false;
    expect(Select.compareValues(undefined, ['Same string'])).to.be.false;
    expect(Select.compareValues('Same string', undefined)).to.be.false;
    expect(Select.compareValues(['Same string'], undefined)).to.be.false;
    expect(Select.compareValues(undefined, undefined)).to.be.true;
  });

  it('should allow render function overriding via configuration.', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config = {
      escapeMarkup: (item) => {
        return item.text + '1';
      },
      formatResult: (item) => {
        return item.text + '2'
      },
      formatSelection: (item) => {
        return item.text + '3';
      }
    };
    select.createActualConfig();

    let item = {
      id: 'value',
      text: 'text'
    };

    assert.isDefined(select.actualConfig.escapeMarkup);
    assert.equal(select.actualConfig.escapeMarkup(item), 'text1');

    assert.isDefined(select.actualConfig.templateResult);
    assert.equal(select.actualConfig.templateResult(item), '<span data-value="value">text2</span>');

    assert.isDefined(select.actualConfig.templateSelection);
    assert.equal(select.actualConfig.templateSelection(item), 'text3');
  });

  it('should assign default template and result selection handlers that escapes html tags', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.createActualConfig();
    let item = {
      id: 'value',
      text: '<tag>'
    };
    expect(select.actualConfig.templateResult(item)).to.equal('<span data-value="value">&lt;tag&gt;</span>');
    expect(select.actualConfig.templateSelection(item)).to.equal('&lt;tag&gt;');
  });

  it('should not assign ajax configuration without data loader', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    assert.isUndefined(select.actualConfig.ajax);
  });

  it('should assign delay value when making ajax requests', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config.delay = 123;
    select.config.dataLoader = {};
    select.createActualConfig();
    assert.equal(select.actualConfig.ajax.delay, 123);
  });

  it('should correctly call the data loaded with parameters', () => {
    let returnSpy = sinon.spy();
    returnSpy.then = sinon.spy();
    let dataLoader = sinon.stub();
    dataLoader.returns(returnSpy);

    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config.dataLoader = dataLoader;
    select.createActualConfig();
    select.actualConfig.ajax.transport('params', 'success', 'failure');

    expect(dataLoader.calledOnce);
    expect(dataLoader.getCall(0).args[0]).to.equal('params');

    expect(returnSpy.then.calledOnce);
    expect(returnSpy.then.getCall(0).args[0]).to.equal('success');
    expect(returnSpy.then.getCall(0).args[1]).to.equal('failure');
  });

  it('should use default data converter if not specified', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config.dataLoader = function () {
    };
    select.createActualConfig();

    let data = select.actualConfig.ajax.processResults(['test']);
    expect(data).to.deep.equal({results: ['test']});
  });

  it('should return data if correctly mapped', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config.dataLoader = function () {
    };
    select.createActualConfig();

    var expected = {results: ['test']};
    let data = select.actualConfig.ajax.processResults(expected);
    expect(data).to.deep.equal(expected);
  });

  it('should call provided dataConverter', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    let spy = sinon.spy();
    select.config.dataLoader = function () {
    };
    select.config.dataConverter = function () {
      spy.apply(null, arguments);
      return arguments[0];
    };
    select.createActualConfig();

    select.actualConfig.ajax.processResults('test');
    expect(spy.calledOnce).to.be.true;
    expect(spy.getCall(0).args[0]).to.equal('test');
  });

  it('could be configured to show/hide search input', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config.hideSearchBox = true;
    select.createActualConfig();
    expect(select.actualConfig.minimumResultsForSearch).to.equal('Infinity');
  });

  it('could be configured to select items on menu closing', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.actualConfig = undefined;

    select.config.selectOnClose = true;
    select.createActualConfig();

    expect(select.actualConfig.selectOnClose).to.be.true;
  });

  it('could be configured to automatically expand it\'s dropdown menu', () => {
    let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
    select.config.dropdownAutoWidth = true;
    select.createActualConfig();
    expect(select.actualConfig.dropdownAutoWidth).to.be.true
  });

  describe('templateResult()', () => {
    it('should wrap result into a span', () => {
      let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      let item = {
        id: 'itemId',
        text: 'itemText'
      };
      expect(select.templateResult(item)).to.equal('<span data-value="itemId">itemText</span>');
    });

    it('should format result before wrapping it into a span', () => {
      let select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      let item = {
        id: 'itemId',
        text: 'itemText'
      };
      let formatResult = (item) => {
        return `-${item.text}-`;
      };
      expect(select.templateResult(item, formatResult)).to.equal('<span data-value="itemId">-itemText-</span>');
    })
  });

  describe('bindToModel()', () => {

    it('should create two-way binding if ngModel is present', () => {
      var scopeMock = {
        $watch: sinon.spy()
      };
      let select = new Select(SelectMocks.mockElement(), scopeMock, SelectMocks.mockTimeout());
      expect(scopeMock.$watch.calledOnce).to.be.true;
    });

    it('should not create two-way binding if ngModel is missing', () => {
      var scopeMock = {
        $watch: sinon.spy()
      };
      var elementMock = SelectMocks.mockElement();
      elementMock.controller = () => {
      };
      let select = new Select(elementMock, scopeMock, SelectMocks.mockTimeout());
      expect(select.$element.change.calledOnce).to.be.false;
      expect(scopeMock.$watch.calledOnce).to.be.false;
    });

    it('should set new value on model change', () => {
      var scopeMock = mock$scope();
      var elementMock = SelectMocks.mockElement();
      let select = new Select(elementMock, scopeMock, SelectMocks.mockTimeout());
      select.$element.val = sinon.spy((param) => {
        if (!param) {
          return undefined;
        }
      });
      select.ngModel.$viewValue = 'newValue';
      scopeMock.$digest();
      expect(select.$element.val.getCall(1).args[0], 'element.val() have to be called with newValue parameter').to.equal('newValue');
      expect(select.$element.trigger.getCall(0).args[0], 'Have to be triggered change event on select element').to.equal('change');
    });

    it('should not set new value on model change if it is the same', () => {
      var scopeMock = mock$scope();
      var elementMock = SelectMocks.mockElement();
      let select = new Select(elementMock, scopeMock, SelectMocks.mockTimeout());
      select.$element.val = sinon.spy(() => {
        return 'newValue';
      });
      select.ngModel.$viewValue = 'newValue';
      scopeMock.$digest();
      expect(select.$element.val.calledOnce).to.be.true;
    });

    it('should not set new value on model change if it is undefined', () => {
      var scopeMock = mock$scope();
      var elementMock = SelectMocks.mockElement();
      let select = new Select(elementMock, scopeMock, SelectMocks.mockTimeout());
      select.$element.val = sinon.spy();
      select.ngModel.$viewValue = undefined;
      scopeMock.$digest();
      expect(select.$element.val.calledOnce).to.be.false;
    });
  });

  describe('initSelect()', () => {
    var select;
    var scopeMock;

    beforeEach(function () {
      scopeMock = mock$scope();
      select = new Select(SelectMocks.mockElement(), scopeMock, SelectMocks.mockTimeout());
    });

    it('should register an event to prevent auto opening when deselecting items', () => {
      var onSpy = select.$element.on;

      // first two calls are not for this test
      expect(onSpy.callCount).to.equal(4);
      expect(onSpy.getCall(2).args[0]).to.eq('select2:opening');
      expect(_.isFunction(onSpy.getCall(2).args[1])).to.be.true;
      expect(onSpy.getCall(3).args[0]).to.eq('select2:unselecting');
      expect(_.isFunction(onSpy.getCall(3).args[1])).to.be.true;
    });

    it('should register an event to ensure the element is focused in IE11 and tags are set to true', () => {
      select.config = {
        tags: true
      };
      var stub = sinon.stub(NavigatorAdapter, 'isInternetExplorer', sinon.spy(() => {
        return true;
      }));
      select.$element.on.reset();

      select.ensureFocusOnTyping(select.$element);
      expect(stub.called).to.be.true;
      expect(select.$element.on.calledOnce).to.be.true;
      expect(select.$element.on.getCall(0).args[0]).to.equal('DOMNodeInserted');

      var callback = select.$element.on.getCall(0).args[1];
      callback();
      expect(select.$element.focus.called).to.be.true;

      stub.restore();
    });

    it('should register an event to ensure the element is focused in IE11 and tags are set to false', () => {
      select.config = {
        tags: false
      };
      var stub = sinon.stub(NavigatorAdapter, 'isInternetExplorer', sinon.spy(() => {
        return true;
      }));
      select.$element.on.reset();

      select.ensureFocusOnTyping(select.$element);
      expect(stub.called).to.be.true;
      expect(select.$element.on.calledOnce).to.be.true;
      expect(select.$element.on.getCall(0).args[0]).to.equal('DOMNodeInserted');

      var callback = select.$element.on.getCall(0).args[1];
      callback();
      expect(select.$element.focus.called).to.be.true;

      stub.restore();
    });

    it('should not register an event to ensure the element is focused in IE11 when tags are undefined', () => {
      var stub = sinon.stub(NavigatorAdapter, 'isInternetExplorer', sinon.spy(() => {
        return true;
      }));
      select.$element.on.reset();

      select.ensureFocusOnTyping(select.$element);
      expect(stub.called).to.be.true;
      expect(select.$element.on.called).to.be.false;

      stub.restore();
    });

    it('should not register an event to ensure the element is focused if it is not IE11', () => {
      select.config = {
        tags: true
      };
      var stub = sinon.stub(NavigatorAdapter, 'isInternetExplorer', sinon.spy(() => {
        return false;
      }));
      select.$element.on.reset();

      select.ensureFocusOnTyping(select.$element);
      expect(stub.called).to.be.true;
      expect(select.$element.on.called).to.be.false;

      stub.restore();
    });
  });

  describe('autoExpandDropdownMenu()', () => {
    var select;
    beforeEach(() => select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout()));

    it('should force dropdown menu expansion under Safari if configured to do so', () => {
      var stub = sinon.stub(NavigatorAdapter, 'isSafari', () => true);
      select.config.dropdownAutoWidth = true;
      select.autoExpandDropdownMenu();
      expect(stub.calledOnce).to.be.true;
      expect(select.$element.select2.calledWith('open')).to.be.true;
      expect(select.$element.select2.calledWith('close')).to.be.true;
      stub.restore();
    });

    it('should not force dropdown menu expansion under Safari if not configured to do so', () => {
      var stub = sinon.stub(NavigatorAdapter, 'isSafari', () => true);
      select.config.dropdownAutoWidth = false;
      select.autoExpandDropdownMenu();
      expect(select.$element.select2.calledWith('open')).to.be.false;
      expect(select.$element.select2.calledWith('close')).to.be.false;
      stub.restore();
    });

    it('should not force dropdown menu expansion if not under Safari but configured to do so', () => {
      var stub = sinon.stub(NavigatorAdapter, 'isSafari', () => false);
      select.config.dropdownAutoWidth = true;
      select.autoExpandDropdownMenu();
      expect(select.$element.select2.calledWith('open')).to.be.false;
      expect(select.$element.select2.calledWith('close')).to.be.false;
      stub.restore();
    });
  });

  describe('ngOnInit()', () => {
    it('should not trigger change if there is a default value', () => {
      var elementMock = SelectMocks.mockElement();
      var select = new Select(elementMock, mock$scope(), SelectMocks.mockTimeout());
      select.config = {
        data: [],
        multiple: false,
        defaultToFirstValue: true,
        defaultValue: ''
      };
      elementMock.find = sinon.spy();
      select.ngOnInit();
      expect(elementMock.find.called).to.be.false;
    });
  });

  describe('handleMenuOpeningEvent(event)', function () {
    var event;
    beforeEach(function () {
      event = {
        preventDefault: sinon.spy()
      };
    });

    it('should not preventDefault if keepMenuClosed is false', function () {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.handleMenuOpeningEvent(event);

      expect(event.preventDefault.called).to.be.false;
    });

    it('should preventDefault if keepMenuClosed is true and set keepMenuClosed back to false', function () {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.keepMenuClosed = true;
      select.handleMenuOpeningEvent(event);

      expect(select.keepMenuClosed).to.be.false;
      expect(event.preventDefault.calledOnce).to.be.true;
    });
  });

  describe('handleUnselectingEvent(event)', function () {
    var jqIsStub;
    var validEvent = {
      params: {
        args: {
          originalEvent: {
            target: 1
          }
        }
      }
    };

    beforeEach(function () {
      jqIsStub = sinon.stub($.fn, 'is');
    });

    afterEach(function () {
      jqIsStub.restore();
    });

    it('should not do anything if we cannot get the original target', function () {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      var event = {};

      select.handleUnselectingEvent(event);
      expect(select.keepMenuClosed).to.be.undefined;
      expect(jqIsStub.called).to.be.false;

      event.params = {};
      select.handleUnselectingEvent(event);
      expect(select.keepMenuClosed).to.be.undefined;
      expect(jqIsStub.called).to.be.false;

      event.params.args = {};
      select.handleUnselectingEvent(event);
      expect(select.keepMenuClosed).to.be.undefined;
      expect(jqIsStub.called).to.be.false;

      event.params.args.originalEvent = {};
      select.handleUnselectingEvent(event);
      expect(select.keepMenuClosed).to.be.undefined;
      expect(jqIsStub.called).to.be.false;
    });

    it('should not do anything the original target is not what we expect', function () {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());

      jqIsStub.returns(false);
      select.handleUnselectingEvent(validEvent);
      expect(select.keepMenuClosed).to.be.undefined;
      expect(jqIsStub.calledOnce).to.be.true;
      expect(jqIsStub.getCall(0).args[0]).to.eq('.select2-selection__choice__remove');
    });

    it('should not do anything the original target is not what we expect', function () {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());

      jqIsStub.returns(true);
      select.handleUnselectingEvent(validEvent);
      expect(select.keepMenuClosed).to.be.true;
      expect(jqIsStub.calledOnce).to.be.true;
      expect(jqIsStub.getCall(0).args[0]).to.eq('.select2-selection__choice__remove');
    });
  });

  describe('enforceMinimumSelectionLength()', () => {
    it('should not register a minimum selection length listener if minimumSelectionLength is less than 1', () => {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.appendListeners = sinon.spy();

      select.enforceMinimumSelectionLength();
      expect(select.appendListeners.called).to.be.false;

      select.enforceMinimumSelectionLength(0);
      expect(select.appendListeners.called).to.be.false;
    });

    it('should not prevent deselecting items if the current amount is more than the configured limit', () => {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.appendListeners = sinon.spy();
      select.$element.val = () => {
        return ['1', '2'];
      };

      select.enforceMinimumSelectionLength(1);

      var listener = select.appendListeners.getCall(0).args[0];
      expect(listener['select2:unselecting']).to.exist;

      var eventSpy = {
        preventDefault: sinon.spy()
      };
      listener['select2:unselecting'](eventSpy);
      expect(eventSpy.preventDefault.called).to.be.false;
    });

    it('should prevent deselecting items if the current amount is equal to the configured limit', () => {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.appendListeners = sinon.spy();
      select.$element.val = () => {
        return ['1'];
      };

      select.enforceMinimumSelectionLength(1);

      var listener = select.appendListeners.getCall(0).args[0];
      var eventSpy = {
        preventDefault: sinon.spy()
      };
      listener['select2:unselecting'](eventSpy);
      expect(eventSpy.preventDefault.calledOnce).to.be.true;
    });
  });

  describe('isDisabled()', () => {
    it('should use the configuration function if provided', () => {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.config.disabled = false;
      select.config.isDisabled = () => {
        return true;
      };
      expect(select.isDisabled()).to.be.true;
    });

    it('should consider undefined as false if config.isDisabled() is misconfigured', () => {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.config.disabled = true;
      select.config.isDisabled = () => {};
      expect(select.isDisabled()).to.be.false;
    });

    it('should use the configuration property if no function is provided', () => {
      var select = new Select(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout());
      select.config.disabled = true;
      select.config.isDisabled = undefined;
      expect(select.isDisabled()).to.be.true;
    });
  });

});
