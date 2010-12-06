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

import java.io.BufferedInputStream;
import org.xml.sax.SAXException;
import simplehttpdb.crypt.DecodingExeption;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplehttpdb.crypt.AESHelper;
import simplehttpdb.model.Definitions;
import simplehttpdb.model.Entries;

/**
 *
 * @author simon thiel
 */
public class HTTPHelper {

    private final AESHelper aes = new AESHelper();

    /**
     * download url to byte array
     * in case this is not possible the function returns null
     * @param url
     * @return
     */
    public byte[] wgetByte(String url) {

//        Logger.getLogger(getClass().getName()).log(Level.INFO,
//                "wget:" + url);


        InputStream is = null;
        ArrayList<Byte> result = new ArrayList();

        try {
            URL myUrl = new URL(url);
            is = myUrl.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            int input = 0;
            while (input != -1) {
                input = bis.read();
                if (input != -1) {
                    result.add((byte) input);
                }
            }
            byte[] resultB = new byte[result.size()];
            for (int i=0; i<result.size();i++){
                resultB[i]=result.get(i);
            }
            return resultB;



        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    /**
     * downloads the url and returns the content as string
     * in case this is not possible an empty string is returned
     * @param url
     * @return
     */
    public String wgetString(String url) {
        try {
            byte[] result = wgetByte(url);
            if (result!=null){
                return new String(result, Definitions.CHARSET_NAME);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * parses a entries list given in XML format
     * the source is retireved from the server via HTTP
     *
     * in case the XML is plaintext the String has to be null
     *
     * @param url
     * @param key the key to decrypt the XML file, null in case the file is not encrypted
     * @return
     * @throws DecodingExeption
     */
    public Entries getListFromUrl(String url, String key) throws DecodingExeption {
        byte[] entriesB = wgetByte(url);

        if (entriesB == null) {
            return new Entries(); // file didn't exist
        }
        String entriesStr = null;

        if (key == null) {
            try {
                entriesStr = new String(entriesB, Definitions.CHARSET_NAME);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {//decrypt first

            //decrypt first
            entriesStr = aes.decrypt(entriesB, aes.toBytes(key));

            if (entriesStr == null) { //something went wrong (wrong key?)
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "unable to decrypt entryStr");
                throw new DecodingExeption("unable to decode entryStr");
            }
        }

        //parse XML
        Entries result = new Entries();
        try {
            result.loadXML(entriesStr);
        } catch (SAXException ex) {
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new DecodingExeption("unable to parse entryStr");
        }


        return result;
    }

    private String getIndexUrl(String weburl) {
        return weburl + Definitions.INDEX_FILE;
    }

    /**
     * returns the index list from the HTTP server
     * (the index is never encrypted)
     * @param url
     * @return
     */
    public Entries getIndex(String url) {
        try {
            return getListFromUrl(getIndexUrl(url), null);
        } catch (DecodingExeption ex) {
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * returns the relative path of where the value for the given name can be found
     * in case the key is not in the index the function returns null
     * @param url
     * @param name
     * @return
     */
    public String findLocationOfName(String url, String name) {

        return getIndex(url).getValue(name);
    }

    /**
     * returns the value of the given name, in case the information is encrypted
     * the key will be used for decrpyting it, otherwise the key should be null
     * @param url
     * @param name
     * @param key
     * @return
     * @throws DecodingExeption
     */
    public String getValue(String url, String name, String key)
            throws DecodingExeption {


        return getListFromUrl(url, key).getValue(name);
    }
}
