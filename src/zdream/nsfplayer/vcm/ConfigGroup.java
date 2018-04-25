package zdream.nsfplayer.vcm;

import java.util.Deque;
import java.util.List;

public class ConfigGroup {
	
	class Pair {
		String k;
		ValueCtrl v;
		public Pair(String _k, ValueCtrl _v) {
			this.k = _k;
			this.v = _v;
		}
	}
	
	public Deque<Pair> members;
	public String label;
	public String desc;
	
	public List<ConfigGroup> subGroup;
	
	public ConfigGroup( final String l, final String d, Configuration b/*=NULL*/ ) {
		this.label = l;
		this.desc = d;
	}

	public boolean addSubGroup(ConfigGroup sub) {
		subGroup.add(sub);
		return true;
	}

	public boolean insert(final String id, ValueCtrl ctrl, ValueConv conv /* =NULL */ ) {
		if (conv != null)
			ctrl.addConv(conv);
		this.members.add(new Pair(id, ctrl));
		return true;
	}

	public boolean insert(final String id, ValueCtrl ctrl, List<ValueConv> convs) {
		ctrl.addConv(convs);
		this.members.add(new Pair(id, ctrl));
		return true;
	}

	public void clear() {
		members.clear();
	}

}
