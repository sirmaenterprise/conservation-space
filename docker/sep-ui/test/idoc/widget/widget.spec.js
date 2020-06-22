import {WidgetControl, WidgetRegistry} from 'idoc/widget/widget';
import _ from 'lodash';

describe('WidgetControl', function () {

  it('should remove the "config" attribute on empty config object', function () {
    var element = {};
    element.removeAttr = sinon.spy();

    var control = new WidgetControl(element);
    control.saveConfig({});

    expect(element.removeAttr.calledWith('config')).to.be.true;
  });

  it('should set the "config" attribute', function () {
    var element = {};
    element.attr = sinon.spy();

    var control = new WidgetControl(element);
    control.saveConfig({'test': '123'});

    expect(element.attr.calledOnce).to.be.true;
  });

  it('should properly read the "config" attribute', function () {
    var element = {};
    element.attr = sinon.spy();

    var control = new WidgetControl(element);
    control.getConfig();

    expect(element.attr.calledWith('config')).to.be.true;
  });

  it('should remove the "value" attribute when an empty value object is provided', function () {
    var element = {};
    element.removeAttr = sinon.spy();

    var control = new WidgetControl(element);
    control.saveValue({});

    expect(element.removeAttr.calledWith('value')).to.be.true;
  });

  it('should properly set the "value" attribute', function () {
    var element = {};
    element.attr = sinon.spy();

    var control = new WidgetControl(element);
    control.saveValue({'test': '123'});

    expect(element.attr.calledOnce).to.be.true;
  });

  it('should properly read the "value" attribute', function () {
    var element = {};
    element.attr = sinon.spy();

    var control = new WidgetControl(element);
    control.getValue();

    expect(element.attr.calledWith('value')).to.be.true;
  });

  it('should properly set "data" to a passed attribute', function () {
    var element = {};
    element.attr = sinon.spy();

    var control = new WidgetControl(element);
    control.storeDataInAttribute({'test': '123'}, 'test-attr');

    expect(element.attr.calledOnce).to.be.true;
  });

  it('should properly provide the widget name', function () {
    const WIDGET_NAME = 'devs-widget';

    var element = {
      attr: function (param) {
        if (param === 'widget') {
          return WIDGET_NAME;
        }
      }
    };

    var control = new WidgetControl(element);

    expect(control.getName()).to.equal(WIDGET_NAME);
  });

  it('should properly provide the widget ID', function () {
    const WIDGET_ID = '2389928-2332-3323-332323';

    var element = {
      attr: function (param) {
        if (param === 'id') {
          return WIDGET_ID;
        }
      }
    };

    var control = new WidgetControl(element);

    expect(control.getId()).to.equal(WIDGET_ID);
  });

  it('should properly restore saved config or value', function () {
    var control = new WidgetControl({});

    var object = {
      'name': 'Widget'
    };

    var serialized = control.serialize(object);

    var deserialized = control.deserialize(serialized);

    expect(deserialized.name).to.equal(object.name);
  });

  const WIDGET_NAME = 'devs-widget';

  PluginRegistry.add('idoc-widget', {
    'name': WIDGET_NAME,
    'class': 'widgets/devs/widget/DevsWidget',
    'config': 'widgets/devs/dev-widget-config/DevsWidgetConfig'
  });

  it('should read the widget definition from the WidgetRegistry', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    var definition = control.getDefinition();

    expect(definition.name).to.equal('devs-widget');
  });

  it('should properly provide the js module path', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    expect(control.getWidgetModule()).to.equal('widgets/devs/widget');
  });

  it('should provide the widget class', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    expect(control.getWidgetClass()).to.equal('DevsWidget');
  });

  it('should provide the widget selector', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    expect(control.getWidgetSelector()).to.equal('devs-widget');
  });

  it('should provide the js module path of the widget config component', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    expect(control.getConfigModule()).to.equal('widgets/devs/dev-widget-config');
  });

  it('should provide the widget configuration component class', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    expect(control.getConfigClass()).to.equal('DevsWidgetConfig');
  });

  it('should provide the selector of the widget configuration component', function () {
    var control = new WidgetControl({});

    control.getName = function () {
      return WIDGET_NAME;
    };

    expect(control.getConfigSelector()).to.equal('devs-widget-config');
  });
  it('should set the given data to the data-value attribute', function () {
    var control = new WidgetControl({});

    control.storeDataInAttribute = sinon.spy();
    let config = {
      saved: true
    };
    control.setDataValue(config);
    expect(control.storeDataInAttribute.callCount).to.equal(1);
    expect(control.storeDataInAttribute.args[0][0].saved).to.equal(true);
  });

  it('should return wrapping editor', () => {
    var control = new WidgetControl({});
    var editor = 'editor';
    _.set(control, 'baseWidget.$scope.editor', editor);
    expect(control.getEditor()).to.equals(editor);
  });
});
