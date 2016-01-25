#======================================================================
# PLC(s) driver configuration commands
#======================================================================
# level=-1:  no output
# level=0:   errors only
# level=1:   startup messages
# level=2: + output record processing
# level=3: + input record processing
# level=4: + driver calls
# level=5: + io printout
# be careful using level>1 since many messages may introduce delays

# var s7plcDebug 2

# s7plcConfigure name,IPaddr,port,inSize,outSize,bigEndian,recvTimeout,sendIntervall, configversion
# connects to PLC <name> on address <IPaddr> port <port>
# <inSize>        : size of data block PLC -> IOC [bytes]
# <outSize>       : size of data block IOC -> PLC [bytes]
# <bigEndian>=1   : motorola format data (MSB first)
# <bigEndian>=0   : intel format data (LSB first)
# <recvInterval>  : receive buffer interval [ms] (Default : 50ms)
# <sendIntervall> : time to wait before sending new data to PLC [ms]
# <configversion> : database configuration version

# s7plcConfigureCmd name,IPaddr,port,outSize,bigEndian,sendIntervall
# connects to PLC <name> on address <IPaddr> port <port>
# <outSize>       : size of data block IOC -> PLC [bytes]
# <bigEndian>=1   : motorola format data (MSB first)
# <bigEndian>=0   : intel format data (LSB first)
# <sendIntervall> : time to wait before sending new data to PLC [ms]


#============================================================================
# s7plc asyn driver configuration commands
#============================================================================


#Configure IP Port driver
drvAsynIPPortConfigure("P0_cfg_2000", "PLC000.codac.iter.org:2000", $(IPPort_priority), $(IPPort_noAutoConnect), $(IPPort_noProcessEos))
#Configure Ports for Single PLC
drvCodacHeaderConfigure("P0_cfg", "P0_cfg_2000", 64, 0,  $(P0_RECVINTERVAL),  $(P0_SENDINTERVAL),"$(P0_DATABLOCKVERSION)")

#============================================================================
# NI-6259 DAQ I/O Module driver configuration commands
#============================================================================
# Reference: ITER_D_3DEY52 v1.3 - NI PXI-6259 EPICS Driver User’s Guide

# For analogue input, analogue output, waveform, initialize using below function
# pxi6259_ai_init(uint8 cardnumber, uint32 range, uint32 clk_source, uint32 clk_edge);
# Example: pxi6259_ai_init(0, 1, 0, 0)

# For binary input, binary output, multi-bit binary input, multi bit binary output, initialize using below function
# pxi6259init(uint8 cardnumber, uint32 portmask0, uint8 portmask1, uint8 portmask2);
# Example: pxi6259_bio_init(0, 0xFF000000, 0xFF, 0xFF)


#============================================================================
# NI-6682 Timing and Synchronization I/O Module driver configuration commands
#============================================================================
# Reference ITER_D_33Q5TX v1.7 - NI Sync EPICS Driver User’s Guide 

# nisyncDrvInit(string port, char* type, int cardNumber);
# Example: nisyncDrvInit("S0", "PXI-6682", "0");
# Example: nisyncDrvInit("S0", "PXI-6683H", "0");
# nisyncTimeInit(int cardID, char* type, int cardNumber);
# Example: nisyncTimeInit("0", "PXI-6682", "0")
# Example: nisyncTimeInit("0", "PXI-6683H", "0")


#============================================================================================
# NI-6368 X Series - Multifunction Data Acquisition I/O Module driver configuration commands
#============================================================================================
# Reference ITER_D_3P4N3R v1.2 - NI X Series EPICS Driver User’s Guide 

# nixseriesInit(char *portName, char *nix6368Card);
# Example: nixseriesInit("ni6368_0", "/dev/ni6368.0");


#============================================================================
# NI-6528 DAQ I/O Module driver configuration commands
#============================================================================
# Reference ITER_D_433VEW - NI PXI-6528 EPICS Driver User's Manual
# ni6528_init(char *portName, char *ni6528Card); 
# Example: pxi6528_init("ni6528_0", "/dev/ni6528.0")
# asynSetTraceMask("<port name>",0,255)
# Example: asynSetTraceMask("pxi6528_0",0,255)
# pxi6528_reset(char *portName)
# Example: pxi6528_reset("pxi6528_0")

