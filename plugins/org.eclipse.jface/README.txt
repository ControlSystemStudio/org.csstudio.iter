This is a copy of the official org.eclipse.jface plugin
from the neon release. The source is taken from 
git://git.eclipse.org/gitroot/platform/eclipse.platform.ui.git
Branch R4_6_maintenance.

The only change from the original is the file
URLImageDescriptor.java in jface bundle. This patch provides 
ovveride to lodaing bigger icons into the CSS. 
If org.eclipse.jface/forceIconZoomLevel is set to 200. Icons 
with @2x will be loaded if they exist. 

The issue related to this patch is described here:
https://bugzilla.iter.org/codac/show_bug.cgi?id=7063
CS-Studio icons scaling