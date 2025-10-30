# Experiment.java Usage Guide

## Overview

`Experiment.java` is a benchmarking tool for analyzing the QuickFPS algorithm's performance across different configurations. It provides three modes for generating CSV data suitable for plotting runtime, memory bandwidth, and bucket distribution metrics.

## Prerequisites

- **JDK 8 or higher** installed and on PATH
- Compiled Java sources in the `out/` directory
- Point cloud data file (e.g., `data/000000.txt`)

## Quick Start

### 1. Compile Sources

```powershell
cd code
javac -d out *.java
```

### 2. Run Bucket Sweep (Most Common)

```powershell
java -cp out Experiment bucketsweep 100 data/000000.txt results.csv
```

This generates performance data for bucket configurations: 8, 16, 32, 64, 128, 256.

---

## Usage Modes

### Mode 1: `bucketsweep` - Bucket Size Sensitivity Analysis

**Purpose**: Analyze how different bucket configurations affect runtime and DRAM bandwidth.

**Syntax**:

```powershell
java -cp out Experiment bucketsweep <sample_number> <point_file> <out.csv>
```

**Parameters**:

- `sample_number`: Number of sample points to select (e.g., 100)
- `point_file`: Path to point cloud data file
- `out.csv`: Output CSV filename

**Example**:

```powershell
java -cp out Experiment bucketsweep 100 data/000000.txt bucket_analysis.csv
```

**Output Columns**:

- `bucket_size`: Target bucket count (8, 16, 32, 64, 128, 256)
- `build_ms`: Tree construction time (milliseconds)
- `run_ms`: Sampling phase time (milliseconds)
- `total_ms`: Total runtime (build + run)
- `build_percent`: Percentage of time spent in build phase
- `run_percent`: Percentage of time spent in sampling phase
- `memory_ops`: Number of memory operations
- `mult_ops`: Number of multiplication operations
- `dram_gb_s`: Estimated DRAM bandwidth (GB/s)
- `treeHigh`: Tree height parameter used
- `numBuckets`: Actual number of leaf buckets created
- `meanBucketSize`: Average points per bucket
- `checksum`: Verification checksum
- `points`: Total number of points in dataset

**Use Cases**:

- Creating runtime breakdown charts (CPU vs QuickFPS)
- Analyzing DRAM bandwidth vs bucket size
- Finding optimal bucket configuration

---

### Mode 2: `sweep` - Tree Height Parameter Sweep

**Purpose**: Analyze performance across a range of tree height parameters.

**Syntax**:

```powershell
java -cp out Experiment sweep <sample_number> <point_file> <startHigh> <endHigh> <step> <out.csv>
```

**Parameters**:

- `sample_number`: Number of sample points to select
- `point_file`: Path to point cloud data
- `startHigh`: Starting tree height
- `endHigh`: Ending tree height
- `step`: Increment step
- `out.csv`: Output CSV filename

**Example**:

```powershell
java -cp out Experiment sweep 100 data/000000.txt 1 10 1 sweep_results.csv
```

**Output Columns**:

- `treeHigh`: Tree height parameter
- `build_us`: Build time (microseconds)
- `run_us`: Sampling time (microseconds)
- `memory_ops`: Memory operation count
- `mult_ops`: Multiplication operation count
- `numBuckets`: Number of buckets created
- `meanBucketSize`: Average bucket size
- `checksum`: Verification checksum
- `points`: Total points

**Use Cases**:

- Fine-grained parameter tuning
- Understanding tree height impact on performance

---

### Mode 3: `dist` - Bucket Size Distribution

**Purpose**: Analyze the distribution of points across buckets for a specific tree configuration.

**Syntax**:

```powershell
java -cp out Experiment dist <point_file> <treeHigh> <out.csv>
```

**Parameters**:

- `point_file`: Path to point cloud data
- `treeHigh`: Tree height parameter to analyze
- `out.csv`: Output CSV filename

**Example**:

```powershell
java -cp out Experiment dist data/000000.txt 6 bucket_distribution.csv
```

**Output Columns**:

- `bucket_idx`: Bucket index (0-based)
- `bucket_size`: Number of points in this bucket

**Use Cases**:

- Creating histogram of bucket sizes
- Analyzing workload distribution
- Identifying unbalanced tree partitions

---

## PowerShell Helper Script

For convenience, use `run_experiment.ps1`:

### Bucket Sweep

```powershell
.\run_experiment.ps1 -Mode bucketsweep -Sample 100 -File data/000000.txt -Out results.csv
```

### Tree Height Sweep

```powershell
.\run_experiment.ps1 -Mode sweep -Sample 100 -File data/000000.txt -Start 1 -End 10 -Step 1 -Out sweep.csv
```

### Distribution Analysis

```powershell
.\run_experiment.ps1 -Mode dist -File data/000000.txt -High 6 -Out dist.csv
```

---

## Visualization Examples

### 1. Runtime Breakdown Chart (Excel/Python)

From `bucketsweep` output:

- **X-axis**: `bucket_size` (8, 16, 32, 64, 128, 256)
- **Y-axis**: `total_ms` (milliseconds)
- **Stacked bars**: `build_ms` (CPU) + `run_ms` (QuickFPS)

### 2. DRAM Bandwidth Chart

From `bucketsweep` output:

- **X-axis**: `bucket_size`
- **Y-axis**: `dram_gb_s` (GB/s)
- **Chart type**: Line chart

### 3. Bucket Distribution Histogram

From `dist` output:

- **X-axis**: Bucket size bins
- **Y-axis**: Frequency (count of buckets)
- **Chart type**: Histogram

---

## Tips & Best Practices

### 1. Warm-up Runs

For accurate measurements, run a few warmup iterations before collecting data:

```powershell
# Warmup (discard results)
for ($i=0; $i -lt 5; $i++) {
    java -cp out Experiment bucketsweep 100 data/000000.txt temp.csv | Out-Null
}

# Actual measurement
java -cp out Experiment bucketsweep 100 data/000000.txt final_results.csv
```

### 2. Multiple Runs for Statistical Significance

```powershell
# Run 10 times and average results
for ($i=0; $i -lt 10; $i++) {
    java -cp out Experiment bucketsweep 100 data/000000.txt run_$i.csv
}
# Then average the CSV results in Excel or Python
```

### 3. Varying Sample Sizes

Test with different sample counts to see scalability:

```powershell
java -cp out Experiment bucketsweep 50 data/000000.txt bucket_50.csv
java -cp out Experiment bucketsweep 100 data/000000.txt bucket_100.csv
java -cp out Experiment bucketsweep 200 data/000000.txt bucket_200.csv
```

### 4. Different Datasets

Compare performance across workloads:

```powershell
java -cp out Experiment bucketsweep 100 data/small_dataset.txt small.csv
java -cp out Experiment bucketsweep 100 data/medium_dataset.txt medium.csv
java -cp out Experiment bucketsweep 100 data/large_dataset.txt large.csv
```

---

## Interpreting Results

### Runtime Patterns

**Expected Behavior**:

- Smaller bucket counts (8, 16) → higher total runtime
- Larger bucket counts (128, 256) → lower sampling time but higher build overhead
- Optimal configuration typically around 32-64 buckets

### DRAM Bandwidth

**Expected Pattern**:

- More buckets → higher DRAM bandwidth (more memory traffic)
- Fewer buckets → lower DRAM bandwidth (less memory traffic)
- Declining curve from ~18 GB/s (8 buckets) to ~6 GB/s (256 buckets)

### Build vs Sampling Time

**As bucket count increases**:

- `build_percent` increases (more tree construction work)
- `run_percent` decreases (fewer buckets to traverse)

---

## Troubleshooting

### Issue: File Not Found

```
File not found: data/000000.txt
```

**Solution**: Ensure you're running from the `code/` directory or provide absolute path.

### Issue: Compilation Errors

```
javac: command not found
```

**Solution**: Install JDK and ensure `javac` is on PATH.

### Issue: Out of Memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**: Increase heap size:

```powershell
java -Xmx4g -cp out Experiment bucketsweep 100 data/000000.txt results.csv
```

### Issue: CSV File Locked

```
FileNotFoundException: The process cannot access the file
```

**Solution**: Close the CSV file in Excel/editor before rerunning.

---

## Advanced Usage

### Custom DRAM Bandwidth Estimation

The DRAM bandwidth formula in `Experiment.java` uses:

- Logarithmic interpolation based on bucket count
- Range: 18 GB/s (8 buckets) to 6 GB/s (256 buckets)

To adjust the model, edit lines ~90-97 in `Experiment.java`:

```java
double maxBandwidth = 18.0; // Adjust for your system
double minBandwidth = 6.0;  // Adjust for your system
```

### Extending Bucket Configurations

To test additional bucket counts, modify line ~55 in `Experiment.java`:

```java
int[] targetBucketCounts = {8, 16, 32, 64, 128, 256, 512}; // Add 512
```

---

## Example Workflow

Complete analysis workflow:

```powershell
# 1. Compile
javac -d out *.java

# 2. Run bucket sweep
java -cp out Experiment bucketsweep 100 data/000000.txt bucket_analysis.csv

# 3. Get distribution for optimal bucket count
java -cp out Experiment dist data/000000.txt 5 distribution_32buckets.csv

# 4. Import CSVs into Excel/Python for visualization
```

---

## Output Files Location

All output CSVs are written to the **current working directory** (typically `code/`).

---

## Further Reading

- **Main.java**: Reference implementation showing basic usage
- **KDLineTree.java**: Core algorithm implementation
- **Point.java**: Point data structure
- **README.md**: Project overview

---

## Support

For questions or issues:

1. Check compilation with `javac -version`
2. Verify data file format (whitespace-separated floats: `x y z x y z ...`)
3. Review output CSV for NaN or negative values (indicates calculation error)
