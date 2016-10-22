// This is a generated file. Not intended for manual editing.
package nullshader.parser.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NullShaderDeclaration extends PsiElement {

  @Nullable
  NullShaderArraySpecifier getArraySpecifier();

  @Nullable
  NullShaderFunctionPrototype getFunctionPrototype();

  @Nullable
  NullShaderIdentifierList getIdentifierList();

  @Nullable
  NullShaderInitDeclaratorList getInitDeclaratorList();

  @Nullable
  NullShaderPrecisionQualifier getPrecisionQualifier();

  @Nullable
  NullShaderStructDeclarationList getStructDeclarationList();

  @Nullable
  NullShaderTypeQualifier getTypeQualifier();

  @Nullable
  NullShaderTypeSpecifier getTypeSpecifier();

}
