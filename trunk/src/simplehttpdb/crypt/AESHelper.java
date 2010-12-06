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
package simplehttpdb.crypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import simplehttpdb.model.Definitions;

/**
 *
 * @author simon thiel
 * inspired from From http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html
 *
 * for a better solution check http://gmailassistant.sourceforge.net/src/org/freeshell/zs/common/Encryptor.java.html
 *
 * This program generates a AES key, retrieves its raw bytes, and
 * then reinstantiates a AES key from the key bytes.
 * The reinstantiated key is used to initialize a AES cipher for
 * encryption and decryption.
 *
 *

 */
public class AESHelper {

    private static final char[] kDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
        'b', 'c', 'd', 'e', 'f'};

    private byte[] toBytes(char[] hex) {
        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++) {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;
            if (value > 127) {
                value -= 256;
            }
            raw[i] = (byte) value;
        }
        return raw;
    }

    /**
     * transforms the string representation into a byte array
     * @param hex
     * @return
     */
    public byte[] toBytes(String hex) {
        return toBytes(hex.toCharArray());
    }

    /**
     * transforms array of bytes into string
     *
     * @param buf	Array of bytes to convert to hex string
     * @return	Generated hex string
     */
    public String toHex(byte buf[]) {
        StringBuilder strbuf = new StringBuilder(buf.length * 2);
        int i;

        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10) {
                strbuf.append("0");
            }

            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));


        }

        return strbuf.toString();
    }

    /**
     * generates a key for encryption as specified in
     *
     * @see simplehttpdb.model.Definitions
     * @return
     * @throws NoSuchAlgorithmException
     */
    public byte[] generateKey() throws NoSuchAlgorithmException {
        // Get the KeyGenerator
        KeyGenerator kgen = KeyGenerator.getInstance(Definitions.CRYPT_ALGORITHM);
        //kgen.init(256);
        kgen.init(Definitions.CRYPT_STRENGTH);
        // Generate the secret key specs.
        byte[] key = kgen.generateKey().getEncoded();
        Logger.getLogger(AESHelper.class.getName()).log(Level.INFO, "key:" + toHex(key) + "\nlength:" + (key.length * 8));
        return key;
    }

    private Cipher initCipher(int mode, byte[] skey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        SecretKeySpec skeySpec = new SecretKeySpec(skey, Definitions.CRYPT_ALGORITHM);
        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance(Definitions.CRYPT_ALGORITHM);

        Logger.getLogger(AESHelper.class.getName()).log(Level.INFO, "skey:"
                + toHex(skey) + "\nlength:" + skey.length);

        cipher.init(mode, skeySpec);
        return cipher;
    }

    private byte[] prepareContent(String content) {

        try {
            return content.getBytes(Definitions.CHARSET_NAME);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    /**
     * encrypts source string by use of the given key
     * the algorithm is defined in
     *
     * @see simplehttpdb.model.Definitions
     * @param source
     * @param skey
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] encrypt(String source, byte[] skey) throws IllegalBlockSizeException, BadPaddingException {
        byte[] result = null;

        try {
            byte[] content = prepareContent(source);
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, skey);
            result = cipher.doFinal(content);


        } catch (InvalidKeyException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * decrypts source into string by use of the given key
     * decryption algorithm is defined in
     *
     * @see simplehttpdb.model.Definitions
     * @param source
     * @param skey
     * @return
     */
    public String decrypt(byte[] source, byte[] skey) {
        String result = null;

        try {
            Logger.getLogger(AESHelper.class.getName()).log(
                    Level.INFO, "source length:"+source.length);
            
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, skey);
            byte[] original = cipher.doFinal(source);
            result = new String(original, Definitions.CHARSET_NAME);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AESHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
