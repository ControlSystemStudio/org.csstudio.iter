#!/bin/bash

#+======================================================================
# $HeadURL: https://svnpub.iter.org/codac/iter/codac/dev/units/m-css-archive/trunk/org.csstudio.archive.config.rdb/archive-configtool.sh $
# $Id: archive-configtool.sh 28709 2012-06-25 17:20:25Z zagara $
#
# Project       : CODAC Core System
#
# Description   : Wrapper script for JMS Send command line tool
#
# Author(s)     : Takashi Nakamoto, Cosylab
#                 Anze Zagar, Cosylab
#
# Copyright (c) : 2010-2013 ITER Organization,
#                 CS 90 046
#                 13067 St. Paul-lez-Durance Cedex
#                 France
#
# This file is part of ITER CODAC software.
# For the terms and conditions of redistribution or use of this software
# refer to the file ITER-LICENSE.TXT located in the top level directory
# of the distribution package.
#
#-======================================================================

CODAC_ROOT=$(readlink -f "$0" | sed -n -e 's|^\(/[^/]*/[^/]*\)/.*|\1|p')
. ${CODAC_ROOT}/bin/codacenv
. ${CODAC_ROOT}/bin/css-wrapper-script

