package org.fft;

import org.fft.TextPair;

import java.io.IOException;
import java.util.StringTokenizer;
import static java.lang.Math.toIntExact; //java8 support

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

//import org.apache.hadoop.mapreduce.Counter;


public class fft{
    //The first job map: just output the input value to reduce

    public static class FillinMapper extends Mapper<LongWritable,Text,LongWritable,Text> {


        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            context.write(key,value);
        }
    }

    //The first job reduce: count the dimension and fill in multiple zero elements to get 2^N dimension
    //the number with the same key reduce to the same reducer, and do the colume number count
    //if the count computation implemented in map, the variable count will be wrong and count all rows
    public static class FillinReducer extends Reducer<LongWritable,Text,LongWritable,TextPair> {

        static long dimension;
        static long size_x;

        public void reduce(LongWritable key, Iterable<Text> value, Context context ) throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            String param = conf.get("dimension");
            dimension = Long.parseLong(param);
            size_x = (long)Math.pow(2, dimension);

            for (Text val : value) {
                long count = 0;
                StringTokenizer itr = new StringTokenizer(val.toString());
                while (itr.hasMoreTokens()) {
                    String str = itr.nextToken();
                    String str1 = itr.nextToken();
                    TextPair cm = new TextPair(count, str, str1);
                    count++;
                    context.write(key, cm);
                }
                for(;count<size_x;++count) {
                    TextPair fillInText = new TextPair(count, "0", "0");
                    context.write(key, fillInText);
                }
            }
        }
    }

    //The second job map：bit reverse of the element index

    public static class BitReverseMapper extends Mapper<LongWritable,Text,LongWritable,TextPair>{

        static long dimension;


        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException
        {

            Configuration conf = context.getConfiguration();
            String param = conf.get("dimension");
            dimension = Long.parseLong(param);

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

    //The second job reduce: using butterfly method to compute fft
    public static class ButterflyReducer extends Reducer<LongWritable,TextPair,LongWritable,TextPair> {

        static long dimension;
        static int size_x;


        /*****************************************************************************************
        ///具体fft的实现:
        ///首先按照2为基底补齐元素,size_x表示补齐后的向量总维度(实现于第一个mapreduce job)
         //其次将元素的下标index按位反转,反转后的新index即是迭代求取fft的元素顺序(实现于第二个job的map阶段)
         //此reduce阶段即是迭代求取fft的具体过程,将元素按照index为奇数和偶数划分

         //公式是 X(K) = G(K) + W(K,N)H(K) (此处的W(K,N)中的N表示以N为底,即元素的log值)
         //      X(K+N/2) = G(K) - W(K,N)H(K)

         // W(K,N) = e^(-2*PI*i*K/N),赋值为: (此处 size_x 相当于 N)
         //     for(int i=0;i<size_x;i++)
                {
                    w[i] = new complex();
                    w[i].real=Math.cos(2*PI/size_x*i);
                    w[i].img=-1*Math.sin(2*PI/size_x*i);
                }

         //     x0  x1  x2  x3  x4  x5  x6  x7 -- 所求fft的向量   (计算中元素按照下标的奇偶划分计算)
                  (x0,x2,x4,x6)     (x1,x3,x5,x7)  -- 迭代第2层划分 i=2
                (x0,x4)  (x2,x6)  (x1,x5)  (x3,x7) -- 迭代第1层划分 i=1
                x0  x4    x2  x6   x1  x5   x3  x7  -- 迭代第0层划分i=0 (此时元素的排列顺序即是位反转之后的排列顺序)

         // i: 表示迭代计算中树的层次, 一层层往上迭代计算, 最后求到原始的向量
         // curPairNum: 表示当前层次的组合元素,即2^i,表示当前层的一个G或者一个H的个数
         // j: 计算每一层中一组GH的初始地址,例如,在第一层中,(x0,x4)是G(0)和G(1),(x2,x6)是H(0)和H(1),用于计算(x0,x2,x4,x6)的fft;
                那么当前层一组GH的大小是2*curPairNum个,下一个GH分别是(x1,x5)和(x3,x7),用于迭代计算(x1,x3,x5,x7)的fft;
                因此,j用于遍历每一层中的一组GH,且每次跳的值为 j += 2*curPairNum;
         // k: 计算每一层中每一组GH内部的元素值。且当前计算的值分别对应下一层计算中的G(0),G(1)...H(0),H(1)...
                j+k表示该层第j个GH组的计算第k个下标
                每一个GH内部,G有curPairNum个,H有curPairNum个,因此
                G[0 - (curPairNum-1)]的下标为 j, j+1, ... , j+curPairNum-1
                H[0 - (curPairNum-1)]的下标为 j+curPairNum, j+curPariNum+1, ... , j+2*curPairNum-1
                因此 k 取值从 0 到 curPairNum, j+k的值表示当前层的G的下标, j+k+curPairNum的值表示当前层的H的下标
         // wMatH: 公式中 W(K,N)H(K) 的值, H(K)的元素为x[j+k+curPairNum], W(K,N)的值计算如下:
                在下面reduce方法中的w[i]循环赋初值的时候,默认的N是size_x,即所求向量按基底2补齐之后的维度,上述示例中为8;
                然而在每一次迭代的过程中,所计算的向量的维度并不是size_x,而是 2*curPairNum,例如在第一层中,(X0,X4)和(X2,X6)计算的是
                (X0,X2,X4,X6)这个向量,因此其维度实际为4,需要的是W(K,4),然而赋w初值的时候都是按照W(0~size_x,size_x)赋值的。
                由于W(aK,aN)=W(K,N),我们需要计算W(k,2curPairNum)的值,根据W的赋值我们知道W(?,size_x)的值,但是我们需要知道对应
                所求的下标号,根据公式得出 ? = size_x * k/2/curPairNum;
                因此, wMatH = x[j+k+curPairNum] * w[size_x * k/2/curPairNum];
         // GaddWH: 公式中的 G(K) + W(K,N)H(K); G(K)对应x[j+k],所以 GaddWH = x[j+k] + wMatH;
         // GsubWH: 公式中的 G(K) - W(K,N)H(K); G(K)对应x[j+k],所以 GaddWH = x[j+k] - wMatH;
         // 最后,根据上述公式, X(K) = GaddWH;
                            X(K+N/2) = X(K+curPairNum) = GsubWH;
         ******************************************************************************************/
        /*void fft(){
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
        }*/

        public void reduce(LongWritable key,Iterable<TextPair> value,Context context)throws IOException,InterruptedException
        {
            Configuration conf = context.getConfiguration();
            String param = conf.get("dimension");
            dimension = Long.parseLong(param);
            size_x = (int)Math.pow(2, dimension);

            complex[] x=new complex[size_x];
            complex[] w=new complex[size_x];

            for (TextPair val : value) {
                int _index = toIntExact(val.getIndex().get()); //java8 support
                Text text=val.getFirst();
                String str=text.toString();
                x[_index] = new complex();
                x[_index].real=Double.parseDouble(str);
                Text text1=val.getSecond();
                String str1=text1.toString();
                x[_index].img=Double.parseDouble(str1);
                //Counter countPrint = context.getCounter("TextPair", _index+" "+x[_index].real+" "+x[_index].img);
            }

            for(int i=0;i<size_x;++i)
            {
                w[i] = new complex();
                w[i].real=Math.cos(2*Math.PI/size_x*i);
                w[i].img=-1*Math.sin(2*Math.PI/size_x*i);
            }

            //fft();
            for(int i=0;i<dimension;++i){
                int curPairNum = 1<<i;
                for(int j=0;j<size_x;j+=(curPairNum*2)){
                    for(int k=0;k<curPairNum;++k){
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

            for(int s=0;s<size_x;++s)
            {
                if(Math.abs(x[s].real)<0.0001)
                    x[s].real=0;
                if(Math.abs(x[s].img)<0.00000000001)
                    x[s].img=0;
            }
            /////写入文件
            for(int i=0;i<size_x;++i)
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
        conf.set("dimension",args[3]);

        //////////////////The first job：fill in element//////////////////////
        Job job = new Job(conf, "fft job1-fillin");

        job.setJarByClass(fft.class);
        job.setMapperClass(FillinMapper.class);
        job.setReducerClass(FillinReducer.class);
        //job.setNumReduceTasks(0);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(TextPair.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.waitForCompletion(true);


        //////////////////The second job：bit reverse in map and butterfly in reduce//////////////////////
        Configuration butterflyconf = new Configuration();
        butterflyconf.set("dimension",args[3]);

        Job butterflyjob = new Job(butterflyconf, "fft job2-butterfly");
        butterflyjob.setMapOutputKeyClass(LongWritable.class);
        butterflyjob.setMapOutputValueClass(TextPair.class);


        butterflyjob.setJarByClass(fft.class);
        butterflyjob.setMapperClass(BitReverseMapper.class);
        butterflyjob.setReducerClass(ButterflyReducer.class);


        butterflyjob.setOutputKeyClass(LongWritable.class);
        butterflyjob.setOutputValueClass(TextPair.class);

        FileInputFormat.addInputPath(butterflyjob, new Path(args[1]));
        FileOutputFormat.setOutputPath(butterflyjob, new Path(args[2]));
        System.exit(butterflyjob.waitForCompletion(true) ? 0 : 1);

    }
}


