package cn.zk.dispatch;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务中心及处理服务节点操作<p>
 * 任务中心：启动机器节点监控<p>
 * 处理服务节点:机器上线添加节点并监控子节点接收处理任务<p>
 *
 * @description User: hpb
 * Date: 2016/1/22
 */
 class CenterNode {
    final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());


    CuratorFramework client = null;
    ExecutorService pool;

    public void init() {
        logger.info("init  node monitor");
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .namespace("center")
                .build();


        ReConnectionStateListener stateListener = new ReConnectionStateListener("/"+"center", "");
        client.getConnectionStateListenable().addListener(stateListener);
        client.start();

        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .forPath("/machine");
        } catch (Exception e) {
            logger.info("init node exsits");
        }

    }


    /**
     * 注册监控节点
     *
     * @param path
     */
    public void addWatcher(String path, PathChildrenCacheListener listener) {
        pool = Executors.newFixedThreadPool(2);
        final PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
        try {
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            childrenCache.getListenable().addListener(
                    listener,
                    pool
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 任务派发
     *
     * @param content
     */
    public void addTaskNode(String ip, String content,PathChildrenCacheListener listener) {
//        logger.info("add task node");
        try {
            String path = "/task";
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path + "/" + ip, content.getBytes());
            addWatcher(path + "/" + ip,listener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void addTaskChildNode(String ip, String data) {
        try {
            String path = "/task";
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(path + "/" + ip + "/task", data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理机器上线时注册节点
     *
     * @param ip       处理服务器ip
     */
    public void addMachineNode(String ip) {
//        logger.info("add machine node");
        try {
            String path ="/machine"+ "/" + ip;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path, "0".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteNode(String path){
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * @param data 处理状态信息
     */
    public void updateMachineNodeData(String ip, String data) {
        logger.info("updateMachineState");
        try {
            client.setData().forPath("/machine" + "/" + ip, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        pool.shutdown();
        logger.info("close");
        client.close();
    }


}
