# **********************************************************
# Makefile for rfilter1 Directory
# **********************************************************
SHELL = /bin/sh
PREFIX=/Users/ruoyuhuang/myworkspace/b3nchai/score/sctk-2.4.10
bindir = $(PREFIX)/bin
T=../test_suite
PERL=/slp/sware/Linux/bin/perl

all: def_art.pl

def_art.pl: 
	chmod +x def_art.pl

setTests:

check: all
	@echo "Testing def_art.pl"
	@echo "   def_art.pl passed without tests"

install: all
	cp def_art.pl $(bindir)

clean:

distclean: clean
	rm -r *~ makefile
