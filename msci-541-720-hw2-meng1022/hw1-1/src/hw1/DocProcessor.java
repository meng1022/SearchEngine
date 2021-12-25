package hw1;

import java.io.*;
import java.util.List;

public class DocProcessor {
    private List<String> lines;
    private String docno = null;
    private int internal_id;
    private String date = null;
    private String year;
    private String month;
    private String day;
    private String headline = "THIS DOCUMENT DOESN'T HAVE A HEADLINE";

    public DocProcessor(List lines){
        this.lines = lines;
    }

    public String getDocno(){
        return docno;
    }
    public String getDate() {
        return date;
    }
    public String getHeadline() {
        return headline;
    }

    public void getMeta(int internal_id){
        this.internal_id = internal_id;
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
        }
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
