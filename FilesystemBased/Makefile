############################################################################
#                                                                          #
#                                                                          #
#  Copyright (c) 2005 by Timothy W Macinta, All Rights Reserved.           #
#                                                                          #
#  This file can be used to compile the Fast MD5 distribution on Linux.    #
#                                                                          #
#  This library is free software; you can redistribute it and/or           #
#  modify it under the terms of the GNU Library General Public             #
#  License as published by the Free Software Foundation; either            #
#  version 2.1 of the License, or (at your option) any later version.      #
#                                                                          #
#  This library is distributed in the hope that it will be useful,         #
#  but WITHOUT ANY WARRANTY; without even the implied warranty of          #
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU       #
#  Library General Public License for more details.                        #
#                                                                          #
#  You should have received a copy of the GNU Library General Public       #
#  License along with this library; if not, write to the Free              #
#  Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.      #
#                                                                          #
#  See http://www.twmacinta.com/myjava/fast_md5.php for more information   #
#  on this package.                                                        #
#                                                                          #
############################################################################

# Adapted version for Nio and other improvements
# @author Frederic Bregier

TARGLINUX=linux_x86/MD5.so
TARGPPC=darwin_ppc/MD5.jnilib
TARGWINDOWS=win32_x86/MD5.dll
TARGAIX=aix53_ppc64/MD5.so

TARGET=../lib/arch/

# change this to match your system
JAVADIR=/cygdrive/c/Program\ Files/Java/jdk1.6.0_12/
TSYST=${TARGET}${TARGWINDOWS}
CPPFLAGS=-I. -g -O3

#WINDOWS using MinGW
TINCLUDE=-I$(JAVADIR)include/win32
CPPSHAREDFLAG=-O3 -shared -mwin32 -march=prescott -Wl,--add-stdcall-alias 

#LINUX
#TINCLUDE=-I$(JAVADIR)include/linux
#CPPSHAREDFLAG=-O3 -shared

# AIX
#TINCLUDE=
#CPPSHAREDFLAG=-maix64 -mcpu=power4 -O3 -shared

JAVABINDIR=$(JAVADIR)bin/
JAVAC=$(JAVABINDIR)javac
JAVAH=$(JAVABINDIR)javah


all: all_objs

clean:
	rm -f $(call toShellArg,$(ALL_OBJS))


ALL_OBJS= \
	$(ALL_CLASSES) \
	${TSYST} \
	goldengate/ftp/filesystembased/digest/CLIB/MD5.h

ALL_CLASSES= \
	../bin/goldengate/ftp/filesystembased/digest/MD5.class \
	../bin/goldengate/ftp/filesystembased/digest/MD5State.class

../bin/goldengate/ftp/filesystembased/digest/MD5.class: goldengate/ftp/filesystembased/digest/MD5.java
	$(JAVAC) -d ../bin/goldengate/ftp/filesystembased/digest/ goldengate/ftp/filesystembased/digest/MD5.java

../bin/goldengate/ftp/filesystembased/digest/MD5State.class: goldengate/ftp/filesystembased/digest/MD5State.java
	$(JAVAC) -d ../bin/goldengate/ftp/filesystembased/digest/ goldengate/ftp/filesystembased/digest/MD5State.java

goldengate/ftp/filesystembased/digest/CLIB/MD5.h: ../bin/goldengate/ftp/filesystembased/digest/MD5.class
	$(JAVABINDIR)javah -classpath ../bin -force -o goldengate/ftp/filesystembased/digest/CLIB/MD5.h -jni goldengate.ftp.filesystembased.digest.MD5

${TSYST}: goldengate/ftp/filesystembased/digest/CLIB/MD5.c goldengate/ftp/filesystembased/digest/CLIB/MD5.h
	gcc ${CPPSHAREDFLAG} -I$(JAVADIR)include ${TINCLUDE} goldengate/ftp/filesystembased/digest/CLIB/MD5.c -o ${TSYST}

all_objs: $(ALL_OBJS)

