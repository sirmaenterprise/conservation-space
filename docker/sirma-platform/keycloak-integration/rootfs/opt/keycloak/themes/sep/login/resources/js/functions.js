// Variables
var $win = $(window);
var winWidth = $win.width();

var $body = $('body');
var $btnMenu = $('.btn-menu');
var $loginBtn = $('.login__btn');
var $dropdownTrigger = $('.dropdown__trigger');
var $dropdownHasDD = $('.dropdown--has-dd');
var $tabsLink = $('.tabs .tabs__nav li a');
var $accordionTrigger = $('.accordion .accordion__trigger');
var $searchToggle = $('.search__toggle');
var $listFilter = $('.list-filters');
var $listFilterLinks = $('.list-filters a');
var $filterField = $('.filter .filter__field .field');
var $filterItem = $('.filter-item');
var $resizer = $('.resizer');
var $dashboardSidebar = $('.dashboard .dashboard__sidebar');
var $dashboardContent = $('.dashboard .dashboard__content');
var $dashboardBarAside = $('.dashboard .dashboard__bar-aside');
var $classToggle = $('.class--toggle');

/**
 * In login-reset-password.ftl invoked on form submit.
 * If the entered username contains admin shows error and stops the form submit.
 *
 * @returns {boolean} false prevents form submit, true continues the submit
 */
function onResetPasswordSubmit() {
	var username = $('#username').val();
	if (username && username === 'admin') {
		alert('Cannot reset password of admin user');
		return false;
	}
	return true;
}

/*
 * Check for the need of items repositioning in .tabs__nav--orderable
 * from visible to more block
 */
function searchForOutsider() {
	var $tabsNavLi  = $('.tabs .tabs__nav--orderable > ul li');

	$tabsNavLi.each(function() {
		var $this = $(this);
		var thisOffsetRight = $this.offset().left + $this.outerWidth();
		var ulOffsetRight = $this.closest('ul').offset().left + $this.closest('ul').outerWidth();

		if (thisOffsetRight > ulOffsetRight) {
			moveLiToMore($this);

			return false;
		}
	});
}

/*
 * Check for the need of items repositioning in .tabs__nav--orderable
 * from more block to visible
 */
function searchForInsider() {
	$('.tabs .tabs__nav--orderable > ul').append($('.tabs__nav-more ul li'));
	
	searchForOutsider();
}

/*
 * Repositions links in tabs__nav--orderable if they are 
 * going outside the limits
 */
function moveLiToMore(currentLi) {
	var $tabsMore = currentLi.closest('ul').siblings('.tabs__nav-more').find('ul');

	currentLi.addClass('to--move');

	while (currentLi.next().length) {
		currentLi = currentLi.next();
		currentLi.addClass('to--move');
	}

	var liToMove = currentLi.parent().find('.to--move');

	if ($tabsMore.find('li').length) {
		liToMove.each(function() {
			$tabsMore.prepend($(this));
		});
	} else {
		liToMove.each(function() {
			$tabsMore.append($(this));
		});
	}

	liToMove.removeClass('to--move');
}

/*
 * Manage menu toggle
 */
$btnMenu.on('click', function(event) {
	event.preventDefault();

	$(this).toggleClass('active');
	$dashboardSidebar.toggleClass('expanded').toggleClass('sliding');
	$dashboardBarAside.toggleClass('expanded');

	setTimeout(function() {
		$dashboardSidebar.removeClass('sliding');
	}, 400);
});

/*
 * Manage login toggle
 */
$loginBtn.on('click', function(event) {
	event.preventDefault();

	var $login = $(this).closest('.login');

	$login
		.toggleClass('moved');
	$login
		.find('.form')
		.toggleClass('visible');
});

/*
 * Manage dropdown
 */
$dropdownTrigger.on('click', function(event) {
	event.preventDefault();

	$(this)
		.toggleClass('active');
});

$dropdownHasDD.on('click', function(event) {
	event.preventDefault();

	if ($win.width() < 1025) {
		$(this)
			.toggleClass('open')
				.siblings()
				.removeClass('open');
	}
});

/*
 * Manage tabs
 */
$tabsLink.on('click', function(event) {
	event.preventDefault();

	var $this = $(this);
	var $target = $($this.attr('href'));

	$this
		.closest('li')
		.addClass('current')
			.siblings()
			.removeClass('current');
	$target
		.addClass('current')
			.siblings()
			.removeClass('current');

	if ($this.closest('.tabs__nav-more').length) {
		$this
			.closest('.tabs__nav-more')
			.siblings('ul')
			.find('.current')
			.removeClass('current');
	}
});

/*
 * Manage accordions
 */
$accordionTrigger.on('click', function(event) {
	event.preventDefault();

	$(this)
		.closest('.box__header')
		.toggleClass('expand');
});

/*
 * Manage search toggle
 */
$searchToggle.on('click', function(event) {
	event.preventDefault();

	if ($(this).closest('.search').length) {
		$(this)
			.closest('.search')
			.toggleClass('expanded');
	} else {
		$($(this).attr('href'))
			.toggleClass('expanded');
	}
});

/*
 * Manage filters
 */

// Manage filtering
$filterField.on('input change', function() {
	var $this = $(this);
	var $filterLi = $this.closest('.filter').find('.list-filters > li');
	var $filterLinks = $this.closest('.filter').find('.list-filters a');
	var fieldValue = $this.val().trim().toLowerCase();

	$filterLinks.each(function() {
		var $filterLink = $(this);
		var filterLinkValue = $filterLink.text().trim().toLowerCase();

		$filterLink.parent().toggleClass('hidden', filterLinkValue.indexOf(fieldValue) === -1);
		$filterLink.parent().toggleClass('visible', filterLinkValue.indexOf(fieldValue) > -1);
	});

	$filterLi.each(function() {
		var $filterLiCurrent = $(this);

		if ($filterLiCurrent.hasClass('hidden')) {
			$filterLiCurrent.toggleClass('hidden', !$filterLiCurrent.find('.visible').length);
			$filterLiCurrent.toggleClass('visible', $filterLiCurrent.find('.visible').length);
		}
	});
});

// Manage filter lists
$listFilterLinks.on('click', function(event) {
	var $this = $(this);
	var thisValue = $this.text().trim();
	var targetId = $this.closest('.list-filters').data('target');

	if (targetId !== undefined) {
		event.preventDefault();

		var $target = $(targetId);

		$this
			.parent()
			.addClass('active')
				.siblings()
				.removeClass('active')
		$target
			.addClass('shown')
				.find('span')
				.text(thisValue);
		$target
			.find('input')
			.val(thisValue);
	}
});

/*
 * Remove filter-item element on click
 */
$filterItem.on('click', function(event) {
	event.preventDefault();

	var $this = $(this);
	var thisId = $this.attr('id');

	$this
		.removeClass('shown')
			.find('input')
			.val('');

	$listFilter.each(function() {
		var $thisListFilter = $(this);

		if ($thisListFilter.data('target') === '#' + thisId) {
			$thisListFilter
				.find('.active')
				.removeClass('active');
		}
	});
});

/*
 * Manage bar's z-index
 */
var $bar = $('.box .bar');

$bar
.on('mouseenter', function() {
	$(this).addClass('hovered');
})
.on('mouseleave', function() {
	$(this)
		.removeClass('hovered')
			.find('.dropdown__trigger.active')
			.removeClass('active');
});

/*
 * Manage info toggle
 */
$classToggle.on('click', function(event) {
	event.preventDefault();

	var $this = $(this);
	var classToToggle = $this.data('class');

	$this.toggleClass('active');
	$($this.attr('href')).toggleClass(classToToggle);
});

/*
 * Manage section's horizontal resizing
 */
$win
.on('load', function() {
	// Initiate the outsider check in .tabs__nav--orderable
	searchForOutsider();
})
.on('resize', function() {
	// check if window is shrinking or growing
	if (winWidth > $win.width()) {
		searchForOutsider();
	} else {
		searchForInsider();
	}

	winWidth = $win.width();

	$dashboardSidebar.prop('style', false);
	$dashboardBarAside.prop('style', false);
	$dashboardContent.prop('style', false);
})
.on('mouseup', function() {
	$resizer.removeClass('resizing');
	$dashboardContent.removeClass('resizing');
})
.on('mousemove', function(event) {
	var $this = $(this);
	var tempOffset = $dashboardSidebar.outerWidth();

	if ($resizer.hasClass('resizing') && $win.width() > 1199) {
		currentOffset = event.clientX;

		if ($win.width() > 1919) {
			currentOffset = currentOffset > 600 ? 600 : currentOffset;
		}
		if ($win.width() < 1920 && $win.width() > 1599) {
			currentOffset = currentOffset > 580 ? 580 : currentOffset;
		}
		if ($win.width() < 1600 && $win.width() > 1199) {
			currentOffset = currentOffset > 340 ? 340 : currentOffset;
		}

		currentOffset = currentOffset < 250 ? 250 : currentOffset;

		$dashboardSidebar.css('width', currentOffset + 'px');
		$dashboardBarAside.css('width', currentOffset + 'px');
		$dashboardContent.css('width', 'calc(100vw - ' + currentOffset + 'px)');

		if (tempOffset < currentOffset) {
			searchForOutsider();
		} else {
			searchForInsider();
		}
	}
});

$resizer
.on('mousedown', function(event) {
	var $this = $(this);

	$this.addClass('resizing');
	$dashboardContent.addClass('resizing');
});
