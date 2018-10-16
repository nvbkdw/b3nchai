/*
 * ASCLITE
 * Author: Jerome Ajot, Jon Fiscus, Nicolas Radde, Chris Laprun
 *
 * This software was developed at the National Institute of Standards and Technology by 
 * employees of the Federal Government in the course of their official duties. Pursuant
 * to title 17 Section 105 of the United States Code this software is not subject to
 * copyright protection and is in the public domain. ASCLITE is an experimental system.
 * NIST assumes no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or any other
 * characteristic. We would appreciate acknowledgement if the software is used.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS."  With regard to this software, NIST MAKES NO EXPRESS
 * OR IMPLIED WARRANTY AS TO ANY MATTER WHATSOEVER, INCLUDING MERCHANTABILITY,
 * OR FITNESS FOR A PARTICULAR PURPOSE.
 */

#ifndef TRN_INPUTPARSER_H
#define TRN_INPUTPARSER_H

#include "linestyle_inputparser.h" // inheriting class's header file

/**
 * Class that handle the parsing of TRN encoded file
 */
class TRNInputParser : public LineStyleInputParser
{
	public:
		// class constructor
		TRNInputParser() {}
		// class destructor
		virtual ~TRNInputParser() {}
		/*
         * Load the named file into a Speech element.
         * Create a segment for each TRN line
         */
        SpeechSet* loadFile(const string& name);
  
    private:
        static Logger* logger;
};

#endif // TRN_INPUTPARSER_H
