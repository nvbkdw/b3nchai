# **********************************************************
# Makefile for rfilter1 Directory
# **********************************************************
SHELL = /bin/sh
PREFIX=/Users/ruoyuhuang/myworkspace/b3nchai/score/sctk-2.4.10
bindir = $(PREFIX)/bin
T=../test_suite

all: md-eval.pl

md-eval.pl: 
	chmod +x md-eval.pl

check: all
	@echo "No Tests for md-eval.pl"

install: all
	cp md-eval.pl $(bindir)

clean:

distclean: clean
	rm -r *~ makefile
