import java.io.IOException;
import java.util.StringTokenizer;

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






public class fft{    //job1文件补齐

	public static class CountMapper extends Mapper<Object,Text,Text,TextPair>
	{  
		private  Text one = new Text("1");
		int count=1;
	    
		public void map(Object key,Text value,Context context) throws IOException,InterruptedException 
		{
			
			 StringTokenizer itr = new StringTokenizer(value.toString());
		      while (itr.hasMoreTokens())
		      { 
		    	 
		    	   String str=itr.nextToken();	
		    	   String str1=itr.nextToken();
		    	   TextPair cm=new TextPair(str,str1);	   
		           context.write(one,cm); 
		      }
	    }
	}

	
	public static class ExchangeMapper extends Mapper<LongWritable,Text,LongWritable,TextPair>{

		private TextPair cm=new TextPair();
		private long n; 
		private int i;
		private LongWritable t=new LongWritable();
		public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException
		{
			 StringTokenizer itr = new StringTokenizer(value.toString());
			
		      while (itr.hasMoreTokens())
		      { 
		    	 
		    	   String s=itr.nextToken();
		    	   n=Long.parseLong(s);
		    	   long[] a=new  long[5000];
		    	   for( i=0;i<5000;i++)
		    		   a[i]=0;
		    	   i=0;
		    	   while(true)
		    	   {
		    		   if(n==0) break;
		    		   a[i++]=n%2;
		    		   n=n/2;
		    	   }
		    	   //读文件
		    	   Configuration conf = new Configuration();
		    	   String path ="hdfs://localhost:9000/user/root/weishu";
		    	   Path p= new Path(path);
		    	   FileSystem fs = FileSystem.get(conf);
		    	   FSDataInputStream fsr = fs.open(p);
		    	   LineReader in=new LineReader(fsr,conf);
		    	   Text s1=new Text();
		    	   in.readLine(s1,1);
		    	   String s2=s1.toString();  
		    	   for(i=0;i<Long.parseLong(s2);i++)//进行倒位序
		    	   {
		    		 n=n*2+a[i];  
		    	   }
		    	   t.set(n);
		    	   String str=itr.nextToken();	
		    	   String str1=itr.nextToken();
		    	   TextPair cm=new TextPair(str,str1);	   
		           context.write(t,cm); 
		      }
			
		}

	}
	
	public static class DiexingMapper extends Mapper<LongWritable,Text,LongWritable,TextPair >{
		private long t=1;
		private LongWritable l=new LongWritable();
		public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException
		{
			System.out.println("执行DiexingMapper");
		   StringTokenizer itr = new StringTokenizer(value.toString());
		    itr.nextToken();
			String str1=itr.nextToken();	
	 	    String str2=itr.nextToken();
	 	   TextPair cm=new TextPair(str1,str2);	   
	 	   l.set(t);
	       context.write(l,cm); 
	     
		}
	}

	


  public static class BuqiReducer extends Reducer<Text,TextPair,LongWritable,TextPair> 
	{
		 private long count=0;
		 private long n=0;
		 private long sum=1,re;
		 private LongWritable result = new LongWritable();
		 private TextPair text=new TextPair("0","0");
		 private Text textweishu =new Text();
		 public void reduce(Text key,Iterable<TextPair> value,  Context context ) throws IOException, InterruptedException 
           {
			  for (TextPair val : value) {
			     result.set(count);
		    context.write(result,val); 
		      count++;      
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
		      // comon.weishu=n;//用一个类来当全局变量
		       //把位数n写入文件
		       Configuration conf = new Configuration();
		       FileSystem fs = FileSystem.get(conf);
		       Path p=new Path("hdfs://localhost:9000/user/root/weishu");
		       FSDataOutputStream out=fs.create(p);
		       String str=Long.toString(n);
		       out.write(str.getBytes("UTF-8"));
		       out.flush();
		       out.sync();
		   //    assertThat(fs.getFileStatus(p).getLen(),is(((long)str.length())));
		       
		       
		       for(int i=1;i<=n;i++)
		       {
		    	   sum*=2;
		       }
		       for(;re<sum;re++)
		       {
		    	      result.set(re);
				   context.write(result,text);  
		       }
		       System.out.println("执行ruducer");
		   }
		
		
     }
  
  public static class ExchangeReducer extends Reducer<LongWritable, TextPair, LongWritable, TextPair>{
		
	    public void reduce(LongWritable key,TextPair value,Context context)throws IOException,InterruptedException
	    {
	    	System.out.println("执行exchangeReducer");
	    	context.write(key,value);
	    	
	    }
	}
  
  public static class DiexingReducer extends Reducer<LongWritable,TextPair,NullWritable,TextPair> {
	  
		complex[] x=new complex[10000000];
		complex[] w=new complex[10000000];
		int size_x=0,i=0;
		double PI=Math.atan(1)*4;
		
		//int n=2;
		
		void add(complex a,complex b,complex c){
			 c.real=a.real+b.real;
			 c.img=a.img+b.img;
				
			}

			void mul(complex a,complex b,complex c){
			 c.real=a.real*b.real - a.img*b.img;
			 c.img=a.real*b.img + a.img*b.real;
			}
			void sub(complex a,complex b,complex c){
			 c.real=a.real-b.real;
			 c.img=a.img-b.img;
			}
			void divi(complex a,complex b,complex c){
			 c.real=( a.real*b.real+a.img*b.img )/( b.real*b.real+b.img*b.img);
			 c.img=( a.img*b.real-a.real*b.img)/(b.real*b.real+b.img*b.img);
			}
			void fft(){
				 int i=0,j=0,k=0,l=0;
				
				
				 for(i=0;i< Math.log(size_x)/Math.log(2) ;i++){ 
				  l=1<<i;
				  for(j=0;j<size_x;j+= 2*l ){            
				   for(k=0;k<l;k++){  
			     complex product=new complex();
				 complex up=new complex();
				 complex down=new complex();     
				     mul(x[j+k+l],w[size_x*k/2/l],product);
				     add(x[j+k],product,up);
				     sub(x[j+k],product,down);
				     x[j+k]=up;
				     x[j+k+l]=down;
				   }
				  }
				 }
				}
		public void reduce(LongWritable key,Iterable<TextPair> value,Context context)throws IOException,InterruptedException
	    {
			  //读文件
	    	   Configuration conf = new Configuration();
	    	   String path ="hdfs://localhost:9000/user/root/weishu";
	    	   Path p= new Path(path);
	    	   FileSystem fs = FileSystem.get(conf);
	    	   FSDataInputStream fsr = fs.open(p);
	    	   LineReader in=new LineReader(fsr,conf);
	    	   Text s1=new Text();
	    	   in.readLine(s1,1);
	    	   String s2=s1.toString(); 
	    	   
			 size_x=(int)Math.pow(2, Long.parseLong(s2));
		    	System.out.println("执行diexingReducer");
			 for (TextPair val : value) {
			   Text text=val.getFirst();
			   String str=text.toString();  
			   x[i]=new complex();
			   x[i].real=Double.parseDouble(str);
			   Text text1=val.getSecond();
			   String str1=text1.toString();
			   x[i++].img=Double.parseDouble(str1);
			      } 
	    	//////////////////
			//diexing运算
			
		
			System.out.println("执行diexingReducer");
			for(i=0;i<size_x;i++)
			{
				w[i]=new complex();
				w[i].real=Math.cos(2*PI/size_x*i);
				w[i].img=-1*Math.sin(2*PI/size_x*i);
			}
			
			fft();
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
		    i=0;
		    for(i=0;i<size_x;i++)
		    {
		      String str1=Double.toString(x[i].real);
		      String str2=Double.toString(x[i].img);
		      TextPair cm=new TextPair(str1,str2);	
	          context.write(NullWritable.get(),cm); 
		    }
		}    
	    
	}
	
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
 
      System.exit(2);
    }
    Job job = new Job(conf, "fft job1");
    job.setMapOutputValueClass(TextPair.class);
    
    
    job.setJarByClass(fft.class);
    job.setMapperClass(CountMapper.class);
    job.setReducerClass(BuqiReducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(TextPair.class);
    
    
    job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(TextPair.class);
    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    job.waitForCompletion(true);
    
    
    
    /////////////////////////////////////////////
    //exchange的job
    Configuration exchangeconf = new Configuration();
    System.out.println("执行exchangejob");
    String[] exchangeotherArgs = new GenericOptionsParser(exchangeconf, args).getRemainingArgs();
    if (exchangeotherArgs.length != 2) {
 
      System.exit(2);
    }
    Job exchangejob = new Job(exchangeconf, "fft job2");
    exchangejob.setMapOutputValueClass(TextPair.class);
    exchangejob.setMapOutputKeyClass(LongWritable.class);
    
    
    exchangejob.setJarByClass(fft.class);
    exchangejob.setMapperClass(ExchangeMapper.class);
    exchangejob.setReducerClass(ExchangeReducer.class);

    
    exchangejob.setOutputKeyClass(LongWritable.class);
    exchangejob.setOutputValueClass(TextPair.class);
    
   String exchangeinputpath="hdfs://localhost:9000/user/root/output01";
    String exchangeoutputpath="hdfs://localhost:9000/user/root/output02";
    FileInputFormat.addInputPath(exchangejob, new Path(exchangeinputpath));
    FileOutputFormat.setOutputPath(exchangejob, new Path(exchangeoutputpath));
    
    exchangejob.waitForCompletion(true) ;
    
    ///////////////////////////////////////////////////////////
    ///diexingjob
    Configuration diexingconf = new Configuration();
    System.out.println("执行diexing job");
    String[] diexingotherArgs = new GenericOptionsParser(diexingconf, args).getRemainingArgs();
    if (diexingotherArgs.length != 2) {
 
      System.exit(2);
    }
    Job diexingjob = new Job(diexingconf, "fft job3");
    diexingjob.setMapOutputValueClass(TextPair.class);
    
    
    diexingjob.setJarByClass(fft.class);
    diexingjob.setMapperClass(DiexingMapper.class);
    diexingjob.setReducerClass(DiexingReducer.class);
	

    
    diexingjob.setOutputKeyClass(LongWritable.class);
    diexingjob.setOutputValueClass(TextPair.class);
    
    String diexinginputpath="hdfs://localhost:9000/user/root/output02";
    String diexingoutputpath="hdfs://localhost:9000/user/root/output03";
    FileInputFormat.addInputPath(diexingjob, new Path(diexinginputpath));
    FileOutputFormat.setOutputPath(diexingjob, new Path(diexingoutputpath));
    System.exit(diexingjob.waitForCompletion(true) ? 0 : 1);
    
   }
  }


