// This is a generated file. Not intended for manual editing.
package nullshader.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NullShaderAssignmentExpr extends NullShaderExpr {

  @NotNull
  NullShaderAssignmentOp getAssignmentOp();

  @NotNull
  List<NullShaderExpr> getExprList();

}