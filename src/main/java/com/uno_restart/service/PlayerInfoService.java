package com.uno_restart.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uno_restart.mapper.PlayerInfoMapper;
import com.uno_restart.types.player.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;


@Service
public class PlayerInfoService
        extends ServiceImpl<PlayerInfoMapper, PlayerInfo>
        implements IPlayerInfoService {
    @Autowired
    Base64.Decoder decoder;

    @Override
    public List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after) {
        String cursor = after == null ? null : new String(decoder.decode(after));
        return baseMapper.selectPlayerInfoPage(playerName, first, cursor);
    }
}
