import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.tasks.JavaExec;

import java.io.File;

public class AspectJPlugin implements Plugin<Project> {


    @Override
    public void apply(Project project) {
        project.getConfigurations().create("ajtools");
        project.getConfigurations().create("ajrt");
        AspectJExtension aspectj = project.getExtensions().create("aspectj", AspectJExtension.class);

        // depends aspectjrt
        DependencyHandler dependency = project.getDependencies();
        dependency.add("ajtools", "org.aspectj:aspectjtools:" + aspectj.version);
        dependency.add("ajrt", "org.aspectj:aspectjrt:" + aspectj.version);

        project.afterEvaluate(p -> {
            // compile aspectj task
            CompileAspectjTask c = p.getTasks().create("compileAspectj", CompileAspectjTask.class);
            // cannot set in task action, or you cannot see it in idea gradle panel
            c.setGroup("aspectj");
            c.setDescription("compile aspectj and java code");

            // run aspectj app task
            JavaExec r = p.getTasks().create("runAspectj", JavaExec.class);
            r.setGroup("aspectj");
            r.setDescription("run application with aspectj weaved!");
            // class path and main class must be set here, not in a self-defined task
            r.classpath(p.getConfigurations().getByName("ajtools").getAsPath());
            r.classpath(p.getConfigurations().getByName("ajrt").getAsPath());
            String workingDir = new File(p.getBuildDir(), "aspectj").getAbsolutePath();
            r.classpath(workingDir);
            ApplicationPluginConvention plugin = p.getConvention().getPlugin(ApplicationPluginConvention.class);
            r.setMain(plugin.getMainClassName());

            r.getCommandLine().forEach(Logger::log);
            r.dependsOn(c);
        });
    }
}
