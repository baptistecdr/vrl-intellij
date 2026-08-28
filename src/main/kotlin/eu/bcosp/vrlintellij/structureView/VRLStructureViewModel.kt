package eu.bcosp.vrlintellij.structureView

import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class VRLStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    StructureViewModelBase(psiFile, editor, VRLStructureViewElement(psiFile))
