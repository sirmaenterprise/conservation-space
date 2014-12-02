;
/*
 * TipTip
 * Copyright 2010 Drew Wilson
 * www.drewwilson.com
 * code.drewwilson.com/entry/tiptip-jquery-plugin
 *
 * Version 1.3   -   Updated: Mar. 23, 2010
 *
 * This Plug-In will create a custom tooltip to replace the default
 * browser tooltip. It is extremely lightweight and very smart in
 * that it detects the edges of the browser window and will make sure
 * the tooltip stays within the current window size. As a result the
 * tooltip will adjust itself to be displayed above, below, to the left 
 * or to the right depending on what is necessary to stay within the
 * browser window. It is completely customizable as well via CSS.
 *
 * This TipTip jQuery plug-in is dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

(function($){
	$.fn.tipTip = function(options) {
		
		var defaults = { 
			activation: "hover",
			keepAlive: false,
			maxWidth: "600px",
			edgeOffset: 3,
			defaultPosition: "bottom",
			delay: 200,
			fadeIn: 100,
			fadeOut: 100,
			attribute: "title",
			tooltipContentSource: '.tooltip',
			tooltipActivator: '.has-tooltip',
			content: false, // HTML or String to fill TipTIp with
		  	enter: function(){},
		  	exit: function(){}
	  	};
		
	 	var opts = $.extend(defaults, options);
	 	
	 	// Setup tip tip elements and render them to the DOM
	 	if($("#tiptip_holder").length <= 0){
	 		var tiptip_holder = $('<div id="tiptip_holder" style=""></div>');
			var tiptip_content = $('<div id="tiptip_content"></div>');
			var tiptip_arrow = $('<div id="tiptip_arrow"></div>');
			$("body").append(tiptip_holder.html(tiptip_content).prepend(tiptip_arrow.html('<div id="tiptip_arrow_inner"></div>')));
		} else {
			var tiptip_holder = $("#tiptip_holder");
			var tiptip_content = $("#tiptip_content");
			var tiptip_arrow = $("#tiptip_arrow");
		}
		
		return this.each(function() {
			var container = $(this);
			var org_elem;
			var org_title = '';
			var timer;
			
			container.on('mouseover focus', opts.tooltipActivator, function(evt) {
				var element = $(this);
				ajaxLoadActive = false;
				
				if(timer) {
					clearTimeout(timer);
					timer = null
				}
				
				changeCursor($(evt.target), 'progress');
				timer = setTimeout(function() {
					var tooltipContent = searchInElement.siblings(element);
					
					if(tooltipContent.length === 0) {
						tooltipContent = searchInElement.children(element);
						if (tooltipContent.length > 0) {
							contentReady(element, tooltipContent);
							changeCursor($(evt.target), 'pointer');
							return;
						}
					}
					
					if(tooltipContent.length === 0) {
						searchInElement.anchor(element);
						changeCursor($(evt.target), 'pointer');
						return;
					}
					
					contentReady(element, tooltipContent);
					changeCursor($(evt.target), 'pointer');
				}, opts.delay);
			})
			.on('mouseout', opts.tooltipActivator, function(evt) {
				changeCursor($(evt.target), 'default');
				deactive_tiptip();
			});
			
			function contentReady(element, tooltipContent) {
				var ttipEl;
				if(element.is('path') || element.is('line') || element.is('ellipse') || element.is('rect')) {
					ttipEl = $("#info_panel").html();
					org_title = $("#info_panel").html();
				} else {
					org_title = $(tooltipContent).html();
				}
				
				org_elem = element;
				
				if (org_title !== '') {
					org_elem.removeAttr(opts.attribute); 
					active_tiptip();					
				}
			};
			
			function changeCursor(element, cursorType) {
				if(element) {
					var current = element.jquery ? element[0] : element;
					current.style.cursor = cursorType;
					document.getElementsByTagName('html')[0].style.cursor = cursorType;
				}
			};

			function active_tiptip(){
				opts.enter.call(this);
				tiptip_content.html(org_title);
				tiptip_holder.hide().removeAttr("class").css("margin","0");
				tiptip_arrow.removeAttr("style");
				// Calculate tool-tip for parent if width is bigger
				// then those of the parent.
				// This fix tool-tip positioning when element is overflowed and text is large
				if(org_elem.parent().width() < org_elem.width()){
					org_elem = org_elem.parent();
				}
				var top = parseInt(org_elem.offset()['top']);
				var left = parseInt(org_elem.offset()['left']);
				var org_width = parseInt(org_elem.outerWidth());
				var org_height = parseInt(org_elem.outerHeight());
				var tip_w = tiptip_holder.outerWidth();
				var tip_h = tiptip_holder.outerHeight();
				var w_compare = Math.round((org_width - tip_w) / 2);
				var h_compare = Math.round((org_height - tip_h) / 2);
				var marg_left = Math.round(left + w_compare);
				var marg_top = Math.round(top + org_height + opts.edgeOffset);
				var t_class = "";
				var arrow_top = "";
				var arrow_left = Math.round(tip_w - 12) / 2;

                if(opts.defaultPosition == "bottom"){
                	t_class = "_bottom";
               	} else if(opts.defaultPosition == "top"){ 
               		t_class = "_top";
               	} else if(opts.defaultPosition == "left"){
               		t_class = "_left";
               	} else if(opts.defaultPosition == "right"){
               		t_class = "_right";
               	}
				
				var right_compare = (w_compare + left) < parseInt($(window).scrollLeft());
				var left_compare = (tip_w + left) > parseInt($(window).width());
				
				if((right_compare && w_compare < 0) || (t_class == "_right" && !left_compare) || (t_class == "_left" && left < (tip_w + opts.edgeOffset + 5))){
					// toolType position based on the element - right
					t_class = "_right";
					arrow_top = Math.round(tip_h - 13) / 2;
					arrow_left = -12;
					marg_left = Math.round(left + org_width + opts.edgeOffset);
					marg_top = Math.round(top + h_compare);
				} else if((left_compare && w_compare < 0) || (t_class == "_left" && !right_compare)){
					// toolType position based on the element - left
					t_class = "_left";
					arrow_top = Math.round(tip_h - 13) / 2;
					arrow_left =  Math.round(tip_w);
					marg_left = Math.round(left - (tip_w + opts.edgeOffset + 10));
					marg_top = Math.round(top + h_compare);
				}

				var top_compare = (top + org_height + opts.edgeOffset + tip_h + 8) > parseInt($(window).height() + $(window).scrollTop());
				var bottom_compare = ((top + org_height) - (opts.edgeOffset + tip_h + 8)) < 0;
				
				if(top_compare || (t_class == "_bottom" && top_compare) || (t_class == "_top" && !bottom_compare)){
					if(t_class == "_top" || t_class == "_bottom"){
						t_class = "_top";
					} else {
						t_class = t_class+"_top";
					}
					// toolType position is above the element
					// define the arrow margin
					arrow_top = tip_h;
					// define the toolType container position
					marg_top = Math.round(top - (tip_h + 10 + opts.edgeOffset));
				} else if(bottom_compare | (t_class == "_top" && bottom_compare) || (t_class == "_bottom" && !top_compare)){
					if(t_class == "_top" || t_class == "_bottom"){
						t_class = "_bottom";
					} else {
						t_class = t_class+"_bottom";
					}
					arrow_top = -12;						
					marg_top = Math.round(top + org_height + opts.edgeOffset);
				}
			
				if(t_class == "_right_top" || t_class == "_left_top"){
					marg_top = marg_top + 5;
				} else if(t_class == "_right_bottom" || t_class == "_left_bottom"){		
					marg_top = marg_top - 5;
				}
				if(t_class == "_left_top" || t_class == "_left_bottom"){	
					marg_left = marg_left + 5;
				}
				tiptip_arrow.css({"margin-left": arrow_left+"px", "margin-top": arrow_top+"px"});
				tiptip_holder.css({"margin-left": marg_left+"px", "margin-top": marg_top+"px"}).attr("class","tip"+t_class);
				
				if (timer){ clearTimeout(timer); }
				timer = setTimeout(function(){ tiptip_holder.stop(true,true).fadeIn(opts.fadeIn); }, opts.delay);	
			}
				
			function deactive_tiptip(){
				opts.exit.call(this);
				if (timer){ clearTimeout(timer); }
				tiptip_holder.fadeOut(opts.fadeOut);
			}
			
			var activeRequests = {};
			var ajaxLoadActive = false;
			
			function hasActiveRequest(instanceId) {
				if (!activeRequests[instanceId]) {
					activeRequests[instanceId] = true;
					return false;
				}
				return true;
			};
			
			function requestCompleted(instanceId) {
				if (activeRequests[instanceId]) {
					delete activeRequests[instanceId];
				}
			};
				
			// Object that holds specific search operations and
			// utils that are needed for the operations.
			var searchInElement = {
					
				/**
				 * Search for tool-tip in all siblings.
				 * 
				 * return tool-tip element
				 */
				siblings : function(element){
					var el = $(element);
					return el.siblings(opts.tooltipContentSource);
				},
				
				/**
				 * Search for tool-tip in all children.
				 * 
				 * return tool-tip element
				 */
				children : function(element){
					var el = $(element);
					return el.children(opts.tooltipContentSource);
				},
					
				/**
				 * Search for tool-tip in anchor. Here the link will be checked
				 * for title attribute and will use the value for tool-tip or 
				 * will be checked for href attribute with specific values(type,id).
				 * These values will be send with AJAX request to the REST service
				 * for retrieving and using the instance header as tool-tip.
				 */
				anchor	 : function(element) {
					var el = $(element);
					var elTitleAttr = el.attr('title');
					var ttipEl = null;
					
					// check for anchor element
					if(el.is('a')) {
						// check for valid title attribute
						if(elTitleAttr && elTitleAttr.length) {
							var template = '<span class="tooltip">' + el.attr('title') + '</span>';
							var elNext = el.next();
							if(!elNext.hasClass('tooltip')) {
								//caching to new tool-tip
								el.after(template);
								elNext = el.next();
							}
							ttipEl = elNext;
							contentReady(element, ttipEl);
						} else {
							// extract type and id from href attribute
							var instanceData = searchInElement.searchUtils.extractInstanceData(el);
							if(!instanceData){
								// type and id no available
								contentReady(element, ttipEl);
								return;
							}
							
							if (!hasActiveRequest(instanceData.id)) {
								ajaxLoadActive = true;
								var data = {
									instanceId	: instanceData.id,
									instanceType: instanceData.type,
									headerType	: null
								};
								
								$.ajax({
									url		: SF.config.contextPath + "/service/tooltip",
									data	: data,
									type	: 'GET',
									// fix IE-11 - caching ajax requests
									cache   : false
								})
								.done(function(data, textStatus, jqXHR) {
									ttipEl = '<span class="tooltip" style="display:none;">' + data.tooltip + '</span>';
									var elNext = el.next();
									if(!elNext.hasClass('tooltip')){
										//caching the new tool-tip
										el.after(ttipEl);
										ttipEl = el.next();
										ttipEl.find('span').each(function(){
											$(this).removeAttr('href');
										});
									}
									requestCompleted(instanceData.id);
									if (ajaxLoadActive) {
										contentReady(element, ttipEl);
										ajaxLoadActive = false;
									}
								})
								.fail(function(data, textStatus, jqXHR) {
									console.info('Failed to retrieve tooltip: ', data, arguments);
								});
							}
						}
						
					} else if(el.is('path') || el.is('line') || el.is('ellipse') || el.is('rect')) {
					    ttipEl = '<span class="tooltip"></span>';
					    contentReady(element, ttipEl);
					}
				},
					
				/**
				 * Holds all actions that will be needed for search operations.
				 */
				searchUtils : {
					
					/**
					 * Extracting type and id from href attribute of anchor.
					 */
					extractInstanceData : function(element){
						var el = $(element);
						
						// data holder
						var instanceData = {
							id   : null,
							type : null
						};
						
						var href = el.attr('href');
						
						if(!$.trim(href).length){
							// instance data not available
							return null;
						}
						
						var data = href.split('&');
						
						if(!data.length){
							return null;
						}
						
						var typeData = data[0].split('=');
						var idData	 = data[1].split('=');
						
						instanceData.type = typeData[1];
						instanceData.id	  = idData[1];
						
						return instanceData;
					}
				}
			}
		});
	}
})(jQuery);  	