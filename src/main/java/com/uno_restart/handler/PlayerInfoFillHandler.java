package com.uno_restart.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PlayerInfoFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "avatarpath", () -> "./avatars/defaultAvatar.png", String.class);
        strictInsertFill(metaObject, "lastLogin", () -> LocalDate.now().toString(), String.class);
        strictInsertFill(metaObject, "isOnline", () -> false, Boolean.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "lastLogin", LocalDate::now, LocalDate.class);
    }
}
