angular.module('permissions')
	.provider('PermissionsConfig', function() {
		var _this = this,
			accessFns = {
				_value: function() {
					var argsLen = arguments && arguments.length,
						argValue;
					if (!argsLen) {
						return;
					}

					if (argsLen === 1) {
						argValue = _this.config[arguments[0]];
						if (typeof argValue === 'function') {
							return argValue();
						}
						return argValue;
					} else if (argsLen === 2) {
						_this.config[arguments[0]] = arguments[1];
					}
				}
			};

		_this.config = { };

		this.$get = function() {
			return _this.config;
		};

		this.extend = function(conf) {
			angular.extend(_this.config, conf, accessFns);
		};
	});