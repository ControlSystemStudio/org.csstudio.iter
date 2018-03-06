#!/bin/sh

#*******************************************************************************
# * Copyright (c) 2010-2018 ITER Organization.
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
    if [ "$3" = "0" ]; then
      repository="repository"
    else
      repository="repository-rcp"
    fi
    echo ::: Update ${product_file} :::
    sed -i "s|\(version=\"\)[^<>]*\(.qualifier\"\)|\1${ver}\2|" products/org.csstudio.iter.${product}.product/${product_file}.product
    sed -i "s|\(<version>\)[^<>]*\(-SNAPSHOT</version>\)|\1${ver}\2|" products/org.csstudio.iter.${product}.product/pom.xml
    sed -i "s|\(Bundle-Version: \)[^<>]*\(.qualifier\)|\1${ver}\2|" products/org.csstudio.iter.${product}.product/META-INF/MANIFEST.MF
    sed -i "s|\(version=\"\)[^<>]*\(.qualifier\"\)|\1${ver}\2|" ${repository}/org.csstudio.iter.product.${product}.product
}

function updateVersion() {
    pom_file=$1
    sed -i "s|\(<version>\)[^<>]*\(-SNAPSHOT</version>\)|\1${ver}\2|" ${pom_file}
}

update "alarm.beast.server" "alarm-server" 0
update "alarm.beast.configtool" "alarm-configtool" 0
update "alarm.beast.annunciator" "alarm-annunciator" 1
update "alarm.beast.notifier" "alarm-notifier" 1
update "archive.config.rdb" "archive-configtool" 0
update "archive.engine" "archive-engine" 0
update "css" "iter-css" 1
update "jms2rdb" "jms2rdb" 0
update "scan.server" "scan-server" 1
update "utility.jmssendcmd" "jms-send" 0
sed -i "s|\(version=\"\)[^<>]*\(.qualifier\"\)|\1${ver}\2|" repository-rcp/org.csstudio.iter.product.opivalidation.product
updateVersion "pom.xml"
updateVersion "features/pom.xml"
updateVersion "plugins/pom.xml"
updateVersion "products/pom.xml"
updateVersion "rap/pom.xml"
updateVersion "repository/pom.xml"
updateVersion "repository-rcp/pom.xml"
