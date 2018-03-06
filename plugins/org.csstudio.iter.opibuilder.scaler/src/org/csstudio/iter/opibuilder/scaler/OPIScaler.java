/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.scaler;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * <code>OPIScaler</code> scales the OPI by changing the position and size properties of all components.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public final class OPIScaler {

    private OPIScaler() {
    }

    /**
     * Scales the OPI.
     *
     * @param opiSourceFile the opi source file, which will be scaled
     * @param opiDestinationFile the destination file, the scaled opi will be stored into
     * @param scale the scale factor
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static void scale(File opiSourceFile, File opiDestinationFile, double scale)
            throws ParserConfigurationException, SAXException, IOException, TransformerException  {
        DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document =  builder.parse(opiSourceFile);

        NodeList nodeList = document.getDocumentElement().getChildNodes();
        updateAllNodes(nodeList, scale);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(opiDestinationFile);
        transformer.transform(source, result);
    }

    /**
     * Returns the dimension of the display in the source file.
     *
     * @param sourceFile the sourcefile to check the display dimension
     * @return the dimension of the source file
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Dimension getDisplayDimension(File sourceFile)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document =  builder.parse(sourceFile);

        NodeList nodeList = document.getDocumentElement().getChildNodes();
        return getDisplayDimension(nodeList);
    }

    private static Dimension getDisplayDimension(NodeList nodeList) {
        if (nodeList == null) return null;
        Node node;
        String name;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (isDisplay(node)) {
                NodeList children = node.getParentNode().getChildNodes();
                int width = -1;
                int height = -1;
                for (int j = 0; j < children.getLength(); j++) {
                    node = children.item(j);
                    name = node.getNodeName();
                    if ("width".equalsIgnoreCase(name)) {
                        width = Integer.parseInt(node.getLastChild().getTextContent().trim());
                    } else if ("height".equalsIgnoreCase(name)) {
                        height = Integer.parseInt(node.getLastChild().getTextContent().trim());
                    }
                }
                return new Dimension(width, height);
            }

            Dimension dim = getDisplayDimension(node.getChildNodes());
            if (dim != null) return dim;
        }
        return null;
    }

    private static void updateAllNodes(NodeList nodeList, double scale) {
        Node node;
        String name;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            name = node.getNodeName();

            if ("x".equalsIgnoreCase(name) || "y".equalsIgnoreCase(name)
                    || "width".equalsIgnoreCase(name) || "height".equalsIgnoreCase(name)) {
                scale(node,scale);
            } else if ("points".equalsIgnoreCase(name)) {
                if (isPolyLineOrPolygon(node)) {
                    NodeList points = node.getChildNodes();
                    for (int k = 0; k < points.getLength(); k++) {
                        node = points.item(k);
                        NamedNodeMap map = node.getAttributes();
                        if (map == null) continue;
                        scale(map.getNamedItem("x"),scale);
                        scale(map.getNamedItem("y"),scale);
                    }
                }
            }
            updateAllNodes(node.getChildNodes(), scale);
        }
    }

    private static boolean isDisplay(Node node) {
        String type  = node.getParentNode().getAttributes().getNamedItem("typeId").getLastChild().getTextContent();
//        String type  = node.getAttributes().getNamedItem("typeId").getLastChild().getTextContent();
        return "org.csstudio.opibuilder.Display".equalsIgnoreCase(type);
    }

    private static boolean isPolyLineOrPolygon(Node node) {
        String type  = node.getParentNode().getAttributes().getNamedItem("typeId").getLastChild().getTextContent();
        return "org.csstudio.opibuilder.widgets.polyline".equalsIgnoreCase(type)
                || "org.csstudio.opibuilder.widgets.polygon".equalsIgnoreCase(type);
    }

    private static void scale(Node node, double scale) {
        int val = Integer.parseInt(node.getLastChild().getTextContent().trim());
        val *= scale;
        node.getLastChild().setTextContent(String.valueOf(val));
    }

    public static void main(String[] args) throws Exception {

        File inFile = null;
        File outFile = null;
        double scale = 2.0;
        try {
            if (args.length == 3) {
                scale = Double.parseDouble(args[0]);
                inFile = new File(args[1]);
                outFile = new File(args[2]);
            }  else if (args.length == 2) {
                scale = Double.parseDouble(args[0]);
                inFile = new File(args[1]);
                outFile = new File(args[1]);
            } else {
                printHelp();
                System.out.println("Bye Bye!");
                return;
            }
        } catch (Exception e) {
            printHelp();
            System.out.println("Bye Bye!");
            return;
        }
        if (!inFile.exists()) {
            System.out.println("Input file '"+inFile+"' does not exist. Bye Bye!");
            return;
        }
        if (outFile.exists()) {
            System.out.println("Output file '" + outFile + "' already exists. Would you like to overwrite it (y/N)?");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            if (line == null) {
                line = "";
            }
            line = line.trim().toLowerCase();
            if (line.isEmpty() || line.charAt(0) != 'y') {
                System.out.println("Bye Bye!");
                return;
            }
        }
        if (scale == 1.0) {
            System.out.println("The size is 1. The new file is a copy of the old one.");
            Files.copy(inFile.toPath(),outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Bye Bye!");
            return;
        }

        scale(inFile, outFile, scale);
        System.out.println("Successfully created file '" + outFile.getAbsolutePath()+ "'.");
        System.out.println("Bye Bye!");
    }

    private static final void printHelp() {
        System.out.println("**************************************************************************");
        System.out.println("*                                                                        *");
        System.out.println("*   Usage: java -jar opiscaler.jar <scale> <input_file> <output_file>    *");
        System.out.println("*                                                                        *");
        System.out.println("*          <scale>      : Scaling ratio new:existing                     *");
        System.out.println("*                         (>1 to increase <1 to decrease the size)       *");
        System.out.println("*          <input_file> : The source OPI file to transform               *");
        System.out.println("*          <output_file>: The destination OPI file to transform          *");
        System.out.println("*                         If output file is not provided the result will *");
        System.out.println("*                         replace the input file.                        *");
        System.out.println("*                                                                        *");
        System.out.println("**************************************************************************");
    }
}
