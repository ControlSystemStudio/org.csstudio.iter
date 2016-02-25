
#======================================================================
# SYS Monitor
#======================================================================
cd $(EPICS_ROOT)/db
dbLoadRecords("sysmon.db","CBS=CTRL-SUP-, CTRLTYPE=H, IDX=0")


#======================================================================
# IOC Monitor
#======================================================================
cd $(EPICS_ROOT)/db
dbLoadRecords("iocmon.db","CBS=CTRL-SUP-, CTRLTYPE=H, IDX=0, IOCTYPE=SYSM")
