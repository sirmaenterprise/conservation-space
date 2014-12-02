/**
 * Session countdown timer.
 */
var SessionTimer = {

	config: {
		
		sessionTimerInitialized: false,
		
		// synchronization variable key name for all active session timers started in separated browser tabs
		startTimeKey: 'emf.starttime',
		
		/* Default text color of countdown timer */
		countdownColorDefault: "#000000",

		/* Level 1 warning time */
		countdownColorLevel1Mins: 0,

		/* Level 1 warning color */
		countdownColorLevel1: "#B07000",

		/* Level 2 warning time */
		countdownColorLevel2Mins: 0,

		/* Level 2 warning color */
		countdownColorLevel2: "#E00000",

		/* Count down timer element ID */
		countdownElementId: "#menuForm\\:countdownTimer",
		
		checkSessionServletUrl: '/activeSession',
		
		// Number of minutes before session timeout.
		sessionTimeoutIntervalInSeconds: 60 
	},
	
	// when debug is enabled, this will hold output element in which the remaining session time will be printed
	output: null,
	
	/**
	 * Counter that starts counting down as soon as the page loads. After the countdown finishes,
	 * a function is called. It also takes care of displaying session time left on screen.
	 * To reset the timer to the initial value, countdownReset() method is to be called.
	 */
	countdownIntervalId: null,
	
	/**
	 * Method that is called right after countdown finishes.
	 * @param logout 
	 * 			If should be performed logout or not.
	 */
	sessionTimeout: function(logout) {
		SessionTimer.clearStarttime();
		var logoutUrl = SF.config.baseURL + 'ServiceLogout';
		window.location.href = logoutUrl;
	},
	
	countdownReset: function() {
		clearInterval(SessionTimer.countdownIntervalId);
		SessionTimer.updateStarttime();
	},
	
	clearStarttime: function() {
		// TODO: implement fallback for not existing localStorage using a cookie
		
		// Null check for Qt based browsers that do not support localStorage.
		if (localStorage) {
			localStorage.removeItem(SessionTimer.config.startTimeKey);
		}
	},
	
	updateStarttime: function() {
		// Null check for Qt based browsers that do not support localStorage.
		if (localStorage) {
			localStorage.setItem(SessionTimer.config.startTimeKey, new Date().getTime());
		}
	},
	
	countdownInit: function() {
		SessionTimer.countdownIntervalId = setInterval( function() { SessionTimer.countdownStep(); }, 1000 );
	},
	
	countdownStep: function() {
		if (!localStorage) {
			return;
		}
		var secondsLeft = 0;
		var starttime = localStorage.getItem(SessionTimer.config.startTimeKey);
		
		// if starttime is cleared from any other tab when user clicks logout, we should end in every other tab too
		if (!starttime) {
			clearInterval(SessionTimer.countdownIntervalId);
			SessionTimer.sessionTimeout(false);
			return;
		}
		
		secondsLeft = SessionTimer.config.sessionTimeoutIntervalInSeconds - Math.round((new Date().getTime() - starttime) / 1000);
		
		if(SessionTimer.config.debugEnabled) {
			SessionTimer.output.text(secondsLeft);
		}
		
		// !!! don't make operations for visualizing - not used in the moment
		//SessionTimer.countdownDisplay(secondsLeft);
		
		if (secondsLeft <= 0) {
			clearInterval(SessionTimer.countdownIntervalId);
			SessionTimer.sessionTimeout(true);
		}
	},
	
	countdownDisplay: function(countdown) {
		var minutesLeft = 0;
		var secondsLeft = 0;
		
		minutesLeft = Math.floor(countdown / 60);
		secondsLeft = countdown % 60;
		
		var htmlElement = $(SessionTimer.config.countdownElementId);
		if (htmlElement) {
			if (minutesLeft < SessionTimer.config.countdownColorLevel2Mins) {
				htmlElement.css('color', SessionTimer.config.countdownColorLevel2);
			} else if (minutesLeft < SessionTimer.config.countdownColorLevel1Mins) {
				htmlElement.css('color', SessionTimer.config.countdownColorLevel1 );
			} else {
				htmlElement.css('color', SessionTimer.config.countdownColorDefault);
			}
			var innerHtml =  (minutesLeft < 10 ? "0" : "") + minutesLeft + ":" + (secondsLeft < 10 ? "0" : "") + secondsLeft;
			htmlElement.text(innerHtml);
		}
	},
	
	/**
	 * This is intended to be invoked from js events such as onclick, keyup, ajaxStart and others, in which case we assume
	 * that this plugin is already initialized and config object can be used internally as oposite to the external 
	 * configuration passed to SessionTimer.init function where we stil don't have full configuration.  
	 */
	reinit: function(evt) {
		SessionTimer.config.sessionTimerInitialized = false;
		SessionTimer.init(SessionTimer.config);
	},
	
	/**
	 * Restart the timer.
	 */
	restart: function() {
		SessionTimer.countdownReset();
		SessionTimer.countdownInit();
	},
	
	init: function(opts) {
		var initialized = SessionTimer.config.sessionTimerInitialized;
		if (initialized) {
			return;
		}

		SessionTimer.config = $.extend(true, SessionTimer.config, {
			sessionTimeoutIntervalInSeconds: (opts.sessionTimeout * 60),
			countdownColorLevel1Mins: (2 * (opts.sessionTimeout / 3)),
			countdownColorLevel2Mins: (1 * (opts.sessionTimeout / 3))
		}, opts);
		
		
		SessionTimer.updateStarttime();
		
		if (SessionTimer.config.debugEnabled) {
			console.log('Initializing SessionTimer with configuration: ', SessionTimer.config);
			
			var output = $('#sessionTimerDebug');
			if (output.length === 0) {
				output = $('<span id="sessionTimerDebug">session timeout in:<b></b></span>').css({
					'position': 'fixed',
					'top': '80px',
					'right': '80px',
					'background-color': 'blue',
					'color': '#ffffff'
				}).appendTo(document.body);
				SessionTimer.output = output.find('b');
			}
		}
		
		$(SessionTimer.config.mainContainerId)
			.off('keyup.sessionTimer').on('keyup.sessionTimer', _.throttle(SessionTimer.restart, 5000));
		
		$(document).off(SessionTimer.config.events.EMF_LOGOUT).on(SessionTimer.config.events.EMF_LOGOUT, function() {
			SessionTimer.clearStarttime();
		})
		.off(SessionTimer.config.events.SESSION_TIMER_RESET)
			.on(SessionTimer.config.events.SESSION_TIMER_RESET, _.throttle(SessionTimer.restart, 5000));
		
		SessionTimer.countdownReset();
		SessionTimer.countdownInit();
		SessionTimer.config.sessionTimerInitialized = true;
	}
};

//Register Session timer module
EMF.modules.register('SessionTimer', SessionTimer, SessionTimer.init);
