/**
 *  EmfAjaxLoaderHandler
 *
 *  <p> page blocker that will be activated by specific elements
 *  and will fill the period between start and end operation time.
 */

(function( $, window, document, undefined ) {

	var pluginName = 'EmfAjaxLoaderHandler';

	// defaults parameters for the page blocker
	var defaults = {

		position : {
			INPLACE : 'inplace',
			STATIC  : 'static',
			AFTER   : 'after',
			MODAL   : 'modal'
		},

		// position where the loading image to be placed
		loaderPosition : 'modal', // inplace, static, after

		// whether to block user input for given field
		// this only applies if loaderPosition:inplace
		blockInput : true,

		// loading image path
		imagePath : "",

		// elements that will invoke the page blocker as filter(string)
		elementFilters : null
	};

	/**
	 * Plugin constructor.
	 */
	function EmfAjaxLoaderHandler( container, options ) {

		this.container = container;

		// merge the default options with provided once
		this.options = $.extend({}, defaults, options);

		this._defaults = defaults;

		this._name = pluginName;

		// call initialize to build the plugin view and handlers
		this.init();
	}

	/**
	 * Initialize the plugin.
	 */
	EmfAjaxLoaderHandler.prototype.init = function() {

		var opts = this.options;

		// append loading image to the body
		this.appendLoadingImage(opts);

	};

	/**
	 *  Shows blocker above all elements and disable all elements
	 *
	 *  @param elem the element that invoke this operation
	 */
	EmfAjaxLoaderHandler.prototype.showAjaxLoader = function(elem) {
		// invoker element
		var el = $(elem);

		// Increase loader count for each shown loader.
		// Ensure that loader will not be prematurely hidden during asynchronous requests.
		var loadercount = this._loader.data('loadercount');
		if (!loadercount) {
			loadercount = 0;
		}
		loadercount += 1;
		this._loader.data('loadercount', loadercount);

		// generate class for the loader
		var loaderClass = this.options.loaderPosition + '-loader';
		// append generated class and show the loader
		this._loader.css(this.getStyle(el)).addClass(loaderClass).show();
	};

	/**
	 * Hiding the GUI blocker
	 *
	 * @param reset If block ui layers counter should be reset inside the plugin.
	 */
	EmfAjaxLoaderHandler.prototype.hideAjaxLoader = function(reset) {
		if (reset) {
			this._loader.data('loadercount', 0);
		}
		// Decrease loader count for each hidden loader.
		var loadercount = this._loader.data('loadercount');
		if (loadercount && loadercount > 0) {
			loadercount -= 1;
		} else {
			loadercount = 0;
		}
		this._loader.data('loadercount', loadercount);
		// If this is the last shown loader it will be hidden.
		if (loadercount == 0) {
			this._loader.hide();
		}
	};

	EmfAjaxLoaderHandler.prototype.restoreFocus = function() {
		//restore focused id
		var _focused = SF.focused;
		SF.focused = null;
		if (_focused) {
			var _element = document.getElementById(_focused);

			if (_element) {
				_element.focus();
			}
		}
	};

	EmfAjaxLoaderHandler.prototype.onBeforeAjax = function(elem) {
		this.showAjaxLoader(elem);
	};

	EmfAjaxLoaderHandler.prototype.onAfterAjax = function() {
		this.hideAjaxLoader();
		this.restoreFocus();
	};

	EmfAjaxLoaderHandler.prototype.onAjaxError = function() {
		this.hideAjaxLoader();
		this.restoreFocus();
	};

	EmfAjaxLoaderHandler.prototype.reloadBlockerIdentificators = function(){

		// retrieve all elements based on filter
		var elementLists = $(this.options.elementFilters);

		// element access through loop
		elementLists.each(function(){

			// prevent duplicate on applying page blocker classes
			$(this).removeClass('blockUI').addClass('blockUI');

		});
	};

	EmfAjaxLoaderHandler.prototype.getStyle = function( elem ) {

		var loaderStyle = {
			'width' : 'auto',
			'height' : 'auto'
		};

		if (this.options.loaderPosition === this.options.position.INPLACE) {

			var offset = elem.offset();

			if (this.options.blockInput) {
				var elemHeight = elem.outerHeight();

				if(EMF.ajaxGUIBlocker.options.fullHeight) {
					var body = document.body,
				    html = document.documentElement;

					var elemHeight = Math.max( body.scrollHeight, body.offsetHeight,
				                       html.clientHeight, html.scrollHeight, html.offsetHeight );
				}

				loaderStyle.width = elem.outerWidth();
				loaderStyle.height = elemHeight;
				loaderStyle.top = offset.top;
				loaderStyle.left = offset.left;
				loaderStyle.backgroundColor = '#ECECEC';
				loaderStyle.opacity = '.4';
				loaderStyle['z-index'] = 100;
				loaderStyle['-ms-filter'] = 'progid:DXImageTransform.Microsoft.Alpha(Opacity=40)';
				loaderStyle['filter'] = 'alpha(opacity = 40)';
			} else {

				loaderStyle.top = offset.top + 7;
				loaderStyle.left = offset.left + (elem.width() - 10);

			}

		} else if (this.options.loaderPosition === this.options.position.STATIC) {

			loaderStyle.width = 20;

		} else if (this.options.loaderPosition === this.options.position.AFTER) {

			var offset = elem.offset();
			var elemHeight = elem.outerHeight();
			loaderStyle.top = offset.top + (elemHeight / 2) - 5;
			loaderStyle.left = offset.left + elem.outerWidth();

		}
		// modal panel should block entire screen
		else if (this.options.loaderPosition === this.options.position.MODAL) {

			loaderStyle.width = '100%';
			loaderStyle.height = '100%';
			loaderStyle.backgroundColor = '#ECECEC';
			loaderStyle.opacity = '.4';
			loaderStyle.top = 0;
			loaderStyle.left = 0;
			loaderStyle['z-index'] = 21000;
			loaderStyle['-ms-filter'] = 'progid:DXImageTransform.Microsoft.Alpha(Opacity=40)';
			loaderStyle['filter'] = 'alpha(opacity = 40)';

		}
		return loaderStyle;
	};

	EmfAjaxLoaderHandler.prototype.appendLoadingImage = function( opts ) {

		var ajaxLoader = [ '<span class="sf-ajax-loader"><img src="',
				opts.imagePath, '" /></span>' ].join('');

		this._loader = $(ajaxLoader).appendTo('body').hide();
	};

	/**
	 * Checks if the provided container id exists and the container itself is in
	 * the DOM.
	 */
	EmfAjaxLoaderHandler.prototype.checkPluginContainer = function() {
		var containerIsOk = true;
		// if container is not provided then return with error
		if (!this.container || $(this.container).length === 0) {
			console.log('Error! Container id must be provided!');
			containerIsOk = false;
		}
		return containerIsOk;
	};

	/**
	 * Extend jQuery with our function. The plugin is instantiated only once
	 * (singleton).
	 */
	$.fn[pluginName] = function( options ) {

		var pluginObject = $.data(document.body, 'plugin_' + pluginName);

		if (!pluginObject) {
			pluginObject = $.data(document.body, 'plugin_' + pluginName,
					new EmfAjaxLoaderHandler(this, options));
		}
		return pluginObject;
	};

})(jQuery, window, document);