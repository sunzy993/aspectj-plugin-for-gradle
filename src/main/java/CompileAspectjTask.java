import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompileAspectjTask extends DefaultTask {

    @TaskAction
    public void ajc() throws Exception {
        // cache the project
        Project p = getProject();
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(p.getConfigurations().getByName("ajtools").getAsPath() + ";" + p.getConfigurations().getByName("ajrt").getAsPath());
        command.add("-Xmx64M");
        command.add("org.aspectj.tools.ajc.Main");
        command.add("-d");
        command.add(new File(getProject().getBuildDir(), "aspectj").getAbsolutePath());
        Objects.requireNonNull(p.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().findByName("main"))
            .getAllJava().getFiles().stream().map(File::getAbsolutePath).forEach(command::add);

        StringBuilder builder = new StringBuilder();
        command.forEach(it -> builder.append(it + " "));

        executeCommand(builder.toString());

        // for debug
        Logger.log(builder.toString());
    }

    public void executeCommand(String cmd) throws Exception{
        Process process = null;
            process = Runtime.getRuntime ().exec(cmd);
            process.waitFor();
            Logger.log("result = " + readAndClose(process.getInputStream()));
            Logger.log("error = " + readAndClose(process.getErrorStream()));

    }

    public String readAndClose(InputStream is) throws Exception{
        String result = "";
        if(is != null) {
            BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String temp;
                StringBuffer buffer = new StringBuffer();
                while ((temp = reader.readLine()) != null) {
                    buffer.append(temp);
                }
                result = buffer.toString();
        }
        return result;
    }
}
