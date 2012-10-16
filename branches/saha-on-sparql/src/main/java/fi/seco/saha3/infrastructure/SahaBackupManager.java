package fi.seco.saha3.infrastructure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * The main backup manager for SAHA. By default, stores three backups for each
 * project: a month old, a week old (taken every saturday) and a day old.
 * Expired backups are deleted.
 * 
 */
public class SahaBackupManager implements DisposableBean {

	private final Logger log = Logger.getLogger(getClass());

	private final Timer timer;

	private SahaProjectRegistry registry;
	private String backupDirectory;

	public SahaBackupManager() {
		this.timer = new Timer(true);

		Date nextBackup = getNextBackupTime();
		log.info("Backup manager launched successfully");
		log.info("Next backup scheduled for: " + nextBackup.toString());

		this.timer.schedule(this.getBackupTask(), nextBackup);

	}

	@Required
	public void setBackupDirectory(String backupDirectory) {
		this.backupDirectory = backupDirectory;
	}

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry registry) {
		this.registry = registry;
	}

	private TimerTask getBackupTask() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				try {
					backup();
				} finally {
					Date nextBackup = getNextBackupTime();
					log.info("Next backup scheduled for: " + nextBackup.toString());

					// FIXME: REMOVE COMMENT FOR PRODUCTION

					timer.schedule(getBackupTask(), nextBackup);
				}
			}
		};

		return task;
	}

	private static Date getNextBackupTime() {
		Calendar cal = Calendar.getInstance();

		// Backup scheduled for the next day, 0200
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE) + 1, 3, 0, 0);

		// FIXME: SWAP FOR PRODUCTION
		// Backup scheduled to happen in 5 seconds (debugging)        
		//         cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 5);

		return cal.getTime();
	}

	private static void setAllPrivileges(File file) {
		file.setReadable(true, false);
		file.setWritable(true, false);
		file.setExecutable(true, false);
	}

	private synchronized void backup() {
		log.info("Starting scheduled backup...");

		boolean errors = false;

		File backupDir = new File(this.backupDirectory);
		if (!backupDir.exists() || !backupDir.isDirectory()) {
			log.error("Could not perform backup -- invalid project directory: " + this.backupDirectory);
			return;
		}

		File backupManifest = new File(this.backupDirectory + "backups.data");
		if (!backupManifest.exists()) setAllPrivileges(backupManifest);

		RandomAccessFile lockFile = null;
		FileChannel fc = null;
		FileLock lock = null;
		try {
			lockFile = new RandomAccessFile(new File(this.backupDirectory + ".lock"), "rw");

			fc = lockFile.getChannel();

			log.info("Acquiring lock for backup manifest...");

			lock = fc.tryLock();
			while (lock == null) {
				log.warn("Could not acquire file lock for backup directory -- trying again in 1 minute");
				Thread.sleep(60000);
				lock = fc.tryLock();
			}

			// Lock acquired -- proceeding

			log.info("Performing backup...");

			Map<String, String> expireDates = new HashMap<String, String>();
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

			Date now = new Date();

			// Remove expired old backups        
			if (backupManifest.exists()) try {
				BufferedReader br = new BufferedReader(new FileReader(backupManifest));
				String line = br.readLine();
				while (line != null) {
					File file = new File(this.backupDirectory + line);
					if (!file.exists()) {
						log.debug("File " + file.getAbsolutePath() + " did not exist.");
						line = br.readLine();
					} else {
						Date expireDate = dateFormat.parse(br.readLine());

						if (expireDate.before(now)) {
							// File expired
				log.info("Deleting expired backup " + file.getName());
				file.delete();
			} else {
				// Keep this expire note in manifest
				log.info("Preserving non-expired backup " + file.getName());
				expireDates.put(file.getName(), dateFormat.format(expireDate));
			}
		}
		line = br.readLine();
	}

	br.close();
} catch (Exception e) {
	log.error("Exception during removal of expired backups:");
	log.error("", e);
	errors = true;
}

			String expirationDate = getExpirationDate(dateFormat);

			// Backup all projects
			for (String projectName : registry.getAllProjects()) {
				// FIXME: COMMENT FOR PRODUCTION
				//                if (!projectName.equals("historia4"))
				//                    continue;

				log.info("Backing up project " + projectName + " ...");

				try {
					this.registry.getLockForProject(projectName).readLock().lock();

					Model m = registry.getModelReader(projectName).getWholeProject();
					//                SahaProject project = registry.getSahaProject(projectName);            
					//                Model m = project.getModel();

					String fileName = projectName + "_" + dateFormat.format(now) + ".ttl";
					File backupFile = new File(this.backupDirectory + fileName);

					if (backupFile.exists()) backupFile.delete();

					m.write(new FileOutputStream(backupFile), "TTL");
					log.info("Backed up project " + projectName + " as " + fileName);

					setAllPrivileges(backupFile);

					// Set expiration date
					expireDates.put(fileName, expirationDate);

				} finally {
					this.registry.getLockForProject(projectName).readLock().unlock();
				}

			}

			backupManifest.delete();

			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(backupManifest));

				for (Entry<String, String> entry : expireDates.entrySet()) {
					bw.write(entry.getKey() + "\n");
					bw.write(entry.getValue() + "\n");
					bw.flush();
				}

				bw.close();
			} catch (IOException e) {
				log.error("Exception during creation of new manifest:");
				log.error("", e);
				errors = true;
			}

			setAllPrivileges(backupManifest);
		} catch (IOException e1) {
			log.error("IO Exception while performing backup: ");
			log.error("", e1);
			errors = true;
		} catch (InterruptedException e) {
			log.error("Thread execution interrupted while waiting for lock -- aborting backup");
			errors = true;
		} finally {
			try {
				if (lock != null && lock.isValid()) lock.release();
			} catch (IOException e) {
				log.fatal("Could not release lock for backup manifest -- lock might have to be released manually");
				errors = true;
			}

			try {
				if (lockFile != null) lockFile.close();

			} catch (IOException e) {
				log.error("Could not close lock file.");
				errors = true;
			}
		}

		if (errors)
			log.warn("Unexpected errors in backup -- might be finished in an undetermined state.");
		else log.info("Backup completed successfully.");
	}

	private String getExpirationDate(DateFormat format) {
		Calendar cal = Calendar.getInstance();

		// Once a month, save for a month
		if (cal.get(Calendar.DAY_OF_MONTH) == 1)
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
		else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 7);
		else cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

		return format.format(cal.getTime());
	}

	@Override
	public void destroy() throws Exception {
		timer.cancel();
	}
}
