package com.vlad.ihaveread.util;

import com.vlad.ihaveread.MainApplication;
import io.github.eliux.mega.Mega;
import io.github.eliux.mega.MegaSession;
import io.github.eliux.mega.cmd.FileInfo;
import io.github.eliux.mega.error.MegaInvalidStateException;
import io.github.eliux.mega.error.MegaLoginRequiredException;
import io.github.eliux.mega.error.MegaUnexpectedFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public class MegaUtil {

    private static final Logger log = LoggerFactory.getLogger(MegaUtil.class);

    public static String uploadDbFile(String localDbPath) throws Exception {
        String ret = "Database uploaded";
        MegaSession sessionMega = getMegaSession();
        try {
            File localDbFile = new File(localDbPath);
            LocalDateTime localDbDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(localDbFile.lastModified()),
                    TimeZone.getDefault().toZoneId()).withNano(0);
            String remoteDbFile = MainApplication.REMOTE_DB_FILE;
            //if remote file exist, check if local change date > remote change date
            boolean bDoUpload = false;
            if (sessionMega.exists(remoteDbFile)) {
                List<FileInfo> files = sessionMega.ls(remoteDbFile).call();
                FileInfo file = files.get(0);
                log.info("Local file. Name: {} size: {} lastModified: {}", localDbFile.getName(), localDbFile.length(), localDbDate);
                log.info("Remote file. Name: {} size: {} lastModified: {}", file.getName(), file.getSize(), file.getDate());
                if (localDbDate.isAfter(file.getDate())) {
                    bDoUpload = true;
                }
            } else {
                bDoUpload = true;
            }
            if (bDoUpload) {
                log.info("upload DB file");
                sessionMega.uploadFile(localDbPath, remoteDbFile).waitToUpload().run();
            } else {
                ret = "Upload not needed";
                log.info("Upload not needed");
            }
        } catch (Exception e) {
            log.error("Error by upload: ", e);
            throw e;
        } finally {
            sessionMega.logout();
        }
        return ret;
    }

    public static String downloadDbFile(String remoteDbFile, String localDbPath) throws Exception {
        String ret = "Database downloaded";
        MegaSession sessionMega = getMegaSession();
        try {
            File localDbFile = new File(localDbPath);
            LocalDateTime localDbDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(localDbFile.lastModified()),
                    TimeZone.getDefault().toZoneId()).withNano(0);
            //if remote file exist, check if local change date < remote change date
            boolean bDoDownload = false;
            if (sessionMega.exists(remoteDbFile)) {
                List<FileInfo> files = sessionMega.ls(remoteDbFile).call();
                FileInfo file = files.get(0);
                log.info("Local file. Name: {} size: {} lastModified: {}", localDbFile.getName(), localDbFile.length(), localDbDate);
                log.info("Remote file. Name: {} size: {} lastModified: {}", file.getName(), file.getSize(), file.getDate());
                if (file.getDate().isAfter(localDbDate)) {
                    bDoDownload = true;
                }
            }
            if (bDoDownload) {
                if (localDbFile.delete()) {
                    log.info("download DB file");
                    sessionMega.get(remoteDbFile, localDbPath).waitToUpload().run();
                } else {
                    ret = "Error deleting local DB file";
                    log.info("Error deleting local DB file");
                }
            } else {
                ret = "Download not needed";
                log.info("Download not needed");
            }
        } catch (Exception e) {
            log.error("Error by download: ", e);
            throw e;
        } finally {
            sessionMega.logout();
        }
        return ret;
    }

    public static MegaSession getMegaSession() throws Exception {
        MegaSession sessionMega;
        try {
            megaLogin();
            log.info("getting current session");
            sessionMega = Mega.currentSession();
        } catch (Exception e) {
            log.error("Error: ", e);
            throw e;
        }
        return sessionMega;
    }

    public static void megaLogin() throws Exception {
        try {
            log.info("login to mega");
            String user = System.getenv("MEGA_EMAIL");
            String pwd = System.getenv("MEGA_PWD");
            if (user == null || pwd == null) {
                throw new Exception("MEGA_EMAIL or MEGA_PWD not set");
            }
            ProcessBuilder builder = new ProcessBuilder("mega-login", user, pwd).inheritIO();
            Process process = builder.start();
            int exitCode = process.waitFor();
            log.info("Exit code = {}", exitCode);
        } catch (Exception e) {
            log.error("Error: ", e);
            throw e;
        }
    }

    public static MegaSession getMegaSessionOrLogin() throws Exception {
        MegaSession sessionMega;
        try {
            log.info("getting current session");
            sessionMega = Mega.init();
        } catch (MegaLoginRequiredException | MegaUnexpectedFailureException | MegaInvalidStateException e) {
            log.error("Error: " + e.getMessage()+ ". Trying again");
            sessionMega = getMegaSession();
        }
        return sessionMega;
    }
}
