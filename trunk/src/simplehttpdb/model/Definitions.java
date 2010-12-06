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
package simplehttpdb.model;

/**
 *
 * @author simon thiel
 */
public class Definitions {

    /**
     * filename of the index file
     */
    public static final String INDEX_FILE = "indexDB.xml";
    /**
     * lockfile used to avoid concurrent write
     */
    public static final String LOCK_FILE = "db_write.lock";
    /**
     * used encryption algoritm
     */
    public static final String CRYPT_ALGORITHM = "AES";
    /**
     * quality of encryption
     * to support 256, make sure to have installed unlimited encryption strength in your VM
     * TODO: once shifted to third-party encryption lib, remove this mark
     */
    public static final int CRYPT_STRENGTH = 256;

    /** name of the character set to use for converting between characters and bytes */
    //FIXME enforce this for the whole project
    public static final String CHARSET_NAME = "UTF-8";
    
    /**
     * Maximum size a file can crow before a new file will be created. (in bytes)
     */
    public static final long MAX_FILE_SIZE = 250 * 1024; //Bytes
}
