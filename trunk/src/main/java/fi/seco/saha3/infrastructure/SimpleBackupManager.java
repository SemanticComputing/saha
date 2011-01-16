package fi.seco.saha3.infrastructure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class SimpleBackupManager implements DisposableBean, InitializingBean {    
	
	private class BackupTask extends TimerTask {
		@Override
		public void run() {
			scheduleNewBackupTask();
			backup();
		}
	}
	
    private Logger log = Logger.getLogger(getClass());
    
    private Timer timer = new Timer();

    private SahaProjectRegistry registry;
    private String backupDirectory;
        
    @Required
    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }
    
    @Required
    public void setSahaProjectRegistry(SahaProjectRegistry registry) {
        this.registry = registry;
    }
    
    private void scheduleNewBackupTask() {
    	log.info("Scheduling new backup task for: " + getNextBackupDate());
		timer.schedule(new BackupTask(),getNextBackupDate());
    }
    
    private Date getNextBackupDate() {
    	return tomorrow4am();
    }
    
    private Date tomorrow4am() {
    	Calendar c = new GregorianCalendar();
    	c.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE)+1,4,0,0);
    	return c.getTime();
    }
    
    private void backup() {
    	for (String projectName : registry.getOpenedProjects())
    		backupProject(projectName,new File(backupDirectory+projectName+".nt"));
    }
    
    private void backupProject(final String projectName, final File file) {
		new Thread() {
			@Override
			public void run() {
				try {
					FileChannel channel = new RandomAccessFile(file,"rw").getChannel();
					FileLock lock = channel.lock();
					registry.getModel(projectName).write(new FileWriter(file),"N-TRIPLE");
					log.info("Wrote backup to: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");
					registry.closeSahaProject(projectName);
					lock.release();
				} catch (IOException e) {
					log.error("",e);
				}
			}
		}.start();
	}

	public void destroy() throws Exception {
		timer.cancel();
	}

	public void afterPropertiesSet() throws Exception {
		scheduleNewBackupTask();
	}
}
