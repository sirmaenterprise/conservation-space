;(function() {
	'use strict';
	
	var module = angular.module('idocEmfCommentsIntegration', [ ]);
	
	module.directive('idocCommentTrigger', ['$timeout', '$document', function($timeout, $document) {
		return function(scope, element, attrs) {
			var objectType = idoc.object.type,
				previouslySelected = [ ];
			
			function selectObjects() {
				var thisArg = this,
					elements,
					nextTop,
					nextId,
					objectId = idoc.object.id,
					newlySelected = [ ];
				
				elements = $('.commented-object');
				_.eachRight(elements, function processSelectedObject(element) {
					var $this = $(element),
						data = $this.data('emfCommentsData'),
						top,
						topMargin = parseInt($this.css('marginTop')),
						id = $this.attr('id');
						
					newlySelected.push(id);
					
					if (!data) {
						data = { 
							aboutSection: id, 
							instanceId: objectId, 
							instanceType: objectType,
							dirty: true, 
							removed: false
						};
					}

					// When save and continue action is executed data.aboutSection is empty at the begining
					// and have to be reloaded after the first save of document/object
					if (!data.aboutSection || !data.instanceId) {
						data = { 
							aboutSection: id, 
							instanceId: objectId, 
							instanceType: objectType,
							dirty: true, 
							removed: false
						};
					}
					
					data.dirty = true;
					top = element.offsetTop;
					if (top !== data.top) {
						data.top = top;
						data.dirty = true;
					}
					
					if (nextId !== data.next) {
						data.next = nextId;
						data.dirty = true;
					}
					
					if (nextTop !== data.nextTop) {
						data.nextTop = nextTop;
						data.dirty = true;
					}

					if (topMargin !== data.topMargin) {
						data.topMargin = topMargin;
						data.dirty = true;
					}

					if (data.dirty || data.removed || data.added) {
						// trigger update
						scope.$broadcast('emf-comments:ng:update-object', data);
					}
					
					data.dirty = false;
					data.removed = false;
					data.added = false;
					$this.data('emfCommentsData', data);

					nextId = id;
					nextTop = top;
				}, thisArg);
				
				var removed = _.difference(previouslySelected, newlySelected);
				
				_.each(removed, function(id) {
					scope.$broadcast('emf-comments:ng:update-object', {
						aboutSection: id,
						removed: true
					});
				});
				
				previouslySelected = newlySelected;
			}
			
			$document.on('idoc-comments-integration:dom:update', function(event) {
				$timeout(function() {
					selectObjects();
				}, 0);
			});
			
			scope.$watch('documentContext.id', function(value, oldValue) {
				if (value) {
					$timeout(function() {
						selectObjects();
					}, 0);
				}
			});
		}
	}]);
}());