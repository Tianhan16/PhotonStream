// ======== server/PhotonStreamServer.java =========
package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import protocol.InitResponse;
import protocol.PDUType;
import shared.DFAValidator;

public class PhotonStreamServer {
	public static void main(String[] args) throws IOException {
		int port = 9090;
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("PhotonStream Server listening on port " + port);

		while (true) {
			Socket client = serverSocket.accept();
			System.out.println("Client connected: " + client.getInetAddress());
			new Thread(() -> handleClient(client)).start();
		}
	}

	private static void handleClient(Socket client) {
		try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream()) {
			DFAValidator dfa = new DFAValidator();

			byte msgType = (byte) in.read();
			if (!dfa.onMessage(msgType) || msgType != PDUType.INIT_REQUEST) {
				out.write(PDUType.ERROR);
				return;
			}

			InitResponse resp = new InitResponse();
			resp.msgType = PDUType.INIT_RESPONSE;
			resp.protocolVersion = 1;
			resp.statusCode = 0x00;
			resp.numTracks = 2;
			resp.durationMs = 100000;
			resp.segmentDurationMs = 2000;
			resp.bitrateKbps = 4000;
			resp.sessionId = 56789;

			out.write(new byte[] { resp.msgType, resp.protocolVersion, resp.statusCode, resp.numTracks });
			DataOutputStream dout = new DataOutputStream(out);
			dout.writeInt(resp.durationMs);
			dout.writeInt(resp.segmentDurationMs);
			dout.writeInt(resp.bitrateKbps);
			dout.writeInt(resp.sessionId);

			dfa.onMessage(resp.msgType);

			byte followup = (byte) in.read();
			if (followup == PDUType.END_SESSION && dfa.onMessage(followup)) {
				System.out.println("Session ended cleanly.");
			} else {
				System.out.println("Unexpected message or state.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
