package zdream.nsfplayer.vcm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * in vcm.h
 * @author Zdream
 */
public class Observable {
	
	protected Set<ObserverI> oblist = new HashSet<ObserverI>();
	
	public void attachObserver(ObserverI p) {
		oblist.add(p);
	}

	public void detachObserver(ObserverI p) {
		oblist.remove(p);
	}

	public int getObserverNum() {
		return oblist.size();
	}

	public Iterator<ObserverI> getObserver() {
		if (oblist.size() != 0)
			return oblist.iterator();
		else
			return null;
	}

	public void notify(int id) {
		for (Iterator<ObserverI> it = oblist.iterator(); it.hasNext();) {
			ObserverI o = it.next();
			o.notify(id);
		}
	}

}
