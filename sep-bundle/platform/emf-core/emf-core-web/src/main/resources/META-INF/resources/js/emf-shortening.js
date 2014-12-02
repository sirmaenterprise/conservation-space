;(function( $, window, document, undefined ) {
	
	var pluginName = 'EmfShorteningPlugin';
	
	var defaults = {
		 /**
		  * Low priority attributes for the <b>EmfShorteningPlugin</b>. Located
		  * data for patterns and additional elements style.
		  */
		 moreLinkPattern 		 : '[...]',
		 lessLinkPattern		 : '[...]',
		 eventContainer			 : 'body',
		 moreLinkStyle 			 : '',
		 
		 /**
		  * High priority attributes for the <b>EmfShorteningPlugin</b>. Located 
		  * data for constructing shortening text structure.
		  */
		 selector				 : '',
		 shortLink				 : {
			 mainClass			 : 'shortLink', 
			 expandClass         : 'more-text',
			 collapseClass 		 : 'less-text' 
		 },
		ellipsesClass			 : 'ellipsis-tree-link'
	};
	
	/**
	 * Default constructor for <b>EmfShorteningPlugin</b>. The constructor will 
	 * extend default with additional options and will invoke
	 * main initialization.
	 * 
	 * @param options - current data options
	 */
	function EmfShorteningPlugin(options) {
		this.options = $.extend({}, defaults, options);
		this._name = pluginName;
		this.init();
	};
	
	
	/**
	 * Initialization for <b>EmfShorteningPlugin</b>. Integrate window resize event 
	 * and shorten link actions.
	 */
	EmfShorteningPlugin.prototype.init = function() {
		
		var resizeTimer;
		var _this = this;
		
		// integrate EmfShorteningPlugin with window resize event
		$(window).resize(function() {
			clearTimeout(resizeTimer);
			resizeTimer = setTimeout(function(){
				var elements = $(_this.options.selector);
				if(elements.size()){
					$(elements).each(function(){
						var currentElement = $(this);
						EmfShorteningPlugin.prototype.elementTransform(currentElement, _this.options);
					});
				}
		    }, 10);
		});
		
		// click event for short link button
		$(this.options.eventContainer).on('click',function(evt){
			var element = evt.target;
			if($(element).hasClass(_this.options.shortLink.mainClass)){
				shorteningButtonToggle(element, _this.options);
			}
		});
	};
	
	
	/**
	 * Toggle action for short link button. The method will change button pattern
	 * and will expand or collapse text for specified element.
	 * 
	 * @param linkButtonExpandCollapse - current short link button
	 * @param options - current data options
	 */
	shorteningButtonToggle = function(linkButtonExpandCollapse, options){
		
		var shortLink = $(linkButtonExpandCollapse);
		var ellipsesClass = options.ellipsesClass;
		
		// expand action
		if(shortLink.hasClass(options.shortLink.expandClass)){
			shortLink.removeClass(options.shortLink.expandClass).addClass(options.shortLink.collapseClass);
			shortLink.html(options.lessLinkPattern);
			
			// just remove ellipses class
			shortLink.parent().find('.'+ellipsesClass).removeClass(ellipsesClass);
		
			// collapse action
		}else if(shortLink.hasClass(options.shortLink.collapseClass)){
			shortLink.removeClass(options.shortLink.collapseClass).addClass(options.shortLink.expandClass);
			shortLink.html(options.moreLinkPattern);
			
			// recalculate
			applyEllipsis(shortLink.parent().find(options.selector), options);
		}
	};
	
	/**
	 * Prepare data that will be truncated.
	 * 
	 * @param currentElement - an component or list with components that will be truncated
	 * @param options - current data options
	 */
	EmfShorteningPlugin.prototype.elementTransform = function(currentElement, options){
		var retrievedOptions = options ? options : this.options; 
		var elements = currentElement ? currentElement : $(retrievedOptions.selector);
		applyEllipsis(elements, retrievedOptions);
	};
	
	
	/**
	 * Core <b>EmfShorteningPlugin</b> method. Here element is transform from normal to shorten.
	 * Calculating and applying component width based on presented <i>DOM</i> structure, apply
	 * short link button. 
	 * 
	 * @param truncatableElements - an component or list with components that will be truncated
	 * @param options - current data options
	 */
	applyEllipsis = function(truncatableElements, options){
		
		var truncatableElementObjects = truncatableElements;
		
		// if we have no available element, go back
		if(!truncatableElementObjects.size()){
			return;
		}
		
		truncatableElementObjects.each(function(){
			
			var currentElement = $(this);
			// remove ellipses class, if any and short link button
			restoreElement(currentElement, options);
			var parent = currentElement.parent(); 
			
			// Win8/IE-10 fix wrong height calculation
			currentElement.css('display','inline-block');
			
			var textHeight = currentElement.height();
			var textLineHeight = currentElement.css('line-height');
			
			currentElement.css('display','');
			
			// Win7/IE-8 fix wrong line-height calculation
			textLineHeight = textLineHeight === "1px" ? "18.6px" : textLineHeight;
			
			var textLineNumber = Math.round(textHeight/parseInt(textLineHeight));
			
			if(textLineNumber > 1){
				var shortLinkClass = '.'+options.shortLink.mainClass;
				var shortLinkObject = parent.find(shortLinkClass);
				// apply short link button in the DOM
				if(!shortLinkObject.length){
					currentElement.after(generateLinkMore(options.moreLinkPattern, options));
					shortLinkObject = parent.find(shortLinkClass);
				}
				currentElement.removeClass(options.ellipsesClass);
				
				// calculate and apply width to the truncated element
				// based on parent element and short link button widths
				currentElement.css('width', ((parent.width() - shortLinkObject.width()) - 30) + 'px');
				
				currentElement.addClass(options.ellipsesClass);
			}
		});
	};
	
	/**
	 * Method that restore the element in normal state. Remove
	 * ellipses class and short link button if any.
	 * 
	 * @param currentElement - current component that holds shortening data
	 * @param options - current data options
	 */
	restoreElement = function(currentElement, options){
		var element = $(currentElement);
		if(!element.size()){
			console.error('Emf-Shortening: Cannot restore element.');
			return;
		}
		var shortLinkClass = '.'+options.shortLink.mainClass;
		var shortLinkObject = element.parent().find(shortLinkClass);
		element.removeClass(options.ellipsesClass);
		if(shortLinkObject){
			shortLinkObject.remove();
		}
	};
	
	/**
	 * Short link button component.
	 * 
	 * @param value - visible text for the button
	 * @param options - current data options
	 */
	generateLinkMore  = function(value, options){
		return ' <a class="shortLink more-text" style="'+options.moreLinkStyle+'">'+value+'</a>';
	};
	
	$.fn[pluginName] = function( options ) {
		var pluginPrefix = 'plugin_';
		var pluginName = pluginPrefix + pluginName;
		var pluginObject = $.data(document.body, pluginName);
		if (!pluginObject) {
			pluginObject = $.data(document.body, pluginName, new EmfShorteningPlugin(options));
		}
		return pluginObject;
	};
	
})(jQuery, window, document);