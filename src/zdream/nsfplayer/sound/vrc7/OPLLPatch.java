package zdream.nsfplayer.sound.vrc7;

public class OPLLPatch {
	/** unsigned */
	int TL, FB, EG, ML, AR, DR, SL, RR, KR, KL, AM, PM, WF;
	
	public void copyFrom(OPLLPatch o) {
		this.TL = o.TL;
		this.FB = o.FB;
		this.EG = o.EG;
		this.ML = o.ML;
		this.AR = o.AR;
		this.DR = o.DR;
		this.SL = o.SL;
		this.RR = o.RR;
		this.KR = o.KR;
		this.KL = o.KL;
		this.AM = o.AM;
		this.PM = o.PM;
		this.WF = o.WF;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("TL:").append(TL).append(',');
		b.append("FB:").append(FB).append(',');
		b.append("EG:").append(EG).append(',');
		b.append("ML:").append(ML).append(',');
		b.append("AR:").append(AR).append(',');
		b.append("DR:").append(DR).append(',');
		b.append("SL:").append(SL).append(',');
		b.append("RR:").append(RR).append(',');
		b.append("KR:").append(KR).append(',');
		b.append("KL:").append(KL).append(',');
		b.append("AM:").append(AM).append(',');
		b.append("PM:").append(PM).append(',');
		b.append("WF:").append(WF);
		return b.toString();
	}
}
