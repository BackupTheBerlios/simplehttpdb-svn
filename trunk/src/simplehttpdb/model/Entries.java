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

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import simplehttpdb.xml.XMLLayerUtils;
import simplehttpdb.xml.XMLTool;

/**
 *
 * @author simon thiel
 */
public class Entries  extends XMLTool implements XMLLayerUtils{

    /**
     * tagname for entries
     */
    public static final String TAGNAME = "es";

    private Hashtable<String, Entry> entries = new Hashtable();

    /**
     * get XML representation
     * @return
     */
    public String toXML() {
        StringBuilder result = new StringBuilder();
        result.append(createLeadIn(TAGNAME));

        for (Entry entry: entries.values()){
            result.append(entry.toXML());
        }

        
        result.append(createLeadOut(TAGNAME));
        return result.toString();

    }

    /**
     * update object by use of given xml structure
     * @param xmlInput
     */
    public void readXMLInput(Node xmlInput) {
         org.w3c.dom.Node childNode = null;

        //read name
        childNode = getFirstChildWithTag(xmlInput, Entry.TAGNAME);
        while (childNode!=null){

            Entry entry = new Entry();
            entry.readXMLInput(childNode);
            entries.put(entry.getName(), entry);

            childNode = getNextSiblingWithTag(childNode, Entry.TAGNAME);
        }
    }

    /**
     * load the given xml-file and establish structure accordingly
     * @param xml
     * @throws SAXException
     */
    public void loadXML(String xml) throws SAXException{

        Logger.getLogger(Entries.class.getName()).log(Level.INFO, "loadXML:\n"+xml);

        org.w3c.dom.Node node = simplehttpdb.xml.XMLAccess.getInstance()
                .parseXML(xml).getFirstChild();

        if (node != null){
            readXMLInput(node);
        }
    }

    /**
     * returns the value of the given entry or null in case it's not existing
     * @param name
     * @return
     */
    public String getValue(String name){
        String result = null;
        Entry entry = entries.get(name);
        if (entry!=null){
            result = entry.getValue();
        }

        return result;
    }

    /**
     * add new entry to the list or updates existing
     * there can only be one entry for each key value
     * @param entry
     */
    public void put(Entry entry){
        entries.put(entry.getName(), entry);
    }

}
