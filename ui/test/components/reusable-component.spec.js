import {ReusableComponent} from 'components/reusable-component';

describe('Tests for ReusableComponent', function() {
	it('Test create ReusableComponent without createActualConfig()', function() {
		expect(function() {new ReusableComponent({})}).to.throw(TypeError, /Must override createActualConfig function/);
	});
	
	it('Test create ReusableComponent with createActualConfig() but not setting actualConfig object', function() {
		class TestClass extends ReusableComponent {
			createActualConfig() {
				
			}
		};
		expect(function() {new TestClass({})}).to.throw(TypeError, /createActualConfig must create actualConfig configuration object for the underlying library/);
	});
	
	it('Test create ReusableComponent with createActualConfig() which set actualConfig object', function() {
		class TestClass extends ReusableComponent {
			createActualConfig() {
				this.actualConfig = {};
			}
		};
		expect(function() {new TestClass({})}).to.not.throw(TypeError);
	});
});