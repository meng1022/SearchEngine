import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BM25 {
    public static void main(String args[]) {
        if(args.length!=2){
            System.out.println("ERROR:wrong number of arguments, " +
                    "the first argument should be the path of directory for your inverted index, lexicon and other needed files" +
                    ",the second argument should be the path of qrels");
        }
        /* path of directory where the lexicon, inverted index ... are stored */
        //D:/UWaterloo/2021Spring/MSCI720/hw4/data
        String dirpath = args[0];
        /* file name of queries(45 topics) */
        String queryfname = "queries.txt";
        /* path of qrels*/
        //D:/UWaterloo/2021Spring/MSCI720/hw3/hw3-files-2021/qrels/LA-only.trec8-401.450.minus416-423-437-444-447.txt
        String judgementfname = args[1];

        File dir = new File(dirpath);
        File queries = new File(queryfname);
        if (!dir.exists() || !queries.exists()) {
            System.out.println("ERROR: directory or file not exist");
            return;
        }
        try{
            LinkedHashMap<String,Integer> Lexicon = LoadLexicon(dirpath);
            LinkedHashMap<Integer,String> InvIndex = LoadInvIndex(dirpath);
            List<Integer> DocLength = LoadLength(dirpath);
            HashMap<Integer,String> IdDocno = Id2Docno(dirpath);
            HashMap<String,Integer> DocnoId = Docno2Id(dirpath);
            LinkedHashMap<Integer,List<Integer>> Qrels = LoadQrels(judgementfname,DocnoId);

            FileInputStream fis = new FileInputStream(queries);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            /* result files*/
            File resultfile = new File("hw4-bm25-stem-m235zhao.txt");
            if(!resultfile.exists())
                resultfile.createNewFile();
            FileOutputStream fos=new FileOutputStream(resultfile);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));

            String line = br.readLine();
            int topicID = 0;
            for (int i = 0; line != null; i++) {
                if (i % 2 == 0) {
                    line = line.replaceAll(" ", "");
                    topicID = Integer.valueOf(line);
                } else {
                    /* the term in query and its corresponding number*/
                    HashMap<String,Integer> queryterms = new HashMap<>();
                    Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        String term = matcher.group(0);
                        term = term.toLowerCase();
                        term = PorterStemmer.stem(term);
                        if(queryterms.containsKey(term))
                            queryterms.put(term,queryterms.get(term)+1);
                        else
                            queryterms.put(term,1);
                    }
                    /*results list of a query*/
                    List<String> results = getResults(Lexicon,InvIndex,DocLength,IdDocno,Qrels,topicID,queryterms);
                    for(String result : results){
                        bw.write(result);
                    }

                }
                line = br.readLine();
            }
            br.close();
            bw.close();
        }catch (FileNotFoundException e){
            System.out.println("ERROR: file not found");
            return;
        }catch (IOException e){
            System.out.println("ERROR: IOException");
            return;
        }
    }


    public static LinkedHashMap<String,Integer> LoadLexicon(String indexdir) throws IOException{
        String lexiconpath = indexdir+"/lexicon/lexicon_token2id.txt";
        LinkedHashMap<String,Integer> Lexicon = new LinkedHashMap<>();

        File lexiCon = new File(lexiconpath);
        FileInputStream fis=new FileInputStream(lexiCon);
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        String []s;
        while(line!=null){
            s=line.split(",");
            int termid = Integer.valueOf(s[1]);
            Lexicon.put(s[0],termid);
            line=br.readLine();
        }
        br.close();

        return Lexicon;
    }

    public static LinkedHashMap<Integer,String>LoadInvIndex(String indexdir)throws IOException{
        String invertindex = indexdir+"/invertedindex/invindex.txt";
        LinkedHashMap<Integer,String> InvIndex = new LinkedHashMap<>();
        File invIndex = new File(invertindex);
        FileInputStream fis=new FileInputStream(invIndex);
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));
        String line=br.readLine();
        int i=0;
        int termid=0;
        while(line!=null){
            int flag=i%2;
            if(flag==0){
                termid=Integer.valueOf(line);
            }
            else{
                InvIndex.put(termid,line);
            }
            line=br.readLine();
            i++;
        }
        br.close();
        return InvIndex;

    }

    public static List<Integer>LoadLength(String indexdir)throws IOException{
        String doclength = indexdir+"/doclengths.txt";
        List<Integer> docLengths = new ArrayList<>();

        File docLength = new File(doclength);
        FileInputStream fis=new FileInputStream(docLength);
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));
        String line=br.readLine();
        while(line!=null){
            docLengths.add(Integer.valueOf(line));
            line=br.readLine();
        }
        br.close();
        return docLengths;
    }

    public static HashMap<Integer,String> Id2Docno(String indexdir)throws IOException{
        String id2docno = indexdir+"/id2docno.txt";
        File id2Docno = new File(id2docno);
        FileInputStream fis=new FileInputStream(id2Docno);
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));

        HashMap<Integer,String> id2docnos = new HashMap<>();
        String line=br.readLine();
        String[] s;
        while(line!=null){
            s = line.split(",");
            id2docnos .put(Integer.valueOf(s[0]),s[1]);
            line=br.readLine();
        }
        br.close();
        return id2docnos;
    }

    public static HashMap<String,Integer> Docno2Id(String indexdir)throws IOException{
        String docno2id = indexdir+"/docno2id.txt";
        File docno2Id = new File(docno2id);
        FileInputStream fis=new FileInputStream(docno2Id);
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));

        HashMap<String,Integer> docno2ids = new HashMap<>();
        String line=br.readLine();
        String[] s;
        while(line!=null){
            s = line.split(",");
            docno2ids.put(s[0],Integer.valueOf(s[1]));
            line=br.readLine();
        }
        br.close();
        return docno2ids;
    }

    public static LinkedHashMap<Integer,List<Integer>> LoadQrels(String judgementfname,HashMap<String,Integer> DocnoId) throws IOException {
        File judgementf = new File(judgementfname);
        LinkedHashMap<Integer,List<Integer>> map= new LinkedHashMap<>();
        FileInputStream fis = new FileInputStream(judgementf);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        while(line!=null){
            String [] s = line.split(" ");
            if(s[3].equals("1")){
                int topicid = Integer.valueOf(s[0]);
                List<Integer> docids;
                if(map.containsKey(topicid)){
                    docids = map.get(topicid);
                }
                else{
                    docids = new ArrayList<>();
                }
                docids.add(DocnoId.get(s[2]));
                map.put(topicid,docids);
            }
            else;
            line = br.readLine();
        }
        return map;
    }

    public static List<String> getResults(LinkedHashMap<String,Integer> Lexicon,
                                          LinkedHashMap<Integer,String> InvIndex,
                                          List<Integer> DocLength,HashMap<Integer,String> Id2Docno,
                                          LinkedHashMap<Integer,List<Integer>> Qrels,
                                          int topicID, HashMap<String,Integer> queryterms){
        double k1 = 1.2; double b = 0.75; double k2 = 7;
        double avdl =0;
        for(int l:DocLength){
            avdl+=l;
        }
        avdl = avdl/DocLength.size();
        int N = Id2Docno.size();
        /* docid : score, the result list to be ranked*/
        HashMap<Integer,Double> rankinglist = new HashMap<>();
        /* for each term in queries */
        for(String term : queryterms.keySet()){
            if(Lexicon.containsKey(term)){
                int termid = Lexicon.get(term);
                String postings = InvIndex.get(termid);
                String s[] = postings.split(" ");
                /* docid : tf */
                LinkedHashMap<Integer,Integer> TermFrequency = new LinkedHashMap<>();
                int docid = 0;
                int tf;
                for(int i=0; i<s.length;i++){
                    if(i%2==0)
                        docid = Integer.valueOf(s[i]);
                    else{
                        tf = Integer.valueOf(s[i]);
                        TermFrequency.put(docid,tf);
                    }
                }

                /* number of documents containing term i and is relevant*/
                int ri=0;
                List<Integer> reldocids = Qrels.get(topicID);
                for(int dOcid: reldocids){
                    if(TermFrequency.containsKey(dOcid))
                        ri++;
                }
                /* number of documents that are relevant*/
                int R = reldocids.size();
                /* number of documents containing term i */
                int ni = TermFrequency.size();
                for(int docId : TermFrequency.keySet()){
                    //BM25_score
                    int dl = DocLength.get(docId-1);
                    double k = k1*((1-b)+b*(dl/avdl));
                    /* term frequency in document */
                    int fi = TermFrequency.get(docId);
                    /* term frequency in query */
                    int qfi = queryterms.get(term);
                    double a1 = (k1+1)*fi/(k+fi);
                    double a2 = (k2+1)*qfi/(k2+qfi);
                    //double a3 = Math.log((N-ni+0.5)/(ni+0.5));
                    double a3 = Math.log((ri+0.5)/(R-ri+0.5)/((ni-ri+0.5)/(N-ni-R+ri+0.5)));
                    double score = a1*a2*a3;
                    if(rankinglist.containsKey(docId)){
                        rankinglist.put(docId,rankinglist.get(docId)+score);
                    }
                    else{
                        rankinglist.put(docId,score);
                    }
                }
            }
            else;
        }

        // sort rankinglist in descending order of score
        LinkedHashMap<Integer,Double> rankedlist = new LinkedHashMap<>();
        rankinglist.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(s->rankedlist.put(s.getKey(),s.getValue()));

        List<String> results = new ArrayList<>();
        int size = (rankedlist.size()>1000)? 1000: rankedlist.size();
        String topicid = String.valueOf(topicID);
        int i=0;
        for(int docid: rankedlist.keySet()){
            if(i>=size)
                break;
            String docno = Id2Docno.get(docid);
            String rank = String.valueOf(i+1);
            String score = String.valueOf(rankedlist.get(docid));
            results.add(topicid+" Q0 "+docno+" "+rank+" "+score+" m235zhaoBM25\n");
            i++;
        }

        return results;
    }

}