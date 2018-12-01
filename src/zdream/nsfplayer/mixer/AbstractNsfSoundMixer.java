package zdream.nsfplayer.mixer;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;

import java.util.ArrayList;

/**
 * <p>抽象的 NSF 混音器.
 * <p>本层用于管理轨道标识号, 而轨道生成以及其它工作由子类完成
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public abstract class AbstractNsfSoundMixer<T extends IMixerChannel> implements ISoundMixer {
	
	protected final ArrayList<ChannelAttr> attrs = new ArrayList<>();

	/**
	 * AbstractNsfSoundMixer 对于每个轨道的参数. 子类根据需要继承.
	 * @author Zdream
	 */
	protected class ChannelAttr {
		public ChannelAttr(byte code, T t) {
			this.channel = t;
			this.code = code;
		}
		/**
		 * 轨道实例
		 */
		public final T channel;
		/**
		 * 输入采样数
		 */
		public int inSample;
		/**
		 * 轨道类型, 或者轨道号
		 */
		public final byte code;
	}
	
	/**
	 * 根据轨道类型, 创建轨道和参数
	 * @param code
	 *   轨道类型
	 * @return
	 */
	protected abstract ChannelAttr createChannelAttr(byte code);
	
	/**
	 * 在 [0, attrs.length) 查询下一个使用的 id (即 attrs 元素为 null 的情况).
	 * @return
	 */
	private int findNextId() {
		return attrs.indexOf(null);
	}
	
	@Override
	public int allocateChannel(byte code) {
		int nextId = findNextId();
		
		// 这里, code 全部转成轨道类型
		code = typeOfChannel(code);
		ChannelAttr attr = createChannelAttr(code);
		
		if (nextId != -1) {
			attrs.set(nextId, attr);
		} else {
			nextId = attrs.size();
			attrs.add(attr);
		}
		
		return nextId;
	}
	
	@Override
	public T getMixerChannel(int id) {
		if (id >= attrs.size()) {
			return null;
		}
		
		ChannelAttr attr = attrs.get(id);
		if (attr != null) {
			return attr.channel;
		}
		return null;
	}
	
	/*
	 * 该方法推荐继承实现, 覆盖方法最后一行写 super.detach(id)
	 */
	@Override
	public void detach(int id) {
		if (id >= attrs.size()) {
			return;
		}
		
		ChannelAttr attr = attrs.get(id);
		if (attr != null) {
			attrs.set(id, null);
		}
	}
	
	/*
	 * 该方法推荐继承实现, 覆盖方法最后一行写 super.detachAll()
	 */
	@Override
	public void detachAll() {
		attrs.clear();
	}
	
}
