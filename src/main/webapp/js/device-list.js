Q(document).ready(function(){
  updateDeviceList();

  Q('.stf-filter').each(function(){
    Q(this).blur(updateDeviceList);
  });
});

function updateDeviceList(evt) {
    var filter = {};
    Q('.stf-filter').each(function(){
      var key = this.name.split(".")[2];
      var value = this.value == "" ? "any" : this.value;
      filter[key] = value;
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

                  if (k == 'owner' && v != null) {
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
              var divName = Q('<div />').addClass('device-list-item-name').text(deviceName);
              var divStatus = Q('<div />');
              if (device.present) {
                if (device.owner == null) {
                  divStatus.text('Ready').addClass('device-list-item-status ready');
                } else {
                  img.addClass('device-is-busy');
                  divName.addClass('device-is-busy');
                  divStatus.text('Using').addClass('device-list-item-status using');
                }
              } else {
                img.addClass('device-is-busy');
                divName.addClass('device-is-busy');
                divStatus.text('Disconnected').addClass('device-list-item-status disconnected');
              }
              Q('#deviceList').append(div.append(img).append(divName).append(divStatus));
            });
        });
    });
}
