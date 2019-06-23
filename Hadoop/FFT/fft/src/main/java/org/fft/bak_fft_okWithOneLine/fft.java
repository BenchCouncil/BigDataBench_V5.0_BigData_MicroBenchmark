package org.fft;

import org.fft.TextPair;

import java.io.IOException;
import java.util.StringTokenizer;
import static java.lang.Math.toIntExact; //java8 support

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.LineRecordReader.LineReader;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


public class fft{
    //The first job map：count the dimension and fill in multiple zero elements to get 2^N dimension

    public static class FillinMapper extends Mapper<LongWritable,Text,LongWritable,TextPair>
    {
        long count=0;
        private long n=0;
        private long sum=1,re;

        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException
        {

            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens())
            {
                String str=itr.nextToken();
                String str1=itr.nextToken();
                TextPair cm=new TextPair(count,str,str1);
                count++;
                context.write(key,cm);
            }
            re=count;
            count--;
            while(true)
            {

                if(count==0)
                    break;
                n++;
                count/=2;
            }
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            Path p=new Path("fft-bitNUM");
            FSDataOutputStream out=fs.create(p);
            String str=Long.toString(n);
            out.write(str.getBytes("UTF-8"));
            out.flush();
            out.sync();

            for(int i=1;i<=n;i++)
            {
                sum*=2;
            }
            for(;re<sum;re++) {
                TextPair fillInText = new TextPair(re, "0", "0");
                context.write(key, fillInText);
            }
        }
    }

    //The first job reduce：bit reverse of the element index

    public static class BitReverseMapper extends Mapper<LongWritable,Text,LongWritable,TextPair>{

        static long dimension;

        static{
            try{
                Configuration conf = new Configuration();
                Path p= new Path("fft-bitNUM");
                FileSystem fs = FileSystem.get(conf);
                FSDataInputStream fsr = fs.open(p);
                LineReader in=new LineReader(fsr,conf);
                Text s1=new Text();
                in.readLine(s1,1);
                String s2=s1.toString();
                dimension=Long.parseLong(s2);
            } catch(IOException e) {
                System.out.println("IOException catch");
                System.exit(1);
            }

        }

        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException
        {

            String parameter[] = value.toString().split("\t");
            long lineNUM = Long.parseLong(parameter[0]);
            long index = Long.parseLong(parameter[1]);
            String first=parameter[2];
            String second=parameter[3];

            long res = 0;
            for (long i = 0; i < dimension; ++i) {
                if ((index & 1) == 1) {
                    res = (res << 1) + 1;
                } else {
                    res = res << 1;
                }
                index = index >> 1;
            }
            context.write(new LongWritable(lineNUM),new TextPair(res,first,second));
        }
    }

    /*public static class BitReverseMapper extends Mapper<LongWritable,TextPair,LongWritable,TextPair>{
        public void map(LongWritable key,Iterable<TextPair> value,Context context) throws IOException,InterruptedException
        {
            Configuration conf = new Configuration();
            Path p= new Path("fft-bitNUM");
            FileSystem fs = FileSystem.get(conf);
            FSDataInputStream fsr = fs.open(p);
            LineReader in=new LineReader(fsr,conf);
            Text s1=new Text();
            in.readLine(s1,1);
            String s2=s1.toString();
            long dimension = Long.parseLong(s2);

            for (TextPair val : value) {
                long res = 0;
                long index = val.getIndex().get();
                for (long i = 0; i < dimension; ++i) {
                    if ((index & 1) == 1) {
                        res = (res << 1) + 1;
                    } else {
                        res = res << 1;
                    }
                    index = index >> 1;
                }
                val.setIndex(res);
                context.write(key,val);
            }
        }
    }*/


    public static class ButterflyReducer extends Reducer<LongWritable,TextPair,LongWritable,TextPair> {

        double PI=Math.atan(1)*4;
        static int size_x=0;

        static{
            try{
                Configuration conf = new Configuration();
                Path p= new Path("fft-bitNUM");
                FileSystem fs = FileSystem.get(conf);
                FSDataInputStream fsr = fs.open(p);
                LineReader in=new LineReader(fsr,conf);
                Text s1=new Text();
                in.readLine(s1,1);
                String s2=s1.toString();
                size_x=(int)Math.pow(2, Long.parseLong(s2));
            } catch(IOException e) {
                System.out.println("IOException catch");
                System.exit(1);
            }

        }

        complex[] x=new complex[size_x];
        complex[] w=new complex[size_x];

        void fft(){
            int i=0,j=0,k=0,curPairNum=0;
            for(i=0;i<Math.log(size_x);i++){
                curPairNum = 1<<i;
                for(j=0;j<size_x;j+=(curPairNum*2)){
                    for(k=0;k<curPairNum;k++){
                        complex wMatH = new complex();
                        complex GaddWH = new complex();
                        complex GsubWH = new complex();
                        wMatH.mul(x[j+k+curPairNum],w[size_x*k/2/curPairNum]);
                        GaddWH.add(x[j+k],wMatH);
                        GsubWH.sub(x[j+k],wMatH);
                        x[j+k]=GaddWH;
                        x[j+k+curPairNum]=GsubWH;
                    }
                }
            }
        }

        /*void fft(complex[] & x, complex[] & w){
            int i=0,j=0,k=0,l=0;

            for(i=0;i< Math.log(size_x)/Math.log(2) ;i++){
                l=1<<i;
                for(j=0;j<size_x;j+= 2*l ){
                    for(k=0;k<l;k++){
                        complex product=new complex();
                        complex up=new complex();
                        complex down=new complex();
                        //mul(x[j+k+l],w[size_x*k/2/l],product);
                        product.mul(x[j+k+l],w[size_x*k/2/l]);
                        //add(x[j+k],product,up);
                        up.add(x[j+k],product);
                        //sub(x[j+k],product,down);
                        down.sub(x[j+k],product);
                        x[j+k]=up;
                        x[j+k+l]=down;
                    }
                }
            }
        }*/

        public void reduce(LongWritable key,Iterable<TextPair> value,Context context)throws IOException,InterruptedException
        {

            for (TextPair val : value) {
                int _index = toIntExact(val.getIndex().get()); //java8 support
                Text text=val.getFirst();
                String str=text.toString();
                x[_index] = new complex();
                x[_index].real=Double.parseDouble(str);
                Text text1=val.getSecond();
                String str1=text1.toString();
                x[_index].img=Double.parseDouble(str1);
            }

            for(int i=0;i<size_x;i++)
            {
                w[i] = new complex();
                w[i].real=Math.cos(2*PI/size_x*i);
                w[i].img=-1*Math.sin(2*PI/size_x*i);
            }

            fft();

            /*int i=0,j=0,k=0,l=0;

            for(i=0;i< Math.log(size_x)/Math.log(2) ;i++){
                l=1<<i;
                for(j=0;j<size_x;j+= 2*l ){
                    for(k=0;k<l;k++){
                        complex product=new complex();
                        complex up=new complex();
                        complex down=new complex();
                        //mul(x[j+k+l],w[size_x*k/2/l],product);
                        product.mul(x[j+k+l],w[size_x*k/2/l]);
                        //add(x[j+k],product,up);
                        up.add(x[j+k],product);
                        //sub(x[j+k],product,down);
                        down.sub(x[j+k],product);
                        x[j+k]=up;
                        x[j+k+l]=down;
                    }
                }
            }*/


            for(int s=0;s<size_x;s++)
            {
                if(Math.abs(x[s].real)<0.0001)
                    x[s].real=0;
                if(Math.abs(x[s].img)<0.00000000001)
                    x[s].img=0;
                System.out.println(x[s].real);
                System.out.println(x[s].img);
            }
            /////写入文件
            for(int i=0;i<size_x;i++)
            {
                String str1=Double.toString(x[i].real);
                String str2=Double.toString(x[i].img);
                TextPair cm=new TextPair(i,str1,str2);
                context.write(key,cm);
            }
        }

    }



    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 3) {

            System.exit(1);
        }

        //////////////////The first job：fill in element//////////////////////
        Job job = new Job(conf, "fft job1");
        job.setMapOutputValueClass(TextPair.class);

        job.setJarByClass(fft.class);
        job.setMapperClass(FillinMapper.class);
        job.setNumReduceTasks(0);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(TextPair.class);

        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(TextPair.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        job.waitForCompletion(true);


        //////////////////The second job：bit reverse in map and butterfly in reduce//////////////////////
        Configuration butterflyconf = new Configuration();
        System.out.println("Job3-butterfly computation");

        Job butterflyjob = new Job(butterflyconf, "fft job3");
        butterflyjob.setMapOutputKeyClass(LongWritable.class);
        butterflyjob.setMapOutputValueClass(TextPair.class);


        butterflyjob.setJarByClass(fft.class);
        butterflyjob.setMapperClass(BitReverseMapper.class);
        butterflyjob.setReducerClass(ButterflyReducer.class);


        butterflyjob.setOutputKeyClass(LongWritable.class);
        butterflyjob.setOutputValueClass(TextPair.class);

        FileInputFormat.addInputPath(butterflyjob, new Path(otherArgs[1]));
        FileOutputFormat.setOutputPath(butterflyjob, new Path(otherArgs[2]));
        System.exit(butterflyjob.waitForCompletion(true) ? 0 : 1);

    }
}


