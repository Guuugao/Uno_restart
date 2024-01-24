create table playerinfo
(
    playerName varchar(32)  not null comment '玩家名称'
        primary key,
    password   char(64)     not null comment '使用SHA-256加密',
    avatarPath varchar(255) not null comment '头像路径',
    lastLogin  char(12)     not null comment '上次登录时间',
    isOnline   tinyint(1)   not null comment '在线状态',
    contact    varchar(64)  null comment '联系方式(email & phone)',
    history    varchar(64)  null comment '游戏历史记录',
    salt       char(64)     null comment '盐, 用于哈希'
);