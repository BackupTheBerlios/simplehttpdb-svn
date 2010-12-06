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
import org.xml.sax.SAXException;

/**
 *
 * @author Simon Thiel
 */
public class XMLAccess {
    
    private static final XMLAccess singleton = new XMLAccess();
    
    
    private XMLAccess(){
    }
    
    /**
     * initates a DOM pars of the given XML-String and returns the XML-Document
     *
     *
     * @param xmlInput
     * @return
     * @throws SAXException
     */
    public org.w3c.dom.Document parseXML(String xmlInput) throws SAXException{
        
        
        javax.xml.parsers.DocumentBuilderFactory factory
        = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document document = null;
        
        try {
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            java.io.InputStream inst =  new java.io.ByteArrayInputStream(xmlInput.getBytes());
            document = builder.parse(inst);
            
            
        }
        catch (javax.xml.parsers.ParserConfigurationException ex) {
            // Parser with specified options can't be built
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            
        }
        catch(java.io.IOException ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return document;
    }
    
  
 
    

    
    /**
     * returns the only instance of this class (singleton)
     * @return
     */
    public static XMLAccess getInstance(){
        return singleton;
    }
    
}
