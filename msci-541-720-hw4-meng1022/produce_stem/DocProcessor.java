package hw2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocProcessor {
    private List<String> lines;
    private String docno = null;
    private int internal_id;
    private String date = null;
    private String year;
    private String month;
    private String day;
    private String headline = "THIS DOCUMENT DOESN'T HAVE A HEADLINE";
    private int length=0;
    private List<String> tokens=new ArrayList<>();
    private LinkedHashMap<Integer,Integer> wordCount=new LinkedHashMap<>();

    public DocProcessor(List lines){
        this.lines = lines;
    }

    public String getDocno(){
        return docno;
    }
    public int getLength(){return length;}

    public void getMeta(int internal_id){
        this.internal_id = internal_id;
        int headstart=0, headend=0;
        int textstart=0, textend=0;
        int graphicstart=0, graphicend=0;
        int size = lines.size();
        for(int i=0;i<size;i++){
            String line = lines.get(i);
            int start = line.indexOf("<DOCNO>");
            int end = line.indexOf("</DOCNO>");
            if(start!=-1&&end!=-1){
                String docno = line.substring(start+7,end);
                docno = docno.replaceAll(" ","");
                this.docno = docno;

                String date = docno.substring(2,8);
                DateProcessor(date);
            }
            if(line.equals("<HEADLINE>")){
                headstart=i+1;
                String title=null;
                int j=i;
                if(lines.get(j+1).equals("<P>")){
                    j=j+2;
                    title = lines.get(j);
                    while(!lines.get(j+1).equals("</P>")){
                        title = title+lines.get(j+1);
                        j++;
                    }
                }
                this.headline=title;
            }
            //extract tokens
            else if(line.equals("</HEADLINE>"))
                headend=i-1;
            else if(line.equals("<TEXT>"))
                textstart=i+1;
            else if(line.equals("</TEXT>"))
                textend=i-1;
            else if(line.equals("<GRAPHIC>"))
                graphicstart=i+1;
            else if(line.equals("</GRAPHIC>"))
                graphicend=i-1;
            else;
        }
        if(headstart!=0 && headend!=0)
            tokens.addAll(Tokenize(headstart,headend));
        if(textstart!=0 && textend!=0)
            tokens.addAll(Tokenize(textstart,textend));
        if(graphicstart!=0 && graphicend!=0)
            tokens.addAll(Tokenize(graphicstart,graphicend));
        this.length=tokens.size();
    }

    List<String> Tokenize(int start,int end){
        List<String> tokenS = new ArrayList<>();
        Pattern pattern= Pattern.compile("[a-zA-Z0-9]+");
        for(int i=start;i<=end;i++){
            String line=lines.get(i);
            if(line.equals("<P>") || line.equals("</P>"))
                continue;
            Matcher matcher=pattern.matcher(line);
            while(matcher.find()){
                String token=matcher.group(0);
                token=token.toLowerCase();
                token=PorterStemmer.stem(token);
                tokenS.add(token);
            }
        }
        return tokenS;
    }

    public LinkedHashMap<String,Integer> updateLexicon(LinkedHashMap<String,Integer> lexicon){
        int size=tokens.size();
        int tokenid;
        for(int i=0;i<size;i++){
            String token=tokens.get(i);
            if(lexicon.containsKey(token))
                tokenid=lexicon.get(token);
            else{
                tokenid=lexicon.size()+1;
                lexicon.put(token,tokenid);
            }
            if(wordCount.containsKey(tokenid))
                wordCount.put(tokenid,wordCount.get(tokenid)+1);
            else
                wordCount.put(tokenid,1);
        }
        return lexicon;
    }

    public LinkedHashMap<Integer,List<Integer>> InvIndex(LinkedHashMap<Integer,List<Integer>> postings){
        for(int tokenid : wordCount.keySet()){
            if(postings.containsKey(tokenid)){
                List<Integer> DocCountPairs=postings.get(tokenid);
                DocCountPairs.add(internal_id);
                DocCountPairs.add(wordCount.get(tokenid));
                postings.put(tokenid,DocCountPairs);
            }
            else{
                List<Integer> DocCountPairs=new ArrayList<>();
                DocCountPairs.add(internal_id);
                DocCountPairs.add(wordCount.get(tokenid));
                postings.put(tokenid,DocCountPairs);
            }
        }
        return postings;
    }

    public void DateProcessor(String date){
        String month= date.substring(0,2);
        String day = date.substring(2,4);
        String year = date.substring(4,6);
        String []months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        int month_number = Integer.valueOf(month);
        month = months[month_number-1];
        int day_number = Integer.valueOf(day);
        day = String.valueOf(day_number);
        year = "19"+year;
        this.year=year;
        this.month=month;
        this.day=day;

        String d = month+" "+day+", "+year;
        this.date = d;
    }

    public void storeMeta(String homeDir) throws IOException{
        int index1;
        if((internal_id%1000)!=0)
            index1 = internal_id/1000 + 1;
        else
            index1 = internal_id/1000;
        String path = homeDir+"/metas/"+String.valueOf(index1);
        File dir = new File(path);
        dir.mkdirs();

        File meta = new File(path+"/"+String.valueOf(internal_id)+".txt");
        meta.createNewFile();

        FileOutputStream fos = new FileOutputStream(meta);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write("docno: "+this.docno);bw.newLine();
        bw.write("internal id: "+String.valueOf(internal_id));bw.newLine();
        bw.write("date: "+this.date);bw.newLine();
        bw.write("headline: "+this.headline);bw.newLine();
        bw.close();
    }

    public void storeDoc(String homeDir) throws IOException {
        String path=homeDir+"/docs/"+year+"/"+month+"/"+day;
        File dir = new File(path);
        dir.mkdirs();

        File doc = new File(path+"/"+docno+".txt");
        doc.createNewFile();

        FileOutputStream fos = new FileOutputStream(doc);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        int size = lines.size();
        for(int i=0;i<size;i++){
            bw.write(lines.get(i));
            bw.newLine();
        }
        bw.close();
    }
}
