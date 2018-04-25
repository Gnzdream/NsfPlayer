package zdream.nsfplayer.vcm;

public class ValueConv {
	
	/**
	 * 将内部值转换为公共值. 如果转换失败, 则返回 false
	 */
	public boolean getExportValue(ValueCtrl vt, Configuration cfg, String id, Value src_value, Value result) {
		result = src_value;
		return true;
	}
	
	/**
	 * 将公共值转换为内部值. 如果转换失败, 则返回 false
	 */
	public boolean getImportValue(ValueCtrl vt, Configuration cfg, String id, final Value src_value, Value result) {
		result = src_value;
		return true;
	}
}
