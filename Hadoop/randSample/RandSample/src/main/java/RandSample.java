//package main.java;


import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;
import static java.lang.Math.toIntExact; //java8 support

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

//import org.apache.hadoop.mapreduce.Counter;


public class RandSample {
    //The first job map: just output the input value to reduce

    public static class sampler extends Mapper<LongWritable, Text, NullWritable, Text> {

        static double threshold;
        private Random rand = new Random();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            String param = conf.get("threshold");
            threshold = Double.parseDouble(param);

            if(rand.nextDouble() < threshold){
                context.write(NullWritable.get(),value);
            }
        }
    }



    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.set("threshold",args[2]);

        Job job = new Job(conf, "RandSample");

        job.setJarByClass(RandSample.class);
        job.setMapperClass(sampler.class);
        //job.setReducerClass(FillinReducer.class);
        job.setNumReduceTasks(0);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.waitForCompletion(true);




    }
}



