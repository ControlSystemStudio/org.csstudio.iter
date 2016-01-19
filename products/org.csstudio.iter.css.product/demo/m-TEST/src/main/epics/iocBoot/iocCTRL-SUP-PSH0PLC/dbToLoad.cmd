
#======================================================================
# Loading DBs
#======================================================================
cd $(TOP)/db
dbLoadRecords("PLC0-CTRL-SUP-CSS.db")



#======================================================================
# PLC Communication Monitoring PVs DB Loading
#======================================================================
cd $(EPICS_ROOT)/db
dbLoadRecords("s7plccom-asyn.db", "CBS1=CTRL, CBS2=SUP, CTRLTYPE=P, IDX=0")

#======================================================================
# IOC Monitor
#======================================================================
cd $(EPICS_ROOT)/db
dbLoadRecords("iocmon.db","CBS=CTRL-SUP-, CTRLTYPE=H, IDX=0, IOCTYPE=PLC")
