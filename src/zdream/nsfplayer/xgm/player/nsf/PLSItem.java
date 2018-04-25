package zdream.nsfplayer.xgm.player.nsf;

/**
 * relative_path 方法跳过;
 * @author Zdream
 */
public class PLSItem {
	
	public int type;
	public String filename;
	public String title;
	public int song;

	public int time_in_ms;
	public int loop_in_ms;
	public int fade_in_ms;

	public int loop_num;

	public int[] enable_vol = new int[4];
	public int[] vol = new int[4];

	public int extra_code;
	public int opll_ch_pan;
	
	/**
	 * 
	 * @param play_time
	 * @param fade_time
	 * @param loop_num
	 * @param vol
	 *   length : 4
	 */
	public void adjust(int play_time, int fade_time, int loop_num, int[] vol) {
		int i;

		if (time_in_ms < 0) {
			time_in_ms = play_time;
			if (loop_in_ms < 0)
				loop_in_ms = 0;
		} else if (loop_in_ms < 0)
			loop_in_ms = time_in_ms;

		if (this.loop_num < 0)
			this.loop_num = loop_num;
		if (fade_in_ms < 0)
			fade_in_ms = fade_time;
		for (i = 0; i < 4; i++)
			if (enable_vol[i] == 0)
				this.vol[i] = vol[i];

		return;
	}
	
	public PLSItem(final char[] text) {

		SST sst;
		int i, abb_value;
		char[] p;

		A: {
			sst = new SST();
			sst.setText(text);

			time_in_ms = -1;
			fade_in_ms = -1;
			loop_in_ms = -1;
			loop_num = -1;
			for (i = 0; i < 4; i++)
				enable_vol[i] = 0;

			filename = SST.chs2string(sst.get_filename());
			if (!sst.check_section(':'))
				break A;

			p = sst.get_type();
			this.type = SST.str2type(SST.chs2string(p));

			if (!sst.check_section(','))
				break A;

			if (type == 0)
				break A;
			else if (type == 1)
				abb_value = -1; /* 0 : old NEZplug behavior */
			else
				abb_value = -1;

			song = sst.get_number(0);
			if (!sst.check_section(','))
				break A;
			if (type == 3)
				song--;

			title = SST.chs2string(sst.get_title());
			if (!sst.check_section(','))
				break A;

			time_in_ms = sst.get_time(-1);
			if ((time_in_ms == -1) && (type == 1))
				break A;
			if (!sst.check_section(','))
				break A;

			loop_in_ms = sst.get_time(-1);
			if (sst.check_section('-'))
				loop_in_ms = time_in_ms - loop_in_ms;
			if (!sst.check_section(','))
				break A;

			fade_in_ms = sst.get_time(abb_value);
			if (!sst.check_section(','))
				break A;

			loop_num = sst.get_number(-1);
			for (i = 0; i < 4; i++) {
				if (!sst.check_section(','))
					break A;
				if (sst.check_section('-')) {
					int iv = sst.get_number(0);
					if (iv > 128) {
						vol[i] = -iv;
					} else {
						vol[i] = -128;
					}
				} else {
					int iv = sst.get_number(0);
					if (iv > 128) {
						vol[i] = iv;
					} else {
						vol[i] = 128;
					}
				}
				if (vol[i] != 128)
					enable_vol[i] = 1;
				else {
					enable_vol[i] = 0;
					vol[i] = 0;
				}
			}

			if (sst.check_section('?')) {
				extra_code = 1;
				opll_ch_pan = sst.get_number(0x7EDE79E);
			}
		}

		/* exit: */
		if (filename == null) {
			filename = SST.chs2string(text);
		}
	}
	
	public StringBuilder print_time(int time) {
		StringBuilder buf = new StringBuilder(32);
		int h, m, s;

		if (time < 0)
			return buf;

		time /= 1000;
		s = time % 60;
		time /= 60;
		m = time % 60;
		time /= 60;
		h = time;

		if (h != 0) {
			if (h < 10) {
				buf.append('0').append(h);
			} else {
				buf.append(h);
			}
			buf.append(':');
		}
		if (m < 10) {
			buf.append('0').append(m);
		} else {
			buf.append(m);
		}
		buf.append(':');
		if (s < 10) {
			buf.append('0').append(s);
		} else {
			buf.append(s);
		}

		return buf;
	}
	
	public void splitpath(char[] fn, StringBuilder path, StringBuilder file) {
		int p;

		for (p = fn.length; p >= 0; p--) {
			if (fn[p] != '\0') {
				break;
			}
		}
		int end = ++p;
		for (; p >= 0; p--) {
			if (fn[p] == '\\') {
				break;
			}
		}
		int start = ++p;
		path.append(fn, start, end);
		path.append(fn, 0, start);
		return;
	}
	
	public void relative_path(StringBuilder src_path, StringBuilder dst_path, StringBuilder rel_path) {
		int i = 0, s, d, r = 0, max;

		if (src_path.charAt(src_path.length() - 1) != '\\') {
			return;
		}
		if (dst_path.charAt(dst_path.length() - 1) != '\\') {
			return;
		}

		while (src_path.charAt(i) != '\0' && dst_path.charAt(i) != '\0') {
			if (src_path.charAt(i) != dst_path.charAt(i))
				break;
			i++;
		}

		s = d = i;

		while (s > 0 && src_path.charAt(s - 1) != '\\')
			s--;
		if (s == 0) {
			rel_path.delete(0, rel_path.length());
			rel_path.append(dst_path.toString());
			return;
		}
		while (d > 0 && dst_path.charAt(d - 1) != '\\')
			d--;

		max = 256 - (dst_path.length() + d);

		while (src_path.charAt(s) != '\0') {
			if (src_path.charAt(s) == '\\') {
				if ((max - 4) < r)
					break;

				rel_path.setCharAt(r++, '.');
				rel_path.setCharAt(r++, '.');
				rel_path.setCharAt(r++, '\\');
			}
			s++;
		}

		rel_path.append(dst_path, d, dst_path.length());
	}
	
	public StringBuilder print(StringBuilder buf, char[] plsfile) {
		int i = 0, length;
		// int p = 0 ; // 指向 buf 的索引指针
		StringBuilder file_path = new StringBuilder(), pls_path = new StringBuilder(), file_name = new StringBuilder(),
				pls_name = new StringBuilder(), rel_path = new StringBuilder();

		if (plsfile != null) {
			splitpath(plsfile, pls_path, pls_name);
			splitpath(filename.toCharArray(), file_path, file_name);
			relative_path(pls_path, file_path, rel_path);

			buf.append(String.format("%s%s::MSX", rel_path, file_name));
		} else {
			buf.append(String.format("%s::MSX", filename));
		}

		buf.append(String.format(",%d,", song));

		if (title != null) {
			length = title.length();
			for (i = 0; i < length; i++) {
				if (title.indexOf("\\,") >= 0) {
					buf.append('\\');
				}
				buf.append(title.charAt(i));
			}
		}

		buf.append(',');
		if (time_in_ms >= 0) {
			buf.append(print_time(time_in_ms));
		}

		buf.append(',');
		if (loop_in_ms >= 0) {
			buf.append(print_time(loop_in_ms));
		}

		buf.append(',');
		if (fade_in_ms >= 0) {
			buf.append(print_time(fade_in_ms));
		}

		buf.append(',');
		if (loop_num >= 0) {
			buf.append(print_time(loop_num));
		}

		for (i = 0; i < 4; i++) {
			buf.append(',');
			if (enable_vol[i] != 0) {
				buf.append(vol[i]);
			}
		}

		if (extra_code != 0) {
			buf.append('?');
		}

		return buf;
	}
	
	public void set_title(final CharSequence title) {
		if (title == null) {
			this.title = null;
			return;
		}

		this.title = title.toString();
	}

}
