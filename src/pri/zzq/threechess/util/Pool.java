package pri.zzq.threechess.util;

import java.util.ArrayList;
import java.util.List;

/**
* @author zzQ
*
* @version 创建时间：Jan 4, 2019 9:11:12 PM
*/

public abstract class Pool<T> {
	public final int max;
	public int peak;

	private final List<T> freeObjects;

	public Pool () {
		this(16, Integer.MAX_VALUE);
	}

	public Pool (int initialCapacity) {
		this(initialCapacity, Integer.MAX_VALUE);
	}

	public Pool (int initialCapacity, int max) {
		freeObjects = new ArrayList<>(initialCapacity);
		this.max = max;
	}

	abstract protected T newObject ();

	public T obtain () {
		return freeObjects.isEmpty() ? newObject() : freeObjects.remove(freeObjects.size() - 1);
	}

	public void free (T object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		if (freeObjects.size() < max) {
			freeObjects.add(object);
			peak = Math.max(peak, freeObjects.size());
		}
		reset(object);
	}

	protected void reset (T object) {
		if (object instanceof Poolable) ((Poolable)object).reset();
	}

	public void freeAll (List<T> objects) {
		if (objects == null) throw new IllegalArgumentException("objects cannot be null.");
		List<T> freeObjects = this.freeObjects;
		int max = this.max;
		for (int i = 0; i < objects.size(); i++) {
			T object = objects.get(i);
			if (object == null) continue;
			if (freeObjects.size() < max) freeObjects.add(object);
			reset(object);
		}
		peak = Math.max(peak, freeObjects.size());
	}

	public void clear () {
		freeObjects.clear();
	}

	public int getFree () {
		return freeObjects.size();
	}

	static public interface Poolable {
		public void reset ();
	}
}
