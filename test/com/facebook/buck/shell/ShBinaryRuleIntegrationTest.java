/*
 * Copyright 2013-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.shell;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.facebook.buck.testutil.integration.DebuggableTemporaryFolder;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.ProjectWorkspace.ProcessResult;
import com.facebook.buck.testutil.integration.TestDataHelper;
import com.facebook.buck.util.environment.DefaultExecutionEnvironment;
import com.facebook.buck.util.environment.ExecutionEnvironment;
import com.facebook.buck.util.environment.Platform;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShBinaryRuleIntegrationTest {

  @Rule
  public DebuggableTemporaryFolder temporaryFolder = new DebuggableTemporaryFolder();

  @Test
  public void testTrivialShBinaryRule() throws IOException {
    // sh_binary is not available on Windows. Ignore this test on Windows.
    Assume.assumeTrue(Platform.detect() != Platform.WINDOWS);
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "sh_binary_trivial", temporaryFolder);
    workspace.setUp();

    ProcessResult buildResult = workspace.runBuckCommand("build", "//:run_example", "-v", "2");
    buildResult.assertExitCode(0);

    // Verify contents of example_out.txt
    File outputFile = workspace.getFile("buck-out/gen/example_out.txt");
    String output = Files.toString(outputFile, Charsets.US_ASCII);
    assertEquals("arg1\narg2\n", output);
  }

  @Test
  public void testShBinaryWithResources() throws IOException {
    // sh_binary is not available on Windows. Ignore this test on Windows.
    Assume.assumeTrue(Platform.detect() != Platform.WINDOWS);
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "sh_binary_with_resources", temporaryFolder);
    workspace.setUp();

    ProcessResult buildResult = workspace.runBuckCommand("build", "//app:create_output_using_node");
    buildResult.assertExitCode(0);

    // Verify contents of output.txt
    File outputFile = workspace.getFile("buck-out/gen/app/output.txt");
    List<String> lines = Files.readLines(outputFile, Charsets.US_ASCII);
    ExecutionEnvironment executionEnvironment = new DefaultExecutionEnvironment();
    String expectedPlatform = executionEnvironment.getPlatform().getPrintableName();
    assertEquals(expectedPlatform, lines.get(0));
    assertEquals("arg1 arg2", lines.get(1));
  }

  @Test
  public void testShBinaryCannotOverwriteResource() throws IOException {
    // sh_binary is not available on Windows. Ignore this test on Windows.
    Assume.assumeTrue(Platform.detect() != Platform.WINDOWS);
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "sh_binary_with_overwrite_violation", temporaryFolder);
    workspace.setUp();

    ProcessResult buildResult = workspace.runBuckCommand("build", "//:overwrite");
    buildResult.assertExitCode(1);

    assertThat(buildResult.getStderr(), containsString("/overwrite.sh: Permission denied"));
  }
}
