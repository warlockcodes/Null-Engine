// This is a generated file. Not intended for manual editing.
package nullshader.parser.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nullshader.parser.psi.NullShaderTypes.*;
import nullshader.parser.psi.*;

public class NullShaderPostfix2ExprImpl extends NullShaderExprImpl implements NullShaderPostfix2Expr {

  public NullShaderPostfix2ExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NullShaderVisitor visitor) {
    visitor.visitPostfix2Expr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NullShaderVisitor) accept((NullShaderVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public NullShaderFunctionCall getFunctionCall() {
    return findNotNullChildByClass(NullShaderFunctionCall.class);
  }

  @Override
  @NotNull
  public NullShaderTypeSpecifier getTypeSpecifier() {
    return findNotNullChildByClass(NullShaderTypeSpecifier.class);
  }

}
