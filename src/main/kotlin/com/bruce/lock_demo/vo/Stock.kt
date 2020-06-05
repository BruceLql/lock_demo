package com.bruce.lock_demo.vo

/*
 *@ClassName Stock
 *@Description TODO
 *@Author Bruce
 *@Date 2020/6/4 23:56
 *@Version 1.0
 */

class Stock {
    companion object{
        // 库存数量
        private var num = 1
    }

    fun reduceStock(): Boolean {
        return if (num > 0) {
            println("库存是num = $num")
            try {
                Thread.sleep(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            num--
            true
        } else {
            false
        }
    }



}