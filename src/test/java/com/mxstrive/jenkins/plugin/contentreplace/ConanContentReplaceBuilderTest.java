package com.mxstrive.jenkins.plugin.contentreplace;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConanContentReplaceBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private String fileEncoding = "UTF-8";
    private String lineSeparator = "Unix";
	private File file;
    private File assertFile;
	private List<FileContentReplaceConfig> configs;

    @Before
    public void init() throws IOException, URISyntaxException {
    	File inputFile = new File(getClass().getResource("conanfile.linux.txt").toURI());
        file = new File(getClass().getResource(".").getPath() + "tmp.txt");
        FileUtils.copyFile(inputFile, file);
        assertFile = new File(getClass().getResource("conanfile.linux.txt.assert").toURI());

    	configs = new ArrayList<>();
    	List<FileContentReplaceItemConfig> cfgs = new ArrayList<>();
		FileContentReplaceItemConfig cfg = new FileContentReplaceItemConfig();
		cfg.setSearch("cpython/\\d+\\.\\d+\\.\\d+");
		cfg.setReplace("cpython/3.9.7");
		cfg.setVerbose(true);
		cfgs.add(cfg);

    	FileContentReplaceConfig config = new FileContentReplaceConfig(file.getAbsolutePath(), fileEncoding, cfgs);
    	config.setLineSeparator(lineSeparator);
    	configs.add(config);
    }

    @After
    public void clean() throws IOException {
        FileUtils.forceDelete(file);
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ContentReplaceBuilder builder = new ContentReplaceBuilder(configs);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("   > replace times: 1, ", build);
        Assert.assertEquals(FileUtils.readFileToString(assertFile, Charset.forName(fileEncoding)), FileUtils.readFileToString(file, Charset.forName(fileEncoding)) );
    }

    @Test
    public void testBuildQuiet() throws Exception {
        configs.get(0).getConfigs().get(0).setVerbose(false);
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ContentReplaceBuilder builder = new ContentReplaceBuilder(configs);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogNotContains("   > replace : ", build);
		jenkins.assertLogNotContains("   > replace times: 1, ", build);
        Assert.assertEquals(FileUtils.readFileToString(assertFile, Charset.forName(fileEncoding)), FileUtils.readFileToString(file, Charset.forName(fileEncoding)) );
        Assert.assertEquals(FileUtils.readFileToString(assertFile, Charset.forName(fileEncoding)), FileUtils.readFileToString(file, Charset.forName(fileEncoding)) );
    }

}