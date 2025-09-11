package com.airoubo.chessrounds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 回合计数记录系统主启动类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableTransactionManagement
@EnableCaching
public class ChessRoundsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessRoundsApplication.class, args);
        System.out.println("\n" +
                "  _____ _                   ____                        _     \n" +
                " / ____| |                 |  _ \\                      | |    \n" +
                "| |    | |__   ___  ___ ___| |_) |___  _   _ _ __   __| |___ \n" +
                "| |    | '_ \\ / _ \\/ __/ __|  _ </ _ \\| | | | '_ \\ / _` / __|\n" +
                "| |____| | | |  __/\\__ \\__ \\ |_) | (_) | |_| | | | | (_| \\__ \\\n" +
                " \\_____|_| |_|\\___||___/___/____/ \\___/ \\__,_|_| |_|\\__,_|___/\n" +
                "\n" +
                ":: Chess Rounds Backend :: (v1.0.0)\n" +
                ":: 回合计数记录系统后端服务已启动 ::")
        ;
    }
}