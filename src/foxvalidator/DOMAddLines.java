/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package foxvalidator;                    

import static foxvalidator.X.getLength;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A sample of Adding lines to the DOM Node. This sample program illustrates:
 * - How to override methods from  DocumentHandler ( XMLDocumentHandler) 
 * - How to turn off ignorable white spaces by overriding ignorableWhiteSpace
 * - How to use the SAX Locator to return row position ( line number of DOM element).
 * - How to attach user defined Objects to Nodes using method setUserData
 * This example relies on the following:
 * - Turning off the "fast" DOM so we can use set expansion to FULL 
 * @version $Id: DOMAddLines.java 447683 2006-09-19 02:36:31Z mrglavas $
 */

public class DOMAddLines extends DOMParser {

   /** Print writer. */
   static private boolean NotIncludeIgnorableWhiteSpaces = false;
   private XMLLocator locator; 
   private String[] mLines;

   public DOMAddLines() {
  	 
      //fNodeExpansion = FULL; // faster than: this.setFeature("http://apache.org/xml/features/defer-node-expansion", false);

      try {                        
         this.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false ); 
      } catch ( org.xml.sax.SAXException e ) {
         System.err.println( "except" + e );
      }
   } // constructor
   
   public void setLines(String[] pLines)
   {
  	 mLines = pLines;
   }

   /* Methods that we override */

   /*   We override startElement callback  from DocumentHandler */

   public void startElement(QName elementQName, XMLAttributes attrList, Augmentations augs) 
    throws XNIException {
      super.startElement(elementQName, attrList, augs);

      Node node = null;
      try {
      	node = (Node) this.getProperty( "http://apache.org/xml/properties/dom/current-element-node" );
      	//System.out.println( "The node = " + node );  TODO JEFF
      }
      catch( org.xml.sax.SAXException ex )
      {
          System.err.println( "except" + ex );;
      }
      if( node != null )
      {
          node.setUserData( "startLine", String.valueOf( locator.getLineNumber() ), null ); // Save location String into node
          node.setUserData("charOffset", String.valueOf( locator.getCharacterOffset() ), null );
      }
   } //startElement 
   
   @Override
  protected void setCharacterData(boolean sawChars) {
	  super.setCharacterData(sawChars);
  }

	protected Attr createAttrNode(QName attrQName)
   {
  	 Attr a = super.createAttrNode(attrQName);
  	 a.setUserData("startLine", String.valueOf( locator.getLineNumber() ), null );
 		int lLineNumber = locator.getLineNumber();
 		
 		int lCharStart = (lLineNumber==-1?0:getLength(mLines,lLineNumber-1)+mLines[lLineNumber-1].indexOf(a.getName()));
 		
 		
  	 a.setUserData("charOffset", String.valueOf( locator.getCharacterOffset() ), null ); //TODO: Get this to be the charOffset for the attribute not for the opening tag.
  	 return a;
   }
   
   protected Element createElementNode(QName elemQName)
   {
  	 Element e = super.createElementNode(elemQName);
  	 e.setUserData("startLine", String.valueOf( locator.getLineNumber() ), null );
  	 e.setUserData("charOffset", String.valueOf( locator.getCharacterOffset() ), null );
  	 return e;
   }

   /* We override startDocument callback from DocumentHandler */

   @Override
  public void characters(XMLString text, Augmentations augs)
      throws XNIException {
	  super.characters(text, augs);
	  fCurrentNode.getLastChild().setUserData( "startLine", String.valueOf( locator.getLineNumber() ), null );
	  fCurrentNode.getLastChild().setUserData("charOffset", String.valueOf( locator.getCharacterOffset() ), null );
  }

	public void startDocument(XMLLocator locator, String encoding, 
                             NamespaceContext namespaceContext, Augmentations augs) throws XNIException {
     super.startDocument(locator, encoding, namespaceContext, augs);
     this.locator = locator;
     Node node = null ;
      try {
        node = (Node) this.getProperty( "http://apache.org/xml/properties/dom/current-element-node" );
      }
     catch( org.xml.sax.SAXException ex )
      {
        System.err.println( "except" + ex );;
      }
     
     if( node != null )
     {
          node.setUserData( "startLine", String.valueOf( locator.getLineNumber() ), null ); // Save location String into node
          node.setUserData("charOffset", String.valueOf( locator.getCharacterOffset() ), null );
     }
  } //startDocument 
   

   public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException
    {
    if(! NotIncludeIgnorableWhiteSpaces )
       super.ignorableWhitespace( text, augs);
    else
       ;// Ignore ignorable white spaces
    }// ignorableWhitespace
   

}
