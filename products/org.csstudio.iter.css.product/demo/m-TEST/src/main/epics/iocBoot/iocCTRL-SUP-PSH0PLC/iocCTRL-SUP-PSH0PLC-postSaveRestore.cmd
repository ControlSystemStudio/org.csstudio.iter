#############################################
## Autosave monitor post setup             ##
#############################################

cd "${TOP}/iocBoot/$(IOC)"
create_monitor_set("iocCTRL-SUP-PSH0PLC.req",30,"P=$(AUTOSAVE_SYSM_PV_PREFIX)")

