package zdream.nsfplayer.xgm.device;

public final class IntHolder {
	public int val;
	public IntHolder() { }
	public IntHolder(int val) {
		super();
		this.val = val;
	}
	@Override
	public String toString() {
		return Integer.toString(val);
	}
}
