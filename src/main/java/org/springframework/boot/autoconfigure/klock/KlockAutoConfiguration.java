package org.springframework.boot.autoconfigure.klock;

import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.klock.config.KlockConfig;
import org.springframework.boot.autoconfigure.klock.core.BusinessKeyProvider;
import org.springframework.boot.autoconfigure.klock.core.KlockAspectHandler;
import org.springframework.boot.autoconfigure.klock.core.LockInfoProvider;
import org.springframework.boot.autoconfigure.klock.lock.LockFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

/**
 * Created by kl on 2017/12/29.
 * Content :klock自动装配
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(KlockConfig.class)
@Import({KlockAspectHandler.class})
public class KlockAutoConfiguration {

    @Autowired
    private KlockConfig klockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        if(klockConfig.getClusterServer()!=null){
            config.useClusterServers().setPassword(klockConfig.getPassword())
                    .addNodeAddress(klockConfig.getClusterServer().getNodeAddresses());
        }else {
            config.useSingleServer().setAddress(klockConfig.getAddress())
                    .setDatabase(klockConfig.getDatabase())
                    .setPassword(klockConfig.getPassword());
        }
        Codec codec=(Codec) ClassUtils.forName(klockConfig.getCodec(),ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockInfoProvider lockInfoProvider(){
        return new LockInfoProvider();
    }

    @Bean
    public BusinessKeyProvider businessKeyProvider(){
        return new BusinessKeyProvider();
    }

    @Bean
    public LockFactory lockFactory(){
        return new LockFactory();
    }
}
