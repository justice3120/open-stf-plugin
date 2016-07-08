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
        var endpointURL = t.responseJSON;
        desc.getDeviceListJSON(filter, function(t) {
            var devices = t.responseJSON;
            Q('#deviceList').html('');
            Q.each(devices, function(index, device){
              var $device = getDeviceListItem(expandDeviceImageURL(endpointURL, device));
              $device.balloon({
                html: true,
                contents: getDeviceBalloonContents(device)
              });
              Q('#deviceList').append($device);
            });
        });
    });
}

function expandDeviceImageURL(endpointURL, device) {
  var stfUrlArray = endpointURL.split("/");
  var schema = stfUrlArray[0];
  var host = stfUrlArray[2];
  device.image = schema + "//" + host + "/static/app/devices/icon/x120/" + (device.image == "" ?  "_default.jpg" : device.image);
  return device;
}

function getDeviceListItem(device) {
  var imageURL = device.image;
  var name = device.name == "" ? "(No Name)" : device.name;
  var busyClass = '';
  var statusClass;
  var statusText;

  //Detect device status from attribute
  if (device.present) {
    if (device.owner == null) {
      statusClass = 'ready';
      statusText = 'Ready';
    } else {
      busyClass = 'device-is-busy';
      statusClass = 'using';
      statusText = 'Using';
    }
  } else {
    busyClass = 'device-is-busy';
    statusClass = 'disconnected';
    statusText = 'Disconnected';
  }

  return Q(
      '<div class="device-list-item ' + busyClass + '">'
    +   '<img class="device-list-item-image ' + busyClass + '" src="' + imageURL + '">'
    +   '<div class="device-list-item-name">' + name + '</div>'
    +   '<div class="device-list-item-status ' + statusClass + '">' + statusText + '</div>'
    + '</div>'
  );
}

function getDeviceBalloonContents(device) {
  var deviceAttrList = Q('<table />').addClass('device-attr-table');
  Q.each(device, function(k, v){
    if (Q.inArray(k, ['image', 'remoteConnectUrl']) == -1) {
      var tdKey = Q('<td class="device-attr"/>').text(k);
      var tdValue = Q('<td class="device-attr"/>').text(v);

      if ((k == 'owner' || k == 'provider') && v != null) {
        tdValue.text(v.name);
      }

      Q('<tr />').append(tdKey).append(tdValue).appendTo(deviceAttrList);
    }
  });
  return deviceAttrList;
}
