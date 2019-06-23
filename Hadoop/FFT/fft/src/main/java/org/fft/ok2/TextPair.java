package org.fft;

import org.fft.complex;

import java.io.*;
import java.util.List;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class TextPair implements WritableComparable<TextPair> {
    private LongWritable index;
    private Text first;
    private Text second;
    
    public TextPair() {
        set(new LongWritable(), new Text(), new Text());
    }
    
    public TextPair(long index, String first, String second) {
        set(new LongWritable(index), new Text(first), new Text(second));
    }
    
    public TextPair(LongWritable index, Text first, Text second) {
        set(index, first, second);
    }
    
    public void set(LongWritable index, Text first, Text second) {
        this.index = index;
        this.first = first;
        this.second = second;
    }
    
    public void setIndex(long index) {
        LongWritable _index = new LongWritable(index);
        this.index = _index;
    }
    
    public LongWritable getIndex() {
        return index;
    }
    
    public Text getFirst() {
        return first;
    }
    public Text getSecond() {
        return second;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        index.write(out);
        first.write(out);
        second.write(out);
    }
    
    @Override
    public void readFields(DataInput in) throws IOException {
        index.readFields(in);
        first.readFields(in);
        second.readFields(in);
    }
    @Override
    public int hashCode() {
        return first.hashCode() * 163 + second.hashCode();
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof TextPair) {
            TextPair tp = (TextPair) o;
            return first.equals(tp.first) && second.equals(tp.second);
        }
        return false;
    }
    @Override
    public String toString() {
        return Long.toString(index.get()) + "\t" + first + "\t" + second;
    }
    @Override
    public int compareTo(TextPair tp) {
        int cmp = first.compareTo(tp.first);
        if (cmp != 0) {
            return cmp;
        }
        return second.compareTo(tp.second);
    }
    
}
