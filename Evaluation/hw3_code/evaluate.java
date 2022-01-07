import javax.print.DocFlavor;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class evaluate {
    public static void main(String args[]){
        if(args.length!=2){
            System.out.println("[ please input the path of qrels file, and the path of results file;\n" +
                                "wrong number of arguments will cause this error ]");
            return;
        }
//        String judgementfname = "D:/UWaterloo/2021Spring/MSCI720/hw3/hw3-files-2021/qrels/LA-only.trec8-401.450.minus416-423-437-444-447.txt";
//        String studentfname = "D:/UWaterloo/2021Spring/MSCI720/hw4/hw4-bm25-stem-m235zhao.txt";
        String judgementfname = args[0];
        String studentfname = args[1];
        LinkedHashMap<Integer,List<String>> map;
        HashMap<String,Integer> docno2id;
        List<Integer> id2length;
        try{
            map = judge(judgementfname);
            docno2id = getDocno2Id();
            id2length = getId2Doclength();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
//        String studentfdir = "D:/UWaterloo/2021Spring/MSCI720/hw3/hw3-files-2021/results-files/student";
//        String  studentfname = "";
//        String[] ns = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14"};

        //for(int i=0;i<ns.length;i++){
//            studentfname = studentfdir + (ns[i]+".results");
            LinkedHashMap<Integer, LinkedHashMap<String, Double>> TopicDocScores = new LinkedHashMap<>();
            /* topicid, rel/non-rel */
            LinkedHashMap<Integer,List<Integer>> TopicRels = new LinkedHashMap<>();
            try{
                TopicDocScores = getTopicDocScores(studentfname);
                TopicRels = getTopicRels(TopicDocScores,map);
            }catch (Exception e){
                if(e.getMessage().equals("ERROR: Improper format")){
                    /*output 'wrong format for each measure'*/
                    //System.out.println("student"+ns[i]+".results: bad format");
                    System.out.println("bad format");
//                    continue;
                    return;
                }
                else{
                    System.out.println(e.getMessage());
                    return;
                }
            }

            //System.out.println("student"+ns[i]+"("+TopicRels.entrySet().size()+" topics) evaluation results:");
            //float AP_sum =0, P10_sum=0, nDCG10_sum =0, nDCG1000_sum=0,TBG_sum=0;
            for(int topicid : TopicRels.keySet()){
                List<Integer> rels = TopicRels.get(topicid);
                LinkedHashMap<String,Double> docnoScore = TopicDocScores.get(topicid);
                float AP = getAveragePrecision(map.get(topicid).size(),rels);
                float P_10 = getPrecisionAt10(rels);
                float nDCG_10 = getnDCG(rels,map.get(topicid).size(),10);
                float nDCG_1000 = getnDCG(rels,map.get(topicid).size(),1000);
                float TBG = getTBG(docno2id,id2length,docnoScore,rels);
                //AP_sum+=AP; P10_sum+=P_10; nDCG10_sum+=nDCG_10; nDCG1000_sum+=nDCG_1000; TBG_sum+=TBG;
                System.out.print(topicid + ": " + "AP=" + AP);
                System.out.print("  P_10=" + P_10);
                System.out.print("  nDCG_10=" + nDCG_10);
                System.out.print("  nDCG_1000=" + nDCG_1000);
                System.out.println("  TBG=" + TBG);
            }
//            DecimalFormat df = new DecimalFormat(".000");
//
//            System.out.print("Mean_AP=" + Float.valueOf(df.format((float)AP_sum/45)));
//            System.out.print("  Mean_P10=" + Float.valueOf(df.format((float)P10_sum/45)));
//            System.out.print("  Mean_nDCG10=" + Float.valueOf(df.format((float)nDCG10_sum/45)));
//            System.out.print("  Mean_nDCG1000=" + Float.valueOf(df.format((float)nDCG1000_sum/45)));
//            System.out.println("  Mean_TBG=" + Float.valueOf(df.format((float)TBG_sum/45)));

//        }



    }

    public static LinkedHashMap<Integer,List<String>> judge(String judgementfname) throws Exception {
        File judgementf = new File(judgementfname);
        LinkedHashMap<Integer,List<String>> map= new LinkedHashMap<>();
        try{
            FileInputStream fis = new FileInputStream(judgementf);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            while(line!=null){
                String [] s = line.split(" ");
                if(s[3].equals("1")){
                    int topicid = Integer.valueOf(s[0]);
                    List<String> docnos;
                    if(map.containsKey(topicid)){
                        docnos = map.get(topicid);
                    }
                    else{
                        docnos = new ArrayList<>();
                    }
                    docnos.add(s[2]);
                    map.put(topicid,docnos);
                }
                else;
                line = br.readLine();
            }
        }catch (IOException e){
            throw new IOException("ERROR");
        }

        return map;
    }

    public static LinkedHashMap<Integer, LinkedHashMap<String, Double>> getTopicDocScores(String studentfname) throws Exception {
        File studentf = new File(studentfname);
        /*topicid,[docno,score]*/
        LinkedHashMap<Integer, LinkedHashMap<String, Double>> TopicDocScores = new LinkedHashMap<>();
        List<Integer> WrongSortedTopics = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(studentf);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            double preScore = 999;
            while(line!=null){
                String[] s =line.split(" ");
                if(!s[1].equals("Q0") || s[2].indexOf("LA")!=0 || s[2].length()!= 13|| s.length!=6)
                    throw new Exception("ERROR: Improper format");
                int topicid = Integer.valueOf(s[0]);
                int rank = Integer.valueOf(s[3]);
                double score = Double.valueOf(s[4]);
                LinkedHashMap<String,Double> map1;
                if(TopicDocScores.containsKey(topicid)){
                    map1 = TopicDocScores.get(topicid);
                    if(map1.containsKey(s[2])){
                        /*duplicate docno in one topic*/
                        throw new Exception("ERROR: Improper format");
                    }
                    if(preScore<score && !WrongSortedTopics.contains(topicid))
                        WrongSortedTopics.add(topicid);
                }
                else{
                    map1 = new LinkedHashMap<>();
                }
                preScore = score;
                map1.put(s[2],score);
                TopicDocScores.put(topicid,map1);
                line = br.readLine();
            }

            //System.out.println("Wrongly sorted topicid:" + WrongSortedTopics);
            for(int topicid : WrongSortedTopics){
                /* sort wrongly sorted results*/
                LinkedHashMap<String,Double> map1 = TopicDocScores.get(topicid);
                LinkedHashMap<String,Double> sortedmap1 = new LinkedHashMap<>();
                map1.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .forEachOrdered(s->sortedmap1.put(s.getKey(),s.getValue()));
                TopicDocScores.put(topicid,sortedmap1);
            }

        }catch (FileNotFoundException e){
            throw new FileNotFoundException("ERROR: File not found");
        }catch (NumberFormatException e){
            throw new NumberFormatException("ERROR: Improper format");
        }

        return TopicDocScores;
    }

    public static LinkedHashMap<Integer,List<Integer>> getTopicRels(LinkedHashMap<Integer, LinkedHashMap<String, Double>> TopicDocScores, LinkedHashMap<Integer,List<String>> map) throws Exception {
        LinkedHashMap<Integer,List<Integer>> TopicRels = new LinkedHashMap<>();
        for(int topicid : TopicDocScores.keySet()){
            if(map.containsKey(topicid)){
                List<Integer> rels = new ArrayList<>();
                List<String> reldocnos = map.get(topicid);
                Set<String> resultdocnos = TopicDocScores.get(topicid).keySet();
                for(String docno : resultdocnos){
                    if(reldocnos.contains(docno))
                        rels.add(1);
                    else
                        rels.add(0);
                }
                TopicRels.put(topicid,rels);
            }
            else;/*ignore extra topic*/
        }
        return TopicRels;
    }

    public static HashMap<String,Integer> getDocno2Id() throws Exception{
        String filename = "docno2id.txt";
        File file = new File(filename);
        HashMap<String,Integer> docno2id = new HashMap<>();
        try{
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            while(line!=null){
                String s[] = line.split(",");
                int id = Integer.valueOf(s[1]);
                docno2id.put(s[0],id);
                line = br.readLine();
            }
        }catch (IOException e){
            throw new IOException("ERROR: Docno2Id File read error");
        }
        return docno2id;
    }

    public static List<Integer> getId2Doclength() throws Exception{
        String filename = "doclengths.txt";
        File file = new File(filename);
        List<Integer> id2doclength = new ArrayList<>();
        try{
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            while(line!=null){
                int length = Integer.valueOf(line);
                id2doclength.add(length);
                line = br.readLine();
            }
        }catch (IOException e){
            throw new IOException("ERROR: Id2Doclength File read error");
        }
        return id2doclength;
    }

    public static float getAveragePrecision(int R, List<Integer> Rels){
        float sum = 0; int relcount=0;
        int size = Rels.size();
        for(int i=0;i<size;i++){
            if(Rels.get(i)==1){
                relcount ++;
                sum += (float)relcount/(i+1);
            }
        }
        DecimalFormat df = new DecimalFormat(".0000");
        String ret = df.format(sum/R);
        return Float.valueOf(ret);
    }

    public static float getPrecisionAt10(List<Integer> Rels){
        /*divider 10 or size?*/
        int relcount=0;
        int size =10;
        if(Rels.size()<10)
            size = Rels.size();
        for(int i=0;i<size;i++){
            if(Rels.get(i)==1)
                relcount++;
        }
        DecimalFormat df = new DecimalFormat(".0000");
        String ret = df.format((float)relcount/10);
        return Float.valueOf(ret);
    }

    public static float getnDCG(List<Integer> Rels,int n,int top){
        float IDCG=0, DCG=0;
        if(n==0)
            return (float) 0.0;
        if(n>top)
            n =top;
        else;
        for(int i=1;i<=n;i++){
            IDCG += (float)(Math.log(2)/Math.log(i+1));
            //IDCG += (float)1/(Math.log(i+1)/Math.log(2));
        }
        int size = Rels.size();
        if(size>=top) size = top;
        for(int i=1;i<=size;i++){
            if(Rels.get(i-1)!=0){
                DCG += (float)(Math.log(2)/Math.log(i+1));
                //DCG += (float)1/(Math.log(i+1)/Math.log(2));
            }
        }
        float nDCG = DCG/IDCG;
        DecimalFormat df = new DecimalFormat(".0000");
        String snDCG = df.format(nDCG);
        return Float.valueOf(snDCG);
    }

    public static float getTBG(HashMap<String,Integer>Docno2Id,List<Integer> Id2Doclength,LinkedHashMap<String, Double> DocnoScore,List<Integer> Rels){
        float Tk = 0; float discount = 0;
        float gain = 0;
        float TBG = 0;
        int doclength;
        int size = Rels.size();
        Set<String> docno = DocnoScore.keySet();
        Iterator<String> docnos = docno.iterator();
        for(int i=0;i<size;i++){
            int rel = Rels.get(i);
            if(i==0){
                discount = 1;
            }
            else{
                //System.out.print(docnos.next()+",");
                int prerel = Rels.get(i-1);
                String docNo = docnos.next();
                if(Docno2Id.get(docNo) == null){
//                    System.out.println(docNo);
                    continue;
                }
                int id = Docno2Id.get(docNo);
                doclength = Id2Doclength.get(id-1);
                float readtime = (float) (0.018 * doclength + 7.8);
                if(prerel == 1){
                    Tk += (float)(4.4 + readtime*0.64);
                }
                else{/*prerel = 0*/
                    Tk += (float)(4.4 + readtime*0.39);
                }
                float b = (float) (-Tk * Math.log(2)/224);
                discount = (float) Math.pow(Math.E,b);
            }
            if(rel == 1)
                gain = (float) (0.64*0.77);
            else
                gain = 0;
            TBG += gain * discount;
        }
        return TBG;
    }

//    public static float studentTtest(List<Float> Aps_student_1,List<Float> Aps_student_2){
////        System.out.println(Aps_student_1);
////        System.out.println(Aps_student_2);
//        float mean1=0, mean2=0;
//        float variance1=0, variance2=0;
//        int size = Aps_student_1.size();
//        for(int i=0;i<size;i++){
//            mean1+=Aps_student_1.get(i);
//            mean2+=Aps_student_2.get(i);
//        }
//        mean1 = mean1/size;
//        mean2 = mean2/size;
//        for(int i=0;i<size;i++){
//            variance1+=Math.pow((Aps_student_1.get(i)-mean1),2);
//            variance2+=Math.pow((Aps_student_2.get(i)-mean2),2);
//        }
//        variance1 = variance1/(size-1);
//        variance2 = variance2/(size-1);
//        float sp =0;
//        float t = 0;
//        float df = 0;
//        if(variance1/variance2<4 && variance1/variance2>0.25){
//            float a = (variance1+variance2)/2;
//            sp = (float) Math.pow(a,0.5);
//            float ta = mean1-mean2;
//            float tb = sp*(float) Math.pow((float)2/size,0.5);
//            t = ta/tb;
//            df = size*2 -2;
//        }
//        else{
//            sp = (float) Math.pow((variance1+variance2)/size,0.5);
//            t = (mean1-mean2)/sp;
//            float dfa = (float) Math.pow((variance1+variance2)/size,2);
//            float dfb = (float) ((Math.pow(variance1/size,2)+Math.pow(variance2/size,2))/(size-1));
//            df = dfa/dfb;
//        }
////        System.out.println("mean1="+mean1+" mean2="+mean2);
////        System.out.println("variance1="+variance1+" variance2="+variance2);
//        System.out.print("degree of freedom: "+df);
//        System.out.println(" test statistic: "+t);
//        return 0;
//    }
}
