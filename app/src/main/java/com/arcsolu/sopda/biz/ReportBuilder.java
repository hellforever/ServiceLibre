package com.arcsolu.sopda.biz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;

import com.arcsolu.sopda.biz.NodeBase.NodeType;
import com.arcsolu.sopda.biz.ValueNode.AlignType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * basic type for all the script word
 * @author user
 *
 */
class NodeBase extends Object {
	public enum NodeType {
		REP, VALUE, FOREACH, IF, CTRL, L, R, TRUE, FALSE, STMT, FUNC
	};

	public NodeType Type;
	public List<NodeBase> Nodes;
	public NodeBase parent;

	public NodeBase() {
		Nodes = new ArrayList<NodeBase>();
		parent = null;
	}
}

/**
 * reserve for control keyword
 * @author user
 *
 */
class CtrlNode extends NodeBase {
	String Code;

	public CtrlNode() {
		Type = NodeType.CTRL;
		Code = "";
	}
}

/**
 * reserve for root keyword
 * @author user
 *
 */
class RepNode extends NodeBase {
	public RepNode() {
		Type = NodeType.REP;
	}
}

/**
 * reserve for attribute keyword
 * @author user
 *
 */
class ValueNode extends NodeBase {
	// public
	enum ValueType {
		TEXT, NUMBER, DATE, TIME
	};

	enum AlignType {
		LEFT, CENTER, RIGHT
	};

	String Source;
	Object Value;
	String Format;
	ValueType vType;
	AlignType aType;
	int Length;

	ValueNode() {
		Type = NodeType.VALUE;
		vType = ValueType.TEXT;
		aType = AlignType.LEFT;
		Length = -1;
		Source = null;
	}
}

/**
 * reserve for Iteration keyword
 * @author user
 *
 */
class ForNode extends NodeBase {
	String Source;
	Object Value;

	ForNode() {
		Type = NodeType.FOREACH;
		Source = null;
	}

}

/**
 * reserve for function keyword
 * @author user
 *
 */
class FuncNode extends ValueNode {
	String MethodName;

	FuncNode() {
		Type = NodeType.FUNC;
	}
}

/**
 * reserve for if condition keyword
 * @author user
 *
 */
class StmtNode extends NodeBase {
	StmtNode() {
		Type = NodeType.STMT;
	}
}

/**
 * reserve for if condition keyword
 * @author user
 *
 */
class IfNode extends NodeBase {
	enum OperType {
		BT, LT, EQ, NE, BE, LE
	}

	OperType oper;
	StmtNode LeftOper;
	StmtNode RightOper;
	NodeBase TrueStatement;
	NodeBase FalseStatement;

	/**
	 * reserve for If keyword
	 */
	public IfNode() {
		Type = NodeType.IF;
	}

}

@SuppressLint("DefaultLocale")
public class ReportBuilder extends DefaultHandler {
	private SAXParser saxParser;
	private DefaultHandler saxHandler;
	private HashMap<String, String> ctrlMap;
	private HashMap<String, Object> paramMap;
	private ByteArrayOutputStream buff;
	private List<NodeBase> repList;
	private NodeBase currentNode;
	private String errorTags;

	/**
	 * init and fill the control Map.
	 */
	public ReportBuilder() {
		errorTags = "";
		repList = new ArrayList<NodeBase>();
		ctrlMap = new HashMap<String, String>();
		paramMap = new HashMap<String, Object>();
		buff = new ByteArrayOutputStream();
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxHandler = this;
		ctrlMap.put("INIT", "27:64");
		ctrlMap.put("CR", "10");
		ctrlMap.put("LR", "13:10");
		ctrlMap.put("CUT", "29:86:66:1");
		ctrlMap.put("RIGHT", "27:97:2");
		ctrlMap.put("LEFT", "27:97:0");
		ctrlMap.put("CENTER", "27:97:1");
		ctrlMap.put("FONT_A", "27:33:0");
		ctrlMap.put("FONT_A_W", "27:33:32");
		ctrlMap.put("FONT_A_H", "27:33:16");
		ctrlMap.put("FONT_A_WH", "27:33:48");
		ctrlMap.put("FONT_B", "27:33:1");
		ctrlMap.put("FONT_B_W", "27:33:33");
		ctrlMap.put("FONT_B_H", "27:33:17");
		ctrlMap.put("FONT_B_WH", "27:33:49");
		try {
			saxParser = saxFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * put param into sax as source.
	 * @param name
	 * @param o
	 */
	public void AddParam(String name, Object o) {
		this.paramMap.put(name.toUpperCase(), o);
	}

	/**
	 * clear up the map parameter, and stream buff
	 */
	public void InitParam() {
		errorTags = "";
		buff.reset();
		this.paramMap.clear();
	}

	/**
	 * string to byteArray
	 * @param code
	 * @return
	 */
	private byte[] StringToHex(String code) {

		String cds[] = code.trim().split(":");
		byte[] rst = new byte[cds.length];
		for (int i = 0; i < cds.length; i++) {
			rst[i] = (byte) Integer.parseInt(cds[i]);
		}
		return rst;
	}

	/**
	 * sax components for save start time
	 * @author user
	 *
	 * @throws SAXException
	 */
	@SuppressLint("DefaultLocale")
	public void startDocument() throws SAXException {
		// buff.reset();
		// try {
		// buff.write(this.StringToHex(ctrlMap.get("INIT")));
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	// public void endDocument() throws SAXException{
	//
	// }
	/**
	 * @author user
	 * sax component for start analyse
	 * @param uri
	 * @param localName
	 * @param qualifiedName
	 * @param atts
	 * @throws SAXException
	 */
	@SuppressLint("DefaultLocale")
	public void startElement(String uri, String localName,
			String qualifiedName, Attributes atts) throws SAXException {
		NodeBase n = null;
		NodeType p = null;
		try {
			p = NodeType.valueOf(qualifiedName.toUpperCase());
		} catch (IllegalArgumentException e) {
			// try {
			// buff.write(("\nTag Error!!"+qualifiedName.toUpperCase()).getBytes());
			// return;
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			errorTags += "\nTag Error!!" + qualifiedName.toUpperCase();
			return;
		}
		switch (p) {
		case FUNC:
			n = new FuncNode();
			setValueNodeAttrs((ValueNode) n, atts);
			((FuncNode) n).MethodName = atts.getValue("method");
			((FuncNode) n).vType = ValueNode.ValueType.valueOf("number"
					.toUpperCase());
			break;
		case REP:
			n = new RepNode();
			repList.add(n);
			currentNode = n;
			return;
		case VALUE:
			n = new ValueNode();
			setValueNodeAttrs((ValueNode) n, atts);

			break;
		case IF:
			n = new IfNode();
			((IfNode) n).oper = IfNode.OperType.valueOf(atts.getValue("oper")
					.toUpperCase());
			break;
		case L:
			n = new StmtNode();
			if (currentNode instanceof IfNode) {
				((IfNode) currentNode).LeftOper = (StmtNode) n;

			}
			break;
		case R:
			n = new StmtNode();
			if (currentNode instanceof IfNode) {
				((IfNode) currentNode).RightOper = (StmtNode) n;

			}
			break;
		case TRUE:
			n = new StmtNode();
			if (currentNode instanceof IfNode) {
				((IfNode) currentNode).TrueStatement = n;

			}
			break;
		case FALSE:
			n = new StmtNode();
			if (currentNode instanceof IfNode) {
				((IfNode) currentNode).FalseStatement = n;

			}
			break;
		case FOREACH:
			n = new ForNode();
			((ForNode) n).Source = atts.getValue("source");

			break;
		case CTRL:
			n = new CtrlNode();
			((CtrlNode) n).Code = atts.getValue("code") == null ? "" : atts
					.getValue("code");
			break;
		default:
			break;
		}
		n.parent = currentNode;
		currentNode.Nodes.add(n);
		currentNode = n;

	}

	/**
	 * set attributes values
	 * @param node
	 * @param atts
	 */
	public void setValueNodeAttrs(ValueNode node, Attributes atts) {
		node.Source = atts.getValue("source");
		node.Format = atts.getValue("format") == null ? "%1$,.2f" : atts
				.getValue("format");
		if (atts.getValue("align") != null) {
			try {
				node.aType = AlignType.valueOf(atts.getValue("align")
						.toUpperCase());
			} catch (IllegalArgumentException e) {
				errorTags += "\nAlignType Error!!" + atts.getValue("align");
			}
		}
		if (atts.getValue("length") != null) {
			node.Length = Integer.valueOf(atts.getValue("length"));
		}
		if (atts.getValue("type") != null) {
			try {
				node.vType = ValueNode.ValueType.valueOf(atts.getValue("type")
						.toUpperCase());
			} catch (IllegalArgumentException e) {
				errorTags += "\nValueType Error!!" + atts.getValue("type");
			}
		}
		if (atts.getValue("value") != null) {
			node.Value = atts.getValue("value");
		}
	}

	/**
	 * @author user
	 * the end of a tree brand
	 * @param uri
	 * @param localName
	 * @param qualifiedName
	 */
	public void endElement(String uri, String localName, String qualifiedName) {
		try {
			NodeType.valueOf(qualifiedName.toUpperCase());
			currentNode = currentNode.parent;
		} catch (IllegalArgumentException e) {

		}

	}

	public void Parse(String script) {
		// create Node Tree
		try {
			saxParser.parse(new InputSource(new StringReader(script)),
					saxHandler);

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public byte[] Run() {
		// 递归 Node

		for (NodeBase r : repList) {
			// try {
			// //buff.write(ParseNode((RepNode)r));
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			ParseNode((RepNode) r, null);
		}
		if (errorTags.length() > 0) {

			try {
				buff.write(errorTags.getBytes());
				buff.write(StringToHex(ctrlMap.get("CR")));
				buff.write(StringToHex(ctrlMap.get("CUT")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		repList.clear();
		return buff.toByteArray();
	}

	public static Object sum(List<Object> paramList) {
		Double res = 0.0;
		for (Object o : paramList) {
			if (o instanceof Double) {
				res += ((Double) o).doubleValue();
			} else if (o instanceof Integer) {
				res += ((Integer) o).doubleValue();
			} else {
				return null;
			}
		}
		return (Object) res;
	}

	@SuppressLint("DefaultLocale")
	@SuppressWarnings("rawtypes")
	void ParseNode(NodeBase node, Object o) {
		switch (node.Type) {
		case FUNC:
			String[] sources = ((FuncNode) node).Source.split(":");
			List<Object> paramList = new ArrayList<Object>();
			Object os = paramMap.get(sources[0].toUpperCase());
			Object value = null;
			if (os instanceof List) {
				for (Object t : (List) os) {
					paramList.add(getValue((ValueNode) node, t));
				}
			} else {
				// paramList.add(getValue((ValueNode)node,null));
				try {
					buff.write("Source is not a list!!".getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (Method method : this.getClass().getMethods()) {
				if (method.getName().equalsIgnoreCase(
						((FuncNode) node).MethodName)) {
					try {
						value = method.invoke(null, paramList);
						break;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				if (value == null) {
					buff.write(("Function " + ((FuncNode) node).MethodName + " error!!")
							.getBytes());
					break;
				} else {
					buff.write(formatValue(value, (ValueNode) node));
				}
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			break;
		case IF:
			if (((IfNode) node).LeftOper == null
					|| ((IfNode) node).RightOper == null
					|| ((IfNode) node).TrueStatement == null
					|| ((IfNode) node).FalseStatement == null) {
				try {
					buff.write("IF statement incomplet!!".getBytes());
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if (((IfNode) node).LeftOper.Nodes.size() != 1
						|| ((IfNode) node).RightOper.Nodes.size() != 1) {
					try {
						buff.write("IF oper statement incomplet!!".getBytes());
						return;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Object l = getValue(
							(ValueNode) ((IfNode) node).LeftOper.Nodes.get(0),
							o);
					Object r = getValue(
							(ValueNode) ((IfNode) node).RightOper.Nodes.get(0),
							o);
					if (l == null || r == null) {
						try {
							buff.write("If Statament's left or right oper is null!\n"
									.getBytes());
							break;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					List<NodeBase> TrueStatements = ((IfNode) node).TrueStatement.Nodes;
					List<NodeBase> FalseStatements = ((IfNode) node).FalseStatement.Nodes;
					switch (((IfNode) node).oper) {
					case EQ:
						if (l.toString().compareToIgnoreCase(r.toString()) == 0) {
							for (int i = 0; i < TrueStatements.size(); i++) {
								ParseNode(TrueStatements.get(i), o);
							}
						} else {
							for (int i = 0; i < FalseStatements.size(); i++) {
								ParseNode(FalseStatements.get(i), o);
							}
						}
						break;
					case NE:
						if (!(l.toString().compareToIgnoreCase(r.toString()) == 0)) {
							for (int i = 0; i < TrueStatements.size(); i++) {
								ParseNode(TrueStatements.get(i), o);
							}
						} else {
							for (int i = 0; i < FalseStatements.size(); i++) {
								ParseNode(FalseStatements.get(i), o);
							}
						}
						break;
					case LT:
						if (l.toString().compareToIgnoreCase(r.toString()) < 0) {
							for (int i = 0; i < TrueStatements.size(); i++) {
								ParseNode(TrueStatements.get(i), o);
							}
						} else {
							for (int i = 0; i < FalseStatements.size(); i++) {
								ParseNode(FalseStatements.get(i), o);
							}
						}
						break;
					case LE:
						if (!(l.toString().compareToIgnoreCase(r.toString()) > 0)) {
							for (int i = 0; i < TrueStatements.size(); i++) {
								ParseNode(TrueStatements.get(i), o);
							}
						} else {
							for (int i = 0; i < FalseStatements.size(); i++) {
								ParseNode(FalseStatements.get(i), o);
							}
						}
						break;
					case BT:
						if (l.toString().compareToIgnoreCase(r.toString()) > 0) {
							for (int i = 0; i < TrueStatements.size(); i++) {
								ParseNode(TrueStatements.get(i), o);
							}
						} else {
							for (int i = 0; i < FalseStatements.size(); i++) {
								ParseNode(FalseStatements.get(i), o);
							}
						}
						break;
					case BE:
						if (!(l.toString().compareToIgnoreCase(r.toString()) < 0)) {
							for (int i = 0; i < TrueStatements.size(); i++) {
								ParseNode(TrueStatements.get(i), o);
							}
						} else {
							for (int i = 0; i < FalseStatements.size(); i++) {
								ParseNode(FalseStatements.get(i), o);
							}
						}
						break;

					}

				}
			}
			break;
		case CTRL:
			try {
				String c = ctrlMap.get(((CtrlNode) node).Code.toUpperCase());
				if (c == null) {
					buff.write(("Control Code:" + ((CtrlNode) node).Code + " not exist")
							.getBytes());
				} else {
					buff.write(this.StringToHex(c));
				}
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;
		case REP:
			for (int i = 0; i < node.Nodes.size(); i++) {
				ParseNode(node.Nodes.get(i), o);
			}
			break;
		case VALUE:
			try {
				buff.write(formatValue(getValue((ValueNode) node, o),
						(ValueNode) node));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case FOREACH:
			// get List from Paramters
			// foreach list
			Object list;
			if (((ForNode) node).Value == null) {
				Object ot = paramMap.get(((ForNode) node).Source.toUpperCase());
				if (ot == null) {
					System.err.println("list not found!!");
					try {
						buff.write("list not found!!".getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
				list = ot;
			} else {
				list = ((ForNode) node).Value;
			}
			if (!(list instanceof List)) {
				System.err.println("source is not a list!!");
				try {
					buff.write("source is not a list!!".getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			} else {
				for (int i = 0; i < ((List) list).size(); i++) {
					for (int j = 0; j < node.Nodes.size(); j++)
						ParseNode(node.Nodes.get(j), ((List) list).get(i));
				}
			}
			break;
		default:
			break;
		}

		// return buf.toByteArray();
	}

	@SuppressLint("DefaultLocale")
	Object getValue(ValueNode node, Object o) {
		if (node.Source != null) {
			String[] sources = ((ValueNode) node).Source.split(":");
			try {
				Object value = null;
				if (o == null) {
					o = paramMap.get(sources[0].toUpperCase());
				}
				Object ot = o;
				if (ot == null) {
					return null;
				}
				Field field;
				for (int i = 1; i < sources.length; i++) {
					field = ot.getClass().getField(sources[i]);
					ot = field.get(ot);
				}
				if (ot != null) {
					try {
						value = ot;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return value;
			} catch (NoSuchFieldException e) {

				System.out.println("No such Field");
				return null;
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				System.out.println("IllegalAccessException");
				return null;
				// e.printStackTrace();
			}
		} else {
			return node.Value;
		}
	}

	@SuppressLint("SimpleDateFormat")
	byte[] formatValue(Object value, ValueNode node) {
		if (value == null) {
			return ("Value" + node.Source + "not exists").getBytes();
		}
		switch (node.vType) {
		case TEXT:
			return formatString(value.toString(), node);
		case NUMBER:
			Double v;
			if (value instanceof Double) {
				v = ((Double) value).doubleValue();
			} else {
				v = ((Integer) value).doubleValue();
			}
			return formatString(String.format(node.Format, v), node);
		case DATE:
		case TIME:
			// Calendar c1=Calendar.getInstance();
			// c1.setTime((Date)value);
			SimpleDateFormat formatter = new SimpleDateFormat(node.Format);
			String t = formatter.format((Date) value);
			return t.getBytes();
			// return formatString(String.format(node.Format, c1), node);
		}
		return null;
	}

	byte[] formatString(String str, ValueNode node) {
		if (node.Length == -1) {
			return str.getBytes();
		}
		if (str.length() < node.Length) {
			int diff = node.Length - str.length();
			for (int i = 0; i < diff; i++) {
				switch (node.aType) {
				case LEFT:
					str += " ";
					break;
				case CENTER:
					if (i % 2 == 0) {
						str += " ";
					} else {
						str = " " + str;
					}
					break;
				case RIGHT:
					str = " " + str;
					break;
				}
			}
			return str.toUpperCase().getBytes();
		} else {
			return str.substring(0, node.Length).toUpperCase().getBytes();
		}
	}
}
