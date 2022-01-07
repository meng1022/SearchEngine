# msci-541-720-hw2-meng1022

Name: Meng Zhao  

WatIAM: m235zhao

Student ID: 20897701

### hw2-1: Modified IndexEngine Program

Compiling:	 .\hw2-1\src\hw2>javac DocProcessor.java IndexEngine.java

Running:	   .\hw2-1\src>java hw2.IndexEngine argument1 argument2

If there is error: java.lang.OutOfMemoryError: Java heap space, the Running command should be: 

.\hw2-1\src>java -Xmx1000m hw2.IndexEngine argument1 argument2

### hw2-2: BooleanAnd Program

Compiling:	 .\hw2-2\src>javac BooleanAND.java

Running:	   .\hw2-2\src>java BooleanAND argument1 argument2 argument3

Note: argument2 should be queries.txt, which is stored under .\hw2-2\src

argument3 for me should be “hw2-results-m235zhao.txt”, which will also be created under the .\hw2-2\src path.

### hw2: 45 Queries Results for Topic 401-450

Results have been stored at .\hw2-2\src\hw2-results-m235zhao.txt

### hw2: SmallTestdata_And_Result

This is the small document collcetion for testing which includes: 

1) the smallcollection.gz file 

2) a folder smalldata holds separate documents, metadata, doclengths, map between docno and docid, lexicon and inverted index 

3) the smallqueries.txt file contains query topics 

4) the query results m235zhao-hw2-smallresults.txt

### hw1-1: Old IndexEngine Program

Compiling:   .\hw1-1\src\hw1>javac DocProcessor.java MyGZFile.java IndexEngine.java

Running:     .\hw1-1\src>java hw1.IndexEngine argument1 argument2


### hw1-2: GetDoc Program

Compiling:   .\hw1-2\src\hw1>javac GetDoc.java

Running:     .\hw1-2\src>java hw1.GetDoc argument1 argument2 argument3
