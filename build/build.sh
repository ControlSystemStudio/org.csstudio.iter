#!/bin/bash

## function from Kay's make.sh used to fix the permission of the css executable for linux and macosx
function patch_product
{
    # Args: original-zip-name  product-name executable final-zip-name
    orig=$1
    product=$2
    exe=$3
    final=$4
    
    # Unzip the file generated by headless build
    unzip -q $orig
    
	# With a headless build in 3.5, the OS X and Linux launchers were not
	# marked executable. https://bugs.eclipse.org/bugs/show_bug.cgi?id=260844 ?
	# With 3.6, this seems no longer necessary, but it can't hurt, either.
    chmod +x $product/$exe

    # Create new ZIP
    rm -f $final
    zip -qr $final $product
    
    # Cleanup
    rm -rf $product
#    rm $orig
    
    echo Created $final
}

# Check parameters
PRODUCT=$1
BUILD="build"
if [ -z "$PRODUCT" ]
then 
  echo You must provide the product
exit -1
fi

# Clean up
rm -rf $BUILD

# Install Eclipse if not there
if [[ -d ext/eclipse ]]
then
  echo Eclipse alredy installed
else
  mkdir -p ext
  cd ext
  if [[ ! -f eclipse-rcp-helios-SR1-linux-gtk.tar.gz ]]
    then
    wget http://ftp.osuosl.org/pub/eclipse//technology/epp/downloads/release/helios/SR1/eclipse-rcp-helios-SR1-linux-gtk.tar.gz
    fi
  if [[ ! -f eclipse-3.6.1-delta-pack.zip ]]
  then
    wget http://download.eclipse.org/eclipse/downloads/drops/R-3.6.1-201009090800/eclipse-3.6.1-delta-pack.zip
  fi
  tar -xzvf eclipse-rcp-helios-SR1-linux-gtk.tar.gz
  unzip -o eclipse-3.6.1-delta-pack.zip
  cd ..
fi

# Copy product sources
cp -R ../products/$PRODUCT $BUILD
cat $BUILD/plugins.list | xargs -i cp -R ../core/plugins/{} $BUILD/plugins
cat $BUILD/plugins.list | xargs -i cp -R ../applications/plugins/{} $BUILD/plugins
cat $BUILD/features.list | xargs -i cp -R ../applications/features/{} $BUILD/features
mkdir $BUILD/BuildDirectory
cd $BUILD
mv features BuildDirectory
mv plugins BuildDirectory
cd ..

# Run the build
# XXX Doing it in the plugin directory: it was breaking otherwise
ABSOLUTE_DIR=$PWD
echo $ABSOLUTE_DIR
java -jar "$ABSOLUTE_DIR"/ext/eclipse/plugins/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar -application org.eclipse.ant.core.antRunner -buildfile "$ABSOLUTE_DIR"/ext/eclipse/plugins/org.eclipse.pde.build_3.5.2.R35x_20100114/scripts/productBuild/productBuild.xml -Dbuilder="$ABSOLUTE_DIR"/build -Dbuild.dir="$ABSOLUTE_DIR"

#cd ext/eclipse/plugins/org.eclipse.pde.build_3.5.2.R35x_20100114/scripts/productBuild
#java -jar ../../../../../../ext/eclipse/plugins/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar -application org.eclipse.ant.core.antRunner -buildfile productBuild.xml -Dbuilder="$ABSOLUTE_DIR/$BUILD" -Dbuild.dir="$ABSOLUTE_DIR"

# read properties from the build.properties and set up variable for each of them
TEMPFILE=$(mktemp)
cat build/build.properties |grep -v "#" |  grep 'buildId=\|archivePrefix=\|launchName=' |sed -re 's/"/"/'g| sed -re 's/=(.*)/="\1"/g'>$TEMPFILE
source $TEMPFILE
rm $TEMPFILE
echo $buildId $archivePrefix $launchName

cd $ABSOLUTE_DIR/build/BuildDirectory/I."$buildId"
if [[ -f "$buildId"-linux.gtk.x86.zip ]]
	then
	echo "found linux"
	patch_product "$buildId"-linux.gtk.x86.zip $archivePrefix $launchName "$buildId"-linux.gtk.x86.zip
	fi
if [[ -f "$buildId"-macosx.cocoa.x86.zip ]]
	then
	echo "found mac"
	patch_product "$buildId"-macosx.cocoa.x86.zip $archivePrefix "$launchName".app/Contents/MacOS/"$launchName" "$buildId"-macosx.cocoa.x86.zip
	fi
cd $ABSOLUTE_DIR
