/**
 * @param formId
 *        The form id where the fields are nested.
 * @param rnc
 *        The rnc object that holds expressions to be evaluated.
 */
;
(function($, window, document, undefined) {

	var pluginName = 'RNC';

	var warnings = {
		WARN : 'WARN'
	};

	// defaults
	var defaults = {
		debugEnabled : true,
		hideEffect : 'hide',
		showEffect : 'show',
		formId : '',
		basePath : '',
		saveCondition : '',
		rnc : {}
	};

	/**
	 * Plugin constructor.
	 */
	function RNC(container, options) {
		this.container = container;

		// merge the default options with provided once
		this.options = $.extend(true, {}, defaults, options);

		this._defaults = defaults;
		this._name = pluginName;
		// call init to build the plugin view and handlers
		this.init();
	}

	/**
	 * Initialize the plugin.
	 */
	RNC.prototype.init = function() {

		// check the container
		if (!this.checkPluginContainer()) {
			// if container is missing, then return
			return;
		}

		var opts = this.options;

		opts.rnc = opts.rnc || CMF.RNC;
		opts.basePath = opts.formId + '\\:' + opts.rnc.basePath;
		opts.saveCondition = opts.rnc.saveCondition;
		opts.conditions = opts.rnc.conditions;
		opts.saveButton = opts.formId + '\\:' + opts.saveButtonId;
		// if there is saveCondition provided, then add it to conditions object
		// !!! default outcome from saveCondition is ENABLED !!!
		if (opts.saveCondition) {
			var saveConditionObj = {
				'id' : opts.saveButton,
				'condition' : [{
					'expression' : opts.saveCondition,
					'renderAs' : 'ENABLED'
				}]
			};
			opts.conditions.push(saveConditionObj);
		}

		//
		opts.debug = opts.debug || {};
		opts.debug.expr = [];
		opts.debug.evalexpr = [];
		opts.debug.fieldValue = [];

		var supportsStickyFlag;
		try {
			RegExp('', 'y');
			supportsStickyFlag = true;
		} catch (e) {
			supportsStickyFlag = false;
		}
		opts.supportsStickyFlag = supportsStickyFlag;

		// bind handlers
		this.bindHandlers(opts);
	};

	/**
	 * Binds the event handlers.
	 *
	 * @param opts
	 *        plugin options
	 */
	RNC.prototype.bindHandlers = function(opts) {

		// immediate execute the rnc on module init
		this.applyRNC(this);

		// bind the event handlers
		// here we explicitly bind the function context to be the plugin object itself
		this.container.on('keyup.rnc', 'input[type=text], textarea, .picklist-preview-field', $.proxy(this.applyRNC, this))
				.on('change.rnc', 'select, input[type=checkbox], input[type=radio]', $.proxy(this.applyRNC, this));

	};

	/**
	 * Call parser to transform the expression that can be evaluated. The result from expression
	 * evaluation is used to apply the outcome.
	 *
	 * @param evt
	 *        event object
	 */
	RNC.prototype.applyRNC = function(evt) {

		var opts = this.options;
		var targets = opts.conditions;

		if (!targets)
			return;

		var invertedOutcome = {
			'HIDDEN' : 'VISIBLE',
			'VISIBLE' : 'HIDDEN',
			'DISABLED' : 'ENABLED',
			'ENABLED' : 'DISABLED',
			'READONLY' : 'ENABLED'
		};

		var targetsArrLength = targets.length;
		for ( var i = 0; i < targetsArrLength; i++) {
			var target = targets[i].id;
			var targetElem = $("[id$=" + target + "]").not("[id^='s2id']");
			var conditionObjArr = eval(targets[i].condition);
			var conditionsArrayLength = conditionObjArr.length;
			for ( var z = 0; z < conditionsArrayLength; z++) {
				var conditionObj = conditionObjArr[z];
				var parsed = this.parseExpression(conditionObj);
				if (!parsed) {
					var msg = 'CMF.RNC: Can not parse expression:' + this.options.errorExpression;
					this.logMsg(msg);
					alert(msg);
					continue;
				}
				var evaluated = eval(parsed);
				this.logMsg('<' + target + ' expression>' + opts.debug.expr.join(' '));
				this.logMsg('<' + target + ' renderAs:' + conditionObj.renderAs + '>' + opts.debug.evalexpr.join(' ') + '=' + evaluated);
				this.logMsg('<' + target + '>' + opts.debug.fieldValue.join(' '));
				opts.debug.expr = [];
				opts.debug.evalexpr = [];
				opts.debug.fieldValue = [];
				if (evaluated === true) {
					this.applyOutcome[conditionObj.renderAs](targetElem, opts);
				} else {
					this.applyOutcome[invertedOutcome[conditionObj.renderAs]](targetElem, opts);
				}
			}
		}
	};

	/**
	 * Method that parse the expression received from the concrete condition. The method
	 * will trigger error message if the current expression is not supported or invalid.
	 * @param condition the specific definition condition
	 */
	RNC.prototype.parseExpression = function(condition) {
		var expr = condition.expression;
		var normalized = expr.replace(/[\s\r\n]+/g, '');
		var tokenizer = this.tokenizer(normalized, this.options);
		var tokens = [];
		while (tokenizer.hasNext()) {
			var token = tokenizer.next();
			// trigger error message on unsupported token
			if (!token) {
				var msg = 'CMF.RNC: Invalid token in expression was found!!!';
				this.logMsg(msg);
				alert(msg);
				this.options.errorExpression = normalized;
				return;
			}
			this.options.debug.expr.push(token);
			// evaluate the current token
			var value = this.evalToken(token, tokenizer, condition);
			tokens.push(value);
			this.options.debug.evalexpr.push(value);
		}
		return tokens.join('');
	};
	
	var evalTokenUtil = {
		getFieldValue : function(token, numElements, opts) {
			var selector;
			var elementsNumber = numElements || null;
			if (!elementsNumber) {
				// id selector
				selector = this.createIdSelector(this.stripBrackets(token));
			} else {
				
				// class selector
				selector = this.createClassSelector(this.stripBrackets(token));
			}
			// search element in the DOM by attribute prefix
			var elem = $(selector).not("[id^='s2id']");
			opts.debug.fieldValue.push(elem.val());
			// if condition says that more than one element should be checked like: +2[someField],
			// then our
			// selector searches by css class name and can find more than one element
			if (elementsNumber) {
				var insideElementCounter = elem.length;
				var tokenFirstChar = token.charAt(0);
				if ((tokenFirstChar === '+') || (tokenFirstChar === elementsNumber)) {
					return numElement === insideElementCounter;
				} else {
					return numElement !== insideElementCounter;
				}
			}
			// if element is a check box element
			if (elem.is(':checkbox')) {
				return elem.is(':checked') ? 'true' : 'false';
			}
			// if token matches a radio button group
			else if (elem.hasClass('radio-button-panel')) {
				return elem.find('input:checked').val();
			}
			// if element is a check box group
			else if (elem.hasClass('checkbox-panel')) {
				// actually this is multiple choice
			}
			// for injected fields we may have hidden field value if there is attached code-list
			else if (elem.hasClass('has-clvalue')) {
				return elem.siblings('input[type=hidden]').val();
			}
			// for hidden fields that contains the code of the displayed value in the code-list
			else if (elem.hasClass('cl-field')) {
				return elem.siblings('.hidden').text();
			}
			// for preview fields we should get the text from HTML tag
			else if (elem.is('span')) {
				return elem.text() ? elem.text() : '';
			}
			// pick-list value goes here too
			var elementValue = elem.val();
			if (elementValue) {
				elementValue = elementValue.replace(/\s/g, '');
			}
			return elementValue;
		},
		stripBrackets : function(token) {
			return token.replace(/(\+|\-)?(\d)?\[|\]/g, '');
		},
		createIdSelector : function(name) {
			return '[id$=' + name + ']';
		},
		createClassSelector : function(name) {
			return '.' + name;
		},
		createId : function(id) {
			return '#' + opts.basePath + id.replace(/(:|\.)/g, '\\$1');
		}
	}
	/**
	 * Transforms tokens to boolean values.
	 * @param token the token
	 * @param tokenizer the tokenizer
	 * @param condition definition condition
	 */
	RNC.prototype.evalToken = function(token, tokenizer, condition) {
		var opts = this.options;
		var choise = /^(?:\[\w+\])$/;
		var empty = /^(?:-\[\w+\])$/;
		var notEmpty = /^(?:\+\[\w+\])$/;
		var checkPrefixDigits = /(\+|\-)?(\d)\[/;
		var checkPrefixSign = /(\+|\-)/;
		var preEvaluatedResultFalse = /^(\[false\])$/;
		var joiners = {
			'AND' : '&&',
			'OR' : '||'
		};
		var funcOps = {
			'IN' : function(value, collection) {
				var quoted = (value) ? ("\'" + value + "\'") : value;
				var matched = null;
				if (collection) {
					matched = collection.match(new RegExp(quoted, 'i'));
				}
				return ((matched !== null) && matched[0] !== '');
			},
			'NOTIN' : function(value, collection) {
				var quoted = (value) ? ("\'" + value + "\'") : value;
				var matched = collection.match(new RegExp(quoted, 'i'));
				return ((matched === null) || matched[0] === '');
			}
		};
		// currently supported message builders
		var builder = {
			tooltip : function(condition){
				if(!condition){
					return;
				}
				var createMessage = function(message){
					return $('<span class="tooltip"><span>'+message+'<span></span>');
				};
				var container = $('#'+condition.id);
				container.find('.tooltip').remove();
				container.addClass('has-tooltip').append(createMessage(condition.messages.join('\n')));
			}
		};
		var value;
		var numElement = 0;
		// like: -[field]
		if (empty.test(token)) {
			value = evalTokenUtil.getFieldValue(token, null, opts);
			return (value === null) || (value === '') || (value === 'false');
			// like: [false]
		}else if(preEvaluatedResultFalse.test(token)){
			builder['tooltip'](condition);
			return false;
			// like: +1[field] or -1[field]
		}else if (checkPrefixDigits.test(token)) {
			if (checkPrefixSign.test(token)) {
				numElement = token.charAt(1);
			} else {
				numElement = token.charAt(0);
			}
			return evalTokenUtil.getFieldValue(token, numElement, opts);
			// like: +[field]
		} else if (notEmpty.test(token)) {
			value = evalTokenUtil.getFieldValue(token, null, opts);
			return (value !== null) && (value !== '') && (value !== 'false');
			// for AND, OR
		} else if (token in joiners) {
			return joiners[token];
			// like: [field] used in choice operator [field] IN ('value1', 'value2')
		} else if (choise.test(token)) {
			var funcOperator = tokenizer.next();
			var collection = tokenizer.nextCollection();
			value = evalTokenUtil.getFieldValue(token, null, opts);
			if (!funcOps.hasOwnProperty(funcOperator)) {
				var msg = 'CMF.RNC: Invalid range operator: ' + token + ' !!!' + funcOperator + '!!! ' + collection;
				this.logMsg(msg);
				alert(msg);
			}
			opts.debug.expr.push(funcOperator);
			opts.debug.expr.push(collection);
			return funcOps[funcOperator](value, collection);
		} else {
			return token;
		}
	};

	/**
	 * Iterator for expression string. Knows how to parse the expression. Provides convenient
	 * functions for iterating the expression.
	 *
	 * @param expr
	 *        An expression string.
	 * @param opts
	 * 		  plugin options
	 */
	RNC.prototype.tokenizer = function(expr, opts) {
		var pattern = /^(?:[\+|-]?(\d)?\[.*\]|AND|OR|IN|NOTIN|\(|\)|\s)$/;
		var collectionPattern = /\(.+?\)/;
		var pointer = 0;
		var exprLength = expr.length;
		var sub = '';
		var getNext = function(regex) {
			for (pointer; pointer <= exprLength; pointer++) {
				sub += expr.substring(pointer, pointer + 1);
				var match = regex.test(sub);
				if (match) {
					var token = sub;
					sub = '';
					pointer = (pointer < exprLength) ? pointer + 1 : exprLength;
					return token;
				}
			}
		};
		var getNextCollection = function(regex) {
			var subExpr = expr.substring(pointer);
			var result = collectionPattern.exec(subExpr);
			if (result) {
				pointer += result[0].length;
				return result[0];
			}
			alert("CMF.RNC: Can't match regex '" + collectionPattern + "' on string '" + subExpr + "'. Please check the definition!");
		};
		return {
			next : function() {
				return getNext(pattern);
			},
			hasNext : function() {
				return pointer < exprLength;
			},
			nextCollection : function() {
				return getNextCollection(collectionPattern);
			}
		};
	};

	/**
	 * A function chain used to apply an outcome to a target element.
	 */
	RNC.prototype.applyOutcome = {
		showHide : function(target, opts, effect) {
			if (target.hasClass('generated-region')) {
				target[effect]();
				clearValue(target.find('input, textarea, select'), effect);
			} else if (target.hasClass('cmf-button')) {
				target[effect]();
			} else if (target.hasClass('transition-action')) {
				target.find('.transition-button')[effect]();
			} else if (target.hasClass('checklist-item')) {
				target.closest('.checklist-item-wrapper')[effect]();
				clearValue(target, effect);
			} else {
				target.closest('.cmf-field-wrapper')[effect]();
				clearValue(target.find('input, textarea, select'), effect);
			}
			function clearValue(target, effect) {
				if (effect === 'slideUp') {
					if (target.length > 0) {
						target.each(function() {
							var field = $(this);
							if (field.is(':checkbox') || field.is(':radio')) {
								field.removeAttr('checked');
							} else {
								field.val('');
							}
						});
					}
				}
			}
		},

		// - form fields are actually set as readonly in order to allow tooltips to work on that
		// fields
		// - buttons are disabled
		// Same function is used for disabled and readonly mode switching. The difference is that in
		// readonly mode the fields
		// must not be cleared unlike the disabled mode.
		enableDisable : function(target, opts, enable, clear) {
			// all fields in regions, radiobutton groups and checklist components should be set
			// readonly
			if (target.hasClass('generated-region') || target.hasClass('radio-button-panel') || target.hasClass('checklist-panel')) {
				toggle(target.find('input, textarea, select'), enable, (clear && true), 'readonly');
			} else if (target.hasClass('cmf-button')) {
				// action buttons should be disabled
				toggle(target, enable, false, 'disabled');
				target.trigger('rnc:toggle-cmf-button', enable);
			} else if (target.hasClass('transition-action')) {
				// transition buttons should be disabled
				toggle(target.find('input'), enable, false, 'disabled');
			} else if (target.hasClass('picklist-hidden-field')) {
				togglePicklist(target, enable);
			} else if (target.hasClass('hasDatepicker')) {
				toggle(target, enable, (clear && true), 'readonly');
				toggleDateField(target, enable);
			} else {
				// form field should be set readonly
				toggle(target, enable, (clear && true), 'readonly');
			}

			function toggle(target, enable, clear, attribute) {
				if (enable) {
					target.removeAttr(attribute);
					target.removeClass('rnc-disabled');
					target.off('focus.cmfrnc');
				} else {
					target.attr(attribute, attribute);
					target.addClass('rnc-disabled');
					initDisabledField(target);
					if (clear) {
						clearValue(target);
					}
				}
				toggleSelect2(target, attribute, enable);
			}

			function togglePicklist(target, enable) {
				var triggerButton = target.siblings('.open-picklist');
				if (!triggerButton) {
					return;
				}
				if (enable) {
					triggerButton.removeClass('hide');
				} else {
					triggerButton.removeClass('hide').addClass('hide');
				}
			}

			function toggleSelect2(target, attr, enable) {
				// group string elements for better code reading
				var select2Attr = {
					readonly : 'readonly',
					enable : 'enable'
				}
				// check element has functionality for select2, this will
				// prevent errors
				if (target.select2) {
					// check for readonly manipulations
					if (attr == select2Attr.readonly) {
						// revert flag needed for setting it correctly
						// in select2 function
						enable = enable ? false : true;
						target.select2(attr, enable);
						return;
					}
					// disable the element
					target.select2(select2Attr.enable, enable);
				}
			}

			function clearValue(target) {
				if (target.length > 0) {
					target.each(function() {
						var field = $(this);
						if (field.is(':checkbox') || field.is(':radio')) {
							field.removeAttr('checked');
						} else {
							field.val('');
						}
					});
				}
			}

			function toggleDateField(target, enable) {
				if (enable) {
					target.datetimepicker('enable');
				} else {
					target.datetimepicker('disable');
				}
			}

			function initDisabledField(target) {
				target.on('focus.cmfrnc', function() {
					$(this).blur();
					return false;
				});
				return false;
			}
			;
		},

		'HIDDEN' : function(target, opts) {
			this.showHide(target, opts, opts.hideEffect);
		},

		'VISIBLE' : function(target, opts) {
			this.showHide(target, opts, opts.showEffect);
		},

		'DISABLED' : function(target, opts) {
			this.enableDisable(target, opts, false, true);
		},

		'ENABLED' : function(target, opts) {
			this.enableDisable(target, opts, true, false);
		},

		'READONLY' : function(target, opts) {
			this.enableDisable(target, opts, false, false);
		}
	};

	// perform logging only if console is opened
	RNC.prototype.logMsg = function(msg) {
		if (window.console && (this.options.debugEnabled)) {
			console.log(msg); // for firebug
		}
	}

	/**
	 * Checks if the provided container id exists and the container itself is in the DOM.
	 */
	RNC.prototype.checkPluginContainer = function() {
		var containerIsOk = true;
		// if container is not provided then return with error
		if (!this.container || $(this.container).length === 0) {
			console.error('Error! Container id must be provided!');
			containerIsOk = false;
		}
		return containerIsOk;
	};

	RNC.prototype.warn = {

		'WARN' : function(msg) {
			alert('Warn!' + msg);
		}

	};

	/**
	 * Extend jQuery with our function. The plugin is instantiated only once (singleton).
	 */
	$.fn[pluginName] = function(options) {
		var pluginObject = $.data(this, 'plugin_' + pluginName);

		if (!pluginObject) {
			pluginObject = $.data(this, 'plugin_' + pluginName, new RNC(this, options));
		}
		return pluginObject;
	};

})(jQuery, window, document);
