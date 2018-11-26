package zdream.nsfplayer.core;

import java.io.Serializable;

/**
 * <p>每个轨道的音量值设置参数类
 * <p>在该版本以前, 轨道音量设置一直携带在各个渲染器的配置中,
 * 现在它单独拆成一个独立的类.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.10
 */
public class ChannelLevelsParameter implements Cloneable, Serializable {
	private static final long serialVersionUID = -4294149990829332747L;
	
	public float level2A03Pules1 = 1.0f;
	public float level2A03Pules2 = 1.0f;
	public float level2A03Triangle = 1.0f;
	public float level2A03Noise = 1.0f;
	public float level2A03DPCM = 1.0f;
	
	public float levelVRC6Pules1 = 1.0f;
	public float levelVRC6Pules2 = 1.0f;
	public float levelVRC6Sawtooth = 1.0f;
	
	public float levelMMC5Pules1 = 1.0f;
	public float levelMMC5Pules2 = 1.0f;
	
	public float levelFDS = 1.0f;
	
	public float levelN163Namco1 = 1.0f;
	public float levelN163Namco2 = 1.0f;
	public float levelN163Namco3 = 1.0f;
	public float levelN163Namco4 = 1.0f;
	public float levelN163Namco5 = 1.0f;
	public float levelN163Namco6 = 1.0f;
	public float levelN163Namco7 = 1.0f;
	public float levelN163Namco8 = 1.0f;
	
	public float levelVRC7FM1 = 1.0f;
	public float levelVRC7FM2 = 1.0f;
	public float levelVRC7FM3 = 1.0f;
	public float levelVRC7FM4 = 1.0f;
	public float levelVRC7FM5 = 1.0f;
	public float levelVRC7FM6 = 1.0f;
	
	public float levelS5BSquare1 = 1.0f;
	public float levelS5BSquare2 = 1.0f;
	public float levelS5BSquare3 = 1.0f;
	
	public ChannelLevelsParameter clone() {
		try {
			return (ChannelLevelsParameter) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 从指定的实例中读取, 并重写自己的数据
	 */
	public void copyFrom(ChannelLevelsParameter p) {
		this.level2A03Pules1 = p.level2A03Pules1;
		this.level2A03Pules2 = p.level2A03Pules2;
		this.level2A03Triangle = p.level2A03Triangle;
		this.level2A03Noise = p.level2A03Noise;
		this.level2A03DPCM = p.level2A03DPCM;
		
		this.levelVRC6Pules1 = p.levelVRC6Pules1;
		this.levelVRC6Pules2 = p.levelVRC6Pules2;
		this.levelVRC6Sawtooth = p.levelVRC6Sawtooth;
		
		this.levelMMC5Pules1 = p.levelMMC5Pules1;
		this.levelMMC5Pules2 = p.levelMMC5Pules2;
		
		this.levelFDS = p.levelFDS;
		
		this.levelN163Namco1 = p.levelN163Namco1;
		this.levelN163Namco2 = p.levelN163Namco2;
		this.levelN163Namco3 = p.levelN163Namco3;
		this.levelN163Namco4 = p.levelN163Namco4;
		this.levelN163Namco5 = p.levelN163Namco5;
		this.levelN163Namco6 = p.levelN163Namco6;
		this.levelN163Namco7 = p.levelN163Namco7;
		this.levelN163Namco8 = p.levelN163Namco8;
		
		this.levelVRC7FM1 = p.levelVRC7FM1;
		this.levelVRC7FM2 = p.levelVRC7FM2;
		this.levelVRC7FM3 = p.levelVRC7FM3;
		this.levelVRC7FM4 = p.levelVRC7FM4;
		this.levelVRC7FM5 = p.levelVRC7FM5;
		this.levelVRC7FM6 = p.levelVRC7FM6;
		
		this.levelS5BSquare1 = p.levelS5BSquare1;
		this.levelS5BSquare2 = p.levelS5BSquare2;
		this.levelS5BSquare3 = p.levelS5BSquare3;
	}

}
