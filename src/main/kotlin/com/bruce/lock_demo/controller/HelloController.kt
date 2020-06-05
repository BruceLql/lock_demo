package com.bruce.lock_demo.controller

import com.bruce.lock_demo.lock.RedisLock
import com.bruce.lock_demo.service.StockMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/*
 *@ClassName HelloController
 *@Description TODO
 *@Author Bruce
 *@Date 2020/6/4 23:35
 *@Version 1.0
 */
@RestController
class HelloController @Autowired constructor(
        private val redisLock:RedisLock
) {

    @GetMapping("/hello")
    fun hello(): String {
        println("--------kotlin-----------------")
        Thread(StockMain.StockThread(redisLock), "线程1").start()
        Thread(StockMain.StockThread(redisLock), "线程2").start()


        return "Hello World!"
    }
}