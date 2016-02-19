This is a copy of the official org.eclipse.jface plugin
from the mars release. This plugin needed to be patched, 
because the original had problems displaying modal dialogs
in full screen mode on CODAC.

The only change from the original is the file
org.eclipse.jface.dialogs.Dialog.java. In the file Dialog.org
in the root of the project you will find the original file and
in the src folder you can find the patched file. With every new 
release (until the problem is fixed) the issue should be retested
and this patch updated accordingly. 

The last known working plugin was part of the eclipse luna. The
problem might be gone with eclipse neon, or maybe with CCS 6.0,
which will be upgraded to the newer red hat kernel.

The issue related to this patch is described here:
https://bugzilla.iter.org/codac/show_bug.cgi?id=7800
Popup messages are behind the running OPI in full screen mode