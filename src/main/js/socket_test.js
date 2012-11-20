load('vertx.js')

function ResponseHandler(buffer, client, recipients) {
  this.buffer = buffer;
  this.client = client;
  this.recipients = recipients;
}

ResponseHandler.prototype.handleResponse = function() {
  var message = this.buffer.toString();
  var pattern=new RegExp("policy-file-request")
  if(pattern.test(message)) {
    var policy = new vertx.Buffer("<?xml version='1.0'?><cross-domain-policy><allow-access-from domain='*' to-ports='*'/></cross-domain-policy>\0")
    vertx.eventBus.send(this.client.writeHandlerID, policy);
  } else {
    var aconns = this.recipients.toArray();
    for (var i = 0; i < aconns.length; i++) {
      if(aconns[i] != this.client.writeHandlerID) {
        vertx.eventBus.send(aconns[i], this.buffer);
      }
    }
  }
}

var conns = vertx.getSet('conns')
var server = vertx.createNetServer().connectHandler(function(socket) {
  conns.add(socket.writeHandlerID)

  socket.dataHandler(function(data) {
    var responseHandler = new ResponseHandler(data, socket, conns)
    responseHandler.handleResponse()
  });
  socket.closedHandler(function() { conns.remove(socket.writeHandlerID) });

}).listen(10005)

