package zdream.nsfplayer.sound.vrc7;

public class OPLLPatch {
	/** unsigned */
	int TL, FB, EG, ML, AR, DR, SL, RR, KR, KL, AM, PM, WF;
	
	public OPLLPatch clone() {
		OPLLPatch o = new OPLLPatch();
		o.TL = TL;
		o.FB = FB;
		o.EG = EG;
		o.ML = ML;
		o.AR = AR;
		o.DR = DR;
		o.SL = SL;
		o.RR = RR;
		o.KR = KR;
		o.KL = KL;
		o.AM = AM;
		o.PM = PM;
		o.WF = WF;
		return o;
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
