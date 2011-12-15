package fi.seco.saha3.infrastructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * A manager for the soft locks that prevent multiple users from navigating
 * to the editor page of the same resource concurrently. NOT meant to be a 
 * hard lock: only "locks" on the UI level, does not interfere with data 
 * access, and can be circumvented.
 * 
 */
public class ResourceLockManager implements DisposableBean {
	
	private class Lock {
		private String resource;
		private String owner;
		private TimerTask lockTimeoutTask;
		public Lock(String resource, String owner) {
			this.resource = resource;
			this.owner = owner;
		}
		public String getOwner() {
			return owner;
		}
		public void refresh() {
			if (lockTimeoutTask != null) lockTimeoutTask.cancel();
			lockTimeoutTask = new TimerTask() {
				public void run() { releaseLock(resource,owner); }
			};
			timer.schedule(lockTimeoutTask,TimeUnit.MINUTES.toMillis(5));
		}
		public void destroy() {
			if (lockTimeoutTask != null) lockTimeoutTask.cancel();
		}
	}
	
	private Timer timer = new Timer();
	private Map<String,Lock> locks = new HashMap<String,Lock>();
	
	public synchronized boolean acquireLock(String resource, String owner) {
		if (locks.containsKey(resource)) {
			Lock lock = locks.get(resource);
			if (lock.getOwner().equals(owner)) {
				lock.refresh();
				return true;
			}
		} else {
			Lock lock = new Lock(resource,owner);
			lock.refresh();
			locks.put(resource,lock);
			return true;
		}
		return false;
	}
	
	public synchronized boolean isLocked(String resource) {
		return locks.containsKey(resource);
	}
	
	public synchronized boolean releaseLock(String resource, String owner) {
		Lock lock = locks.get(resource);
		if (lock != null && lock.getOwner().equals(owner)) {
			lock.destroy();
			locks.remove(resource);
			return true;
		}
		return false;
	}
	
	public synchronized void releaseAllLocks() {
		for (Lock lock : locks.values()) lock.destroy();
		locks.clear();
	}
	
	public synchronized Set<String> getLockedResources() {
		return locks.keySet();
	}
	
	public synchronized Map<String,Collection<String>> getLockedResourcesByOwner() {
		Multimap<String,String> map = TreeMultimap.create();
		for (Map.Entry<String,Lock> entry : locks.entrySet())
			map.put(entry.getValue().getOwner(),entry.getKey());
		return map.asMap();
	}

	public void destroy() throws Exception {
		releaseAllLocks();
		timer.cancel();
	}
	
}
