
SCALA_HOME = /scala/inst/27
SDIR = /local/lib/scala
JDIR = /local/lib/java
GNUMAIL = $(JDIR)/activation.jar:$(JDIR)/gnumail.jar:$(JDIR)/inetlib.jar:$(JDIR)/gnumail-providers.jar
JAVAMAIL = $(JDIR)/javamail/mail.jar
JARS = $(SDIR)/specs.jar:$(SDIR)/specs-tests.jar:$(SDIR)/scalacheck-1.5.jar:$(SCALA_HOME)/lib/scala-library.jar:$(JDIR)/junit-4.4.jar:$(JDIR)/cglib.jar:$(JDIR)/asm-1.5.3.jar:$(JDIR)/objenesis-1.1.jar:$(JDIR)/hamcrest-all-1.1.jar:$(JDIR)/jmock-2.4.0.jar:$(SDIR)/scalatest.jar:$(JDIR)/textile-j.jar:$(JDIR)/jna.jar:$(GNUMAIL):$(JDIR)/tagsoup.jar:$(JDIR)/base64.jar

CP = -cp ./out:$(JARS)

JAVA_OPTS = -Xcheck:jni -d32
# JNAPATH = -Djna.library.path=/Library/Filesystems/fusefs.fs/Support/fusefs.kext/Contents/MacOS/fusefs
# MISCOPTS = -XstartOnFirstThread -Xint -Xrs -Xcheck:jni -d32
JNADUMP = -Djna.dump_memory=true
SCALAC = $(SCALA_HOME)/bin/scalac -deprecation $(CP) -d out
JAVAC = javac -Xlint:unchecked -g $(CP) -d out
JAVA = java -XstartOnFirstThread -d32 $(CP) $(JNADUMP)
SCALA = $(SCALA_HOME)/bin/scala $(CP)
ALLSRC = `find src/scala src/java src/test -name '*.scala' -o -name '*.java'`
JAVASRC = `find src/java src/test -name '*.java'`
# FUSEOPTS = -o direct_io -d /mnt
# FUSEOPTS = -d /mnt
FUSEOPTS = -o kill_on_unmount -f -d /mnt
JARFSPATH = /local/lib/java

build:
	make scala

all:
	make prep
	make java
	make scala
	make java

prep:
	$(SCALAC) src/scala/CSizes.scala
	bin/makeFuseOps > src/scala/GeneratedFuse.scala

scala:
	$(SCALAC) $(ALLSRC)
	
helloscala:
	$(SCALA) org.improving.fuse.HelloScala $(FUSEOPTS)

go:
	echo $(SCALA)

jarfs:
	# env JARFSPATH="/local/lib/java" $(SCALA) org.improving.fuse.JarFS -f -d /mnt
	env JARFSPATH=$(JARFSPATH) $(SCALA) org.improving.fuse.JarFS $(FUSEOPTS)
	# $(SCALA) org.improving.fuse.JarFS -f -d /mnt
	# export JAVA_OPTS="$(JAVA_OPTS)"
	# $(SCALA) org.improving.fuse.JarFS /local/lib/java/ant.jar -f /mnt &

stat:
	$(SCALA) org.improving.fuse.Stat $(ARGS)

java:
	$(JAVAC) $(JAVASRC)
	
loopback:
	$(SCALA) org.improving.fuse.Loopback $(FUSEOPTS)
	# $(SCALA) org.improving.fuse.Scuse /mnt
	# $(JAVA) org.improving.fuse.Hello /mnt

fuse:
	./ext/macfuse/core/macfuse_buildtool.sh -c Debug -t lib
	rsync -av /tmp/fuse-2.7.3/lib/.libs/* lib/

int:
	$(SCALA_HOME)/bin/scala $(CP) -i etc/interpreter.txt

test:
	java $(CP) org.improving.fuse.test.allSpecs

clean:
	rm -rf out
	mkdir out
	umount /mnt
