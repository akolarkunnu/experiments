public class BulkCollectionInliningDemo {
    public static void main(String[] args) {
        NumericSumCollector collector = new NumericSumCollector();
        for (int doc = 0; doc < 1_000_000; doc++) {
            long value = readDocValue(doc);          // pretend doc_values
            long running = collector.getRunningSum(); // JIT may inline
            collector.setRunningSum(running + value); // JIT may inline
        }
        System.out.println("Collected sum: " + collector.getRunningSum());
    }
    static long readDocValue(int doc) { return doc % 7; } // stand-in for doc_values access
}
class NumericSumCollector {
    private long runningSum;
    public long getRunningSum() { return runningSum; }
    public void setRunningSum(long sum) { this.runningSum = sum; }
}
