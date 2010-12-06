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

import org.w3c.dom.Node;
import simplehttpdb.xml.XMLLayerUtils;
import simplehttpdb.xml.XMLTool;

/**
 *
 * @author simon thiel
 */
public class Entry extends XMLTool implements XMLLayerUtils{

    /**
     * entry tag
     */
    public static final String TAGNAME = "e";
    /**
     * name tag
     */
    public static final String TAGNAME_NAME = "n";
    /**
     * value tag
     */
    public static final String TAGNAME_VALUE = "v";

    private String name = null;
    private String value = null;

    /**
     * constructor
     */
    public Entry(){
    }

    /**
     * constructor
     * @param name
     * @param value
     */
    public Entry(String name, String value){
        this.name = name;
        this.value = value;
    }

    private static String encode(String in) {
        String result = in;
        result = result.replaceAll("&","&amp;");
        result = result.replaceAll("<","&lt;");
        result = result.replaceAll(">","&gt;");
        result = result.replaceAll("\"","&quot;");

        return result;
    }

    private static String decode(String in) {
        String result = in;

        result = result.replaceAll("&lt;","<");
        result = result.replaceAll("&gt;",">");
        result = result.replaceAll("&quot;","\"");
        result = result.replaceAll("&amp;", "&");

        return result;
    }

    /**
     * returns XML representation of the entry
     * @return
     */
    public String toXML() {
        StringBuilder result = new StringBuilder();
        result.append(createLeadIn(TAGNAME));
        //name
        result.append(createLeadIn(TAGNAME_NAME));
        result.append(encode(getName()));
        result.append(createLeadOut(TAGNAME_NAME));
        //value
        result.append(createLeadIn(TAGNAME_VALUE));
        result.append(encode(getValue()));
        result.append(createLeadOut(TAGNAME_VALUE));


        result.append(createLeadOut(TAGNAME));
        return result.toString();
    }

    /**
     * populates entry members according to given XML structure
     * @param xmlInput
     */
    public void readXMLInput(Node xmlInput) {
       org.w3c.dom.Node childNode = null;


        //read name
        childNode = getFirstChildWithTag(xmlInput, TAGNAME_NAME);
        if (childNode!=null){
            setName(decode(getText(childNode)));
        }

        //read value
        childNode = getFirstChildWithTag(xmlInput, TAGNAME_VALUE);
        if (childNode!=null){
            setValue(decode(getText(childNode)));
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + (17*value.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entry other = (Entry) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }






}
