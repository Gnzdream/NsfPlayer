package com.zdream.nsfplayer.xgm.player.nsf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.zdream.nsfplayer.xgm.player.SoundDataMSP;

/**
 * NSF 文件内的数据
 * @author Zdream
 */
public class NsfAudio extends SoundDataMSP {
	
	/*
	 * NSF 文件里
	 * 地址 0x000000 至 0x00007F 为帧头数据
	 * 
	 * 注意数据低位在前, 高位在后
	 */
	public byte[] magic = new byte[4]; // cpp 文件是 5 因为最后一位是 \0
	
	/**
	 * 当前 NSF 文件的版本号<br>
	 * 地址 0x000005, 单字节
	 */
	public int version;
	/**
	 * NSF 中乐曲数<br>
	 * 地址 0x000006, 单字节
	 */
	public int songs;
	public int total_songs;
	/**
	 * 起始乐曲播放的号码<br>
	 * 地址 0x000007, 单字节
	 */
	public int start;
	
	/**
	 * 数据载入的内存地址, 范围 ($8000-$FFFF)<br>
	 * 地址 0x000008-0x000009, 双字节<br><br>
	 * 
	 * 这里说明了在游戏机 RAM 中的地址. 如果游戏放到内存中运行, 则 NSF 将放到内存中.
	 * 除去文件头 (地址 0x000000 至 0x00007F), 其它数据将放到 lenA 对应的地址中
	 */
	public int load_address;
	/**
	 * 初始化数据开始的地址, 范围($8000-$FFFF)<br>
	 * 地址 0x00000A-0x00000B, 双字节
	 */
	public int init_address;
	/**
	 * 乐曲播放地址, 范围($8000-$FFFF)<br>
	 * 地址 0x00000C-0x00000D, 双字节
	 */
	public int play_address;
	
	public String filename;
	
	public byte[] title_nsf;
	public byte[] artist_nsf;
	public byte[] copyright_nsf;
	
	public String title;
	public String artist;
	public String copyright;
	
	/**
	 * NSFe only
	 */
	public String ripper;
	/**
	 * NSFe only
	 */
	public String text;
	
	/*
	 * 游戏或乐曲的标题、曲作者或艺术家名称、版权部分,附加说明等略过
	 */
	/**
	 * NTSC 制式下乐曲循环播放速度, 常为 [16666]
	 */
	public int speed_ntsc;
	/**
	 * Bank 切换, 初始 8 bit 值<br>
	 * 
	 * 6502 汇编的寻址空间为 64K, 但是 NES 却只用 $8000-$FFFF, 共 32K, 对于像超级玛莉 1 这样的小游戏, 不用
	 * 考虑存储体 (Bank) 切换, 但是对于像魂斗罗 1, 2 代这样的游戏, 超过 32K, 就要进行存储体 (Bank) 切换,
	 * 大小可能不太一样, 有的是 16K, 有的是 32K, 有的是 8K 等等.
	 * 地址也不一样, $8000, $A000, $C000 都有可能.
	 * NSF 也会遇到空间不够的情况, 这时就要用到存储体 (Bank) 切换. NSF 存储体 (Bank) 切换大小为4K.
	 */
	public int bankswitch[] = new int[8];
	/**
	 * NTSC 制式下乐曲循环播放速度
	 */
	public int speed_pal;
	/**
	 * PAL/NTSC 制式选择<br>
	 * 
	 * 位开关, 数据从左（高）到右（低），前 6 位强制为 0
	 * 第 7 位如果为 1, NTSC/PAL, 此时第 8 位必须为 0; (= 2)
	 * 否则第 7 位为 0, 第 8 位为0, 为 NTSC 制式; 为1, 为 PAL 制式
	 */
	public int pal_ntsc;
	/**
	 * <b>特殊声音芯片</b><br>
	 * 
	 * 位开关, 数据从左（高）到右（低），前 2 位强制为 0<br>
	 * 第 3 位如果为 1, 使用 Sunsoft (FME7) 芯片;<br>
	 * 第 4 位如果为 1, 使用 Namcot (106) 芯片;<br>
	 * 第 5 位如果为 1, 使用 Nintendo (MMC5) 芯片;<br>
	 * 第 6 位如果为 1, 使用 Nintendo (FDS) 芯片;<br>
	 * 第 7 位如果为 1, 使用 Konami (VRC7) 芯片;<br>
	 * 第 8 位如果为 1, 使用 Konami (VRC6) 芯片;<br>
	 * 
	 * 如果 f2 == 0, 什么芯片也不用
	 */
	public int soundchip;
	/**
	 * @see #soundchip
	 */
	public boolean useVrc6, useVrc7, useFds, useMmc5, useN106, useFme7;
	
	public byte[] extra = new byte[4];
	public byte[] body;
	public byte[] nsfe_image;
	public byte[] nsfe_plst;
	public NsfeEntry[] nsfe_entry = new NsfeEntry[256];

	{
		for (int i = 0; i < 256; i++) {
			nsfe_entry[i] = new NsfeEntry();
		}
	}
	
	/*
	 * 下面是播放相关数据
	 */
	
	/** 现在正在选择的歌曲号，从 0 开始 */
	public int song;
	/** 从游戏名单看的数据 true */
	public boolean playlist_mode;
	/** 演唱的时间 | 秒 */
	public int time_in_ms;
	/** 默认的播放时间 */
	public int default_playtime;
	/** 循环时间 */
	public int loop_in_ms;
	/** 渐出时间 */
	public int fade_in_ms, default_fadetime;
	/** 循环次数 */
	public int loop_num, default_loopnum;
	/** 演奏时间不明的时候启用（默认的演奏时间） */
	public boolean playtime_unknown;
	public boolean title_unknown;
	
	public String print_title;
	
	/**
	 * 这个在原来的 C++ 文件中是没有的. 它缓存了上次打开的 PLSItem.<br>
	 * 因为原来 C++ 文件在运行过程中即使用同一个文件, 进行上下曲切歌, 仍然需要重新读一遍文件.
	 * 这样的 IO 操作实际上是无意义的. 因此增加了这个缓存变量.<br>
	 * 如果想利用它的话, 请使用 <code>reload()</code> 方法.<br>
	 * 当然, 如果打开了另一个不同的文件, 这个自然就无效了.
	 * 系统仍然会去进行 IO 操作读文件.
	 */
	PLSItem last_item;
	
	public void setDefaults(int p, int f, int l) {
		default_playtime = p;
		default_fadetime = f;
		default_loopnum = l;
	}
	
	public boolean loadFile(final String fn) throws IOException {
		File f = new File(fn);
		FileInputStream r = new FileInputStream(f);
		byte[] bs = new byte[(int) f.length()];
		r.read(bs);
		r.close();
		
		return loadBytes(fn, bs);
	}

	public boolean loadAssets(final String str) throws IOException {
		InputStream in = getClass().getResourceAsStream(str);
		byte[] bs, buf = new byte[8192];
		List<byte[]> list = new ArrayList<byte[]>();
		int lastLen = 0, len = 0, i;
		
		while ((i = in.read(buf)) > 0) {
			lastLen = i;
			len += i;
			list.add(buf);
			buf = new byte[8192];
		}
		
		bs = new byte[len];
		int limit = list.size() - 1;
		for (i = 0; i < limit; i++) {
			System.arraycopy(list.get(i), 0, bs, 8192 * i, 8192);
		}
		System.arraycopy(list.get(limit), 0, bs, 8192 * limit, lastLen);
		
		return loadBytes(str, bs);
	}
	
	/**
	 * NSF 文件读取, 这里需要调用者先将其转化为 byte 数组.
	 * loads file (playlist or NSF or NSFe)
	 */
	public boolean loadBytes(final String str, final byte[] bs) throws IOException {
		A: {
			PLSItem pls = new PLSItem(str.toCharArray());

			if (pls.type == 3) {
				filename = pls.filename;
			} else if (str.endsWith(".nsf") || str.endsWith(".NSF") || str.endsWith(".nsfe") || str.endsWith(".NSFE")) {
				filename = pls.filename;
			} else {
				break A;
			}

			if (load(bs) == false) {
				break A;
			}

			if (pls.type == 3) {
				setTitle(pls.title);
				song = pls.song;
				playlist_mode = true;
				title_unknown = false;
				enable_multi_tracks = false;
			} else {
				playlist_mode = false;
				title_unknown = true;
				enable_multi_tracks = true;
			}

			time_in_ms = pls.time_in_ms;
			loop_in_ms = pls.loop_in_ms;
			fade_in_ms = pls.fade_in_ms;
			loop_num = pls.loop_num;

			if (time_in_ms < 0)
				playtime_unknown = true;
			else
				playtime_unknown = false;

			// 这步用于缓存文件
			this.last_item = pls;
			
			return true;
		}

		last_item = null;
		return false;
	}
	
	public void reload() {
		time_in_ms = this.last_item.time_in_ms;
		loop_in_ms = this.last_item.loop_in_ms;
		fade_in_ms = this.last_item.fade_in_ms;
		loop_num = this.last_item.loop_num;
	}
	
	@Override
	public void setLength(int t) {
		time_in_ms = t;
		playtime_unknown = false;
	}

	public int getPlayTime() {
		int s = song;
		if (nsfe_plst != null)
			s = nsfe_plst[song];
		if (nsfe_entry[s].time >= 0) {
			return nsfe_entry[s].time;
		}

		return time_in_ms < 0 ? default_playtime : time_in_ms;
	}

	public int getLoopTime() {
		return loop_in_ms < 0 ? 0 : loop_in_ms;
	}

	public int getFadeTime() {
		int s = song;
		if (nsfe_plst != null)
			s = nsfe_plst[song];
		if (nsfe_entry[s].fade >= 0) {
			return nsfe_entry[s].fade;
		}

		if (fade_in_ms < 0)
			return default_fadetime;
		else if (fade_in_ms == 0)
			return 50;
		else
			return fade_in_ms;
	}

	public int getLoopNum() {
		return loop_num > 0 ? loop_num - 1 : default_loopnum - 1;
	}

	@Override
	public int getLength() {
		return getPlayTime() + getLoopTime() * getLoopNum() + getFadeTime();
	}

	@Override
	public int getSong() {
		return song;
	}

	@Override
	public int getSongNum() {
		return songs;
	}

	public boolean useNSFePlaytime() {
		if (nsfe_plst == null)
			return false;
		return nsfe_entry[nsfe_plst[song]].time >= 0;
	}

	@Override
	public void setSong(int s) {
		song = s % songs;
	}

	public boolean load(byte[] image) {
		if (image.length < 4) // no FourCC
			return false;

		// fill NSFe values with defaults

		// 'plst'
		nsfe_plst = null;

		// entries 'tlbl', 'time', 'fade'
		for (int i = 0; i < 256; ++i) {
			nsfe_entry[i].tlbl = "";
			nsfe_entry[i].time = -1;
			nsfe_entry[i].fade = -1;
		}

		// load the NSF or NSFe
		System.arraycopy(image, 0, magic, 0, 4);
		if (magic[0] == 'M' && magic[1] == 'E' && magic[2] == 'S' && magic[3] == 'M') {
			return loadNSFe(image, false);
		}
		if (image.length < 0x80) { // no header?
			return false;
		}

		version = image[0x05] & 0xFF;
		total_songs = songs = image[0x06] & 0xFF;
		start = image[0x07] & 0xFF;

		load_address = (image[0x08] & 0xFF) | ((image[0x09] & 0xFF) << 8);
		init_address = (image[0x0a] & 0xFF) | ((image[0x0B] & 0xFF) << 8);
		play_address = (image[0x0c] & 0xFF) | ((image[0x0D] & 0xFF) << 8);

		int end = 0;
		
		title_nsf = new byte[32];
		System.arraycopy(image, 0x0e, title_nsf, 0, 32);
		for (end = 0; end < title_nsf.length; end++) {
			if (title_nsf[end] == 0)
				break;
		}
		title = new String(title_nsf, 0, end);

		artist_nsf = new byte[32];
		System.arraycopy(image, 0x2e, artist_nsf, 0, 32);
		for (end = 0; end < artist_nsf.length; end++) {
			if (artist_nsf[end] == 0)
				break;
		}
		artist = new String(artist_nsf, 0, end);

		copyright_nsf = new byte[32];
		System.arraycopy(image, 0x4e, copyright_nsf, 0, 32);
		for (end = 0; end < copyright_nsf.length; end++) {
			if (copyright_nsf[end] == 0)
				break;
		}
		copyright = new String(copyright_nsf, 0, end);

		ripper = ""; // NSFe only
		text = null; // NSFe only

		speed_ntsc = (image[0x6e] & 0xFF) | ((image[0x6f] & 0xFF) << 8);
		for (int i = 0; i < 8; i++) {
			bankswitch[i] = image[0x70 + i] & 0xFF;
		}
		speed_pal = (image[0x78] & 0xFF) | ((image[0x79] & 0xFF) << 8);
		pal_ntsc = image[0x7a] & 0xFF;
		if (speed_pal == 0)
			speed_pal = 19997;
		if (speed_ntsc == 0)
			speed_ntsc = 16639;
		soundchip = image[0x7b] & 0xFF;

		useVrc6 = (soundchip & 1) != 0 ? true : false;
		useVrc7 = (soundchip & 2) != 0 ? true : false;
		useFds = (soundchip & 4) != 0 ? true : false;
		useMmc5 = (soundchip & 8) != 0 ? true : false;
		useN106 = (soundchip & 16) != 0 ? true : false;
		useFme7 = (soundchip & 32) != 0 ? true : false;

		extra = new byte[4];
		System.arraycopy(image, 0x7c, extra, 0, 4);

		// 这里的 body 就是除去头 128 字节的数据
		body = new byte[image.length - 0x80];
		System.arraycopy(image, 0x80, body, 0, body.length);

		song = start - 1;
		return true;
	}

	public boolean loadNSFe(byte[] image, boolean info) {
		// store entire file for string references, etc.
		nsfe_image = new byte[image.length + 1];

		System.arraycopy(image, 0, nsfe_image, 0, image.length);
		nsfe_image[image.length] = 0; // null terminator for safety
		image = nsfe_image;

		if (image.length < 4) // no FourCC
			return false;

		System.arraycopy(image, 0, magic, 0, 4);
		if (magic[0] != 'M' || magic[1] != 'E' || magic[2] != 'F' || magic[3] != 'E') {
			return false;
		}

		int chunk_offset = 4; // skip 'NSFE'
		while (true) {
			if ((image.length - chunk_offset) < 8) // not enough data for chunk size + FourCC
				return false;

			// UINT8* chunk = image + chunk_offset;
			int chunkp = chunk_offset;

			int chunk_size = ((image[chunkp] & 0xFF)) + ((image[chunkp + 1] & 0xFF) << 8)
					+ ((image[chunkp + 2] & 0xFF) << 16) + ((image[chunkp + 3] & 0xFF) << 24);

			if ((image.length - chunk_offset) < (chunk_size + 8)) // not enough data for chunk
				return false;

			byte[] cid = new byte[4];
			cid[0] = image[chunkp + 4];
			cid[1] = image[chunkp + 5];
			cid[2] = image[chunkp + 6];
			cid[3] = image[chunkp + 7];

			chunk_offset += 8;
			chunkp += 8;

			if (cid[0] == 'N' && cid[1] == 'E' && cid[2] == 'N' && cid[3] == 'D') { // end of chunks
				break;
			}

			if (cid[0] == 'I' && cid[1] == 'N' && cid[2] == 'F' && cid[3] == 'O') {
				if (chunk_size < 0x0A)
					return false;

				version = 1;
				load_address = (image[chunkp + 0x00] & 0xFF) | ((image[chunkp + 0x01] & 0xFF) << 8);
				init_address = (image[chunkp + 0x02] & 0xFF) | ((image[chunkp + 0x03] & 0xFF) << 8);
				play_address = (image[chunkp + 0x04] & 0xFF) | ((image[chunkp + 0x05] & 0xFF) << 8);
				pal_ntsc = (image[chunkp + 0x06] & 0xFF);
				soundchip = (image[chunkp + 0x07] & 0xFF);
				songs = (image[chunkp + 0x08] & 0xFF);
				start = (image[chunkp + 0x09] & 0xFF) + 1; // note NSFe is 0 based, unlike NSF
				total_songs = songs;

				// NSFe doesn't allow custom speeds
				speed_ntsc = 16639; // 60.09Hz
				speed_pal = 19997; // 50.00Hz

				// other variables contained in other banks
				for (int i = 0; i < 8; i++) {
					bankswitch[i] = 0;
				}
				for (int i = 0; i < 4; i++) {
					extra[i] = 0;
				}

				// setup derived variables
				useVrc6 = (soundchip & 1) != 0 ? true : false;
				useVrc7 = (soundchip & 2) != 0 ? true : false;
				useFds = (soundchip & 4) != 0 ? true : false;
				useMmc5 = (soundchip & 8) != 0 ? true : false;
				useN106 = (soundchip & 16) != 0 ? true : false;
				useFme7 = (soundchip & 32) != 0 ? true : false;
				song = start - 1;

				// body should follow in 'DATA' chunk
				body = null;

				// description strings should follow in 'auth' chunk
				title_nsf[0] = 0;
				artist_nsf[0] = 0;
				copyright_nsf[0] = 0;
				title = new String(title_nsf);
				artist = new String(artist_nsf);
				copyright = new String(copyright_nsf);
				ripper = "";
				text = null;

				// INFO chunk read
				info = true;
			} else if (cid[0] == 'D' && cid[1] == 'A' && cid[2] == 'T' && cid[3] == 'A') // DATA
			{
				if (!info)
					return false;

				body = new byte[chunk_size];
				System.arraycopy(image, chunkp, body, 0, chunk_size);
			} else if (cid[0] == 'B' && cid[1] == 'A' && cid[2] == 'N' && cid[3] == 'K') // BANK
			{
				if (!info)
					return false;

				for (int i = 0; i < 8 && i < chunk_size; ++i) {
					bankswitch[i] = image[chunkp + i];
				}
			} else if (cid[0] == 'a' && cid[1] == 'u' && cid[2] == 't' && cid[3] == 'h') // auth 小写
			{
				/*
				 * #define NSFE_STRING(p) \ if (n >= chunk_size) break; \ p =
				 * reinterpret_cast<char*>(chunk+n); \ while (n < chunk_size && chunk[n] != 0)
				 * ++n; \ if(chunk[n] == 0) ++n;
				 */

				int n = 0;
				while (true) {
					// title
					if (n >= chunk_size)
						break;
					int begin = chunkp + n;
					while (n < chunk_size && image[chunkp + n] != 0)
						++n;
					byte[] bytes = new byte[n - begin];
					System.arraycopy(image, begin, bytes, 0, bytes.length);
					title = new String(bytes);
					if (image[chunkp + n] == 0)
						++n;

					// artist
					if (n >= chunk_size)
						break;
					begin = chunkp + n;
					while (n < chunk_size && image[chunkp + n] != 0)
						++n;
					bytes = new byte[n - begin];
					System.arraycopy(image, begin, bytes, 0, bytes.length);
					artist = new String(bytes);
					if (image[chunkp + n] == 0)
						++n;

					// copyright
					if (n >= chunk_size)
						break;
					begin = chunkp + n;
					while (n < chunk_size && image[chunkp + n] != 0)
						++n;
					bytes = new byte[n - begin];
					System.arraycopy(image, begin, bytes, 0, bytes.length);
					copyright = new String(bytes);
					if (image[chunkp + n] == 0)
						++n;

					// ripper
					if (n >= chunk_size)
						break;
					begin = chunkp + n;
					while (n < chunk_size && image[chunkp + n] != 0)
						++n;
					bytes = new byte[n - begin];
					System.arraycopy(image, begin, bytes, 0, bytes.length);
					ripper = new String(bytes);
					if (image[chunkp + n] == 0)
						++n;

					break;
				}
			} else if (cid[0] == 'p' && cid[1] == 'l' && cid[2] == 's' && cid[3] == 't') // plst 小写
			{
				nsfe_plst = new byte[chunk_size];
				System.arraycopy(image, chunkp, nsfe_plst, 0, chunk_size);
			} else if (cid[0] == 't' && cid[1] == 'i' && cid[2] == 'm' && cid[3] == 'e') // time 小写
			{
				int i = 0;
				int n = 0;
				while (i < 256 && (n + 3) < chunk_size) {
					int value = ((image[chunkp + n] & 0xFF)) + ((image[chunkp + n + 1] & 0xFF) << 8)
							+ ((image[chunkp + n + 2] & 0xFF) << 16) + ((image[chunkp + n + 3] & 0xFF) << 24);
					nsfe_entry[i].time = (int) value;
					++i;
					n += 4;
				}
			} else if (cid[0] == 'f' && cid[1] == 'a' && cid[2] == 'd' && cid[3] == 'e') // fade 小写
			{
				int i = 0;
				int n = 0;
				while (i < 256 && (n + 3) < chunk_size) {
					int value = ((image[chunkp + n] & 0xFF)) + ((image[chunkp + n + 1] & 0xFF) << 8)
							+ ((image[chunkp + n + 2] & 0xFF) << 16) + ((image[chunkp + n + 3] & 0xFF) << 24);
					nsfe_entry[i].fade = (int) value;
					++i;
					n += 4;
				}
			} else if (cid[0] == 't' && cid[1] == 'l' && cid[2] == 'b' && cid[3] == 'l') // tlbl 小写
			{
				int n = 0;
				for (int i = 0; i < 256; ++i) {
					if (n >= chunk_size)
						break;
					int begin = chunkp + n;
					while (n < chunk_size && image[chunkp + n] != 0)
						++n;
					byte[] bytes = new byte[n - begin];
					System.arraycopy(image, begin, bytes, 0, bytes.length);
					nsfe_entry[i].tlbl = new String(bytes);
					if (image[chunkp + n] == 0)
						++n;
				}
			} else if (cid[0] == 't' && cid[1] == 'e' && cid[2] == 'x' && cid[3] == 't') // text 小写
			{
				text = new String(image, chunkp, chunk_size);
			} else { // unknown chunk
				if (cid[0] >= 'A' && cid[0] <= 'Z') {
					return false;
				}
			}

			// next chunk
			chunk_offset += chunk_size;
		}

		return true;
	}
	
	public void debugOut() {
		int i;
		// char buf[256] = "";
		PrintStream out = System.out;

		out.print(String.format("Magic:    %4s\n", new String(magic)));
		out.print(String.format("Version:  %d\n", version));

		out.print(String.format("Songs:    %d\n", songs));
		out.print(String.format("Load:     %04x\n", load_address));
		out.print(String.format("Init:     %04x\n", init_address));
		out.print(String.format("Play:     %04x\n", play_address));
		out.print(String.format("Title:    %s\n", title));
		out.print(String.format("Artist:   %s\n", artist));
		out.print(String.format("Copyright:%s\n", copyright));
		out.print(String.format("Speed(N): %d\n", speed_ntsc));
		out.print(String.format("Speed(P): %d\n", speed_pal));

		out.print("Bank :");
		for (i = 0; i < 8; i++) {
			out.print(String.format("[%02x]", bankswitch[i]));
		}
		out.print("\n");

		if ((pal_ntsc & 1) != 0)
			out.print("PAL mode.\n");
		else
			out.print("NTSC mode.\n");
		if ((pal_ntsc & 2) != 0)
			out.print("Dual PAL and NTSC mode.\n");

		if ((soundchip & 1) != 0)
			out.print("VRC6 ");
		if ((soundchip & 2) != 0)
			out.print("VRC7 ");
		if ((soundchip & 4) != 0)
			out.print("FDS ");
		if ((soundchip & 8) != 0)
			out.print("MMC5 ");
		if ((soundchip & 16) != 0)
			out.print("Namco 106 ");
		if ((soundchip & 32) != 0)
			out.print("FME-07 ");

		out.print("\n");

		out.print("Extra:     ");
		for (i = 0; i < 4; i++) {
			out.print(String.format("[%02x]", extra[i]));
		}
		out.print("\n");
		out.print(String.format("DataSize: %d\n", body.length));
	}
	
	public String getTitle(String format, int song) {

		String fn;
		int i;
		if ((i = this.filename.lastIndexOf('\\')) >= 0) {
			fn = this.filename.substring(i + 1);
		} else if ((i = this.filename.lastIndexOf('/')) >= 0) {
			fn = this.filename.substring(i + 1);
		} else {
			fn = this.filename;
		}

		if (song < 0)
			song = this.song;

		if (!title_unknown) {
			return print_title = "<unknown>";
		}

		if (format == null)
			format = "%L (%n/%e) %T - %A";
		int ptr = 0; // ptr 是指向 format 的索引指针
		int len = format.length();

		StringBuilder b = new StringBuilder(len * 4 + 32); // 生成 print_title

		while (ptr < len) {
			char ch = format.charAt(ptr);
			if (SST.is_sjis_prefix(ch)) {
				b.append(ch);
				b.append(format.charAt(ptr++));
				ptr++;
				continue;
			} else if (ch == '%') {
				ch = format.charAt(++ptr);
				switch (ch) {
				case 'F':
				case 'f':
					b.append(fn);
					ptr++;
					break;
				case 'P':
				case 'p':
					b.append(this.filename);
					ptr++;
					break;
				case 'T':
				case 't':
					b.append(this.title);
					ptr++;
					break;
				case 'A':
				case 'a':
					b.append(this.artist);
					ptr++;
					break;
				case 'C':
				case 'c':
					b.append(this.copyright);
					ptr++;
					break;
				case 'L':
				case 'l':
					b.append(nsfe_entry[nsfe_plst != null ? nsfe_plst[song] : song].tlbl);
					ptr++;
					break;
				case 'N':
					b.append(String.format("$%02x", song + 1));
					ptr++;
					break;
				case 'n':
					b.append(String.format("%03d", song + 1));
					ptr++;
					break;
				case 'S':
					b.append(String.format("$%02x", start));
					ptr++;
					break;
				case 's':
					b.append(String.format("%03d", start));
					ptr++;
					break;
				case 'E':
					b.append(String.format("$%02x", songs));
					ptr++;
					break;
				case 'e':
					b.append(String.format("%03d", songs));
					ptr++;
					break;
				default:
					break;
				}
			} else {
				b.append(format.charAt(ptr++));
			}
		}

		// strip leading whitespace
		int wp;
		for (wp = 0; b.charAt(wp) == ' '; ++wp) {
		}
		if (wp > 0) {
			b.delete(0, wp);
		}

		title_unknown = false;
		return print_title = b.toString();
	}

}
