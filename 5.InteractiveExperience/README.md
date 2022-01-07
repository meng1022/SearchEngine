## Interactive Experience

* [Running_instruction](#running_instruction)
* [How_to_use_this_search_engine](#how_to_use_this_search_engine)
* [Ways_to_compute_snippet](#ways_to_compute_snippet)
* [Examples](#examples)

### Running_Instruction 
```
InteractiveExperience> javac run.java
```
```
InteractiveExperience >java run [path to store generated files(same as that in "Tokenization&PostingsList&BooleanAND")]
```

### How_to_use_this_search_engine
* After running the program, user is allowed to type anything as a query up to pressing the Enter key, which submits the query to the engine.

* After receiving a query, the engine would perform BM25 retrieval with the standard parameters used in [BM25](../4\.BM25_Retrieval/README.md)

* The engine then would display the top 10 ranked results to the user. Each result is displayed in format: Rank. Headline (Date) Query-Biased Snippet (Docno)

* After displaying the results, the engine would report the length of time it took to perform the retrieval and compute the summaries.

* After display the time, the engine would prompt the user to either type in the number of a document to view or type "N" for "new query" or "Q" for "quit". 

    If the user types a valid number, the engine should then display the full document from the document store, and then repeat this step and prompt the user again. 
    
    If the user enters N, the system goes back to step B and prompts for a query. 
    
    If the user types Q, the program exits.

### Ways_to_compute_snippet
For each document in the result list:
* Strip the tags. In my implementation, I extract the sentences within specific tags, for the following document, split text inside “TEXT” and “GRAPHIC” on certain stops: . ? ! without removing the stops, and get several sentences, and the headline inside “HEADLINE” tag is considered as a single sentence whether or not there is a stop at the end of the headline.
    ```
    <HEADLINE><P>headline</P></HEADLINE>; 
    <TEXT><P>sentence 1</P><P> Sentence 1</P></TEXT>; 
    <GRAPHIC>sentence3<GRAPHIC>.
    ```
* Score each sentence, for example, a sentence S containing n words: S=[w_1,w_2,…,w_n],  using this scoring function: 
    ```
    V(S)=l+2*c+3*d+4*k
    ```
    where l=2 if S is the first sentence in the document, l=1 if S is the second sentence, and l=0 in other cases;
c=# ( words in S that are query terms including repetitions);
d=# ( distinct query terms matching some words in S);
k= the longest contiguous ran of query terms in S;
If a parameter has more influence on helping users better discern the value of the document with respect to their queries(info need), it will be given higher weight. The highest weight is assigned to k, because in my point of view, k is much more important than the other parameters.
	
* Choose two sentences with highest scores as the summary of this document.


### Examples
[Demo_vid1](./videos/example1.mp4)

[Demo_vid2](./videos/example2.mp4)