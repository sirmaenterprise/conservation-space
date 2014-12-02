(function() {
	'use strict';
	
	var module = angular.module('emfCommentsServices', []);
	
	module.filter('objectFilter', function() {
	    return function(items, filter) {
	        if (!filter) {
	            return items;
	        }
	        return _.filter(items, filter);
	    }
	});
	
	module.filter('dateFormatterFilter', function() {
		return function(isoDateString) {
			var date = new Date(isoDateString);
			return EMF.date.format(date, SF.config.dateFormatPattern) + ' ' + EMF.date.getDateTimeSeconds(date);
		}
	});
	
	module.filter('htmlAsString', function() {
		return function(item, toText) {
			if (toText) {
				// TODO: better way to extract text from html?
				return $('<div>' + item + '</div>').text()
			}
			return item;
		}
	});
	
	module.provider('CommentsConfig', function() {
	    this.defaults = {
	    	showCreateTopicBtn: true,
	    	topicPlaceholderText: _emfLabels['comments.topic.placeholder'] || '',
	    	replyPlaceholderText: _emfLabels['comments.reply.placeholder'] || '',
	    	topicDeleteMessage: _emfLabels['comments.delete.topicMessage'] || '',
	    	replyDeleteMessage: _emfLabels['comments.delete.replyMessage'] || ''
	    };
	 
	    this.$get = function() {
	        return this.settings || this.defaults;
	    };
	    
	    this.updateConfig = function(settings) {
	    	this.settings = angular.extend({ }, this.defaults, settings);
	    }
	});
	
	module.service('TagsService', [function() {
		var allTags = { };

		//http://stackoverflow.com/a/7616484/325489
		function hashString(str) {
			  var hash = 0, i, chr, len;
			  if (str.length == 0) {
				  return hash;
			  }
			  for (i = 0, len = str.length; i < len; i++) {
			    chr   = str.charCodeAt(i);
			    hash  = ((hash << 5) - hash) + chr;
			    hash |= 0; 
			  }
			  return hash;
		}
		
		return {
			getAll: function() {
				var all = [ ];
				_.forOwn(allTags, function(value, key) {
					all.push(value.id);
				});
				return all;
			},
			
			addAll: function(tags) {
				if (!tags) {
					return;
				}
				var allToAdd = tags,
					_this = this;
				if (typeof tags === 'string') {
					allToAdd = tags.split('|');
				}
				_.each(allToAdd, function(tag) {
					_this.add({ id: tag });
				});
			},
			
			add: function(tag) {
				if (!tag) {
					return;
				}
				var key = hashString(tag.id),
					data = allTags[key];
				if (!data) {
					data = { id: tag.id };
				}
				
				if (!data.count) {
					data.count = 0;
				} 
				data.count++;
				allTags[key] = data;
			},
			
			remove: function(tag) {
				if (!tag) {
					return;
				}
				var key = hashString(tag.id),
					data = allTags[key];
				data.count--;
				if (!data.count) {
					delete allTags[key];
				} else {
					allTags[key] = data;
				}
			}
		}
	}]);
	
	module.service('CommentsService', ['$http', function($http) {
		var topicsUrl = EMF.servicePath + '/topics';
		return {
			saveTopic: function(topic) {
				var promise;
				if (topic.Id) {
					promise = $http.put(topicsUrl + '/' + topic.Id, topic);
				} else {
					promise = $http.post(topicsUrl, topic);
				}
				return promise;
			},
			
			deleteTopic: function(id) {
				return $http.delete(topicsUrl + '/' + id);
			},
			
			deleteReply: function(topicId, id) {
				return $http.delete(topicsUrl + '/' + topicId + '/replies/' + id);
			},
			
			loadTopics: function(params) {
				return $http.get(topicsUrl, { params: params });
			},
			
			count: function(params) {
				return $http.get(topicsUrl + '/count', { params: params });
			},
			
			saveReply: function(reply) {
				var promise;
				if (reply.Id) {
					promise = $http.put(topicsUrl + '/' + reply.replyTo + '/replies/' + reply.Id, reply);
				} else {
					promise = $http.post(topicsUrl + '/' + reply.replyTo + '/replies', reply);
				}
				return promise;
			},
			
			loadTopicReplies: function(topic) {
				return $http.get(topicsUrl + '/' + topic.Id + '/replies');
			},
			
			loadTopicActions: function(topic) {
				return $http.get(topicsUrl + '/' + topic.Id + '/actions');
			}
		}
	}]);
	
	
	
}());