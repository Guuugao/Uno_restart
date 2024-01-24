package com.uno_restart.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.uno_restart.types.player.PlayerContact;
import com.uno_restart.types.player.PlayerHistory;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PlayerInfoFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "avatarPath", () -> "./uploads/defaultAvatar.png", String.class);
        this.strictInsertFill(metaObject, "lastLogin", () -> LocalDate.now().toString(), String.class);
        this.strictInsertFill(metaObject, "isOnline", () -> false, Boolean.class);
        this.strictInsertFill(metaObject, "contact", () -> new PlayerContact("", ""), PlayerContact.class);
        this.strictInsertFill(metaObject, "history", () -> new PlayerHistory(0, 0), PlayerHistory.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "lastLogin", LocalDate::now, LocalDate.class);
    }
}
