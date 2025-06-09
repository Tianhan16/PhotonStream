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
		if (args.length < 2) {
			System.out.println("Usage: java PhotonStreamClient <host> <port>");
			return;
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		URI uri = new URI("quic://" + host + ":" + port);

		QuicClientConnection conn = QuicClientConnection.newBuilder().uri(uri).applicationProtocol("PhotonStream")
				.noServerCertificateCheck().build();

		conn.connect();
		QuicStream stream = conn.createStream(true);

		OutputStream out = stream.getOutputStream();
		InputStream in = stream.getInputStream();

		InitRequest request = new InitRequest();
		request.writeTo(out);

		InitResponse response = InitResponse.readFrom(in);
		System.out.println("Session ID: " + response.sessionId);

		out.write(PDUType.END_SESSION);
		out.flush();
		Thread.sleep(200); // Give the server time to process END_SESSION
		System.out.println("Sent END_SESSION");

		conn.close();
	}
}
