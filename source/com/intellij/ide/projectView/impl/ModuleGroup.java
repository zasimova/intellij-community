/**
 * @author cdr
 */
package com.intellij.ide.projectView.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import gnu.trove.THashSet;

import java.util.*;

public class ModuleGroup {
  private static final Logger LOG = Logger.getInstance("#com.intellij.ide.projectView.impl.ModuleGroup");
  private final String[] myGroupPath;

  public ModuleGroup(String[] groupPath) {
    LOG.assertTrue(groupPath != null);
    myGroupPath = groupPath;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ModuleGroup)) return false;

    final ModuleGroup moduleGroup = (ModuleGroup)o;

    if (!Arrays.equals(myGroupPath, moduleGroup.myGroupPath)) return false;

    return true;
  }

  public int hashCode() {
    return myGroupPath[myGroupPath.length-1].hashCode();
  }

  public String[] getGroupPath() {
    return myGroupPath;
  }

  public Module[] modulesInGroup(Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    List<Module> result = new ArrayList<Module>();
    for (int i = 0; i < modules.length; i++) {
      final Module module = modules[i];
      String[] group = ModuleManager.getInstance(project).getModuleGroupPath(module);
      if (Arrays.equals(myGroupPath, group)) {
        result.add(module);
      }
    }
    return result.toArray(new Module[result.size()]);
  }

  public Collection<ModuleGroup> childGroups(Project project) {
    final Module[] allModules = ModuleManager.getInstance(project).getModules();

    Set<ModuleGroup> result = new THashSet<ModuleGroup>();
    for (int i = 0; i < allModules.length; i++) {
      Module module = allModules[i];
      String[] group = ModuleManager.getInstance(project).getModuleGroupPath(module);
      if (group == null) continue;
      final String[] directChild = directChild(myGroupPath, group);
      if (directChild != null) {
        result.add(new ModuleGroup(directChild));
      }
    }

    return result;
  }

  private static String[] directChild(final String[] parent, final String[] descendant) {
    if (parent.length >= descendant.length) return null;
    final String[] path = new String[parent.length + 1];
    for (int i = 0; i < parent.length; i++) {
      String group = parent[i];
      if (!group.equals(descendant[i])) return null;
      path[i] = parent[i];
    }
    path[parent.length] = descendant[parent.length];
    return path;
  }

  public String presentableText() {
    return "'" + myGroupPath[myGroupPath.length - 1] + "'";
  }

}