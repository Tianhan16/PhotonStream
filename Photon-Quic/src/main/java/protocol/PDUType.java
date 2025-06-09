// ======== protocol/PDUType.java =========
package protocol;

public class PDUType {
	public static final byte INIT_REQUEST = 0x01;
	public static final byte INIT_RESPONSE = 0x02;
	public static final byte END_SESSION = 0x08;
	public static final byte ERROR = 0x09;
}
