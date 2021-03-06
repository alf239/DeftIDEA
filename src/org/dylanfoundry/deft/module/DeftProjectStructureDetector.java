/*
 * Copyright 2013, Konstantin Bulenkov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dylanfoundry.deft.module;

import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class DeftProjectStructureDetector extends ProjectStructureDetector {

  @NotNull
  @Override
  public DirectoryProcessingResult detectRoots(@NotNull File dir,
                                               @NotNull File[] children,
                                               @NotNull File base,
                                               @NotNull List<DetectedProjectRoot> result) {
    for (File child : children) {
      if (FileUtilRt.extensionEquals(child.getName(), "dylan") || FileUtilRt.extensionEquals(child.getName(), "lid")) {
        result.add(new DeftDetectedSourceRoot(dir));
        return DirectoryProcessingResult.SKIP_CHILDREN;
      }
    }
    return DirectoryProcessingResult.PROCESS_CHILDREN;
  }

  @Override
  public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                    @NotNull ProjectDescriptor projectDescriptor,
                                    @NotNull final ProjectFromSourcesBuilder builder) {
    if (!roots.isEmpty() && !builder.hasRootsFromOtherDetectors(this)) {
      List<ModuleDescriptor> modules = projectDescriptor.getModules();
      if (modules.isEmpty()) {
        modules = new ArrayList<ModuleDescriptor>();
        for (DetectedProjectRoot root : roots) {
          modules.add(new ModuleDescriptor(new File(builder.getBaseProjectPath()), JavaModuleType.getModuleType(), root){

            @Override
            public void updateModuleConfiguration(Module module, ModifiableRootModel rootModel) {
              super.updateModuleConfiguration(module, rootModel);
              for (ModuleBuilder moduleBuilder : builder.getContext().getAllBuilders()) {
                if (moduleBuilder instanceof DeftModuleBuilder) {
                  ((DeftModuleBuilder) moduleBuilder).moduleCreated(module);
                  return;
                }
              }
            }
          });
        }
        projectDescriptor.setModules(modules);
      }
    }
  }

  @Override
  public List<ModuleWizardStep> createWizardSteps(ProjectFromSourcesBuilder builder, ProjectDescriptor projectDescriptor, Icon stepIcon) {
    for (ModuleBuilder moduleBuilder : builder.getContext().getAllBuilders()) {
      if (moduleBuilder instanceof DeftModuleBuilder) {
        ArrayList<ModuleWizardStep> steps = new ArrayList<ModuleWizardStep>();
        steps.add(new DeftModuleWizardStep((DeftModuleBuilder) moduleBuilder));
        return steps;
      }
    }
    return Collections.emptyList();
  }

  @Override
  public String getDetectorId() {
    return "Dylan";
  }
}
