/**
 *
 * Copyright (C) 2004-2010 Simon Thiel.  All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package simplehttpdb.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import simplehttpdb.crypt.AESHelper;
import simplehttpdb.crypt.EncodingExeption;
import simplehttpdb.model.Definitions;
import simplehttpdb.model.Entries;
import simplehttpdb.model.Entry;

/**
 *
 * @author simon thiel
 */
public class FTPHelper {

    private static final String START_COMMENT_TAG = "\n<!--";
    private static final String END_COMMENT_TAG = "-->\n";
    
    private HTTPHelper http = null;
    private final AESHelper aes = new AESHelper();
    private final Random rnd = new Random(Calendar.getInstance().getTimeInMillis());

    /**
     * constructor takes a HTTPHelper as initial parameter
     * @param http
     */
    public FTPHelper(HTTPHelper http) {
        this.http = http;
    }

    /**
     * returns an FTP connection to the given server
     * if login fails null will be returned
     * @param ftpServer
     * @param ftpUser
     * @param ftpPass
     * @return
     * @throws SocketException
     * @throws IOException
     */
    public FTPClient getFTPConnection(String ftpServer, String ftpUser, String ftpPass)
            throws SocketException, IOException {

        FTPClient ftp = new FTPClient();

        int reply;

        ftp.connect(ftpServer);
        Logger.getLogger(FTPHelper.class.getName()).log(Level.INFO,
                "Connected to " + ftpServer + " --> " + ftp.getReplyString());

        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            ftp.disconnect();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "FTP server refused connection.");
            return null;
        }
        if (ftp.login(ftpUser, ftpPass)) {
            return ftp;
        }//else
        return null;
    }

    /**
     * searches the FTP directory for a fitting file to add a new entry
     * creates a new file as necessary
     * TODO: add support for multiple encryption keys
     *
     * @param ftpClient
     * @param rootDir
     * @param relPath
     * @return
     * @throws IOException
     */
    public String findFileToAddNewEntry(FTPClient ftpClient, String rootDir, String relPath)
            throws IOException {

        String localDir = rootDir + relPath;

        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "search for files in:" + localDir + " relPath:" + relPath);

        FTPFile[] files = ftpClient.listFiles(localDir);
        String result = "";
        for (FTPFile file : files) {

            //just handle files with name.legth>2 (avoiding "." and ".." as filenames)
            if (file.getName().length() < 3) {
                continue;
            }

            if (file.getSize() < 1024 * Definitions.MAX_FILE_SIZE) {
                result = relPath + file.getName();
                break;
            }
        }
        if (result.equals("")) {
            //we have to create a new file
            result = relPath + UUID.randomUUID() + ".xml";
        }
        return result;
    }

    private String extractDir(String file) {

        //first check whether we got just a directory
        if (file.endsWith("/")) {
            return file;
        }

        StringBuilder path = new StringBuilder();

        String[] segments = file.split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            path.append("/");
            path.append(segments[i]);

        }
        return path.toString();
    }

    private void createDirIfNotExists(FTPClient ftpClient, String path) throws IOException {

        //TODO: find a better way to check whether directory exists

//        Logger.getLogger(getClass().getName()).log(Level.INFO,
//                "trying to create dir:" + path);


        if (ftpClient.makeDirectory(path)) {
//            Logger.getLogger(getClass().getName()).log(Level.INFO,
//                    "done. "+ftpClient.getReplyString());
        } else {
//            Logger.getLogger(getClass().getName()).log(Level.INFO,
//                    "failed. "+ftpClient.getReplyString());
        }


    }

    private boolean writeFile(FTPClient ftpClient, byte[] content, String path)
            throws IOException {

        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "writing local file: " + path+" content length="+content.length);


        OutputStream os = ftpClient.storeFileStream(path);
        if (os == null) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "cannot open remote file " + path);
            return false;

        }
        os.write(content);
        os.close();
        ftpClient.completePendingCommand();
        return true;
    }

    private byte[] encryptXML(String content, String key) throws EncodingExeption {
        try {
  
            return aes.encrypt(content, aes.toBytes(key));
            
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new EncodingExeption("unable to encode content with key" + key);
    }

    private boolean writeFile(FTPClient ftpClient, String content, String path, String key)
            throws IOException, EncodingExeption {

        byte[] myContent;


        if (key == null) { //don't encrypt
            myContent = content.getBytes(Definitions.CHARSET_NAME);
        }else{
            myContent = encryptXML(content, key);
        }

        return writeFile(ftpClient, myContent, path);
    }

    private boolean writeFile(FTPClient ftpClient, Entries entries, String path, String key)
            throws IOException, EncodingExeption {

        return writeFile(ftpClient, entries.toXML(), path, key);
    }

    private String readLockFile(String webUrl) {
        return http.wgetString(webUrl + Definitions.LOCK_FILE);
    }

    private boolean writeLock(FTPClient ftpClient, String lock, String rootDir) {

        try {
            return writeFile(ftpClient, lock.getBytes(Definitions.CHARSET_NAME),
                    rootDir + Definitions.LOCK_FILE);
            
        } catch (IOException ex) {
            Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String getLock(String webUrl, String rootDir, FTPClient ftpClient) {

        //loop until lock was removed
        Logger.getLogger(FTPHelper.class.getName()).log(Level.INFO, "wait for unlock");
        String myLock = readLockFile(webUrl);
        for (int repeats = 0; (repeats < 3) && (!myLock.equals("")); repeats++) {
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            myLock = readLockFile(webUrl);

        }
        //write lock
        Logger.getLogger(FTPHelper.class.getName()).log(Level.INFO, "write lock");
        myLock = UUID.randomUUID().toString();
        if (!writeLock(ftpClient, myLock, rootDir)) {
            return null;
        }

        //try to double check the lock by loading it again from the server
        Logger.getLogger(FTPHelper.class.getName()).log(Level.INFO, "double check");
        String remoteLock = readLockFile(webUrl);
        for (int repeats = 0; (repeats < 3) && (remoteLock.equals("")); repeats++) {
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
            remoteLock = readLockFile(webUrl);
        }
        Logger.getLogger(FTPHelper.class.getName()).log(Level.INFO, "done");
        if (remoteLock.equals(myLock)) { //ok everything is fine
            return myLock;
        }//else
        //something was going wrong
        return null;

    }

    private void releaseLock(FTPClient ftpClient, String rootDir) {
        try {
            ftpClient.deleteFile(rootDir + Definitions.LOCK_FILE);
        } catch (IOException ex) {
            Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * writes entrylist localList to the given file and updates the index accordingly
     * in case it is not already up-to-date (e.g. only the value changed)
     *
     * The function also creates all directories as required
     *
     * @param webURL
     * @param rootDir
     * @param ftpClient
     * @param name
     * @param relPath
     * @param localList
     * @param key
     * @return
     * @throws EncodingExeption
     */
    public boolean storeFileAndUpdateIndex(String webURL, String rootDir, FTPClient ftpClient,
            String name, String relPath, Entries localList, String key) throws EncodingExeption {

        try {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "trying to store " + name
                    + " in:" + relPath + " with list:\n" + localList.toXML());
            //set binary mode
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //try to get writeLock
            String myLock = getLock(webURL, rootDir, ftpClient);
            if (myLock == null) {
                return false;
            }
            createDirIfNotExists(ftpClient, rootDir);
            createDirIfNotExists(ftpClient, extractDir(rootDir + relPath));
            //write local file
            if (!writeFile(ftpClient, localList, rootDir + relPath, key)) {
                return false;
            }
            Entries indexList = http.getIndex(webURL);
            //check whether update necessary
            String oldPath = indexList.getValue(name);
            if ((oldPath == null) || (!oldPath.equals(relPath))) {
                //ok, we have to update
                indexList.put(new Entry(name, relPath));
                if (!writeFile(ftpClient, indexList, rootDir + Definitions.INDEX_FILE, null)) {
                    return false;
                }
            }
            return true;

        } catch (IOException ex) {
            Logger.getLogger(FTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            releaseLock(ftpClient, rootDir);
        }
        return false;
    }
}
