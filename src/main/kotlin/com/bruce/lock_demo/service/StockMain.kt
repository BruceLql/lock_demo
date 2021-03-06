package com.bruce.lock_demo.service

import com.bruce.lock_demo.lock.RedisLock
import com.bruce.lock_demo.lock.ZkLock
import com.bruce.lock_demo.vo.Stock
import org.apache.commons.lang.time.DateFormatUtils
import org.apache.commons.lang.time.DateUtils
import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.config.SingleServerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

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

//            val config:Config = Config()
//            val singleServerConfig : SingleServerConfig = config.useSingleServer().setAddress("redis://39.108.162.204:6379").setPassword("123456").setDatabase(0)
//            val redisson = Redisson.create(config)
//            val mylock = redisson.getLock("LOCK_NAME")

            val zkLock = ZkLock("39.108.162.204:2181","stock_zk")


        }

        override fun run() {
            println("${System.nanoTime()}准备上锁--------------"+zkLock)

            // 上锁
//            redisLock.lock()
//            mylock.lock()
            zkLock?.lock()
            // 减少库存的方法
            val b: Boolean = Stock().reduceStock()
            // 解锁
            zkLock?.unlock()
//            mylock.unlock()
//            redisLock.unlock()
            println("${System.nanoTime()} 解锁")


            if (b) {
                println(Thread.currentThread().name + "减少库存成功")
            } else {
                println(Thread.currentThread().name + "减少库存失败")
            }
        }
    }
}