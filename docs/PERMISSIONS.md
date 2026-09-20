# Permissions

A API permite testar permissões por nome:

    GafiPermissions.has(player, "gafiscript.race.start");

A implementação usa a permission API do Minecraft por defeito.

Quando LuckPerms está presente, o GafiScript tenta consultar a API pública de LuckPerms via reflection. LuckPerms não é uma dependência obrigatória.
