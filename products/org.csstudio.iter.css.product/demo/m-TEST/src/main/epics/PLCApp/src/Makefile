TOP=../..

include $(TOP)/configure/CONFIG
#----------------------------------------
#  ADD MACRO DEFINITIONS AFTER THIS LINE
#========================================

#========================================
# Build the IOC application

PROD_IOC = PLC
# PLC.dbd will be created and installed
DBD += PLC.dbd

# PLC.dbd will be made up from these files:
PLC_DBD += base.dbd
PLC_DBD += iocmon.dbd
PLC_DBD += sysmon.dbd
PLC_DBD += std.dbd
PLC_DBD += asSupport.dbd
PLC_DBD += s7plc.dbd
# # ASYN
PLC_DBD += asyn.dbd
PLC_DBD += drvAsynSerialPort.dbd
PLC_DBD += drvAsynIPPort.dbd
PLC_DBD += s7PLCAsyn.dbd

# Add all the support libraries needed by this IOC
PLC_LIBS += autosave
PLC_LIBS += iocmon
PLC_LIBS += sysmon
PLC_LIBS += std
PLC_LIBS += asyn
PLC_LIBS += s7PLCAsyn
PLC_LIBS += s7plc

# # SNCSEQ
# <sncProgram>_SNCFLAGS += +r -c +d
# PLC_DBD += <sncProgram>.dbd
# PLC_SRCS += <sncProgram>.stt
# PLC_LIBS += seq pv


# PLC_registerRecordDeviceDriver.cpp derives from PLC.dbd
PLC_SRCS += PLC_registerRecordDeviceDriver.cpp

# Build the main IOC entry point on workstation OSs.
PLC_SRCS_DEFAULT += PLCMain.cpp
PLC_SRCS_vxWorks += -nil-

# Add support from base/src/vxWorks if needed
#PLC_OBJS_vxWorks += $(EPICS_BASE_BIN)/vxComLibrary

# Finally link to the EPICS Base libraries
PLC_LIBS += $(EPICS_BASE_IOC_LIBS)

#===========================

include $(TOP)/configure/RULES
#----------------------------------------
#  ADD RULES AFTER THIS LINE
#----------------------------------------
