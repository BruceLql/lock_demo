package com.bruce.lock_demo.lock

import org.apache.zookeeper.*
import org.springframework.beans.factory.annotation.Autowired
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock

/*
 *@ClassName ZkLock
 *@Description TODO
 *@Author Bruce
 *@Date 2020/6/5 14:49
 *@Version 1.0
 */
class ZkLock: Lock {
    // zk客户端
    @Autowired
    private var zk: ZooKeeper
    // zk 的一个目录结构
    private var root: String = "/locks"

    // 锁的名称
    private var lockName: String? = null

    // 用当前线程创建的序列node
    private var nodeId: ThreadLocal<String> = ThreadLocal<String>()

    // 用来同步等待zkclient链接到了服务端
    private var connectSignal: CountDownLatch = CountDownLatch(1)
    private val sessionTimeout = 3000
    private val data = byteArrayOf(0)

    // 1.首先在ZkLock的构造方法中，链接zk，创建lock根节点

    constructor(config: String, lockName: String) {
        this.lockName = lockName

        try {
            println("${System.nanoTime()} --- zk 建立连接")
            zk = ZooKeeper(config, sessionTimeout, Watcher { event ->
                // 建立连接
                if (event.state == Watcher.Event.KeeperState.SyncConnected)
                    connectSignal.countDown()
            })
            println("获取到链接：$zk")
            connectSignal.await()
            val stat = zk.exists(root, false)
            println("${System.nanoTime()} ===================stat : $stat")
            if (null == stat) {
                println("[根节点不存在，首创建根节点：$root  ${System.nanoTime()}]")
                // 创建根节点
                zk.create(root, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)

        }


    }

    class LockWatcher : Watcher {
        private var latch: CountDownLatch? = null

        constructor(latch: CountDownLatch) {
            this.latch = latch
        }

        override fun process(event: WatchedEvent) {

            if (event.type == Watcher.Event.EventType.NodeDeleted)
                latch?.countDown()
        }

    }


    override fun lock() {
        println("[调用上锁方法 lock]")
        try {
            // 创建临时子节点
            val myNode = zk.create("$root/$lockName", data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL)
            println("${Thread.currentThread().name}+$myNode  ------ created")

            val sortedNodes: TreeSet<String> = TreeSet<String>()
            // 取出所有子节点
            zk.getChildren(root, false).forEach { node ->
                sortedNodes.add("$root/$node")
            }

            val smallNode = sortedNodes.first()

            // 如果是最小的节点，则表示取得锁
            if (myNode.equals(smallNode)) {
                println("${Thread.currentThread().name} -- $myNode --- get lock")
                this.nodeId.set(myNode)
                return
            }
            val preNode = sortedNodes.lower(myNode)
            val latch = CountDownLatch(1)
            val stat = zk.exists(preNode, LockWatcher(latch)) // 同时注册监听
            // 判断比自己小一个数字的节点是否存在，如果不存在则无需等待锁，同时注册监听
            if (stat != null) {
                println("${Thread.currentThread().name}-- $myNode -- waiting for ${root}/$preNode released lock")
                latch.await() // 等待，这里应该一直等待其他线程释放锁
                nodeId.set(myNode)
                latch == null
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun tryLock(): Boolean {
        TODO("Not yet implemented")
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        TODO("Not yet implemented")
    }

    override fun unlock() {
        try {
            println("${Thread.currentThread().name}-- unlock")
            // 删除节点
            if (null != nodeId) zk.delete(nodeId.get(), -1)
            nodeId.remove()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun lockInterruptibly() {
        TODO("Not yet implemented")
    }

    override fun newCondition(): Condition {
        TODO("Not yet implemented")
    }

}