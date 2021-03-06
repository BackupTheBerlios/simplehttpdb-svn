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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Simon Thiel
 */
public class XMLTool {
    
    

    
    
    /** Creates a new instance of XMLTool */
    public XMLTool() {
    }
    
    /**
     * creates a XML-open tag like "<tagName>"
     * @param tagName
     * @return
     */
    protected StringBuffer createLeadIn(String tagName){
        StringBuffer result = new StringBuffer();
        
        result.append("\n<");
        result.append(tagName);
        result.append(">");
        
        return result;
    }
    
    /**
     * creates a XML-open tag like "<tagName ka="va" kb="vb" >"
     *
     * @param tagName
     * @param xmlAttributeList
     * @return
     */
    protected StringBuffer createLeadIn(String tagName, java.util.Vector xmlAttributeList){
        
        XMLAttribute attr;
        StringBuffer result = new StringBuffer();
        java.util.Iterator iter = xmlAttributeList.iterator();
        
        result.append("\n<");
        result.append(tagName);
        
        //add attributes
        while (iter.hasNext()){
            attr = (XMLAttribute)iter.next();
            result.append(" ").append(attr.toString());
        }
        
        result.append(">");
        
        return result;
    }
    /**
     * creates a XML-open tag like "<tagName ka="va" >"
     * @param tagName
     * @param xmlAttribute
     * @return
     */
    protected StringBuffer createLeadIn(String tagName, XMLAttribute xmlAttribute){
        
        StringBuffer result = new StringBuffer();
        
        result.append("\n<");
        result.append(tagName);
        
        result.append(" ").append(xmlAttribute.toString());
        
        result.append(">");
        
        return result;
    }
    
    
    /**
     * creates a XML-close tag like "</tagName>"
     * @param tagName
     * @return
     */
    protected StringBuffer createLeadOut(String tagName){
        StringBuffer result = new StringBuffer();
        
        result.append("</");
        result.append(tagName);
        result.append(">");
        
        return result;
    }
    
    /**
     *  extracts all text of a element node
     *
     * @param xmlInput
     * @return
     */
    protected String getText(org.w3c.dom.Node xmlInput){
        String result = "";
        org.w3c.dom.Node childNode = null;
        
        childNode = xmlInput.getFirstChild();
        while(childNode != null){
            if (childNode.getNodeType()==org.w3c.dom.Node.TEXT_NODE){
                result += childNode.getNodeValue();
            }
            childNode = childNode.getNextSibling();
        }
        
        return result.trim();
    }

    /**
     * returns given attribute as boolean
     * @param xmlInput
     * @param tag
     * @return
     */
    protected boolean getAttributeAsBool(org.w3c.dom.Node xmlInput, String tag){
        boolean result = false;
        String value = getAttribute(xmlInput, tag);
        if ((value!=null)&&(!value.equals(""))){


            try {
                result = Boolean.parseBoolean(value);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
                result = false;
            }

        }else{
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Empty value or attribute attribute not found:"+tag+" returning false !");
        }
        
        return result;
    }

    /**
     * returns given attribute as int
     * @param xmlInput
     * @param tag
     * @return
     */
    protected int getAttributeAsInt(org.w3c.dom.Node xmlInput, String tag){
        int result = -1;
        String value = getAttribute(xmlInput, tag);
        if ((value!=null)&&(!value.equals(""))){

            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, 
                        "Exception while parsing number for attribute:"+tag, ex);
                result = -1;
            }
        }else{
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Empty value or attribute attribute not found:"+tag+" returning -1 !");
            
        }
        return result;
    }

    /**
     * returns given attribute as double
     * @param xmlInput
     * @param tag
     * @return
     */
    protected double getAttributeAsDouble(org.w3c.dom.Node xmlInput, String tag){
        double result = -1;
        String value = getAttribute(xmlInput, tag);
        if ((value!=null)&&(!value.equals(""))){

            try {
                result = Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                        "Exception while parsing number for attribute:"+tag, ex);
                result = -1;
            }
        }else{
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Empty value or attribute attribute not found:"+tag+" returning -1 !");
            
        }
        return result;
    }

    /**
     * returns given attribute
     * @param xmlInput
     * @param tag
     * @return
     */
    protected String getAttribute(org.w3c.dom.Node xmlInput, String tag){
        return ((org.w3c.dom.Element)xmlInput).getAttribute(tag);
    }
    
    /**
     *
     * @param xmlInput
     * @return
     */
    protected java.util.Vector<XMLAttribute> getAttributes(org.w3c.dom.Node xmlInput){
        java.util.Vector result = new java.util.Vector();
        org.w3c.dom.NamedNodeMap nodes = ((org.w3c.dom.Element)xmlInput).getAttributes();
        org.w3c.dom.Node aktNode;
        XMLAttribute aktAttr;
        
        for (int i=0;i<nodes.getLength();i++){
            aktNode = nodes.item(i);
            try{
                aktAttr = new XMLAttribute(aktNode.getNodeName(),aktNode.getNodeValue());
            }
            catch (org.w3c.dom.DOMException ex){
                Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                        "DOMException - Attributevalue of "
                        + aktNode.getNodeName()+" set to \"\"", ex);
                
                aktAttr = new XMLAttribute(aktNode.getNodeName(),"");
            }
            result.add(aktAttr);
        }
        
        //((org.w3c.dom.Element)xmlInput).getAttribute(tag);
        
        return result;
    }
    /**
     *  Returns the Text of a Textnode
     *
     * @param xmlInput
     * @return
     */
    protected String getTextNode(org.w3c.dom.Node xmlInput){
        String result = "";
        try{
            result=xmlInput.getNodeValue();
        }
        catch (org.w3c.dom.DOMException ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "DOMException - Textvalue of "
                    + xmlInput.getNodeName()+" set to \"\"", ex);
                                   
        }
        return result;
    }
    
    /**
     * returns the first matching attribute of the given element
     * this is useful if there is only one attribte with the given name
     * which should be the normal case ;-)
     *
     * @param attribs
     * @param attribName searched for attrib name
     * @return the attribute
     */
    public XMLAttribute getFirstAttributeByName(java.util.Vector<XMLAttribute> attribs, String attribName) {
                
        XMLAttribute aktAttrib = null;
        
        if (!attribs.isEmpty()){            
            
            java.util.Iterator<XMLAttribute> iter = attribs.iterator();
            while(iter.hasNext()){
                
                aktAttrib = iter.next();
                if (aktAttrib.getTag().equals(attribName)){
                    return aktAttrib;
                }
            }
        }        
        return null;
    }
    
    /**
     * returns a vector of XMLAttributes with the same tagname
     * (normaly there should only be one - this is usefull when handeling spoiled XML input)
     * @param attribs
     * @param attribName the searchedfor attribute name
     * @return Vector of the found attributes with type XMLAttribute
     */
    public java.util.Vector<XMLAttribute> getAttribsByName(java.util.Vector<XMLAttribute> attribs,  String attribName){
        
        java.util.Iterator<XMLAttribute> iter = attribs.iterator();
        java.util.Vector<XMLAttribute> result = new java.util.Vector();
        XMLAttribute aktAttrib = null;
        
        while(iter.hasNext()){
            aktAttrib = iter.next();
            if (aktAttrib.getTag().equals(attribName)){
                result.add(aktAttrib);
            }
        }
        return result;
    }

   
    /**
     * returns the first child of the searched for tag
     * @param parent
     * @param tagname
     * @return
     */
    protected org.w3c.dom.Node getFirstChildWithTag( org.w3c.dom.Node parent, String tagname ){

        return getNextSiblingWithTag(parent.getFirstChild(), tagname);
    }

    /**
     * returns the next sibling with tag or null if not exists
     * @param sibling
     * @param tagname
     * @return
     */
    protected org.w3c.dom.Node getNextSiblingWithTag( org.w3c.dom.Node sibling, String tagname ){

        if (sibling==null){
            return null;
        }

        org.w3c.dom.Node nextSibling = sibling.getNextSibling() ;

        while(nextSibling != null){
            if (nextSibling.getNodeType()==org.w3c.dom.Node.ELEMENT_NODE){
                if (nextSibling.getNodeName().equals(tagname)){
                     return nextSibling;
                }
            }
            nextSibling = nextSibling.getNextSibling();
        }
        return null;
    }
}
