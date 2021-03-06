/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.ex.DisableInspectionToolAction;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.profile.codeInspection.ui.SingleInspectionProfilePanel;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBLabelDecorator;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Dmitry Batkovich
 */
public class InspectionNodeInfo extends JPanel {
  private final JButton myButton;
  private final JBLabel myEnabledLabel;
  private final HighlightDisplayKey myKey;
  private final InspectionProfileImpl myCurrentProfile;
  private final Project myProject;
  @NotNull private final InspectionTree myTree;

  public InspectionNodeInfo(@NotNull final InspectionTree tree,
                            @NotNull final Project project) {
    myTree = tree;
    setLayout(new GridBagLayout());
    setBorder(IdeBorderFactory.createEmptyBorder(11, 0, 0, 0));
    final InspectionToolWrapper toolWrapper = tree.getSelectedToolWrapper();
    myProject = project;
    myCurrentProfile = (InspectionProfileImpl)InspectionProjectProfileManager.getInstance(project).getProjectProfileImpl();
    myKey = HighlightDisplayKey.find(toolWrapper.getID());
    myButton = new JButton();

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
    JBLabelDecorator label = JBLabelDecorator.createJBLabelDecorator().setBold(true);
    label.setText(toolWrapper.getDisplayName() + " inspection");
    titlePanel.add(label);
    titlePanel.add(Box.createHorizontalStrut(JBUI.scale(16)));
    myEnabledLabel = new JBLabel();
    myEnabledLabel.setForeground(JBColor.GRAY);
    titlePanel.add(myEnabledLabel);

    add(titlePanel,
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new JBInsets(0, 12, 5, 16),
                               0, 0));

    JEditorPane description = new JEditorPane();
    description.setContentType(UIUtil.HTML_MIME);
    description.setEditable(false);
    description.setOpaque(false);
    description.addHyperlinkListener(BrowserHyperlinkListener.INSTANCE);
    final String toolDescription = toolWrapper.loadDescription();
    SingleInspectionProfilePanel.readHTML(description, SingleInspectionProfilePanel.toHTML(description, toolDescription == null ? "" : toolDescription, false));
    JScrollPane pane = ScrollPaneFactory.createScrollPane(description, true);

    add(pane,
        new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL,
                               new JBInsets(0, 10, 0, 0), getFontMetrics(UIUtil.getLabelFont()).charWidth('f') * 110 - pane.getMinimumSize().width, 0));
    add(myButton,
        new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                               new JBInsets(15, 9, 9, 0), 0, 0));
    updateEnableButtonText(false);

    new ClickListener() {
      @Override
      public boolean onClick(@NotNull MouseEvent event, int clickCount) {
        updateEnableButtonText(true);
        tree.revalidate();
        tree.repaint();
        return true;
      }
    }.installOn(myButton);
  }

  private void updateEnableButtonText(boolean revert) {
    boolean isEnabled = myCurrentProfile.isToolEnabled(myKey);
    if (revert) {
      final boolean isEnabledAsFinal = isEnabled;
      DisableInspectionToolAction.modifyAndCommitProjectProfile(model -> {
        if (isEnabledAsFinal) {
          model.disableTool(myKey.getID(), myProject);
        }
        else {
          ((InspectionProfileImpl)model).enableTool(myKey.getID(), myProject);
        }
      }, myProject);
      isEnabled = !isEnabled;
    }
    myButton.setText((isEnabled ? "Disable" : "Enable") + " inspection");
    myButton.revalidate();
    myButton.repaint();
    myEnabledLabel.setText(isEnabled ? "Enabled" : "Disabled");
    myEnabledLabel.revalidate();
    myEnabledLabel.repaint();
    myTree.revalidate();
    myTree.repaint();
  }
}
