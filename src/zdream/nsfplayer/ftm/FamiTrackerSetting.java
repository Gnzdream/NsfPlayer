package zdream.nsfplayer.ftm;

/**
 * TODO
 * @author Zdream
 */
public class FamiTrackerSetting {

	public FamiTrackerSetting() {
		// TODO Auto-generated constructor stub
	}
	
	public class General {
		public boolean bNoDPCMReset = false;
	}
	
	public class Sound {
		public int iDevice = 0;
		public int iSampleRate = 48000;
		public int iSampleSize = 16;
		public int iBufferLength = 40;
		public int iBassFilter = 30;
		public int iTrebleFilter = 12000;
		public int iTrebleDamping = 24;
		public int iMixVolume = 100;
	}
	
	/**
	 * 默认全是 0
	 */
	public class ChipLevels{
		public int iLevelAPU1;
		public int iLevelAPU2;
		public int iLevelVRC6;
		public int iLevelVRC7;
		public int iLevelMMC5;
		public int iLevelFDS;
		public int iLevelN163;
		public int iLevelS5B;
	}
	
	public General general = new General();
	public Sound sound = new Sound();
	public ChipLevels chipLevels = new ChipLevels();

}
