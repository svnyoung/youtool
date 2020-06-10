package com.svnyoung.youtool.idwork.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <br><b> · </b>日期位，精确到毫秒 例如20181717111213999
 *
 *
 * <br><b> · </b>14位序列，毫秒内的计数，14位的计数顺序号支持每个节点每毫秒(同一机器，同一时间戳)产生16384个ID序号
 * <br><b> · </b>对系统时间的依赖性非常强，时间修正位3位，最大值为7
 * <br><b> . </b>机房编号，5位最大值为31
 * <br><b> · </b>机器码 10位 最大值1024


 * @author sunyang
 * @since 1.0.0
 */
public class SnowFlakeSequence implements Sequence {

	private static final Logger logger = LoggerFactory.getLogger(SnowFlakeSequence.class);

	/**
	 * 节点数，最大为32个
	 * **/
	private final long nodeIdBites = 5;

	/**
	 * 序列在id中占的位数
	 */
	private final long sequenceBits = 14L;

	/**
	 * 机器码占得长度
	 * **/
	private final long machineBits = 10L;

	/**最大修正位数**/
	private final long reviseBits = 3L;


	private final static String STRING_FORMAT_02D = "%02d";

	private final static String STRING_FORMAT_03D = "%03d";

	private final static String STRING_FORMAT_04D = "%04d";

	private final static String DATE_PATTEN = "yyyyMMddHHmmssSSS";

	/**
	 * 一天的毫秒数
	 * **/
	private static final long DAY_MSEC = 86400000L;


	/**
	 * 应用部署所占的位数最大为31 (从0开始)
	 */
	private final long maxNodeId = -1L ^ -1L << this.nodeIdBites;

	/**
	 * 应用部署所占的位数最大为31 (从0开始)
	 */
	private final long maxMachineId = -1L ^ -1L << this.machineBits;

	/**
	 * 生成序列的掩码，这里为16384
	 */
	private final long maxSequence = -1L ^ -1L << this.sequenceBits;


	/**
	 * 最大修正位长度
	 * **/
	private final long maxRevise = -1L ^ -1L << this.reviseBits;



	/**
	 * 修正位偏移量
	 * **/
	private final long sequenceOffset = reviseBits + nodeIdBites + machineBits;

	/**
	 * 修正位偏移量
	 * **/
	private final long reviseOffset = nodeIdBites + machineBits;

	/**
	 * 节点偏移量
	 * **/
	private final long nodeOffset = machineBits;


	/**
	 * 机器ID
	 */
	private final long nodeId;

	/**
	 * 机器ID
	 */
	private final long machineId;


	/**
	 * 修正位，防止出现时间倒退的情况 （0~31）
	 */
	private long revise = 0L;

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
	 * **/
	private final Calendar calendar = Calendar.getInstance();


	public SnowFlakeSequence(int nodeId, int machineId){
		if (nodeId > this.maxNodeId || nodeId < 0) {
			String message = String.format("node Id can't be greater than %d or less than 0", this.maxNodeId);
			throw new IllegalArgumentException(message);
		}
		if (machineId > this.maxMachineId || machineId < 0) {
			String message = String.format("machine Id can't be greater than %d or less than 0", this.maxMachineId);
			throw new IllegalArgumentException(message);
		}
		this.nodeId = nodeId;
		this.machineId=machineId;
		long maxInc = maxSequence << sequenceOffset  | maxRevise << reviseOffset | maxNodeId << nodeOffset |  maxMachineId;
		this.formatIncByMill = "%0"+String.valueOf(maxInc).length()+"d";
	}

	private long incByMill(long timestamp){
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
		if(timestamp/DAY_MSEC != lastDate){
			this.lastDate = timestamp/DAY_MSEC;
			this.revise = 0L;
		}
		// 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
		if (timestamp < this.lastTimestamp) {
			String message = String.format("Clock moved backwards. Refusing to generate id for %d milliseconds.", (this.lastTimestamp - timestamp));
			logger.warn(message);
			//执行数据修正
			this.revise();
		}
		this.lastTimestamp = timestamp;
		return sequence << sequenceOffset  | revise << reviseOffset | nodeId << nodeOffset |  machineId;
	}

	private void revise(){

		this.revise = this.revise + 1 & this.maxRevise;
		//一天内修正时间不能如果超过31，超过则报错
		if (this.revise == 0) {
			String message = String.format("Over %d revisions",revise);
			logger.error(message);
			throw new RuntimeException(message);
		}

	}

	/**
	 * 获得ID
	 * @return SnowflakeId
	 */
	@Override
	public synchronized String nextValue() {
		//获取当前时间，到毫秒
		long timestamp = timeGen();
		//追加时间戳
		StringBuilder sb = new StringBuilder();
		sb.append(dateToStr(timestamp));
		sb.append(String.format(formatIncByMill,incByMill(timestamp)));
		//追加每一毫秒不重复的值
		return sb.toString();

	}

	@Override
	public Object[] valueBy(String id) {
		Object[]values = new Object[5];
		try {
			String dateSite = id.substring(0,17);
			Long valueSite = Long.parseLong(id.substring(17));
			values[0] = new SimpleDateFormat(DATE_PATTEN).parse(dateSite).getTime();
			values[1] = (valueSite >> sequenceOffset) & maxSequence;
			values[2] = (valueSite >> reviseOffset) &  maxRevise;
			values[3] = (valueSite >> nodeOffset) & maxNodeId;
			values[4] = valueSite & maxMachineId;
		}catch (ParseException e){
			logger.error("解析日期失败",e);
			throw new RuntimeException(e);
		}catch (Exception e){
			logger.error("解析ID出错",e);
			throw new RuntimeException(e);
		}
		return values;
	}

	private String dateToStr(long dateTime){
		//防止频繁创建
		calendar.setTimeInMillis(dateTime);
		StringBuilder sb = new StringBuilder();
		//追加年
		sb.append(format04d(calendar.get(Calendar.YEAR)));
		//追加月
		sb.append(format02d(calendar.get(Calendar.MONTH)+1));
		//追加日
		sb.append(format02d(calendar.get(Calendar.DATE)));
		//追加时
		sb.append(format02d(calendar.get(Calendar.HOUR_OF_DAY)));
		//追加分
		sb.append(format02d(calendar.get(Calendar.MINUTE)));
		//追加秒
		sb.append(format02d(calendar.get(Calendar.SECOND)));
		//追加毫秒
		sb.append(format02d(calendar.get(Calendar.MILLISECOND)));
		return sb.toString();
	}

	private String format04d(int number){
		return String.format(STRING_FORMAT_04D,number);
	}

	private String format02d(int number){
		return String.format(STRING_FORMAT_02D,number);
	}

	/**
	 * 等待下一个毫秒的到来, 保证返回的毫秒数在参数lastTimestamp之后
	 * @param lastTimestamp 上次生成ID的时间戳
	 * @return
	 */
	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * 获得系统当前毫秒数
	 */
	private long timeGen() {
		return System.currentTimeMillis();
	}


}