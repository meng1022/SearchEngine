Name: Meng Zhao

WatIAM: m235zhao


## BM25 retrieval with stemming
1) produce_stem: Under this folder is code for producing inverted index, lexicon and other needed files with stemming.
2) BM_stem: code for implement BM25 with stemming 

## BM25 retrieval without stemming
1) produce_wostem: Under this folder is code for producing inverted index, lexicon and other needed files without stemming.
2) BM_wostem: code for implement BM25 without stemming 

## 5 effectiveness measures
1) effectiveness_measures: code for compute AP, P@10, nDCG@10, nDCG@1000 and TBG for each topic

## Compiling and running instructions of retrieval without stemming
1) produce_wostem: 

    compiling: produce_wostem>javac DocProcessor.java IndexEngine.java
    
    running: produce_wostem>java IndexEngine [path to latimes.gz] [path of directory to store generated inverted index and other files]
2) BM_wostem:
 
    compiling: BM_wostem>javac BM25.java
    
    running: BM_wostem>java BM25 [path of directory storing inverted index and other needed files(same as second argument of produce_wostem)]
    
3) effectiveness_measures:
    
    compiling: effectiveness_measures>javac evaluate.java
    
    running: effectiveness_measures>java evaluate [path to the qrels] [path to store results file]

## Compiling and running instructions of retrieval with stemming
1) produce_stem: 

    compiling: produce_stem>javac DocProcessor.java IndexEngine.java PorterStemmer.java
    
    running: produce_stem>java IndexEngine [path to latimes.gz] [path of directory to store generated inverted index and other files]
2) BM_stem:
 
    compiling: BM_stem>javac BM25.java PorterStemmer.java
    
    running: BM_stem>java BM25 [path of directory storing inverted index and other needed files(same as second argument of produce_stem)]
    
3) effectiveness_measures:
    
    compiling: effectiveness_measures>javac evaluate.java
    
    running: effectiveness_measures>java evaluate [path to the qrels] [path to store results file]
