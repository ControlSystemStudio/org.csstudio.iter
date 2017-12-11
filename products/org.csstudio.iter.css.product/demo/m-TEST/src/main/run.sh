#!/bin/bash

function softIoc {
    ######################################################################
    # Run the IOCs
    ######################################################################
    
    echo -e "\n\tStart the IOCs...\n"
    
    tab=" --tab"
    options=" --profile='default'"
    
    cmds[1]="bash -c 'softIoc -d ./epics/CTRL-SUPApp/Db/PSH0-CTRL-SUP-CSS.db'"
    titles[1]="PSH0-CTRL-SUP-CSS.db"
    
    cmds[2]="bash -c 'softIoc -d ./epics/CTRL-SUPApp/Db/PSH0-CTRL-SUP-BOY.db'"
    titles[2]="PSH0-CTRL-SUP-BOY.db"
    
    cmds[3]="bash -c 'softIoc -d ./epics/CTRL-SUPApp/Db/PSH0-CTRL-SUP-BEAS.db'"
    titles[3]="PSH0-CTRL-SUP-BEAS.db"
    
    cmds[4]="bash -c 'softIoc -d ./epics/CTRL-SUPApp/Db/PSH0-CTRL-SUP-BEAU.db'"
    titles[4]="PSH0-CTRL-SUP-BEAU.db"
    
    cmds[5]="bash -c 'softIoc -d ./epics/UTIL-S15App/Db/PSH0-UTIL-S15-0000.db'"
    titles[5]="PSH0-UTIL-S15-0000.db"
     
    for i in {1..5}; do
        options+=($tab -t "\"${titles[i]}\"" -e "\"${cmds[i]}\"")
    done
}

function alarm {
    ######################################################################
    # Configure and start the alarm system
    ######################################################################
    
    echo -e "\n\tConfigure and start the alarm system...\n"
    
    tab=" --tab"
    options=" --profile='default'"
    
    cmds[1]="bash -c 'alarm-configtool -root demo -import -file ./beast/CTRL-beast.xml ; alarm-server -root demo'"
    titles[1]="alarm-server -root demo"
    
    cmds[2]="bash -c 'alarm-configtool -root UTIL -import -file ./beast/UTIL-beast.xml ; alarm-server -root UTIL'"
    titles[2]="alarm-server -root UTIL"
    
    for i in {1..2}; do
        options+=($tab -t "\"${titles[i]}\"" -e "\"${cmds[i]}\"")
    done
}

function archive {
    ######################################################################
    # Configure and start the archive system
    ######################################################################
    
    echo -e "\n\tConfigure and start the archive system...\n"
    
    tab=" --tab"
    options=" --profile='default'"
    
    cmds[1]="bash -c 'archive-configtool -engine demo -port 5812 -import -config ./beauty/CTRL-beauty.xml -replace_engine; archive-engine -engine demo -port 5812'"
   titles[1]="archive-engine -engine demo -port 5812"
    
    for i in {1..1}; do
        options+=($tab -t "\"${titles[i]}\"" -e "\"${cmds[i]}\"")
    done
}

date

softIoc

alarm

archive

CMD="gnome-terminal "${options[@]}""
echo -e "$CMD"
eval "$CMD"

exit 0
