// ======== protocol/InitResponse.java =========
package protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InitResponse {
	public byte msgType;
	public byte protocolVersion;
	public byte statusCode;
	public byte numTracks;
	public int durationMs;
	public int segmentDurationMs;
	public int bitrateKbps;
	public int sessionId;

	public static InitResponse readFrom(InputStream is) throws IOException {
		DataInputStream in = new DataInputStream(is);
		InitResponse resp = new InitResponse();
		resp.msgType = in.readByte();
		resp.protocolVersion = in.readByte();
		resp.statusCode = in.readByte();
		resp.numTracks = in.readByte();
		resp.durationMs = in.readInt();
		resp.segmentDurationMs = in.readInt();
		resp.bitrateKbps = in.readInt();
		resp.sessionId = in.readInt();
		return resp;
	}
}
