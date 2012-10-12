package com.laureninnovations.oopool.office.pool;

import com.laureninnovations.oopool.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfficePool {
    private List<String> sharedProcessOptions;

    @Autowired
    private Configuration configuration;

    public void init() throws Exception {
        // Verify that our command exists and is executable for starting open office instances.
        File command = new File(new File(configuration.getOfficeBaseDirectory(), "program"), "soffice");
        if (!command.canExecute()) {
            throw new Exception("Unable to execute the file '" + command.getAbsolutePath() + "'.");
        }

        sharedProcessOptions = new ArrayList<String>();
        sharedProcessOptions.add(command.getAbsolutePath());
        sharedProcessOptions.add("--nologo");
        sharedProcessOptions.add("--headless");
        sharedProcessOptions.add("--nofirststartwizard");
    }

    public void destroy() {
    }

    public OfficeInstance newInstance() throws Exception {
        return newOfficeInstance(8101);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected OfficeInstance newOfficeInstance(int port) throws Exception {
        File userInstallationDir = new File(configuration.getOfficeBaseUserDirectory(), "instance-" + port);
        OfficeInstance officeInstance = new OfficeInstance();
        officeInstance.setUserInstallationDir(userInstallationDir);
        officeInstance.setProcessBuilder(newProcessBuilder(port, userInstallationDir));
        officeInstance.setPort(port);
        return officeInstance;
    }

    protected ProcessBuilder newProcessBuilder(int port, File userInstallationDir) throws Exception {
        List<String> commandLine = new ArrayList<String>(sharedProcessOptions);
        //commandLine.add("--accept=socket,host=localhost,port=" + port + ";urp;StarOffice.ServiceManager");
        commandLine.add("--accept=socket,host=0,port=" + port + ";urp;");
        commandLine.add("-env:UserInstallation=file://" + userInstallationDir.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }

}
