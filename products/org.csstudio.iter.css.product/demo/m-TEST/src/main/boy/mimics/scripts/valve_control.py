from epics import caget, caput, camonitor
from time import sleep

# Settings for the performance test
# open: OPEN THRESHOLD - 75%
# close: CLOSE THRESHOLD - 25%
open = caget('CTRL-SUP-BOY:VC101-COH.VAL') + 15
close = caget('CTRL-SUP-BOY:VC101-COL.VAL') - 15
start = caget('CTRL-SUP-BOY:PERF-SRT.VAL')

init = 40
firstValve = 101
lastValve = 350
tempo = 5

# scan: scan rate for the valve position
scan = caget('CTRL-SUP-BOY:PERF-SCAN.VAL')
maxLoop = int(caget('CTRL-SUP-BOY:PERF-LOOP.VAL'))

# define a callback function on 'pvname' and 'value'
def onChanges(pvname=None, value=None, **kw):
    scan = caget('CTRL-SUP-BOY:PERF-SCAN.VAL')
    print pvname, str(value), repr(kw)

camonitor('CTRL-SUP-BOY:PERF-SCAN.VAL', callback=onChanges)

# Valve status initialisation
for iValve in range(firstValve, lastValve+1):
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-FB', init)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-CO', init)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-TRIP', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-INTLK', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-FOMD', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-LOMD', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-MAMD', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-AUMD', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-IOERR', 0)
    caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-IOSIM', 0)

# wait for START

for iLoop in range(0, maxLoop):

    sleep(tempo)
    for iValve in range(firstValve, lastValve+1):
        caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-CO', open)
    for x in range(int(init), int(open)+1):
        for iValve in range(firstValve, lastValve+1):
            caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-FB', x)
            extra = [  'CTRL-SUP-BOY:VC'+ str(iValve) + '-FOMD'
                     , 'CTRL-SUP-BOY:VC'+ str(iValve) + '-LOMD'
                     , 'CTRL-SUP-BOY:VC'+ str(iValve) + '-MAMD'
                     , 'CTRL-SUP-BOY:VC'+ str(iValve) + '-AUMD']
            output = (0 if caget(extra[x % len(extra)]) else 1)
            caput(extra[x % len(extra)], output)
        sleep(scan)
    
    sleep(tempo)
    for iValve in range(firstValve, lastValve+1):
        caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-CO', close)
    
    for x in range(int(open), int(close)-1, -1):
        for iValve in range(firstValve, lastValve+1):
            caput('CTRL-SUP-BOY:VC'+ str(iValve) + '-FB', x)
            extra = [  'CTRL-SUP-BOY:VC'+ str(iValve) + '-INTLK'
                     , 'CTRL-SUP-BOY:VC'+ str(iValve) + '-TRIP'
                     , 'CTRL-SUP-BOY:VC'+ str(iValve) + '-IOSIM'
                     , 'CTRL-SUP-BOY:VC'+ str(iValve) + '-IOERR']
            output = (0 if caget(extra[x % len(extra)]) else 1)
            caput(extra[x % len(extra)], output)
        sleep(scan)
