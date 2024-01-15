package com.uno_restart.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uno_restart.mapper.PlayerInfoMapper;
import com.uno_restart.types.player.PlayerInfo;
import org.springframework.stereotype.Service;


@Service
public class PlayerInfoService
        extends ServiceImpl<PlayerInfoMapper, PlayerInfo>
        implements IPlayerInfoService {
}
