package cn.zk.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Created with IntelliJ IDEA.
 *
 * @description 机器上线添加节点并监控任务子节点接收处理任务
 * User: hpb
 * Date: 2016/2/16
 */
public class TaskTracker {
    final Logger logger = LoggerFactory.getLogger(getClass());
    CenterNode centerNode;
    
    private int mChannelId = 0;

    /**
     * 处理服务器启动初始化
     */
    public void start() {
        centerNode = new CenterNode();

        centerNode.init();
        centerNode.addMachineNode(getIP());
        centerNode.addTaskNode(getIP(), "", new NodeWatcherListener() {
            @Override
            public void childAdd(String path, String json) {
                logger.info("new task,data={} ", json);
                centerNode.deleteNode(path);
                //FIXME 任务处理

                //parse json and get channelId, update channelId
            }


            @Override
            public void childUpdate(String path, String data) {

            }

            @Override
            public void childRemoved(String path) {


            }
        });
    }


    private String getIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public int  getChannelId() {
    	return mChannelId;
    }

    public void stop() {
        centerNode.close();
    }


}
