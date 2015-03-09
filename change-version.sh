#!/bin/sh

#*******************************************************************************
# * Copyright (c) 2010-2015 ITER Organization.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# ******************************************************************************/

# Check parameters
ver=$1
if [ -z "$ver" ]
then 
  echo You must provide the product version \(e.g. \"change-version 3.2.4\"\)
exit -1
fi

function update() {
    product=$1
    product_file=$2
    echo ::: Update ${product_file} :::
    sed -i "s|\(version=\"\)[^<>]*\(.qualifier\"\)|\1${ver}\2|" products/org.csstudio.iter.${product}.product/${product_file}.product
    sed -i "s|\(<version>\)[^<>]*\(-SNAPSHOT</version>\)|\1${ver}\2|" products/org.csstudio.iter.${product}.product/pom.xml
    sed -i "s|\(Bundle-Version: \)[^<>]*\(.qualifier\)|\1${ver}\2|" products/org.csstudio.iter.${product}.product/META-INF/MANIFEST.MF
    sed -i "s|\(version=\"\)[^<>]*\(.qualifier\"\)|\1${ver}\2|" repository/org.csstudio.iter.product.${product}.product
}

update "alarm.beast.server" "alarm-server"
update "alarm.beast.configtool" "alarm-configtool"
update "alarm.beast.annunciator" "alarm-annunciator"
update "alarm.beast.notifier" "alarm-notifier"
update "archive.config.rdb" "archive-configtool"
update "archive.engine" "archive-engine"
update "css" "iter-css"
update "jms2rdb" "jms2rdb"
update "scan.server" "scan-server"
update "utility.jmssendcmd" "jms-send"

