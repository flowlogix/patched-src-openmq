#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#
# This is a developer edition for internal use.
# imqkeytool is a wrapper script for JDK Keytool for generation of a keystore
# and a self signed certificate for use with SSL.
#
# To generate keystore and self signed certificate for the broker
# usage: imqkeytool [-broker]
#
# To generate keystore and a self-signed certificate for the HTTPS tunnel
# servlet
# usage: imqkeytool -servlet <keystore location>
#

#
# print usage
#
usage() {
    echo ""
    echo "usage:"
    echo "imqkeytool [-broker]"
    echo "   generates a keystore and self-signed certificate for the broker"
    echo ""
    echo "imqkeytool -servlet <keystore_location>"
    echo "   generates a keystore and self-signed certificate for the HTTPS"
    echo "   tunnel servlet, keystore_location specifies the name and location"
    echo "   of the keystore file"
    echo ""
    exit 1
}

# file is stored (by default) in /etc/imq/keystore
#
# The admin script looks in a few set locations for
# java e.g. it doesnt try to automatically pick up
# new versions of the JDK on linux (unlike imqbrokerd)
#
# The scripts do not verify the specific version of
# the JDK (unlike imqbrokerd)

bin_home=`/usr/bin/dirname $0`

# set keystore_home
imq_home=$bin_home/..
keystore_home=$imq_home/etc

# ######hpux-dev#####
PLATFORM=`uname`
ARCH=`uname -p`
if [ $PLATFORM = HP-UX ]; then
    javahome=${_JAVA_HOME:-/opt/java6}
elif [ $PLATFORM = Darwin ]; then
    javahome=${_JAVA_HOME:-/Library/Java/Home}
elif [ $PLATFORM = AIX ]; then
    javahome=${_JAVA_HOME:-/usr/java6}
elif [ $PLATFORM = SunOS ]; then
    javahome=${_JAVA_HOME:-/usr/jdk/latest}
elif [ $PLATFORM = Linux ]; then
    javahome=${_JAVA_HOME:-/usr/java/latest}
fi

cmd=broker
while [ $# != 0 ]; do
  case "$1" in
    -broker) cmd="broker"; shift ;;
    -servlet)
	cmd="servlet"
	if [ $# = 1 ]; then
	    echo "Please specify keystore location for the -servlet option"
	    usage
	else
	    keystore=$2
	    shift 2
	fi
	;;
    -javahome) javahome=$2; shift 2;;
    *)  usage ;;
  esac
done

if [ $cmd = broker ]; then
    #
    # generate keystore and certificate for the broker
    #
    echo "Generating keystore for the broker ..."

    if [ ! -w $keystore_home ]; then
    	/bin/echo "You do not have permission to create the keystore file in"
    	/bin/ls -ld $keystore_home
    	exit 1
    fi

    keystore=$keystore_home/keystore
    echo Keystore=$keystore

    # Generate the keypair. This also wraps them in a self-signed certificate
    $javahome/bin/keytool -genkey -keyalg "RSA" -alias imq -keystore $keystore -v 
    status=$?

    #Make sure keystore is readable by everybody so broker can read it
    chmod a+r $keystore

elif [ $cmd = servlet ]; then
    #
    # generate keystore and certificate for the HTTPS tunnel servlet
    #
    echo "Generating keystore for the HTTPS tunnel servlet ..."

    keystoredir=`/usr/bin/dirname $keystore`
    if [ ! -w $keystoredir ]; then
    	/bin/echo "You do not have permission to create the keystore file in"
    	/bin/ls -ld $keystoredir
    	exit 1
    fi

    echo Keystore=$keystore

    # Generate the keypair. This also wraps them in a self-signed certificate
    $javahome/bin/keytool -genkey -keyalg "RSA" -alias imqservlet -keystore $keystore -v 
    status=$?
    if [ $status = 0 ]; then
    	echo "Make sure the keystore is accessible and readable by"
    	echo "the HTTPS tunnel servlet."
    fi

else
    #
    # print usage
    #
    usage
fi
exit $status
