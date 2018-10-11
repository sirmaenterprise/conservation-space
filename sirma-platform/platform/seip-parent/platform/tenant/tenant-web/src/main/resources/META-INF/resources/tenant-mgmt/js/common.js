$(document).ready(function () {
    $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
        // Set the jwt token of all request.
        options.url += getSecurityInfo();
    });

    /**
     * Get an url parameter from the current page's url.
     *
     * @param param
     *            the param to look for
     */
    function getUrlParameter(param) {
        var url = decodeURIComponent(window.location.search.substring(1));
        var variables = url.split('&');
        var paramName;
        var i;

        for (i = 0; i < variables.length; i++) {
            paramName = variables[i].split('=');

            if (paramName[0] === param) {
                return paramName[1] === undefined ? true : paramName[1];
            }
        }
    }

    $("#menu-create-tenant").click(function () {
        openPage("./index.html");
    });

    $("#menu-update-tenant").click(function () {
        openPage("./update.html");
    });

    $("#menu-manage-tenant").click(function () {
        openPage("./manage.html");
    });

    $("#logout").click(function () {
        var redirectAfterLogout = window.location.pathname;

        // append the security info token to know which user to logout and
        // appends redirect location to the current page so that after logging again will open the same page
        window.location.replace("../ServiceLogout" + getSecurityInfo() + "&RelayState=" + redirectAfterLogout);
    });

    /**
     * Opens the given page address in the same window but before that appends the security token to the address.
     * Used for menu actions
     *
     * @param page
     *            the page to open
     */
    function openPage(page) {
        var link = page + getSecurityInfo();
        window.open(link, "_self")
    }

    function getSecurityInfo() {
        return "?jwt=" + getUrlParameter("jwt");
    }
});