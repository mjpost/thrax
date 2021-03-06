package edu.jhu.thrax.hadoop.datatypes;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.io.TwoDArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AlignmentArray implements WritableComparable<AlignmentArray>
{
    private Text [][] array;

    public AlignmentArray()
    {
        // do nothing
    }

    public AlignmentArray(Text [][] values)
    {
        array = values;
    }

    public void set(Text [][] values)
    {
        array = values;
    }

    public Text [][] get()
    {
        return array;
    }

    public void readFields(DataInput in) throws IOException
    {
        array = new Text[in.readInt()][];
        for (int i = 0; i < array.length; i++)
            array[i] = new Text[in.readInt()];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                Text t = new Text();
                t.readFields(in);
                array[i][j] = t;
            }
        }
    }

    public void write(DataOutput out) throws IOException
    {
        out.writeInt(array.length);
        for (int i = 0; i < array.length; i++)
            out.writeInt(array[i].length);

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j].write(out);
            }
        }
    }

    public int compareTo(AlignmentArray other)
    {
        Text [][] mine = array;
        Text [][] theirs = other.get();
        if (mine.length != theirs.length)
            return mine.length - theirs.length;
        for (int i = 0; i < mine.length; i++) {
            Text [] mc = mine[i];
            Text [] tc = theirs[i];
            if (mc.length != tc.length)
                return mc.length - tc.length;
            for (int j = 0; j < mc.length; j++) {
                int cmp = mc[j].compareTo(tc[j]);
                if (cmp != 0)
                    return cmp;
            }
        }
        return 0;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof AlignmentArray))
            return false;
        AlignmentArray other = (AlignmentArray) o;
        return (compareTo(other) == 0);
    }

    public int hashCode()
    {
        Text [][] text = array;
        int result = 163;
        result = result * 37 + text.length;
        for (Text [] ct : text) {
            result = result * 37 + ct.length;
            for (Text t : ct)
                result = result * 37 + t.hashCode();
        }
        return result;
    }

    public static class Comparator extends WritableComparator
    {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();
        public Comparator()
        {
            super(AlignmentArray.class);
        }

        public int compare(byte [] b1, int s1, int l1,
                           byte [] b2, int s2, int l2)
        {
            int h1 = readInt(b1, s1);
            int h2 = readInt(b2, s2);
            if (h1 != h2)
                return h1 - h2;
            int idx1 = s1 + 4;
            int idx2 = s2 + 4;
            int total = 0;
            for (int i = 0; i < h1; i++) {
                int r1 = readInt(b1, idx1);
                int r2 = readInt(b2, idx2);
                if (r1 != r2)
                    return r1 - r2;
                idx1 += 4;
                idx2 += 4;
                total += r1;
            }
            for (int j = 0; j < total; j++) {
                try {
                    int length1 = WritableUtils.decodeVIntSize(b1[idx1]) + readVInt(b1, idx1);
                    int length2 = WritableUtils.decodeVIntSize(b2[idx2]) + readVInt(b2, idx2);
                    int cmp = TEXT_COMPARATOR.compare(b1, idx1, length1, b2, idx2, length2);
                    if (cmp != 0)
                        return cmp;
                    idx1 += length1;
                    idx2 += length2;
                }
                catch (IOException e) {
                    throw new IllegalArgumentException();
                }
            }
            return 0;
        }

        public int encodedLength(byte [] b1, int s1)
        {
            int result = 0;
            int idx = s1;
            int h = readInt(b1, s1);
            idx += 4;
            int numTexts = 0;
            for (int i = 0; i < h; i++) {
                numTexts += readInt(b1, idx);
                idx += 4;
            }
            for (int j = 0; j < numTexts; j++) {
                try {
                    int length = WritableUtils.decodeVIntSize(b1[idx]) + readVInt(b1, idx);
                    idx += length;
                }
                catch (IOException e) {
                    throw new IllegalArgumentException();
                }
            }
            return idx - s1;
        }
    }

    static {
        WritableComparator.define(AlignmentArray.class, new Comparator());
    }
}
