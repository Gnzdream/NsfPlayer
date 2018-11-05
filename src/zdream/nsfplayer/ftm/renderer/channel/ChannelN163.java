package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.sound.SoundN163;

/**
 * N163 轨道
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class ChannelN163 extends ChannelTone {

	public ChannelN163(int num) {
		super((byte) (CHANNEL_N163_1 + (num - 1)));
	}

	@Override
	public void playNote() {
		super.playNote();
		
		// sequence
		updateSequence();
		
		// 发声器
		writeToSound();
		processSound();
	}

	@Override
	public void reset() {
		super.reset();
		seq.reset();
		sound.reset();
	}
	
	/* **********
	 * 乐器序列 *
	 ********** */
	/**
	 * 更新序列, 并将序列的数据回写到轨道上
	 */
	private void updateSequence() {
		// TODO
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	public final SoundN163 sound = new SoundN163();

	@Override
	public SoundN163 getSound() {
		return sound;
	}
	
	/**
	 * <p>将轨道中的数据写到发声器中.
	 * </p>
	 */
	public void writeToSound() {
		// TODO
	}
	
	/**
	 * 指导发声器工作一帧
	 */
	public void processSound() {
		// TODO
	}

}
