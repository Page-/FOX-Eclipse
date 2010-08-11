/*--

 Copyright (C) 2001 Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "Java and XML" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact brett@newInstance.com.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed for the
      'Java and XML' book, by Brett McLaughlin (O'Reilly & Associates)."

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 */
package foxvalidator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <b><code>DOMSerializer</code></b> will take a DOM
 *   tree and serialize that tree.
 */
public class DOMSerializer {

    /** Indentation to use */
    private String indent;

    /** Line separator to use */
    private String lineSeparator;

    /**
     * <p> This initializes the needed settings. </p>
     */
    public DOMSerializer() {
        indent = "\t";
        lineSeparator = "\n";
    }

    /**
     * <p> This sets the indentation to use. </p>
     *
     * @param indent the indentation <code>String</code> to use.
     */
    public void setIndent(String indent) {
        this.indent = indent;
    }

    /**
     * <p> This sets the line separator to use. </p>
     *
     * @param lineSeparator line separator to use.
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /**
     * <p> This serializes a DOM tree to the supplied
     *  <code>OutputStream</code>.</p>
     *
     * @param doc DOM tree to serialize.
     * @param out <code>OutputStream</code> to write to.
     */
    public void serialize(Document doc, OutputStream out)
        throws IOException {

        Writer writer = new OutputStreamWriter(out);
        serialize(doc, writer);
    }

    /**
     * <p> This serializes a DOM tree to the supplied
     *  <code>OutputStream</code>.</p>
     *
     * @param doc DOM tree to serialize.
     * @param file <code>File</code> to write to.
     */
    public void serialize(Document doc, File file)
        throws IOException {

        Writer writer = new FileWriter(file);
        serialize(doc, writer);
    }

    /**
     * <p> This serializes a DOM tree to the supplied
     *  <code>OutputStream</code>.</p>
     *
     * @param doc DOM tree to serialize.
     * @param writer <code>Writer</code> to write to.
     */
    public void serialize(Document doc, Writer writer)
        throws IOException {

        // Start serialization recursion with no indenting
        serializeNode(doc, writer, "");
        writer.flush();
    }

    public String serializeNode(Node node)
    {
    	StringWriter sw = new StringWriter();
    	try {
	      this.serializeNode(node,sw,"");
      } catch (IOException e) {
	      e.printStackTrace();
      }
    	return sw.toString().replaceAll(this.lineSeparator+this.lineSeparator, this.lineSeparator);
    }

    /**
     * <p> This will serialize a DOM <code>Node</code> to
     *   the supplied <code>Writer</code>. </p>
     *
     * @param node DOM <code>Node</code> to serialize.
     * @param writer <code>Writer</code> to write to.
     * @param indentLevel current indentation.
     */
    public void serializeNode(Node node, Writer writer,
                              String indentLevel)
        throws IOException {

        // Determine action based on node type
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                writer.write("<?xml version=\"1.0\"?>");
                writer.write(lineSeparator);

                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                        serializeNode(nodes.item(i), writer, "");
                    }
                }
                /*
                Document doc = (Document)node;
                serializeNode(doc.getDocumentElement(), writer, "");
                */
                break;

            case Node.ELEMENT_NODE:
                String name = node.getNodeName();
                writer.write(indentLevel + "<" + name);
                NamedNodeMap attributes = node.getAttributes();
                for (int i=0; i<attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    writer.write(" " + current.getNodeName() +
                                 "=\"" + current.getNodeValue() +
                                 "\"");
                }

                // recurse on each child
                NodeList children = node.getChildNodes();
                if (children == null || children.getLength() == 0)
                	writer.write("/>");
                else {
                	writer.write(">");
                    for (int i=0; i<children.getLength(); i++) {
	                      if (children.item(i) != null && children.item(i).getNodeType() == Node.ELEMENT_NODE
	                      		&& !(i>0 && children.item(i-1) != null && children.item(i-1).getNodeType() == Node.ELEMENT_NODE)) {
	
	                          writer.write(lineSeparator);
	                      }
                        serializeNode(children.item(i), writer, indentLevel + indent);
                    }
                    String s = writer.toString();
                    if(s.endsWith(lineSeparator))
                    	writer.write(indentLevel);
                    writer.write("</" + name + ">");
                }
                writer.write(lineSeparator);
                break;

            case Node.TEXT_NODE:
                writer.write(node.getNodeValue().trim());
                break;

            case Node.CDATA_SECTION_NODE:
                writer.write("<![CDATA[" +
                             node.getNodeValue() + "]]>");
                break;

            case Node.COMMENT_NODE:
                writer.write(indentLevel + "<!-- " +
                             node.getNodeValue() + " -->");
                writer.write(lineSeparator);
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                writer.write("<?" + node.getNodeName() +
                             " " + node.getNodeValue() +
                             "?>");
                writer.write(lineSeparator);
                break;

            case Node.ENTITY_REFERENCE_NODE:
                writer.write("&" + node.getNodeName() + ";");
                break;

            case Node.DOCUMENT_TYPE_NODE:
                DocumentType docType = (DocumentType)node;
                writer.write("<!DOCTYPE " + docType.getName());
                if (docType.getPublicId() != null)  {
                    System.out.print(" PUBLIC \"" +
                        docType.getPublicId() + "\" ");
                } else {
                    writer.write(" SYSTEM ");
                }
                writer.write("\"" + docType.getSystemId() + "\">");
                writer.write(lineSeparator);
                break;

            case Node.ATTRIBUTE_NODE:
            		if(node.getPrefix()!=null)
            		{
	            			writer.write(node.getPrefix());
	            			writer.write(":");
            		}
	          		writer.write(node.getLocalName());
	          		writer.write("=\"");
	          		writer.write(node.getNodeValue());
	          		writer.write("\"");
	              break;
        }
    }
}