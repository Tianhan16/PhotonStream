// ======== protocol/InitRequest.java =========
package protocol;

import java.io.*;

public class InitRequest {
	public byte msgType = PDUType.INIT_REQUEST;
	public byte protocolVersion = 1;
	public byte numTracks = 2;
	public byte reserved = 0;
	public int videoId = 12345;
	public int startTimestamp = 0;
	public byte trackFlags = 0x03; // video + audio
	public byte preferredCodec = 0x01; // H.264
	public byte qualityLevel = (byte) 200;
	public String authToken = "admin123";

	public static InitRequest readFrom(InputStream is) throws IOException {
		DataInputStream in = new DataInputStream(is);
		InitRequest req = new InitRequest();
		req.msgType = in.readByte();
		req.protocolVersion = in.readByte();
		req.numTracks = in.readByte();
		req.reserved = in.readByte();
		req.videoId = in.readInt();
		req.startTimestamp = in.readInt();
		req.trackFlags = in.readByte();
		req.preferredCodec = in.readByte();
		req.qualityLevel = in.readByte();
		int tokenLen = in.readByte();
		byte[] tokenBytes = new byte[tokenLen];
		in.readFully(tokenBytes);
		req.authToken = new String(tokenBytes);
		return req;
	}

	public void writeTo(OutputStream os) throws IOException {
		DataOutputStream out = new DataOutputStream(os);
		out.writeByte(msgType);
		out.writeByte(protocolVersion);
		out.writeByte(numTracks);
		out.writeByte(reserved);
		out.writeInt(videoId);
		out.writeInt(startTimestamp);
		out.writeByte(trackFlags);
		out.writeByte(preferredCodec);
		out.writeByte(qualityLevel);
		out.writeByte(authToken.length());
		out.writeBytes(authToken);
	}
}