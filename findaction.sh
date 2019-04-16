#!/bin/sh

here=.
cleohome=.
cleohome=$(cd $cleohome && pwd -P)
unset DISPLAY
classpath=$here/vlapi-hosttree-5.6.0.0-SNAPSHOT.jar:$(find $cleohome/lib -type d|sed 's|$|/*|'|paste -s -d : -):$cleohome/webserver/AjaxSwing/lib/ajaxswing.jar
(cd $cleohome; ./jre/bin/java -cp $classpath com.cleo.labs.vlapi.hosttree.FindAction "$@")
