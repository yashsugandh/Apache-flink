import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;

public class WordCount {

  public static final String INPUT_PATH = "input.path";

  public static final String OUTPUT_PATH = "output.path";

  public static final String PARALLELISM = "parallelism";

  public static void main(String[] args) throws Exception {

    // set up the execution environment
    final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    // Using the parser provided by Flink
    ParameterTool parameterTool = ParameterTool.fromArgs(args);

    // To get only one file as output
    env.setParallelism(parameterTool.getInt(PARALLELISM));

    //Read input from the given path , Just remember internally its TextInputFormat
    //The path of the file, as a URI
    //(e.g., "file:///some/local/file" or "hdfs://host:port/file/path").
    DataSet<String> text = env.readTextFile(parameterTool.get(INPUT_PATH));

    DataSet<Tuple2<String, Integer>> wordCounts = text.flatMap(new LineSplitter()).groupBy(0)
        .sum(1);

    //Save to given output path
    wordCounts.writeAsCsv(parameterTool.get(OUTPUT_PATH));

    // Execute the Flink Job with the given Name
    env.execute("Word Count Example");
  }
}
