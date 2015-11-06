package com.manu.dynasty.util;

import java.util.concurrent.ConcurrentHashMap;

import com.sun.jna.Platform;


/**
 * 开放平台报警
 * 
 * @author Administrator
 * 
 */
public class AlarmTool {
	public static String ALARM_CONNECT = "connect";
	public static String ALARM_DB_CONNECT = "db_conn";
	public static String ALARM_DB_ASYNC = "asyncdb";	
	public static String ALARM_DCAGENT = "dcagent";	
	public static boolean shutdown = false;
	private static String cmd = "/usr/local/services/CloudDCAgent_L5-1.0/alarm/cloud_alarm ";
	private static ConcurrentHashMap<String, Alarm> alarmMap = null;

	static {
		alarmMap = new ConcurrentHashMap<String, Alarm>();
		alarmMap.put(ALARM_CONNECT, new Alarm(60, 10));// 1分钟内10次连接不上则报警
		alarmMap.put(ALARM_DB_CONNECT, new Alarm(60, 10));// 1分钟内10次连接不上则报警

	}

	public static void alarm(String alarmType, String message) {

		if (Constant.APP_ID != null && !shutdown && Platform.isLinux()) {
			Alarm alarm = alarmMap.get(alarmType);
			if (alarm == null) {
				alarm = new Alarm();
				alarm = alarmMap.putIfAbsent(alarmType, alarm);
			}

			alarm.sendAlarm(alarmType, message);

		}
	}

	public static void addAlarmType(String type, Alarm alarm) {
		alarmMap.putIfAbsent(type, alarm);
	}

	public static void removeAlarmType(String type) {
		alarmMap.remove(type);
	}

	static class Alarm {

		/**
		 * 多长时间(秒)内报警达到 alarmNum才开始报警
		 */
		private int alarmPeriod = 300;
		/**
		 * 报警多少次才发送报警数据
		 */
		private int alarmNum = 5;

		/**
		 * 报警时间：秒
		 */
		private long dttm;
		/**
		 * 报警实际次数
		 */
		private int num;

		public Alarm() {

		}

		public Alarm(int alarmPeriod, int alarmNum) {
			this.alarmPeriod = alarmPeriod;
			this.alarmNum = alarmNum;
		}

		public void sendAlarm(String alarmType, String message) {

			long now = System.currentTimeMillis() / 1000L;
			if (dttm == 0) {
				num++;
				dttm = now;
			} else {
				if (now - dttm > alarmPeriod) {
					dttm = now;
					num = 1;
				} else {
					num++;
				}
			}

			if (num >= alarmNum) {
				String cmd1 = cmd + Constant.APP_ID + " " + Constant.AREA_ID
						+ "|" + message + "-g " + alarmType;
				try {
					Runtime.getRuntime().exec(cmd1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				dttm = now;
				num = 0;
			}
		}
	}

//	public static void main(String[] args) {
//		Constant.APP_ID = "100656690";
//		int sendNum = 10;
//		try {
//			sendNum = Integer.parseInt(args[0]);
//		} catch (Exception e) {
//
//		}
//		for (int i = 0; i < sendNum; i++) {
//			AlarmTool.alarm(AlarmTool.ALARM_CONNECT, "王卫华的测试，别慌啊！");
//		}
//	}
}
