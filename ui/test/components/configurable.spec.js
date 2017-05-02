import {Configurable} from 'components/configurable';

class ConfigurableStub extends Configurable {
  constructor(defaultConf) {
    super(defaultConf);
  }
}

describe('Tests for Configurable', function() {
  it('Test create Configurable with default configuration', function() {
    ConfigurableStub.prototype.config = {
      'property1' : 'value1'
    };
    const defaultConfiguration = {
      'property1' : 'different value',
      'property2' : 'value2'
    };
    let configurable = new ConfigurableStub(defaultConfiguration);
    expect(configurable.config).to.have.property('property1', 'value1');
    expect(configurable.config).to.have.property('property2', 'value2');
  });

  it('Test create Configurable with empty default configuration', function() {
    ConfigurableStub.prototype.config = {
      'property1' : 'value1'
    };
    const defaultConfiguration = {};
    let configurable = new ConfigurableStub(defaultConfiguration);
    expect(configurable.config).to.have.property('property1', 'value1');
    expect(configurable.config).not.to.have.property('property2');
  });

  it('Test create Configurable without default configuration', function() {
    ConfigurableStub.prototype.config = {
      'property1' : 'value1'
    };
    expect(function() {
      new ConfigurableStub();
    }).to.throw(TypeError, /Default configuration object is expected as first argument/);
  });
});