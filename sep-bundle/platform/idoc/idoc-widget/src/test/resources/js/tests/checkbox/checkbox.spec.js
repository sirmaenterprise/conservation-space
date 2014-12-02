describe('Checkbox widget', function () {
	var element,
		widgetBodyWrapper,
  		scope, 
  		compile, 
  		defaultData,
  		validTemplate = '<checkbox></checkbox>';

	function createDirective(config, template) {
		// Setup scope state
		scope.config = config || defaultData;

		// Create directive
		element = compile(template || validTemplate)(scope);
		// Trigger watchers
		scope = element.scope();
		scope.$digest();
		
		widgetBodyWrapper = element.parent();
		// Return
		return element;
	}

	beforeEach(function () {

		// Load the directive's module
		module('app');
		module('widgetTemplates');

		// Reset data each time
		defaultData = {
			checked: [
			    {
			    	label: 'test',
			    	checked: false
			    }
			]
		};

		// Provide any mocks needed
		module(function ($provide) {
			//$provide.value('Name', new MockName());
		});

		// Inject in angular constructs otherwise,
		//  you would need to inject these into each test
		inject(function ($rootScope, $compile) {
			scope = $rootScope.$new();
			compile = $compile;
		});
	});

	describe('when created', function () {
		it('there should be no checkboxes if the config is empty', function() {
			createDirective({ });
			
			expect(scope.allCheckboxes.length).toBe(0);
			expect(widgetBodyWrapper.find('input[type="checkbox"]').length).toBe(0);
		});

		it('rendered checkboxes should mirror config model', function() {
			createDirective();
			
			expect(scope.allCheckboxes.length).toBe(1);
			expect(widgetBodyWrapper.find('input[type="checkbox"]').length).toBe(1);
		});
	});
	
	describe('when the model changes', function () {
		it('changes should be reflected in the DOM', function() {
			var checkboxElement;
			
			createDirective({ });
			
			expect(scope.allCheckboxes.length).toBe(0);
			expect(widgetBodyWrapper.find('input[type="checkbox"]').length).toBe(0);
			
			scope.allCheckboxes.push({
				label: 'Does this test pass?',
				checked: true
			});
			
			scope.$digest();
			
			checkboxElement = widgetBodyWrapper.find('input[type="checkbox"]');
			expect(scope.allCheckboxes.length).toBe(1);
			expect(checkboxElement.length).toBe(1);
			expect(checkboxElement.prop('checked')).toBe(true);
		});
		
		it('rendered checkboxes should be clickable in edit mode', function() {
			var checkboxElement;
			
			createDirective();
			
			expect(scope.allCheckboxes[0].checked).toBe(false);
			
			checkboxElement = widgetBodyWrapper.find('input[type="checkbox"]');
			checkboxElement.prop('checked', true);
			checkboxElement.change();
			
			scope.$digest();
			expect(scope.allCheckboxes[0].checked).toBe(true);
		});
	});
});