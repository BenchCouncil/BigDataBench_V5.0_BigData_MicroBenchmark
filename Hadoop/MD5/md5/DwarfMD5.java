import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import java.security.*;
import org.apache.commons.codec.binary.Hex;


public class DwarfMD5 extends Configured implements Tool {
	
	public static class Map extends Mapper<LongWritable, Text, NullWritable, Text>{
		public void map(LongWritable key, Text value, Context context) throws IOException,InterruptedException{
			byte[] line=value.getBytes();
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(line);
				byte[] digest = md.digest();
				String md5 = new String(Hex.encodeHex(digest));
				Text md5Value=new Text(md5);
				context.write(NullWritable.get(), md5Value);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("md5 computation error", e);
				}
		}
	}
	
	public int run(String[] arg) throws Exception{
		Configuration conf=getConf();
		FileSystem fs=FileSystem.get(conf);
		Job job=new Job(conf,"DwarfMD5");
		job.setJarByClass(getClass());
		FileInputFormat.setInputPaths(job,arg[0]);
		Path outDir=new Path(arg[1]);
		fs.delete(outDir,true);
		FileOutputFormat.setOutputPath(job, outDir);
		job.setNumReduceTasks(0);
		job.setMapperClass(Map.class);
		job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
        //job.setOutputFormatClass(.class);
        return job.waitForCompletion(true)?0:1;
	}
	
	public static void main(String[] args) throws Exception{
		int res=ToolRunner.run(new Configuration(),new DwarfMD5(), args);
		System.exit(res);
	}

}
