/*
 * Turns a <select> menu into jQuery autocomplete.
 * 
  Got from http://odoepner.wordpress.com/2012/07/11/autocomplete-combobox-using-jquery-ui/
 */
 (function($) {

    $.widget("ui.combobox", {
 
        _create: function() {
            var select = this.element;
 
            if (select.is(":disabled") 
                    || select.hasClass("inputOverlayCreated")) {
                return;
            }
 
            select.hide();
 
            // set up input text element
            var input = $("<input type='text' id=" + select[0].id + "_field>");
            input.insertAfter(select);
            
 
            // remember that combobox is creates (to avoid odd duplicates)
            select.addClass("inputOverlayCreated");
 
            // the class ui-combobox-content is required for proper
            // highlighting of data changes
            input.addClass("ui-combobox-content ui-widget");
 
            input.attr("menustatus", "closed");
 
            // over-ride form submit, so it can't submit
            // if the menu is open
            var form = $(input).parents('form:first');
            $(form).submit(function(e) {
                return (input.attr('menustatus') == 'closed');
            });
            
            // set up button for fake 'select'
            //$('<span class="ui-icon ui-icon-triangle-1-s">').insertAfter(input);
            var btn = $('<span class="ui-icon ui-icon-triangle-1-s"></span>');
//            btn.attr("title", "Show All Items");
            btn.insertAfter(input);
            var offset = input.position();
            var left = input.width() + offset.left - 12 + 'px';
            var top = offset.top + 4 +'px';
            btn.css({ 'left' : left, 'top' : top });
            btn.click(menuTrigger);
            input.click(menuTrigger);
            
            function menuTrigger() {
                //event.preventDefault();
                // close if already visible
                if (input.autocomplete("widget").is(":visible")) {
                    input.autocomplete("close");
                    return false; // return false, so form isn't automatically submitted
                }
                // pass empty string as value to search for, displaying all results
                input.autocomplete("search", "");
                input.focus();
                return false; // return false, so form isn't automatically submitted
            }
        },

        _init : function() {
            var select = this.element;
 
            if (select.is(":disabled")) {
                // we don't apply any fancy combobox behaviour at all
                // if the underlying drop-down list is disabled
                return;
            }
 
            var opts = new Array();
            $('option', select).each(function(index) {
            	var _element = $(this);
	            var opt = new Object();
	            opt.val = _element.val();
	            opt.label = _element.text();
	            opts[opts.length] = opt;
            });
 
            var input = select.next();
 
            // initialise text with what's currently selected
            input.val($(':selected', select).text());
 
            input.autocomplete({
                source: opts,
                delay: 0,
                change: function(event, ui) {
                    if (!ui.item) {
                        // user didn't select an option, but what they typed may still match
                        var enteredString = $(this).val();
                        var stringMatch = false;
                        for (var i = 0; i < opts.length; i++) {
                            if (opts[i].label.toLowerCase() == enteredString.toLowerCase()) {
                                select.val(opts[i].val);// update (hidden) select
                                $(this).val(opts[i].label);// corrects any incorrect case
                                select.trigger("change");
                                stringMatch = true;
                                break;
                            }
                        }
                        if (!stringMatch) {
                            // remove invalid value, as it didn't match anything
                            $(this).val($(':selected', select).text());
                        }
                        return true;
                    }
                },
                select: function(event, ui) {
                    select.val(ui.item.val);// update (hidden) select
                    select.trigger("change");
                    return true;
                },
                // stop parent form from being while menu is open
                open: function(event, ui) {
                    input.attr("menustatus", "open");
                    //$('.ui-menu').width($(this).width()); 
                },
                close: function(event, ui) {
                    input.attr("menustatus", "closed");
                },
                minLength: 0
            });
        }
    });
 
})(jQuery);
 