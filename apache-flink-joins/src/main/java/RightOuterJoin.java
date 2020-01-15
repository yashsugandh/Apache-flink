import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;

public class RightOuterJoin {
  public static final String PARALLELISM = "parallelism";

  public static void main(String[] args) throws Exception {
    final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    final ParameterTool parameterTool = ParameterTool.fromArgs(args);

    // To get only one file as output
    env.setParallelism(parameterTool.getInt(PARALLELISM));

    final DataSet<Tuple2<Integer, String>> personSet = env.readTextFile(parameterTool.get("input1"))
        .map(new MapFunction<String, Tuple2<Integer, String>>() {
          public Tuple2<Integer, String> map(String value) {
            String[] words = value.split(",");
            return new Tuple2<Integer, String>(Integer.parseInt(words[0]), words[1]);
          }
        });

    DataSet<Tuple2<Integer, String>> locationSet = env.readTextFile(parameterTool.get("input2"))
        .map(new MapFunction<String, Tuple2<Integer, String>>() {
          public Tuple2<Integer, String> map(String value) {
            String[] words = value.split(",");
            return new Tuple2<Integer, String>(Integer.parseInt(words[0]), words[1]);
          }
        });

    DataSet<Tuple3<Integer, String, String>> joined = personSet.rightOuterJoin(locationSet).where(0)
        .equalTo(0).with(
            new JoinFunction<Tuple2<Integer, String>, Tuple2<Integer, String>, Tuple3<Integer, String, String>>() {
              public Tuple3<Integer, String, String> join(Tuple2<Integer, String> person,
                  Tuple2<Integer, String> location) {
                if (person == null) {
                  return new Tuple3<Integer, String, String>(location.f0, "NULL", location.f1);
                }
                return new Tuple3<Integer, String, String>(person.f0, person.f1, location.f1);
              }
            });

    joined.writeAsCsv(parameterTool.get("output"), "\n", " ");

    env.execute("Right Join Example");
  }
}
