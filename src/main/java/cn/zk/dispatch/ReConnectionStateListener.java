package cn.zk.dispatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to monitor connection state & re-register to Zookeeper when connection lost.
 *
 */
 class ReConnectionStateListener implements org.apache.curator.framework.state.ConnectionStateListener {
    private Logger logger= LoggerFactory.getLogger(getClass());
    private String zkRegPathPrefix;
    private String regContent;

    public ReConnectionStateListener(String zkRegPathPrefix, String regContent) {
        this.zkRegPathPrefix = zkRegPathPrefix;
        this.regContent = regContent;
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
        logger.info("zookeeper connect state={}",connectionState);
        if (connectionState == ConnectionState.LOST) {
            while (true) {
                try {
                    if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                                .forPath(zkRegPathPrefix, regContent.getBytes("UTF-8"));
                        break;
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //TODO: log something
                    break;
                } catch (Exception e) {
                    //TODO: log something
                }
            }
        }
    }
}