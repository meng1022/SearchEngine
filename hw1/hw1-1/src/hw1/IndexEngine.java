package hw1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IndexEngine {
    public static void main(String args[]) throws IOException {
        if(args.length!=2){
            System.out.println("  *********************************************************************************************** \n"+
                    "|  This program is to read the latimes.gz file to store separately each document and its metadata.|\n"+
                    "|  Your first input should be the location where you store the latimes.gz file                    |\n"+
                    "|  Your second input should be the location where you want your separate files to be stored.      |\n"+
                    "|  PS: You should split the two inputs by a blank.                                                |\n"+
                    "  *********************************************************************************************** ");
            return;
        }
        String filename = args[0];
        String homeDir = args[1];
        // D:/UWaterloo/2021Spring/MSCI720/hw/latimes.gz D:/UWaterloo/2021Spring/MSCI720/hw/data
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
            MyGZFile gzfile = new MyGZFile(filename);
            BufferedReader reader = gzfile.read();
            String line= reader.readLine();
            List<String> lines = new ArrayList<String>();
            int internal_id = 1;
            while(line!=null){
                lines.add(line);
                if(line.equals("</DOC>")){
                    DocProcessor docProcessor = new DocProcessor(lines);
                    docProcessor.getMeta(internal_id);
                    docProcessor.storeMeta(homeDir);
                    docProcessor.storeDoc(homeDir);
                    bw.write(String.valueOf(internal_id)+","+docProcessor.getDocno());
                    bw.newLine();
                    bw1.write(docProcessor.getDocno()+","+String.valueOf(internal_id));
                    bw1.newLine();

                    internal_id++;
                    lines = new ArrayList<String>();
                }
                line = reader.readLine();
            }
            bw.close();
            bw1.close();
            gzfile.close();
        }catch (FileNotFoundException e){
            System.out.println("Error: File not found");
            return;
        }
    }
}
