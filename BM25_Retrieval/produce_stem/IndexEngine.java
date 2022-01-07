package hw2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class IndexEngine {
    public static void main(String args[]) throws IOException {
        // args = new String[2];
        // args[0]="D:/UWaterloo/2021Spring/MSCI720/latimes.gz";
        // args[1]="D:/UWaterloo/2021Spring/MSCI720/hw4/data-new";

        if(args.length!=2){
            System.out.println("  *********************************************************************************************** \n"+
                    "|  Your first input should be the location where you store the latimes.gz file                    |\n"+
                    "|  Your second input should be the location where you want your files to be stored.               |\n"+
                    "|  PS: You should split the two inputs by a blank.                                                |\n"+
                    "  *********************************************************************************************** ");
            return;
        }
        String filename = args[0];
        String homeDir = args[1];
        File dir = new File(homeDir);
        if(dir.exists()){
            System.out.println("Error: Direcory already exists or invalid path for separate files");
            return;
        }
        dir.mkdir();

        //id2docno.txt stores the mapping from internalid to docno
        File id2docno = new File(homeDir+"/id2docno.txt");
        id2docno.createNewFile();
        FileOutputStream fos = new FileOutputStream(id2docno);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        //docno2id.txt stores the mapping from docno to internalid
        File docno2id = new File(homeDir+"/docno2id.txt");
        docno2id.createNewFile();
        FileOutputStream fos1 = new FileOutputStream(docno2id);
        BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(fos1));

        try{
            InputStream fileStream = new FileInputStream(filename);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileStream);
            Reader r = new InputStreamReader(gzipInputStream);
            BufferedReader reader = new BufferedReader(r);
            String line= reader.readLine();
            List<String> lines = new ArrayList<String>();

            LinkedHashMap<String,Integer> lexicon = new LinkedHashMap<>();//lexicon
            LinkedHashMap<Integer,List<Integer>> postings= new LinkedHashMap<>();//postings
            int internal_id = 1;
            List<Integer> doclengths = new ArrayList<>();
            while(line!=null){
                lines.add(line);
                if(line.equals("</DOC>")){
                    DocProcessor docProcessor = new DocProcessor(lines);
                    docProcessor.getMeta(internal_id);
                    doclengths.add(docProcessor.getLength());
                    docProcessor.storeMeta(homeDir);
                    lexicon=docProcessor.updateLexicon(lexicon);
                    postings=docProcessor.InvIndex(postings);
                    docProcessor.storeDoc(homeDir);
                    bw.write(String.valueOf(internal_id)+","+docProcessor.getDocno());
                    bw.newLine();
                    bw1.write(docProcessor.getDocno()+","+String.valueOf(internal_id));
                    bw1.newLine();

                    internal_id++;
                    //break;
                    lines = new ArrayList<String>();
                }
                line = reader.readLine();
            }
            bw.close();
            bw1.close();
            fileStream.close();
            gzipInputStream.close();

            storeDoclengths(args[1],doclengths);//store doclengths in ascending docid order
            storeLexicon(args[1],lexicon);//store lexicon
            storePostings(args[1],postings);//store inverted index
        }catch (FileNotFoundException e){
            System.out.println("Error: File not found");
            return;
        }
    }
    public static void storeDoclengths(String dir,List<Integer> doclengths) throws IOException{
        String lengthspath=dir+"/doclengths.txt";
        File docLengths=new File(lengthspath);
        docLengths.createNewFile();
        FileOutputStream fos = new FileOutputStream(docLengths);
        BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
        for(int doclength : doclengths){
            bw.write(String.valueOf(doclength));
            bw.newLine();
        }
        bw.close();
    }

    public static void storeLexicon(String dir,LinkedHashMap<String,Integer>lexicon) throws IOException{
        String lexiconpath=dir+"/lexicon";
        File lexicondir=new File(lexiconpath);
        lexicondir.mkdir();
        //token-->tokenid
        File lexiCon = new File(lexicondir+"/lexicon_token2id.txt");
        lexiCon.createNewFile();
        FileOutputStream fos=new FileOutputStream(lexiCon);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        //tokenid-->token
        File lexiCon_ = new File(lexicondir+"/lexicon_id2token.txt");
        lexiCon_.createNewFile();
        FileOutputStream fos1=new FileOutputStream(lexiCon_);
        BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(fos1));

        for(String token:lexicon.keySet()){
            String tokenid = String.valueOf(lexicon.get(token));
            bw.write(token+","+tokenid);bw.newLine();
            bw1.write(tokenid+","+token);bw1.newLine();
        }
        bw.close();
        bw1.close();
    }

    public static void storePostings(String dir,HashMap<Integer,List<Integer>>postings) throws IOException{
        String invpath=dir+"/invertedindex";
        File invdir=new File(invpath);
        invdir.mkdir();
        File InvIndex = new File(invpath+"/invindex.txt");
        InvIndex.createNewFile();
        FileOutputStream fos=new FileOutputStream(InvIndex);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for(Integer tokenid : postings.keySet()){
            bw.write(String.valueOf(tokenid));
            bw.newLine();
            for(Integer i:postings.get(tokenid)){
                bw.write(String.valueOf(i));
                bw.write(" ");
            }
            bw.newLine();
        }
        bw.close();
    }
}
