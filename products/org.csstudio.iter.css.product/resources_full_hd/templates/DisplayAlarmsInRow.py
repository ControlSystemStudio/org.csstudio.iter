from org.csstudio.opibuilder.scriptUtil import PVUtil
from org.csstudio.opibuilder.scriptUtil import ColorFontUtil

import datetime

pv = pvs[0]

s = PVUtil.getTimeInMilliseconds(pv)
t = datetime.datetime.fromtimestamp(float(s)/1000.)

format = "%H:%M:%S"

widget.setPropertyValue("on_label", t.strftime(format))
widget.setPropertyValue("off_label", t.strftime(format))
