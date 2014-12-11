/*
 * $Id$
 * $URL$
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2014 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 * 4. The University of California, San Diego, La Jolla, CA, USA
 * 5. The Babraham Institute, Cambridge, UK
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.xml;

import static org.sbml.jsbml.JSBML.OPERATION_FAILED;
import static org.sbml.jsbml.JSBML.OPERATION_SUCCESS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.xml.parsers.XMLNodeWriter;
import org.sbml.jsbml.xml.stax.SBMLReader;

/**
 * Representation of a node in an XML document tree.
 * <p>
 * Beginning with version 1.0, JSBML implements an XML abstraction layer, like
 * LibSBML. The basic data object in the XML abstraction is a <em>node</em>,
 * represented by {@link XMLNode}.
 * <p>
 * An {@link XMLNode} can contain any number of children. Each child is another
 * {@link XMLNode}, thereby forming a tree. The methods
 * {@link XMLNode#getChildCount()} and {@link XMLNode#getChildAt(int)} can be
 * used to access the tree structure starting from a given node.
 * <p>
 * <p>
 * Each {@link XMLNode} is subclassed from {@link XMLToken}, and thus has the
 * same methods available as {@link XMLToken}. These methods include
 * {@link XMLToken#getNamespaces()}, {@link XMLToken#getPrefix()},
 * {@link XMLToken#getName()}, {@link XMLToken#getURI()}, and
 * {@link XMLToken#getAttributes()}.
 * <p>
 * <h2>Conversion between an XML {@link String} and an {@link XMLNode}</h2>
 * <p>
 * JSBML provides the following utility functions for converting an XML
 * {@link String} (e.g., {@code <annotation>...</annotation>}) to/from an
 * {@link XMLNode} object.
 * <ul>
 * <li>{@link XMLNode#toXMLString()} returns a {@link String} representation of
 * the {@link XMLNode} object.</li>
 * <li>{@link XMLNode#convertXMLNodeToString(XMLNode)} (static function) returns a
 * {@link String} representation of the given {@link XMLNode} object.</li>
 * <li>{@link XMLNode#convertStringToXMLNode(String)} (static function) returns an
 * {@link XMLNode} object converted from the given XML {@link String}.</li>
 * </ul>
 * <p>
 * The returned {@link XMLNode} object by
 * {@link XMLNode#convertStringToXMLNode(String)} is a dummy root (container)
 * {@link XMLNode} if the given XML {@link String} has two or more top-level
 * elements (e.g., {@code <p>...</p><p>...</p>}). In the
 * dummy root node, each top-level element in the given XML {@link String} is
 * contained as a child {@link XMLNode}. {@link XMLToken#isEOF()} can be used to
 * identify if the returned {@link XMLNode} object is a dummy node or not. Here
 * is an example: <div class='fragment'>
 * 
 * Checks if the returned {@link XMLNode} object by
 * {@link XMLNode#convertStringToXMLNode(String)} is a dummy root node:
 * <pre class="brush:java">
 * 
 * String str = "...";
 * XMLNode xn = XMLNode.convertStringToXMLNode(str);
 * if (xn == null) {
 *   // returned value is null (error)
 *   // ...
 * } else if (xn.isEOF()) {
 *   // root node is a dummy node
 *   for (int i = 0; i $lt; xn.getChildCount(); i++) {
 *     // access to each child node of the dummy node.
 *     XMLNode xnChild = xn.getChild(i);
 *     // ...
 *   }
 * } else {
 *   // root node is NOT a dummy node
 *   // ...
 * }
 * </pre>
 * </div>
 * <p>
 * 
 * @author Nicolas Rodriguez
 * @since 0.8
 * @version $Rev$
 */
public class XMLNode extends XMLToken {

  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = -7699595383368237593L;

  /**
   * This reader is used to parse all notes-Strings.
   * It takes a lot of time to re-initialize the {@link SBMLReader}
   * every time notes are being set or appended, thus, this instance
   * is static and should be used to parse all notes.
   */
  private static SBMLReader notesReader = new SBMLReader();


  /**
   * Returns an {@link XMLNode} which is derived from a {@link String}
   * containing XML content.
   * <p>
   * The XML namespace must be defined using argument {@code xmlns} if the
   * corresponding XML namespace attribute is not part of the {@link String} of
   * the first argument.
   * 
   * @param xmlstr
   *        {@link String} to be converted to a XML node.
   * @jsbml.note The caller owns the returned {@link XMLNode} and is responsible
   *             for deleting it.
   *             The returned {@link XMLNode} object is a dummy root (container)
   *             {@link XMLNode} if the top-level element in the given XML
   *             {@link String} is NOT {@code html}, {@code body},
   *             {@code annotation}, {@code notes}.
   *             In the dummy root node, each top-level element in the given XML
   *             string is contained as a child {@link XMLNode}.
   *             {@link XMLToken#isEOF()} can be used to identify if the
   *             returned {@link XMLNode} object is a dummy node.
   * @return a {@link XMLNode} which is converted from string {@code xmlstr}.
   *         {@code null} is returned if the conversion failed.
   * @throws XMLStreamException
   */
  public static XMLNode convertStringToXMLNode(String xmlstr) throws XMLStreamException {
    /* Initializing the SBMLReader again and again
     * for every time we append notes takes a lot of time
     * (especially calling the initializePackageParsers()
     * method) => use a static instance here */
    return notesReader.readNotes(xmlstr);
  }

  /**
   * Returns an {@link XMLNode} which is derived from a string containing XML
   * content.
   * <p>
   * The XML namespace must be defined using argument {@code xmlns} if the
   * corresponding XML namespace attribute is not part of the string of the
   * first argument.
   * <p>
   * @param xmlstr string to be converted to a XML node.
   * @param xmlns {@link XMLNamespaces} the namespaces to set (default value is {@code null}). This argument is ignored at the moment.
   * <p>
   * @jsbml.note The caller owns the returned {@link XMLNode} and is responsible for deleting it.
   * The returned {@link XMLNode} object is a dummy root (container) {@link XMLNode} if the top-level
   * element in the given XML string is NOT {@code html}, {@code body}, {@code annotation}, {@code notes}.
   * In the dummy root node, each top-level element in the given XML string is contained
   * as a child {@link XMLNode}. XMLToken.isEOF() can be used to identify if the returned {@link XMLNode}
   * object is a dummy node.
   * <p>
   * @return a {@link XMLNode} which is converted from string {@code xmlstr}. {@code null} is returned
   * if the conversion failed.
   * 
   */
  public static XMLNode convertStringToXMLNode(String xmlstr, XMLNamespaces xmlns) {

    // TODO: check how to use the xmlns arguments inside the SBMLReader.readNotes.

    try {
      return notesReader.readNotes(xmlstr);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Returns a string representation of a given {@link XMLNode}.
   * <p>
   * @param node the {@link XMLNode} to be represented as a string
   * <p>
   * @return a string-form representation of {@code node}
   * @throws XMLStreamException
   */
  public static String convertXMLNodeToString(XMLNode node) throws XMLStreamException {
    return node.toXMLString();
  }

  /**
   * 
   */
  private List<XMLNode> childrenElements = null;

  /**
   * Creates a new empty {@link XMLNode} with no children.
   */
  public XMLNode() {
    super();
  }

  /**
   * Creates a text {@link XMLNode}.
   * <p>
   * @param chars a string, the text to be added to the {@link XMLToken}
   * 
   */
  public XMLNode(String chars) {
    super(chars);
  }

  /**
   * Creates a text {@link XMLNode}.
   * <p>
   * @param chars a string, the text to be added to the {@link XMLToken}
   * @param line a long integer, the line number (default = 0).
   * 
   */
  public XMLNode(String chars, long line) {
    super(chars, line, 0);
  }

  /**
   * Creates a text {@link XMLNode}.
   * <p>
   * @param chars a string, the text to be added to the {@link XMLToken}
   * @param line a long integer, the line number (default = 0).
   * @param column a long integer, the column number (default = 0).
   * 
   */
  public XMLNode(String chars, long line, long column) {
    super(chars, line, column);
  }

  /**
   * Creates a copy of this {@link XMLNode}.
   * <p>
   * @param orig the {@link XMLNode} instance to copy.
   */
  public XMLNode(XMLNode orig) {
    super(orig);

    if (orig.childrenElements != null && orig.childrenElements.size() > 0) {
      childrenElements = new ArrayList<XMLNode>();
      for (XMLNode origchildren : orig.childrenElements) {
        childrenElements.add(origchildren.clone());
      }
    }
    // TODO - clone our jsbml user objects for XMLNode

  }

  /**
   * Creates a new {@link XMLNode} by copying token.
   * <p>
   * @param orig {@link XMLToken} to be copied to {@link XMLNode}
   */
  public XMLNode(XMLToken orig) {
    if (orig.triple != null) {
      triple = orig.triple.clone();
    }
    if (orig.attributes != null) {
      attributes = orig.attributes.clone();
    }
    if (orig.namespaces != null) {
      namespaces = orig.namespaces.clone();
    }
    line = orig.line;
    column = orig.column;
    if (orig.characters != null) {
      characters = new StringBuilder();
      characters.append(orig.getCharacters());
    }
    isText = orig.isText;
    isStartElement = orig.isStartElement;
    isEndElement = orig.isEndElement;
    isEOF = orig.isEOF;
  }

  /**
   * Creates an end element {@link XMLNode}.
   * <p>
   * @param triple {@link XMLTriple}.
   * 
   */
  public XMLNode(XMLTriple triple) {
    super(triple);
  }

  /**
   * Creates an end element {@link XMLNode}.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param line a long integer, the line number (default = 0).
   * 
   */
  public XMLNode(XMLTriple triple, long line) {
    super(triple, line);
  }

  /**
   * Creates an end element {@link XMLNode}.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param line a long integer, the line number (default = 0).
   * @param column a long integer, the column number (default = 0).
   * 
   */
  public XMLNode(XMLTriple triple, long line, long column) {
    super(triple, line, column);
  }

  /**
   * Creates a start element {@link XMLNode} with the given set of attributes.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param attributes {@link XMLAttributes}, the attributes to set.
   * 
   */
  public XMLNode(XMLTriple triple, XMLAttributes attributes) {
    super(triple, attributes);
  }

  /**
   * Creates a start element {@link XMLNode} with the given set of attributes.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param attributes {@link XMLAttributes}, the attributes to set.
   * @param line a long integer, the line number (default = 0).
   * 
   */
  public XMLNode(XMLTriple triple, XMLAttributes attributes, long line) {
    super(triple, attributes, line, 0);
  }

  /**
   * Creates a start element {@link XMLNode} with the given set of attributes.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param attributes {@link XMLAttributes}, the attributes to set.
   * @param line a long integer, the line number (default = 0).
   * @param column a long integer, the column number (default = 0).
   * 
   */
  public XMLNode(XMLTriple triple, XMLAttributes attributes, long line, long column) {
    super(triple, attributes, line, column);
  }

  /**
   * Creates a new start element {@link XMLNode} with the given set of attributes and
   * namespace declarations.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param attributes {@link XMLAttributes}, the attributes to set.
   * @param namespaces {@link XMLNamespaces}, the namespaces to set.
   * 
   */
  public XMLNode(XMLTriple triple, XMLAttributes attributes, XMLNamespaces namespaces) {
    super(triple, attributes, namespaces);
  }

  /**
   * Creates a new start element {@link XMLNode} with the given set of attributes and
   * namespace declarations.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param attributes {@link XMLAttributes}, the attributes to set.
   * @param namespaces {@link XMLNamespaces}, the namespaces to set.
   * @param line a long integer, the line number (default = 0).
   * 
   */
  public XMLNode(XMLTriple triple, XMLAttributes attributes,
    XMLNamespaces namespaces, long line) {
    super(triple, attributes, namespaces, line);
  }

  /**
   * Creates a new start element {@link XMLNode} with the given set of attributes and
   * namespace declarations.
   * <p>
   * @param triple {@link XMLTriple}.
   * @param attributes {@link XMLAttributes}, the attributes to set.
   * @param namespaces {@link XMLNamespaces}, the namespaces to set.
   * @param line a long integer, the line number (default = 0).
   * @param column a long integer, the column number (default = 0).
   */
  public XMLNode(XMLTriple triple, XMLAttributes attributes, XMLNamespaces namespaces, long line, long column) {
    super(triple, attributes, namespaces, line, column);
  }

  /**
   * Adds a child to this {@link XMLNode}.
   * <p>
   * The given {@code node} is added at the end of the list of children.
   * <p>
   * @param node the {@link XMLNode} to be added as child.
   * <p>
   * @return integer value indicating success/failure of the
   * function.   The possible values
   * returned by this function are:
   * <ul>
   * <li> {@link JSBML#OPERATION_SUCCESS}
   * <li> {@link JSBML#OPERATION_FAILED}
   * </ul>
   * <p>
   * @jsbml.note The given node is added at the end of the children list.
   */
  public int addChild(XMLNode node) {

    if (node == null) {
      return OPERATION_FAILED;
    }

    if (isEnd()) {
      unsetEnd();
    }
    // TODO: there are more tests in libsbml XMLNode.cpp, check if we need them, like isEOF()

    if (childrenElements == null) {
      childrenElements = new ArrayList<XMLNode>();
    }

    childrenElements.add(node);
    node.fireNodeAddedEvent();
    node.parent = this;

    return OPERATION_SUCCESS;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.xml.XMLToken#clone()
   */
  @Override
  public XMLNode clone() {
    return new XMLNode(this);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeNode#getAllowsChildren()
   */
  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeNode#getChildAt(int)
   */
  @Override
  public XMLNode getChildAt(int childIndex) {
    if (childrenElements == null) {
      throw new IndexOutOfBoundsException(Integer.toString(childIndex));
    }
    return childrenElements.get(childIndex);
  }

  /**
   * Returns the child {@link XMLNode} at index childIndex.
   * 
   * @param childIndex the index.
   * @return the child {@link XMLNode} at index childIndex.
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
   * @libsbml.deprecated you could use {@link #getChildAt(int)}
   */
  public XMLNode getChild(int childIndex) {
    return getChildAt(childIndex);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeNode#getChildCount()
   */
  @Override
  public int getChildCount() {
    return childrenElements != null ? childrenElements.size() : 0;
  }

  /**
   * Inserts a node as the {@code n}th child of this
   * {@link XMLNode}.
   * <p>
   * If the given index {@code n} is out of range for this {@link XMLNode} instance,
   * the {@code node} is added at the end of the list of children.  Even in
   * that situation, this method does not throw an error.
   * <p>
   * @param n an integer, the index at which the given node is inserted
   * @param node an {@link XMLNode} to be inserted as {@code n}th child.
   * <p>
   * @return a reference to the newly-inserted child {@code node}
   */
  public XMLNode insertChild(int n, XMLNode node) {
    if (node == null) {
      return node;
    }

    if (isEnd()) {
      unsetEnd();
    }
    if ((n > getChildCount()) || (n < 0)) {
      //			childrenElements.add(node);
      throw new IndexOutOfBoundsException(Integer.toString(n));
    } else {
      if (childrenElements == null) {
        childrenElements = new ArrayList<XMLNode>();
      }
      childrenElements.add(n, node);
      node.fireNodeAddedEvent();
    }
    node.parent = this;

    return node;
  }

  /**
   * Removes the {@code n}th child of this {@link XMLNode} and returns the
   * removed node.
   * <p>
   * It is important to keep in mind that a given {@link XMLNode} may have more
   * than one child.  Calling this method erases all existing references to
   * child nodes <em>after</em> the given position {@code n}.  If the index {@code n} is
   * greater than the number of child nodes in this {@link XMLNode}, this method
   * takes no action (and returns {@code null}).
   * <p>
   * @param n an integer, the index of the node to be removed
   * <p>
   * @return the removed child, or {@code null} if {@code n} is greater than the number
   * of children in this node
   * <p>
   * @jsbml.note The caller owns the returned node and is responsible for deleting it.
   */
  public XMLNode removeChild(int n) {
    if ((n < 0) || (getChildCount() < n) || (childrenElements == null)) {
      return null;
    }
    XMLNode oldNode =  childrenElements.remove(n);
    oldNode.fireNodeRemovedEvent();
    return oldNode;
  }

  /**
   * Removes all children from this node.
   * @return integer value indicating success/failure of the
   * function.   The possible values
   * returned by this function are:
   * <ul>
   * <li> {@link JSBML#OPERATION_SUCCESS}
   * </ul>
   */
  public int removeChildren() {
    if (childrenElements != null) {
      List<XMLNode> removedChildren = childrenElements;
      childrenElements.clear();
      for (XMLNode child : removedChildren) {
        child.fireNodeRemovedEvent();
      }
    }

    return OPERATION_SUCCESS;
  }

  /**
   * Returns a string representation of this {@link XMLNode}.
   * <p>
   * @return a string derived from this {@link XMLNode}.
   * @throws XMLStreamException if an error occurs while writing the {@link XMLNode} to {@link String}.
   */
  public String toXMLString() throws XMLStreamException {
    if (isText() && (getChildCount() == 0)) {
      return getCharacters();
    }
    return XMLNodeWriter.toXML(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [childrenElements=");
    builder.append(childrenElements);
    builder.append(", attributes=");
    builder.append(attributes);
    builder.append(", characters=");
    builder.append(characters);
    builder.append(", column=");
    builder.append(column);
    builder.append(", isEndElement=");
    builder.append(isEndElement);
    builder.append(", isEOF=");
    builder.append(isEOF);
    builder.append(", isStartElement=");
    builder.append(isStartElement);
    builder.append(", isText=");
    builder.append(isText);
    builder.append(", line=");
    builder.append(line);
    builder.append(", namespaces=");
    builder.append(namespaces);
    builder.append(", triple=");
    builder.append(triple);
    builder.append("]");
    return builder.toString();
  }

  /**
   * Removes the first occurrence of the specified element from this list.
   * <p>
   * If this list does not contain the element, it is unchanged. More formally,
   * removes the element with the lowest index i such that
   * {@code (o == null ? get(i) == null : o.equals(get(i)))} (if such an
   * element exists). Returns {@code true} if this list contained the specified
   * element (or equivalently, if this list changed as a result of the call).
   * 
   * @param xmlNode
   * @return {@code true} if the {@link XMLNode} was found and removed.
   */
  public boolean removeChild(XMLNode xmlNode) {
    if (childrenElements == null) {
      return false;
    }

    return childrenElements.remove(xmlNode);
  }

  /**
   * Gets the first direct child element of this {@link XMLNode} with the given local name and namespace.
   * 
   * <p>If {@code null} or an empty {@link String} is given for either the elementName or
   * the elementURI, it is interpreted as any name or namespace. This is the same as passing the
   * special value "*".
   * 
   * @param elementName - The local name of the elements to match on. The special value "*" matches all local names.
   * @param elementURI - The namespace URI of the elements to match on. The special value "*" matches all namespaces.
   * @return the first direct child of this {@link XMLNode} with the given local name and namespace.
   */
  public XMLNode getChildElement(String elementName, String elementURI)
  {
    // checking the inputs
    if (elementName == null || elementName.trim().length() == 0) {
      elementName = "*";
    }
    if (elementURI == null || elementURI.trim().length() == 0) {
      elementURI = "*";
    }

    for (int i = 0; i < getChildCount(); i++) {
      XMLNode child = getChildAt(i);

      if (child.isElement()
          && (child.getName().equals(elementName) || elementName.equals("*"))
          && (elementURI.equals("*") || elementURI.equals(child.getURI())))
      {
        return child;
      }

    }

    return null;
  }

  /**
   * Gets all the direct children of this {@link XMLNode} with the given local
   * name and namespace.
   * <p>
   * If {@code null} or an empty {@link String} is given for either the
   * elementName or the elementURI, it is interpreted as any name or namespace.
   * This is the same as passing the special value "*".
   * 
   * @param elementName
   *        - The local name of the elements to match on. The special value "*"
   *        matches all local names.
   * @param elementURI
   *        - The namespace URI of the elements to match on. The special value
   *        "*" matches all namespaces.
   * @return all the direct children of this {@link XMLNode} with the given
   *         local name and namespace.
   */
  public List<XMLNode> getChildElements(String elementName, String elementURI)
  {
    // checking the inputs
    if ((elementName == null) || (elementName.trim().length() == 0)) {
      elementName = "*";
    }
    if ((elementURI == null) || (elementURI.trim().length() == 0)) {
      elementURI = "*";
    }

    List<XMLNode> foundNodes = new ArrayList<XMLNode>();

    for (int i = 0; i < getChildCount(); i++) {
      XMLNode child = getChildAt(i);

      if (child.isElement()
          && (child.getName().equals(elementName) || elementName.equals("*"))
          && (elementURI.equals(child.getURI()) || elementURI.equals("*")))
      {
        foundNodes.add(child);
      }
    }

    return foundNodes;
  }

}
