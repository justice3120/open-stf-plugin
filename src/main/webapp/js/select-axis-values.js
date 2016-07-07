Q(function() {
    Q.each(Q('select[fillurl *= "hudson.plugins.openstf.axis.STFDeviceConditionAxis/fillValuesItems"]'), function(i, select) {
        Q(select).on('filled', function(e) {
            // Convert string (formated: "[x, y, z]") to Array
            var cachedValues = Q(select).attr('value').slice(1, -1).replace(/,\s+|\s+,/g, ',').split(',');

            Q.each(Q(select).children('option'), function(i, option) {
                if (cachedValues.includes(Q(option).text())) {
                    Q(option).attr('selected', true);
                }
            })
        });
    });
});
