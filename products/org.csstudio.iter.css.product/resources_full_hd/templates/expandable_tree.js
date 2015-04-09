/*
Copyright (c) : 2010-2015 ITER Organization,
CS 90 046
13067 St. Paul-lez-Durance Cedex
France
 
This product is part of ITER CODAC software.
For the terms and conditions of redistribution or use of this software
refer to the file ITER-LICENSE.TXT located in the top level directory
of the distribution package.
*/

importPackage(Packages.org.csstudio.opibuilder.scriptUtil);
importPackage(Packages.java.lang)

var DIR_IMAGES = "pictures/";
var IMG_PLUS = DIR_IMAGES + "btnPlus.svg";
var IMG_MINUS = DIR_IMAGES + "btnMinus.svg";
 
/*var imgPlus = new Image();
imgPlus.src = IMG_PLUS;
var imgMinus = new Image();
imgMinus.src = IMG_MINUS;*/
 
var objLocalTree = null;
 
var INDENT_WIDTH = 25;

// ---

function jsTree() {
 
    //Public Properties 
    this.root = null;           //the root node of the tree
 
     //Public Collections 
    this.nodes = new Array;     //array for all nodes in the tree
   
    //Constructor
    //assign to local copy of the tree 
    objLocalTree = this;
}

function jsTreeNode(strIcon, strText, strOPI) {
 
    //Public Properties 
    this.icon = strIcon;            //the icon to display
    this.text = strText;            //the text to display
    this.opi = strOPI;              //the OPI with macro to open on click
    
    //Private Properties 
    this.indent = 0;                //the indent for the node
    
    //Public States 
    this.expanded = false;          //is this node expanded?
 
    //Public Collections    
    this.childNodes = new Array;    //the collection of child nodes
}

jsTreeNode.prototype.addChild = function (strIcon, strText, strOPI) {
 
    //create a new node 
    var objNode = new jsTreeNode(strIcon, strText, strOPI);
    
    //assign an ID for internal tracking 
    objNode.id = this.id + "_" + this.childNodes.length;
    
    //assign the indent for this node
    objNode.indent = this.indent + 1;
    
    //add into the array of child nodes 
    this.childNodes[this.childNodes.length] = objNode;
    
    //add it into the array of all nodes 
    objLocalTree.nodes[objNode.id] = objNode;
    
    //return the created node 
    return objNode;
}

jsTreeNode.prototype.addToCBSMap = function (objWidgetParent) {
	System.out.println("addToCBSMap " + this.indent + " " + this.id + " " + this.icon + " " + this.expanded);
    // creating the container for the node 
	var linkingContainer = WidgetUtil.createWidgetModel("org.csstudio.opibuilder.widgets.linkingContainer");
		
	linkingContainer.setPropertyValue("opi_file", "CBSMapElt.opi");
	linkingContainer.setPropertyValue("auto_size", true);
	linkingContainer.setPropertyValue("zoom_to_fit", false);
	linkingContainer.setPropertyValue("border_style", 0);
    
    // no indent needed for root or level under root 
    if (this.indent > 1) {
		linkingContainer.setPropertyValue("x", this.indent * INDENT_WIDTH);
    }
	
    // adding macros ID, CBS and OPI_FILE to the container
	linkingContainer.addMacro("ID", this.id);	
	linkingContainer.addMacro("CBS", this.icon + " - " + this.text);
	if (this.opi.search(".opi") > 0) {	
		linkingContainer.addMacro("OPI_FILE", this.opi.substring(0,this.opi.search(".opi")));	
	}
    
    // there is no plus/minus image for the root
    // if there are children, then making a plus/minus image visible
	linkingContainer.addMacro("EXPANDED", this.expanded);	
    if (this.indent > 0) {
        // if there are children, then making a plus/minus image visible
        if (this.childNodes.length > 0) {
        	if (this.expanded) {
//        		objWidgetParent.getWidget("btnMinus" + this.id).setPropertyValue("visible", "yes");
				System.out.println("--- btnMinus visible yes " + this.id);
        	}
        	else {
//        		objWidgetParent.getWidget("btnPlus" + this.id).setPropertyValue("visible", "yes");
				System.out.println("+++ btnPlus visible yes " + this.id);
        	}
        }
    }
    
    // adding the container to the parent widget 
    objWidgetParent.addChildToBottom(linkingContainer);
    
    // call for all children 
    for (var i=0; i < this.childNodes.length; i++)
        this.childNodes[i].addToCBSMap(widget);      
 }

jsTreeNode.prototype.collapse = function () {
 
    //check to see if the node is already collapsed 
    if (!this.expanded) {
    
        //throw an error 
        throw "Node is already collapsed"
 
    } else {
    
        //change the state of the node 
        this.expanded = false;
        
        //change the plus/minus image to be plus 
        document.images["imgPM_" + this.id].src = imgPlus.src;
        
        //hide the child nodes 
        document.getElementById("divChildren_" + this.id).style.display = "none";
    }
}
 
jsTreeNode.prototype.expand = function () {
 
    //check to see if the node is already expanded 
    if (this.expanded) {
    
        //throw an error 
        throw "Node is already expanded"
    
    } else {
    
        //change the state of the node 
        this.expanded = true;
        
        //change the plus/minus image to be minus 
        document.images["imgPM_" + this.id].src = imgMinus.src;
        
        //show the child nodes 
        document.getElementById("divChildren_" + this.id).style.display = "block";
    }
}

jsTree.prototype.createRoot = function(strIcon, strText, strOPI) {
 
    //create a new node 
    this.root = new jsTreeNode(strIcon, strText, strOPI);
    
    //assign an ID for internal tracking 
    this.root.id = "root";
    
    //add it into the array of all nodes 
    this.nodes["root"] = this.root;
    
    //make sure that the root is expanded 
    this.root.expanded = true;
    
    //return the created node 
    return this.root;
}

jsTree.prototype.buildCBSMap = function() {
 
    //call method to add root to the OPI map, which will recursively
    //add all other nodes 
    this.root.addToCBSMap(widget);
}

jsTree.prototype.toggleExpand = function(strNodeID) {
 
    //get the node 
    var objNode = this.nodes[strNodeID];
    
    //determine whether to expand or collapse
    if (objNode.expanded)
        objNode.collapse();
    else
        objNode.expand();
}


// getting the current cbs level for this screen
var current_cbs = widget.getMacroValue("LEVEL");

// loading XML document and getting the root element
// the result is a JDOM Element
var root = FileUtil.loadXMLFile("Navigation.xml", widget);

// Creating the CBS tree structure starting from root
createCBSTree(root, 0, null);

objLocalTree.buildCBSMap();

// ---

// recursive list function on CBS tree
function createCBSTree(current, depth, root){
	var cbs = current.getChildren();	
	var itr = cbs.iterator();
	var parent;
	while (itr.hasNext()) {
	    var elt = itr.next();
	    if (depth == 0) {
			// Creation of the root tree
			var cbsTree = new jsTree();
			parent = cbsTree.createRoot(elt.getAttributeValue("name"), elt.getAttributeValue("description"), elt.getAttributeValue("opi_file"));
	    } else {
			// Creation of a node
			parent = root.addChild(elt.getAttributeValue("name"), elt.getAttributeValue("description"), elt.getAttributeValue("opi_file"));
	    }
    	createCBSTree(elt, depth+1, parent);
	}
	return parent;
}

