package cn.zk.dispatch;

/**
 * Created with IntelliJ IDEA.
 *
 * @description 用途说明
 * User: hpb
 * Date: 2016/2/16
 */
 class VideoServerStat {
    private String ip;
    private int  channelId;
    public VideoServerStat(String ip) {
        this.ip = ip;
    }
    public VideoServerStat(String ip, int channelId) {
        this.ip = ip;
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  VideoServerStat){
            VideoServerStat v=(VideoServerStat)obj;
            if(v.ip.equals(this.ip)){
                return true;
            }
        }
        return false;
    }
}
