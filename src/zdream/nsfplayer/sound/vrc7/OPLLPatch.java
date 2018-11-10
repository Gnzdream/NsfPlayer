package zdream.nsfplayer.sound.vrc7;

import zdream.nsfplayer.core.IResetable;

public class OPLLPatch implements IResetable {
	
	public boolean AM, PM, EG, KR;
	
	/** unsigned */
	public int TL, FB, ML, AR, DR, SL, RR, KL, WF;
	
	public void copyFrom(OPLLPatch o) {
		this.AM = o.AM;
		this.PM = o.PM;
		this.EG = o.EG;
		this.KR = o.KR;
		
		this.TL = o.TL;
		this.FB = o.FB;
		this.ML = o.ML;
		this.AR = o.AR;
		this.DR = o.DR;
		this.SL = o.SL;
		this.RR = o.RR;
		this.KL = o.KL;
		this.WF = o.WF;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("AM:").append(AM ? 1 : 0).append(',');
		b.append("PM:").append(PM ? 1 : 0).append(',');
		b.append("EG:").append(EG ? 1 : 0).append(',');
		b.append("KR:").append(KR ? 1 : 0).append(',');
		
		b.append("TL:").append(TL).append(',');
		b.append("FB:").append(FB).append(',');
		b.append("ML:").append(ML).append(',');
		b.append("AR:").append(AR).append(',');
		b.append("DR:").append(DR).append(',');
		b.append("SL:").append(SL).append(',');
		b.append("RR:").append(RR).append(',');
		b.append("KL:").append(KL).append(',');
		b.append("WF:").append(WF);
		return b.toString();
	}

	@Override
	public void reset() {
		this.AM = false;
		this.PM = false;
		this.EG = false;
		this.KR = false;
		
		this.TL = 0;
		this.FB = 0;
		this.ML = 0;
		this.AR = 0;
		this.DR = 0;
		this.SL = 0;
		this.RR = 0;
		this.KL = 0;
		this.WF = 0;
	}
}
