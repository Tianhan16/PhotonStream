// ======== shared/DFAValidator.java =========
package shared;

public class DFAValidator {
	private State state = State.IDLE;

	public boolean onMessage(byte msgType) {
		switch (state) {
		case IDLE:
			if (msgType == 0x01) {
				state = State.SESSION_INIT;
				return true;
			}
			break;
		case SESSION_INIT:
			if (msgType == 0x02) {
				state = State.STREAMING;
				return true;
			}
			break;
		case STREAMING:
			if (msgType == 0x08) {
				state = State.SESSION_END;
				return true;
			}
			break;
		}
		return false;
	}

	public State getState() {
		return state;
	}
}
