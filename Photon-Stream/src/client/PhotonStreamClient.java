// ======== client/PhotonStreamClient.java =========
package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import protocol.InitRequest;
import protocol.InitResponse;
import protocol.PDUType;

public class PhotonStreamClient {
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: java PhotonStreamClient <host> <port>");
			return;
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);

		try (Socket socket = new Socket(host, port)) {
			System.out.println("Connected to server.");
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();

			InitRequest request = new InitRequest();
			request.writeTo(out);

			InitResponse response = InitResponse.readFrom(in);
			System.out.println("Session ID: " + response.sessionId);

			out.write(PDUType.END_SESSION);
			System.out.println("Sent END_SESSION");
		}
	}
}
