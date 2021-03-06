/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package com.intellij.codeInspection.ui;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.CommonProblemDescriptor;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.reference.RefDirectory;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.ui.ComputableIcon;
import com.intellij.util.containers.FactoryMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

/**
 * @author max
 */
public class RefElementNode extends CachedInspectionTreeNode implements RefElementAware {
  private boolean myHasDescriptorsUnder = false;
  private CommonProblemDescriptor mySingleDescriptor = null;
  protected final InspectionToolPresentation myToolPresentation;
  private final Icon myIcon;
  public RefElementNode(@Nullable RefEntity userObject, @NotNull InspectionToolPresentation presentation) {
    super(userObject);
    myToolPresentation = presentation;
    init();
    final RefEntity refEntity = getElement();
    myIcon = refEntity == null ? null : refEntity.getIcon(false);
  }

  public boolean hasDescriptorsUnder() {
    return myHasDescriptorsUnder;
  }

  @Nullable
  public RefEntity getElement() {
    return (RefEntity)getUserObject();
  }

  @Override
  @Nullable
  public Icon getIcon(boolean expanded) {
    return myIcon;
  }

  @Override
  protected String calculatePresentableName() {
    final RefEntity element = getElement();
    if (element == null || !element.isValid()) {
      return InspectionsBundle.message("inspection.reference.invalid");
    } else {
      return element.getRefManager().getRefinedElement(element).getName();
    }
  }

  @Override
  protected boolean calculateIsValid() {
    final RefEntity refEntity = getElement();
    return refEntity != null && refEntity.isValid();
  }

  @Override
  public boolean isResolved(ExcludedInspectionTreeNodesManager excludedManager) {
    return myToolPresentation.isElementIgnored(getElement());
  }

  @Override
  public void ignoreElement(ExcludedInspectionTreeNodesManager excludedManager) {
    myToolPresentation.ignoreCurrentElement(getElement());
    super.ignoreElement(excludedManager);
  }

  @Override
  public void amnesty(ExcludedInspectionTreeNodesManager excludedManager) {
    myToolPresentation.amnesty(getElement());
    super.amnesty(excludedManager);
  }

  @Override
  public FileStatus getNodeStatus() {
    return  myToolPresentation.getElementStatus(getElement());
  }

  @Override
  public void add(MutableTreeNode newChild) {
    super.add(newChild);
    if (newChild instanceof ProblemDescriptionNode) {
      myHasDescriptorsUnder = true;
    }
  }

  public void setProblem(@NotNull CommonProblemDescriptor descriptor) {
    mySingleDescriptor = descriptor;
  }

  public CommonProblemDescriptor getProblem() {
    return mySingleDescriptor;
  }

  @Override
  public RefEntity getContainingFileLocalEntity() {
    final RefEntity element = getElement();
    return element instanceof RefElement && !(element instanceof RefDirectory)
           ? element
           : super.getContainingFileLocalEntity();
  }

  @Override
  public int getProblemCount() {
    return Math.max(1, super.getProblemCount());
  }

  @Override
  public void visitProblemSeverities(FactoryMap<HighlightDisplayLevel, Integer> counter) {
    if (isLeaf()) {
      counter.put(HighlightDisplayLevel.WARNING, counter.get(HighlightDisplayLevel.WARNING) + 1);
      return;
    }
    super.visitProblemSeverities(counter);
  }
}
