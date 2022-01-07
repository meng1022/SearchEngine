package hw1;

import java.io.*;

public class GetDoc {
    public static void main(String args[]) throws IOException {
        //D:/UWaterloo/2021Spring/MSCI720/hw/data
//        String []args = new String[3];
//        args[0]="D:\\UWaterloo\\2021Spring\\MSCI720\\hw\\data";
//        args[1]="docno";
//        args[2]="LA010190-0040";
        if(args.length!=3){
            System.out.println("  *********************************************************************************************** \n"+
                    "|  This program accepts three command line arguments:                                             |\n"+
                    "|  Your first input should be the location where you store the separate documents                 |\n"+
                    "|  Your second input should be 'id' or 'docno'                                                    |\n"+
                    "|  Your third input should be the corresponding id or docno                                       |\n"+
                    "|      PS: The 'id' is the internal id of all the documents, which increses from 1 to 131896;     |\n"+
                    "|                  any number of out this range would not be accepted                             |\n"+
                    "  *********************************************************************************************** ");
            return;
        }
        File dir = new File(args[0]);
        if(!dir.exists()){
            System.out.println("Error: Directory not existed");
            return;
        }
        String docno=null;
        int internal_id=0;
        if(args[1].equals("id")){
            try{
                String id = args[2];
                internal_id = Integer.valueOf(id);
                if(internal_id<1 || internal_id>131896){
                    System.out.println("Error: Id is out of range");
                    return;
                }
            }catch (NumberFormatException e){
                System.out.println("Error: Wrong format of number");
                return;
            }
        }
        else if(args[1].equals("docno")){
            docno = args[2];
            int index = docno.indexOf("LA");
            if(index==-1||docno.length()<8){
                System.out.println("Error: Wrong format of docno");
                return;
            }
            FileInputStream fis = new FileInputStream(args[0]+"/docno2id.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            for(int i=0;line!=null;i++){
                if(line.indexOf(args[2])!=-1){
                    String[] s = line.split(",");
                    internal_id= Integer.valueOf(s[1]);
                    break;
                }
                line = br.readLine();
            }
        }
        else{
            System.out.println("Error: The 2nd argument can only be 'id' or 'docno'");
            return;
        }

        //internal id
        //System.out.println("internal id: "+internal_id);
        int index1;
        if((internal_id%1000)!=0)
            index1 = internal_id/1000 + 1;
        else
            index1 = internal_id/1000;
        String meta_path = args[0]+"/metas/"+String.valueOf(index1)+"/"+String.valueOf(internal_id)+".txt";
        File meta = new File(meta_path);
        if(meta.exists()){
            FileInputStream fis = new FileInputStream(meta_path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            String []s = line.split(" ");
            docno = s[1];
            while(line!=null && line!=""){
                System.out.println(line);
                line = br.readLine();
            }
            br.close();
        }
        else{
            System.out.println("Error: Metadata not existed");
            return;
        }

        int index = docno.indexOf("LA");
        if(index==-1||docno.length()<8){
            System.out.println("Error: Wrong format of docno");
            return;
        }
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

        String doc_path=args[0]+"/docs/"+year+"/"+month+"/"+day+"/"+docno+".txt";
        //System.out.println("doc_path: "+doc_path);
        File doc = new File(doc_path);
        if(doc.exists()){
            System.out.println("raw document:");
            FileInputStream fis = new FileInputStream(doc_path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            while(line!=null && line!=""){
                System.out.println(line);
                line = br.readLine();
            }
            br.close();
        }
        else{
            System.out.println("Error: Documents not existed");
            return;
        }
    }
}
