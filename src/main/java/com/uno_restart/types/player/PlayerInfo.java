package com.uno_restart.types.player;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@RequiredArgsConstructor
@TableName(value = "playerinfo", autoResultMap = true)
public class PlayerInfo {
    @NotNull
    @TableId(type = IdType.INPUT)
    private String playerName;
    @NotNull
    private String password;
    @TableField(fill = FieldFill.INSERT)
    private String avatarpath;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String lastLogin;
    @TableField(fill = FieldFill.INSERT)
    private Boolean isOnline;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private PlayerContact contact;
    @TableField(typeHandler = JacksonTypeHandler.class, fill = FieldFill.INSERT)
    private PlayerHistory history;
}