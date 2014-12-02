/*

Ext Scheduler 2.2.15
Copyright(c) 2009-2013 Bryntum AB
http://bryntum.com/contact
http://bryntum.com/license

*/
/**
@class Sch.locale.Locale

Base locale class. You need to subclass it, when creating new locales for Bryntum components. Usually subclasses of this class
will be singletones.

See <a href="#!/guide/gantt_scheduler_localization">Localization guide</a> for additional details.

*/
Ext.define('Sch.locale.Locale', {

    /**
     * @cfg {Object} l10n An object with the keys corresponding to class names and values are in turn objects with "phraseName/phraseTranslation"
     * key/values. For example:
     *
    l10n        : {
        'Sch.plugin.EventEditor' : {
            saveText: 'Speichern',
            deleteText: 'Löschen',
            cancelText: 'Abbrechen'
        },
        'Sch.plugin.CurrentTimeLine' : {
            tooltipText : 'Aktuelle Zeit'
        },

        ...
    }

     */
    l10n            : null,

    legacyMode      : true,
    
    localeName      : null,
    namespaceId     : null,
    

    constructor     : function () {
        if (!Sch.locale.Active) {
            Sch.locale.Active = {};
            this.bindRequire();
        }

        var name            = this.self.getName().split('.');
        var localeName      = this.localeName = name.pop();
        this.namespaceId    = name.join('.');

        var currentLocale   = Sch.locale.Active[ this.namespaceId ];
        
        // let's localize all the classes that are loaded
        // except the cases when English locale is being applied over some non-english locale
        if (!(localeName == 'En' && currentLocale && currentLocale.localeName != 'En')) this.apply();
    },

    bindRequire : function () {
        // OVERRIDE
        // we need central hook to localize class once it's been created
        // to achieve it we override Ext.ClassManager.triggerCreated
        var _triggerCreated = Ext.ClassManager.triggerCreated;

        Ext.ClassManager.triggerCreated = function(className) {
            _triggerCreated.apply(this, arguments);

            var cls     = Ext.ClassManager.get(className);
            // trying to apply locales for the loaded class
            for (var namespaceId in Sch.locale.Active) {
                Sch.locale.Active[namespaceId].apply(cls);
            }
        };
    },

    /**
     * Apply this locale to classes.
     * @param {String[]/Object[]} [classNames] Array of class names (or classes themself) to localize.
     * If no classes specified then will localize all exisiting classes.
     */
    apply       : function (classNames) {
        if (this.l10n) {
            var me              = this, i, l;
            var localeId        = this.self.getName();

            var applyToClass    = function (clsName, cls) {
                cls = cls || Ext.ClassManager.get(clsName);

                if (cls && (cls.activeLocaleId !== localeId)) {
                    var locale = me.l10n[clsName];

                    // if it's procedural localization - run provided callback
                    if (typeof locale === 'function') {
                        locale(clsName);

                    // if it's singleton - apply to it
                    } else if (cls.singleton) {
                        cls.l10n = Ext.apply(cls.l10n || {}, locale);

                    // otherwise we override class
                    } else {
                        Ext.override(cls, { l10n : locale });
                    }

                    // if we support old locales approach let's duplicate locale to old places
                    if (me.legacyMode) {
                        var target;

                        // we update either class prototype
                        if (cls.prototype) {
                            target = cls.prototype;
                        // or object itself in case of singleton
                        } else if (cls.singleton) {
                            target = cls;
                        }

                        if (target) {

                            if (target.legacyHolderProp) {
                                if (!target[target.legacyHolderProp]) target[target.legacyHolderProp] = {};

                                target = target[target.legacyHolderProp];
                            }

                            for (var p in locale) {
                                if (typeof target[p] !== 'function') target[p] = locale[p];
                            }
                        }
                    }

                    // keep applied locale
                    cls.activeLocaleId  = localeId;

                    // for singletons we can have some postprocessing
                    if (cls.onLocalized) cls.onLocalized();
                }
            };

            // if class name is specified
            if (classNames) {
                if (!Ext.isArray(classNames)) classNames = [classNames];

                var name, cls;
                for (i = 0, l = classNames.length; i < l; i++) {
                    if (Ext.isObject(classNames[i])) {
                        if (classNames[i].singleton) {
                            cls     = classNames[i];
                            name    = Ext.getClassName(Ext.getClass(cls));
                        } else {
                            cls     = Ext.getClass(classNames[i]);
                            name    = Ext.getClassName(cls);
                        }
                    } else {
                        cls     = null;
                        name    = 'string' === typeof classNames[i] ? classNames[i] : Ext.getClassName(classNames[i]);
                    }

                    if (name && name in this.l10n) {
                        applyToClass(name, cls);
                    }
                }

            // localize all the classes that we know about
            } else {
                // update active locales
                Sch.locale.Active[this.namespaceId] = this;

                for (var clsName in this.l10n) {
                    applyToClass(clsName);
                }
            }
        }
    }
});

/**
 * English translations for the Scheduler component
 *
 * NOTE: To change locale for month/day names you have to use the corresponding Ext JS language file.
 */
Ext.define('Sch.locale.En', {
    extend      : 'Sch.locale.Locale',
    singleton   : true,

    l10n        : {
        'Sch.util.Date' : {
            unitNames : {
                YEAR        : { single : 'year',    plural : 'years',   abbrev : 'yr' },
                QUARTER     : { single : 'quarter', plural : 'quarters',abbrev : 'q' },
                MONTH       : { single : 'month',   plural : 'months',  abbrev : 'mon' },
                WEEK        : { single : 'week',    plural : 'weeks',   abbrev : 'w' },
                DAY         : { single : 'day',     plural : 'days',    abbrev : 'd' },
                HOUR        : { single : 'hour',    plural : 'hours',   abbrev : 'h' },
                MINUTE      : { single : 'minute',  plural : 'minutes', abbrev : 'min' },
                SECOND      : { single : 'second',  plural : 'seconds', abbrev : 's' },
                MILLI       : { single : 'ms',      plural : 'ms',      abbrev : 'ms' }
            }
        },
        
        'Sch.view.SchedulerGridView' : {
            loadingText : 'Loading events...'
        },

        'Sch.plugin.CurrentTimeLine' : {
            tooltipText : 'Current time'
        },

        'Sch.plugin.EventEditor' : {
            saveText    : 'Save',
            deleteText  : 'Delete',
            cancelText  : 'Cancel'
        },

        'Sch.plugin.SimpleEditor' : {
            newEventText    : 'New booking...'
        },

        'Sch.widget.ExportDialog' : {
            generalError                : 'An error occured, try again.',
            title                       : 'Export Settings',
            formatFieldLabel            : 'Paper format',
            orientationFieldLabel       : 'Orientation',
            rangeFieldLabel             : 'Export range',
            showHeaderLabel             : 'Add page number',
            orientationPortraitText     : 'Portrait',
            orientationLandscapeText    : 'Landscape',
            completeViewText            : 'Complete schedule',
            currentViewText             : 'Current view',
            dateRangeText               : 'Date range',
            dateRangeFromText           : 'Export from',
            pickerText                  : 'Resize column/rows to desired value',
            dateRangeToText             : 'Export to',
            exportButtonText            : 'Export',
            cancelButtonText            : 'Cancel',
            progressBarText             : 'Exporting...',
            exportToSingleLabel         : 'Export as single page',
            adjustCols                  : 'Adjust column width',
            adjustColsAndRows           : 'Adjust column width and row height',
            specifyDateRange            : 'Specify date range'
        },

        // -------------- View preset date formats/strings -------------------------------------
        'Sch.preset.Manager' : function () {
            var M = Sch.preset.Manager,
                vp = M.getPreset("hourAndDay");

            if (vp) {
                vp.displayDateFormat = 'G:iA';
                vp.headerConfig.middle.dateFormat = 'G:iA';
                vp.headerConfig.top.dateFormat = 'D d/m';
            }
            
            vp = M.getPreset("secondAndMinute");
            if (vp) {
                vp.displayDateFormat = 'g:i:s';
                vp.headerConfig.top.dateFormat = 'D, d g:iA';
            }
            

            vp = M.getPreset("dayAndWeek");
            if (vp) {
                vp.displayDateFormat = 'm/d h:i A';
                vp.headerConfig.middle.dateFormat = 'D d M';
            }

            vp = M.getPreset("weekAndDay");
            if (vp) {
                vp.displayDateFormat = 'm/d';
                vp.headerConfig.bottom.dateFormat = 'd M';
                vp.headerConfig.middle.dateFormat = 'Y F d';
            }

            vp = M.getPreset("weekAndMonth");
            if (vp) {
                vp.displayDateFormat = 'm/d/Y';
                vp.headerConfig.middle.dateFormat = 'm/d';
                vp.headerConfig.top.dateFormat = 'm/d/Y';
            }

            vp = M.getPreset("weekAndDayLetter");
            if (vp) {
                vp.displayDateFormat = 'm/d/Y';
                vp.headerConfig.middle.dateFormat = 'D d M Y';
            }

            vp = M.getPreset("weekDateAndMonth");
            if (vp) {
                vp.displayDateFormat = 'm/d/Y';
                vp.headerConfig.middle.dateFormat = 'd';
                vp.headerConfig.top.dateFormat = 'Y F';
            }

            vp = M.getPreset("monthAndYear");
            if (vp) {
                vp.displayDateFormat = 'm/d/Y';
                vp.headerConfig.middle.dateFormat = 'M Y';
                vp.headerConfig.top.dateFormat = 'Y';
            }

            vp = M.getPreset("year");
            if (vp) {
                vp.displayDateFormat = 'm/d/Y';
                vp.headerConfig.middle.dateFormat = 'Y';
            }

            vp = M.getPreset("manyYears");
            if (vp) {
                vp.displayDateFormat = 'm/d/Y';
                vp.headerConfig.middle.dateFormat = 'Y';
            }
        }
    }
});

/**
 * @class Sch.util.Patch
 * @static
 * @private
 * Private utility class for Ext JS patches for the Bryntum components
 */
Ext.define('Sch.util.Patch', {
    /**
     * @cfg {String} target The class name to override
     */
    target      : null,

    /**
     * @cfg {String} minVersion The minimum Ext JS version for which this override is applicable. E.g. "4.0.5"
     */
    minVersion  : null,
    
    /**
     * @cfg {String} maxVersion The minimum Ext JS version for which this override is applicable. E.g. "4.0.7"
     */
    maxVersion  : null,

    /**
     * @cfg {String} reportUrl A url to the forum post describing the bug/issue in greater detail
     */
    reportUrl   : null,
    
    /**
     * @cfg {String} description A brief description of why this override is required
     */
    description : null,
    
    /**
     * @cfg {Function} applyFn A function that will apply the patch(es) manually, instead of using 'overrides';
     */
    applyFn : null,

    /**
     * @cfg {Boolean} ieOnly true if patch is only applicable to IE
     */
    ieOnly : false,

    /**
     * @cfg {Object} overrides a custom object containing the methods to be overridden.
     */
    overrides : null,

    onClassExtended: function(cls, data) {
        
        if (Sch.disableOverrides) {
            return;
        }

        if (data.ieOnly && !Ext.isIE) {
            return;
        }

        if ((!data.minVersion || Ext.versions.extjs.equals(data.minVersion) || Ext.versions.extjs.isGreaterThan(data.minVersion)) &&
            (!data.maxVersion || Ext.versions.extjs.equals(data.maxVersion) || Ext.versions.extjs.isLessThan(data.maxVersion))) {
            if (data.applyFn) {
                // Custom override, implementor has full control
                data.applyFn();
            } else {
                // Simple case, just an Ext override
                Ext.ClassManager.get(data.target).override(data.overrides);
            }
        }
    }
});

//@PATCH to fix https://www.assembla.com/spaces/bryntum/tickets/869#/activity/ticket
// When resizing columns with fixed locked grid width, columns become smaller due to the default Ext header resizing logic
Ext.define('Sch.patches.ColumnResize', {
    override      : 'Sch.panel.TimelineGridPanel',

    afterRender : function() {
        this.callParent(arguments);

        var resizer = this.lockedGrid.headerCt.findPlugin('gridheaderresizer');

        if (resizer) {
            // Implementation from Ext 4.2.0
            resizer.getConstrainRegion = function() {
                var me       = this,
                    dragHdEl = me.dragHd.el,
                    nextHd;



                if (me.headerCt.forceFit) {
                    nextHd = me.dragHd.nextNode('gridcolumn:not([hidden]):not([isGroupHeader])');
                    if (!me.headerInSameGrid(nextHd)) {
                        nextHd = null;
                    }
                }

                return me.adjustConstrainRegion(
                    Ext.util.Region.getRegion(dragHdEl),
                    0,
                    me.headerCt.forceFit ? (nextHd ? nextHd.getWidth() - me.minColWidth : 0) : me.maxColWidth - dragHdEl.getWidth(),
                    0,
                    me.minColWidth
                );
            };
        }
    }
});

//@PATCH to fix https://www.assembla.com/spaces/bryntum/tickets/869#/activity/ticket
// When resizing columns with fixed locked grid width, columns become smaller due to the default Ext header resizing logic
Ext.define('Sch.patches.ColumnResizeTree', {
    override      : 'Sch.panel.TimelineTreePanel',

    afterRender : function() {
        this.callParent(arguments);

        var resizer = this.lockedGrid.headerCt.findPlugin('gridheaderresizer');

        if (resizer) {
            // Implementation from Ext 4.2.0
            resizer.getConstrainRegion = function() {
                var me       = this,
                    dragHdEl = me.dragHd.el,
                    nextHd;



                if (me.headerCt.forceFit) {
                    nextHd = me.dragHd.nextNode('gridcolumn:not([hidden]):not([isGroupHeader])');
                    if (!me.headerInSameGrid(nextHd)) {
                        nextHd = null;
                    }
                }

                return me.adjustConstrainRegion(
                    Ext.util.Region.getRegion(dragHdEl),
                    0,
                    me.headerCt.forceFit ? (nextHd ? nextHd.getWidth() - me.minColWidth : 0) : me.maxColWidth - dragHdEl.getWidth(),
                    0,
                    me.minColWidth
                );
            };
        }
    }
});

//@PATCH to fix http://www.sencha.com/forum/showthread.php?261753-4.2-Drag-drop-bug-with-ScrollManager&p=959144#post959144
Ext.define('Sch.patches.ElementScroll', {
    override      : 'Sch.mixin.TimelineView',

    _onAfterRender : function() {
        this.callParent(arguments);

        if (Ext.versions.extjs.isLessThan('4.2.1') || Ext.versions.extjs.isGreaterThan('4.2.2')) return;

        this.el.scroll = function(direction, distance, animate) {
            if (!this.isScrollable()) {
                return false;
            }
            direction = direction.substr(0, 1);
            var me = this,
                dom = me.dom,
                side = direction === 'r' || direction === 'l' ? 'left' : 'top',
                scrolled = false,
                currentScroll, constrainedScroll;

            if (direction === 'r' || direction === 't' || direction === 'u') {
                distance = -distance;
            }

            if (side === 'left') {
                currentScroll = dom.scrollLeft;
                constrainedScroll = me.constrainScrollLeft(currentScroll + distance);
            } else {
                currentScroll = dom.scrollTop;
                constrainedScroll = me.constrainScrollTop(currentScroll + distance);
            }

            if (constrainedScroll !== currentScroll) {
                this.scrollTo(side, constrainedScroll, animate);
                scrolled = true;
            }

            return scrolled;
        };
    }
});

/**
@class Sch.mixin.Localizable

A mixin providing localization functionality to the consuming class.

    Ext.define('My.Toolbar', {
        extend      : 'Ext.Toolbar',
        mixins      : [ 'Sch.mixin.Localizable' ],

        initComponent   : function () {
            Ext.apply(this, {
                items   : [
                    {
                        xtype       : 'button',

                        // get the button label from the current locale
                        text        : this.L('loginText')
                    }
                ]
            });

            this.callParent(arguments);
        }
    });

*/
Ext.define('Sch.mixin.Localizable', {

    // This line used to be like this:
    //      if Sch.config.locale is specified then we'll require corresponding class
    //      by default we require Sch.locale.En class
//          requires            : [ typeof Sch != 'undefined' && Sch.config && Sch.config.locale || 'Sch.locale.En' ],
    //
    // But, SenchaCMD does not support dynamic expressions for `requires`
    // Falling back to requiring English locale - that will cause English locale to always be included in the build
    // (even if user has specified another locale in other `requires`), but thats better than requiring users
    // to always specify and load the locale they need explicitly
    requires            : [ 'Sch.locale.En' ],

    legacyMode          : true,

    activeLocaleId      : '',

    /**
     * @cfg {Object} l10n Container of locales for the class.
     */
    l10n                : null,

    isLocaleApplied     : function () {
        var activeLocaleId = (this.singleton && this.activeLocaleId) || this.self.activeLocaleId;

        if (!activeLocaleId) return false;

        for (var ns in Sch.locale.Active) {
            if (activeLocaleId === Sch.locale.Active[ns].self.getName()) return true;
        }

        return false;
    },

    applyLocale         : function () {
        // loop over activated locale classes and call apply() method of each one
        for (var ns in Sch.locale.Active) {
            Sch.locale.Active[ns].apply(this.singleton ? this : this.self.getName());
        }
    },

    /**
     * This is shorthand reference to {@link #localize}. Retrieves translation of a phrase.
     * @param {String} id Identifier of phrase.
     * @param {String} [legacyHolderProp=this.legacyHolderProp] Legacy class property name containing locales.
     * @param {Boolean} [skipLocalizedCheck=false] Do not localize class if it's not localized yet.
     * @return {String} Translation of specified phrase.
     */
    L                   : function () {
        return this.localize.apply(this, arguments);
    },

    /**
     * Retrieves translation of a phrase. There is a shorthand {@link #L} for this method.
     * @param {String} id Identifier of phrase.
     * @param {String} [legacyHolderProp=this.legacyHolderProp] Legacy class property name containing locales.
     * @param {Boolean} [skipLocalizedCheck=false] Do not localize class if it's not localized yet.
     * @return {String} Translation of specified phrase.
     */
    localize            : function (id, legacyHolderProp, skipLocalizedCheck) {
        // if not localized yet let's do it
        if (!this.isLocaleApplied() && !skipLocalizedCheck) {
            this.applyLocale();
        }

        // `l10n` instance property has highest priority
        if (this.hasOwnProperty('l10n') && this.l10n.hasOwnProperty(id) && 'function' != typeof this.l10n[id]) return this.l10n[id];

        var clsProto    = this.self && this.self.prototype;

        // if there was old properties for locales on this class
        if (this.legacyMode) {
            // if they were kept under some property
            var prop        = legacyHolderProp || this.legacyHolderProp;

            // check object instance first
            var instHolder  = prop ? this[prop] : this;
            if (instHolder && instHolder.hasOwnProperty(id) && 'function' != typeof instHolder[id]) return instHolder[id];

            if (clsProto) {
                // then let's check class definition
                var clsHolder = prop ? clsProto[prop] : clsProto;
                if (clsHolder && clsHolder.hasOwnProperty(id) && 'function' != typeof clsHolder[id]) return clsHolder[id];
            }
        }

        // let's try to get locale from class prototype `l10n` property
        var result      = clsProto.l10n[id];

        // if no transalation found
        if (result === null || result === undefined) {

            var superClass  = clsProto && clsProto.superclass;
            // if parent class also has localize() method
            if (superClass && superClass.localize) {
                // try to get phrase translation from parent class
                result = superClass.localize(id, legacyHolderProp, skipLocalizedCheck);
            }

            if (result === null || result === undefined) throw 'Cannot find locale: '+id+' ['+this.self.getName()+']';
        }

        return result;
    }
});

/**
 * @class Sch.tooltip.ClockTemplate
 * @private
 * Template showing a clock, accepts an object containing a 'date' and a 'text' property to its apply method.
 * @constructor
 * @param {Object} config The object containing the configuration of this model.
 **/
Ext.define("Sch.tooltip.ClockTemplate", {

    constructor : function() {
        var toRad = Math.PI / 180,
            cos = Math.cos,
            sin = Math.sin,
            minuteHeight = 7,
            minuteTop = 2,
            minuteLeft = 10,
            hourHeight = 6,
            hourTop = 3,
            hourLeft = 10,
            isLegacyIE = Ext.isIE && (Ext.ieVersion < 9 || Ext.isIEQuirks);

        function getHourStyleIE(degrees) {
            var rad = degrees * toRad,
                cosV = cos(rad),
                sinV = sin(rad),
                y = hourHeight * sin((90-degrees)*toRad),
                x =hourHeight * cos((90-degrees)*toRad),
                topAdjust = Math.min(hourHeight, hourHeight - y),
                leftAdjust = degrees > 180 ? x : 0,
                matrixString = "progid:DXImageTransform.Microsoft.Matrix(sizingMethod='auto expand', M11 = " + cosV + ", M12 = " + (-sinV) + ", M21 = " + sinV + ", M22 = " + cosV + ")";
        
            return Ext.String.format("filter:{0};-ms-filter:{0};top:{1}px;left:{2}px;", matrixString, topAdjust+hourTop, leftAdjust+hourLeft);
        }

        function getMinuteStyleIE(degrees) {
            var rad = degrees * toRad,
                cosV = cos(rad),
                sinV = sin(rad),
                y = minuteHeight * sin((90-degrees)*toRad),
                x = minuteHeight * cos((90-degrees)*toRad),
                topAdjust = Math.min(minuteHeight, minuteHeight - y),
                leftAdjust = degrees > 180 ? x : 0,
                matrixString = "progid:DXImageTransform.Microsoft.Matrix(sizingMethod='auto expand', M11 = " + cosV + ", M12 = " + (-sinV) + ", M21 = " + sinV + ", M22 = " + cosV + ")";
        
            return Ext.String.format("filter:{0};-ms-filter:{0};top:{1}px;left:{2}px;", matrixString, topAdjust+minuteTop, leftAdjust+minuteLeft);
        }

        function getStyle(degrees) {
            return Ext.String.format("transform:rotate({0}deg);-ms-transform:rotate({0}deg);-moz-transform: rotate({0}deg);-webkit-transform: rotate({0}deg);-o-transform:rotate({0}deg);", degrees);
        }

        return new Ext.XTemplate(
            '<div class="sch-clockwrap {cls}">' +
                '<div class="sch-clock">' +
                    '<div class="sch-hourIndicator" style="{[this.getHourStyle((values.date.getHours()%12) * 30)]}">{[Ext.Date.monthNames[values.date.getMonth()].substr(0,3)]}</div>' +
                    '<div class="sch-minuteIndicator" style="{[this.getMinuteStyle(values.date.getMinutes() * 6)]}">{[values.date.getDate()]}</div>' +
                '</div>' +
                '<span class="sch-clock-text">{text}</span>' +
            '</div>',
            {
                compiled : true,
                disableFormats : true,

                getMinuteStyle : isLegacyIE ? getMinuteStyleIE : getStyle,
                getHourStyle : isLegacyIE ? getHourStyleIE : getStyle
            }
        );
    } 
});

/**
@class "Sch.tooltip.Tooltip"
@extends Ext.ToolTip
@private

Internal plugin showing a tooltip with event start/end information.
*/
Ext.define("Sch.tooltip.Tooltip", {
    extend : "Ext.tip.ToolTip",
    requires : [
        'Sch.tooltip.ClockTemplate'
    ],
    autoHide            : false,
    anchor              : 'b',
    padding             : '0 3 0 0',
    showDelay           : 0,
    hideDelay           : 0,
    quickShowInterval   : 0,
    dismissDelay        : 0,
    trackMouse          : false,
    valid               : true,
    anchorOffset        : 5,
    shadow              : false,
    frame               : false,

    constructor : function(config) {
        var clockTpl = Ext.create("Sch.tooltip.ClockTemplate");
        this.renderTo = document.body;
        this.startDate = this.endDate = new Date();

        if (!this.template) {
            this.template = Ext.create("Ext.XTemplate",
                '<div class="{[values.valid ? "sch-tip-ok" : "sch-tip-notok"]}">',
                   '{[this.renderClock(values.startDate, values.startText, "sch-tooltip-startdate")]}',
                   '{[this.renderClock(values.endDate, values.endText, "sch-tooltip-enddate")]}',
                '</div>',
                {
                    compiled : true,
                    disableFormats : true,

                    renderClock : function(date, text, cls) {
                        return clockTpl.apply({
                            date : date, 
                            text : text,
                            cls : cls
                        });
                    }
                }
            );
        }

        this.callParent(arguments);
    },

    update : function(startDate, endDate, valid) {

        if (this.startDate - startDate !== 0 ||
            this.endDate - endDate !== 0 ||
            this.valid !== valid) {
            
            // This will be called a lot so cache the values
            this.startDate = startDate;
            this.endDate = endDate;
            this.valid = valid;
            var startText = this.schedulerView.getFormattedDate(startDate),
                endText = this.schedulerView.getFormattedEndDate(endDate, startDate);

            // If resolution is day or greater, and end date is greater then start date
            if (this.mode === 'calendar' && endDate.getHours() === 0 && endDate.getMinutes() === 0 && 
                !(endDate.getYear() === startDate.getYear() && endDate.getMonth() === startDate.getMonth() && endDate.getDate() === startDate.getDate())) {
                endDate = Sch.util.Date.add(endDate, Sch.util.Date.DAY, -1);
            }
        
            this.callParent([
                this.template.apply({
                    valid       : valid,
                    startDate   : startDate,
                    endDate     : endDate,
                    startText   : startText,
                    endText     : endText
                })
            ]);
        }
    },
     
    show : function(el, xOffset) {
        if (!el) {
            return;
        }

        if (Sch.util.Date.compareUnits(this.schedulerView.getTimeResolution().unit, Sch.util.Date.DAY) >= 0) {
            this.mode = 'calendar';
            this.addCls('sch-day-resolution');
        } else {
            this.mode = 'clock';
            this.removeCls('sch-day-resolution');
        }
        this.mouseOffsets = [xOffset - 18, -7];
        
        this.setTarget(el);
        this.callParent();
        this.alignTo(el, 'bl-tl', this.mouseOffsets);

        this.mon(Ext.getDoc(), 'mousemove', this.onMyMouseMove, this);
        this.mon(Ext.getDoc(), 'mouseup', this.onMyMouseUp, this, { single : true });
    },

    onMyMouseMove : function() {
        this.el.alignTo(this.target, 'bl-tl', this.mouseOffsets);
    },

    onMyMouseUp : function() {
        this.mun(Ext.getDoc(), 'mousemove', this.onMyMouseMove, this);
    },

    afterRender : function() {
        this.callParent(arguments);

        // In slower browsers, the mouse pointer may end up over the tooltip interfering with drag drop etc
        this.el.on('mouseenter', this.onElMouseEnter, this);
    },

    onElMouseEnter : function() {
        this.alignTo(this.target, 'bl-tl', this.mouseOffsets);
    }
}); 

/**
 * @class Sch.util.Date
 * @static
 * Static utility class for Date manipulation
 */
Ext.define('Sch.util.Date', {
    requires        : 'Ext.Date',
    mixins          : ['Sch.mixin.Localizable'],
    singleton       : true,

    // These stem from Ext.Date in Ext JS but since they don't exist in Sencha Touch we'll need to keep them here
    stripEscapeRe   : /(\\.)/g,
    hourInfoRe      : /([gGhHisucUOPZ]|MS)/,

    unitHash        : null,
    unitsByName     : {},

    // Override this to localize the time unit names.
    //unitNames   : {
        //YEAR    : { single : 'year', plural : 'years', abbrev : 'yr' },
        //QUARTER : { single : 'quarter', plural : 'quarters', abbrev : 'q' },
        //MONTH   : { single : 'month', plural : 'months', abbrev : 'mon' },
        //WEEK    : { single : 'week', plural : 'weeks', abbrev : 'w' },
        //DAY     : { single : 'day', plural : 'days', abbrev : 'd' },
        //HOUR    : { single : 'hour', plural : 'hours', abbrev : 'h' },
        //MINUTE  : { single : 'minute', plural : 'minutes', abbrev : 'min' },
        //SECOND  : { single : 'second', plural : 'seconds', abbrev : 's' },
        //MILLI   : { single : 'ms', plural : 'ms', abbrev : 'ms' }
    //},


    constructor : function () {
        var ED = Ext.Date;
        var unitHash = this.unitHash = {
            /**
             * Date interval constant
             * @static
             * @type String
             */
            MILLI : ED.MILLI,

            /**
             * Date interval constant
             * @static
             * @type String
             */
            SECOND : ED.SECOND,

            /**
             * Date interval constant
             * @static
             * @type String
             */
            MINUTE : ED.MINUTE,

            /** Date interval constant
             * @static
             * @type String
             */
            HOUR : ED.HOUR,

            /**
             * Date interval constant
             * @static
             * @type String
             */
            DAY : ED.DAY,

            /**
             * Date interval constant
             * @static
             * @type String
             */
            WEEK : "w",

            /**
             * Date interval constant
             * @static
             * @type String
             */
            MONTH : ED.MONTH,

            /**
             * Date interval constant
             * @static
             * @type String
             */
            QUARTER : "q",

            /**
             * Date interval constant
             * @static
             * @type String
             */
            YEAR : ED.YEAR
        };
        Ext.apply(this, unitHash);

        var me = this;
        this.units = [me.MILLI, me.SECOND, me.MINUTE, me.HOUR, me.DAY, me.WEEK, me.MONTH, me.QUARTER, me.YEAR];
    },


    onLocalized : function () {
        this.setUnitNames(this.L('unitNames'));
    },


    /**
     * Call this method to provide your own, localized values for duration unit names. See the "/js/Sch/locale/sch-lang-*.js" files for examples
     *
     * @param {Object} unitNames
     */
    setUnitNames : function (unitNames, preserveLocales) {
        var unitsByName = this.unitsByName = {};

        this.l10n.unitNames = unitNames;

        this._unitNames     = Ext.apply({}, unitNames);

        var unitHash        = this.unitHash;

        // Make it possible to lookup readable date names from both 'DAY' and 'd' etc.
        for (var name in unitHash) {
            if (unitHash.hasOwnProperty(name)) {
                var unitValue = unitHash[name];

                this._unitNames[ unitValue ] = this._unitNames[name];

                unitsByName[ name ] = unitValue;
                unitsByName[ unitValue ] = unitValue;
            }
        }
    },


    /**
     * Checks if this date is >= start and < end.
     * @param {Date} date The source date
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Boolean} true if this date falls on or between the given start and end dates.
     * @static
     */
    betweenLesser : function (date, start, end) {
        var t = date.getTime();
        return start.getTime() <= t && t < end.getTime();
    },

    /**
     * Constrains the date within a min and a max date
     * @param {Date} date The date to constrain
     * @param {Date} min Min date
     * @param {Date} max Max date
     * @return {Date} The constrained date
     * @static
     */
    constrain : function (date, min, max) {
        return this.min(this.max(date, min), max);
    },

    /**
     * Returns 1 if first param is a greater unit than second param, -1 if the opposite is true or 0 if they're equal
     * @static
     *
     * @param {String} unit1 The 1st unit
     * @param {String} unit2 The 2nd unit
     */
    compareUnits : function (u1, u2) {
        var ind1 = Ext.Array.indexOf(this.units, u1),
            ind2 = Ext.Array.indexOf(this.units, u2);

        return ind1 > ind2 ? 1 : (ind1 < ind2 ? -1 : 0);
    },

    /**
     * Returns true if first unit passed is strictly greater than the second.
     * @static
     *
     * @param {String} unit1 The 1st unit
     * @param {String} unit2 The 2nd unit
     */
    isUnitGreater : function (u1, u2) {
        return this.compareUnits(u1, u2) > 0;
    },

    /**
     * Copies hours, minutes, seconds, milliseconds from one date to another
     * @static
     *
     * @param {String} targetDate The target date
     * @param {String} sourceDate The source date
     */
    copyTimeValues : function (targetDate, sourceDate) {
        targetDate.setHours(sourceDate.getHours());
        targetDate.setMinutes(sourceDate.getMinutes());
        targetDate.setSeconds(sourceDate.getSeconds());
        targetDate.setMilliseconds(sourceDate.getMilliseconds());
    },

    /**
     * Adds a date unit and interval
     * @param {Date} date The source date
     * @param {String} unit The date unit to add
     * @param {Int} value The number of units to add to the date
     * @return {Date} The new date
     * @static
     */
    add : function (date, unit, value) {
        var d = Ext.Date.clone(date);
        if (!unit || value === 0) return d;

        switch (unit.toLowerCase()) {
            case this.MILLI:
                d = new Date(date.getTime() + value);
                break;
            case this.SECOND:
                d = new Date(date.getTime() + (value * 1000));
                break;
            case this.MINUTE:
                d = new Date(date.getTime() + (value * 60000));
                break;
            case this.HOUR:
                d = new Date(date.getTime() + (value * 3600000));
                break;
            case this.DAY:
                d.setDate(date.getDate() + value);
                break;
            case this.WEEK:
                d.setDate(date.getDate() + value * 7);
                break;
            case this.MONTH:
                var day = date.getDate();
                if (day > 28) {
                    day = Math.min(day, Ext.Date.getLastDateOfMonth(this.add(Ext.Date.getFirstDateOfMonth(date), this.MONTH, value)).getDate());
                }
                d.setDate(day);
                d.setMonth(d.getMonth() + value);
                break;
            case this.QUARTER:
                d = this.add(date, this.MONTH, value * 3);
                break;
            case this.YEAR:
                d.setFullYear(date.getFullYear() + value);
                break;
        }
        return d;
    },


    getMeasuringUnit : function (unit) {
        if (unit === this.WEEK) {
            return this.DAY;
        }
        return unit;
    },


    /**
     * Returns a duration of the timeframe in the given unit.
     * @static
     * @param {Date} start The start date of the timeframe
     * @param {Date} end The end date of the timeframe
     * @param {String} unit Duration unit
     * @return {Number} The duration in the units
     */
    getDurationInUnit : function (start, end, unit) {
        var units;

        switch (unit) {
            case this.YEAR:
                units = Math.round(this.getDurationInYears(start, end));
                break;

            case this.QUARTER:
                units = Math.round(this.getDurationInMonths(start, end) / 3);
                break;

            case this.MONTH:
                units = Math.round(this.getDurationInMonths(start, end));
                break;

            case this.WEEK:
                units = Math.round(this.getDurationInDays(start, end)) / 7;
                break;

            case this.DAY:
                units = Math.round(this.getDurationInDays(start, end));
                break;

            case this.HOUR:
                units = Math.round(this.getDurationInHours(start, end));
                break;

            case this.MINUTE:
                units = Math.round(this.getDurationInMinutes(start, end));
                break;

            case this.SECOND:
                units = Math.round(this.getDurationInSeconds(start, end));
                break;

            case this.MILLI:
                units = Math.round(this.getDurationInMilliseconds(start, end));
                break;
        }

        return units;
    },


    getUnitToBaseUnitRatio : function (baseUnit, unit) {
        if (baseUnit === unit) {
            return 1;
        }

        switch (baseUnit) {
            case this.YEAR:
                switch (unit) {
                    case this.QUARTER:
                        return 1 / 4;

                    case this.MONTH:
                        return 1 / 12;
                }
                break;

            case this.QUARTER:
                switch (unit) {
                    case this.YEAR:
                        return 4;

                    case this.MONTH:
                        return 1 / 3;
                }
                break;

            case this.MONTH:
                switch (unit) {
                    case this.YEAR:
                        return 12;

                    case this.QUARTER:
                        return 3;
                }
                break;

            case this.WEEK:
                switch (unit) {
                    case this.DAY:
                        return 1 / 7;

                    case this.HOUR:
                        return 1 / 168;
                }
                break;

            case this.DAY:
                switch (unit) {
                    case this.WEEK:
                        return 7;

                    case this.HOUR:
                        return 1 / 24;

                    case this.MINUTE:
                        return 1 / 1440;
                }
                break;

            case this.HOUR:
                switch (unit) {
                    case this.DAY:
                        return 24;

                    case this.MINUTE:
                        return 1 / 60;
                }
                break;

            case this.MINUTE:
                switch (unit) {
                    case this.HOUR:
                        return 60;

                    case this.SECOND:
                        return 1 / 60;

                    case this.MILLI:
                        return 1 / 60000;
                }
                break;

            case this.SECOND:
                switch (unit) {
                    case this.MILLI:
                        return 1 / 1000;
                }
                break;


            case this.MILLI:
                switch (unit) {
                    case this.SECOND:
                        return 1000;
                }
                break;

        }

        return -1;
    },

    /**
     * Returns the number of milliseconds between the two dates
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} true number of minutes between the two dates
     * @static
     */
    getDurationInMilliseconds : function (start, end) {
        return (end - start);
    },

    /**
     * Returns the number of seconds between the two dates
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} The number of seconds between the two dates
     * @static
     */
    getDurationInSeconds : function (start, end) {
        return (end - start) / 1000;
    },

    /**
     * Returns the number of minutes between the two dates
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} true number of minutes between the two dates
     * @static
     */
    getDurationInMinutes : function (start, end) {
        return (end - start) / 60000;
    },

    /**
     * Returns the number of hours between the two dates.
     *
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} true number of hours between the two dates
     * @static
     */
    getDurationInHours : function (start, end) {
        return (end - start) / 3600000;
    },

    /**
     * Simplistic method which returns the number of days between the two dates. Assumes a day has 24 hours and does not take DST into account.
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} true number of days between the two dates
     * @static
     */
    getDurationInDays : function (start, end) {
        return (end - start) / 86400000;
    },

    /**
     * Returns the number of business days between the two dates
     * @deprecated Will be removed in v3.0. Use Calendar instead to find duration for a period of time, excluding non-working time.
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} true number of business days between the two dates
     * @static
     */
    getDurationInBusinessDays : function (start, end) {
        var nbrDays = Math.round((end - start) / 86400000),
            nbrBusinessDays = 0,
            d;

        for (var i = 0; i < nbrDays; i++) {
            d = this.add(start, this.DAY, i).getDay();
            if (d !== 6 && d !== 0) {
                nbrBusinessDays++;
            }
        }
        return nbrBusinessDays;
    },

    /**
     * Returns the number of whole months between the two dates
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} The number of whole months between the two dates
     * @static
     */
    getDurationInMonths : function (start, end) {
        return ((end.getFullYear() - start.getFullYear()) * 12) + (end.getMonth() - start.getMonth());
    },

    /**
     * Returns the number of years between the two dates
     * @param {Date} start Start date
     * @param {Date} end End date
     * @return {Int} The number of whole months between the two dates
     * @static
     */
    getDurationInYears : function (start, end) {
        return this.getDurationInMonths(start, end) / 12;
    },

    /**
     * Returns the lesser of the two dates
     * @param {Date} date 1
     * @param {Date} date 2
     * @return {Date} Returns the lesser of the two dates
     * @static
     */
    min : function (d1, d2) {
        return d1 < d2 ? d1 : d2;
    },

    /**
     * Returns the greater of the two dates
     * @param {Date} date 1
     * @param {Date} date 2
     * @return {Date} Returns the greater of the two dates
     * @static
     */
    max : function (d1, d2) {
        return d1 > d2 ? d1 : d2;
    },

    /**
     * Returns true if dates intersect
     * @param {Date} start 1
     * @param {Date} end 1
     * @param {Date} start 2
     * @param {Date} end 2
     * @return {Boolean} Returns true if dates intersect
     * @static
     */
    intersectSpans : function (date1Start, date1End, date2Start, date2End) {
        return this.betweenLesser(date1Start, date2Start, date2End) ||
            this.betweenLesser(date2Start, date1Start, date1End);
    },

    /**
     * Returns a name of the duration unit, matching its property on the Sch.util.Date class.
     * So, for example:
     *
     *      Sch.util.Date.getNameOfUnit(Sch.util.Date.DAY) == 'DAY' // true
     *
     * @static
     * @param {String} unit Duration unit
     * @return {String}
     */
    getNameOfUnit : function (unit) {
        unit = this.getUnitByName(unit);

        switch (unit.toLowerCase()) {
            case this.YEAR      :
                return 'YEAR';
            case this.QUARTER   :
                return 'QUARTER';
            case this.MONTH     :
                return 'MONTH';
            case this.WEEK      :
                return 'WEEK';
            case this.DAY       :
                return 'DAY';
            case this.HOUR      :
                return 'HOUR';
            case this.MINUTE    :
                return 'MINUTE';
            case this.SECOND    :
                return 'SECOND';
            case this.MILLI     :
                return 'MILLI';
        }

        throw "Incorrect UnitName";
    },

    /**
     * Returns a human-readable name of the duration unit. For for example for `Sch.util.Date.DAY` it will return either
     * "day" or "days", depending from the `plural` argument
     * @static
     * @param {String} unit Duration unit
     * @param {Boolean} plural Whether to return a plural name or singular
     * @return {String}
     */
    getReadableNameOfUnit : function (unit, plural) {
        if (!this.isLocaleApplied()) this.applyLocale();
        return this._unitNames[ unit ][ plural ? 'plural' : 'single' ];
    },

    /**
     * Returns an abbreviated form of the name of the duration unit.
     * @static
     * @param {String} unit Duration unit
     * @return {String}
     */
    getShortNameOfUnit : function (unit) {
        if (!this.isLocaleApplied()) this.applyLocale();
        return this._unitNames[ unit ].abbrev;
    },

    getUnitByName : function (name) {
        if (!this.isLocaleApplied()) this.applyLocale();

        if (!this.unitsByName[ name ]) {
            Ext.Error.raise('Unknown unit name: ' + name);
        }

        return this.unitsByName[ name ];
    },


    /**
     * Returns the beginning of the Nth next duration unit, after the provided `date`.
     * For example for the this call:
     *      Sch.util.Date.getNext(new Date('Jul 15, 2011'), Sch.util.Date.MONTH, 1)
     *
     * will return: Aug 1, 2011
     *
     * @static
     * @param {Date} date The date
     * @param {String} unit The duration unit
     * @param {Integer} increment How many duration units to skip
     * @param {Number} weekStartDay The day index of the 1st day of the week.
     *                Only required when `unit` is `WEEK`. 0 for Sunday, 1 for Monday, 2 for Tuesday, and so on (defaults to 1).
     * @return {Date} The beginning of the next duration unit interval
     */
    getNext : function (date, unit, increment, weekStartDay) {
        var dt = Ext.Date.clone(date);

        weekStartDay = arguments.length < 4 ? 1 : weekStartDay;
        increment = increment || 1;

        switch (unit) {
            case this.MILLI:
                dt = this.add(date, unit, increment);
                break;

            case this.SECOND:
                dt = this.add(date, unit, increment);

                if (dt.getMilliseconds() > 0) {
                    dt.setMilliseconds(0);
                }
                break;

            case this.MINUTE:
                dt = this.add(date, unit, increment);

                if (dt.getSeconds() > 0) {
                    dt.setSeconds(0);
                }
                if (dt.getMilliseconds() > 0) {
                    dt.setMilliseconds(0);
                }
                break;

            case this.HOUR:
                dt = this.add(date, unit, increment);

                // Without these checks Firefox messes up the date and it changes timezone in certain edge cases
                // See test 021_sch_util_date_dst.t.js
                if (dt.getMinutes() > 0) {
                    dt.setMinutes(0);
                }
                if (dt.getSeconds() > 0) {
                    dt.setSeconds(0);
                }
                if (dt.getMilliseconds() > 0) {
                    dt.setMilliseconds(0);
                }
                break;

            case this.DAY:
                // Check if date has 23 hrs and is in Chile timezone
                var midnightNotInTimeScale = date.getHours() === 23 && this.add(dt, this.HOUR, 1).getHours() === 1;

                if (midnightNotInTimeScale) {
                    // Correct the date manually for DST transitions happening at 00:00
                    dt = this.add(dt, this.DAY, 2);
                    Ext.Date.clearTime(dt);
                    return dt;
                }

                Ext.Date.clearTime(dt);

                dt = this.add(dt, this.DAY, increment);
                break;

            case this.WEEK:

                Ext.Date.clearTime(dt);
                var day = dt.getDay();
                dt = this.add(dt, this.DAY, weekStartDay - day + 7 * (increment - (weekStartDay <= day ? 0 : 1)));

                // For south american timezones, midnight does not exist on DST transitions, adjust...
                if (dt.getDay() !== weekStartDay) {
                    dt = this.add(dt, this.HOUR, 1);
                } else {
                    Ext.Date.clearTime(dt);
                }
                break;

            case this.MONTH:
                dt = this.add(dt, this.MONTH, increment);
                dt.setDate(1);
                Ext.Date.clearTime(dt);
                break;

            case this.QUARTER:
                dt = this.add(dt, this.MONTH, ((increment - 1) * 3) + (3 - (dt.getMonth() % 3)));
                Ext.Date.clearTime(dt);
                dt.setDate(1);
                break;

            case this.YEAR:
                dt = new Date(dt.getFullYear() + increment, 0, 1);
                break;

            default:
                throw 'Invalid date unit';
        }

        return dt;
    },


    getNumberOfMsFromTheStartOfDay : function (date) {
        return date - Ext.Date.clearTime(date, true) || 86400000;
    },


    getNumberOfMsTillTheEndOfDay : function (date) {
        return this.getStartOfNextDay(date, true) - date;
    },


    getStartOfNextDay : function (date, clone, noNeedToClearTime) {
        var nextDay = this.add(noNeedToClearTime ? date : Ext.Date.clearTime(date, clone), this.DAY, 1);

        // DST case
        if (nextDay.getDate() == date.getDate()) {
            var offsetNextDay   = this.add(Ext.Date.clearTime(date, clone), this.DAY, 2).getTimezoneOffset();
            var offsetDate      = date.getTimezoneOffset();

            nextDay             = this.add(nextDay, this.MINUTE, offsetDate - offsetNextDay);
        }

        return nextDay;
    },

    getEndOfPreviousDay : function (date, noNeedToClearTime) {
        var dateOnly = noNeedToClearTime ? date : Ext.Date.clearTime(date, true);

        // dates are different
        if (dateOnly - date) {
            return dateOnly;
        } else {
            return this.add(dateOnly, this.DAY, -1);
        }
    },

    /**
     * Returns true if the first time span completely 'covers' the second time span. E.g.
     *      Sch.util.Date.timeSpanContains(new Date(2010, 1, 2), new Date(2010, 1, 5), new Date(2010, 1, 3), new Date(2010, 1, 4)) ==> true
     *      Sch.util.Date.timeSpanContains(new Date(2010, 1, 2), new Date(2010, 1, 5), new Date(2010, 1, 3), new Date(2010, 1, 6)) ==> false
     * @static
     * @param {Date} spanStart The start date for initial time span
     * @param {Date} spanEnd The end date for initial time span
     * @param {Date} otherSpanStart The start date for the 2nd time span
     * @param {Date} otherSpanEnd The end date for the 2nd time span
     * @return {Boolean}
     */
    timeSpanContains : function (spanStart, spanEnd, otherSpanStart, otherSpanEnd) {
        return (otherSpanStart - spanStart) >= 0 && (spanEnd - otherSpanEnd) >= 0;
    }
});

/**
 * To analyze possible errors in your setup, include this on your HTML page and use Firebug (or any other console application) to execute line below:
 * >
 * > Sch.util.Debug.runDiagnostics();
 * > ...
 */ 
Ext.define("Sch.util.Debug", {
    singleton : true,

    runDiagnostics : function() {
        var log;
        var C = window.console;

        if (C && C.log) {
            log = function (arg) {
                C.log(arg);
            };
        } else {
            if (!window.schedulerDebugWin) {
                window.schedulerDebugWin = new Ext.Window({
                    height:400,
                    width: 500,
                    bodyStyle:'padding:10px',
                    closeAction : 'hide',
                    autoScroll:true
                });
            }
            window.schedulerDebugWin.show();
            schedulerDebugWin.update('');
            log = function(text){ schedulerDebugWin.update((schedulerDebugWin.body.dom.innerHTML || '') + text + '<br/>');};
        }

        var els = Ext.select('.sch-schedulerpanel');
    
        if (els.getCount() === 0) log('No scheduler component found');
    
        var s = Ext.getCmp(els.elements[0].id),
            resourceStore = s.getResourceStore(),
            eventStore = s.getEventStore();

        if (!eventStore.isEventStore) {
            log("Your event store must be or extend Sch.data.EventStore"); 
        }

        log('Scheduler view start: ' + s.getStart() + ', end: ' + s.getEnd());
    
        if (!resourceStore) { log('No store configured'); return; }
        if (!eventStore) {log('No event store configured'); return; }
    
        log(resourceStore.getCount() + ' records in the resource store'); 
        log(eventStore.getCount() + ' records in the eventStore'); 
        var eventIdProp = eventStore.model.prototype.idProperty;
        var resourceIdProp = resourceStore.model.prototype.idProperty;

        var eventIdPropertyFound = eventStore.model.prototype.fields.getByKey(eventIdProp);
        var resourceIdPropertyFound = resourceStore.model.prototype.fields.getByKey(resourceIdProp);

        if (!(eventStore.model.prototype instanceof Sch.model.Event)) {
            log("Your event model must extend Sch.model.Event"); 
        }
        if (!(resourceStore.model.prototype instanceof Sch.model.Resource)) {
            log("Your event model must extend Sch.model.Resource"); 
        }

        if (!eventIdPropertyFound) {
            log("idProperty on the event model is incorrectly setup, value: " + eventIdProp); 
        }
        if (!resourceIdPropertyFound) {
            log("idProperty on the resource model is incorrectly setup, value: " + resourceIdProp); 
        }

        var view = s.getSchedulingView();

        log(view.el.select(view.eventSelector).getCount() + ' events present in the DOM'); 
    
        if (eventStore.getCount() > 0) {
            if (!eventStore.first().getStartDate() || !(eventStore.first().getStartDate() instanceof Date)) {
                log ('The eventStore reader is misconfigured - The StartDate field is not setup correctly, please investigate');
                log('StartDate is configured with dateFormat: ' + eventStore.model.prototype.fields.getByKey('StartDate').dateFormat);
                log('See Ext JS docs for information about different date formats: http://docs.sencha.com/ext-js/4-0/#!/api/Ext.Date');
            }
        
            if (!eventStore.first().getEndDate() || !(eventStore.first().getEndDate() instanceof Date)) {
                log('The eventStore reader is misconfigured - The EndDate field is not setup correctly, please investigate');
                log('EndDate is configured with dateFormat: ' + eventStore.model.prototype.fields.getByKey('EndDate').dateFormat);
                log('See Ext JS docs for information about different date formats: http://docs.sencha.com/ext-js/4-0/#!/api/Ext.Date');
            }

            if (eventStore.proxy && eventStore.proxy.reader && eventStore.proxy.reader.jsonData) {
                log('Dumping jsonData to console');
                console && console.dir && console.dir(eventStore.proxy.reader.jsonData);
            }
        
            log('Records in the event store:');
            eventStore.each(function(r, i) {
                log((i + 1) + '. ' + r.startDateField + ':' + r.getStartDate() + ', ' + r.endDateField + ':' + r.getEndDate() + ', ' + r.resourceIdField + ':' + r.getResourceId());
                if (!r.getStartDate()) {
                    log(r.getStartDate());
                }
            });
        } else {
            log('Event store has no data. Has it been loaded properly?');
        }

        if (resourceStore instanceof Ext.data.TreeStore) resourceStore = resourceStore.nodeStore;

        if (resourceStore.getCount() > 0) {
            log('Records in the resource store:');
            resourceStore.each(function(r, i) {
                log((i + 1) + '. ' + r.idProperty + ':' + r.getId());
                return;
            });
        } else {
            log('Resource store has no data.');
            return;
        }
    
        log('Everything seems to be setup ok!');
    }    
});
/**
 * @class Sch.util.DragTracker
 * @private
 * 
 * Simple drag tracker with an extra useful getRegion method
 **/
Ext.define('Sch.util.DragTracker', {
    extend      : 'Ext.dd.DragTracker',
    
    /**
     * @cfg {Number} xStep
     * The number of horizontal pixels to snap to when dragging
     */
    xStep : 1,

    /**
     * @cfg {Number} yStep
     * The number of vertical pixels to snap to when dragging
     */
    yStep : 1,

    /**
     * Set the number of horizontal pixels to snap to when dragging
     * @param {Number} step
     */
    setXStep : function(step) {
        this.xStep = step;
    },

    /**
     * Set the number of vertical pixels to snap to when dragging
     * @param {Number} step
     */
    setYStep : function(step) {
        this.yStep = step;
    },

    getRegion : function() {
        var startXY   = this.startXY,
            currentXY = this.getXY(),
            minX      = Math.min(startXY[0], currentXY[0]),
            minY      = Math.min(startXY[1], currentXY[1]),
            width     = Math.abs(startXY[0] - currentXY[0]),
            height    = Math.abs(startXY[1] - currentXY[1]);
            
        return new Ext.util.Region(minY, minX + width, minY + height, minX);
    },

    // @OVERRIDE
    onMouseDown: function(e, target){
        // If this is disabled, or the mousedown has been processed by an upstream DragTracker, return
        if (this.disabled ||e.dragTracked) {
            return;
        }

        var xy = e.getXY(),
            elX, elY,
            x = xy[0], 
            y = xy[1];
        // TODO handle if this.el is scrolled
        if (this.xStep > 1) {
            elX = this.el.getX();
            x -= elX;
            x = Math.round(x/this.xStep)*this.xStep;
            x += elX;
        }
        
        if (this.yStep > 1) {
            elY = this.el.getY();
            y -= elY;
            y = Math.round(y/this.yStep)*this.yStep;
            y += elY;
        }

        // This information should be available in mousedown listener and onBeforeStart implementations
        this.dragTarget = this.delegate ? target : this.handle.dom;
        this.startXY = this.lastXY = [x, y];
        this.startRegion = Ext.fly(this.dragTarget).getRegion();

        if (this.fireEvent('mousedown', this, e) === false ||
            this.fireEvent('beforedragstart', this, e) === false ||
            this.onBeforeStart(e) === false) {
            return;
        }

        // Track when the mouse is down so that mouseouts while the mouse is down are not processed.
        // The onMouseOut method will only ever be called after mouseup.
        this.mouseIsDown = true;

        // Flag for downstream DragTracker instances that the mouse is being tracked.
        e.dragTracked = true;

        if (this.preventDefault !== false) {
            e.preventDefault();
        }
        Ext.getDoc().on({
            scope: this,
            mouseup: this.onMouseUp,
            mousemove: this.onMouseMove,
            selectstart: this.stopSelect
        });
        if (this.autoStart) {
            this.timer =  Ext.defer(this.triggerStart, this.autoStart === true ? 1000 : this.autoStart, this, [e]);
        }
    },

    // @OVERRIDE
    // Adds support for snapping to increments while dragging
    onMouseMove: function(e, target){

        // Bug fix required for IE
        if (this.active && Ext.isIE && Ext.ieVersion < 10 && !e.browserEvent.button) {
            e.preventDefault();
            this.onMouseUp(e);
            return;
        }

        e.preventDefault();
        var xy = e.getXY(),
            s = this.startXY;
        
        if (!this.active) {
            if (Math.max(Math.abs(s[0]-xy[0]), Math.abs(s[1]-xy[1])) > this.tolerance) {
                this.triggerStart(e);
            } else {
                return;
            }
        }

        var x = xy[0], 
            y = xy[1];
        
        // TODO handle if this.el is scrolled
        if (this.xStep > 1) {
            x -= this.startXY[0];
            x = Math.round(x/this.xStep)*this.xStep;
            x += this.startXY[0];
        }
        
        if (this.yStep > 1) {
            y -= this.startXY[1];
            y = Math.round(y/this.yStep)*this.yStep;
            y += this.startXY[1];
        }

        var snapping = this.xStep > 1 || this.yStep > 1;

        if (!snapping || x !== xy[0] || y !== xy[1]) {
            this.lastXY = [x,y];
        
            if (this.fireEvent('mousemove', this, e) === false) {
                this.onMouseUp(e);
            } else {
                this.onDrag(e);
                this.fireEvent('drag', this, e);
            }
        }
    }
});

/**
@class Sch.preset.ViewPreset
Not used directly, but the properties below are rather provided inline as seen in the source of {@link Sch.preset.Manager}. This class is just provided for documentation purposes.

A sample preset looks like:

        hourAndDay : {
            timeColumnWidth         : 60,       // Time column width (used for rowHeight in vertical mode)
            rowHeight               : 24,       // Only used in horizontal orientation
            resourceColumnWidth     : 100,      // Only used in vertical orientation
            
            displayDateFormat       : 'G:i',    // Controls how dates will be displayed in tooltips etc
            
            shiftIncrement          : 1,        // Controls how much time to skip when calling shiftNext and shiftPrevious.
            shiftUnit               : "DAY",    // Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
            defaultSpan             : 12,       // By default, if no end date is supplied to a view it will show 12 hours
            
            timeResolution          : {         // Dates will be snapped to this resolution
                unit        : "MINUTE",         // Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
                increment   : 15
            },
            
            headerConfig            : {         // This defines your header, you must include a "middle" object, and top/bottom are optional. 
                middle      : {                 // For each row you can define "unit", "increment", "dateFormat", "renderer", "align", and "scope"
                    unit        : "HOUR",
                    dateFormat  : 'G:i'
                },
                top         : {
                    unit        : "DAY",
                    dateFormat  : 'D d/m'
                }
            }
        },

See the {@link Sch.preset.Manager} for the list of available presets.
 
*/
Ext.define("Sch.preset.ViewPreset", {
    
    columnWidth         : null,
    rowHeight           : null,
    
    /**
     * @cfg {Int} timeAxisColumnWidth The width of the time axis column in the vertical orientation 
     */
    timeAxisColumnWidth : null,
    
    /**
    * @cfg {String} dateFormat Defines how dates will be formatted in tooltips etc
    */
    displayDateFormat   : 'G:i',
    
    /**
    * @cfg {String} shiftUnit The unit to shift when calling shiftNext/shiftPrevious to navigate in the chart. 
    * Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
    */
    shiftUnit           : "HOUR",
    
    /**
    * @cfg {Int} shiftIncrement The amount to shift (in shiftUnits)
    */
    shiftIncrement      : 1,
    
    /**
    * @cfg {Int} defaultSpan The amount of time to show by default in a view (in the unit defined by the middle header)
    */
    defaultSpan         : 12,
    
    /**
    * @cfg {Object} timeResolution An object containing a unit identifier and an increment variable. Example:
    * 
        timeResolution : {
            unit        : "HOUR",  //Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
            increment   : 1
        }
    * 
    */
    timeResolution      : null,

    /**
    * @cfg {Object} headerConfig An object containing one or more {@link Sch.preset.ViewPresetHeaderRow} rows defining how your headers shall be composed. 
    * Your 'main' unit should be the middle header unit. This object can contain "bottom", "middle" and "top" header definitions. The 'middle' header is mandatory.
    */
    headerConfig        : null,
    
    
    headers             : null,
    mainHeader          : 0,
    
    
    constructor : function (config) {
        Ext.apply(this, config);
    },

    getHeaders : function () {
        if (this.headers) return this.headers;
        
        var headerConfig        = this.headerConfig;
        
        this.mainHeader         = headerConfig.top ? 1 : 0;
        
        return this.headers     = [].concat(headerConfig.top || [], headerConfig.middle || [], headerConfig.bottom || []);
    },
    
    
    getMainHeader : function () {
        return this.getHeaders()[ this.mainHeader ];
    },
    
    
    getBottomHeader : function () {
        var headers     = this.getHeaders();
        
        return headers[ headers.length - 1 ];
    },
    
    
    getTimeAxisConfig : function () {
        return {
            mainUnit                : this.getMainHeader().unit,
            
            unit                    : this.getBottomHeader().unit,
            increment               : this.getBottomHeader().increment,
            
            shiftUnit               : this.shiftUnit,
            shiftIncrement          : this.shiftIncrement,
            
            defaultSpan             : this.defaultSpan,
            
            resolutionUnit          : this.timeResolution.unit,
            resolutionIncrement     : this.timeResolution.increment
        };
    },
    
    
    clone : function () {
        var config      = {};
        var me          = this;
        
        Ext.each([
            'columnWidth',
            'rowHeight',
            'timeAxisColumnWidth',
            'dateFormat',
            'shiftUnit',
            'shiftIncrement',
            'defaultSpan',
            'timeResolution',
            'headerConfig'
        ], function (name) {
            config[ name ] = me[ name ];
        });
        
        return new this.self(Ext.clone(config));
    }    
});

/**
@class Sch.preset.Manager
@singleton

Provides a registry of the possible view presets that any instance of a Panel with {@link Sch.mixin.SchedulerPanel} mixin can use.

See the {@link Sch.preset.ViewPreset} and {@link Sch.preset.ViewPresetHeaderRow} classes for a description of the view preset properties.

Available presets are:

- `minuteAndHour` - creates 2 level headers - hour and minutes within it
- `hourAndDay` - creates 2 level headers - day and hours within it: {@img scheduler/images/hourAndDay.png}
- `dayAndWeek` - creates 2 level headers - week and days within it: {@img scheduler/images/dayAndWeek.png}
- `weekAndDay` - just like `dayAndWeek` but with different formatting: {@img scheduler/images/weekAndDay.png}
- `weekAndMonth` - creates 2 level headers - month and weeks within it: {@img scheduler/images/weekAndMonth.png}

- `monthAndYear` - creates 2 level headers - year and months within it: {@img scheduler/images/monthAndYear.png}
- `year` - creates 2 level headers - year and quarters within it: {@img scheduler/images/year-preset.png}
- `weekAndDayLetter` - creates a 2 level header - with weeks and day letters within it.
- `weekDateAndMonth` - creates 2 level headers - month and weeks within it (weeks shown by first day only): {@img scheduler/images/weekDateAndMonth.png}

You can register your own preset with the {@link #registerPreset} call

*/

Ext.define('Sch.preset.Manager', {
    extend: 'Ext.util.MixedCollection',
    requires: [
        'Sch.util.Date',
        'Sch.preset.ViewPreset'
    ],
    mixins: ['Sch.mixin.Localizable'],

    singleton: true,

    constructor : function() {
        this.callParent(arguments);
        this.registerDefaults();
    },

    /**
    * Registers a new view preset to be used by any scheduler grid or tree on the page.
    * @param {String} name The unique name identifying this preset
    * @param {Object} config The configuration properties of the view preset (see {@link Sch.preset.ViewPreset} for more information)
    */
    registerPreset : function(name, cfg) {
        if (cfg) {
            var headerConfig    = cfg.headerConfig;
            var DATE            = Sch.util.Date;

            // Make sure date "unit" constant specified in the preset are resolved
            for (var o in headerConfig) {
                if (headerConfig.hasOwnProperty(o)) {
                    if (DATE[headerConfig[o].unit]) {
                        headerConfig[o].unit = DATE[headerConfig[o].unit.toUpperCase()];
                    }
                }
            }

            if (!cfg.timeColumnWidth) {
                cfg.timeColumnWidth = 50;
            }

            // Resolve date units
            if (cfg.timeResolution && DATE[cfg.timeResolution.unit]) {
                cfg.timeResolution.unit = DATE[cfg.timeResolution.unit.toUpperCase()];
            }

            // Resolve date units
            if (cfg.shiftUnit && DATE[cfg.shiftUnit]) {
                cfg.shiftUnit = DATE[cfg.shiftUnit.toUpperCase()];
            }
        }

        if (this.isValidPreset(cfg)) {
            if (this.containsKey(name)){
                this.removeAtKey(name);
            }
            this.add(name, new Sch.preset.ViewPreset(cfg));
        } else {
            throw 'Invalid preset, please check your configuration';
        }
    },

    isValidPreset : function(cfg) {
        var D = Sch.util.Date,
            valid = true,
            validUnits = Sch.util.Date.units;

        // Make sure all date "unit" constants are valid
        for (var o in cfg.headerConfig) {
            if (cfg.headerConfig.hasOwnProperty(o)) {
                valid = valid && Ext.Array.indexOf(validUnits, cfg.headerConfig[o].unit) >= 0;
            }
        }

        if (cfg.timeResolution) {
            valid = valid && Ext.Array.indexOf(validUnits, cfg.timeResolution.unit) >= 0;
        }

        if (cfg.shiftUnit) {
            valid = valid && Ext.Array.indexOf(validUnits, cfg.shiftUnit) >= 0;
        }

        return valid;
    },

    /**
    * Fetches a view preset from the global cache
    * @param {String} name The name of the preset
    * @return {Object} The view preset, see {@link Sch.preset.ViewPreset} for more information
    */
    getPreset : function(name) {
        return this.get(name);
    },

    /**
    * Deletes a view preset
    * @param {String} name The name of the preset
    */
    deletePreset : function(name) {
        this.removeAtKey(name);
    },

    registerDefaults : function() {
        var pm = this,
            vp = this.defaultPresets;

        for (var p in vp) {
            pm.registerPreset(p, vp[p]);
        }
    },

    defaultPresets : {
        secondAndMinute : {
            timeColumnWidth     : 30,   // Time column width (used for rowHeight in vertical mode)
            rowHeight           : 24,    // Only used in horizontal orientation
            resourceColumnWidth : 100,   // Only used in vertical orientation
            displayDateFormat   : 'G:i:s', // Controls how dates will be displayed in tooltips etc
            shiftIncrement      : 10,     // Controls how much time to skip when calling shiftNext and shiftPrevious.
            shiftUnit           : "MINUTE",// Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
            defaultSpan         : 24,    // By default, if no end date is supplied to a view it will show 24 hours
            timeResolution      : {      // Dates will be snapped to this resolution
                unit        : "SECOND",  // Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
                increment   : 5
            },
            headerConfig        : {      // This defines your header, you must include a "middle" object, top/bottom are optional. For each row you can define "unit", "increment", "dateFormat", "renderer", "align", and "scope"
                middle  : {
                    unit        : "SECOND",
                    increment   : 10,
                    align       : 'center',
                    dateFormat  : 's'
                },
                top     : {
                    unit        : "MINUTE",
                    align       : 'center',
                    dateFormat  : 'D, d g:iA'
                }
            }
        },
        minuteAndHour : {
            timeColumnWidth     : 100,   // Time column width (used for rowHeight in vertical mode)
            rowHeight           : 24,    // Only used in horizontal orientation
            resourceColumnWidth : 100,   // Only used in vertical orientation
            displayDateFormat   : 'G:i', // Controls how dates will be displayed in tooltips etc
            shiftIncrement      : 1,     // Controls how much time to skip when calling shiftNext and shiftPrevious.
            shiftUnit           : "HOUR",// Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
            defaultSpan         : 24,    // By default, if no end date is supplied to a view it will show 24 hours
            timeResolution      : {      // Dates will be snapped to this resolution
                unit        : "MINUTE",  // Valid values are "MILLI", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR".
                increment   : 30
            },
            headerConfig        : {      // This defines your header, you must include a "middle" object, top/bottom are optional. For each row you can define "unit", "increment", "dateFormat", "renderer", "align", and "scope"
                middle  : {
                    unit        : "MINUTE",
                    increment   : '30',
                    align       : 'center',
                    dateFormat  : 'i'
                },
                top     : {
                    unit        : "HOUR",
                    align       : 'center',
                    dateFormat  : 'D, gA/d'
                }
            }
        },
        hourAndDay : {
            timeColumnWidth     : 60,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'G:i',
            shiftIncrement      : 1,
            shiftUnit           : "DAY",
            defaultSpan         : 24,
            timeResolution      : {
                unit        : "MINUTE",
                increment   : 30
            },
            headerConfig        : {
                middle      : {
                    unit        : "HOUR",
                    align       : 'center',
                    dateFormat  : 'G:i'
                },
                top         : {
                    unit        : "DAY",
                    align       : 'center',
                    dateFormat  : 'D d/m'
                }
            }
        },
        dayAndWeek : {
            timeColumnWidth     : 100,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d G:i',
            shiftUnit           : "DAY",
            shiftIncrement      : 1,
            defaultSpan         : 5,
            timeResolution      : {
                unit        : "HOUR",
                increment   : 1
            },
            headerConfig        : {
                middle      : {
                    unit        : "DAY",
                    align       : 'center',
                    dateFormat  : 'D d M'
                },
                top : {
                    unit        : "WEEK",
                    align       : 'center',
                    renderer    : function(start, end, cfg) {
                        return Sch.util.Date.getShortNameOfUnit("WEEK") + '.' + Ext.Date.format(start, 'W M Y');
                    }
                }
            }
        },

        weekAndDay : {
            timeColumnWidth     : 100,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftUnit           : "WEEK",
            shiftIncrement      : 1,
            defaultSpan         : 1,
            timeResolution      : {
                unit        : "DAY",
                increment   : 1
            },
            headerConfig        : {
                bottom : {
                    unit        : "DAY",
                    align       : 'center',
                    increment   : 1,
                    dateFormat  : 'd/m'
                },
                middle : {
                    unit        : "WEEK",
                    dateFormat  : 'D d M'
                }
            }
        },

        weekAndMonth : {
            timeColumnWidth     : 100,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftUnit           : "WEEK",
            shiftIncrement      : 5,
            defaultSpan         : 6,
            timeResolution      : {
                unit        : "DAY",
                increment   : 1
            },
            headerConfig        : {
                middle      : {
                    unit    : "WEEK",
                    renderer: function(start, end, cfg) {
                        return Ext.Date.format(start, 'd M');
                    }
                },
                top         : {
                    unit        : "MONTH",
                    align       : 'center',
                    dateFormat  : 'M Y'
                }
            }
        },

        monthAndYear : {
            timeColumnWidth     : 110,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftIncrement      : 3,
            shiftUnit           : "MONTH",
            defaultSpan         : 12,
            timeResolution      : {
                unit        : "DAY",
                increment   : 1
            },
            headerConfig        : {
                middle      : {
                    unit        : "MONTH",
                    align       : 'center',
                    dateFormat  : 'M Y'
                },
                top         : {
                    unit        : "YEAR",
                    align       : 'center',
                    dateFormat  : 'Y'
                }
            }
        },
        year : {
            timeColumnWidth     : 100,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftUnit           : "YEAR",
            shiftIncrement      : 1,
            defaultSpan         : 1,
            timeResolution      : {
                unit        : "MONTH",
                increment   : 1
            },
            headerConfig        : {
                middle      : {
                    unit        : "QUARTER",
                    align       : 'center',
                    renderer    : function(start, end, cfg) {
                        return Ext.String.format(Sch.util.Date.getShortNameOfUnit("QUARTER").toUpperCase() + '{0}', Math.floor(start.getMonth() / 3) + 1);
                    }
                },
                top         : {
                    unit        : "YEAR",
                    align       : 'center',
                    dateFormat  : 'Y'
                }
            }
        },
        manyYears : {
            timeColumnWidth     : 50,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftUnit           : "YEAR",
            shiftIncrement      : 1,
            defaultSpan         : 1,
            timeResolution      : {
                unit        : "YEAR",
                increment   : 1
            },
            headerConfig        : {
                middle      : {
                    unit        : "YEAR",
                    align       : 'center',
                    dateFormat  : 'Y'
                }
            }
        },
        weekAndDayLetter : {
            timeColumnWidth     : 20,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftUnit           : "WEEK",
            shiftIncrement      : 1,
            defaultSpan         : 10,
            timeResolution      : {
                unit        : "DAY",
                increment   : 1
            },
            headerConfig        : {
                bottom  : {
                    unit        : "DAY",
                    align       : 'center',
                    renderer    : function(start) {
                        return Ext.Date.dayNames[start.getDay()].substring(0, 1);
                    }
                },
                middle          : {
                    unit        : "WEEK",
                    dateFormat  : 'D d M Y'
                }
            }
        },
        weekDateAndMonth : {
            timeColumnWidth     : 30,
            rowHeight           : 24,
            resourceColumnWidth : 100,
            displayDateFormat   : 'Y-m-d',
            shiftUnit           : "WEEK",
            shiftIncrement      : 1,
            defaultSpan         : 10,
            timeResolution      : {
                unit        : "DAY",
                increment   : 1
            },
            headerConfig : {
                middle      : {
                    unit        : "WEEK",
                    align       : 'center',
                    dateFormat  : 'd'
                },
                top         : {
                    unit        : "MONTH",
                    dateFormat  : 'Y F'
                }
            }
        }
    }
});

/**
@class Sch.feature.AbstractTimeSpan
@extends Ext.util.Observable

Plugin for visualizing "global" time span in the scheduler grid, these can by styled easily using just CSS. This is an abstract class not intended for direct use.

*/
Ext.define("Sch.feature.AbstractTimeSpan", {
    extend              : 'Ext.AbstractPlugin',
    lockableScope       : 'top',
    
    schedulerView       : null,
    timeAxis            : null,
    containerEl         : null,
    
    // If lines/zones should stretch to fill the whole view container element in case the table does not fill it
    expandToFitView     : false,
    
    disabled            : false,
    
    /**
     * @property {String} cls An internal css class which is added to each rendered timespan element
     * @private
     */
    cls                 : null,
    
    /**
     * @cfg {String} clsField Name of field  
     */
    clsField            : 'Cls',
    
    /**
     * @cfg {Ext.XTemplate} template Template to render the timespan elements  
     */
    template            : null,
    
    /**
     * @cfg {Ext.data.Store/String} store A store with timespan data, or a string identifying a store.
     */
    store               : null,
    
    renderElementsBuffered      : false,
    
    /**
     * @cfg {Int} renderDelay Delay the zones rendering by this amount (in ms) to speed up the default rendering of rows and events.
     */
    renderDelay                 : 15,

    // true to refresh the sizes of the rendered elements when an item in the bound view changes
    // false to do a full refresh instead
    refreshSizeOnItemUpdate     : true,

    _resizeTimer                : null,
    _renderTimer                : null,
    
    /**
     * @cfg {Boolean} showHeaderElements Set this to `true` to show indicators in the timeline header area.
     * 
     * Header indicators are placed right above the corresponding element of the scheduling view. You can customize the HTML markup
     * for these indicators with the {@link #headerTemplate} config. Note that the indicators are rendered as a regular div element,
     * which will be styled differently in modern vs legacy browsers.
     *
     */
    showHeaderElements          : false,
    
    /**
     * @private
     * @cfg {Ext.XTemplate} template Template to render the elements at header 
     */
    headerTemplate              : null,
    
    
    /**
     * @cfg {String/Ext.XTemplate} innerHeaderTpl A template providing additional markup to render into each timespan header element
     */
    innerHeaderTpl            : null,
    
    headerContainerCls          : 'sch-header-secondary-canvas',
    headerContainerEl           : null,

    constructor : function(cfg) {
        // unique css class to be able to identify only the zones belonging to this plugin instance
        this.uniqueCls = this.uniqueCls || ('sch-timespangroup-' + Ext.id());
        
        Ext.apply(this, cfg);

        this.callParent(arguments);
    },

    
    /**
     * @param {Boolean} disabled Pass `true` to disable the plugin and remove all rendered elements.
     */
    setDisabled : function(disabled) {
        if (disabled) {
            this.removeElements();
        }
        
        this.disabled = disabled;
    },

    
    removeElements : function () {
        this.removeBodyElements();
        
        if (this.showHeaderElements) {
            this.removeHeaderElements();
        }
    },
    
    //Returns the currently rendered DOM elements of this plugin (if any), as a {@link Ext.CompositeElementLite} collection.
    getBodyElements : function() {
        if (this.containerEl) {
            return this.containerEl.select('.' + this.uniqueCls);
        }

        return null;
    },
    
    /**
     * Returns container to render header elements.
     * 
     * @return {Ext.dom.Element}
     */
    getHeaderContainerEl : function() {
        var containerEl = this.headerContainerEl,
            prefix = Ext.baseCSSPrefix,
            parent;
            
        if (!containerEl || !containerEl.dom) {
            if (this.schedulerView.isHorizontal()) {
                parent = this.panel.getTimeAxisColumn().headerView.containerEl;
            } else {
                parent = this.panel.el.down('.' + prefix + 'grid-inner-locked' +
                    ' .' + prefix + 'panel-body' +
                    ' .' + prefix + 'grid-view');
            }
            
            if (parent) {
                containerEl = parent.down('.' + this.headerContainerCls);
                
                if (!containerEl) {
                    containerEl = parent.appendChild({
                        cls : this.headerContainerCls
                    });
                }
                
                this.headerContainerEl = containerEl;
            }
        }

        return containerEl;
    },
    
    
    getHeaderElements : function() {
        var containerEl = this.getHeaderContainerEl();
        
        if (containerEl) {
            return containerEl.select('.' + this.uniqueCls);
        }

        return null;
    },
    
    
    // private
    removeBodyElements : function() {
        var els = this.getBodyElements();
        
        if (els) {
            els.each(function(el) { el.destroy(); });
        }
    },
    
    
    removeHeaderElements : function() {
        var els = this.getHeaderElements();
        
        if (els) {
            els.each(function(el) { el.destroy(); });
        }
    },
    
    /**
     * Returns id of element for data record.
     * 
     * @param {Ext.data.Model} record
     * 
     * @retrun {String}
     */
    getElementId : function(record) {
        return this.uniqueCls + '-' + record.internalId;
    },
    
    /**
     * Returns id of header element for data record.
     * 
     * @param {Ext.data.Model} record
     * 
     * @retrun {String}
     */
    getHeaderElementId : function(record) {
        return this.uniqueCls + '-header-' + record.internalId;
    },
    
    /**
     * Returns template data to render elements.
     * 
     * @param {Ext.data.Model} record
     * 
     * @return {Object}
     */
    getTemplateData : function(record) {
        return this.prepareTemplateData ? this.prepareTemplateData(record) : record.data;
    },
    
    
    /**
     * Return element class for a record.
     * 
     * @param {Ext.data.Model} record Data record
     * @param {Object} data Template data
     * 
     * @return {String}
     */
    getElementCls : function(record, data) {
        var clsField = record.clsField || this.clsField;
            
        if (!data) {
            data = this.getTemplateData(record);
        }
        
        return this.cls + ' ' + this.uniqueCls + ' ' + (data[clsField] || '');
    },
    
    
    /**
     * Return header element class for data record.
     * 
     * @param {Ext.data.Model} record Data record
     * @param {Object} data
     * 
     * @return {String}
     */
    getHeaderElementCls : function(record, data) {
        var clsField = record.clsField || this.clsField;
            
        if (!data) {
            data = this.getTemplateData(record);
        }

        return 'sch-header-indicator ' + this.uniqueCls + ' ' + (data[clsField] || '');
    },
    
    
    init:function(scheduler) {
        // TODO COMMENT
        if (Ext.versions.touch && !scheduler.isReady()) {
            scheduler.on('viewready', function() { this.init(scheduler); }, this);
            return;
        }
        
        if (Ext.isString(this.innerHeaderTpl)) {
            this.innerHeaderTpl = new Ext.XTemplate(this.innerHeaderTpl);
        }
        
        var innerHeaderTpl = this.innerHeaderTpl;
        
        if (!this.headerTemplate) {
            this.headerTemplate = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div id="{id}" class="{cls}" style="{side}:{position}px;">' +
                    (innerHeaderTpl ? '{[this.renderInner(values)]}' : '') +
                    '</div>',
                '</tpl>',
                {
                    renderInner : function(values) {
                        return innerHeaderTpl.apply(values);
                    }
                }
            );
        }

        this.schedulerView = scheduler.getSchedulingView(); 
        this.panel = scheduler;
        this.timeAxis = scheduler.getTimeAxis();

        this.store = Ext.StoreManager.lookup(this.store);

        if (!this.store) {
            Ext.Error.raise("Error: You must define a store for this plugin");
        }

        if (!this.schedulerView.getEl()) {
            this.schedulerView.on({
                afterrender : this.onAfterRender, 
                scope       : this
            });
        } else {
            this.onAfterRender();
        }

        this.schedulerView.on({
            destroy     : this.onDestroy, 
            scope       : this
        });
    },
    
    
    onAfterRender : function (scheduler) {
        var view            = this.schedulerView;
        this.containerEl    = view.getSecondaryCanvasEl();

        this.storeListeners = {
            load            : this.renderElements,
            datachanged     : this.renderElements, 
            clear           : this.renderElements,
            
            // Ext JS
            add             : this.refreshSingle, 
            remove          : this.renderElements, 
            update          : this.refreshSingle, 
            
            // Sencha Touch
            addrecords      : this.refreshSingle,
            removerecords   : this.renderElements,
            updaterecord    : this.refreshSingle,

            scope           : this
        };

        this.store.on(this.storeListeners);

        if (Ext.data.NodeStore && view.store instanceof Ext.data.NodeStore) {
            // if the view is animated, then update the elements in "after*" events (when the animation has completed)
            if (view.animate) {
                // NOT YET SUPPORTED
//                view.on({
//                    afterexpand     : this.renderElements, 
//                    aftercollapse   : this.renderElements,
//                    
//                    scope           : this
//                });
            } else {
                view.mon(view.store, {
                    expand      : this.renderElements, 
                    collapse    : this.renderElements,
                    
                    scope       : this
                });
            }
        }
        
        view.on({
            bufferedrefresh     : this.renderElements,
            refresh             : this.renderElements,
            itemadd             : this.refreshSizeOnItemUpdate ? this.refreshSizes : this.renderElements,
            itemremove          : this.refreshSizeOnItemUpdate ? this.refreshSizes : this.renderElements,
            itemupdate          : this.refreshSizeOnItemUpdate ? this.refreshSizes : this.renderElements,

            // start grouping events
            groupexpand         : this.renderElements, 
            groupcollapse       : this.renderElements,
            
            columnwidthchange   : this.renderElements,
            resize              : this.renderElements,

            scope               : this
        });

        if (view.headerCt) {
            view.headerCt.on({
                add         : this.renderElements,
                remove      : this.renderElements,
                scope       : this
            });
        }

        this.panel.on({
            viewchange          : this.renderElements,
            show                : this.refreshSizes,
            orientationchange   : this.forceNewRenderingTimeout,
            
            scope               : this
        });

        var rowContainer = view.getRowContainerEl();

        if (rowContainer && rowContainer.down('.sch-timetd')) {
            this.renderElements();
        }
    },
    
    
    forceNewRenderingTimeout : function () {
        this.renderElementsBuffered = false;
        
        clearTimeout(this._renderTimer);
        clearTimeout(this._resizeTimer);

        this.renderElements();
    },

    
    refreshSizesInternal : function() {
        // This can only be called in Horizontal mode
        if (!this.schedulerView.isDestroyed && this.schedulerView.isHorizontal()) {
    
            // Date here is irrelevant, we just want a fresh height value
            var region = this.schedulerView.getTimeSpanRegion(new Date(), null, this.expandToFitView);
            this.getBodyElements().setHeight(region.bottom - region.top);
        }
    },
    
    refreshSizes : function() {
        clearTimeout(this._resizeTimer);

        this._resizeTimer = Ext.Function.defer(this.refreshSizesInternal, this.renderDelay, this);
    },

    
    renderElements : function() {
        if (this.renderElementsBuffered || this.disabled) return;

        this.renderElementsBuffered = true;

        clearTimeout(this._renderTimer);
        
        // Defer to make sure rendering is not delayed by this plugin
        // deferring on 15 because the cascade delay is 10 (cascading will trigger a view refresh)
        this._renderTimer = Ext.Function.defer(this.renderElementsInternal, this.renderDelay, this);
    },
    
    
    /**
     * Sets element X-coordinate relative direction (rtl or ltr).
     * 
     * @param {Ext.Element} el
     * @param {Number} x
     */
    setElementX : function(el, x) {
        if (this.panel.rtl) {
            el.setRight(x);
        } else {
            el.setLeft(x);
        }
    },

    /**
     * Returns position of header element by date.
     * 
     * @param {Date} date
     * 
     * @return {Number}
     */
    getHeaderElementPosition : function(date) {
        var viewModel = this.schedulerView.getTimeAxisViewModel();
        
        return Math.round(viewModel.getPositionFromDate(date));
    },
    
    
    renderBodyElementsInternal : function (records) {
        Ext.DomHelper.append(this.containerEl, this.generateMarkup(false, records));
    },
    
    
    getHeaderElementData : function(records, isPrint) {
        throw 'Abstract method call';
    },
    
    
    renderHeaderElementsInternal : function (records) {
        var containerEl = this.getHeaderContainerEl();
        
        if (containerEl) {
            Ext.DomHelper.append(containerEl, this.generateHeaderMarkup(false, records));
        }
    },
    
 
    renderElementsInternal : function() {
        this.renderElementsBuffered = false;

        // component could be destroyed during the buffering time frame
        if (this.disabled || this.schedulerView.isDestroyed) return;

        if (Ext.versions.extjs && !this.schedulerView.el.down('table')) return;

        this.removeElements();
        
        this.renderBodyElementsInternal();
        
        if (this.showHeaderElements) {
            this.headerContainerEl = null;
            this.renderHeaderElementsInternal();
        }
    },

    
    /**
     * Generates markup for elements.
     * 
     * @param {Boolean} isPrint
     * @param {Array} records
     *  
     * @return {String}
     */
    generateMarkup : function(isPrint, records) {
        var start       = this.timeAxis.getStart(),
            end         = this.timeAxis.getEnd(),
            data        = this.getElementData(start, end, records, isPrint);

        return this.template.apply(data);
    },
    
    
    /**
     * Generates markup for headers elements.
     * 
     * @param {Boolean} isPrint
     * @param {Array} records
     *  
     * @return {String}
     */
    generateHeaderMarkup : function (isPrint, records) {
        var data = this.getHeaderElementData(records, isPrint);

        return this.headerTemplate.apply(data);
    },


    getElementData : function (viewStart, viewEnd, records, isPrint) {
        throw 'Abstract method call';
    },
    
    
    updateBodyElement : function (record) {
        var el = Ext.get(this.getElementId(record));
        
        if (el) {
            var start       = this.timeAxis.getStart(), 
                end         = this.timeAxis.getEnd(),
                data        = this.getElementData(start, end, [record])[0];

            if (data) {
                // Reapply CSS classes
                el.dom.className = data.$cls;

                el.setTop(data.top);
                this.setElementX(el, data.left);
                
                el.setSize(data.width, data.height);
            } else {
                Ext.destroy(el);
            }
        } else {
            // if element is not found, then its probably a newly added record in the store
            // in this case `renderBodyElementsInternal` will only add markup for that record
            this.renderBodyElementsInternal([ record ]);
        }
    },
    
    
    updateHeaderElement : function (record) {
        var el = Ext.get(this.getHeaderElementId(record));
        
        if (el) {
            var data = this.getHeaderElementData([record])[0];

            if (data) {
                // Reapply CSS classes
                el.dom.className = data.cls;

                if (this.schedulerView.isHorizontal()) {
                    this.setElementX(el, data.position);
                    el.setWidth(data.size);
                } else {
                    el.setTop(data.position);
                    el.setHeight(data.size);
                }
            } else {
                Ext.destroy(el);
            }
        } else {
            // if element is not found, then its probably a newly added record in the store
            // in this case `renderHeaderElementsInternal` will only add markup for that record
            this.renderHeaderElementsInternal([record]);
        }
    },
    
    
    onDestroy : function() {
        clearTimeout(this._renderTimer);
        clearTimeout(this._resizeTimer);

        if (this.store.autoDestroy) {
            this.store.destroy();
        }

        this.store.un(this.storeListeners);
    },
    

    refreshSingle : function(store, record) {
        Ext.each(record, this.updateBodyElement, this);
        
        if (this.showHeaderElements) {
            Ext.each(record, this.updateHeaderElement, this);
        }
    }
}); 

/**
 * @private
 * @class Sch.feature.DragCreator
 * @constructor
 * An internal class which shows a drag proxy while clicking and dragging.
 * Create a new instance of this plugin
 * @param {Sch.schedulerViewPanel} scheduler The scheduler instance
 * @param {Object} config The configuration options
 */
Ext.define("Sch.feature.DragCreator", {
    requires : [
        'Ext.XTemplate',
        'Sch.util.Date',
        'Sch.util.DragTracker',
        'Sch.tooltip.Tooltip',
        'Sch.tooltip.ClockTemplate'
    ],

    /**
     * @cfg {Boolean} disabled true to start disabled
     */
    disabled            : false,

    /**
     * @cfg {Boolean} showHoverTip true to show a time tooltip when hovering over the time cells
     */
    showHoverTip        : true,

    /**
     * @cfg {Boolean} showDragTip true to show a time tooltip when dragging to create a new event
     */
    showDragTip         : true,

    /**
     * @cfg {Int} dragTolerance Number of pixels the drag target must be moved before dragging is considered to have started. Defaults to 2.
     */
    dragTolerance       : 2,

    /**
     * @cfg {Ext.Template} template The HTML template shown when dragging to create new items
     */

    /**
     * An empty function by default, but provided so that you can perform custom validation on the item being created
     * @param {Sch.model.Resource} resourceRecord the resource for which the event is being created
     * @param {Date} startDate
     * @param {Date} endDate
     * @param {Event} e The event object
     * @return {Boolean} isValid True if the creation event is valid, else false to cancel
     */
    validatorFn         : Ext.emptyFn,

    /**
     * @cfg {Object} validatorFnScope
     * The scope for the validatorFn
     */
    validatorFnScope    : null,
    
    
    hoverTipTemplate    : null,


    constructor : function (config) {
        Ext.apply(this, config || {});

        this.lastTime = new Date();
        this.template = this.template || Ext.create("Ext.Template",
            '<div class="sch-dragcreator-proxy">' +
                '<div class="sch-event-inner">&#160;</div>' +
                '</div>',
            {
                compiled       : true,
                disableFormats : true
            }
        );

        this.schedulerView.on("destroy", this.onSchedulerDestroy, this);

        // Lazy setup and rendering of the tooltips
        this.schedulerView.el.on('mousemove', this.setupTooltips, this, { single : true });

        this.callParent([config]);
    },


    /**
     * Enable/disable the plugin
     * @param {Boolean} disabled True to disable this plugin
     */
    setDisabled : function (disabled) {
        this.disabled = disabled;
        if (this.hoverTip) {
            this.hoverTip.setDisabled(disabled);
        }

        if (this.dragTip) {
            this.dragTip.setDisabled(disabled);
        }
    },

    getProxy          : function () {
        if (!this.proxy) {
            this.proxy = this.template.append(this.schedulerView.getSecondaryCanvasEl(), {}, true);
        }
        return this.proxy;
    },

    // private
    onMouseMove       : function (e) {
        var tip = this.hoverTip;

        // If tip is disabled, return
        if (tip.disabled || this.dragging) {
            return;
        }

        if (e.getTarget('.' + this.schedulerView.timeCellCls, 5) && !e.getTarget(this.schedulerView.eventSelector)) {

            var time = this.schedulerView.getDateFromDomEvent(e, 'floor');

            if (time) {
                if (time - this.lastTime !== 0) {
                    this.updateHoverTip(time);

                    if (tip.hidden) { // HACK, find better solution
                        tip[Sch.util.Date.compareUnits(this.schedulerView.getTimeResolution().unit, Sch.util.Date.DAY) >= 0 ? 'addCls' : 'removeCls']('sch-day-resolution');
                        tip.show();
                    }
                }
            } else {
                tip.hide();
                this.lastTime = null;
            }
        } else {
            tip.hide();
            this.lastTime = null;
        }
    },

    // private
    updateHoverTip    : function (date) {
        if (date) {
            var formattedDate = this.schedulerView.getFormattedDate(date);

            this.hoverTip.update(this.hoverTipTemplate.apply({
                date : date,
                text : formattedDate
            }));
            this.lastTime = date;
        }
    },

    // private
    onBeforeDragStart : function (tracker, e) {
        var s = this.schedulerView,
            t = e.getTarget('.' + s.timeCellCls, 5);

        if (t && !e.getTarget(s.eventSelector)) {
            var resourceRecord = s.resolveResource(t);
            var dateTime = s.getDateFromDomEvent(e);

            if (!this.disabled && t && s.fireEvent('beforedragcreate', s, resourceRecord, dateTime, e) !== false) {

                // Save record if the user ends the drag outside the current row
                this.resourceRecord = resourceRecord;

                // Start time of the event to be created
                this.originalStart = dateTime;

                // Constrain the dragging within the current row schedule area
                this.resourceRegion = s.getScheduleRegion(this.resourceRecord, this.originalStart);

                // Save date constraints
                this.dateConstraints = s.getDateConstraints(this.resourceRecord, this.originalStart);

                // TODO apply xStep or yStep to drag tracker
                return true;
            }
        }
        return false;
    },

    // private
    onDragStart       : function () {
        var me = this,
            view = me.schedulerView,
            proxy = me.getProxy(),
            snap = me.schedulerView.snapToIncrement;

        this.dragging = true;

        if (this.hoverTip) {
            this.hoverTip.disable();
        }

        me.start = me.originalStart;
        me.end = me.start;

        if (view.getOrientation() === 'horizontal') {
            me.rowBoundaries = {
                top    : me.resourceRegion.top,
                bottom : me.resourceRegion.bottom
            };

            proxy.setRegion({
                top    : me.rowBoundaries.top,
                right  : me.tracker.startXY[0],
                bottom : me.rowBoundaries.bottom,
                left   : me.tracker.startXY[0]
            });
        } else {
            me.rowBoundaries = {
                left  : me.resourceRegion.left,
                right : me.resourceRegion.right
            };

            proxy.setRegion({
                top    : me.tracker.startXY[1],
                right  : me.resourceRegion.right,
                bottom : me.tracker.startXY[1],
                left   : me.resourceRegion.left
            });
        }

        proxy.show();

        me.schedulerView.fireEvent('dragcreatestart', me.schedulerView);

        if (me.showDragTip) {
            me.dragTip.enable();
            me.dragTip.update(me.start, me.end, true);
            me.dragTip.show(proxy);

            // for some reason Ext set `visibility` to `hidden` after a couple of `.hide()` calls
            me.dragTip.el.setStyle('visibility', 'visible');
        }
    },

    // private
    onDrag            : function (tracker, e) {
        var me = this,
            view = me.schedulerView,
            dragRegion = me.tracker.getRegion().constrainTo(me.resourceRegion),
            dates = view.getStartEndDatesFromRegion(dragRegion, 'round');

        if (!dates) {
            return;
        }

        me.start = dates.start || me.start;
        me.end = dates.end || me.end;

        var dc = me.dateConstraints;

        if (dc) {
            me.end = Sch.util.Date.constrain(me.end, dc.start, dc.end);
            me.start = Sch.util.Date.constrain(me.start, dc.start, dc.end);
        }

        me.valid = this.validatorFn.call(me.validatorFnScope || me, me.resourceRecord, me.start, me.end) !== false;

        if (me.showDragTip) {
            me.dragTip.update(me.start, me.end, me.valid);
        }

        Ext.apply(dragRegion, me.rowBoundaries);

        this.getProxy().setRegion(dragRegion);
    },

    // private
    onDragEnd         : function (tracker, e) {
        var me = this,
            s = me.schedulerView,
            doFinalize = true;

        me.dragging = false;

        if (me.showDragTip) {
            me.dragTip.disable();
        }

        if (!me.start || !me.end || (me.end - me.start <= 0)) {
            me.valid = false;
        }

        me.createContext = {
            start          : me.start,
            end            : me.end,
            resourceRecord : me.resourceRecord,
            e              : e,
            finalize       : function () {
                me.finalize.apply(me, arguments);
            }
        };

        if (me.valid) {
            doFinalize = s.fireEvent('beforedragcreatefinalize', me, me.createContext, e) !== false;
        }

        if (doFinalize) {
            me.finalize(me.valid);
        }
    },

    finalize : function (doCreate) {
        var context = this.createContext;
        var schedulerView = this.schedulerView;

        if (doCreate) {
            var ev = Ext.create(schedulerView.eventStore.model);

            // HACK, for the Gantt chart assignments to work properly - we need the task to be in the task store before assigning it
            // to the resource.
            if (Ext.data.TreeStore && schedulerView.eventStore instanceof Ext.data.TreeStore) {
                ev.set('leaf', true);
                schedulerView.eventStore.append(ev);
            }

            ev.assign(context.resourceRecord);
            ev.setStartEndDate(context.start, context.end);
            schedulerView.fireEvent('dragcreateend', schedulerView, ev, context.resourceRecord, context.e);
        } else {
            this.proxy.hide();
        }

        this.schedulerView.fireEvent('afterdragcreate', schedulerView);

        if (this.hoverTip) {
            this.hoverTip.enable();
        }
    },

    tipCfg : {
        trackMouse   : true,
        bodyCssClass : 'sch-hovertip',
        autoHide     : false,
        dismissDelay : 1000,
        showDelay    : 300
    },

    dragging : false,

    setupTooltips : function () {
        var me = this,
            sv = me.schedulerView;

        me.tracker = new Sch.util.DragTracker({
            el        : sv.el,
            tolerance : me.dragTolerance,
            listeners : {
                mousedown       : me.verifyLeftButtonPressed,
                beforedragstart : me.onBeforeDragStart,
                dragstart       : me.onDragStart,
                drag            : me.onDrag,
                dragend         : me.onDragEnd,
                scope           : me
            }
        });

        if (this.showDragTip) {
            this.dragTip = Ext.create("Sch.tooltip.Tooltip", {
                cls           : 'sch-dragcreate-tip',
                schedulerView : sv,
                listeners     : {
                    beforeshow : function () {
                        return me.dragging;
                    }
                }
            });
        }

        if (me.showHoverTip) {
            var gridViewBodyEl = sv.el;

            me.hoverTipTemplate = me.hoverTipTemplate || Ext.create("Sch.tooltip.ClockTemplate");

            me.hoverTip = new Ext.ToolTip(Ext.applyIf({
                renderTo : document.body,
                target   : gridViewBodyEl,
                disabled : me.disabled
            }, me.tipCfg));

            me.hoverTip.on('beforeshow', me.tipOnBeforeShow, me);

            sv.mon(gridViewBodyEl, {
                mouseleave : function () {
                    me.hoverTip.hide();
                },
                mousemove  : me.onMouseMove,
                scope      : me
            });
        }
    },
    
    verifyLeftButtonPressed : function (dragTracker, e) {
        return e.button === 0;
    },
    

    onSchedulerDestroy : function () {
        if (this.hoverTip) {
            this.hoverTip.destroy();
        }

        if (this.dragTip) {
            this.dragTip.destroy();
        }

        if (this.tracker) {
            this.tracker.destroy();
        }

        if (this.proxy) {
            Ext.destroy(this.proxy);
            this.proxy = null;
        }
    },

    tipOnBeforeShow : function (tip) {
        return !this.disabled && !this.dragging && this.lastTime !== null;
    }
});

/**
@class Sch.feature.SchedulerDragZone
@extends Ext.dd.DragZone

A custom scheduler dragzone that also acts as the dropzone, and optionally
constrains the drag to the resource area that contains the dragged element.
 
Generally it should not need to be used directly.
To configure drag and drop use {@link Sch.mixin.SchedulerPanel#cfg-dragConfig SchedulerPanel} dragConfig instead.
 
@constructor
@param {Object} config The object containing the configuration of this model.
*/
Ext.define("Sch.feature.SchedulerDragZone", {
    extend      : "Ext.dd.DragZone",

    requires    : [
        'Sch.tooltip.Tooltip',
        // a missing require of Ext.dd.DragDrop:
        // http://www.sencha.com/forum/showthread.php?276603-4.2.1-Ext.dd.DragDrop-missing-Ext.util.Point-in-dependency-quot-requires-quot
        'Ext.util.Point'
    ],

    repairHighlight     : false,
    repairHighlightColor : 'transparent',
    // this has to be set to `false` because we will manually register the view element in the ScrollManager
    // we don't need to register the dragged element in it
    containerScroll     : false,
    dropAllowed         : "sch-dragproxy",
    dropNotAllowed      : "sch-dragproxy",

    /**
     * @cfg {Boolean} showTooltip Specifies whether or not to show tooltip while dragging event
     */
    showTooltip         : true,
    
    tip                 : null,
    
    schedulerView       : null,


    /**
     * @cfg {Boolean} enableCopy true to enable copy by pressing modifier key
     * (see {@link #enableCopyKey enableCopyKey}) during drag drop.
     */
    enableCopy          : false,

    /**
     *
     * @cfg {String} enableCopyKey
     * Modifier key that should be pressed during drag drop to copy item.
     * Available values are 'CTRL', 'ALT', 'SHIFT'
     */
    enableCopyKey       : 'SHIFT',
    
    /**
     * @cfg {Object} validatorFn
     * 
     * An empty function by default, but provided so that you can perform custom validation on
     * the item being dragged. This function is called during the drag and drop process and also after the drop is made
     * @param {Sch.model.Event[]} dragRecords an array containing the records for the events being dragged
     * @param {Sch.model.Resource} targetResourceRecord the target resource of the the event
     * @param {Date} date The date corresponding to the current mouse position
     * @param {Int} duration The duration of the item being dragged
     * @param {Event} e The event object
     * @return {Boolean} true if the drop position is valid, else false to prevent a drop
     */
    validatorFn         : function (dragRecords, targetResourceRecord, date, duration, e) { return true; },

    /**
     * @cfg {Object} validatorFnScope
     * The scope for the {@link #validatorFn}
     */
    validatorFnScope    : null,
    
    copyKeyPressed      : false,
    
    
    constructor : function (el, config) {
        // Drag drop won't work in IE8 if running in an iframe
        // https://www.assembla.com/spaces/bryntum/tickets/712#/activity/ticket:
        if (Ext.isIE && (Ext.isIE8 || Ext.isIE7 || Ext.ieVersion < 9) && window.top !== window) {
            Ext.dd.DragDropManager.notifyOccluded = true;
        }
        
        var proxy       = this.proxy = this.proxy || Ext.create("Ext.dd.StatusProxy", {
            shadow                  : false,
            dropAllowed             : this.dropAllowed,
            dropNotAllowed          : this.dropNotAllowed,
            
            // HACK, we want the proxy inside the scheduler, so that when user drags the event
            // out of the scheduler el, the event should be cropped by the scheduler edge
            ensureAttachedToBody    : Ext.emptyFn
        });

        this.callParent(arguments);
        this.isTarget   = true;
        this.scroll     = false;
        this.ignoreSelf = false;
        
        var schedulerView   = this.schedulerView;
        
        Ext.dd.ScrollManager.register(schedulerView.el);
        
        schedulerView.ownerCt.el.appendChild(proxy.el);

        if (schedulerView.rtl) {
            proxy.addCls('sch-rtl');
        }
    },
    
    
    destroy : function () {
        this.callParent(arguments);
        if (this.tip) {
            this.tip.destroy();
        }
        Ext.dd.ScrollManager.unregister(this.schedulerView.el);
    },

    // @OVERRIDE
    autoOffset: function (x, y) {
        this.setDelta(0, 0);
    },
    
    // private
    setupConstraints : function (constrainRegion, elRegion, xOffset, yOffset, isHorizontal, tickSize) {
        this.clearTicks();
        
        var xTickSize       = isHorizontal && tickSize > 1 ? tickSize : 0;
        var yTickSize       = !isHorizontal && tickSize > 1 ? tickSize : 0;
        
        this.resetConstraints();
        
        this.initPageX      = constrainRegion.left + xOffset;
        this.initPageY      = constrainRegion.top + yOffset;
        
        var width           = elRegion.right - elRegion.left;
        var height          = elRegion.bottom - elRegion.top;
        
        if (isHorizontal) {
            this.setXConstraint(constrainRegion.left, constrainRegion.right, xTickSize);
            this.setYConstraint(constrainRegion.top + yOffset, constrainRegion.bottom - height + yOffset, yTickSize);
        } else {
            this.setXConstraint(constrainRegion.left + xOffset, constrainRegion.right - width + xOffset, xTickSize);
            this.setYConstraint(constrainRegion.top, constrainRegion.bottom, yTickSize);
        }
    },

    // @OVERRIDE
    setXConstraint: function(iLeft, iRight, iTickSize) {
        this.leftConstraint = iLeft;
        this.rightConstraint = iRight;

        this.minX = iLeft;
        this.maxX = iRight;
        if (iTickSize) { this.setXTicks(this.initPageX, iTickSize); }

        this.constrainX = true;
    },

    // @OVERRIDE
    setYConstraint: function(iUp, iDown, iTickSize) {
        this.topConstraint = iUp;
        this.bottomConstraint = iDown;

        this.minY = iUp;
        this.maxY = iDown;
        if (iTickSize) { this.setYTicks(this.initPageY, iTickSize); }

        this.constrainY = true;
    },
    
    
    // These cause exceptions, and are not needed
    onDragEnter : Ext.emptyFn,
    onDragOut   : Ext.emptyFn,
    
    
    setVisibilityForSourceEvents : function (show) {
        Ext.each(this.dragData.eventEls, function (el) {
            el[ show ? 'show' : 'hide' ]();
        });
    },

    
    // private
    onDragOver: function(e, id){
        this.checkShiftChange();

        var dd          = this.dragData;

        if (!dd.originalHidden) {
            // Hide dragged event elements at this time
            this.setVisibilityForSourceEvents(false);

            dd.originalHidden   = true;
        }

        var start       = dd.startDate;
        var resource    = dd.newResource;

        this.updateDragContext(e);

        if (dd.startDate - start !== 0 || resource !== dd.newResource) {
            this.schedulerView.fireEvent('eventdrag', this.schedulerView, dd.eventRecords, dd.startDate, dd.newResource, dd);
        }

        if (this.showTooltip) {
            this.tip.update(dd.startDate, dd.endDate, dd.valid);
        }
    },

    
    getDragData: function (e) {
        var s           = this.schedulerView,
            t           = e.getTarget(s.eventSelector);

        if (!t) return;

        var eventRecord = s.resolveEventRecord(t);

        // there will be no event record when trying to drag the drag creator proxy for example
        if (!eventRecord || eventRecord.isDraggable() === false || s.fireEvent('beforeeventdrag', s, eventRecord, e) === false) {
            return null;
        }

        var xy          = e.getXY(),
            eventEl     = Ext.get(t),
            eventXY     = eventEl.getXY(),
            offsets     = [ xy[0] - eventXY[0], xy[1] - eventXY[1] ],
            eventRegion = eventEl.getRegion();
            
        // To make sure any elements made visible by hover are not visible when the original element is hidden (using visibility:hidden)
        eventEl.removeCls('sch-event-hover');
            
        var isHorizontal    = s.getOrientation() == 'horizontal';
        var resource        = s.resolveResource(t);

        if (s.constrainDragToResource && !resource) throw 'Resource could not be resolved for event: ' + eventRecord.getId();

        this.setupConstraints(
            s.getScheduleRegion(s.constrainDragToResource ? resource : null, eventRecord), 
            eventRegion, 
            offsets[ 0 ], offsets[ 1 ],
            isHorizontal,
            s.getSnapPixelAmount()
        );

        var origStart           = eventRecord.getStartDate(),
            origEnd             = eventRecord.getEndDate(),
            timeAxis            = s.timeAxis,
            relatedRecords      = this.getRelatedRecords(eventRecord),
            eventEls            = [ eventEl ];

        // Collect additional elements to drag
        Ext.Array.each(relatedRecords, function (r) {
            var el = s.getElementFromEventRecord(r);

            if (el) eventEls.push(el);
        });

        var dragData = {
            offsets             : offsets,
            repairXY            : eventXY,
            
            eventEls            : eventEls,
            
            eventRecords        : [ eventRecord ].concat(relatedRecords),
            relatedEventRecords : relatedRecords,
            
            resourceRecord      : resource,
            
            sourceDate          : s.getDateFromCoordinate(xy[ isHorizontal ? 0 : 1 ]),
            origStart           : origStart,
            origEnd             : origEnd,
            startDate           : origStart,
            endDate             : origEnd,
            timeDiff            : 0,
            
            startsOutsideView   : origStart < timeAxis.getStart(),
            endsOutsideView     : origEnd > timeAxis.getEnd(),
            
            duration            : origEnd - origStart,
            
            bodyScroll          : Ext.getBody().getScroll(),
            eventObj            : e // So we can know if SHIFT/CTRL was pressed
        };

        dragData.ddel = this.getDragElement(eventEl, dragData);

        return dragData;
    },    
    
    onStartDrag : function (x, y) {
        var s       = this.schedulerView,
            dd      = this.dragData;

        s.fireEvent('eventdragstart', s, dd.eventRecords);
    },


    getCopyKeyPressed : function() {
        return Boolean(this.enableCopy && this.dragData.eventObj[ this.enableCopyKey.toLowerCase() + 'Key' ]);
    },

    
    checkShiftChange : function() {
        var copyKeyPressed  = this.getCopyKeyPressed(),
            dd              = this.dragData;

        if (copyKeyPressed !== this.copyKeyPressed) {
            this.copyKeyPressed = copyKeyPressed;

            if (copyKeyPressed) {
                dd.refElements.addCls('sch-event-copy');
                this.setVisibilityForSourceEvents(true);
            } else {
                dd.refElements.removeCls('sch-event-copy');
                this.setVisibilityForSourceEvents(false);
            }
        }
    },


    onKey : function(e) {
        if (e.getKey() === e[ this.enableCopyKey ]) this.checkShiftChange();
    },


    // HACK, overriding private method, proxy needs to be shown before aligning to it
    startDrag : function() {
        if (this.enableCopy) {
            Ext.EventManager.on(document, "keydown", this.onKey, this);
            Ext.EventManager.on(document, "keyup", this.onKey, this);
        }
        
        var retVal              = this.callParent(arguments);
        var dragData            = this.dragData;

        // This is the representation of the original element inside the proxy
        dragData.refElement     = this.proxy.el.down('#sch-id-dd-ref');
        dragData.refElements    = this.proxy.el.select('.sch-event');

        if (this.showTooltip) {
            var s               = this.schedulerView;

            if (!this.tip) {
                this.tip        = Ext.create("Sch.tooltip.Tooltip", {
                    schedulerView   : s,
                    cls             : 'sch-dragdrop-tip',
                    renderTo        : document.body
                });
            }
            this.tip.update(dragData.origStart, dragData.origEnd, true);
            // Seems required as of Ext 4.1.0, to clear the visibility:hidden style.
            this.tip.el.setStyle('visibility');
            this.tip.show(dragData.refElement, dragData.offsets[ 0 ]);
        }

        this.copyKeyPressed     = this.getCopyKeyPressed();

        if (this.copyKeyPressed) {
            dragData.refElements.addCls('sch-event-copy');
            dragData.originalHidden = true;
        }

        return retVal;
    },

    
    endDrag : function() {
        if (this.enableCopy) {
            Ext.EventManager.un(document, "keydown", this.onKey, this);
            Ext.EventManager.un(document, "keyup", this.onKey, this);
        }
        this.callParent(arguments);
    },

    
    updateRecords : function (context) {
        var me              = this,
            s               = me.schedulerView,
            resourceStore   = s.resourceStore,
            newResource     = context.newResource,
            draggedEvent    = context.eventRecords[0],
            toAdd           = [],
            copyKeyPressed  = this.getCopyKeyPressed(),
            eventsStore     = draggedEvent.store;

        if (copyKeyPressed) {
            draggedEvent    = draggedEvent.copy();
            toAdd.push(draggedEvent);
        }
        
        var resourceRecord  = context.resourceRecord;

        // Process dragged record
        draggedEvent.beginEdit();
        if (newResource !== resourceRecord) {
            draggedEvent.unassign(resourceRecord);
            draggedEvent.assign(newResource);
        }

        draggedEvent.setStartDate(context.startDate, true, s.eventStore.skipWeekendsDuringDragDrop);
        draggedEvent.endEdit();

        // Process related records
        var timeDiff            = context.timeDiff,
            isTreeStore         = Ext.data.TreeStore && resourceStore instanceof Ext.data.TreeStore;
            
        var flatResourceStore   = isTreeStore ? s.store : resourceStore;

        var indexDiff           = flatResourceStore.indexOf(resourceRecord) - flatResourceStore.indexOf(newResource);

        Ext.each(context.relatedEventRecords, function (related) {
            // grabbing resource early, since after ".copy()" the record won't belong to any store
            // and ".getResource()" won't work
            var relatedResource = related.getResource();
            
            if (copyKeyPressed) {
                related         = related.copy();
                toAdd.push(related);
            }

            related.beginEdit();
            
            related.shift(Ext.Date.MILLI, timeDiff);

            var newIndex        = flatResourceStore.indexOf(relatedResource) - indexDiff;
            
            if (newIndex < 0) newIndex = 0;
            if (newIndex >= flatResourceStore.getCount()) newIndex = flatResourceStore.getCount() - 1;

            related.setResource(flatResourceStore.getAt(newIndex));
            
            related.endEdit();
        });

        if (toAdd.length) eventsStore.add(toAdd);

        // Tell the world there was a succesful drop
        s.fireEvent('eventdrop', s, context.eventRecords, copyKeyPressed);
    },

    
    isValidDrop : function (oldResource, newResource, sourceEvent) {
        // Not allowed to assign an event twice to the same resource - 
        // this may happen when scheduler is loaded with the resource store and task store data from gantt
        // same event (task) may be rendered several times in this case
        if (oldResource !== newResource && sourceEvent.isAssignedTo(newResource)) {
            return false;
        }
        
        return true;
    },
    

    resolveResource : function (xy, e) {
        var proxyDom        = this.proxy.el.dom;
        var bodyScroll      = this.dragData.bodyScroll;

        proxyDom.style.display = 'none';
        var node    = document.elementFromPoint(xy[0] - bodyScroll.left, xy[1] - bodyScroll.top);

        // IE8 likes it twice, for simulated events..
        if (Ext.isIE8 && e && e.browserEvent.synthetic) {
            node    = document.elementFromPoint(xy[0] - bodyScroll.left, xy[1] - bodyScroll.top);
        }

        proxyDom.style.display = 'block';

        if (!node) {
            return null;
        }
        
        var view            = this.schedulerView;

        if (!node.className.match(view.timeCellCls)) {
            var parent = Ext.fly(node).up('.' + view.timeCellCls);

            if (parent) {
                node = parent.dom;
            } else {
                return null;
            }
        }
        return view.resolveResource(node);
    },

    // private
    updateDragContext : function (e) {
        var dd              = this.dragData;

        if (!dd.refElement) {
            return;
        }

        var s               = this.schedulerView,
            proxyRegion     = dd.refElement.getRegion();
            
        if (s.timeAxis.isContinuous()) {
            var newDate     = s.getDateFromCoordinate(e.getXY()[ s.getOrientation() == 'horizontal' ? 0 : 1 ]);
            
            dd.timeDiff     = newDate - dd.sourceDate;
    
            dd.startDate    = s.timeAxis.roundDate(new Date(dd.origStart - 0 + dd.timeDiff));
            dd.endDate      = new Date(dd.startDate - 0 + dd.duration);
        } else {
            var range       = this.resolveStartEndDates(proxyRegion);
            
            dd.startDate    = range.startDate;
            dd.endDate      = range.endDate;
            
            dd.timeDiff     = dd.startDate - dd.origStart;
        }
        
        dd.newResource      = s.constrainDragToResource ? 
                dd.resourceRecord 
            : 
                this.resolveResource([ proxyRegion.left + dd.offsets[ 0 ], proxyRegion.top + dd.offsets[ 1 ] ], e);

        if (dd.newResource) {
            dd.valid        = this.validatorFn.call(this.validatorFnScope || this, dd.eventRecords, dd.newResource, dd.startDate, dd.duration, e);
        } else {
            dd.valid        = false;
        }
    },

    /**
     * Provide your custom implementation of this to allow additional event records to be dragged together with the original one.
     * @param {Sch.model.Event} eventRecord The eventRecord about to be dragged
     * @return {Sch.model.Event[]} An array of event records to drag together with the original event
     */
    getRelatedRecords : function(sourceEventRecord) {
        var s = this.schedulerView;
        var sm = s.selModel;
        var result = [];

        if (sm.selected.getCount() > 1) {
            sm.selected.each(function(rec) {
                if (rec !== sourceEventRecord && rec.isDraggable() !== false) {
                    result.push(rec);
                }
            });
        }

        return result;
    },

    /**
     * This function should return a DOM node representing the markup to be dragged. By default it just returns the selected element(s) that are to be dragged.
     * If dragging multiple events, the clone of the original item should be assigned the special id 'sch-id-dd-ref'
     * @param {Ext.Element} sourceEl The event element that is the source drag element
     * @param {Object} dragData The drag drop context object
     * @return {HTMLElement} The DOM node to drag
     */
    getDragElement : function(sourceEl, dragData) {
        var eventEls            = dragData.eventEls;
        var copy;
        
        var offsetX             = dragData.offsets[ 0 ];
        var offsetY             = dragData.offsets[ 1 ];

        if (eventEls.length > 1) {
            var ctEl            = Ext.core.DomHelper.createDom({
                tag     : 'div',
                cls     : 'sch-dd-wrap',
                style   : { overflow : 'visible' }
            });

            Ext.Array.each(eventEls, function (el) {
                copy            = el.dom.cloneNode(true);
                
                copy.id         = el.dom === sourceEl.dom ? "sch-id-dd-ref" : Ext.id();
                
                ctEl.appendChild(copy);

                var elOffsets   = el.getOffsetsTo(sourceEl);

                // Adjust each element offset to the source event element
                Ext.fly(copy).setStyle({
                    left    : elOffsets[ 0 ] - offsetX + 'px',
                    top     : elOffsets[ 1 ] - offsetY + 'px'
                });
            });

            return ctEl;
        } else {
            copy                = sourceEl.dom.cloneNode(true);
            
            copy.id             = "sch-id-dd-ref";
            copy.style.left     = -offsetX + 'px';
            copy.style.top      = -offsetY + 'px';
            
            return copy;
        }
    },
    
    
    onDragDrop: function (e, id) {
        this.updateDragContext(e);

        var me          = this,
            s           = me.schedulerView,
            target      = me.cachedTarget || Ext.dd.DragDropMgr.getDDById(id),
            dragData    = me.dragData,
            modified    = false,
            doFinalize  = true;

        // Used later in finalizeDrop
        dragData.ddCallbackArgs = [ target, e, id ];

        if (dragData.valid && dragData.startDate && dragData.endDate) {
            dragData.finalize   = function () { me.finalize.apply(me, arguments); };

            // Allow implementor to take control of the flow, by returning false from this listener,
            // to show a confirmation popup etc.
            doFinalize          = s.fireEvent('beforeeventdropfinalize', me, dragData, e) !== false;

              // Internal validation, making sure all dragged records fit inside the view
            if (doFinalize && me.isValidDrop(dragData.resourceRecord, dragData.newResource, dragData.eventRecords[ 0 ])) {
                modified        = (dragData.startDate - dragData.origStart) !== 0 || dragData.newResource !== dragData.resourceRecord;
            }
        }

        if (doFinalize) {
            me.finalize(dragData.valid && modified);
        }
    },

    finalize : function (updateRecords) {
        var me          = this,
            view        = me.schedulerView,
            dragData    = me.dragData;

        if (me.tip) {
            me.tip.hide();
        }

        if (updateRecords){
            // Catch one more edge case, if a taskStore with calendars is used - there is a possible scenario where the UI isn't
            // repainted. In gantt+scheduler demo, move an event in the scheduler a few px and it disappears since Calendar adjusts its start date and scheduler is unaware of this.
            var updated,
                checkerFn = function() {updated = true;};

            view.on('itemupdate', checkerFn, null, { single : true });
            me.updateRecords(dragData);
            view.un('itemupdate', checkerFn, null, { single : true });

            if (!updated) {
                me.onInvalidDrop.apply(me, dragData.ddCallbackArgs);
            } else {
                // For our good friend IE9, the pointer cursor gets stuck without the defer
                if (Ext.isIE9) {
                    me.proxy.el.setStyle('visibility', 'hidden');
                    Ext.Function.defer(me.onValidDrop, 10, me, dragData.ddCallbackArgs);
                } else {
                    me.onValidDrop.apply(me, dragData.ddCallbackArgs);
                }
                view.fireEvent('aftereventdrop', view, dragData.eventRecords);
            }

        } else {
            me.onInvalidDrop.apply(me, dragData.ddCallbackArgs);
        }
    },


    // HACK: Override for IE, if you drag the task bar outside the window or iframe it crashes (missing e.target)
    // https://www.assembla.com/spaces/bryntum/tickets/716
    onInvalidDrop : function (target, e, id) {
        if (Ext.isIE && !e) {
            e       = target;
            target  = target.getTarget() || document.body;
        }

        if (this.tip) {
            this.tip.hide();
        }

        this.setVisibilityForSourceEvents(true);

        var s       = this.schedulerView,
            retVal  = this.callParent([ target, e, id ]);

        s.fireEvent('aftereventdrop', s, this.dragData.eventRecords);

        return retVal;
    },
    
    
    resolveStartEndDates : function(proxyRegion) {
        var dd          = this.dragData,
            startEnd,
            start       = dd.origStart,
            end         = dd.origEnd;
            
        var DATE        = Sch.util.Date;

        if (!dd.startsOutsideView) {
            startEnd    = this.schedulerView.getStartEndDatesFromRegion(proxyRegion, 'round');
            if (startEnd) {
                start   = startEnd.start || dd.startDate;
                end     = DATE.add(start, DATE.MILLI, dd.duration);
            }
        } else if (!dd.endsOutsideView) {
            startEnd    = this.schedulerView.getStartEndDatesFromRegion(proxyRegion, 'round');
            if (startEnd) {
                end     = startEnd.end || dd.endDate;
                start   = DATE.add(end, DATE.MILLI, -dd.duration);
            }
        }

        return {
            startDate   : start,
            endDate     : end
        };
    }
    
});
/**
 * @class Sch.feature.DragDrop
 * @private
 * Internal class enabling drag and drop for event nodes and creating drag proxy (classic or simplified).
 * Type of proxy can be configured with {@link Sch.mixin.SchedulerPanel#cfg-dragConfig SchedulerPanel} dragConfig property.
 * @constructor
 * @param {Sch.panel.SchedulerGrid} scheduler The scheduler instance
 * @param {Object} config The object containing the configuration of this model.
 */
Ext.define("Sch.feature.DragDrop", {

    requires : [
        'Ext.XTemplate',
        'Sch.feature.SchedulerDragZone'
    ],

    /**
     * An empty function by default, but provided so that you can perform custom validation on
     * the item being dragged. This function is called during the drag and drop process and also after the drop is made
     * @param {Sch.model.Event[]} dragRecords an array containing the records for the events being dragged
     * @param {Sch.model.Resource} targetResourceRecord the target resource of the the event
     * @param {Date} date The date corresponding to the current mouse position
     * @param {Int} duration The duration of the item being dragged
     * @param {Event} e The event object
     * @return {Boolean} true if the drop position is valid, else false to prevent a drop
     */
    validatorFn : function(dragRecords, targetResourceRecord, date, duration, e) {
        return true;
    },


    /**
     * @cfg {Object} validatorFnScope
     * The scope for the validatorFn
     */
    validatorFnScope        : null,

    /**
     * @cfg {Object} dragConfig
     * 
     * The config object which will be passed to the `Sch.feature.SchedulerDragZone` instance
     */
    dragConfig              : null,
    
    eventDragZone           : null,


    constructor : function(schedulerView, config) {
        Ext.apply(this, config);

        this.schedulerView = schedulerView;

        var supportsElementFromPoint = !!document.elementFromPoint;

        if (supportsElementFromPoint) {
            this.initProxyLessDD();
        } else {
            if (typeof console !== 'undefined') {
                var c = console;
                c.log('WARNING: Your browser does not support document.elementFromPoint required for the Drag drop feature');
            }
        }

        this.schedulerView.on("destroy", this.cleanUp, this);

        this.callParent([config]);
    },

    
    cleanUp : function() {
        var schedulerView       = this.schedulerView;
        
        if (schedulerView.eventDragZone) {
            schedulerView.eventDragZone.destroy();
        }

        if (schedulerView.dropZone) {
            schedulerView.dropZone.destroy();
        }
    },

    
    initProxyLessDD : function() {
        var s = this.schedulerView;

        // The drag zone behaviour, can't attach to the view el (crash in IE), using panel el instead.
        s.eventDragZone = new Sch.feature.SchedulerDragZone(s.ownerCt.el, Ext.apply({
            ddGroup             : s.id,
            schedulerView       : s,
            validatorFn         : this.validatorFn,
            validatorFnScope    : this.validatorFnScope
        }, this.dragConfig));
    }
});
/**
 * @class Sch.feature.ResizeZone
 * @extends Ext.util.Observable
 * @private
 * Internal classing enabling resizing of rendered events
 * @constructor
 * @param {Sch.panel.SchedulerGrid} scheduler The scheduler instance
 * @param {Object} config The object containing the configuration of this model.
 */


Ext.define("Sch.feature.ResizeZone", {
    extend : "Ext.util.Observable",
    requires : [
        'Ext.resizer.Resizer',
        'Sch.tooltip.Tooltip'
    ],

    /**
      * @cfg showTooltip {Boolean} false to not show a tooltip while resizing
      */
    showTooltip         : true,

    /**
     * An empty function by default, but provided so that you can perform custom validation on
     * the item being resized.
     * @param {Sch.model.Resource} resourceRecord the resource to which the event belongs
     * @param {Sch.model.Event} eventRecord the event being resized
     * @param {Date} startDate
     * @param {Date} endDate
     * @param {Ext.EventObject} e The event object
     * @return {Boolean} true if the new duration is valid, else false to signal that it is not.
     */
    validatorFn         : Ext.emptyFn,

    /**
     * @cfg {Object} validatorFnScope
     * The scope for the validatorFn
     */
    validatorFnScope    : null,
    
    schedulerView       : null,

    origEl              : null,
    handlePos           : null,
    eventRec            : null,
    tip                 : null,

    
    constructor : function(config) {
        Ext.apply(this, config);
        var s = this.schedulerView;

        s.on({
            destroy : this.cleanUp,
            scope   : this
        });

        s.mon(s.el, {
            mousedown       : this.onMouseDown,
            mouseup         : this.onMouseUp,
            scope           : this,
            delegate        : '.sch-resizable-handle'
        });
        this.callParent(arguments);
    },

    onMouseDown : function(e, t) {
        var s               = this.schedulerView;
        var eventRec        = this.eventRec = s.resolveEventRecord(t);
        var isResizable     = eventRec.isResizable();

        if (e.button !== 0 || (isResizable === false || typeof isResizable === 'string' && !t.className.match(isResizable))) {
            return;
        }

        this.eventRec       = eventRec;
        this.handlePos      = this.getHandlePosition(t);
        this.origEl         = Ext.get(e.getTarget('.sch-event'));
        
        s.el.on({
            mousemove   : this.onMouseMove,
            scope       : this,
            single      : true
        });
    },

    onMouseUp : function(e, t) {
        var s = this.schedulerView;

        s.el.un({
            mousemove   : this.onMouseMove,
            scope       : this,
            single      : true
        });
    },

     
    onMouseMove : function(e, t) {
        var s           = this.schedulerView;
        var eventRec    = this.eventRec;

        if (!eventRec || s.fireEvent('beforeeventresize', s, eventRec, e) === false) {
            return;
        }
        
        delete this.eventRec;
        e.stopEvent();

        var handlePos   = this.handlePos;
        this.resizer    = this.createResizer(this.origEl, eventRec, handlePos, e, t);

        this.resizer.resizeTracker.onMouseDown(e, this.resizer[handlePos].dom);

        if (this.showTooltip) {
             if (!this.tip) {
                this.tip = Ext.create("Sch.tooltip.Tooltip", {
                    schedulerView   : s,
                    cls             : 'sch-resize-tip'
                });
            }
            this.tip.update(eventRec.getStartDate(), eventRec.getEndDate(), true);
            this.tip.show(this.origEl);
        }
        s.fireEvent('eventresizestart', s, eventRec);
    },

    getHandlePosition : function(node) {
        if (this.schedulerView.getOrientation() === 'horizontal') {
            if (this.schedulerView.rtl) {
                return node.className.match('start') ? 'east' : 'west';
            }
            return node.className.match('start') ? 'west' : 'east';
        } else {
             return node.className.match('start') ? 'north' : 'south';
        }
    },

    // private
    createResizer : function (el, eventRecord, handlePos) {
        var s                   = this.schedulerView,
            resourceRecord      = s.resolveResource(el),
            increment           = s.getSnapPixelAmount(),
            constrainRegion     = s.getScheduleRegion(resourceRecord, eventRecord),
            dateConstraints     = s.getDateConstraints(resourceRecord, eventRecord),
            height              = el.getHeight,
            isStart             = (s.rtl && handlePos[0] === 'e') || (!s.rtl && handlePos[0] === 'w') || handlePos[0] === 'n',

            resizerCfg          = {
                target          : el,
                isStart         : isStart,
                dateConstraints : dateConstraints,
                resourceRecord  : resourceRecord,
                eventRecord     : eventRecord,
                handles         : handlePos[0],
                minHeight       : height,
                constrainTo     : constrainRegion,

                listeners       : {
                    resizedrag  : this.partialResize,
                    resize      : this.afterResize,
                    scope       : this
                }
            };

        // HACK, make it unique to prevent Ext JS from getting the wrong one if multiple events with same Id exist.
        // Remove this when scheduler has assignment store awareness
        var prevId          = el.id;
        var newId           = '_' + prevId;
            
        el.id               = el.dom.id = newId;
        
        // duplicate the cache entry for this element, so Ext.get(newId) or Ext.get(el) will reference the same entry 
        Ext.cache[ newId ]  = Ext.cache[ prevId ];
        // EOF HACK

        // Apply orientation specific configs
        if (s.getOrientation() === 'vertical') {
            if (increment > 0) {
                var w = el.getWidth();

                Ext.apply(resizerCfg, {
                    minHeight       : increment,
                    // To avoid SHIFT causing a ratio preserve
                    minWidth        : w,
                    maxWidth        : w,
                    heightIncrement : increment
                });
            }
        } else {
            if (increment > 0) {

                Ext.apply(resizerCfg, {
                    minWidth        : increment,
                    // To avoid SHIFT causing a ratio preserve
                    maxHeight       : height,
                    widthIncrement  : increment
                });
            }
        }

        var resizer = Ext.create('Ext.resizer.Resizer', resizerCfg);

        if (resizer.resizeTracker && Ext.isWebKit) {
            // Ignore resizing action if dragging outside the scheduler
            // Fixes WebKit issue https://www.assembla.com/spaces/bryntum/tickets/994#/activity/ticket:
            var old = resizer.resizeTracker.updateDimensions;

            resizer.resizeTracker.updateDimensions = function(e) {
                if (e.getTarget('.sch-timelineview')) {
                    old.apply(this, arguments);
                }
            };
        }
        // Make sure the resizing event is on top of other events
        el.setStyle('z-index', parseInt(el.getStyle('z-index'), 10)+1);
        return resizer;
    },

    getStartEndDates : function(xy) {
        var r           = this.resizer,
            rEl         = r.el,
            s           = this.schedulerView,
            isStart     = r.isStart,
            start,
            end;

        if (isStart) {
            end         = r.eventRecord.getEndDate();
            start       = s.getDateFromXY([s.rtl ? rEl.getRight() : rEl.getLeft() + 1, rEl.getTop()], 'round');
        } else {
            start       = r.eventRecord.getStartDate();
            end         = s.getDateFromXY([s.rtl ? rEl.getLeft() : rEl.getRight(), rEl.getBottom()], 'round');
        }

        if (r.dateConstraints) {
            start       = Sch.util.Date.constrain(start, r.dateConstraints.start, r.dateConstraints.end);
            end         = Sch.util.Date.constrain(end, r.dateConstraints.start, r.dateConstraints.end);
        }

        return {
            start   : start,
            end     : end
        };
    },

    // private
    partialResize : function (r, width, height, e) {
        var s = this.schedulerView,
            startEndDates = this.getStartEndDates(e.getXY()),
            start = startEndDates.start,
            end = startEndDates.end;

        if (!start || !end || ((r.start - start === 0) && (r.end - end === 0))) {
            return;
        }

        var valid = this.validatorFn.call(this.validatorFnScope || this, r.resourceRecord, r.eventRecord, start, end) !== false;

        r.end = end;
        r.start = start;

        s.fireEvent('eventpartialresize', s, r.eventRecord, start, end, r.el);

        if (this.showTooltip) {
            this.tip.update(start, end, valid);
        }
    },

    // private
    afterResize : function (r, w, h, e) {
        if (this.showTooltip) {
            this.tip.hide();
        }
        
        // HACK, restore original id
        // removing extra cache entry for this element
        delete Ext.cache[ r.el.id ];
        
        r.el.id = r.el.dom.id = r.el.id.substr(1);
        // EOF HACK

        var me              = this,
            resourceRecord  = r.resourceRecord,
            eventRecord     = r.eventRecord,
            oldStart        = eventRecord.getStartDate(),
            oldEnd          = eventRecord.getEndDate(),
            start           = r.start || oldStart,
            end             = r.end || oldEnd,
            sv              = me.schedulerView,
            modified        = false,
            doFinalize      = true;

        me.resizeContext    = {
            eventRecord     : eventRecord,
            start           : start,
            end             : end,
            finalize        : function() { me.finalize.apply(me, arguments); }
        };

        if (start && end && (end - start > 0) && // Input sanity check
            ((start - oldStart !== 0) || (end - oldEnd !== 0)) && // Make sure start OR end changed
            me.validatorFn.call(me.validatorFnScope || me, resourceRecord, eventRecord, start, end, e) !== false) {


            // Seems to be a valid resize operation, ask outside world if anyone wants to take control over the finalizing,
            // to show a confirm dialog prior to applying the new values.
            doFinalize = sv.fireEvent('beforeeventresizefinalize', me, me.resizeContext, e) !== false;
            modified = true;
        } else {
            sv.repaintEventsForResource(resourceRecord);
        }

        if (doFinalize) {
            me.finalize(modified);
        }
    },

    finalize : function(updateRecord) {
        var sv = this.schedulerView;
        var context = this.resizeContext;

        if (updateRecord) {
            if (this.resizer.isStart) {
                context.eventRecord.setStartDate(context.start, sv.eventStore.skipWeekendsDuringDragDrop);
            } else {
                context.eventRecord.setEndDate(context.end, sv.eventStore.skipWeekendsDuringDragDrop);
            }
        } else {
            sv.repaintEventsForResource(context.eventRecord.getResource());
        }

        // Destroy resizer
        this.resizer.destroy();

        sv.fireEvent('eventresizeend', sv, context.eventRecord);

        this.resizeContext = null;
    },

    cleanUp : function() {
        if (this.tip) {
            this.tip.destroy();
        }
    }
});

Ext.define("Sch.eventlayout.Horizontal", {
    
    view                    : null,
    
    nbrOfBandsByResource    : null,
    
    
    
    constructor : function(config) {
        Ext.apply(this, config);
        
        this.nbrOfBandsByResource       = {};
    },
    
    
    clearCache : function (resource) {
        if (resource)
            delete this.nbrOfBandsByResource[ resource.internalId ];
        else
            this.nbrOfBandsByResource   = {};
    },
    
    
    getNumberOfBands : function (resource, resourceEvents) {
        if (!this.view.dynamicRowHeight) return 1;
        
        var nbrOfBandsByResource = this.nbrOfBandsByResource;
        
        if (nbrOfBandsByResource.hasOwnProperty(resource.internalId)) return nbrOfBandsByResource[ resource.internalId ];
        
        return this.calculateNumberOfBands(resource, resourceEvents);
    },
    
    
    getRowHeight : function (resource, resourceEvents) {
        var view                    = this.view;
        var nbrOfBandsRequired      = this.getNumberOfBands(resource, resourceEvents);
        
        return (nbrOfBandsRequired * view.rowHeight) - ((nbrOfBandsRequired - 1) * view.barMargin);
    },
    
    
    calculateNumberOfBands : function (resource, resourceEvents) {
        var eventsData      = [];
        // avoid extra `getEventsForResource` call if events are already provided 
        resourceEvents      = resourceEvents || this.view.eventStore.getEventsForResource(resource);
        
        var timeAxis        = this.view.timeAxis;
        
        for (var i = 0; i < resourceEvents.length; i++) {
            var event       = resourceEvents[ i ];
            var start       = event.getStartDate();
            var end         = event.getEndDate();
            
            if (start && end && timeAxis.timeSpanInAxis(start, end)) eventsData[ eventsData.length ] = { start : start, end : end };
        }
        
        return this.applyLayout(eventsData, resource);
    },
    
    
    // TODO DOC
    applyLayout: function (events, resource) {
        var rowEvents = events.slice();

        // Sort events by start date, and text properties.
        rowEvents.sort(this.sortEvents);

        // return a number of bands required
        return this.nbrOfBandsByResource[ resource.internalId ] = this.layoutEventsInBands(0, rowEvents);
    },

    
    // TODO DOC
    sortEvents : function (a, b) {
        var sameStart = (a.start - b.start === 0);

        if (sameStart) {
            return a.end > b.end ? -1 : 1;
        } else {
            return (a.start < b.start) ? -1 : 1;
        }
    },

    
    layoutEventsInBands: function (bandIndex, events) {
        var view        = this.view;
        
        do {
            var event   = events[0],
                bandTop = bandIndex === 0 ? view.barMargin : (bandIndex * view.rowHeight - (bandIndex - 1) * view.barMargin);
                
            if (bandTop >= view.cellBottomBorderWidth) {
                bandTop -= view.cellBottomBorderWidth;
            }
    
            while (event) {
                // Apply band height to the event cfg
                event.top   = bandTop;
    
                // Remove it from the array and continue searching
                Ext.Array.remove(events, event);
                
                event       = this.findClosestSuccessor(event, events);
            }
    
            bandIndex++;
        } while (events.length > 0);

        // Done!
        return bandIndex;
    },

    
    findClosestSuccessor: function (event, events) {
        var minGap = Infinity,
            closest,
            eventEnd = event.end,
            gap;

        for (var i = 0, l = events.length; i < l; i++) {
            gap = events[i].start - eventEnd;

            if (gap >= 0 && gap < minGap) {
                closest = events[i];
                minGap = gap;
            }
        }
        return closest;
    }
});
/*
    @class Sch.eventlayout.Horizontal
    @private

*/
Ext.define("Sch.eventlayout.Vertical", {
    requires : ['Sch.util.Date'],

    constructor : function(config) {
        Ext.apply(this, config);
    },

    // Try to pack the events to consume as little space as possible
    applyLayout: function (events, columnWidth) {

        if (events.length === 0) {
            return;
        }

        // Sort events by start date, and text properties.
        events.sort(this.sortEvents);

        var start, end, 
            view = this.view,
            D = Sch.util.Date,
            band = 1,
            startFraction,
            slot,
            totalAvailableWidth = columnWidth - (2*view.barMargin),
            firstInCluster,
            j;
            
        for (var i = 0, l = events.length; i < l; i++) {
            firstInCluster = events[i];
            start = firstInCluster.start;
            end = firstInCluster.end;
                
            slot = this.findStartSlot(events, firstInCluster);
                
            var cluster = this.getCluster(events, i);
                    
            if (cluster.length > 1) {
                firstInCluster.left = slot.start;
                firstInCluster.width = slot.end - slot.start;
                    
                // If there are multiple slots and events in the cluster have multiple start dates, group all same-start events into first slot
                j = 1;
                    
                while(j < (cluster.length-1) && cluster[j+1].start - firstInCluster.start === 0) {
                    j++;
                }
                    
                // See if there's more than 1 slot available for this cluster, if so - first group in cluster consumes the entire first slot
                var nextSlot = this.findStartSlot(events, cluster[j]);
                if (nextSlot && nextSlot.start < 0.8) {
                    cluster = cluster.slice(0, j);
                }
            }

            var count = cluster.length,
                barWidth = (slot.end-slot.start)/count;
                
            // Apply fraction values
            for (j = 0; j < count; j++) {
                cluster[j].width = barWidth;
                cluster[j].left = slot.start + (j*barWidth);
            }
            
            i += count - 1;
        }
        
        for (i = 0, l = events.length; i < l; i++) {
            events[i].width = events[i].width*totalAvailableWidth;
            events[i].left = view.barMargin + (events[i].left*totalAvailableWidth);
        }
    },

    findStartSlot : function(events, event) {
        var priorOverlappers = this.getPriorOverlappingEvents(events, event),
            i;
            
        if (priorOverlappers.length === 0) {
            return {
                start : 0,
                end : 1
            };
        }

        for (i = 0; i < priorOverlappers.length ; i++) {
            if (i === 0 && priorOverlappers[0].left > 0) {
                return {
                    start : 0,
                    end : priorOverlappers[0].left
                };
            } else if (priorOverlappers[i].left + priorOverlappers[i].width < (i < priorOverlappers.length - 1 ? priorOverlappers[i+1].left : 1)) {
                return {
                    start : priorOverlappers[i].left + priorOverlappers[i].width,
                    end : i < priorOverlappers.length - 1 ? priorOverlappers[i+1].left : 1
                };
            }
        }

        return false;
    },

    getPriorOverlappingEvents : function(events, event) {
        var D = Sch.util.Date,
            start = event.start,
            end = event.end,
            overlappers = [];

        for (var i = 0, l = Ext.Array.indexOf(events, event); i < l ; i++) {
            if (D.intersectSpans(start, end, events[i].start, events[i].end)) {
                overlappers.push(events[i]);
            } 
        }
            
        overlappers.sort(this.sortOverlappers);

        return overlappers;
    },

    sortOverlappers : function(e1, e2) {
        return e1.left < e2.left ? -1 : 1;
    },

    getCluster : function(events, startIndex) {
        if (startIndex >= events.length-1) {
            return [events[startIndex]];
        }

        var evts    = [events[startIndex]],
            start   = events[startIndex].start,
            end     = events[startIndex].end,
            l       = events.length,
            D       = Sch.util.Date,
            i       = startIndex+1;

        while(i < l && D.intersectSpans(start, end, events[i].start, events[i].end)) {
            evts.push(events[i]);
            start = D.max(start, events[i].start);
            end = D.min(events[i].end, end);
            i++;
        }

        return evts;
    },

    sortEvents : function (a, b) {
        var sameStart = (a.start - b.start === 0);

        if (sameStart) {
            return a.end > b.end ? -1 : 1;
        } else {
            return (a.start < b.start) ? -1 : 1;
        }
    }
});
/*
@class Sch.column.Resource
@extends Ext.grid.Column
@private

A Column representing a resource, used only in vertical orientation. By default this column will use the resource
name as the header text. To get complete control over the rendering, you can use your own custom Column class by
using the {@link Sch.mixin.SchedulerPanel#resourceColumnClass resourceColumnClass} config on your SchedulerPanel.

*/
Ext.define("Sch.column.Resource", {
    extend          : "Ext.grid.Column",
    
    alias           : "widget.resourcecolumn",
    cls             : 'sch-resourcecolumn-header',

    /**
     * Default resource column properties
     */
    align           : 'center',
    menuDisabled    : true,
    hideable        : false,
    sortable        : false,
    locked          : false,
    lockable        : false,
    draggable       : false,
    enableLocking   : false,
    tdCls           : 'sch-timetd',

    // associated resource instance
    model           : null
});
/**
* @class Sch.view.model.TimeAxis
* @extends Ext.util.Observable
* @private
*
* This class is an internal view model class, describing the visual representation of a {@link Sch.data.TimeAxis timeaxis}.
* The config for the header rows is described in the {@link Sch.preset.ViewPreset#headerConfig headerConfig}.
* To calculate the widths of each cell in the time axis, this class requires:
*     * availableWidth  - The total width available for the rendering
*     * tickWidth     - The fixed width of each cell in the lowest header row. This value is normally read from the 
*                         {@link Sch.preset.ViewPreset viewPreset} but this can also be updated programmatically using {@link #setTickWidth}
*
* Normally you should not interact with this class directly.
*/
Ext.define("Sch.view.model.TimeAxis", {
    extend: 'Ext.util.Observable',

    requires: [
        'Ext.Date',
        'Sch.util.Date'
    ],

    /**
     * @cfg {Sch.data.TimeAxis} timeAxis
     * The time axis providing the underlying data to be visualized
     */
    timeAxis        : null,

    /**
     * @cfg {Int} availableWidth
     * The available width, this is normally not known by the consuming UI component using this model class until it has been fully rendered.
     * The consumer of this model should call {@link setAvailableWidth} when its width has changed.
     */
    availableWidth  : 0,

    /**
     * @cfg {Int} tickWidth
     * The "tick width" to use for the cells in the bottom most header row.
     * This value is normally read from the {@link Sch.preset.ViewPreset viewPreset}
     */
    tickWidth     : 100,

    /**
     * @cfg {Boolean} snapToIncrement
     * true if there is a requirement to be able to snap events to a certain view resolution.
     * This has implications of the {@link #tickWidth} that can be used, since all widths must be in even pixels.
     */
    snapToIncrement : false,
    
    /**
     * @cfg {Boolean} forceFit
     * true if cells in the bottom-most row should be fitted to the {@link #availableWidth available width}.
     */
    forceFit        : false,

    //used for Exporting. Make sure the tick columns are not recalculated when resizing.
    suppressFit : false,

    // Since this model may be shared by multiple synced timelinePanels, we need to keep count of usage to know when we can destroy the view model.
    refCount     : 0,

    // cache of the config currently used.
    columnConfig    : {},

    constructor: function (config) {
        var me = this;
        Ext.apply(this, config);

        me.addEvents(
            /**
            * @event update
            * Fires after the model has been updated.
            * @param {Sch.view.model.TimeAxis} model The model instance
            */
            'update'
        );
        
        if (me.timeAxis.preset) {
            // Time axis already configured, initialize using its values
            me.tickWidth = me.timeAxis.preset.timeColumnWidth;
        }

        // When time axis is changed, reconfigure the model
        me.timeAxis.on('reconfigure', me.onTimeAxisReconfigure, me);

        this.callParent(arguments);
    },

    destroy : function() {
        this.timeAxis.un('reconfigure', this.onTimeAxisReconfigure, this);
    },

    onTimeAxisReconfigure: function (timeAxis, presetChanged) {

        if (presetChanged) {
            this.setTickWidth(timeAxis.preset.timeColumnWidth);
        } else {
            this.update();
        }
    },

    /**
    *  Returns a model object of the current timeAxis, containing an array representing the cells for each level in the header. 
    *  This object will always contain a 'middle' array, and depending on the {@link Sch.preset.ViewPreset#headerConfig} it can also contain a 'top' and 'bottom' property.
    *  @return {Object} The model representing each cell (with start date and end date) in the timeline representation.
    */
    getColumnConfig : function() {
        return this.columnConfig;
    },

    /**
    *  Updates the view model current timeAxis configuration and available width.
    *  @param {Int} availableWidth The available width for the rendering of the axis (used in forceFit mode)
    */
    update: function (availableWidth, suppressEvent) {
        var timeAxis = this.timeAxis,
            headerConfig = timeAxis.headerConfig;

        this.availableWidth = Math.max(availableWidth || this.availableWidth, 0);

        if (this.forceFit && this.availableWidth <= 0) {
            // No point in continuing
            return;
        }

        this.columnConfig = {};
        
        // Generate the underlying date ranges for each header row, which will provide input to the cell rendering
        for (var pos in headerConfig) {
            if (headerConfig[pos].cellGenerator) {
                this.columnConfig[pos] = headerConfig[pos].cellGenerator.call(this, this.timeAxis.getStart(), this.timeAxis.getEnd());
            } else {
                this.columnConfig[pos] = this.createHeaderRow(pos, headerConfig[pos]);
            }
        }
        
        var lowestHeader = this.columnConfig.bottom || this.columnConfig.middle;

        // The "column width" is considered to be the width of each tick in the lowest header row and this width 
        // has to be same for all cells in the lowest row.
        this.tickWidth = this.calculateTickWidth(this.getTickWidth());
        
        if (!Ext.isNumber(this.availableWidth) || this.availableWidth < 0) {
            throw 'Invalid available width provided to Sch.view.model.TimeAxis';
        }

        if (!Ext.isNumber(this.tickWidth) || this.tickWidth <= 0) {
            throw 'Invalid column width calculated in Sch.view.model.TimeAxis';
        }

        if (!suppressEvent) {
            this.fireEvent('update', this);
        }
    },

    // private
    createHeaderRow: function (position, headerConfig) {
        var cells   = [],
            me      = this,
            colConfig,
            align   = headerConfig.align,
            today   = Ext.Date.clearTime(new Date());

        me.timeAxis.forEachInterval(position, function (start, end, i) {
            colConfig = {
                align       : align,
                start       : start,
                end         : end,
                headerCls   : ''
            };

            if (headerConfig.renderer) {
                colConfig.header = headerConfig.renderer.call(headerConfig.scope || me, start, end, colConfig, i);
            } else {
                colConfig.header = Ext.Date.format(start, headerConfig.dateFormat);
            }

            // To be able to style individual day cells, weekends or other important days
            if (headerConfig.unit === Sch.util.Date.DAY && (!headerConfig.increment || headerConfig.increment === 1)) {
                colConfig.headerCls += ' sch-dayheadercell-' + start.getDay();

                if (Ext.Date.clearTime(start, true) - today === 0) {
                    colConfig.headerCls += ' sch-dayheadercell-today';
                }
            }

            cells.push(colConfig);
        });

        return cells;
    },

    /**
    *  Returns the distance for a timespan with the given start and end date.
    *  @return {Int} The width of the time span
    */
    getDistanceBetweenDates: function (start, end) {
        var baseUnit = this.timeAxis.unit,
            width,
            measuringUnit = Sch.util.Date.getMeasuringUnit(baseUnit),
            durationInMeasuringUnit = Sch.util.Date.getDurationInUnit(start, end, measuringUnit);

        if (this.timeAxis.isContinuous()) {
            width = durationInMeasuringUnit * this.getSingleUnitInPixels(measuringUnit);
        } else {
            width = this.getPositionFromDate(end) - this.getPositionFromDate(start);
        }

        return width;
    },

    /**
     *  Gets the position of a date on the projected time axis or -1 if the date is not in the timeAxis.
     *  @param {Date} date, the date to query for.
     *  @returns {Int} the coordinate representing the date
     */
    getPositionFromDate: function (date) {
        var pos = -1,
            tick = this.timeAxis.getTickFromDate(date);

        if (tick >= 0) {
            pos = this.tickWidth * tick;
        }

        return pos;
    },

    /**
     * Gets the date for a position on the time axis
     * @param {Int} position The page X or Y coordinate
     * @param {String} roundingMethod The rounding method to use
     * @returns {Date} the Date corresponding to the xy coordinate
     */
    getDateFromPosition: function (position, roundingMethod) {
        var tick = position / this.getTickWidth(),
            nbrTicks = this.timeAxis.getCount();

        if (tick < 0 || tick > nbrTicks) {
            return null;
        }

        return this.timeAxis.getDateFromTick(tick, roundingMethod);
    },

    /**
    * Returns the amount of pixels for a single unit
    * @private
    * @return {String} The unit in pixel
    */
    getSingleUnitInPixels: function (unit) {
        return Sch.util.Date.getUnitToBaseUnitRatio(this.timeAxis.getUnit(), unit) * this.tickWidth / this.timeAxis.increment;
    },

    /**
     * [Experimental] Returns the pixel increment for the current view resolution.
     * @return {Int} The width increment
     */
    getSnapPixelAmount: function () {
        if (this.snapToIncrement) {
            var resolution = this.timeAxis.getResolution();
            return (resolution.increment || 1) * this.getSingleUnitInPixels(resolution.unit);
        } else {
            return 1;
        }
    },

    /**
    * Returns the current time column width (the width of a cell in the lowest header row)
    * @return {Int} The width
    */
    getTickWidth: function () {
        return this.tickWidth;
    },

    /**
    * Sets a new tick width (the width of a time cell in the bottom-most time axis row)
    * @param {Int} width The width
    */
    setTickWidth: function (width, suppressEvent) {
        this.tickWidth = width;

        this.update(null, suppressEvent);
    },

    /**
    * Returns the total width of the time axis representation.
    * @return {Int} The width
    */
    getTotalWidth: function () {
        return this.tickWidth * this.timeAxis.getCount();
    },

    // Calculates the time column width based on the value defined viewPreset "timeColumnWidth". It also checks for the forceFit view option
    // and the snapToIncrement, both of which impose constraints on the time column width configuration.
    calculateTickWidth: function (proposedWidth) {
        var forceFit = this.forceFit;
        
        var width = 0,
            timelineUnit = this.timeAxis.getUnit(),
            nbrTimeColumns = this.timeAxis.getCount(),
            ratio = Number.MAX_VALUE,
            measuringUnit, fittingWidth;

        if (this.snapToIncrement) {
            var res = this.timeAxis.getResolution(),
                unit = res.unit,
                resIncr = res.increment;

            ratio = Sch.util.Date.getUnitToBaseUnitRatio(timelineUnit, unit) * resIncr;
        }

        measuringUnit = Sch.util.Date.getMeasuringUnit(timelineUnit);

        ratio = Math.min(ratio, Sch.util.Date.getUnitToBaseUnitRatio(timelineUnit, measuringUnit));

        fittingWidth = Math[forceFit ? 'floor' : 'round'](this.getAvailableWidth() / this.timeAxis.getCount());

        if (!this.suppressFit){
            width = (forceFit || proposedWidth < fittingWidth) ? fittingWidth : proposedWidth;
            
            if (ratio > 0 && (!forceFit || ratio < 1)) {
                width = Math.round(Math.max(1, Math[forceFit ? 'floor' : 'round'](ratio * width)) / ratio);
            }
        } else {
            width = proposedWidth;
        }

        return width;
    },

    /**
    * Returns the available width for the time axis representation.
    * @return {Int} The available width
    */
    getAvailableWidth: function () {
        return this.availableWidth;
    },

    /**
    * Sets the available width for the model, which (if changed) will cause it to update its contents and fire the {@link #update} event.
    * @param {Int} width The width
    */
    setAvailableWidth: function (width) {
        this.availableWidth = Math.max(0, width);

        var newTickWidth = this.calculateTickWidth(this.tickWidth);

        if (newTickWidth !== this.tickWidth) {
            this.setTickWidth(newTickWidth);
        }
    },

    /**
     * This function fits the time columns into the available space in the time axis column.
     * @param {Boolean} suppressEvent `true` to skip firing the 'update' event.
     */
    fitToAvailableWidth: function (suppressEvent) {
        var proposedWidth   = Math.floor(this.availableWidth / this.timeAxis.getCount());

        this.setTickWidth(proposedWidth, suppressEvent);
    },

    /**
    * Sets the forceFit value for the model, which will cause it to update its contents and fire the {@link #update} event.
    * @param {Boolean} value 
    */
    setForceFit: function (value) {
        if (value !== this.forceFit) {
            this.forceFit = value;
            this.update();
        }
    },

    /**
    * Sets the snapToIncrement value for the model, which will cause it to update its contents and fire the {@link #update} event.
    * @param {Boolean} value 
    */
    setSnapToIncrement: function (value) {
        if (value !== this.snapToIncrement) {
            this.snapToIncrement = value;
            this.update();
        }
    }
});



/**
* @class Sch.view.HorizontalTimeAxis
* @extends Ext.util.Observable
* @private
*
* A visual representation of the time axis described in the {@link Sch.preset.ViewPreset#headerConfig headerConfig}. 
* Normally you should not interact with this class directly.
*/
Ext.define("Sch.view.HorizontalTimeAxis", {
    extend: 'Ext.util.Observable',

    requires: [
        'Ext.XTemplate'
    ],

    /**
    * @cfg {Boolean} trackHeaderOver `true` to highlight each header cell when the mouse is moved over it. 
    */
    trackHeaderOver: true,

    /**
    * @cfg {Int} compactCellWidthThreshold The minimum width for a bottom row header cell to be considered 'compact', which adds a special CSS class to the row (for special styling). 
    *            Defaults to 15px.
    */
    compactCellWidthThreshold: 15,

    baseCls : 'sch-column-header',
    tableCls : 'sch-header-row',

    // a 2nd template for the 2nd mode, w/o `containerEl`
    headerHtmlRowTpl:
        '<table border="0" cellspacing="0" cellpadding="0" style="width: {totalWidth}px; {tstyle}" class="{{tableCls}} sch-header-row-{position} {cls}">' +
            '<thead>' +
                '<tr>' +
                    '<tpl for="cells">' +
                        '<td class="{{baseCls}} {headerCls}" style="position : static; text-align: {align}; width: {width}px; {style}" tabIndex="0"' +
                            'headerPosition="{parent.position}" headerIndex="{[xindex-1]}">' +
                                '<div class="sch-simple-timeheader">{header}</div>' +
                        '</td>' +
                    '</tpl>' +
                '</tr>' +
            '</thead>' +
        '</table>',
        
    // TODO DOCS
    model           : null,

    // TODO DOCS
    hoverCls        : '',
    
    // optional
    // this view class will work in 2 modes - one with provided `containerEl` and one w/o it
    containerEl     : null,

    // Only used for IE10 in Touch Scheduler since display:box is too buggy in IE.
    height : null,

    constructor: function (config) {
        var me = this;
        var isTouch = !!Ext.versions.touch;
        var clickEventName = isTouch ? 'tap' : 'click';

        Ext.apply(this, config);
        me.callParent(arguments);

        me.model.on({
            update  : me.onModelUpdate,
            scope   : me
        });

        this.addEvents(
            /** 
            * @event refresh
            * Fires after the view has been updated, (after the time axis has been reconfigured,
            * or as a result of time column width change or available schedule width change).
            * @param {Sch.view.HorizontalTimeAxis} timeAxisView The time axis view
            */
            'refresh'
        );

        me.containerEl = Ext.get(me.containerEl);

        if (!(me.headerHtmlRowTpl instanceof Ext.Template)) {
            me.headerHtmlRowTpl = me.headerHtmlRowTpl.replace('{{baseCls}}', this.baseCls).replace('{{tableCls}}', this.tableCls);
            me.headerHtmlRowTpl = new Ext.XTemplate(me.headerHtmlRowTpl);
        }

        if (me.trackHeaderOver && me.hoverCls) {
            me.containerEl.on({
                mousemove   : me.highlightCell,
                delegate    : '.sch-column-header',
                scope       : me
            });

            me.containerEl.on({
                mouseleave  : me.clearHighlight,
                scope       : me
            });
        }

        var listenerCfg = {
            scope       : this, 
            delegate    : '.sch-column-header'
        };

        if (isTouch) {
            listenerCfg.tap = this.onElClick('tap');
            listenerCfg.doubletap = this.onElClick('doubletap');
            this.addEvents(
                'timeheadertap',
                'timeheaderdoubletap'
            );
        } else {
            listenerCfg.click = this.onElClick('click');
            listenerCfg.dblclick = this.onElClick('dblclick');

            this.addEvents(
                'timeheaderclick',
                'timeheaderdblclick'
            );
        }

        me._listenerCfg = listenerCfg;
        if (me.containerEl) me.containerEl.on(listenerCfg);
    },

    destroy : function() {
        var me = this;
        
        if (me.containerEl) { 
            me.containerEl.un(me._listenerCfg);
            
            me.containerEl.un({
                mousemove: me.highlightCell,
                delegate: '.sch-simple-timeheader',
                scope: me
            });
    
            me.containerEl.un({
                mouseleave: me.clearHighlight,
                scope: me
            });
        }

        me.model.un({
            update  : me.onModelUpdate,
            scope   : me
        });
    },

    onModelUpdate: function () {
        this.render();
    },
    
    
    getHTML : function (header, meta, column) {
        var columnConfig        = this.model.getColumnConfig();
        var totalWidth          = this.model.getTotalWidth();
        var nbrRows             = Ext.Object.getKeys(columnConfig).length;
        var rowHeight           = this.height ? this.height / nbrRows : 0;
        var html                = '';
        
        var currentCellWidth;
        
        if (columnConfig.top) {
            this.embedCellWidths(columnConfig.top);
            
            html                += this.headerHtmlRowTpl.apply({
                totalWidth      : totalWidth,
                cells           : columnConfig.top,
                position        : 'top',
                tstyle          : 'border-top : 0;' + (rowHeight ?  'height:' + rowHeight + 'px' : '')
            });
        }
        
        if (columnConfig.middle) {
            this.embedCellWidths(columnConfig.middle);

            html                += this.headerHtmlRowTpl.apply({
                totalWidth      : totalWidth,
                cells           : columnConfig.middle,
                position        : 'middle',
                tstyle          : (columnConfig.top ? '' : 'border-top : 0;') + (rowHeight ?  'height:' + rowHeight + 'px' : ''),
                cls             : !columnConfig.bottom && this.model.getTickWidth() <= this.compactCellWidthThreshold ? 'sch-header-row-compact' : ''
            });
        }
        
        if (columnConfig.bottom) {
            this.embedCellWidths(columnConfig.bottom);
            
            html                += this.headerHtmlRowTpl.apply({
                totalWidth      : totalWidth,
                cells           : columnConfig.bottom,
                position        : 'bottom',
                tstyle          : (rowHeight ? 'height:' + rowHeight + 'px' : ''),
                cls             : this.model.getTickWidth() <= this.compactCellWidthThreshold ? 'sch-header-row-compact' : ''
            });
        }

        return html + '<div class="sch-header-secondary-canvas"></div>';
    },
    

    // Outputs the tables and cells based on the header row config in the active viewPreset
    render: function () {
        if (!this.containerEl) return;
        
        var innerCt     = this.containerEl,
            ctDom       = innerCt.dom,
            oldDisplay  = ctDom.style.display,
            columnConfig = this.model.getColumnConfig(),
            parent      = ctDom.parentNode;

        ctDom.style.display = 'none';
        parent.removeChild(ctDom);

        var renderData = this.getHTML();

        ctDom.innerHTML = renderData;

        if (!columnConfig.top && !columnConfig.middle) {
            this.containerEl.addCls('sch-header-single-row');
        } else {
            this.containerEl.removeCls('sch-header-single-row');
        }

        parent && parent.appendChild(ctDom);
        ctDom.style.display = oldDisplay;
        
        this.fireEvent('refresh', this);
    },

    embedCellWidths : function (cells) {
        // For desktop only, flags such as Ext.isSafari only exist in Ext JS (in touch it's set in Ext.os)
        var widthAdjust     = (Ext.isIE7 || Ext.isSafari) ? 1 : 0;

        for (var i = 0; i < cells.length; i++) {
            var cell        = cells[ i ];
            var width       = this.model.getDistanceBetweenDates(cell.start, cell.end);
            
            if (width) {
                cell.width  = width - (i ? widthAdjust : 0);
            } else {
                cell.width  = 0;
                cell.style  = 'display: none';
            }
        }
    },
    

    // private
    onElClick: function(eventName) {
        return function (event, target) { 
            // Normalize ST vs Ext JS (Ext passes the delegated target as the target argument, ST passes the clicked DOM node)
            target = event.delegatedTarget || target;
            
            var position        = Ext.fly(target).getAttribute('headerPosition'),
                index           = Ext.fly(target).getAttribute('headerIndex'),
                headerConfig    = this.model.getColumnConfig()[position][index];
        
            this.fireEvent('timeheader' + eventName, this, headerConfig.start, headerConfig.end, event);
        };
    },


    highlightCell: function (e, cell) {
        var me = this;

        if (cell !== me.highlightedCell) {
            me.clearHighlight();
            me.highlightedCell = cell;
            Ext.fly(cell).addCls(me.hoverCls);
        }
    },

    clearHighlight: function () {
        var me = this,
            highlighted = me.highlightedCell;

        if (highlighted) {
            Ext.fly(highlighted).removeCls(me.hoverCls);
            delete me.highlightedCell;
        }
    }
    /* EOF Proxied model methods */
});



/*
 * @class Sch.column.timeAxis.Horizontal
 * @extends Ext.grid.column.Column
 * @private
 *
 * A simple grid column providing a visual representation of the time axis. This class does not produce any real Ext JS grid columns, instead it just renders a Sch.view.HorizontalTimeAxis inside its element.
 * This class can represent up to three different axes, that are defined in the view preset config object. 
 */
Ext.define("Sch.column.timeAxis.Horizontal", {
    extend          : 'Ext.grid.column.Column',
    alias           : 'widget.timeaxiscolumn',
    
    draggable       : false,
    groupable       : false,
    hideable        : false,
    sortable        : false,
    fixed           : true,
    menuDisabled    : true,
    cls             : 'sch-simple-timeaxis',
    tdCls           : 'sch-timetd',
    enableLocking   : false,

    requires        : [
        'Sch.view.HorizontalTimeAxis'
    ],


    timeAxisViewModel           : null,
    headerView      : null,
    hoverCls : 'sch-column-header-over',

    /**
     * @cfg {Boolean} trackHeaderOver `true` to highlight each header cell when the mouse is moved over it. 
     */
    trackHeaderOver         : true,

    /**
     * @cfg {Int} compactCellWidthThreshold The minimum width for a bottom row header cell to be considered 'compact', which adds a special CSS class     *            to the row. 
     *            Defaults to 15px.
     */
    compactCellWidthThreshold : 20,

    initComponent : function() {
        // Keep this and use for our own cell hover functionality.
        this.ownHoverCls = this.hoverCls;

        // We do not want the default hover cls to be added to container column TD.
        this.hoverCls = '';

        this.callParent(arguments);
    },


    afterRender : function() {
        var me = this;

        me.headerView = new Sch.view.HorizontalTimeAxis({
            model                       : me.timeAxisViewModel,
            containerEl                 : me.titleEl,
            hoverCls                    : me.ownHoverCls,
            trackHeaderOver             : me.trackHeaderOver,
            compactCellWidthThreshold   : me.compactCellWidthThreshold
        });

        me.headerView.on('refresh', me.onTimeAxisViewRefresh, me);
        
        me.ownerCt.on('afterlayout', function() {
            // If the container of this column changes size, we need to re-evaluate the size for the
            // time axis view
            me.mon(me.ownerCt, "resize", me.onHeaderContainerResize, me );

            if (this.getWidth() > 0) {
                me.timeAxisViewModel.update(me.getAvailableWidthForSchedule());
                me.setWidth(me.timeAxisViewModel.getTotalWidth());
            }
        }, null, { single : true });

        this.enableBubble('timeheaderclick', 'timeheaderdblclick');

        me.relayEvents(me.headerView, [
            'timeheaderclick',
            'timeheaderdblclick'
        ]);
        
        me.callParent(arguments);
    },

    initRenderData: function() {
        var me = this;
        me.renderData.headerCls = me.renderData.headerCls || me.headerCls;
        return me.callParent(arguments);
    },

    destroy : function() {
        if (this.headerView) {
            this.headerView.destroy();
        }
        this.callParent(arguments);
    },

    onTimeAxisViewRefresh : function() {
        // Make sure we don't create an infinite loop
        this.headerView.un('refresh', this.onTimeAxisViewRefresh, this);

        this.setWidth(this.timeAxisViewModel.getTotalWidth());

        this.headerView.on('refresh', this.onTimeAxisViewRefresh, this);
    },

    getAvailableWidthForSchedule : function() {
        var available   = this.ownerCt.getWidth();
        var items       = this.ownerCt.items;
        
        // substracting the widths of all columns starting from 2nd ("right" columns)
        for (var i = 1; i < items.length; i++) {
            available -= items.get(i).getWidth();
        }
            
        return available - Ext.getScrollbarSize().width - 1;
    },

    onResize: function () {
        this.callParent(arguments);
        this.timeAxisViewModel.setAvailableWidth(this.getAvailableWidthForSchedule());
    },

    onHeaderContainerResize: function () {
        this.timeAxisViewModel.setAvailableWidth(this.getAvailableWidthForSchedule());
        this.headerView.render();
    }
});



/*
 * @class Sch.column.timeAxis.Vertical
 * @private
 * @extends Ext.grid.column.Column
 * A Column representing the time span in vertical orientation
 * @constructor
 * @param {Object} config The configuration options
 */
Ext.define('Sch.column.timeAxis.Vertical', {

    extend : 'Ext.grid.column.Column',

    alias : 'widget.verticaltimeaxis',


    /**
     * Default timeaxis column properties
     */
    align : 'right',

    draggable             : false,
    groupable             : false,
    hideable              : false,
    sortable              : false,
    menuDisabled          : true,
    timeAxis              : null,
    timeAxisViewModel     : null,
    cellTopBorderWidth    : null,
    cellBottomBorderWidth : null,
    totalBorderWidth      : null,
    enableLocking         : false,
    locked                : true,

    initComponent : function () {
        this.callParent(arguments);
        this.tdCls = (this.tdCls || '') + ' sch-verticaltimeaxis-cell';
        this.scope = this;

        this.totalBorderWidth = this.cellTopBorderWidth + this.cellBottomBorderWidth;
    },

    // HACK, until we have a proper time axis view for vertical too (not relying on Ext column)
    afterRender   : function () {
        this.callParent(arguments);
        var panel = this.up('panel');
        panel.getView().on('resize', this.onContainerResize, this);
    },

    onContainerResize : function (cmp, width, height) {
        // Grab the full height of the view, minus the spacer el height and an extra buffer
        this.timeAxisViewModel.update(height - 21);
    },

    renderer : function (val, meta, record, rowIndex) {
        var hConfig = this.timeAxis.headerConfig,
            hc = hConfig.bottom || hConfig.middle;

        meta.style = 'height:' + (this.timeAxisViewModel.getTickWidth() - this.totalBorderWidth) + 'px';

        if (hc.renderer) {
            return hc.renderer.call(hc.scope || this, record.data.start, record.data.end, meta, rowIndex);
        } else {
            return Ext.Date.format(record.data.start, hc.dateFormat);
        }
    }
});


/**
 * @class Sch.mixin.Lockable
 * @extends Ext.grid.locking.Lockable.
 * @private
 * This is a private class for internal use.
 */
Ext.define('Sch.mixin.Lockable', {
    extend                      : 'Ext.grid.locking.Lockable',
    
    useSpacer                   : true,

    syncRowHeight               : false,
    
    horizontalScrollForced      : false,

    // overridden
    // @OVERRIDE
    injectLockable: function () {

        var me          = this;
        var isTree      = Ext.data.TreeStore && me.store instanceof Ext.data.TreeStore;

        var eventSelModel = me.getEventSelectionModel ? me.getEventSelectionModel() : me.getSelectionModel();

        // Make local copies of these configs in case someone puts them on the prototype of a subclass.
        me.lockedGridConfig = Ext.apply({}, me.lockedGridConfig || {});
        me.normalGridConfig = Ext.apply({}, me.schedulerConfig || me.normalGridConfig || {});

        if (me.lockedXType) {
            me.lockedGridConfig.xtype = me.lockedXType;
        }

        if (me.normalXType) {
            me.normalGridConfig.xtype = me.normalXType;
        }

        var lockedGrid = me.lockedGridConfig,
            normalGrid = me.normalGridConfig;

        // Configure the child grids
        Ext.applyIf(me.lockedGridConfig, {
            useArrows           : true,
            trackMouseOver      : false,
            split               : true,
            animCollapse        : false,
            collapseDirection   : 'left',
            region              : 'west'
        });

        Ext.applyIf(me.normalGridConfig, {
            viewType            : me.viewType,

            layout              : 'fit',

            sortableColumns     : false,
            enableColumnMove    : false,
            enableColumnResize  : false,
            enableColumnHide    : false,

            /* @COMPAT 2.2 */
            getSchedulingView   : function() {
                var con = typeof console !== "undefined" ? console : false;

                if (con && con.log) con.log('getSchedulingView is deprecated on the inner grid panel. Instead use getView on the "normal" subgrid.');

                return this.getView();
            },

            selModel            : eventSelModel,

            collapseDirection   : 'right',
            animCollapse        : false,
            region              : 'center'
        });

        if (me.orientation === 'vertical') {
            lockedGrid.store = normalGrid.store = me.timeAxis;
        }

        if (lockedGrid.width) {
            // User has specified a fixed width for the locked section, disable the syncLockedWidth method
            me.syncLockedWidth = Ext.emptyFn;
            // Enable scrollbars for locked section
            lockedGrid.scroll = 'horizontal';
            lockedGrid.scrollerOwner = true;
        }

        var lockedViewConfig    = me.lockedViewConfig = me.lockedViewConfig || {};
        var normalViewConfig    = me.normalViewConfig = me.normalViewConfig || {};

        if (isTree) {
            // HACK, speeding up by preventing an edit to cause a massive relayout
            var oldOnUpdate = Ext.tree.View.prototype.onUpdate;
            lockedViewConfig.onUpdate = function() {
                // truncated version of original "refreshSize" which does not do any layouts, but which however still
                // should update the reference of the "body" element
                this.refreshSize = function () {
                    var me = this,
                        bodySelector = me.getBodySelector();

                    // On every update of the layout system due to data update, capture the view's main element in our private flyweight.
                    // IF there *is* a main element. Some TplFactories emit naked rows.
                    if (bodySelector) {
                        me.body.attach(me.el.child(bodySelector, true));
                    }
                };
                Ext.suspendLayouts();
                oldOnUpdate.apply(this, arguments);
                Ext.resumeLayouts();
                this.refreshSize = Ext.tree.View.prototype.refreshSize;
            };

            // need to use the NodeStore instance, created in the FilterableTreeStore mixin for both views
            lockedViewConfig.store  = normalViewConfig.store = me.store.nodeStore;
        }

        var origLayout = me.layout;
        var lockedWidth = lockedGrid.width;

        this.callParent(arguments);

        // HACK, no sane way of getting rid of these it seems (as of 4.2.1).
        // Grouping view overwrites showMenuBy property
        // http://www.sencha.com/forum/showthread.php?269612-Config-to-get-rid-of-Lock-Unlock-column-options&p=987653#post987653
        this.on('afterrender', function() {
            var showMenuBy = this.lockedGrid.headerCt.showMenuBy;

            this.lockedGrid.headerCt.showMenuBy = function() {
                showMenuBy.apply(this, arguments);

                me.showMenuBy.apply(this, arguments);
            };
        });

        // At this point, the 2 child grids are created

        var lockedView = me.lockedGrid.getView();
        var normalView = me.normalGrid.getView();

        this.patchViews();

        // Now post processing, changing and overriding some things that Ext.grid.Lockable sets up
        if (lockedWidth || origLayout === 'border') {
            if (lockedWidth) {
                me.lockedGrid.setWidth(lockedWidth);
            }

            // Force horizontal scrollbar to be shown to keep spacerEl magic working when scrolling to bottom
            normalView.addCls('sch-timeline-horizontal-scroll');
            lockedView.addCls('sch-locked-horizontal-scroll');
            
            me.horizontalScrollForced   = true;
        }

        if (me.normalGrid.collapsed) {
            // Need to workaround this, child grids cannot be collapsed initially
            me.normalGrid.collapsed = false;

            // Note, for the case of buffered view/store we need to wait for the view box to be ready before collapsing
            // since the paging scrollbar reads the view height during setup. When collapsing too soon, its viewSize will be 0.
            normalView.on('boxready', function(){
                me.normalGrid.collapse();
            }, me, { delay : 10 });
        }

        if (me.lockedGrid.collapsed) {
            if (lockedView.bufferedRenderer) lockedView.bufferedRenderer.disabled = true;
        }

        // Without this fix, scrolling on Mac Chrome does not work in locked grid
        if (Ext.getScrollbarSize().width === 0) {
            // https://www.assembla.com/spaces/bryntum/support/tickets/252
            lockedView.addCls('sch-ganttpanel-force-locked-scroll');
        }

        if (isTree) {
            this.setupLockableTree();
        }

        if (me.useSpacer) {
            normalView.on('refresh', me.updateSpacer, me);
            lockedView.on('refresh', me.updateSpacer, me);
        }

        if (origLayout !== 'fit') {
            me.layout = origLayout;
        }

        // Sanity check to make sure grids were setup correctly
        if (lockedView.store !== normalView.store) {
            Ext.Error.raise('Sch.mixin.Lockable setup failed, not sharing store between the two views');
        }

        // @OVERRIDE using some private methods to sync the top scroll position for a locked grid which is initially collapsed
        if (normalView.bufferedRenderer) {
            // Need to sync vertical position after child gridpanel expand
            this.lockedGrid.on('expand', function() {
                lockedView.el.dom.scrollTop     = normalView.el.dom.scrollTop;
            });

            this.patchSubGrid(this.lockedGrid, true);
            this.patchSubGrid(this.normalGrid, false);

            this.patchBufferedRenderingPlugin(normalView.bufferedRenderer);
            this.patchBufferedRenderingPlugin(lockedView.bufferedRenderer);
        }


        // Patch syncHorizontalScroll to solve header scroll issue
        this.patchSyncHorizontalScroll(this.lockedGrid);
        this.patchSyncHorizontalScroll(this.normalGrid);
        
        this.delayReordererPlugin(this.lockedGrid);
        this.delayReordererPlugin(this.normalGrid);
    },


    setupLockableTree: function () {
        var me              = this;
        var lockedView      = me.lockedGrid.getView();

        // enable filtering support for trees
        var filterableProto = Sch.mixin.FilterableTreeView.prototype;

        lockedView.initTreeFiltering        = filterableProto.initTreeFiltering;
        lockedView.onFilterChangeStart      = filterableProto.onFilterChangeStart;
        lockedView.onFilterChangeEnd        = filterableProto.onFilterChangeEnd;
        lockedView.onFilterCleared          = filterableProto.onFilterCleared;
        lockedView.onFilterSet              = filterableProto.onFilterSet;

        lockedView.initTreeFiltering();
    },

    
    patchSyncHorizontalScroll : function(grid) {
        // Override scrollTask
        grid.scrollTask = new Ext.util.DelayedTask(function (left, setBody) {
            // Patched method, always get scroll left position from dom, not from args
            // http://www.sencha.com/forum/showthread.php?273464-Grid-panel-header-scrolls-incorrectly-after-column-resizing
            // test: header/318_header_scroll_bug.t.js
            var target = this.getScrollTarget().el;

            if (target) this.syncHorizontalScroll(target.dom.scrollLeft, setBody);
        }, grid);
    },
    
    
    // the columns re-orderer plugin is being initialized synchronously, after rendering the header container
    // but before layouts
    // its initializing involves creation of drag/drop zones which performs "verifyEl" call on the headerCt element
    // which, in turn, tries to access "el.offsetParent" - that slows down rendering for no reason.
    // the initilization of the column reoderer can be delayed.
    // for 700 tasks / 300 dependencies project this optimization brings rendering time down from 3s to 2.5s in Chrome
    // (for other browsers speed up is less significant)
    delayReordererPlugin : function (grid) {
        var headerCt                = grid.headerCt;
        var reorderer               = headerCt.reorderer;
        
        if (reorderer) {
            headerCt.un('render', reorderer.onHeaderCtRender, reorderer);
            headerCt.on('render', function () {
                if (!headerCt.isDestroyed) reorderer.onHeaderCtRender(); 
            }, reorderer, { single : true, delay : 10 });
        }
    },

    
    updateSpacer : function() {

        var lockedView = this.lockedGrid.getView();
        var normalView = this.normalGrid.getView();

        if (lockedView.rendered && normalView.rendered && lockedView.el.child('table')) {
            var me   = this,
            // This affects scrolling all the way to the bottom of a locked grid
            // additional test, sort a column and make sure it synchronizes
                lockedViewEl   = lockedView.el,
                normalViewEl = normalView.el.dom,
                spacerId = lockedViewEl.dom.id + '-spacer',
                spacerHeight = (normalViewEl.offsetHeight - normalViewEl.clientHeight) + 'px';

            me.spacerEl = Ext.getDom(spacerId);

            // HACK ie 6-7 and 8 in quirks mode fail to set style of hidden elements, so we must remove it manually
            if (Ext.isIE6 || Ext.isIE7 || (Ext.isIEQuirks && Ext.isIE8) && me.spacerEl) {

                Ext.removeNode(me.spacerEl);
                me.spacerEl = null;
            }

            if (me.spacerEl) {
                me.spacerEl.style.height = spacerHeight;
            } else {
                // put the spacer inside of stretcher with special css class (see below), which will cause the
                // stretcher to increase its height on the height of spacer
                var spacerParent = lockedViewEl;

                Ext.core.DomHelper.append(spacerParent, {
                    id      : spacerId,
//                    cls     : this.store.buffered ? 'sch-locked-buffered-spacer' : '',
                    style   : 'height: ' + spacerHeight
                });
            }
        }
    },


    // TODO remove after dropping support for 4.2.0?
    onLockedViewScroll: function() {
        this.callParent(arguments);

        var lockedBufferedRenderer  = this.lockedGrid.getView().bufferedRenderer;

        if (lockedBufferedRenderer) lockedBufferedRenderer.onViewScroll();
    },

    // TODO remove after dropping support for 4.2.0?
    onNormalViewScroll: function() {
        this.callParent(arguments);

        var normalBufferedRenderer  = this.normalGrid.getView().bufferedRenderer;

        if (normalBufferedRenderer) normalBufferedRenderer.onViewScroll();
    },


    // we need to disabled the plugin when grid is collapsed
    patchSubGrid : function (grid, isLocked) {
        var view                    = grid.getView();
        var bufferedRenderer        = view.bufferedRenderer;

        grid.on({
            collapse        : function () { bufferedRenderer.disabled = true; },
            expand          : function () { bufferedRenderer.disabled = false; }
        });
        
        // bug in ExtJS: http://www.sencha.com/forum/showthread.php?276800-4.2.2-Buffered-rendering-plugin-issues&p=1013797#post1013797
        // the `tableStyle` misses "px" at the end
        var prevCollectData         = view.collectData;
        
        view.collectData            = function () {
            var result              = prevCollectData.apply(this, arguments);
            var tableStyle          = result.tableStyle;
            
            // checking if `tableStyle` ends with "px" (trying to do it fast)
            if (tableStyle && tableStyle[ tableStyle.length - 1 ] != 'x') result.tableStyle += 'px';
            
            return result;
        };
        // eof bug
        
        // onRemove patch
        // in case of tree, normal and locked views have different types - locked view is a tree view and normal view is a regular
        // grid view. This causes the buffered rendererers, attached to the views,  behave differently, because of 
        // Ext.grid.plugin.BufferedRendererTreeView override.
        // When some node is collapsed for example, the locked view is fully refreshed (not sure why the full refresh is needed)
        // after refresh the "onViewRefresh" method of the buffered renderer is called for the locked view
        // and it updates the stretcher size among other things
        // but, this method is not called for normal grid, because its a regular grid view, w/o Ext.grid.plugin.BufferedRendererTreeView
        // so we call that method manually
        var isTree                  = Ext.data.TreeStore && this.store instanceof Ext.data.TreeStore;
        
        if (isLocked && isTree) {
            var prevOnRemove        = view.onRemove;
            
            view.onRemove           = function () {
                prevOnRemove.apply(this, arguments);
                
                this.lockingPartner.bufferedRenderer.onViewRefresh();
            };
            
            // we will need re-bind the store, that will happen at the bottom of the method
        }
        // eof onRemove patch
        
        // The buffered renderer plugin includes 2 overrides for grid view: Ext.grid.plugin.BufferedRendererTableView and 
        // Ext.grid.plugin.BufferedRendererTreeView.
        // Seems the buffered renderer behavior is completely broken when doing CRUD with the store (nodes are inserted in
        // random places in the view), that is why Ext.grid.plugin.BufferedRendererTreeView modestly does a full refresh 
        // for a "onRemove" handler. We will do the same for "onAdd" and for regular table as well.
        var prevOnAdd               = view.onAdd;
        
        view.onAdd                  = function () {
            var me = this;
            // Using buffered rendering - removal (eg folder node collapse)
            // Has to refresh the view
            if (me.rendered && me.bufferedRenderer) {
                me.refreshView();
            }
            // No BufferedRenderer preent
            else {
                prevOnAdd.apply(this, arguments);
            }
        };
        
        // re-bind the store, so that new versions of `onRemove` and `onAdd` methods will be bound instead of default ones
        view.bindStore(null);
        view.bindStore(isTree ? this.store.nodeStore : this.resourceStore);
    },
    
    
    afterLockedViewLayout : function () {
        // do nothing if horizontal scrolling has been forced
        // this method performs some bottom border with adjustment
        // which we don't need in case of forced scrolling
        if (!this.horizontalScrollForced) return this.callParent(arguments);
    },


    patchBufferedRenderingPlugin : function (plugin) {
        plugin.variableRowHeight    = true;

        if (Ext.getVersion('extjs').isLessThan('4.2.1.883')) {
            // TODO find more robust way to unsubscribe from "scroll" event of the view
            plugin.view.on('afterrender', function () {
                plugin.view.el.un('scroll', plugin.onViewScroll, plugin);
            }, this, { single : true, delay : 1 });

            var prevStretchView         = plugin.stretchView;

            plugin.stretchView          = function (view, scrollRange) {
                var me              = this,
                    recordCount     = (me.store.buffered ? me.store.getTotalCount() : me.store.getCount());

                if (recordCount && (me.view.all.endIndex === recordCount - 1)) {
                    scrollRange     = me.bodyTop + view.body.dom.offsetHeight;
                }

                prevStretchView.apply(this, [ view, scrollRange ]);
            };
        } else {
            var prevEnable          = plugin.enable;

            plugin.enable           = function () {
                if (plugin.grid.collapsed) return;

                return prevEnable.apply(this, arguments);
            };
        }
    },

    // HACK, no sane way of getting rid of these it seems (as of 4.2.1).
    // http://www.sencha.com/forum/showthread.php?269612-Config-to-get-rid-of-Lock-Unlock-column-options&p=987653#post987653
    showMenuBy: function(t, header) {
        var menu = this.getMenu(),
            unlockItem  = menu.down('#unlockItem'),
            lockItem = menu.down('#lockItem'),
            sep = unlockItem.prev();

        sep.hide();
        unlockItem.hide();
        lockItem.hide();
    },

    // Various ugly overrides to avoid locked grid causing crazy scrolling in IE.
    // REMOVE FOR EXT 5, UNTIL THEN - ENJOY
    patchViews : function() {
        if (Ext.isIE) {
            var selModel    = this.getSelectionModel();
            var me          = this;
            var lockedView  = me.lockedGrid.view;
            var normalView  = me.normalGrid.view;

            //@OVERRIDE to fix https://www.assembla.com/spaces/bryntum/tickets/661
            // https://www.assembla.com/spaces/bryntum/tickets/1095
            var old         = selModel.processSelection;
            
            var eventName   = Ext.getVersion('extjs').isLessThan('4.2.2.1144') ? 'mousedown' : 'click';
            var focusMethod = lockedView.doFocus ? 'doFocus' : 'focus';
            
            selModel.processSelection = function (view, record, item, index, e) {
                var oldScrollRowIntoView, oldFocus;

                if (e.type == eventName) {
                    oldScrollRowIntoView            = lockedView.scrollRowIntoView;
                    oldFocus                        = lockedView[ focusMethod ];

                    lockedView.scrollRowIntoView    = normalView.scrollRowIntoView = Ext.emptyFn;
                    lockedView[ focusMethod ]       = normalView[ focusMethod ] = Ext.emptyFn;
                }

                old.apply(this, arguments);

                if (e.type == eventName) {
                    lockedView.scrollRowIntoView    = normalView.scrollRowIntoView = oldScrollRowIntoView;
                    lockedView[ focusMethod ]       = normalView[ focusMethod ] = oldFocus;
                    
                    lockedView.el.focus();
                }
            };

            //@OVERRIDE to fix https://www.assembla.com/spaces/bryntum/tickets/661
            var oldRF = normalView.onRowFocus;

            normalView.onRowFocus = function (rowIdx, highlight, suppressFocus) {
                oldRF.call(this, rowIdx, highlight, true);
            };
            
//            var oldNormalFocusRow   = normalView.focusRow;
//            normalView.focusRow = function (row, delay) { return oldNormalFocusRow.call(this, row, 0) };
//
//            var oldLockedFocusRow   = lockedView.focusRow;
//            lockedView.focusRow = function (row, delay) { return oldLockedFocusRow.call(this, row, 0) };
            
            if (Ext.tree && Ext.tree.plugin && Ext.tree.plugin.TreeViewDragDrop) {

                lockedView.on('afterrender', function() {
                    Ext.each(lockedView.plugins, function(plug) {

                        if (plug instanceof Ext.tree.plugin.TreeViewDragDrop) {

                            var old = lockedView[ focusMethod ];

                            plug.dragZone.view.un('itemmousedown', plug.dragZone.onItemMouseDown, plug.dragZone);

                            plug.dragZone.view.on('itemmousedown', function() {

                                lockedView[ focusMethod ] = Ext.emptyFn;

                                if (lockedView.editingPlugin) {
                                    lockedView.editingPlugin.completeEdit();
                                }
                                plug.dragZone.onItemMouseDown.apply(plug.dragZone, arguments);

                                lockedView[ focusMethod ] = old;
                            });

                            return false;
                        }
                    });

                }, null, { delay : 100 });
            }
        }
    }
});

/**

@class Sch.model.Customizable
@extends Ext.data.Model

This class represent a model with customizable field names. Customizable fields are defined in separate 
class config `customizableFields`. The format of definition is just the same as for usual fields:

    Ext.define('BaseModel', {
        extend      : 'Sch.model.Customizable',
        
        customizableFields  : [
            { name      : 'StartDate',  type    : 'date', dateFormat : 'c' },
            { name      : 'EndDate',    type    : 'date', dateFormat : 'c' }
        ],
        
        fields              : [
            'UsualField'
        ],
        
        getEndDate : function () {
            return "foo"
        }
    });

For each customizable field will be created getter and setter, using the camel-cased name of the field ("stable name"), 
prepended with "get/set" respectively. They will not overwrite any existing methods:

    var baseModel       = new BaseModel({
        StartDate   : new Date(2012, 1, 1),
        EndDate     : new Date(2012, 2, 3)
    });
    
    // using getter for "StartDate" field
    // returns date for "2012/02/01"
    var startDate   = baseModel.getStartDate();
    
    // using custom getter for "EndDate" field
    // returns "foo"
    var endDate     = baseModel.getEndDate();
    
You can change the name of the customizable fields in the subclasses of the model or completely re-define them. 
For that, add a special property to the class, name of this property should be formed as name of the field with lowercased first
letter, appended with "Field". The value of the property should contain the new name of the field.

    Ext.define('SubModel', {
        extend      : 'BaseModel',
        
        startDateField      : 'beginDate',
        endDateField        : 'finalizeDate',
        
        fields              : [
            { name      : 'beginDate',  type    : 'date', dateFormat : 'Y-m-d' },
        ]
    });
    
    var subModel       = new SubModel({
        beginDate       : new Date(2012, 1, 1),
        finalizeDate    : new Date(2012, 2, 3)
    });
    
    // name of getter is still the same
    var startDate   = subModel.getStartDate();

In the example above the `StartDate` field was completely re-defined to the `beginDate` field with different date format.
The `EndDate` has just changed its name to "finalizeDate". Note, that getters and setters are always named after "stable"
field name, not the customized one.

*/

// Don't redefine the class, which will screw up instanceof checks etc
if (!Ext.ClassManager.get("Sch.model.Customizable")) {

    Ext.define('Sch.model.Customizable', {
        extend      : 'Ext.data.Model',

        // Defined in subclasses, this prevents the default 'id' from being added to the fields collection
        idProperty  : null,

        /**
         * @cfg {Array} customizableFields
         *
         * The array of customizale fields definitions.
         */
        customizableFields      : null,

        // @private
        // Keeps temporary state of the previous state for a model, but is only available
        // when a model has changed, e.g. after 'set' or 'reject'. After those operations are completed, this property is cleared.
        previous                : null,

        onClassExtended : function (cls, data, hooks) {
            var onBeforeCreated = hooks.onBeforeCreated;

            hooks.onBeforeCreated = function (cls, data) {
                onBeforeCreated.call(this, cls, data);

                var proto                   = cls.prototype;

                if (!proto.customizableFields) {
                    return;
                }

                // combining our customizable fields with ones from superclass
                // our fields goes after fields from superclass to overwrite them if some names match
                proto.customizableFields    = (cls.superclass.customizableFields || []).concat(proto.customizableFields);

                var customizableFields      = proto.customizableFields;

                // collect fields here, overwriting old ones with new
                var customizableFieldsByName    = {};

                Ext.Array.each(customizableFields, function (field) {
                    // normalize to object
                    if (typeof field == 'string') field = { name : field };

                    customizableFieldsByName[ field.name ] = field;
                });

                // already processed by the Ext.data.Model `onBeforeCreated`
                var fields                  = proto.fields;

                var toRemove                = [];

                fields.each(function (field) {
                    if (field.isCustomizableField) toRemove.push(field);
                });

                fields.removeAll(toRemove);

                Ext.Object.each(customizableFieldsByName, function (name, customizableField) {
                    // mark all customizable fields with special property, to be able remove them later
                    customizableField.isCustomizableField     = true;

                    var stableFieldName     = customizableField.name || customizableField.getName();

                    var fieldProperty       = stableFieldName === 'Id' ? 'idProperty' : stableFieldName.charAt(0).toLowerCase() + stableFieldName.substr(1) + 'Field';
                    var overrideFieldName   = proto[ fieldProperty ];

                    var realFieldName       = overrideFieldName || stableFieldName;

                    if (fields.containsKey(realFieldName)) {
                        // if user has re-defined some customizable field, mark it accordingly
                        // such fields weren't be inheritable though (won't replace the customizable field)
                        fields.getByKey(realFieldName).isCustomizableField = true;

                        // add it to our customizable fields list on the last position, so in the subclasses
                        // it will overwrite other fields with this name
                        customizableFields.push(
                            new Ext.data.Field(
                                Ext.applyIf({ name : stableFieldName, isCustomizableField : true }, fields.getByKey(realFieldName))
                            )
                        );
                    } else
                        // we create a new copy of the `customizableField` using possibly new name
                        fields.add(new Ext.data.Field(Ext.applyIf({ name : realFieldName, isCustomizableField : true }, customizableField)));

                    var capitalizedStableName  = Ext.String.capitalize(stableFieldName);

                    // don't overwrite `getId` method
                    if (capitalizedStableName != 'Id') {
                        var getter              = 'get' + capitalizedStableName;
                        var setter              = 'set' + capitalizedStableName;

                        // overwrite old getters, pointing to a different field name
                        if (!proto[ getter ] || proto[ getter ].__getterFor__ && proto[ getter ].__getterFor__ != realFieldName) {
                            proto[ getter ] = function () {
                                return this.data[ realFieldName ];
                            };

                            proto[ getter ].__getterFor__   = realFieldName;
                        }

                        // same for setters
                        if (!proto[ setter ] || proto[ setter ].__setterFor__ && proto[ setter ].__setterFor__ != realFieldName) {
                            proto[ setter ] = function (value) {
                                return this.set(realFieldName, value);
                            };

                            proto[ setter ].__setterFor__   = realFieldName;
                        }
                    }
                });
            };
        },

        // Overridden to be able to track previous record field values
        set : function(fieldName, value) {
            var currentValue;
            this.previous = this.previous || {};

            if (arguments.length > 1) {
                currentValue = this.get(fieldName);

                // Store previous field value
                if (currentValue !== value) {
                    this.previous[fieldName] = currentValue;
                }
            } else {
                for (var o in fieldName) {
                    currentValue = this.get(o);

                    // Store previous field value
                    if (currentValue !== fieldName[o]) {
                        this.previous[o] = currentValue;
                    }
                }
            }
            this.callParent(arguments);
        },

        // @OVERRIDE
        // Overridden to be able to clear the previous record field values. Must be done here to have access to the 'previous' object after
        // an endEdit call.
        afterEdit : function() {
            this.callParent(arguments);

            // Reset the previous tracking object
            delete this.previous;
        },

        // Overridden to be able to track previous record field values
        reject : function () {
            var me = this,
                modified = me.modified,
                field;

            me.previous = me.previous || {};
            for (field in modified) {
                if (modified.hasOwnProperty(field)) {
                    if (typeof modified[field] != "function") {
                        me.previous[field] = me.get(field);
                    }
                }
            }
            me.callParent(arguments);

            // Reset the previous tracking object
            delete me.previous;
        }
    });
}

/**

 @class Sch.model.Range
 @extends Sch.model.Customizable

 This class represent a simple date range. It is being used in various subclasses and plugins which operate on date ranges.

 Its a subclass of the {@link Sch.model.Customizable}, which is in turn subclass of {@link Ext.data.Model}.
 Please refer to documentation of those classes to become familar with the base interface of this class.

 A range has the following fields:

 - `StartDate`   - start date of the task in the ISO 8601 format
 - `EndDate`     - end date of the task in the ISO 8601 format (not inclusive)
 - `Name`        - an optional name of the range
 - `Cls`         - an optional CSS class to be associated with the range.

 The name of any field can be customized in the subclass. Please refer to {@link Sch.model.Customizable} for details.

 */
Ext.define('Sch.model.Range', {
    extend      : 'Sch.model.Customizable',

    requires    : [
        'Sch.util.Date'
    ],

    idProperty  : 'Id',

    // For Sencha Touch
    config : {
        idProperty  : 'Id'
    },

    /**
     * @cfg {String} startDateField The name of the field that defines the range start date. Defaults to "StartDate".
     */
    startDateField  : 'StartDate',

    /**
     * @cfg {String} endDateField The name of the field that defines the range end date. Defaults to "EndDate".
     */
    endDateField    : 'EndDate',

    /**
     * @cfg {String} nameField The name of the field that defines the range name. Defaults to "Name".
     */
    nameField       : 'Name',

    /**
     * @cfg {String} clsField The name of the field that holds the range "class" value (usually corresponds to a CSS class). Defaults to "Cls".
     */
    clsField        : 'Cls',

    customizableFields : [
        /**
         * @method getStartDate
         *
         * Returns the range start date
         *
         * @return {Date} The start date
         */
        { name      : 'StartDate',  type    : 'date', dateFormat : 'c' },

        /**
         * @method getEndDate
         *
         * Returns the range end date
         *
         * @return {Date} The end date
         */
        { name      : 'EndDate',    type    : 'date', dateFormat : 'c' },

        /**
         * @method getCls
         *
         * Gets the "class" of the range
         *
         * @return {String} cls The "class" of the range
         */
        /**
         * @method setCls
         *
         * Sets the "class" of the range
         *
         * @param {String} cls The new class of the range
         */
        {
            name            : 'Cls', type    : 'string'
        },

        /**
         * @method getName
         *
         * Gets the name of the range
         *
         * @return {String} name The "name" of the range
         */
        /**
         * @method setName
         *
         * Sets the "name" of the range
         *
         * @param {String} name The new name of the range
         */
        {
            name            : 'Name', type    : 'string'
        }
    ],

    /**
     * @method setStartDate
     *
     * Sets the range start date
     *
     * @param {Date} date The new start date
     * @param {Boolean} keepDuration Pass `true` to keep the duration of the task ("move" the event), `false` to change the duration ("resize" the event).
     * Defaults to `false`
     */
    setStartDate : function(date, keepDuration) {
        var endDate = this.getEndDate();
        var oldStart = this.getStartDate();

        this.set(this.startDateField, date);

        if (keepDuration === true && endDate && oldStart) {
            this.setEndDate(Sch.util.Date.add(date, Sch.util.Date.MILLI, endDate - oldStart));
        }
    },

    /**
     * @method setEndDate
     *
     * Sets the range end date
     *
     * @param {Date} date The new end date
     * @param {Boolean} keepDuration Pass `true` to keep the duration of the task ("move" the event), `false` to change the duration ("resize" the event).
     * Defaults to `false`
     */
    setEndDate : function(date, keepDuration) {
        var startDate = this.getStartDate();
        var oldEnd = this.getEndDate();

        this.set(this.endDateField, date);

        if (keepDuration === true && startDate && oldEnd) {
            this.setStartDate(Sch.util.Date.add(date, Sch.util.Date.MILLI, -(oldEnd - startDate)));
        }
    },

    /**
     * Sets the event start and end dates
     *
     * @param {Date} start The new start date
     * @param {Date} end The new end date
     */
    setStartEndDate : function(start, end) {
        // wrap with "begineEdit/endEdit" unless already wrapped from outside code
        var needToWrap  = !this.editing; 
        
        needToWrap && this.beginEdit();
        this.set(this.startDateField, start);
        this.set(this.endDateField, end);
        needToWrap && this.endEdit();
    },

    /**
     * Returns an array of dates in this range. If the range starts/ends not at the beginning of day, the whole day will be included.
     * @return {Array[Date]}
     */
    getDates : function () {
        var dates   = [],
            endDate = this.getEndDate();

        for (var date = Ext.Date.clearTime(this.getStartDate(), true); date < endDate; date = Sch.util.Date.add(date, Sch.util.Date.DAY, 1)) {

            dates.push(date);
        }

        return dates;
    },


    /**
     * Iterates over the results from {@link #getDates}
     * @param {Function} func The function to call for each date
     * @param {Object} scope The scope to use for the function call
     */
    forEachDate : function (func, scope) {
        return Ext.each(this.getDates(), func, scope);
    },

    // Simple check if end date is greater than start date
    isValid : function() {
        var valid = this.callParent(arguments);

        if (valid) {
            var start = this.getStartDate(),
                end = this.getEndDate();

            valid = !start || !end || (end - start >= 0);
        }

        return valid;
    },

    /**
     * Shift the dates for the date range by the passed amount and unit
     * @param {String} unit The unit to shift by (e.g. range.shift(Sch.util.Date.DAY, 2); ) to bump the range 2 days forward
     * @param {Int} amount The amount to shift
     */
    shift : function(unit, amount) {
        this.setStartEndDate(
            Sch.util.Date.add(this.getStartDate(), unit, amount),
            Sch.util.Date.add(this.getEndDate(), unit, amount)
        );
    }
});
/*
 * @class Sch.model.TimeAxisTick
 * @extends Sch.model.Range
 *
 * A simple model with a start/end date interval defining a 'tick' on the time axis.
 */
Ext.define('Sch.model.TimeAxisTick', {
    extend          : 'Sch.model.Range',

    startDateField  : 'start',
    endDateField    : 'end'
});

/**

@class Sch.model.Event
@extends Sch.model.Range

This class represent a single event in your schedule. Its a subclass of the {@link Sch.model.Range}, which is in turn subclass of {@link Sch.model.Customizable} and {@link Ext.data.Model}.
Please refer to documentation of those classes to become familar with the base interface of the task.

The Event model has a few predefined fields as seen below. If you want to add new fields or change the options for the existing fields,
you can do that by subclassing this class (see example below). 

Fields
------

- `Id`          - (mandatory) unique identificator of task
- `Name`        - name of the event (task title)
- `StartDate`   - start date of the task in the ISO 8601 format
- `EndDate`     - end date of the task in the ISO 8601 format,
- `ResourceId`  - The id of the associated resource
- `Resizable`   - A field allowing you to easily control how an event can be resized. You can set it to: true, false, 'start' or 'end' as its value.
- `Draggable`   - A field allowing you to easily control if an event can be dragged. (true or false)
- `Cls`         - A field containing a CSS class to be added to the rendered event element.

Subclassing the Event model class
--------------------

    Ext.define('MyProject.model.Event', {
        extend      : 'Sch.model.Event',
                
        fields      : [
            // adding new field
            { name: 'MyField', type : 'number', defaultValue : 0 }
        ],
                
        myCheckMethod : function () {
            return this.get('MyField') > 0 
        },
        ...
    });

If you want to use other names for the StartDate, EndDate, ResourceId and Name fields you can configure them as seen below:

    Ext.define('MyProject.model.Event', {
        extend      : 'Sch.model.Event',
                
        startDateField  : 'taskStart',
        endDateField    : 'taskEnd',
                
        // just rename the fields
        resourceIdField : 'userId',
        nameField       : 'taskTitle',
        
        fields      : [
            // completely change the definition of fields
            { name: 'taskStart', type: 'date', dateFormat : 'Y-m-d' },
            { name: 'taskEnd', type: 'date', dateFormat : 'Y-m-d' },
        ]
        ...
    });
    
Please refer to {@link Sch.model.Customizable} for additional details.
    
*/
Ext.define('Sch.model.Event', {
    extend: 'Sch.model.Range',

    idProperty: 'Id',
    config : { idProperty: 'Id' },

    customizableFields: [
        { name: 'Id' },
        { name: 'ResourceId' },
        { name: 'Draggable', type: 'boolean', persist: false, defaultValue : true },   // true or false
        { name: 'Resizable', persist: false, defaultValue : true }                     // true, false, 'start' or 'end'
    ],

    /**
    * @cfg {String} resourceIdField The name of the field identifying the resource to which an event belongs. Defaults to "ResourceId".
    */
    resourceIdField: 'ResourceId',

    /**
    * @cfg {String} draggableField The name of the field specifying if the event should be draggable in the timeline
    */
    draggableField: 'Draggable',

    /**
    * @cfg {String} resizableField The name of the field specifying if/how the event should be resizable.
    */
    resizableField: 'Resizable',

    /**
    * Returns either the resource associated with this event (when called w/o `resourceId`) or resource with specified id. 
    * 
    * @param {String} resourceId (optional)
    * @return {Sch.model.Resource} 
    */
    getResource: function (resourceId) {
        if (this.stores && this.stores.length > 0) {
            var rs = this.stores[0].resourceStore;
            resourceId = resourceId || this.get(this.resourceIdField);

            if (Ext.data.TreeStore && rs instanceof Ext.data.TreeStore) {
                return rs.getNodeById(resourceId) || rs.getRootNode().findChildBy(function (r) { return r.internalId === resourceId; });
            } else {
                return rs.getById(resourceId) || rs.data.map[resourceId];
            }
        }

        return null;
    },

    /**
    * Sets the resource which the event should belong to.
    * 
    * @param {Sch.model.Resource/Mixed} resource The new resource
    */
    setResource: function (resourceOrId) {
        this.set(this.resourceIdField, (resourceOrId instanceof Ext.data.Model) ? resourceOrId.getId() || resourceOrId.internalId : resourceOrId);
    },

    /**
    * Assigns this event to the specified resource, alias for 'setResource'
    * 
    * @param {Sch.model.Resource/Mixed} resource The new resource for this event
    */
    assign: function (resourceOrId) {
        this.setResource.apply(this, arguments);
    },

    /**
    * Unassigns this event from the specified resource
    * 
    * @param {Sch.model.Resource/Mixed} resource The new resource for this event
    */
    unassign: function (resourceOrId) {
        // TODO
        //this.setResourceId(null);
    },

    /**
    * @method isDraggable
    * 
    * Returns true if event can be drag and dropped
    * @return {Mixed} The draggable state for the event.
    */
    isDraggable: function () {
        return this.getDraggable();
    },

    /**
     * @method isAssignedTo
     * Returns true if this event is assigned to a certain resource.
     *
     * @param {Sch.model.Resource} resource The resource to query for
     * @return {Boolean}
     */
    isAssignedTo: function (resource) {
        return this.getResource() === resource;
    },

    /**
    * @method setDraggable
    * 
    * Sets the new draggable state for the event
    * @param {Boolean} draggable true if this event should be draggable
    */

    /**
    * @method isResizable
    * 
    * Returns true if event can be resized, but can additionally return 'start' or 'end' indicating how this event can be resized. 
    * @return {Mixed} The resource Id
    */
    isResizable: function () {
        return this.getResizable();
    },

    /**
    * @method setResizable
    * 
    * Sets the new resizable state for the event. You can specify true/false, or 'start'/'end' to only allow resizing one end of an event.
    * @param {Boolean} resizable true if this event should be resizable
    */

    /**
    * @method getResourceId
    * 
    * Returns the resource id of the resource that the event belongs to.
    * @return {Mixed} The resource Id
    */

    /**
    * @method setResourceId
    * 
    * Sets the new resource id of the resource that the event belongs to.
    * @param {Mixed} The resource Id
    */

    /**
    * Returns false if a linked resource is a phantom record, i.e. it's not persisted in the database.
    * @return {Boolean} valid
    */
    isPersistable: function () {
        var resources = this.getResources();
        var persistable = true;

        Ext.each(resources, function(r) { 
            if (r.phantom) {
                persistable = false;
                return false;
            }
        }); 

        return persistable;
    },

    forEachResource: function (fn, scope) {
        var rs = this.getResources();
        for (var i = 0; i < rs.length; i++) {
            if (fn.call(scope || this, rs[i]) === false) {
                return;
            }
        }
    },

    getResources : function() {
        var resource    = this.getResource();

        return resource ? [ resource ] : [];
    }
});
/**

 @class Sch.model.Resource
 @extends Sch.model.Customizable

 This class represent a single Resource in the scheduler chart. Its a subclass of the {@link Sch.model.Customizable}, which is in turn subclass of {@link Ext.data.Model}.
 Please refer to documentation of those classes to become familar with the base interface of the resource.

 A Resource has only 2 mandatory fields - `Id` and `Name`. If you want to add more fields with meta data describing your resources then you should subclass this class:

 Ext.define('MyProject.model.Resource', {
        extend      : 'Sch.model.Resource',
        
        fields      : [
            // `Id` and `Name` fields are already provided by the superclass
            { name: 'Company',          type : 'string' }
        ],
        
        getCompany : function () {
            return this.get('Company');
        },
        ...
    });

 If you want to use other names for the Id and Name fields you can configure them as seen below:

 Ext.define('MyProject.model.Resource', {
        extend      : 'Sch.model.Resource',
        
        nameField   : 'UserName',
        ...
    });

 Please refer to {@link Sch.model.Customizable} for details.

 */

// Don't redefine the class, which will screw up instanceof checks etc
if (!Ext.ClassManager.get("Sch.model.Resource")) {

    Ext.define('Sch.model.Resource', {
        extend : 'Sch.model.Customizable',

        idProperty : 'Id',
        config     : { idProperty : 'Id' },

        /**
         * @cfg {String} nameField The name of the field that holds the resource name. Defaults to "Name".
         */
        nameField : 'Name',

        customizableFields : [
            'Id',

        /**
         * @method getName
         *
         * Returns the resource name
         *
         * @return {String} The name of the resource
         */
        /**
         * @method setName
         *
         * Sets the resource name
         *
         * @param {String} The new name of the resource
         */
            { name : 'Name', type : 'string' }
        ],

        getEventStore : function () {
            return this.stores[0] && this.stores[0].eventStore || this.parentNode && this.parentNode.getEventStore();
        },


        /**
         * Returns an array of events, associated with this resource
         * @param {Sch.data.EventStore} eventStore (optional) The event store to get events for (if a resource is bound to multiple stores)
         * @return {Array[Sch.model.Event]}
         */
        getEvents : function (eventStore) {
            var events = [],
                ev,
                id = this.getId() || this.internalId;

            eventStore = eventStore || this.getEventStore();

            for (var i = 0, l = eventStore.getCount(); i < l; i++) {
                ev = eventStore.getAt(i);
                if (ev.data[ev.resourceIdField] === id) {
                    events.push(ev);
                }
            }

            return events;
        }
    });
}
/**
@class Sch.data.mixin.EventStore

This is a mixin, containing functionality related to managing events. 

It is consumed by the regular {@link Sch.data.EventStore} class and {@link Gnt.data.TaskStore} class 
to allow data sharing between gantt chart and scheduler. Please note though, that datasharing is still
an experimental feature and not all methods of this mixin can be used yet on a TaskStore. 

*/
Ext.define("Sch.data.mixin.EventStore", {
    model : 'Sch.model.Event',
    config : { model : 'Sch.model.Event' },

    requires : [
        'Sch.util.Date'
    ],

    isEventStore : true,

    /**
     * Sets the resource store for this store
     * 
     * @param {Sch.data.ResourceStore} resourceStore
     */
    setResourceStore : function (resourceStore) {
        if (this.resourceStore) {
            this.resourceStore.un({
                beforesync  : this.onResourceStoreBeforeSync,
                write       : this.onResourceStoreWrite,
                scope       : this
            });
        }
        
        this.resourceStore    = resourceStore;
        
        if (resourceStore) {
            resourceStore.on({
                beforesync : this.onResourceStoreBeforeSync,
                write      : this.onResourceStoreWrite,
                scope       : this
            });
        }
    },

    onResourceStoreBeforeSync: function (records, options) {
        var recordsToCreate     = records.create;
        
        if (recordsToCreate) {
            for (var r, i = recordsToCreate.length - 1; i >= 0; i--) {
                r = recordsToCreate[i];
                
                // Save the phantom id to be able to replace the task phantom task id's in the dependency store
                r._phantomId = r.internalId;
            }
        }
    },

    /* 
     * This method will update events that belong to a phantom resource, to make sure they get the 'real' resource id
     */
    onResourceStoreWrite: function (store, operation) {
        if (operation.wasSuccessful()) {
            var me = this,
                rs = operation.getRecords();

            Ext.each(rs, function(resource) {
                if (resource._phantomId && !resource.phantom) {
                    me.each(function (event) {
                        if (event.getResourceId() === resource._phantomId) {
                            event.assign(resource);
                        }
                    });
                }
            });
        }
    },

    /**
    * Checks if a date range is allocated or not for a given resource.
    * @param {Date} start The start date
    * @param {Date} end The end date
    * @param {Sch.model.Event} excludeEvent An event to exclude from the check (or null)
    * @param {Sch.model.Resource} resource The resource
    * @return {Boolean} True if the timespan is available for the resource
    */
    isDateRangeAvailable: function (start, end, excludeEvent, resource) {
        var available = true,
            DATE = Sch.util.Date;

        this.forEachScheduledEvent(function (ev, startDate, endDate) {
            if (DATE.intersectSpans(start, end, startDate, endDate) &&
                resource === ev.getResource() && 
                (!excludeEvent || excludeEvent !== ev)) {
                available = false;
                return false;
            }
        });

        return available;
    },

    /**
    * Returns events between the supplied start and end date
    * @param {Date} start The start date
    * @param {Date} end The end date
    * @param {Boolean} allowPartial false to only include events that start and end inside of the span
    * @return {Ext.util.MixedCollection} the events
    */
    getEventsInTimeSpan: function (start, end, allowPartial) {

        if (allowPartial !== false) {
            var DATE = Sch.util.Date;

            return this.queryBy(function (event) {
                var eventStart = event.getStartDate(),
                    eventEnd = event.getEndDate();

                return eventStart && eventEnd && DATE.intersectSpans(eventStart, eventEnd, start, end);
            });
        } else {
            return this.queryBy(function (event) {
                var eventStart = event.getStartDate(),
                    eventEnd = event.getEndDate();

                return eventStart && eventEnd && (eventStart - start >= 0) && (end - eventEnd >= 0);
            });
        }
    },

    /**
     * Calls the supplied iterator function once for every scheduled event, providing these arguments
     *      - event : the event record
     *      - startDate : the event start date
     *      - endDate : the event end date
     *
     * Returning false cancels the iteration.
     *
     * @param {Function} the iterator function
     * @param {Object} the 'this' object to use for the function
     */
    forEachScheduledEvent : function (fn, scope) {

        this.each(function (event) {
            var eventStart = event.getStartDate(),
                eventEnd = event.getEndDate();

            if (eventStart && eventEnd) {
                return fn.call(scope || this, event, eventStart, eventEnd);
            }
        }, this);
    },

    /**
     * Returns an object defining the earliest start date and the latest end date of all the events in the store.
     * 
     * @return {Object} An object with 'start' and 'end' Date properties (or null values if data is missing).
     */
    getTotalTimeSpan : function() {
        var earliest = new Date(9999,0,1), 
            latest = new Date(0), 
            D = Sch.util.Date;
        
        this.each(function(r) {
            if (r.getStartDate()) {
                earliest = D.min(r.getStartDate(), earliest);
            }
            if (r.getEndDate()) {
                latest = D.max(r.getEndDate(), latest);
            }
        });

        earliest = earliest < new Date(9999,0,1) ? earliest : null;
        latest = latest > new Date(0) ? latest : null;

        return {
            start : earliest || null,
            end : latest || earliest || null
        };
    },

    /**
    * Returns the events associated with a resource
    * @param {Sch.model.Resource} resource
    * @return {Sch.model.Event[]} the events
    */
    getEventsForResource: function (resource) {
        var events = [], 
            ev,   
            id = resource.getId() || resource.internalId;
            
        for (var i = 0, l = this.getCount(); i < l; i++) {
            ev = this.getAt(i);
            if (ev.data[ev.resourceIdField] == id) {
                events.push(ev);
            }
        }

        return events;
    },

    // This method provides a way for the store to append a new record, and the consuming class has to implement it
    // since Store and TreeStore don't share the add API.
    append : function(record) {
        throw 'Must be implemented by consuming class';
    },

    // Sencha Touch <-> Ext JS normalization
    getModel : function() {
        return this.model;
    }
});
/**
@class Sch.data.EventStore

This is a class holding all the {@link Sch.model.Event events} to be rendered into a {@link Sch.SchedulerPanel scheduler panel}.

*/
Ext.define("Sch.data.EventStore", {
    extend : 'Ext.data.Store',

    model : 'Sch.model.Event',
    config : { model : 'Sch.model.Event' },

    mixins : [
        'Sch.data.mixin.EventStore'
    ],

    constructor : function() {
        this.callParent(arguments);

        var newC = function () {};
        newC.prototype = Ext.ModelManager.getModel(this.getModel()).prototype;

        if (!(new newC() instanceof Sch.model.Event)) {
            throw 'The model for the EventStore must subclass Sch.model.Event';
        }
    },

    getByInternalId : function(id) {
        return this.data.getByKey(id);
    },

    /**
     * Appends a new record to the store
     * @param {Sch.model.Event} record The record to append to the store
     */
    append : function(record) {
        this.add(record);
    }
});
/**
 * @class Sch.data.mixin.ResourceStore
 * This is a mixin for the ResourceStore functionality. It is consumed by the {@link Sch.data.ResourceStore} class ("usual" store) and {@link Sch.data.ResourceTreeStore} - tree store.
 * 
 */
Ext.define("Sch.data.mixin.ResourceStore", {

    // Sencha Touch <-> Ext JS normalization
    getModel : function() {
        return this.model;
    }
});
Ext.define("Sch.data.FilterableNodeStore", {
    extend          : 'Ext.data.NodeStore',
    
    // @OVERRIDE
    onNodeExpand : function (parent, records, suppressEvent) {
        var visibleRecords      = [];
        
        for (var i = 0; i < records.length; i++) {
            var record      = records[ i ];
            
            if (!(record.isHidden && record.isHidden() || record.hidden || record.data.hidden)) visibleRecords[ visibleRecords.length ] = record;
        }
        
        return this.callParent([ parent, visibleRecords, suppressEvent ]);
    },
    
    
    onNodeCollapse: function (parent, records, suppressEvent, callback, scope) {
        var data                = this.data;
        var prevContains        = data.contains;
        
        data.contains           = function () {
            for (var i = 0; i < records.length; i++) 
                if (!records[ i ].hidden && prevContains.call(this, records[ i ])) return true;
            
            return false;
        };
        
        this.callParent(arguments);
        
        data.contains           = prevContains;
    }
});
/**
@class Sch.data.mixin.FilterableTreeStore

This is a mixin for the Ext.data.TreeStore providing filtering functionality. Please note, that Ext JS does not support filtering of tree stores,
and the functionality of this mixin is not related to the standard Ext JS store filtering (which utilizes Ext.util.Filter etc). This implementation should however be flexible
enough to cover all common uses cases.

The functionality of this class can be divided into two sections:

Filtering
=========

Filtering of a tree store is different from filtering flat stores. In a flat store, all nodes (items) are of the same type and on the same hieararchy level.
Filtering can hide any of nodes that does not match some criteria.

On the other hand, in tree stores some of the nodes represent parent nodes with child nodes ("parent", "folder", "group" etc) and other nodes are "leaves".
And usually a "leaf" node can't be sufficiently identified w/o its parents - i.e. it is important to know the parents a particular leaf node belongs to. So when filtering
tree stores, we need to show all parent nodes of the filtered nodes.

Moreover, filtering is usually being used for searching and thus should ignore the "expanded/collapsed" state of tree nodes (we need to search among all nodes,
including collapsed ones). Because of that and the underlying Ext JS implementation **expanding/collapsing/adding/removing nodes when the tree store is filtered is not supported.** This limitation
could be removed in future releases.

Filtering can be activated with the {@link #filterTreeBy} method and cleared with {@link #clearTreeFilter}.

Hiding/Showing nodes
====================

Sometimes we want to keep some nodes in the tree, but remove them from the visual presentation and hide them. This can be done with {@link #hideNodesBy} method and {@link #showAllNodes} can be
used to restore the previous state. When a node is hidden all its child nodes are hidden too.

"Hidden" nodes will never appear in filtered results - consider them removed from the tree store completely. They will, however, appear in a data package for a `store.sync()` operation
(you can override the the "filterUpdated" method to exclude them from there if needed).

Note, that it is possible to filter the store with hidden nodes, but not the other way around (hide some nodes of the filtered store).

*/
Ext.define("Sch.data.mixin.FilterableTreeStore", {
    
    requires : [
        'Sch.data.FilterableNodeStore'
    ],
    
    
    nodeStoreClassName      : 'Sch.data.FilterableNodeStore',
    
    nodeStore               : null,
    
    isFilteredFlag          : false,

    // ref to the last filter applied
    lastTreeFilter          : null,
    
    /**
     * Should be called in the constructor of the consuming class, to activate the filtering functionality.
     */
    initTreeFiltering : function () {
        if (!this.nodeStore) this.nodeStore = this.createNodeStore(this);
        
        this.addEvents(
            'filter-set',
            'filter-clear',
            'nodestore-datachange-start',
            'nodestore-datachange-end'
        );
    },
    
    
    createNodeStore : function (treeStore) {
        return Ext.create(this.nodeStoreClassName, {
            treeStore       : treeStore,
            recursive       : true,
            rootVisible     : this.rootVisible
        });
    },
    
    
    /**
     * Clears current filter (if any).
     * 
     * See also {@link Sch.data.mixin.FilterableTreeStore} for additional information.
     */
    clearTreeFilter : function () {
        if (!this.isTreeFiltered()) return;
        
        this.refreshNodeStoreContent();
        
        this.isFilteredFlag     = false;
        this.lastTreeFilter     = null;
        this.fireEvent('filter-clear', this);
    },
    
    
    refreshNodeStoreContent : function (skipUIRefresh) {
        var root            = this.getRootNode(),
            linearNodes     = [];
            
        var rootVisible     = this.rootVisible;
        
        var collectNodes    = function (node) {
            if (node.isHidden && node.isHidden() || node.hidden || node.data.hidden) return;
            
            if (rootVisible || node != root) linearNodes[ linearNodes.length ] = node;
            
            if (!node.data.leaf && node.isExpanded()) {
                var childNodes  = node.childNodes,
                    length      = childNodes.length;
                
                for (var k = 0; k < length; k++) collectNodes(childNodes[ k ]);
            }
        };
        
        collectNodes(root);
        
        this.fireEvent('nodestore-datachange-start', this);
        
        var nodeStore       = this.nodeStore;
        
        // "loadDataInNodeStore" is a special hook for buffered case
        // in buffered case, instead of "loadRecords" we need to use "cachePage"
        if (!this.loadDataInNodeStore || !this.loadDataInNodeStore(linearNodes)) nodeStore.loadRecords(linearNodes);
        
        // HACK - forcing view to refresh, the usual "refresh" event is blocked by the tree view (see `blockRefresh` property)
        if (!skipUIRefresh) nodeStore.fireEvent('clear', nodeStore);
        
        this.fireEvent('nodestore-datachange-end', this);
    },
    
    
    getIndexInTotalDataset : function (record) {
        var root            = this.getRootNode(),
            index           = -1;
            
        var rootVisible     = this.rootVisible;
        
        if (!rootVisible && record == root) return -1;
        
        var collectNodes    = function (node) {
            if (node.isHidden && node.isHidden() || node.hidden || node.data.hidden)
                // stop scanning if record we are looking for is hidden
                if (node == record) return false;
            
            if (rootVisible || node != root) index++;
                
            // stop scanning if we found the record
            if (node == record) return false;
            
            if (!node.data.leaf && node.isExpanded()) {
                var childNodes  = node.childNodes,
                    length      = childNodes.length;
                
                for (var k = 0; k < length; k++) 
                    if (collectNodes(childNodes[ k ]) === false) return false;
            }
        };
        
        collectNodes(root);
        
        return index;
    },
    
    
    
    /**
     * Returns the boolean, indicating whether this store is currently filtered
     * 
     * @return {Boolean}
     */
    isTreeFiltered : function () {
        return this.isFilteredFlag;
    },    
    
    
    /**
     * This method filters the tree store. It accepts an object with the following properties:
     * 
     * - `filter` - a function to check if the node should be included in the result. It will be called for each **leaf** node in tree and will receive the current node as the first argument.
     * It should return `true` if the node should remain visible, `false` otherwise. The result will also contain all parents nodes of all matching leafs. Results will not include
     * parent nodes, which do not have at least one matching child.
     * To call this method for parent nodes too, pass an additional parameter - `checkParents` (see below).
     * - `scope` - a scope to call the filter with (optional)
     * - `checkParents` - when set to `true` will also call the `filter` function for each parent node. If the function returns `false` for some parent node,
     * it still could be included in filter results if some of its children matches the `filter` (see also "shallow" option below). If the function returns `true` for a parent node, it will be
     * included in the filtering results even if it does not have any matching child nodes. 
     * - `shallow` - implies `checkParents`. When set to `true`, it will stop checking child nodes if the `filter` function return `false` for a parent node. The whole sub-tree, starting
     * from a non-matching parent, will be excluded from the result in such case.
     * - `onlyParents` - alternative for `checkParents`. When set to `true` it will only call the provided `filter` function for parent tasks. If
     * the filter returns `true`, the parent and all its direct child leaf nodes will be included in the results. If the `filter` returns `false`, a parent node still can
     * be included in the results (w/o direct children leafs), if some of its child nodes matches the filter.
     * - `fullMatchingParents` - implies `onlyParents`. In this mode, if a parent node matches the filter, then not only its direct children
     * will be included in the results, but the whole sub-tree, starting from the matching node.
     * 
     * Repeated calls to this method will clear previous filters.
     * 
     * This function can be also called with 2 arguments, which should be the `filter` function and `scope` in such case.
     * 
     * For example:

    treeStore.filterTreeBy({
        filter          : function (node) { return node.get('name').match(/some regexp/) },
        checkParents    : true
    })
    
    // or, if you don't need to set any options:
    treeStore.filterTreeBy(function (node) { return node.get('name').match(/some regexp/) })

     * 
     * See also {@link Sch.data.mixin.FilterableTreeStore} for additional information.
     * 
     * @param {Object} params
     */
    filterTreeBy : function (params, scope) {
        var filter;
        
        if (arguments.length == 1 && Ext.isObject(arguments[ 0 ])) {
            scope       = params.scope;
            filter      = params.filter;
        } else {
            filter      = params;
            params      = { filter : filter };
        }
        
        this.fireEvent('nodestore-datachange-start', this);
        
        params                      = params || {};
        
        var shallowScan             = params.shallow;
        var checkParents            = params.checkParents || shallowScan;
        var fullMathchingParents    = params.fullMathchingParents;
        var onlyParents             = params.onlyParents || fullMathchingParents;
        var rootVisible             = this.rootVisible;
        
        if (onlyParents && checkParents) throw new Error("Can't combine `onlyParents` and `checkParents` options");
        
        var keepTheseParents    = {};
        
        var root                = this.getRootNode(),
            linearNodes         = [];
        
        var includeParentNodesInResults = function (node) {
            var parent  = node.parentNode;
            
            while (parent && !keepTheseParents[ parent.internalId ]) {
                keepTheseParents[ parent.internalId ] = true;
                
                parent = parent.parentNode;
            }
        };
        
        var collectNodes    = function (node) {
            if (node.isHidden && node.isHidden() || node.hidden || node.data.hidden) return;
            
            var nodeMatches, childNodes, length, k;
            
            // `collectNodes` should not be called for leafs at all
            if (node.data.leaf) {
                if (filter.call(scope, node, keepTheseParents)) {
                    linearNodes[ linearNodes.length ] = node;
                    
                    includeParentNodesInResults(node);
                }
            } else {
                // include _all_ parent nodes in intermediate result set originally, except the root one
                // intermediate result set will be filtered
                if (rootVisible || node != root) linearNodes[ linearNodes.length ] = node;
                
                if (onlyParents) {
                    nodeMatches     = filter.call(scope, node);

                    childNodes      = node.childNodes;
                    length          = childNodes.length;
                        
                    if (nodeMatches) {
                        keepTheseParents[ node.internalId ] = true;
                        
                        includeParentNodesInResults(node);
                        
                        if (fullMathchingParents) {
                            node.cascadeBy(function (currentNode) {
                                if (currentNode != node) {
                                    linearNodes[ linearNodes.length ] = currentNode;
                                    
                                    if (!currentNode.data.leaf) keepTheseParents[ currentNode.internalId ] = true;
                                }
                            });
                            
                            return;
                        }
                    }
                    
                    // at this point nodeMatches and fullMathchingParents can't be both true
                    for (k = 0; k < length; k++)
                        if (nodeMatches && childNodes[ k ].data.leaf) 
                            linearNodes[ linearNodes.length ] = childNodes[ k ];
                        else if (!childNodes[ k ].data.leaf)
                            collectNodes(childNodes[ k ]);
                        
                } else {
                    // mark matching nodes to be kept in results
                    if (checkParents) {
                        nodeMatches = filter.call(scope, node, keepTheseParents);
                        
                        if (nodeMatches) {
                            keepTheseParents[ node.internalId ] = true;
                            
                            includeParentNodesInResults(node);
                        }
                    }
                    
                    // recurse if
                    // - we don't check parents
                    // - shallow scan is not enabled
                    // - shallow scan is enabled and parent node matches the filter or it does not, but its and invisible root, so we don't care
                    if (!checkParents || !shallowScan || shallowScan && (nodeMatches || node == root && !rootVisible)) {
                        childNodes      = node.childNodes;
                        length          = childNodes.length;
                        
                        for (k = 0; k < length; k++) collectNodes(childNodes[ k ]);
                    }
                }
            }
        };
        
        collectNodes(root);
        
        // additional filtering of the result set
        // removes the parent nodes which do not match filter themselves and have no macthing children  
        var nodesToKeep = [];
            
        for (var i = 0, len = linearNodes.length; i < len; i++) {
            var node    = linearNodes[ i ];
            
            if (node.data.leaf || keepTheseParents[ node.internalId ]) nodesToKeep[ nodesToKeep.length ] = node;
        }
        
        var nodeStore   = this.nodeStore;
        
        // "loadDataInNodeStore" is a special hook for buffered case
        // in buffered case, instead of "loadRecords" we need to use "cachePage"
        if (!this.loadDataInNodeStore || !this.loadDataInNodeStore(nodesToKeep)) { 
            nodeStore.loadRecords(nodesToKeep, false);
        
            // HACK - forcing view to refresh, the usual "refresh" event is blocked by the tree view (see `blockRefresh` property)
            nodeStore.fireEvent('clear', nodeStore);
        }
        
        this.isFilteredFlag     = true;
        this.lastTreeFilter     = params;
        this.fireEvent('nodestore-datachange-end', this);
        
        this.fireEvent('filter-set', this);
    },
    
    
    /**
     * Hide nodes from the tree store visual presentation (they still remain in the store).
     * 
     * See also {@link Sch.data.mixin.FilterableTreeStore} for additional information.
     * 
     * @param {Function} filter - A filtering function. Will be called for each node in the tree store and receive the current node as the 1st argument. Should return `true` to **hide** the node
     * and `false`, to **keep it visible**.
     * @param {Object} scope (optional).
     */
    hideNodesBy : function (filter, scope) {
        if (this.isFiltered()) throw new Error("Can't hide nodes of the filtered tree store");
        
        var me      = this;
        scope       = scope || this;
        
        this.getRootNode().cascadeBy(function (node) {
            node.hidden = filter.call(scope, node, me);
        });
        
        this.refreshNodeStoreContent();
    },
    
    
    /**
     * Shows all nodes, previously hidden with {@link #hideNodesBy}
     * 
     * See also {@link Sch.data.mixin.FilterableTreeStore} for additional information.
     */
    showAllNodes : function () {
        this.getRootNode().cascadeBy(function (node) {
            node.hidden = node.data.hidden = false;
        });
        
        this.refreshNodeStoreContent();
    },
    
    
    inheritables : function () {
        return {
            // the NodeStore does not fire a neither "clear" event when root.removeAll() method is called
            // nor "refresh" event when new nodes are loaded
            // meanwhile, the buffered renderer plugin relies on that event (at least one of them)
            // to clear some internal state, so we fire that event manually
            // the "clear" event seems to be safer to fire, because "refresh" may cause an extra view refresh
            // ("clear" will do too, but the view will be empty)
            load : function() {
                var root        = this.getRootNode();
                
                if (root) {
                    var nodeStore       = this.nodeStore;
                    var prevRemoveAll   = root.removeAll;
                    
                    root.removeAll  = function () {
                        prevRemoveAll.apply(this, arguments);
                        
                        nodeStore && nodeStore.fireEvent('clear', nodeStore);
                        
                        delete root.removeAll;
                    };
                }
        
                this.callParent(arguments);
                
                if (root) delete root.removeAll;
            }
            // eof load
        };
        // eof object returned from inheritables
    }
    // eof inheritables
});
/**
@class Sch.data.ResourceStore
 
This is a class holding the collection the {@link Sch.model.Resource resources} to be rendered into a {@link Sch.panel.SchedulerGrid scheduler panel}. 
Its a subclass of "Ext.data.Store" - a store with linear data presentation.

*/
Ext.define("Sch.data.ResourceStore", {
    extend  : 'Ext.data.Store',
    model   : 'Sch.model.Resource',
    config : { model : 'Sch.model.Resource' },
    
    mixins  : [
        'Sch.data.mixin.ResourceStore'
    ],

    constructor : function() {
        this.callParent(arguments);

        var newC = function () {};
        newC.prototype = Ext.ModelManager.getModel(this.getModel()).prototype;

        if (!(new newC() instanceof Sch.model.Resource)) {
            throw 'The model for the ResourceStore must subclass Sch.model.Resource';
        }
    }
});
/**
 * @class Sch.data.ResourceTreeStore
 * @mixin Sch.data.mixin.FilterableTreeStore
 * 
 * This is a class holding all the resources to be rendered into a {@link Sch.panel.SchedulerTree}. It is a subclass of "Ext.data.TreeStore" - a store containing hierarchical data.
 * 
 * Filtering capabilities are provided by {@link Sch.data.mixin.FilterableTreeStore}, please refer to its documentation for additional information.
 */
Ext.define("Sch.data.ResourceTreeStore", {
    extend  : 'Ext.data.TreeStore',
    model   : 'Sch.model.Resource',
    
    mixins  : [
        'Sch.data.mixin.ResourceStore',
        'Sch.data.mixin.FilterableTreeStore'
    ],
    
    constructor : function () {
        this.callParent(arguments);
        
        this.initTreeFiltering();

        var newC = function () {};
        newC.prototype = Ext.ModelManager.getModel(this.getModel()).prototype;

        if (!(new newC() instanceof Sch.model.Resource)) {
            throw 'The model for the ResourceTreeStore must subclass Sch.model.Resource';
        }
    }
}, function() {
    this.override(Sch.data.mixin.FilterableTreeStore.prototype.inheritables() || {});
});
/**
@class Sch.data.TimeAxis
@extends Ext.data.JsonStore

A class representing the time axis of the scheduler. The scheduler timescale is based on the ticks generated by this class.
This is a pure "data" (model) representation of the time axis and has no UI elements.
 
The time axis can be {@link #continuous} or not. In continuos mode, each timespan starts where the previous ended, and in non-continuous mode
 there can be gaps between the ticks.
A non-continuous time axis can be used when want to filter out certain periods of time (like weekends) from the time axis.

To create a non-continuos time axis you have 2 options. First, you can create a time axis containing only the time spans of interest.
To do that, subclass this class and override the {@link #generateTicks} method. See the `noncontinuous-timeaxis` example in the Ext Scheduler SDK for guidance.

The other alternative is to call the {@link #filterBy} method, passing a function to it which should return `false` if the time tick should be filtered out.
Calling the {@link #clearFilter} method will return you to full time axis.
 
*/
Ext.define("Sch.data.TimeAxis", {
    extend      : "Ext.data.JsonStore",
    
    requires    : [
        'Sch.util.Date',
        // this "require" is needed for Sencha Touch
        'Sch.model.TimeAxisTick'
    ],

    model : 'Sch.model.TimeAxisTick',

    /**
    * @cfg {Boolean} continuous
    * Set to false if the timeline is not continuous, e.g. the next timespan does not start where the previous ended (for example skipping weekends etc).
    */
    continuous : true,

    /**
    * @cfg {Boolean} autoAdjust
    * Automatically adjust the timespan when generating ticks with {@link #generateTicks} according to the `viewPreset` configuration. Setting this to false
    * may lead to shifting time/date of ticks.
    */    
    autoAdjust : true,


    // TODO should be removed from this class
    preset              : null,

    unit                : null,
    increment           : null,
    resolutionUnit      : null,
    resolutionIncrement : null,

    weekStartDay        : null,

    mainUnit            : null,
    shiftUnit           : null,

    headerConfig        : null,
    shiftIncrement      : 1,
    defaultSpan         : 1,

    // private
    constructor : function(config) {
        var me = this;

        // For Sencha Touch, config system
        if (me.setModel) {
            me.setModel(me.model);
        }

        me.originalContinuous = me.continuous;

        me.callParent(arguments);

        me.addEvents(
            /**
            * @event beforereconfigure
            * Fires before the timeaxis is about to be reconfigured (e.g. new start/end date or unit/increment). Return false to abort the operation.
            * @param {Sch.data.TimeAxis} timeAxis The time axis instance
            * @param {Date} startDate The new time axis start date
            * @param {Date} endDate The new time axis end date
            */
            'beforereconfigure',

            /**
            * @event reconfigure
            * Fires when the timeaxis has been reconfigured (e.g. new start/end date or unit/increment)
            * @param {Sch.data.TimeAxis} timeAxis The time axis instance
            */
            'reconfigure'
        );

        me.on(Ext.versions.touch ? 'refresh' : 'datachanged', function(ta, newPreset) {
            me.fireEvent('reconfigure', me, newPreset);
        });

        if (config && me.start) {
            me.reconfigure(config);
        }
    },

    /**
    * Reconfigures the time axis based on the config object supplied and generates the new 'ticks'.
    * @param {Object} config
    * @private
    */
    reconfigure : function(config) {
        Ext.apply(this, config); 
        var ticks = this.generateTicks(this.start, this.end, this.unit, this.increment || 1, this.mainUnit);
        
        if (this.fireEvent('beforereconfigure', this, this.start, this.end) !== false) {
            // Distinguish between altering the properties of the timeAxis as opposed to
            // traversing it, changing the start/end dates
            var nbrConfigKeys = Ext.Object.getKeys(config).length;
            var presetNotChanged = (nbrConfigKeys === 1 && "start" in config) ||
                                   (nbrConfigKeys === 2 && "start" in config && "end" in config);

            // Suspending to be able to detect an invalid filter
            this.removeAll(true);
            this.suspendEvents();
            this.add(ticks);
        
            if (this.getCount() === 0) {
                Ext.Error.raise('Invalid time axis configuration or filter, please check your input data.');
            }
            this.resumeEvents();

            this.fireEvent('datachanged', this, !presetNotChanged);
            this.fireEvent('refresh', this, !presetNotChanged);
        }
    },

    /**
    * Changes the time axis timespan to the supplied start and end dates.
    * @param {Date} start The new start date
    * @param {Date} end The new end date
    */
    setTimeSpan : function(start, end) {
        var adjusted = this.getAdjustedDates(start, end);

        if (this.getStart() - adjusted.start !== 0 || this.getEnd() - adjusted.end !== 0) {
            this.reconfigure({
                start   : start,
                end     : end
            });
        }
    },

    /**
     * [Experimental] Filter the time axis by a function. The passed function will be called with each tick in time axis. 
     * If the function returns true, the 'tick' is included otherwise it is filtered.
     * @param {Function} fn The function to be called, it will receive an object with start/end properties, and 'index' of the tick.
     * @param {Object} scope (optional) The scope (`this` reference) in which the function is executed. 
     */
    filterBy : function(fn, scope) {
        this.continuous = false;
        scope = scope || this;
        
        this.clearFilter(true);
        // Suspending to be able to detect an invalid filter
        this.suspendEvents(true);
        this.filter([{
            filterFn : function(t, index) {
                return fn.call(scope, t.data, index);
            }
        }]);

        if (this.getCount() === 0) {
            this.clearFilter();
            this.resumeEvents();
            Ext.Error.raise('Invalid time axis filter - no ticks passed through the filter. Please check your filter method.');
        }
        this.resumeEvents();
    },

    /**
     * Returns `true` if the time axis is continuos (will return `false` when filtered)
     * @return {Boolean}
     */
    isContinuous : function() {
        return this.continuous && !this.isFiltered();
    },

    /**
     * Clear the current filter of the time axis
     */
    clearFilter : function() {
        this.continuous = this.originalContinuous;
        this.callParent(arguments);
    },

    /**
     * Method generating the ticks for this time axis. Should return an array of ticks. Each tick is an object of the following structure:
        {
            start       : ..., // start date
            end         : ...  // end date
        }
     *
     * Take notice, that this function either has to be called with `start`/`end` parameters, or create those variables.
     * 
     * @param {Date} startDate The start date of the interval
     * @param {Date} endDate The end date of the interval
     * @param {String} unit The unit of the time axis
     * @param {Mixed} increment The increment for the unit specified.
     * @return {Array} ticks The ticks representing the time axis
     */
    generateTicks : function(start, end, unit, increment) {
        var ticks           = [],
            intervalEnd,
            DATE            = Sch.util.Date,
            dstDiff         = 0;

        unit                = unit || this.unit;
        increment           = increment || this.increment;
        
        if (this.autoAdjust) {
            var adjusted    = this.getAdjustedDates(start, end);
            start           = adjusted.start;
            end             = adjusted.end;
        }
       
        while (start < end) {
            intervalEnd     = this.getNext(start, unit, increment);
            
            if (!this.autoAdjust && intervalEnd > end) intervalEnd = end;
            
            // Handle hourly increments crossing DST boundaries to keep the timescale looking correct
            // Only do this for HOUR resolution currently, and only handle it once per tick generation.
            if (unit === DATE.HOUR && increment > 1 && ticks.length > 0 && dstDiff === 0) {
                var prev    = ticks[ ticks.length - 1 ];
                
                dstDiff     = ((prev.start.getHours() + increment) % 24) - prev.end.getHours();

                if (dstDiff !== 0) {
                    // A DST boundary was crossed in previous tick, adjust this tick to keep timeaxis "symmetric".
                    intervalEnd = DATE.add(intervalEnd, DATE.HOUR, dstDiff);
                }
            }

            ticks.push({
                start   : start,
                end     : intervalEnd
            });
            start           = intervalEnd;
        }
        
        return ticks;
    },

    getAdjustedDates : function(start, end) {
        if (this.autoAdjust) {
            start = this.floorDate(start || this.getStart(), false);
            end = this.ceilDate(end || Sch.util.Date.add(start, this.mainUnit, this.defaultSpan), false);
        }
        return { start : start, end : end };
    },

    /**
    * Gets a tick "coordinate" representing the date position on the time scale. Returns -1 if the date is not part of the time axis.
    * @param {Date} date the date
    * @return {Float} the tick position on the scale or -1 if the date is not part of the time axis
    */
    getTickFromDate : function(date) {
        
        if (this.getStart() > date || this.getEnd() < date) {
            return -1;
        } 

        var ticks = this.getRange(),
            tickStart, tickEnd, i, l;

        for (i = 0, l = ticks.length; i < l; i++) {
            tickEnd = ticks[i].data.end;
            if (date <= tickEnd) {
                tickStart = ticks[i].data.start;
                    
                return i + (date > tickStart ? (date - tickStart)/(tickEnd - tickStart) : 0);
            } 
        }
        
        return -1;
    },

    /**
    * Gets the time represented by a tick "coordinate".
    * @param {Float} tick the tick "coordinate"
    * @param {String} roundingMethod The rounding method to use
    * @return {Date} The date to represented by the tick "coordinate", or null if invalid.
    */
    getDateFromTick : function(tick, roundingMethod) {
        var count = this.getCount();
        
        if (tick === count){
            return this.getEnd();
        }

        var wholeTick = Math.floor(tick),
            fraction = tick - wholeTick,
            t = this.getAt(wholeTick);

        if (!t) {
            return null;
        }

        var tickData = t.data;
        var date = Sch.util.Date.add(tickData.start, Sch.util.Date.MILLI, fraction * (tickData.end - tickData.start));

        if (roundingMethod) {
            date = this[roundingMethod + 'Date'](date);
        }

        return date;
    },

    /**
    * Returns the ticks of the timeaxis in an array of objects with a "start" and "end" date.
    * @return {Array[Object]} the ticks on the scale
    */
    getTicks : function() {
        var ticks = [];
        
        this.each(function(r) { ticks.push(r.data); });
        return ticks;
    },

    /**
    * Method to get the current start date of the time axis
    * @return {Date} The start date
    */
    getStart : function() {
        var first = this.first();
        
        if (first) {
            return Ext.Date.clone(first.data.start);
        }
        return null;
    },

    /**
    * Method to get a the current end date of the time axis
    * @return {Date} The end date
    */
    getEnd : function() {
        var last = this.last();
        
        if (last) {
            return Ext.Date.clone(last.data.end);
        }
        return null;
    },

    // Floors a date and optionally snaps it to one of the following resolutions:
    // 1. 'resolutionUnit'. If param 'resolutionUnit' is passed, the date will simply be floored to this unit.
    // 2. If resolutionUnit is not passed: If date should be snapped relative to the timeaxis start date, the resolutionUnit of the timeAxis will be used, or the timeAxis 'mainUnit' will be used to snap the date
    //
    // returns a copy of the original date
    // private
    floorDate : function(date, relativeToStart, resolutionUnit) {
        relativeToStart = relativeToStart !== false;
        
        var dt = Ext.Date.clone(date),
            relativeTo = relativeToStart ? this.getStart() : null,
            increment = this.resolutionIncrement,
            unit;

        if (resolutionUnit) {
            unit = resolutionUnit;
        } else {
            unit = relativeToStart ? this.resolutionUnit : this.mainUnit;
        }

        switch(unit) {
            case Sch.util.Date.MILLI:    
                if (relativeToStart) {
                    var milliseconds = Sch.util.Date.getDurationInMilliseconds(relativeTo, dt),
                        snappedMilliseconds = Math.floor(milliseconds / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MILLI, snappedMilliseconds);
                }
                break;

            case Sch.util.Date.SECOND:
                if (relativeToStart) {
                    var seconds = Sch.util.Date.getDurationInSeconds(relativeTo, dt),
                        snappedSeconds = Math.floor(seconds / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MILLI, snappedSeconds * 1000);
                } else {
                    dt.setMilliseconds(0);
                }
                break;

            case Sch.util.Date.MINUTE:
                if (relativeToStart) {
                    var minutes = Sch.util.Date.getDurationInMinutes(relativeTo, dt),
                        snappedMinutes = Math.floor(minutes / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.SECOND, snappedMinutes * 60);
                } else {
                    dt.setSeconds(0);
                    dt.setMilliseconds(0);
                }
                break; 

            case Sch.util.Date.HOUR:
                if (relativeToStart) {
                    var nbrHours = Sch.util.Date.getDurationInHours(this.getStart(), dt),
                        snappedHours = Math.floor(nbrHours / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MINUTE, snappedHours * 60);
                } else {
                    dt.setMinutes(0);
                    dt.setSeconds(0);
                    dt.setMilliseconds(0);
                }
                break;

            case Sch.util.Date.DAY:
                if (relativeToStart) {
                    var nbrDays = Sch.util.Date.getDurationInDays(relativeTo, dt),
                        snappedDays = Math.floor(nbrDays / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.DAY, snappedDays);
                } else {
                    Ext.Date.clearTime(dt);
                }
                break;

            case Sch.util.Date.WEEK:
                var day = dt.getDay();
                Ext.Date.clearTime(dt);
                if (day !== this.weekStartDay) {
                    dt = Sch.util.Date.add(dt, Sch.util.Date.DAY, -(day > this.weekStartDay ? (day - this.weekStartDay) : (7 - day - this.weekStartDay)));
                }
                break;

            case Sch.util.Date.MONTH:
                if (relativeToStart) {
                    var nbrMonths = Sch.util.Date.getDurationInMonths(relativeTo, dt),
                        snappedMonths = Math.floor(nbrMonths / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MONTH, snappedMonths);
                } else {
                    Ext.Date.clearTime(dt);
                    dt.setDate(1);
                }
                break;

            case Sch.util.Date.QUARTER:
                Ext.Date.clearTime(dt);
                dt.setDate(1);
                dt = Sch.util.Date.add(dt, Sch.util.Date.MONTH, -(dt.getMonth() % 3));
                break;

            case Sch.util.Date.YEAR:
                if (relativeToStart) {
                    var nbrYears = Sch.util.Date.getDurationInYears(relativeTo, dt),
                        snappedYears = Math.floor(nbrYears / increment) * increment;
                    dt = Sch.util.Date.add(relativeTo, Sch.util.Date.YEAR, snappedYears);
                } else {
                    dt = new Date(date.getFullYear(), 0, 1);
                }
                break;

        }
        return dt;
    },


    // Rounds the date to nearest minute increment
    // private
    roundDate : function(date) {
        var dt = Ext.Date.clone(date),
            relativeTo = this.getStart(),
            increment = this.resolutionIncrement;

        switch(this.resolutionUnit) {
            case Sch.util.Date.MILLI:
                var milliseconds = Sch.util.Date.getDurationInMilliseconds(relativeTo, dt),
                    snappedMilliseconds = Math.round(milliseconds / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MILLI, snappedMilliseconds);
                break;

            case Sch.util.Date.SECOND:
                var seconds = Sch.util.Date.getDurationInSeconds(relativeTo, dt),
                    snappedSeconds = Math.round(seconds / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MILLI, snappedSeconds * 1000);
                break;

            case Sch.util.Date.MINUTE:
                var minutes = Sch.util.Date.getDurationInMinutes(relativeTo, dt),
                    snappedMinutes = Math.round(minutes / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.SECOND, snappedMinutes * 60);
                break;

            case Sch.util.Date.HOUR:
                var nbrHours = Sch.util.Date.getDurationInHours(this.getStart(), dt),
                    snappedHours = Math.round(nbrHours / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MINUTE, snappedHours * 60);
                break;

            case Sch.util.Date.DAY:
                var nbrDays = Sch.util.Date.getDurationInDays(relativeTo, dt),
                    snappedDays = Math.round(nbrDays / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.DAY, snappedDays);
                break;

            case Sch.util.Date.WEEK:
                Ext.Date.clearTime(dt);

                var distanceToWeekStartDay = dt.getDay() - this.weekStartDay,
                    toAdd;

                if (distanceToWeekStartDay < 0) {
                    distanceToWeekStartDay = 7 + distanceToWeekStartDay;
                }

                if (Math.round(distanceToWeekStartDay/7) === 1) {
                    toAdd = 7 - distanceToWeekStartDay;
                } else {
                    toAdd = -distanceToWeekStartDay;
                }

                dt = Sch.util.Date.add(dt, Sch.util.Date.DAY, toAdd);
                break;

            case Sch.util.Date.MONTH:
                var nbrMonths = Sch.util.Date.getDurationInMonths(relativeTo, dt) + (dt.getDate() / Ext.Date.getDaysInMonth(dt)),
                    snappedMonths = Math.round(nbrMonths / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.MONTH, snappedMonths);
                break;

            case Sch.util.Date.QUARTER:
                Ext.Date.clearTime(dt);
                dt.setDate(1);
                dt = Sch.util.Date.add(dt, Sch.util.Date.MONTH, 3 - (dt.getMonth() % 3));
                break;

            case Sch.util.Date.YEAR:
                var nbrYears = Sch.util.Date.getDurationInYears(relativeTo, dt),
                    snappedYears = Math.round(nbrYears / increment) * increment;
                dt = Sch.util.Date.add(relativeTo, Sch.util.Date.YEAR, snappedYears);
                break;

        }
        return dt;
    },

    // private
    ceilDate : function(date, relativeToStart, resolutionUnit) {
        var dt = Ext.Date.clone(date);
        relativeToStart = relativeToStart !== false;
        
        var increment = relativeToStart ? this.resolutionIncrement : 1,
            doCall = false,
            unit;

        if (resolutionUnit){
            unit = resolutionUnit;
        } else {
            unit = relativeToStart ? this.resolutionUnit : this.mainUnit;
        }

        switch (unit) {
            case Sch.util.Date.HOUR:
                if (dt.getMinutes() > 0 || dt.getSeconds() > 0 || dt.getMilliseconds() > 0) {
                    doCall = true;
                }
            break;

            case Sch.util.Date.DAY:
                if (dt.getHours() > 0 || dt.getMinutes() > 0 || dt.getSeconds() > 0 || dt.getMilliseconds() > 0) {
                    doCall = true;
                }
            break;

            case Sch.util.Date.WEEK: 
                Ext.Date.clearTime(dt);
                if (dt.getDay() !== this.weekStartDay) {
                    doCall = true;
                }
            break;

            case Sch.util.Date.MONTH: 
                Ext.Date.clearTime(dt);
                if(dt.getDate() !== 1) {
                    doCall = true;
                }
            break;

            case Sch.util.Date.QUARTER:
                Ext.Date.clearTime(dt);
                if(dt.getMonth() % 3 !== 0) {
                    doCall = true;
                }
            break;
            
            case Sch.util.Date.YEAR:
                Ext.Date.clearTime(dt);
                if(dt.getMonth() !== 0 || dt.getDate() !== 1) {
                    doCall = true;
                }
            break;

            default:
            break;
        }

        if (doCall) {
            return this.getNext(dt, unit, increment);
        } else {
            return dt;
        }
    },

    // private
    getNext : function(date, unit, increment) {
        return Sch.util.Date.getNext(date, unit, increment, this.weekStartDay);
    },

    // private
    getResolution : function() {
        return {
            unit : this.resolutionUnit,
            increment : this.resolutionIncrement
        };
    },

    // private
    setResolution : function(unit, increment) {
        this.resolutionUnit = unit;
        this.resolutionIncrement = increment || 1;
    },

    /**
     * Moves the time axis by the passed amount and unit.
     * @param {Int} amount The number of units to jump
     * @param {String} unit The unit (Day, Week etc)
     */
    shift: function (amount, unit) {
        this.setTimeSpan(Sch.util.Date.add(this.getStart(), unit, amount), Sch.util.Date.add(this.getEnd(), unit, amount));
    },

    /**
    * Moves the time axis forward in time in units specified by the view preset `shiftUnit`, and by the amount specified by the `shiftIncrement` 
    * config of the current view preset.
    * @param {Int} amount (optional) The number of units to jump forward
    */
    shiftNext: function (amount) {
        amount = amount || this.getShiftIncrement();
        var unit = this.getShiftUnit();
        this.setTimeSpan(Sch.util.Date.add(this.getStart(), unit, amount), Sch.util.Date.add(this.getEnd(), unit, amount));
    },

    /**
    * Moves the time axis backward in time in units specified by the view preset `shiftUnit`, and by the amount specified by the `shiftIncrement` config of the current view preset.
    * @param {Int} amount (optional) The number of units to jump backward
    */
    shiftPrevious: function (amount) {
        amount = -(amount || this.getShiftIncrement());
        var unit = this.getShiftUnit();
        this.setTimeSpan(Sch.util.Date.add(this.getStart(), unit, amount), Sch.util.Date.add(this.getEnd(), unit, amount));
    },

    getShiftUnit: function () {
        return this.shiftUnit || this.getMainUnit();
    },
    
    // private
    getShiftIncrement: function () {
        return this.shiftIncrement || 1;
    },
    
    // private
    getUnit: function () {
        return this.unit;
    },

    // private
    getIncrement: function () {
        return this.increment;
    },

    /**
    * Returns true if the passed date is inside the span of the current time axis.
    * @param {Date} date The date to query for
    * @return {Boolean} true if the date is part of the timeaxis
    */
    dateInAxis: function(date) {
        return Sch.util.Date.betweenLesser(date, this.getStart(), this.getEnd());
    },

    /**
    * Returns true if the passed timespan is part of the current time axis (in whole or partially).
    * @param {Date} start The start date
    * @param {Date} end The end date
    * @return {boolean} true if the timespan is part of the timeaxis
    */
    timeSpanInAxis: function(start, end) {
        if (this.isContinuous()) {
            return Sch.util.Date.intersectSpans(start, end, this.getStart(), this.getEnd());
        } else {
            return (start < this.getStart() && end > this.getEnd()) || 
                   this.getTickFromDate(start) !== this.getTickFromDate(end);
        }
    },

    /**
    * Calls the supplied iterator function once per interval. The function will be called with three parameters, start date and end date and an index.
    * Return false to break the iteration.
    * @param {String} position 'main' (middle), 'top' or 'bottom'
    * @param {Function} iteratorFn The function to call, will be called with start date, end date and "tick index"
    * @param {Object} scope (optional) The "this" object to use for the function call
    */
    forEachInterval : function(position, iteratorFn, scope) {
        scope = scope || this;

        if (!this.headerConfig) return;     // Not initialized

        if (position === 'top' || (position === 'middle' && this.headerConfig.bottom)) {
            this.forEachAuxInterval(position, iteratorFn, scope);
        } else {
            // This is the lowest header row, which should be fed the data in the tickStore
            this.each(function(r, index) { 
                return iteratorFn.call(scope, r.data.start, r.data.end, index);
            });
        }
    },

    /**
    * Calls the supplied iterator function once per interval. The function will be called with three parameters, start date and end date and an index.
    * Return false to break the iteration.
    * @protected
    * @param {Function} iteratorFn The function to call
    * @param {Object} scope (optional) The "this" object to use for the function call
    */
    forEachMainInterval : function(iteratorFn, scope) {
        this.forEachInterval('middle', iteratorFn, scope);
    },

    /**
    * Calls the supplied iterator function once per interval. The function will be called with three parameters, start date and end date and an index.
    * @protected
    * @param {Function} iteratorFn The function to call
    * @param {Object} scope (optional) The "this" object to use for the function call
    */
    forEachAuxInterval : function(position, iteratorFn, scope) {
        scope = scope || this;

        var end             = this.getEnd(),
            dt              = this.getStart(),
            i               = 0,
            intervalEnd;

        if (dt > end) throw 'Invalid time axis configuration';

        while (dt < end) {
            intervalEnd =  Sch.util.Date.min(this.getNext(dt, this.headerConfig[position].unit, this.headerConfig[position].increment || 1), end);
            iteratorFn.call(scope, dt, intervalEnd, i);
            dt = intervalEnd;
            i++;
        }
    }
});
/**
 * @class Sch.view.Horizontal
 * @private
 *
 * An internal view mixin, purposed to be consumed along with {@link Sch.mixin.AbstractTimelineView}.
 * This class is consumed by the scheduling view and provides the horizontal implementation of certain methods.
 */
Ext.define("Sch.view.Horizontal", {
    requires : [
        'Ext.util.Region',
        'Ext.Element',
        'Sch.util.Date'
    ],
    // Provided by creator, in the config object
    view: null,

    constructor: function (config) {
        Ext.apply(this, config);
    },

    translateToScheduleCoordinate: function (x) {
        var view = this.view;

        if (view.rtl) {
            return view.getTimeAxisColumn().getEl().getRight() - x;
        }
        return x - view.getEl().getX() + view.getScroll().left;
    },

    translateToPageCoordinate: function (x) {
        var view = this.view;
        return x + view.getEl().getX() - view.getScroll().left;
    },

    getEventRenderData: function (event, start, end) {
        var eventStart  = start || event.getStartDate(),
            eventEnd    = end || event.getEndDate() || eventStart, // Allow events to be rendered even they are missing an end date
            view        = this.view,
            viewStart   = view.timeAxis.getStart(),
            viewEnd     = view.timeAxis.getEnd(),
            M           = Math,
            startX      = view.getXFromDate(Sch.util.Date.max(eventStart, viewStart)),
            endX        = view.getXFromDate(Sch.util.Date.min(eventEnd, viewEnd)),
            data        = {};

        if (this.view.rtl) {
            data.right = M.min(startX, endX);
        } else {
            data.left = M.min(startX, endX);
        }

        data.width = M.max(1, M.abs(endX - startX)) - view.eventBorderWidth;

        if (view.managedEventSizing) {
            data.top = M.max(0, (view.barMargin - ((Ext.isIE && !Ext.isStrict) ? 0 : view.eventBorderWidth - view.cellTopBorderWidth)));
            data.height = view.rowHeight - (2 * view.barMargin) - view.eventBorderWidth;
        }

        data.start              = eventStart;
        data.end                = eventEnd;
        data.startsOutsideView  = eventStart < viewStart;
        data.endsOutsideView    = eventEnd > viewEnd;
        return data;
    },

    /**
    * Gets the Ext.util.Region, relative to the page, represented by the schedule and optionally only for a single resource. This method will call getDateConstraints to 
    * allow for additional resource/event based constraints. By overriding that method you can constrain events differently for
    * different resources.
    * @param {Sch.model.Resource} resourceRecord (optional) The resource record
    * @param {Sch.model.Event} eventRecord (optional) The event record
    * @return {Ext.util.Region} The region of the schedule
    */
    getScheduleRegion: function (resourceRecord, eventRecord) {
        var getRegionFn     = Ext.Element.prototype.getRegion ? 'getRegion' : 'getPageBox',
            view            = this.view,
            region          = resourceRecord ? Ext.fly(view.getRowNode(resourceRecord))[getRegionFn]() : view.getTableRegion(),
            taStart         = view.timeAxis.getStart(),
            taEnd           = view.timeAxis.getEnd(),
            dateConstraints = view.getDateConstraints(resourceRecord, eventRecord) || { start: taStart, end: taEnd },
            startX          = this.translateToPageCoordinate(view.getXFromDate(dateConstraints.start)),
            endX            = this.translateToPageCoordinate(view.getXFromDate(dateConstraints.end)),
            top             = region.top + view.barMargin,
            bottom          = region.bottom - view.barMargin - view.eventBorderWidth;

        return new Ext.util.Region(top, Math.max(startX, endX), bottom, Math.min(startX, endX));
    },


    /**
    * Gets the Ext.util.Region, relative to the scheduling view element, representing the passed resource and optionally just for a certain date interval.
    * @param {Sch.model.Resource} resourceRecord The resource record
    * @param {Date} startDate A start date constraining the region
    * @param {Date} endDate An end date constraining the region
    * @return {Ext.util.Region} The region of the resource
    */
    getResourceRegion: function (resourceRecord, startDate, endDate) {
        var view        = this.view,
            rowNode     = view.getRowNode(resourceRecord),
            offsets     = Ext.fly(rowNode).getOffsetsTo(view.getEl()),
            taStart     = view.timeAxis.getStart(),
            taEnd       = view.timeAxis.getEnd(),
            start       = startDate ? Sch.util.Date.max(taStart, startDate) : taStart,
            end         = endDate ? Sch.util.Date.min(taEnd, endDate) : taEnd,
            startX      = view.getXFromDate(start),
            endX        = view.getXFromDate(end),
            top         = offsets[1] + view.cellTopBorderWidth,
            bottom      = offsets[1] + Ext.fly(rowNode).getHeight() - view.cellBottomBorderWidth;

        if (!Ext.versions.touch) {
            var ctElScroll  = view.getScroll();
            top += ctElScroll.top;
            bottom += ctElScroll.top;
        }
        return new Ext.util.Region(top, Math.max(startX, endX), bottom, Math.min(startX, endX));
    },


    columnRenderer: function (val, meta, resourceRecord, rowIndex, colIndex) {
        var view            = this.view;
        var resourceEvents  = view.eventStore.getEventsForResource(resourceRecord);

        if (resourceEvents.length === 0) {
            return;
        }

        var ta              = view.timeAxis,
            eventsTplData   = [],
            i, l;

        // Iterate events belonging to current row
        for (i = 0, l = resourceEvents.length; i < l; i++) {
            var event       = resourceEvents[i],
                start       = event.getStartDate(),
                end         = event.getEndDate();

            // Determine if the event should be rendered or not
            if (start && end && ta.timeSpanInAxis(start, end)) {
                eventsTplData[eventsTplData.length] = view.generateTplData(event, resourceRecord, rowIndex);
            }
        }

        // Event data is now gathered, calculate layout properties for each event (if dynamicRowHeight is used)
        if (view.dynamicRowHeight) {
            var layout              = view.eventLayout.horizontal;
            
            layout.applyLayout(eventsTplData, resourceRecord);
            
            meta.rowHeight          = layout.getRowHeight(resourceRecord, resourceEvents);
        }

        return view.eventTpl.apply(eventsTplData);
    },
    
    
    // private
    resolveResource: function (t) {
        var view = this.view;
        var node = view.findRowByChild(t);

        if (node) {
            return view.getRecordForRowNode(node);
        }

        return null;
    },

    /**
    *  Returns the region for a "global" time span in the view. Coordinates are relative to element containing the time columns
    *  @param {Date} startDate The start date of the span
    *  @param {Date} endDate The end date of the span
    *  @return {Ext.util.Region} The region for the time span
    */
    getTimeSpanRegion: function (startDate, endDate, useViewSize) {
        var view    = this.view,
            startX  = view.getXFromDate(startDate),
            endX    = endDate ? view.getXFromDate(endDate) : startX,
            height, region;

        region = view.getTableRegion();
        
        if (useViewSize) {
            height = Math.max(region ? region.bottom - region.top: 0, view.getEl().dom.clientHeight); // fallback in case grid is not rendered (no rows/table)
        } else {
            height = region ? region.bottom - region.top: 0;
        }
        return new Ext.util.Region(0, Math.max(startX, endX), height, Math.min(startX, endX));
    },

    /**
    * Gets the start and end dates for an element Region
    * @param {Region} region The region to map to start and end dates
    * @param {String} roundingMethod The rounding method to use
    * @returns {Object} an object containing start/end properties
    */
    getStartEndDatesFromRegion: function (region, roundingMethod, allowPartial) {
        var view        = this.view;
        var rtl         = view.rtl;
        
        var startDate   = view.getDateFromCoordinate(rtl ? region.right : region.left, roundingMethod),
            endDate     = view.getDateFromCoordinate(rtl ? region.left : region.right, roundingMethod);
            
        if (startDate && endDate || allowPartial && (startDate || endDate)) {
            return {
                start   : startDate,
                end     : endDate
            };
        }
        
        return null;
    },

    // private
    onEventAdd: function (s, events) {
        var view = this.view;
        var affectedResources = {};

        for (var i = 0, l = events.length; i < l; i++) {
            var resources = events[i].getResources();

            for (var j = 0, k = resources.length; j < k; j++) {
                var resource = resources[j];

                affectedResources[resource.getId()] = resource;
            }
        }

        Ext.Object.each(affectedResources, function (id, resource) {
            view.repaintEventsForResource(resource);
        });
    },

    // private
    onEventRemove: function (s, eventRecords) {
        var view = this.view;
        var resourceStore   = this.resourceStore;
        var isTree          = Ext.tree && Ext.tree.View && view instanceof Ext.tree.View;

        if (!Ext.isArray(eventRecords)) {
            eventRecords = [eventRecords];
        }

        var updateResource  = function(resource) {
            if (view.store.indexOf(resource) >= 0) {
                view.repaintEventsForResource(resource);
            }
        };

        for (var i = 0; i < eventRecords.length; i++) {
            var resources = eventRecords[i].getResources();

            if (resources.length > 1) {
                Ext.each(resources, updateResource, this);
            } else {
                var node = view.getEventNodeByRecord(eventRecords[i]);

                if (node) {
                    var resource = view.resolveResource(node);

                    // Note, the methods below should not rely on Ext.get but since Ext.anim.run
                    // doesn't support HTMLElements, and due to this bug:
                    // http://www.sencha.com/forum/showthread.php?248981-4.1.x-fadeOut-fadeIn-etc-not-safe-to-use-with-flyweights&p=911314#post911314
                    // currently we have to live with this

                    if (Ext.Element.prototype.fadeOut) {
                        Ext.get(node).fadeOut({
                            callback: function() { updateResource(resource); }
                        });
                    } else {
                        Ext.Anim.run(Ext.get(node), 'fade', {
                            out         : true,
                            duration    : 500,
                            after       : function() { updateResource(resource); },
                            autoClear   : false
                        });
                    }
                }
            }
        }
    },

    // private
    onEventUpdate: function (store, model, operation) {
        var previous = model.previous;
        var view = this.view;

        if (previous && previous[model.resourceIdField]) {
            // If an event has been moved to a new row, refresh old row first
            var resource = model.getResource(previous[model.resourceIdField]);
            if (resource) {
                view.repaintEventsForResource(resource, true);
            }
        }

        var resources = model.getResources();

        Ext.each(resources, function(resource) {
            view.repaintEventsForResource(resource, true);
        });
    },

    setColumnWidth: function (width, preventRefresh) {
        var view = this.view;

        view.getTimeAxisViewModel().setTickWidth(width, preventRefresh);

        view.fireEvent('columnwidthchange', view, width);
    },

    /**
    * Method to get the currently visible date range in a scheduling view. Please note that it only works when the schedule is rendered.
    * @return {Object} object with `startDate` and `endDate` properties.
    */
    getVisibleDateRange: function () {
        var view = this.view;

        if (!view.getEl()) {
            return null;
        }

        var tableRegion = view.getTableRegion(),
            startDate   = view.timeAxis.getStart(),
            endDate     = view.timeAxis.getEnd(),
            width       = view.getWidth();

        if ((tableRegion.right - tableRegion.left) < width) {
            return { startDate: startDate, endDate: endDate };
        }

        var scroll      = view.getScroll();

        return {
            startDate   : view.getDateFromCoordinate(scroll.left, null, true),
            endDate     : view.getDateFromCoordinate(scroll.left + width, null, true)
        };
    }
}); 

/**
@class Sch.view.Vertical

A mixin, purposed to be consumed along with {@link Sch.mixin.AbstractTimelineView} and providing the implementation of some methods, specific to vertical orientation.

*/
Ext.define("Sch.view.Vertical", {

    // Provided by creator, in the config object
    view : null,

    constructor : function(config) {
        Ext.apply(this, config);
    },

    translateToScheduleCoordinate: function (y) {
        var view = this.view;
        return y - view.getEl().getY() + view.getScroll().top;
    },

    // private
    translateToPageCoordinate: function (y) {
        var view = this.view;
        var el = view.getEl(),
            scroll = el.getScroll();

        return y + el.getY() - scroll.top;
    },

    getEventRenderData : function(event) {
        var eventStart  = event.getStartDate(),
            eventEnd    = event.getEndDate(),
            view        = this.view,
            viewStart   = view.timeAxis.getStart(),
            viewEnd     = view.timeAxis.getEnd(),
            M           = Math,
            startY      = M.floor(view.getCoordinateFromDate(Sch.util.Date.max(eventStart, viewStart))),
            endY        = M.floor(view.getCoordinateFromDate(Sch.util.Date.min(eventEnd, viewEnd))),
            colWidth    = this.getResourceColumnWidth(event.getResource()),
            data;

        data = {
            top     : M.max(0, M.min(startY, endY) - view.eventBorderWidth),
            height  : M.max(1, M.abs(startY - endY))
        };

        if (view.managedEventSizing) {
            data.left = view.barMargin;
            data.width = colWidth - (2*view.barMargin) - view.eventBorderWidth;
        }

        data.start = eventStart;
        data.end = eventEnd;
        data.startsOutsideView = eventStart < viewStart;
        data.endsOutsideView = eventEnd > viewEnd;

        return data;
    },

    getScheduleRegion: function (resourceRecord, eventRecord) {
        var view        = this.view,
            region      = resourceRecord ? Ext.fly(view.getScheduleCell(0, view.resourceStore.indexOf(resourceRecord))).getRegion() : view.getTableRegion(),

            taStart     = view.timeAxis.getStart(),
            taEnd       = view.timeAxis.getEnd(),

            dateConstraints     = view.getDateConstraints(resourceRecord, eventRecord) || { start: taStart, end: taEnd },

            startY      = this.translateToPageCoordinate(view.getCoordinateFromDate(Sch.util.Date.min(taStart, dateConstraints.start))),
            endY        = this.translateToPageCoordinate(view.getCoordinateFromDate(Sch.util.Date.max(taEnd, dateConstraints.end))),

            left        = region.left + view.barMargin,
            right       = (resourceRecord ? (region.left + this.getResourceColumnWidth(resourceRecord)) : region.right) - view.barMargin;

        return new Ext.util.Region(Math.min(startY, endY), right, Math.max(startY, endY), left);
    },

    getResourceColumnWidth : function(resource) {
        return this.view.resourceColumnWidth;
    },

    /**
    * Gets the Ext.util.Region representing the passed resource and optionally just for a certain date interval.
    * @param {Sch.model.Resource} resourceRecord The resource record
    * @param {Date} startDate A start date constraining the region
    * @param {Date} endDate An end date constraining the region
    * @return {Ext.util.Region} The region of the resource
    */
    getResourceRegion: function (resourceRecord, startDate, endDate) {
        var view            = this.view,
            cellLeft        = view.resourceStore.indexOf(resourceRecord) * this.getResourceColumnWidth(resourceRecord),
            taStart         = view.timeAxis.getStart(),
            taEnd           = view.timeAxis.getEnd(),
            start           = startDate ? Sch.util.Date.max(taStart, startDate) : taStart,
            end             = endDate ? Sch.util.Date.min(taEnd, endDate) : taEnd,
            startY          = Math.max(0, view.getCoordinateFromDate(start) - view.cellTopBorderWidth),
            endY            = view.getCoordinateFromDate(end) - view.cellTopBorderWidth,
            left            = cellLeft + view.cellBorderWidth,
            right           = cellLeft + this.getResourceColumnWidth(resourceRecord) - view.cellBorderWidth;

        return new Ext.util.Region(Math.min(startY, endY), right, Math.max(startY, endY), left);
    },

    columnRenderer: function (val, meta, resourceRecord, rowIndex, colIndex) {
        var view = this.view;
        var retVal = '';

        if (rowIndex === 0) {
            var D               = Sch.util.Date,
                ta              = view.timeAxis,
                columnEvents,
                resourceEvents,
                i, l;

            columnEvents = [];
            resourceEvents = view.eventStore.getEventsForResource(resourceRecord);

            // Iterate events (belonging to current resource)
            for (i = 0, l = resourceEvents.length; i < l; i++) {
                var event   = resourceEvents[i],
                    start   = event.getStartDate(),
                    end     = event.getEndDate();

                // Determine if the event should be rendered or not
                if (start && end && ta.timeSpanInAxis(start, end)) {
                    columnEvents.push(view.generateTplData(event, resourceRecord, colIndex));
                }
            }
            view.eventLayout.vertical.applyLayout(columnEvents, this.getResourceColumnWidth(resourceRecord));
            retVal = '&#160;' + view.eventTpl.apply(columnEvents);

            // HACK: Required for IE in Quirks mode
            if (Ext.isIE) {
                meta.tdAttr = 'style="z-index:1000"';
            }
        }

        if (colIndex % 2 === 1) {
            meta.tdCls = (meta.tdCls || '') + ' ' + view.altColCls;
            meta.cellCls = (meta.cellCls || '') + ' ' + view.altColCls;
        }

        return retVal;
    },

    // private
    resolveResource: function (el) {
        var view = this.view;
        el = Ext.fly(el).is(view.timeCellSelector) ? el : Ext.fly(el).up(view.timeCellSelector);

        if (el) {
            var node = el.dom ? el.dom : el;
            var index = Ext.Array.indexOf(Array.prototype.slice.call(node.parentNode.children), node);

            if (index >= 0) {
                return view.resourceStore.getAt(index);
            }
        }

        return null;
    },

    // private
    onEventUpdate: function (store, model) {
        this.renderSingle.call(this, model);
        var view    = this.view;
        var previous = model.previous;

        if (previous && previous[model.resourceIdField]) {
            // If an event has been moved to a new resource, refresh old resource first
            var resource = model.getResource(previous[model.resourceIdField]);
            this.relayoutRenderedEvents(resource);
        }

        this.relayoutRenderedEvents(model.getResource());

        if (view.getSelectionModel().isSelected(model)) {
            view.onEventSelect(model, true);
        }
    },

    // private
    onEventAdd: function (s, recs) {
        var view = this.view;

        if (recs.length === 1) {
            this.renderSingle(recs[0]);
            this.relayoutRenderedEvents(recs[0].getResource());
        } else {
            view.repaintAllEvents();
        }
    },

    // private
    onEventRemove: function (s, recs) {
        var view = this.view;

        if (recs.length === 1) {
            this.relayoutRenderedEvents(this.getResourceByEventRecord(recs[0]));
        } else {
            view.repaintAllEvents();
        }
    },

    relayoutRenderedEvents : function(resource) {
        var data = [],
            view = this.view,
            i, l, event, node,
            events = view.eventStore.getEventsForResource(resource);

        if (events.length > 0) {

            for (i = 0, l = events.length; i < l; i++) {
                event = events[i];
                node = view.getEventNodeByRecord(event);

                if (node) {
                    data.push({
                        start   : event.getStartDate(),
                        end     : event.getEndDate(),
                        id      : node.id
                    });
                }
            }

            view.eventLayout.vertical.applyLayout(data, this.getResourceColumnWidth(resource));

            for (i = 0; i < data.length; i++) {
                event = data[i];
                Ext.fly(event.id).setStyle({
                    left    : event.left + 'px',
                    width   : event.width + 'px'
                });
            }
        }
    },

    renderSingle : function (event) {
        // Inject moved event into correct cell
        var resource    = event.getResource();
        var view        = this.view;
        var existing    = view.getEventNodeByRecord(event);
        var rIndex      = view.resourceStore.indexOf(resource);

        if (existing) {
            Ext.fly(existing).destroy();
        }

        var containerCell   = Ext.fly(view.getScheduleCell(0, rIndex));
        var data            = view.generateTplData(event, resource, rIndex);

        if (!Ext.versions.touch) {
            containerCell = containerCell.first();
        }

        view.eventTpl.append(containerCell, [data]);
    },

    /**
    *  Returns the region for a "global" time span in the view. Coordinates are relative to element containing the time columns
    *  @param {Date} startDate The start date of the span
    *  @param {Date} endDate The end date of the span
    *  @return {Ext.util.Region} The region for the time span
    */
    getTimeSpanRegion: function (startDate, endDate) {
        var view        = this.view,
            startY      = view.getCoordinateFromDate(startDate),
            endY        = endDate ? view.getCoordinateFromDate(endDate) : startY,
            tableRegion = view.getTableRegion(),
            width       = tableRegion ? tableRegion.right - tableRegion.left : view.getEl().dom.clientWidth; // fallback in case grid is not rendered (no rows/table)

        return new Ext.util.Region(Math.min(startY, endY), width, Math.max(startY, endY), 0);
    },

    /**
    * Gets the start and end dates for an element Region
    * @param {Region} region The region to map to start and end dates
    * @param {String} roundingMethod The rounding method to use
    * @returns {Object} an object containing start/end properties
    */
    getStartEndDatesFromRegion: function (region, roundingMethod, allowPartial) {
        var topDate = this.view.getDateFromCoordinate(region.top, roundingMethod),
            bottomDate = this.view.getDateFromCoordinate(region.bottom, roundingMethod);

        if (topDate && bottomDate) {
            return {
                start : Sch.util.Date.min(topDate, bottomDate),
                end : Sch.util.Date.max(topDate, bottomDate)
            };
        } else {
            return null;
        }
    },

    setColumnWidth : function (width, preventRefresh) {
        var view = this.view;
        view.resourceColumnWidth = width;

        view.fireEvent('columnwidthchange', view, width);
    },

    /**
    * Method to get the currently visible date range in a scheduling view. Please note that it only works when the schedule is rendered.
    * @return {Object} object with `startDate` and `endDate` properties.
    */
    getVisibleDateRange: function () {
        var view = this.view;

        if (!view.rendered) {
            return null;
        }

        var scroll      = view.getScroll(),
            height      = view.getHeight(),
            tableRegion = view.getTableRegion(),
            viewEndDate = view.timeAxis.getEnd();

        if (tableRegion.bottom - tableRegion.top < height) {
            var startDate   = view.timeAxis.getStart();

            return { startDate: startDate, endDate: endDate };
        }

        return {
            startDate   : view.getDateFromCoordinate(scroll.top, null, true),
            endDate     : view.getDateFromCoordinate(scroll.top + height, null, true) || viewEndDate
        };
    }
});

/**
@class Sch.selection.EventModel
@extends Ext.selection.Model

This class provides the basic implementation event selection in a grid.

*/
Ext.define("Sch.selection.EventModel", {
    extend      : 'Ext.selection.Model',

    alias       : 'selection.eventmodel',

    requires    : [ 'Ext.util.KeyNav' ],

    /**
     * @cfg {Boolean} deselectOnContainerClick `True` to deselect all events when user clicks on the underlying space in scheduler. Defaults to `true`.
     */
    deselectOnContainerClick : true,

    // Stores selected record on mousedown event to avoid 
    // unselecting record on click
    selectedOnMouseDown : false,

    constructor: function(cfg){
        this.addEvents(
            /**
             * @event beforedeselect
             * Fired before a record is deselected. If any listener returns false, the
             * deselection is cancelled.
             * @param {Sch.selection.EventModel} this
             * @param {Sch.model.Event} record The selected event
             */
            'beforedeselect',

            /**
             * @event beforeselect
             * Fired before a record is selected. If any listener returns false, the
             * selection is cancelled.
             * @param {Sch.selection.EventModel} this
             * @param {Sch.model.Event} record The selected event
             */
            'beforeselect',

            /**
             * @event deselect
             * Fired after a record is deselected
             * @param {Sch.selection.EventModel} this
             * @param {Sch.model.Event} record The selected event
             */
            'deselect',

            /**
             * @event select
             * Fired after a record is selected
             * @param {Sch.selection.EventModel} this
             * @param {Sch.model.Event} record The selected event
             */
            'select'
        );
        this.callParent(arguments);
    },

    bindComponent: function(view) {
        var me = this,
            eventListeners = {
                refresh : me.refresh,
                scope   : me
            };

        me.view = view;
        me.bindStore(view.getEventStore());

        view.on({
            eventclick     : me.onEventClick,
            eventmousedown : me.onEventMouseDown,
            itemmousedown  : me.onItemMouseDown,
            scope          : this
        });

        view.on(eventListeners);
    },

    onEventMouseDown: function(view, record, e) {
        // Reset previously stored records
        this.selectedOnMouseDown = null;
        
        // Change selection before dragging to avoid moving of unselected events
        if (!this.isSelected(record)) {
            this.selectedOnMouseDown = record;
            this.selectWithEvent(record, e);
        }
    },
    
    onEventClick: function(view, record, e) {
        // Don't change selection if record been already selected on mousedown
        if (!this.selectedOnMouseDown) {
            this.selectWithEvent(record, e);
        }
    },

    onItemMouseDown: function() {
        if (this.deselectOnContainerClick) {
            this.deselectAll();
        }
    },

    onSelectChange: function(record, isSelected, suppressEvent, commitFn) {
         var me      = this,
            view   = me.view,
            store   = me.store,
            eventName = isSelected ? 'select' : 'deselect',
            i = 0;

        if ((suppressEvent || me.fireEvent('before' + eventName, me, record)) !== false &&
                commitFn() !== false) {

            if (isSelected) {
                view.onEventSelect(record, suppressEvent);
            } else {
                view.onEventDeselect(record, suppressEvent);
            }

            if (!suppressEvent) {
                me.fireEvent(eventName, me, record);
            }
        }
    },

    // Not supported.
    selectRange : Ext.emptyFn,

    selectNode: function(node, keepExisting, suppressEvent) {
        var r = this.view.resolveEventRecord(node);
        if (r) {
            this.select(r, keepExisting, suppressEvent);
        }
    },

    deselectNode: function(node, keepExisting, suppressEvent) {
        var r = this.view.resolveEventRecord(node);
        if (r) {
            this.deselect(r, suppressEvent);
        }
    }
});
/**
@class Sch.plugin.Printable

Plugin (ptype = 'scheduler_printable') for printing an Ext Scheduler instance. Please note that this will not generate a picture perfect
 printed version, due to various limitations in the browser print implementations. If you require a high quality print, you should use the Export plugin instead and first export to PDF.

 To use this plugin, add it to scheduler as usual. The plugin will add an additional `print` method to the scheduler:

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                Ext.create('Sch.plugin.Printable', {
                    // default values
                    docType             : '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">',
                    autoPrintAndClose   : true
                })
            ]
        });

        ...

        scheduler.print();


*/
Ext.define("Sch.plugin.Printable", {
    extend          : 'Ext.AbstractPlugin',

    alias           : 'plugin.scheduler_printable',

    lockableScope   : 'top',

    /**
     * @cfg {String} docType This is the DOCTYPE to use for the print window. It should be the same DOCTYPE as on your application page.
     */
    docType             : '<!DOCTYPE HTML>',

    /**
     * An empty function by default, but provided so that you can perform a custom action
     * before the print plugin extracts data from the scheduler.
     * @param {SchedulerPanel} scheduler The scheduler instance
     * @method beforePrint
     */
    beforePrint         : Ext.emptyFn,

    /**
     * An empty function by default, but provided so that you can perform a custom action
     * after the print plugin has extracted the data from the scheduler.
     * @param {SchedulerPanel} scheduler The scheduler instance
     * @method afterPrint
     */
    afterPrint          : Ext.emptyFn,

    /**
     * @cfg {Boolean} autoPrintAndClose True to automatically call print and close the new window after printing. Default value is `true`
     */
    autoPrintAndClose   : true,

     /**
     * @cfg {Boolean} fakeBackgroundColor True to reset background-color of events and enable use of border-width to fake background color (borders print by default in every browser). Default value is `true`
     */
    fakeBackgroundColor : true,

    scheduler           : null,


    constructor : function(config) {
        Ext.apply(this, config);
    },

    init : function(scheduler) {
        this.scheduler = scheduler;
        scheduler.print = Ext.Function.bind(this.print, this);
    },

    // private, the template for the new window
    mainTpl : new Ext.XTemplate('{docType}' +
          '<html class="' + Ext.baseCSSPrefix + 'border-box {htmlClasses}">' +
            '<head>' +
              '<meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />' +
              '<title>{title}</title>' +
              '{styles}' +
            '</head>' +
            '<body class="sch-print-body {bodyClasses}">'+
                '<div class="sch-print-ct {componentClasses}" style="width:{totalWidth}px">'+
                    '<div class="sch-print-headerbg" style="border-left-width:{totalWidth}px;height:{headerHeight}px;"></div>'+
                    '<div class="sch-print-header-wrap">'+
                        '{[this.printLockedHeader(values)]}'+
                        '{[this.printNormalHeader(values)]}'+
                    '</div>'+
                    '{[this.printLockedGrid(values)]}'+
                    '{[this.printNormalGrid(values)]}'+
                '</div>' +
                '<script type="text/javascript">'+
                    '{setupScript}'+
                '</script>'+
            '</body>'+
          '</html>',
          {
            printLockedHeader: function(values){
                var str = '';

                if (values.lockedGrid){
                    str += '<div style="left:-'+values.lockedScroll+'px;margin-right:-'+values.lockedScroll+'px;width:'+(values.lockedWidth + values.lockedScroll)+'px"';
                    str += 'class="sch-print-lockedheader ' + values.lockedGrid.headerCt.el.dom.className + '">';
                    str +=     values.lockedHeader;
                    str += '</div>';
                }
                return str;
            },
            printNormalHeader: function(values){
                var str = '';

                if (values.normalGrid){
                    str += '<div style="left:'+(values.lockedGrid ? values.lockedWidth : '0')+'px;width:'+values.normalWidth+'px;" class="sch-print-normalheader '  + values.normalGrid.headerCt.el.dom.className + '">';
                    str +=     '<div style="margin-left:-'+values.normalScroll+'px">'+values.normalHeader+'</div>';
                    str += '</div>';
                }
                return str;
            },
            printLockedGrid: function(values){
                var str = '';

                if (values.lockedGrid){
                    str += '<div id="lockedRowsCt" style="left:-'+values.lockedScroll+'px;margin-right:-'+values.lockedScroll+'px;width:'+(values.lockedWidth + values.lockedScroll)+'px;top:'+values.headerHeight+'px;" class="sch-print-locked-rows-ct ' + values.innerLockedClasses + ' ' + Ext.baseCSSPrefix + 'grid-inner-locked">';
                    str +=     values.lockedRows;
                    str += '</div>';
                }
                return str;
            },
            printNormalGrid: function(values){
                var str = '';

                if (values.normalGrid){
                    str += '<div id="normalRowsCt" style="left:'+(values.lockedGrid ? values.lockedWidth : '0')+'px;top:'+values.headerHeight+'px;width:'+values.normalWidth+'px" class="sch-print-normal-rows-ct '+values.innerNormalClasses+'">';
                    str +=     '<div style="position:relative;overflow:visible;margin-left:-'+values.normalScroll+'px">'+values.normalRows+'</div>';
                    str += '</div>';
                }
                return str;
            }
          }),

    // private
    getGridContent : function(component) {
        var normalGrid = component.normalGrid,
            lockedGrid = component.lockedGrid,
            lockedView = lockedGrid.getView(),
            normalView = normalGrid.getView(),
            header, lockedRows, normalRows, lockedScroll, normalScroll,
            normalWidth, lockedWidth;

        this.beforePrint(component);

        if (lockedGrid.collapsed && !normalGrid.collapsed){
            normalWidth = lockedGrid.getWidth() + normalGrid.getWidth();
        } else {
            normalWidth = normalGrid.getWidth();
            lockedWidth = lockedGrid.getWidth();
        }

        // Render rows
        var records  = lockedView.store.getRange();
        lockedRows   = lockedView.tpl.apply(lockedView.collectData(records, 0));
        normalRows   = normalView.tpl.apply(normalView.collectData(records, 0));
        lockedScroll = lockedView.el.getScroll().left;
        normalScroll = normalView.el.getScroll().left;

        // Hide hidden columns
        var div = document.createElement('div');
        div.innerHTML = lockedRows;

        if (Ext.versions.extjs.isLessThan('4.2.1')) {
            lockedGrid.headerCt.items.each(function(column, i) {
                if (column.isHidden()) {
                    Ext.fly(div).down('colgroup:nth-child(' + (i+1) + ') col').setWidth(0);
                }
            });
        }

        lockedRows = div.innerHTML;

        // Print additional markup produced by lines plugins, zones plugins etc
        if (Sch.feature && Sch.feature.AbstractTimeSpan) {
            var toIterate = (component.plugins || []).concat(component.normalGrid.plugins || []).concat(component.columnLinesFeature || []);
            Ext.each(toIterate, function(plug) {
                if (plug instanceof Sch.feature.AbstractTimeSpan && plug.generateMarkup) {
                    normalRows = plug.generateMarkup(true) + normalRows;
                }
            });
        }

        this.afterPrint(component);

        return {
            normalHeader       : normalGrid.headerCt.el.dom.innerHTML,
            lockedHeader       : lockedGrid.headerCt.el.dom.innerHTML,
            lockedGrid         : lockedGrid.collapsed ? false : lockedGrid,
            normalGrid         : normalGrid.collapsed ? false : normalGrid,
            lockedRows         : lockedRows,
            normalRows         : normalRows,
            lockedScroll       : lockedScroll,
            normalScroll       : normalScroll,
            lockedWidth        : lockedWidth - (Ext.isWebKit ? 1 : 0),
            normalWidth        : normalWidth,
            headerHeight       : normalGrid.headerCt.getHeight(),
            innerLockedClasses : lockedGrid.view.el.dom.className,
            innerNormalClasses : normalGrid.view.el.dom.className + (this.fakeBackgroundColor ? ' sch-print-fake-background' : ''),
            width              : component.getWidth()
        };
    },

    getStylesheets : function() {
        return Ext.getDoc().select('link[rel="stylesheet"]');
    },

    /**
     * Prints a scheduler panel. This method will be aliased to the main scheduler instance, so you can call it directly:
     *
     *      scheduler.print()
     */
    print : function() {
        var component = this.scheduler;

        if (!(this.mainTpl instanceof Ext.Template)) {
            // Compile the tpl upon first call
            var headerRowHeight = 22;

            this.mainTpl = Ext.create("Ext.XTemplate", this.mainTpl, {
                compiled : true,
                disableFormats : true
            });
        }

        var v = component.getView(),
            styles = this.getStylesheets(),
            ctTmp = Ext.get(Ext.core.DomHelper.createDom({
                tag : 'div'
            })),
            styleFragment;

        styles.each(function(s) {
            ctTmp.appendChild(s.dom.cloneNode(true));
        });

        styleFragment = ctTmp.dom.innerHTML + '';

        var gridContent = this.getGridContent(component),
            html = this.mainTpl.apply(Ext.apply({
                waitText            : this.waitText,
                docType             : this.docType,
                htmlClasses         : Ext.getBody().parent().dom.className,
                bodyClasses         : Ext.getBody().dom.className,
                componentClasses    : component.el.dom.className,
                title               : (component.title || ''),
                styles              : styleFragment,
                totalWidth          : component.getWidth(),
                setupScript         : ("window.onload = function(){ (" + this.setupScript.toString() + ")(" +
                    component.syncRowHeight + ", " + this.autoPrintAndClose + ", " + Ext.isChrome + ", " + Ext.isIE +
                "); };")
            }, gridContent));

        // Assign to this for testability, need a reference to the opened window
        var win = window.open('', 'printgrid');
        this.printWindow = win;
        win.document.write(html);
        win.document.close();
    },

    // Script executed in the newly open window, to sync row heights
    setupScript : function (syncRowHeight, autoPrintAndClose, isChrome, isIE) {
        var syncHeightAndPrint  = function () {
            if (syncRowHeight) {
                var lockedTableCt = document.getElementById('lockedRowsCt'),
                    normalTableCt = document.getElementById('normalRowsCt'),

                    //checks added in case of hidden/collapsed grids
                    lockedRows = lockedTableCt && lockedTableCt.getElementsByTagName('tr'),
                    normalRows = normalTableCt && normalTableCt.getElementsByTagName('tr'),
                    count      = normalRows && lockedRows ? normalRows.length : 0;

                for (var i = 0; i < count; i++) {
                    var normalHeight    = normalRows[ i ].clientHeight;
                    var lockedHeight    = lockedRows[ i ].clientHeight;

                    var max             = Math.max(normalHeight, lockedHeight) + 'px';

                    lockedRows[ i ].style.height = normalRows[ i ].style.height = max;
                }
            }

            if (autoPrintAndClose) {
                window.print();
                // Chrome cannot print the page if you close the window being printed
                if (!isChrome) {
                    window.close();
                }
            }
        };

        if (isIE)
            // TODO: probably we don't need this anymore, as we now use window.onload to call setupScript
            setTimeout(syncHeightAndPrint, 0);
        else
            syncHeightAndPrint();
    }
});
/**
@class Sch.plugin.Export
@extends Ext.util.Observable

A plugin (ptype = 'scheduler_export') for generating PDF/PNG out of a Scheduler panel. NOTE: This plugin will make an Ajax request to the server, POSTing
 the HTML to be exported. The {@link #printServer} url must therefore be on the same domain as your application.

#Configuring/usage

To use this plugin, add it to your scheduler as any other plugin. It is also required to have [PhantomJS][1] and [Imagemagick][2]
installed on the server. The complete process of setting up a backend for this plugin can be found in the readme file inside export examples
as well as on our [blog][3]. Note that export is currently not supported if your view (or store) is buffered.

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                Ext.create('Sch.plugin.Export', {
                    // default values
                    printServer: 'server.php'
                })
            ]
        });

Scheduler will be extended with three new methods:

* {@link #setFileFormat}, which allows setting the format to which panel should be exported. Default format is `pdf`.

* {@link #showExportDialog}, which shows export settings dialog

        scheduler.showExportDialog();

* {@link #doExport} which actually performs the export operation using {@link #defaultConfig} or provided config object :

        scheduler.doExport(
            {
                format: "A5",
                orientation: "landscape",
                range: "complete",
                showHeader: true,
                singlePageExport: false
            }
        );

#Export options

In the current state, plugin gives few options to modify the look and feel of the generated PDF document throught a dialog window :

{@img scheduler/images/export_dialog.png}

If no changes are made to the form, the {@link #defaultConfig} will be used.

##Export Range

This setting controls the timespan visible on the exported document. Three options are available here :

{@img scheduler/images/export_dialog_ranges.png}

###Complete schedule

Whole current timespan will be visible on the exported document.

###Date range

User can select the start and end dates (from the total timespan of the panel) visible on the exported document.

{@img scheduler/images/export_dialog_ranges_date.png}

###Current view

Timespan of the exported document/image will be set to the currently visible part of the time axis. User can control
the width of the time column and height of row.

{@img scheduler/images/export_dialog_ranges_current.png}

##Paper Format

This combo gives control of the size of the generated document/image by choosing one from a list of supported ISO paper sizes : (`A5`, `A4`, `A3`, `Letter`).
Generated PDF has a fixed DPI value of 72. Dafault format is `A4`.

{@img scheduler/images/export_dialog_format.png}

##Orientation

This setting defines the orientation of the generated document/image.

{@img scheduler/images/export_dialog_orientation.png}

Default option is the `portrait` (horizontal) orientation :

{@img scheduler/images/export_dialog_portrait.png}

Second option is the `landscape` (vertical) orientation :

{@img scheduler/images/export_dialog_landscape.png}

[1]: http://www.phantomjs.org
[2]: http://www.imagemagick.org
[3]: http://bryntum.com/blog

*/
Ext.define('Sch.plugin.Export', {
    extend                  : 'Ext.util.Observable',

    alternateClassName      : 'Sch.plugin.PdfExport',
    alias                   : 'plugin.scheduler_export',

    mixins                  : ['Ext.AbstractPlugin'],

    requires        : [
        'Ext.XTemplate'
    ],

    lockableScope           : 'top',

    /**
    * @cfg {String}
    * URL of the server responsible for running the export steps.
    */
    printServer             : undefined,

    //private template for the temporary export html page
    tpl                     : null,

    /**
    * @cfg {String}
    * Class name of the dialog used to change export settings.
    */
    exportDialogClassName   : 'Sch.widget.ExportDialog',

    /**
    * @cfg {Object}
    * Config object for the {@link #exportDialogClass}. Use this to override default values for the export dialog.
    */
    exportDialogConfig      : {},

    /**
    * @cfg {Object}
    * Default export configuration.
    */
    defaultConfig           : {
        format              : "A4",
        orientation         : "portrait",
        range               : "complete",
        showHeader          : true,
        singlePageExport    : false
    },

    /**
     * @cfg {Boolean} expandAllBeforeExport Only applicable for tree views, set to true to do a full expand prior to the export. Defaults to false.
     */
    expandAllBeforeExport   : false,

    /**
    * @private
    * @cfg {Object}
    * Predefined paper sizes in inches for different formats, as defined by ISO standards.
    */
    pageSizes               : {
        A5      : {
            width   : 5.8,
            height  : 8.3
        },
        A4      : {
            width   : 8.3,
            height  : 11.7
        },
        A3      : {
            width   : 11.7,
            height  : 16.5
        },
        Letter  : {
            width   : 8.5,
            height  : 11
        },
        Legal   : {
            width   : 8.5,
            height  : 14
        }
    },

    /**
    * @cfg {Boolean}
    * If set to true, open new window with the generated document after the operation has finished.
    */
    openAfterExport         : true,

    /**
     * An empty function by default, but provided so that you can perform a custom action
     * before the export plugin extracts data from the scheduler.
     * @param {SchedulerPanel} scheduler The scheduler instance
     * @param {Array[Object]} ticks The ticks gathered by plugin to export.
     * @method beforeExport
     */
    beforeExport            : Ext.emptyFn,

    /**
     * An empty function by default, but provided so that you can perform a custom action
     * after the export plugin has extracted the data from the scheduler.
     * @param {SchedulerPanel} scheduler The scheduler instance
     * @method afterExport
     */
    afterExport             : Ext.emptyFn,

    /**
    * @cfg {String}
    * Format of the exported file, selectable from `pdf` or `png`. By default plugin exports panel contents to PDF
    * but PNG file format is also available.
    */
    fileFormat              : 'pdf',

    //private Constant DPI value for generated PDF
    DPI                     : 72,

    constructor : function (config) {
        config = config || {};

        if (config.exportDialogConfig) {
            Ext.Object.each(this.defaultConfig, function(k, v, o){
                var configK = config.exportDialogConfig[k];
                if (configK) {
                    o[k] = configK;
                }
            });
        }

        this.callParent([ config ]);

        if (!this.tpl) {
            this.tpl = new Ext.XTemplate('<!DOCTYPE html>' +
                '<html class="' + Ext.baseCSSPrefix + 'border-box {htmlClasses}">' +
                    '<head>' +
                        '<meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />' +
                        '<title>{column}/{row}</title>' +
                        '{styles}' +
                    '</head>' +
                    '<body class="' + Ext.baseCSSPrefix + 'webkit sch-export {bodyClasses}">' +
                        '<tpl if="showHeader">' +
                            '<div class="sch-export-header" style="width:{totalWidth}px"><h2>{column}/{row}</h2></div>' +
                        '</tpl>' +
                        '<div class="{componentClasses}" style="height:{bodyHeight}px; width:{totalWidth}px; position: relative !important">' +
                            '{HTML}' +
                        '</div>' +
                    '</body>' +
                '</html>',
                {
                    disableFormats: true
                }
            );
        }

        this.addEvents(
            /**
             * @event hidedialogwindow
             * Fires to hide the dialog window.
             */
            'hidedialogwindow',

            /**
             * @event showdialogerror
             * Fires to show error in the dialog window.
             * @param {String} message Error message to show in the dialog window.
             */
            'showdialogerror',

            /**
             * @event updateprogressbar
             * Fires when a progressbar of the {@link #exportDialogClassName dialog} should update it's value.
             * @param {Number} value Value (between 0 and 1) to set on the progressbar.
             */
            'updateprogressbar'
        );

        this.setFileFormat(this.fileFormat);
    },

    init : function (scheduler) {
        this.scheduler = scheduler;

        scheduler.showExportDialog = Ext.Function.bind(this.showExportDialog, this);
        scheduler.doExport         = Ext.Function.bind(this.doExport, this);
    },

    /**
    * Function for setting the {@link #fileFormat} of exporting panel. Can be either `pdf` or `png`.
    *
    * @param {String} format format of the file to set. Can take either `pdf` or `png`.
    */
    setFileFormat : function (format) {
        if (typeof format !== 'string') {
            this.fileFormat = 'pdf';
        } else {
            format = format.toLowerCase();

            if (format === 'png') {
                this.fileFormat = format;
            } else {
                this.fileFormat = 'pdf';
            }
        }
    },

    /**
    * Instantiates and shows a new {@link #exportDialogClassName} class using {@link #exportDialogConfig} config.
    * This popup should give user possibility to change export settings.
    */
    showExportDialog : function() {
        var me   = this,
            view = me.scheduler.getSchedulingView();

        //dialog window is always removed to avoid resetting its layout after hiding
        if (me.win) {
            me.win.destroy();
            me.win = null;
        }

        me.win  = Ext.create(me.exportDialogClassName, {
            plugin                  : me,
            exportDialogConfig      : Ext.apply({
                startDate       : me.scheduler.getStart(),
                endDate         : me.scheduler.getEnd(),
                rowHeight       : view.rowHeight,
                columnWidth     : view.timeAxisViewModel.getTickWidth(),
                defaultConfig   : me.defaultConfig
            }, me.exportDialogConfig)
        });

        me.saveRestoreData();

        me.win.show();
    },

    /*
    * @private
    * Save values to restore panel after exporting
    */
    saveRestoreData : function() {
        var component  = this.scheduler,
            view       = component.getSchedulingView(),
            normalGrid = component.normalGrid,
            lockedGrid = component.lockedGrid;

        //values needed to restore original size/dates of panel
        this.restoreSettings = {
            width           : component.getWidth(),
            height          : component.getHeight(),
            rowHeight       : view.rowHeight,
            columnWidth     : view.timeAxisViewModel.getTickWidth(),
            startDate       : component.getStart(),
            endDate         : component.getEnd(),
            normalWidth     : normalGrid.getWidth(),
            normalLeft      : normalGrid.getEl().getStyle('left'),
            lockedWidth     : lockedGrid.getWidth(),
            lockedCollapse  : lockedGrid.collapsed,
            normalCollapse  : normalGrid.collapsed
        };
    },

    /*
    * @private
    * Get links to the stylesheets of current page.
    */
    getStylesheets : function() {
        var styleSheets = Ext.getDoc().select('link[rel="stylesheet"]'),
            ctTmp = Ext.get(Ext.core.DomHelper.createDom({
                tag : 'div'
            })),
            stylesString;

        styleSheets.each(function(s) {
            ctTmp.appendChild(s.dom.cloneNode(true));
        });

        stylesString = ctTmp.dom.innerHTML + '';

        return stylesString;
    },

    /**
    * Function performing the export operation using config from arguments or default {@link #defaultConfig config}. After getting data
    * from the scheduler an XHR request to {@link #printServer} will be made with the following JSON encoded data :
    *
    * * `html` {Array}         - array of html strings containing data of each page
    * * `format` {String}      - paper size of the exported file
    * * `orientation` {String} - orientation of the exported file
    * * `range`       {String} - range of the exported file
    * * `fileFormat`  {String} - file format of the exported file
    *
    * @param {Object} [conf] Config options for exporting. If not provided, {@link #defaultConfig} is used.
    * Possible parameters are :
    *
    * * `format` {String}            - format of the exported document/image, selectable from the {@link #pageSizes} list.
    * * `orientation` {String}       - orientation of the exported document/image. Either `portrait` or `landscape`.
    * * `range` {String}             - range of the panel to be exported. Selectable from `complete`, `current`, `date`.
    * * `showHeader` {Boolean}       - boolean value defining if exported pages should have row/column numbers added in the headers.
    * * `singlePageExport` {Boolean} - boolean value defining if exported file should be divided into separate pages or not
    *
    * @param {Function} [callback] Optional function that will be called after successful response from export backend script.
    * @param {Function} [errback] Optional function that will be called if export backend script returns error.
    */
    doExport : function (conf, callback, errback) {
        // put mask on the panel
        this.mask();

        var me           = this,
            component    = me.scheduler,
            view         = component.getSchedulingView(),
            styles       = me.getStylesheets(),
            config       = conf || me.defaultConfig,
            normalGrid   = component.normalGrid,
            lockedGrid   = component.lockedGrid,
            headerHeight = normalGrid.headerCt.getHeight();

        // keep scheduler state to restore after export
        me.saveRestoreData();

        //expand grids in case they're collapsed
        normalGrid.expand();
        lockedGrid.expand();

        me.fireEvent('updateprogressbar', 0.1);

        // For Tree grid, optionally expand all nodes
        if (this.expandAllBeforeExport && component.expandAll) {
            component.expandAll();
        }

        var ticks           = component.timeAxis.getTicks(),
            timeColumnWidth = view.timeAxisViewModel.getTickWidth(),
            paperWidth,
            printHeight,
            paperHeight;

        //check if we're not exporting to single image as those calculations are not needed in this case
        if (!config.singlePageExport) {
            //size of paper we will be printing on. 72 DPI used by phantomJs generator
            //take orientation into account
            if (config.orientation === 'landscape') {
                paperWidth     = me.pageSizes[config.format].height*me.DPI;
                paperHeight    = me.pageSizes[config.format].width*me.DPI;
            } else {
                paperWidth     = me.pageSizes[config.format].width*me.DPI;
                paperHeight    = me.pageSizes[config.format].height*me.DPI;
            }

            var pageHeaderHeight = 41;

            printHeight = Math.floor(paperHeight) - headerHeight - (config.showHeader ? pageHeaderHeight : 0);
        }

        view.timeAxisViewModel.suppressFit = true;

        var skippedColsBefore   = 0;
        var skippedColsAfter    = 0;

        // if we export a part of scheduler
        if (config.range !== 'complete') {
            var newStart, newEnd;

            switch (config.range) {
                case 'date' :
                    newStart    = new Date(config.dateFrom);
                    newEnd      = new Date(config.dateTo);

                    // ensure that specified period has at least a day
                    if (Sch.util.Date.getDurationInDays(newStart, newEnd) < 1) {
                        newEnd  = Sch.util.Date.add(newEnd, Sch.util.Date.DAY, 1);
                    }

                    newStart    = Sch.util.Date.constrain(newStart, component.getStart(), component.getEnd());
                    newEnd      = Sch.util.Date.constrain(newEnd, component.getStart(), component.getEnd());
                    break;

                case 'current' :
                    var visibleSpan = view.getVisibleDateRange();
                    newStart        = visibleSpan.startDate;
                    newEnd          = visibleSpan.endDate || view.timeAxis.getEnd();

                    if (config.cellSize) {
                        // will change columns wiidth to provided value
                        timeColumnWidth = config.cellSize[0];

                        // change the row height only if value is provided
                        if (config.cellSize.length > 1) {
                            view.setRowHeight(config.cellSize[1]);
                        }
                    }
                    break;
            }

            // set specified time frame
            component.setTimeSpan(newStart, newEnd);

            var startTick   = Math.floor(view.timeAxis.getTickFromDate(newStart));
            var endTick     = Math.floor(view.timeAxis.getTickFromDate(newEnd));

            ticks       = component.timeAxis.getTicks();
            // filter only needed ticks
            ticks       = Ext.Array.filter(ticks, function (tick, index) {
                if (index < startTick) {
                    skippedColsBefore++;
                    return false;
                } else if (index > endTick) {
                    skippedColsAfter++;
                    return false;
                }
                return true;
            });
        }

        // run template method
        this.beforeExport(component, ticks);

        var format, htmlArray, calculatedPages;

        // multiple pages mode
        if (!config.singlePageExport) {

            component.setWidth(paperWidth);
            component.setTimeColumnWidth(timeColumnWidth);
            view.timeAxisViewModel.setTickWidth(timeColumnWidth);

            //calculate amount of pages in the document
            calculatedPages = me.calculatePages(config, ticks, timeColumnWidth, paperWidth, printHeight);

            htmlArray       = me.getExportJsonHtml(calculatedPages, {
                styles              : styles,
                config              : config,
                ticks               : ticks,
                skippedColsBefore   : skippedColsBefore,
                skippedColsAfter    : skippedColsAfter,
                printHeight         : printHeight,
                paperWidth          : paperWidth,
                headerHeight        : headerHeight
            });

            format          = config.format;

        // single page mode
        } else {

            htmlArray           = me.getExportJsonHtml(null, {
                styles              : styles,
                config              : config,
                ticks               : ticks,
                skippedColsBefore   : skippedColsBefore,
                skippedColsAfter    : skippedColsAfter,
                timeColumnWidth     : timeColumnWidth
            });

            var sizeInInches    = me.getRealSize(),
                width           = Ext.Number.toFixed(sizeInInches.width / me.DPI, 1),
                height          = Ext.Number.toFixed(sizeInInches.height / me.DPI, 1);

            format = width+'in*'+height+'in';
        }

        //further update progress bar
        me.fireEvent('updateprogressbar', 0.4);

        if (me.printServer) {

            // if it's not debugging or test environment
            if (!me.debug && !me.test) {
                Ext.Ajax.request({
                    type    : 'POST',
                    url     : me.printServer,
                    timeout : 60000,
                    params  : Ext.apply({
                        html        : {
                            array   : htmlArray
                        },
                        startDate   : component.getStartDate(),
                        endDate     : component.getEndDate(),
                        format      : format,
                        orientation : config.orientation,
                        range       : config.range,
                        fileFormat  : me.fileFormat
                    }, this.getParameters()),
                    success : function(response) {
                        me.onSuccess(response, callback, errback);
                    },
                    failure : function(response) {
                        me.onFailure(response, errback);
                    },
                    scope   : me
                });

            // for debugging mode we just show output instead of sending it to server
            } else if (me.debug) {

                var w, a = Ext.JSON.decode(htmlArray);
                for (var i = 0, l = a.length; i < l; i++) {
                    w = window.open();
                    w.document.write(a[i].html);
                    w.document.close();
                }

            }

        } else {
            throw 'Print server URL is not defined, please specify printServer config';
        }

        view.timeAxisViewModel.suppressFit = false;

        // restore scheduler state
        me.restorePanel();

        // run template method
        this.afterExport(component);

        // for test environment we return export results
        if (me.test) {
            return {
                htmlArray       : Ext.JSON.decode(htmlArray),
                calculatedPages : calculatedPages
            };
        }
    },
    
    /**
     * This method should return an object, that will be applied to the JSON data passed to the export XHR request.
     * By default this method returns empty object, it is supposed to be overriden in the subclass to provide some extra custom data.
     * 
     * @return {Object}
     */
    getParameters : function () {
        return {};
    },

    /*
    * @private
    * Function returning full width and height of both grids.
    *
    * @return {Object} values Object containing width and height properties.
    */
    getRealSize : function() {
        var component    = this.scheduler,
            headerHeight = component.normalGrid.headerCt.getHeight(),
            height       = (headerHeight + component.lockedGrid.getView().getEl().down('.' + Ext.baseCSSPrefix + 'grid-table').getHeight()),
            width        =  (component.lockedGrid.headerCt.getEl().first().getWidth() +
                component.normalGrid.body.select('.' + Ext.baseCSSPrefix + 'grid-table').first().getWidth());

        return {
            width   : width,
            height  : height
        };
    },

    /*
    * @private
    * Function calculating amount of pages in vertical/horizontal direction in the exported document/image.
    *
    * @param {Array} ticks Ticks from the TickStore.
    * @param {Int} timeColumnWidth Width of a single time column.
    * @return {Object} valuesObject Object containing calculated amount of pages, rows and columns.
    */
    calculatePages : function (config, ticks, timeColumnWidth, paperWidth, printHeight) {
        var me                  = this,
            component           = me.scheduler,
            lockedGrid          = component.lockedGrid,
            rowHeight           = component.getSchedulingView().rowHeight,
            lockedHeader        = lockedGrid.headerCt,
            lockedGridWidth     = lockedHeader.getEl().first().getWidth(),
            lockedColumnPages   = null,
            //amount of columns with locked grid visible
            columnsAmountLocked = 0;

        if (lockedGridWidth > lockedGrid.getWidth()) {
            var startCursor   = 0,
                endCursor     = 0,
                width         = 0,
                addItem       = false,
                columnWidth;

            lockedColumnPages = [];

            lockedGrid.headerCt.items.each(function(column, idx, len) {
                columnWidth = column.width;

                if (!width || width + columnWidth < paperWidth) {
                    width += columnWidth;
                    if (idx === len -1) {
                        addItem = true;

                        //we still need to check if any time columns fit
                        var widthLeft = paperWidth - width;
                        columnsAmountLocked = Math.floor(widthLeft / timeColumnWidth);
                    }
                } else {
                    addItem = true;
                }

                if (addItem) {
                    endCursor = idx;

                    lockedColumnPages.push({
                        firstColumnIdx    : startCursor,
                        lastColumnIdx     : endCursor,
                        totalColumnsWidth : width || columnWidth
                    });

                    startCursor = endCursor + 1;
                    width       = 0;
                }
            });
        } else {
            columnsAmountLocked = Math.floor((paperWidth - lockedGridWidth) / timeColumnWidth);
        }

        //amount of columns without locked grid visible
        var columnsAmountNormal = Math.floor(paperWidth / timeColumnWidth),
            //amount of pages horizontally
            columnPages         = Math.ceil((ticks.length - columnsAmountLocked) / columnsAmountNormal),
            rowsAmount          = Math.floor(printHeight/rowHeight);

        if (!lockedColumnPages || columnPages === 0) {
            columnPages += 1;
        }

        return {
            columnsAmountLocked : columnsAmountLocked,
            columnsAmountNormal : columnsAmountNormal,
            lockedColumnPages   : lockedColumnPages,
            rowsAmount          : rowsAmount,
            //amount of pages vertically
            rowPages            : Math.ceil(component.getSchedulingView().store.getCount()/rowsAmount),
            columnPages         : columnPages,
            timeColumnWidth     : timeColumnWidth,
            lockedGridWidth     : lockedGridWidth,
            rowHeight           : rowHeight,
            panelHTML           : {}
        };
    },

    /*
    * @private
    * Method exporting panel's HTML to JSON structure. This function is taking snapshots of the visible panel (by changing timespan
    * and hiding rows) and pushing their html to an array, which is then encoded to JSON.
    *
    * @param {Object} calculatedPages Object with values returned from {@link #calculatePages}.
    * @param {Object} params Object with additional properties needed for calculations.
    *
    * @return {Array} htmlArray JSON string created from an array of objects with stringified html.
    */
    getExportJsonHtml : function (calculatedPages, params) {
        var me                  = this,
            component           = me.scheduler,
            htmlArray           = [],

            //Remove any non-webkit browser-specific css classes
            re                  = new RegExp(Ext.baseCSSPrefix + 'ie\\d?|' + Ext.baseCSSPrefix + 'gecko', 'g'),
            bodyClasses         = Ext.getBody().dom.className.replace(re, ''),
            componentClasses    = component.el.dom.className,
            styles              = params.styles,
            config              = params.config,
            ticks               = params.ticks,
            panelHTML, readyHTML, htmlObject, html,
            timeColumnWidth;

        //Hack for IE
        if (Ext.isIE) {
            bodyClasses += ' sch-ie-export';
        }

        //we need to prevent Scheduler from auto adjusting the timespan
        component.timeAxis.autoAdjust = false;

        if (!config.singlePageExport) {
            var columnsAmountLocked = calculatedPages.columnsAmountLocked,
                columnsAmountNormal = calculatedPages.columnsAmountNormal,
                lockedColumnPages   = calculatedPages.lockedColumnPages,
                rowsAmount          = calculatedPages.rowsAmount,
                rowPages            = calculatedPages.rowPages,
                columnPages         = calculatedPages.columnPages,
                paperWidth          = params.paperWidth,
                printHeight         = params.printHeight,
                headerHeight        = params.headerHeight,
                lastColumn          = null,
                columns, lockedColumnPagesLen;

            timeColumnWidth = calculatedPages.timeColumnWidth;
            panelHTML       = calculatedPages.panelHTML;

            panelHTML.skippedColsBefore = params.skippedColsBefore;
            panelHTML.skippedColsAfter  = params.skippedColsAfter;

            if (lockedColumnPages) {
                lockedColumnPagesLen = lockedColumnPages.length;
                columnPages         += lockedColumnPagesLen;
            }

            //horizontal pages
            for (var i = 0; i < columnPages; i++) {

                //set visible time range to corresponding ticks
                if (lockedColumnPages && i < lockedColumnPagesLen) {
                    if (i === lockedColumnPagesLen - 1 && columnsAmountLocked !== 0) {
                        component.normalGrid.show();
                        lastColumn = Ext.Number.constrain((columnsAmountLocked-1), 0, (ticks.length - 1));
                        component.setTimeSpan(ticks[0].start, ticks[lastColumn].end);
                    } else {
                        component.normalGrid.hide();
                    }
                    var visibleColumns = lockedColumnPages[i];

                    this.showLockedColumns();
                    this.hideLockedColumns(visibleColumns.firstColumnIdx, visibleColumns.lastColumnIdx);

                    //resize lockedGrid to width of visible columns + 1px of border
                    component.lockedGrid.setWidth(visibleColumns.totalColumnsWidth+1);
                } else {

                    if (i === 0) {
                        this.showLockedColumns();

                        if (columnsAmountLocked !== 0) {
                            component.normalGrid.show();
                        }

                        lastColumn = Ext.Number.constrain(columnsAmountLocked - 1, 0, ticks.length - 1);
                        component.setTimeSpan(ticks[0].start, ticks[lastColumn].end);
                    } else {
                        //hide locked grid
                        component.lockedGrid.hide();

                        component.normalGrid.show();

                        if (lastColumn === null) {
                            //set lastColumn to -1 as it'll be incremented by 1, and in this case
                            //we want to start from 0
                            lastColumn = -1;
                        }

                        if (ticks[lastColumn+columnsAmountNormal]){
                            component.setTimeSpan(ticks[lastColumn+1].start, ticks[lastColumn+columnsAmountNormal].end);
                            lastColumn = lastColumn+columnsAmountNormal;
                        } else {
                            component.setTimeSpan(ticks[lastColumn+1].start, ticks[ticks.length-1].end);
                        }
                    }
                }

                //changing timespan resets column width
                component.setTimeColumnWidth(timeColumnWidth, true);
                component.getSchedulingView().timeAxisViewModel.setTickWidth(timeColumnWidth);

                //vertical pages
                for (var k = 0; k < rowPages; k+=1) {

                    //hide rows that are not supposed to be visible on the current page
                    me.hideRows(rowsAmount, k);

                    panelHTML.dom   = component.body.dom.innerHTML;
                    panelHTML.k     = k;
                    panelHTML.i     = i;

                    readyHTML       = me.resizePanelHTML(panelHTML);

                    html            = me.tpl.apply(Ext.apply({
                        bodyClasses      : bodyClasses,
                        bodyHeight       : printHeight + headerHeight,
                        componentClasses : componentClasses,
                        styles           : styles,
                        showHeader       : config.showHeader,
                        HTML             : readyHTML.dom.innerHTML,
                        totalWidth       : paperWidth,
                        headerHeight     : headerHeight,
                        column           : i+1,
                        row              : k+1
                    }));

                    htmlObject = {'html': html};
                    htmlArray.push(htmlObject);

                    //unhide all rows
                    me.showRows();
                }
            }

        } else {
            timeColumnWidth = params.timeColumnWidth;
            panelHTML = calculatedPages ? calculatedPages.panelHTML : {};

            component.setTimeSpan(ticks[0].start, ticks[ticks.length-1].end);
            component.lockedGrid.setWidth(component.lockedGrid.headerCt.getEl().first().getWidth());
            component.setTimeColumnWidth(timeColumnWidth);
            component.getSchedulingView().timeAxisViewModel.setTickWidth(timeColumnWidth);

            var realSize  = me.getRealSize();

            Ext.apply(panelHTML, {
                dom                 : component.body.dom.innerHTML,
                column              : 1,
                row                 : 1,
                timeColumnWidth     : params.timeColumnWidth,
                skippedColsBefore   : params.skippedColsBefore,
                skippedColsAfter    : params.skippedColsAfter
            });

            readyHTML   = me.resizePanelHTML(panelHTML);

            html        = me.tpl.apply(Ext.apply({
                bodyClasses      : bodyClasses,
                bodyHeight       : realSize.height,
                componentClasses : componentClasses,
                styles           : styles,
                showHeader       : false,
                HTML             : readyHTML.dom.innerHTML,
                totalWidth       : realSize.width
            }));

            htmlObject = {'html': html};
            htmlArray.push(htmlObject);
        }

        component.timeAxis.autoAdjust = true;

        return Ext.JSON.encode(htmlArray);
    },

    /*
    * @private
    * Resizes panel elements to fit on the print page. This has to be done manually in case of wrapping Scheduler
    * inside another, smaller component.
    *
    * @param {Object} HTML Object with html of panel, and row & column number.
    *
    * @return {Object} frag Ext.dom.Element with resized html.
    */
    resizePanelHTML: function (HTML) {
        //create empty div that will temporarily hold our panel current HTML
        var frag       = Ext.get(Ext.core.DomHelper.createDom({
                tag: 'div',
                html: HTML.dom
            })),
            component  = this.scheduler,
            lockedGrid = component.lockedGrid,
            normalGrid = component.normalGrid,
            lockedEl,
            lockedElements,
            normalElements;

        //HACK for resizing in IE6/7 and Quirks mode. Requires operating on a document fragment with DOM methods
        //instead of using unattached div and Ext methods.
        if (Ext.isIE6 || Ext.isIE7 || Ext.isIEQuirks){
            var dFrag = document.createDocumentFragment(),
                method, selector;

            //IE removed getElementById from documentFragment in later browsers
            if (dFrag.getElementById){
                method   = 'getElementById';
                selector = '';
            } else {
                method = 'querySelector';
                selector = '#';
            }

            dFrag.appendChild(frag.dom);

            lockedEl = lockedGrid.view.el;

            lockedElements = [
                dFrag[method](selector+component.id+'-targetEl'),
                dFrag[method](selector+component.id+'-innerCt'),
                dFrag[method](selector+lockedGrid.id),
                dFrag[method](selector+lockedGrid.body.id),
                dFrag[method](selector+lockedEl.id)
            ];
            normalElements = [
                dFrag[method](selector+normalGrid.id),
                dFrag[method](selector+normalGrid.headerCt.id),
                dFrag[method](selector+normalGrid.body.id),
                dFrag[method](selector+normalGrid.getView().id)
            ];

            Ext.Array.each(lockedElements, function(el){
                if(el !== null){
                    el.style.height = '100%';
                    el.style.width  = '100%';
                }
            });

            Ext.Array.each(normalElements, function(el, idx){
                if (el !== null){
                    if (idx === 1){
                        el.style.width = '100%';
                    } else {
                        el.style.height = '100%';
                        el.style.width  = '100%';
                    }
                }
            });

            frag.dom.innerHTML = dFrag.firstChild.innerHTML;
        } else {
            //this wasn't needed in real life, only for tests under 4.2 to pass
            lockedEl = lockedGrid.view.el;

            lockedElements = [
                frag.select('#'+component.id+'-targetEl').first(),
                frag.select('#'+component.id+'-innerCt').first(),
                frag.select('#'+lockedGrid.id).first(),
                frag.select('#'+lockedGrid.body.id).first(),
                frag.select('#'+lockedEl.id)
            ];
            normalElements = [
                frag.select('#'+normalGrid.id).first(),
                frag.select('#'+normalGrid.headerCt.id).first(),
                frag.select('#'+normalGrid.body.id).first(),
                frag.select('#'+normalGrid.getView().id).first()
            ];

            Ext.Array.each(lockedElements, function(el, idx){
                if(el){
                    el.setHeight('100%');
                    if (idx !== 3 && idx !== 2 ) {
                        el.setWidth('100%');
                    }
                }
            });

            Ext.Array.each(normalElements, function(el, idx){
                //don't change height of the header, just width
                if (idx === 1){
                    el.setWidth('100%');
                } else {
                    el.applyStyles({
                        height: '100%',
                        width: '100%'
                    });
                }
            });
        }

        return frag;
    },

    //Private used to prevent using old reference in the response callbacks
    getWin : function () {
        return this.win || null;
    },

    //Private.
    onSuccess : function (response, callback, errback) {
        var me  = this,
            win = me.getWin(),
            result;

        try {
            result = Ext.JSON.decode(response.responseText);
        } catch (e) {
            this.onFailure(response, errback);
            return;
        }

        //set progress to 100%
        me.fireEvent('updateprogressbar', 1);

        if (result.success) {
            //close print widget
            setTimeout(function() {

                //fire event for hiding window
                me.fireEvent('hidedialogwindow');
                me.unmask();

                if (me.openAfterExport) {
                    window.open(result.url, 'ExportedPanel');
                }
            }, win ? win.hideTime : 3000);
        } else {
            //show error message in print widget window
            me.fireEvent('showdialogerror', win, result.msg);
            me.unmask();
        }

        if (callback) {
            callback.call(this, response);
        }
    },

    //Private.
    onFailure : function (response, errback) {
        var win = this.getWin(),                     // Not JSON           // Decoded JSON ok
            msg = response.status === 200 ? response.responseText : response.statusText;

        this.fireEvent('showdialogerror', win, msg);
        this.unmask();

        if (errback) {
            errback.call(this, response);
        }
    },

    /*
    * @private
    * Hide rows from the panel that are not needed on current export page by adding css class to them.
    *
    * @param {Int} rowsAmount Amount of rows to be hidden.
    * @param {Int} page Current page number.
    */
    hideRows : function (rowsAmount, page) {
        var lockedRows = this.scheduler.lockedGrid.view.getNodes(),
            normalRows = this.scheduler.normalGrid.view.getNodes(),
            start      = rowsAmount * page,
            end        = start + rowsAmount;

        for (var i = 0, l = normalRows.length; i < l; i++) {
            if (i < start || i >= end) {
                lockedRows[i].className += ' sch-none';
                normalRows[i].className += ' sch-none';
            }
        }
    },

    /*
    * @private
    * Unhide all rows of the panel by removing the previously added css class from them.
    */
    showRows : function () {
        this.scheduler.getEl().select(this.scheduler.getSchedulingView().getItemSelector()).each(function(el){
            el.removeCls('sch-none');
        });
    },

    hideLockedColumns : function (startColumn, endColumn) {
        var lockedColumns = this.scheduler.lockedGrid.headerCt.items.items;

        for (var i = 0, l = lockedColumns.length; i < l; i++) {
            if (i < startColumn || i > endColumn) {
                lockedColumns[i].hide();
            }
        }
    },

    showLockedColumns : function () {
        this.scheduler.lockedGrid.headerCt.items.each(function(column){
            column.show();
        });
    },

    /*
    * @private
    * Mask the body, hiding panel to allow changing it's parameters in the background.
    */
    mask : function () {
        var mask = Ext.getBody().mask();
        mask.addCls('sch-export-mask');
    },

    //Private.
    unmask : function () {
        Ext.getBody().unmask();
    },

    /*
    * @private
    * Restore panel to pre-export state.
    */
    restorePanel : function () {
        var s      = this.scheduler,
            config = this.restoreSettings;

        s.setWidth(config.width);
        s.setHeight(config.height);
        s.setTimeSpan(config.startDate, config.endDate);
        s.setTimeColumnWidth(config.columnWidth, true);
        s.getSchedulingView().setRowHeight(config.rowHeight);
        s.lockedGrid.show();
        s.normalGrid.setWidth(config.normalWidth);
        s.normalGrid.getEl().setStyle('left', config.normalLeft);
        s.lockedGrid.setWidth(config.lockedWidth);

        if (config.lockedCollapse) {
            s.lockedGrid.collapse();
        }
        if (config.normalCollapse) {
            s.normalGrid.collapse();
        }
    },

    destroy : function () {
        if (this.win) {
            this.win.destroy();
        }
    }
});

/**
@class Sch.plugin.Lines
@extends Sch.feature.AbstractTimeSpan

Plugin (ptype = 'scheduler_lines') for showing "global" time lines in the scheduler grid. It uses a store to populate itself, records in this store should have the following fields:

- `Date` The date of the line. This date is formatted based on what's configured in the {@link Sch.preset.ViewPreset#displayDateFormat} option of the current "viewPreset".
- `Text` The Text to show when hovering over the line (optional)
- `Cls`  A CSS class to add to the line (optional)

To add this plugin to scheduler:

        var dayStore    = new Ext.data.Store({
            fields  : [ 'Date', 'Text', 'Cls' ],

            data    : [
                {
                    Date        : new Date(2011, 06, 19),
                    Text        : 'Some important day'
                }
            ]
        });


        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                Ext.create('Sch.plugin.Lines', { store : dayStore })
            ]
        });


*/
Ext.define("Sch.plugin.Lines", {
    extend      : "Sch.feature.AbstractTimeSpan",
    alias       : 'plugin.scheduler_lines',

    cls : 'sch-timeline',

    /**
     * @cfg {Boolean} showTip 'true' to include a native browser tooltip when hovering over the line.
     */
    showTip : true,

    /**
     * @cfg {String/Ext.XTemplate} innerTpl A template providing additional markup to render into each timespan element
     */
    innerTpl            : null,
    

    prepareTemplateData : null,
    side : null,

    init : function(scheduler) {
        if (Ext.isString(this.innerTpl)) {
            this.innerTpl = new Ext.XTemplate(this.innerTpl);
        }

        this.side = scheduler.rtl ? 'right' : 'left';

        var innerTpl = this.innerTpl;

        if (!this.template) {
            this.template = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div id="{id}" ' + (this.showTip ? 'title="{[this.getTipText(values)]}" ' : '') + 'class="{$cls}" style="' + this.side + ':{left}px;top:{top}px;height:{height}px;width:{width}px">' +
                    (innerTpl ? '{[this.renderInner(values)]}' : '') +
                    '</div>',
                '</tpl>',
                {
                    getTipText : function (values) {
                        return scheduler.getSchedulingView().getFormattedDate(values.Date) + ' ' + (values.Text || "");
                    },

                    renderInner : function(values) {
                        return innerTpl.apply(values);
                    }
                }
            );
        }
        
        this.callParent(arguments);
    },


    getElementData : function(viewStart, viewEnd, records) {
        var s = this.store,
            scheduler = this.schedulerView,
            isHorizontal = scheduler.isHorizontal(),
            rs = records || s.getRange(),
            data = [],
            height,
            width,
            region = scheduler.getTimeSpanRegion(viewStart, null, this.expandToFitView),
            record, date, templateData;

        if (Ext.versions.touch){
            height = '100%';
        } else {
            height = isHorizontal ? region.bottom - region.top : 1;
        }

        width = isHorizontal ? 1 : region.right - region.left;

        for (var i = 0, l = rs.length; i < l; i++) {
            record = rs[i];
            date = record.get('Date');

            if (date && Sch.util.Date.betweenLesser(date, viewStart, viewEnd)) {
                var pos = scheduler.getCoordinateFromDate(date);

                templateData = Ext.apply({}, this.getTemplateData(record));
                templateData.id = this.getElementId(record);
                // using $cls to avoid possible conflict with "Cls" field in the record
                // `getElementCls` will append the "Cls" field value to the class
                templateData.$cls = this.getElementCls(record, templateData);

                templateData.width = width;
                templateData.height = height;

                if (isHorizontal) {
                    templateData.left = pos;
                } else{
                    templateData.top = pos;
                }
                data.push(templateData);
            }
        }
        
        return data;
    },
    
    
    getHeaderElementData : function(records) {
        var startDate = this.timeAxis.getStart(),
            endDate = this.timeAxis.getEnd(),
            isHorizontal = this.schedulerView.isHorizontal(),
            data = [],
            record, date, position, templateData;

        records = records || this.store.getRange();

        for (var i = 0, l = records.length; i < l; i++) {
            record = records[i];
            date = record.get('Date');
            
            if (date && Sch.util.Date.betweenLesser(date, startDate, endDate)) {
                position = this.getHeaderElementPosition(date);
                templateData = this.getTemplateData(record);
                
                data.push(Ext.apply({
                    id       : this.getHeaderElementId(record),
                    side     : isHorizontal ? this.side : 'top',
                    cls      : this.getHeaderElementCls(record, templateData),
                    position : position
                }, templateData));
            }
        }
        
        return data;
    }
    
});

/**
@class Sch.plugin.CurrentTimeLine
@extends Sch.plugin.Lines

Plugin (ptype = 'scheduler_currenttimeline') indicating the current date and time as a line in the schedule.

To add this plugin to scheduler:

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                Ext.create('Sch.plugin.CurrentTimeLine', { updateInterval : 30000 })
            ]
        });


*/
Ext.define("Sch.plugin.CurrentTimeLine", {
    extend              : "Sch.plugin.Lines",
    alias               : 'plugin.scheduler_currenttimeline',
    mixins              : ['Sch.mixin.Localizable'],

    /**
     * @cfg {String} tooltipText The text to show in the tooltip next to the current time (defaults to 'Current time').
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {Object} l10n
     * A object, purposed for the class localization. Contains the following keys/values:

            - tooltipText : 'Current time'
     */

    /**
     * @cfg {Int} updateInterval This value (in ms) defines how often the timeline shall be refreshed. Defaults to every once every minute.
     */
    updateInterval      : 60000,

    showHeaderElements  : true,

    /**
     * @cfg {Boolean} autoUpdate true to automatically update the line position over time. Default value is `true`
     */
    autoUpdate          : true,

    expandToFitView     : true,

    timer               : null,

    init                : function(cmp) {
        var store = Ext.create("Ext.data.JsonStore", {
            fields : ['Date', 'Cls', 'Text'],
            data : [{Date : new Date(), Cls : 'sch-todayLine', Text : this.L('tooltipText')}]
        });

        var record = store.first();

        if (this.autoUpdate) {
            this.timer = setInterval(function() {
                record.set('Date', new Date());
            }, this.updateInterval);
        }

        cmp.on('destroy', this.onHostDestroy, this);

        this.store = store;
        this.callParent(arguments);
    },

    onHostDestroy       : function() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }

        if (this.store.autoDestroy) {
            this.store.destroy();
        }
    }
});

/**
 @class Sch.plugin.DragSelector
 @extends Ext.util.Observable

 Plugin (ptype = 'scheduler_dragselector') for selecting multiple events by "dragging" an area in the scheduler chart. Currently only enabled **when CTRL is pressed**

 {@img scheduler/images/drag-selector.png}

 To add this plugin to scheduler:

 var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...
    
            resourceStore   : resourceStore,
            eventStore      : eventStore,
            
            plugins         : [
                Ext.create('Sch.plugin.DragSelector')
            ]
        });


 */
Ext.define("Sch.plugin.DragSelector", {
    extend        : "Sch.util.DragTracker",
    alias         : 'plugin.scheduler_dragselector',
    mixins        : ['Ext.AbstractPlugin'],
    lockableScope : 'top',

    scheduler     : null,
    schedulerView : null,
    eventData     : null,
    sm            : null,
    proxy         : null,
    bodyRegion    : null,

    constructor : function (cfg) {
        cfg = cfg || {};

        Ext.applyIf(cfg, {
            onBeforeStart : this.onBeforeStart,
            onStart       : this.onStart,
            onDrag        : this.onDrag,
            onEnd         : this.onEnd
        });

        this.callParent(arguments);
    },

    init : function (scheduler) {
        scheduler.on({
            afterrender : this.onSchedulerRender,
            destroy     : this.onSchedulerDestroy,
            scope       : this
        });

        scheduler.getSchedulingView().on({
            afterrender : this.onSchedulingViewRender,
            scope       : this
        });

        this.scheduler = scheduler;
    },

    onBeforeStart : function (e) {
        return e.ctrlKey;
    },

    onStart : function (e) {
        var schedulerView = this.schedulerView;

        this.proxy.show();

        this.bodyRegion = schedulerView.getScheduleRegion();

        var eventData = [];

        schedulerView.getEventNodes().each(function (el) {
            eventData[ eventData.length ] = {
                region : el.getRegion(),
                node   : el.dom
            };
        });

        this.eventData = eventData;

        this.sm.deselectAll();
    },

    onDrag : function (e) {
        var sm = this.sm,
            eventData = this.eventData,
            dragRegion = this.getRegion().constrainTo(this.bodyRegion),
            i, ev, len, sel;

        this.proxy.setRegion(dragRegion);

        for (i = 0, len = eventData.length; i < len; i++) {
            ev = eventData[i];
            sel = dragRegion.intersect(ev.region);

            if (sel && !ev.selected) {
                ev.selected = true;
                sm.selectNode(ev.node, true);
            } else if (!sel && ev.selected) {
                ev.selected = false;
                sm.deselectNode(ev.node);
            }
        }
    },

    onEnd : function (e) {
        if (this.proxy) {
            this.proxy.setDisplayed(false);
        }
    },

    onSchedulerRender : function (s) {
        this.sm = s.getEventSelectionModel();
        this.schedulerView = s.getSchedulingView();

        this.initEl(s.el);
    },


    onSchedulingViewRender : function () {
        // the proxy has to be set up immediately after rendering the view, so it will be included in the 
        // "fixedNodes" of the grid view and won't be overwritten after refresh
        this.proxy = this.scheduler.getSchedulingView().el.createChild({ cls : 'sch-drag-selector' });
    },

    onSchedulerDestroy : function () {
        if (this.proxy) Ext.destroy(this.proxy);

        this.destroy();
    }
});

  
/**
@class Sch.plugin.EventEditor
@extends Ext.form.Panel

A plugin (ptype = 'scheduler_eventeditor') used to edit event start/end dates as well as any meta data. It inherits from {@link Ext.form.FormPanel} so you can define any fields and use any layout you want.

{@img scheduler/images/event-editor.png}

Normally, this plugin shows the same form for all events. However you can show different forms for different event types. To do that:

- the event type is supposed to be provided as the value of the `EventType` field in the event model.
- in the {@link #fieldsPanelConfig} provide the container with card layout. The children of that container should be the forms which will be used to edit different
event types
- each such form should contain the `EventType` configuration option, matching to the appropriate event type.
- the small top form, containing the start date, start time and duration fields is always shared among all forms.
- this whole behavior can be disabled with the `dynamicForm : false` option.

The overall picture will look like:
            fieldsPanelConfig : {
                xtype       : 'container',

                layout      : 'card',

                items       : [
                    // form for "Meeting" EventType
                    {
                        EventType   : 'Meeting',

                        xtype       : 'form',

                        items       : [
                            ...
                        ]
                    },
                    // eof form for "Meeting" EventType

                    // form for "Appointment" EventType
                    {
                        EventType   : 'Appointment',

                        xtype       : 'form',

                        items       : [
                            ...
                        ]
                    }
                    // eof form for "Appointment" EventType
                ]
            }

Note, that you can customize the start date, start time and duration fields with appropriate configuration options: {@link #dateConfig}, {@link #timeConfig}, {@link #durationConfig}


        var eventEditor    = Ext.create('Sch.plugin.EventEditor', {
            ...
            timeConfig      : {
                minValue    : '08:00',
                maxValue    : '18:00'
            },
            ...
        });


        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                eventEditor
            ]
        });


If you include additional components which have floating sub-components, such as combo boxes or date pickers -
you need to decorate them with a special CSS class so the editor will stay open when clicking 'outside' of the editor element.

    var myComboBox = new Ext.form.ComboBox({
        width           : 70,
        ...
    });

    // Tell the editor that this component and its picker are part of the editor, clicking them should not hide the editor.
    myComboBox.getPicker().addCls('sch-event-editor-ignore-click');

 */
Ext.define("Sch.plugin.EventEditor", {
    extend      : "Ext.form.Panel",

    mixins      : [
        'Ext.AbstractPlugin',
        'Sch.mixin.Localizable'
    ],

    alias       : ['widget.eventeditor',  'plugin.scheduler_eventeditor'],

    lockableScope : 'normal',

    requires    : [
        'Sch.util.Date'
    ],

    /**
     * @cfg {String} saveText The text to show on the save button
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} deleteText The text to show on the delete button
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} cancelText The text to show on the cancel button
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {Object} l10n
     * A object, purposed for the class localization. Contains the following keys/values:

            - saveText    : 'Save',
            - deleteText  : 'Delete',
            - cancelText  : 'Cancel'
     */

    /**
     * @cfg {Boolean} hideOnBlur True to hide this panel if a click is detected outside the panel (defaults to true)
     */
    hideOnBlur      : true,

    /**
     * This property provides access to the start date field
     * @property {Ext.form.field.Date} startDateField
     */
    startDateField  : null,

    /**
     * This property provides access to the start time field
     * @property {Ext.form.field.Time} startTimeField
     */
    startTimeField  : null,

    /**
     * This property provides access to the duration spinner field
     * @property {Ext.form.field.Number} durationField
     */
    durationField   : null,

    /**
     * @cfg {Object} timeConfig Config for the `startTimeField` constructor.
     */
    timeConfig      : null,

    /**
     * @cfg {Object} dateConfig Config for the `startDateField` constructor.
     */
    dateConfig      : null,

    /**
     * @cfg {Object} durationConfig A custom config object that is used to configure the {@link Ext.form.field.Number duration field}.
     */
    durationConfig  : null,

    /**
     * @cfg {String} durationUnit The unit in which the duration is measured, defaults to Sch.util.Date.HOUR.
     */
    durationUnit    : null,

    /**
     * @cfg {String} durationText The text to show after the duration spinner field
     */
    durationText    : null,

    /**
     * @cfg {String} triggerEvent The event that shall trigger showing the editor. Defaults to 'eventdblclick', set to '' or null to disable editing of existing events.
     */
    triggerEvent    : 'eventdblclick',

    /**
     * @cfg {Object} fieldsPanelConfig A panel config representing your fields that is associated with a scheduled event.
     *
     * Example:

        fieldsPanelConfig : {
            layout      : 'form',

            style       : 'background : #fff',
            border      : false,
            cls         : 'editorpanel',
            labelAlign  : 'top',

            defaults    : {
                width : 135
            },

            items       : [
                titleField      = new Ext.form.TextField({
                    name            : 'Title',
                    fieldLabel      : 'Task'
                }),

                locationField   = new Ext.form.TextField({
                    name            : 'Location',
                    fieldLabel      : 'Location'
                })
            ]
        }
     *
     */
    fieldsPanelConfig   : null,

    /**
     * @cfg {String} dateFormat This config parameter is passed to the `startDateField` constructor.
     */
    dateFormat      : 'Y-m-d',

    /**
     * @cfg {String} timeFormat This config parameter is passed to the `startTimeField` constructor.
     */
    timeFormat      : 'H:i',


    cls             : 'sch-eventeditor',
    border          : false,
    shadow          : false,

    /**
     * @cfg {Boolean} dynamicForm True to use several forms. Default is `true`.
     */
    dynamicForm     : true,

    /**
     * @property {Sch.model.Event} eventRecord The current {@link Sch.model.Event} record, which is being edited by the event editor
     */
    eventRecord     : null,

    hidden          : true,
    collapsed       : true,
    currentForm     : null,
    schedulerView   : null,
    resourceRecord  : null,
    preventHeader   : true,
    floating        : true,
    hideMode        : 'offsets',
    ignoreCls       : 'sch-event-editor-ignore-click',
    readOnly        : false, // Cached value of scheduler setting

    layout          : {
        type    : 'vbox',
        align   : 'stretch'
    },

    /**
     * @cfg {Boolean} constrain Pass `true` to enable the constraining - ie editor panel will not exceed the document edges. This option will disable the animation
     * during the expansion. Default value is `false`.
     */
    constrain           : false,

    constructor : function(config) {
        config              = config || {};
        Ext.apply(this, config);

        this.durationUnit   = this.durationUnit || Sch.util.Date.HOUR;

        this.addEvents(
            /**
            * @event beforeeventdelete
            * Fires before an event is deleted (return false to cancel the operation)
            * @param {Sch.plugin.EventEditor} editor The editor instance
            * @param {Sch.model.Event} eventRecord The record about to be deleted
            */
            'beforeeventdelete',

            /**
            * @event beforeeventsave
            * Fires before an event is saved (return false to cancel the operation)
            * @param {Sch.plugin.EventEditor} editor The editor instance
            * @param {Sch.model.Event} eventRecord The record about to be saved
            */
            'beforeeventsave'
        );

        this.callParent(arguments);
    },


    initComponent : function() {

        if (!this.fieldsPanelConfig) throw 'Must define a fieldsPanelConfig property';

        Ext.apply(this, {
            fbar            : this.buttons || this.buildButtons(),

            items           : [
                {
                    xtype   : 'container',
                    layout  : 'hbox',
                    height  : 35,
                    border  : false,
                    cls     : 'sch-eventeditor-timefields',

                    items   : this.buildDurationFields()
                },
                Ext.applyIf(this.fieldsPanelConfig, {
                    flex        : 1,
                    activeItem  : 0
                })
            ]
        });

        this.callParent(arguments);
    },


    init : function (grid) {
        // setting the ownerCt helps a possible container of the scheduler (such as a window), to not try to
        // position itself above the editor, since it's in sort of a "child" of the Window component in that case.
        this.ownerCt        = grid;

        this.schedulerView  = grid.getView();
        this.eventStore     = this.schedulerView.getEventStore();

        this.schedulerView.on({
            afterrender     : this.onSchedulerRender,
            destroy         : this.onSchedulerDestroy,
            dragcreateend   : this.onDragCreateEnd,

            scope           : this
        });

        if (this.triggerEvent) {
            this.schedulerView.on(this.triggerEvent, this.onActivateEditor, this);
        }

        this.schedulerView.registerEventEditor(this);
    },


    onSchedulerRender : function() {
        this.render(Ext.getBody());

        if (this.hideOnBlur) {
            // Hide when clicking outside panel
            this.mon(Ext.getDoc(), 'mousedown', this.onMouseDown, this);
        }
    },


    /**
     * Activates the editor for the passed event record.
     * @param {Sch.model.Event} eventRecord The record to show in the editor panel
     */
    show : function (eventRecord, alignToEl) {

        var readOnly = this.schedulerView.isReadOnly();

        if (readOnly !== this.readOnly) {
            Ext.Array.each(this.query('field'), function(field) {
                field.setReadOnly(readOnly);
            });

            this.saveButton.setVisible(!readOnly);
            this.deleteButton.setVisible(!readOnly);

            this.readOnly = readOnly;
        }

        // Only show delete button if the event belongs to a store
        if (this.deleteButton) {
            this.deleteButton.setVisible(!readOnly && this.eventStore.indexOf(eventRecord) >= 0);
        }

        this.eventRecord = eventRecord;

        // Manually set the duration field value
        this.durationField.setValue(Sch.util.Date.getDurationInUnit(eventRecord.getStartDate(), eventRecord.getEndDate(), this.durationUnit));

        var startDate = eventRecord.getStartDate();
        this.startDateField.setValue(startDate);
        this.startTimeField.setValue(startDate);

        // If the scheduler we're attached to is inside a floating component (window etc)
        // we need to make sure we're on top of it at show time.
        var floatingContainer = this.schedulerView.up('[floating=true]');
        if (floatingContainer){
            this.getEl().setZIndex(floatingContainer.getEl().getZIndex() + 1);
            floatingContainer.addCls(this.ignoreCls);
        }

        this.callParent();

        alignToEl = alignToEl || this.schedulerView.getElementFromEventRecord(eventRecord);

        this.alignTo(alignToEl, this.schedulerView.getOrientation() == 'horizontal' ? 'bl' : 'tl-tr', this.getConstrainOffsets(alignToEl));

        this.expand(!this.constrain);

        if (this.constrain) {
            this.doConstrain(Ext.util.Region.getRegion(Ext.getBody()));
        }

        var form,
            eventType = eventRecord.get('EventType');

        if (eventType && this.dynamicForm) {
            var fieldsPanel     = this.items.getAt(1),
                forms           = fieldsPanel.query('> component[EventType=' + eventType + ']');

            if (!forms.length)                                  {
                throw "Can't find form for EventType=" + eventType;
            }
            if (!fieldsPanel.getLayout().setActiveItem)         {
                throw "Can't switch active component in the 'fieldsPanel'";
            }

            form = forms[ 0 ];

            if (!(form instanceof Ext.form.Panel))              {
                throw "Each child component of 'fieldsPanel' should be a 'form'";
            }

            fieldsPanel.getLayout().setActiveItem(form);
        } else {
            form = this;
        }

        this.currentForm = form;

        // get the "basicForm" from current form and load it from event record
        form.getForm().loadRecord(eventRecord);
    },

    // Override this to add support for constraining the editor panel to the viewport or scheduler
    getConstrainOffsets : function(eventEl) {
        return [0, 0];
    },

    onSaveClick : function() {
        var formPanel   = this,
            record      = formPanel.eventRecord,
            form        = this.currentForm.getForm();

        if (form.isValid() && this.fireEvent('beforeeventsave', this, record) !== false) {

            var startDate   = formPanel.startDateField.getValue(),
                endDate,
                startTime   = formPanel.startTimeField.getValue(),
                duration    = formPanel.durationField.getValue();

            if (startDate && duration >= 0) {

                if (startTime) {
                    Sch.util.Date.copyTimeValues(startDate, startTime);
                }

                endDate = Sch.util.Date.add(startDate, this.durationUnit, duration);
            } else {
                return;
            }

            // Manually locate the event resource for new records
            var resources = record.getResources();
            var resource = (resources.length > 0 && resources[0]) || this.resourceRecord;

            if (!this.schedulerView.allowOverlap && !this.schedulerView.isDateRangeAvailable(startDate, endDate, record, resource)) {
                return;
            }

            record.beginEdit();

            // wrap the call to `updateRecord` with the code, which
            // disables the `endEdit` method of the record
            // this will prevent the double "update" event from being fired (and double "sync" call)
            // (as we need to batch the form update with our own updates)
            // this is ugly, but `updateRecord` should been really providing the parameter
            // to omit the `beginUpdate/endUpdate` calls..
            var prevEndEdit = record.endEdit;
            record.endEdit = Ext.emptyFn;

            form.updateRecord(record);

            record.endEdit = prevEndEdit;

            record.setStartEndDate(startDate, endDate);

            record.endEdit();

            // Check if this is a new record
            if (this.eventStore.indexOf(this.eventRecord) < 0) {
                if (this.schedulerView.fireEvent('beforeeventadd', this.schedulerView, record) !== false) {
                    this.eventStore.append(record);
                }
            }
            formPanel.collapse(null, true);
        }
    },


    onDeleteClick : function() {
        if (this.fireEvent('beforeeventdelete', this, this.eventRecord) !== false) {
            this.eventStore.remove(this.eventRecord);
        }
        this.collapse(null, true);
    },


    onCancelClick : function() {
        this.collapse(null, true);
    },

    buildButtons : function() {

        this.saveButton = new Ext.Button({
            text        : this.L('saveText'),

            scope       : this,
            handler     : this.onSaveClick
        });

        this.deleteButton = new Ext.Button({
            text        : this.L('deleteText'),

            scope       : this,
            handler     : this.onDeleteClick
        });

        this.cancelButton = new Ext.Button({
            text        : this.L('cancelText'),

            scope       : this,
            handler     : this.onCancelClick
        });

        return [ this.saveButton, this.deleteButton, this.cancelButton ];
    },


    buildDurationFields : function() {

        this.startDateField = new Ext.form.field.Date(Ext.apply({
            width           : 90,
            allowBlank      : false,
            format          : this.dateFormat
        }, this.dateConfig || {}));

        this.startDateField.getPicker().addCls(this.ignoreCls);

        this.startTimeField = new Ext.form.field.Time(Ext.apply({
            width           : 70,
            allowBlank      : false,
            format          : this.timeFormat

        }, this.timeConfig || {}));

        this.startTimeField.getPicker().addCls(this.ignoreCls);

        this.durationField = new Ext.form.field.Number(Ext.apply({

            width           : 45,

            value           : 0,

            minValue        : 0,
            allowNegative   : false

        }, this.durationConfig || {}));


        this.durationLabel = Ext.create("Ext.form.Label", {
            text            : this.getDurationText()
        });

        return [ this.startDateField, this.startTimeField, this.durationField, this.durationLabel ];
    },


    onActivateEditor : function(g, evtRecord) {
        this.show(evtRecord);
    },


    onMouseDown : function(e){

        if (
            this.collapsed || e.within(this.getEl()) ||
            // ignore the click on the menus and combo-boxes (which usually floats as the direct child of <body> and
            // leaks through the `e.within(this.getEl())` check

            // if clicks should be ignored for any other element - it should have this class
                e.getTarget('.' + this.ignoreCls, 9) ||
                e.getTarget(this.schedulerView.eventSelector)
        ) {
            return;
        }

        this.collapse();
    },


    onSchedulerDestroy : function() {
        this.destroy();
    },


    onDragCreateEnd : function(s, eventRecord, resourceRecord) {
        if (!this.dragProxyEl && this.schedulerView.dragCreator) {
            this.dragProxyEl = this.schedulerView.dragCreator.getProxy();
        }

        this.resourceRecord = resourceRecord;

        // Call scheduler template method
        this.schedulerView.onEventCreated(eventRecord);

        this.show(eventRecord, this.dragProxyEl);
    },

    hide : function() {
        this.callParent(arguments);
        var dpEl = this.dragProxyEl;

        if (dpEl) {
            dpEl.hide();
        }
    },

    // Always hide drag proxy on collapse
    afterCollapse : function() {
        // currently the header is kept even after collapse, so need to hide the form completely
        this.hide();

        this.callParent(arguments);
    },


    getDurationText : function () {
        if (this.durationText) {
            return this.durationText;
        }

        return Sch.util.Date.getShortNameOfUnit(Sch.util.Date.getNameOfUnit(this.durationUnit));
    }
});

/**
@class Sch.plugin.EventTools
@extends Ext.Container

A plugin (ptype = 'scheduler_eventtools') showing a tools menu with event actions when the mouse hovers over a rendered event in the timeline.
Each tool can also define a visibleFn, which is called before the tools menu is shown. This allows you to get control over which actions
can be performed on which events.

Sample usage:
    
    plugins : [
        Ext.create('Sch.plugin.EventTools', {
            items : [
                { type: 'details',  handler: onToolClick, tooltip: 'Show Event Details' },
                { type: 'edit',     handler: onToolClick, tooltip: 'Edit Event' },
                { type: 'repeat',   handler: onToolClick, tooltip: 'Repeat Event' },
                { type: 'drop',     handler: onToolClick, tooltip: 'Remove Event', visibleFn: function(model) { return !!model.get('Deletable'); } }
            ]
        })
    ]

*/
Ext.define('Sch.plugin.EventTools', {
    extend          : 'Ext.Container',
    mixins          : ['Ext.AbstractPlugin'],
    lockableScope   : 'top',
    alias           : 'plugin.scheduler_eventtools',

    /**
    * @cfg {Int} hideDelay The menu will be hidden after this number of ms, when the mouse leaves the tools element. 
    */
    hideDelay       : 500,
    
    /**
    * @cfg {String} align The alignment of the tools menu
    */
    align           : 'right',
    
    /**
    * @cfg {Object} defaults The defaults for each action item in the tools menu
    */
    defaults: {
        xtype       : 'tool',
        baseCls     : 'sch-tool',
        overCls     : 'sch-tool-over',
        width       : 20,
        height      : 20,
        visibleFn   : Ext.emptyFn
    },

    // private
    hideTimer   : null,
    
    // private
    lastPosition    : null,
    
    // private
    cachedSize      : null,

    // private
    offset          : { x: 0, y: 1 },

    autoRender      : true,
    floating        : true,
    hideMode        : 'offsets',
    hidden          : true,

    /**
    * Returns the record that this tools menu is currently associated with
    * @return {Sch.model.Event} record The event record
    */
    getRecord : function() {
        return this.record;
    },
     
    init: function (scheduler) {
        if (!this.items) throw 'Must define an items property for this plugin to function correctly';

        // Let client use 'cls' property
        this.addCls('sch-event-tools');

        this.scheduler = scheduler;

        scheduler.on({
            // Suspend during resize
            'eventresizestart'  : this.onOperationStart,
            'eventresizeend'    : this.onOperationEnd,
            
            // Suspend during drag drop
            'eventdragstart'    : this.onOperationStart,
            'eventdrop'         : this.onOperationEnd,
            
            'eventmouseenter'   : this.onEventMouseEnter,
            'eventmouseleave'   : this.onContainerMouseLeave,
            
            scope: this
        });
    },

    onRender: function () {
        this.callParent(arguments);

        this.scheduler.mon(this.el, {
            mouseenter : this.onContainerMouseEnter,
            mouseleave : this.onContainerMouseLeave,
            scope       : this
        });
    },

    onEventMouseEnter: function (sch, model, event) {

        var doShow  = false;
        var visible;
        this.record = model;
        
        this.items.each(function (tool) {
            visible = tool.visibleFn(model) !== false;
            tool.setVisible(visible);

            if (visible) {
                doShow = true;
            }
        }, this);

        if (!doShow) return;

        if (!this.rendered) {
            this.doAutoRender();
        }

        var node    = event.getTarget(sch.eventSelector);
        var box     = Ext.fly(node).getBox();

        this.doLayout();

        // Needs to be done after doLayout
        var size = this.getSize();

        this.lastPosition = [
            event.getXY()[0] - (size.width/2), 
            box.y - size.height - this.offset.y
        ];

        this.onContainerMouseEnter();
    },

    onContainerMouseEnter: function () {
        window.clearTimeout(this.hideTimer);
        this.setPosition.apply(this, this.lastPosition);
        this.show();
    },

    onContainerMouseLeave: function () {
        window.clearTimeout(this.hideTimer);
        this.hideTimer = Ext.defer(this.hide, this.hideDelay, this);
    },

    onOperationStart: function () {
        this.scheduler.un("eventmouseenter", this.onEventMouseEnter, this);
        window.clearTimeout(this.hideTimer);
        this.hide();
    },

    onOperationEnd: function () {
        this.scheduler.on("eventmouseenter", this.onEventMouseEnter, this);
    }
});



/**
@class Sch.plugin.Pan

A plugin (ptype = 'scheduler_pan') enabling panning by clicking and dragging in a scheduling view.

To add this plugin to your scheduler or gantt view:

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                Ext.create('Sch.plugin.Pan', { enableVerticalPan : true })
            ]
        });
*/
Ext.define("Sch.plugin.Pan", {
    extend        : 'Ext.AbstractPlugin',
    alias         : 'plugin.scheduler_pan',
    lockableScope : 'top',

    /**
     * @cfg {Boolean} enableVerticalPan
     * True to allow vertical panning
     */
    enableVerticalPan : true,

    statics : {
        /**
         * @cfg {Number} KEY_SHIFT Constant for shift key
         */
        KEY_SHIFT : 1,

        /**
         * @cfg {Number} KEY_CTRL Constant for ctrl / meta key
         */
        KEY_CTRL  : 2,

        /**
         * @cfg {Number} KEY_ALT Constant for alt key
         */
        KEY_ALT   : 4,

        /**
         * @cfg {Number} KEY_ALL Constant for all modifier keys (shift, ctrl / meta, alt)
         */
        KEY_ALL   : 7
    },

    /**
     * @cfg {Number} disableOnKey Specifies which key should be pressed to disable panning.
     * See {@link #KEY_SHIFT}, {@link #KEY_CTRL}, {@link #KEY_ALT}, {@link #KEY_ALL}.
     * For example to disable panning when shift or ctrl is pressed:

        Ext.create('Sch.plugin.Pan', {
            disableOnKey : Sch.plugin.Pan.KEY_SHIFT + Sch.plugin.Pan.KEY_CTRL
        })

     */
    disableOnKey : 0,

    panel : null,

    constructor : function (config) {
        Ext.apply(this, config);
    },

    init : function (pnl) {
        this.panel = pnl.normalGrid || pnl;
        this.view = pnl.getSchedulingView();

        this.view.on('afterrender', this.onRender, this);
    },

    onRender : function (s) {
        this.view.el.on('mousedown', this.onMouseDown, this);
    },

    onMouseDown : function (e, t) {
        var self = this.self,
            disableOnKey = this.disableOnKey;

        // Ignore event if #disableOnKey is specified and at least one of the
        // functional keys is pressed
        if ((e.shiftKey && (disableOnKey & self.KEY_SHIFT)) ||
            (e.ctrlKey && (disableOnKey & self.KEY_CTRL)) ||
            (e.altKey && (disableOnKey & self.KEY_ALT))) {
            return;
        }

        // ignore clicks on tasks and events
        if (e.getTarget('.' + this.view.timeCellCls, 10) && !e.getTarget(this.view.eventSelector)) {
            this.mouseX = e.getPageX();
            this.mouseY = e.getPageY();
            Ext.getBody().on('mousemove', this.onMouseMove, this);
            Ext.getDoc().on('mouseup', this.onMouseUp, this);

            // For IE (and FF if using frames), if you move mouse onto the browser chrome and release mouse button
            // we won't know about it. Next time mouse enters the body, cancel any ongoing pan activity as a fallback.
            if (Ext.isIE || Ext.isGecko) {
                Ext.getBody().on('mouseenter', this.onMouseUp, this);
            }

            // required for some weird chrome bug/behavior, when whole panel was scrolled-out
            e.stopEvent();
        }
    },

    onMouseMove : function (e) {
        e.stopEvent();

        var x = e.getPageX(),
            y = e.getPageY(),
            xDelta = x - this.mouseX,
            yDelta = y - this.mouseY;

        this.panel.scrollByDeltaX(-xDelta);
        this.mouseX = x;
        this.mouseY = y;

        if (this.enableVerticalPan) {
            this.panel.scrollByDeltaY(-yDelta);
        }
    },

    onMouseUp : function (e) {
        Ext.getBody().un('mousemove', this.onMouseMove, this);
        Ext.getDoc().un('mouseup', this.onMouseUp, this);

        if (Ext.isIE || Ext.isGecko) {
            Ext.getBody().un('mouseenter', this.onMouseUp, this);
        }
    }
});

/**
@class Sch.plugin.SimpleEditor
@extends Ext.Editor

A plugin (ptype = 'scheduler_simpleeditor') for basic text editing of an event name.

{@img scheduler/images/simple-editor.png}

To add this plugin to scheduler:

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...

            resourceStore   : resourceStore,
            eventStore      : eventStore,

            plugins         : [
                Ext.create('Sch.plugin.SimpleEditor', { dataIndex : 'Title' })
            ]
        });


*/
Ext.define("Sch.plugin.SimpleEditor", {
    extend          : "Ext.Editor",
    alias           : 'plugin.scheduler_simpleeditor',
    mixins          : ['Ext.AbstractPlugin', 'Sch.mixin.Localizable'],
    lockableScope   : 'top',


    cls             : 'sch-simpleeditor',
    allowBlur       : false,

    // private
    delegate        : '.sch-event-inner',

    /**
     * @cfg {String} dataIndex Required. A field, containing the task's title. This field will be updated by the editor. Defaults to the value of the {@link Sch.model.Event#nameField}.
     */
    dataIndex       : null,

    completeOnEnter     : true,
    cancelOnEsc         : true,
    ignoreNoChange      : true,
    height              : 19,

    autoSize            : {
        width   : 'boundEl' // The width will be determined by the width of the boundEl, the height from the editor (21)
    },


    constructor : function(config) {
        config = config || {};
        config.field = config.field || Ext.create("Ext.form.TextField", { selectOnFocus : true });
        this.callParent(arguments);
    },


    init : function(scheduler) {
        this.scheduler = scheduler.getSchedulingView();

        scheduler.on('afterrender', this.onSchedulerRender, this);
        this.scheduler.registerEventEditor(this);

        this.dataIndex = this.dataIndex || this.scheduler.getEventStore().model.prototype.nameField;
    },

    // Programmatically enter edit mode
    edit : function(record, el) {
        el = el || this.scheduler.getElementFromEventRecord(record);
        this.startEdit(el.child(this.delegate));
        this.record = record;
        this.setValue(this.record.get(this.dataIndex));
    },


    onSchedulerRender : function(scheduler) {

        this.on({
            startedit   : this.onBeforeEdit,

            complete    : function(editor, value, original) {
                var record = this.record;
                var eventStore = this.scheduler.eventStore;
                record.set(this.dataIndex, value);

                // Check if this is a new record
                if (eventStore.indexOf(record) < 0) {
                    if (this.scheduler.fireEvent('beforeeventadd', this.scheduler, record) !== false) {
                        eventStore.append(record);
                    }
                }

                this.onAfterEdit();
            },

            canceledit  : this.onAfterEdit,

            hide        : function() {
                if (this.dragProxyEl) {
                    this.dragProxyEl.hide();
                }
            },

            scope       : this
        });

        scheduler.on({
            eventdblclick : function(s, r, e){
                if (!scheduler.isReadOnly()) {
                    this.edit(r);
                }
            },
            dragcreateend: this.onDragCreateEnd,
            scope : this
        });
    },

    onBeforeEdit  : function() {
        if (!this.allowBlur) {
            // This should be removed when this bug is fixed:
            // http://www.sencha.com/forum/showthread.php?244580-4.1-allowBlur-on-Ext.Editor-not-working
            Ext.getBody().on('mousedown', this.onMouseDown, this);
            this.scheduler.on('eventmousedown', function() { this.cancelEdit(); }, this);
        }
    },

    onAfterEdit  : function() {
        if (!this.allowBlur) {
            Ext.getBody().un('mousedown', this.onMouseDown, this);
            this.scheduler.un('eventmousedown', function() { this.cancelEdit(); }, this);
        }
    },

    onMouseDown : function(e, t) {
        if (this.editing && this.el && !e.within(this.el)) {
            this.cancelEdit();
        }
    },

    onDragCreateEnd : function(s, eventRecord) {
        if (!this.dragProxyEl && this.scheduler.dragCreator) {
            this.dragProxyEl = this.scheduler.dragCreator.getProxy();
        }

        // Call scheduler template method
        this.scheduler.onEventCreated(eventRecord);

        if (eventRecord.get(this.dataIndex) === '') {
            eventRecord.set(this.dataIndex, this.L('newEventText'));
        }
        this.edit(eventRecord, this.dragProxyEl);
    }
});

/**
@class Sch.plugin.SummaryColumn
@extends Ext.grid.column.Column

Plugin (ptype = 'scheduler_summarycolumn') showing the currently allocated time for the resources. It will simply summarize the durations **of the events that are in the current view**.
The information can be displayed as either a time unit or a percentage of the available time.

To add this plugin to scheduler:

        var summaryCol = Ext.create("Sch.plugin.SummaryColumn", { 
            header : 'Time allocated', 
            width: 80,
            showPercent : false 
        });

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            resourceStore   : resourceStore,
            eventStore      : eventStore,
            
            columns         : [
                ...
                summaryCol,
                ...
            ],

            plugins : [summaryCol]
        });

*/
Ext.define('Sch.plugin.SummaryColumn', {
    extend      : "Ext.grid.column.Column",
    alias       : ["widget.summarycolumn", "plugin.scheduler_summarycolumn"],
    mixins      : ['Ext.AbstractPlugin'],
    
    lockableScope : 'top',

    /**
     * @cfg {Boolean} showPercent True to show percentage values, false to show summarized time. Default value is `false`.
     */
    showPercent : false,
    
    /**
     * @cfg {Int} nbrDecimals The number of decimals to show, only applicable when `showPercent` is set to false
     */
    nbrDecimals : 1,
    
    sortable        : false,
    fixed           : true,
    menuDisabled    : true,
    
    width           : 80,
    dataIndex       : '_sch_not_used',
    
    scheduler       : null,
    eventStore      : null,

    constructor : function(config){
        this.scope = this;
        this.callParent(arguments);
    },

    
    init : function(scheduler) {
        // Only attach to a scheduler 
        if (!('eventStore' in scheduler)) return;
	
        this.scheduler = scheduler;

        // This plugin requires the scheduler to refresh its locked grid when the unlocked scheduler view is refreshed
        // e.g. when currently viewed timespan is changed.
        this.scheduler.lockedGridDependsOnSchedule = true;

        this.eventStore = scheduler.eventStore;
    },
    
    
    renderer : function(v, p, record){
        var g = this.scheduler,
            s = this.eventStore,
            viewStart = g.getStart(),
            viewEnd = g.getEnd(),
            retVal = 0,
            totalAllocatedMinutesInView = this.calculate(s.getEventsForResource(record), viewStart, viewEnd);
        
        if (totalAllocatedMinutesInView <= 0) {
            return '';
        }
        
        if (this.showPercent) {
            var timeInView = Sch.util.Date.getDurationInMinutes(viewStart, viewEnd);
            return (Math.round((totalAllocatedMinutesInView * 100)/ timeInView)) + ' %';
        } else {
            if (totalAllocatedMinutesInView > 1440) {
                return (totalAllocatedMinutesInView / 1440).toFixed(this.nbrDecimals) + ' ' + Sch.util.Date.getShortNameOfUnit("DAY");
            }
            if (totalAllocatedMinutesInView >= 30) {
                return (totalAllocatedMinutesInView / 60).toFixed(this.nbrDecimals) + ' ' + Sch.util.Date.getShortNameOfUnit("HOUR");
            }
            return totalAllocatedMinutesInView + ' ' + Sch.util.Date.getShortNameOfUnit("MINUTE");
        }
    },
    
    calculate : function(eventRecords, viewStart, viewEnd){
        var totalTime = 0,
            eventStart,
            eventEnd,
            D = Sch.util.Date;
       
        Ext.each(eventRecords, function(eRec) {
            eventStart = eRec.getStartDate();
            eventEnd = eRec.getEndDate();
                
            if (D.intersectSpans(viewStart, viewEnd, eventStart, eventEnd)) {
               totalTime += D.getDurationInMinutes(D.max(eventStart, viewStart), D.min(eventEnd, viewEnd)); 
            }
        });
        
        return totalTime;
    }
});
/**
@class Sch.plugin.Zones
@extends Sch.feature.AbstractTimeSpan

Plugin (ptype = 'scheduler_zones') for showing "global" zones in the scheduler grid, these can by styled easily using just CSS.
To populate this plugin you need to pass it a store having `Sch.model.Range` as the model.

{@img scheduler/images/scheduler-grid-horizontal.png}

To add this plugin to scheduler:

        var zonesStore = Ext.create('Ext.data.Store', {
            model   : 'Sch.model.Range',
            data    : [
                {
                    StartDate   : new Date(2011, 0, 6),
                    EndDate     : new Date(2011, 0, 7),
                    Cls         : 'myZoneStyle'
                }
            ]
        });

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...
    
            resourceStore   : resourceStore,
            eventStore      : eventStore,
            
            plugins         : [
                Ext.create('Sch.plugin.Zones', { store : zonesStore })
            ]
        });


*/
Ext.define("Sch.plugin.Zones", {
    extend      : "Sch.feature.AbstractTimeSpan",
    alias       : "plugin.scheduler_zones",

    requires    : [
        'Sch.model.Range'
    ],

    /**
     * @cfg {String/Ext.XTemplate} innerTpl A template providing additional markup to render into each timespan element
     */
    innerTpl            : null,

    cls                 : 'sch-zone',
    side                : null,

    
    init : function (scheduler) {
        if (Ext.isString(this.innerTpl)) {
            this.innerTpl = new Ext.XTemplate(this.innerTpl);
        }

        this.side = scheduler.rtl ? 'right' : 'left';

        var innerTpl = this.innerTpl;

        if (!this.template) {
            this.template = new Ext.XTemplate(
                '<tpl for=".">' +
                    '<div id="{id}" class="{$cls}" style="' + this.side + ':{left}px;top:{top}px;height:{height}px;width:{width}px;{style}">' +
                    (innerTpl ? '{[this.renderInner(values)]}' : '') + 
                    '</div>' +
                '</tpl>',
                {
                    renderInner : function(values) {
                        return innerTpl.apply(values);
                    }
                }
            );
        }
        
        
        if (Ext.isString(this.innerHeaderTpl)) {
            this.innerHeaderTpl = new Ext.XTemplate(this.innerHeaderTpl);
        }
        
        this.callParent(arguments);
    },

    
    getElementData : function(viewStart, viewEnd, records, isPrint) {
        var schedulerView   = this.schedulerView,
            data            = [],
            region          = schedulerView.getTimeSpanRegion(viewStart, viewEnd, this.expandToFitView),
            isHorizontal    = this.schedulerView.isHorizontal(),
            record, spanStart, spanEnd, zoneData, width, templateData;
        
        records             = records || this.store.getRange();
            
        for (var i = 0, l = records.length; i < l; i++) {
            record       = records[i];
            spanStart    = record.getStartDate();
            spanEnd      = record.getEndDate();
            templateData = this.getTemplateData(record);
            
            if (spanStart && spanEnd && Sch.util.Date.intersectSpans(spanStart, spanEnd, viewStart, viewEnd)) {
                var startPos = schedulerView.getCoordinateFromDate(Sch.util.Date.max(spanStart, viewStart));
                var endPos = schedulerView.getCoordinateFromDate(Sch.util.Date.min(spanEnd, viewEnd));

                zoneData = Ext.apply({}, templateData);

                zoneData.id = this.getElementId(record);
                // using $cls to avoid possible conflict with "Cls" field in the record
                // `getElementCls` will append the "Cls" field value to the class
                zoneData.$cls = this.getElementCls(record, templateData);

                if (isHorizontal) {
                    zoneData.left = startPos;
                    zoneData.top = region.top;

                    zoneData.width = isPrint ? 0 : endPos - startPos;
                    zoneData.height = region.bottom - region.top;

                    zoneData.style = isPrint ? ('border-left-width:' + (endPos - startPos) + 'px') : "";
                } else {
                    zoneData.left = region.left;
                    zoneData.top = startPos;

                    zoneData.height = isPrint ? 0 : endPos - startPos;
                    zoneData.width = region.right - region.left;

                    zoneData.style = isPrint ? ('border-top-width:' + (endPos - startPos) + 'px') : "";
                }

                data.push(zoneData);
            }
        }
        return data;
    },
    
        
    getHeaderElementId : function(record, isStart) {
        return this.callParent([record]) + (isStart ? '-start' : '-end');
    },
    
    
    /**
     * Return header element class for data record.
     * 
     * @param {Sch.model.Range} record Data record
     * @param {Object} data
     * @param {Boolean} isStart
     * 
     * @return {String}
     */
    getHeaderElementCls : function(record, data, isStart) {
        var clsField = record.clsField || this.clsField;
            
        if (!data) {
            data = this.getTemplateData(record);
        }
        
        return 'sch-header-indicator sch-header-indicator-' + (isStart ? 'start ' : 'end ') +
            this.uniqueCls + ' ' + (data[clsField] || '');
    },
    
    
    getZoneHeaderElementData : function(startDate, endDate, record, isStart) {
        var date = isStart ? record.getStartDate() : record.getEndDate(),
            data = null,
            position, isHorizontal, templateData;
            
        if (date && Sch.util.Date.betweenLesser(date, startDate, endDate)) {
            position     = this.getHeaderElementPosition(date);
            isHorizontal = this.schedulerView.isHorizontal();
            templateData = this.getTemplateData(record);
            
            data = Ext.apply({
                id       : this.getHeaderElementId(record, isStart),
                cls      : this.getHeaderElementCls(record, templateData, isStart),
                isStart  : isStart,
                
                side     : isHorizontal ? this.side : 'top',
                position : position
            }, templateData);
        }
        
        return data;
    },
    
    
    getHeaderElementData : function(records) {
        var startDate = this.timeAxis.getStart(),
            endDate = this.timeAxis.getEnd(),
            data = [],
            record, startData, endData;
            
        records = records || this.store.getRange();
        
        for (var i = 0, l = records.length; i < l; i++) {
            record = records[i];
            
            startData = this.getZoneHeaderElementData(startDate, endDate, record, true);
            if (startData) {
                data.push(startData);
            }
            
            endData = this.getZoneHeaderElementData(startDate, endDate, record, false);
            if (endData) {
                data.push(endData);
            }
            
        }

        return data;
    },
    
    
    updateZoneHeaderElement : function(el, data) {
        // Reapply CSS classes
        el.dom.className = data.cls;

        if (this.schedulerView.isHorizontal()) {
            this.setElementX(el, data.position);
        } else {
            el.setTop(data.position);
        }
    },
    
    
    updateHeaderElement : function(record) {
        var startDate = this.timeAxis.getStart(),
            endDate = this.timeAxis.getEnd(),
            startEl = Ext.get(this.getHeaderElementId(record, true)),
            endEl   = Ext.get(this.getHeaderElementId(record, false)),
            startData = this.getZoneHeaderElementData(startDate, endDate, record, true),
            endData   = this.getZoneHeaderElementData(startDate, endDate, record, false);
            
        if (!(startEl && endData) || !(endEl && endData)) {
            Ext.destroy(startEl, endEl);
            this.renderHeaderElementsInternal([record]);
        } else {
            if (startEl) {
                if (!startData) {
                    Ext.destroy(startEl);
                } else {
                    this.updateZoneHeaderElement(startEl, startData);
                }
            }
            
            if (endEl) {
                if (!endData) {
                    Ext.destroy(endEl);
                } else {
                    this.updateZoneHeaderElement(endEl, endData);
                }
            }
        }
    }
    
}); 

/**
@class Sch.plugin.TimeGap
@extends Sch.plugin.Zones

Plugin (ptype = 'scheduler_timegap') for highlighting unallocated slots of time for all resources. You can use the `getZoneCls` method to customize the css class of the "gaps".

{@img scheduler/images/plugin-timegap.png}

To add this plugin to scheduler:

        var scheduler = Ext.create('Sch.panel.SchedulerGrid', {
            ...
    
            resourceStore   : resourceStore,
            eventStore      : eventStore,
            
            plugins         : [
                Ext.create('Sch.plugin.TimeGap', {
                
                    getZoneCls : function (startDate, endDate) {
                        return 'myGapCls'
                    }
                })
            ]
        });

*/
Ext.define("Sch.plugin.TimeGap", {
    extend : "Sch.plugin.Zones",
    alias  : "plugin.scheduler_timegap",

    /**
     * An empty function by default, but provided so that you can return a custom CSS class for each unallocated zone area
     * @param {Date} start The start date of the unallocated time slot
     * @param {Date} end The end date of the unallocated time slot
     * @return {String} The CSS class to be applied to the zone element
     */
    getZoneCls : Ext.emptyFn,

    init:function(scheduler) {

        this.store = new Ext.data.JsonStore({
             model : 'Sch.model.Range'
        });

        this.scheduler = scheduler;

        scheduler.mon(scheduler.eventStore, {
            'load' : this.populateStore,
            'update' : this.populateStore,
            'remove' : this.populateStore,
            'add' : this.populateStore,
            'datachanged' : this.populateStore,
            scope : this
        });

        scheduler.on('viewchange', this.populateStore, this);

        this.schedulerView = scheduler.getSchedulingView();

        this.callParent(arguments);
    },

    populateStore : function(eventStore) {
        var eventsInView = this.schedulerView.getEventsInView(),
            timeGaps = [],
            viewStart = this.scheduler.getStart(),
            viewEnd = this.scheduler.getEnd(),
            l = eventsInView.getCount(),
            cursor = viewStart,
            eventStart,
            index = 0,
            r;

        // Sort by start time    
        eventsInView.sortBy(function(r1, r2) {
            return r1.getStartDate() - r2.getStartDate();
        });

        r = eventsInView.getAt(0);

        while(cursor < viewEnd && index < l) {
            eventStart = r.getStartDate();

            if (!Sch.util.Date.betweenLesser(cursor, eventStart, r.getEndDate()) && cursor < eventStart) {
                timeGaps.push(new this.store.model({
                    StartDate : cursor,
                    EndDate : eventStart,
                    Cls : this.getZoneCls(cursor, eventStart) || ''
                }));
            }
            cursor = Sch.util.Date.max(r.getEndDate(), cursor);
            index++;
            r = eventsInView.getAt(index);
        }

        // Check if there's a gap between last cursor and view end time
        if (cursor < viewEnd) {
            timeGaps.push(new this.store.model({
                StartDate : cursor,
                EndDate : viewEnd,
                Cls : this.getZoneCls(cursor, viewEnd) || ''
            }));
        }

        // Don't refresh twice, the add will cause the zones to redraw
        this.store.removeAll(timeGaps.length > 0);
        this.store.add(timeGaps);
    }
}); 

/**
@class Sch.plugin.TreeCellEditing
@extends Ext.grid.plugin.CellEditing

A specialized "cell editing" plugin (ptype = 'scheduler_treecellediting'), purposed to correctly work with trees. Add it to your component (scheduler with tree view or gantt)
as usual grid plugin:

    var gantt = Ext.create('Gnt.panel.Gantt', {

        plugins             : [
            Ext.create('Sch.plugin.TreeCellEditing', {
                clicksToEdit: 1
            })
        ],
        ...
    })

This class allows us to do 'complex data editing', which is not supported by the regular CellEditing plugin or the Ext.grid.CellEditor which
 assumes a column is always tied to a single field existing on the grid store model (which is not the case for Gantt, dependencies, assignments etc).
*/
Ext.define('Sch.plugin.TreeCellEditing', {
    extend          : 'Ext.grid.plugin.CellEditing',
    alias           : 'plugin.scheduler_treecellediting',

    lockableScope   : 'locked',

    init : function (pnl) {
        this._grid      = pnl;

        // This is used to prevent editing of readonly cells
        this.on('beforeedit', this.checkReadOnly, this);

        this.callParent(arguments);
    },

    bindPositionFixer : function () {
        Ext.on({
            afterlayout : this.fixEditorPosition,

            scope       : this
        });
    },

    unbindPositionFixer : function () {
        Ext.un({
            afterlayout : this.fixEditorPosition,

            scope       : this
        });
    },

    /*
     * Fixes active editor position.
     */
    fixEditorPosition : function (record) {
        // check if we have an active editor
        var editor  = this.getActiveEditor();
        if (editor) {// && (!record || record === this.context.record)) {
            // rebuild editing context
            var editingContext  = this.getEditingContext(this.context.record, this.context.column);
            if (editingContext) {
                // after layout flushing we have references to not exisiting
                // DOM elements for row and for cell, so we update them
                this.context.row        = editingContext.row;
                this.context.rowIdx     = editingContext.rowIdx;
                editor.boundEl          = this.getCell(editingContext.record, editingContext.column);
                editor.realign();

                var lockedView          = this._grid.getView();
                // Since gridview/treeview isn't built to handle a refresh during editing,
                // we help the view by re-setting the focusedRow which is invalid after the refresh
                lockedView.focusedRow = lockedView.getNode(editingContext.rowIdx);
            }
        }
    },

    /*
    * Checks if panel is not locked for editing, and prevents cell edits if needed
    */
    checkReadOnly : function() {
        var pnl = this._grid;

        if (!(pnl instanceof Sch.panel.TimelineTreePanel)) {
            pnl = pnl.up('tablepanel');
        }
        return !pnl.isReadOnly();
    },

    // Preventing a possibly massive relayout on start edit
    startEdit : function(record, columnHeader, context) {
        this._grid.suspendLayouts();

//        TODO this code is probably no longer required, after we have moved the "onBeforeEdit" handler
//        in the TreeCellEditing to the `showEditor` method
//        but need to review all tests
//        var editor   = columnHeader.getEditor && columnHeader.getEditor();
//
//        // if editor has suppress record updating method - call it
//        if (editor && editor.setSuppressTaskUpdate) {
//            editor.setSuppressTaskUpdate(true);
//        }
//
//        // we have to call completeEdit here
//        // since for some reason in parent method
//        // completeEdit starts after 'beforeedit' firing
//        this.completeEdit();

        var val = this.callParent(arguments);

//        // restore suppressing state
//        if (editor && editor.setSuppressTaskUpdate) {
//            editor.setSuppressTaskUpdate(false);
//        }

        this._grid.resumeLayouts();

        return val;
    },

    // @OVERRIDE - model set() method
    onEditComplete : function(ed, value, startValue) {
        var me = this, record, restore;

        // if field instance contains applyChanges() method
        // then we delegate saving to it
        if (ed.field.applyChanges) {
            record      = ed.field.task || me.context.record;
            restore     = true;
            // overwrite original set() method to use applyChanges() instead
            record.set  = function() {
                // restore original method
                delete record.set;
                restore = false;

                ed.field.applyChanges(record);
            };
        }

        this.callParent(arguments);

        // restore original set() method
        if (restore) {
            delete record.set;
        }

        this.unbindPositionFixer();
    },

    // @OVERRIDE - Fixes layout issues during editing in IE
    showEditor : function(ed, context, value) {
        var sm                  = this.grid.getSelectionModel();
        var oldSelect           = sm.selectByPosition;

        // for some reason we are disabling this method
        // could be because it was doing focus, and thus triggering some side effects
        // as of 4.2.2 ExtJS seems to prevent the focus itself, so may be we don't need it anymore
        // in 4.2.2 this disabling breaks the editing when user click the row which is not yet selected (clicksToEdit : 1)
        // (gantt columns/1014_dependency.t.js)
        if (Ext.getVersion('extjs').isLessThan('4.2.2.1144')) {
            sm.selectByPosition = Ext.emptyFn;
        }
        
        var field               = ed.field;

        // if editor has suppress record updating method - call it
        if (field && field.setSuppressTaskUpdate) {
            field.setSuppressTaskUpdate(true);
        }
        
        var prevStartEdit       = ed.startEdit;

        // Sencha can't decide whether this method should be called synchronously or not
        // in some versions it is called synchronously, in others - with 1ms delay
        // so we can't call the "setSuppressTaskUpdate(false)" synchronously after `callParent()` here
        // need to do this only after the `startEdit` of the editor has happened
        ed.startEdit            = function () {
            ed.startEdit        = prevStartEdit;
            
            ed.startEdit.apply(ed, arguments);
            
            // restore suppressing state
            if (field && field.setSuppressTaskUpdate) {
                field.setSuppressTaskUpdate(false);
            }
        };

        // For editing of cells where the data isn't coming from the task model itself, we need to help the
        // editor understand what's going on and set a proper initial value instead of 'undefined'
        if (field) {
            // if it's a field mixed with TaskField mixin
            if (field.setTask) {
                // then after setTask calling field already has correct value
                field.setTask(context.record);
                value = context.value = context.originalValue = field.getValue();

            } else if (!context.column.dataIndex && context.value === undefined) {
                value = context.value = field.getDisplayValue(context.record);
            }
        }

        this.callParent([ed, context, value]);

        if (Ext.getVersion('extjs').isLessThan('4.2.2.1144')) {
            sm.selectByPosition = oldSelect;
        }

        this.bindPositionFixer();
    },

    cancelEdit : function () {
        this.callParent(arguments);

        this.unbindPositionFixer();
    }

});

/**
@class Sch.plugin.ResourceZones
@extends Sch.feature.AbstractTimeSpan

A plugin (ptype = 'scheduler_resourcezones') for visualizing resource specific meta data such as availability, used internally by the Scheduler.
To use this feature, assign an {@link Sch.data.EventStore eventStore} to the {@link Sch.mixin.SchedulerPanel#cfg-resourceZones resourceZones}  
config on the main Scheduler panel class. Additionally, you can provide the {@link Sch.mixin.SchedulerPanel#cfg-resourceZonesConfig resourceZonesConfig} object
with configuration options.

    {
         xtype           : 'schedulergrid',
         region          : 'center',
         startDate       : new Date(2013, 0, 1),
         endDate         : new Date(2014, 0, 1),
         resourceStore   : new Sch.data.ResourceStore(),
         resourceZones   : new Sch.data.EventStore(), // Meta events such as availabilities can be visualized here
         resourceZonesConfig : {
            innerTpl                : '... customized template here ...'
         },
         eventStore      : new Sch.data.EventStore()  // Regular tasks in this store
    },

Records in the store should be regular {@link Sch.model.Event events} where you can specify the Resource, StartDate, EndDate and Cls (to set a CSS class on the rendered zone).
*/
Ext.define("Sch.plugin.ResourceZones", {
    extend              : 'Sch.plugin.Zones',
    alias               : 'plugin.scheduler_resourcezones',

    /**
     * @cfg {String/Ext.XTemplate} innerTpl A template providing additional markup to render into each timespan element
     */
    innerTpl            : null,

    /**
    * @cfg {Sch.data.EventStore} store The store containing the meta 'events' to be rendered for each resource
    * @Required
    */
    store               : null,

    cls                 : 'sch-resourcezone',

    init : function(scheduler) {
        // unique css class to be able to identify the elements belonging to this instance
        this.uniqueCls = this.uniqueCls || ('sch-timespangroup-' + Ext.id());

        this.scheduler = scheduler;
        scheduler.on('destroy', this.onSchedulerDestroy, this);

        scheduler.registerRenderer(this.renderer, this);

        if (Ext.isString(this.innerTpl)) {
            this.innerTpl = new Ext.XTemplate(this.innerTpl);
        }

        var innerTpl = this.innerTpl;

        if (!this.template) {
            this.template = new Ext.XTemplate(
                '<tpl for=".">' +
                    '<div id="' + this.uniqueCls + '-{id}" class="' + this.cls + ' ' + this.uniqueCls + ' {Cls}" style="' + (scheduler.rtl ? 'right' : 'left') + ':{start}px;width:{width}px;top:{start}px;height:{width}px;{style}">' +
                    (innerTpl ? '{[this.renderInner(values)]}' : '') +

                    '</div>' +
                    '</tpl>',
                {
                    renderInner : function(values) {
                        return innerTpl.apply(values);
                    }
                }
            );
        }

        this.storeListeners = {
            load            : this.fullRefresh,
            datachanged     : this.fullRefresh,
            clear           : this.fullRefresh,

            // Ext JS
            add             : this.fullRefresh,
            remove          : this.fullRefresh,
            update          : this.refreshSingle,

            // Sencha Touch
            addrecords      : this.fullRefresh,
            removerecords   : this.fullRefresh,
            updaterecord    : this.refreshSingle,

            scope           : this
        };

        this.store.on(this.storeListeners);
    },

    onSchedulerDestroy : function() {
        this.store.un(this.storeListeners);
    },

    fullRefresh : function() {
        this.scheduler.getSchedulingView().refresh();
    },

    renderer : function (val, meta, resource, rowIndex) {
        if (this.scheduler.getOrientation() === 'horizontal' ||
            rowIndex === 0)
        {
            return this.renderZones(resource);
        }

        return '';
    },

    renderZones : function (resource) {
        var store = this.store,
            scheduler = this.scheduler,
            viewStart = scheduler.timeAxis.getStart(),
            viewEnd = scheduler.timeAxis.getEnd(),
            data = [],
            spanStartDate, spanEndDate;

        store.each(function (record) {
            spanStartDate = record.getStartDate();
            spanEndDate = record.getEndDate();

            // Make sure resource exists in resourceStore (filtering etc)
            if (record.getResource() === resource && spanStartDate && spanEndDate &&
                // Make sure this zone is inside current view
                Sch.util.Date.intersectSpans(spanStartDate, spanEndDate, viewStart, viewEnd))
            {
                var renderData = scheduler.getSchedulingView()[scheduler.getOrientation()].getEventRenderData(record);
                var start, width;

                if (scheduler.getOrientation() === 'horizontal') {
                    start = scheduler.rtl ? renderData.right : renderData.left;
                    width = renderData.width;
                } else {
                    start = renderData.top;
                    width = renderData.height;
                }

                data[data.length] = Ext.apply({
                    id      : record.internalId,

                    start   : start,
                    width   : width,

                    Cls     : record.getCls()
                }, record.data);
            }
        });

        return this.template.apply(data);
    },

    refreshSingle : function(store, record) {
        var el = Ext.get(this.uniqueCls + '-' + record.internalId);

        if (el) {
            var scheduler = this.scheduler,
                viewStart = scheduler.timeAxis.getStart(),
                viewEnd = scheduler.timeAxis.getEnd();

            var start       = Sch.util.Date.max(viewStart, record.getStartDate()),
                end         = Sch.util.Date.min(viewEnd, record.getEndDate()),
                cls         = record.getCls();

            var startPos = scheduler.getSchedulingView().getCoordinateFromDate(start);
            var width = scheduler.getSchedulingView().getCoordinateFromDate(end) - startPos;

            // Reapply CSS classes
            el.dom.className = this.cls + ' ' + this.uniqueCls + ' ' + (cls || '');

            el.setStyle({ left : startPos+'px', top : startPos+'px', height : width+'px', width : width+'px' });
        }
    }
});
/**
@class Sch.widget.ResizePicker
@private
@extends Ext.Panel

Size picker widget for changing columns width/rows height.

*/
Ext.define('Sch.widget.ResizePicker', {
    extend          : 'Ext.Panel',
    alias           : 'widget.dualrangepicker',
    width           : 200,
    height          : 200,
    border          : true,
    collapsible     : false,
    bodyStyle       : 'position:absolute; margin:5px',

    verticalCfg     : {
        height      : 120,
        value       : 24,
        increment   : 2,
        minValue    : 20,
        maxValue    : 80,
        reverse     : true,
        disabled    : true
    },

    horizontalCfg   : {
        width       : 120,
        value       : 100,
        minValue    : 25,
        increment   : 5,
        maxValue    : 200,
        disable     : true
    },

    initComponent : function () {
        var me = this;

        me.addEvents('change', 'changecomplete', 'select');

        me.horizontalCfg.value  = me.dialogConfig.columnWidth;
        me.verticalCfg.value    = me.dialogConfig.rowHeight;
        me.verticalCfg.disabled = me.dialogConfig.scrollerDisabled || false;

        me.dockedItems = [
            me.vertical     = new Ext.slider.Single(Ext.apply({
                dock        : 'left',
                style       : 'margin-top:10px',
                vertical    : true,
                listeners   : {
                    change          : me.onSliderChange,
                    changecomplete  : me.onSliderChangeComplete,
                    scope           : me
                }
            }, me.verticalCfg)),

            me.horizontal   = new Ext.slider.Single(Ext.apply({
                dock        : 'top',
                style       : 'margin-left:28px',
                listeners   : {
                    change          : me.onSliderChange,
                    changecomplete  : me.onSliderChangeComplete,
                    scope           : me
                }
            }, me.horizontalCfg))
        ];

        me.callParent(arguments);
    },

    afterRender : function () {
        var me = this;

        me.addCls('sch-ux-range-picker');
        me.valueHandle = this.body.createChild({
            cls : 'sch-ux-range-value',
            cn  : {
                tag: 'span'
            }
        });
        me.valueSpan = this.valueHandle.down('span');

        var dd = new Ext.dd.DD(this.valueHandle);

        Ext.apply(dd, {
            startDrag   : function () {
                me.dragging = true;
                this.constrainTo(me.body);
            },
            onDrag      : function () {
                me.onHandleDrag.apply(me, arguments);
            },
            endDrag     : function () {
                me.onHandleEndDrag.apply(me, arguments);
                me.dragging = false;
            },
            scope       : this
        });

        this.setValues(this.getValues());
        this.callParent(arguments);

        this.body.on('click', this.onBodyClick, this);
    },

    onBodyClick: function (e, t) {
        var xy = [e.getXY()[0] - 8 - this.body.getX(), e.getXY()[1] - 8 - this.body.getY()];

        this.valueHandle.setLeft(Ext.Number.constrain(xy[0], 0, this.getAvailableWidth()));
        this.valueHandle.setTop(Ext.Number.constrain(xy[1], 0, this.getAvailableHeight()));

        this.setValues(this.getValuesFromXY([this.valueHandle.getLeft(true), this.valueHandle.getTop(true)]));
        this.onSliderChangeComplete();
    },

    getAvailableWidth: function () {
        return this.body.getWidth() - 18;
    },

    getAvailableHeight: function () {
        return this.body.getHeight() - 18;
    },

    onHandleDrag: function () {
        this.setValues(this.getValuesFromXY([this.valueHandle.getLeft(true), this.valueHandle.getTop(true)]));
    },

    onHandleEndDrag: function () {
        this.setValues(this.getValuesFromXY([this.valueHandle.getLeft(true), this.valueHandle.getTop(true)]));
    },

    getValuesFromXY: function (xy) {
        var xFraction = xy[0] / this.getAvailableWidth();
        var yFraction = xy[1] / this.getAvailableHeight();

        var horizontalVal = Math.round((this.horizontalCfg.maxValue - this.horizontalCfg.minValue) * xFraction);
        var verticalVal = Math.round((this.verticalCfg.maxValue - this.verticalCfg.minValue) * yFraction) + this.verticalCfg.minValue;

        return [horizontalVal + this.horizontalCfg.minValue, verticalVal];
    },

    getXYFromValues: function (values) {
        var xRange = this.horizontalCfg.maxValue - this.horizontalCfg.minValue;
        var yRange = this.verticalCfg.maxValue - this.verticalCfg.minValue;

        var x = Math.round((values[0] - this.horizontalCfg.minValue) * this.getAvailableWidth() / xRange);

        var verticalVal = values[1] - this.verticalCfg.minValue;
        var y = Math.round(verticalVal * this.getAvailableHeight() / yRange);

        return [x, y];
    },

    updatePosition: function () {
        var values = this.getValues();
        var xy = this.getXYFromValues(values);

        this.valueHandle.setLeft(Ext.Number.constrain(xy[0], 0, this.getAvailableWidth()));
        if (this.verticalCfg.disabled){
            this.valueHandle.setTop(this.dialogConfig.rowHeight);
        } else {
            this.valueHandle.setTop(Ext.Number.constrain(xy[1], 0, this.getAvailableHeight()));
        }

        this.positionValueText();
        this.setValueText(values);
    },

    positionValueText: function () {
        var handleTop = this.valueHandle.getTop(true);
        var handleLeft = this.valueHandle.getLeft(true);

        this.valueSpan.setLeft(handleLeft > 30 ? -30 : 10);
        this.valueSpan.setTop(handleTop > 10 ? -20 : 20);
    },

    setValueText: function(values){
        if (this.verticalCfg.disabled) values[1] = this.dialogConfig.rowHeight;
        this.valueSpan.update('[' + values.toString() + ']');
    },

    setValues: function (values) {
        this.horizontal.setValue(values[0]);

        if (this.verticalCfg.reverse) {
            if (!this.verticalCfg.disabled) this.vertical.setValue(this.verticalCfg.maxValue + this.verticalCfg.minValue - values[1]);
        } else {
            if (!this.verticalCfg.disabled) this.vertical.setValue(values[1]);
        }

        if (!this.dragging) {
            this.updatePosition();
        }
        this.positionValueText();

        this.setValueText(values);
    },

    getValues: function () {
        if (!this.verticalCfg.disabled) {
            var verticalVal = this.vertical.getValue();

            if (this.verticalCfg.reverse) {
                verticalVal = this.verticalCfg.maxValue - verticalVal + this.verticalCfg.minValue;
            }

            return [this.horizontal.getValue(), verticalVal];

        }

        return [this.horizontal.getValue()];
    },

    onSliderChange: function () {
        this.fireEvent('change', this, this.getValues());

        if (!this.dragging) {
            this.updatePosition();
        }
    },

    onSliderChangeComplete: function () {
        this.fireEvent('changecomplete', this, this.getValues());
    },

    afterLayout: function () {
        this.callParent(arguments);
        this.updatePosition();
    }
});

/**
 @class Sch.widget.ExportDialogForm
 @private
 @extends Ext.form.Panel

 Form for {@link Sch.widget.ExportDialog}. This is a private class and can be overriden by providing `formPanel` option to
 {@link Sch.plugin.Export#cfg-exportDialogConfig exportDialogConfig}.
 */
Ext.define('Sch.widget.ExportDialogForm', {
    extend      : 'Ext.form.Panel',
    requires    : [
        'Ext.ProgressBar',
        'Ext.form.field.ComboBox',
        'Ext.form.field.Date',
        'Ext.form.FieldContainer',
        'Ext.form.field.Checkbox',
        'Sch.widget.ResizePicker'
    ],
    border      : false,
    bodyPadding : '10 10 0 10',
    autoHeight  : true,

    initComponent : function () {
        var me = this;

        // HACK
        // fix for tooltip width
        // http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text
        if (Ext.getVersion('extjs').isLessThan('4.2.1')) {
            if (typeof Ext.tip !== 'undefined' && Ext.tip.Tip && Ext.tip.Tip.prototype.minWidth != 'auto') {
                Ext.tip.Tip.prototype.minWidth      = 'auto';
            }
        }

        me.createFields();

        Ext.apply(this, {
            fieldDefaults   : {
                labelAlign  : 'left',
                labelWidth  : 120,
                anchor      : '99%'
            },
            items           : [
                me.rangeField,
                me.resizerHolder,
                me.datesHolder,
                me.showHeaderField,
                me.exportToSingleField,
                me.formatField,
                me.orientationField,

                me.progressBar || me.createProgressBar()
            ]
        });

        me.callParent(arguments);

        me.onRangeChange(null, me.dialogConfig.defaultConfig.range);

        me.on({
            hideprogressbar     : me.hideProgressBar,
            showprogressbar     : me.showProgressBar,
            updateprogressbar   : me.updateProgressBar,
            scope               : me
        });
    },

    isValid : function () {
        var me  = this;
        if (me.rangeField.getValue() === 'date') return me.dateFromField.isValid() && me.dateToField.isValid();

        return true;
    },

    getValues : function (asString, dirtyOnly, includeEmptyText, useDataValues) {
        var result      = this.callParent(arguments);

        var cellSize    = this.resizePicker.getValues();
        if (!asString) {
            result.cellSize = cellSize;
        } else {
            result += '&cellSize[0]='+cellSize[0]+'&cellSize[1]='+cellSize[1];
        }

        return result;
    },

    createFields : function () {
        var me                  = this,
            cfg                 = me.dialogConfig,
            beforeLabelTextTpl  = '<table class="sch-fieldcontainer-label-wrap"><td width="1" class="sch-fieldcontainer-label">',
            afterLabelTextTpl   = '<td><div class="sch-fieldcontainer-separator"></div></table>';

        me.rangeField = new Ext.form.field.ComboBox({
            value           : cfg.defaultConfig.range,
            triggerAction   : 'all',
            cls             : 'sch-export-dialog-range',
            forceSelection  : true,
            editable        : false,
            fieldLabel      : cfg.rangeFieldLabel,
            name            : 'range',
            queryMode       : 'local',
            displayField    : 'name',
            valueField      : 'value',
            store           : Ext.create('Ext.data.Store', {
                fields  : ['name', 'value'],
                data    : [
                    { name : cfg.completeViewText,  value : 'complete' },
                    { name : cfg.dateRangeText,     value : 'date' },
                    { name : cfg.currentViewText,   value : 'current' }
                ]
            }),
            listeners      : {
                change  : me.onRangeChange,
                scope   : me
            }
        });

        // col/row resizer
        me.resizePicker = new Sch.widget.ResizePicker({
            dialogConfig    : cfg,
            margin          : '10 20'
        });

        me.resizerHolder    = new Ext.form.FieldContainer({
            fieldLabel          : cfg.scrollerDisabled ? cfg.adjustCols : cfg.adjustColsAndRows,
            labelAlign          : 'top',
            hidden              : true,
            labelSeparator      : '',
            beforeLabelTextTpl  : beforeLabelTextTpl,
            afterLabelTextTpl   : afterLabelTextTpl,
            layout              : 'vbox',
            defaults            : {
                flex        : 1,
                allowBlank  : false
            },
            items               : [me.resizePicker]
        });

        // from date
        me.dateFromField = new Ext.form.field.Date({
            fieldLabel  : cfg.dateRangeFromText,
            baseBodyCls : 'sch-exportdialogform-date',
            name        : 'dateFrom',
            format      : cfg.dateRangeFormat || Ext.Date.defaultFormat,
            allowBlank  : false,
            maxValue    : cfg.endDate,
            minValue    : cfg.startDate,
            value       : cfg.startDate
        });

        // till date
        me.dateToField = new Ext.form.field.Date({
            fieldLabel  : cfg.dateRangeToText,
            name        : 'dateTo',
            format      : cfg.dateRangeFormat || Ext.Date.defaultFormat,
            baseBodyCls : 'sch-exportdialogform-date',
            allowBlank  : false,
            maxValue    : cfg.endDate,
            minValue    : cfg.startDate,
            value       : cfg.endDate
        });

        // date fields holder
        me.datesHolder  = new Ext.form.FieldContainer({
            fieldLabel          : cfg.specifyDateRange,
            labelAlign          : 'top',
            hidden              : true,
            labelSeparator      : '',
            beforeLabelTextTpl  : beforeLabelTextTpl,
            afterLabelTextTpl   : afterLabelTextTpl,
            layout              : 'vbox',
            defaults            : {
                flex        : 1,
                allowBlank  : false
            },
            items               : [me.dateFromField, me.dateToField]
        });

        me.showHeaderField = new Ext.form.field.Checkbox({
            xtype       : 'checkboxfield',
            boxLabel    : me.dialogConfig.showHeaderLabel,
            name        : 'showHeader',
            checked     : !!cfg.defaultConfig.showHeaderLabel
        });

        me.exportToSingleField = new Ext.form.field.Checkbox({
            xtype       : 'checkboxfield',
            boxLabel    : me.dialogConfig.exportToSingleLabel,
            name        : 'singlePageExport',
            checked     : !!cfg.defaultConfig.singlePageExport
        });

        me.formatField = new Ext.form.field.ComboBox({
            value          : cfg.defaultConfig.format,
            triggerAction  : 'all',
            forceSelection : true,
            editable       : false,
            fieldLabel     : cfg.formatFieldLabel,
            name           : 'format',
            queryMode      : 'local',
            store          : ["A5", "A4", "A3", "Letter", "Legal"]
        });

        var orientationLandscapeCls = cfg.defaultConfig.orientation === "portrait" ? 'class="sch-none"' : '',
            orientationPortraitCls = cfg.defaultConfig.orientation === "landscape" ? 'class="sch-none"' : '';

        me.orientationField = new Ext.form.field.ComboBox({
            value          : cfg.defaultConfig.orientation,
            triggerAction  : 'all',
            baseBodyCls    : 'sch-exportdialogform-orientation',
            forceSelection : true,
            editable       : false,
            fieldLabel     : me.dialogConfig.orientationFieldLabel,
            afterSubTpl    : new Ext.XTemplate('<span id="sch-exportdialog-imagePortrait" ' + orientationPortraitCls +
                '></span><span id="sch-exportdialog-imageLandscape" ' + orientationLandscapeCls + '></span>'),
            name           : 'orientation',
            displayField   : 'name',
            valueField     : 'value',
            queryMode      : 'local',
            store          : Ext.create('Ext.data.Store', {
                fields : ['name', 'value'],
                data   : [
                    { name : cfg.orientationPortraitText, value : 'portrait' },
                    { name : cfg.orientationLandscapeText, value : 'landscape' }
                ]
            }),
            listeners      : {
                change : function (field, newValue) {
                    switch (newValue) {
                        case 'landscape':
                            Ext.fly('sch-exportdialog-imagePortrait').toggleCls('sch-none');
                            Ext.fly('sch-exportdialog-imageLandscape').toggleCls('sch-none');
                            break;
                        case 'portrait':
                            Ext.fly('sch-exportdialog-imagePortrait').toggleCls('sch-none');
                            Ext.fly('sch-exportdialog-imageLandscape').toggleCls('sch-none');
                            break;
                    }
                }
            }
        });
    },

    createProgressBar : function () {
        return this.progressBar = new Ext.ProgressBar({
            text    : this.config.progressBarText,
            animate : true,
            hidden  : true,
            margin  : '4px 0 10px 0'
        });
    },

    onRangeChange : function (field, newValue) {
        switch (newValue) {
            case 'complete':
                this.datesHolder.hide();
                this.resizerHolder.hide();
                break;
            case 'date':
                this.datesHolder.show();
                this.resizerHolder.hide();
                break;
            case 'current':
                this.datesHolder.hide();
                this.resizerHolder.show();
                this.resizePicker.expand(true);
                break;
        }
    },

    showProgressBar : function () {
        if (this.progressBar) this.progressBar.show();
    },

    hideProgressBar : function () {
        if (this.progressBar) this.progressBar.hide();
    },

    updateProgressBar : function (value) {
        if (this.progressBar) this.progressBar.updateProgress(value);
    }
});

/**
 @class Sch.widget.ExportDialog
 @private
 @extends Ext.window.Window

 Widget for export options.

 */
Ext.define('Sch.widget.ExportDialog', {
    alternateClassName  : 'Sch.widget.PdfExportDialog',
    extend              : 'Ext.window.Window',
    requires            : ['Sch.widget.ExportDialogForm'],
    mixins              : ['Sch.mixin.Localizable'],
    alias               : "widget.exportdialog",

    //Panel settings. Overridable with {@link Sch.plugin.Export#cfg-exportDialogConfig}
    modal               : false,
    width               : 350,
    cls                 : 'sch-exportdialog',
    frame               : false,
    layout              : 'fit',
    draggable           : true,
    padding             : 0,

    //Private
    plugin              : null,

    /**
     * @cfg {Ext.Component} buttonsPanel Component with buttons controlling export.
     */
    buttonsPanel        : null,

    /**
     * @cfg {Object} buttonsPanelScope
     * The scope for the {@link #buttonsPanel}
     */
    buttonsPanelScope   : null,

    /**
     * @cfg {Ext.Component} progressBar Progress bar component.
     */
    progressBar         : null,

    /**
     * @cfg {String} generalError Text used for displaying errors, when no error message was returned from the server.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} formatFieldLabel Text used as a label for the paper format setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} orientationFieldLabel Text used as a label for the orientation setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} rangeFieldLabel Text used as a label for the export range setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} showHeaderLabel Text used as a label for the showing/hiding of page numbers checkbox.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} exportToSingleLabel Text used as a label for the checkbox defining if export should create single page.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} orientationPortraitText Text used for the portrait orientation setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} orientationLandscapeText Text used for the landscape orientation setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} completeViewText Text used for the complete view export range setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} currentViewText Text used for the current view export range setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} dateRangeText Text used for the date range export range setting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} dateRangeFromText Text indicating the start of timespan when exporting a date range.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} pickerText Text used as a legend for the row/column picker.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} dateRangeToText Text indicating the end of timespan when exporting a date range.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} exportButtonText Text displayed on the button starting the export operation.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} cancelButtonText Text displayed on the button cancelling the export and hiding the dialog.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {String} progressBarText Text displayed on the progress bar while exporting.
     * @deprecated Please use {@link #l10n l10n} instead.
     */
    /**
     * @cfg {Object} l10n
     * A object, purposed for the class localization. Contains the following keys/values:

            - generalError                : 'An error occured, try again.',
            - title                       : 'Export Settings',
            - formatFieldLabel            : 'Paper format',
            - orientationFieldLabel       : 'Orientation',
            - rangeFieldLabel             : 'Export range',
            - showHeaderLabel             : 'Add page number',
            - orientationPortraitText     : 'Portrait',
            - orientationLandscapeText    : 'Landscape',
            - completeViewText            : 'Complete schedule',
            - currentViewText             : 'Current view',
            - dateRangeText               : 'Date range',
            - dateRangeFromText           : 'Export from',
            - pickerText                  : 'Resize column/rows to desired value',
            - dateRangeToText             : 'Export to',
            - exportButtonText            : 'Export',
            - cancelButtonText            : 'Cancel',
            - progressBarText             : 'Exporting...',
            - exportToSingleLabel         : 'Export as single page'
     */

    /**
     * @cfg {String} dateRangeFormat Valid date format to be used by the date ranges fields.
     */
    dateRangeFormat : '',

    constructor : function (config) {
        Ext.apply(this, config.exportDialogConfig);

        Ext.Array.forEach(
            [
                'generalError',
                'title',
                'formatFieldLabel',
                'orientationFieldLabel',
                'rangeFieldLabel',
                'showHeaderLabel',
                'orientationPortraitText',
                'orientationLandscapeText',
                'completeViewText',
                'currentViewText',
                'dateRangeText',
                'dateRangeFromText',
                'pickerText',
                'dateRangeToText',
                'exportButtonText',
                'cancelButtonText',
                'progressBarText',
                'exportToSingleLabel'
            ],
            function (prop) {
                if (prop in config) this[prop] = config[prop];
            },
            this
        );

        this.title = this.L('title');

        //store fields texts in the config object for further use by form
        this.config = Ext.apply({
            progressBarText          : this.L('progressBarText'),
            dateRangeToText          : this.L('dateRangeToText'),
            pickerText               : this.L('pickerText'),
            dateRangeFromText        : this.L('dateRangeFromText'),
            dateRangeText            : this.L('dateRangeText'),
            currentViewText          : this.L('currentViewText'),
            formatFieldLabel         : this.L('formatFieldLabel'),
            orientationFieldLabel    : this.L('orientationFieldLabel'),
            rangeFieldLabel          : this.L('rangeFieldLabel'),
            showHeaderLabel          : this.L('showHeaderLabel'),
            exportToSingleLabel      : this.L('exportToSingleLabel'),
            orientationPortraitText  : this.L('orientationPortraitText'),
            orientationLandscapeText : this.L('orientationLandscapeText'),
            completeViewText         : this.L('completeViewText'),
            adjustCols               : this.L('adjustCols'),
            adjustColsAndRows        : this.L('adjustColsAndRows'),
            specifyDateRange         : this.L('specifyDateRange'),
            dateRangeFormat          : this.dateRangeFormat,
            defaultConfig            : this.defaultConfig
        }, config.exportDialogConfig);

        this.callParent(arguments);
    },

    initComponent : function () {
        var me          = this,
            listeners   = {
                hidedialogwindow    : me.destroy,
                showdialogerror     : me.showError,
                updateprogressbar   : function (value) {
                    me.fireEvent('updateprogressbar', value);
                },
                scope               : this
            };

        me.form         = me.buildForm(me.config);

        Ext.apply(this, {
            items : me.form,
            fbar  : me.buildButtons(me.buttonsPanelScope || me)
        });

        me.callParent(arguments);

        me.plugin.on(listeners);
    },

    afterRender : function () {
        var me = this;

        me.relayEvents(me.form.resizePicker, ['change', 'changecomplete', 'select']);

        me.form.relayEvents(me, ['updateprogressbar', 'hideprogressbar', 'showprogressbar']);

        me.callParent(arguments);
    },

    /**
     * Create Dialog's buttons.
     *
     * @param {Object} buttonsScope Scope for the buttons.
     * @return {Object} buttons Object containing buttons for Exporting/Cancelling export.
     */
    buildButtons : function (buttonsScope) {
        return [
            {
                xtype   : 'button',
                scale   : 'medium',
                text    : this.L('exportButtonText'),
                handler : function () {
                    if (this.form.isValid()) {
                        this.fireEvent('showprogressbar');
                        this.plugin.doExport(this.form.getValues());
                    }
                },
                scope   : buttonsScope
            },
            {
                xtype   : 'button',
                scale   : 'medium',
                text    : this.L('cancelButtonText'),
                handler : function () {
                    this.destroy();
                },
                scope   : buttonsScope
            }
        ];
    },

    /**
     * Build the {@link Sch.widget.ExportDialogForm} for the dialog window.
     *
     * @param {Object} config Config object for the form, containing field names and values.
     * @return {@link Sch.widget.ExportDialogForm} form
     */
    buildForm : function (config) {
        return new Sch.widget.ExportDialogForm({
            progressBar  : this.progressBar,
            dialogConfig : config
        });
    },

    /**
     * @private
     * Displays error message in the dialog. When it's called, both form and buttons are hidden. Dialog will be automatically
     * hidden after {@link #hideTime} time.
     *
     * @param {String} (optional) error Text of the message that will be displayed in the dialog. If not provided, {@link #generalError}
     * will be used.
     */
    showError : function (dialog, error) {
        var me = dialog,
            text = error || me.L('generalError');

        me.fireEvent('hideprogressbar');
        Ext.Msg.alert('', text);
    }
});

/**
@class Sch.feature.ColumnLines
@extends Sch.plugin.Lines

A simple feature adding column lines (to be used when using the SingleTimeAxis column).

*/
Ext.define("Sch.feature.ColumnLines", {
    extend : 'Sch.plugin.Lines',

    cls : 'sch-column-line',
    
    showTip : false,

    requires : [
        'Ext.data.JsonStore'
    ],
    
    init : function (panel) {
        this.timeAxis = panel.getTimeAxis();
        this.panel = panel;

        this.store = new Ext.data.JsonStore({
            fields   : [ 'Date' ]
        });

        // Sencha Touch normalization
        this.store.loadData = this.store.loadData || this.store.setData;

        this.callParent(arguments);

        panel.on({
            orientationchange   : this.populate,
            destroy             : this.onHostDestroy,
            scope               : this
        });

        this.timeAxis.on('reconfigure', this.populate, this);
        
        this.populate();
    },

    onHostDestroy : function() {
        this.timeAxis.un('reconfigure', this.populate, this);
    },

    populate: function() {
        this.store.loadData(this.getData());
    },
    
    getElementData : function() {
        var sv = this.schedulerView;

        if (sv.isHorizontal() && sv.store.getCount() > 0) {
            return this.callParent(arguments);
        }

        return [];
    },

    getData : function() {
        var panel = this.panel,
            ticks = [];

        if (panel.isHorizontal()) {
            this.timeAxis.forEachMainInterval(function(start, end, i) {
                if (i > 0) {
                    ticks.push({ Date : start });
                }
            });
        }

        return ticks;
    }
});
/**
@class Sch.mixin.AbstractTimelineView
@private

 A base mixin for giving to the consuming view "time line" functionality.
This means that the view will be capable to display a list of "events", ordered on the {@link Sch.data.TimeAxis time axis}.

This class should not be used directly.

*/
Ext.define("Sch.mixin.AbstractTimelineView", {
    requires: [
        'Sch.data.TimeAxis',
        'Sch.view.Horizontal'
    ],

    /**
    * @cfg {String} selectedEventCls
    * A CSS class to apply to an event in the view on mouseover (defaults to 'sch-event-selected').
    */
    selectedEventCls : 'sch-event-selected',

    // private
    readOnly            : false,
    horizontalViewClass : 'Sch.view.Horizontal',

    //    can not "declare" it here, because will conflict with the default value from  SchedulerView
    //    verticalViewClass   : null,

    timeCellCls         : 'sch-timetd',
    timeCellSelector    : '.sch-timetd',

    eventBorderWidth        : 1,

    timeAxis            : null,
    timeAxisViewModel   : null,
    
    eventPrefix         : null,
    
    rowHeight           : null,
//    can not "declare" it here, because will conflict with the default value from  SchedulerView
//    barMargin           : null,
    
    orientation         : 'horizontal',

    altColCls : 'sch-col-alt',

    horizontal          : null,
    vertical            : null,
    
    secondaryCanvasEl   : null,
    
    panel               : null,
    
    displayDateFormat   : null,
    
    snapToIncrement     : null,

    // Accessor to the Ext.Element for this view
    el                  : null,    

    _initializeTimelineView         : function() { 
        if (this.horizontalViewClass) {
            this.horizontal = Ext.create(this.horizontalViewClass, { view : this });
        }

        if (this.verticalViewClass) {
            this.vertical = Ext.create(this.verticalViewClass, { view : this });
        }

        this.eventPrefix = (this.eventPrefix || this.getId()) + '-';
    },

    getTimeAxisViewModel : function () {
        return this.timeAxisViewModel;
    },

    /**
    * Method to get a formatted display date
    * @private
    * @param {Date} date The date
    * @return {String} The formatted date
    */
    getFormattedDate: function (date) {
        return Ext.Date.format(date, this.getDisplayDateFormat());
    },

    /**
    * Method to get a formatted end date for a scheduled event, the grid uses the "displayDateFormat" property defined in the current view preset.
    * @private
    * @param {Date} endDate The date to format
    * @param {Date} startDate The start date 
    * @return {String} The formatted date
    */
    getFormattedEndDate: function (endDate, startDate) {
        var format = this.getDisplayDateFormat();

        if (
                // If time is midnight,
                endDate.getHours() === 0 && endDate.getMinutes() === 0 &&

                // and end date is greater then start date
                !(endDate.getYear() === startDate.getYear() && endDate.getMonth() === startDate.getMonth() && endDate.getDate() === startDate.getDate()) &&

                // and UI display format doesn't contain hour info (in this case we'll just display the exact date)
                !Sch.util.Date.hourInfoRe.test(format.replace(Sch.util.Date.stripEscapeRe, ''))
            )
        {
            // format the date inclusively as 'the whole previous day'.
            endDate = Sch.util.Date.add(endDate, Sch.util.Date.DAY, -1);
        }
                
        return Ext.Date.format(endDate, format);
    },

    // private
    getDisplayDateFormat: function () {
        return this.displayDateFormat;
    },

    // private
    setDisplayDateFormat: function (format) {
        this.displayDateFormat = format;
    },
   

    /**
    * This function fits the schedule area columns into the available space in the grid.
    * @param {Boolean} preventRefresh `true` to prevent a refresh of view
    */ 
    fitColumns: function (preventRefresh) { // TODO test
        if (this.orientation === 'horizontal') {
            this.getTimeAxisViewModel().fitToAvailableWidth(preventRefresh);
        } else {
            var w = Math.floor((this.panel.getWidth() - Ext.getScrollbarSize().width - 1) / this.headerCt.getColumnCount());
            this.setColumnWidth(w, preventRefresh);
        }
    },
    
    /**
    * <p>Returns the Ext Element representing an event record</p> 
    * @param {Sch.model.Event} record The event record
    * @return {Ext.Element} The Ext.Element representing the event record
    */
    getElementFromEventRecord: function (record) {
        return Ext.get(this.eventPrefix + record.internalId);
    },
        
    getEventNodeByRecord: function(record) {
        return document.getElementById(this.eventPrefix + record.internalId);
    },

    getEventNodesByRecord: function(record) {
        return this.el.select("[id=" + this.eventPrefix + record.internalId + "]");
    },

    /**
    * Gets the start and end dates for an element Region
    * @param {Region} region The region to map to start and end dates
    * @param {String} roundingMethod The rounding method to use
    * @returns {Object} an object containing start/end properties
    */
    getStartEndDatesFromRegion: function (region, roundingMethod, allowPartial) {
        return this[this.orientation].getStartEndDatesFromRegion(region, roundingMethod, allowPartial);
    },

    
    /**
    * Returns the current time resolution object, which contains a unit identifier and an increment count.
    * @return {Object} The time resolution object
    */
    getTimeResolution: function () {
        return this.timeAxis.getResolution();
    },

    /**
    * Sets the current time resolution, composed by a unit identifier and an increment count.
    * @return {Object} The time resolution object
    */
    setTimeResolution: function (unit, increment) {
        this.timeAxis.setResolution(unit, increment);

        // View will have to be updated to support snap to increment
        if (this.snapToIncrement) {
            this.refreshKeepingScroll();
        }
    },

    /**
    * <p>Returns the event id for a DOM id </p>
    * @private
    * @param {String} id The id of the DOM node
    * @return {Sch.model.Event} The event record
    */
    getEventIdFromDomNodeId: function (id) {
        return id.substring(this.eventPrefix.length);
    },

     
    /**
    *  Gets the time for a DOM event such as 'mousemove' or 'click'
    *  @param {Ext.EventObject} e, the EventObject instance
    *  @param {String} roundingMethod (optional), 'floor' to floor the value or 'round' to round the value to nearest increment
    *  @returns {Date} The date corresponding to the EventObject x coordinate
    */
    getDateFromDomEvent : function(e, roundingMethod) {
        return this.getDateFromXY(e.getXY(), roundingMethod);
    },

    /**
    * [Experimental] Returns the pixel increment for the current view resolution.
    * @return {Int} The width increment
    */
    getSnapPixelAmount: function () {
        return this.getTimeAxisViewModel().getSnapPixelAmount();
    },

    // @deprecated
    getTimeColumnWidth : function() {
        return this.getTimeAxisViewModel().getTickWidth();
    },

    /**
    * Controls whether the scheduler should snap to the resolution when interacting with it.
    * @param {Boolean} enabled true to enable snapping when interacting with events.
    */
    setSnapEnabled: function (enabled) {
        this.snapToIncrement = enabled;

        if (enabled) {
            this.refreshKeepingScroll();
        }
    },

    /**
    * Sets the readonly state which limits the interactivity (resizing, drag and drop etc).
    * @param {Boolean} readOnly The new readOnly state
    */
    setReadOnly: function (readOnly) {
        this.readOnly = readOnly;
        this[readOnly ? 'addCls' : 'removeCls'](this._cmpCls + '-readonly');
    },

    /**
    * Returns true if the view is currently readOnly.
    * @return {Boolean} readOnly 
    */
    isReadOnly: function () {
        return this.readOnly;
    },

        
    /**
    * Sets the current orientation.
    * 
    * @param {String} orientation Either 'horizontal' or 'vertical'
    */
    setOrientation : function(orientation) {
        this.orientation = orientation; 
    },

    /**
    * Returns the current view orientation
    * @return {String} The view orientation ('horizontal' or 'vertical')
    */
    getOrientation: function () {
        return this.orientation;
    },
    
    isHorizontal : function() {
        return this.getOrientation() === 'horizontal';
    },


    isVertical : function() {
        return !this.isHorizontal();
    },
       
    /**
    * Gets the date for an XY coordinate
    * @param {Array} xy The page X and Y coordinates
    * @param {String} roundingMethod The rounding method to use
    * @param {Boolean} local, true if the coordinate is local to the scheduler view element 
    * @returns {Date} the Date corresponding to the xy coordinate
    */
    getDateFromXY: function (xy, roundingMethod, local) {
        return this.getDateFromCoordinate(this.orientation === 'horizontal' ? xy[0] : xy[1], roundingMethod, local);
    },

    /**
    * Gets the date for an X or Y coordinate, either local to the view element or the page based on the 3rd argument.
    * @param {Int} coordinate The X or Y coordinate
    * @param {String} roundingMethod The rounding method to use
    * @param {Boolean} local, true if the coordinate is local to the scheduler view element 
    * @returns {Date} the Date corresponding to the xy coordinate
    */
    getDateFromCoordinate: function (coord, roundingMethod, local) {
        if (!local) {
            coord = this[this.orientation].translateToScheduleCoordinate(coord);
        }
        return this.timeAxisViewModel.getDateFromPosition(coord, roundingMethod);
    },

    /**
    * Gets the date for the passed X coordinate.
    * If the coordinate is not in the currently rendered view, -1 will be returned.
    * @param {Int} x The X coordinate
    * @param {String} roundingMethod The rounding method to use
    * @returns {Date} the Date corresponding to the x coordinate
    * @abstract
    */
    getDateFromX: function (x, roundingMethod) {
        return this.getDateFromCoordinate(x, roundingMethod);
    },

    /**
    * Gets the date for the passed Y coordinate
    * If the coordinate is not in the currently rendered view, -1 will be returned.
    * @param {Int} y The Y coordinate
    * @param {String} roundingMethod The rounding method to use
    * @returns {Date} the Date corresponding to the y coordinate
    * @abstract
    */
    getDateFromY: function (y, roundingMethod) {
        return this.getDateFromCoordinate(y, roundingMethod);
    },

    /**
    *  Gets the x or y coordinate relative to the scheduling view element, or page coordinate (based on the 'local' flag)
    *  If the coordinate is not in the currently rendered view, -1 will be returned.
    *  @param {Date} date, the date to query for
    *  @param {Boolean} local, true to return a coordinate local to the scheduler view element (defaults to true)
    *  @returns {Int} the x or y position representing the date on the time axis
    */
    getCoordinateFromDate: function (date, local) {
        var pos = this.timeAxisViewModel.getPositionFromDate(date);

        if (local === false) {
            pos = this[this.orientation].translateToPageCoordinate(pos);
        }

        return Math.round(pos);
    },

    /**
    *  Gets the x coordinate relative to the scheduling view element, or page coordinate (based on the 'local' flag)
    *  @param {Date} date, the date to query for
    *  @param {Boolean} local, true to return a coordinate local to the scheduler view element (defaults to false)
    *  @returns {Array} the XY coordinates representing the date
    */
    getXFromDate: function (date, local) {
        return this.getCoordinateFromDate(date, local);
    },

    /**
    *  Gets xy coordinates relative to the scheduling view element, or page coordinates (based on the 'local' flag)
    *  @param {Date} xy, the page X and Y coordinates
    *  @param {Boolean} local, true to return a coordinate local to the scheduler view element 
    *  @returns {Array} the XY coordinates representing the date
    */
    getYFromDate: function (date, local) {
        return this.getCoordinateFromDate(date, local);
    },

    /**
    *  Returns the distance in pixels the for time span in the view.
    *  @param {Date} startDate The start date of the span
    *  @param {Date} endDate The end date of the span
    *  @return {Int} The distance in pixels
    */
    getTimeSpanDistance: function (startDate, endDate) {
        return this.timeAxisViewModel.getDistanceBetweenDates(startDate, endDate);
    },

    /**
    *  Returns the region for a "global" time span in the view. Coordinates are relative to element containing the time columns
    *  @param {Date} startDate The start date of the span
    *  @param {Date} endDate The end date of the span
    *  @return {Ext.util.Region} The region for the time span
    */
    getTimeSpanRegion: function (startDate, endDate) {
        return this[this.orientation].getTimeSpanRegion(startDate, endDate);
    },

    /**
    * Gets the Ext.util.Region represented by the schedule and optionally only for a single resource. The view will ask the scheduler for 
    * the resource availability by calling getResourceAvailability. By overriding that method you can constrain events differently for
    * different resources.
    * @param {Sch.model.Resource} resourceRecord (optional) The resource record 
    * @param {Sch.model.Event} eventRecord (optional) The event record 
    * @return {Ext.util.Region} The region of the schedule
    */
    getScheduleRegion: function (resourceRecord, eventRecord) {
        return this[this.orientation].getScheduleRegion(resourceRecord, eventRecord);
    },

    // Returns the region of the table element containing the rows of the schedule
    getTableRegion : function () {
        throw 'Abstract method call';
    },

    // Returns the table element containing the rows of the schedule
    getRowNode: function (resourceRecord) {
        throw 'Abstract method call';
    },

    getRecordForRowNode : function(node) {
        throw 'Abstract method call';
    },
    
    /**
    * Method to get the currently visible date range in a scheduling view. Please note that it only works when the schedule is rendered.
    * @return {Object} object with `startDate` and `endDate` properties.
    */
    getVisibleDateRange: function () {
        return this[this.orientation].getVisibleDateRange();
    },

    setColumnWidth: function (width, preventRefresh) {
        this[this.orientation].setColumnWidth(width, preventRefresh);
    },

    findRowByChild : function(t) {
        throw 'Abstract method call';
    },

    /**
    * Sets the amount of margin to keep between bars and rows.
    * @param {Int} margin The new margin value
    * @param {Boolean} preventRefresh true to skip refreshing the view
    */
    setBarMargin: function (margin, preventRefresh) {
        this.barMargin = margin;
        if (!preventRefresh) {
            this.refreshKeepingScroll();
        }
    },

    /**
     * Returns the current row height used by the view (only applicable in a horizontal view)
     * @return {Boolean} The row height
     */
    getRowHeight: function () {
        return this.rowHeight;
    },

    /**
    * Sets the height of row
    * @param {Number} height The height to set
    * @param {Boolean} preventRefresh `true` to prevent view refresh
    */
    setRowHeight: function (height, preventRefresh) {
        this.rowHeight = height || 24;

        if (this.orientation === 'vertical') {
            this.timeAxisViewModel.setTickWidth(this.rowHeight, preventRefresh);
        } else {
            if (this.getEl() && !preventRefresh) {
                this.refreshKeepingScroll();
            }
        }
    },
    
    /**
    * Refreshes the view and maintains the scroll position.
    */
    refreshKeepingScroll : function() {
        throw 'Abstract method call';
    },

    /**
     * Scrolls the view vertically
     * @param {Int} y The y-value to scroll to
     * @param {Boolean/Object} animate The animation config, or true/false
     */
    scrollVerticallyTo : function(y, animate) {
        throw 'Abstract method call';
    },

    /**
     * Scrolls the view horizontally
     * @param {Int} x The x-value to scroll to
     * @param {Boolean/Object} animate The animation config, or true/false
     */
    scrollHorizontallyTo : function(x, animate) {
        throw 'Abstract method call';
    },

    /**
     * Returns the current vertical scroll value
     */
    getVerticalScroll : function() {
        throw 'Abstract method call';
    },

    /**
     * Returns the current horizontal scroll value
     */
    getHorizontalScroll : function() {
        throw 'Abstract method call';
    },

    // This method should be implemented by the consuming class
    getEl : Ext.emptyFn,
    
    
    // returns a secondary canvas el - the el to be used for drawing column lines, zones etc
    getSecondaryCanvasEl : function () {
        if (!this.secondaryCanvasEl) {
            this.secondaryCanvasEl = this.getEl().createChild({ cls : 'sch-secondary-canvas' });
        }
        return this.secondaryCanvasEl;
    },

    /**
     * Returns the current viewport scroll position as an object with left/top properties.
     */
    getScroll : function() {
        throw 'Abstract method call';
    },

    getOuterEl : function() {
        return this.getEl();
    },

    getRowContainerEl : function() {
        return this.getEl();
    },

    getScheduleCell : function(row, col) {
        return this.getCellByPosition({ row : row, column : col});
    },
    
    
    getScrollEventSource : function () {
        return this.getEl();
    },

    getViewportHeight : function () {
        return this.getEl().getHeight();
    },

    getViewportWidth : function () {
        return this.getEl().getWidth();
    },

    getDateConstraints : Ext.emptyFn
});


Ext.apply(Sch, {
    /*PKGVERSION*/VERSION : '2.2.15'
});
/**
@class Sch.mixin.TimelineView

A base mixin for {@link Ext.view.View} classes, giving to the consuming view the "time line" functionality.
This means that the view will be capable to display a list of "events", ordered on the {@link Sch.data.TimeAxis time axis}.

By itself this mixin is not enough for correct rendering. The class, consuming this mixin, should also consume one of the
{@link Sch.view.Horizontal} or {@link Sch.view.Vertical} mixins, which provides the implementation of some orientation-specfic methods.

Generally, should not be used directly, if you need to subclass the view, subclass the {@link Sch.view.SchedulerGridView} instead.

*/
Ext.define("Sch.mixin.TimelineView", {
    extend : 'Sch.mixin.AbstractTimelineView',

    requires : ['Sch.patches.ElementScroll'],

    /**
    * @cfg {String} overScheduledEventClass
    * A CSS class to apply to each event in the view on mouseover (defaults to 'sch-event-hover').
    */
    overScheduledEventClass: 'sch-event-hover',

    ScheduleEventMap    : {
        click           : 'Click',
        mousedown       : 'MouseDown',
        mouseup         : 'MouseUp',
        dblclick        : 'DblClick',
        contextmenu     : 'ContextMenu',
        keydown         : 'KeyDown',
        keyup           : 'KeyUp'
    },

    _initializeTimelineView : function() {
        this.callParent(arguments);

        this.on('destroy', this._onDestroy, this);
        this.on('afterrender', this._onAfterRender, this);

        this.setOrientation(this.orientation);

        this.addEvents(
            /**
             * @event beforetooltipshow
             * Fires before the event tooltip is shown, return false to suppress it.
             * @param {Sch.mixin.SchedulerPanel} scheduler The scheduler object
             * @param {Sch.model.Event} eventRecord The event record corresponding to the rendered event
             */
            'beforetooltipshow',

            'columnwidthchange'
        );

        this.enableBubble('columnwidthchange');

        this.addCls("sch-timelineview");

        if (this.readOnly) {
            this.addCls(this._cmpCls + '-readonly');
        }

        this.addCls(this._cmpCls);

        if (this.eventAnimations) {
            this.addCls('sch-animations-enabled');
        }
    },

    inheritables : function() {
        return {
            // @OVERRIDE
            processUIEvent: function(e){
                var eventBarNode = e.getTarget(this.eventSelector),
                    map = this.ScheduleEventMap,
                    type = e.type,
                    preventViewEvent = false;

                if (eventBarNode && type in map) {
                    this.fireEvent(this.scheduledEventName + type, this, this.resolveEventRecord(eventBarNode), e);

                    // In Scheduler, clicking or interacting with an event should not trigger itemclick or other itemXXX events
                    // In gantt, a rendered bar corresponds to the row, so let view superclass process the event too
                    preventViewEvent = !(this.getSelectionModel() instanceof Ext.selection.RowModel);
                }

                if (!preventViewEvent) {
                    // For gantt, default actions should be executed too
                    return this.callParent(arguments);
                }
            }
        };
    },


    // private, clean up
    _onDestroy: function () {
        if (this.tip) {
            this.tip.destroy();
        }
    },

    _onAfterRender : function () {
        if (this.overScheduledEventClass) {
            this.setMouseOverEnabled(true);
        }

        if (this.tooltipTpl) {
            this.el.on('mousemove', this.setupTooltip, this, { single : true });
        }

        var bufferedRenderer    = this.bufferedRenderer;

        if (bufferedRenderer) {
            this.patchBufferedRenderingPlugin(bufferedRenderer);
            this.patchBufferedRenderingPlugin(this.lockingPartner.bufferedRenderer);
        }

        this.on('bufferedrefresh', this.onBufferedRefresh, this, { buffer : 10 });

        this.setupTimeCellEvents();

        // The `secondaryCanvasEl` needs to be setup early, for the underlying gridview to know about it 
        // and not remove it on later 'refresh' calls.
        this.getSecondaryCanvasEl();
    },


    patchBufferedRenderingPlugin : function (plugin) {
        var me                      = this;
        var oldSetBodyTop           = plugin.setBodyTop;

        // @OVERRIDE Overriding buffered renderer plugin
        plugin.setBodyTop           = function (bodyTop, calculatedTop) {
            if (bodyTop < 0) bodyTop = 0;

            var val                 = oldSetBodyTop.apply(this, arguments);

            me.fireEvent('bufferedrefresh', this);

            return val;
        };
    },



    onBufferedRefresh : function() {
        this.getSecondaryCanvasEl().dom.style.top = this.body.dom.style.top;
    },

    setMouseOverEnabled : function(enabled) {
        this[enabled ? "mon" : "mun"](this.el, {
            mouseover : this.onEventMouseOver,
            mouseout  : this.onEventMouseOut,
            delegate  : this.eventSelector,
            scope     : this
        });
    },

    // private
    onEventMouseOver: function (e, t) {
        if (t !== this.lastItem) {
            this.lastItem = t;

            Ext.fly(t).addCls(this.overScheduledEventClass);

            var eventModel      = this.resolveEventRecord(t);

            // do not fire this event if model can not be found
            // this can be the case for "sch-dragcreator-proxy" elements for example
            if (eventModel) this.fireEvent('eventmouseenter', this, eventModel, e);
        }
    },

    // private
    onEventMouseOut: function (e, t) {
        if (this.lastItem) {
            if (!e.within(this.lastItem, true, true)) {
                Ext.fly(this.lastItem).removeCls(this.overScheduledEventClass);
                this.fireEvent('eventmouseleave', this, this.resolveEventRecord(this.lastItem), e);
                delete this.lastItem;
            }
        }
    },

    // Overridden since locked grid can try to highlight items in the unlocked grid while it's loading/empty
    highlightItem: function(item) {
        if (item) {
            var me = this;
            me.clearHighlight();
            me.highlightedItem = item;
            Ext.fly(item).addCls(me.overItemCls);
        }
    },

    // private
    setupTooltip: function () {
        var me = this,
            tipCfg = Ext.apply({
                renderTo    : Ext.getBody(),
                delegate    : me.eventSelector,
                target      : me.el,
                anchor      : 'b',
                rtl         : me.rtl,

                show : function() {
                    Ext.ToolTip.prototype.show.apply(this, arguments);

                    // Some extra help required to correct alignment (in cases where event is in part outside the scrollable area
                    // https://www.assembla.com/spaces/bryntum/tickets/626#/activity/ticket:
                    if (this.triggerElement && me.getOrientation() === 'horizontal') {
                        this.setX(this.targetXY[0]-10);
                        this.setY(Ext.fly(this.triggerElement).getY()-this.getHeight()-10);
                    }
                }
            }, me.tipCfg);

        me.tip = Ext.create('Ext.ToolTip', tipCfg);

        me.tip.on({
            beforeshow: function (tip) {
                if (!tip.triggerElement || !tip.triggerElement.id) {
                    return false;
                }

                var record = this.resolveEventRecord(tip.triggerElement);

                if (!record || this.fireEvent('beforetooltipshow', this, record) === false) {
                    return false;
                }

                tip.update(this.tooltipTpl.apply(this.getDataForTooltipTpl(record)));
            },

            scope: this
        });
    },

    getTimeAxisColumn : function () {
        if (!this.timeAxisColumn) {
            this.timeAxisColumn = this.headerCt.down('timeaxiscolumn');
        }

        return this.timeAxisColumn;
    },

    /**
    * Template method to allow you to easily provide data for your {@link Sch.mixin.TimelinePanel#tooltipTpl} template.
    * @return {Mixed} The data to be applied to your template, typically any object or array.
    */
    getDataForTooltipTpl : function(record) {
        return Ext.apply({
            _record : record
        }, record.data);
    },

    /**
     * Refreshes the view and maintains the scroll position.
     */
    refreshKeepingScroll : function() {
        Ext.suspendLayouts();

        this.saveScrollState();

        this.refresh();

        if (this.up('tablepanel[lockable=true]').lockedGridDependsOnSchedule) {
            this.lockingPartner.refresh();
        }

        this.restoreScrollState();

        Ext.resumeLayouts(true);
    },

    setupTimeCellEvents: function () {
        this.mon(this.el, {
            // `handleScheduleEvent` is an abstract method, defined in "SchedulerView" and "GanttView"
            click       : this.handleScheduleEvent,
            dblclick    : this.handleScheduleEvent,
            contextmenu : this.handleScheduleEvent,

            scope       : this
        });
    },

    getTableRegion: function () {
        var tableEl = this.el.down('.' + Ext.baseCSSPrefix + 'grid-table');

        // Also handle odd timing cases where the table hasn't yet been inserted into the dom
        return (tableEl || this.el).getRegion();
    },

    // Returns the table element containing the rows of the schedule
    getRowNode: function (resourceRecord) {
        return this.getNodeByRecord(resourceRecord);
    },

    findRowByChild : function(t) {
        return this.findItemByChild(t);
    },

    getRecordForRowNode : function(node) {
        return this.getRecord(node);
    },

    /**
    * Refreshes the view and maintains the resource axis scroll position.
    */
    refreshKeepingResourceScroll : function() {
        var scroll = this.getScroll();

        this.refresh();

        if (this.getOrientation() === 'horizontal') {
            this.scrollVerticallyTo(scroll.top);
        } else {
            this.scrollHorizontallyTo(scroll.left);
        }
    },

    scrollHorizontallyTo : function(x, animate) {
        var el = this.getEl();
        if (el) {
            el.scrollTo('left', Math.max(0, x), animate);
        }
    },

    scrollVerticallyTo : function(y, animate) {
        var el = this.getEl();
        if (el) {
            el.scrollTo('top', Math.max(0,  y), animate);
        }
    },

    getVerticalScroll : function() {
        var el = this.getEl();
        return el.getScroll().top;
    },

    getHorizontalScroll : function() {
        var el = this.getEl();
        return el.getScroll().left;
    },

    getScroll : function() {
        var scroll = this.getEl().getScroll();

        return {
            top : scroll.top,
            left : scroll.left
        };
    },

    /**
     * @deprecated Use getCoordinateFromDate instead.
     * @BWCOMPAT 2.2
     */
    getXYFromDate : function() {
        var coord = this.getCoordinateFromDate.apply(this, arguments);

        return this.orientation === 'horizontal' ? [coord, 0] : [0, coord];
    },

    handleScheduleEvent : function (e) {
    }

});

/**

@class Sch.view.TimelineGridView
@extends Ext.grid.View
@mixin Sch.mixin.TimelineView

A grid view class, that consumes the {@link Sch.mixin.TimelineView} mixin. Used internally.

*/

Ext.define("Sch.view.TimelineGridView", {
    extend      : "Ext.grid.View",
    mixins      : ['Sch.mixin.TimelineView']
}, function() {
    this.override(Sch.mixin.TimelineView.prototype.inheritables() || {});
});
/**

@class Sch.mixin.AbstractSchedulerView
@private

A mixin for {@link Ext.view.View} classes, providing "scheduling" functionality to the consuming view. A consuming class
should have already consumed the {@link Sch.mixin.TimelineView} mixin.

Generally, should not be used directly, if you need to subclass the view, subclass the {@link Sch.view.SchedulerGridView} instead.

*/
Ext.define('Sch.mixin.AbstractSchedulerView', {
    requires: [
        'Sch.eventlayout.Horizontal'
    ],

    _cmpCls: 'sch-schedulerview',
    scheduledEventName: 'event',

    /**
    * @cfg {Int} barMargin
    * Controls how much space to leave between the event bars and the row borders.
    */
    barMargin: 1,

    /**
    * @cfg {Boolean} constrainDragToResource Set to true to only allow dragging events within the same resource.
    */
    constrainDragToResource: false,

    // Provided by panel
    allowOverlap    : null,
    readOnly        : null,

    /**
    * @cfg {Boolean} dynamicRowHeight
    * True to layout events without overlapping, meaning the row height will be dynamically calculated to fit any overlapping events.
    */
    dynamicRowHeight: true,

    /**
    * @cfg {Boolean} managedEventSizing
    * True to size events based on the rowHeight and barMargin settings. Set this to false if you want to control height and top properties via CSS instead.
    */
    managedEventSizing: true,

    /**
    * @cfg {Boolean} eventAnimations
    * True to animate event updates, currently only used in vertical orientation in CSS3 enabled browsers.
    */
    eventAnimations: true,

    eventCls        : 'sch-event',

    verticalViewClass : null,

    eventTpl: [
        '<tpl for=".">',
            '<div unselectable="on" id="{{evt-prefix}}{id}" style="right:{right}px;left:{left}px;top:{top}px;height:{height}px;width:{width}px;{style}" class="sch-event ' + Ext.baseCSSPrefix + 'unselectable {internalCls} {cls}">',
                '<div unselectable="on" class="sch-event-inner {iconCls}">',
                    '{body}',
                '</div>',
            '</div>',
        '</tpl>'
    ],

    eventStore              : null,
    resourceStore           : null,


    _initializeSchedulerView        : function() {
        this.eventSelector = '.' + this.eventCls;

        this.eventLayout = {};

        if (Sch.eventlayout.Horizontal) {
            this.eventLayout.horizontal = new Sch.eventlayout.Horizontal({ view : this });
        }

        if (Sch.eventlayout.Vertical) {
            this.eventLayout.vertical = new Sch.eventlayout.Vertical({ view : this });
        }

        this.store              = this.store || this.resourceStore;
        this.resourceStore      = this.resourceStore || this.store;
    },

    generateTplData: function (event, resourceRecord, resourceIndex) {
        var renderData = this[this.orientation].getEventRenderData(event),
            start       = event.getStartDate(),
            end         = event.getEndDate(),
            internalCls = event.getCls() || '';

        internalCls += ' sch-event-resizable-' + event.getResizable();

        if (event.dirty)                    internalCls += ' sch-dirty ';
        if (renderData.endsOutsideView)     internalCls += ' sch-event-endsoutside ';
        if (renderData.startsOutsideView)   internalCls += ' sch-event-startsoutside ';
        if (this.eventBarIconClsField)      internalCls += ' sch-event-withicon ';
        if (event.isDraggable() === false)  internalCls += ' sch-event-fixed ';
        if (end - start === 0)              internalCls += ' sch-event-milestone ';

        renderData.id          = event.internalId;
        renderData.internalCls = internalCls;
        renderData.start       = start;
        renderData.end         = end;
        renderData.iconCls     = event.data[this.eventBarIconClsField] || '';

        if (this.eventRenderer) {
            // User has specified a renderer fn, either to return a simple string, or an object intended for the eventBodyTemplate
            var value = this.eventRenderer.call(this.eventRendererScope || this, event, resourceRecord, renderData, resourceIndex);
            if (Ext.isObject(value) && this.eventBodyTemplate) {
                renderData.body = this.eventBodyTemplate.apply(value);
            } else {
                renderData.body = value;
            }
        } else if (this.eventBodyTemplate) {
            // User has specified an eventBodyTemplate, but no renderer - just apply the entire event record data.
            renderData.body = this.eventBodyTemplate.apply(event.data);
        } else if (this.eventBarTextField) {
            // User has specified a field in the data model to read from
            renderData.body = event.data[this.eventBarTextField] || '';
        }
        return renderData;
    },

    /**
    * Resolves the resource based on a dom element
    * @param {HtmlElement} node The HTML element
    * @return {Sch.model.Resource} The resource corresponding to the element, or null if not found.
    */
    resolveResource: function (node) {
        return this[this.orientation].resolveResource(node);
    },

    /**
    * Gets the Ext.util.Region representing the passed resource and optionally just for a certain date interval.
    * @param {Sch.model.Resource} resourceRecord The resource record
    * @param {Date} startDate A start date constraining the region
    * @param {Date} endDate An end date constraining the region
    * @return {Ext.util.Region} The region of the resource
    */
    getResourceRegion: function (resourceRecord, startDate, endDate) {
        return this[this.orientation].getResourceRegion(resourceRecord, startDate, endDate);
    },

    /**
    * <p>Returns the event record for a DOM element </p>
    * @param {HTMLElement/Ext.Element} el The DOM node or Ext Element to lookup
    * @return {Sch.model.Event} The event record
    */
    resolveEventRecord: function (el) {
        // Normalize to DOM node
        el = el.dom ? el.dom : el;

        if (!(Ext.fly(el).hasCls(this.eventCls))) {
            el = Ext.fly(el).up(this.eventSelector);
        }
        return this.getEventRecordFromDomId(el.id);
    },

    // DEPRECATED, remove for 3.0
    getResourceByEventRecord: function (eventRecord) {
        return eventRecord.getResource();
    },

    /**
    * <p>Returns the event record for a DOM id </p>
    * @param {String} id The id of the DOM node
    * @return {Sch.model.Event} The event record
    */
    getEventRecordFromDomId: function (id) {
        var trueId = this.getEventIdFromDomNodeId(id);
        return this.eventStore.getByInternalId(trueId);
    },


    /**
    * Checks if a date range is allocated or not for a given resource.
    * @param {Date} start The start date
    * @param {Date} end The end date
    * @param {Sch.model.Event} excludeEvent An event to exclude from the check (or null)
    * @param {Sch.model.Resource} resource The resource
    * @return {Boolean} True if the timespan is available for the resource
    */
    isDateRangeAvailable: function (start, end, excludeEvent, resource) {
        return this.eventStore.isDateRangeAvailable(start, end, excludeEvent, resource);
    },

    /**
    * Returns events that are (partly or fully) inside the timespan of the current view.
    * @return {Ext.util.MixedCollection} The collection of events
    */
    getEventsInView: function () {
        var viewStart = this.timeAxis.getStart(),
            viewEnd = this.timeAxis.getEnd();

        return this.eventStore.getEventsInTimeSpan(viewStart, viewEnd);
    },

    /**
    * Returns the current set of rendered event nodes
    * @return {CompositeElement} The collection of event nodes
    */
    getEventNodes: function () {
        return this.getEl().select(this.eventSelector);
    },

    onEventCreated: function (newEventRecord) {
        // Empty but provided so that you can override it to supply default record values etc.
    },

    getEventStore: function () {
        return this.eventStore;
    },

    registerEventEditor: function (editor) {
        this.eventEditor = editor;
    },

    getEventEditor: function () {
        return this.eventEditor;
    },

    // Call orientation specific implementation
    onEventUpdate: function (store, model, operation) {
        this[this.orientation].onEventUpdate(store, model, operation);
    },

    // Call orientation specific implementation
    onEventAdd: function (s, recs) {
        this[this.orientation].onEventAdd(s, recs);
    },

    // Call orientation specific implementation
    onEventRemove: function (s, recs) {
        this[this.orientation].onEventRemove(s, recs);
    },

    bindEventStore: function (eventStore, initial) {

        var me = this;
        var listenerCfg = {
            scope       : me,
            refresh     : me.onEventDataRefresh,

            // Sencha Touch
            addrecords      : me.onEventAdd,
            updaterecord    : me.onEventUpdate,
            removerecords   : me.onEventRemove,

            // Ext JS
            add         : me.onEventAdd,
            update      : me.onEventUpdate,
            remove      : me.onEventRemove
        };

        // Sencha Touch fires "refresh" when clearing the store. Avoid double repaints
        if (!Ext.versions.touch) {
            listenerCfg.clear = me.onEventDataRefresh;
        }

        if (!initial && me.eventStore) {
            me.eventStore.setResourceStore(null);

            if (eventStore !== me.eventStore && me.eventStore.autoDestroy) {
                me.eventStore.destroy();
            }
            else {
                if (me.mun) {
                    me.mun(me.eventStore, listenerCfg);
                } else {
                    me.eventStore.un(listenerCfg);
                }
            }

            if (!eventStore) {
                if (me.loadMask && me.loadMask.bindStore) {
                    me.loadMask.bindStore(null);
                }
                me.eventStore = null;
            }
        }
        if (eventStore) {
            eventStore = Ext.data.StoreManager.lookup(eventStore);

            if (me.mon) {
                me.mon(eventStore, listenerCfg);
            } else {
                eventStore.on(listenerCfg);
            }
            if (me.loadMask && me.loadMask.bindStore) {
                me.loadMask.bindStore(eventStore);
            }

            me.eventStore = eventStore;

            eventStore.setResourceStore(me.resourceStore);
        }

        if (eventStore && !initial) {
            me.refresh();
        }
    },

    onEventDataRefresh: function () {
        this.refreshKeepingScroll();
    },

    // invoked by the selection model to maintain visual UI cues
    onEventSelect: function (record) {
        var nodes = this.getEventNodesByRecord(record);
        if (nodes) {
            nodes.addCls(this.selectedEventCls);
        }
    },

    // invoked by the selection model to maintain visual UI cues
    onEventDeselect: function (record) {
        var nodes = this.getEventNodesByRecord(record);
        if (nodes) {
            nodes.removeCls(this.selectedEventCls);
        }
    },

    refresh : function() {
        throw 'Abstract method call';
    },

    /**
    * Refreshes the events for a single resource
    * @param {Sch.model.Resource} resource
    */
    repaintEventsForResource : function(record) {
        throw 'Abstract method call';
    },

    /**
     * Refreshes all events in the scheduler view
     */
    repaintAllEvents : function() {
        this.refreshKeepingScroll();
    },

    /**
     *  Scrolls an event record into the viewport.
     *  If the resource store is a tree store, this method will also expand all relevant parent nodes to locate the event.
     *
     *  @param {Sch.model.Event} eventRec, the event record to scroll into view
     *  @param {Boolean/Object} highlight, either `true/false` or a highlight config object used to highlight the element after scrolling it into view
     *  @param {Boolean/Object} animate, either `true/false` or an animation config object used to scroll the element
     */
    scrollEventIntoView: function (eventRec, highlight, animate, callback, scope) {
        scope                   = scope || this;

        var me                  = this;
        var eventStore          = this.eventStore;

        var basicScroll         = function (el) {

            // HACK
            if (Ext.versions.extjs) {
                // After a time axis change, the header is resized and Ext JS TablePanel reacts to the size change.
                // Ext JS reacts after a short delay, so we cancel this task to prevent Ext from messing up the scroll sync
                me.up('panel').scrollTask.cancel();
            }

            el.scrollIntoView(me.el, true, animate);

            if (highlight) {
                if (typeof highlight === "boolean") {
                    el.highlight();
                } else {
                    el.highlight(null, highlight);
                }
            }

            // XXX callback will be called too early, need to wait for scroll & highlight to complete
            callback && callback.call(scope);
        };

        // If resource store is a TreeStore, make sure the resource is expanded all the way up first.
        if (Ext.data.TreeStore && this.resourceStore instanceof Ext.data.TreeStore) {
            var resources            = eventRec.getResources();
            // eventRec could be a Gantt task model, use getResources and just grab the first

            if (resources.length > 0 && !resources[0].isVisible()) {
                resources[0].bubble(function (node) { node.expand(); });
            }
        }

        var timeAxis        = this.timeAxis;
        var startDate       = eventRec.getStartDate();
        var endDate         = eventRec.getEndDate();

        // If el is not in the currently viewed time span, change time span
        if (!timeAxis.dateInAxis(startDate) || !timeAxis.dateInAxis(endDate)) {
            var range = timeAxis.getEnd() - timeAxis.getStart();

            timeAxis.setTimeSpan(new Date(startDate.getTime() - range / 2), new Date(endDate.getTime() + range / 2));
        }

        var el = this.getElementFromEventRecord(eventRec);

        if (el) {
            basicScroll(el);
        } else {
            var verticalScroller    = this.panel.verticalScroller;
            // TODO handle buffering case
//            if (eventStore.buffered && verticalScroller) {
//
//                Ext.Function.defer(function () {
//                    verticalScroller.scrollTo(eventStore.getIndexInTotalDataset(eventRec), false, function () {
//                        // el should present now
//                        var el = me.getElementFromEventRecord(eventRec);
//
//                        if (el) basicScroll(el);
//                    });
//
//                }, isCollapsed ? 10 : 0);
//            }
        }
    }
});

/**

@class Sch.mixin.SchedulerView

A mixin for {@link Ext.view.View} classes, providing "scheduling" functionality to the consuming view. A consuming class
should have already consumed the {@link Sch.mixin.TimelineView} mixin.

Generally, should not be used directly, if you need to subclass the view, subclass the {@link Sch.view.SchedulerGridView} instead.

*/
Ext.define('Sch.mixin.SchedulerView', {
    extend : 'Sch.mixin.AbstractSchedulerView',

    requires: [
        'Sch.tooltip.Tooltip',
        'Sch.feature.DragCreator',
        'Sch.feature.DragDrop',
        'Sch.feature.ResizeZone',
        'Sch.column.Resource',
        'Sch.view.Vertical',
        'Sch.eventlayout.Vertical',
        'Ext.XTemplate'
    ],

    verticalViewClass : 'Sch.view.Vertical',

    /**
     * @property {Sch.feature.SchedulerDragZone} eventDragZone
     * Accessor to the event dragzone (available only if the drag drop feature is enabled)
     */

    /**
    * @cfg {String} eventResizeHandles Defines which resize handles to use. Possible values: 'none', 'start', 'end', 'both'. Defaults to 'end'
    */
    eventResizeHandles: 'end',

    /**
    * An empty function by default, but provided so that you can perform custom validation on
    * the item being dragged. This function is called during a drag and drop process and also after the drop is made
    * @param {[Sch.model.Event]} dragRecords an array containing the records for the events being dragged
    * @param {Sch.model.Resource} targetResourceRecord the target resource of the the event
    * @param {Date} date The date corresponding to the drag proxy position
    * @param {Int} duration The duration of the item being dragged in milliseconds
    * @param {Event} e The event object
    * @return {Boolean} true if the drop position is valid, else false to prevent a drop
    */
    dndValidatorFn: function (dragRecords, targetResourceRecord, date, duration, e) {
        return true;
    },

    /**
    * An empty function by default, but provided so that you can perform custom validation on
    * an item being resized.
    * @param {Sch.model.Resource} resourceRecord the resource of the row in which the event is located
    * @param {Sch.model.Event} eventRecord the event being resized
    * @param {Date} startDate
    * @param {Date} endDate
    * @param {Event} e The event object
    * @return {Boolean} true if the resize state is valid, else false
    */
    resizeValidatorFn: function (resourceRecord, eventRecord, startDate, endDate, e) {
        return true;
    },

    /**
    * An empty function by default, but provided so that you can perform custom validation on the item being created
    * @param {Sch.model.Resource} resourceRecord the resource for which the event is being created
    * @param {Date} startDate
    * @param {Date} endDate
    * @param {Event} e The event object
    * @return {Boolean} true if the creation event is valid, else false
    */
    createValidatorFn: function (resourceRecord, startDate, endDate, e) {
        return true;
    },

    _initializeSchedulerView : function() {
        this.callParent(arguments);

        this.on('destroy', this._destroy, this);
        this.on('afterrender', this._afterRender, this);

        // No row hover effect for the schedule
        this.trackOver = false;

        this.addEvents(
            // Scheduled events: click events --------------------------

            /**
             * @event eventclick
             * Fires when an event is clicked
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record of the clicked event
             * @param {Ext.EventObject} e The event object
             */
            'eventclick',

            /**
             * @event eventmousedown
             * Fires when a mousedown event is detected on a rendered event
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record
             * @param {Ext.EventObject} e The event object
             */
            'eventmousedown',

            /**
             * @event eventmouseup
             * Fires when a mouseup event is detected on a rendered event
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record
             * @param {Ext.EventObject} e The event object
             */
            'eventmouseup',

            /**
             * @event eventdblclick
             * Fires when an event is double clicked
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record of the clicked event
             * @param {Ext.EventObject} e The event object
             */
            'eventdblclick',

            /**
             * @event eventcontextmenu
             * Fires when contextmenu is activated on an event
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record of the clicked event
             * @param {Ext.EventObject} e The event object
             */
            'eventcontextmenu',

            /**
             * @event eventmouseenter
             * Fires when the mouse moves over an event
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record
             * @param {Ext.EventObject} e The event object
             */
            'eventmouseenter',
            /**
             * @event eventmouseout
             * Fires when the mouse moves out of an event
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} eventRecord The event record
             * @param {Ext.EventObject} e The event object
             */
            'eventmouseout',

            // Resizing events start --------------------------
            /**
             * @event beforeeventresize
             * Fires before a resize starts, return false to stop the execution
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} record The record about to be resized
             * @param {Ext.EventObject} e The event object
             */
            'beforeeventresize',

            /**
             * @event eventresizestart
             * Fires when resize starts
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} record The event record being resized
             */
            'eventresizestart',

            /**
             * @event eventpartialresize
             * Fires during a resize operation and provides information about the current start and end of the resized event
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} record The event record being resized
             * @param {Date} startDate The new start date of the event
             * @param {Date} endDate The new end date of the event
             * @param {Ext.Element} element The proxy element being resized
             */
            'eventpartialresize',

            /**
             * @event beforeeventresizefinalize
             * Fires before a succesful resize operation is finalized. Return false from a listener function to prevent the finalizing to
             * be done immedieately, giving you a chance to show a confirmation popup before applying the new values.
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Object} resizeContext An object containing, 'start', 'end', 'newResource' properties.
             * @param {Ext.EventObject} e The event object
             */
            'beforeeventresizefinalize',

            /**
             * @event eventresizeend
             * Fires after a succesful resize operation
             * @param {Mixed} view The scheduler view instance
             * @param {Sch.model.Event} record The updated event record
             */
            'eventresizeend',
            // Resizing events end --------------------------

            // Dnd events start --------------------------
            /**
             * @event beforeeventdrag
             * Fires before a dnd operation is initiated, return false to cancel it
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} record The record corresponding to the node that's about to be dragged
             * @param {Ext.EventObject} e The event object
             */
            'beforeeventdrag',

            /**
             * @event eventdragstart
             * Fires when a dnd operation starts
             * @param {Sch.mixin.SchedulerView} scheduler The scheduler object
             * @param {Array} records the records being dragged
             */
            'eventdragstart',


            /**
             * @event beforeeventdropfinalize
             * Fires before a succesful drop operation is finalized.
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Object} dragContext An object containing, 'start', 'end', 'newResource' properties.
             * @param {Ext.EventObject} e The event object
             */
            'beforeeventdropfinalize',

            /**
             * @event eventdrop
             * Fires after a succesful drag and drop operation
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event[]} records the affected records (if copies were made, they were not inserted into the store)
             * @param {Boolean} isCopy True if the records were copied instead of moved
             */
            'eventdrop',

            /**
             * @event aftereventdrop
             * Fires when after a drag n drop operation, even when drop was performed on an invalid location
             * @param {Mixed} view The scheduler view instance
             */
            'aftereventdrop',
            // Dnd events end --------------------------

            // Drag create events start --------------------------
            /**
             * @event beforedragcreate
             * Fires before a drag starts, return false to stop the execution
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Resource} resource The resource record
             * @param {Date} date The clicked date on the timeaxis
             * @param {Ext.EventObject} e The event object
             */
            'beforedragcreate',

            /**
             * @event dragcreatestart
             * Fires before a drag starts, return false to stop the execution
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             */
            'dragcreatestart',

            /**
             * @event beforedragcreatefinalize
             * Fires before a succesful resize operation is finalized. Return false from a listener function to prevent the finalizing to
             * be done immedieately, giving you a chance to show a confirmation popup before applying the new values.
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Object} createContext An object containing, 'start', 'end', 'resourceRecord' properties.
             * @param {Ext.EventObject} e The event object
             */
            'beforedragcreatefinalize',

            /**
             * @event dragcreateend
             * Fires after a successful drag-create operation
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} newEventRecord The newly created event record (added to the store in onEventCreated method)
             * @param {Sch.model.Resource} resource The resource record to which the event belongs
             * @param {Ext.EventObject} e The event object
             */
            'dragcreateend',

            /**
             * @event afterdragcreate
             * Always fires after a drag-create operation
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             */
            'afterdragcreate',
            // Drag create events end --------------------------

            /**
             * @event beforeeventadd
             * Fires after a successful drag-create operation, before the new event is added to the store. Return false to prevent the event from being added to the store.
             * @param {Sch.mixin.SchedulerView} view The scheduler view instance
             * @param {Sch.model.Event} newEventRecord The newly created event record
             */
            'beforeeventadd',


            /**
             * @event scheduleclick
             * Fires after a click on the schedule area
             * @param {Sch.mixin.SchedulerView} schedulerView The scheduler view object
             * @param {Date} clickedDate The clicked date
             * @param {Int} rowIndex The row index
             * @param {Sch.model.Resource} resource The resource, an event occured on
             * @param {Ext.EventObject} e The event object
             */
            'scheduleclick',

            /**
             * @event scheduledblclick
             * Fires after a doubleclick on the schedule area
             * @param {Sch.mixin.SchedulerView} schedulerView The scheduler view object
             * @param {Date} clickedDate The clicked date
             * @param {Int} rowIndex The row index
             * @param {Sch.model.Resource} resource The resource, an event occured on
             * @param {Ext.EventObject} e The event object
             */
            'scheduledblclick',

            /**
             * @event schedulecontextmenu
             * Fires after a context menu click on the schedule area
             * @param {Sch.mixin.SchedulerView} schedulerView The scheduler view object
             * @param {Date} clickedDate The clicked date
             * @param {Int} rowIndex The row index
             * @param {Sch.model.Resource} resource The resource, an event occured on
             * @param {Ext.EventObject} e The event object
             */
            'schedulecontextmenu'
        );

        var me = this;

        if (!this.eventPrefix) {
            throw 'eventPrefix missing';
        }

        if (Ext.isArray(me.eventTpl)) {
            var clone       = Ext.Array.clone(me.eventTpl),
                resizeTpl   = '<div class="sch-resizable-handle sch-resizable-handle-{0}"></div>';
                
            var eventResizeHandles  = this.eventResizeHandles;

            if (eventResizeHandles === 'start' || eventResizeHandles === 'both') {
                clone.splice(2, 0, Ext.String.format(resizeTpl, 'start'));
            }
            if (eventResizeHandles === 'end' || eventResizeHandles === 'both') {
                clone.splice(2, 0, Ext.String.format(resizeTpl, 'end'));
            }

            me.eventTpl     = new Ext.XTemplate(clone.join('').replace('{{evt-prefix}}', this.eventPrefix));
        }
    },

    inheritables: function () {
        return {

            // Configuring underlying grid view
            loadingText: 'Loading events...',
            overItemCls: '',
            // EOF: Configuring underlying grid view

            setReadOnly: function (readOnly) {
                if (this.dragCreator) {
                    this.dragCreator.setDisabled(readOnly);
                }
                this.callParent(arguments);
            },

            repaintEventsForResource : function(resourceRecord, refreshSelections) {
                // For vertical, we always repaint all events (do per-column repaint is not supported)
                var index = this.orientation === 'horizontal' ? this.store.indexOf(resourceRecord) : 0;

                if (this.orientation === 'horizontal') {
                    this.eventLayout.horizontal.clearCache(resourceRecord);
                }

                if (index >= 0) {
                    this.refreshNode(index);
                    this.lockingPartner.refreshNode(index);

                    if (refreshSelections) {
                        var selModel = this.getSelectionModel();
                        var events = resourceRecord.getEvents();

                        Ext.each(events, function(ev) {
                            if (selModel.isSelected(ev)) {
                               this.onEventSelect(ev, true);
                            }
                        }, this);
                    }
                }
            },

            repaintAllEvents : function() {
                if (this.orientation === 'horizontal') {
                    this.refresh();
                } else {
                    // All events are rendered in first row, no need to do a full refresh
                    this.refreshNode(index);
                }
            },


            handleScheduleEvent : function(e) {
                var t = e.getTarget('.' + this.timeCellCls, 2);

                if (t) {
                    var clickedDate     = this.getDateFromDomEvent(e, 'floor');
                    var resourceNode    = this.findRowByChild(t);
                    var index           = this.indexOf(resourceNode);

                    var resource;

                    if (this.orientation == 'horizontal') {
                        resource        = this.getRecordForRowNode(resourceNode);
                    } else {
                        var cellNode    = e.getTarget(this.timeCellSelector, 5);

                        if (cellNode) {
                            var cellIndex   = typeof cellNode.cellIndex == 'number' ? cellNode.cellIndex : cellNode.getAttribute('data-cellIndex');
                            var header      = this.headerCt.getGridColumns()[ cellIndex ];

                            resource        = header && header.model;
                        }
                    }

                    this.fireEvent('schedule' + e.type, this, clickedDate, index, resource, e);
                }
            },


            onEventDataRefresh: function () {
                this.clearRowHeightCache();
                this.callParent(arguments);
            },


            onUnbindStore: function(store) {
                store.un({
                    refresh     : this.clearRowHeightCache,
                    clear       : this.clearRowHeightCache,
                    load        : this.clearRowHeightCache,

                    scope       : this
                });
                this.callParent(arguments);
            },

            // our listeners must go before any other listeners, that's why we override the 'bindStore'
            // instead of `onBindStore`
            bindStore: function (store, initial, propName) {
                store && store.on({
                    refresh     : this.clearRowHeightCache,
                    clear       : this.clearRowHeightCache,
                    load        : this.clearRowHeightCache,

                    scope       : this
                });
                this.callParent(arguments);
            }
        };
    },


    _afterRender: function () {
        this.bindEventStore(this.eventStore, true);

        this.setupEventListeners();

        this.configureFunctionality();

        var resizer = this.headerCt.resizer;

        if (resizer) {
            resizer.doResize = Ext.Function.createSequence(resizer.doResize, this.afterHeaderResized, this);
        }
    },

    // private, clean up
    _destroy: function () {
        this.bindEventStore(null);
    },


    clearRowHeightCache : function () {
        if (this.orientation === 'horizontal') {
            this.eventLayout.horizontal.clearCache();
        }
    },


    configureFunctionality: function () {
        var vfScope = this.validatorFnScope || this;

        if (this.eventResizeHandles !== 'none' && Sch.feature.ResizeZone) {
            this.resizePlug = new Sch.feature.ResizeZone(Ext.applyIf({
                schedulerView: this,

                validatorFn: function (resourceRecord, eventRecord, startDate, endDate) {
                    return (this.allowOverlap || this.isDateRangeAvailable(startDate, endDate, eventRecord, resourceRecord)) &&
                            this.resizeValidatorFn.apply(vfScope, arguments) !== false;
                },

                validatorFnScope: this
            }, this.resizeConfig || {}));
        }

        if (this.enableEventDragDrop !== false && Sch.feature.DragDrop) {

            this.dragdropPlug = new Sch.feature.DragDrop(this, {
                validatorFn: function (dragRecords, targetResourceRecord, date, duration, e) {
                    return (this.allowOverlap || this.isDateRangeAvailable(date, Sch.util.Date.add(date, Sch.util.Date.MILLI, duration), dragRecords[0], targetResourceRecord)) &&
                            this.dndValidatorFn.apply(vfScope, arguments) !== false;
                },

                validatorFnScope: this,

                dragConfig: this.dragConfig || {}
            });
        }

        if (this.enableDragCreation !== false && Sch.feature.DragCreator) {
            this.dragCreator = new Sch.feature.DragCreator(Ext.applyIf({
                schedulerView: this,
                disabled: this.readOnly,
                validatorFn: function (resourceRecord, startDate, endDate) {
                    return (this.allowOverlap || this.isDateRangeAvailable(startDate, endDate, null, resourceRecord)) &&
                            this.createValidatorFn.apply(vfScope, arguments) !== false;
                },
                validatorFnScope: this
            }, this.createConfig || {}));
        }
    },

    // ---------------------------------------
    // Interaction listeners

    onBeforeDragDrop: function (s, rec, e) {
        return !this.readOnly && !e.getTarget().className.match('sch-resizable-handle');
    },

    onDragDropStart: function () {
        if (this.dragCreator) {
            this.dragCreator.setDisabled(true);
        }

        if (this.tip) {
            this.tip.hide();
            this.tip.disable();
        }

        if (this.overScheduledEventClass) {
            this.setMouseOverEnabled(false);
        }
    },

    onDragDropEnd: function () {
        if (this.dragCreator) {
            this.dragCreator.setDisabled(false);
        }

        if (this.tip) {
            this.tip.enable();
        }

        if (this.overScheduledEventClass) {
            this.setMouseOverEnabled(true);
        }
    },

    onBeforeDragCreate: function (s, resourceRecord, date, e) {
        return !this.readOnly && !e.ctrlKey;
    },

    onDragCreateStart: function () {
        if (this.overScheduledEventClass) {
            this.setMouseOverEnabled(false);
        }

        if (this.tip) {
            this.tip.hide();
            this.tip.disable();
        }
    },

    onDragCreateEnd: function (s, newEventRecord, e) {
        // If an event editor is defined, it has to manage how/if/when the event is added to the event store
        if (!this.getEventEditor()) {
            if (this.fireEvent('beforeeventadd', this, newEventRecord) !== false) {
                this.onEventCreated(newEventRecord);
                this.eventStore.append(newEventRecord);
            }
            this.dragCreator.getProxy().hide();
        }

        if (this.overScheduledEventClass) {
            this.setMouseOverEnabled(true);
        }
    },

    // Empty but provided so that you can override it to supply default record values etc.
    onEventCreated: function (newEventRecord) {
    },

    onAfterDragCreate: function () {
        if (this.overScheduledEventClass) {
            this.setMouseOverEnabled(true);
        }

        if (this.tip) {
            this.tip.enable();
        }
    },

    onBeforeResize: function (s, rec, e) {
        return !this.readOnly;
    },

    onResizeStart: function () {
        if (this.tip) {
            this.tip.hide();
            this.tip.disable();
        }

        if (this.dragCreator) {
            this.dragCreator.setDisabled(true);
        }
    },

    onResizeEnd: function () {
        if (this.tip) {
            this.tip.enable();
        }

        if (this.dragCreator) {
            this.dragCreator.setDisabled(false);
        }
    },

    // EOF Interaction listeners
    // ---------------------------------------


    setupEventListeners: function () {
        this.on({
            beforeeventdrag     : this.onBeforeDragDrop,
            eventdragstart      : this.onDragDropStart,
            aftereventdrop      : this.onDragDropEnd,

            beforedragcreate    : this.onBeforeDragCreate,
            dragcreatestart     : this.onDragCreateStart,
            dragcreateend       : this.onDragCreateEnd,
            afterdragcreate     : this.onAfterDragCreate,

            beforeeventresize   : this.onBeforeResize,
            eventresizestart    : this.onResizeStart,
            eventresizeend      : this.onResizeEnd,

            scope               : this
        });
    },

    afterHeaderResized: function () {
        var resizer = this.headerCt.resizer;

        if (resizer && resizer.dragHd instanceof Sch.column.Resource) {
            var w = resizer.dragHd.getWidth();
            this.setColumnWidth(w);
        }
    },

    columnRenderer: function (val, meta, record, row, col) {
        return this[this.orientation].columnRenderer(val, meta, record, row, col);
    }
});

/**

@class Sch.view.SchedulerGridView
@extends Sch.view.TimelineGridView
@mixin Sch.mixin.SchedulerView

Empty class just consuming the Sch.mixin.SchedulerView mixin.

*/
Ext.define("Sch.view.SchedulerGridView", {
    extend              : 'Sch.view.TimelineGridView',
    mixins              : ['Sch.mixin.SchedulerView', 'Sch.mixin.Localizable'],
    alias               : 'widget.schedulergridview'
}, function() {
    this.override(Sch.mixin.SchedulerView.prototype.inheritables() || {});
});


/**
@class Sch.mixin.Zoomable

A mixin for {@link Sch.mixin.TimelinePanel} class, providing "zooming" functionality to the consuming panel.

The zooming feature works by reconfiguring panel's time axis with the current zoom level values selected from the {@link #zoomLevels} array.
Zoom levels can be added and removed from the array to change the amount of available steps. Range of zooming in/out can be also
modified with {@link #maxZoomLevel} / {@link #minZoomLevel} properties.

This mixin adds additional methods to the scheduler : {@link #setMaxZoomLevel}, {@link #setMinZoomLevel}, {@link #zoomToLevel}, {@link #zoomIn},
{@link #zoomOut}, {@link #zoomInFull}, {@link #zoomOutFull}.

Notice: zooming doesn't work properly when `forceFit` option is set to true for the panel.
*/

Ext.define('Sch.mixin.Zoomable', {
    /**
     * @cfg {Array} [zoomLevels=Array] Predefined map of zoom levels for each preset in the ascending order. Zoom level is basically a {@link Sch.preset.ViewPreset view preset},
     * which is based on another preset, with some values overriden.
     *
     * Each element is an {Object} with the following parameters :
     *
     * - `preset` (String)      - {@link Sch.preset.ViewPreset} to be used for this zoom level. This must be a valid preset name registered in {@link Sch.preset.Manager preset manager}.
     * - `width` (Int)          - {@link Sch.preset.ViewPreset#timeColumnWidth timeColumnWidth} time column width value from the preset
     * - `increment` (Int)      - {@link Sch.preset.ViewPresetHeaderRow#increment increment} value from the bottom header row of the preset
     * - `resolution` (Int)     - {@link Sch.preset.ViewPreset#timeResolution increment} part of the `timeResolution` object in the preset
     * - `resolutionUnit` (String) (Optional) - {@link Sch.preset.ViewPreset#timeResolution unit} part of the `timeResolution` object in the preset
     */
    zoomLevels: [
        //YEAR
        { width: 80,    increment: 5,   resolution: 1, preset: 'manyYears', resolutionUnit: 'YEAR' },
        { width: 40,    increment: 1,   resolution: 1, preset: 'manyYears', resolutionUnit: 'YEAR' },
        { width: 80,    increment: 1,   resolution: 1, preset: 'manyYears', resolutionUnit: 'YEAR' },
        { width: 30,    increment: 1,   resolution: 1, preset: 'year', resolutionUnit: 'MONTH' },
        { width: 50,    increment: 1,   resolution: 1, preset: 'year', resolutionUnit: 'MONTH'},
        { width: 100,   increment: 1,   resolution: 1, preset: 'year', resolutionUnit: 'MONTH'},
        { width: 200,   increment: 1,   resolution: 1, preset: 'year', resolutionUnit: 'MONTH'},

        //MONTH
        { width: 100,   increment: 1,   resolution: 7, preset: 'monthAndYear', resolutionUnit: 'DAY'},
        { width: 30,    increment: 1,   resolution: 1, preset: 'weekDateAndMonth', resolutionUnit: 'DAY'},

        //WEEK
        { width: 35,    increment: 1,   resolution: 1, preset: 'weekAndMonth', resolutionUnit: 'DAY'},
        { width: 50,    increment: 1,   resolution: 1, preset: 'weekAndMonth', resolutionUnit: 'DAY'},
        { width: 20,    increment: 1,   resolution: 1, preset: 'weekAndDayLetter' },

        //DAY
        { width: 50,    increment: 1,   resolution: 1, preset: 'weekAndDay', resolutionUnit: 'HOUR'},
        { width: 100,   increment: 1,   resolution: 1, preset: 'weekAndDay', resolutionUnit: 'HOUR' },

        //HOUR
        { width: 50,    increment: 6,   resolution: 30, preset: 'hourAndDay', resolutionUnit: 'MINUTE' },
        { width: 100,   increment: 6,   resolution: 30, preset: 'hourAndDay', resolutionUnit: 'MINUTE' },
        { width: 60,    increment: 2,   resolution: 30, preset: 'hourAndDay', resolutionUnit: 'MINUTE' },
        { width: 60,    increment: 1,   resolution: 30, preset: 'hourAndDay', resolutionUnit: 'MINUTE' },

        //MINUTE
        { width: 30,    increment: 15,  resolution: 5, preset: 'minuteAndHour' },
        { width: 60,    increment: 15,  resolution: 5, preset: 'minuteAndHour' },
        { width: 130,   increment: 15,  resolution: 5, preset: 'minuteAndHour' },
        { width: 60,    increment: 5,   resolution: 5, preset: 'minuteAndHour' },
        { width: 100,   increment: 5,   resolution: 5, preset: 'minuteAndHour' },
        { width: 50,    increment: 2,   resolution: 1, preset: 'minuteAndHour' },

        //SECOND
        { width: 30,    increment: 10,  resolution: 5,  preset: 'secondAndMinute' },
        { width: 60,    increment: 10,  resolution: 5,  preset: 'secondAndMinute' },
        { width: 130,   increment: 5,   resolution: 5,  preset: 'secondAndMinute' }
    ],

    /**
     * @cfg {Int} minZoomLevel Minimal zoom level to which {@link #zoomOut} will work.
     */
    minZoomLevel        : null,

    /**
     * @cfg {Int} maxZoomLevel Maximal zoom level to which {@link zoomIn} will work.
     */
    maxZoomLevel        : null,


    /**
     * Integer number indicating the size of timespan during zooming. When zooming, the timespan is adjusted to make the scrolling area `visibleZoomFactor` times
     * wider than the scheduler size itself.
     */
    visibleZoomFactor   : 5,
    
    /**
     * @cfg {Boolean} zoomKeepsOriginalTimespan Whether the originally rendered timespan should be preserved while zooming. By default it is set to `false`,
     * meaning scheduler will adjust the currently rendered timespan to limit the amount of HTML content to render. When setting this option
     * to `true`, be careful not to allow to zoom a big timespan in seconds resolution for example. That will cause **a lot** of HTML content
     * to be rendered and affect performance. You can use {@link minZoomLevel} and {@link maxZoomLevel} config options for that.
     */
    zoomKeepsOriginalTimespan    : false,


    cachedCenterDate    : null,
    isFirstZoom         : true,
    isZooming           : false,


    initializeZooming: function () {
        //create instance-specific copy of zoomLevels
        this.zoomLevels         = this.zoomLevels.slice();

        this.setMinZoomLevel(this.minZoomLevel || 0);
        this.setMaxZoomLevel(this.maxZoomLevel !== null ? this.maxZoomLevel : this.zoomLevels.length - 1);

        this.on('viewchange', this.clearCenterDateCache, this);
    },


    getZoomLevelUnit : function (zoomLevel) {
        var headerConfig        = Sch.preset.Manager.getPreset(zoomLevel.preset).headerConfig;

        return headerConfig.bottom ? headerConfig.bottom.unit : headerConfig.middle.unit;
    },


    getMilliSecondsPerPixelForZoomLevel : function (level) {
        var DATE    = Sch.util.Date;

        // trying to convert the unit + increment to a number of milliseconds
        // this number is not fixed (month can be 28, 30 or 31 day), but at least this convertion
        // will be consistent (should be no DST changes at year 1
        return Math.round(
            (DATE.add(new Date(1, 0, 1), this.getZoomLevelUnit(level), level.increment) - new Date(1, 0, 1)) / level.width
        );
    },


    // XXX possibly merge into "calculateCurrentZoomLevel" and remove after
    presetToZoomLevel : function (presetName) {
        var preset              = Sch.preset.Manager.getPreset(presetName);
        var headerConfig        = preset.headerConfig;
        var bottom              = headerConfig.bottom;
        var middle              = headerConfig.middle;

        return {
            preset          : presetName,
            increment       : (bottom ? bottom.increment : middle.increment) || 1,
            resolution      : preset.timeResolution.increment,
            resolutionUnit  : preset.timeResolution.unit,
            width           : preset.timeColumnWidth
        };
    },


    calculateCurrentZoomLevel : function () {
        var zoomLevel       = this.presetToZoomLevel(this.viewPreset);
        var headerConfig    = this.timeAxis.headerConfig;
        var bottom          = headerConfig.bottom;
        var middle          = headerConfig.middle;

        // update the `width` of the zoomLevel
        zoomLevel.width     = this.timeAxis.preset.timeColumnWidth;
        zoomLevel.increment = (bottom ? bottom.increment : middle.increment) || 1;

        return zoomLevel;
    },


    getCurrentZoomLevelIndex : function () {
        var currentZoomLevel        = this.calculateCurrentZoomLevel();
        var currentFactor           = this.getMilliSecondsPerPixelForZoomLevel(currentZoomLevel);

        var zoomLevels              = this.zoomLevels;

        for (var i = 0; i < zoomLevels.length; i++) {
            var zoomLevelFactor     = this.getMilliSecondsPerPixelForZoomLevel(zoomLevels[ i ]);

            if (zoomLevelFactor == currentFactor) return i;

            // current zoom level is outside of pre-defined zoom levels
            if (i === 0 && currentFactor > zoomLevelFactor) return -0.5;
            if (i == zoomLevels.length - 1 && currentFactor < zoomLevelFactor) return zoomLevels.length - 1 + 0.5;

            var nextLevelFactor     = this.getMilliSecondsPerPixelForZoomLevel(zoomLevels[ i + 1 ]);

            if (zoomLevelFactor > currentFactor && currentFactor > nextLevelFactor) return i + 0.5;
        }

        throw "Can't find current zoom level index";
    },


    /**
    * Sets the {@link #maxZoomLevel} value.
    * @param {Int} level The level to limit zooming in to.
    */
    setMaxZoomLevel: function (level) {
        if (level < 0 || level >= this.zoomLevels.length) {
            throw new Error("Invalid range for `setMinZoomLevel`");
        }

        this.maxZoomLevel = level;
    },

    /**
    * Sets the {@link #minZoomLevel} value.
    * @param {Int} level The level to limit zooming out to.
    */
    setMinZoomLevel: function (level) {
        if (level < 0 || level >= this.zoomLevels.length) {
            throw new Error("Invalid range for `setMinZoomLevel`");
        }

        this.minZoomLevel = level;
    },


    // when zooming out, the precision for the center date becomes not so good (1px starts to contains too big time interval)
    // because of that zooming will be "floating"
    // to prevent that we cache the center date
    // cache will be cleared after any user scroll operation
    /** @ignore */
    getViewportCenterDateCached : function () {
        if (this.cachedCenterDate) return this.cachedCenterDate;

        return this.cachedCenterDate = this.getViewportCenterDate();
    },


    clearCenterDateCache : function () {
        this.cachedCenterDate = null;
    },


    /**
     * Allows zooming to certain level of {@link #zoomLevels} array. Automatically limits zooming between {@link #maxZoomLevel} 
     * and {@link #minZoomLevel}. Can also set time axis timespan to the supplied start and end dates.
     *
     * @param {Int} level Level to zoom to.
     * @param {Object} span The time frame. Used to set time axis timespan to the supplied start and end dates. If provided, the view
     * will be centered in this time interval
     * @param {Date} span.start The time frame start.
     * @param {Date} span.end The time frame end.
     *
     * @return {Int} level Current zoom level or null if it hasn't changed.
     */
    zoomToLevel: function (level, span) {
        level                       = Ext.Number.constrain(level, this.minZoomLevel, this.maxZoomLevel);

        var currentZoomLevel        = this.calculateCurrentZoomLevel();
        var currentFactor           = this.getMilliSecondsPerPixelForZoomLevel(currentZoomLevel);

        var nextZoomLevel           = this.zoomLevels[ level ];
        var nextFactor              = this.getMilliSecondsPerPixelForZoomLevel(nextZoomLevel);

        if (currentFactor == nextFactor && !span) {
            // already at requested zoom level
            return null;
        }

        var me                      = this;
        var view                    = this.getSchedulingView();
        var viewEl                  = view.getOuterEl();
        var scrollSource            = view.getScrollEventSource();

        if (this.isFirstZoom) {
            this.isFirstZoom = false;

            // clear the center date cache on any scroll operation
            scrollSource.on('scroll', this.clearCenterDateCache, this);
        }

        var isVertical              = this.orientation == 'vertical';

        var centerDate              = span ? new Date((span.start.getTime() + span.end.getTime()) / 2) : this.getViewportCenterDateCached();

        var panelSize               = isVertical ? viewEl.getHeight() : viewEl.getWidth();

        var presetCopy              = Ext.clone(Sch.preset.Manager.getPreset(nextZoomLevel.preset));
        
        var hasSpanProvided         = Boolean(span);

        span                        = span || this.calculateOptimalDateRange(centerDate, panelSize, nextZoomLevel);

        var headerConfig            = presetCopy.headerConfig;
        var bottom                  = headerConfig.bottom;
        var middle                  = headerConfig.middle;

        presetCopy[ isVertical ? 'rowHeight' : 'timeColumnWidth' ] = nextZoomLevel.width;

        if (bottom) {
            bottom.increment        = nextZoomLevel.increment;
        } else {
            middle.increment        = nextZoomLevel.increment;
        }

        this.isZooming              = true;

        this.viewPreset             = nextZoomLevel.preset;

        var unit                    = bottom ? bottom.unit : middle.unit;
        var timeAxis                = this.timeAxis;

        timeAxis.reconfigure({
            preset              : presetCopy,
            headerConfig        : headerConfig,

            unit                : unit,
            increment           : nextZoomLevel.increment,

            resolutionUnit      : Sch.util.Date.getUnitByName(nextZoomLevel.resolutionUnit || unit),
            resolutionIncrement : nextZoomLevel.resolution,

            weekStartDay        : this.weekStartDay,

            mainUnit            : middle.unit,
            shiftUnit           : presetCopy.shiftUnit,
            shiftIncrement      : presetCopy.shiftIncrement || 1,

            defaultSpan         : presetCopy.defaultSpan || 1,

            start               : span.start || this.getStart(),
            end                 : span.end || this.getEnd()
        });
        
        // re-calculate the center date after reconfiguring the timeaxis, 
        // because the actual start/end date may change because of time axis "autoAdjust" property
        if (hasSpanProvided) centerDate = new Date((timeAxis.getStart().getTime() + timeAxis.getEnd().getTime()) / 2);

        // restore the cached center date to keep it stable
        // this handler will be called 2nd after the clearing handler and will restore the cache
        // this handler will be called only for the programming scroll  called below
        scrollSource.on('scroll', function () { me.cachedCenterDate = centerDate; }, this, { single : true });

        if (isVertical) {
            var y = view.getYFromDate(centerDate, true);
            view.scrollVerticallyTo(y - panelSize / 2);
        } else {
            var x = view.getXFromDate(centerDate, true);
            view.scrollHorizontallyTo(x - panelSize / 2);
        }

        me.isZooming              = false;

        /**
         * @event zoomchange
         *
         * Fires after zoom level has been changed
         *
         * @param {Sch.mixin.SchedulerPanel} scheduler The scheduler object
         * @param {Integer} level The index of the new zoom level
         */
        this.fireEvent('zoomchange', this, level);

        return level;
    },

    /**
     * Sets time frame to specified range and applies zoom level which allows to fit all columns to this range.
     *
     * @param {Object} span The time frame.
     * @param {Date} span.start The time frame start.
     * @param {Date} span.end The time frame end.
     * @param {Object} config Configuration object containing following properties:
     * @param {Integer} config.adjustStart Set timeaxis start date to `adjustStart` time frame units earlier than span.start value. Use this to have some margin for comfortable reading. By default this set to `1`.
     * @param {Integer} config.adjustEnd Set timeaxis end date to `adjustEnd` time frame units after than span.end value. Use this to have some margin for comfortable reading. By default this set to `1`.
     *
     * @return {Int} level Current zoom level or null if it hasn't changed.
     */
    zoomToSpan : function (span, config) {
        if (span.start && span.end && span.start < span.end) {

            config = Ext.applyIf(config || {}, {
                adjustStart : 1,
                adjustEnd   : 1
            });

            var start       = span.start,
                end         = span.end,
                fnSame      = function (v) { return v; },
                adjustStart = fnSame,
                adjustEnd   = fnSame;

            if (this.timeAxis.autoAdjust) {

                if (config.adjustStart) {
                    adjustStart = function (start, unit, timeAxis) {
                        var result  = Sch.util.Date.add(start, timeAxis && timeAxis.mainUnit || unit, - config.adjustStart);

                        if (timeAxis) {
                            var adjusted    = timeAxis.getAdjustedDates(result, end);
                            return adjusted.start;
                        }

                        return result;
                    };
                }
                if (config.adjustEnd) {
                    adjustEnd   = function (end, unit, timeAxis) {
                        var result  = Sch.util.Date.add(end, timeAxis && timeAxis.mainUnit || unit, config.adjustEnd);

                        if (timeAxis) {
                            var adjusted    = timeAxis.getAdjustedDates(start, result);
                            return adjusted.end;
                        }

                        return result;
                    };
                }

            } else {

                if (config.adjustStart) {
                    adjustStart = function (start, unit) { return Sch.util.Date.add(start, unit, - config.adjustStart); };
                }
                if (config.adjustEnd) {
                    adjustEnd   = function (end, unit) { return Sch.util.Date.add(end, unit, config.adjustEnd); };
                }

            }

            var timeAxis    = Ext.create('Sch.data.TimeAxis');

            // get scheduling view width
            var width       = this.getSchedulingView().getTimeAxisViewModel().getAvailableWidth();

            // if potential width of col is less than col width provided by zoom level
            //   - we'll zoom out panel until col width fit into width from zoom level
            // and if width of column is more than width from zoom level
            //   - we'll zoom in until col width fit won't fit into width from zoom level

            var currLevel   = Math.floor(this.getCurrentZoomLevelIndex());

            // if we zoomed out even more than the highest zoom level - limit it to the highest zoom level
            if (currLevel == -1) currLevel = 0;

            var diffMS      = adjustEnd(end, this.timeAxis.mainUnit, this.timeAxis) - adjustStart(start, this.timeAxis.mainUnit, this.timeAxis),
                factor      = this.getMilliSecondsPerPixelForZoomLevel(this.zoomLevels[currLevel]),
                // increment to get next zoom level (-1 for zoom out and +1 - for zoom in)
                inc         = diffMS / factor > width ? -1 : 1,
                level       = currLevel + inc,
                sizeProp    = this.orientation == 'vertical' ? 'rowHeight' : 'timeColumnWidth';


            var zoomLevel, preset, unit, levelToZoom = null, unitToZoom = this.timeAxis.mainUnit;

            // loop over zoom levels
            while (level >= 0 && level <= this.zoomLevels.length - 1) {

                // get zoom level
                zoomLevel   = this.zoomLevels[level];

                //preset      = Sch.preset.Manager.getPreset(zoomLevel.preset);
                //unit        = (preset.headerConfig.bottom || preset.headerConfig.middle).unit;

                // get its preset copy
                preset      = Ext.clone(Sch.preset.Manager.getPreset(zoomLevel.preset));
                var bottom  = preset.headerConfig.bottom;
                var middle  = preset.headerConfig.middle;

                preset[sizeProp]        = zoomLevel.width;

                if (bottom) {
                    bottom.increment    = zoomLevel.increment;
                    unit                = bottom.unit;
                } else {
                    middle.increment    = zoomLevel.increment;
                    unit                = middle.unit;
                }

                // apply zoom level params to timeAxis
                Ext.apply(timeAxis, {
                    autoAdjust          : this.timeAxis.autoAdjust,
                    preset              : preset,
                    headerConfig        : preset.headerConfig,
                    unit                : unit,
                    increment           : zoomLevel.increment,
                    resolutionUnit      : Sch.util.Date.getUnitByName(zoomLevel.resolutionUnit || unit),
                    resolutionIncrement : zoomLevel.resolution,
                    mainUnit            : middle.unit,
                    shiftUnit           : preset.shiftUnit,
                    shiftIncrement      : preset.shiftIncrement || 1,
                    defaultSpan         : preset.defaultSpan || 1
                });

                diffMS      = adjustEnd(end, unit, timeAxis) - adjustStart(start, unit, timeAxis);
                factor      = this.getMilliSecondsPerPixelForZoomLevel(zoomLevel);

                // if zooming out
                if (inc < 0) {
                    // if columns fit into available space
                    if (diffMS / factor <= width) {
                        levelToZoom     = level;
                        unitToZoom      = unit;
                        // stop searching
                        break;
                    }
                // if zooming in
                } else {
                    // if columns fit into available space
                    if (diffMS / factor <= width) {
                        // if it's not currently active level
                        if (currLevel !== level - inc) {
                            // remember this level as applicable
                            levelToZoom     = level - inc;
                            unitToZoom      = unit;
                        }
                    } else {
                        // Sanity check to find the following case:
                        // If we're already zoomed in at the appropriate level, but the current zoomLevel is "too small" to fit and had to be expanded,
                        // there is an edge case where we should actually just stop and use the currently selected zoomLevel
                        break;
                    }
                }

                level += inc;
            }

            // If we didn't find a large/small enough zoom level, use the lowest/highest level
            levelToZoom = levelToZoom !== null ? levelToZoom : level-inc;

            return this.zoomToLevel(levelToZoom, {
                start   : adjustStart(start, unitToZoom),
                end     : adjustEnd(end, unitToZoom)
            });
        }

        return null;
    },

    /**
    * Zooms in Scheduler view following map of zoom levels. If the amount of levels to zoom is given, view will zoom in by this value.
    * Otheriwse a value of `1` will be used.
    *
    * @param {Int levels}[1] (optional) amount of levels to zoom in
    *
    * @return {Int} currentLevel New zoom level of the panel or null if level hasn't changed.
    */
    zoomIn: function (levels) {
        //if called without parameters or with 0, zoomIn by 1 level
        levels          = levels || 1;

        var currentZoomLevelIndex       = this.getCurrentZoomLevelIndex();

        if (currentZoomLevelIndex >= this.zoomLevels.length - 1) return null;

        return this.zoomToLevel(Math.floor(currentZoomLevelIndex) + levels);
    },

    /**
    * Zooms out Scheduler view following map of zoom levels. If the amount of levels to zoom is given, view will zoom out by this value.
    * Otheriwse a value of `1` will be used.
    *
    * @param {Int levels}[1] (optional) amount of levels to zoom out
    *
    * @return {Int} currentLevel New zoom level of the panel or null if level hasn't changed.
    */
    zoomOut: function(levels){
        //if called without parameters or with 0, zoomIn by 1 level
        levels          = levels || 1;

        var currentZoomLevelIndex       = this.getCurrentZoomLevelIndex();

        if (currentZoomLevelIndex <= 0) return null;

        return this.zoomToLevel(Math.ceil(currentZoomLevelIndex) - levels);
    },

    /**
    * Zooms in Scheduler view to the {@link maxZoomLevel} following map of zoom levels.
    *
    * @return {Int} currentLevel New zoom level of the panel or null if level hasn't changed.
    */
    zoomInFull: function () {
        return this.zoomToLevel(this.maxZoomLevel);
    },

    /**
    * Zooms out Scheduler view to the {@link minZoomLevel} following map of zoom levels.
    *
    * @return {Int} currentLevel New zoom level of the panel or null if level hasn't changed.
    */
    zoomOutFull: function () {
        return this.zoomToLevel(this.minZoomLevel);
    },


    /*
    * Adjusts the timespan of panel to the new zoom level. Used for performance reasons,
    * as rendering too many columns takes noticeable amount of time so their number is limited.
    */
    calculateOptimalDateRange: function (centerDate, panelSize, zoomLevel) {
        var timeAxis            = this.timeAxis;
        
        if (this.zoomKeepsOriginalTimespan) {
            return {
                start           : timeAxis.getStart(),
                end             : timeAxis.getEnd()
            };
        }
        
        var schDate             = Sch.util.Date;

        var headerConfig        = Sch.preset.Manager.getPreset(zoomLevel.preset).headerConfig;
        var topUnit             = headerConfig.top ? headerConfig.top.unit : headerConfig.middle.unit;

        var unit                = this.getZoomLevelUnit(zoomLevel);
//        this is weird, but we need to floor/ceil to the "topUnit" above, not the resolution unit..
//        var resolutionUnit      = zoomLevel.resolutionUnit ? schDate.getUnitByName(zoomLevel.resolutionUnit) : unit;

        var difference          = Math.ceil(panelSize / zoomLevel.width * zoomLevel.increment * this.visibleZoomFactor / 2);

        var startDate           = schDate.add(centerDate, unit, -difference);
        var endDate             = schDate.add(centerDate, unit, difference);

        return {
            start   : timeAxis.floorDate(startDate, false, topUnit),
            end     : timeAxis.ceilDate(endDate, false, topUnit)
        };
    }
});

/**

@class Sch.mixin.AbstractTimelinePanel
@private

A base mixin giving the consuming panel "time line" functionality. 
This means that the panel will be capable to display a list of "events", along a {@link Sch.data.TimeAxis time axis}.

This class should not be used directly.

*/

Ext.define('Sch.mixin.AbstractTimelinePanel', {
    requires: [
        'Sch.data.TimeAxis',
        'Sch.view.model.TimeAxis',
        'Sch.feature.ColumnLines',
        'Sch.preset.Manager'
    ],
    
    mixins: [
        'Sch.mixin.Zoomable'
    ],

    /**
     * @cfg {String} orientation An initial orientation of the view - can be either `horizontal` or `vertical`. Default value is `horizontal`.
     */
    orientation             : 'horizontal',

    /**
     * @cfg {Int} weekStartDay A valid JS date index between 0-6. (0: Sunday, 1: Monday etc.).
     */
    weekStartDay            : 1,

    /**
     * @cfg {Boolean} snapToIncrement true to snap to resolution increment while interacting with scheduled events.
     */
    snapToIncrement         : false,

    /**
     * @cfg {Boolean} readOnly true to disable editing.
     */
    readOnly                : false,

    /**
     * @cfg {Boolean} forceFit Set to true to force the time columns to fit to the available horizontal space.
     */
    forceFit                : false,

    /**
     * @cfg {String} eventResizeHandles Defines which resize handles to use for resizing events. Possible values: 'none', 'start', 'end', 'both'. Defaults to 'both'
     */
    eventResizeHandles      : 'both',

    /**
     * @cfg {Int} rowHeight The row height (used in horizontal mode only)
     */

    /**
     * @cfg {Sch.data.TimeAxis} timeAxis The backing store providing the input date data for the timeline panel.
     */
    timeAxis                : null,
    
    /**
     * @cfg {Boolean} autoAdjustTimeAxis The value for the {@link Sch.data.TimeAxis#autoAdjust} config option, which will be used
     * when creating the time axis instance. You can set this option to `false` to make the timeline panel start and end on the exact provided
     * {@link #startDate}/{@link #endDate} w/o adjusting them.
     */
    autoAdjustTimeAxis      : true,

    /**
     * @private
     * @cfg {Sch.view.model.TimeAxis/Object} timeAxisViewModel The backing view model for the visual representation of the time axis.
     * Either a real instance or a simple config object.
     */
    timeAxisViewModel       : null,

    /**
     * @cfg {Object} validatorFnScope
     * The scope used for the different validator functions.
     */

    /**
     * @cfg {String} viewPreset
     * @property {String} viewPreset
     * A key used to lookup a predefined {@link Sch.preset.ViewPreset} (e.g. 'weekAndDay', 'hourAndDay'), managed by {@link Sch.preset.Manager}. See {@link Sch.preset.Manager} for more information.
     */
    viewPreset              : 'weekAndDay',

    /**
     * @cfg {Boolean} trackHeaderOver `true` to highlight each header cell when the mouse is moved over it.
     */
    trackHeaderOver         : true,

    /**
     * @cfg {Date} startDate The start date of the timeline. If omitted, the start date of the provided {@link Sch.data.TimeAxis} will be used.
     * Defaults to the current date and time if no time axis has been provided.
     */
    startDate               : null,

    /**
     * @cfg {Date} endDate The end date of the timeline. If omitted, it will be calculated based on the {@link #startDate} setting and
     * the 'defaultSpan' property of the current {@link #viewPreset}.
     */
    endDate                 : null,

    columnLines             : true,
    trackMouseOver          : false,

    // If user supplied a 'rowHeight' config or a panel subclass with such a value - skip reading this setting
    // from the viewpreset
    readRowHeightFromPreset : true,

    /**
     * @cfg {Int} eventBorderWidth
     * The width of the border of your event, needed to calculate the correct start/end positions
     */
    eventBorderWidth        : 1,

    /**
    * Returns the orientation of this panel, "horizontal" or "vertical"
    * @return {String}  
    */
    getOrientation: function () {
        return this.orientation;
    },


    isHorizontal : function() {
        return this.getOrientation() === 'horizontal';
    },


    isVertical : function() {
        return !this.isHorizontal();
    },

    cellBorderWidth         : 1,
    cellTopBorderWidth      : 1,        // 0 since Ext JS 4.2.1
    cellBottomBorderWidth   : 1,

    // Must be called during initialization by consuming class
    _initializeTimelinePanel : function() {

        this.initializeZooming();

        this.renderers = [];

        this.readRowHeightFromPreset = !this.rowHeight;

        if (!this.timeAxis) {
            this.timeAxis = new Sch.data.TimeAxis({ autoAdjust : this.autoAdjustTimeAxis });
        }

        if (!this.timeAxisViewModel || !(this.timeAxisViewModel instanceof Sch.view.model.TimeAxis)) {
            var config = Ext.apply({
                snapToIncrement : this.snapToIncrement,
                forceFit        : this.forceFit,
                timeAxis        : this.timeAxis
            }, this.timeAxisViewModel || {});

            this.timeAxisViewModel = new Sch.view.model.TimeAxis(config);
        }

        this.timeAxisViewModel.on('update', this.onTimeAxisViewModelUpdate, this);
        this.timeAxisViewModel.refCount++;

        if (!this.viewPreset) {
            throw 'You must define a valid view preset object. See Sch.preset.Manager class for reference';
        }

        this.on('destroy', this.onPanelDestroyed, this);
        this.timeAxis.on('reconfigure', this.onTimeAxisReconfigure, this);

        this.addCls(['sch-timelinepanel', 'sch-' + this.orientation]);
    },

    onTimeAxisViewModelUpdate : function() {
        var view = this.getSchedulingView();

        if (view && view.viewReady) {
            view.refreshKeepingScroll();
        }
    },

    onPanelDestroyed : function() {
        this.timeAxisViewModel.un('update', this.onTimeAxisViewModelUpdate, this);
        this.timeAxisViewModel.refCount--;

        if (this.timeAxisViewModel.refCount <= 0) {
            this.timeAxisViewModel.destroy();
        }
        this.timeAxis.un('reconfigure', this.onTimeAxisReconfigure, this);
    },

    /**
    * @abstract
    * 
    * @return {Sch.mixin.AbstractSchedulerView} A view consuming the {@link Sch.mixin.AbstractSchedulerView} mixin
    */
    getSchedulingView: function () {
        throw 'Abstract method call';
    },

    /**
    * The {@link #readOnly} accessor. Use it to switch the `readonly` state. 
    */
    setReadOnly: function (readOnly) {
        this.getSchedulingView().setReadOnly(readOnly);
    },

    /**
    * Returns true if the panel is currently read only.
    * @return {Boolean} readOnly 
    */
    isReadOnly: function () {
        return this.getSchedulingView().isReadOnly();
    },

    /**
    * Switches the current view preset. See the {@link Sch.preset.Manager} class for details. Calling it will first fire a {@link #beforeviewchange} event,
    * followed by a {@link #viewchange} event. Returning `false` from any 'beforeviewchange' listener will cancel the switch.
    * 
    * @param {String} preset The name of the new preset
    * @param {Date} startDate (optional) A new start date for the time axis
    * @param {Date} endDate (optional) A new end date for the time axis
    */
    switchViewPreset: function (preset, startDate, endDate, initial) {

        // Timeaxis may already be configured, no need to reconfigure it
        if (initial && this.timeAxis.preset) {
            this.applyViewSettings(this.timeAxis.preset, initial);

            return;
        }

        if (this.fireEvent('beforeviewchange', this, preset, startDate, endDate) !== false) {
            if (Ext.isString(preset)) {
                this.viewPreset = preset;
                preset = Sch.preset.Manager.getPreset(preset);
            }

            if (!preset) {
                throw 'View preset not found';
            }

            var hConf = preset.headerConfig;

            var timeAxisCfg = {
                unit                : hConf.bottom ? hConf.bottom.unit : hConf.middle.unit,
                increment           : (hConf.bottom ? hConf.bottom.increment : hConf.middle.increment) || 1,
                resolutionUnit      : preset.timeResolution.unit,
                resolutionIncrement : preset.timeResolution.increment,

                weekStartDay        : this.weekStartDay,

                mainUnit            : hConf.middle.unit,
                shiftUnit           : preset.shiftUnit,

                headerConfig        : preset.headerConfig,
                shiftIncrement      : preset.shiftIncrement || 1,
                preset              : preset,
                defaultSpan         : preset.defaultSpan || 1
            };

            if (initial) {
                if (this.timeAxis.getCount() === 0 || startDate) {
                    timeAxisCfg.start = startDate || new Date();
                }
            } else {
                timeAxisCfg.start = startDate || this.timeAxis.getStart();
            }

            timeAxisCfg.end = endDate;
            this.timeAxis.reconfigure(timeAxisCfg);

            if (this.getOrientation() === 'horizontal') {
                this.getSchedulingView().scrollHorizontallyTo(0);
            } else {
                this.getSchedulingView().scrollVerticallyTo(0);
            }
        }
    },

    // Applies view specific settings from the preset about to be used
    applyViewSettings: function (preset, initial) {
        var view = this.getSchedulingView();

        view.setDisplayDateFormat(preset.displayDateFormat);

        if (this.orientation === 'horizontal') {
            view.setRowHeight(this.readRowHeightFromPreset ? preset.rowHeight : this.rowHeight, true);
        } else {
            // timeColumnWidth is used for row height in vertical mode
            var height = preset.timeColumnWidth || 60;

            view.setRowHeight(height, true);
            view.setColumnWidth(this.resourceColumnWidth || preset.resourceColumnWidth || 100, true);
        }
    },

    /**
    * Method to get a the current start date of the scheduler view
     * @return {Date} The start date
    */
    getStart: function () {
        return this.getStartDate();
    },

    /**
     * Method to get a the current start date of the scheduler view
     * @return {Date} The start date
     */
    getStartDate: function () {
        return this.timeAxis.getStart();
    },


    /**
    * Method to get a the current end date of the scheduler view
    * @return {Date} The end date
    */
    getEnd: function () {
        return this.getEndDate();
    },

    /**
     * Method to get a the current end date of the scheduler view
     * @return {Date} The end date
     */
    getEndDate: function () {
        return this.timeAxis.getEnd();
    },

    /**
    * Updates the widths of all time columns to the supplied value. Only applicable when forceFit is set to false on the view.
    * @param {Int} width The new time column width
    */
    setTimeColumnWidth: function (width, preventRefresh) {
        this.timeAxisViewModel.setTickWidth(width, preventRefresh);
    },

    /**
    * @return {Int} width The time column width
    */
    getTimeColumnWidth: function () {
        return this.timeAxisViewModel.getTickWidth();
    },

    // private
    onTimeAxisReconfigure: function (timeAxis) {
        // Apply view specific properties to the view
        // Subclass may decide which properties from the preset to use (orientation specific)
        this.applyViewSettings(timeAxis.preset);

        this.fireEvent('viewchange', this);
    },

    /**
    * Moves the time axis forward in time in units specified by the view preset 'shiftUnit', and by the amount specified by the parameter or by the shiftIncrement config of the current view preset.
    * @param {Int} amount (optional) The number of units to jump forward
    */
    shiftNext: function (amount) {
        this.suspendLayouts && this.suspendLayouts();

        this.timeAxis.shiftNext(amount);

        this.suspendLayouts && this.resumeLayouts(true);
    },

    /**
    * Moves the time axis backward in time in units specified by the view preset 'shiftUnit', and by the amount specified by the parameter or by the shiftIncrement config of the current view preset.
    * @param {Int} amount (optional) The number of units to jump backward
    */
    shiftPrevious: function (amount) {
        this.suspendLayouts && this.suspendLayouts();

        this.timeAxis.shiftPrevious(amount);

        this.suspendLayouts && this.resumeLayouts(true);
    },

    /**
    * Convenience method to go to current date.
    */
    goToNow: function () {
        this.setTimeSpan(new Date());
    },

    /**
    * Changes the time axis timespan to the supplied start and end dates.
    * @param {Date} start The new start date
    * @param {Date} end (Optional) The new end date. If not supplied, the {@link Sch.preset.ViewPreset#defaultSpan} property of the current view preset will be used to calculate the new end date.
    */
    setTimeSpan: function (start, end) {
        if (this.timeAxis) {
            this.timeAxis.setTimeSpan(start, end);
        }
    },

    /**
    * Changes the time axis start date to the supplied date.
    * @param {Date} amount The new start date
    */
    setStart: function (date) {
        this.setTimeSpan(date);
    },

    /**
    * Changes the time end start date to the supplied date.
    * @param {Date} amount The new end date
    */
    setEnd: function (date) {
        this.setTimeSpan(null, date);
    },

    /**
    * Returns the {@link Sch.data.TimeAxis} instance in use.
    * @return {Sch.data.TimeAxis}
    */
    getTimeAxis: function () {
        return this.timeAxis;
    },


    /**
    * Scrolls the time line to the specified `date`.
    * @param {Date} date The date to which to scroll the time line
    */
    scrollToDate: function (date, animate) {
        var view = this.getSchedulingView();
        var coordinate = view.getCoordinateFromDate(date, true);

        if (coordinate < 0) {
            // Not currently showing this date, adjust the timeaxis first
            var halfVisibleSpan = (this.timeAxis.getEnd() - this.timeAxis.getStart())/2;
            var me = this;

            this.setTimeSpan(new Date(date.getTime() - halfVisibleSpan), new Date(date.getTime() + halfVisibleSpan));

            // Need to wait for the view to refresh itself first
            setTimeout(function() {
                me.scrollToDate(date, animate);
            }, 20);

            return;
        }

        if(this.orientation === 'horizontal'){
            view.scrollHorizontallyTo(coordinate, animate);
        } 
        else {
            view.scrollVerticallyTo(coordinate, animate);
        }
    },

     /**
    * Returns the center date of the currently visible timespan of scheduler.
    *
    * @return {Date} date Center date for the viewport.
    */
    getViewportCenterDate: function(){
        var view       = this.getSchedulingView(),
            scroll     = view.getScroll(),
            xy;

        if (this.getOrientation() === 'vertical') {
            xy                 = [ 0, scroll.top + view.getViewportHeight() / 2 ];
        } else {     
            xy                 = [ scroll.left + view.getViewportWidth() / 2, 0 ];
        }

        return view.getDateFromXY(xy, null, true);
    },   

    addCls : function() {
        throw 'Abstract method call';
    },

    removeCls : function() {
        throw 'Abstract method call';
    },

    registerRenderer : function(fn, scope) {
        this.renderers.push({
            fn      : fn,
            scope   : scope
        });
    },

    deregisterRenderer : function(fn, scope) {
        Ext.each(this.renderers, function(rend, i) {
            if (fn === rend) {
                Ext.Array.removeAt(this.renderers, i);
                return false;
            }
        });
    }
});
/**

 @class Sch.mixin.TimelinePanel
 @extends Sch.mixin.AbstractTimelinePanel
 A base mixing for {@link Ext.panel.Panel} classes, giving to the consuming panel the "time line" functionality.
 This means that the panel will be capabale to display a list of "events", ordered on the {@link Sch.data.TimeAxis time axis}.

 Generally, should not be used directly, if you need to subclass the scheduler panel, subclass the {@link Sch.panel.SchedulerGrid} or {@link Sch.panel.SchedulerTree}
 instead.

*/

Ext.define('Sch.mixin.TimelinePanel', {
    extend : 'Sch.mixin.AbstractTimelinePanel',

    requires: [
        'Sch.util.Patch',

        'Sch.column.timeAxis.Horizontal',
        'Sch.preset.Manager'
    ],

    mixins: [
        'Sch.mixin.Zoomable',
        'Sch.mixin.Lockable'
    ],

    /**
    * @cfg {Object} lockedGridConfig A custom config object used to initialize the left (locked) grid panel.
    */

    /**
    * @cfg {Object} schedulerConfig A custom config object used to initialize the right (schedule) grid panel. 
    */

    /**
    * @cfg {Ext.Template} tooltipTpl 
    * Template used to show a tooltip over a scheduled item, null by default (meaning no tooltip). The tooltip will be populated with the data in 
    * record corresponding to the hovered element. See also {@link #tipCfg}.
    */

    /**
     * @cfg {Sch.mixin.TimelinePanel} partnerTimelinePanel A reference to another timeline panel that this panel should be 'partner' with.
     * If this config is supplied, this panel will:
     *
     * - Share and use the {@link Sch.data.TimeAxis} timeAxis from the partner panel.
     * - Synchronize the width of the two locked grid panels (after a drag of the splitter).
     * - Synchronize horizontal scrolling between two panels.
     */

    /**
    * @cfg {Object} tipCfg
    * The {@link Ext.Tooltip} config object used to configure a tooltip (only applicable if tooltipTpl is set).
    */
    tipCfg: {
        cls: 'sch-tip',

        showDelay: 1000,
        hideDelay: 0,

        autoHide: true,
        anchor: 'b'
    },
    
    inheritables: function() {

        return {
            // Configuring underlying table panel
            columnLines         : true,
            enableLocking       : true,
            lockable            : true,

            // EOF: Configuring underlying table panel

            // private
            initComponent: function () {
                if (this.partnerTimelinePanel) {
                    this.timeAxisViewModel = this.partnerTimelinePanel.timeAxisViewModel;
                    this.timeAxis = this.partnerTimelinePanel.getTimeAxis();
                    this.startDate  = this.timeAxis.getStart();
                    this.endDate    = this.timeAxis.getEnd();
                }

                // @COMPAT 2.2
                if (this.viewConfig && this.viewConfig.forceFit) this.forceFit = true;

                if (Ext.versions.extjs.isGreaterThanOrEqual("4.2.1")) {
                    this.cellTopBorderWidth = 0;
                }

                this._initializeTimelinePanel();

                this.configureColumns();

                var viewConfig = this.normalViewConfig = this.normalViewConfig || {};
                var id = this.getId();

                // Copy some properties to the view instance
                Ext.apply(this.normalViewConfig, {
                    id                      : id + '-timelineview',
                    eventPrefix             : this.autoGenId ? null : id,
                    timeAxisViewModel       : this.timeAxisViewModel,
                    eventBorderWidth        : this.eventBorderWidth,
                    timeAxis                : this.timeAxis,
                    readOnly                : this.readOnly,
                    orientation             : this.orientation,
                    rtl                     : this.rtl,
                    cellBorderWidth         : this.cellBorderWidth,
                    cellTopBorderWidth      : this.cellTopBorderWidth,
                    cellBottomBorderWidth   : this.cellBottomBorderWidth
                });

                Ext.Array.forEach(
                    [
                        "eventRendererScope",
                        "eventRenderer",
                        "dndValidatorFn",
                        "resizeValidatorFn",
                        "createValidatorFn",
                        "tooltipTpl",
                        "validatorFnScope",
                        "snapToIncrement",
                        "eventResizeHandles",
                        "enableEventDragDrop",
                        "enableDragCreation",
                        "resizeConfig",
                        "createConfig",
                        "tipCfg",
                        "getDateConstraints"
                    ],
                    function(prop) {
                      if (prop in this) viewConfig[prop] = this[prop];
                    },
                    this
                );

                this.mon(this.timeAxis, 'reconfigure', this.onMyTimeAxisReconfigure, this);

                this.addEvents(

                    /** 
                    * @event timeheaderclick
                    * Fires after a click on a time header cell
                    * @param {Sch.column.timeAxis.Horizontal} column The column object
                    * @param {Date} startDate The start date of the header cell
                    * @param {Date} endDate The start date of the header cell
                    * @param {Ext.EventObject} e The event object
                    */
                    'timeheaderclick',

                    /** 
                    * @event timeheaderdblclick
                    * Fires after a doubleclick on a time header cell
                    * @param {Sch.column.timeAxis.Horizontal} column The column object
                    * @param {Date} startDate The start date of the header cell
                    * @param {Date} endDate The start date of the header cell
                    * @param {Ext.EventObject} e The event object
                    */
                    'timeheaderdblclick',

                    /**
                    * @event beforeviewchange
                    * Fires before the current view changes to a new view type or a new time span. Return false to abort this action.
                    * @param {Sch.mixin.SchedulerPanel} scheduler The scheduler object
                    * @param {Object} preset The new preset
                    */
                    'beforeviewchange',

                    /**
                    * @event viewchange
                    * Fires after current view preset or time span has changed
                    * @param {Sch.mixin.SchedulerPanel} scheduler The scheduler object
                    */
                    'viewchange'
                );

                this.callParent(arguments);

                this.switchViewPreset(this.viewPreset, this.startDate || this.timeAxis.getStart(), this.endDate || this.timeAxis.getEnd(), true);

                var columnLines     = this.columnLines;
                
                if (columnLines) {
                    this.columnLinesFeature = new Sch.feature.ColumnLines(Ext.isObject(columnLines) ? columnLines : undefined);
                    this.columnLinesFeature.init(this);
                    
                    this.columnLines    = true;
                }

                this.relayEvents(this.getSchedulingView(), [
                    /**
                    * @event beforetooltipshow
                    * Fires before the event tooltip is shown, return false to suppress it.
                    * @param {Sch.mixin.TimelinePanel} scheduler The scheduler object
                    * @param {Sch.model.Event} eventRecord The event record of the clicked record
                    */
                    'beforetooltipshow'
                ]);

                this.on('afterrender', this.__onAfterRender, this);

                // HACK, required since Ext has an async scroll sync mechanism setup which won't play nice with our "sync scroll" above.
                this.on('zoomchange', function() {
                    // After a zoom, the header is resized and Ext JS TablePanel reacts to the size change.
                    // Ext JS reacts after a short delay, so we cancel this task to prevent Ext from messing up the scroll sync
                    this.normalGrid.scrollTask.cancel();
                });
            },

            getState: function () {
                var me = this,
                    state = me.callParent(arguments);
                    
                Ext.apply(state, {
                    viewPreset      : me.viewPreset,
                    startDate       : me.getStart(),
                    endDate         : me.getEnd(),
                    zoomMinLevel    : me.zoomMinLevel,
                    zoomMaxLevel    : me.zoomMaxLevel,
                    currentZoomLevel: me.currentZoomLevel
                });
                return state;
            },

            applyState: function (state) {
                var me = this;

                me.callParent(arguments);

                if (state && state.viewPreset) {
                    me.switchViewPreset(state.viewPreset, state.startDate, state.endDate);
                }
                if (state && state.currentZoomLevel){
                    me.zoomToLevel(state.currentZoomLevel);
                }
            }
        };
    },

    // private
    onMyTimeAxisReconfigure: function (timeAxis) {
        if (this.stateful && this.rendered) {
            this.saveState();
        }
    },

    onLockedGridItemDblClick : function(grid, record, el, rowIndex, event){
        if(this.orientation === 'vertical' && record) {
            this.fireEvent('timeheaderdblclick', this, record.get('start'), record.get('end'), rowIndex, event);
        }
    },

    /**
    * Returns the view which renders the schedule and time columns. This method should be used instead of the usual `getView`,
    * since `getView` will return an instance of a special "locking" grid view, which has no scheduler-specific features.
    * 
    * @return {Sch.mixin.SchedulerView} view A view implementing the {@link Sch.mixin.SchedulerView} mixin
    */
    getSchedulingView: function () {
        return this.normalGrid.getView();
    },

    getTimeAxisColumn : function () {
        if (!this.timeAxisColumn) {
            this.timeAxisColumn = this.down('timeaxiscolumn');
        }

        return this.timeAxisColumn;
    },

    configureColumns : function() {

        var columns         = this.columns || [];

        // The 'columns' config can also be a config object for Ext.grid.header.Container
        if (columns.items) {
            columns = columns.items;
        } else {
            // Clone it to make sure we handle the case of a column array object put on the class prototype
            columns = this.columns = columns.slice();
        }

        var lockedColumns   = [];
        var normalColumns   = [];

        // Split locked and normal columns first
        Ext.Array.each(columns, function (column) {
            if (column.position === 'right') {
                if (!Ext.isNumber(column.width)) {
                    Ext.Error.raise('"Right" columns must have a fixed width');
                }
                column.locked = false;

                normalColumns.push(column);
            } else {
                column.locked = true;
                lockedColumns.push(column);
            }
            column.lockable = false;
        });

        Ext.Array.erase(columns, 0, columns.length);
        Ext.Array.insert(columns, 0, lockedColumns.concat(
            {
                xtype                   : 'timeaxiscolumn',
                timeAxisViewModel       : this.timeAxisViewModel,
                trackHeaderOver         : this.trackHeaderOver,
                renderer                : this.mainRenderer,
                scope                   : this
            }
        ).concat(normalColumns));

        // Save reference to original set of columns
        this.horizontalColumns = Ext.Array.clone(columns);

        this.verticalColumns = [
            Ext.apply({
                xtype                   : 'verticaltimeaxis',
                width                   : 100,
                timeAxis                : this.timeAxis,
                timeAxisViewModel       : this.timeAxisViewModel,
                cellTopBorderWidth      : this.cellTopBorderWidth,
                cellBottomBorderWidth   : this.cellBottomBorderWidth
            }, this.timeAxisColumnCfg || {})
        ];

        if (this.orientation === 'vertical') {
            this.columns    = this.verticalColumns;
            this.store      = this.timeAxis;

            this.on('beforerender', function () {
                this.normalGrid.headerCt.add(this.createResourceColumns(this.resourceColumnWidth || this.timeAxis.preset.resourceColumnWidth));
            }, this);
        }
    },


    mainRenderer : function(val, meta, rowRecord, rowIndex, colIndex) {
        var rend = this.renderers,
            isHorizontal = this.orientation === 'horizontal',
            resource = isHorizontal ? rowRecord : this.resourceStore.getAt(colIndex),
            retVal = '&nbsp;'; // To ensure cells always consume correct height

        // Ext doesn't clear the meta object between cells
        meta.rowHeight = null;

        for (var i = 0; i < rend.length; i++) {
            retVal += rend[i].fn.call(rend[i].scope || this, val, meta, resource, rowIndex, colIndex) || '';
        }

        if (this.variableRowHeight) {
            // Set row height
            var view = this.getSchedulingView();
            var defaultRowHeight = isHorizontal ? view.getRowHeight() : this.timeAxisViewModel.getTickWidth();

            meta.style = 'height:' + ((meta.rowHeight || defaultRowHeight) - view.cellTopBorderWidth - view.cellBottomBorderWidth) + 'px';
        }

        return retVal;
    },

    // Child grids sync code
    // ---------------------------------
    __onAfterRender: function () {
        var me = this;

        me.normalGrid.on({
            collapse    : me.onNormalGridCollapse,
            expand      : me.onNormalGridExpand,
            scope       : me
        });

        me.lockedGrid.on({
            collapse    : me.onLockedGridCollapse,
            itemdblclick: me.onLockedGridItemDblClick,
            scope       : me
        });

        if (me.lockedGridDependsOnSchedule) {
            me.normalGrid.getView().on('itemupdate', me.onNormalViewItemUpdate, me);
        }

        if (this.partnerTimelinePanel) {
            if (this.partnerTimelinePanel.rendered) {
                this.setupPartnerTimelinePanel();
            } else {
                this.partnerTimelinePanel.on('afterrender', this.setupPartnerTimelinePanel, this);
            }
        }
    },


    onLockedGridCollapse : function() {
        if (this.normalGrid.collapsed) {
            this.normalGrid.expand();
        }
    },

    onNormalGridCollapse : function() {
        var me = this;

        //Hack for Gantt to prevent creating second expander when normal grid initially collapsed
        if(!me.normalGrid.reExpander){
            me.normalGrid.reExpander = me.normalGrid.placeholder;
        }

        if (!me.lockedGrid.rendered) {
            me.lockedGrid.on('render', me.onNormalGridCollapse, me, { delay: 1 });
        } else {
            me.lockedGrid.flex = 1;
            me.lockedGrid.doLayout();

            if (me.lockedGrid.collapsed) {
                me.lockedGrid.expand();
            }

            // Show a vertical scrollbar in locked grid if normal grid is collapsed
            me.addCls('sch-normalgrid-collapsed');
        }
    },

    onNormalGridExpand : function() {
        this.removeCls('sch-normalgrid-collapsed');

        delete this.lockedGrid.flex;
        this.lockedGrid.doLayout();
    },


    onNormalViewItemUpdate: function (record, index, oldRowEl) {
        if (this.lockedGridDependsOnSchedule) {
            var lockedView = this.lockedGrid.getView();

            lockedView.suspendEvents();
            lockedView.refreshNode(index);
            lockedView.resumeEvents();
        }
    },

    setupPartnerTimelinePanel : function () {

        // Sync locked grids by listening for splitter resize events of both locked grids.
        var otherPanel = this.partnerTimelinePanel;
        var externalSplitter = otherPanel.down('splitter');
        var ownSplitter = this.down('splitter');

        if (externalSplitter) {
            externalSplitter.on('dragend', function() {
                this.lockedGrid.setWidth(otherPanel.lockedGrid.getWidth());
            }, this);
        }

        if (ownSplitter) {
            ownSplitter.on('dragend', function() {
                otherPanel.lockedGrid.setWidth(this.lockedGrid.getWidth());
            }, this);
        }

        var lockedWidth = this.partnerTimelinePanel.lockedGrid.getWidth();
        this.lockedGrid.setWidth(lockedWidth);

        // sync scrolling with external timeline panel
        var otherViewEl  = this.partnerTimelinePanel.getSchedulingView().getEl(),
            ownViewEl = this.getSchedulingView().getEl();

        this.partnerTimelinePanel.mon(ownViewEl, 'scroll', function (e, el) {
            otherViewEl.scrollTo('left', el.scrollLeft);
        });

        this.mon(otherViewEl, 'scroll', function (e, el) {
            ownViewEl.scrollTo('left', el.scrollLeft);
        });
    }


    // EOF child grids sync code --------------------------
}, function () {
    var MIN_EXT_VERSION = '4.2.1';

    Ext.apply(Sch, {
        /*PKGVERSION*/VERSION : '2.2.15'
    });

    // DELETE THIS CHECK IF YOU WANT TO RUN AGAINST AN OLDER UNSUPPORTED EXT JS VERSION
    if (Ext.versions.extjs.isLessThan(MIN_EXT_VERSION)) {
        alert('The Ext JS version you are using needs to be updated to at least ' + MIN_EXT_VERSION);
    }
});
/**

@class Sch.mixin.AbstractSchedulerPanel
@private

A mixin providing "scheduling" functionality to the consuming "panel". 
A consuming class should have already consumed the {@link Sch.mixin.AbstractTimelinePanel} mixin.

This should not be used directly.

*/

Ext.define('Sch.mixin.AbstractSchedulerPanel', {

    requires: [
        'Sch.model.Event',
        'Sch.model.Resource',
        'Sch.data.EventStore', 
        'Sch.data.ResourceStore', 
        'Sch.util.Date'
    ],

    /**
    * @cfg {String} eventBarIconClsField
    * A field in the Event model whose value will be applied as a CSS class to each event bar to place a 16x16 icon.
    */
    eventBarIconClsField    : '',

    /**
    * @cfg {Boolean} enableEventDragDrop true to enable drag and drop of events, defaults to true
    */
    enableEventDragDrop: true,

    /**
    * @cfg {String} eventBarTextField The field in your data model that will be rendered into each event bar. 
    * You can alternatively use the eventBarRenderer to get full control over what gets displayed.
    */

    /**
     * @cfg {String} resourceColumnClass
     * Defines the column class for the resources, override this to use your own custom column class. (Used only in vertical orientation)
     */
    resourceColumnClass : "Sch.column.Resource",

    /**
     * @cfg {Number} resourceColumnWidth
     * Used only in vertical orientation. Defines the width of a single column.
     */
    resourceColumnWidth: null,

    /**
    * @cfg {Boolean} allowOverlap Set to false if you don't want to allow events overlapping (defaults to true).
    */
    allowOverlap: true,

    /**
    * @cfg {String} startParamName The name of the start date parameter that will be passed to in every `eventStore` load request.
    */
    startParamName: 'startDate',

    /**
    * @cfg {String} endParamName The name of the end date parameter that will be passed to in every `eventStore` load request.
    */
    endParamName: 'endDate',

    /**
    * @cfg {Boolean} passStartEndParameters true to apply start and end dates of the current view to any `eventStore` load requests.
    */
    passStartEndParameters: false,

    variableRowHeight : true,

    /**
     * @cfg {Int} barMargin
     * Controls how much space to leave between the event bars and the row borders. Defaults to 1.
     */

    /**
     * @cfg {Boolean} constrainDragToResource
     * Set to true to only allow dragging events within the same resource. Defaults to false.
     */

    /**
    * @cfg {Function} eventRenderer
    * An empty function by default, but provided so that you can override it. This function is called each time an event 
    * is rendered into the schedule to render the contents of the event. It's called with the event, its resource and a tplData object which
    * allows you to populate data placeholders inside the event template.
    * By default, the eventTpl includes placeholders for 'cls' and 'style'. The cls property is a CSS class which will be added to the
    * event element. The style property is an inline style declaration for the event element. If you override the default eventTpl, you can of course
    * include other placeholder in your template markup. Note: You will still need to keep the original built-in placeholders for the scheduler to work. 
    *
    * <pre>
    *  eventRenderer : function (eventRec, resourceRec, templateData) {
    *      templateData.style = 'color:white';                 // You can use inline styles too.
    *      templateData.cls = resourceRec.get('Category');     // Read a property from the resource record, used as a CSS class to style the event
    *
    *      return Ext.Date.format(eventRec.getStartDate(), 'Y-m-d') + ': ' + eventRec.getName();
    *  }
    *</pre>
    * @param {Sch.model.Event} eventRecord The event about to be rendered
    * @param {Sch.model.Resource} resourceRecord The resource row in which the event is being created
    * @param {Object} tplData An object that will be applied to the containing {@link #eventTpl}.
    * @param {Int} row The row index
    * @param {Int} col The column index
    * @param {Sch.data.ResourceStore} ds The resource store
    * @return {String/Object} A simple string, or a custom object which will be applied to the {@link #eventBodyTemplate}, creating the actual HTML
    */
    eventRenderer: null,

    /**
    * @cfg {Object} eventRendererScope The scope to use for the `eventRenderer` function 
    */
    eventRendererScope : null,

    /**
     * @cfg {Sch.data.EventStore} eventStore The {@link Ext.data.Store} holding the events to be rendered into the scheduler (required).
     * @required
    */
    eventStore: null,

    /**
    * @cfg {Sch.data.ResourceStore} resourceStore The {@link Ext.data.Store} holding the resources to be rendered into the scheduler (required).
    */
    resourceStore: null,

    /**
     * @method onEventCreated An empty function by default, but provided so that you can override it to supply default record values etc. This function is called after a new event has been created (but
     * before it is inserted to the store). This is for example called after a user dragged a new bar in the scheduler (the DragFreate feature).
     * @param {Sch.model.Event} eventRecord The event that was just created
     */
    onEventCreated: function (newEventRecord) {},

    /**
    * @cfg {Ext.Template} eventTpl The wrapping template used to renderer your events in the scheduler. Normally you should not override this,
    * only do so if you need total control of how the events are rendered/styled. See the {@link eventBodyTemplate} for more information. 
    */

    /**
    * @cfg {String/Ext.Template} eventBodyTemplate The template used to generate the markup of your events in the scheduler. To 'populate' the eventBodyTemplate with data, use the {@link eventRenderer} method 
    */

    /**
    *  @cfg {Object} timeAxisColumnCfg A {@link Ext.grid.column.Column} config used to configure the time axis column in vertical orientation.
    */


    initStores : function() {
        var resourceStore   = this.resourceStore || this.store;
        
        if (!resourceStore) {
            Ext.Error.raise("You must specify a resourceStore config");
        }

        if (!this.eventStore) {
            Ext.Error.raise("You must specify an eventStore config");
        }

        // Set "store" for the grid panel API
        this.store          = Ext.StoreManager.lookup(resourceStore);
        this.resourceStore  = this.store;
        this.eventStore     = Ext.StoreManager.lookup(this.eventStore);

        if (!this.eventStore.isEventStore) {
            Ext.Error.raise("Your eventStore should be a subclass of Sch.data.EventStore (or consume the EventStore mixin)");
        }

        this.resourceStore.eventStore = this.eventStore;

        if (this.passStartEndParameters) {
            this.eventStore.on('beforeload', this.applyStartEndParameters, this);
        }
    },

    _initializeSchedulerPanel : function() {
        this.initStores();
                
        if (this.eventBodyTemplate && Ext.isString(this.eventBodyTemplate)) {
            this.eventBodyTemplate = new Ext.XTemplate(this.eventBodyTemplate);
        }
    },

    /**
    * Returns the resource store instance
    * @return {Sch.data.ResourceStore}
    */
    getResourceStore: function () {
        return this.resourceStore;
    },

    /**
    * Returns the event store instance
    * @return {Sch.data.EventStore}
    */
    getEventStore: function () {
        return this.eventStore;
    },

    // Applies the start and end date to each event store request
    applyStartEndParameters: function (eventStore, options) {
        options.params = options.params || {};

        options.params[this.startParamName] = this.getStart();
        options.params[this.endParamName] = this.getEnd();
    },

    createResourceColumns : function (colWidth) {
        var newItems = [];

        var me = this;

        this.resourceStore.each(function (resource) {

            newItems.push(Ext.create(this.resourceColumnClass, {
                renderer : me.mainRenderer,
                scope    : me,
                width    : colWidth || 100,
                text     : resource.getName(),
                model    : resource
            }));
        }, this);

        return newItems;
    }
});


/**

 @class Sch.mixin.SchedulerPanel
 @extends Sch.mixin.AbstractSchedulerPanel

 A mixin for {@link Ext.panel.Panel} classes, providing "scheduling" functionality to the consuming panel.
 A consuming class should have already consumed the {@link Sch.mixin.TimelinePanel} mixin.

 Generally, should not be used directly, if you need to subclass the scheduler panel, subclass the {@link Sch.panel.SchedulerGrid} or {@link Sch.panel.SchedulerTree}
 instead.

 */
Ext.define('Sch.mixin.SchedulerPanel', {

    extend : 'Sch.mixin.AbstractSchedulerPanel',

    requires : [
        'Sch.view.SchedulerGridView',
        'Sch.selection.EventModel',
        'Sch.plugin.ResourceZones',
        'Sch.column.timeAxis.Vertical'
    ],

    /**
     * @cfg {String} eventSelModelType The xtype of the selection model to be used to events. Should be a {@link Sch.selection.EventModel} or its subclass.
     */
    eventSelModelType : 'eventmodel',

    /**
     * @cfg {Object} eventSelModel The configuration object for the event selection model. See {@link Sch.selection.EventModel} for available configuration options.
     */
    eventSelModel : null,

    /**
     * @cfg {Boolean} enableEventDragDrop true to enable drag and drop of events, defaults to true
     */
    enableEventDragDrop : true,

    /**
     * @cfg {Boolean} enableDragCreation true to enable creating new events by click and drag, defaults to true
     */
    enableDragCreation : true,

    /**
     * @cfg {Object} dragConfig Custom config to pass to the {@link Sch.feature.SchedulerDragZone}
     * instance which will be created by {@link Sch.feature.DragDrop}.
     */
    dragConfig : null,

    /**
     * @cfg {Object} timeAxisColumnCfg A {@link Ext.grid.column.Column} config used to configure the time axis column in vertical orientation.
     */

    /**
     * @cfg {Sch.data.EventStore} resourceZones A special store containing data used to highlight the underlying schedule for the resources,
     * using {@link Sch.plugin.ResourceZones}. This can be used to color non-working time or any other meta data associated with a resource.
     * See also {@link #resourceZonesConfig}.
     */
    resourceZones : null,

    /**
     * @cfg {Object} resourceZonesConfig An object with configuration options for {@link Sch.plugin.ResourceZones}. Ignored, if no {@link #resourceZones}
     * config is provided.
     */
    resourceZonesConfig : null,

    /**
     * @cfg {Object} createConfig Custom config to pass to the {@link Sch.feature.DragCreator} instance
     */

    /**
     * @cfg {Object} resizeConfig Custom config to pass to the {@link Sch.Resize} instance
     */

    componentCls                : 'sch-schedulerpanel',

    // even that this config "belongs" to the Sch.mixin.TimelinePanel mixin
    // we can't define it there, because of various reasons (extjs mixin system)
    // this is guarded by the 203_buffered_view_1.t.js test in gantt and 092_rowheight.t.js in scheduler
    /**
     * @ignore
     * @cfg {Boolean} lockedGridDependsOnSchedule set this to true if you require the left (locked) grid section to be refreshed when the schedule is updated.
     */
    lockedGridDependsOnSchedule : true,

    /**
     * @cfg {Boolean} [multiSelect=false]
     * True to allow selection of more than one event at a time, false to allow selection of only a single item
     * at a time or no selection at all, depending on the value of {@link #singleSelect}.
     */
    /**
     * @cfg {Boolean} [singleSelect]
     * Allows selection of exactly one event at a time. As this is the default selection mode anyway, this config
     * is completely ignored.
     */
    /**
     * @cfg {Boolean} [simpleSelect=false]
     * True to enable multiselection by clicking on multiple events without requiring the user to hold Shift or Ctrl,
     * false to force the user to hold Ctrl or Shift to select more than on item.
     */

    /**
     * @method dndValidatorFn
     * An empty function by default, but provided so that you can perform custom validation on
     * the item being dragged. This function is called during a drag and drop process and also after the drop is made.  To control what 'this' points to inside this function, use {@link #validatorFnScope}.
     * @param {Array} dragRecords an array containing the records for the events being dragged
     * @param {Sch.model.Resource} targetResourceRecord the target resource of the the event
     * @param {Date} date The date corresponding to the drag proxy position
     * @param {Int} duration The duration of the item being dragged in milliseconds
     * @param {Event} e The event object
     * @return {Boolean} true if the drop position is valid, else false to prevent a drop
     */

    /**
     * @method resizeValidatorFn
     * An empty function by default, but provided so that you can perform custom validation on
     * an item being resized. To control what 'this' points to inside this function, use {@link #validatorFnScope}.
     * @param {Sch.model.Resource} resourceRecord the resource of the row in which the event is located
     * @param {Sch.model.Event} eventRecord the event being resized
     * @param {Date} startDate
     * @param {Date} endDate
     * @param {Event} e The event object
     * @return {Boolean} true if the resize state is valid, else false
     */

    /**
     * @method createValidatorFn
     * An empty function by default, but provided so that you can perform custom validation on the item being created. To control what 'this' points to inside this function, use {@link #validatorFnScope}.
     * @param {Sch.model.Resource} resourceRecord the resource for which the event is being created
     * @param {Date} startDate
     * @param {Date} endDate
     * @param {Event} e The event object
     * @return {Boolean} true if the creation event is valid, else false
     */

    inheritables : function () {
        return {
            // private
            initComponent : function () {
                var viewConfig = this.normalViewConfig = this.normalViewConfig || {};
                this._initializeSchedulerPanel();

                Ext.apply(viewConfig, {
                    eventStore        : this.eventStore,
                    resourceStore     : this.resourceStore,
                    eventBarTextField : this.eventBarTextField || this.eventStore.model.prototype.nameField
                });

                Ext.Array.forEach(
                    [
                        "barMargin",
                        "eventBodyTemplate",
                        "eventTpl",
                        "allowOverlap",
                        "dragConfig",
                        "eventBarIconClsField",
                        "onEventCreated",
                        "constrainDragToResource"
                    ],
                    function (prop) {
                        if (prop in this) viewConfig[prop] = this[prop];
                    },
                    this
                );

                if (this.orientation === 'vertical') {
                    this.mon(this.resourceStore, {
                        clear       : this.refreshResourceColumns,
                        datachanged : this.refreshResourceColumns,
                        update      : this.refreshResourceColumns,
                        load        : this.refreshResourceColumns,
                        scope       : this
                    });
                }

                this.callParent(arguments);

                var lockedView      = this.lockedGrid.getView();
                var normalView      = this.getSchedulingView();
                
                this.registerRenderer(normalView.columnRenderer, normalView);

                if (this.resourceZones) {
                    var resourceZoneStore = Ext.StoreManager.lookup(this.resourceZones);
                    resourceZoneStore.setResourceStore(this.resourceStore);

                    this.resourceZonesPlug = new Sch.plugin.ResourceZones(Ext.apply({
                        store : resourceZoneStore
                    }, this.resourceZonesConfig));

                    this.resourceZonesPlug.init(this);
                }

                normalView.on('columnwidthchange', this.onColWidthChange, this);

                // Relaying after parent class has setup the locking grid components
                this.relayEvents(this.getSchedulingView(), [
                /**
                 * @event eventclick
                 * Fires when an event is clicked
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} eventRecord The event record of the clicked event
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventclick',

                /**
                 * @event eventmousedown
                 * Fires when a mousedown event is detected on a rendered event
                 * @param {Mixed} view The scheduler view instance
                 * @param {Sch.model.Event} eventRecord The event record
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventmousedown',

                /**
                 * @event eventmouseup
                 * Fires when a mouseup event is detected on a rendered event
                 * @param {Mixed} view The scheduler view instance
                 * @param {Sch.model.Event} eventRecord The event record
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventmouseup',

                /**
                 * @event eventdblclick
                 * Fires when an event is double clicked
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} eventRecord The event record of the clicked event
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventdblclick',

                /**
                 * @event eventcontextmenu
                 * Fires when contextmenu is activated on an event
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} eventRecord The event record of the clicked event
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventcontextmenu',

                /**
                 * @event eventmouseenter
                 * Fires when the mouse moves over an event
                 * @param {Mixed} view The scheduler view instance
                 * @param {Sch.model.Event} eventRecord The event record
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventmouseenter',

                /**
                 * @event eventmouseleave
                 * Fires when the mouse moves out of an event
                 * @param {Mixed} view The scheduler view instance
                 * @param {Sch.model.Event} eventRecord The event record
                 * @param {Ext.EventObject} e The event object
                 */
                    'eventmouseleave',

                    // Resizing events start --------------------------
                /**
                 * @event beforeeventresize
                 * Fires before a resize starts, return false to stop the execution
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} record The record about to be resized
                 * @param {Ext.EventObject} e The event object
                 */
                    'beforeeventresize',

                /**
                 * @event eventresizestart
                 * Fires when resize starts
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} record The event record being resized
                 */
                    'eventresizestart',

                /**
                 * @event eventpartialresize
                 * Fires during a resize operation and provides information about the current start and end of the resized event
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} record The event record being resized
                 * @param {Date} startDate The new start date of the event
                 * @param {Date} endDate The new end date of the event
                 * @param {Ext.Element} element The proxy element being resized
                 */
                    'eventpartialresize',

                /**
                 * @event beforeeventresizefinalize
                 * Fires before a succesful resize operation is finalized. Return false from a listener function to prevent the finalizing to
                 * be done immediately, giving you a chance to show a confirmation popup before applying the new values.
                 * To finalize the operation, call the 'finalize' method available on the resizeContext object.
                 * @param {Mixed} view The scheduler view instance
                 * @param {Object} resizeContext An object containing 'eventRecord', 'start', 'end' and 'finalize' properties.
                 * @param {Ext.EventObject} e The event object
                 */
                    'beforeeventresizefinalize',

                /**
                 * @event eventresizeend
                 * Fires after a succesful resize operation
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} record The updated event record
                 */
                    'eventresizeend',
                    // Resizing events end --------------------------

                    // Dnd events start --------------------------
                /**
                 * @event beforeeventdrag
                 * Fires before a dnd operation is initiated, return false to cancel it
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} record The record corresponding to the node that's about to be dragged
                 * @param {Ext.EventObject} e The event object
                 */
                    'beforeeventdrag',

                /**
                 * @event eventdragstart
                 * Fires when a dnd operation starts
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Array} records the records being dragged
                 */
                    'eventdragstart',

                /**
                 * @event eventdrag
                 * Fires before a dnd operation is initiated, return false to cancel it
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event[]} records The records being dragged
                 * @param {Date} date The new start date of the main event record
                 * @param {Sch.model.Resource} date The new resource for the main event record
                 * @param {Object} dragData A custom drag drop context object
                 */
                    'eventdrag',

                /**
                 * @event beforeeventdropfinalize
                 * Fires before a succesful drop operation is finalized. Return false to finalize the drop at a later time.
                 * To finalize the operation, call the 'finalize' method available on the context object.
                 * @param {Mixed} view The scheduler view instance
                 * @param {Object} dragContext An object containing 'eventRecord', 'start', 'end', 'newResource', 'finalize' properties.
                 * @param {Ext.EventObject} e The event object
                 */
                    'beforeeventdropfinalize',

                /**
                 * @event eventdrop
                 * Fires after a succesful drag and drop operation
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {[Sch.model.Event]} records the affected records (if copies were made, they were not inserted into the store)
                 * @param {Boolean} isCopy True if the records were copied instead of moved
                 */
                    'eventdrop',

                /**
                 * @event aftereventdrop
                 * Fires when after a drag n drop operation, even when drop was performed on an invalid location
                 * @param {SchedulerView} scheduler The scheduler view
                 */
                    'aftereventdrop',
                    // Dnd events end --------------------------

                    // Drag create events start --------------------------
                /**
                 * @event beforedragcreate
                 * Fires before a drag starts, return false to stop the execution
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Resource} resource The resource record
                 * @param {Date} date The clicked date on the timeaxis
                 * @param {Ext.EventObject} e The event object
                 */
                    'beforedragcreate',

                /**
                 * @event dragcreatestart
                 * Fires before a drag starts
                 * @param {SchedulerView} scheduler The scheduler view
                 */
                    'dragcreatestart',

                /**
                 * @event beforedragcreatefinalize
                 * Fires before a succesful resize operation is finalized. Return false from a listener function to prevent the finalizing to
                 * be done immediately, giving you a chance to show a confirmation popup before applying the new values.
                 * To finalize the operation, call the 'finalize' method available on the createContext object.
                 * @param {Mixed} view The scheduler view instance
                 * @param {Object} createContext An object containing, 'start', 'end', 'resourceRecord' properties.
                 * @param {Ext.EventObject} e The event object
                 */
                    'beforedragcreatefinalize',

                /**
                 * @event dragcreateend
                 * Fires after a successful drag-create operation
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} newEventRecord The newly created event record (added to the store in onEventCreated method)
                 * @param {Sch.model.Resource} resource The resource record to which the event belongs
                 * @param {Ext.EventObject} e The event object
                 */
                    'dragcreateend',

                /**
                 * @event afterdragcreate
                 * Always fires after a drag-create operation
                 * @param {SchedulerView} scheduler The scheduler view
                 */
                    'afterdragcreate',
                    // Drag create events end --------------------------

                /**
                 * @event beforeeventadd
                 * Fires after a successful drag-create operation, before the new event is added to the store. Return false to prevent the event from being added to the store.
                 * @param {SchedulerView} scheduler The scheduler view
                 * @param {Sch.model.Event} newEventRecord The newly created event record
                 */
                    'beforeeventadd',

                /**
                 * @event scheduleclick
                 * Fires after a click on the schedule area
                 * @param {Sch.mixin.TimelinePanel} scheduler The scheduler object
                 * @param {Date} clickedDate The clicked date
                 * @param {Int} rowIndex The row index
                 * @param {Sch.model.Resource} resource The resource, an event occured on
                 * @param {Ext.EventObject} e The event object
                 */
                    'scheduleclick',

                /**
                 * @event scheduledblclick
                 * Fires after a doubleclick on the schedule area
                 * @param {Sch.mixin.TimelinePanel} scheduler The scheduler object
                 * @param {Date} clickedDate The clicked date
                 * @param {Int} rowIndex The row index
                 * @param {Sch.model.Resource} resource The resource, an event occured on
                 * @param {Ext.EventObject} e The event object
                 */
                    'scheduledblclick',

                /**
                 * @event schedulecontextmenu
                 * Fires after a context menu click on the schedule area
                 * @param {Sch.mixin.TimelinePanel} scheduler The scheduler object
                 * @param {Date} clickedDate The clicked date
                 * @param {Int} rowIndex The row index
                 * @param {Sch.model.Resource} resource The resource, an event occured on
                 * @param {Ext.EventObject} e The event object
                 */
                    'schedulecontextmenu'
                ]);

                this.addEvents(
                    /**
                     * @event orientationchange
                     * Fires after an orientation change
                     * @param {SchedulerPanel} scheduler The scheduler panel
                     * @param {String} orientation The new orientation ('horizontal' or 'vertical')
                     */
                    'orientationchange'
                );

                // enable our row height injection if the default extjs row height synching mechanism is disabled
                // (it is disabled by default in our Lockable mixin, because it's slow)
                if (!this.syncRowHeight) this.enableRowHeightInjection(lockedView, normalView);
            }
        };
    },

    onColWidthChange : function (view, width) {
        if (this.getOrientation() === 'vertical') {
            this.resourceColumnWidth = width;
            this.refreshResourceColumns();
        }
    },


    enableRowHeightInjection : function (lockedView, schedulingView) {
        var cellTpl = new Ext.XTemplate(
            '{%',
            'this.processCellValues(values);',
            'this.nextTpl.applyOut(values, out, parent);',
            '%}',
            {
                priority          : 1,
                processCellValues : function (cellValues) {
                    if (schedulingView.orientation === 'horizontal') {
                        var layout = schedulingView.eventLayout.horizontal;
                        var resource = cellValues.record;

                        var height = layout.getRowHeight(resource) - schedulingView.cellTopBorderWidth - schedulingView.cellBottomBorderWidth;

                        cellValues.style = (cellValues.style || '') + ';height:' + height + 'px;';
                    }
                }
            }
        );

        lockedView.addCellTpl(cellTpl);
        
        // on the `refresh` event from the store, we want the normal view to be refreshed first,
        // because refreshing it will also cache the events layout data. After that, the locked view will just reuse the 
        // cached data, otherwise the layout data would be calculated twice
        lockedView.store.un('refresh', lockedView.onDataRefresh, lockedView);
        lockedView.store.on('refresh', lockedView.onDataRefresh, lockedView);
    },


    /**
     * Returns the selection model being used, and creates it via the configuration
     * if it has not been created already.
     * @return {Sch.selection.EventModel} selModel
     */
    getEventSelectionModel : function () {
        // already has the event selection model
        if (this.eventSelModel && this.eventSelModel.events) {
            return this.eventSelModel;
        }

        if (!this.eventSelModel) {
            this.eventSelModel = {};
        }

        var eventSelModel = this.eventSelModel;

        var mode = 'SINGLE';

        if (this.simpleSelect) {
            mode = 'SIMPLE';
        } else if (this.multiSelect) {
            mode = 'MULTI';
        }

        Ext.applyIf(eventSelModel, {
            allowDeselect : this.allowDeselect,
            mode          : mode
        });

        if (!eventSelModel.events) {
            eventSelModel = this.eventSelModel = Ext.create('selection.' + this.eventSelModelType, eventSelModel);
        }

        if (!eventSelModel.hasRelaySetup) {
            this.relayEvents(eventSelModel, [
            /**
             * @event
             * Fired after a selection change has occurred
             * @param {Sch.selection.EventModel} this
             * @param {[Sch.model.Event]} selected The selected events
             */
                'selectionchange',

            /**
             * @event deselect
             * Fired after a record is deselected
             * @param {Sch.selection.EventModel} this
             * @param  {Sch.model.Event} record The deselected event
             */
                'deselect',

            /**
             * @event select
             * Fired after a record is selected
             * @param {Sch.selection.EventModel} this
             * @param  {Sch.model.Event} record The selected event
             */
                'select'
            ]);
            eventSelModel.hasRelaySetup = true;
        }

        // lock the selection model if user
        // has disabled selection
        if (this.disableSelection) {
            eventSelModel.locked = true;
        }

        return eventSelModel;
    },

    refreshResourceColumns : function () {
        var headerCt = this.normalGrid.headerCt;

        headerCt.suspendLayouts();
        headerCt.removeAll();

        headerCt.add(this.createResourceColumns(this.resourceColumnWidth || this.timeAxis.preset.resourceColumnWidth));
        headerCt.resumeLayouts();

        this.getView().refresh();
    },


    /**
     * Switches the orientation of this panel
     *
     * @param {String} orientation Either "horizontal" or "vertical"
     */
    setOrientation : function (orientation, force) {
        if (orientation === this.orientation && !force) {
            return;
        }

        this.removeCls('sch-' + this.orientation);
        this.addCls('sch-' + orientation);

        this.orientation = orientation;

        var me = this,
            normalGrid = me.normalGrid,
            schedulingView = me.getSchedulingView(),
            normalHeaderCt = normalGrid.headerCt;

        schedulingView.setOrientation(orientation);

        Ext.suspendLayouts();
        normalHeaderCt.removeAll(true);
        Ext.resumeLayouts();

        if (orientation === 'horizontal') {
            me.mun(me.resourceStore, {
                clear       : me.refreshResourceColumns,
                datachanged : me.refreshResourceColumns,
                load        : me.refreshResourceColumns,

                scope : me
            });

            schedulingView.setRowHeight(me.rowHeight || me.timeAxis.preset.rowHeight, true);
            me.reconfigure(me.resourceStore, me.horizontalColumns);
        } else {

            me.mon(me.resourceStore, {
                clear       : me.refreshResourceColumns,
                datachanged : me.refreshResourceColumns,
                load        : me.refreshResourceColumns,

                scope : me
            });

            me.reconfigure(me.timeAxis, me.verticalColumns.concat(me.createResourceColumns(me.resourceColumnWidth || me.timeAxis.preset.resourceColumnWidth)));
            schedulingView.setColumnWidth(me.timeAxis.preset.resourceColumnWidth || 100, true);
        }

        this.fireEvent('orientationchange', this, orientation);
    }
});


// !XXX when adding new methods to this mixing need to also update the 
// `setupLockableTree` method in the Sch.mixin.Lockable
Ext.define("Sch.mixin.FilterableTreeView", {
    
    prevBlockRefresh        : null,
    
    
    initTreeFiltering : function () {
        var doInit  = function () {
            var treeStore       = this.up('tablepanel').store;

            // if are we inside of locked grid 
            if (treeStore instanceof Ext.data.NodeStore) treeStore  = this.up('tablepanel[lockable=true]').store;
            
            this.mon(treeStore, 'nodestore-datachange-start', this.onFilterChangeStart, this);
            this.mon(treeStore, 'nodestore-datachange-end', this.onFilterChangeEnd, this);
            
            this.mon(treeStore, 'filter-clear', this.onFilterCleared, this);
            this.mon(treeStore, 'filter-set', this.onFilterSet, this);
        };
        
        if (this.rendered)
            doInit.call(this);
        else
            this.on('beforerender', doInit, this, { single : true });
    },
    
    
    onFilterChangeStart : function () {
        this.prevBlockRefresh   = this.blockRefresh;
        
        // block refresh in filterable tree view, for the cases when this mixin is consumed by non-tree views
        // "blockRefresh" is already true in all tree views, so in the Sch.data.mixin.FilterableTreeStore
        // we use "nodeStore.fireEvent('clear', nodeStore);" hack, which is directly tied to "refresh" method of the view
        // in case when non-tree view consumes this mixin, we need to temporarily block the refresh manually,
        // to avoid double refresh
        this.blockRefresh       = true;
        
        Ext.suspendLayouts();
    },
    
    
    onFilterChangeEnd : function () {
        Ext.resumeLayouts(true);
        
        this.blockRefresh       = this.prevBlockRefresh;
    },
    
    
    onFilterCleared : function () {
        delete this.toggle;
        
        var el          = this.getEl();
        
        if (el) el.removeCls('sch-tree-filtered');
    },
    
    
    onFilterSet : function () {
        this.toggle     = function () {};
        
        var el          = this.getEl();
        
        if (el) el.addCls('sch-tree-filtered');
    }
});
/**

@class Sch.panel.TimelineGridPanel
@extends Ext.grid.Panel
@mixin Sch.mixin.TimelinePanel

Internal class. 

*/
Ext.define("Sch.panel.TimelineGridPanel", {
    extend  : "Ext.grid.Panel",
    mixins  : ['Sch.mixin.TimelinePanel'],
    subGridXType            : 'gridpanel',

    requires : [
        'Sch.patches.ColumnResize'
    ],

    initComponent : function() {
        this.callParent(arguments);
        this.getSchedulingView()._initializeTimelineView();
    }
}, function() {
    this.override(Sch.mixin.TimelinePanel.prototype.inheritables() || {});
});
/**

@class Sch.panel.TimelineTreePanel
@extends Ext.tree.Panel
@mixin Sch.mixin.TimelinePanel

Internal class.

*/
Ext.define("Sch.panel.TimelineTreePanel", {
    extend      : "Ext.tree.Panel",
    requires    : [
        // need to require grid panel too here, because one of the sub-grids will be a normal grid
        'Ext.grid.Panel',
        'Ext.data.TreeStore',
        // will be used in the `setupLockableTree` of lockable mixin
        'Sch.mixin.FilterableTreeView',

        'Sch.patches.ColumnResizeTree'
    ],
    mixins      : ['Sch.mixin.TimelinePanel'],

    useArrows   : true,
    rootVisible : false,
    lockedXType : 'treepanel',

    initComponent : function() {
        this.callParent(arguments);
        this.getSchedulingView()._initializeTimelineView();
    }
}, function() { 
    this.override(Sch.mixin.TimelinePanel.prototype.inheritables() || {});
});
/** 

@class Sch.panel.SchedulerGrid
@extends Sch.panel.TimelineGridPanel
@mixin Sch.mixin.SchedulerPanel

@alternateClassName Sch.SchedulerPanel

A scheduler panel based on the {@link Ext.grid.Panel} class which allows you to visualize and manage "resources" and their scheduled "events".

Please refer to the <a href="#!/guide/scheduler_getting_started">getting started guide</a> for a detailed introduction.

{@img scheduler/images/ext-scheduler.png}

*/
Ext.define("Sch.panel.SchedulerGrid", {
    extend                  : "Sch.panel.TimelineGridPanel",
    mixins                  : [ 
        'Sch.mixin.SchedulerPanel'
    ],
    alias                   : [ 'widget.schedulergrid', 'widget.schedulerpanel'],
    alternateClassName      : 'Sch.SchedulerPanel',
    viewType                : 'schedulergridview',

    initComponent : function() {
        this.callParent(arguments);
        this.getSchedulingView()._initializeSchedulerView();
    }

}, function() {
    this.override(Sch.mixin.SchedulerPanel.prototype.inheritables() || {});
});
/** 

@class Sch.panel.SchedulerTree
@extends Sch.panel.TimelineTreePanel
@mixin Sch.mixin.SchedulerPanel

A complete scheduler panel using a tree view. This class can be used when you have a lot of resources organized in a hierarchical structure.

{@img scheduler/images/scheduler-tree.png}

Please refer to the {@link Sch.panel.SchedulerGrid} for additional documentation, as these classes
are virtually identical. This document will only provide the tree-specific details.

In the scheduler tree case:

- the `resourceStore` is expected to be an instance of the {@link Sch.data.ResourceTreeStore}.
- the scheduler tree does not support vertical orientation.
- in your columns definition, you must include a column which will contain the tree itself (the `treecolumn` xtype):

        var schedulerTree = Ext.create('Sch.panel.SchedulerTree', {
            columns: [
                {
                    xtype       : 'treecolumn',
                    
                    text        : 'Name',
                    width       : 200,
                    sortable    : true,
                    dataIndex   : 'Name'
                }
            ]
        });


*/
Ext.define("Sch.panel.SchedulerTree", {
    extend      : "Sch.panel.TimelineTreePanel",
    mixins      : ['Sch.mixin.SchedulerPanel'], 
    alias       : [ 'widget.schedulertree'],
    viewType    : 'schedulergridview',

    setOrientation : function (orientation) {
        if (orientation == 'vertical') {
            Ext.Error.raise("Sch.panel.SchedulerTree does not support vertical orientation");
        }
    },

    initComponent : function() {
        this.callParent(arguments);
        this.getSchedulingView()._initializeSchedulerView();
    }
}, function() {
    this.override(Sch.mixin.SchedulerPanel.prototype.inheritables() || {});
});
