// ======== client/PhotonStreamClient.java =========
package client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import protocol.InitRequest;
import protocol.InitResponse;
import protocol.PDUType;
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;

public class PhotonStreamClient {
	public static void main(String[] args) throws Exception {
		// Validate command line arguments
		if (args.length < 2) {
			System.out.println("Usage: java PhotonStreamClient <host> <port>");
			return;
		}

		// Construct server URI from arguments
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		URI uri = new URI("quic://" + host + ":" + port);

		// Establish QUIC connection to the PhotonStream server
		QuicClientConnection conn = QuicClientConnection.newBuilder().uri(uri).applicationProtocol("PhotonStream")
				.noServerCertificateCheck() // Disable TLS cert validation (development use only)
				.build();

		conn.connect();

		// Open a bidirectional stream to communicate with the server
		QuicStream stream = conn.createStream(true);
		OutputStream out = stream.getOutputStream();
		InputStream in = stream.getInputStream();

		// Build and send the INIT_REQUEST with streaming parameters
		InitRequest request = new InitRequest();
		request.writeTo(out);

		// Receive and parse the INIT_RESPONSE from the server
		InitResponse response = InitResponse.readFrom(in);
		System.out.println("Session ID: " + response.sessionId);

		// Send END_SESSION to gracefully terminate the stream session
		out.write(PDUType.END_SESSION);
		out.flush(); // Ensure all data is sent before closing
		Thread.sleep(200); // Delay to give the server time to process END_SESSION
		System.out.println("Sent END_SESSION");

		// Close the QUIC connection
		conn.close();
	}
}
