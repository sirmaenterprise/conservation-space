/**
 * Centers DOM element.
 */
jQuery.fn.center = function() {
    return this.each(function() {
		var t = $(this);
		t.css({
		    left : '50%',
		    top : '50%',
		    marginLeft : '-' + (t.outerWidth() / 2) + 'px',
		    marginTop : '-' + (t.outerHeight() / 2) + 'px',
		    position : 'fixed'
		});
    });
};

/**
 * Centers vertical DOM element.
 */
jQuery.fn.verticalCenter = function() {
    return this.each(function() {
		var t = $(this);
		t.css({
		    left : '100%',
		    top : '50%',
		    marginLeft : '-' + t.outerWidth() + 'px',
		    marginTop : '-' + (t.outerHeight() / 2) + 'px',
		    position : 'fixed'
		});
    });
};

/**
 * Generates default value for inputs.
 * FIXME: optimization is needed
 * @param value
 *                is the default value
 */
jQuery.fn.defaultInputValue = function(value) {
    return this.each(function() {
	$(this).val(value);
	$(this).attr('defaultValue', value);
	$(this).focus(function() {
	    if ($(this).val() == $(this).attr('defaultValue')) {
		$(this).val("");
		$(this).removeClass('blur');
		$(this).addClass('focus');
	    }
	});
	$(this).blur(function() {
	    if ($(this).val() == "") {
		$(this).val($(this).attr('defaultValue'));
		$(this).removeClass('focus');
		$(this).addClass('blur');
	    }
	});
    });
};