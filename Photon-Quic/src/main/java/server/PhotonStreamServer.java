// ======== server/PhotonStreamServer.java =========
package server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import protocol.InitRequest;
import protocol.InitResponse;
import protocol.PDUType;
import shared.DFAValidator;
import shared.State;
import tech.kwik.core.QuicConnection;
import tech.kwik.core.QuicStream;
import tech.kwik.core.server.ApplicationProtocolConnection;
import tech.kwik.core.server.ApplicationProtocolConnectionFactory;
import tech.kwik.core.server.ServerConnectionConfig;
import tech.kwik.core.server.ServerConnector;

public class PhotonStreamServer {
	public static void main(String[] args) throws Exception {
		int port = 9090;

		List<QuicConnection.QuicVersion> versions = new ArrayList<>();
		versions.add(QuicConnection.QuicVersion.V1);
		versions.add(QuicConnection.QuicVersion.V2);

		ServerConnectionConfig config = ServerConnectionConfig.builder().maxIdleTimeoutInSeconds(30)
				.maxUnidirectionalStreamBufferSize(1_000_000).maxBidirectionalStreamBufferSize(1_000_000)
				.maxConnectionBufferSize(10_000_000).maxOpenPeerInitiatedUnidirectionalStreams(10)
				.maxOpenPeerInitiatedBidirectionalStreams(100).retryRequired(false).connectionIdLength(8).build();

		ServerConnector connector = ServerConnector.builder().withPort(port).withSupportedVersions(versions)
				.withConfiguration(config)
				.withCertificate(new FileInputStream("server-cert.pem"), new FileInputStream("server-key.pem"))
				.withLogger(new tech.kwik.core.log.SysOutLogger()).build();

		connector.registerApplicationProtocol("PhotonStream", new PhotonProtocolFactory());

		connector.start();
		System.out.println("PhotonStream QUIC Server started on port " + port);
	}
}

class PhotonProtocolFactory implements ApplicationProtocolConnectionFactory {
	@Override
	public ApplicationProtocolConnection createConnection(String protocol, QuicConnection conn) {
		return new PhotonConnectionHandler(conn);
	}
}

class PhotonConnectionHandler implements ApplicationProtocolConnection {
	private final QuicConnection conn;

	public PhotonConnectionHandler(QuicConnection conn) {
		this.conn = conn;
	}

	@Override
	public void acceptPeerInitiatedStream(QuicStream stream) {
		new Thread(() -> {
			try (InputStream in = stream.getInputStream(); OutputStream out = stream.getOutputStream()) {
				DFAValidator dfa = new DFAValidator();

				InitRequest req = InitRequest.readFrom(in);
				System.out.println("Received INIT_REQUEST with videoId: " + req.videoId);

				if (!dfa.onMessage(req.msgType)) {
					System.out.println("Unexpected or invalid INIT_REQUEST message.");
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
				System.out.println("Received followup message: 0x" + String.format("%02X", followup));
				System.out.println("Current DFA state before followup: " + dfa.getState());

				if (!dfa.onMessage(followup)) {
					System.out.println("DFA rejected message type 0x" + String.format("%02X", followup));
				}

				if (followup == PDUType.END_SESSION && dfa.getState() == State.SESSION_END) {
					System.out.println("Session ended cleanly.");
				} else {
					System.out.println("Unexpected message or state.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

}
