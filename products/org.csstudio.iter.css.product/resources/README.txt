INTRODUCTION
------------

These OPI resources demonstrate ITER Design Guide and provide templates that 
can be reused to develop a plant system operator interface.

* For a full description of ITER Design Guide, refer to the IDM document:
   https://user.iter.org/?uid=3XLESZ - Philosophy of ITER Operator User Interface (3XLESZ) 

REQUIREMENTS
------------
These OPI resources require CS-Studio (CSS) BOY and the symbol library installed.

CONFIGURATION
-------------

* The preferred configuration to run these OPIs is:

   - a VDU with Quad HD resolution (4k) - 3840 x 2160

   - but a Full HD can also be used zooming out the display of 50%
   
* To start the demonstration, check you screen configuration by executing the 
following command in a Linux console:

    $ boy-switch-resolution 
BOY resources and SymbolLibrary are currently in 4k mode!
   
   - if the resolution is Full HD, you need to change it (this requires root privileges)
   $ boy-switch-resolution 4k

   
* Then in CSS, browse the folder CSS -> opi -> boy -> resources

   - make a right-click on Demo.opi and select Open With -> OPI Runtime
   
   - make a right-click on the display and switch to OPI Runtime Perspective
   
   - depending on your screen resolution, you can zoom out to 50%
   
   - make a right-click on the display and switch to Full Screen (F11)
   
   - read the instructions on the screen to navigate through the demo
   
 * Finally, you can copy Demo_SUP.opi and Demo_UTIL*.opi in your workspace to start your own development
 
