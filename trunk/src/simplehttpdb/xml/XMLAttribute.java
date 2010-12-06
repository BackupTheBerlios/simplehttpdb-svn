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

package simplehttpdb.xml;

/**
 *
 * @author Simon Thiel
 */
public class XMLAttribute {
    
    private String tag;
    private String value;
    
    /** Creates a new instance of XMLAttribute
     * @param tag
     * @param value
     */
    public XMLAttribute(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }
    
    /**
     * Getter for property tag.
     * @return Value of property tag.
     */
    public java.lang.String getTag() {
        return tag;
    }
    
    /**
     * Setter for property tag.
     * @param tag New value of property tag.
     */
    public void setTag(java.lang.String tag) {
        this.tag = tag;
    }
    
    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public java.lang.String getValue() {
        return value;
    }
    
    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(java.lang.String value) {
        this.value = value;
    }
    
    @Override
    public String toString(){
        if (tag==null) 
            throw new NullPointerException("Attribute:name==null\n");
            
        if (value==null) 
            return tag + "=\"\"";        
        
        return tag + "=\""+value.trim()+"\"";
    }
    

    
}
