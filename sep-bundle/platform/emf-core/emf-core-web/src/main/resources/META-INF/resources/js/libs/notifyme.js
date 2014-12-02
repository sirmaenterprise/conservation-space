/**
 * Popup plugin requires some element to be provided when is initialized.
 * If $('body') is provided, the the popup will be displayed relative to the
 * html body. If any other element is provided as container for the plugin,
 * then the popup is opened inside it.
 *
 * To activate the popup we should trigger a custom event with some parameters:
 * $('#button').trigger('notifyme')
 * or
 * $('#button').trigger('notifyme', [message, level, position])
 *
 * Plugin accepts an options map: [O]-optional, [R]-required parameter
 *                [O|R][available values][default value]
 * position:      [O][top|bottom|center|bottomRight][top]
 * closeButton:   [O][true|false][true]
 * visiblePeriod: [O][number in ms][2000]
 * popupWidth:    [O][number in pixels][250] applied only if position is set to 'center'
 * popupHeight:   [O][number in pixels][80] applied only if position is set to 'center'
 * roundCorners:  [O][true|false][true]
 * showEffect:    [O][fadeIn|slideDown|show]['fadeIn']
 * fadeEffect:    [O][fadeOut|slideUp|hide]['fadeOut']
 * effectSpeed:   [O][number in ms][500]
 * level:         [O][info|ok|warning|error][info] mainly determines the popup visual style and icon
 */
;(function ( $, window, document, undefined ) {

  var pluginName = 'notifyMe';

  // defaults
  var defaults = {
    availablePositions 	: {
      top         : 'top',
      bottom      : 'bottom',
      center      : 'center',
      bottomRight : 'bottomRight'
    },
    // top, bottom, center, bottomRight
    position 			: 'center', 
    // if close button should be avaialble in the popup
    // true: means that the popup must be closed trough the button
    // false: means that the popup will auto dissapear after a given period
    closeButton 		: true,
    // visible period works only if closeButton is false
    visiblePeriod 		: 2000,
    // applied only if position is centered
    popupWidth 			: 400,
    // applied only if position is centered
    popupHeight 		: 80,
    // if corners should be rounded
    roundCorners		: false,
    // slideUp, fadeOut, hide
    fadeEffect 			: 'fadeOut', 
    // slideDown, fadeIn, show
    showEffect 			: 'fadeIn', 
    effectSpeed 		: 500,
    // info-message, ok-message, error-message, warn-message, fatal-message
    level 				: 'info-message', 
    showLevelIcon 		: true,
    clearMessagesUrl	: '/emf/service/notification/clear',
    handleMessageUrl	: '/emf/service/notification/handle'
  };

  /**
   * Plugin constructor.
   */
  function NotifyMe( container, options ) {
    this.container = container;

    if(options) {
      var position = options.position;
      // override default position option only if correct value is provided
      if(position && !(position in defaults.availablePositions))  {
        delete options.position;
      }
    }

    // merge the default options with provided once
    this.options = $.extend( {}, defaults, options) ;

    this._defaults = defaults;
    this._name = pluginName;
    // call init to build the plugin view and handlers
    this.init();
  }

  /**
   * Initialize the plugin.
   */
  NotifyMe.prototype.init = function () {
    // check the container
    if(!this.checkPluginContainer()) {
      // if container is missing, then return
      return;
    }

    var opts = this.options;
    // create plugin DOM element
    this.popup = this.createDOM(opts);
    // bind handlers
    this.bindHandlers(opts);
  };


  /**
   * Builds the popup container.
   */
  NotifyMe.prototype.createDOM = function(opts) {
    var popupCssStyle = this.getPopupCssStyle(opts.position, opts);
    var popupStyleClass = this.getPopupStyleClass(opts.position, opts);

    var popupHTML = 
    '<div class="' + popupStyleClass + '" style="' + popupCssStyle + '"> \
    	<div class="notifyme-header"> \
    		<span class="notifyme-icon"></span> \
    		<span class="notifyme-header-title"></span> \
    		<span class="notifyme-close-button"></span> \
    	</div> \
    	<div class="notifyme-content"></div> \
    	<div class="notifyme-footer"> \
    		<div class="notifyme-footer-buttons"></div> \
    	</div> \
     </div>';
    
    return $(popupHTML).appendTo(this.container);
  };

  /**
   * Calculates the popup panel css style classes based on initial configuration passed.
   */
  NotifyMe.prototype.getPopupStyleClass = function(position, opts) {
    var styleClass = 'notifyme ' + position;
    if(opts.roundCorners) {
      styleClass += ' notifyme-round-corners';
    }
    if(opts.closeButton === false) {
      styleClass += ' no-close-btn';
    }

    return styleClass;
  };

  /**
   * Calculates the popup panel css styles based on initial configuration passed.
   */
  NotifyMe.prototype.getPopupCssStyle = function(position, opts) {
    var container = this.container;

    var containerWidth;
    var containerHeight;
    var scrollTop;
    var containerTop;
    var containerLeft;

    var isBody = false;
    if(container.is('body')) {
      containerWidth = $(window).width();
      containerHeight = $(window).height();
      scrollTop = $(document).scrollTop();
      isBody = true;
    } else {
      containerWidth = container.width();
      containerHeight = container.height();
      var offset = container.offset();
      containerTop = offset.top;
      containerLeft = offset.left;
    }

    var availablePositions = opts.availablePositions;
    var style = 'position: absolute; ';
    switch(position) {
      case availablePositions.top:
        var top = isBody ? scrollTop : containerTop + 1;
        var left = isBody ? 1 : containerLeft + 2;
        style += ' top: ' + top + 'px; left: ' + left + 'px;';
        style += ' width: ' + (containerWidth - 2) + 'px;';
        break;
      case availablePositions.bottom:
        if(isBody) {
          style += ' bottom: -' + scrollTop + 'px; left: ' + 1 + 'px;';
        } else {
          style += ' top: ' + (containerTop + containerHeight - 20) + 'px; left: ' + (containerLeft + 2) + 'px;';
        }
        style += ' width: ' + (containerWidth - 2) + 'px;';
        break;
      case availablePositions.center:
        var popupWidth = opts.popupWidth;
        var popupHeight = opts.popupHeight;
        var top;
        var left;
        if(isBody) {
          top = Math.ceil((containerHeight / 2) - (popupHeight / 2)) + scrollTop;
          left = Math.ceil((containerWidth / 2) - (popupWidth / 2));
        } else {
          top = Math.ceil((containerHeight / 2) - (popupHeight / 2)) + containerTop;
          left = Math.ceil((containerWidth / 2) - (popupWidth / 2)) + containerLeft;
        }
        style += ' top: ' + top + 'px; left: ' + left + 'px;';
        style += ' width: ' + popupWidth + 'px;'
        //style += ' height: ' + popupHeight + 'px;';
        break;
      case availablePositions.bottomRight:
        var popupWidth = opts.popupWidth;
        var popupHeight = opts.popupHeight;
        var top;
        var left;
        if(isBody) {
          top = Math.ceil(containerHeight - popupHeight - 3) + scrollTop;
          left = Math.ceil(containerWidth - popupWidth - 3);
        } else {
          top = Math.ceil(containerHeight - popupHeight) + containerTop;
          left = Math.ceil(containerWidth - popupWidth) + containerLeft;
        }
        style += ' top: ' + top + 'px; left: ' + left + 'px;';
        style += ' width: ' + popupWidth + 'px; height: ' + popupHeight + 'px;';
        break;
      default:
        break;
    }

    return style;
  };

  /**
   * Binds the event handlers.
   */
  NotifyMe.prototype.bindHandlers = function(opts) {
    var plugin = this;
    var popup = $(this.popup);
    var body = $('body');

    // bind a click handler that would close the popup if available
    body.on('click.notifyme', '.notifyme-close-button', closePopup);
    
    // popupOpen event is triggered right after popup is displayed
    body.on('popupOpen', function(evt) {
      if(evt.closeButton === false) {
        // run a timer that will trigger popupClose after given period
        var timer = setTimeout(timeoutHandler, opts.visiblePeriod);
      }
    })
    
    // popupClose is used mainly for auto close functionality
    .on('popupClose', closePopup);

    // some of the configration options can be passed trough the event object
    //
    // message: the text or html fragment to be displayed inside the popup
    // level: notification level - determines what icon to be visualized
    // position: where to position the popup
    // showLevelIcon: whether to show notification level icon
    body.on('notifyme', function showPopup(evt) {
      var message = evt.message;
      // if message is not provided, then an empty popup will be displayed
      if(message) {
        popup.find('.notifyme-content').empty().html(message);
      } else {
        popup.find('.notifyme-content').empty();
      }
      // If position is provided, the calculate new styles according to it.
      // In all cases the styles are recalculated when this event is triggered
      // because the page may have been scrolled in the between plugin
      // reinitialization and when this event is triggered.
      var currentPosition = evt.position || opts.position;
      popup.attr('style', plugin.getPopupCssStyle(currentPosition, opts));
      popup.attr('class', plugin.getPopupStyleClass(currentPosition, opts));

      // If level is provided, the it will override the default level.
      var level = evt.level || opts.level;
      popup.addClass(level);
      
      // whether to show level icon is calculated according to 
      // passed parameter or the default one
      var showIcon;
      if(evt.showLevelIcon !== undefined) {
        showIcon = evt.showLevelIcon;
      } else {
        showIcon = opts.showLevelIcon;
      }
      if(!showIcon) {
        popup.addClass('hidden-icon');
      }
      
      var closeButton;
      if(evt.closeButton !== undefined) {
        closeButton = evt.closeButton;
      } else {
        closeButton = opts.closeButton;
      }
      if(closeButton) {
        popup.removeClass('no-close-btn');
      } else {
        popup.addClass('no-close-btn');
      }
      
      if(evt.messageId) {
    	  var messageId = evt.messageId;
    	  var actionButton = '<input type="button" id="' + messageId + '" name="' + messageId + '" value="' + evt.buttonLabel + '" class="btn btn-primary standard-button" />';
    	  $(actionButton).appendTo(popup.find('.notifyme-footer-buttons')).on('click', function () {
    	    	$.ajax({
    	    		url: opts.handleMessageUrl,
    	    		data: { messageId: messageId }
    	    	}).done(function() {
    	    		//
    	    	});
    	  });
      }

      // open the popup and trigger the popupOpen event
      popup[opts.showEffect](opts.effectSpeed, function() {
        popup.trigger({ type: 'popupOpen', closeButton: closeButton });
      });
    });
    
    function closePopup() {
  	  popup[opts.fadeEffect](opts.effectSpeed);
  	  $.ajax({
  		  url: opts.clearMessagesUrl
  	  }).done(function() {
  		  // succesfully cleared
  	  });
    };
    
    function timeoutHandler() {
      popup.trigger('popupClose');
    }
  };
  
  /**
   * Checks if the provided container id exists and the container
   * itself is in the DOM.
   */
  NotifyMe.prototype.checkPluginContainer = function() {
    var containerIsOk = true;
    // if container is not provided then return with error
    if(!this.container || $(this.container).length === 0) {
      console.log('Error! Container id must be provided!');
      containerIsOk = false;
    }
    return containerIsOk;
  };

  /**
   * Extend jQuery with our function. The plugin is instantiated only once (singleton).
   */
  $.fn[pluginName] = function ( options ) {
	// TODO: if body or document.body is passed as target, then binding doesn't happen
    var pluginObject = $.data(document.body, 'plugin_' + pluginName);

    if (!pluginObject) {
      pluginObject = $.data(document.body, 'plugin_' + pluginName,
        new NotifyMe( this, options ));
    }
    return pluginObject;
  }

})( jQuery, window, document );
