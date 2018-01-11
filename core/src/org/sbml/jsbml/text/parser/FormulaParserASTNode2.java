/* Generated By:JavaCC: Do not edit this line. FormulaParserASTNode2.java */
/*
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2018 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 * 4. The University of California, San Diego, La Jolla, CA, USA
 * 5. The Babraham Institute, Cambridge, UK
 * 6. The University of Toronto, Toronto, ON, Canada
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.text.parser;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.math.ASTNode2;
import org.sbml.jsbml.math.ASTBoolean;
import org.sbml.jsbml.math.ASTCiNumberNode;
import org.sbml.jsbml.math.ASTCnExponentialNode;
import org.sbml.jsbml.math.ASTCnIntegerNode;
import org.sbml.jsbml.math.ASTCnRealNode;
import org.sbml.jsbml.math.ASTConstantNumber;
import org.sbml.jsbml.math.ASTCSymbolAvogadroNode;
import org.sbml.jsbml.math.ASTCSymbolTimeNode;
import org.sbml.jsbml.math.ASTDivideNode;
import org.sbml.jsbml.math.ASTFunction;
import org.sbml.jsbml.math.ASTLambdaFunctionNode;
import org.sbml.jsbml.math.ASTLogarithmNode;
import org.sbml.jsbml.math.ASTLogicalOperatorNode;
import org.sbml.jsbml.math.ASTMinusNode;
import org.sbml.jsbml.math.ASTPiecewiseFunctionNode;
import org.sbml.jsbml.math.ASTPowerNode;
import org.sbml.jsbml.math.ASTPlusNode;
import org.sbml.jsbml.math.ASTQualifierNode;
import org.sbml.jsbml.math.ASTRelationalOperatorNode;
import org.sbml.jsbml.math.ASTRootNode;
import org.sbml.jsbml.math.ASTTimesNode;
import org.sbml.jsbml.math.ASTUnaryFunctionNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.resources.Resource;
import org.sbml.jsbml.text.parser.IFormulaParser;

/**
 * Class used to parse infix mathematical formula and returns a representation of it as an Abstract Syntax Tree (AST).
 * <p>
 * Support almost the same syntax as defined in <a href="http://sbml.org/Software/libSBML/docs/java-api/org/sbml/libsbml/libsbml.html#parseFormula(java.lang.String)">
 * the LibSBML C-inspired infix notation taken from SBML Level 1 parser</a>.
 *
 * @author Alexander D&ouml;rr
 * @author Nicolas Rodriguez
 * @author Victor Kofia
 * @since 0.8
 */
public class FormulaParserASTNode2 implements IFormulaParser, FormulaParserASTNode2Constants {
  public static Properties stringToType = new Properties();

  static
  {
    String path = "cfg/ASTNodeTokens.xml";
    try
    {
      stringToType.loadFromXML(Resource.class.getResourceAsStream(path));
    }
    catch (InvalidPropertiesFormatException e)
    {
      throw new RuntimeException("Invalid configuration file entries in file " + Resource.class.getResource(path), e);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Could not read configuration file " + Resource.class.getResource(path), e);
    }
  }

  private void checkSize(ArrayList < ASTNode2 > arguments, int i) throws ParseException
  {
    if (arguments.size() > i)
    {
      throw new ParseException();
    }
  }

  private Integer getInteger(ASTNode2 node)
  {
    return node.getType() == Type.INTEGER ? ((ASTCnIntegerNode)node).getInteger() : null;
  }

  final public Token string() throws ParseException {
  Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LOG:
      t = jj_consume_token(LOG);
      break;
    case FACTORIAL:
      t = jj_consume_token(FACTORIAL);
      break;
    case STRING:
      t = jj_consume_token(STRING);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return t;}
    throw new Error("Missing return statement in function");
  }

  final public ASTNode parse() throws ParseException {
  ASTNode2 node = null;
    node = Expression();
    {if (true) return new ASTNode(node);}
    throw new Error("Missing return statement in function");
  }

  final private ASTNode2 Expression() throws ParseException {
  ASTNode2 value = null;
    value = TermLvl1();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 0:
      jj_consume_token(0);
      break;
    case EOL:
      jj_consume_token(EOL);
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final private ASTNode2 TermLvl3() throws ParseException {
  ASTNode2 rightChild = null;
  ASTNode2 leftChild = null;
    leftChild = Primary();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case POWER:
      case ABS:
      case FACTORIAL:
      case NOT:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ABS:
        jj_consume_token(ABS);
      ASTUnaryFunctionNode abs = new ASTUnaryFunctionNode(Type.FUNCTION_ABS);
      abs.addChild(leftChild);
      leftChild = abs;
        break;
      case NOT:
        jj_consume_token(NOT);
        rightChild = Primary();
      ASTUnaryFunctionNode not = new ASTUnaryFunctionNode(Type.LOGICAL_NOT);
      not.addChild(leftChild);
        break;
      case POWER:
        jj_consume_token(POWER);
        rightChild = Primary();
      ASTPowerNode power = new ASTPowerNode();
      power.addChild(leftChild);
      power.addChild(rightChild);
      leftChild = power;
        break;
      case FACTORIAL:
        jj_consume_token(FACTORIAL);
      ASTUnaryFunctionNode factorial = new ASTUnaryFunctionNode(Type.FUNCTION_FACTORIAL);
      factorial.addChild(leftChild);
      leftChild = factorial;
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return leftChild;}
    throw new Error("Missing return statement in function");
  }

  final private ASTNode2 TermLvl2() throws ParseException {
  ASTNode2 node = null;
  ASTNode2 rightChild = null;
  ASTNode2 leftChild = null;
    leftChild = TermLvl3();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case TIMES:
      case DIVIDE:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_2;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case TIMES:
        jj_consume_token(TIMES);
        rightChild = TermLvl3();
      node = new ASTTimesNode();
      ((ASTTimesNode)node).addChild(leftChild);
      ((ASTTimesNode)node).addChild(rightChild);
      leftChild = node;
        break;
      case DIVIDE:
        jj_consume_token(DIVIDE);
        rightChild = TermLvl3();
      node = new ASTDivideNode(leftChild, rightChild);
      leftChild = node;
        break;
      default:
        jj_la1[5] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return leftChild;}
    throw new Error("Missing return statement in function");
  }

  final private ASTNode2 TermLvl1() throws ParseException {
  ASTNode2 node = null;
  ASTNode2 rightChild = null;
  ASTNode2 leftChild = null;
  Token t;
  String s;
  Type type = null;
    leftChild = TermLvl2();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
      case COMPARISON:
      case BOOLEAN_LOGIC:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_3;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        rightChild = TermLvl2();
      node = new ASTPlusNode(leftChild, rightChild);
      leftChild = node;
        break;
      case MINUS:
        jj_consume_token(MINUS);
        rightChild = TermLvl2();
      node = new ASTMinusNode(leftChild, rightChild);
      leftChild = node;
        break;
      case BOOLEAN_LOGIC:
        t = jj_consume_token(BOOLEAN_LOGIC);
        rightChild = TermLvl2();
      node = new ASTLogicalOperatorNode(Type.getTypeFor(t.image));
      ((ASTLogicalOperatorNode)node).addChild(leftChild);
      ((ASTLogicalOperatorNode)node).addChild(rightChild);
      leftChild = node;
        break;
      case COMPARISON:
        t = jj_consume_token(COMPARISON);
        rightChild = TermLvl2();
      node = new ASTRelationalOperatorNode(Type.getTypeFor(t.image));
      ((ASTRelationalOperatorNode)node).addChild(leftChild);
      ((ASTRelationalOperatorNode)node).addChild(rightChild);
      leftChild = node;
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return leftChild;}
    throw new Error("Missing return statement in function");
  }

  final private ASTNode2 Primary() throws ParseException, NumberFormatException {
  Token t;
  double d;
  int i;
  ASTNode2 node = null;
  ASTFunction vector = new ASTFunction();
  ASTNode2 child, furtherChild;
  String s;
  String vals [ ];
  ArrayList < ASTNode2 > arguments = new ArrayList < ASTNode2 > ();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER:
      t = jj_consume_token(INTEGER);
    i = Integer.parseInt(t.image);
    node = new ASTCnIntegerNode(i);
    {if (true) return node;}
      break;
    case NUMBER:
      t = jj_consume_token(NUMBER);
    d = Double.parseDouble(t.image);
    node = new ASTCnRealNode(d);
    {if (true) return node;}
      break;
    case EXPNUMBER:
      t = jj_consume_token(EXPNUMBER);
    s = t.image;
    vals = s.toLowerCase().split("e");
    if (vals [ 1 ].startsWith("+"))
    {
      i = Integer.parseInt(vals [ 1 ].substring(1));
    }
    else
    {
      i = Integer.parseInt(vals [ 1 ]);
    }
    node = new ASTCnExponentialNode();
    ((ASTCnExponentialNode)node).setMantissa(Double.parseDouble(vals [ 0 ]));
    ((ASTCnExponentialNode)node).setExponent(i);
    {if (true) return node;}
      break;
    default:
      jj_la1[12] = jj_gen;
      if (jj_2_1(2)) {
        t = string();
        jj_consume_token(OPEN_PAR);
        child = TermLvl1();
        label_4:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case SLPITTER:
            ;
            break;
          default:
            jj_la1[8] = jj_gen;
            break label_4;
          }
          jj_consume_token(SLPITTER);
          furtherChild = TermLvl1();
      arguments.add(furtherChild);
        }
        jj_consume_token(CLOSE_PAR);
    s = t.image;
    Type type = null;

    if (stringToType.containsKey(s.toLowerCase()))
    {
      type = Type.valueOf(stringToType.getProperty(s.toLowerCase()).toUpperCase());
    }
    if (s.equalsIgnoreCase("abs"))
    {
      checkSize(arguments, 0);
      node = new ASTUnaryFunctionNode(Type.FUNCTION_ABS);
      ((ASTUnaryFunctionNode)node).addChild(child);
    }
    else if (s.equalsIgnoreCase("factorial"))
    {
      checkSize(arguments, 0);
      node = new ASTUnaryFunctionNode(Type.FUNCTION_FACTORIAL);
      ((ASTUnaryFunctionNode)node).addChild(child);
    }
    else if (s.equalsIgnoreCase("pow"))
    {
      checkSize(arguments, 0);
      node = new ASTPowerNode();
      ((ASTPowerNode)node).addChild(child);
    }
    else if (s.equalsIgnoreCase("sqr"))
    {
      checkSize(arguments, 0);
      node = new ASTRootNode(child);
    }
    else if (s.equalsIgnoreCase("sqrt"))
    {
      checkSize(arguments, 0);
      node = new ASTRootNode(child);
    }
    else if (s.equalsIgnoreCase("not"))
    {
      checkSize(arguments, 0);
      node = new ASTLogicalOperatorNode(Type.LOGICAL_NOT);
      ((ASTLogicalOperatorNode)node).addChild(child);
    }
    else if (s.equalsIgnoreCase("log") || s.equalsIgnoreCase("log10"))
    {
      checkSize(arguments, 0);
      node = new ASTLogarithmNode(new ASTCnIntegerNode(10), child);
    }

    else if (s.equalsIgnoreCase("ln"))
    {
      checkSize(arguments, 0);
      node = new ASTLogarithmNode(new ASTConstantNumber(Math.E), child);
    }
    else if (s.equalsIgnoreCase("lambda"))
    {
          node = new ASTLambdaFunctionNode();
          ASTQualifierNode bvar = null;
      bvar = new ASTQualifierNode(Type.QUALIFIER_BVAR);
      bvar.addChild(child);
      ((ASTLambdaFunctionNode)node).addChild(bvar);
    }
    else if (s.equalsIgnoreCase("piecewise"))
    {
      node = new ASTPiecewiseFunctionNode();
      ((ASTPiecewiseFunctionNode)node).addChild(child);
    }
    else
    {
      {if (true) throw new ParseException(MessageFormat.format("{0} is not recognized", s));}
    }
    {if (true) return node;}
      } else if (jj_2_2(4)) {
        t = jj_consume_token(STRING);
                                    ASTFunction selector = new ASTFunction();
                                    selector.setType(Type.FUNCTION_SELECTOR);
                                    selector.addChild(new ASTRelationalOperatorNode(Type.getTypeFor(t.image)));
        label_5:
        while (true) {
          jj_consume_token(LEFT_BRACKET);
          node = TermLvl1();
                                        selector.addChild(node);
          jj_consume_token(RIGHT_BRACKET);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case LEFT_BRACKET:
            ;
            break;
          default:
            jj_la1[9] = jj_gen;
            break label_5;
          }
        }
   {if (true) return selector;}
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case OPEN_PAR:
          jj_consume_token(OPEN_PAR);
          node = TermLvl1();
          jj_consume_token(CLOSE_PAR);
    {if (true) return node;}
          break;
        default:
          jj_la1[13] = jj_gen;
          if (jj_2_3(2)) {
            jj_consume_token(LEFT_BRACES);
            node = TermLvl1();
    ASTFunction selector = new ASTFunction();
    boolean isSelector = false;
        selector.setType(Type.FUNCTION_SELECTOR);
    vector.setType(Type.VECTOR);
    vector.addChild(node);
            label_6:
            while (true) {
              switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
              case SLPITTER:
                ;
                break;
              default:
                jj_la1[10] = jj_gen;
                break label_6;
              }
              jj_consume_token(SLPITTER);
              node = TermLvl1();
      vector.addChild(node);
            }
            jj_consume_token(RIGHT_BRACES);
        selector.addChild(vector);
            label_7:
            while (true) {
              switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
              case LEFT_BRACKET:
                ;
                break;
              default:
                jj_la1[11] = jj_gen;
                break label_7;
              }
              jj_consume_token(LEFT_BRACKET);
              node = TermLvl1();
          isSelector = true;
          selector.addChild(node);
              jj_consume_token(RIGHT_BRACKET);
            }
   {if (true) return isSelector ? selector : vector;}
          } else {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case LEFT_BRACES:
              jj_consume_token(LEFT_BRACES);
    vector.setType(Type.VECTOR);
              jj_consume_token(RIGHT_BRACES);
    {if (true) return vector;}
              break;
            case MINUS:
              jj_consume_token(MINUS);
              node = Primary();
    ASTMinusNode uiMinus = new ASTMinusNode();
    uiMinus.addChild(node);
    {if (true) return uiMinus;}
              break;
            case NOT:
              jj_consume_token(NOT);
              node = TermLvl1();
    ASTLogicalOperatorNode not = new ASTLogicalOperatorNode(Type.LOGICAL_NOT);
    not.addChild(node);
    {if (true) return not;}
              break;
            case LOG:
              jj_consume_token(LOG);
              child = Primary();
    ASTLogarithmNode log = new ASTLogarithmNode(new ASTConstantNumber(Math.E), child);
    {if (true) return log;}
              break;
            case STRING:
              t = jj_consume_token(STRING);
    s = t.image;
    if (s.equalsIgnoreCase("true"))
    {
      node = new ASTBoolean(Type.CONSTANT_TRUE);
    }
    else if (s.equalsIgnoreCase("false"))
    {
      node = new ASTBoolean(Type.CONSTANT_FALSE);
    }
    else if (s.equalsIgnoreCase("pi"))
    {
      node = new ASTConstantNumber(Type.CONSTANT_PI);
    }
    else if (s.equalsIgnoreCase("avogadro"))
    {
      node = new ASTCSymbolAvogadroNode();
    }
    else if (s.equalsIgnoreCase("time"))
    {
      node = new ASTCSymbolTimeNode();
    }
    else if (s.equalsIgnoreCase("exponentiale"))
    {
      node = new ASTConstantNumber(Type.CONSTANT_E);
    }
    else if (s.equalsIgnoreCase("-infinity"))
    {
      node = new ASTCnRealNode(Double.NEGATIVE_INFINITY);
    }
    else if (s.equalsIgnoreCase("infinity"))
    {
      node = new ASTCnRealNode(Double.POSITIVE_INFINITY);
    }
    else if (s.equalsIgnoreCase("e"))
    {
      node = new ASTConstantNumber(Math.E);
    }
    else if (!s.isEmpty())
    {
      node = new ASTCiNumberNode();
      ((ASTCiNumberNode)node).setRefId(s);
    }
    else
    {
      {if (true) throw new ParseException();}
    }
    {if (true) return node;}
              break;
            default:
              jj_la1[14] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
          }
        }
      }
    }
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_3R_16() {
    if (jj_scan_token(MINUS)) return true;
    return false;
  }

  private boolean jj_3R_9() {
    if (jj_scan_token(LEFT_BRACKET)) return true;
    if (jj_3R_10()) return true;
    if (jj_scan_token(RIGHT_BRACKET)) return true;
    return false;
  }

  private boolean jj_3R_24() {
    if (jj_scan_token(NUMBER)) return true;
    return false;
  }

  private boolean jj_3R_12() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_15()) {
    jj_scanpos = xsp;
    if (jj_3R_16()) {
    jj_scanpos = xsp;
    if (jj_3R_17()) {
    jj_scanpos = xsp;
    if (jj_3R_18()) return true;
    }
    }
    }
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_scan_token(PLUS)) return true;
    return false;
  }

  private boolean jj_3R_35() {
    if (jj_scan_token(FACTORIAL)) return true;
    return false;
  }

  private boolean jj_3R_10() {
    if (jj_3R_11()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_12()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_23() {
    if (jj_scan_token(INTEGER)) return true;
    return false;
  }

  private boolean jj_3R_19() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_23()) {
    jj_scanpos = xsp;
    if (jj_3R_24()) {
    jj_scanpos = xsp;
    if (jj_3R_25()) {
    jj_scanpos = xsp;
    if (jj_3_1()) {
    jj_scanpos = xsp;
    if (jj_3_2()) {
    jj_scanpos = xsp;
    if (jj_3R_26()) {
    jj_scanpos = xsp;
    if (jj_3_3()) {
    jj_scanpos = xsp;
    if (jj_3R_27()) {
    jj_scanpos = xsp;
    if (jj_3R_28()) {
    jj_scanpos = xsp;
    if (jj_3R_29()) {
    jj_scanpos = xsp;
    if (jj_3R_30()) {
    jj_scanpos = xsp;
    if (jj_3R_31()) return true;
    }
    }
    }
    }
    }
    }
    }
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3R_31() {
    if (jj_scan_token(STRING)) return true;
    return false;
  }

  private boolean jj_3R_34() {
    if (jj_scan_token(POWER)) return true;
    return false;
  }

  private boolean jj_3R_30() {
    if (jj_scan_token(LOG)) return true;
    if (jj_3R_19()) return true;
    return false;
  }

  private boolean jj_3R_33() {
    if (jj_scan_token(NOT)) return true;
    return false;
  }

  private boolean jj_3_3() {
    if (jj_scan_token(LEFT_BRACES)) return true;
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_32() {
    if (jj_scan_token(ABS)) return true;
    return false;
  }

  private boolean jj_3R_20() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_32()) {
    jj_scanpos = xsp;
    if (jj_3R_33()) {
    jj_scanpos = xsp;
    if (jj_3R_34()) {
    jj_scanpos = xsp;
    if (jj_3R_35()) return true;
    }
    }
    }
    return false;
  }

  private boolean jj_3R_29() {
    if (jj_scan_token(NOT)) return true;
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_26() {
    if (jj_scan_token(OPEN_PAR)) return true;
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_22() {
    if (jj_scan_token(DIVIDE)) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_3R_8()) return true;
    if (jj_scan_token(OPEN_PAR)) return true;
    return false;
  }

  private boolean jj_3R_13() {
    if (jj_3R_19()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_20()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_28() {
    if (jj_scan_token(MINUS)) return true;
    if (jj_3R_19()) return true;
    return false;
  }

  private boolean jj_3R_8() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(27)) {
    jj_scanpos = xsp;
    if (jj_scan_token(14)) {
    jj_scanpos = xsp;
    if (jj_scan_token(28)) return true;
    }
    }
    return false;
  }

  private boolean jj_3R_14() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_21()) {
    jj_scanpos = xsp;
    if (jj_3R_22()) return true;
    }
    return false;
  }

  private boolean jj_3R_21() {
    if (jj_scan_token(TIMES)) return true;
    return false;
  }

  private boolean jj_3R_18() {
    if (jj_scan_token(COMPARISON)) return true;
    return false;
  }

  private boolean jj_3R_11() {
    if (jj_3R_13()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_14()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_27() {
    if (jj_scan_token(LEFT_BRACES)) return true;
    if (jj_scan_token(RIGHT_BRACES)) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_scan_token(STRING)) return true;
    Token xsp;
    if (jj_3R_9()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_9()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_17() {
    if (jj_scan_token(BOOLEAN_LOGIC)) return true;
    return false;
  }

  private boolean jj_3R_25() {
    if (jj_scan_token(EXPNUMBER)) return true;
    return false;
  }

  /** Generated Token Manager. */
  public FormulaParserASTNode2TokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[15];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x18004000,0x80000001,0x4006200,0x4006200,0x1800,0x1800,0x600500,0x600500,0x80,0x80000,0x80,0x80000,0x68,0x8000,0x1c020400,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[3];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public FormulaParserASTNode2(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public FormulaParserASTNode2(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new FormulaParserASTNode2TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public FormulaParserASTNode2(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FormulaParserASTNode2TokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public FormulaParserASTNode2(FormulaParserASTNode2TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(FormulaParserASTNode2TokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[32];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 15; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 32; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 3; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
