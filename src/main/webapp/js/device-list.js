Behaviour.specify("INPUT.repeatable-add", 'repeatable', 0, function(e) {
    makeButton(e,function(e) {
        repeatableSupport.onAdd(e.target);

        //hack jenkins default behaviour
        if (Q('#openSTFPluginContent').find(Q(e.target)).length) {
          Q('#openSTFPluginContent').find('.repeated-chunk.last').find('.stf-filter').each(function() {
            Q(this).blur(updateDeviceList);
          });
        }
    });
    e = null;
});

Q(document).ready(function(){
  updateDeviceList();

  Q('.stf-filter').each(function(){
    Q(this).blur(updateDeviceList);
  });
});

function updateDeviceList(evt) {
    var filter = {};
    Q('.stf-filter').each(function(){
      var key = Q(this).closest('tbody').find('select').val();
      var value = Q(this).val() == "" ? "any" : Q(this).val();
      if (key) {
        filter[key] = value;
      }
    });
    desc.getStfApiEndpoint(function(t){
        var stfUrlArray = t.responseJSON.split("/");
        desc.getDeviceListJSON(filter, function(t) {
            var devices = t.responseJSON;
            Q('#deviceList').html('');
            Q.each(devices, function(index, device){
              var deviceAttrList = Q('<table />').addClass('device-attr-table');
              Q.each(device, function(k, v){
                if (Q.inArray(k, ['image', 'remoteConnectUrl']) == -1) {
                  var tdKey = Q('<td />').text(k).addClass('device-attr');
                  var tdValue = Q('<td />').text(v).addClass('device-attr');

                  if ((k == 'owner' || k == 'provider') && v != null) {
                    tdValue.text(v.name);
                  }

                  Q('<tr />').append(tdKey).append(tdValue).appendTo(deviceAttrList);
                }
              });
              var div = Q('<div />').addClass('device-list-item').balloon({
                html: true,
                contents: deviceAttrList
              });
              var imgUrl = stfUrlArray[0] + "//" + stfUrlArray[2] + "/static/app/devices/icon/x120/" + (device.image == "" ?  "_default.jpg" : device.image);
              var img = Q('<img />').addClass('device-list-item-image').attr('src', imgUrl);
              var deviceName = device.name == "" ? "(No Name)" : device.name;
              var pName = Q('<p />').addClass('device-list-item-name').text(deviceName);
              var pStatus = Q('<p />');
              if (device.present) {
                if (device.owner == null) {
                  pStatus.text('Ready').addClass('device-list-item-status ready');
                } else {
                  img.addClass('device-is-busy');
                  pName.addClass('device-is-busy');
                  pStatus.text('Using').addClass('device-list-item-status using');
                }
              } else {
                img.addClass('device-is-busy');
                pName.addClass('device-is-busy');
                pStatus.text('Disconnected').addClass('device-list-item-status disconnected');
              }
              Q('#deviceList').append(div.append(img).append(pName).append(pStatus));
            });
        });
    });
}
