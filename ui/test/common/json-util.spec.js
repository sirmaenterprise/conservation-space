import {JsonUtil} from 'common/json-util';

describe('Tests for JsonUtil', function() {
	it('Copy property with different object/source key', function() {
		const source = {'property1':'value1'};
		const object = {};
		JsonUtil.copyProperty(object, 'new property name', source, 'property1');
		expect(object).to.have.property('new property name', source['property1']);
	});

	it('Copy property with same object/source key', function() {
		const source = {'property1':'value1'};
		const object = {};
		JsonUtil.copyProperty(object, 'property1', source);
		expect(object).to.have.property('property1', source['property1']);
	});

	it('Override existing property', function() {
		const source = {'property1':'value1'};
		const object = {'property1':'value2'};
		JsonUtil.copyProperty(object, 'property1', source);
		expect(object).to.have.property('property1', 'value1');
	});

	it('Copy not existing property', function() {
		const source = {'property1':'value1'};
		const object = {};
		JsonUtil.copyProperty(object, 'property2', source);
		expect(object).to.not.have.property('property1');
	});
});