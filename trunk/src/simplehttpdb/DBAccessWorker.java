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
package simplehttpdb;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import simplehttpdb.crypt.DecodingExeption;
import simplehttpdb.crypt.EncodingExeption;
import simplehttpdb.model.Entries;
import simplehttpdb.model.Entry;
import simplehttpdb.net.FTPHelper;
import simplehttpdb.net.HTTPHelper;

/**
 * DBAccessWorker is the access interface for the key-value database. It only provides two functions: read and write.
 *
 * An example about how to use it can be found in
 *
 * @see simplehttpdb.test.TestGui
 *
 * @author simon thiel
 */
public class DBAccessWorker {

    
    private HTTPHelper http = new HTTPHelper();
    private FTPHelper ftp = new FTPHelper(http);


    /**
     * reads the requested value from the given url
     * in case hexKey != null the function tries to decrypt the value with the given key
     * @param url
     * @param name
     * @param hexKey
     * @return
     */
    public String read(String url, String name, String hexKey) {

        Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO, "\n\read:\n"
                + "url: " + url + "\nhexKey: " + hexKey + "\nname: " + name +"\n");


        Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO,
                "read entries from:" + url);

        String result = null;

        String relPath = http.findLocationOfName(url, name);
        Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO,
                "relPath:" + relPath);

        if (relPath != null) {

            Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO,
                    "loading name-value from:" + url + relPath);
            try {
                result = http.getValue(url + relPath, name, hexKey);
            } catch (DecodingExeption ex) {
                Logger.getLogger(DBAccessWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
    }

    /**
     * write a key-value pair to the given server
     * in case hexKey != null the value will be encrypted by use of the given key
     * in case no update is required the function won't write to the server
     *
     * FIXME: switching between encrypted and plain text is not supported yet
     * FIXME: using different keys for the same server is not supported yet
     *
     * @param webURL
     * @param ftpServer
     * @param ftpUser
     * @param ftpPass
     * @param rootDir
     * @param hexKey
     * @param name
     * @param value
     * @return
     * @throws DecodingExeption
     * @throws EncodingExeption
     */
    public int write(String webURL, String ftpServer, String ftpUser, String ftpPass,
            String rootDir, String hexKey, String name, String value) 
            throws DecodingExeption, EncodingExeption {


        Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO, "\n\nwrite:\n"
                + "webURL: " + webURL + "\nftpServer: " + ftpServer + "\nftpUser: " + ftpUser
                + "\nrootDir: " + rootDir + "\nhexKey: " + hexKey + "\nname: " + name
                + "\nvalue: " + value+"\n");

        int result = -1;
        String relPath = http.findLocationOfName(webURL, name);
        FTPClient ftpClient = null;
        try {
            

            Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO,
                    "relpath=" + relPath);

            if (relPath == null) { //entry doesn't exist yet
                if (hexKey == null) {
                    relPath = "free/";
                } else {
                    relPath = "crypt/";
                }
                ftpClient = ftp.getFTPConnection(ftpServer, ftpUser, ftpPass);
                relPath = ftp.findFileToAddNewEntry(ftpClient, rootDir, relPath);
            } else {
                //we got the name of the file
            }
            Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO,
                    "relpath(2)=" + relPath);



            //handling files
            Entries localList = http.getListFromUrl(webURL + relPath, hexKey);
            //if file is not existing we just continue with the empty list

            //check if value changed after all
            String oldValue = localList.getValue(name);
            if ((oldValue==null) || (!oldValue.equals(value))){ //ok, we have to write to ftp

                localList.put(new Entry(name, value));

                if (ftpClient==null){ //for performace issues, ftp only got initialized in case
                                      //there was no entry found in the index already
                                      //so it might be required to initialize it here
                    ftpClient = ftp.getFTPConnection(ftpServer, ftpUser, ftpPass);
                }
                if (ftp.storeFileAndUpdateIndex(webURL, rootDir, ftpClient, name,
                        relPath, localList, hexKey)) {
                    result = 0;
                }
            }else{
                //no update was necessary
                result = 0;
            }

            //Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO, "logout");
            if (ftpClient!=null){
                ftpClient.logout();
            }
        } catch (SocketException ex) {
            Logger.getLogger(DBAccessWorker.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } catch (IOException ex) {
            Logger.getLogger(DBAccessWorker.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            if ((ftpClient!=null) && (ftpClient.isConnected())) {
                try {
                    //Logger.getLogger(DBAccessWorker.class.getName()).log(Level.INFO, "disconnect");
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    return -1;
                }
            }


        }
        return result;
    }
}
