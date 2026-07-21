import java.util.*;
/** Base collector — polymorphic entry point (virtual dispatch). */
abstract class MetricCollector {
    abstract void collect(long docValue);
}
/** Sum aggregation partial collector. */
class SumCollector extends MetricCollector {
    private long partialSum;
    @Override
    void collect(long docValue) {
        // Simulate lightweight per-doc work
        partialSum += docValue;
    }
    long getPartialSum() { return partialSum; }
}
/** Max aggregation partial collector. */
class MaxCollector extends MetricCollector {
    private long partialMax = Long.MIN_VALUE;
    @Override
    void collect(long docValue) {
        if (docValue > partialMax) partialMax = docValue;
    }
    long getPartialMax() { return partialMax; }
}
/** Count aggregation partial collector. */
class CountCollector extends MetricCollector {
    private long docCount;
    @Override
    void collect(long docValue) {
        docCount++;
    }
    long getDocCount() { return docCount; }
}
public class AggregationVirtualMethodsDemo {
    public static void main(String[] args) {
        int totalDocs = 100_000_000; // large dataset for measurable difference
        List<MetricCollector> collectors = new ArrayList<>(totalDocs);
        // Mixed metric collectors — like heterogeneous agg pipeline per doc
        for (int i = 0; i < totalDocs; i++) {
            if (i % 3 == 0) collectors.add(new SumCollector());
            else if (i % 3 == 1) collectors.add(new MaxCollector());
            else collectors.add(new CountCollector());
        }
        // --- Per-doc polymorphic collection (virtual dispatch every time) ---
        long start = System.nanoTime();
        for (int doc = 0; doc < totalDocs; doc++) {
            long docValue = doc % 7; // pretend doc_values[doc]
            collectors.get(doc).collect(docValue); // virtual call per doc
        }
        long end = System.nanoTime();
        System.out.printf(
            "Per-doc polymorphic collect time: %.2f ms%n",
            (end - start) / 1_000_000.0
        );
        // --- Group by collector type (bulk-friendly layout) ---
        List<SumCollector> sumCollectors = new ArrayList<>();
        List<MaxCollector> maxCollectors = new ArrayList<>();
        List<CountCollector> countCollectors = new ArrayList<>();
        for (MetricCollector c : collectors) {
            if (c instanceof SumCollector) sumCollectors.add((SumCollector) c);
            else if (c instanceof MaxCollector) maxCollectors.add((MaxCollector) c);
            else countCollectors.add((CountCollector) c);
        }
        // --- Grouped / monomorphic collection ---
        start = System.nanoTime();
        for (int i = 0; i < sumCollectors.size(); i++) {
            sumCollectors.get(i).collect(i % 7);
        }
        for (int i = 0; i < maxCollectors.size(); i++) {
            maxCollectors.get(i).collect(i % 7);
        }
        for (int i = 0; i < countCollectors.size(); i++) {
            countCollectors.get(i).collect(i % 7);
        }
        end = System.nanoTime();
        System.out.printf(
            "Bulk grouped collect time: %.2f ms%n",
            (end - start) / 1_000_000.0
        );
    }
}