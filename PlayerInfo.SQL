create table uno.playerinfo
(
    playerName varchar(32)  not null comment '玩家名称'
        primary key,
    password   varchar(64)  not null comment '密码',
    avatarPath varchar(255) not null comment '头像路径',
    lastLogin  char(12)     not null comment '上次登录时间',
    isOnline   tinyint(1)   not null comment '在线状态',
    contact    varchar(128) null comment '联系方式(email & phone)',
    history    varchar(64)  null comment '游戏历史记录'
);
