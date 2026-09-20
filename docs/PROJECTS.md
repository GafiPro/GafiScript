# Projects

Projetos ficam em:

    <server>/gafiscript/projects/<name>/

Estrutura:

    manifest.json
    src/
      Main.java

O manifest suporta nome, versão, main class, autor, descrição e dependências.

Projetos podem ser criados, editados dentro do Minecraft, executados, recarregados, exportados para .gafiscript e importados com validação contra path traversal.

A compilação do projeto inclui todos os .java de src/.
