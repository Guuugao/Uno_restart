package com.uno_restart.dataFetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.uno_restart.service.PlayerInfoService;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.player.PlayerRegisterFeedback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

@Slf4j
@DgsComponent
public class PlayerDataFetcher {
    @Autowired
    PlayerInfoService service;

    @DgsMutation
    public PlayerRegisterFeedback playerRegister(@NotNull @InputArgument String playerName, @NotNull @InputArgument String password) {
        PlayerInfo player = new PlayerInfo(playerName, password);
        PlayerRegisterFeedback feedback = new PlayerRegisterFeedback(true);

        try {
            service.save(player);
            feedback.setMessage("注册成功");
        }catch (DuplicateKeyException e) {
            feedback.setSuccess(false);
            feedback.setMessage("注册失败, 请更换昵称或密码重试!");
            return feedback;
        }

        return feedback;
    }
}
