package zdream.nsfplayer.xgm.device.sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;

/**
 * 这个类从 {@link NesDMC} 中分离出来, 主要侦测 0x4017 位置的内存
 * @author Zdream
 *
 */
public class FrameSequenceCounter implements IDevice {
	
	/**
	 * current cycle count
	 */
	int frame_sequence_count;
	/**
	 * CPU cycles per FrameSequence
	 */
	int frame_sequence_length;
	/**
	 * current step of frame sequence
	 */
	int frame_sequence_step;
	/**
	 * 4/5 steps per frame
	 */
	int frame_sequence_steps;
	
	boolean frame_irq;
	boolean frame_irq_enable;
	
	/**
	 * NTSC - 0, PAL - 1
	 */
	int pal;
	
	/**
	 * 绑定到这个类的设备
	 */
	List<IFrameSequencer> ss;

	public FrameSequenceCounter() {
		frame_sequence_length = 7458;
		frame_sequence_steps = 4;
		setPal(false);
		
		ss = new ArrayList<>();
	}
	
	/**
	 * @param clocks
	 *   unsigned
	 */
	public final void tickFrameSequence(int clocks) {
		frame_sequence_count += clocks;
		while (frame_sequence_count > frame_sequence_length) {
			frameSequenceNotify();
			if (frame_sequence_step == 0 && frame_sequence_steps == 4) {
				frame_irq = true;
			}
			
			frame_sequence_count -= frame_sequence_length;
			++frame_sequence_step;
			if (frame_sequence_step >= frame_sequence_steps)
				frame_sequence_step = 0;
		}
	}
	
	public final void setPal(boolean is_pal) {
		pal = (is_pal ? 1 : 0);
		// set CPU cycles in frame_sequence
		frame_sequence_length = is_pal ? 8314 : 7458;
	}

	@Override
	public void reset() {
		frame_sequence_count = 0;
		frame_sequence_steps = 4;
		frame_sequence_step = 0;
		
		frame_irq = false;
		frame_irq_enable = false;
	}
	
	public void addSequencer(IFrameSequencer s) {
		ss.add(s);
	}
	
	public void clear() {
		ss.clear();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0x4017) {
			// DEBUG_OUT("4017 = %02X\n", val);
			frame_irq_enable = ((val & 0x40) == 0x40);
			frame_irq = (frame_irq_enable ? frame_irq : false);
			frame_sequence_count = 0;
			if ((val & 0x80) != 0) {
				frame_sequence_steps = 5;
				frame_sequence_step = 0;
				
				frameSequenceNotify();
				
				++frame_sequence_step;
			} else {
				frame_sequence_steps = 4;
				frame_sequence_step = 1;
			}
		}
		return false;
	}
	
	private void frameSequenceNotify() {
		for (Iterator<IFrameSequencer> it = ss.iterator(); it.hasNext();) {
			IFrameSequencer s = it.next();
			s.frameSequence(frame_sequence_step);
		}
		if (frame_sequence_step == 0 && frame_sequence_steps == 4) {
			frame_irq = true;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		return false;
	}

}
