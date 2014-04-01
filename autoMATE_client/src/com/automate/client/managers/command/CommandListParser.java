package com.automate.client.managers.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.automate.protocol.models.Status;
import com.automate.protocol.models.Type;
import com.automate.protocol.models.Type.TypeFormatException;

public class CommandListParser {

	public List<Command> parse(Document document) {
		Node node = document.getFirstChild();
		if(node.getNodeName().equals("CommandList")) {
			return parseCommandList(node.getChildNodes());
		} else return null;
	}

	private List<Command> parseCommandList(NodeList childNodes) {
		List<Command> commands = new ArrayList<Command>();
		for(int i = 0; i < childNodes.getLength(); ++i) {
			Node node = childNodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals("Command")) {
					if(node instanceof Element) {
						Command command = parseCommand((Element) node);
						if(command == null) return null;
						commands.add(command);
					}
				} else {
					return null;
				}
			}
		}
		return commands;
	}

	private Command parseCommand(Element command) {
		String name = command.getAttribute("name");
		List<ArgumentSpecification> args = new ArrayList<ArgumentSpecification>();
		Condition<?> condition = new TautologyCondition();
		NodeList children = command.getChildNodes();
		for(int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				if(child.getNodeName().equals("Argument")) {
					ArgumentSpecification arg = parseArgument((Element)child);
					if(arg == null) return null;
					args.add(arg);
				} else if(child.getNodeName().equals("StatusValueCondition")) {
					condition = parseStatusValueCondition((Element)child);
					if(condition == null) return null;
				} else return null;
			}
		}
		return new Command(name, args, condition);
	}

	private ArgumentSpecification parseArgument(Element node) {
		String name = node.getAttribute("name");
		String type = node.getAttribute("type");
		if(name == null || type == null) return null;
		NodeList children = node.getChildNodes();
		ArgumentRange range = new ArgumentRange.TautologyRange();
		for(int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				range = parseRange((Element) child);
				if(range == null) return null;
			}
		}
		try {
			return new ArgumentSpecification(name, Type.parseType(type.toUpperCase()), range);
		} catch (TypeFormatException e) {
			return null;
		}
	}

	private ArgumentRange<?> parseRange(Element item) {
		if(item.getNodeName().equals("NumericRange")) {
			try {
				Double n1 = Double.parseDouble(item.getAttribute("lower"));
				Double n2 = Double.parseDouble(item.getAttribute("upper"));
				return new ArgumentRange.NumericRange<Double>(n1, n2);
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (item.getNodeName().equals("EnumRange")) {
			NodeList children = item.getChildNodes();
			List<String> values = new ArrayList<String>();
			for(int i = 0; i < children.getLength(); ++i) {
				Node child = children.item(i);
				if(child.getNodeType() == Node.ELEMENT_NODE) { 
					if(child.getNodeName().equals("Value")) {						
						values.add(child.getTextContent());
					}
				}
			}
			return new ArgumentRange.EnumRange(values);
		} else {
			return null;
		}
	}

	private StatusValueCondition parseStatusValueCondition(Element node) {
		String statusName = node.getAttribute("status");
		String type = node.getAttribute("type");
		String value = node.getAttribute("value");
		String defaultValue = node.getAttribute("default");
		boolean defaultValueBool = false;
		if(defaultValue.equalsIgnoreCase("true")) {
			defaultValueBool = true;
		} else if(defaultValue.equalsIgnoreCase("false")) {
			defaultValueBool = false;
		} else return null;
		try {
			Status<?> status = Status.newStatus(statusName, Type.parseType(type.toUpperCase()), value);
			if(status == null) return null;
			return new StatusValueCondition(status.name, status.type, status.value, defaultValueBool);
		} catch (TypeFormatException e) {
			return null;
		}
	}

}
