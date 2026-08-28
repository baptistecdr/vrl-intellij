package eu.bcosp.vrlintellij.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.refactoring.RefactoringActionHandler

class VRLRefactoringSupportProvider : RefactoringSupportProvider() {

    override fun getIntroduceVariableHandler(): RefactoringActionHandler = VRLIntroduceVariableHandler()
}
