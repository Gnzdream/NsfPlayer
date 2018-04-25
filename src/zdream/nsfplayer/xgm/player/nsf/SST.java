package zdream.nsfplayer.xgm.player.nsf;

import java.util.Arrays;

/**
 * sublimate 方法同 delete + getStr()<br>
 * length 方法同 .length<br>
 * getn 方法同 .str<br>
 * @author Zdream
 *
 */
public class SST implements CharSequence {
	
	public static final int
			SST_BUF_GSIZE = 32,
			SST_EOF = -1;
	
	public int size;
	public int length;
	public int index;
	public char[] str;

	public void setText(final char[] str) {
		this.str = str;
		
		length = str.length;
		size = length + 1;
		index = 0;
	}
	
	public int seekoff(int offset) {
		int pos;

		pos = index + offset;
		if (pos < 0 || size < pos)
			return -1;
		index = pos;
		return 0;
	}
	
	public int seekpos(int pos) {
		if (pos < 0 || size < pos)
			return -1;
		index = pos;
		return 0;
	}
	
	public int tell() {
		return index;
	}
	
	public int putc(int c) {
		if (str == null) {
			str = new char[16];
			size = 16;
		}
		if (size <= index) {
			char[] ncs = new char[size + SST_BUF_GSIZE];
			System.arraycopy(str, 0, ncs, 0, str.length);
			str = ncs;
			size += SST_BUF_GSIZE;
		}
		str[index++] = (char) (c & 0xff);
		if (length < index)
			length = index;
		return c;
	}

	public int ungetc(int c) {
		if (c == SST_EOF)
			return -1;
		else if (index > 0) {
			str[--index] = (char) (c & 0xff);
			return c;
		} else
			return -1;
	}
	
	public int getc() {
		int c;

		if (size <= index + 1)
			return SST_EOF;
		c = str[index++];
		return c;
	}
	
	@Override
	public String toString() {
		return new String(str, 0, length);
	}
	
	/**
	 * ppls.cpp - skip_space 函数
	 */
	public void skip_space() {
		int c;

		while (true) {
			c = getc();
			if (c != ' ' && c != '\t')
				break;
		}

		ungetc(c);
	}
	
	/**
	 * ppls.cpp - get_filename 函数
	 */
	public char[] get_filename() {
		SST token;
		int c;

		skip_space();
		token = new SST();

		while ((c = getc()) != SST_EOF) {
			if (c == ':') {
				c = getc();
				if (c == ':')
					break;
				c = ungetc(c);
				c = ':';
			}
			token.putc(c);
			if (is_sjis_prefix(c))
				token.putc(getc());
		}
		ungetc(c);

		return token.str;
	}
	
	public static boolean is_sjis_prefix(int c) {
		if ((0x81 <= c && c <= 0x9F) || (0xE0 <= c && c <= 0xFC))
			return true;
		else
			return false;
	}
	
	public char[] get_type() {
		SST token;
		int c;

		skip_space();
		token = new SST();

		while ((c = getc()) != ',' && c != SST_EOF && c != ' ' && c != '\t') {
			token.putc(c);
			if (is_sjis_prefix(c))
				token.putc(getc());
		}
		ungetc(c);

		return token.str;
	}

	public char[] get_title() {
		SST token;
		int c;

		skip_space();
		token = new SST();

		while ((c = getc()) != ',' && c != SST_EOF) {
			if (c == '\\')
				c = getc();
			token.putc(c);
			if (is_sjis_prefix(c))
				token.putc(getc());
		}
		ungetc(c);

		return token.str;
	}
	
	public static int str2type(final String str) {
		if ("KSS".equalsIgnoreCase(str))
			return 1;
		if ("MSX".equalsIgnoreCase(str))
			return 2;
		if ("NSF".equalsIgnoreCase(str))
			return 3;
		return 0;
	}
	
	public static boolean is_digit(int c) {
		if ('0' <= c && c <= '9')
			return true;
		else
			return false;
	}
	
	public int get_number(int default_value) {
		int c;
		int ret = 0;
		int raw = 0;

		skip_space();
		c = getc();
		if (c == SST_EOF)
			return 0;

		if (c == '$') {
			while (true) {
				c = getc();
				if (is_digit(c))
					ret = (ret << 4) + c - '0';
				else {
					// toLower
					if ('A' <= c && c <= 'F') {
						ret = (ret << 4) + c + ('a' - 'A') - 'a' + 10;
					} else if ('a' <= c && c <= 'f') {
						ret = (ret << 4) + c - 'a' + 10;
					} else
						break;
				}
				raw++;
			}
		} else {
			ungetc(c);
			while (true) {
				c = getc();
				if (is_digit(c))
					ret = ret * 10 + c - '0';
				else
					break;
				raw++;
			}
		}

		ungetc(c);

		if (raw == 0)
			ret = default_value;
		return ret;
	}
	
	public boolean check_section(int scode) {
		int c;

		skip_space();
		if ((c = getc()) == scode)
			return true;
		ungetc(c);

		return false;
	}
	
	public int get_time(int default_value) {
		int time = 0;
		int c;

		skip_space();
		c = getc();
		ungetc(c);
		if (!is_digit(c))
			return default_value;

		time = get_number(0);
		if (check_section(':')) {
			time = time * 60 + get_number(0);
			if (check_section(':'))
				time = time * 60 + get_number(0);
		}

		time *= 1000;

		if (check_section('\''))
			time += get_number(0) * 10;

		return time;
	}
	
	public static int get_time(char[] text, int d) {
		int ret;
		SST sst = new SST();

		sst.setText(text);
		ret = sst.get_time(d);
		return ret;
	}
	
	public static String chs2string(char[] cs) {
		int i = 0;
		for (i = 0; i < cs.length; i++) {
			if (cs[i] == '\0') {
				break;
			}
		}
		return new String(cs, 0, i);
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		return this.str[index];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		char[] sub = Arrays.copyOfRange(str, start, end);
		SST n = new SST();
		n.setText(sub);
		return n;
	}
	
}
