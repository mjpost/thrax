package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.IntWritable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import java.io.IOException;

public class MinRuleCountReducer extends Reducer<RuleWritable,IntWritable,RuleWritable,IntWritable>
{
    /**
     * The minimum number of times a rule must be seen in the corpus in order
     * to be outputted by this reducer.
     */
    private int minCount;

    public void setup(Context context) throws IOException, InterruptedException
    {
        Configuration conf = context.getConfiguration();
        minCount = conf.getInt("thrax.min-rule-count", 1);
        return;
    }

    protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
    {
        int sum = 0;
        for (IntWritable v : values)
            sum += v.get();
        if (sum >= minCount)
            context.write(key, new IntWritable(sum));
        return;
    }
}

