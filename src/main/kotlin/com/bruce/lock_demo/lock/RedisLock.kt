package com.bruce.lock_demo.lock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
@Component
class RedisLock @Autowired constructor(
        private val redisTemplate: StringRedisTemplate
) : Lock{

    // redis key
    private val LOCK_KEY = "LOCK_NAME"

    // redis value
    private val LOCK_NAME = "redis_lock_stock"

    /**
     * 加锁
     */
    override fun lock() {
        while (true){
//            val b = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, LOCK_NAME)
            val b = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, LOCK_NAME,15,TimeUnit.SECONDS)
            if(b!!) {
                return
            }else println("循环等待中...")
        }
    }

    override fun tryLock(): Boolean {
        TODO("Not yet implemented")
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * 解锁
     */
    override fun unlock() {
        redisTemplate.delete(LOCK_KEY)
    }

    override fun lockInterruptibly() {
        TODO("Not yet implemented")
    }

    override fun newCondition(): Condition {
        TODO("Not yet implemented")
    }

}