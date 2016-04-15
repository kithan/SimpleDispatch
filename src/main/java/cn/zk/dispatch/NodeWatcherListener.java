package cn.zk.dispatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 *
 * @description 用途说明
 * User: hpb
 * Date: 2016/2/16
 */
  abstract   class NodeWatcherListener implements PathChildrenCacheListener {
    final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception {
        switch (event.getType()) {
            case CHILD_ADDED:
                childAdd(event.getData().getPath(), new String(event.getData().getData()));
                break;
            case CHILD_UPDATED:
                childUpdate(event.getData().getPath(), new String(event.getData().getData()));
                break;
            case CHILD_REMOVED:
                childRemoved(event.getData().getPath());
                break;

            default:
                break;
        }
    }
    public abstract void childAdd(String path,String data);
    public abstract void childUpdate(String path,String data);
    public abstract void childRemoved(String path);
}
