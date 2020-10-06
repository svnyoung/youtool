package com.svnyoung.youtool.idwork.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 该算法保证单节点ID生成的值越来越大，分布式节点数据不重复
 * <br><b> · </b>日期位，精确到日期 例如20181217
 *
 * <br><b> · </b>对系统时间的依赖性非常强，时间如果出现越来越小的情况，开始执行时间修正位【0-7】，最大值为9，从7开始递减
 * <br><b> · </b>28位时间位，时分秒111213999
 * <br><b> · </b>14位序列，毫秒内的计数，14位的计数顺序号支持每个节点每毫秒(同一机器，同一时间戳)产生16384个ID序号
 * <br><b> . </b>机房编号 4位最大值为15
 * <br><b> · </b>机器码 10位 最大值1023
 * <br><b> · </b>进程号 4位 最大值15
 *
 * @author sunyang
 * @since 1.0.0
 */
public class SnowFlakeSequence implements Sequence {

    private static final Logger logger = LoggerFactory.getLogger(SnowFlakeSequence.class);


    private final long reviseBits = 3;
    /**
     * 时间位，时间最大235959999,最大值268435456
     * **/
    private final long timeBits = 28;

    /**
     * 节点数，最大为32个
     **/
    private final long nodeIdBites = 5;

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 14L;

    /**
     * 机器码占得长度
     **/
    private final long machineBits = 10L;

    /**
     * 最大修正位数
     **/
    private final long pidSeqBits = 3L;


    private final static String STRING_FORMAT_02D = "%02d";

    private final static String STRING_FORMAT_04D = "%04d";

    private final static String DATE_PATTEN = "yyyyMMdd";

    /**
     * 一天的毫秒数
     **/
    private static final long DAY_MSEC = 86400000L;


    /**
     * 应用部署所占的位数最大为31 (从0开始)
     */
    private final long maxNodeId = -1L ^ -1L << this.nodeIdBites;


    /**
     * 应用部署所占的位数最大为31 (从0开始)
     */
    private final long maxTime = -1L ^ -1L << this.timeBits;

    /**
     * 应用部署所占的位数最大为31 (从0开始)
     */
    private final long maxMachineId = -1L ^ -1L << this.machineBits;

    /**
     * 生成序列的掩码，这里为16384
     */
    private final long maxSequence = -1L ^ -1L << this.sequenceBits;


    /**
     * 进程号序列，这里为16384
     */
    private final long maxPidSeq = -1L ^ -1L << this.pidSeqBits;


    /**
     * 最大修正位长度
     **/
    private final long maxRevise = -1L ^ -1L << this.reviseBits;

    /**
     * time偏移量
     **/
    private final long reviseOffset = timeBits + sequenceBits + nodeIdBites + machineBits + pidSeqBits;


    /**
     * time偏移量
     **/
    private final long timeOffset = sequenceBits + nodeIdBites + machineBits + pidSeqBits;


    /**
     * sequence偏移量
     **/
    private final long sequenceOffset = nodeIdBites + machineBits + pidSeqBits;


    /**
     * 节点偏移量
     **/
    private final long nodeOffset = machineBits + pidSeqBits;


    /**
     * 机器码偏移量
     **/
    private final long machineOffset = pidSeqBits;


    /**
     * 机器ID
     */
    private final long nodeId;

    /**
     * 机器ID
     */
    private final long machineId;


    /***
     * 进程序列
     * **/
    private final long pidSeq;


    /**
     * 修正位，防止出现时间倒退的情况 （0~7）
     */
    private long revise = maxRevise;

    /**
     * 并发控制，毫秒内序列(0~16384)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;


    private long lastDate = -1L;


    private final String formatIncByMill;
    /**
     * 由于线程安全，所以可以直接存放日历对象
     **/
    private final Calendar calendar = Calendar.getInstance();


    public SnowFlakeSequence(int nodeId, int machineId, int pidSeq) {

        if (nodeId > this.maxNodeId || nodeId < 0) {
            String message = String.format("nodeId 不能大于 %d 或者小于 0", this.maxNodeId);
            throw new IllegalArgumentException(message);
        }
        if (machineId > this.maxMachineId || machineId < 0) {
            String message = String.format("machineId 不能大于 %d 或者小于 0", this.maxMachineId);
            throw new IllegalArgumentException(message);
        }
        if (pidSeq > this.maxPidSeq || pidSeq < 0) {
            String message = String.format("pidSeq 不能大于 %d 或者小于 0", this.maxMachineId);
            throw new IllegalArgumentException(message);
        }
        this.nodeId = nodeId;
        this.machineId = machineId;
        this.pidSeq = pidSeq;
        long maxInc =revise << reviseOffset |  maxTime << timeOffset | maxSequence << sequenceOffset | maxNodeId << nodeOffset | maxMachineId << machineOffset | maxPidSeq;
        this.formatIncByMill = "%0" + String.valueOf(maxInc).length() + "d";
    }

    /**
     * @param timestamp 当前时间戳
     *                  当前毫秒内自增
     **/
    private long incByMill(long timestamp) {
        // 如果上一个timestamp与新产生的相等，则sequence加1(0-4095循环);
        if (this.lastTimestamp == timestamp) {
            // 对新的timestamp，sequence从0开始
            this.sequence = this.sequence + 1 & this.maxSequence;
            // 毫秒内序列溢出
            if (this.sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            this.sequence = 0;
        }
        //将lastDate和修正值清零
        if (timestamp / DAY_MSEC != lastDate) {
            this.lastDate = timestamp / DAY_MSEC;
            this.revise = maxRevise;
        }
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < this.lastTimestamp) {
            String message = String.format("Clock moved backwards. Refusing to generate id for %d milliseconds.", (this.lastTimestamp - timestamp));
            logger.warn(message);
            //执行数据修正
            this.revise();
        }
        this.lastTimestamp = timestamp;

        return revise << reviseOffset | timestamp % DAY_MSEC << timeOffset | sequence << sequenceOffset | nodeId << nodeOffset | machineId << machineOffset | pidSeq;
    }

    /***
     * 时间修正，按照天进行修正
     * **/
    private void revise() {

        this.revise = this.revise - 1;
        //一天内修正时间不能如果超过7，超过则报错
        if (this.revise < 0) {
            String message = String.format("Over %d revisions", revise);
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    /**
     * 获得ID
     *
     * @return SnowflakeId
     */
    @Override
    public synchronized String nextValue() {
        //获取当前时间，到毫秒
        long timestamp = timeGen();
        //追加时间戳
        StringBuilder sb = new StringBuilder();
        sb.append(dateToStr(timestamp));
        sb.append(String.format(formatIncByMill, this.incByMill(timestamp)));
        //追加每一毫秒不重复的值
        return sb.toString();

    }

    @Override
    public Object[] valueBy(String id) {
        Object[] values = new Object[6];
        try {
            StringBuffer dateSite = new StringBuffer(id.substring(0, 18));
            dateSite = dateSite.deleteCharAt(8);
            Long valueSite = Long.parseLong(id.substring(18));
            values[0] = new SimpleDateFormat(DATE_PATTEN).parse(dateSite.toString()).getTime();
            //通过ascII 码减去30得到
            values[1] = (valueSite >> reviseOffset) & maxRevise;
            values[2] = (valueSite >> timeOffset) & maxTime;
            values[3] = (valueSite >> sequenceOffset) & maxSequence;
            values[4] = (valueSite >> nodeOffset) & maxNodeId;
            values[5] = (valueSite >> machineOffset) & maxMachineId;
            values[6] = valueSite & pidSeq;
        } catch (ParseException e) {
            logger.error("解析日期失败", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("解析ID出错", e);
            throw new RuntimeException(e);
        }
        return values;
    }

    private String dateToStr(long dateTime) {
        //防止频繁创建
        calendar.setTimeInMillis(dateTime);
        StringBuilder sb = new StringBuilder();
        //追加年
        sb.append(format04d(calendar.get(Calendar.YEAR)));
        //追加月
        sb.append(format02d(calendar.get(Calendar.MONTH) + 1));
        //追加日
        sb.append(format02d(calendar.get(Calendar.DATE)));
        return sb.toString();
    }

    private String format04d(int number) {
        return String.format(STRING_FORMAT_04D, number);
    }

    private String format02d(int number) {
        return String.format(STRING_FORMAT_02D, number);
    }

    /**
     * 等待下一个毫秒的到来, 保证返回的毫秒数在参数lastTimestamp之后
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    /**
     * 获得系统当前毫秒数
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }


}