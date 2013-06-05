
jpp
===========

jpp is a preprocessor for Java source code.

You add special comments to the code, then run the preprocessor.  
It manipulates the source
code in various ways; for example, it allows you to generate a sequence of 
numeric constants in a convenient way, similar to the C language's enum instruction.


Example Usage
-------------

Consider the following Java source file, Test.java (it's not syntactically correct, but serves to 
illustrate the operation of the preprocessor):


		  aaaa // This represents code that should be copied without changing
		  bbbb
		  
		  // Generate integer constants, with public scope, starting with zero
		  /*! .enum alpha beta gamma */  //!
		  
		  cccc
		  
		  // Generate integer constants, with private scope, starting with 23;
		  // have step size at beta be 5 instead of the default 1
		  /*!  .enum 23 .private  alpha beta 5 gamma delta */ //!
		  
			// Generate integer constants, with associated lengths (i.e. step sizes)
		  /*! .enum  27 .withlengths alpha 5 beta gamma 12 delta epsilon 3  */
		     ...this will be replaced with generated code...
		  //!
		    
		  dddd
		  eeee
		  
		  // Generate strings; parse space-delimited text and place within double quotes
		  /*! .strs alpha beta gamma delta "two words" epsilon */
		  //!
		  
		   // Generate characters; parse space-delimited text and place within single quotes
		  /*! .chars alpha beta gamma delta "two words" epsilon */
		  //!
		  
		  ffff
		  
		  // Generate string array, with particular name
		  /*! .strs 
		        .name StringName 
		        .private
		        
		        alpha beta gamma delta "two words" epsilon */
		  //!
		  
		  // Generate string representing verbatim contents 
		  // (note that program is confused if non-ASCII characters appear in the verbatim text)
		  /*! .vb  .name Gettysburgh
					Four score and seven years ago our fathers brought forth on this continent a new nation, 
					conceived in liberty, and dedicated to the proposition that all men are created equal.
		
					Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived 
					and so dedicated, can long endure. We are met on a great battlefield of that war. We have come 
					to dedicate a portion of that field, as a final resting place for those who here gave their lives 
					that that nation might live. It is altogether fitting and proper that we should do this.
		    */
		    //!
		  
		   gggg
		   hhhh

The preprocessor is run on this file by typing:


			java -jar jpp.jar Test.java
			
	
Afterwards, the contents of Test.jar have changed to:

		  aaaa // This represents code that should be copied without changing
		  bbbb
		  
		  // Generate integer constants, with public scope, starting with zero
		  /*! .enum alpha beta gamma */
		    public static final int ALPHA            = 0;   
		    public static final int BETA             = 1;   
		    public static final int GAMMA            = 2;   
		//!
		  
		  cccc
		  
		  // Generate integer constants, with private scope, starting with 23;
		  // have step size at beta be 5 instead of the default 1
		  /*!  .enum 23 .private  alpha beta 5 gamma delta */
		    private static final int ALPHA            = 23;  
		    private static final int BETA             = 24;  
		    private static final int GAMMA            = 29;  
		    private static final int DELTA            = 30;  
		//!
		  
			// Generate integer constants, with associated lengths (i.e. step sizes)
		  /*! .enum  27 .withlengths alpha 5 beta gamma 12 delta epsilon 3  */
		
		    public static final int ALPHA            = 27;  
		    public static final int ALPHA_LEN        = 5;   
		    public static final int BETA             = 32;  
		    public static final int BETA_LEN         = 1;   
		    public static final int GAMMA            = 33;  
		    public static final int GAMMA_LEN        = 12;  
		    public static final int DELTA            = 45;  
		    public static final int DELTA_LEN        = 1;   
		    public static final int EPSILON          = 46;  
		    public static final int EPSILON_LEN      = 3;   
		//!
		    
		  dddd
		  eeee
		  
		  // Generate strings; parse space-delimited text and place within double quotes
		  /*! .strs alpha beta gamma delta "two words" epsilon */
		      "alpha","beta","gamma","delta","two words","epsilon"
		//!
		  
		   // Generate characters; parse space-delimited text and place within single quotes
		  /*! .chars alpha beta gamma delta "two words" epsilon */
		      'a','l','p','h','a','b','e','t','a','g','a','m','m','a',
		      'd','e','l','t','a','t','w','o',' ','w','o','r','d','s',
		      'e','p','s','i','l','o','n'
		//!
		  
		  ffff
		  
		  // Generate string array, with particular name
		  /*! .strs 
		        .name StringName 
		        .private
		        
		        alpha beta gamma delta "two words" epsilon */
		private static final String[] StringName = {  
		      "alpha","beta","gamma","delta","two words","epsilon"
		};
		//!
		  
		  // Generate string representing verbatim contents 
		  // (note that program is confused if non-ASCII characters appear in the verbatim text)
		  /*! .vb  .name Gettysburgh
					Four score and seven years ago our fathers brought forth on this continent a new nation, 
					conceived in liberty, and dedicated to the proposition that all men are created equal.
		
					Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived 
					and so dedicated, can long endure. We are met on a great battlefield of that war. We have come 
					to dedicate a portion of that field, as a final resting place for those who here gave their lives 
					that that nation might live. It is altogether fitting and proper that we should do this.
		    */
		public static final String Gettysburgh = 
		      "Four score and seven years ago our fathers brought forth on this continent a new nation, \n"
		      +"conceived in liberty, and dedicated to the proposition that all men are created equal.\n"
		      +"\n"
		      +"Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived \n"
		      +"and so dedicated, can long endure. We are met on a great battlefield of that war. We have come \n"
		      +"to dedicate a portion of that field, as a final resting place for those who here gave their lives \n"
		      +"that that nation might live. It is altogether fitting and proper that we should do this.\n"
		;
		//!
		  
		   gggg
		   hhhh

The preprocessor can be run on entire directories of source files.  For additional help, type:

		java -jar jpp.jar -h

Syntax of jpp directives
---------------------

Each preprocessor directive starts with a special multiline comment beginning with "/*!".
Each directive must be terminated with a single line comment "//!".  All the code between these two
comments is replaced by code generated by the preprocessor.

Source backups
---------------
For safety, the old source file (if it differs from the new one) is saved in a directory structure
in your home directory named ".jppbackups".  It only saves the most recent several versions of a particular file.
