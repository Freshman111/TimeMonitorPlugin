public class MethodStatPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        println "plugin execute";
    }
}