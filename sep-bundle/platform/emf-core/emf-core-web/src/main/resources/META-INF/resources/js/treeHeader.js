/**
 * TreeHeaderHandler
 * 
 * jQuery PLUGIN that will manage treeHeader actions under 
 * available elements.
 * 
 */
;(function($){
	
	$.treeHeader = function(options){
		
		// default setting
		var defaults = {
            animated 		: true,
			duration	 	: 500,
			mainContainerId : '#contentBody'
        };
		
		// holds current reference
		var treePlugin = this;
		
		// define attribute that will holds default and user defined
		// options extended in one data structure
		treePlugin.options = {};
		
		var clickHandlerDispatcher = function(evt){
			var element = evt.target;
			var elParent = $(element).parent();
			if(elParent != null && elParent.hasClass('instance-header')){
				clickHandler.toggleTreeHeaderLinks(element); 
			}else if(elParent != null && elParent.hasClass('item')){
				clickHandler.toggleTreeBottomLinks(element); 
			}
		};
		
		// define start point
		var init = function(){
			treePlugin.options = $.extend({},defaults,options);
			// apply click to the default container and assign to the dispatcher
			$(treePlugin.options.mainContainerId).on('click',clickHandlerDispatcher);
		};
		
		// Holds all click handlers that can be applied to the current main container
		var clickHandler = {
			// TODO: refactoring 
			toggleTreeHeaderLinks : function(element){
				
				var link = $(element);
				var parentOfLink = $(element).parent();
				var elementsNextParent = parentOfLink.nextAll(":not('.extended-current ')");
				var isCollapsed = false;
				
				// determine and switch class for icons
				if(link.hasClass('expanded')){
					link.removeClass('expanded').addClass('collapsed');
				}else{
					link.removeClass('collapsed').addClass('expanded');
					isCollapsed = true;
				}
				
				$(elementsNextParent).each(function(index) {
					
					// next element of the parent
					var currentElement = elementsNextParent[index];
					
					if(!isCollapsed){
						// animate slide up
						$(currentElement).hide();
						// prevent sub-classes conflicts
						$(currentElement).find('span:eq(1)').removeClass('expanded').addClass('collapsed');
						
					}else{
						//animate slide down
						$(currentElement).show();
						// prevent sub-classes conflicts
						$(currentElement).find('span:eq(1)').removeClass('collapsed').addClass('expanded');
					}
				});
				
			},
			
			/**
			 * TODO: Refactoring the HTML structure displaying tree elements,
			 * that will allow us to merge this function with {@link toggleTreeHeaderLinks}
			 */
			toggleTreeBottomLinks : function(element){
				var el = $(element),
					parent = el.parent(),
					expanded = el.hasClass('expanded');
				
				if(expanded){
					el.removeClass('expanded').addClass('collapsed');
					parent.find('ul').hide();
				}else{
					el.removeClass('collapsed').addClass('expanded');
					parent.find('ul').show();
					var els = parent.find('ul').end().find('span.toggler');
					for(var i = 0; i < els.length; i++){
						$(els[i]).removeClass('collapsed').addClass('expanded');
					}
				}
			}
			
		}
		
		init();
	}
	
})(jQuery);