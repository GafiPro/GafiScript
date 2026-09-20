package com.gafipro.gafiscript.api;

import net.minecraft.command.DefaultPermissions;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Method;

public final class GafiPermissions {
    private GafiPermissions() {}

    public static boolean has(
            ServerPlayerEntity player,
            String permission
    ) {
        if (player == null || permission == null ||
                permission.isBlank()) {
            return false;
        }

        Boolean luckPerms =
                luckPermsHas(
                        player,
                        permission
                );

        if (luckPerms != null) {
            return luckPerms;
        }

        return player.getCommandSource()
                .getPermissions()
                .hasPermission(
                        DefaultPermissions.GAMEMASTERS
                );
    }

    public static boolean has(
            GafiPlayer player,
            String permission
    ) {
        return player != null &&
                has(
                        player.raw(),
                        permission
                );
    }

    private static Boolean luckPermsHas(
            ServerPlayerEntity player,
            String permission
    ) {
        try {
            Class<?> apiClass =
                    Class.forName(
                            "net.luckperms.api.LuckPermsProvider"
                    );

            Method get =
                    apiClass.getMethod("get");

            Object api = get.invoke(null);

            Method getUserManager =
                    api.getClass().getMethod(
                            "getUserManager"
                    );

            Object manager =
                    getUserManager.invoke(api);

            Method getUser =
                    manager.getClass().getMethod(
                            "getUser",
                            java.util.UUID.class
                    );

            Object user =
                    getUser.invoke(
                            manager,
                            player.getUuid()
                    );

            if (user == null) {
                return false;
            }

            Method getCachedData =
                    user.getClass().getMethod(
                            "getCachedData"
                    );

            Object cached =
                    getCachedData.invoke(user);

            Method getPermissionData =
                    cached.getClass().getMethod(
                            "getPermissionData"
                    );

            Object permissionData =
                    getPermissionData.invoke(cached);

            Method checkPermission =
                    permissionData.getClass().getMethod(
                            "checkPermission",
                            String.class
                    );

            Object result =
                    checkPermission.invoke(
                            permissionData,
                            permission
                    );

            Method asBoolean =
                    result.getClass().getMethod(
                            "asBoolean"
                    );

            return (Boolean) asBoolean.invoke(result);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
