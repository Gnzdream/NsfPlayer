package zdream.nsfplayer.vcm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Configuration extends Observable {
	
	protected Map<String, Value> data = new HashMap<String, Value>();
	
	/**
	 * 读取值 如果没有, 返回 null.
	 * @param id
	 * @return
	 */
	public synchronized Value get(String id) {
		return data.get(id);
    }
	
	/**
	 * 创建值. 如果 id 已经存在则失败, 不做任何操作.
	 * @param id
	 *   键
	 * @return
	 *   如果 id 已经存在, 返回 false
	 */
	public synchronized boolean createValue(final String id, final Value value) {
		if (data.containsKey(id)) {
			return false;
		} else {
			data.put(id, value);
			return true;
		}
    }
	
	/**
	 * 创建值. 如果 id 已经存在则失败, 不做任何操作.
	 * @param id
	 *   键
	 * @return
	 *   如果 id 已经存在, 返回 false
	 */
	public boolean createValue(final String id, final int value) {
		return createValue(id, new Value(value));
	}
	
	/**
	 * 创建值. 如果 id 已经存在则失败, 不做任何操作.
	 * @param id
	 *   键
	 * @return
	 *   如果 id 已经存在, 返回 false
	 */
	public boolean createValue(final String id, final String value) {
		return createValue(id, new Value(value));
	}
	
	/**
	 * 设置值
	 */
	public synchronized void setValue(final String id, final Value value) {
		data.put(id, value);
	}

	/**
	 * 获取值
	 */
	public synchronized int getIntValue(final String id) {
		return data.get(id).toInt();
	}
    
	/**
	 * 清空值
	 */
	public synchronized void clear() {
		data.clear();
	}
	
	public synchronized void read(Configuration src) {
		Iterator<Entry<String, Value>> it = src.data.entrySet().iterator();
		
		for (; it.hasNext();) {
			Entry<String, Value> e = it.next();
			data.put(e.getKey(), e.getValue());
		}
    }
    
	public synchronized void write(Configuration src) {
		for (Iterator<Entry<String, Value>> it = data.entrySet().iterator(); it.hasNext();) {
			Entry<String, Value> e = it.next();
			src.data.put(e.getKey(), e.getValue());
		}
    }

}
