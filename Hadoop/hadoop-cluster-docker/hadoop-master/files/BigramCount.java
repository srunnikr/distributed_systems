import java.io.IOException;
import java.util.StringTokenizer;
import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class BigramCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private String previous = "";

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      String alphaNumStr = value.toString().replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
      StringTokenizer itr = new StringTokenizer(alphaNumStr);
      // while (itr.hasMoreTokens()) {
      //   word.set(itr.nextToken());
      //   context.write(word, one);
      // }
      if (itr.hasMoreTokens()) {
        if (previous.equals("")){
          previous = itr.nextToken();
        }
        
        while (itr.hasMoreTokens()) {
          String current = itr.nextToken();
          word.set(previous +" "+ current);
          context.write(word, one);
          previous = current;
        }
      }
      
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  // public void processOutput(Path outputPath){
  //   listFilesForFolder(new File(outputPath))
  // }

  public static void listFilesForFolder(final File folder) {
    for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            listFilesForFolder(fileEntry);
        } else {
            System.out.println(fileEntry.getName());
        }
    }
}

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(BigramCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
    //job.waitForCompletion(true);

    //listFilesForFolder(new File(args[1]));
  }
}