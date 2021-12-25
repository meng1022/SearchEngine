import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooleanAND implements Comparator<String> {
    public static void main(String args[]) throws IOException {
//        String []args = new String[3];
//        args[0]="D:/UWaterloo/2021Spring/MSCI720/hw2/data"; args[1]="queries.txt"; args[2]="m235zhao-hw2-results.txt";

        if(args.length!=3){
            System.out.println("  *********************************************************************************************** \n"+
                    "|                                   This program takes 3 arguments:                               |\n"+
                    "|  First input: the location where you store the inverted index.                                  |\n"+
                    "|  Second input: the name of the topic401-topic450 queries file(stored at the project path)       |\n"+
                    "|  Third input: the name of the file to store search results.                                     |\n"+
                    "|  PS: Split each argument by a space.                                                            |\n"+
                    "  *********************************************************************************************** ");
            return;
        }
        String indexdir = args[0];//D:/UWaterloo/2021Spring/MSCI720/hw2/data
        String queriesfile= args[1];//queries.txt
        String resultfilename = args[2];//m235zhao-hw2-results.txt

        LinkedHashMap<String,Integer> Lexicon = LoadLexicon(indexdir);
        LinkedHashMap<Integer,String> InvIndex = LoadInvIndex(indexdir); // String: Postings--> 1 2 3 1 (docid count docid count)

        String id2docnofn = "id2docno.txt";
        String runTag="m235zhaoAND";
        int topicID=0;
        try {
            FileInputStream fis = new FileInputStream(queriesfile);
            BufferedReader br=new BufferedReader(new InputStreamReader(fis));

            File resultfile = new File(resultfilename);
            if(!resultfile.exists())
                resultfile.createNewFile();
            FileOutputStream fos=new FileOutputStream(resultfile);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));

            String line=br.readLine();
            for(int i=0;line!=null;i++){
                if(i%2==0){
                    line=line.replaceAll(" ","");
                    topicID=Integer.valueOf(line);
                }
                else{
                    List<String> queryterms=new ArrayList<>();
                    Pattern pattern= Pattern.compile("[a-zA-Z0-9]+");
                    Matcher matcher=pattern.matcher(line);
                    while(matcher.find()){
                        String term=matcher.group(0);
                        term=term.toLowerCase();
                        queryterms.add(term);
                    }
//                    System.out.println(topicID);
//                    System.out.println(queryterms);
                    List<Integer> result= booleanand(queryterms,Lexicon,InvIndex);
                    int retrievedcount = result.size();
                    //System.out.println(result);

                    FileInputStream fis1 = new FileInputStream(indexdir+"/"+id2docnofn);
                    BufferedReader br1=new BufferedReader(new InputStreamReader(fis1));
                    String line1=br1.readLine();
                    int j=1;
                    for(int k=1;k<=retrievedcount;k++){
                        int docid=result.get(k-1);
                        String docno;
                        for(;line1!=null;j++){
                            if(j==docid){
                                docno=(line1.split(","))[1];
                                bw.write(String.valueOf(topicID)+" "
                                        +"Q0 "
                                        +docno+" "
                                        + k +" "
                                        + (retrievedcount - k) +" "
                                        +runTag);
                                bw.newLine();
                                break;
                            }
                            line1=br1.readLine();
                        }
                    }
                    br1.close();
                }
                line=br.readLine();
            }
            bw.close();
            br.close();
        }catch (FileNotFoundException e){
            System.out.println("Error: File Not Found");
            return;
        }

    }

    public static LinkedHashMap<String,Integer> LoadLexicon(String indexdir) throws IOException{
        String lexiconpath = indexdir+"/lexicon/lexicon_token2id.txt";
        LinkedHashMap<String,Integer> Lexicon = new LinkedHashMap<>();
        try{
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
        }catch (FileNotFoundException e){
            throw new FileNotFoundException("Error: File not found");
        }
        return Lexicon;
    }

    public static LinkedHashMap<Integer,String>LoadInvIndex(String indexdir)throws IOException{
        String invertindex = indexdir+"/invertedindex/invindex.txt";
        LinkedHashMap<Integer,String> InvIndex = new LinkedHashMap<>();
        try{
            File invIndex = new File(invertindex);
            FileInputStream fis=new FileInputStream(invIndex);
            BufferedReader br=new BufferedReader(new InputStreamReader(fis));
            String line=br.readLine();
            int i=0;
            int termid=0;
            String []s;
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
        }catch (FileNotFoundException e){
            throw new FileNotFoundException("Error: File not found");
        }
        return InvIndex;

    }

    public static List<Integer> booleanand(List<String> queryterms,
                                           LinkedHashMap<String,Integer> Lexicon,
                                           LinkedHashMap<Integer,String> InvIndex){
        List<Integer> queryids=new ArrayList<>();
        List<String> Spostings = new ArrayList<>();
        int termid;
        //get spotings of tokenids
        for(String term : queryterms){
            if(Lexicon.containsKey(term)){
                termid = Lexicon.get(term);
                queryids.add(termid);
                Spostings.add(InvIndex.get(termid));
            }
        }

        Collections.sort(Spostings, new BooleanAND());
        int size=Spostings.size();
        List<List<Integer>> spotings = new ArrayList<>();
        for(int i=0;i<size;i++){
            List<Integer> spoting = new ArrayList<>();
            String[] s=Spostings.get(i).split(" ");
            for(int j=0;j<s.length;j+=2){
                spoting.add(Integer.valueOf(s[j]));
            }
            //System.out.println(spoting);
            spotings.add(spoting);
        }
        if(spotings.size()==1)
            return spotings.get(0);
        List<Integer> docids=new ArrayList<>();
        for(int i=0;i<size-1;i++){
            int j=0;int k=0;
            List<Integer> postings1=new ArrayList<>();
            if(i==0)
                postings1=spotings.get(i);
            else{
                for(Integer id: docids){
                    postings1.add(id);
                }
            }
            List<Integer> postings2=spotings.get(i+1);
            int length1= postings1.size();
            int length2= postings2.size();
            docids.clear();
            while(j!=length1 && k!=length2){
                int postings1id=postings1.get(j);
                int postings2id=postings2.get(k);
                if(postings1id==postings2id){
                    docids.add(postings1id);
                    j++;k++;
                }
                else if(postings1id<postings2id)
                    j++;
                else
                    k++;
            }
        }
        return docids;
    }

    //comparator
    @Override
    public int compare(String var1, String var2) {
        if (var1.length() > var2.length()) {
            return 1;
        } else if (var1.length() == var2.length()) {
            return 0;
        } else {
            return -1;
        }
    }
}
