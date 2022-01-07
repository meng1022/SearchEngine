import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class run {
    /* path of inverted index and other created data */
    private static String dirpath = "";
    public static void main(String args[]) {
        if(args.length!=1){
            System.out.println("ERROR: wrong number of arguments");
            return;
        }
        //D:/UWaterloo/2021Spring/MSCI720/hw2/data
        dirpath = args[0];
        File dir = new File(dirpath);
        if (!dir.exists()) {
            System.out.println("ERROR: directory or file not exist");
            return;
        }
        try{
            LinkedHashMap<String,Integer> Lexicon = LoadLexicon(dirpath);
            LinkedHashMap<Integer,String> InvIndex = LoadInvIndex(dirpath);
            List<Integer> DocLength = LoadLength(dirpath);
            HashMap<Integer,String> IdDocno = Id2Docno(dirpath);

            long start_time, end_time;
            System.out.print("Please input query:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String query = reader.readLine();
            HashMap<String, Integer> queryterms = new HashMap<>();
            start_time = System.currentTimeMillis();
            Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
            Matcher matcher = pattern.matcher(query);
            while (matcher.find()) {
                String term = matcher.group(0);
                term = term.toLowerCase();
                if (queryterms.containsKey(term))
                    queryterms.put(term, queryterms.get(term) + 1);
                else
                    queryterms.put(term, 1);
            }
            /*results list of a query*/
            List<HashMap<String, String>> results = getResults(Lexicon, InvIndex, DocLength, IdDocno, queryterms, query);
            end_time = System.currentTimeMillis();
            double time_interval = (end_time-start_time)/1000.0;
            System.out.println("-------------------------start of the result list-------------------------");
            for(HashMap res : results){
                System.out.println(res.get("rank")+"."+res.get("headline")+"("+res.get("date")+")");
                System.out.println(res.get("summary")+"("+res.get("docno")+")\n");
            }
            System.out.println("-------------------------end of the result list-------------------------");
            System.out.println("Retrieval took "+time_interval+" seconds.\n");

            System.out.println("Please input command: N: next query, Q:quit, Rankid: show the complete document");
            String command = reader.readLine();
            Pattern patternDigit = Pattern.compile("^[\\d]+$");
            Matcher matcherDigit = patternDigit.matcher(command);

            while(command.equals("N")||command.equals("Q")||matcherDigit.find()){
                if(command.equals("Q"))
                    break;
                if (command.equals("N")) {
                    /* query input by user */
                    System.out.print("Please input query:");
                    /* user query */
                    query = reader.readLine();
                    queryterms = new HashMap<>();
                    matcher = pattern.matcher(query);
                    start_time = System.currentTimeMillis();
                    while (matcher.find()) {
                        String term = matcher.group(0);
                        term = term.toLowerCase();
                        if (queryterms.containsKey(term))
                            queryterms.put(term, queryterms.get(term) + 1);
                        else
                            queryterms.put(term, 1);
                    }
                    /*results list of a query*/
                    results = getResults(Lexicon, InvIndex, DocLength, IdDocno, queryterms, query);
                    end_time = System.currentTimeMillis();
                    System.out.println("-------------------------start of the result list-------------------------");
                    for(HashMap res : results){
                        System.out.println(res.get("rank")+"."+res.get("headline")+"("+res.get("date")+")");
                        System.out.println(res.get("summary")+"("+res.get("docno")+")\n");
                    }
                    System.out.println("-------------------------end of the result list-------------------------");
                    time_interval = (end_time-start_time)/1000.0;
                    System.out.println("Retrieval took "+time_interval+" seconds.\n");
                }
                else{
                    int rankid = Integer.valueOf(command);
                    int resultcount = results.size();
                    if(rankid>0 && rankid<=resultcount) {
                        String docno = results.get(rankid-1).get("docno");
                        String docpath = GetFilePath(docno);
                        File docfile = new File(docpath);
                        FileInputStream fis = new FileInputStream(docfile);
                        BufferedReader reader1 = new BufferedReader(new InputStreamReader(fis));
                        String line1 = reader1.readLine();
                        System.out.println("-------------------------start of the document-------------------------");
                        while(line1!=null){
                            System.out.println(line1);
                            line1 = reader1.readLine();
                        }
                        System.out.println("-------------------------end of the document-------------------------");
                    }
                    else{
                        System.out.println("Error: out of rankid range\n");
                    }
                }
                System.out.println("Please input command: N: next query, Q:quit, Rankid: show the complete document");
                command = reader.readLine();
                matcherDigit = patternDigit.matcher(command);

            }
        }catch (FileNotFoundException e){
            System.out.println("ERROR: file not found");
            return;
        }catch (IOException e){
            System.out.println("ERROR: IOException");
            return;
        }
    }

    public static String GetFilePath(String docno){
        String []months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String date = docno.substring(2,8);
        String month= date.substring(0,2);
        String day = date.substring(2,4);
        String year = date.substring(4,6);
        int month_number = Integer.valueOf(month);
        month = months[month_number-1];
        int day_number = Integer.valueOf(day);
        day = String.valueOf(day_number);
        year = "19"+year;

        String doc_path=dirpath+"/docs/"+year+"/"+month+"/"+day+"/"+docno+".txt";
        return doc_path;
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

    public static List<String> LoadMeta(int docid) throws IOException{
        List<String> metas = new ArrayList<>();
        int index1;
        if((docid%1000)!=0)
            index1 = docid/1000 + 1;
        else
            index1 = docid/1000;
        String meta_path = dirpath+"/metas/"+index1+"/"+docid+".txt";
        File meta = new File(meta_path);
        if(meta.exists()){
            FileInputStream fis = new FileInputStream(meta_path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            int i=1;
            while(line!=null && line!=""){
                if(i==3)
                    metas.add(line.substring(6));
                else if(i==4)
                    metas.add(line.substring(10));
                else;
                line = br.readLine();
                i++;
            }
            br.close();
        }
        return metas;
    }

    public static String GetSummary(String docno,String query) throws IOException{
        String doc_path=GetFilePath(docno);

        File docfile = new File(doc_path);
        FileInputStream fis = new FileInputStream(docfile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();

        List<String> doclines = new ArrayList<>();
        int headstart = 0, headend = 0;
        int textstart = 0, textend = 0;
        int graphstart = 0, graphend = 0;
        int i = 0;
        while(line!=null){
            doclines.add(line);
            if(line.equals("<HEADLINE>"))
                headstart = i+1;
            if(line.equals("</HEADLINE>"))
                headend = i-1;
            if(line.equals("<TEXT>"))
                textstart=i+1;
            else if(line.equals("</TEXT>"))
                textend=i-1;
            else if(line.equals("<GRAPHIC>"))
                graphstart=i+1;
            else if(line.equals("</GRAPHIC>"))
                graphend=i-1;
            else;
            line=reader.readLine();
            i ++;
        }
        List<String> sentences=GetSen(doclines,headstart,headend,textstart,textend,graphstart,graphend);
        List<String> querywords = new ArrayList<>();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
        Matcher matcher = pattern.matcher(query);
        while (matcher.find()) {
            String term = matcher.group(0);
            term = term.toLowerCase();
            querywords.add(term);
        }

        List<List<String>> docsens = new ArrayList<>();
        int sIZE = sentences.size();
        for(i=0; i<sIZE;i++){
            matcher = pattern.matcher(sentences.get(i));
            List<String> senwords = new ArrayList<>();
            while(matcher.find()){
                String term = matcher.group(0);
                term = term.toLowerCase();
                senwords.add(term);
            }
            docsens.add(senwords);
        }

        //List<Integer> scores = new ArrayList<>();
        int size = docsens.size();
        int l=0, c=0, d=0, k=0;
        int highestscore = 0;
        int secondhighestscore = 0;
        int summaryindex = -1;
        int summaryindex2 = -1;
        /* for each sentence in doc */
        for(i=0;i<size;i++){
            if(i==0)
                l=2;
            else if(i==1)
                l=1;
            else;
            /* the words of ith sentence in doc*/
            List<String> senwords = docsens.get(i);
            int sensize = senwords.size();
            for(String queryw: querywords){
                /* the occurrence of query term in sentence */
                int occurrence = Collections.frequency(senwords,queryw);
                if(occurrence!=0){
                    d += 1;
                    c += occurrence;
                }
            }
            String senstr = "";
            for(String senw : senwords){
                senstr += (senw+" ");
            }
            int querysize = querywords.size();
            for(int j=querysize;j>0;j--){
                /* j:= size of the query subset */
                for(int m=0; m<=querysize-j;m++){
                    /* m:= start index of subquery */
                    String subqstr = "";
                    for(int K=m;K<m+j;K++){
                        subqstr += (querywords.get(K)+" ");
                    }
                    if(senstr.indexOf(subqstr)!=-1){
                        k = j;
                        break;
                    }
                }
                if(k!=0)
                    break;
            }
            int score = l+2*c+3*d+4*k;

            if(score>highestscore && sensize>=5) {
                secondhighestscore = highestscore;
                highestscore = score;
                summaryindex2 = summaryindex;
                summaryindex = i;
            }
            else if(score>secondhighestscore && score<highestscore && sensize>=5){
                secondhighestscore = score;
                summaryindex2 = i;
            }
            l=0; c=0; d=0; k=0;
        }
        /* 2 sentences in summary */
        String summary = sentences.get(summaryindex);
        if(summaryindex2!=-1)
            summary += sentences.get(summaryindex2);
        return summary;
    }

    public static List<String> GetSen(List<String> lines,int headstart,int headend,int textstart,int textend, int graphstart,int graphend){
        String content="";
        String headcontent = "";
        /* sentences */
        List<String> sens = new ArrayList<>();
        if(headstart!=0 && headend!=0) {
            for (int i = headstart; i <= headend; i++) {
                String line = lines.get(i);
                if (line.equals("<P>") || line.equals("</P>"))
                    continue;
                headcontent += lines.get(i);
            }
            sens.add(headcontent);
        }
        if(textstart!=0 && textend!=0) {
            for (int i = textstart; i <= textend; i++) {
                String line = lines.get(i);
                if (line.equals("<P>") || line.equals("</P>"))
                    continue;
                content += lines.get(i);
            }
        }
        if(graphstart!=0 && graphend!=0) {
            for (int i = graphstart; i <= graphend; i++) {
                String line = lines.get(i);
                if (line.equals("<P>") || line.equals("</P>"))
                    continue;
                content += lines.get(i);
            }
        }


        Pattern pattern = Pattern.compile("[^.?!]*\\.|[^.?!]*\\?|[^.?!]*\\!");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String sen = matcher.group(0);
            sens.add(sen);
        }
        if(sens.size()==0)
            sens.add(content);
        return sens;
    }

    public static List<HashMap<String,String>> getResults(LinkedHashMap<String,Integer> Lexicon,
                                          LinkedHashMap<Integer,String> InvIndex,
                                          List<Integer> DocLength,HashMap<Integer,String> Id2Docno,
                                          HashMap<String,Integer> queryterms,String query) throws IOException {
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
                    double a3 = Math.log((N-ni+0.5)/(ni+0.5));
                    //double a3 = Math.log((ri+0.5)/(R-ri+0.5)/((ni-ri+0.5)/(N-ni-R+ri+0.5)));
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

        List<HashMap<String,String>> results = new ArrayList<>();
        /* top 10 results */
        int size = (rankedlist.size()>10)? 10: rankedlist.size();
        int i=0;
        //THIS DOCUMENT DOESN'T HAVE A HEADLINE
        for(int docid: rankedlist.keySet()){
            HashMap<String,String>map = new HashMap<>();
            if(i>=size)
                break;
            String docno = Id2Docno.get(docid);
            String rank = String.valueOf(i+1);
            List<String> meta = LoadMeta(docid);
            String head = meta.get(1);
            String summary = GetSummary(docno,query);
            map.put("rank",rank);
            /* e.g., the 8th doc */
            if(!head.equals("THIS DOCUMENT DOESN'T HAVE A HEADLINE")){
                map.put("headline",head);
            }
            else{
                if(summary.length()>=50)
                    map.put("headline",summary.substring(0,50)+"...");
                else
                    map.put("headline",summary);
            }
            map.put("date",meta.get(0));
            map.put("summary",summary);
            map.put("docno",docno);
            results.add(map);
            i++;
        }
        return results;
    }

}
