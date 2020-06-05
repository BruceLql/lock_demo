package com.bruce.lock_demo.service

import com.bruce.lock_demo.lock.RedisLock
import com.bruce.lock_demo.vo.Stock
import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.config.SingleServerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/*
 *@ClassName StockMain
 *@Description TODO
 *@Author Bruce
 *@Date 2020/6/4 18:25
 *@Version 1.0
 */
@Service
class StockMain @Autowired constructor(
        private val redisLoc: RedisLock
) {

    class StockThread @Autowired constructor(
            private val redisLock:RedisLock
    ) : Runnable {
        companion object{

            val config:Config = Config()
            val singleServerConfig : SingleServerConfig = config.useSingleServer().setAddress("redis://39.108.162.204:6379").setPassword("123456").setDatabase(0)
            val redisson = Redisson.create(config)
            val mylock = redisson.getLock("LOCK_NAME")

        }

        override fun run() {
            // 上锁
//            redisLock.lock()
            mylock.lock()

            // 减少库存的方法
            val b: Boolean = Stock().reduceStock()
            // 解锁
//            redisLock.unlock()
            mylock.unlock()

            if (b) {
                println(Thread.currentThread().name + "减少库存成功")
            } else {
                println(Thread.currentThread().name + "减少库存失败")
            }
        }
    }
}