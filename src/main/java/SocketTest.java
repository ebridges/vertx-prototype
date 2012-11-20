
import static java.lang.String.format;

import java.util.Date;
import java.util.Set;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.deploy.Verticle;

@SuppressWarnings("UnusedDeclaration")
public class SocketTest extends Verticle {
  private static final String TAG = "SocketTest";
  private static final Integer PORT = 10005;


  @Override
  public void start() throws Exception {
    Logi(TAG, "start() called.");
    final Set<String> connections = vertx.sharedData().getSet("conns");
    Logi(TAG, "got %d connections", connections.size());

    vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
      private final String TAG = "ConnectHandler";

      public void handle(final NetSocket socket) {
        Logi(TAG, "storing reference to socket [%s]", socket.writeHandlerID);
        connections.add(socket.writeHandlerID);

        // attach a handler to take care of arriving data
        socket.dataHandler(new Handler<Buffer>() {
          public void handle(Buffer message) {
            if(!message.toString().trim().isEmpty()) {
              // details for dealing with the response are in the ResponseHandler class
              ResponseHandler handler = new ResponseHandler(message, socket, connections);
              handler.handleResponse();
            }
          }
        });

        // attache a handler to the socket for close events.
        socket.closedHandler(new SimpleHandler() {
          private static final String TAG = "DisconnectHandler";

          public void handle() {
            Logi(TAG, "handling close of socket [%s]", socket.writeHandlerID);
            connections.remove(socket.writeHandlerID);
          }
        });
      }
    }).listen(PORT);
  }

  class ResponseHandler {
    private final static String TAG = "ResponseHandler";
    private Buffer buffer;
    private NetSocket client;
    private Set<String> recipientList;

    private static final String CROSS_DOMAIN_POLICY = "<?xml version='1.0'?><cross-domain-policy><allow-access-from domain='*' to-ports='*'/></cross-domain-policy>\0";

    ResponseHandler(Buffer buffer, NetSocket client, Set<String> recipientList) {
      this.buffer = buffer;
      this.client = client;
      this.recipientList = recipientList;
    }
    
    public void handleResponse() {
      Logi(TAG, "handleResponse() called.");
      String message = buffer.toString().trim();
      if (message.contains("policy-file-request")) {
        policyFileResponse();
      } else {
        echoResponse();
      }
    }

    private void echoResponse() {
      Logi(TAG, "sending message to registered clients other than sender.");
      for (String connection : recipientList) {
        if (!connection.equals(client.writeHandlerID)) {
          Logi(TAG, "publishing message [%s] to connection [%s]", buffer.toString().trim(), connection);
          buffer.appendString("\0");
          vertx.eventBus().publish(connection, buffer);
        } else {
          Logi(TAG, "skipping publish to [%s] since it's the sender", connection);
        }
      }
    }

    private void policyFileResponse() {
      Logi(TAG, "sending cross domain policy in response to a policy file request.");
      Buffer response = new Buffer(CROSS_DOMAIN_POLICY.getBytes());
      vertx.eventBus().publish(client.writeHandlerID, response);
    }
  }

  private static final String FORMAT = "[%s] [%s] [%s] %s\n";

  private static void Logi(String tag, String mesg, Object ... args) {
    String date = new Date().toString();
    String appmessage;
    if(args != null && args.length > 0) {
      appmessage = format(mesg, args);
    } else {
      appmessage = mesg;
    }
    String logmessage = format(FORMAT, date, tag, "I", appmessage);
    System.out.print(logmessage);
  }
}
