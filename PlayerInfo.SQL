create table if not exists uno.playerinfo(
    playerName varchar(32)  not null comment '玩家名称'
        primary key,
    password   varchar(64)  not null comment '密码',
    avatarPath varchar(255) not null comment '头像路径',
    lastLogin  char(12)     not null comment '上次登录时间',
    isOnline   tinyint(1)   not null comment '在线状态',
    contact    varchar(64)  null comment '联系方式(email & phone)',
    history    varchar(64)  null comment '游戏历史记录'
);



insert into uno.playerinfo(playerName, password, avatarPath, lastLogin, isOnline, contact, history) VALUES
    ('admin', 'admin0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Thh_1', 'Thh0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Thh_2', 'Thh0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Thh_3', 'Thh0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Thh_4', 'Thh0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Zll_1', 'Zll0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Zll_2', 'Zll0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Zll_3', 'Zll0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}'),
    ('Zll_4', 'Zll0000', './uploads/defaultAvatar.png', '2024-01-18', 0, '{"email":null,"phone":null}', '{"winTimes":0,"failTimes":0,"totalGames":0}');
