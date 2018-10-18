package zdream.nsfplayer.vcm;

/**
 * 值. in vcm.h
 * @author Zdream
 */
public class Value {
	public String data; // 任何类型均可以保存
	public boolean update; // 如果更新则为真

	public Value(int i) {
		this.data = Integer.toString(i);
	}

	public Value(String o) {
		this.data = o;
	}

	public int toInt() {
		return Integer.parseInt(data.toString());
	}
	
	public String toString() {
		return data.toString();
	}
	
	public void set(String o) {
		this.data = o;
		this.update = true;
	}
	
	public void set(int i) {
		this.data = Integer.toString(i);
		this.update = true;
	}
}
