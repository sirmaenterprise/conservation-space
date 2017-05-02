import registry from 'common/lib/plugin-registry';

var expect = chai.expect;

describe('Tests for the PluginRegistry', function() {
	it('Test add() for unordered plugins', function() {
		registry.clear('route');
		
		registry.add('route', {
			"stateName" : "login",
		});
		
		registry.add('route', {
			"stateName": "search",
		});
		
		expect(registry.get('route')).to.have.length(2);
	});
	
	it('Test add() for multiple unordered plugins as array', function() {
		registry.clear('route');
		
		registry.add('route', [
		    {
		    	"stateName" : "login",
		    },
		    {
		    	"stateName": "search",
		    }
		]);
		
		expect(registry.get('route')).to.have.length(2);
	});
	
	it('Test add() for throwing error when adding an ordered plugin to unordered list', function() {
		registry.clear('route');
		
		registry.add('route', {
			"stateName" : "login",
		});
		
		expect(function () { registry.add('route', {
			"order": 1,
			"stateName": "search",
			});
		}).to.throw(Error);
		
		expect(registry.get('route')).to.have.length(1);
	});
	
	it('Test add() for ordered plugins', function() {
		registry.clear('route');
		
		registry.add('route', {
			"order": 2,
			"stateName": "search",
		});
		
		registry.add('route', {
			"order": 1,
			"stateName" : "login",
		});
		
		var result = registry.get('route');
		
		expect(result).to.have.length(2);
		
		expect(result[0].order).to.equal(1);
		expect(result[1].order).to.equal(2);
	});
	
	it('Test add() for two plugins with the same order', function() {
		registry.clear('route');
		
		registry.add('route', {
			"order": 2,
			"stateName": "search",
		});
		
		expect(function () { registry.add('route', {
			"order": 2,
			"stateName": "search",
			});
		}).to.throw(Error);
		
		expect(registry.get('route')).to.have.length(1);
	});
	
	it('Test add() for two plugins with the same order but greater priority', function() {
		registry.clear('route');
		
		registry.add('route', {
			"order": 2,
			"priority": 1,
			"stateName": "search",
		});
		
		registry.add('route', {
			"order": 2,
			"priority": 2,
			"stateName": "login",
		});
		
		expect(registry.get('route')).to.have.length(1);
	});
	
	it('Test add() for two plugins with the same order but lower priority', function() {
		registry.clear('route');
		
		registry.add('route', {
			"order": 2,
			"priority": 2,
			"stateName": "search",
		});
		
		registry.add('route', {
			"order": 2,
			"priority": 1,
			"stateName": "login",
		});
		
		expect(registry.get('route')).to.have.length(1);
	});
	
	it('Test add() for throwing error when adding unordered plugin to an ordered list', function() {
		registry.clear('route');
		
		registry.add('route', {
			"order": 1,
			"stateName" : "login",
		});
		
		expect(function () { registry.add('route', {
			"stateName": "search",
			});
		}).to.throw(Error);
		
		expect(registry.get('route')).to.have.length(1);
	});
	
	it('Test add() for ordered plugin with non-number priority', function() {
		expect(function () { registry.add('noNumberPriority', {
			"order": 1,
			"priority": "1",
			"stateName": "search",
			});
		}).to.throw(Error);
		
		expect(registry.get('noNumberPriority')).to.have.length(0);
	});
	
});