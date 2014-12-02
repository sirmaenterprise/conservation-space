(function($) {
	
	"use strict";

    $.itoc = function(options) {
    	// default plugin settings
        var defaults = {
            source: 'body',
            scrollTime: 500,
            eventsObject: {}
        }

        var plugin = this;
        var selectedId;
        
        var HEADERS_SELECTOR = "h1,h2,h3,h4,h5,h6";
        var COLLAPSED_CLASS = "idoc-visual-collapsed";
        var COLLAPSED_HIDDEN_CLASS = "idoc-visual-collapsed-hidden";
        var EXPANDED_ICON_CLASS = "ui-icon-triangle-1-s";
        var COLLAPSED_ICON_CLASS = "ui-icon-triangle-1-e";
        var SECTION_COLLAPSE_BUTTON_CLASS = "idoc-section-collapse-header";
        
        plugin.settings = {}
        

        /**
         * Plugin initialization. This function is responsible for merging the default settings
         * with the user provided ones. Also it registers an event handler for the 'document-content-changed' event.
         */
        var init = function() {
            plugin.settings = $.extend({}, defaults, options);
            $(plugin.settings.eventsObject).on('document-content-changed', plugin.refresh);
            $(plugin.settings.eventsObject).on('document-heading-selected', plugin.selectItem);
            $(plugin.settings.eventsObject).on('document-heading-remove-selection', plugin.removeSelection);
        }

        /**
         * Builds the actual navigation tree by iterating headings level one through six.
         */
        var build = function() {
            var subtreeHistory = [];
            var currentSubtree = null; 
            var currentLeaf = $(plugin.settings.target);
            var level = 0;
            
            // Loop through all child headings on the source element.
            $(HEADERS_SELECTOR, $(plugin.settings.source)).each(function(index) {
                var heading = $(this);

                heading
                	.css({ position: 'relative' })
                	.addClass('commented-object')
                	.off('dragover dragenter drop')
                	.on('dragover dragenter drop', function(event) {
                		event.stopPropagation();
                		event.preventDefault();
                		return false;
                	});
                
                // if empty section is created add default text (placeholder) in it
                // REVIEW: if(heading.text())
                if(heading.text() == '') {
                	// REVIEW: chain please
                	heading.text('Empty section');
                	heading.addClass('section-placeholder');
                	heading.removeClass('idoc-visual-heading-selected');
                }

                var currentLevel = parseInt(heading.prop('tagName').substr(1));
                
                if (level > currentLevel) {
                	// getting up in the tree
                    currentLeaf = createListItem(heading);
                    
                    /* find the appropriate sub-tree - loop through the sub-tree history until a level match is found,
                     * or unpil the history list is empty (in that case the last element to be popped out will be used).
                     * 
                     * TODO: clarify  if we are going to build the tree like word and Goggle Docs i.e
                     * if the user has skipped a level (h3 right after h1) are we going to add empty
                     * sub trees - if we are, then this if-else logic here can be replaced with much simpler one.
                     */
                    var entry = subtreeHistory.pop();
                    if (entry.level == currentLevel) {
                        currentSubtree = entry.tree;
                    } else {
                        while (subtreeHistory.length != 0) {
                            entry = subtreeHistory.pop();
                            if (entry.level == currentLevel) {
                                currentSubtree = entry.tree;
                                break;
                            }
                        }
                    }
                    subtreeHistory.push({ level: currentLevel, tree: currentSubtree});
                    currentSubtree.append(currentLeaf);
                } else if (level < currentLevel) {
                	// check if the leaf was actually created
                	if (currentLeaf != null) {
                	
	                	// getting deeper in the tree 
	                    currentSubtree = $('<ul />');
	                    currentLeaf.append(currentSubtree);
	                    
	                    currentLeaf = createListItem(heading);
	                    currentSubtree.append(currentLeaf);
	
	                    // register a new sub-tree - this is needed when we are getting out of the tree
	                    subtreeHistory.push({ level: currentLevel, tree: currentSubtree });
                	}
                } else {
                    currentLeaf = createListItem(heading);
                    currentSubtree.append(currentLeaf);
                }
                level = currentLevel;
            });

            // Throw an event for all interested parties letting them know that there's a change in the headings
            $(plugin.settings.eventsObject).trigger('headings-changend');
            
            var navigationRoot = EMF.util.jqById("idoc-navigation");
            
            if (plugin.settings.previewMode == false) {
	            navigationRoot.children("ul").nestedSortable({
	            	cursor: "move",
	            	forcePlaceholderSize: true,
	            	items: "li",
	            	placeholder: "idoc-nav-placeholder",
	            	tolerance: "pointer",
	            	toleranceElement: "> a",
	            	listType: "ul",
	            	start: function(event, ui){
	            		// REVIEW: why height is not directly placed as css like the display, this will be optimal
	            		var $span = $("<span/>").css("display","block").height(ui.item.height());
	            		// REVIEW: all DOM functions have alternatives like: append/appendTo, after/insertAfter, ...
	            		// the meaning is that you can chain DOM element creation and append immediately without 
	            		// the need to temporary cache the element
	            		// $("<span/>").css({'display':'block', 'height':ui.item.height()+'px'}).appendTo(ui.placeholder)
	            	    ui.placeholder.append($span);        
	            	},
	            	stop: function(event, ui) {
	            		var moved = moveSection(ui.item, navigationRoot);
	            		
	            		//show error message if the section cannot be moved
	            		if (!moved) {
	            			EMF.dialog.notify({
	            				title: _emfLabels['error'],
	            				message: _emfLabels['idoc.section.dnd.not_allowed']
	            			});
	            		}
	            		
	            		plugin.refresh();
	                	$('.idoc-comments').trigger('commentRefresh');
	                	$('.idoc-comments').trigger('commentResize');
	                	
	            		
	            		//always cancel the dnd as we will refresh the navigation tree manually
	            		//and don't need jquery sortable to refresh it
	            		//if not cancelled click on section will be triggered
	            		return false;
	            	}
	            });
            }
            
            $(document).trigger('idoc-comments-integration:dom:update');
        }

		/**
		 * Generates a list item (<li />) to be used in the tree.
		 * This function also checks if the heading has an 'id' and generates one if it doesn't.
		 * Also a click handler is registered to the link inside the item to handle the actual navigation in the document.
		 * Nodes for sections that are collapsed are NOT created.
		 */
        var createListItem = function(heading) {
        	//Don't create list item for collapsed (hidden) elements
        	if (heading.hasClass(COLLAPSED_HIDDEN_CLASS)) {
        		return null;
        	}
        	
            var headingId = heading.attr('id');
            if(!headingId){
            	// prepend the emf uri prefix when generating id so the id becomes a real emf id
            	headingId = EMF.uriPrefix + EMF.util.generateUUID();
                heading.attr('id', headingId);
                $('.idoc-comments').trigger('commentRefresh');
            }
            
            heading.addClass('idoc-heading');

            var item = $('<li />');
            var headingClone = heading.clone();
            headingClone.find('widget').remove();
            
            //construct button for collapse/expand section
            var iconClass;
            if (heading.hasClass(COLLAPSED_CLASS)) {
            	iconClass = COLLAPSED_ICON_CLASS;
            } else {
            	iconClass = EXPANDED_ICON_CLASS;
            }

            var collapseExpandHandler = function(event) {
            	//stop propagation to the idoc editor
            	event.stopPropagation();
            	collapseExpandSectionDebounced(heading);
            }
            
            //add collapse/expand button in the navigation tree
            var treeCollapseExpandHandleHtml = '<span class="idoc-section-collapse ui-icon ' + iconClass + '"></span>';
            $(treeCollapseExpandHandleHtml).click(collapseExpandHandler).appendTo(item);
            
            //add collapse/expand button to each heading only if there isn't one
            var headingCollapseExpandHandleHtml = '<span unselectable="on" class="' + SECTION_COLLAPSE_BUTTON_CLASS + ' ui-icon ' + iconClass + '" contenteditable="false"></span>';
	        $(headingCollapseExpandHandleHtml).mouseup(collapseExpandHandler).appendTo(heading);
	        
            //constructs link for navigation to a section
            var anchor = $('<a class="idoc-section-link" href="javascript:void(0);" data-ref-id="'+ headingId +'" />').html(headingClone.text());
            //when the link gets clicked it should scroll the idoc content container to the relevant heading
            anchor.click(function(event) {
            	var topPosition = document.getElementById(headingId).offsetTop;
            	
            	// Fix for CMF-4021. Somethimes offset is not calculated correctly
            	if(document.getElementById(headingId).offsetParent.id != "idoc-editor") {
            		topPosition = document.getElementById(headingId).offsetTop - 270;
            	}
            	
                $('.idoc-content-container').animate({
                    scrollTop: topPosition 
                }, plugin.settings.scrollTime);
  
                $(plugin.settings.eventsObject).trigger('document-toc-item-selected', headingId);
                event.preventDefault();
            });

            // if document is in edit mode delete button is added to all available sections
            //TODO see why this gets 'undefined' when performing DnD
            if (plugin.settings.previewMode == false) {
	            var btnDelete = $('<span class="button-delete glyphicons remove_2">&#160;</span>');
	            
	            btnDelete.click(
		        function(event) {
	            	 var confirmDelete = confirm(_emfLabels['idoc.section.delete.confirmation']);
		             if (confirmDelete==true) {

			             var smallerOrEqualHeadings = getSmallerOrEqualHeadings(heading);
			             heading.nextUntil(smallerOrEqualHeadings).remove();
			             // REVIEW: not so important but end().remove() can be used
			             heading.remove();
			             	
			             idoc.triggerGlobalEvent('document-content-changed', false);
			             
			             event.preventDefault();	
			             event.stopPropagation();
		             }
	            });
	            anchor.append(btnDelete); 
            }
            item.append(anchor);
            return item;
        }
        
        /**
         * Collapse/expand sections with a debounced function because if the user click many times (fast),
         * it breaks the content.
         */
        var collapseExpandSectionDebounced = _.debounce(collapseExpandSection, 1000, true);
        
        /** Gets all sections in the provided section (heading) that until it finds a section with the same
         *  heading number or smaller. I.e. if you collapse a section with H2 it hides all elements until
         *  H1 or H2 is found. 
         */
        function collapseExpandSection(heading) {
        	var smallerOrEqualHeadings = getSmallerOrEqualHeadings(heading);

        	// Fix for issue - CMF-6503. Sections should not be toggled two times.
        	// TODO This code should be improved
        	$.each(heading.nextUntil(smallerOrEqualHeadings), function(i, node) {
       		
        		var headingsArray = smallerOrEqualHeadings.split(",");
        		var headingsSize = headingsArray.length - 2;

        		if(($(node).prop("tagName").substring(1, 0) == 'H') && ($(node).prop("tagName").substring(1) > headingsArray[headingsSize].substring(1)) 
        				&& $(node).hasClass(COLLAPSED_CLASS)) {
        			var smaller = getSmallerOrEqualHeadings($(node));
        			$(node).nextUntil(smaller).toggleClass(COLLAPSED_HIDDEN_CLASS).toggle();
        			$(node).toggleClass(COLLAPSED_CLASS);
        		}
        	});

            heading.nextUntil(smallerOrEqualHeadings).toggleClass(COLLAPSED_HIDDEN_CLASS).toggle("blind",function() {
            	$('.idoc-comments').trigger('commentRefresh');
            });
        	heading.toggleClass(COLLAPSED_CLASS);
        	plugin.refresh();
        }
        
        /**
         * When the element is dropped finds if there is a previous sibling in the parent. If there is such,
         * the sections should be inserted in the new parent just after the previous sibling. If there isn't next sibling,
         * the section will be prepended to the new parent.
         */
        function moveSection(movedElement, root) {
        	//Maximal level is H6 perform checks to see if there are children of the moved section that has to be relabeled to H6
        	
        	var sectionHeading = EMF.util.jqById(movedElement.children("a").attr("data-ref-id")); 
        	var smallerOrEqualHeadings = getSmallerOrEqualHeadings(sectionHeading);
        	
        	var sectionContent = sectionHeading.nextUntil(smallerOrEqualHeadings);
        	
        	var prevSibling = movedElement.prev();
        	if (prevSibling.length > 0) {
        		//if there is a previous sibling, insert content after it
        		var prevSectionHeading = EMF.util.jqById(prevSibling.children("a").attr("data-ref-id"));
        		
        		if (canMove(sectionHeading, sectionContent, prevSectionHeading.prop("tagName"))) {
        			//detach the section heading and its content so it doesn't interfere the algorithm
                	sectionHeading.detach();
                	sectionContent.detach();
	        	
	        		//find the content of the previous sibling
	        		var prevSectionContent = prevSectionHeading.nextUntil(getSmallerOrEqualHeadings(prevSectionHeading));
	        		
	        		//if there is no content use the heading as it's the last element within the section
	        		if (prevSectionContent.length == 0) {
	        			prevSectionContent = prevSectionHeading;
	        		}
        		
        			//insert the dropped heading and its content just after the end of the content of the previous section
	        		// REVIEW: consider one invocation instead two
	        		//var a = $('<b>1</b>');
	        		//var b = $('<b>2</b>');
	        		//a.add(b).insertAfter('div');
            		sectionHeading.insertAfter(prevSectionContent.last());
            		sectionContent.insertAfter(sectionHeading);
            		
            		recalculateHeadings(sectionHeading, sectionContent, prevSectionHeading.prop("tagName"));
            		return true;
        		}
        	} else {
        		var nextSibling = movedElement.next();
        		if (nextSibling.length > 0) {
        			var nextSectionHeading = EMF.util.jqById(nextSibling.children("a").attr("data-ref-id"));
        			
        			if (canMove(sectionHeading, sectionContent, nextSectionHeading.prop("tagName"))) {
        				//detach the section heading and its content so it doesn't interfere the algorithm
        	        	sectionHeading.detach();
        	        	sectionContent.detach();
        				
        				//insert the dropped heading and its content just before the heading of the next section
        	        	// REVIEW: consider one invocation instead two
                		sectionHeading.insertBefore(nextSectionHeading);
                		sectionContent.insertAfter(sectionHeading);
                		
                		recalculateHeadings(sectionHeading, sectionContent, nextSectionHeading.prop("tagName"));
                		return true;
        			}
        		} else {
	        		//if there isn't a next or previous sibling insert the content directly after the content of the root heading
        			//in the  parent section
	        		var parentHeading = EMF.util.jqById(movedElement.parent().closest("li").children("a").attr("data-ref-id"));
	        		if (parentHeading.length > 0) {
	        			
	        			var parentHeadingNumber = "H" + (parseInt(parentHeading.prop("tagName").substring(1)) + 1)
		        		if (canMove(sectionHeading, sectionContent, parentHeadingNumber)) {
		        			//detach the section heading and its content so it doesn't interfere the algorithm
	        	        	sectionHeading.detach();
	        	        	sectionContent.detach();
		        			
		        			var parentContent = parentHeading.nextUntil(HEADERS_SELECTOR);
		        			//if there is no content use the heading as it's the last element within the section
		            		if (parentContent.length == 0) {
		            			parentContent = parentHeading;
		            		}
		            		// REVIEW: consider one invocation instead two
		        			sectionHeading.insertAfter(parentContent);
		            		sectionContent.insertAfter(sectionHeading);
		            		//recalculate heading using the heading number of the parent + 1
		            		recalculateHeadings(sectionHeading, sectionContent, parentHeadingNumber);
		            		return true;
	            		}
	        		}
        		}
        	}
        	
        	return false;
        }
        
        /** Constructs an array with tag names of the headings with equal or smaller number for a given heading */
        function getSmallerOrEqualHeadings(heading) {
        	//collect a list with all elements
        	var smallerOrEqualHeadings = "";
        	var headingNumber = heading.prop("tagName").substring(1);
        	for (var i=1;i<=headingNumber;i++) {
        		smallerOrEqualHeadings = smallerOrEqualHeadings + "H" + i + ",";
        	}
        	
        	return smallerOrEqualHeadings;
        }
        
        /**
         * After moving the section headings number need updating according to the level of nesting. Nesting on a level deeper
         * than six is impossible because there are no H7,H8,... tags.
         */
        function canMove(sectionHeading, sectionContent, newSectionTag) {
        	var sectionTag = sectionHeading.prop("tagName");
        	
        	if (newSectionTag != sectionTag) {
        		var difference = parseInt(newSectionTag.substring(1)) - parseInt(sectionTag.substring(1));
        	
	        	var highestHeading = sectionTag;
	        	
	        	sectionContent.filter(HEADERS_SELECTOR).each(function() {
	    			if (highestHeading == null || this.tagName > highestHeading) {
	    				highestHeading = this.tagName;
	    			}
	    		});
	        	
	        	var newHeadingNumber = parseInt(highestHeading.substring(1)) + difference;
	        	if (newHeadingNumber > 6) {
	        		return false;
	        	}
        	}
        	
        	return true;
        }
        
        /** When a section is moved into another place, its heading has to be recalculated according to the new position.
         * I.e. if the section was on second level (H2) and it has children with H3 but now it's moved on root position, its heading
         * has to be changed to H1 and the headings of its children has to be changed to H2 instead of H3.
         * 
         * @param newSectionTag the new root heading tag
         */
        function recalculateHeadings(sectionHeading, sectionContent, newSectionTag) {
        	var sectionTag = sectionHeading.prop("tagName");
        	
        	if (sectionTag != newSectionTag) {
        		//calculate tag difference
        		var difference = parseInt(newSectionTag.substring(1)) - parseInt(sectionTag.substring(1));
        		
        		//update the headings of the child section headings
        		sectionContent.filter(HEADERS_SELECTOR).each(function() {
        			updateHeading($(this), difference);
        		});
        		
        		//update the heading of the section
        		updateHeading(sectionHeading, difference);
        	}
        }
        
        /** Updates the heading of an element with the provided difference. I.e. if the heading is H2 and the difference is
         * 1, then the heading becomes H3. If the difference is -1, the heading becomes H1. */
        function updateHeading(heading, difference) {
        	var tagName = heading.prop("tagName");
        	var newHeadingNumber = parseInt(tagName.substring(1)) + difference;
        	heading.replaceWith("<H" + newHeadingNumber + ">" + heading.html() + "</H" + newHeadingNumber +  ">");
        }
        
        /** 
         * This function is used to rebuild the navigation tree.
         * It first removes the existing tree and then calls the 'build' function.
         */
        plugin.refresh = function(e, previewMode) {
        	if (typeof(previewMode) !== "undefined") {
        		plugin.settings.previewMode = previewMode;
        	}
        	
        	//clear navigation tree
            $(plugin.settings.target).children().remove();
            
            //remove all collapse/expand buttons from the headings
            $('.' + SECTION_COLLAPSE_BUTTON_CLASS).remove();
            
            //rebuild navigation
            build();
            $(plugin.settings.eventsObject).trigger('document-heading-selected', selectedId);
            $(plugin.settings.eventsObject).trigger('document-toc-item-selected', selectedId);
        }
        
        plugin.selectItem = function(e, id) {
        	var tocWrapper = $(plugin.settings.target);
        	plugin.removeSelection();
        	var anchor = $('a[data-ref-id="'+ id +'"]', tocWrapper);
        	anchor.addClass('itoc-selected');
        	selectedId = id;
        }
        
        plugin.removeSelection = function() {
        	var tocWrapper = $(plugin.settings.target);
        	$('a.itoc-selected', tocWrapper).removeClass('itoc-selected');
        	selectedId = '';
        }

        init();
        build();
    }

})(jQuery);