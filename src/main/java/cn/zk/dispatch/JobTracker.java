package cn.zk.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 *
 * @description 启动节点监控，并派发任务给在线空闲服务器
 * User: hpb
 * Date: 2016/2/16
 */
public class JobTracker {
    final Logger logger = LoggerFactory.getLogger(getClass());
    CenterNode centerNode;
    private ArrayList<VideoServerStat> serverList = new ArrayList<VideoServerStat>();

    /**
     * 检索引擎启动服务监控
     */
    public void start() {
        centerNode = new CenterNode();
        centerNode.init();
        centerNode.addWatcher("/machine", new NodeWatcherListener() {
            @Override
            public void childAdd(String path, String data) {
                String ip = path.split("/")[2];
                logger.info("new machine online: ip={},data={} ", ip, data);
                VideoServerStat stat = new VideoServerStat(ip, Integer.parseInt(data));
                serverList.add(stat);
            }

            @Override
            public void childUpdate(String path, String data) {
                String ip = path.split("/")[2];
                logger.info("one machine update data: ip={},data={} ", ip, data);
                VideoServerStat stat = new VideoServerStat(ip);
                stat = serverList.get(serverList.indexOf(stat));
                stat.setChannelId(Integer.parseInt(data));
            }

            @Override
            public void childRemoved(String path) {
                String ip = path.split("/")[2];
                logger.info("one machine offline: ip={} ", ip);
                VideoServerStat stat = new VideoServerStat(ip);
                serverList.remove(stat);
                centerNode.deleteNode("/task"+ "/" + ip);
            }
        });
    }

    /**
     * 派发处理任务
     * @param content
     * @return true:派发成功
     */
    public boolean sendTask(int channelId, String content) {
        logger.info("sending task... ");
        
        //先寻找有没有已经处理该频道的 client
        for (VideoServerStat server : serverList) {
            if (server.getChannelId() == channelId) {
                logger.info("select server= {} ", server.getIp());
                centerNode.addTaskChildNode(server.getIp(), content);
                return true;
            }
        }
        
        //如果没有，则寻找一个空闲的client
        for (VideoServerStat server : serverList) {
        	 if (server.getChannelId() == 0) {
                 logger.info("select server= {} ", server.getIp());
                 centerNode.addTaskChildNode(server.getIp(), content);
                 return true;
             }
        }
        return false;
    }

    public void stop() {
        centerNode.close();
    }


    public static void main(String[] args) {
        JobTracker jobTracker = new JobTracker();
        jobTracker.start();
        try {
            Thread.sleep(10000);
            jobTracker.sendTask(1, "from send center");
            Thread.sleep(10000);
            jobTracker.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
