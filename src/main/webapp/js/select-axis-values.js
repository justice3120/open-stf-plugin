jQuery(function() {
    jQuery.each(jQuery('select[fillurl *= "hudson.plugins.openstf.axis"]'), function(i, select) {
        jQuery(select).on('filled', function(e) {
            var axisName = jQuery(e.target).attr('fillurl').split('/')[5].split('.').last();
            eval("desc" + axisName).getCachedValuesJSON(function(t) {
                var cachedValues = t.responseJSON;
                jQuery.each(jQuery(select).children('option'), function(i, option) {
                    if (cachedValues.includes(jQuery(option).text())) {
                        jQuery(option).attr('selected', true);
                    }
                })
            });
        });
    });
});
